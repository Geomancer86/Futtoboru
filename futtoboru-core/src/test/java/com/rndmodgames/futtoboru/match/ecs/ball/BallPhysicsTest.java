package com.rndmodgames.futtoboru.match.ecs.ball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.system.Log;

/**
 * E-11: ball physics against closed forms and BALL_AND_KIT_PHYSICS.md (BKP), determinism golden hash, cost.
 * Replaces the D20 spike ({@code match.ecs.spike.BallFlightSpikeTest}).
 */
class BallPhysicsTest {

    static final double DT = 1.0 / 60;

    /** Params without air: closed-form flight. */
    static PhysicsParams vacuum() {
        PhysicsParams p = new PhysicsParams();
        p.airDensity = 0;
        return p;
    }

    static BallComponent ball(PhysicsParams p) {
        return BallComponent.atRest(0, 0, p.ballDryMassKg);
    }

    /** Distance from the kick to the first landing. */
    static double carry(BallPhysics physics, PhysicsParams p, BallComponent b, double impulse, double elevationDeg) {
        physics.kick(b, impulse, StrictMath.toRadians(elevationDeg), 0, 0);
        int contacts = b.groundContacts;
        for (int i = 0; i < 6000; i++) {
            physics.step(b, p.dry, DT);
            if (b.groundContacts > contacts) {
                return b.contactX;
            }
        }
        throw new AssertionError("never landed");
    }

    // ---- flight --------------------------------------------------------------------------------------------------

    @Test
    void withoutAirTheFlightIsTheClosedFormProjectile() {
        PhysicsParams p = vacuum();
        BallPhysics physics = new BallPhysics(p);
        BallComponent b = ball(p);
        double impulse = 8.0;
        double el = StrictMath.toRadians(40);
        physics.kick(b, impulse, el, 0, 0);
        double v0 = impulse / p.ballDryMassKg;
        double vx = v0 * StrictMath.cos(el);
        double vz = v0 * StrictMath.sin(el);
        double flightTime = 2 * vz / p.gravity;
        int steps = (int) (flightTime / DT);
        for (int i = 1; i <= steps; i++) {
            physics.step(b, p.dry, DT);
            double t = i * DT;
            assertEquals(vx * t, b.x, 1e-9, "x at " + t);
            assertEquals(vz * t - 0.5 * p.gravity * t * t, b.z, 1e-9, "z at " + t);
            assertEquals(vz - p.gravity * t, b.vz, 1e-9, "vz at " + t);
        }
        double range = carry(physics, p, ball(p), impulse, 40);
        assertEquals(v0 * v0 * StrictMath.sin(2 * el) / p.gravity, range, 1e-9, "range");
    }

    @Test
    void dragShortensTheCarry() {
        PhysicsParams air = new PhysicsParams();
        double withAir = carry(new BallPhysics(air), air, ball(air), 8.0, 35);
        PhysicsParams none = vacuum();
        double without = carry(new BallPhysics(none), none, ball(none), 8.0, 35);
        assertTrue(withAir < without * 0.95 && withAir > without * 0.5, withAir + " vs " + without);
    }

    @Test
    void dragGivesTheTerminalSpeed() {
        PhysicsParams p = new PhysicsParams();
        BallPhysics physics = new BallPhysics(p);
        BallComponent b = ball(p);
        b.z = 1e6;
        b.state = BallComponent.AIR;
        for (int i = 0; i < 60 * 120; i++) {
            physics.step(b, p.dry, DT);
        }
        double area = Math.PI * p.ballRadiusM * p.ballRadiusM;
        double terminal = Math.sqrt(2 * p.ballDryMassKg * p.gravity / (p.airDensity * p.ballDragCoefficient * area));
        assertEquals(terminal, -b.vz, 1e-6);
    }

