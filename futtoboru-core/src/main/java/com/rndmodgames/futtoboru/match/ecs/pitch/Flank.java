package com.rndmodgames.futtoboru.match.ecs.pitch;

/** The two touchlines: SOUTH at {@code y = -width/2}, NORTH at {@code y = +width/2}. */
public enum Flank {
    SOUTH(-1), NORTH(1);

    private final int sign;

    Flank(int sign) {
        this.sign = sign;
    }

    /** -1 for SOUTH, +1 for NORTH. */
    public int sign() {
        return sign;
    }

    /** The flank on the side of {@code y}; {@code y == 0} counts as NORTH (deterministic tie-break). */
    public static Flank ofY(double y) {
        return y < 0 ? SOUTH : NORTH;
    }
}
