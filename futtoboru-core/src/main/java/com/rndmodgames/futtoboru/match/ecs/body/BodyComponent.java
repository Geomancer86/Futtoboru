package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * A player's body on the pitch (E-13): a circle on the ground plane (axes as the ball: x along the pitch, y across,
 * origin at the centre spot; SI units, doubles). Written by {@link LocomotionSystem} (movement) and
 * {@link ContactSystem} (collisions, falls).
 */
public final class BodyComponent implements Component, HashedComponent {

    public final Team team;
    /** Slot in the team's shape (0 = keeper), see {@link Shape}. */
    public final int slot;
    /** Circle radius, m (physique by position, D22). */
    public final double radius;
    /** Body mass without kit, kg (physique by position, D22). */
    public final double bodyMassKg;

    public double x, y;
    public double vx, vy;
    /** Position at the start of the current tick (before locomotion), for swept contacts. */
    public double prevX, prevY;
    /** Direction the player faces, rad (0 = +x, counter-clockwise); the direction of motion while moving. */
    public double facing;
    /** Unit vector of {@link #facing} (kept by locomotion; set both together or use {@link #face(double)}). */
    public double fx, fy;
    /** Body plus kit (boots as they soak, shin pads), kg; refreshed every tick by locomotion. */
    public double massKg;
    /** Ticks left without full control after a stumble. */
    public int stumbleTicks;
    /** Ticks left on the ground after a knock-down (no control, no contacts given). */
    public int downTicks;

    public BodyComponent(Team team, int slot, double radius, double bodyMassKg, double x, double y, double facing) {
        if (team == null || !(radius > 0) || !(bodyMassKg > 0)) {
            throw new IllegalArgumentException("team, radius and mass are required");
        }
        this.team = team;
        this.slot = slot;
        this.radius = radius;
        this.bodyMassKg = bodyMassKg;
        this.massKg = bodyMassKg;
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        face(facing);
    }

    /** Sets the facing angle and its unit vector (spawn time, tests: uses StrictMath.cos/sin). */
    public void face(double angle) {
        facing = angle;
        fx = StrictMath.cos(angle);
        fy = StrictMath.sin(angle);
    }

    public double speed() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    public boolean isDown() {
        return downTicks > 0;
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("body.team", team.ordinal());
        h.mix("body.slot", slot);
        h.mix("body.radius", radius);
        h.mix("body.bodyMass", bodyMassKg);
        h.mix("body.x", x);
        h.mix("body.y", y);
        h.mix("body.vx", vx);
        h.mix("body.vy", vy);
        h.mix("body.prevX", prevX);
        h.mix("body.prevY", prevY);
        h.mix("body.facing", facing);
        h.mix("body.fx", fx);
        h.mix("body.fy", fy);
        h.mix("body.mass", massKg);
        h.mix("body.stumble", stumbleTicks);
        h.mix("body.down", downTicks);
    }
}
