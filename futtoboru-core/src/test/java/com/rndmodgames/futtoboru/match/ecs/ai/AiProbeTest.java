package com.rndmodgames.futtoboru.match.ecs.ai;

import java.util.Locale;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.action.ActionRecord;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.KitParams;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.body.TeamSheet;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** Temporary probe: per-team action counts. */
@Tag("slow")
class AiProbeTest {

    @Test
    void probe() {
        int n = Integer.getInteger("ai.probe.matches", 4);
        TeamStyle hs = TeamStyle.valueOf(System.getProperty("ai.probe.home", "COMBINATION"));
        TeamStyle as = TeamStyle.valueOf(System.getProperty("ai.probe.away", "DRIBBLING"));
        int goals = 0;
        for (int m = 0; m < n; m++) {
            goals += run(m, hs, as);
        }
        System.out.println("goals/game " + goals / (double) n);
    }

    static int run(int m, TeamStyle hs, TeamStyle as) {
        PhysicsParams params = new PhysicsParams();
        for (String key : System.getProperties().stringPropertyNames()) {
            if (key.startsWith("ai.p.")) {
                try {
                    java.lang.reflect.Field f = AiParams.class.getField(key.substring(5));
                    String v = System.getProperty(key);
                    if (f.getType() == int.class) {
                        f.setInt(params.ai, Integer.parseInt(v));
                    } else {
                        f.setDouble(params.ai, Double.parseDouble(v));
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(key, e);
                }
            }
        }
        PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
        MatchWorld w = new MatchWorld(params, new UniformPitch(Surface.DRY, 0),
                RandomSource.seeded(77L).fork("match:p" + m));
        MatchSystems.installSimulation(w, pitch);
        MatchEnds ends = new MatchEnds(Team.HOME);
        BodySpawner.spawnTeams(w, pitch, TeamSheet.stub(Shape.TWO_THREE_FIVE), TeamSheet.stub(Shape.TWO_THREE_FIVE),
                KitParams.season1888(), ends, Team.HOME);
        Entity be = w.spawn(SpawnRole.BALL);
        BallComponent ball = BallComponent.atRest(0, 0, params.ballDryMassKg);
        be.add(ball);
        AiSetup.install(w, new AiSetup.Side(Shape.TWO_THREE_FIVE, hs), new AiSetup.Side(Shape.TWO_THREE_FIVE, as),
                ends, Team.HOME);
        ActionSystem actions = w.engine().getSystem(ActionSystem.class);
        PlayStateComponent play = be.getComponent(PlayStateComponent.class);
        PossessionComponent poss = be.getComponent(PossessionComponent.class);
        long[][] count = new long[2][ActionIntent.values().length];
        long[] passOk = new long[2];
        long[] passAll = new long[2];
        long[] possTicks = new long[2];
        long[] shotsOnTarget = new long[2];
        long deadTicks = 0;
        long[] deadBy = new long[5];
        int pendingPassTeam = -1;
        long half = 45L * 60 * 30;
        for (long t = 0; t < 2 * half; t++) {
            if (t == half) {
                AiSetup.halfTime(w, pitch);
            }
            w.tick();
            if (Boolean.getBoolean("ai.probe.trace") && t < 400 && t % 10 == 0) {
                Entity k = w.engine().getEntitiesFor(com.badlogic.ashley.core.Family.all(
                        com.rndmodgames.futtoboru.match.ecs.body.BodyComponent.class).get()).get(8);
                com.rndmodgames.futtoboru.match.ecs.body.BodyComponent kb = k.getComponent(
                        com.rndmodgames.futtoboru.match.ecs.body.BodyComponent.class);
                com.rndmodgames.futtoboru.match.ecs.action.ActionCommand ka = k.getComponent(
                        com.rndmodgames.futtoboru.match.ecs.action.ActionCommand.class);
                TeamTacticsComponent tc0 = w.engine().getEntitiesFor(com.badlogic.ashley.core.Family.all(
                        TeamTacticsComponent.class).get()).get(0).getComponent(TeamTacticsComponent.class);
                System.out.println(String.format(Locale.ROOT,
                        "t=%d live=%b ready=%d dead=%d taker=%d/%d kicker (%.2f,%.2f) v(%.2f,%.2f) act=%s/%d res=%d"
                                + " ball (%.2f,%.2f) last=%d",
                        t, play.live, play.readyTick, play.deadTick, tc0.restartTaker, tc0.takerForDeadTick, kb.x,
                        kb.y, kb.vx, kb.vy, ka.intent, ka.armTicksLeft, ka.result, ball.x, ball.y,
                        poss.lastTouchTick));
            }
            if (!play.live) {
                deadTicks++;
                deadBy[play.restartType.ordinal()]++;
            } else if (poss.lastTouchTeam >= 0) {
                possTicks[poss.lastTouchTeam]++;
            }
            for (int k = 0; k < actions.recordCount(); k++) {
                ActionRecord r = actions.record(k);
                if (r.team < 0) {
                    continue;
                }
                if (pendingPassTeam >= 0) {
                    passAll[pendingPassTeam]++;
                    if (r.team == pendingPassTeam) {
                        passOk[pendingPassTeam]++;
                    }
                    pendingPassTeam = -1;
                }
                count[r.team][r.intent.ordinal()]++;
                if (r.intent == ActionIntent.PASS) {
                    pendingPassTeam = r.team;
                }
                if (r.intent == ActionIntent.SHOT) {
                    double vx = r.afterVx;
                    double gx = vx > 0 ? pitch.halfLength() : -pitch.halfLength();
                    double tt = (gx - r.afterX) / vx;
                    double y = r.afterY + r.afterVy * tt;
                    if (tt > 0 && Math.abs(y) < pitch.postCentreY()) {
                        shotsOnTarget[r.team]++;
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "probe %d %s v %s: %d-%d, dead %.0f%%, possession %.0f/%.0f%n", m, hs, as,
                play.homeGoals, play.awayGoals, 100.0 * deadTicks / (2 * half),
                100.0 * possTicks[0] / (possTicks[0] + possTicks[1]),
                100.0 * possTicks[1] / (possTicks[0] + possTicks[1])));
        for (int t = 0; t < 2; t++) {
            sb.append(t == 0 ? "  home " : "  away ");
            for (ActionIntent a : ActionIntent.values()) {
                if (count[t][a.ordinal()] > 0) {
                    sb.append(a).append('=').append(count[t][a.ordinal()]).append(' ');
                }
            }
            sb.append(String.format(Locale.ROOT, "passOK %d/%d onTarget %d%n", passOk[t], passAll[t],
                    shotsOnTarget[t]));
        }
        sb.append("  throwIns ").append(play.throwIns).append(" goalKicks ").append(play.goalKicks)
                .append(" corners ").append(play.corners).append(" deadBy ")
                .append(java.util.Arrays.toString(deadBy));
        System.out.println(sb);
        return play.homeGoals + play.awayGoals;
    }
}
