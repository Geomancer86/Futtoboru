package com.rndmodgames.futtoboru.match.ecs.laws;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.action.ActionRecord;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.ContactRecord;
import com.rndmodgames.futtoboru.match.ecs.body.ContactSystem;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.LocomotionSystem;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.Flank;
import com.rndmodgames.futtoboru.match.ecs.pitch.LawDecision;
import com.rndmodgames.futtoboru.match.ecs.pitch.LineCrossing;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchEnd;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Restart;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartDecider;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;
import com.rndmodgames.futtoboru.match.ecs.pitch.SweepResult;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.futtoboru.match.ecs.pitch.Vec3;
import com.rndmodgames.futtoboru.match.ecs.pitch.BallSegment;

/**
 * The Laws of the season on the pitch (E-16, priority LAWS: after actions, ball and contacts; ECS §4.2, V2 §1).
 * Active only when the ball carries a {@link PlayStateComponent} ({@link PlayStateComponent#attach}).
 *
 * <ul>
 * <li><b>Dead ball:</b> the ball is kept on the restart spot until a player of the restart side plays it after
 * {@link LawsParams#restartDelaySeconds} (a throw-in only with a two-handed throw); then it is in play.</li>
 * <li><b>Out of play:</b> the ball wholly over a line ({@link PitchGeometry#sweep} of the ball centre from last tick's,
 * or from where it last bounced off a post or the bar this tick, {@link BallSystem#frameContactThisTick()}) goes to the
 * {@link RestartDecider}: throw-in, goal kick, corner, goal (kick-off). A close call with no nets is a disputed goal:
 * the side the line decision goes against appeals.</li>
 * <li><b>Offside:</b> every time the ball is played ({@code ActionRecord}) the margins of the player's team-mates are
 * snapshotted with the {@link OffsideRule} of the season (none from a goal kick or corner). When one of them then
 * plays the ball himself, the defenders may appeal (a near miss too, less often).</li>
 * <li><b>Fouls:</b> from this tick's {@code ContactRecord}s: a charge from behind, a charge on a protected keeper who
 * is not holding the ball (from 1892), or a fair charge that floored a man (claimed, mostly refused). Handball: a
 * player who reached for the ball with his hands where the law forbids it ({@code RESULT_ILLEGAL_HANDLING} with the
 * ball in hand reach). Carrying: a keeper forced to drop the ball at the step limit.</li>
 * <li><b>Appeals</b> are decided by the {@link Adjudicator} (club umpires, referee settles). Upheld: indirect free kick
 * where it happened (direct from 1903; a penalty from 1891 in the penalty zone), very rarely a sending-off after a
 * violent foul. Refused: play goes on.</li>
 * <li><b>Clock:</b> half-time at the end of the tick that reaches {@link PlayStateComponent#halfTicks}: ends change,
 * players and officials change ends (mirrored through the centre), {@link AthleteComponent#halfTime}, kick-off by the
 * other side. Full time after two halves: the ball stays dead.</li>
 * </ul>
 * Each decision is written to a reused {@link LawRecord} for event detection (E-17). Random draws come from the
 * {@code laws:appeal}, {@code officials:noise} and {@code laws:discipline} forks. No allocation per tick except when
 * the ball goes out of play (the swept test builds its records then).
 */
public final class LawsSystem extends EntitySystem implements SimSystem {

    private static final Team[] TEAMS = Team.values();
    private static final RestartType[] RESTARTS = RestartType.values();

    public static final int MAX_RECORDS_PER_TICK = 16;

    private final ComponentMapper<BodyComponent> bodyMap = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<AthleteComponent> athleteMap = ComponentMapper.getFor(AthleteComponent.class);
    private final ComponentMapper<InjuryComponent> injuryMap = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<MoveCommand> moveMap = ComponentMapper.getFor(MoveCommand.class);
    private final ComponentMapper<ActionCommand> commandMap = ComponentMapper.getFor(ActionCommand.class);
    private final ComponentMapper<IdentityComponent> idMap = ComponentMapper.getFor(IdentityComponent.class);
    private final ComponentMapper<BallComponent> ballMap = ComponentMapper.getFor(BallComponent.class);
    private final ComponentMapper<PossessionComponent> possessionMap = ComponentMapper.getFor(PossessionComponent.class);
    private final ComponentMapper<PlayStateComponent> stateMap = ComponentMapper.getFor(PlayStateComponent.class);
    private final ComponentMapper<OfficialComponent> officialMap = ComponentMapper.getFor(OfficialComponent.class);

