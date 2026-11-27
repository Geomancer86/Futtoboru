package com.rndmodgames.futtoboru.match.ecs;

/**
 * Hash of the simulation state (§1.1, §2.1): FNV-1a over the exact bits of every hashed value, in entity creation
 * order and {@link MatchSystems#HASHED_COMPONENTS} order. Same input, same seed, same engine version: same hash
 * sequence on every machine.
 *
 * Describe mode ({@link #begin(StringBuilder)} with a builder) also writes one line per value
 * ({@code label=value bits}), so two diverging runs can be diffed. Without a builder it allocates nothing.
 */
public final class StateHash {

    private static final long OFFSET = 0xcbf29ce484222325L;
    private static final long PRIME = 0x100000001b3L;

    private long value = OFFSET;
    private StringBuilder describe;

    /** Starts a new hash; {@code describeTo} null for the plain, allocation-free mode. */
    public void begin(StringBuilder describeTo) {
        value = OFFSET;
        describe = describeTo;
    }

    public void mix(String label, double v) {
        long bits = Double.doubleToLongBits(v);
        mixBits(bits);
        if (describe != null) {
            describe.append(label).append('=').append(v).append(" #").append(Long.toHexString(bits)).append('\n');
        }
    }

    public void mix(String label, long v) {
        mixBits(v);
        if (describe != null) {
            describe.append(label).append('=').append(v).append('\n');
        }
    }

    /** The hash so far. */
    public long value() {
        return value;
    }

    private void mixBits(long bits) {
        // Byte-wise FNV-1a: every bit of the value reaches the result.
        for (int i = 0; i < 64; i += 8) {
            value ^= (bits >>> i) & 0xff;
            value *= PRIME;
        }
    }
}
