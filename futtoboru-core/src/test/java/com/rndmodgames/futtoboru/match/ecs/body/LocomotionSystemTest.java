package com.rndmodgames.futtoboru.match.ecs.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;

/** E-13 locomotion: acceleration, top speed, turn vs speed, grip per surface x sole, stamina, boots. */
class LocomotionSystemTest {

    static final double EPS = 1e-9;

    /** Sprints along +x for {@code ticks}; returns the speed per tick. */
    static double[] sprint(MatchWorld w, Entity e, int ticks) {
        double[] v = new double[ticks];
        Bodies.cmd(e).set(20, 0);
        for (int t = 0; t < ticks; t++) {
            w.tick();
            v[t] = Bodies.body(e).speed();
        }
        return v;
    }

    @Test
    void accelerationIsLimitedAndTopSpeedIsNeverExceeded() {
        MatchWorld w = Bodies.simWorld(Surface.DRY, 0);
        Entity e = Bodies.add(w, 0, 0);
        AthleteComponent a = Bodies.athlete(e);
        double[] v = sprint(w, e, 30 * 8);
        double dt = w.tickSeconds();
        assertEquals(a.accel * dt, v[0], 1e-12, "first tick: full acceleration from standstill");
        double peak = 0;
        int t95 = -1;
        for (int t = 0; t < v.length; t++) {
            double prev = t == 0 ? 0 : v[t - 1];
            assertTrue(v[t] - prev <= a.accel * dt + EPS, "tick " + t);
            assertTrue(v[t] <= a.topSpeed + EPS, "tick " + t + " speed " + v[t]);
            peak = Math.max(peak, v[t]);
            if (t95 < 0 && v[t] >= 0.9 * a.topSpeed) {
                t95 = t;
            }
        }
        // A sprinter reaches ~90 % of his top speed in 2-5 s (D) and stamina then trims it.
        assertTrue(t95 > 2 * 30 && t95 < 5 * 30, "ticks to 90 %: " + t95);
        assertTrue(peak > 0.93 * a.topSpeed, "peak " + peak + " of " + a.topSpeed);
        assertTrue(v[v.length - 1] < peak, "stamina trims a long sprint");

        // a faster rating runs faster
        MatchWorld w2 = Bodies.simWorld(Surface.DRY, 0);
        Entity fast = Bodies.add(w2, AthleteInputs.stub(Position.HALF_BACK).withSpeed(23),
                Bodies.kit(Studs.BARS, false), 0, 0, 0);
        double[] vf = sprint(w2, fast, 30 * 8);
        assertTrue(max(vf) > peak + 0.5, max(vf) + " vs " + peak);
    }

    @Test
    void brakingToAStandstill() {
        MatchWorld w = Bodies.simWorld(Surface.DRY, 0);
        Entity e = Bodies.add(w, 0, 0);
        sprint(w, e, 30 * 4);
        double v0 = Bodies.body(e).speed();
        Bodies.cmd(e).stop();
        int ticks = 0;
        while (Bodies.body(e).speed() > 0 && ticks < 300) {
            w.tick();
            ticks++;
        }
        AthleteComponent a = Bodies.athlete(e);
        assertEquals(0, Bodies.body(e).speed(), 0);
        double expected = 30 * v0 / a.decel;
        assertTrue(ticks >= expected - 1 && ticks <= expected + 2, "braking took " + ticks + " ticks, " + expected);
    }

    /** Ticks to turn the velocity 90 degrees, starting at {@code runUpTicks} of sprinting along +x. */
    static int ticksToTurn(Surface surface, Studs studs, int runUpTicks, double[] maxTurnStep) {
        MatchWorld w = Bodies.simWorld(surface, 0);
        Entity e = Bodies.add(w, AthleteInputs.stub(Position.HALF_BACK), Bodies.kit(studs, false), 0, 0, 0);
        sprint(w, e, runUpTicks);
        BodyComponent b = Bodies.body(e);
        AthleteComponent a = Bodies.athlete(e);
        Bodies.cmd(e).set(0, 20);
        double heading = b.facing;
        for (int t = 1; t < 600; t++) {
            double v = b.speed();
            w.tick();
            double step = Math.abs(LocomotionSystem.wrap(b.facing - heading));
            assertTrue(step <= a.turnRate(v) * w.tickSeconds() + EPS, "turn step " + step + " at " + v);
            maxTurnStep[0] = Math.max(maxTurnStep[0], step);
            heading = b.facing;
            if (Math.abs(LocomotionSystem.wrap(heading - StrictMath.PI / 2)) < StrictMath.toRadians(5)
                    && b.speed() > 0.5) {
                return t;
            }
        }
        return 600;
    }