    private final MatchWorld world;
    private final PhysicsParams pp;
    private final LawsParams lp;
    private final RulesProfile rules;
    private final RandomSource appealRandom;
    private final RandomSource discipline;
    private final Adjudicator adjudicator;
    private final Adjudicator.Noise offsideNoise;
    private final Adjudicator.Noise foulNoise;
    private final Adjudicator.Noise goalNoise;
    private final Adjudicator.Noise carryNoise;
    private final OffsideRule.NthSmallest nth;
    private PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
    private RestartDecider decider;

    private Engine engine;
    private ActionSystem actions;
    private ContactSystem contacts;
    private BallSystem ballSystem;
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> balls;
    private ImmutableArray<Entity> officialEntities;
    private int cached = -1;
    private int cachedOfficials = -1;
    private BodyComponent[] body = new BodyComponent[0];
    private AthleteComponent[] athlete = new AthleteComponent[0];
    private InjuryComponent[] injury = new InjuryComponent[0];
    private MoveCommand[] move = new MoveCommand[0];
    private ActionCommand[] command = new ActionCommand[0];
    private int[] index = new int[0];
    private OfficialComponent[] officials = new OfficialComponent[0];

    private final LawRecord[] records = new LawRecord[MAX_RECORDS_PER_TICK];
    private int recordCount;
    private long dropped;

    private MatchEnds ends = new MatchEnds(Team.HOME);
    private Restart current;
    private PlayStateComponent state;
    private BallComponent ball;
    private PossessionComponent poss;

