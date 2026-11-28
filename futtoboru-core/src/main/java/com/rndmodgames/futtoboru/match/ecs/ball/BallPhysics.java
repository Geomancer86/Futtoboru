package com.rndmodgames.futtoboru.match.ecs.ball;

import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams.SurfaceParams;

/**
 * 2.5D ball physics (docs/design/MATCH_ENGINE_ECS.md §3.2, E-11): gravity, quadratic drag, Magnus curl from
 * vertical-axis spin, bounce and roll per surface, wet-mass soak (BALL_AND_KIT_PHYSICS.md §1.3), kicks as impulses.
 *
 * Determinism (§2.1): doubles only, {@code + - * / sqrt} and {@code StrictMath} for transcendentals, no allocation.
 *
 * Integration: within a substep the acceleration is evaluated once from the current velocity and the position is
 * advanced with {@code x += v dt + a dt^2 / 2} (exact for pure gravity), so a drag-free flight matches the closed form
 * to rounding. A ground contact inside a substep is found by solving that quadratic for the contact time; the bounce
 * happens there and the rest of the substep continues after it. Rolling uses constant deceleration, integrated
 * exactly (stops at {@code v^2 / (2 a)}).
 */
public final class BallPhysics {

    private final PhysicsParams p;
    /** 0.5 * rho * Cd * A: drag force = dragK * |v| * v. */
    private final double dragK;
    /** 0.5 * rho * A * r * liftSlope: Magnus force = magnusK * (omega x v). */
    private final double magnusK;

    public BallPhysics(PhysicsParams params) {
        this.p = params;
        double area = Math.PI * params.ballRadiusM * params.ballRadiusM;
        this.dragK = 0.5 * params.airDensity * params.ballDragCoefficient * area;
        this.magnusK = 0.5 * params.airDensity * area * params.ballRadiusM * params.ballMagnusLiftSlope;
    }

    // ---- wet mass (BKP §1.3: m = m0 * (1 + U * (1 - exp(-E / tau)))) -----------------------------------------------

    /** Share of the water ceiling taken up after {@code exposureMin} minutes of effective submersion. */
    public double saturationAt(double exposureMin) {
        return 1.0 - StrictMath.exp(-exposureMin / p.ballUptakeTauMin);
    }

    /** Mass of a ball that started at {@code startMassKg} after {@code exposureMin} minutes of submersion. */
    public double massAt(double startMassKg, double exposureMin) {
        return startMassKg * (1.0 + p.ballWaterUptakeMax * saturationAt(exposureMin));
    }

    /** Exposes the ball to {@code wetness} (0..1) for {@code seconds} and updates its mass. */
    public void soak(BallComponent b, double wetness, double seconds) {
        if (wetness <= 0) {
            return;
        }
        b.exposureMin += wetness * seconds / 60.0;
        b.saturation = saturationAt(b.exposureMin);
        b.massKg = b.startMassKg * (1.0 + p.ballWaterUptakeMax * b.saturation);
    }

    // ---- kicks ---------------------------------------------------------------------------------------------------

    /** Adds an impulse (N*s): the velocity change is J / m, so a heavier (wetter) ball leaves slower. */
    public void applyImpulse(BallComponent b, double jx, double jy, double jz) {
        double m = b.massKg;
        b.vx += jx / m;
        b.vy += jy / m;
        b.vz += jz / m;
        if (b.z > 0 || b.vz > 0) {
            b.state = BallComponent.AIR;
        } else {
            b.vz = 0; // driven into the ground: the ground takes the downward part
            b.state = b.vx != 0 || b.vy != 0 ? BallComponent.ROLLING : BallComponent.REST;
        }
    }

    /**
     * A kick: impulse {@code impulseNs} at {@code elevationRad} above the ground, towards {@code headingRad}
     * (0 = +x, counter-clockwise), with side spin {@code spinRadS} (positive curls left).
     */
    public void kick(BallComponent b, double impulseNs, double elevationRad, double headingRad, double spinRadS) {
        double c = StrictMath.cos(elevationRad);
        applyImpulse(b, impulseNs * c * StrictMath.cos(headingRad), impulseNs * c * StrictMath.sin(headingRad),
                impulseNs * StrictMath.sin(elevationRad));
        b.spin = spinRadS;
    }

    /**
     * Impulse a boot swinging at {@code footSpeedMps} gives a ball of mass {@code ballMassKg} (BKP §1.5):
     * {@code v_ball = v_foot * M (1 + e) / (M + m)}, so {@code J = m * v_ball}. A 526 g wet ball leaves ~7 % slower
     * than a 400 g dry one for the same swing.
     */
    public double footImpulse(double footSpeedMps, double ballMassKg) {
        return footImpulse(footSpeedMps, ballMassKg, p.footEffectiveMassKg);
    }