    @Test
    void turnRateFallsWithSpeed() {
        double[] s = new double[1];
        int standing = ticksToTurn(Surface.DRY, Studs.BARS, 0, s);
        int jogging = ticksToTurn(Surface.DRY, Studs.BARS, 20, s);
        int sprinting = ticksToTurn(Surface.DRY, Studs.BARS, 120, s);
        assertTrue(standing < jogging && jogging < sprinting,
                "standing " + standing + ", jogging " + jogging + ", sprinting " + sprinting);
        AthleteComponent a = AthleteComponent.from(AthleteInputs.stub(Position.HALF_BACK), new BodyParams());
        assertTrue(a.turnRate(0) > 3 * a.turnRate(a.topSpeed));
        assertEquals(a.turnAtTop, a.turnRate(2 * a.topSpeed), 0);
    }

    @Test
    void mudSlowsAndReducesGrip() {
        MatchWorld dryW = Bodies.simWorld(Surface.DRY, 0);
        Entity dry = Bodies.add(dryW, AthleteInputs.stub(Position.HALF_BACK), Bodies.kit(Studs.FLAT, false), 0, 0, 0);
        MatchWorld mudW = Bodies.simWorld(Surface.MUDDY, 0);
        Entity mud = Bodies.add(mudW, AthleteInputs.stub(Position.HALF_BACK), Bodies.kit(Studs.FLAT, false), 0, 0, 0);
        double[] vd = sprint(dryW, dry, 30 * 6);
        double[] vm = sprint(mudW, mud, 30 * 6);
        assertTrue(vm[0] < vd[0], "grip caps the first step in mud: " + vm[0] + " vs " + vd[0]);
        BodyParams p = new BodyParams();
        assertEquals(p.muddy.grip(Studs.FLAT) * 9.81 / 30, vm[0], 1e-12);
        assertTrue(max(vm) < 0.9 * max(vd), "mud top speed " + max(vm) + " vs " + max(vd));
        // the same 5 m/s jog for a minute tires more in mud
        Bodies.cmd(dry).set(5, 0);
        Bodies.cmd(mud).set(5, 0);
        Bodies.athlete(dry).stamina = 1;
        Bodies.athlete(mud).stamina = 1;
        double dryReserve = Bodies.athlete(dry).reserve;
        double mudReserve = Bodies.athlete(mud).reserve;
        dryW.runTicks(30 * 60);
        mudW.runTicks(30 * 60);
        assertTrue(Bodies.athlete(mud).stamina < Bodies.athlete(dry).stamina, "mud tires faster");
        assertTrue(mudReserve - Bodies.athlete(mud).reserve > dryReserve - Bodies.athlete(dry).reserve);

        double[] s = new double[1];
        assertTrue(ticksToTurn(Surface.MUDDY, Studs.FLAT, 90, s) > ticksToTurn(Surface.DRY, Studs.FLAT, 90, s),
                "a slippery pitch widens the turn");
    }

    @Test
    void barsAndStudsHelpOnWetAndSkateOnFrozen() {
        double[] s = new double[1];
        int flat = ticksToTurn(Surface.WET, Studs.FLAT, 90, s);
        int bars = ticksToTurn(Surface.WET, Studs.BARS, 90, s);
        assertTrue(bars < flat, "bars turn quicker on a wet pitch: " + bars + " vs " + flat);
        BodyParams p = new BodyParams();
        assertTrue(p.wet.grip(Studs.STUDS) > p.wet.grip(Studs.FLAT));
        assertTrue(p.muddy.grip(Studs.BARS) > p.muddy.grip(Studs.FLAT));
        assertTrue(p.frozen.grip(Studs.FLAT) > p.frozen.grip(Studs.BARS));
        int frozenFlat = ticksToTurn(Surface.FROZEN, Studs.FLAT, 90, s);
        int frozenStuds = ticksToTurn(Surface.FROZEN, Studs.STUDS, 90, s);
        assertTrue(frozenStuds > frozenFlat, "studs skate on frozen ground: " + frozenStuds + " vs " + frozenFlat);
    }

