package com.rndmodgames.futtoboru.match.ecs.pitch;

/** The two goal ends: WEST at {@code x = -length/2}, EAST at {@code x = +length/2}. */
public enum PitchEnd {
    WEST(-1), EAST(1);

    private final int sign;

    PitchEnd(int sign) {
        this.sign = sign;
    }

    /** -1 for WEST, +1 for EAST. */
    public int sign() {
        return sign;
    }

    public PitchEnd opposite() {
        return this == WEST ? EAST : WEST;
    }

    /** The end on the side of {@code x}; {@code x == 0} counts as EAST (deterministic tie-break). */
    public static PitchEnd ofX(double x) {
        return x < 0 ? WEST : EAST;
    }
}
