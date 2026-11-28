package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.function.LongSupplier;

import com.rndmodgames.futtoboru.match.ecs.FixedStepClock;

/**
 * Time-budgeted viewer clock on top of {@link FixedStepClock} (docs/design/MATCH_VIEWER_PLAN.md §1; the budget idea
 * follows DarkBlade's simulation clock). Per frame it runs the steps that are due, but
 *
 * <ul>
 * <li>at most {@code maxStepsPerFrame} steps, and</li>
 * <li>only while the frame's simulation time stays under {@code budgetSeconds} (at least one step always runs);</li>
 * <li>steps not run are carried to the next frame, not dropped; the backlog is capped at {@code maxBacklogSteps}
 * (beyond that {@link FixedStepClock} drops it), so a slow machine slows the match, not the frame rate.</li>
 * </ul>
 *
 * The wall clock is injected (tests use a fake one). Game speed multiplies the steps, never the step size.
 */
public final class ViewerClock {

    public static final int DEFAULT_MAX_STEPS_PER_FRAME = 16;
    public static final int DEFAULT_MAX_BACKLOG_STEPS = 60;
    public static final double DEFAULT_BUDGET_SECONDS = 0.008;

    private final FixedStepClock clock;
    private final int maxStepsPerFrame;
    private final long budgetNanos;
    private final LongSupplier nanoTime;
    private int lastSteps;
    private int lastCarried;

    /** Defaults: 16 steps per frame, 2 s of backlog at 30 Hz, 8 ms of simulation per frame, the system clock. */
    public ViewerClock(double stepSeconds) {
        this(stepSeconds, DEFAULT_MAX_STEPS_PER_FRAME, DEFAULT_MAX_BACKLOG_STEPS, DEFAULT_BUDGET_SECONDS,
                System::nanoTime);
    }

    public ViewerClock(double stepSeconds, int maxStepsPerFrame, int maxBacklogSteps, double budgetSeconds,
            LongSupplier nanoTime) {
        if (maxStepsPerFrame < 1 || maxBacklogSteps < maxStepsPerFrame || !(budgetSeconds > 0) || nanoTime == null) {
            throw new IllegalArgumentException("steps/frame " + maxStepsPerFrame + ", backlog " + maxBacklogSteps
                    + ", budget " + budgetSeconds);
        }
        this.clock = new FixedStepClock(stepSeconds, maxBacklogSteps);
        this.maxStepsPerFrame = maxStepsPerFrame;
        this.budgetNanos = (long) (budgetSeconds * 1e9);
        this.nanoTime = nanoTime;
    }

    /**
     * Adds one frame's real time and runs the due steps within the caps.
     *
     * @param step one simulation step (tick + whatever the viewer records per tick); reuse one instance
     * @return steps run this frame
     */
    public int runFrame(double frameSeconds, Runnable step) {
        int due = clock.advance(frameSeconds);
        int run = 0;
        long start = nanoTime.getAsLong();
        while (run < due && run < maxStepsPerFrame) {
            step.run();
            run++;
            if (nanoTime.getAsLong() - start >= budgetNanos) {
                break;
            }
        }
        lastCarried = due - run;
        clock.carry(lastCarried);
        lastSteps = run;
        return run;
    }

    /** Share of a step left for interpolation (0..1; 1 while a backlog is carried). */
    public double alpha() {
        return clock.alpha();
    }

    public void setSpeed(double speed) {
        clock.setSpeed(speed);
    }

    public double speed() {
        return clock.speed();
    }

    public double stepSeconds() {
        return clock.stepSeconds();
    }

    /** Steps run by the last frame. */
    public int lastSteps() {
        return lastSteps;
    }

    /** Steps the last frame carried to the next one (cap or budget). */
    public int lastCarried() {
        return lastCarried;
    }
}