    @Test
    void staminaDrainsInSprintsAndRecoversWalking() {
        MatchWorld w = Bodies.simWorld(Surface.DRY, 0);
        Entity e = Bodies.add(w, 0, 0);
        AthleteComponent a = Bodies.athlete(e);
        double[] v = sprint(w, e, 30 * 30);
        assertTrue(a.stamina < 0.35, "30 s flat out nearly empties the tank: " + a.stamina);
        BodyParams p = new BodyParams();
        assertTrue(v[v.length - 1] < 0.85 * a.topSpeed, "tired: " + v[v.length - 1]);
        for (int t = 1; t < v.length; t++) {
            assertTrue(t < 30 * 8 || v[t] <= v[t - 1] + EPS, "slowing down as he tires, tick " + t);
        }
        double reserveAfterSprint = a.reserve;
        assertTrue(reserveAfterSprint < 1 && reserveAfterSprint > 0.95, "reserve " + reserveAfterSprint);

        // walk: short-term stamina comes back, the reserve keeps going down slowly
        Bodies.cmd(e).set(1.5, 0);
        double last = a.stamina;
        for (int t = 0; t < 30 * 40; t++) {
            w.tick();
            assertTrue(t < 90 || a.stamina >= last - EPS, "walking never drains: tick " + t); // after slowing down
            last = a.stamina;
        }
        assertTrue(a.stamina > 0.95, "recovered: " + a.stamina);
        assertTrue(a.reserve < reserveAfterSprint);

        a.halfTime(p);
        assertEquals(1, a.stamina, 0);
        assertTrue(a.reserve > reserveAfterSprint - 0.01);

        // a better stamina rating lasts longer
        MatchWorld w2 = Bodies.simWorld(Surface.DRY, 0);
        AthleteInputs fit = new AthleteInputs(Position.HALF_BACK, 13, 13, 13, 23, 13, 13, 25, 1, false);
        Entity e2 = Bodies.add(w2, fit, Bodies.kit(Studs.BARS, false), 0, 0, 0);
        sprint(w2, e2, 30 * 15);
        MatchWorld w3 = Bodies.simWorld(Surface.DRY, 0);
        Entity e3 = Bodies.add(w3, 0, 0);
        sprint(w3, e3, 30 * 15);
        assertTrue(Bodies.athlete(e2).stamina > Bodies.athlete(e3).stamina);
    }

    @Test
    void wetBootsGetHeavierAndHeavyBootsAccelerateSlowerButKickHarder() {
        BodyParams p = new BodyParams();
        KitComponent boots = Bodies.kit(Studs.BARS, false);
        boots.soak(PitchConditions.RAIN, 90 * 60);
        double expected = 0.5 * (1 + 0.3 * (1 - StrictMath.exp(-PitchConditions.RAIN * 90 / 39.0)));
        assertEquals(expected, boots.bootMassKg, 1e-12);
        assertTrue(boots.bootMassKg > 0.58 && boots.bootMassKg < 0.6, "boot after 90 min of rain " + boots.bootMassKg);
        assertTrue(boots.kickEffectiveMassKg(1.2, p) > 1.2);

        // over a rainy match the boots soak in the world as well
        MatchWorld rain = Bodies.simWorld(Surface.WET, PitchConditions.RAIN);
        Entity e = Bodies.add(rain, 0, 0);
        rain.runTicks(30 * 60 * 45);
        KitComponent k = e.getComponent(KitComponent.class);
        assertTrue(k.bootMassKg > 0.55, "half-time boot " + k.bootMassKg);
        assertEquals(Bodies.body(e).bodyMassKg + 2 * k.bootMassKg, Bodies.body(e).massKg, 1e-12);

        // heavy boots: slower start
        MatchWorld lightW = Bodies.simWorld(Surface.DRY, 0);
        Entity light = Bodies.add(lightW, 0, 0);
        MatchWorld heavyW = Bodies.simWorld(Surface.DRY, 0);
        KitParams heavyKit = KitParams.season1888();
        heavyKit.bootMassDryKg = 1.0;
        Entity heavy = Bodies.add(heavyW, AthleteInputs.stub(Position.HALF_BACK), KitComponent.of(heavyKit, false),
                0, 0, 0);
        sprint(lightW, light, 30);
        sprint(heavyW, heavy, 30);
        assertTrue(Bodies.body(heavy).x < Bodies.body(light).x,
                "heavy " + Bodies.body(heavy).x + " vs light " + Bodies.body(light).x);
        assertTrue(KitComponent.of(heavyKit, false).kickEffectiveMassKg(1.2, p)
                > KitComponent.of(KitParams.season1888(), false).kickEffectiveMassKg(1.2, p));
    }

    @Test
    void injuredAndDownedBodiesHaveReducedOrNoControl() {
        MatchWorld w = Bodies.simWorld(Surface.DRY, 0);
        Entity ok = Bodies.add(w, 0, 0);
        Entity passenger = Bodies.add(w, 0, 10);
        Entity off = Bodies.add(w, 0, 20);
        Entity down = Bodies.add(w, 0, 30);
        BodyParams p = w.params().body;
        passenger.getComponent(InjuryComponent.class).injure(InjuryComponent.PASSENGER, 200, p);
        off.getComponent(InjuryComponent.class).injure(InjuryComponent.OFF, 300, p);
        Bodies.body(down).vx = 6;
        Bodies.body(down).downTicks = 30;
        for (Entity e : new Entity[] {ok, passenger, off, down}) {
            Bodies.cmd(e).set(20, 0);
        }
        w.runTicks(30 * 6);
        double vOk = Bodies.body(ok).speed();
        assertTrue(Bodies.body(passenger).speed() < 0.7 * vOk);
        assertEquals(0, Bodies.body(off).x, 0);
        assertEquals(0, Bodies.body(down).downTicks);
        assertTrue(Bodies.body(down).x > 0 && Bodies.body(down).x < Bodies.body(ok).x);
    }

    static double max(double[] v) {
        double m = 0;
        for (double d : v) {
            m = Math.max(m, d);
        }
        return m;
    }
}