    public LawsSystem(MatchWorld world) {
        super(SystemPriority.LAWS);
        this.world = world;
        this.pp = world.params();
        this.lp = pp.laws;
        this.rules = pp.rules;
        this.appealRandom = world.random().fork("laws:appeal");
        this.discipline = world.random().fork("laws:discipline");
        this.adjudicator = new Adjudicator(world.random().fork("officials:noise"));
        this.offsideNoise = new Adjudicator.Noise(lp.offsideSigmaBaseM, lp.offsideSigmaPerM, lp.offsideSigmaPerLineM,
                lp.offsideLeanM);
        this.foulNoise = new Adjudicator.Noise(lp.foulSigmaBase, lp.foulSigmaPerM, 0, lp.foulLean);
        this.goalNoise = new Adjudicator.Noise(lp.goalSigmaBaseM, lp.goalSigmaPerM, 0, lp.goalLeanM);
        this.carryNoise = new Adjudicator.Noise(lp.carrySigmaSteps, 0, 0, lp.carryLeanSteps);
        this.nth = new OffsideRule.NthSmallest(rules.offsideOpponents());
        this.decider = new RestartDecider(pitch, rules.restartLaws());
        for (int i = 0; i < records.length; i++) {
            records[i] = new LawRecord();
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
        players = engine.getEntitiesFor(LocomotionSystem.family());
        balls = engine.getEntitiesFor(Family.all(BallComponent.class).get());
        officialEntities = engine.getEntitiesFor(Family.all(OfficialComponent.class).get());
    }

    /** The ground (lines, goal frames, penalty markings). Default: 110 x 70 yd, 1888-89. */
    public void setPitch(PitchGeometry pitch) {
        if (pitch == null) {
            throw new IllegalArgumentException("pitch");
        }
        this.pitch = pitch;
        this.decider = new RestartDecider(pitch, rules.restartLaws());
    }

    // ---- tick -------------------------------------------------------------------------------------------------------

    @Override
    public void update(float ignoredFrameDelta) {
        recordCount = 0;
        if (balls.size() == 0) {
            return;
        }
        Entity ballEntity = balls.get(0);
        state = stateMap.get(ballEntity);
        if (state == null) {
            return;
        }
        ball = ballMap.get(ballEntity);
        poss = possessionMap.get(ballEntity);
        if (actions == null) {
            actions = engine.getSystem(ActionSystem.class);
        }
        if (contacts == null) {
            contacts = engine.getSystem(ContactSystem.class);
        }
        if (ballSystem == null) {
            ballSystem = engine.getSystem(BallSystem.class);
        }
        if (players.size() != cached) {
            cache();
        }
        if (officialEntities.size() != cachedOfficials) {
            cacheOfficials();
        }
        long now = world.tickCount();
        if (!state.started) {
            start();
        }
        if (state.phase == PlayStateComponent.FULL_TIME) {
            pinBall(ball.x, ball.y);
            rememberBall();
            return;
        }
        int firstRecord = 0;
        if (state.phase == PlayStateComponent.STOPPED) {
            firstRecord = takeRestart(now);
            if (firstRecord < 0) {
                pinBall(state.restartX, state.restartY);
            }
        }
        if (state.phase == PlayStateComponent.IN_PLAY) {
            inPlay(now, firstRecord);
        }
        clock(now);
        rememberBall();
    }

    private void inPlay(long now, int firstRecord) {
        int n = actions == null ? 0 : actions.recordCount();
        for (int k = firstRecord; k < n && state.inPlay(); k++) {
            ActionRecord r = actions.record(k);
            if (r.outcome == ActionRecord.RELEASED_CARRY_LIMIT) {
                carrying(r, now);
            } else if (r.outcome != ActionRecord.DROPPED_KNOCKED_DOWN) {
                played(r, now);
            }
        }
        if (state.inPlay()) {
            handball(now);
        }
        if (state.inPlay() && contacts != null) {
            for (int k = 0; k < contacts.contactCount() && state.inPlay(); k++) {
                contact(contacts.contact(k), now);
            }
        }
        if (state.inPlay()) {
            outOfPlay(now);
        }
    }

    // ---- start, restarts --------------------------------------------------------------------------------------------

    private void start() {
        state.started = true;
        ends = new MatchEnds(TEAMS[state.westDefender]);
        if (actions != null) {
            actions.setEnds(ends);
        }
        stop(decider.kickOff(TEAMS[state.kickedOffFirst]), world.tickCount());
        state.prevBallX = ball.x;
        state.prevBallY = ball.y;
        state.prevBallZ = ball.z;
    }

    /** Puts the ball dead for {@code restart}, takeable from {@code readyTick}. */
    private void stop(Restart restart, long readyTick) {
        current = restart;
        state.phase = PlayStateComponent.STOPPED;
        state.restartType = restart.type().ordinal();
        state.restartTeam = restart.team().ordinal();
        state.restartX = restart.spot().x();
        state.restartY = restart.spot().y();
        state.restartOffside = restart.offsideApplies();
        state.restartGoalDirect = restart.goalDirectAllowed();
        state.readyTick = readyTick;
        state.directFrom = -1;
        state.directTaker = -1;
        clearSnapshot();
        if (poss != null && poss.held) {
            poss.release();
        }
        pinBall(state.restartX, state.restartY);
    }

    private long delayTicks(double seconds) {
        return Math.round(seconds * pp.tickHz);
    }

    /** At a dead ball: the index of the first action record after a valid take (play is on), or -1. */
    private int takeRestart(long now) {
        if (now < state.readyTick || actions == null) {
            return -1;
        }
        RestartType type = state.restartType();
        for (int k = 0; k < actions.recordCount(); k++) {
            ActionRecord r = actions.record(k);
            if (r.team != state.restartTeam || r.outcome == ActionRecord.RELEASED_CARRY_LIMIT
                    || r.outcome == ActionRecord.DROPPED_KNOCKED_DOWN) {
                continue;
            }
            boolean ok = type == RestartType.THROW_IN ? r.intent == ActionIntent.THROW_IN
                    : r.intent != ActionIntent.THROW_IN && !r.handled;
            if (!ok) {
                continue;
            }
            state.phase = PlayStateComponent.IN_PLAY;
            state.directFrom = state.restartType;
            state.directTaker = r.actor;
            LawRecord rec = record(LawRecord.RESTART_TAKEN, now);
            if (rec != null) {
                rec.offender = r.actor;
                rec.x = state.restartX;
                rec.y = state.restartY;
                rec.restartType = state.restartType;
                rec.restartTeam = state.restartTeam;
                rec.restartX = state.restartX;
                rec.restartY = state.restartY;
            }
            if (state.restartOffside) {
                snapshot(r.team, r.actor, r.beforeX, now);
            } else {
                clearSnapshot();
            }
            return k + 1;
        }
        return -1;
    }

    private void pinBall(double x, double y) {
        ball.x = x;
        ball.y = y;
        ball.z = 0;
        ball.vx = 0;
        ball.vy = 0;
        ball.vz = 0;
        ball.spin = 0;
        ball.state = BallComponent.REST;
    }

    private void rememberBall() {
        state.prevBallX = ball.x;
        state.prevBallY = ball.y;
        state.prevBallZ = ball.z;
    }

    // ---- the ball played: offside -----------------------------------------------------------------------------------

    private void played(ActionRecord r, long now) {
        if (state.directTaker != r.actor) {
            state.directFrom = -1;
            state.directTaker = -1;
        }
        int q = r.actor;
        if (state.snapshotTeam == r.team && q >= 0 && q < PlayStateComponent.MAX_TRACKED
                && (state.snapshotMask & (1L << q)) != 0) {
            if (offsideInvolvement(r, q, now)) {
                return;
            }
        }
        snapshot(r.team, r.actor, r.beforeX, now);
    }

    /** {@code q} plays the ball after a team-mate: appeal if he was (nearly) offside. True if play stopped. */
    private boolean offsideInvolvement(ActionRecord r, int q, long now) {
        double m = state.margin[q];
        double near = lp.offsideNearMarginM;
        if (m <= -near) {
            return false;
        }
        double chance = m > 0 ? lp.appealOffside : lp.appealNearOffside * (1 + m / near);
        boolean appeal = appealRandom.nextDouble() < chance;
        if (!appeal && !(m > 0)) {
            return false;
        }
        Team attackers = TEAMS[r.team];
        Team defenders = attackers.other();
        int slot = slotOf(q);
        double x = slot >= 0 ? body[slot].x : r.beforeX;
        double y = slot >= 0 ? body[slot].y : r.beforeY;
        LawRecord rec = record(LawRecord.OFFSIDE, now);
        LawRecord scratch = rec != null ? rec : spare;
        scratch.offender = q;
        scratch.x = x;
        scratch.y = y;
        if (!appeal) {
            scratch.evidence = m;
            scratch.appealingTeam = -1;
            return false;
        }
        state.appeals++;
        int sign = state.attackSign(attackers);
        double lineX = x + sign * m; // the line he was beyond
        boolean given = adjudicator.judge(officials, cachedOfficials, rules.refereeOnField(), scratch,
                defenders.ordinal(), m, offsideNoise, x, y, lineX);
        if (!given) {
            return false;
        }
        state.appealsGiven++;
        state.offsidesGiven++;
        award(scratch, freeKick(defenders, x, y, false), now);
        return true;
    }

    /** New snapshot: the ball played by {@code passer} of {@code team} at {@code ballX}. */
    private void snapshot(int team, int passer, double ballX, long now) {
        Team attackers = TEAMS[team];
        int sign = state.attackSign(attackers);
        double hl = pitch.halfLength();
        nth.reset();
        for (int i = 0; i < cached; i++) {
            if (body[i].team != attackers && onField(i)) {
                nth.add(OffsideRule.depth(body[i].x, sign, hl));
            }
        }
        double nthDepth = nth.value();
        double ballDepth = OffsideRule.depth(ballX, sign, hl);
        long mask = 0;
        for (int i = 0; i < cached; i++) {
            int q = index[i];
            if (body[i].team != attackers || q == passer || q < 0 || q >= PlayStateComponent.MAX_TRACKED
                    || !onField(i)) {
                continue;
            }
            state.margin[q] = OffsideRule.margin(OffsideRule.depth(body[i].x, sign, hl), ballDepth, nthDepth,
                    rules.offsideOwnHalfExempt(), hl);
            mask |= 1L << q;
        }
        state.snapshotTeam = team;
        state.snapshotPasser = passer;
        state.snapshotTick = now;
        state.snapshotMask = mask;
    }

    private void clearSnapshot() {
        state.snapshotTeam = -1;
        state.snapshotPasser = -1;
        state.snapshotTick = -1;
        state.snapshotMask = 0;
    }

    private boolean onField(int slot) {
        return !injury[slot].isOff();
    }

    // ---- handball, carrying, fouls ---------------------------------------------------------------------------------

    private void handball(long now) {
        double reach = pp.action.handReachM;
        for (int i = 0; i < cached && state.inPlay(); i++) {
            ActionCommand c = command[i];
            if (c == null || c.result != ActionCommand.RESULT_ILLEGAL_HANDLING || c.resultTick != now
                    || !onField(i)) {
                continue;
            }
            BodyComponent b = body[i];
            double dx = ball.x - b.x;
            double dy = ball.y - b.y;
            if (dx * dx + dy * dy > (b.radius + reach) * (b.radius + reach) || ball.z > pp.action.handMaxZ) {
                continue;
            }
            if (appealRandom.nextDouble() >= lp.appealHandball) {
                continue;
            }
            Team wronged = b.team.other();
            LawRecord rec = recordOrSpare(LawRecord.HANDBALL, now);
            rec.offender = index[i];
            rec.x = ball.x;
            rec.y = ball.y;
            state.appeals++;
            if (adjudicator.judge(officials, cachedOfficials, rules.refereeOnField(), rec, wronged.ordinal(), 1,
                    foulNoise, ball.x, ball.y, Double.NaN)) {
                state.appealsGiven++;
                award(rec, freeKick(wronged, ball.x, ball.y, true), now);
            }
        }
    }

    private void carrying(ActionRecord r, long now) {
        if (appealRandom.nextDouble() >= lp.appealCarry) {
            return;
        }
        Team wronged = TEAMS[r.team].other();
        LawRecord rec = recordOrSpare(LawRecord.CARRYING, now);
        rec.offender = r.actor;
        rec.x = r.beforeX;
        rec.y = r.beforeY;
        state.appeals++;
        double evidence = r.carrySteps - pp.action.maxCarrySteps - lp.carryToleranceSteps;
        if (adjudicator.judge(officials, cachedOfficials, rules.refereeOnField(), rec, wronged.ordinal(), evidence,
                carryNoise, r.beforeX, r.beforeY, Double.NaN)) {
            state.appealsGiven++;
            award(rec, freeKick(wronged, r.beforeX, r.beforeY, true), now);
        }
    }

    private void contact(ContactRecord c, long now) {
        if (c.tick != now) {
            return;
        }
        if (c.bCharged) {
            charge(c, c.a, c.b, c.aHitFromBehind, c.aKeeper, c.aFall, now);
        }
        if (c.aCharged && state.inPlay()) {
            charge(c, c.b, c.a, c.bHitFromBehind, c.bKeeper, c.bFall, now);
        }
    }

    /** {@code offender} charged {@code victim}: appeal if the law of the day (or the victim's side) calls it a foul. */
    private void charge(ContactRecord c, int victim, int offender, boolean fromBehind, boolean keeper, int fall,
            long now) {
        int kind;
        double evidence;
        if (fromBehind) {
            kind = LawRecord.FOUL_FROM_BEHIND;
            evidence = 1;
        } else if (keeper && rules.keeperChargeProtected() && !(poss != null && poss.held && poss.holderIndex == victim)) {
            kind = LawRecord.FOUL_KEEPER_CHARGE;
            evidence = 1;
        } else if (fall == ContactRecord.DOWN) {
            kind = LawRecord.FOUL_ROUGH;
            evidence = -1;
        } else {
            return;
        }
        double chance = evidence > 0 ? lp.appealFoul : lp.appealFairChargeDown;
        if (appealRandom.nextDouble() >= chance) {
            return;
        }
        int vs = slotOf(victim);
        if (vs < 0) {
            return;
        }
        Team wronged = body[vs].team;
        LawRecord rec = recordOrSpare(LawRecord.FOUL, now);
        rec.foulKind = kind;
        rec.offender = offender;
        rec.victim = victim;
        rec.x = c.x;
        rec.y = c.y;
        state.appeals++;
        if (!adjudicator.judge(officials, cachedOfficials, rules.refereeOnField(), rec, wronged.ordinal(), evidence,
                foulNoise, c.x, c.y, Double.NaN)) {
            return;
        }
        state.appealsGiven++;
        state.foulsGiven++;
        award(rec, freeKick(wronged, c.x, c.y, true), now);
        if (c.impulseNs >= lp.violentImpulseNs && discipline.nextDouble() < lp.sendOffChance) {
            sendOff(offender, c.x, c.y, now);
        }
    }

    private void sendOff(int offender, double x, double y, long now) {
        int s = slotOf(offender);
        if (s < 0 || offender >= 64) {
            return;
        }
        state.sentOffMask |= 1L << offender;
        state.sendingsOff++;
        injury[s].state = InjuryComponent.OFF; // leaves the field like a man carried off; not replaced (D7)
        injury[s].knockTicksLeft = 0;
        body[s].vx = 0;
        body[s].vy = 0;
        LawRecord rec = record(LawRecord.SENDING_OFF, now);
        if (rec != null) {
            rec.offender = offender;
            rec.x = x;
            rec.y = y;
            rec.given = true;
        }
    }

    /** Free kick to {@code team} at (x, y), a penalty instead from 1891 for a foul in the defenders' penalty zone. */
    private Restart freeKick(Team team, double x, double y, boolean penaltyPossible) {
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        x = Math.max(-hl, Math.min(hl, x));
        y = Math.max(-hw, Math.min(hw, y));
        if (penaltyPossible && rules.penalties()) {
            PitchEnd end = ends.endDefendedBy(team.other());
            if (pitch.inPenaltyZone(end, x, y)) {
                double px = end.sign() * (hl - 12 * PitchParams.YARD);
                return new Restart(RestartType.PENALTY_KICK, team, new Vec3(px, 0, 0),
                        rules.offsideFrom(RestartType.PENALTY_KICK), true, false);
            }
        }
        return new Restart(RestartType.FREE_KICK, team, new Vec3(x, y, 0), rules.offsideFrom(RestartType.FREE_KICK),
                rules.restartLaws().goalDirectFrom(RestartType.FREE_KICK), false);
    }

    /** An upheld appeal: the ball is dead for {@code restart}. */
    private void award(LawRecord rec, Restart restart, long now) {
        stop(restart, now + delayTicks(lp.restartDelaySeconds));
        rec.restartType = restart.type().ordinal();
        rec.restartTeam = restart.team().ordinal();
        rec.restartX = restart.spot().x();
        rec.restartY = restart.spot().y();
    }

    // ---- out of play, goals -----------------------------------------------------------------------------------------

    private void outOfPlay(long now) {
        double r = pp.ballRadiusM;
        if (Math.abs(ball.x) <= pitch.halfLength() + r && Math.abs(ball.y) <= pitch.halfWidth() + r) {
            return;
        }
        LineCrossing crossing = crossing(r);
        Team lastTouch = poss != null && poss.lastTouchTeam >= 0 ? TEAMS[poss.lastTouchTeam]
                : (crossing.line() == LineCrossing.Line.GOAL_LINE ? ends.attackerOf(crossing.end()) : Team.HOME);
        RestartType directFrom = state.directFrom >= 0 ? RESTARTS[state.directFrom] : null;
        LawDecision d = decider.decide(crossing, lastTouch, directFrom, ends);
        LawDecision fin = d;
        LawRecord rec;
        if (d.disputed()) {
            Team attackers = ends.attackerOf(crossing.end());
            Team claimant = d.goal() ? attackers.other() : attackers;
            double inside = crossing.betweenPosts() ? crossing.frameGap() : -crossing.frameGap();
            double evidence = claimant == attackers ? inside : -inside;
            rec = recordOrSpare(LawRecord.DISPUTED_GOAL, now);
            rec.x = crossing.ballCentre().x();
            rec.y = crossing.planeY();
            state.appeals++;
            state.disputedGoals++;
            Vec3 c = crossing.ballCentre();
            if (adjudicator.judge(officials, cachedOfficials, rules.refereeOnField(), rec, claimant.ordinal(),
                    evidence, goalNoise, c.x(), c.y(), Double.NaN)) {
                state.appealsGiven++;
                fin = d.ifReversed();
            }
        } else {
            rec = recordOrSpare(fin.goal() ? LawRecord.GOAL : LawRecord.OUT_OF_PLAY, now);
            rec.x = crossing.ballCentre().x();
            rec.y = crossing.ballCentre().y();
        }
        if (fin.goal()) {
            if (fin.scoringTeam() == Team.HOME) {
                state.homeGoals++;
            } else {
                state.awayGoals++;
            }
            rec.scoringTeam = fin.scoringTeam().ordinal();
        }
        Restart restart = fin.restart();
        double delay = restart.type() == RestartType.KICK_OFF ? lp.kickOffDelaySeconds : lp.restartDelaySeconds;
        stop(restart, now + delayTicks(delay));
        rec.restartType = restart.type().ordinal();
        rec.restartTeam = restart.team().ordinal();
        rec.restartX = restart.spot().x();
        rec.restartY = restart.spot().y();
    }

    /**
     * The line crossing of this tick's ball path (ball centres); synthesized if the swept test cannot give one. The
     * path starts at last tick's ball, or where the ball last bounced off the frame this tick if that was in play.
     */
    private LineCrossing crossing(double r) {
        double x0 = state.prevBallX;
        double y0 = state.prevBallY;
        double z0 = state.prevBallZ;
        if (ballSystem != null && ballSystem.frameContactThisTick()
                && Math.abs(ballSystem.contactX()) <= pitch.halfLength() + r
                && Math.abs(ballSystem.contactY()) <= pitch.halfWidth() + r) {
            x0 = ballSystem.contactX();
            y0 = ballSystem.contactY();
            z0 = ballSystem.contactZ();
        }
        SweepResult s = pitch.sweep(BallSegment.of(x0, y0, z0 + r, ball.x, ball.y, ball.z + r, r));
        if (s.hasCrossing()) {
            return s.crossing();
        }
        // The path started out of play (over the line, then back off the frame): judge from the positions.
        double hl = pitch.halfLength();
        Vec3 at = new Vec3(ball.x, ball.y, ball.z + r);
        if (Math.abs(ball.x) > hl + r) {
            PitchEnd end = PitchEnd.ofX(ball.x);
            double gx = pitch.goalLineX(end);
            double dx = ball.x - x0;
            double f = Math.abs(dx) < 1e-12 ? 1 : Math.max(0, Math.min(1, (gx - x0) / dx));
            double py = y0 + (ball.y - y0) * f;
            double pz = z0 + r + (ball.z - z0) * f;
            boolean between = Math.abs(py) <= pitch.goalMouthHalfWidth() - r && pz <= pitch.crossbarUndersideZ() - r;
            return new LineCrossing(LineCrossing.Line.GOAL_LINE, end, Flank.ofY(py), 1, at, py, pz, between,
                    between ? 0 : r, false);
        }
        return new LineCrossing(LineCrossing.Line.TOUCHLINE, PitchEnd.ofX(ball.x), Flank.ofY(ball.y), 1, at, 0, 0,
                false, 0, false);
    }

    // ---- clock -----------------------------------------------------------------------------------------------------

    private void clock(long now) {
        long half = state.halfTicks;
        if (half <= 0) {
            return;
        }
        if (state.half == 1 && now + 1 >= half) {
            halfTime(now);
        } else if (state.half == 2 && now + 1 >= 2 * half) {
            state.phase = PlayStateComponent.FULL_TIME;
            clearSnapshot();
            if (poss != null && poss.held) {
                poss.release();
            }
            pinBall(ball.x, ball.y);
            LawRecord rec = record(LawRecord.FULL_TIME, now);
            if (rec != null) {
                rec.x = ball.x;
                rec.y = ball.y;
            }
        }
    }

    private void halfTime(long now) {
        state.half = 2;
        RestartDecider.HalfTime ht = decider.halfTime(ends, TEAMS[state.kickedOffFirst]);
        ends = ht.ends();
        state.westDefender = ends.westDefender().ordinal();
        if (actions != null) {
            actions.setEnds(ends);
        }
        for (int i = 0; i < cached; i++) {
            BodyComponent b = body[i];
            b.x = -b.x;
            b.y = -b.y;
            b.prevX = -b.prevX;
            b.prevY = -b.prevY;
            b.vx = 0;
            b.vy = 0;
            b.fx = -b.fx;
            b.fy = -b.fy;
            b.facing = b.facing >= 0 ? b.facing - StrictMath.PI : b.facing + StrictMath.PI;
            athlete[i].halfTime(pp.body);
            move[i].stop();
            if (command[i] != null) {
                command[i].cancel();
            }
        }
        for (int i = 0; i < cachedOfficials; i++) {
            officials[i].x = -officials[i].x;
            officials[i].vx = 0;
            officials[i].vy = 0;
        }
        stop(ht.kickOff(), now + 1 + delayTicks(lp.kickOffDelaySeconds));
        LawRecord rec = record(LawRecord.HALF_TIME, now);
        if (rec != null) {
            rec.restartType = state.restartType;
            rec.restartTeam = state.restartTeam;
        }
    }

    // ---- caches, records --------------------------------------------------------------------------------------------

    private void cache() {
        int n = players.size();
        body = new BodyComponent[n];
        athlete = new AthleteComponent[n];
        injury = new InjuryComponent[n];
        move = new MoveCommand[n];
        command = new ActionCommand[n];
        index = new int[n];
        for (int i = 0; i < n; i++) {
            Entity e = players.get(i);
            body[i] = bodyMap.get(e);
            athlete[i] = athleteMap.get(e);
            injury[i] = injuryMap.get(e);
            move[i] = moveMap.get(e);
            command[i] = commandMap.get(e);
            IdentityComponent id = idMap.get(e);
            index[i] = id == null ? i : id.index;
        }
        cached = n;
    }

    private void cacheOfficials() {
        int n = officialEntities.size();
        officials = new OfficialComponent[n];
        for (int i = 0; i < n; i++) {
            officials[i] = officialMap.get(officialEntities.get(i));
        }
        cachedOfficials = n;
    }

    private int slotOf(int creationIndex) {
        for (int i = 0; i < cached; i++) {
            if (index[i] == creationIndex) {
                return i;
            }
        }
        return -1;
    }

    /** Scratch record when the tick's records are full (decisions still apply, the record is lost). */
    private final LawRecord spare = new LawRecord();

    private LawRecord record(int type, long now) {
        if (recordCount == records.length) {
            dropped++;
            return null;
        }
        LawRecord r = records[recordCount++];
        r.clear();
        r.type = type;
        r.tick = now;
        return r;
    }

    private LawRecord recordOrSpare(int type, long now) {
        LawRecord r = record(type, now);
        if (r == null) {
            spare.clear();
            spare.type = type;
            spare.tick = now;
            return spare;
        }
        return r;
    }

    // ---- API ----------------------------------------------------------------------------------------------------------

    /** Decisions of the current tick (reused next tick). */
    public int recordCount() {
        return recordCount;
    }

    public LawRecord record(int k) {
        if (k < 0 || k >= recordCount) {
            throw new IndexOutOfBoundsException(k);
        }
        return records[k];
    }

    /** Records beyond {@link #MAX_RECORDS_PER_TICK} so far (not kept). */
    public long droppedRecords() {
        return dropped;
    }

    /** The restart awaited at a dead ball (or the last one), null before the first tick under the laws. */
    public Restart currentRestart() {
        return current;
    }

    /** The ends of the current half. */
    public MatchEnds ends() {
        return ends;
    }

    public RulesProfile rules() {
        return rules;
    }

    /**
     * Makes the ball dead for {@code restart} now (tests, tools, and later scripted incidents); takeable after the
     * usual delay. Only while the laws are active.
     */
    public void award(Restart restart) {
        if (state == null) {
            throw new IllegalStateException("no PlayStateComponent on the ball yet (tick once after attaching)");
        }
        stop(restart, world.tickCount() + delayTicks(lp.restartDelaySeconds));
    }
}
