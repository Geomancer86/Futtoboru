package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * Which side defends which goal. Ends change at half-time only (since 1875, V2 §1).
 *
 * @param westDefender the side defending the WEST goal (attacking EAST)
 */
public record MatchEnds(Team westDefender) {

    public MatchEnds {
        if (westDefender == null) {
            throw new IllegalArgumentException("westDefender");
        }
    }

    public Team defenderOf(PitchEnd end) {
        return end == PitchEnd.WEST ? westDefender : westDefender.other();
    }

    public Team attackerOf(PitchEnd end) {
        return defenderOf(end).other();
    }

    public PitchEnd endDefendedBy(Team team) {
        return team == westDefender ? PitchEnd.WEST : PitchEnd.EAST;
    }

    /** The ends for the second half. */
    public MatchEnds changed() {
        return new MatchEnds(westDefender.other());
    }
}
