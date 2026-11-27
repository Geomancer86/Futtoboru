package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * The ball going wholly over a boundary line (the ball centre one radius beyond it) within a substep.
 *
 * @param line         which kind of line
 * @param end          for a goal line, the goal end; for a touchline, the half the ball left in
 * @param flank        the side of the pitch ({@code y} sign) where it went out
 * @param t            fraction (0..1) of the substep when the ball is wholly over
 * @param ballCentre   the ball centre at that moment
 * @param planeY       goal line only: {@code y} of the ball centre where its path crosses the goal-frame plane
 * @param planeZ       goal line only: {@code z} of the ball centre at that plane
 * @param betweenPosts goal line only: the ball passed between the posts and under the bar (a goal in geometry)
 * @param frameGap     goal line only: clearance between the ball surface and the nearest part of the frame (metres)
 * @param disputed     goal line only: {@code frameGap} below the disputed margin and no nets (D19-5), so the umpires
 *                     must decide on appeal (E-16)
 */
public record LineCrossing(Line line, PitchEnd end, Flank flank, double t, Vec3 ballCentre, double planeY,
        double planeZ, boolean betweenPosts, double frameGap, boolean disputed) {

    /** Boundary lines. */
    public enum Line {
        GOAL_LINE, TOUCHLINE
    }
}
