package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * What a player wants his body to do this tick: a target velocity on the ground and whether he puts his shoulder into
 * the next contact. Written by the AI (E-15) or tests; read by {@link LocomotionSystem} (which may not reach it:
 * acceleration, turn, grip and stamina limits) and {@link ContactSystem}. Zero = stand still.
 */
public final class MoveCommand implements Component, HashedComponent {

    /** Wanted velocity, m/s. */
    public double targetVx, targetVy;
    /** A deliberate shoulder charge into whoever he meets this tick (an action, §3.3; E-14 decides when). */
    public boolean charge;

    public MoveCommand set(double vx, double vy) {
        targetVx = vx;
        targetVy = vy;
        return this;
    }

    /** Run from (fromX, fromY) toward (x, y) at {@code speed}; stop when there. */
    public MoveCommand toward(double fromX, double fromY, double x, double y, double speed) {
        double dx = x - fromX;
        double dy = y - fromY;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d < 1e-9) {
            return set(0, 0);
        }
        return set(dx / d * speed, dy / d * speed);
    }

    public MoveCommand stop() {
        charge = false;
        return set(0, 0);
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("cmd.vx", targetVx);
        h.mix("cmd.vy", targetVy);
        h.mix("cmd.charge", charge ? 1 : 0);
    }
}
