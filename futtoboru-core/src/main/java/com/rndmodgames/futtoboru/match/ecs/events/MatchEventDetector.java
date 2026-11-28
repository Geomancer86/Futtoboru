package com.rndmodgames.futtoboru.match.ecs.events;

import java.util.Arrays;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.action.ActionRecord;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.ContactRecord;
import com.rndmodgames.futtoboru.match.ecs.body.ContactSystem;
import com.rndmodgames.futtoboru.match.ecs.laws.LawRecord;
import com.rndmodgames.futtoboru.match.ecs.laws.LawsSystem;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;

/**
 * Event detection (E-17, docs/design/MATCH_ENGINE_ECS.md §5.1): after each tick, reads that tick's
 * {@link ActionRecord}s, {@link ContactRecord}s and {@link LawRecord}s plus the ball, and appends what happened to a
 * {@link MatchEventLog}. It only reads simulation state and draws no random numbers, so it never changes a simulated
 * bit (the state hash and the goldens are the same with or without it). Per tick it allocates nothing.
 *
 * What it notes (level in brackets, D19-9):
 * <ul>
 * <li>KICK_OFF from the restart taken at the centre [key when it opens a half, else full];</li>
 * <li>SHOT: a kick with the SHOT intent, or a header aimed at the goal mouth [extended]; SAVE: the defending keeper
 *     the next man to play a shot, within {@link #SAVE_WINDOW_SECONDS} [extended];</li>
 * <li>GOAL with the last man to play the ball (own goal when he is of the other side) and DISPUTED_GOAL with both
 *     umpires' votes and the referee's [key];</li>
 * <li>OFFSIDE_APPEAL and FOUL (charges, handball, carrying) with the votes [extended when given, full when refused];
 *     SENDING_OFF [key];</li>
 * <li>INJURY from a contact: knock [extended], passenger or carried off [key]; LACE_CUT [key];</li>
 * <li>HEAVY_BALL once the wet leather has gained {@link #HEAVY_BALL_GAIN} of its dry mass [extended];</li>
 * <li>HALF_TIME and FULL_TIME with the score [key].</li>
 * </ul>
 */
public final class MatchEventDetector {

    /** A keeper's touch this long after a shot (and nobody else's in between) is a save. */
    public static final double SAVE_WINDOW_SECONDS = 4.0;
    /** Share of the dry mass the ball must gain from water to be called heavy (BKP §1.3: up to 0.31). */
    public static final double HEAVY_BALL_GAIN = 0.15;
    /** A header aimed this close to the goal line, within the posts plus this margin, is a shot (m). */
    static final double HEADER_GOAL_MARGIN_M = 1.5;

    private final MatchWorld world;
    private final PitchGeometry pitch;
    private final BallComponent ball;
    private final Entity ballEntity;
    private final PlayStateComponent play;
    private final ActionSystem actions;
    private final ContactSystem contacts;
    private final LawsSystem laws;
    private final MatchEventLog log;
    private final double heavyMassKg;
    private final long saveWindowTicks;

    /** By creation index: team ordinal (-1 not a player), slot, athlete. */
    private final int[] teamOf;
    private final int[] slotOf;
    private final AthleteComponent[] athleteOf;

    private boolean nextKickOffOpens = true;
    private boolean heavyLogged;
    private int shotTeam = -1;
    private int shotRef = -1;
    private long shotTick;

