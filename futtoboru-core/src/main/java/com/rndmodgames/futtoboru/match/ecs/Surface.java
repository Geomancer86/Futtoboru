package com.rndmodgames.futtoboru.match.ecs;

/**
 * State of the playing surface, which sets bounce and roll ({@link PhysicsParams.SurfaceParams}). Mapping to
 * docs/data/history/BALL_AND_KIT_PHYSICS.md §1.4 / §3: DRY = firm turf, WET = heavy soft pitch, MUDDY = mud /
 * standing water, FROZEN = hard or frozen ground.
 *
 * Shared with the pitch lane (E-12, {@code match.ecs.pitch}): it may later return a surface per patch through
 * {@link PitchConditions}; the enum itself stays here.
 */
public enum Surface {
    DRY,
    WET,
    MUDDY,
    FROZEN
}
