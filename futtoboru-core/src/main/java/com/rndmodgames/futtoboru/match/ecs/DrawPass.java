package com.rndmodgames.futtoboru.match.ecs;

/**
 * A viewer pass (§1.1): an Ashley {@code EntitySystem} that implements this interface is never updated by the
 * simulation loop; {@link MatchWorld#draw(double)} calls {@link #draw(double)} once per frame instead. A draw pass
 * reads simulation components and never writes them; {@link MatchWorld#setDrawGuard(boolean)} proves it by hashing
 * the simulation state around every pass. Cosmetic randomness must come from its own source, never the match fork.
 */
public interface DrawPass {

    /**
     * Draws one frame.
     *
     * @param alpha share of a tick elapsed since the last simulated tick (0..1), for interpolation
     */
    void draw(double alpha);
}