    @Test
    void sideSpinCurlsTheBallLeftAndRight() {
        PhysicsParams p = new PhysicsParams();
        BallPhysics physics = new BallPhysics(p);
        double[] sideways = new double[3];
        double[] spins = {8, 0, -8};
        for (int k = 0; k < 3; k++) {
            BallComponent b = ball(p);
            physics.kick(b, 9.0, StrictMath.toRadians(20), 0, spins[k]);
            for (int i = 0; i < 60 && b.state == BallComponent.AIR; i++) {
                physics.step(b, p.dry, DT);
            }
            sideways[k] = b.y;
        }
        assertTrue(sideways[0] > 0.3, "left curl " + sideways[0]);
        assertEquals(0, sideways[1], 0);
        assertEquals(-sideways[0], sideways[2], 1e-12);
    }

    // ---- wet ball ------------------------------------------------------------------------------------------------

    @Test
    void theMassCurveMatchesTheMeasuredDataPoints() {
        PhysicsParams p = new PhysicsParams();
        BallPhysics physics = new BallPhysics(p);
        double m0 = p.ballDryMassKg;
        // FA test 1953: uncoated ball, wet day, +19 % after 45 min (wetness 0.36).
        assertEquals(1.19, physics.massAt(m0, 0.36 * 45) / m0, 0.005);
        // Sussex Express 1909: +47 % after 90 min (heavy conditions, wetness 0.8).
        assertEquals(1.47, physics.massAt(m0, 0.8 * 90) / m0, 0.005);
        // PH-25 lab: full submersion reaches ~90 % of saturation (+55.8 %) within 90 min.
        assertEquals(0.90, physics.saturationAt(90), 0.005);
        assertEquals(m0 * 1.56, physics.massAt(m0, 1e9), 1e-12);
        // BKP table, 400 g ball: drizzle 466 g, rain 526 g, waterlogged 589 g at 90 min.
        assertEquals(0.466, physics.massAt(m0, PitchConditions.DRIZZLE * 90), 0.0005);
        assertEquals(0.526, physics.massAt(m0, PitchConditions.RAIN * 90), 0.0005);
        assertEquals(0.589, physics.massAt(m0, PitchConditions.WATERLOGGED * 90), 0.0005);
        // The soak curve passes 1 - 1/e at tau.
        assertEquals(1 - StrictMath.exp(-1), physics.saturationAt(p.ballUptakeTauMin), 1e-15);
    }

