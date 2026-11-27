package com.rndmodgames.futtoboru.match.ecs.spike;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Spike for docs/design/MATCH_ENGINE_ECS.md (D20), test scope only, not engine code.
 *
 * Question it answers: can the match simulation be plain Java inside a headless Ashley engine (no Box2D natives, no
 * GL), at a fixed step, bit-for-bit reproducible across machines, with a 2.5D ball (height, gravity, drag, spin,
 * bounce/roll per pitch) and a ball that gains mass when wet, and how much does a tick cost?
 *
 * The physics here is deliberately small and the numbers are placeholders until lane T's
 * docs/data/history/BALL_AND_KIT_PHYSICS.md; the golden hash pins the arithmetic so that BOOK3PRO, WORKSTATION and CI
 * prove they compute the same match.
 */
class BallFlightSpikeTest {

    static final double STEP = 1.0 / 60.0;
    static final double G = 9.81;
    static final double AIR_DENSITY = 1.225;

    /** Pitch surfaces: restitution of a bounce, horizontal speed kept by a bounce, rolling resistance coefficient. */
    enum Surface {
        FIRM(0.62, 0.80, 0.030), WET(0.50, 0.85, 0.045), MUDDY(0.30, 0.55, 0.140), FROZEN(0.70, 0.92, 0.015);

        final double restitution;
        final double bounceKeep;
        final double rolling;

        Surface(double restitution, double bounceKeep, double rolling) {
            this.restitution = restitution;
            this.bounceKeep = bounceKeep;
            this.rolling = rolling;
        }
    }

    /** Simulation state of the ball, in doubles and SI units. */
    static final class BallState implements Component {
        double x, y, z, vx, vy, vz;
        /** Spin about the vertical axis (rad/s): curls the ball sideways in flight (Magnus). */
        double spin;
        double dryMass = 0.40;
        /** Water taken up so far (kg). */
        double water;
        double radius = 0.11;
        double dragCoefficient = 0.30;
        double magnusCoefficient = 0.00025;
        /** Max water a fully soaked leather ball holds, as a share of its dry mass (placeholder). */
        double maxWaterShare = 0.35;
        /** Seconds of play on a wet pitch to take up ~63 % of the max water (placeholder). */
        double soakTau = 1800;

        double mass() {
            return dryMass + water;
        }
    }

    /** Fixed-step ball physics: one Ashley system, reads match conditions, writes only simulation state. */
    static final class BallPhysicsSystem extends EntitySystem {
        private final ComponentMapper<BallState> balls = ComponentMapper.getFor(BallState.class);
        private ImmutableArray<Entity> entities;
        private final Surface surface;
        /** 0 = dry day, 1 = steady rain. */
        private final double wetness;

        BallPhysicsSystem(Surface surface, double wetness) {
            this.surface = surface;
            this.wetness = wetness;
        }

        @Override
        public void addedToEngine(Engine engine) {
            entities = engine.getEntitiesFor(Family.all(BallState.class).get());
        }

        @Override
        public void update(float ignoredFrameDelta) {
            // The simulation never uses the frame delta: one call = one fixed step.
            for (int i = 0; i < entities.size(); i++) {
                step(balls.get(entities.get(i)), STEP);
            }
        }

        void step(BallState b, double dt) {
            // Water uptake: first-order soak toward the max, scaled by how wet the day is. StrictMath for exp:
            // Math.exp may be an intrinsic that differs by CPU; StrictMath is fdlibm, the same bits everywhere.
            double target = b.maxWaterShare * b.dryMass * wetness;
            b.water += (target - b.water) * (1 - StrictMath.exp(-dt / b.soakTau));

            double m = b.mass();
            double speed = Math.sqrt(b.vx * b.vx + b.vy * b.vy + b.vz * b.vz);
            double ax = 0, ay = 0, az = 0;
            boolean airborne = b.z > 0 || b.vz > 0;
            if (airborne) {
                double area = Math.PI * b.radius * b.radius;
                double drag = 0.5 * AIR_DENSITY * b.dragCoefficient * area * speed / m;
                ax -= drag * b.vx;
                ay -= drag * b.vy;
                az -= drag * b.vz + G;
                // Magnus from vertical-axis spin: perpendicular to the horizontal velocity.
                double magnus = b.magnusCoefficient * b.spin / m;
                ax += -magnus * b.vy;
                ay += magnus * b.vx;
                b.spin *= 1 - 0.05 * dt;
            } else {
                double hs = Math.sqrt(b.vx * b.vx + b.vy * b.vy);
                if (hs > 0) {
                    double decel = Math.min(hs / dt, surface.rolling * G);
                    ax -= decel * b.vx / hs;
                    ay -= decel * b.vy / hs;
                }
                b.spin *= 1 - 2.0 * dt;
            }
            // Semi-implicit Euler.
            b.vx += ax * dt;
            b.vy += ay * dt;
            b.vz += az * dt;
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            b.z += b.vz * dt;
            if (b.z < 0) {
                b.z = 0;
                if (b.vz < -0.8) {
                    // A wet, heavier ball bounces a little less: placeholder linear factor on the restitution.
                    double soaked = b.water / (b.maxWaterShare * b.dryMass);
                    b.vz = -b.vz * surface.restitution * (1 - 0.15 * soaked);
                    b.vx *= surface.bounceKeep;
                    b.vy *= surface.bounceKeep;
                } else {
                    b.vz = 0;
                }
            }
        }
    }

