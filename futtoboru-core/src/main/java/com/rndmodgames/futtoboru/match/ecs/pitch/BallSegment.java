package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * The path of the ball centre within one ball substep: a straight segment from {@code start} to {@code end}
 * (metres, pitch coordinates), with the ball radius. This is the only input E-12 needs from the ball system
 * (E-11): build one per substep from the position before and after integration.
 */
public record BallSegment(Vec3 start, Vec3 end, double radius) {

    public BallSegment {
        if (start == null || end == null) {
            throw new IllegalArgumentException("segment needs a start and an end");
        }
        if (!(radius > 0)) {
            throw new IllegalArgumentException("ball radius must be > 0: " + radius);
        }
    }

    public static BallSegment of(double x0, double y0, double z0, double x1, double y1, double z1, double radius) {
        return new BallSegment(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1), radius);
    }

    /** The ball centre at fraction {@code t} (0..1) of the segment. */
    public Vec3 at(double t) {
        return start.plus(end.minus(start).times(t));
    }
}
