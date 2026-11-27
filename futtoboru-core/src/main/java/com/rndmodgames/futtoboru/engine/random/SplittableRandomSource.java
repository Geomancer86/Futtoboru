package com.rndmodgames.futtoboru.engine.random;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.SplittableRandom;

/**
 * Default {@link RandomSource} over {@link SplittableRandom}.
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

    private final long seed;
    private final SplittableRandom random;

    public SplittableRandomSource(long seed) {
        this.seed = seed;
        this.random = new SplittableRandom(seed);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public int nextInt(int origin, int bound) {
        return random.nextInt(origin, bound);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    @Override
    public double nextGaussian() {
        return random.nextGaussian();
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
        public synchronized void setSeed(long seed) {
            // ignored: the sequence belongs to the RandomSource (Random's constructor calls this before 'source' is set)
        }
    }
}
