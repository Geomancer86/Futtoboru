package com.rndmodgames.futtoboru.engine.random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

/** P2-0: the seeded random source and its forks. */
class RandomSourceTests {

    private static long[] draw(RandomSource rng, int n) {
        long[] out = new long[n];
        for (int i = 0; i < n; i++) {
            out[i] = rng.nextLong();
        }
        return out;
    }

    @Test
    void sameSeedGivesTheSameSequence() {
        RandomSource a = RandomSource.seeded(1888);
        RandomSource b = RandomSource.seeded(1888);
        for (int i = 0; i < 100; i++) {
            assertEquals(a.nextInt(1000), b.nextInt(1000));
            assertEquals(a.nextDouble(), b.nextDouble());
            assertEquals(a.nextGaussian(), b.nextGaussian());
            assertEquals(a.nextBoolean(), b.nextBoolean());
            assertEquals(a.nextInt(5, 50), b.nextInt(5, 50));
        }
        assertEquals(1888, a.getSeed());
    }

    @Test
    void differentSeedsDiffer() {
        assertFalse(java.util.Arrays.equals(draw(RandomSource.seeded(1), 20), draw(RandomSource.seeded(2), 20)));
    }

    @Test
    void aForkDoesNotDependOnForkOrderOrOnDrawsFromTheParent() {
        RandomSource first = RandomSource.seeded(42);
        long[] matchFirst = draw(first.fork("match:7"), 30);

        RandomSource second = RandomSource.seeded(42);
        draw(second, 17);                       // the parent has been used
        draw(second.fork("match:8"), 5);        // other forks were taken before
        draw(second.fork("gate:7"), 5);
        long[] matchLater = draw(second.fork("match:7"), 30);

        assertArrayEquals(matchFirst, matchLater);
    }

    @Test
    void forksWithDifferentNamesOrParentsAreIndependent() {
        RandomSource root = RandomSource.seeded(42);
        assertFalse(java.util.Arrays.equals(draw(root.fork("match:12"), 20), draw(root.fork("match:21"), 20)));
        assertFalse(java.util.Arrays.equals(draw(root.fork("match:1"), 20), draw(root, 20)));
        assertFalse(java.util.Arrays.equals(draw(RandomSource.seeded(43).fork("match:1"), 20),
                draw(RandomSource.seeded(42).fork("match:1"), 20)));
        // nested forks are deterministic too
        assertArrayEquals(draw(RandomSource.seeded(9).fork("a").fork("b"), 10),
                draw(RandomSource.seeded(9).fork("a").fork("b"), 10));
        assertNotEquals(root.fork("a").fork("b").getSeed(), root.fork("b").fork("a").getSeed());
    }

    /** The fork derivation is part of the save contract (a saved seed must replay the same game). */
    @Test
    void forkDerivationIsPinned() {
        assertEquals(0xcbf29ce484222325L, SplittableRandomSource.fnv1a64(""));
        assertEquals(0xaf63dc4c8601ec8cL, SplittableRandomSource.fnv1a64("a"));
        long child = SplittableRandomSource.forkSeed(1888L, "match:1");
        assertEquals(0x9343b4bae782e10aL, child);
        assertEquals(372, RandomSource.seeded(1888L).fork("match:1").nextInt(1000));
        assertEquals(child, RandomSource.seeded(1888L).fork("match:1").getSeed());
        assertEquals(SplittableRandomSource.mix64(SplittableRandomSource.mix64(1888L)
                ^ SplittableRandomSource.fnv1a64("match:1")), child);
    }

    @Test
    void javaRandomViewDrawsFromTheSource() {
        Random a = RandomSource.seeded(5).fork("view").asRandom();
        Random b = RandomSource.seeded(5).fork("view").asRandom();
        for (int i = 0; i < 50; i++) {
            assertEquals(a.nextInt(28), b.nextInt(28));
            assertEquals(a.nextDouble(), b.nextDouble());
        }
        a.setSeed(1); // ignored
        int x = a.nextInt(100);
        assertTrue(x >= 0 && x < 100);
    }

    /**
     * E-DET: the Gaussian is our own Marsaglia polar method, so this sequence is the same on every JDK (the JDK's
     * {@code SplittableRandom.nextGaussian} gave different numbers on JDK 17 and JDK 25). Bits, not tolerances: a
     * change here changes every seeded match and must bump {@code EcsMatchEngine.ENGINE_VERSION}.
     */
    @Test
    void gaussianSequenceIsPinned() {
        long[] expected = {0x3fef210e20c1da9dL, 0x3ff172476ea2dae6L, 0x3ffda5f1656f1750L, 0xbfd17767d3307612L,
            0xbfff58cf6db410bdL, 0xbffe5f1d972766e1L, 0x4001a69e2cc91dbdL, 0x3ffa9224e9148f05L};
        RandomSource rng = RandomSource.seeded(1888L);
        for (int i = 0; i < expected.length; i++) {
            double g = rng.nextGaussian();
            assertEquals(expected[i], Double.doubleToLongBits(g), "gaussian " + i + " = " + g);
        }
        assertEquals(0.9727850570478328, RandomSource.seeded(1888L).nextGaussian());
        // the java.util.Random view uses the same Gaussian, not Random's own
        assertEquals(0.9727850570478328, RandomSource.seeded(1888L).asRandom().nextGaussian());
    }

    @Test
    void gaussianIsStandardNormal() {
        RandomSource rng = RandomSource.seeded(8);
        int n = 200_000;
        double sum = 0;
        double sumSq = 0;
        for (int i = 0; i < n; i++) {
            double g = rng.nextGaussian();
            sum += g;
            sumSq += g * g;
        }
        double mean = sum / n;
        assertEquals(0, mean, 0.01);
        assertEquals(1, sumSq / n - mean * mean, 0.015);
    }

    /**
     * The in-house SplitMix64 core reproduces {@code SplittableRandom(seed)} (the generator before E-DET), so taking
     * it in-house moved no seeded sequence except the Gaussian. The pinned values guard it on any JDK.
     */
    @Test
    void coreMatchesSplittableRandomAndIsPinned() {
        for (long seed : new long[] {0L, 1888L, -7L, Long.MAX_VALUE}) {
            RandomSource ours = RandomSource.seeded(seed);
            SplittableRandom jdk = new SplittableRandom(seed);
            for (int i = 0; i < 2000; i++) {
                assertEquals(jdk.nextLong(), ours.nextLong());
                assertEquals(jdk.nextInt(1000), ours.nextInt(1000));
                assertEquals(jdk.nextInt(64), ours.nextInt(64));
                assertEquals(jdk.nextInt(-5, 77), ours.nextInt(-5, 77));
                assertEquals(jdk.nextInt(Integer.MIN_VALUE + 5, Integer.MAX_VALUE),
                        ours.nextInt(Integer.MIN_VALUE + 5, Integer.MAX_VALUE));
                assertEquals(jdk.nextDouble(), ours.nextDouble());
                assertEquals(jdk.nextFloat(), ours.nextFloat());
                assertEquals(jdk.nextBoolean(), ours.nextBoolean());
            }
        }
        RandomSource rng = RandomSource.seeded(1888L);
        assertEquals(0xa2323cedbcabe188L, rng.nextLong());
        assertEquals(857, rng.nextInt(1000));
        assertEquals(0.6294220106583537, rng.nextDouble());
    }
}
