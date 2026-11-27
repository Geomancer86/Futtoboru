package com.rndmodgames.futtoboru.match.ecs.pitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

/** E-12: geometry, swept crossings, frame contacts, disputed-goal band, era switches, determinism. */
class PitchGeometryTest {

    private static final double R = 0.11;
    private static final double EPS = 1e-9;

    private final PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
    private final double halfL = pitch.halfLength();

    /** A shot along +x towards the EAST goal at constant (y, z). */
    private SweepResult shotEast(PitchGeometry p, double y, double z) {
        return p.sweep(BallSegment.of(p.halfLength() - 5, y, z, p.halfLength() + 2, y, z, R));
    }

    // ---- geometry sanity ----

    @Test
    void defaultGroundIs110By70YardsWith8YardBy8FootGoals() {
        assertEquals(110 * 0.9144, pitch.length(), EPS);
        assertEquals(70 * 0.9144, pitch.width(), EPS);
        assertEquals(4 * 0.9144, pitch.goalMouthHalfWidth(), EPS);
        assertEquals(8 * 0.3048, pitch.crossbarUndersideZ(), EPS);
        assertEquals(pitch.goalMouthHalfWidth() + pitch.params().postRadiusM(), pitch.postCentreY(), EPS);
        assertEquals(pitch.crossbarUndersideZ() + pitch.params().crossbarRadiusM(), pitch.crossbarCentreZ(), EPS);
        assertEquals(new Vec3(0, 0, 0), pitch.centre());
        assertEquals(0, pitch.halfwayX());
        assertEquals(halfL, pitch.goalLineX(PitchEnd.EAST), EPS);
        assertEquals(-halfL, pitch.goalLineX(PitchEnd.WEST), EPS);
        assertEquals(-pitch.halfWidth(), pitch.touchlineY(Flank.SOUTH), EPS);
        assertEquals(new Vec3(-halfL, pitch.halfWidth(), 0), pitch.cornerFlag(PitchEnd.WEST, Flank.NORTH));
        assertEquals(new Vec3(halfL, -pitch.postCentreY(), 0), pitch.postBase(PitchEnd.EAST, Flank.SOUTH));
        assertTrue(pitch.inPlay(new Vec3(halfL + R, pitch.halfWidth() + R, 0), R), "on the line is in play");
        assertFalse(pitch.inPlay(new Vec3(halfL + R + 1e-6, 0, 0), R));
        assertFalse(pitch.params().nets(), "no nets in 1888-89");
        assertTrue(pitch.params().markings().isEmpty(), "no era markings in 1888-89");
    }

    @Test
    void lawsOf1888LimitThePitchSize() {
        PitchParams.laws1888(100, 50);
        PitchParams.laws1888(200, 100);
        assertThrows(IllegalArgumentException.class, () -> PitchParams.laws1888(99, 70));
        assertThrows(IllegalArgumentException.class, () -> PitchParams.laws1888(201, 70));
        assertThrows(IllegalArgumentException.class, () -> PitchParams.laws1888(110, 49));
        assertThrows(IllegalArgumentException.class, () -> PitchParams.laws1888(110, 101));
        assertEquals(PitchParams.defaults1888(), PitchParams.forSeason(1888, 110, 70));
    }

    // ---- crossings ----

    @Test
    void ballThroughThePostsUnderTheBarIsAGoalWhenWhollyOver() {
        SweepResult s = shotEast(pitch, 0, 1);
        assertTrue(s.hasCrossing());
        assertFalse(s.hasContact());
        LineCrossing c = s.crossing();
        assertEquals(LineCrossing.Line.GOAL_LINE, c.line());
        assertEquals(PitchEnd.EAST, c.end());
        assertTrue(c.betweenPosts());
        assertFalse(c.disputed());
        assertEquals(halfL + R, c.ballCentre().x(), 1e-9, "out only when the whole ball is over");
        assertEquals(0, c.planeY(), EPS);
        assertEquals(1, c.planeZ(), EPS);
    }

