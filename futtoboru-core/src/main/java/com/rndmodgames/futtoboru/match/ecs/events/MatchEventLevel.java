package com.rndmodgames.futtoboru.match.ecs.events;

/**
 * Filter levels of the match screen and the report (D19-9, FM-style): {@link #KEY} events are shown at every level,
 * {@link #EXTENDED} ones at extended and full, {@link #FULL} ones only at full.
 */
public enum MatchEventLevel {
    /** Goals, disputed goals, injuries that hurt a side, lace cuts, sendings-off, kick-off, half-time, full time. */
    KEY,
    /** Shots, saves, appeals upheld, knocks, the ball grown heavy. */
    EXTENDED,
    /** Everything: appeals refused, kick-offs after goals. */
    FULL;

    /** True when an event of level {@code event} is shown under this filter. */
    public boolean shows(MatchEventLevel event) {
        return event.ordinal() <= ordinal();
    }
}