    /** A crude stand-in for 22 players: point bodies that seek the ball with capped speed/acceleration and separate. */
    static final class Runner implements Component {
        double x, y, vx, vy, maxSpeed, maxAccel;
    }

    static final class RunnerSystem extends EntitySystem {
        private final ComponentMapper<Runner> runners = ComponentMapper.getFor(Runner.class);
        private final ComponentMapper<BallState> balls = ComponentMapper.getFor(BallState.class);
        private ImmutableArray<Entity> people;
        private ImmutableArray<Entity> ball;

        @Override
        public void addedToEngine(Engine engine) {
            people = engine.getEntitiesFor(Family.all(Runner.class).get());
            ball = engine.getEntitiesFor(Family.all(BallState.class).get());
        }

        @Override
        public void update(float ignored) {
            BallState b = balls.get(ball.get(0));
            int n = people.size();
            for (int i = 0; i < n; i++) {
                Runner r = runners.get(people.get(i));
                double dx = b.x - r.x, dy = b.y - r.y;
                // Separation: ordered O(n^2) pass, index order = entity insertion order (deterministic).
                for (int j = 0; j < n; j++) {
                    if (j == i) {
                        continue;
                    }
                    Runner o = runners.get(people.get(j));
                    double sx = r.x - o.x, sy = r.y - o.y, d2 = sx * sx + sy * sy;
                    if (d2 < 4 && d2 > 1e-9) {
                        dx += sx * 4 / d2;
                        dy += sy * 4 / d2;
                    }
                }
                double len = Math.sqrt(dx * dx + dy * dy);
                if (len > 1e-9) {
                    double dvx = dx / len * r.maxSpeed - r.vx, dvy = dy / len * r.maxSpeed - r.vy;
                    double dv = Math.sqrt(dvx * dvx + dvy * dvy), cap = r.maxAccel * STEP;
                    if (dv > cap) {
                        dvx *= cap / dv;
                        dvy *= cap / dv;
                    }
                    r.vx += dvx;
                    r.vy += dvy;
                }
                r.x += r.vx * STEP;
                r.y += r.vy * STEP;
            }
        }
    }

    // ---- harness -----------------------------------------------------------------------------------------------

    static final class Sim {
        final Engine engine = new Engine();
        final BallState ball = new BallState();
        final java.util.List<Runner> people = new java.util.ArrayList<>();
        long hash = 0xcbf29ce484222325L;

        Sim(Surface surface, double wetness, int runners, long seed) {
            Entity e = new Entity();
            e.add(ball);
            engine.addEntity(e);
            RandomSource rng = RandomSource.seeded(seed).fork("match:spike");
            for (int i = 0; i < runners; i++) {
                Runner r = new Runner();
                r.x = -50 + 100 * rng.nextDouble();
                r.y = -32 + 64 * rng.nextDouble();
                r.maxSpeed = 6.5 + rng.nextDouble();
                r.maxAccel = 3.0 + rng.nextDouble();
                Entity p = new Entity();
                p.add(r);
                engine.addEntity(p);
                people.add(r);
            }
            engine.addSystem(new BallPhysicsSystem(surface, wetness));
            if (runners > 0) {
                engine.addSystem(new RunnerSystem());
            }
        }

        /** A kick is an impulse (N*s) from the boot, so the launch speed falls as the ball gets heavier. */
        void kick(double impulse, double elevationDeg, double headingDeg, double spin) {
            double v0 = impulse / ball.mass();
            double el = StrictMath.toRadians(elevationDeg), hd = StrictMath.toRadians(headingDeg);
            ball.vx = v0 * StrictMath.cos(el) * StrictMath.cos(hd);
            ball.vy = v0 * StrictMath.cos(el) * StrictMath.sin(hd);
            ball.vz = v0 * StrictMath.sin(el);
            ball.spin = spin;
        }