    @Test
    void wideAndOverAreNotGoals() {
        LineCrossing wide = shotEast(pitch, 6, 1).crossing();
        assertFalse(wide.betweenPosts());
        assertFalse(wide.disputed());
        assertEquals(Flank.NORTH, wide.flank());
        LineCrossing over = shotEast(pitch, 0, 3.5).crossing();
        assertFalse(over.betweenPosts());
        assertFalse(over.disputed());
        LineCrossing west = pitch.sweep(BallSegment.of(-halfL + 3, -10, R, -halfL - 3, -10, R, R)).crossing();
        assertEquals(PitchEnd.WEST, west.end());
        assertEquals(Flank.SOUTH, west.flank());
        assertFalse(west.betweenPosts());
    }

    @Test
    void touchlineCrossingIsWhollyOverAndNamesTheHalf() {
        LineCrossing c = pitch.sweep(BallSegment.of(-20, 30, R, -21, 34, R, R)).crossing();
        assertEquals(LineCrossing.Line.TOUCHLINE, c.line());
        assertEquals(Flank.NORTH, c.flank());
        assertEquals(PitchEnd.WEST, c.end());
        assertEquals(pitch.halfWidth() + R, c.ballCentre().y(), 1e-9);
        assertFalse(c.disputed());
    }

    @Test
    void segmentInsideOrAlreadyOutGivesNothing() {
        assertSame(SweepResult.NOTHING, pitch.sweep(BallSegment.of(0, 0, R, 10, 5, 2, R)));
        assertSame(SweepResult.NOTHING, pitch.sweep(BallSegment.of(halfL + 1, 20, R, halfL + 5, 20, R, R)));
        // Reaching the line but not wholly over it.
        assertSame(SweepResult.NOTHING, pitch.sweep(BallSegment.of(0, 30, R, 0, pitch.halfWidth() + R / 2, R, R)));
    }

    @Test
    void cornerTieCountsAsGoalLine() {
        // A square 100 x 100 yd ground (legal in 1888): a diagonal is wholly over both lines at exactly the same t.
        PitchGeometry square = new PitchGeometry(PitchParams.laws1888(100, 100));
        double h = square.halfLength();
        LineCrossing c = square.sweep(BallSegment.of(h - 1, h - 1, R, h + 1, h + 1, R, R)).crossing();
        assertEquals(LineCrossing.Line.GOAL_LINE, c.line());
        double hw = pitch.halfWidth();
        LineCrossing first = pitch.sweep(BallSegment.of(halfL - 1, hw - 3, R, halfL + 3, hw + 1, R, R)).crossing();
        assertEquals(LineCrossing.Line.GOAL_LINE, first.line(), "x reaches its limit first");
        LineCrossing touch = pitch.sweep(BallSegment.of(halfL - 3, hw - 1, R, halfL + 1, hw + 3, R, R)).crossing();
        assertEquals(LineCrossing.Line.TOUCHLINE, touch.line());
    }

    // ---- tunnelling ----

    @Test
    void veryFastBallCannotTunnel() {
        // 1200 m/s for a 1/60 s substep = 20 m per segment: far longer than the ball, posts or bar.
        SweepResult goal = pitch.sweep(BallSegment.of(halfL - 10, 0, 1, halfL + 10, 0, 1, R));
        assertTrue(goal.crossing().betweenPosts());
        SweepResult post = pitch.sweep(BallSegment.of(halfL - 10, pitch.postCentreY(), 1, halfL + 10,
                pitch.postCentreY(), 1, R));
        assertTrue(post.hasContact());
        assertFalse(post.hasCrossing(), "a frame hit voids the rest of the segment");
        assertEquals(FrameMember.NORTH_POST, post.contact().member());
        assertEquals(PitchEnd.EAST, post.contact().end());
        assertEquals(halfL - pitch.params().postRadiusM() - R, post.contact().ballCentre().x(), 1e-9);
        SweepResult bar = pitch.sweep(BallSegment.of(halfL - 10, 0, pitch.crossbarCentreZ(), halfL + 10, 0,
                pitch.crossbarCentreZ(), R));
        assertEquals(FrameMember.CROSSBAR, bar.contact().member());
        SweepResult touch = pitch.sweep(BallSegment.of(0, 0, R, 0, 100, R, R));
        assertEquals(LineCrossing.Line.TOUCHLINE, touch.crossing().line());
        SweepResult westPost = pitch.sweep(BallSegment.of(-halfL + 10, -pitch.postCentreY(), 0.5, -halfL - 10,
                -pitch.postCentreY(), 0.5, R));
        assertEquals(PitchEnd.WEST, westPost.contact().end());
        assertEquals(FrameMember.SOUTH_POST, westPost.contact().member());
        assertNormal(new Vec3(1, 0, 0), westPost.contact().normal());
    }

