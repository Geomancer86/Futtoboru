package com.rndmodgames.futtoboru.match.ecs.laws;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.SkillComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteInputs;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyParams;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitParams;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** Test helpers for E-16: worlds under the laws, players, ball, officials. Home defends west, kicks off. */
final class Laws {

    static final PitchGeometry PITCH = new PitchGeometry(PitchParams.defaults1888());

    private Laws() {
    }

    /** Params of {@code season} with no kick noise, sure appeals and no restart delay. */
    static PhysicsParams params(int season) {
        PhysicsParams p = new PhysicsParams();
        p.rules = RulesProfile.forSeason(season);
        p.action.noiseScale = 0;
        p.laws.restartDelaySeconds = 0;
        p.laws.kickOffDelaySeconds = 0;
        p.laws.appealOffside = 1;
        p.laws.appealFoul = 1;
        p.laws.appealFairChargeDown = 1;
        return p;
    }

    static MatchWorld world(PhysicsParams p, long seed) {
        MatchWorld w = new MatchWorld(p, new UniformPitch(Surface.DRY, 0), RandomSource.seeded(seed).fork("match:laws"));
        MatchSystems.installSimulation(w);
        return w;
    }

    static Entity player(MatchWorld w, Team team, Position pos, double x, double y, double facing) {
        BodyParams p = w.params().body;
        AthleteInputs in = AthleteInputs.stub(pos);
        int o = pos.ordinal();
        Entity e = w.spawn(team == Team.HOME ? SpawnRole.HOME_PLAYER : SpawnRole.AWAY_PLAYER);
        BodyComponent b = new BodyComponent(team, pos == Position.GOALKEEPER ? 0 : 1, p.radiusByPosition[o],
                p.massByPosition[o], x, y, facing);
        KitComponent kit = KitComponent.of(KitParams.season1888(), false);
        b.massKg = b.bodyMassKg + kit.carriedMassKg();
        e.add(b);
        e.add(AthleteComponent.from(in, p));
        e.add(kit);
        e.add(new InjuryComponent());
        e.add(new MoveCommand());
        e.add(SkillComponent.from(in, p));
        e.add(new ActionCommand());
        return e;
    }

    /** Ball at (x, y) with the laws attached (halves of {@code halfMinutes}). */
    static Entity ball(MatchWorld w, double x, double y, double halfMinutes) {
        Entity e = w.spawn(SpawnRole.BALL);
        e.add(BallComponent.atRest(x, y, w.params().ballDryMassKg));
        PlayStateComponent.attach(e, new MatchEnds(Team.HOME), Team.HOME, halfMinutes, w.params().tickHz);
        return e;
    }

    static LawsSystem laws(MatchWorld w) {
        return w.engine().getSystem(LawsSystem.class);
    }

    static PlayStateComponent state(Entity ball) {
        return ball.getComponent(PlayStateComponent.class);
    }

    static BallComponent ballOf(Entity ball) {
        return ball.getComponent(BallComponent.class);
    }

    static ActionCommand cmd(Entity e) {
        return e.getComponent(ActionCommand.class);
    }

    static BodyComponent body(Entity e) {
        return e.getComponent(BodyComponent.class);
    }

    static int index(Entity e) {
        return e.getComponent(com.rndmodgames.futtoboru.match.ecs.IdentityComponent.class).index;
    }

    /** Ball still at (x, y). */
    static void place(Entity ball, double x, double y) {
        BallComponent b = ballOf(ball);
        b.x = x;
        b.y = y;
        b.z = 0;
        b.vx = b.vy = b.vz = 0;
        b.state = BallComponent.REST;
    }

    /** The first record of {@code type} this tick, or null. */
    static LawRecord find(MatchWorld w, int type) {
        LawsSystem s = laws(w);
        for (int k = 0; k < s.recordCount(); k++) {
            if (s.record(k).type == type) {
                return s.record(k);
            }
        }
        return null;
    }
}
