package com.rndmodgames.futtoboru.match.ecs;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.ai.AiSetup;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.KitParams;
import com.rndmodgames.futtoboru.match.ecs.laws.LawsSystem;
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
 * Plays a whole match headless: builds the world, lines up both sides at their kick-off spots (E-13 bodies, 1888-89
 * kit, default 110 x 70 yd ground), places the ball on the centre spot, spawns the referee and the two club umpires,
 * puts the 1888-89 Laws on the match (E-16: kick-off, goals, restarts, offside, fouls, half-time with change of ends,
 * full time) and wires the player AI (E-15, {@link AiSetup}); home defends the west goal and kicks off. The result
 * carries the score; events and stats are E-17.
 */
public final class EcsMatchEngine {

    /** Saved with every match (§5.3); bump when a change alters any simulated bit. */
    public static final String ENGINE_VERSION = "ecs-0.5";

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
        PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
        MatchWorld world = new MatchWorld(params, input.pitch(), rng);
        MatchSystems.installSimulation(world, pitch);
        MatchEnds ends = new MatchEnds(Team.HOME);
        BodySpawner.spawnTeams(world, pitch, input.home().sheet(), input.away().sheet(), KitParams.season1888(), ends,
                Team.HOME);
        Entity ballEntity = world.spawn(SpawnRole.BALL);
        BallComponent ball = BallComponent.atRest(0, 0, params.ballDryMassKg * (1.0 + input.ballCarryShare()));
        ballEntity.add(ball);
        Officials.spawn(world, pitch, ends);
        PlayStateComponent play = PlayStateComponent.attach(ballEntity, ends, Team.HOME, input.halfMinutes(),
                params.tickHz);
        AiSetup.install(world, side(input.home()), side(input.away()), ends);

        long halfTicks = (long) input.halfMinutes() * 60L * params.tickHz;
        world.runTicks(halfTicks);
        double halfTimeMass = ball.massKg;
        world.runTicks(halfTicks);
        long actions = world.engine().getSystem(ActionSystem.class).totalActions();
        return new EcsMatchResult(input.matchKey(), ENGINE_VERSION, rng.getSeed(), world.tickCount(),
                world.stateHash(), halfTimeMass, ball.massKg, play.homeGoals, play.awayGoals, actions,
                play.offsidesGiven, play.foulsGiven);
    }

    private static AiSetup.Side side(EcsMatchInput.Side s) {
        return new AiSetup.Side(s.sheet().shape(), s.style());
    }
}