    // ---- contact normals ----

    @Test
    void headOnPostAndBarNormalsPointBackAtTheBall() {
        FrameContact post = shotEast(pitch, -pitch.postCentreY(), 1).contact();
        assertEquals(FrameMember.SOUTH_POST, post.member());
        assertNormal(new Vec3(-1, 0, 0), post.normal());
        assertEquals(halfL - pitch.params().postRadiusM(), post.point().x(), 1e-9);
        FrameContact bar = shotEast(pitch, 0, pitch.crossbarCentreZ()).contact();
        assertEquals(FrameMember.CROSSBAR, bar.member());
        assertNormal(new Vec3(-1, 0, 0), bar.normal());
        // Rising ball under the bar on the goal line: hits the underside.
        FrameContact under = pitch.sweep(BallSegment.of(halfL, 0, 1, halfL, 0, 3, R)).contact();
        assertEquals(FrameMember.CROSSBAR, under.member());
        assertNormal(new Vec3(0, 0, -1), under.normal());
        assertEquals(pitch.crossbarUndersideZ(), under.point().z(), 1e-9);
        assertEquals(pitch.crossbarUndersideZ() - R, under.ballCentre().z(), 1e-9);
    }

    @Test
    void glancingPostHitHasAnAngledNormal() {
        double reach = pitch.params().postRadiusM() + R;
        double y = pitch.postCentreY() - 0.1;
        FrameContact c = shotEast(pitch, y, 1).contact();
        assertNotNull(c);
        double dx = -Math.sqrt(reach * reach - 0.1 * 0.1);
        assertNormal(new Vec3(dx / reach, -0.1 / reach, 0), c.normal());
        assertEquals(halfL + dx, c.ballCentre().x(), 1e-9);
        Vec3 axis = new Vec3(halfL, pitch.postCentreY(), 1);
        Vec3 expectedPoint = axis.plus(c.normal().times(pitch.params().postRadiusM()));
        assertEquals(expectedPoint.x(), c.point().x(), 1e-9);
        assertEquals(expectedPoint.y(), c.point().y(), 1e-9);
        assertEquals(reach, c.ballCentre().minus(axis).length(), 1e-9);
    }

    @Test
    void cornerOfPostAndBarHasADiagonalNormal() {
        FrameContact c = shotEast(pitch, pitch.postCentreY() + 0.1, pitch.crossbarCentreZ() + 0.1).contact();
        assertNotNull(c);
        assertEquals(1, c.normal().length(), 1e-12);
        assertTrue(c.normal().x() < 0 && c.normal().y() > 0 && c.normal().z() > 0, c.normal().toString());
    }

    @Test
    void ballTouchingTheFrameAndMovingAwayIsNotAContact() {
        double x = halfL - pitch.params().postRadiusM() - R + 1e-6;
        SweepResult away = pitch.sweep(BallSegment.of(x, pitch.postCentreY(), 1, x - 1, pitch.postCentreY(), 1, R));
        assertFalse(away.hasContact());
        SweepResult in = pitch.sweep(BallSegment.of(x, pitch.postCentreY(), 1, x + 1, pitch.postCentreY(), 1, R));
        assertEquals(0, in.contact().t());
    }

    // ---- disputed goals ----

