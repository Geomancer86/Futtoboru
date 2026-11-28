package com.rndmodgames.futtoboru.match.ecs.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.Surface;

/** E-13 contacts: symmetry, momentum, no tunnelling, charges, falls, keeper charge, from behind, injury rates. */
class ContactSystemTest {

    static ContactSystem contacts(MatchWorld w) {
        return w.engine().getSystem(ContactSystem.class);
    }

    /** Ticks until the first impact (max 60); the record of that tick, or null. */
    static ContactRecord runToImpact(MatchWorld w) {
        ContactSystem cs = contacts(w);
        for (int t = 0; t < 60; t++) {
            w.tick();
            if (cs.contactCount() > 0) {
                return cs.contact(0);
            }
        }
        return null;
    }

    /** Params without injuries (a random injury would break the symmetry checks). */
    static PhysicsParams noInjuries() {
        PhysicsParams p = new PhysicsParams();
        p.body.injuryThresholdNs = 1e12;
        return p;
    }

    @Test
    void headOnContactIsMirrorSymmetricAndIndependentOfCreationOrder() {
        MatchWorld w = Bodies.contactWorld(noInjuries(), 1);
        Entity a = Bodies.add(w, -1.3, 0.2);
        Entity b = Bodies.add(w, 1.3, -0.2);
        Bodies.body(a).vx = 5;
        Bodies.body(a).vy = -0.3;
        Bodies.body(b).vx = -5;
        Bodies.body(b).vy = 0.3;
        ContactRecord c = runToImpact(w);
        assertTrue(c != null);
        w.runTicks(5);
        BodyComponent ba = Bodies.body(a);
        BodyComponent bb = Bodies.body(b);
        assertEquals(-ba.x, bb.x, 1e-12);
        assertEquals(-ba.y, bb.y, 1e-12);
        assertEquals(-ba.vx, bb.vx, 1e-12);
        assertEquals(-ba.vy, bb.vy, 1e-12);
        assertTrue((bb.vx - ba.vx) * (bb.x - ba.x) + (bb.vy - ba.vy) * (bb.y - ba.y) > 0, "separating after impact");

        // swapped creation order: same physics, mirrored roles
        MatchWorld w2 = Bodies.contactWorld(noInjuries(), 1);
        Entity b2 = Bodies.add(w2, 1.3, -0.2);
        Entity a2 = Bodies.add(w2, -1.3, 0.2);
        Bodies.body(a2).vx = 5;
        Bodies.body(a2).vy = -0.3;
        Bodies.body(b2).vx = -5;
        Bodies.body(b2).vy = 0.3;
        runToImpact(w2);
        w2.runTicks(5);
        assertEquals(ba.x, Bodies.body(a2).x, 1e-12);
        assertEquals(ba.vx, Bodies.body(a2).vx, 1e-12);
        assertEquals(bb.y, Bodies.body(b2).y, 1e-12);
    }

    @Test
    void momentumIsConservedWithUnequalMasses() {
        MatchWorld w = Bodies.contactWorld(new PhysicsParams(), 1);
        Entity heavy = Bodies.add(w, AthleteInputs.stub(Position.FULL_BACK), Bodies.kit(Studs.BARS, false), 90, -1.5,
                0.1, 0);
        Entity light = Bodies.add(w, AthleteInputs.stub(Position.LEFT_WING), Bodies.kit(Studs.BARS, false), 60, 1.0,
                0, 0);
        BodyComponent h = Bodies.body(heavy);
        BodyComponent l = Bodies.body(light);
        h.vx = 6;
        l.vx = -2;
        double px = h.massKg * h.vx + l.massKg * l.vx;
        double py = h.massKg * h.vy + l.massKg * l.vy;
        assertTrue(runToImpact(w) != null);
        assertEquals(px, h.massKg * h.vx + l.massKg * l.vx, 1e-9);
        assertEquals(py, h.massKg * h.vy + l.massKg * l.vy, 1e-9);
        assertTrue(l.vx > 0, "the heavy, faster man carries the light one back: " + l.vx);
        double dx = l.x - h.x;
        double dy = l.y - h.y;
        assertTrue(Math.sqrt(dx * dx + dy * dy) >= h.radius + l.radius - 1e-9, "no overlap after the tick");
    }

    @Test
    void sprintersCannotPassThroughEachOtherInOneTick() {
        MatchWorld w = Bodies.contactWorld(noInjuries(), 1);
        Entity a = Bodies.add(w, -0.6, 0);
        Entity b = Bodies.add(w, 0.6, 0);
        // closing at 30 m/s = 1 m per tick each: without a swept test they swap sides with no overlap at either end
        Bodies.body(a).vx = 15;
        Bodies.body(b).vx = -15;
        w.tick();
        assertEquals(1, contacts(w).contactCount());
        assertTrue(Bodies.body(a).x < Bodies.body(b).x, "A stays west of B");
        assertTrue(Bodies.body(a).vx < 0 && Bodies.body(b).vx > 0);
    }

