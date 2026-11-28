package com.rndmodgames.futtoboru.match.ecs;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.laws.Officials;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Entry point of the ECS match engine, shaped like V2's {@code MatchEngine.play(input, rng)} (§5.2). Not wired into
 * {@code MatchSimulator} (that is E-17, coordinated with BOOK3PRO).
 *
 * Stub for E-10..E-16: builds the world, lines up 22 average players in 2-3-5 at their kick-off spots (E-13 bodies,
 * 1888-89 kit, default 110 x 70 yd ground), places the ball on the centre spot, spawns the referee and the two club
 * umpires and puts the 1888-89 Laws on the match (E-16: kick-off, half-time with change of ends, full time), then
 * plays two halves headless. Nobody
 * moves or kicks yet (AI is E-15), so the result has no events; ball and boots still soak up the day's rain, and the
 * final state hash pins the whole loop.
 */
public final class EcsMatchEngine {

    /** Saved with every match (§5.3); bump when a change alters any simulated bit. */
    public static final String ENGINE_VERSION = "ecs-0.4";

    private final PhysicsParams params;

    public EcsMatchEngine() {
        this(new PhysicsParams());
    }

    public EcsMatchEngine(PhysicsParams params) {
        this.params = params.validate();
    }

    /**
     * @param rng the match fork, {@code game.random().fork("match:" + input.matchKey())}
     */
    public EcsMatchResult play(EcsMatchInput input, RandomSource rng) {
        MatchWorld world = new MatchWorld(params, input.pitch(), rng);
        MatchSystems.installSimulation(world);
        PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
        BodySpawner.spawnDefault(world, pitch);
        Entity ballEntity = world.spawn(SpawnRole.BALL);
        BallComponent ball = BallComponent.atRest(0, 0, params.ballDryMassKg * (1.0 + input.ballCarryShare()));
        ballEntity.add(ball);
        MatchEnds ends = new MatchEnds(Team.HOME);
        Officials.spawn(world, pitch, ends);
        PlayStateComponent.attach(ballEntity, ends, Team.HOME, input.halfMinutes(), params.tickHz);

        long halfTicks = (long) input.halfMinutes() * 60L * params.tickHz;
        world.runTicks(halfTicks);
        double halfTimeMass = ball.massKg;
        world.runTicks(halfTicks);
        return new EcsMatchResult(input.matchKey(), ENGINE_VERSION, rng.getSeed(), world.tickCount(),
                world.stateHash(), halfTimeMass, ball.massKg);
    }
}
