package com.rndmodgames.futtoboru.engine.random;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Default {@link RandomSource}: SplitMix64, implemented here so no seeded result depends on the JDK (E-DET).
 *
 * The core is bit-for-bit the algorithm of {@code java.util.SplittableRandom(seed)} as of JDK 17 ({@code nextLong},
 * {@code nextInt}, {@code nextInt(bound)}, {@code nextInt(origin, bound)}, {@code nextDouble}, {@code nextFloat},
 * {@code nextBoolean}), so seeded sequences did not move when it was taken in-house. {@link #nextGaussian()} is our
 * own Marsaglia polar method (see there): the JDK's {@code SplittableRandom.nextGaussian} is unspecified and gives
 * different numbers on JDK 17 and JDK 25. Nothing here may call into a JDK generator or a CPU-dependent
 * {@code Math} function; {@code RandomSourceTests} pins the sequences for a known seed.
 *
 * Fork seeds: {@code child = mix64(mix64(seed) ^ fnv1a64(utf8(stream)))}.
 * <ul>
 * <li>The stream name is hashed with 64-bit FNV-1a over its UTF-8 bytes, not {@code String.hashCode()}: it is
 * fully specified, independent of the JVM, and 64 bits wide, so "match:12" and "match:21" do not collide the way
 * 32-bit hashes of similar strings easily do.</li>
 * <li>The child is derived from the parent's <em>seed</em>, not from its current state, so taking forks or drawing
 * numbers in another order never changes a fork's sequence (needed for saves: a restored game re-derives the same
 * streams from the saved seed).</li>
 * <li>{@code mix64} is the SplitMix64 finalizer (Stafford variant 13), the same mixer SplittableRandom uses.</li>
 * </ul>
 * The derivation is part of the save contract: changing it changes every seeded game.
 */
public final class SplittableRandomSource implements RandomSource {

    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    /** SplitMix64 increment (the golden gamma of {@code SplittableRandom(long)}). */
    private static final long GOLDEN_GAMMA = 0x9e3779b97f4a7c15L;

    private final long seed;
    /** The SplitMix64 state, advanced by {@link #GOLDEN_GAMMA} per 64-bit draw. */
    private long state;

    public SplittableRandomSource(long seed) {
        this.seed = seed;
        this.state = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    @Override
    public float nextFloat() {
        return (nextInt32() >>> 8) * 0x1.0p-24f;
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        int m = bound - 1;
        int r = nextInt32();
        if ((bound & m) == 0) {
            return r & m; // power of two
        }
        for (int u = r >>> 1; u + m - (r = u % bound) < 0; u = nextInt32() >>> 1) {
            // reject over-represented candidates
        }
        return r;
    }

    @Override
    public int nextInt(int origin, int bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("bound must be greater than origin");
        }
        int r = nextInt32();
        int n = bound - origin;
        int m = n - 1;
        if ((n & m) == 0) {
            return (r & m) + origin;
        }
        if (n > 0) {
            for (int u = r >>> 1; u + m - (r = u % n) < 0; u = nextInt32() >>> 1) {
                // reject over-represented candidates
            }
            return r + origin;
        }
        while (r < origin || r >= bound) { // range wider than int
            r = nextInt32();
        }
        return r;
    }

    @Override
    public long nextLong() {
        return mix64(state += GOLDEN_GAMMA);
    }

    @Override
    public boolean nextBoolean() {
        return nextInt32() < 0;
    }

    /**
     * Standard normal by the Marsaglia polar method: draw (u, v) uniform in the square [-1, 1)^2 until
     * {@code 0 < s = u*u + v*v < 1}, return {@code u * sqrt(-2 ln(s) / s)}. Uses only {@link #nextDouble()},
     * IEEE arithmetic (strict since JDK 17), {@code StrictMath.log} and {@code StrictMath.sqrt}, so the sequence is
     * identical on every JDK and CPU. Stateless: the second normal of each pair ({@code v * ...}) is discarded rather
     * than cached, so a source's state is only its SplitMix64 counter and every call costs one accepted pair
     * (about 2.55 uniforms on average).
     */
    @Override
    public double nextGaussian() {
        double u;
        double v;
        double s;
        do {
            u = 2 * nextDouble() - 1;
            v = 2 * nextDouble() - 1;
            s = u * u + v * v;
        } while (s >= 1 || s == 0);
        return u * StrictMath.sqrt(-2 * StrictMath.log(s) / s);
    }

    /** 32 random bits: SplittableRandom's {@code nextInt()} (high 32 bits of Stafford variant 4). */
    private int nextInt32() {
        long z = state += GOLDEN_GAMMA;
        z = (z ^ (z >>> 33)) * 0x62a9d9ed799705f5L;
        return (int) (((z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32);
    }

    @Override
    public RandomSource fork(String stream) {
        return new SplittableRandomSource(forkSeed(seed, stream));
    }

    @Override
    public Random asRandom() {
        return new RandomView(this);
    }

    @Override
    public String toString() {
        return "SplittableRandomSource[seed=" + seed + "]";
    }

    /** The seed of {@code fork(stream)} of a source seeded with {@code parentSeed}. */
    public static long forkSeed(long parentSeed, String stream) {
        if (stream == null) {
            throw new IllegalArgumentException("stream name is required");
        }
        return mix64(mix64(parentSeed) ^ fnv1a64(stream));
    }

    static long fnv1a64(String text) {
        long hash = FNV_OFFSET;
        for (byte b : text.getBytes(StandardCharsets.UTF_8)) {
            hash ^= (b & 0xff);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    /** java.util.Random drawing from a RandomSource (every Random method is built on next(bits)). */
    private static final class RandomView extends Random {
        private static final long serialVersionUID = 1L;
        private final transient RandomSource source;

        RandomView(RandomSource source) {
            super(0L);
            this.source = source;
        }

        @Override
        protected int next(int bits) {
            return (int) (source.nextLong() >>> (64 - bits));
        }

        @Override
        public double nextGaussian() {
            return source.nextGaussian(); // never Random's own (stateful) Gaussian
        }

        @Override
        public synchronized void setSeed(long seed) {
            // ignored: the sequence belongs to the RandomSource (Random's constructor calls this before 'source' is set)
        }
    }
}
