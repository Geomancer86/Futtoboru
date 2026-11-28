package com.rndmodgames.futtoboru.match.ecs.body;

/**
 * Boot sole (BALL_AND_KIT_PHYSICS.md §2, ERA_TIMELINE 1887-88): from 1888 the Laws allow leather bars (flat, at
 * least 1.5 x 0.5 in) or round leather studs (at least 0.5 in across), neither projecting more than 0.5 in. Grip per
 * sole and surface: {@link BodyParams.SurfaceBody#soleGrip}.
 */
public enum Studs {
    /** Plain sole (no bars or studs). */
    FLAT,
    /** Leather bars across the sole: the common 1888 boot (D). */
    BARS,
    /** Round leather studs. */
    STUDS
}