    @Test
    void closeCallsWithinTheMarginAreDisputed() {
        double margin = pitch.params().disputedMarginM();
        double mouth = pitch.goalMouthHalfWidth();
        double frame = 2 * pitch.params().postRadiusM();
        LineCrossing insidePost = shotEast(pitch, mouth - R - 0.05, 1).crossing();
        assertTrue(insidePost.betweenPosts());
        assertTrue(insidePost.disputed());
        assertEquals(0.05, insidePost.frameGap(), 1e-9);
        LineCrossing clearInside = shotEast(pitch, mouth - R - 0.2, 1).crossing();
        assertTrue(clearInside.betweenPosts());
        assertFalse(clearInside.disputed());
        LineCrossing outsidePost = shotEast(pitch, -(mouth + frame + R + 0.05), 1).crossing();
        assertFalse(outsidePost.betweenPosts());
        assertTrue(outsidePost.disputed());
        LineCrossing overBar = shotEast(pitch, 0, pitch.crossbarUndersideZ() + 2 * pitch.params().crossbarRadiusM()
                + R + 0.05).crossing();
        assertFalse(overBar.betweenPosts());
        assertTrue(overBar.disputed());
        LineCrossing underBar = shotEast(pitch, 0, pitch.crossbarUndersideZ() - R - 0.05).crossing();
        assertTrue(underBar.betweenPosts());
        assertTrue(underBar.disputed());
        // Threshold: just beyond the margin is clear (gap < margin is the rule).
        LineCrossing atMargin = shotEast(pitch, mouth - R - margin - 1e-6, 1).crossing();
        assertFalse(atMargin.disputed());
        LineCrossing inMargin = shotEast(pitch, mouth - R - margin + 1e-6, 1).crossing();
        assertTrue(inMargin.disputed());
        PitchGeometry noBand = new PitchGeometry(PitchParams.defaults1888().withDisputedMargin(0));
        assertFalse(shotEast(noBand, mouth - R - 0.05, 1).crossing().disputed());
    }

    @Test
    void netsEndDisputes() {
        PitchGeometry netted = new PitchGeometry(PitchParams.defaults1888().withNets(true));
        LineCrossing c = shotEast(netted, netted.goalMouthHalfWidth() - R - 0.05, 1).crossing();
        assertTrue(c.betweenPosts());
        assertFalse(c.disputed());
    }

    // ---- era parameters ----

    @Test
    void eraMarkingsSwitchOnBySeason() {
        PitchGeometry p1888 = pitch;
        assertTrue(p1888.penaltyLineX(PitchEnd.EAST).isEmpty());
        assertTrue(p1888.centreCircleRadius().isEmpty());
        assertTrue(p1888.goalAreaArcRadius().isEmpty());
        assertTrue(p1888.penaltyArea(PitchEnd.EAST).isEmpty());
        assertTrue(p1888.penaltySpot(PitchEnd.EAST).isEmpty());
        assertFalse(p1888.inPenaltyZone(PitchEnd.EAST, halfL - 1, 0), "no penalties in 1888-89");

        PitchGeometry p1891 = new PitchGeometry(PitchParams.forSeason(1891, 110, 70));
        assertTrue(p1891.params().nets());
        assertEquals(EnumSet.of(EraMarking.PENALTY_LINE_1891, EraMarking.GOAL_AREA_ARCS_1891,
                EraMarking.CENTRE_CIRCLE_1891), p1891.params().markings());
        assertEquals(halfL - 12 * PitchParams.YARD, p1891.penaltyLineX(PitchEnd.EAST).getAsDouble(), EPS);
        assertEquals(-(halfL - 12 * PitchParams.YARD), p1891.penaltyLineX(PitchEnd.WEST).getAsDouble(), EPS);
        assertEquals(10 * PitchParams.YARD, p1891.centreCircleRadius().getAsDouble(), EPS);
        assertTrue(p1891.inPenaltyZone(PitchEnd.EAST, halfL - 1, 30), "the 1891 line runs the full width");
        assertFalse(p1891.inPenaltyZone(PitchEnd.EAST, halfL - 13 * PitchParams.YARD, 0));
        assertTrue(p1891.inPenaltyZone(PitchEnd.WEST, -halfL + 1, 0));
        assertFalse(p1891.inPenaltyZone(PitchEnd.WEST, halfL - 1, 0));
        assertFalse(new PitchGeometry(PitchParams.forSeason(1890, 110, 70)).params().nets());

        PitchGeometry p1902 = new PitchGeometry(PitchParams.forSeason(1902, 110, 70));
        assertTrue(p1902.penaltyLineX(PitchEnd.EAST).isEmpty());
        Rect box = p1902.penaltyArea(PitchEnd.EAST).get();
        assertEquals(halfL - 18 * PitchParams.YARD, box.minX(), EPS);
        assertEquals(halfL, box.maxX(), EPS);
        assertEquals(44 * PitchParams.YARD, box.maxY() - box.minY(), EPS);
        assertEquals(20 * PitchParams.YARD, p1902.goalAreaBox(PitchEnd.WEST).get().maxY() * 2, EPS);
        assertEquals(new Vec3(-(halfL - 12 * PitchParams.YARD), 0, 0), p1902.penaltySpot(PitchEnd.WEST).get());
        assertTrue(p1902.inPenaltyZone(PitchEnd.EAST, halfL - 1, 0));
        assertFalse(p1902.inPenaltyZone(PitchEnd.EAST, halfL - 1, 30), "outside the 1902 box");
    }

