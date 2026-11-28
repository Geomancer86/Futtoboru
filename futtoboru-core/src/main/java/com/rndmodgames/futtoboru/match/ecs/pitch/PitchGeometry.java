package com.rndmodgames.futtoboru.match.ecs.pitch;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * The pitch of one match built from {@link PitchParams}: lines, corner flags, goal frames, era markings, and the
 * swept test of a ball substep against them ({@link #sweep(BallSegment)}). Immutable; coordinates in
 * {@code package-info}.
 *
 * <p>Goal frame: the goal width is measured between the inner edges of the posts, the bar height to the underside
 * of the bar. Post axes stand on the goal line at {@code y = ±postCentreY()}; the bar axis lies on the goal line at
 * {@code z = crossbarCentreZ()}. Each member is a capsule (a segment with a radius), so the corner where post meets
 * bar is round and has a well-defined normal.
 *
 * <p>A ball is out of play when it is <b>wholly</b> over a line (its centre one radius beyond it). Whether it was a
 * goal is decided where the ball centre path crosses the plane of the goal frame (the goal line): between the posts
 * and under the bar is a goal, anything else is not. With no nets (1888-89) nothing stops or marks the ball after it
 * passes the frame, so a crossing with less than {@link PitchParams#disputedMarginM()} between the ball and the frame
 * is flagged disputed (D19-5).
 */
public final class PitchGeometry {

    private final PitchParams params;
    private final double halfLength;
    private final double halfWidth;
    private final double postCentreY;
    private final double crossbarCentreZ;

    public PitchGeometry(PitchParams params) {
        if (params == null) {
            throw new IllegalArgumentException("params");
        }
        this.params = params;
        this.halfLength = params.lengthM() / 2;
        this.halfWidth = params.widthM() / 2;
        this.postCentreY = params.goalWidthM() / 2 + params.postRadiusM();
        this.crossbarCentreZ = params.crossbarHeightM() + params.crossbarRadiusM();
    }

    public PitchParams params() {
        return params;
    }

    // ---- lines and points ----

    public double length() {
        return params.lengthM();
    }

    public double width() {
        return params.widthM();
    }

    public double halfLength() {
        return halfLength;
    }

    public double halfWidth() {
        return halfWidth;
    }

    /** {@code x} of the goal line at {@code end}. */
    public double goalLineX(PitchEnd end) {
        return end.sign() * halfLength;
    }

    /** {@code y} of the touchline at {@code flank}. */
    public double touchlineY(Flank flank) {
        return flank.sign() * halfWidth;
    }

    /** {@code x} of the halfway line (always 0). */
    public double halfwayX() {
        return 0;
    }

    /** The centre of the pitch, where every kick-off is taken. */
    public Vec3 centre() {
        return new Vec3(0, 0, 0);
    }

    /** Foot of the corner flag at the meeting of the goal line of {@code end} and the touchline of {@code flank}. */
    public Vec3 cornerFlag(PitchEnd end, Flank flank) {
        return new Vec3(goalLineX(end), touchlineY(flank), 0);
    }

    /** {@code |y|} of each post axis. */
    public double postCentreY() {
        return postCentreY;
    }

    /** Half the open goal mouth between the inner edges of the posts (4 yd in 1888-89). */
    public double goalMouthHalfWidth() {
        return params.goalWidthM() / 2;
    }

    /** Height of the underside of the bar (8 ft in 1888-89). */
    public double crossbarUndersideZ() {
        return params.crossbarHeightM();
    }

    /** Height of the bar axis. */
    public double crossbarCentreZ() {
        return crossbarCentreZ;
    }

    /** Foot of the post axis at {@code end}, {@code flank} side. */
    public Vec3 postBase(PitchEnd end, Flank flank) {
        return new Vec3(goalLineX(end), flank.sign() * postCentreY, 0);
    }

    /** True while the ball (centre {@code c}, radius {@code r}) is not wholly over any boundary line. */
    public boolean inPlay(Vec3 c, double r) {
        return Math.abs(c.x()) <= halfLength + r && Math.abs(c.y()) <= halfWidth + r;
    }

    // ---- era markings (empty in 1888-89) ----

    /** {@code x} of the 1891 twelve-yard line at {@code end}, if the season has it. */
    public OptionalDouble penaltyLineX(PitchEnd end) {
        return params.has(EraMarking.PENALTY_LINE_1891)
                ? OptionalDouble.of(end.sign() * (halfLength - 12 * PitchParams.YARD)) : OptionalDouble.empty();
    }

    /** Radius of the 1891 goal-area arcs drawn from each post, if the season has them. */
    public OptionalDouble goalAreaArcRadius() {
        return params.has(EraMarking.GOAL_AREA_ARCS_1891) ? OptionalDouble.of(6 * PitchParams.YARD)
                : OptionalDouble.empty();
    }

    /** Radius of the centre circle (1891), if the season has it. */
    public OptionalDouble centreCircleRadius() {
        return params.has(EraMarking.CENTRE_CIRCLE_1891) ? OptionalDouble.of(10 * PitchParams.YARD)
                : OptionalDouble.empty();
    }

    /** The 1902 penalty area (18 yd deep, 44 yd wide) at {@code end}, if the season has it. */
    public Optional<Rect> penaltyArea(PitchEnd end) {
        return params.has(EraMarking.PENALTY_AREA_1902) ? Optional.of(box(end, 18, 22)) : Optional.empty();
    }

    /** The 1902 goal area (6 yd deep, 20 yd wide) at {@code end}, if the season has it. */
    public Optional<Rect> goalAreaBox(PitchEnd end) {
        return params.has(EraMarking.PENALTY_AREA_1902) ? Optional.of(box(end, 6, 10)) : Optional.empty();
    }

    /** The 1902 penalty spot, 12 yd out, if the season has it. */
    public Optional<Vec3> penaltySpot(PitchEnd end) {
        return params.has(EraMarking.PENALTY_AREA_1902)
                ? Optional.of(new Vec3(end.sign() * (halfLength - 12 * PitchParams.YARD), 0, 0)) : Optional.empty();
    }

    /**
     * True if {@code (x, y)} is where a foul by the defenders of {@code end} would give a penalty in this season:
     * inside the 1902 area, or (1891-1902) between the twelve-yard line and the goal line. Always false in 1888-89.
     */
    public boolean inPenaltyZone(PitchEnd end, double x, double y) {
        Optional<Rect> area = penaltyArea(end);
        if (area.isPresent()) {
            return area.get().contains(x, y);
        }
        OptionalDouble line = penaltyLineX(end);
        if (line.isPresent()) {
            double depth = end.sign() * x;
            return depth >= end.sign() * line.getAsDouble() && depth <= halfLength && Math.abs(y) <= halfWidth;
        }
        return false;
    }

    private Rect box(PitchEnd end, double depthYd, double halfWidthYd) {
        double line = goalLineX(end);
        double inner = line - end.sign() * depthYd * PitchParams.YARD;
        double hw = halfWidthYd * PitchParams.YARD;
        return new Rect(Math.min(line, inner), Math.max(line, inner), -hw, hw);
    }

    // ---- swept detection ----

    /**
     * Tests one ball substep against the goal frames and the boundary lines. Returns the earliest event: a frame
     * contact (the ball system reflects and re-integrates the remainder) or a crossing (the ball is out of play,
     * see {@link RestartDecider}); {@link SweepResult#NOTHING} otherwise. Exact for straight segments at any speed,
     * so fast shots cannot tunnel through posts, bar or lines. A segment that starts already out of play yields no
     * crossing; a ball that starts touching the frame reports a contact at {@code t = 0} only while moving into it.
     */
    public SweepResult sweep(BallSegment seg) {
        FrameContact contact = earliestFrameContact(seg);
        double limit = contact != null ? contact.t() : 1.0;
        LineCrossing crossing = earliestCrossing(seg, limit);
        if (crossing != null && (contact == null || crossing.t() < contact.t())) {
            return new SweepResult(null, crossing);
        }
        return contact != null ? new SweepResult(contact, null) : SweepResult.NOTHING;
    }

    /**
     * The earliest contact of the substep with a post or the bar, or null, whatever the lines do (E-16b: the ball
     * system bounces the ball off the frame even just after it went wholly over a line, e.g. skimming the outside of
     * a post; the laws judge the line crossing separately).
     */
    public FrameContact frameContact(BallSegment seg) {
        return earliestFrameContact(seg);
    }

    /**
     * Cheap pre-test: true when a substep from {@code (x0, y0, z0)} to {@code (x1, y1, z1)} (ball centres) may come
     * within {@code r} of either goal frame, so {@link #frameContact} is worth calling.
     */
    public boolean nearFrame(double x0, double y0, double z0, double x1, double y1, double z1, double r) {
        double reach = r + Math.max(params.postRadiusM(), params.crossbarRadiusM());
        double ax0 = Math.abs(x0);
        double ax1 = Math.abs(x1);
        if (Math.max(ax0, ax1) < halfLength - reach || Math.min(ax0, ax1) > halfLength + reach
                && (x0 > 0) == (x1 > 0)) {
            return false;
        }
        return Math.min(Math.abs(y0), Math.abs(y1)) <= postCentreY + reach + Math.abs(y1 - y0)
                && Math.min(z0, z1) <= crossbarCentreZ + reach;
    }

    private FrameContact earliestFrameContact(BallSegment seg) {
        Vec3 p0 = seg.start();
        Vec3 d = seg.end().minus(p0);
        double r = seg.radius();
        FrameContact best = null;
        for (PitchEnd end : PitchEnd.values()) {
            double gx = goalLineX(end);
            for (FrameMember member : FrameMember.values()) {
                Vec3 a;
                Vec3 b;
                double memberR;
                switch (member) {
                    case SOUTH_POST:
                        a = new Vec3(gx, -postCentreY, 0);
                        b = new Vec3(gx, -postCentreY, crossbarCentreZ);
                        memberR = params.postRadiusM();
                        break;
                    case NORTH_POST:
                        a = new Vec3(gx, postCentreY, 0);
                        b = new Vec3(gx, postCentreY, crossbarCentreZ);
                        memberR = params.postRadiusM();
                        break;
                    default:
                        a = new Vec3(gx, -postCentreY, crossbarCentreZ);
                        b = new Vec3(gx, postCentreY, crossbarCentreZ);
                        memberR = params.crossbarRadiusM();
                        break;
                }
                double t = sweepCapsule(p0, d, a, b, memberR + r);
                if (!Double.isNaN(t) && (best == null || t < best.t())) {
                    best = contactAt(end, member, seg, t, a, b, memberR, d);
                }
            }
        }
        return best;
    }

    private static FrameContact contactAt(PitchEnd end, FrameMember member, BallSegment seg, double t, Vec3 a, Vec3 b,
            double memberR, Vec3 d) {
        Vec3 c = seg.at(t);
        Vec3 ab = b.minus(a);
        double len2 = ab.dot(ab);
        double s = clamp(c.minus(a).dot(ab) / len2, 0, 1);
        Vec3 q = a.plus(ab.times(s));
        Vec3 diff = c.minus(q);
        double dist = diff.length();
        Vec3 normal;
        if (dist > 0) {
            normal = diff.times(1 / dist);
        } else {
            double dl = d.length();
            normal = dl > 0 ? d.times(-1 / dl) : new Vec3(0, 0, 1);
        }
        return new FrameContact(end, member, t, c, q.plus(normal.times(memberR)), normal);
    }

    /** Earliest {@code t} in [0, 1] at which point {@code p0 + t d} is within {@code rad} of segment AB, or NaN. */
    private static double sweepCapsule(Vec3 p0, Vec3 d, Vec3 a, Vec3 b, double rad) {
        Vec3 ab = b.minus(a);
        double len = ab.length();
        Vec3 n = ab.times(1 / len);
        Vec3 ao = p0.minus(a);
        double aoN = ao.dot(n);
        double dN = d.dot(n);
        Vec3 m = ao.minus(n.times(aoN));
        Vec3 k = d.minus(n.times(dN));
        double qa = k.dot(k);
        double qb = 2 * m.dot(k);
        double qc = m.dot(m) - rad * rad;
        double best = Double.NaN;
        if (qc <= 0 && aoN >= 0 && aoN <= len) {
            // Already touching the body of the capsule: a contact only while moving further in.
            return m.dot(k) < 0 ? 0 : Double.NaN;
        }
        if (qc > 0 && qa > 0) {
            double disc = qb * qb - 4 * qa * qc;
            if (disc >= 0) {
                double t = (-qb - Math.sqrt(disc)) / (2 * qa);
                if (t >= 0 && t <= 1) {
                    double s = aoN + t * dN;
                    if (s >= 0 && s <= len) {
                        best = t;
                    }
                }
            }
        }
        double ta = sweepSphere(p0, d, a, rad);
        if (!Double.isNaN(ta) && (Double.isNaN(best) || ta < best)) {
            best = ta;
        }
        double tb = sweepSphere(p0, d, b, rad);
        if (!Double.isNaN(tb) && (Double.isNaN(best) || tb < best)) {
            best = tb;
        }
        return best;
    }

    private static double sweepSphere(Vec3 p0, Vec3 d, Vec3 c, double rad) {
        Vec3 e = p0.minus(c);
        double qc = e.dot(e) - rad * rad;
        double ed = e.dot(d);
        if (qc <= 0) {
            return ed < 0 ? 0 : Double.NaN;
        }
        double qa = d.dot(d);
        if (qa == 0) {
            return Double.NaN;
        }
        double disc = ed * ed - qa * qc;
        if (disc < 0) {
            return Double.NaN;
        }
        double t = (-ed - Math.sqrt(disc)) / qa;
        return t >= 0 && t <= 1 ? t : Double.NaN;
    }

    private LineCrossing earliestCrossing(BallSegment seg, double limit) {
        double r = seg.radius();
        Vec3 p0 = seg.start();
        Vec3 p1 = seg.end();
        if (!inPlay(p0, r)) {
            return null;
        }
        double dx = p1.x() - p0.x();
        double dy = p1.y() - p0.y();
        double bestT = Double.NaN;
        LineCrossing.Line bestLine = null;
        int bestSign = 0;
        // Goal lines first, so an exact tie at a corner counts as over the goal line (deterministic).
        for (PitchEnd end : PitchEnd.values()) {
            double t = exitT(p0.x(), dx, end.sign(), halfLength + r);
            if (!Double.isNaN(t) && t <= limit && (Double.isNaN(bestT) || t < bestT)) {
                bestT = t;
                bestLine = LineCrossing.Line.GOAL_LINE;
                bestSign = end.sign();
            }
        }
        for (Flank flank : Flank.values()) {
            double t = exitT(p0.y(), dy, flank.sign(), halfWidth + r);
            if (!Double.isNaN(t) && t <= limit && (Double.isNaN(bestT) || t < bestT)) {
                bestT = t;
                bestLine = LineCrossing.Line.TOUCHLINE;
                bestSign = flank.sign();
            }
        }
        if (bestLine == null) {
            return null;
        }
        Vec3 c = seg.at(bestT);
        if (bestLine == LineCrossing.Line.TOUCHLINE) {
            return new LineCrossing(bestLine, PitchEnd.ofX(c.x()), bestSign < 0 ? Flank.SOUTH : Flank.NORTH, bestT, c,
                    Double.NaN, Double.NaN, false, Double.NaN, false);
        }
        PitchEnd end = bestSign < 0 ? PitchEnd.WEST : PitchEnd.EAST;
        // Where the centre path meets the frame plane (may lie just before this substep: extrapolated linearly).
        double tp = (goalLineX(end) - p0.x()) / dx;
        double py = p0.y() + tp * dy;
        double pz = p0.z() + tp * (p1.z() - p0.z());
        boolean between = Math.abs(py) < postCentreY && pz < crossbarCentreZ;
        double gap = frameGap(py, pz) - r;
        boolean disputed = !params.nets() && gap < params.disputedMarginM();
        return new LineCrossing(bestLine, end, Flank.ofY(c.y()), bestT, c, py, pz, between, gap, disputed);
    }

    /** {@code t} in [0, 1] when coordinate {@code u0 + t du} passes {@code sign * limit} outward, or NaN. */
    private static double exitT(double u0, double du, int sign, double limit) {
        double v0 = sign * u0;
        double dv = sign * du;
        if (dv <= 0 || v0 + dv <= limit) {
            return Double.NaN;
        }
        double t = (limit - v0) / dv;
        return t < 0 ? 0 : t;
    }

    /** Distance in the goal plane from {@code (y, z)} to the surface of the nearest frame member. */
    private double frameGap(double y, double z) {
        double toPost = Math.min(distToVertical(y, z, -postCentreY), distToVertical(y, z, postCentreY))
                - params.postRadiusM();
        double by = clamp(y, -postCentreY, postCentreY);
        double toBar = Math.sqrt((y - by) * (y - by) + (z - crossbarCentreZ) * (z - crossbarCentreZ))
                - params.crossbarRadiusM();
        return Math.min(toPost, toBar);
    }

    private double distToVertical(double y, double z, double py) {
        double pzc = clamp(z, 0, crossbarCentreZ);
        return Math.sqrt((y - py) * (y - py) + (z - pzc) * (z - pzc));
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