    @Test
    void theBallSoaksTickByTickInsideTheWorld() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.WET, PitchConditions.RAIN),
                RandomSource.seeded(1).fork("match:soak"));
        MatchSystems.installSimulation(w);
        BallComponent b = BallComponent.atRest(0, 0, 0.4);
        w.spawn(SpawnRole.BALL).add(b);
        w.runTicks(45L * 60 * 30);
        assertEquals(0.4 * 1.19, b.massKg, 0.4 * 0.005);
        assertEquals(PitchConditions.RAIN * 45, b.exposureMin, 1e-9);
    }

    @Test
    void aWetBallLeavesSlowerAndCarriesShorterForTheSameKick() {
        PhysicsParams p = new PhysicsParams();
        BallPhysics physics = new BallPhysics(p);
        BallComponent dry = ball(p);
        BallComponent wet = ball(p);
        physics.soak(wet, PitchConditions.RAIN, 90 * 60);
        assertEquals(0.526, wet.massKg, 0.0005);

        // Same impulse: launch speed J / m.
        double dryCarry = carry(physics, p, dry, 8.0, 30);
        double wetCarry = carry(physics, p, wet, 8.0, 30);
        assertTrue(dryCarry > 20 && dryCarry < 40, "dry carry " + dryCarry);
        assertTrue(wetCarry < dryCarry * 0.75, "wet " + wetCarry + " vs dry " + dryCarry);

        // Same swing (BKP §1.5): the 526 g ball comes off the foot ~7 % slower than the 400 g one.
        double vDry = physics.footImpulse(20, 0.400) / 0.400;
        double vWet = physics.footImpulse(20, 0.526) / 0.526;
        assertEquals(0.927, vWet / vDry, 0.002);
        BallComponent dry2 = ball(p);
        BallComponent wet2 = ball(p);
        physics.soak(wet2, PitchConditions.RAIN, 90 * 60);
        double swingDry = carry(physics, p, dry2, physics.footImpulse(20, dry2.massKg), 30);
        double swingWet = carry(physics, p, wet2, physics.footImpulse(20, wet2.massKg), 30);
        assertTrue(swingWet < swingDry, swingWet + " vs " + swingDry);
    }

    // ---- bounce and roll -----------------------------------------------------------------------------------------

    /** Height of the first rebound of a ball dropped from {@code h} metres. */
    static double rebound(PhysicsParams p, Surface s, double saturation, double h) {
        BallPhysics physics = new BallPhysics(p);
        BallComponent b = ball(p);
        b.saturation = saturation;
        b.z = h;
        b.state = BallComponent.AIR;
        boolean bounced = false;
        double apex = 0;
        for (int i = 0; i < 600; i++) {
            physics.step(b, p.surface(s), DT / 8);
            if (b.vz > 0) {
                bounced = true;
            }
            if (bounced) {
                apex = Math.max(apex, b.z);
                if (b.vz < 0) {
                    return apex;
                }
            }
        }
        throw new AssertionError("no rebound on " + s);
    }

    @Test
    void reboundHeightIsRestitutionSquaredAndOrderedBySurface() {
        PhysicsParams p = vacuum();
        double[] heights = new double[Surface.values().length];
        for (Surface s : Surface.values()) {
            double e = p.surface(s).restitution;
            heights[s.ordinal()] = rebound(p, s, 0, 2.0);
            // Apex sampled at substep resolution: within a few mm of e^2 * h.
            assertEquals(e * e * 2.0, heights[s.ordinal()], 0.003, s.toString());
        }
        assertTrue(heights[Surface.FROZEN.ordinal()] > heights[Surface.DRY.ordinal()]);
        assertTrue(heights[Surface.DRY.ordinal()] > heights[Surface.WET.ordinal()]);
        assertTrue(heights[Surface.WET.ordinal()] > heights[Surface.MUDDY.ordinal()]);
        // A saturated ball bounces with 0.875 of the restitution.
        double soaked = rebound(p, Surface.DRY, 1.0, 2.0);
        double e = p.dry.restitution * p.ballWetRestitutionScale;
        assertEquals(e * e * 2.0, soaked, 0.003);
    }

    @Test
    void rollingDistanceIsVSquaredOverTwoMuGAndMudStopsTheBallDead() {
        PhysicsParams p = new PhysicsParams();
        BallPhysics physics = new BallPhysics(p);
        double[] stops = new double[Surface.values().length];
        for (Surface s : Surface.values()) {
            BallComponent b = ball(p);
            physics.kick(b, 0.4 * 10, 0, 0, 0); // 10 m/s along the ground
            assertEquals(BallComponent.ROLLING, b.state);
            for (int i = 0; i < 60 * 120 && b.state != BallComponent.REST; i++) {
                physics.step(b, p.surface(s), DT);
            }
            assertEquals(BallComponent.REST, b.state, s.toString());
            double mu = p.ballRollingResistance * p.surface(s).rollDrag;
            assertEquals(100 / (2 * mu * p.gravity), b.x, 1e-9, s.toString());
            stops[s.ordinal()] = b.x;
        }
        assertTrue(stops[Surface.MUDDY.ordinal()] < stops[Surface.WET.ordinal()]);
        assertTrue(stops[Surface.WET.ordinal()] < stops[Surface.DRY.ordinal()]);
        assertTrue(stops[Surface.DRY.ordinal()] < stops[Surface.FROZEN.ordinal()]);
    }

    @Test
    void aLoftedKickBouncesRollsAndComesToRestOnEverySurface() {
        PhysicsParams p = new PhysicsParams();
        BallPhysics physics = new BallPhysics(p);
        double[] rest = new double[Surface.values().length];
        for (Surface s : Surface.values()) {
            BallComponent b = ball(p);
            physics.kick(b, 6.0, StrictMath.toRadians(35), 0, 0);
            for (int i = 0; i < 60 * 120 && b.state != BallComponent.REST; i++) {
                physics.step(b, p.surface(s), DT);
            }
            assertEquals(BallComponent.REST, b.state, s.toString());
            assertEquals(0, b.z, 0);
            rest[s.ordinal()] = b.x;
        }
        assertTrue(rest[Surface.MUDDY.ordinal()] < rest[Surface.WET.ordinal()]);
        assertTrue(rest[Surface.WET.ordinal()] < rest[Surface.DRY.ordinal()]);
        assertTrue(rest[Surface.DRY.ordinal()] < rest[Surface.FROZEN.ordinal()]);
    }

    // ---- determinism ---------------------------------------------------------------------------------------------

    /**
     * Ninety seconds per surface in a soaked second half: a kick with alternating side spin every 10 s, each tick's
     * state hash folded into one value (4 x 2,700 ticks).
     */
    static long scriptedHash() {
        long folded = 0;
        for (Surface s : Surface.values()) {
            MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(s, PitchConditions.WATERLOGGED),
                    RandomSource.seeded(1888L).fork("match:golden"));
            MatchSystems.installSimulation(w);
            BallComponent b = BallComponent.atRest(0, 0, w.params().ballDryMassKg * 1.1);
            w.spawn(SpawnRole.BALL).add(b);
            BallPhysics physics = w.engine().getSystem(BallSystem.class).physics();
            for (int k = 0; k < 9; k++) {
                physics.kick(b, 6.0 + 0.4 * k, StrictMath.toRadians(10 + 4 * k), StrictMath.toRadians(40 * k),
                        (k % 2 == 0 ? 1 : -1) * 6.0);
                for (int t = 0; t < 300; t++) {
                    w.tick();
                    folded = folded * 31 + w.stateHash();
                }
            }
        }
        return folded;
    }

    // ---- goal frame (E-16b) ----------------------------------------------------------------------------------------

    /** A world with only the ball (no laws), ball rolling or flying from (x, y, z) at (vx, vy, vz). */
    static BallComponent frameShot(MatchWorld[] out, double x, double y, double z, double vx, double vy, double vz) {
        PhysicsParams p = vacuum();
        MatchWorld w = new MatchWorld(p, new UniformPitch(Surface.DRY, 0), RandomSource.seeded(3).fork("match:frame"));
        MatchSystems.installSimulation(w);
        BallComponent b = BallComponent.atRest(x, y, p.ballDryMassKg);
        b.z = z;
        b.vx = vx;
        b.vy = vy;
        b.vz = vz;
        b.state = z > 0 || vz > 0 ? BallComponent.AIR : BallComponent.ROLLING;
        w.spawn(SpawnRole.BALL).add(b);
        out[0] = w;
        return b;
    }

    @Test
    void aShotStraightAtThePostBouncesBackIntoPlayWithRigidRestitution() {
        PitchGeometry g = new PitchGeometry(PitchParams.defaults1888());
        MatchWorld[] w = new MatchWorld[1];
        // rolling at 20 m/s along the pitch, dead centre on the north post of the east goal
        BallComponent b = frameShot(w, g.halfLength() - 10, g.postCentreY(), 0, 20, 0, 0);
        BallSystem balls = w[0].engine().getSystem(BallSystem.class);
        boolean hit = false;
        for (int t = 0; t < 30 && !hit; t++) {
            w[0].runTicks(1);
            hit = balls.frameContactThisTick();
        }
        assertTrue(hit, "the ball reached the post");
        assertEquals(1, balls.frameContacts());
        assertTrue(b.x < g.halfLength(), "still in front of the goal line: " + b.x);
        // rolling resistance only slows it a little over 10 m: the rebound is about -e times the arrival speed
        assertTrue(b.vx < -0.75 * 0.80 * 20 && b.vx > -0.80 * 20, "rebound " + b.vx);
        assertEquals(0, b.vy, 1e-9);
        w[0].runTicks(30);
        assertTrue(b.x < g.halfLength() - 5, "it came back out: " + b.x);
    }

    @Test
    void aBallUnderTheBarAndInsideThePostsIsNotTouchedAndOneOnTheBarComesDown() {
        PitchGeometry g = new PitchGeometry(PitchParams.defaults1888());
        MatchWorld[] w = new MatchWorld[1];
        BallComponent in = frameShot(w, g.halfLength() - 10, g.goalMouthHalfWidth() - 0.2, 0, 20, 0, 0);
        w[0].runTicks(40);
        assertEquals(0, w[0].engine().getSystem(BallSystem.class).frameContacts());
        assertTrue(in.x > g.halfLength() + 1, "went through the mouth: " + in.x);
        // flat and fast at the height of the bar axis (ball centre = z + r): it hits the bar and comes back
        double r = new PhysicsParams().ballRadiusM;
        BallComponent bar = frameShot(w, g.halfLength() - 3, 0, g.crossbarCentreZ() - r, 25, 0, 0);
        BallSystem balls = w[0].engine().getSystem(BallSystem.class);
        w[0].runTicks(10);
        assertTrue(balls.frameContacts() >= 1, "hit the bar");
        assertTrue(bar.vx < 0, "rebounded: " + bar.vx);
        assertTrue(bar.x < g.halfLength(), "in front of the line: " + bar.x);
    }

    /**
     * Doubles (strict since Java 17) and StrictMath must give the same bits on Windows x64, macOS arm64 and Linux CI.
     * If this fails on one machine only, the determinism premise is broken there: do not just update the number. Update
     * it only in a PR that intentionally changes the physics (and bump EcsMatchEngine.ENGINE_VERSION).
     * Re-pinned by E-14 (ecs-0.3): the full simulation now includes the action system, which adds a
     * {@code PossessionComponent} to the ball (hashed); the ball's flight is unchanged.
     */
    static final long GOLDEN = 0x374bc8c5e47bad57L;

    @Test
    void sameInputGivesTheSameBitsOnEveryMachine() {
        long a = scriptedHash();
        assertEquals(a, scriptedHash());
        assertEquals(GOLDEN, a, () -> "hash " + Long.toHexString(a));
    }

    // ---- cost ----------------------------------------------------------------------------------------------------

    @Test
    void costAndAllocationPerTick() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.WET, PitchConditions.RAIN),
                RandomSource.seeded(7).fork("match:bench"));
        MatchSystems.installSimulation(w);
        BallComponent b = BallComponent.atRest(0, 0, 0.4);
        w.spawn(SpawnRole.BALL).add(b);
        BallPhysics physics = w.engine().getSystem(BallSystem.class).physics();
        int ticks = 30 * 60 * 10; // ten simulated minutes per round
        for (int round = 0; round < 3; round++) { // JIT warm-up
            run(w, b, physics, ticks);
        }
        com.sun.management.ThreadMXBean mx = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long tid = Thread.currentThread().getId();
        long bytes0 = mx.getThreadAllocatedBytes(tid);
        long t0 = System.nanoTime();
        run(w, b, physics, ticks);
        long ns = System.nanoTime() - t0;
        long bytes = mx.getThreadAllocatedBytes(tid) - bytes0;
        double nsPerTick = ns / (double) ticks;
        Log.info(Log.ENGINE_MATCH, "BallPhysicsTest", String.format(java.util.Locale.ROOT,
                "ECS ball world: %.3f us per tick (tick + state hash), %d bytes allocated over %d ticks,"
                        + " %.1f ms per 90 min at %d Hz",
                nsPerTick / 1000, bytes, ticks, nsPerTick * 162_000 / 1e6, w.params().tickHz));
        // Generous ceilings (machine dependent): fail only on a gross regression or per-tick garbage.
        assertTrue(nsPerTick < 50_000, "ns per tick " + nsPerTick);
        assertTrue(bytes < 64 * 1024, "allocated " + bytes + " bytes over " + ticks + " ticks");
    }

    private static long run(MatchWorld w, BallComponent b, BallPhysics physics, int ticks) {
        long sink = 0;
        for (int t = 0; t < ticks; t++) {
            if (t % 150 == 0) {
                physics.kick(b, 5.0, 0.4, 0.001 * t, 3.0);
            }
            w.tick();
            sink += w.stateHash();
        }
        return sink;
    }
}