    // ---- determinism ----

    @Test
    void sameInputsGiveBitIdenticalOutputs() {
        assertEquals(sweepHash(new PitchGeometry(PitchParams.defaults1888())),
                sweepHash(new PitchGeometry(PitchParams.defaults1888())));
        assertEquals(GOLDEN, sweepHash(pitch), "arithmetic changed: re-pin only on purpose");
    }

    /** Pinned on WORKSTATION; BOOK3PRO and CI must compute the same bits. */
    private static final long GOLDEN = -1640709954089272491L;

    /** Sweeps a fixed fan of fast segments at the EAST goal and folds every output bit into a hash. */
    private static long sweepHash(PitchGeometry p) {
        long h = 1125899906842597L;
        double hl = p.halfLength();
        for (int i = 0; i < 41; i++) {
            for (int j = 0; j < 31; j++) {
                double y = -6 + i * 0.3;
                double z = 0.11 + j * 0.1;
                double ang = (i - 20) * 0.01;
                SweepResult s = p.sweep(BallSegment.of(hl - 4, y, z, hl + 4, y + 8 * ang, z + 0.05 * (j - 15), R));
                if (s.hasContact()) {
                    FrameContact c = s.contact();
                    h = mix(h, c.member().ordinal());
                    h = mix(h, c.t());
                    h = mix(h, c.normal().x());
                    h = mix(h, c.normal().y());
                    h = mix(h, c.normal().z());
                    h = mix(h, c.point().x());
                } else if (s.hasCrossing()) {
                    LineCrossing c = s.crossing();
                    h = mix(h, 100 + c.line().ordinal());
                    h = mix(h, c.t());
                    h = mix(h, c.frameGap());
                    h = mix(h, c.betweenPosts() ? 1 : 0);
                    h = mix(h, c.disputed() ? 1 : 0);
                } else {
                    h = mix(h, -1);
                }
            }
        }
        return h;
    }

    private static long mix(long h, double v) {
        return 31 * h + Double.doubleToLongBits(v);
    }

    private static void assertNormal(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x(), actual.x(), 1e-9, "nx");
        assertEquals(expected.y(), actual.y(), 1e-9, "ny");
        assertEquals(expected.z(), actual.z(), 1e-9, "nz");
        assertEquals(1, actual.length(), 1e-12);
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> BallSegment.of(0, 0, 0, 1, 1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PitchGeometry(null));
        assertNull(pitch.sweep(BallSegment.of(0, 0, R, 1, 0, R, R)).crossing());
    }
}
