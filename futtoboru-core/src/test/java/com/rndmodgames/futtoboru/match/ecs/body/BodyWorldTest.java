package com.rndmodgames.futtoboru.match.ecs.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.futtoboru.system.Log;

/** E-13 in a whole world: kick-off line-ups, a scripted scrimmage pinned by a golden hash, cost per tick. */
class BodyWorldTest {

    static final PitchGeometry PITCH = new PitchGeometry(PitchParams.defaults1888());

    @Test
    void lineUpAtKickOffFollowsThe1888Law() {
        for (Shape shape : Shape.values()) {
            for (Team kicking : Team.values()) {
                MatchWorld w = Bodies.simWorld(Surface.DRY, 0);
                MatchEnds ends = new MatchEnds(Team.AWAY);
                Entity[] es = BodySpawner.spawnTeams(w, PITCH, TeamSheet.stub(shape), TeamSheet.stub(shape),
                        KitParams.season1888(), ends, kicking);
                assertEquals(22, es.length);
                int kickers = 0;
                for (int i = 0; i < 22; i++) {
                    BodyComponent b = Bodies.body(es[i]);
                    AthleteComponent a = Bodies.athlete(es[i]);
                    IdentityComponent id = es[i].getComponent(IdentityComponent.class);
                    assertEquals(i, id.index);
                    assertEquals(i < 11 ? SpawnRole.HOME_PLAYER : SpawnRole.AWAY_PLAYER, id.role);
                    assertEquals(i < 11 ? Team.HOME : Team.AWAY, b.team);
                    assertEquals(i % 11, b.slot);
                    assertEquals(shape.slot(b.slot).position(), a.position);
                    int own = ends.endDefendedBy(b.team).sign();
                    assertTrue(b.x * own > 0, shape + " " + i + ": in his own half, x " + b.x);
                    assertTrue(Math.abs(b.y) < PITCH.halfWidth() && Math.abs(b.x) < PITCH.halfLength());
                    assertEquals(own < 0 ? 0 : StrictMath.PI, b.facing, 0, "faces the other goal");
                    double fromBall = Math.sqrt(b.x * b.x + b.y * b.y);
                    if (b.team == kicking && b.slot == shape.kickerSlot()) {
                        kickers++;
                        assertTrue(fromBall < 1, "kicker at the ball");
                    } else if (b.team != kicking) {
                        assertTrue(fromBall - b.radius >= 10 * PitchParams.YARD, i + " within ten yards");
                    }
                    if (a.isKeeper()) {
                        assertTrue(PITCH.halfLength() - Math.abs(b.x) < 2, "keeper on his line");
                    }
                    for (int j = 0; j < i; j++) {
                        BodyComponent o = Bodies.body(es[j]);
                        double d = Math.sqrt((o.x - b.x) * (o.x - b.x) + (o.y - b.y) * (o.y - b.y));
                        assertTrue(d > o.radius + b.radius, "overlap " + i + "," + j);
                    }
                }
                assertEquals(1, kickers);
            }
        }
    }

    @Test
    void physiqueByPositionAndShinPadsByTheSeasonShare() {
        MatchWorld w = Bodies.simWorld(Surface.DRY, 0);
        Entity[] es = BodySpawner.spawnDefault(w, PITCH);
        BodyParams p = w.params().body;
        int pads = 0;
        for (Entity e : es) {
            BodyComponent b = Bodies.body(e);
            Position pos = Bodies.athlete(e).position;
            assertEquals(p.massByPosition[pos.ordinal()], b.bodyMassKg, 0);
            assertEquals(p.radiusByPosition[pos.ordinal()], b.radius, 0);
            KitComponent k = e.getComponent(KitComponent.class);
            assertEquals(Studs.BARS, k.studs);
            assertEquals(b.bodyMassKg + k.carriedMassKg(), b.massKg, 1e-12);
            pads += k.shinPads ? 1 : 0;
        }
        assertTrue(pads > 5 && pads < 20, "about 60 % wear pads: " + pads);
        // same seed, same choice
        MatchWorld w2 = Bodies.simWorld(Surface.DRY, 0);
        Entity[] es2 = BodySpawner.spawnDefault(w2, PITCH);
        for (int i = 0; i < 22; i++) {
            assertEquals(es[i].getComponent(KitComponent.class).shinPads,
                    es2[i].getComponent(KitComponent.class).shinPads);
        }
    }

    // ---- scripted scrimmage --------------------------------------------------------------------------------------

