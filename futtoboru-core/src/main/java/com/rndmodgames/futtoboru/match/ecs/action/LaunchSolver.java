package com.rndmodgames.futtoboru.match.ecs.action;

import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams.SurfaceParams;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallPhysics;

/**
 * The launch velocity a player aims for (E-14): what he wants the ball to do, before noise and before his foot's
 * limits. Results in {@link #vx}, {@link #vy}, {@link #vz}.
 *
 * <ul>
 * <li><b>Ground ball:</b> closed form of the roll model ({@link BallPhysics} rolls with constant deceleration):
 * {@code v0 = sqrt(va^2 + 2 a d)} with the roll drag the kicker <em>plans</em> for.</li>
 * <li><b>Ball in the air:</b> shooting method on a scratch ball with the real flight model (drag, Magnus curl from the
 * wanted spin): start from the drag-free arc with flight time {@code T0}, fly it, and correct the launch velocity by
 * the miss at the target's range divided by the time to get there; {@link ActionParams#solverIterations} rounds.
 * The planner flies the ball at the mass the kicker feels at kick-off ({@code startMassKg}), not its soaked mass.</li>
 * </ul>
 * No allocation: one scratch ball, reused.
 */
final class LaunchSolver {

    private final PhysicsParams p;
    private final ActionParams a;
    private final BallPhysics physics;
    private final BallComponent scratch = new BallComponent();
    private final double dt;
    private final int maxSteps;

    /** The solved launch velocity, m/s. */
    double vx, vy, vz;
    /** Distance of the last trial from the target, m (air solves). */
    double missM;

    LaunchSolver(PhysicsParams params, double substepSeconds) {
        this.p = params;
        this.a = params.action;
        this.physics = new BallPhysics(params);
        this.dt = substepSeconds;
        this.maxSteps = (int) Math.ceil(a.solverMaxSeconds / substepSeconds);
    }

    /** Ground ball from (x, y) arriving at (tx, ty) with {@code arrival} m/s, planning roll drag {@code rollDrag}. */
    void ground(double x, double y, double tx, double ty, double arrival, double rollDrag) {
        double dx = tx - x;
        double dy = ty - y;
        double d = Math.sqrt(dx * dx + dy * dy);
        vz = 0;
        missM = 0;
        if (d < 1e-6) {
            vx = 0;
            vy = 0;
            return;
        }
        double decel = p.ballRollingResistance * rollDrag * p.gravity;
        double v0 = Math.sqrt(arrival * arrival + 2 * decel * d);
        vx = dx / d * v0;
        vy = dy / d * v0;
    }

    /** Flight time of the drag-free arc from height {@code z} to {@code tz} over {@code d} with launch slope {@code loft}. */
    double loftTime(double d, double z, double tz, double loft) {
        double t2 = 2 * (loft * d - (tz - z)) / p.gravity;
        double t = t2 > 0 ? Math.sqrt(t2) : 0;
        return t < 0.25 ? 0.25 : t;
    }

    /**
     * Ball in the air from (x, y, z) to pass through (tx, ty, tz), first guess flight time {@code t0}, with side spin
     * {@code spin}, flown at {@code massKg}. A target at height 0 means "land there".
     */
    void air(double x, double y, double z, double tx, double ty, double tz, double t0, double spin, double massKg,
            SurfaceParams surface) {
        double dx = tx - x;
        double dy = ty - y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double ux = d > 1e-9 ? dx / d : 1;
        double uy = d > 1e-9 ? dy / d : 0;
        double t = t0 > 0.05 ? t0 : 0.05;
        vx = ux * d / t;
        vy = uy * d / t;
        vz = (tz - z + 0.5 * p.gravity * t * t) / t;
        missM = Double.MAX_VALUE;
        boolean land = tz <= 0;
        for (int it = 0; it < a.solverIterations; it++) {
            scratch.x = x;
            scratch.y = y;
            scratch.z = z < 0 ? 0 : z;
            scratch.vx = vx;
            scratch.vy = vy;
            scratch.vz = vz;
            scratch.spin = spin;
            scratch.massKg = massKg;
            scratch.startMassKg = massKg;
            scratch.saturation = 0;
            scratch.groundContacts = 0;
            scratch.state = BallComponent.AIR;
            double px = scratch.x;
            double py = scratch.y;
            double pz = scratch.z;
            double prog = 0;
            double xa = px;
            double ya = py;
            double za = pz;
            double ta = 0;
            boolean found = false;
            for (int k = 1; k <= maxSteps; k++) {
                physics.step(scratch, surface, dt);
                if (scratch.groundContacts > 0) {
                    double lp = (scratch.contactX - x) * ux + (scratch.contactY - y) * uy;
                    if (land || lp < d) {
                        // landed first (short, or a "land there" target): the landing point is the trial's result
                        xa = scratch.contactX;
                        ya = scratch.contactY;
                        za = 0;
                        ta = k * dt;
                        found = true;
                        break;
                    }
                }
                double np = (scratch.x - x) * ux + (scratch.y - y) * uy;
                if (np >= d && !land) {
                    double f = np > prog ? (d - prog) / (np - prog) : 1;
                    xa = px + f * (scratch.x - px);
                    ya = py + f * (scratch.y - py);
                    za = pz + f * (scratch.z - pz);
                    ta = (k - 1 + f) * dt;
                    found = true;
                    break;
                }
                if (scratch.state != BallComponent.AIR) {
                    break;
                }
                px = scratch.x;
                py = scratch.y;
                pz = scratch.z;
                prog = np;
            }
            if (!found) {
                xa = scratch.x;
                ya = scratch.y;
                za = scratch.z;
                ta = maxSteps * dt;
            }
            double ex = tx - xa;
            double ey = ty - ya;
            double ez = land ? 0 : tz - za;
            missM = Math.sqrt(ex * ex + ey * ey + ez * ez);
            if (missM < a.solverToleranceM) {
                return;
            }
            double tt = ta < 0.05 ? 0.05 : ta;
            vx += ex / tt;
            vy += ey / tt;
            vz += ez / tt;
        }
    }
}