    @Test
    void bodiesNeverEndATickOverlappingInAFullScrimmage() {
        MatchWorld w = Bodies.simWorld(Surface.WET, 0.2);
        Entity[] es = new Entity[22];
        for (int i = 0; i < es.length; i++) {
            double ang = 2 * StrictMath.PI * i / es.length;
            es[i] = Bodies.add(w, 15 * StrictMath.cos(ang), 15 * StrictMath.sin(ang));
        }
        for (int t = 0; t < 30 * 20; t++) {
            for (Entity e : es) {
                BodyComponent b = Bodies.body(e);
                Bodies.cmd(e).toward(b.x, b.y, 0, 0, 9);
            }
            w.tick();
            // the contact system resolves pairs in order, so a later pair may push a body back into an earlier one by
            // a little; it never stays deep inside another body
            for (int i = 0; i < es.length; i++) {
                for (int j = i + 1; j < es.length; j++) {
                    BodyComponent a = Bodies.body(es[i]);
                    BodyComponent b = Bodies.body(es[j]);
                    double d = Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
                    assertTrue(d > 0.5 * (a.radius + b.radius), "tick " + t + " pair " + i + "," + j + " d " + d);
                }
            }
        }
        assertTrue(contacts(w).totalContacts() > 100);
    }

    /** A charger of {@code mass} at {@code speed} into a standing average half back; the victim's record side. */
    static ContactRecord charge(PhysicsParams params, double mass, double speed, boolean charge, Position victimPos,
            double victimFacing, long seed, boolean victimPads) {
        MatchWorld w = Bodies.contactWorld(params, seed);
        Entity a = Bodies.add(w, AthleteInputs.stub(Position.CENTRE_FORWARD), Bodies.kit(Studs.BARS, false), mass,
                -1.0, 0, 0);
        Bodies.add(w, AthleteInputs.stub(victimPos).withShinPads(victimPads), Bodies.kit(Studs.BARS, victimPads),
                71, 0, 0, victimFacing);
        Bodies.body(a).vx = speed;
        Bodies.cmd(a).charge = charge;
        return runToImpact(w);
    }

    @Test
    void chargeOutcomeIsMonotonicInMassAndSpeed() {
        PhysicsParams params = new PhysicsParams();
        double[] masses = {55, 60, 65, 70, 75, 80, 85, 90, 95};
        double[] speeds = {1.5, 2, 3, 4, 5, 6, 7, 8, 9};
        double[][] impulse = new double[masses.length][speeds.length];
        int[][] fall = new int[masses.length][speeds.length];
        for (int m = 0; m < masses.length; m++) {
            for (int s = 0; s < speeds.length; s++) {
                ContactRecord c = charge(params, masses[m], speeds[s], true, Position.HALF_BACK, StrictMath.PI, 3,
                        false);
                impulse[m][s] = c.impulseNs;
                fall[m][s] = c.bFall;
                assertTrue(c.aCharged && !c.bCharged);
            }
        }
        for (int m = 0; m < masses.length; m++) {
            for (int s = 0; s < speeds.length; s++) {
                if (m > 0) {
                    assertTrue(impulse[m][s] > impulse[m - 1][s] && fall[m][s] >= fall[m - 1][s], "mass " + m);
                }
                if (s > 0) {
                    assertTrue(impulse[m][s] > impulse[m][s - 1] && fall[m][s] >= fall[m][s - 1], "speed " + s);
                }
            }
        }
        assertEquals(ContactRecord.STUMBLE, fall[0][0], "a light man at a walk only rocks him");
        assertEquals(ContactRecord.DOWN, fall[masses.length - 1][speeds.length - 1], "a heavy sprinter floors him");

        ContactRecord shoulder = charge(params, 75, 5, true, Position.HALF_BACK, StrictMath.PI, 3, false);
        ContactRecord bump = charge(params, 75, 5, false, Position.HALF_BACK, StrictMath.PI, 3, false);
        assertTrue(shoulder.impulseNs > bump.impulseNs + 30, "the shoulder drive adds impulse");
        ContactRecord walk = charge(params, 70, 1.0, false, Position.HALF_BACK, StrictMath.PI, 3, false);
        assertEquals(ContactRecord.NONE, walk.bFall);
    }

