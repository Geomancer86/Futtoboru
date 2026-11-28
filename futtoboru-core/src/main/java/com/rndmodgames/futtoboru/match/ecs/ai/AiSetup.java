package com.rndmodgames.futtoboru.match.ecs.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.Restart;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Wires the player AI (E-15) into a world whose players and ball are spawned: a {@link SpawnRole#TEAM} entity per side
 * with its {@link TeamTacticsComponent}, a {@link PerceptionComponent} per player, and the ball's
 * {@link PlayStateComponent} (dead for the opening kick-off) and {@link PossessionComponent}. Also the half-time hook
 * (until E-16 owns the match clock): rest, change of ends, the other side kicks off.
 */
public final class AiSetup {

    private AiSetup() {
    }

    /** One side's choices before the match (no tactics changes during it yet). */
    public record Side(Shape shape, TeamStyle style) {

        public Side {
            if (shape == null || style == null) {
                throw new IllegalArgumentException("shape and style are required");
            }
        }
    }

    /**
     * Call after the players (with bodies), the ball and any officials are spawned, before the first tick.
     *
     * @param ends       the ends of the first half
     * @param kickingOff the side that kicks off
     */
    public static void install(MatchWorld world, Side home, Side away, MatchEnds ends, Team kickingOff) {
        ImmutableArray<Entity> players = world.engine().getEntitiesFor(Family.all(BodyComponent.class).get());
        int n = players.size();
        for (int i = 0; i < n; i++) {
            players.get(i).add(new PerceptionComponent(n));
        }
        world.spawn(SpawnRole.TEAM).add(new TeamTacticsComponent(Team.HOME, home.shape(), home.style(), ends));
        world.spawn(SpawnRole.TEAM).add(new TeamTacticsComponent(Team.AWAY, away.shape(), away.style(), ends));
        Entity ballEntity = ball(world);
        BallComponent ball = ballEntity.getComponent(BallComponent.class);
        if (ballEntity.getComponent(PossessionComponent.class) == null) {
            ballEntity.add(new PossessionComponent());
        }
        PlayStateComponent play = new PlayStateComponent();
        play.westDefender = ends.westDefender();
        play.kickedOffFirst = kickingOff;
        Restart kickOff = restarts(world).decider().kickOff(kickingOff);
        play.dead(kickOff, -1, (long) (world.params().ai.kickOffWaitS * 0.2 * world.params().tickHz));
        play.prevX = ball.x;
        play.prevY = ball.y;
        play.prevZ = ball.z + world.params().ballRadiusM;
        ballEntity.add(play);
        ActionSystem actions = world.engine().getSystem(ActionSystem.class);
        if (actions != null) {
            actions.setEnds(ends);
        }
    }

    /**
     * Half-time: everyone rests ({@link AthleteComponent#halfTime}), ends change, the players line up for the second
     * half's kick-off (they leave the field at the interval, so they are placed, not run there), and the side that did
     * not kick off at the start kicks off.
     */
    public static void halfTime(MatchWorld world, PitchGeometry pitch) {
        Entity ballEntity = ball(world);
        PlayStateComponent play = ballEntity.getComponent(PlayStateComponent.class);
        BallComponent ball = ballEntity.getComponent(BallComponent.class);
        PossessionComponent poss = ballEntity.getComponent(PossessionComponent.class);
        if (play == null) {
            throw new IllegalStateException("AiSetup.install was not called");
        }
        play.half = 2;
        play.westDefender = play.westDefender.other();
        MatchEnds ends = play.ends();
        Team kicking = play.kickedOffFirst.other();
        ImmutableArray<Entity> teams = world.engine().getEntitiesFor(Family.all(TeamTacticsComponent.class).get());
        TeamTacticsComponent[] tactics = new TeamTacticsComponent[2];
        for (int t = 0; t < teams.size(); t++) {
            TeamTacticsComponent tc = teams.get(t).getComponent(TeamTacticsComponent.class);
            tc.setEnds(ends);
            tc.restartTaker = -1;
            tc.takerForDeadTick = Long.MIN_VALUE;
            tactics[tc.team.ordinal()] = tc;
        }
        ActionSystem actions = world.engine().getSystem(ActionSystem.class);
        if (actions != null) {
            actions.setEnds(ends);
        }
        double[] spot = new double[2];
        ImmutableArray<Entity> players = world.engine().getEntitiesFor(Family.all(BodyComponent.class).get());
        for (int i = 0; i < players.size(); i++) {
            Entity e = players.get(i);
            BodyComponent b = e.getComponent(BodyComponent.class);
            AthleteComponent a = e.getComponent(AthleteComponent.class);
            InjuryComponent inj = e.getComponent(InjuryComponent.class);
            MoveCommand move = e.getComponent(MoveCommand.class);
            ActionCommand act = e.getComponent(ActionCommand.class);
            if (a != null) {
                a.halfTime(world.params().body);
            }
            if (move != null) {
                move.stop();
            }
            if (act != null) {
                act.cancel();
            }
            TeamTacticsComponent tc = tactics[b.team.ordinal()];
            int slot = tc == null ? b.slot : tc.slotOf[b.slot];
            if (slot == TeamTacticsComponent.NO_SLOT || inj != null && inj.isOff()) {
                continue;
            }
            Shape shape = tc == null ? Shape.TWO_THREE_FIVE : tc.shape;
            int own = ends.endDefendedBy(b.team).sign();
            BodySpawner.kickOffSpot(pitch, shape, slot, b.radius, world.params().ballRadiusM, own, b.team == kicking,
                    spot);
            b.x = spot[0];
            b.y = spot[1];
            b.prevX = b.x;
            b.prevY = b.y;
            b.vx = 0;
            b.vy = 0;
            b.stumbleTicks = 0;
            b.downTicks = 0;
            b.face(own < 0 ? 0 : StrictMath.PI);
        }
        if (poss != null && poss.held) {
            poss.release();
        }
        Restart kickOff = restarts(world).decider().kickOff(kicking);
        long tick = world.tickCount();
        play.dead(kickOff, tick - 1, tick + (long) (world.params().ai.kickOffWaitS * 0.2 * world.params().tickHz));
        StandInRestartSystem.pin(ball, play.spotX, play.spotY);
        play.prevX = ball.x;
        play.prevY = ball.y;
        play.prevZ = ball.z + world.params().ballRadiusM;
    }

    private static Entity ball(MatchWorld world) {
        ImmutableArray<Entity> balls = world.engine().getEntitiesFor(Family.all(BallComponent.class).get());
        if (balls.size() == 0) {
            throw new IllegalStateException("spawn the ball first");
        }
        return balls.get(0);
    }

    private static StandInRestartSystem restarts(MatchWorld world) {
        StandInRestartSystem s = world.engine().getSystem(StandInRestartSystem.class);
        if (s == null) {
            throw new IllegalStateException("MatchSystems.installSimulation was not called");
        }
        return s;
    }
}
