package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.Entity;

/**
 * View-only position of one drawn entity (docs/design/MATCH_VIEWER_PLAN.md §2.3): the simulation position at the last
 * two ticks and, after {@link InterpolationPass} ran this frame, their blend by the frame's alpha in floats. Every
 * other draw pass reads {@link #x}, {@link #y}, {@link #z} instead of the simulation components.
 *
 * Owned by {@link InterpolationPass}, not an Ashley component: drawing never adds anything to a simulation entity.
 */
public final class ViewTransform {

    public final Entity entity;

    /** Simulation position at the previous captured tick. */
    double prevX, prevY, prevZ;
    /** Simulation position at the last captured tick. */
    double currX, currY, currZ;
    /** The tick of {@code curr*}; -1 before the first capture. */
    long tick = -1;

    /** Interpolated position for this frame, metres. */
    public float x, y, z;

    ViewTransform(Entity entity) {
        this.entity = entity;
    }

    public double prevX() {
        return prevX;
    }

    public double prevY() {
        return prevY;
    }

    public double prevZ() {
        return prevZ;
    }

    public double currX() {
        return currX;
    }

    public double currY() {
        return currY;
    }

    public double currZ() {
        return currZ;
    }

    /** The simulation tick of the current position. */
    public long tick() {
        return tick;
    }
}
