package com.rndmodgames.futtoboru.match.ecs.action;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteInputs;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyParams;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitParams;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** Test helpers for E-14: worlds with the full simulation, players with skills, a ball. */
final class Actions {

    private Actions() {
    }

    static PhysicsParams params(double noiseScale) {
        PhysicsParams p = new PhysicsParams();
        p.action.noiseScale = noiseScale;
        return p;
    }

    static MatchWorld world(PhysicsParams params, Surface surface, double wetness, long seed) {
        MatchWorld w = new MatchWorld(params, new UniformPitch(surface, wetness),
                RandomSource.seeded(seed).fork("match:action"));
        MatchSystems.installSimulation(w);
        return w;
    }

    /** A player at (x, y) facing {@code facing}; home players must be added before away players. */
    static Entity player(MatchWorld w, Team team, AthleteInputs in, double x, double y, double facing) {
        BodyParams p = w.params().body;
        int pos = in.position().ordinal();
        Entity e = w.spawn(team == Team.HOME ? SpawnRole.HOME_PLAYER : SpawnRole.AWAY_PLAYER);
        BodyComponent b = new BodyComponent(team, 0, p.radiusByPosition[pos], p.massByPosition[pos], x, y, facing);
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

    static Entity player(MatchWorld w, Team team, Position pos, double x, double y, double facing) {
        return player(w, team, AthleteInputs.stub(pos), x, y, facing);
    }

    /** All ball skills at {@code rating}. */
    static AthleteInputs skilled(Position pos, double rating) {
        return AthleteInputs.stub(pos).withSkills(rating, rating, rating, rating, rating);
    }

    static BallComponent ball(MatchWorld w, double x, double y) {
        BallComponent b = BallComponent.atRest(x, y, w.params().ballDryMassKg);
        w.spawn(SpawnRole.BALL).add(b);
        return b;
    }

    static ActionSystem system(MatchWorld w) {
        return w.engine().getSystem(ActionSystem.class);
    }

    static ActionCommand cmd(Entity e) {
        return e.getComponent(ActionCommand.class);
    }

    static BodyComponent body(Entity e) {
        return e.getComponent(BodyComponent.class);
    }

    static PossessionComponent possession(MatchWorld w) {
        return w.engine().getEntitiesFor(com.badlogic.ashley.core.Family.all(BallComponent.class).get()).get(0)
                .getComponent(PossessionComponent.class);
    }

    /** Puts the ball at (x, y, z) with velocity (vx, vy, vz), in the air if above the ground or rising. */
    static void place(BallComponent b, double x, double y, double z, double vx, double vy, double vz) {
        b.x = x;
        b.y = y;
        b.z = z;
        b.vx = vx;
        b.vy = vy;
        b.vz = vz;
        b.spin = 0;
        b.state = z > 0 || vz > 0 ? BallComponent.AIR
                : (vx != 0 || vy != 0 ? BallComponent.ROLLING : BallComponent.REST);
    }

    /** Soaks the ball fully (saturation 1) without changing its kick-off mass. */
    static void saturate(MatchWorld w, BallComponent b) {
        b.saturation = 1;
        b.massKg = b.startMassKg * (1 + w.params().ballWaterUptakeMax);
    }
}
