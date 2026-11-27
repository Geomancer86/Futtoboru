package com.rndmodgames.futtoboru.match.ecs;

/**
 * Viewer clock (§1.1): turns frame time into a whole number of fixed simulation steps. Game speed multiplies the
 * number of steps, never the step size; leftover time carries to the next frame; a frame runs at most
 * {@code maxStepsPerFrame} steps and drops the backlog beyond that (the viewer slows down instead of stuttering).
 * Headless runs do not use a clock: they call {@link MatchWorld#tick()} in a loop.
 */
public final class FixedStepClock {

    private final double stepSeconds;
    private final int maxStepsPerFrame;
    private double speed = 1.0;
    private double accumulator;

    public FixedStepClock(double stepSeconds, int maxStepsPerFrame) {
        if (!(stepSeconds > 0) || maxStepsPerFrame < 1) {
            throw new IllegalArgumentException("step " + stepSeconds + ", cap " + maxStepsPerFrame);
        }
        this.stepSeconds = stepSeconds;
        this.maxStepsPerFrame = maxStepsPerFrame;
    }

    /** Game speed: 2 = twice as many steps per real second. */
    public void setSpeed(double speed) {
        if (!(speed >= 0)) {
            throw new IllegalArgumentException("speed " + speed);
        }
        this.speed = speed;
    }

    /** Adds one frame's real time and returns how many fixed steps to run now. */
    public int advance(double frameSeconds) {
        if (frameSeconds > 0) {
            accumulator += frameSeconds * speed;
        }
        int steps = (int) (accumulator / stepSeconds);
        if (steps > maxStepsPerFrame) {
            steps = maxStepsPerFrame;
            accumulator = 0; // drop the backlog: slow down rather than spiral
        } else {
            accumulator -= steps * stepSeconds;
        }
        return steps;
    }

    /** Share of a step left over after the last {@link #advance(double)}, for interpolation (0..1). */
    public double alpha() {
        return accumulator / stepSeconds;
    }

    public double stepSeconds() {
        return stepSeconds;
    }
}