    /**
     * Call once the world is fully set up (players, ball, officials, play state, AI) and before the first tick.
     *
     * @param conditions the ground and weather (kept in the log for the report)
     * @param dryMassKg  the ball's dry mass
     */
    public MatchEventDetector(MatchWorld world, PitchGeometry pitch, Entity ballEntity, PlayStateComponent play,
            PitchConditions conditions, int halfMinutes, double dryMassKg) {
        this.world = world;
        this.pitch = pitch;
        this.ballEntity = ballEntity;
        this.ball = ballEntity.getComponent(BallComponent.class);
        this.play = play;
        this.actions = world.engine().getSystem(ActionSystem.class);
        this.contacts = world.engine().getSystem(ContactSystem.class);
        this.laws = world.engine().getSystem(LawsSystem.class);
        int tickHz = (int) Math.round(1.0 / world.tickSeconds());
        Surface surface = conditions != null ? conditions.surfaceAt(0, 0) : null;
        int wet = conditions != null ? (int) Math.round(conditions.wetness() * 1000) : 0;
        this.log = new MatchEventLog(tickHz, halfMinutes, surface != null ? surface.ordinal() : -1, wet);
        this.heavyMassKg = dryMassKg * (1 + HEAVY_BALL_GAIN);
        this.heavyLogged = ball != null && ball.massKg >= heavyMassKg;
        this.saveWindowTicks = Math.round(SAVE_WINDOW_SECONDS * tickHz);

        int n = 0;
        ImmutableArray<Entity> all = world.engine().getEntitiesFor(Family.all(IdentityComponent.class).get());
        for (Entity e : all) {
            n = Math.max(n, e.getComponent(IdentityComponent.class).index + 1);
        }
        teamOf = new int[n];
        slotOf = new int[n];
        athleteOf = new AthleteComponent[n];
        Arrays.fill(teamOf, -1);
        Arrays.fill(slotOf, -1);
        for (Entity e : world.engine().getEntitiesFor(Family.all(BodyComponent.class, IdentityComponent.class).get())) {
            int index = e.getComponent(IdentityComponent.class).index;
            BodyComponent body = e.getComponent(BodyComponent.class);
            teamOf[index] = body.team.ordinal();
            slotOf[index] = body.slot;
            athleteOf[index] = e.getComponent(AthleteComponent.class);
        }
    }

    /** The events so far. */
    public MatchEventLog log() {
        return log;
    }

    /** Reads the tick just run ({@code world.tickCount() - 1}). */
    public void afterTick() {
        long now = world.tickCount() - 1;
        if (actions != null) {
            for (int k = 0, n = actions.recordCount(); k < n; k++) {
                ActionRecord r = actions.record(k);
                if (r.tick == now) {
                    action(r, now);
                }
            }
        }
        if (contacts != null) {
            for (int k = 0, n = contacts.contactCount(); k < n; k++) {
                ContactRecord c = contacts.contact(k);
                if (c.tick == now) {
                    contact(c, now);
                }
            }
        }
        if (laws != null) {
            for (int k = 0, n = laws.recordCount(); k < n; k++) {
                LawRecord r = laws.record(k);
                if (r.tick == now) {
                    law(r, now);
                }
            }
        }
        if (!heavyLogged && ball != null && ball.massKg >= heavyMassKg) {
            heavyLogged = true;
            log.add(now, MatchEventType.HEAVY_BALL, -1, -1, -1, ball.x, ball.y, (int) Math.round(ball.massKg * 1000));
        }
    }

    // ---- ball actions -----------------------------------------------------------------------------------------------

    private void action(ActionRecord r, long now) {
        if (r.outcome == ActionRecord.RELEASED_CARRY_LIMIT || r.outcome == ActionRecord.DROPPED_KNOCKED_DOWN) {
            return;
        }
        int ref = ref(r.actor);
        if (shotTeam >= 0) {
            AthleteComponent a = r.actor >= 0 && r.actor < athleteOf.length ? athleteOf[r.actor] : null;
            if (r.team != shotTeam && a != null && a.isKeeper() && now - shotTick <= saveWindowTicks) {
                log.add(now, MatchEventType.SAVE, r.team, ref, shotRef, r.beforeX, r.beforeY, r.outcome);
            }
            shotTeam = -1;
        }
        if (r.laceCut) {
            log.add(now, MatchEventType.LACE_CUT, r.team, ref, -1, r.beforeX, r.beforeY, 0);
        }
        boolean shot = (r.outcome == ActionRecord.KICKED && r.intent == ActionIntent.SHOT)
                || (r.outcome == ActionRecord.HEADED && headerAtGoal(r));
        if (shot) {
            double speed = Math.sqrt(r.afterVx * r.afterVx + r.afterVy * r.afterVy + r.afterVz * r.afterVz);
            int data = (r.outcome == ActionRecord.HEADED ? 1 : 0) | ((int) Math.min(4000, Math.round(speed * 10)) << 1);
            log.add(now, MatchEventType.SHOT, r.team, ref, -1, r.beforeX, r.beforeY, data);
            shotTeam = r.team;
            shotRef = ref;
            shotTick = now;
        }
    }

    private boolean headerAtGoal(ActionRecord r) {
        if (r.team < 0 || r.team > 1) {
            return false;
        }
        int sign = play.attackSign(com.rndmodgames.futtoboru.match.ecs.pitch.Team.values()[r.team]);
        return r.targetX * sign >= pitch.halfLength() - HEADER_GOAL_MARGIN_M
                && Math.abs(r.targetY) <= pitch.goalMouthHalfWidth() + HEADER_GOAL_MARGIN_M;
    }