    /**
     * As {@link #footImpulse(double, double)} with the kicker's effective foot mass {@code footMassKg} (E-14: the
     * boot's {@code KitComponent.kickEffectiveMassKg}; a heavy wet boot kicks harder).
     */
    public double footImpulse(double footSpeedMps, double ballMassKg, double footMassKg) {
        return ballMassKg * footSpeedMps * footMassKg * (1.0 + p.footKickRestitution) / (footMassKg + ballMassKg);
    }

    // ---- stepping ------------------------------------------------------------------------------------------------

    /** Advances the ball by {@code dt} seconds on surface {@code s}. */
    public void step(BallComponent b, SurfaceParams s, double dt) {
        double left = dt;
        // At most a few phase changes per substep (contact, bounce, roll); bounded so the cost stays flat.
        for (int phase = 0; phase < 4 && left > 0; phase++) {
            if (b.state == BallComponent.AIR) {
                left = flight(b, s, left);
            } else if (b.state == BallComponent.ROLLING) {
                roll(b, s, left);
                left = 0;
            } else {
                left = 0;
            }
        }
        double decay = (b.state == BallComponent.AIR ? p.ballSpinDecayAirPerS : p.ballSpinDecayGroundPerS) * dt;
        b.spin *= decay >= 1 ? 0 : 1 - decay;
    }

    /** Flies for up to {@code dt}; on ground contact bounces or starts rolling and returns the time left. */
    private double flight(BallComponent b, SurfaceParams s, double dt) {
        double m = b.massKg;
        double speed = Math.sqrt(b.vx * b.vx + b.vy * b.vy + b.vz * b.vz);
        double drag = dragK * speed / m;
        double magnus = magnusK * b.spin / m;
        // omega = (0, 0, spin): omega x v = spin * (-vy, vx, 0).
        double ax = -drag * b.vx - magnus * b.vy;
        double ay = -drag * b.vy + magnus * b.vx;
        double az = -drag * b.vz - p.gravity;

        double t = dt;
        boolean contact = false;
        double zEnd = b.z + b.vz * dt + 0.5 * az * dt * dt;
        if (zEnd <= 0) {
            contact = true;
            t = contactTime(b.z, b.vz, az, dt);
        }
        b.x += b.vx * t + 0.5 * ax * t * t;
        b.y += b.vy * t + 0.5 * ay * t * t;
        b.z += b.vz * t + 0.5 * az * t * t;
        b.vx += ax * t;
        b.vy += ay * t;
        b.vz += az * t;
        if (!contact) {
            return 0;
        }
        b.z = 0;
        b.groundContacts++;
        b.contactX = b.x;
        b.contactY = b.y;
        double down = -b.vz;
        if (down > p.ballRollThresholdMps) {
            double wetScale = 1.0 - (1.0 - p.ballWetRestitutionScale) * b.saturation;
            b.vz = down * s.restitution * wetScale;
            b.vx *= s.bounceKeep;
            b.vy *= s.bounceKeep;
            b.spin *= s.bounceKeep;
        } else {
            b.vz = 0;
            b.state = b.vx != 0 || b.vy != 0 ? BallComponent.ROLLING : BallComponent.REST;
        }
        return dt - t;
    }

    /** First t in [0, dt] with z + vz t + az t^2 / 2 = 0, given that it is <= 0 at dt. */
    private static double contactTime(double z, double vz, double az, double dt) {
        if (z <= 0 && vz <= 0) {
            return 0;
        }
        double t;
        if (az < 0) {
            double disc = vz * vz - 2.0 * az * z;
            t = (vz + Math.sqrt(disc > 0 ? disc : 0)) / -az;
        } else {
            // Falling faster than terminal speed (drag pushes up): bisection, rare and bounded.
            double lo = 0;
            double hi = dt;
            for (int i = 0; i < 60; i++) {
                double mid = 0.5 * (lo + hi);
                if (z + vz * mid + 0.5 * az * mid * mid > 0) {
                    lo = mid;
                } else {
                    hi = mid;
                }
            }
            t = hi;
        }
        return t < 0 ? 0 : (t > dt ? dt : t);
    }

    /** Rolls with constant deceleration for {@code dt}; stops exactly at v^2 / (2 a). */
    private void roll(BallComponent b, SurfaceParams s, double dt) {
        double hs = Math.sqrt(b.vx * b.vx + b.vy * b.vy);
        if (hs <= 0) {
            b.vx = 0;
            b.vy = 0;
            b.state = BallComponent.REST;
            return;
        }
        double decel = p.ballRollingResistance * s.rollDrag * p.gravity;
        double dv = decel * dt;
        double ux = b.vx / hs;
        double uy = b.vy / hs;
        if (hs <= dv) {
            double d = hs * hs / (2.0 * decel);
            b.x += ux * d;
            b.y += uy * d;
            b.vx = 0;
            b.vy = 0;
            b.state = BallComponent.REST;
        } else {
            double next = hs - dv;
            double d = 0.5 * (hs + next) * dt;
            b.x += ux * d;
            b.y += uy * d;
            b.vx = ux * next;
            b.vy = uy * next;
        }
    }
}
