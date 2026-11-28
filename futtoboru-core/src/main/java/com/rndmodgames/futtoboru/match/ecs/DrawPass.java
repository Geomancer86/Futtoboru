package com.rndmodgames.futtoboru.match.ecs;

/**
 * A viewer pass (§1.1): an Ashley {@code EntitySystem} that implements this interface is never updated by the
 * simulation loop; {@link MatchWorld#draw(FrameContext)} calls {@link #draw(FrameContext)} once per frame instead. A
 * draw pass reads simulation components and never writes them; {@link MatchWorld#setDrawGuard(boolean)} proves it by
 * hashing the simulation state around every pass. Cosmetic randomness must come from its own source, never the match
 * fork.
 *
 * A pass overrides {@link #draw(FrameContext)} (real frame time, alpha, view clock) or, when it only needs the
 * interpolation share, {@link #draw(double)}.
 */
public interface DrawPass {

    /**
     * Draws one frame. The default hands the interpolation share to {@link #draw(double)}.
     *
     * @param frame this frame's alpha, real frame delta and view clock; valid only during the call
     */
    default void draw(FrameContext frame) {
        draw(frame.alpha());
    }

    /**
     * Draws one frame (passes that need only the interpolation share).
     *
     * @param alpha share of a tick elapsed since the last simulated tick (0..1), for interpolation
     */
    default void draw(double alpha) {
    }
}