    // ---- contacts ---------------------------------------------------------------------------------------------------

    private void contact(ContactRecord c, long now) {
        if (c.aInjury > 0) {
            injury(c.a, c.b, c.aInjury, c, now);
        }
        if (c.bInjury > 0) {
            injury(c.b, c.a, c.bInjury, c, now);
        }
    }

    private void injury(int hurt, int other, int severity, ContactRecord c, long now) {
        int team = team(hurt);
        if (team < 0) {
            return;
        }
        log.add(now, MatchEventType.INJURY, team, ref(hurt), ref(other), c.x, c.y,
                Math.max(MatchEvent.KNOCK, Math.min(MatchEvent.CARRIED_OFF, severity)));
    }

    // ---- laws -------------------------------------------------------------------------------------------------------

    private void law(LawRecord r, long now) {
        switch (r.type) {
            case LawRecord.RESTART_TAKEN:
                if (r.restartType == RestartType.KICK_OFF.ordinal()) {
                    log.add(now, MatchEventType.KICK_OFF, r.restartTeam, ref(r.offender), -1, r.x, r.y,
                            nextKickOffOpens ? 1 : 0);
                    nextKickOffOpens = false;
                }
                break;
            case LawRecord.GOAL:
                shotTeam = -1;
                goal(r, now, false);
                break;
            case LawRecord.DISPUTED_GOAL: {
                shotTeam = -1;
                int data = votes(r, 0) | (r.scoringTeam >= 0 ? MatchEvent.GOAL_STOOD : 0);
                log.add(now, MatchEventType.DISPUTED_GOAL, r.appealingTeam, -1, -1, r.x, r.y, data);
                if (r.scoringTeam >= 0) {
                    goal(r, now, true);
                }
                break;
            }
            case LawRecord.OUT_OF_PLAY:
                shotTeam = -1;
                break;
            case LawRecord.OFFSIDE:
                if (r.appealed) {
                    log.add(now, MatchEventType.OFFSIDE_APPEAL, r.appealingTeam, ref(r.offender), -1, r.x, r.y,
                            votes(r, 0));
                }
                break;
            case LawRecord.FOUL:
            case LawRecord.HANDBALL:
            case LawRecord.CARRYING:
                if (r.appealed) {
                    int kind = r.type == LawRecord.HANDBALL ? MatchEvent.FOUL_HANDBALL
                            : r.type == LawRecord.CARRYING ? MatchEvent.FOUL_CARRYING : r.foulKind;
                    log.add(now, MatchEventType.FOUL, team(r.offender), ref(r.offender), ref(r.victim), r.x, r.y,
                            votes(r, kind));
                }
                break;
            case LawRecord.SENDING_OFF:
                log.add(now, MatchEventType.SENDING_OFF, team(r.offender), ref(r.offender), -1, r.x, r.y, 0);
                break;
            case LawRecord.HALF_TIME:
                shotTeam = -1;
                nextKickOffOpens = true;
                log.add(now, MatchEventType.HALF_TIME, -1, -1, -1, 0, 0, score());
                break;
            case LawRecord.FULL_TIME:
                shotTeam = -1;
                log.add(now, MatchEventType.FULL_TIME, -1, -1, -1, 0, 0, score());
                break;
            default:
                break;
        }
    }

    private void goal(LawRecord r, long now, boolean disputed) {
        PossessionComponent poss = ballEntity.getComponent(PossessionComponent.class);
        int index = poss != null ? poss.lastTouchIndex : -1;
        int team = team(index);
        boolean own = team >= 0 && team != r.scoringTeam;
        log.add(now, MatchEventType.GOAL, r.scoringTeam, ref(index), -1, r.x, r.y,
                (own ? 1 : 0) | (disputed ? 2 : 0));
        nextKickOffOpens = false;
    }

    private static int votes(LawRecord r, int kind) {
        return MatchEvent.appeal(r.homeUmpireVote, r.awayUmpireVote, r.refereeCalled, r.refereeVote, r.given, kind);
    }

    private int score() {
        return Math.min(255, play.homeGoals) | (Math.min(255, play.awayGoals) << 8);
    }

    private int team(int index) {
        return index >= 0 && index < teamOf.length ? teamOf[index] : -1;
    }

    private int ref(int index) {
        int team = team(index);
        return team < 0 ? -1 : MatchEvent.ref(team, slotOf[index]);
    }
}
