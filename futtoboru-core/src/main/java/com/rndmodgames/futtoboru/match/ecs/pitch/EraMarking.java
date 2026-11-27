package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * Pitch markings that came after 1888-89 (docs/data/history/ERA_TIMELINE.md). None exists in 1888-89: only the
 * boundary lines, goals and corner flags (V2 §1). They switch on by season in {@link PitchParams#forSeason}.
 */
public enum EraMarking {
    /** June 1891 (IFAB): a line across the pitch 12 yd from each goal line; penalty for fouls inside it. */
    PENALTY_LINE_1891,
    /** June 1891: goal area as two 6 yd quarter-circles drawn from each post. */
    GOAL_AREA_ARCS_1891,
    /** June 1891: centre spot and a 10 yd centre circle. */
    CENTRE_CIRCLE_1891,
    /** 1902 (IFAB): the 18 yd penalty area (18 x 44 yd), 6 yd goal area box (6 x 20 yd), penalty spot at 12 yd. */
    PENALTY_AREA_1902
}
