package com.rndmodgames.futtoboru.match.ecs;

/**
 * What the ball system needs to know about the ground and the weather at the current tick. This is the minimal
 * input the ball (E-11) reads; the pitch lane (E-12, {@code match.ecs.pitch}) and the weather fork may implement it
 * with per-patch surfaces and a rain schedule. Implementations must be deterministic functions of the match input
 * and the simulation state (never of wall time or the viewer).
 */
public interface PitchConditions {

    /** Wetness per minute (effective submersion), BALL_AND_KIT_PHYSICS.md §1.3: dry. */
    double DRY = 0.0;
    /** Damp or dew. */
    double DAMP = 0.05;
    /** Drizzle. */
    double DRIZZLE = 0.15;
    /** Rain or a wet pitch: +19 % ball mass at 45 min (FA test 1953). */
    double RAIN = 0.36;
    /** Heavy rain, waterlogged or mud: +47 % at 90 min (press report 1909). */
    double WATERLOGGED = 0.8;

    /** Surface under the ball at (x, y), metres from the centre spot. */
    Surface surfaceAt(double x, double y);

    /** Wetness 0..1 at the current tick: share of full submersion the ball is exposed to (scale above). */
    double wetness();
}
