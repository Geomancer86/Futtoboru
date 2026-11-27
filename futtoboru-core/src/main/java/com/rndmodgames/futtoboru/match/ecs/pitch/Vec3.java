package com.rndmodgames.futtoboru.match.ecs.pitch;

/** An immutable 3D point or direction in pitch coordinates (metres), see {@code package-info}. */
public record Vec3(double x, double y, double z) {

    public Vec3 plus(Vec3 o) {
        return new Vec3(x + o.x, y + o.y, z + o.z);
    }

    public Vec3 minus(Vec3 o) {
        return new Vec3(x - o.x, y - o.y, z - o.z);
    }

    public Vec3 times(double s) {
        return new Vec3(x * s, y * s, z * s);
    }

    public double dot(Vec3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public double length() {
        return Math.sqrt(dot(this));
    }
}