    @Test
    void chargingTheKeeperIsFairIn1888ButChargingFromBehindIsNot() {
        PhysicsParams params = new PhysicsParams();
        ContactRecord keeper = charge(params, 75, 6, true, Position.GOALKEEPER, StrictMath.PI, 3, false);
        assertTrue(keeper.bKeeper && keeper.aCharged && !keeper.keeperProtected);
        assertFalse(keeper.foulSuspected(), "1888-89: the keeper may be charged");

        PhysicsParams protectedKeeper = new PhysicsParams();
        protectedKeeper.rules = com.rndmodgames.futtoboru.match.ecs.laws.RulesProfile.forSeason(1894); // 1892/1894 onward
        assertTrue(charge(protectedKeeper, 75, 6, true, Position.GOALKEEPER, StrictMath.PI, 3, false).foulSuspected());

        ContactRecord front = charge(params, 75, 6, true, Position.HALF_BACK, StrictMath.PI, 3, false);
        assertFalse(front.bHitFromBehind || front.foulSuspected());
        ContactRecord behind = charge(params, 75, 6, true, Position.HALF_BACK, 0, 3, false);
        assertTrue(behind.bHitFromBehind && behind.foulSuspected(), "1869: no charging from behind");
        ContactRecord side = charge(params, 75, 6, true, Position.HALF_BACK, StrictMath.PI / 2, 3, false);
        assertFalse(side.bHitFromBehind);
    }

    @Test
    void injuryModelBounds() {
        BodyParams p = new BodyParams();
        assertEquals(0, InjuryModel.risk(p.injuryThresholdNs, 1, false, p), 0);
        assertEquals(p.injuryRiskAtDouble, InjuryModel.risk(2 * p.injuryThresholdNs, 1, false, p), 1e-15);
        assertEquals(p.injuryRiskAtDouble * p.shinPadInjuryScale, InjuryModel.risk(2 * p.injuryThresholdNs, 1, true, p),
                1e-15);
        assertEquals(p.injuryRiskMax, InjuryModel.risk(1e6, 3, false, p), 0);
        assertEquals(InjuryComponent.KNOCK, InjuryModel.severity(0, 1e6, p));
        assertEquals(InjuryComponent.OFF, InjuryModel.severity(0.999, p.injuryThresholdNs, p));
        // harder hits are more severe
        assertTrue(InjuryModel.severity(0.6, 4 * p.injuryThresholdNs, p) > InjuryModel.severity(0.6,
                p.injuryThresholdNs, p));
        // fragility from age and condition
        AthleteComponent young = AthleteComponent.from(AthleteInputs.stub(Position.HALF_BACK), p);
        AthleteComponent old = AthleteComponent.from(new AthleteInputs(Position.HALF_BACK, 13, 13, 13, 13, 13, 13, 35,
                0.5, null), p);
        assertEquals(1, young.fragility, 0);
        assertTrue(old.fragility > 1.5);
    }

    @Test
    void injuryRatesStayWithinTheConfiguredBoundsOverSeededTrials() {
        PhysicsParams params = new PhysicsParams();
        params.body.injuryRiskAtDouble = 0.10;
        int n = 2000;
        int[] injured = new int[2];
        int[] knocks = new int[2];
        double[] expected = new double[2];
        double impulse = 0;
        for (int pads = 0; pads < 2; pads++) {
            for (int i = 0; i < n; i++) {
                ContactRecord c = charge(params, 85, 8, true, Position.HALF_BACK, StrictMath.PI, 10_000L * pads + i,
                        pads == 1);
                impulse = c.impulseNs;
                if (c.bInjury != 0) {
                    injured[pads]++;
                    if (c.bInjury == InjuryComponent.KNOCK) {
                        knocks[pads]++;
                    }
                }
            }
            expected[pads] = InjuryModel.risk(impulse, 1, pads == 1, params.body);
        }
        assertTrue(expected[0] > 0.1 && expected[0] < 0.3, "risk " + expected[0] + " at " + impulse + " N s");
        for (int pads = 0; pads < 2; pads++) {
            double mean = n * expected[pads];
            double sigma = Math.sqrt(n * expected[pads] * (1 - expected[pads]));
            assertTrue(Math.abs(injured[pads] - mean) < 4 * sigma,
                    "pads " + pads + ": " + injured[pads] + " injured, expected " + mean + " +- " + 4 * sigma);
        }
        assertTrue(injured[1] < injured[0], "shin pads lower the risk: " + injured[1] + " vs " + injured[0]);
        // severity split: knock share as configured for this impulse
        double knockShare = params.body.knockShare
                - params.body.severityShiftPerExcess * (impulse / params.body.injuryThresholdNs - 1);
        int all = injured[0] + injured[1];
        int k = knocks[0] + knocks[1];
        double sigma = Math.sqrt(all * knockShare * (1 - knockShare));
        assertTrue(Math.abs(k - all * knockShare) < 4 * sigma, k + " knocks of " + all + ", share " + knockShare);
    }
}
