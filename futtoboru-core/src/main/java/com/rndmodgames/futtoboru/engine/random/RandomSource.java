package com.rndmodgames.futtoboru.engine.random;

import java.util.Random;

/**
 * The one seeded random source of a game (P2-0, docs/design/MATCH_ENGINE_V2.md).
 *
 * The game owns a single root source made from a seed ({@code Futtoboru.random()}). Game logic never makes its own
 * {@code java.util.Random}: it asks the root for a named child with {@link #fork(String)}, e.g.
 * {@code fork("match:" + matchId)}. A fork depends only on (parent seed, stream name), never on how many numbers
 * were drawn before or on the order in which forks are taken, so a match result depends only on the game seed, the
 * match id and its input.
 *
 * Instances are not thread-safe; fork one per thread or task.
 */
public interface RandomSource {

    /** The seed this source was made from; {@code seeded(getSeed())} replays the same sequence. */
    long getSeed();

    /** Uniform in [0, 1). */
    double nextDouble();

    /** Uniform in [0, 1). */
    float nextFloat();

    /** Uniform in [0, bound); bound must be positive. */
    int nextInt(int bound);

    /** Uniform in [origin, bound); origin must be lower than bound. */
    int nextInt(int origin, int bound);

    long nextLong();

    boolean nextBoolean();

    /** Standard normal (mean 0, standard deviation 1). */
    double nextGaussian();

    /**
     * A deterministic, independent child stream named {@code stream}. Forking the same name twice gives two
     * sources with the same sequence, so names must be unique per use (put the entity id and date in them).
     */
    RandomSource fork(String stream);

    /**
     * A {@link java.util.Random} view drawing from this source, for APIs that need one. {@code setSeed} on the view
     * is ignored.
     */
    Random asRandom();

    /** The default implementation (SplittableRandom inside). */
    static RandomSource seeded(long seed) {
        return new SplittableRandomSource(seed);
    }

    /** A fresh seed for a new game (not reproducible by design: it is the one place entropy enters). */
    static long newSeed() {
        return SplittableRandomSource.mix64(System.nanoTime() ^ Double.doubleToLongBits(Math.random()));
    }
}