        void run(int ticks) {
            for (int t = 0; t < ticks; t++) {
                engine.update((float) STEP);
                mix(ball.x);
                mix(ball.y);
                mix(ball.z);
                mix(ball.water);
                for (Runner r : people) {
                    mix(r.x);
                    mix(r.y);
                }
            }
        }

        private void mix(double v) {
            hash ^= Double.doubleToLongBits(v);
            hash *= 0x100000001b3L;
        }

        /** Distance from the kick spot to where the ball first lands. */
        double carry(double impulse, double elevationDeg) {
            kick(impulse, elevationDeg, 0, 0);
            double startX = ball.x;
            ball.z = 1e-9;
            for (int t = 0; t < 600; t++) {
                engine.update((float) STEP);
                if (ball.z == 0) {
                    return ball.x - startX;
                }
            }
            return Double.NaN;
        }
    }

    /** Ninety seconds of play: a lofted kick with side spin every 10 s, on a wet heavy pitch, 22 runners. */
    static long scriptedHash() {
        Sim sim = new Sim(Surface.MUDDY, 1.0, 22, 1888L);
        sim.ball.water = 0.5 * sim.ball.maxWaterShare * sim.ball.dryMass; // second half: half soaked
        for (int k = 0; k < 9; k++) {
            sim.kick(9.0 + k * 0.3, 18 + 3 * k, 10 * k, (k % 2 == 0 ? 1 : -1) * 6.0);
            sim.run(600);
        }
        return sim.hash;
    }

    @Test
    void sameInputGivesTheSameBitsOnEveryMachine() {
        long a = scriptedHash();
        assertEquals(a, scriptedHash());
        // Golden value: pure Java doubles (strict since Java 17) + StrictMath for transcendental functions must give
        // the same bits on Windows x64, macOS arm64 and the Linux CI. If this fails on one machine only, the design's
        // determinism premise is broken there; do not just update the number.
        assertEquals(GOLDEN, a, () -> "hash " + Long.toHexString(a));
    }

    static final long GOLDEN = 0xa016f0f7a1246a92L;

    @Test
    void aWetBallFliesShorterForTheSameKick() {
        double dry = new Sim(Surface.FIRM, 0, 0, 1).carry(10.0, 30);
        Sim soaked = new Sim(Surface.FIRM, 1, 0, 1);
        soaked.ball.water = soaked.ball.maxWaterShare * soaked.ball.dryMass;
        double wet = soaked.carry(10.0, 30);
        assertTrue(dry > 20 && dry < 40, "dry carry " + dry);
        assertTrue(wet < dry * 0.75, "wet " + wet + " vs dry " + dry);
    }

    @Test
    void theBallSoaksUpWaterOverAMatch() {
        Sim sim = new Sim(Surface.WET, 1.0, 0, 1);
        sim.run(45 * 60 * 60);
        double halfTime = sim.ball.mass();
        sim.run(45 * 60 * 60);
        double fullTime = sim.ball.mass();
        assertTrue(halfTime > 0.40 * 1.2 && fullTime > halfTime && fullTime < 0.40 * 1.35 + 1e-9,
                "mass " + halfTime + " -> " + fullTime);
    }

    @Test
    void mudKillsTheBounceAndTheRoll() {
        double[] rest = new double[Surface.values().length];
        for (Surface s : Surface.values()) {
            Sim sim = new Sim(s, 0, 0, 1);
            sim.kick(10.0, 35, 0, 0);
            sim.ball.z = 1e-9;
            sim.run(60 * 90);
            assertEquals(0, sim.ball.z, 0, s + " still in the air");
            assertTrue(Math.abs(sim.ball.vx) < 1e-6, s + " still rolling " + sim.ball.vx);
            rest[s.ordinal()] = sim.ball.x;
        }
        assertTrue(rest[Surface.MUDDY.ordinal()] < rest[Surface.FIRM.ordinal()]);
        assertTrue(rest[Surface.FIRM.ordinal()] < rest[Surface.FROZEN.ordinal()]);
    }

    @Test
    void costOfOneSimulatedMinuteWith22Runners() {
        Sim sim = new Sim(Surface.FIRM, 0, 22, 7);
        sim.run(60 * 60 * 5); // warm-up for the JIT
        long t0 = System.nanoTime();
        int ticks = 60 * 60 * 10;
        sim.run(ticks);
        double nsPerTick = (System.nanoTime() - t0) / (double) ticks;
        // Not asserted (machine dependent): reported for the design doc's budget.
        Log.info("TEST", "ECS spike: " + Math.round(nsPerTick) + " ns per tick (ball + 22 runners), "
                + Math.round(nsPerTick * 60 * 60 * 90 / 1e6) + " ms per 90 min at 60 Hz");
    }
}
