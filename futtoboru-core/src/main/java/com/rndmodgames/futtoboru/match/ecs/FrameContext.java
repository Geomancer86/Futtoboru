package com.rndmodgames.futtoboru.match.ecs;

/**
 * What a {@link DrawPass} knows about the frame it draws (E-21a, docs/design/MATCH_VIEWER_PLAN.md §2.1). The viewer
 * fills one value per frame ({@code match.ecs.view.ViewFrame}); {@link MatchWorld#draw(double)} uses a plain one.
 * Simulation systems never see it: they only know the fixed tick.
 */
public interface FrameContext {

    /** Share of a tick elapsed since the last simulated tick (0..1), for interpolation. */
    double alpha();

    /** Real time of this frame in seconds (0 when the caller has none); for particles, flicker, camera springs. */
    double frameSeconds();

    /** Real seconds the viewer has run, paused time excluded; cosmetic animation clock. */
    double viewSeconds();

    /** Simulation ticks run when this frame was drawn. */
    long tick();
}
