package com.rndmodgames.futtoboru.match.ecs;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ai.AiSetup;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.KitParams;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventDetector;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLog;
import com.rndmodgames.futtoboru.match.ecs.laws.LawsSystem;
import com.rndmodgames.futtoboru.match.ecs.laws.Officials;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Entry point of the ECS match engine, shaped like V2's {@code MatchEngine.play(input, rng)} (§5.2). The game reaches
 * it through {@code engine.simulation.EcsMatchEngineAdapter} behind {@code -Dfuttoboru.match.engine=ecs} (E-17a).
 *
 * Plays a whole match headless: builds the world, lines up both sides at their kick-off spots (E-13 bodies, 1888-89
 * kit, default 110 x 70 yd ground), places the ball on the centre spot, spawns the referee and the two club umpires,
 * puts the 1888-89 Laws on the match (E-16: kick-off, goals, restarts, offside, fouls, half-time with change of ends,
 * full time) and wires the player AI (E-15, {@link AiSetup}); home defends the west goal and kicks off. The result
 * carries the score, who scored each goal and every event ({@link MatchEventDetector}, E-17), read from the
 * state after each tick so the events never change a simulated bit.
 */
public final class EcsMatchEngine {

    /** Saved with every match (§5.3); bump when a change alters any simulated bit. */
    public static final String ENGINE_VERSION = "ecs-0.8";

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
        return play(input, rng, null);
    }

    /**
     * Same as {@link #play(EcsMatchInput, RandomSource)}; {@code atFullTime} (may be null) then reads the world, e.g.
     * the calibration harness counting injuries (E-20). It runs after the last tick, so it never changes a result.
     */
    public EcsMatchResult play(EcsMatchInput input, RandomSource rng,
            java.util.function.Consumer<MatchWorld> atFullTime) {
        Live live = start(input, rng);
        while (!live.finished()) {
            live.tick();
        }
        if (atFullTime != null) {
            synchronized (ASHLEY_STATICS) {
                atFullTime.accept(live.world());
            }
        }
        return live.result();
    }

    /**
     * A match readied to be played tick by tick (E-21f: the match screen watches it in the viewer). Playing it to the
     * end gives exactly the result of {@link #play(EcsMatchInput, RandomSource)} for the same input and stream: the
     * viewer's draw passes never change a simulated bit.
     */
    public Live start(EcsMatchInput input, RandomSource rng) {
        Setup s;
        synchronized (ASHLEY_STATICS) {
            s = setUp(input, rng);
        }
        return new Live(s, input, rng);
    }

    /** One match in progress: {@link #tick()} until {@link #finished()}, then {@link #result()}. */
    public final class Live {
        private final Setup s;
        private final EcsMatchInput input;
        private final RandomSource rng;
        private final long halfTicks;
        private final List<EcsMatchResult.Goal> goals = new ArrayList<>();
        private long done;
        private double halfTimeMass;

        private Live(Setup s, EcsMatchInput input, RandomSource rng) {
            this.s = s;
            this.input = input;
            this.rng = rng;
            this.halfTicks = (long) input.halfMinutes() * 60L * params.tickHz;
        }

        public MatchWorld world() {
            return s.world();
        }

        /** Ticks played so far. */
        public long ticksDone() {
            return done;
        }

        /** Ticks of the whole match (two halves). */
        public long totalTicks() {
            return halfTicks * 2;
        }

        public boolean finished() {
            return done >= totalTicks();
        }

        public int homeGoals() {
            return s.play().homeGoals;
        }

        public int awayGoals() {
            return s.play().awayGoals;
        }

        /** The events so far (grows while the match is played). */
        public MatchEventLog events() {
            return s.events().log();
        }

        /**
         * Plays one tick and notes a goal if one was given (E-17a): the side it counts for and the last player to play
         * the ball, and lets the event detector read the tick (E-17). Reads the state only after the tick, so it never
         * changes a simulated bit.
         */
        public void tick() {
            if (finished()) {
                throw new IllegalStateException("the match is over");
            }
            PlayStateComponent play = s.play();
            int home = play.homeGoals;
            int away = play.awayGoals;
            s.world().tick();
            s.events().afterTick();
            done++;
            if (done == halfTicks) {
                halfTimeMass = s.ball().massKg;
            }
            if (play.homeGoals == home && play.awayGoals == away) {
                return;
            }
            Team scoring = play.homeGoals != home ? Team.HOME : Team.AWAY;
            PossessionComponent poss = s.ballEntity().getComponent(PossessionComponent.class);
            int index = poss != null ? poss.lastTouchIndex : -1;
            int team = index >= 0 && index < s.teamOf().length ? s.teamOf()[index] : -1;
            int slot = team >= 0 ? s.slotOf()[index] : -1;
            goals.add(new EcsMatchResult.Goal(scoring, team < 0 ? null : Team.values()[team], slot,
                    s.world().tickCount()));
        }

        /** The result of the finished match. */
        public EcsMatchResult result() {
            if (!finished()) {
                throw new IllegalStateException("the match is not over");
            }
            PlayStateComponent play = s.play();
            long actions = s.world().engine().getSystem(ActionSystem.class).totalActions();
            return new EcsMatchResult(input.matchKey(), ENGINE_VERSION, rng.getSeed(), s.world().tickCount(),
                    s.world().stateHash(), halfTimeMass, s.ball().massKg, play.homeGoals, play.awayGoals, actions,
                    play.offsidesGiven, play.foulsGiven, goals, s.events().log());
        }
    }

    /**
     * Ashley keeps its {@code Family} builder and component-type registry in unsynchronized statics, so building a world
     * (systems, entities, family lookups) on two threads at once can hand a system the wrong family. Matches played on
     * a thread pool (E-19, the E-20 calibration harness) build their worlds under this lock; ticking only reads those
     * statics once every component type is registered, which the world's setup does.
     */
    private static final Object ASHLEY_STATICS = new Object();

    /** The world at kick-off and what {@link #runHalf} needs. */
    private record Setup(MatchWorld world, Entity ballEntity, BallComponent ball, PlayStateComponent play,
            int[] teamOf, int[] slotOf, MatchEventDetector events) {
    }

    private Setup setUp(EcsMatchInput input, RandomSource rng) {
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
        world.engine().getSystem(ActionSystem.class).setHomeGround(!input.neutral());

        int[] teamOf = new int[world.engine().getEntities().size()];
        int[] slotOf = new int[teamOf.length];
        java.util.Arrays.fill(teamOf, -1);
        for (Entity e : world.engine().getEntitiesFor(Family.all(BodyComponent.class, IdentityComponent.class).get())) {
            int index = e.getComponent(IdentityComponent.class).index;
            BodyComponent body = e.getComponent(BodyComponent.class);
            teamOf[index] = body.team.ordinal();
            slotOf[index] = body.slot;
        }

        MatchEventDetector events = new MatchEventDetector(world, pitch, ballEntity, play, input.pitch(),
                input.halfMinutes(), params.ballDryMassKg);
        return new Setup(world, ballEntity, ball, play, teamOf, slotOf, events);
    }

    private static AiSetup.Side side(EcsMatchInput.Side s) {
        return new AiSetup.Side(s.sheet().shape(), s.style());
    }
}