    /** 22 bodies at kick-off, ball on the spot; every player chases a moving point, some charge. */
    static MatchWorld scrimmage() {
        MatchWorld w = Bodies.world(new PhysicsParams(), Surface.WET, PitchConditions.RAIN, 1888);
        MatchSystems.installSimulation(w);
        BodySpawner.spawnDefault(w, PITCH);
        w.spawn(SpawnRole.BALL).add(BallComponent.atRest(0, 0, w.params().ballDryMassKg));
        return w;
    }

    static final class Script {
        final Entity[] players;

        Script(MatchWorld w) {
            players = w.engine().getEntitiesFor(LocomotionSystem.family()).toArray(Entity.class);
        }

        /** Triangle wave in [-1, 1], period 4 (no trig: StrictMath.sin allocates, which the cost test would count). */
        static double wave(double t) {
            double u = t - 4 * Math.floor(t / 4);
            return u < 2 ? u - 1 : 3 - u;
        }

        void apply(long tick) {
            double phase = tick * 0.004;
            double tx = 20 * wave(phase);
            double ty = 12 * wave(phase * 1.7);
            for (int i = 0; i < players.length; i++) {
                BodyComponent b = players[i].getComponent(BodyComponent.class);
                MoveCommand c = players[i].getComponent(MoveCommand.class);
                double speed = 3 + (i % 5) * 1.5;
                c.toward(b.x, b.y, tx + (i % 3) - 1, ty + (i % 4) - 1.5, speed);
                c.charge = (i + tick / 90) % 4 == 0;
            }
        }
    }

    static long scrimmageHash(int ticks, long[] contactsOut) {
        MatchWorld w = scrimmage();
        Script s = new Script(w);
        for (int t = 0; t < ticks; t++) {
            s.apply(t);
            w.tick();
        }
        if (contactsOut != null) {
            contactsOut[0] = w.engine().getSystem(ContactSystem.class).totalContacts();
        }
        return w.stateHash();
    }

    /**
     * Two minutes of the scripted scrimmage in rain. Same rule as BallPhysicsTest.GOLDEN: identical on every machine;
     * update only in a PR that changes body physics on purpose (and bump EcsMatchEngine.ENGINE_VERSION).
     */
    static final long GOLDEN_SCRIMMAGE = 0xda6ad6de89c37ab9L;

    @Test
    void scrimmageIsDeterministicAndPinnedByAGoldenHash() {
        long[] contacts = new long[1];
        long a = scrimmageHash(30 * 120, contacts);
        assertEquals(a, scrimmageHash(30 * 120, null));
        assertTrue(contacts[0] > 50, "the scrimmage has contacts: " + contacts[0]);
        assertEquals(GOLDEN_SCRIMMAGE, a, () -> "scrimmage " + Long.toHexString(a));
    }

    // ---- cost ----------------------------------------------------------------------------------------------------

    @Test
    void costAndAllocationPerTickFor22Bodies() {
        MatchWorld w = scrimmage();
        Script s = new Script(w);
        int ticks = 30 * 60 * 5;
        long tick = 0;
        for (int round = 0; round < 3; round++) { // JIT warm-up
            for (int t = 0; t < ticks; t++) {
                s.apply(tick++);
                w.tick();
            }
        }
        com.sun.management.ThreadMXBean mx = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long tid = Thread.currentThread().getId();
        long bytes0 = mx.getThreadAllocatedBytes(tid);
        long t0 = System.nanoTime();
        long sink = 0;
        for (int t = 0; t < ticks; t++) {
            s.apply(tick++);
            w.tick();
            sink += w.stateHash();
        }
        long ns = System.nanoTime() - t0;
        long bytes = mx.getThreadAllocatedBytes(tid) - bytes0;
        long t1 = System.nanoTime();
        for (int t = 0; t < ticks; t++) {
            s.apply(tick++);
            w.tick();
        }
        double simPerTick = (System.nanoTime() - t1) / (double) ticks;
        double nsPerTick = ns / (double) ticks;
        Log.info(Log.ENGINE_MATCH, "BodyWorldTest", String.format(java.util.Locale.ROOT,
                "ECS 22 bodies + ball: %.3f us per tick with state hash, %.3f us without (%.1f ms per 90 min at %d"
                        + " Hz), %d bytes allocated over %d ticks, %d contacts (sink %d)",
                nsPerTick / 1000, simPerTick / 1000, simPerTick * 162_000 / 1e6, w.params().tickHz, bytes, ticks,
                w.engine().getSystem(ContactSystem.class).totalContacts(), sink & 1));
        assertTrue(nsPerTick < 200_000, "ns per tick " + nsPerTick);
        assertTrue(bytes < 64 * 1024, "allocated " + bytes + " bytes over " + ticks + " ticks");
    }
}
