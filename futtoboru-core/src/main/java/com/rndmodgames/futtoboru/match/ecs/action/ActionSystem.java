package com.rndmodgames.futtoboru.match.ecs.action;

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
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams.SurfaceParams;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallPhysics;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyParams;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Ball actions (E-14, docs/design/MATCH_ENGINE_ECS.md §4.1): turns each player's {@link ActionCommand} into a physical
 * impulse on the ball, once per tick after locomotion and before the ball flies.
 *
 * <ul>
 * <li><b>Reach:</b> a player plays the ball only when it is within his reach for the action: feet up to
 * {@link ActionParams#footMaxZ}, head in the head band (plus a jump by heading skill), keeper hands up to
 * {@link ActionParams#handMaxZ} (plus a jump by handling). A stumbling player reaches less; a player on the ground or
 * off the field not at all. An action stays armed for {@link ActionParams#armTicks} waiting for the ball. When
 * several players can play the ball in one tick, the nearest plays it (ties: creation order); the rest are
 * {@link ActionCommand#RESULT_CONTESTED}.</li>
 * <li><b>Kicks:</b> the {@link LaunchSolver} gives the launch velocity the kicker aims for, planned with the roll drag
 * he expects ({@link ActionParams#groundReading}) and the ball mass he felt at kick-off. The needed velocity change
 * becomes a foot speed through the BKP §1.5 impulse model with the reference foot mass, capped by his kicking rating;
 * then Gaussian noise on direction, elevation, foot speed and spin, scaled by skill, fatigue, pressure, ball
 * saturation, awkward angle and stumbling. The impulse that reaches the ball uses the real boot
 * ({@link KitComponent#kickEffectiveMassKg}: heavy wet boots kick harder) and the real ball mass (a soaked ball leaves
 * slower). Noise is on these inputs only: no dice decide the result (§4.1).</li>
 * <li><b>Touches:</b> a dribble touch pushes the ball ahead toward the target; a ball arriving faster than the
 * player can cushion (control by technique and dribbling, worse with a wet ball) keeps part of its pace.</li>
 * <li><b>Headers:</b> only in head range; the outgoing speed is capped at neck drive + restitution x incoming speed.
 * Each header draws on the {@code action:lace} fork: a lace strike ({@link ActionParams#laceStrikeShare}) and, rarely,
 * a cut (x {@link ActionParams#laceCutWetFactor} with a saturated ball) that gives the player a knock.</li>
 * <li><b>Keeper:</b> catch, punch and keeper's throw only where the season's law allows hands (own half in 1888-89,
 * own penalty area from 1912); elsewhere the command is refused ({@link ActionCommand#RESULT_ILLEGAL_HANDLING}). A
 * ball faster than he can hold is parried. While he holds it, the ball sits in his hands
 * ({@link PossessionComponent#held}; the ball system leaves it alone); beyond {@link ActionParams#maxCarrySteps} steps
 * (1883 law) he must let it go and the ball drops. He releases it with a punt (any kick), a throw or a bounce
 * (dribble touch). Knocked down while holding: the ball drops loose (charging the keeper is fair in 1888-89).</li>
 * <li><b>Records:</b> every action of the tick goes to a reused {@link ActionRecord}; {@link PossessionComponent}
 * keeps who touched the ball last.</li>
 * </ul>
 * Determinism (§2.1): doubles, no trig (directions are rotated with the rational half-angle form; on JDK 17
 * StrictMath.sin/cos allocate), noise from the {@code action:noise} fork with a fixed four draws per action, no
 * allocation per tick once the player set is fixed.
 */
public final class ActionSystem extends EntitySystem implements SimSystem {

    /** Actions kept per tick; more are counted in {@link #droppedRecords()} but not recorded. */
    public static final int MAX_RECORDS_PER_TICK = 32;

    private static final int NOT_IN_REACH = -1;
    private static final int ILLEGAL = -2;

    private final ComponentMapper<BodyComponent> bodyMap = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<AthleteComponent> athleteMap = ComponentMapper.getFor(AthleteComponent.class);
    private final ComponentMapper<KitComponent> kitMap = ComponentMapper.getFor(KitComponent.class);
    private final ComponentMapper<InjuryComponent> injuryMap = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<ActionCommand> commandMap = ComponentMapper.getFor(ActionCommand.class);
    private final ComponentMapper<SkillComponent> skillMap = ComponentMapper.getFor(SkillComponent.class);
    private final ComponentMapper<IdentityComponent> idMap = ComponentMapper.getFor(IdentityComponent.class);
    private final ComponentMapper<BallComponent> ballMap = ComponentMapper.getFor(BallComponent.class);
    private final ComponentMapper<PossessionComponent> possessionMap = ComponentMapper.getFor(PossessionComponent.class);

    private final MatchWorld world;
    private final PhysicsParams pp;
    private final ActionParams a;
    private final BodyParams bp;
    private final BallPhysics physics;
    private final LaunchSolver solver;
    private final RandomSource noise;
    private final RandomSource lace;
    private MatchEnds ends = new MatchEnds(Team.HOME);
    private boolean homeGround;
    private PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());

    private final ActionRecord[] records = new ActionRecord[MAX_RECORDS_PER_TICK];
    private int recordCount;
    private long dropped;
    private long totalActions;
    private long laceStrikes;
    private long laceCuts;

    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> balls;
    private int cached = -1;
    private BodyComponent[] body = new BodyComponent[0];
    private AthleteComponent[] athlete = new AthleteComponent[0];
    private KitComponent[] kit = new KitComponent[0];
    private InjuryComponent[] injury = new InjuryComponent[0];
    private ActionCommand[] command = new ActionCommand[0];
    private SkillComponent[] skill = new SkillComponent[0];
    private int[] index = new int[0];
    private int[] cand = new int[0];
    private double[] candDist = new double[0];
    private boolean[] executed = new boolean[0];

    // scratch results (no allocation)
    private double dvx, dvy, dvz;
    private double lastNoiseFactor;

    public ActionSystem(MatchWorld world) {
        super(SystemPriority.ACTION);
        this.world = world;
        this.pp = world.params();
        this.a = pp.action;
        this.bp = pp.body;
        this.physics = new BallPhysics(pp);
        this.solver = new LaunchSolver(pp, world.tickSeconds() / pp.ballSubsteps);
        this.noise = world.random().fork("action:noise");
        this.lace = world.random().fork("action:lace");
        for (int i = 0; i < records.length; i++) {
            records[i] = new ActionRecord();
        }
    }

    /** Players that can act: body, athlete and an action command. */
    public static Family family() {
        return Family.all(BodyComponent.class, AthleteComponent.class, ActionCommand.class).get();
    }

    @Override
    public void addedToEngine(Engine engine) {
        players = engine.getEntitiesFor(family());
        balls = engine.getEntitiesFor(Family.all(BallComponent.class).get());
    }

    /**
     * Played on the home club's ground (E-20): the away side's input noise grows by
     * {@link ActionParams#awayNoiseGain}. Default false (a neutral ground, as in drills and scenarios).
     */
    public void setHomeGround(boolean homeGround) {
        this.homeGround = homeGround;
    }

    /** Which side defends which goal (keeper's own half); change at half-time. Default: home defends west. */
    public void setEnds(MatchEnds ends) {
        if (ends == null) {
            throw new IllegalArgumentException("ends");
        }
        this.ends = ends;
    }

    /** The ground (goal lines for the 1912 penalty-area law). Default: 110 x 70 yd. */
    public void setPitch(PitchGeometry pitch) {
        if (pitch == null) {
            throw new IllegalArgumentException("pitch");
        }
        this.pitch = pitch;
    }

    @Override
    public void update(float ignoredFrameDelta) {
        recordCount = 0;
        if (players.size() != cached) {
            cache();
        }
        if (balls.size() == 0) {
            expireAll();
            return;
        }
        Entity ballEntity = balls.get(0);
        BallComponent ball = ballMap.get(ballEntity);
        PossessionComponent poss = possessionMap.get(ballEntity);
        if (poss == null) {
            poss = new PossessionComponent(); // once per match
            ballEntity.add(poss);
        }
        long tick = world.tickCount();
        int n = cached;

        // 1. the keeper's hands: drop the ball if he went down, else carry it
        int holder = poss.held ? slotOf(poss.holderIndex) : -1;
        if (poss.held && (holder < 0 || body[holder].isDown() || injury[holder] != null && injury[holder].isOff())) {
            double steps = poss.carrySteps(a.stepLengthM);
            beginRecord(holder, ActionIntent.NONE, ball, tick);
            poss.release();
            if (holder >= 0) {
                ball.vx = body[holder].vx;
                ball.vy = body[holder].vy;
            }
            ball.vz = 0;
            ball.state = BallComponent.AIR;
            endRecord(ActionRecord.DROPPED_KNOCKED_DOWN, ball, 0, 0, true, steps);
            holder = -1;
        } else if (poss.held) {
            BodyComponent hb = body[holder];
            double mx = hb.x - poss.holderLastX;
            double my = hb.y - poss.holderLastY;
            poss.carriedM += Math.sqrt(mx * mx + my * my);
            poss.holderLastX = hb.x;
            poss.holderLastY = hb.y;
            pin(ball, hb);
        }

        // 2. who can play the ball now
        int c = 0;
        for (int i = 0; i < n; i++) {
            executed[i] = false;
            ActionCommand cmd = command[i];
            if (!cmd.armed()) {
                continue;
            }
            double d = reach(i, cmd.intent, ball, poss);
            if (d == ILLEGAL) {
                finish(cmd, ActionCommand.RESULT_ILLEGAL_HANDLING, tick);
                executed[i] = true;
                continue;
            }
            if (d == NOT_IN_REACH) {
                continue;
            }
            int at = c++;
            while (at > 0 && candDist[at - 1] > d) {
                cand[at] = cand[at - 1];
                candDist[at] = candDist[at - 1];
                at--;
            }
            cand[at] = i;
            candDist[at] = d;
        }

        // 3. the nearest plays it
        boolean played = false;
        for (int k = 0; k < c; k++) {
            int i = cand[k];
            executed[i] = true;
            if (played) {
                finish(command[i], ActionCommand.RESULT_CONTESTED, tick);
                continue;
            }
            execute(i, ball, poss, tick);
            finish(command[i], ActionCommand.RESULT_DONE, tick);
            played = true;
        }

        // 4. carrying limit (1883): still holding beyond the steps allowed -> the ball must go
        if (poss.held) {
            holder = slotOf(poss.holderIndex);
            double steps = poss.carrySteps(a.stepLengthM);
            if (steps > a.maxCarrySteps) {
                beginRecord(holder, ActionIntent.NONE, ball, tick);
                poss.release();
                poss.carryLimitReleases++;
                ball.vz = 0;
                ball.state = BallComponent.AIR;
                endRecord(ActionRecord.RELEASED_CARRY_LIMIT, ball, 0, 0, true, steps);
            }
        }

        // 5. armed commands that did not act wait one tick less
        for (int i = 0; i < n; i++) {
            ActionCommand cmd = command[i];
            if (!executed[i] && cmd.armed() && --cmd.armTicksLeft <= 0) {
                finish(cmd, ActionCommand.RESULT_EXPIRED, tick);
            }
        }
    }

    private void expireAll() {
        if (players.size() != cached) {
            cache();
        }
        for (int i = 0; i < cached; i++) {
            ActionCommand cmd = command[i];
            if (cmd.armed() && --cmd.armTicksLeft <= 0) {
                finish(cmd, ActionCommand.RESULT_EXPIRED, world.tickCount());
            }
        }
    }

    private void cache() {
        int n = players.size();
        body = new BodyComponent[n];
        athlete = new AthleteComponent[n];
        kit = new KitComponent[n];
        injury = new InjuryComponent[n];
        command = new ActionCommand[n];
        skill = new SkillComponent[n];
        index = new int[n];
        cand = new int[n];
        candDist = new double[n];
        executed = new boolean[n];
        for (int i = 0; i < n; i++) {
            Entity e = players.get(i);
            body[i] = bodyMap.get(e);
            athlete[i] = athleteMap.get(e);
            kit[i] = kitMap.get(e);
            injury[i] = injuryMap.get(e);
            command[i] = commandMap.get(e);
            SkillComponent s = skillMap.get(e);
            skill[i] = s != null ? s : SkillComponent.AVERAGE;
            IdentityComponent id = idMap.get(e);
            index[i] = id == null ? i : id.index;
        }
        cached = n;
    }

    private int slotOf(int creationIndex) {
        for (int i = 0; i < cached; i++) {
            if (index[i] == creationIndex) {
                return i;
            }
        }
        return -1;
    }

    private static void finish(ActionCommand cmd, int result, long tick) {
        cmd.intent = ActionIntent.NONE;
        cmd.armTicksLeft = 0;
        cmd.result = result;
        cmd.resultTick = tick;
    }

    private void pin(BallComponent ball, BodyComponent hb) {
        double out = hb.radius * 0.5;
        ball.x = hb.x + hb.fx * out;
        ball.y = hb.y + hb.fy * out;
        ball.z = a.holdZ;
        ball.vx = hb.vx;
        ball.vy = hb.vy;
        ball.vz = 0;
        ball.spin = 0;
        ball.state = BallComponent.AIR;
    }

    // ---- reach ----------------------------------------------------------------------------------------------------

    /** Horizontal distance to the ball if player {@code i} can do {@code intent} now, else NOT_IN_REACH or ILLEGAL. */
    private double reach(int i, ActionIntent intent, BallComponent ball, PossessionComponent poss) {
        BodyComponent b = body[i];
        if (b.isDown() || injury[i] != null && injury[i].isOff()) {
            return NOT_IN_REACH;
        }
        boolean mine = poss.held && poss.holderIndex == index[i];
        double f = b.stumbleTicks > 0 ? a.stumbleReachFactor : 1;
        double dx = ball.x - b.x;
        double dy = ball.y - b.y;
        double d = Math.sqrt(dx * dx + dy * dy);
        switch (intent) {
            case KEEPER_CATCH:
            case KEEPER_PUNCH:
                if (!athlete[i].isKeeper() || !handsAllowed(b.team, ball.x, ball.y)) {
                    return ILLEGAL;
                }
                if (mine) {
                    return intent == ActionIntent.KEEPER_CATCH ? 0 : NOT_IN_REACH;
                }
                if (poss.held) {
                    return NOT_IN_REACH;
                }
                return d <= b.radius + a.handReachM * f
                        && ball.z <= a.handMaxZ + lerp(a.keeperJumpMinM, a.keeperJumpMaxM, skill[i].handling) * f
                                ? d : NOT_IN_REACH;
            case THROW_IN:
                if (mine) {
                    return 0;
                }
                if (poss.held) {
                    return NOT_IN_REACH;
                }
                return d <= b.radius + a.handReachM * f && ball.z <= a.handMaxZ ? d : NOT_IN_REACH;
            case HEADER:
                if (poss.held) {
                    return NOT_IN_REACH;
                }
                double top = a.headMaxZ + lerp(a.jumpMinM, a.jumpMaxM, skill[i].heading) * f;
                return d <= b.radius + a.headReachM * f && ball.z >= a.headMinZ && ball.z <= top ? d : NOT_IN_REACH;
            default: // feet: kicks and touches
                if (mine) {
                    return 0;
                }
                if (poss.held) {
                    return NOT_IN_REACH;
                }
                return d <= b.radius + a.footReachM * f && ball.z <= a.footMaxZ * f ? d : NOT_IN_REACH;
        }
    }

    /** May a keeper of {@code team} use his hands at (x, y) under this season's law? */
    public boolean handsAllowed(Team team, double x, double y) {
        int s = ends.endDefendedBy(team).sign();
        if (pp.rules.keeperHandling() == ActionParams.KeeperHandling.OWN_HALF) {
            return x * s >= 0;
        }
        double depth = pitch.halfLength() - x * s;
        return depth >= 0 && depth <= a.penaltyAreaDepthM && Math.abs(y) <= a.penaltyAreaHalfWidthM;
    }

    // ---- execution -------------------------------------------------------------------------------------------------

    private void execute(int i, BallComponent ball, PossessionComponent poss, long tick) {
        ActionCommand cmd = command[i];
        ActionIntent intent = cmd.intent;
        BodyComponent b = body[i];
        SkillComponent s = skill[i];
        boolean mine = poss.held && poss.holderIndex == index[i];
        double steps = mine ? poss.carrySteps(a.stepLengthM) : 0;
        totalActions++;
        beginRecord(i, intent, ball, tick);
        if (pending != null) {
            pending.targetX = cmd.targetX;
            pending.targetY = cmd.targetY;
            pending.targetZ = cmd.targetZ;
        }
        double rvx = ball.vx - b.vx;
        double rvy = ball.vy - b.vy;
        double incoming = Math.sqrt(rvx * rvx + rvy * rvy + ball.vz * ball.vz);
        double sat = ball.saturation;
        SurfaceParams surface = pp.surface(world.pitch().surfaceAt(ball.x, ball.y));

        if (intent.isKick()) {
            if (mine) {
                poss.release(); // a punt out of his hands
                ball.vx = b.vx;
                ball.vy = b.vy;
                ball.vz = 0;
            }
            double sk = intent == ActionIntent.PASS || intent == ActionIntent.CROSS ? s.technique : s.kicking;
            double factor = noiseFactor(i, ball, cmd.targetX, cmd.targetY, sat);
            double planMass = ball.startMassKg > 0 ? ball.startMassKg : ball.massKg;
            double d = horizontal(ball, cmd.targetX, cmd.targetY);
            double kRef = pp.footEffectiveMassKg * (1 + pp.footKickRestitution) / (pp.footEffectiveMassKg + planMass);
            double footMax = lerp(a.footSpeedMin, a.footSpeedMax, s.kicking);
            if (intent == ActionIntent.PASS && cmd.targetZ <= 0 && ball.z <= 0) {
                double plannedRoll = pp.dry.rollDrag + a.groundReading * (surface.rollDrag - pp.dry.rollDrag);
                solver.ground(ball.x, ball.y, cmd.targetX, cmd.targetY, a.passArrivalMps, plannedRoll);
            } else {
                double t0;
                double tz = cmd.targetZ < 0 ? 0 : cmd.targetZ;
                switch (intent) {
                    case CROSS:
                        t0 = solver.loftTime(d, ball.z, tz, a.crossLoft);
                        break;
                    case CLEAR:
                        t0 = solver.loftTime(d, ball.z, tz, a.clearLoft);
                        break;
                    case SHOT:
                        double v = lerp(a.shotPowerMin, a.shotPowerMax, cmd.power) * footMax * kRef;
                        t0 = d / v;
                        break;
                    default:
                        t0 = solver.loftTime(d, ball.z, tz, a.lobLoft);
                        break;
                }
                solver.air(ball.x, ball.y, ball.z, cmd.targetX, cmd.targetY, tz, t0, cmd.spin, planMass, surface);
            }
            kick(i, ball, sk, factor, kRef, footMax, cmd.spin);
            endRecord(ActionRecord.KICKED, ball, lastFoot, lastImpulse, mine, steps);
        } else if (intent == ActionIntent.DRIBBLE_TOUCH) {
            if (mine) {
                poss.release(); // the keeper's bounce: the ball leaves his hands, the step count starts again
                ball.vx = b.vx;
                ball.vy = b.vy;
                ball.vz = -a.bounceSpeedMps;
                ball.state = BallComponent.AIR;
                endRecord(ActionRecord.BOUNCED, ball, 0, ball.massKg * a.bounceSpeedMps, true, steps);
            } else {
                touch(i, ball, cmd, sat, incoming);
                endRecord(ActionRecord.TOUCHED, ball, 0, lastImpulse, false, 0);
            }
        } else if (intent == ActionIntent.HEADER) {
            double factor = noiseFactor(i, ball, cmd.targetX, cmd.targetY, sat);
            double drive = lerp(a.headDriveMin, a.headDriveMax, s.heading);
            double maxOut = drive + a.headRestitution * incoming;
            double tz = cmd.targetZ < 0 ? 0 : cmd.targetZ;
            double d = horizontal(ball, cmd.targetX, cmd.targetY);
            solver.air(ball.x, ball.y, ball.z, cmd.targetX, cmd.targetY, tz, d / maxOut, 0, ball.massKg, surface);
            capSolved(maxOut);
            redirect(ball, s.heading, factor, 0);
            ActionRecord r = endRecord(ActionRecord.HEADED, ball, 0, lastImpulse, false, 0);
            laceCheck(i, sat, r);
        } else if (intent == ActionIntent.KEEPER_CATCH) {
            if (mine) {
                endRecord(ActionRecord.CAUGHT, ball, 0, 0, true, steps); // already holding
            } else {
                double factor = noiseFactor(i, ball, ball.x - rvx, ball.y - rvy, sat);
                double catchable = lerp(a.catchSpeedMin, a.catchSpeedMax, s.handling) * (1 - a.wetCatchPenalty * sat);
                // the hands meet the ball well or badly: noise on the cushioning, not a dice roll on the catch
                double hands = 1 + noise.nextGaussian() * a.noiseScale * factor
                        * lerp(a.speedSigmaWorst, a.speedSigmaBest, s.handling);
                noise.nextGaussian(); // fixed four draws per action
                noise.nextGaussian();
                noise.nextGaussian();
                if (incoming <= catchable * hands) {
                    poss.hold(index[i], b.x, b.y);
                    double j = ball.massKg * incoming;
                    pin(ball, b);
                    endRecord(ActionRecord.CAUGHT, ball, 0, j, true, 0);
                } else {
                    // parried: the relative velocity comes back off the hands with restitution
                    solver.vx = b.vx - rvx * a.parryRestitution;
                    solver.vy = b.vy - rvy * a.parryRestitution;
                    solver.vz = -ball.vz * a.parryRestitution;
                    redirectNoNoise(ball);
                    endRecord(ActionRecord.PARRIED, ball, 0, lastImpulse, true, 0);
                }
            }
        } else if (intent == ActionIntent.KEEPER_PUNCH) {
            double factor = noiseFactor(i, ball, cmd.targetX, cmd.targetY, sat);
            double maxOut = lerp(a.punchDriveMin, a.punchDriveMax, s.handling) + a.punchRestitution * incoming;
            double tz = cmd.targetZ < 0 ? 0 : cmd.targetZ;
            double d = horizontal(ball, cmd.targetX, cmd.targetY);
            solver.air(ball.x, ball.y, ball.z, cmd.targetX, cmd.targetY, tz,
                    solver.loftTime(d, ball.z, tz, a.lobLoft), 0, ball.massKg, surface);
            capSolved(maxOut);
            redirect(ball, s.handling, factor, 0);
            endRecord(ActionRecord.PUNCHED, ball, 0, lastImpulse, true, 0);
        } else if (intent == ActionIntent.THROW_IN) {
            if (mine) {
                poss.release();
            }
            double out = b.radius * 0.5;
            ball.x = b.x + b.fx * out;
            ball.y = b.y + b.fy * out;
            ball.z = a.throwReleaseZ;
            ball.vx = b.vx;
            ball.vy = b.vy;
            ball.vz = 0;
            ball.spin = 0;
            ball.state = BallComponent.AIR;
            double factor = noiseFactor(i, ball, cmd.targetX, cmd.targetY, sat);
            double maxOut = lerp(a.throwSpeedMin, a.throwSpeedMax, s.technique);
            double tz = cmd.targetZ < 0 ? 0 : cmd.targetZ;
            double d = horizontal(ball, cmd.targetX, cmd.targetY);
            solver.air(ball.x, ball.y, ball.z, cmd.targetX, cmd.targetY, tz,
                    solver.loftTime(d, ball.z, tz, a.lobLoft), 0, ball.massKg, surface);
            capSolved(maxOut);
            redirect(ball, s.technique, factor, 0);
            endRecord(ActionRecord.THROWN, ball, 0, lastImpulse, mine, steps);
        }
        poss.touch(index[i], b.team.ordinal(), intent, tick);
    }

    private double lastFoot;
    private double lastImpulse;

    /** Foot kick toward the solved launch velocity (BKP §1.5), noise on the inputs. */
    private void kick(int i, BallComponent ball, double sk, double factor, double kRef, double footMax,
            double spin) {
        dvx = solver.vx - ball.vx;
        dvy = solver.vy - ball.vy;
        dvz = solver.vz - ball.vz;
        double dv = Math.sqrt(dvx * dvx + dvy * dvy + dvz * dvz);
        double foot = dv / kRef;
        if (foot > footMax) {
            foot = footMax; // cannot swing harder: the ball falls short
        }
        double gDir = noise.nextGaussian();
        double gElev = noise.nextGaussian();
        double gSpeed = noise.nextGaussian();
        double gSpin = noise.nextGaussian();
        double scale = a.noiseScale * factor;
        rotate(gDir * scale * lerp(a.dirSigmaWorst, a.dirSigmaBest, sk),
                gElev * scale * lerp(a.elevSigmaWorst, a.elevSigmaBest, sk));
        foot *= 1 + gSpeed * scale * lerp(a.speedSigmaWorst, a.speedSigmaBest, sk);
        if (foot < 0) {
            foot = 0;
        }
        if (foot > footMax * 1.1) {
            foot = footMax * 1.1;
        }
        double len = Math.sqrt(dvx * dvx + dvy * dvy + dvz * dvz);
        double mEff = kit[i] != null ? kit[i].kickEffectiveMassKg(pp.footEffectiveMassKg, bp) : pp.footEffectiveMassKg;
        double j = physics.footImpulse(foot, ball.massKg, mEff);
        if (len > 0) {
            physics.applyImpulse(ball, j * dvx / len, j * dvy / len, j * dvz / len);
        }
        ball.spin = spin + gSpin * scale * lerp(a.spinSigmaWorst, a.spinSigmaBest, sk);
        lastFoot = foot;
        lastImpulse = j;
    }

    /** Header, punch, throw: velocity change to the (capped) solved velocity, with noise. */
    private void redirect(BallComponent ball, double sk, double factor, double spin) {
        dvx = solver.vx - ball.vx;
        dvy = solver.vy - ball.vy;
        dvz = solver.vz - ball.vz;
        double gDir = noise.nextGaussian();
        double gElev = noise.nextGaussian();
        double gSpeed = noise.nextGaussian();
        double gSpin = noise.nextGaussian();
        double scale = a.noiseScale * factor;
        rotate(gDir * scale * lerp(a.dirSigmaWorst, a.dirSigmaBest, sk),
                gElev * scale * lerp(a.elevSigmaWorst, a.elevSigmaBest, sk));
        double k = 1 + gSpeed * scale * lerp(a.speedSigmaWorst, a.speedSigmaBest, sk);
        if (k < 0) {
            k = 0;
        }
        dvx *= k;
        dvy *= k;
        dvz *= k;
        applyDelta(ball);
        ball.spin = spin + gSpin * scale * lerp(a.spinSigmaWorst, a.spinSigmaBest, sk);
    }

    /** Velocity change to the solved velocity without noise (parry: the noise was in the hands). */
    private void redirectNoNoise(BallComponent ball) {
        dvx = solver.vx - ball.vx;
        dvy = solver.vy - ball.vy;
        dvz = solver.vz - ball.vz;
        applyDelta(ball);
        ball.spin = 0;
    }

    private void applyDelta(BallComponent ball) {
        double m = ball.massKg;
        lastImpulse = m * Math.sqrt(dvx * dvx + dvy * dvy + dvz * dvz);
        physics.applyImpulse(ball, m * dvx, m * dvy, m * dvz);
    }

    /** Dribble touch or first touch: push the ball ahead toward the target; fast balls are only partly cushioned. */
    private void touch(int i, BallComponent ball, ActionCommand cmd, double sat, double incoming) {
        BodyComponent b = body[i];
        SkillComponent s = skill[i];
        double tx = cmd.targetX - ball.x;
        double ty = cmd.targetY - ball.y;
        double d = Math.sqrt(tx * tx + ty * ty);
        double ux = d > 1e-9 ? tx / d : b.fx;
        double uy = d > 1e-9 ? ty / d : b.fy;
        double along = b.vx * ux + b.vy * uy;
        double speed = (along > 0 ? along : 0) + lerp(a.dribbleTouchMin, a.dribbleTouchMax, cmd.power);
        double control = 0.5 * (s.technique + s.dribbling);
        double controllable = lerp(a.controlSpeedMin, a.controlSpeedMax, control) * (1 - a.wetControlPenalty * sat);
        double share = incoming <= controllable ? 1 : controllable / incoming;
        solver.vx = ball.vx + (ux * speed - ball.vx) * share;
        solver.vy = ball.vy + (uy * speed - ball.vy) * share;
        solver.vz = ball.vz + (0 - ball.vz) * share;
        double factor = noiseFactor(i, ball, cmd.targetX, cmd.targetY, sat);
        redirect(ball, control, factor, 0);
    }

    /** Scales the solved velocity down to {@code maxOut}. */
    private void capSolved(double maxOut) {
        double v = Math.sqrt(solver.vx * solver.vx + solver.vy * solver.vy + solver.vz * solver.vz);
        if (v > maxOut && v > 0) {
            double k = maxOut / v;
            solver.vx *= k;
            solver.vy *= k;
            solver.vz *= k;
        }
    }

    /**
     * Rotates (dvx, dvy, dvz) by about {@code yaw} around the vertical and {@code pitchAngle} in the vertical plane.
     * Rational half-angle form (t = angle / 2): exact rotations, no trig; the angle is 2 atan(t) ~ the input.
     */
    private void rotate(double yaw, double pitchAngle) {
        double t = 0.5 * yaw;
        double c = (1 - t * t) / (1 + t * t);
        double s = 2 * t / (1 + t * t);
        double x = c * dvx - s * dvy;
        double y = s * dvx + c * dvy;
        dvx = x;
        dvy = y;
        double h = Math.sqrt(dvx * dvx + dvy * dvy);
        if (h > 1e-12) {
            t = 0.5 * pitchAngle;
            c = (1 - t * t) / (1 + t * t);
            s = 2 * t / (1 + t * t);
            double h2 = c * h - s * dvz;
            double z2 = s * h + c * dvz;
            double k = h2 / h;
            dvx *= k;
            dvy *= k;
            dvz = z2;
        }
    }

    /** fatigue x pressure x wet ball x awkward angle x stumble multiplier for player {@code i} aiming at (tx, ty). */
    private double noiseFactor(int i, BallComponent ball, double tx, double ty, double sat) {
        BodyComponent b = body[i];
        AthleteComponent at = athlete[i];
        double fatigue = 1 - 0.5 * (at.stamina + at.reserve);
        double min2 = Double.MAX_VALUE;
        for (int j = 0; j < cached; j++) {
            BodyComponent o = body[j];
            if (o.team == b.team || injury[j] != null && injury[j].isOff()) {
                continue;
            }
            double dx = o.x - b.x;
            double dy = o.y - b.y;
            double d2 = dx * dx + dy * dy;
            if (d2 < min2) {
                min2 = d2;
            }
        }
        double pressure = min2 == Double.MAX_VALUE ? 0 : 1 - Math.sqrt(min2) / a.pressureRadiusM;
        if (pressure < 0) {
            pressure = 0;
        }
        double dx = tx - ball.x;
        double dy = ty - ball.y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double awkward = d > 1e-9 ? 0.5 * (1 - (b.fx * dx + b.fy * dy) / d) : 0;
        double f = (1 + a.fatigueGain * fatigue) * (1 + a.pressureGain * pressure) * (1 + a.wetBallGain * sat)
                * (1 + a.awkwardGain * awkward) * (b.stumbleTicks > 0 ? 1 + a.stumbleGain : 1)
                * (homeGround && b.team == Team.AWAY ? 1 + a.awayNoiseGain : 1);
        lastNoiseFactor = f;
        return f;
    }

    /** Lace strike and cut on a header (BKP §1.5; rare, anecdotal): draws on the {@code action:lace} fork. */
    private void laceCheck(int i, double sat, ActionRecord r) {
        if (!a.laced) {
            return;
        }
        boolean strike = lace.nextDouble() < a.laceStrikeShare;
        if (!strike) {
            return;
        }
        laceStrikes++;
        boolean cut = lace.nextDouble() < laceCutChance(sat);
        if (r != null) {
            r.laceStrike = true;
            r.laceCut = cut;
        }
        if (cut) {
            laceCuts++;
            if (injury[i] != null) {
                injury[i].injure(InjuryComponent.KNOCK, lastImpulse, bp);
            }
        }
    }

    /** Chance that a lace strike cuts, for a ball at {@code saturation} 0..1. */
    public double laceCutChance(double saturation) {
        double p = a.laceCutChance * (1 + (a.laceCutWetFactor - 1) * saturation);
        return p > 1 ? 1 : p;
    }

    private static double horizontal(BallComponent ball, double tx, double ty) {
        double dx = tx - ball.x;
        double dy = ty - ball.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double lerp(double lo, double hi, double t) {
        return lo + (hi - lo) * t;
    }

    // ---- records ---------------------------------------------------------------------------------------------------

    private ActionRecord pending;

    private void beginRecord(int i, ActionIntent intent, BallComponent ball, long tick) {
        lastNoiseFactor = 1;
        lastImpulse = 0;
        lastFoot = 0;
        if (recordCount == records.length) {
            dropped++;
            pending = null;
            return;
        }
        ActionRecord r = records[recordCount++];
        r.clear();
        r.tick = tick;
        r.actor = i >= 0 ? index[i] : -1;
        r.team = i >= 0 ? body[i].team.ordinal() : -1;
        r.intent = intent;
        r.beforeX = ball.x;
        r.beforeY = ball.y;
        r.beforeZ = ball.z;
        r.beforeVx = ball.vx;
        r.beforeVy = ball.vy;
        r.beforeVz = ball.vz;
        if (i >= 0) {
            double rvx = ball.vx - body[i].vx;
            double rvy = ball.vy - body[i].vy;
            r.incomingSpeed = Math.sqrt(rvx * rvx + rvy * rvy + ball.vz * ball.vz);
        }
        pending = r;
    }

    private ActionRecord endRecord(int outcome, BallComponent ball, double foot, double impulse, boolean handled,
            double steps) {
        ActionRecord r = pending;
        pending = null;
        if (r == null) {
            return null;
        }
        r.outcome = outcome;
        r.afterX = ball.x;
        r.afterY = ball.y;
        r.afterZ = ball.z;
        r.afterVx = ball.vx;
        r.afterVy = ball.vy;
        r.afterVz = ball.vz;
        r.afterSpin = ball.spin;
        r.footSpeed = foot;
        r.impulseNs = impulse;
        r.noiseFactor = lastNoiseFactor;
        r.handled = handled;
        r.carrySteps = steps;
        return r;
    }

    // ---- results of this tick -------------------------------------------------------------------------------------

    /** Actions recorded in the current tick. */
    public int recordCount() {
        return recordCount;
    }

    /** The k-th action of this tick (reused next tick). */
    public ActionRecord record(int k) {
        if (k < 0 || k >= recordCount) {
            throw new IndexOutOfBoundsException(k);
        }
        return records[k];
    }

    /** Actions beyond {@link #MAX_RECORDS_PER_TICK} so far (not recorded). */
    public long droppedRecords() {
        return dropped;
    }

    /** Ball actions executed so far this match. */
    public long totalActions() {
        return totalActions;
    }

    /** Headers that met the lace / that cut, so far this match. */
    public long laceStrikes() {
        return laceStrikes;
    }

    public long laceCuts() {
        return laceCuts;
    }
}
