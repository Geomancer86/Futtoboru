package com.rndmodgames.futtoboru.match.ecs.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.action.ActionParams;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.action.SkillComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyParams;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;

/**
 * Decisions (E-15, §4.1): each player, at the think rate (10 Hz, staggered by player like perception), turns his
 * beliefs ({@link PerceptionComponent}) into a {@link MoveCommand} and, near the ball, an {@link ActionCommand}.
 *
 * <p><b>Roles.</b> Every player guesses how soon each player reaches the ball (believed ball, constant deceleration;
 * his team-mates' and opponents' believed positions). The quickest of his side goes for it; when the other side has it
 * a second man presses in a dribbling side ({@link AiParams#chasersDribbling}); the rest hold their slot in the shape
 * or support. The keeper has his own logic.
 *
 * <p><b>Shape.</b> A slot's anchor is its kick-off spot ({@link Shape}) shifted along the pitch with the ball (more
 * when attacking), pulled across toward it, spread wide by a combination side when attacking; backs and halves blend
 * the anchor with a goal-side mark on the nearest opponent when defending. Players off the field leave the shape
 * ({@link TeamTacticsComponent#goneOff}); a passenger goes out to the wing and is never sent to chase.
 *
 * <p><b>Support (1888).</b> Dribbling sides back up the dribbler: the next {@link AiParams#backersUp} men run in a pack
 * just behind and to either side of him, for the loose ball when he is charged off it. Combination sides keep their
 * spread and the nearest man offers a short pass at an angle.
 *
 * <p><b>On the ball</b> (utility scores; the highest wins, plus a small whim drawn from the player's
 * {@code ai:decision:<index>} fork): shoot (distance, angle, kicking, pressure), pass to each team-mate along the
 * ground (progress, how open he is, a clear lane, length vs the style's short game) or lofted (no lane needed), cross
 * from wide in the last third, clear from the own third under pressure, or dribble (space ahead, the {@code dribbling}
 * skill, style). Club style weights dribbling and passing. Kicks are armed before the ball is in reach (they wait
 * {@link ActionParams#armTicks}) and kept once armed. A chaser near the man on the ball shoulder-charges him.
 *
 * <p><b>Keeper.</b> Stays on the line between ball and goal centre, a few metres out; moves across to a shot's line and
 * arms a catch (a punch when the ball is high and an opponent close); comes out for a loose ball near his goal when he
 * gets there first. Holding the ball he stands still and releases it ({@link AiParams#keeperHoldTicks}, or at once at
 * {@link AiParams#keeperReleaseSteps}) with a throw to a free back or half, else a punt, well inside the two steps.
 *
 * <p><b>Laws</b> (E-16 {@link PlayStateComponent}, read only). At a dead ball one taker of the restart side (kick-off:
 * the kicker slot; throw-in and free kick: the nearest outfielder; goal kick: the nearest back; corner: the nearest
 * forward) goes to the ball and plays it once the laws say the restart is ready (a throw-in with
 * {@code THROW_IN}); everybody else takes position (kick-off: own half, the other side ten yards off; corner: forwards
 * in the goalmouth) and plays nothing. In play, forwards keep onside of the third-last opponent as they believe him,
 * passes to a man believed offside score low, and a man the laws hold in an offside position leaves the ball to
 * others. Ends follow the laws ({@link PlayStateComponent#attackSign}); at full time everybody stops.
 *
 * <p>Pacing: chasing is a sprint (less when short-term stamina is low); everything else runs at most at a cruising
 * pace below the aerobic level, lower as the match reserve empties. Determinism: doubles, no trig, a fixed number of
 * draws per decision, no allocation per tick.
 */
public final class DecisionSystem extends EntitySystem implements SimSystem {

    private static final double INF = 1e9;

    private final ComponentMapper<BodyComponent> bodyMap = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<AthleteComponent> athleteMap = ComponentMapper.getFor(AthleteComponent.class);
    private final ComponentMapper<InjuryComponent> injuryMap = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<MoveCommand> moveMap = ComponentMapper.getFor(MoveCommand.class);
    private final ComponentMapper<ActionCommand> actMap = ComponentMapper.getFor(ActionCommand.class);
    private final ComponentMapper<SkillComponent> skillMap = ComponentMapper.getFor(SkillComponent.class);
    private final ComponentMapper<PerceptionComponent> perceptionMap = ComponentMapper.getFor(PerceptionComponent.class);
    private final ComponentMapper<IdentityComponent> idMap = ComponentMapper.getFor(IdentityComponent.class);
    private final ComponentMapper<TeamTacticsComponent> tacticsMap = ComponentMapper.getFor(TeamTacticsComponent.class);
    private final ComponentMapper<BallComponent> ballMap = ComponentMapper.getFor(BallComponent.class);
    private final ComponentMapper<PossessionComponent> possMap = ComponentMapper.getFor(PossessionComponent.class);
    private final ComponentMapper<PlayStateComponent> playMap = ComponentMapper.getFor(PlayStateComponent.class);

    private final MatchWorld world;
    private final AiParams p;
    private final ActionParams ap;
    private final BodyParams bp;
    private final PitchGeometry pitch;
    private final int think;
    private final double hl, hw, postY;
    private final double cruiseShare;

    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> teams;
    private ImmutableArray<Entity> balls;
    private int cached = -1;
    private int cachedTeams = -1;
    private BodyComponent[] body = new BodyComponent[0];
    private AthleteComponent[] athlete = new AthleteComponent[0];
    private InjuryComponent[] injury = new InjuryComponent[0];
    private MoveCommand[] move = new MoveCommand[0];
    private ActionCommand[] act = new ActionCommand[0];
    private SkillComponent[] skill = new SkillComponent[0];
    private PerceptionComponent[] perception = new PerceptionComponent[0];
    private RandomSource[] whims = new RandomSource[0];
    private int[] index = new int[0];
    private int[] side = new int[0];
    /** [team][original slot] -> AI player order, -1 none. */
    private final int[][] member = new int[2][11];
    private final TeamTacticsComponent[] tactics = new TeamTacticsComponent[2];

    private BallComponent ball;
    private PossessionComponent poss;
    private PlayStateComponent play;

    // scratch results (no allocation)
    private double ix, iy;
    private double ax, ay;
    private double tOppBest, tMateBest;
    /** Team frame x of the offside line as player i believes it (third-last opponent, or the ball if further). */
    private double onside;
    private int rank;
    private double bestU;
    private ActionIntent bestIntent;
    private double bestX, bestY, bestZ, bestPower;

    public DecisionSystem(MatchWorld world, PitchGeometry pitch) {
        super(SystemPriority.DECISION);
        this.world = world;
        this.p = world.params().ai;
        this.ap = world.params().action;
        this.bp = world.params().body;
        this.pitch = pitch;
        this.think = world.params().thinkEveryTicks;
        this.hl = pitch.halfLength();
        this.hw = pitch.halfWidth();
        this.postY = pitch.postCentreY();
        this.cruiseShare = Math.sqrt(bp.aerobicPowerShare) * 0.95;
    }

    @Override
    public void addedToEngine(Engine engine) {
        players = engine.getEntitiesFor(PerceptionSystem.family());
        teams = engine.getEntitiesFor(Family.all(TeamTacticsComponent.class).get());
        balls = engine.getEntitiesFor(Family.all(BallComponent.class, PlayStateComponent.class).get());
    }

    @Override
    public void update(float ignoredFrameDelta) {
        if (players.size() != cached || teams.size() != cachedTeams) {
            cache();
        }
        if (cached == 0 || balls.size() == 0 || tactics[0] == null || tactics[1] == null) {
            return;
        }
        Entity be = balls.get(0);
        ball = ballMap.get(be);
        poss = possMap.get(be);
        play = playMap.get(be);
        if (poss == null || !play.started || play.restartType() == null) {
            return; // the laws set up the kick-off at the end of the first tick
        }
        long tick = world.tickCount();
        injuries();
        for (int t = 0; t < 2; t++) {
            tactics[t].attackSign = play.attackSign(tactics[t].team);
        }
        if (play.fullTime()) {
            for (int i = 0; i < cached; i++) {
                move[i].stop();
                act[i].cancel();
            }
            return;
        }
        if (play.stopped()) {
            pickTaker();
            cancelNonTakers();
        }
        keeperHolding(tick);
        for (int i = 0; i < cached; i++) {
            if ((tick + i) % think == 0) {
                think(i, tick);
            }
        }
    }

    // ---- per tick ----------------------------------------------------------------------------------------------------

    /** New injuries: off -> leaves the shape; passenger -> to the wing. */
    private void injuries() {
        for (int i = 0; i < cached; i++) {
            InjuryComponent inj = injury[i];
            if (inj == null || inj.state < InjuryComponent.PASSENGER) {
                continue;
            }
            TeamTacticsComponent tc = tactics[side[i]];
            int k = body[i].slot;
            if (inj.isOff()) {
                if ((tc.offMask & (1 << k)) == 0) {
                    tc.goneOff(k, tc.slotOf[k] == 0 ? bestHandler(side[i], i) : -1);
                    move[i].stop();
                    act[i].cancel();
                }
            } else if ((tc.passengerMask & (1 << k)) == 0) {
                tc.passenger(k);
            }
        }
    }

    private int bestHandler(int team, int except) {
        int best = -1;
        double h = -1;
        for (int k = 0; k < 11; k++) {
            int j = member[team][k];
            if (j < 0 || j == except || injury[j] != null && injury[j].isOff()) {
                continue;
            }
            if (skill[j].handling > h) {
                h = skill[j].handling;
                best = k;
            }
        }
        return best;
    }

    private void pickTaker() {
        if (play.restartTeam() == null) {
            return;
        }
        TeamTacticsComponent tc = tactics[play.restartTeam];
        if (tc.takerForRestart == play.readyTick) {
            return;
        }
        int team = tc.team.ordinal();
        int pick = -1;
        if (play.restartType() == RestartType.KICK_OFF) {
            pick = tc.playerIn(tc.shape.kickerSlot());
            if (pick >= 0 && !onField(member[team][pick])) {
                pick = -1;
            }
        }
        if (pick < 0) {
            double best = INF;
            for (int pass = 0; pass < 2 && pick < 0; pass++) {
                for (int k = 0; k < 11; k++) {
                    int j = member[team][k];
                    if (j < 0 || !onField(j)) {
                        continue;
                    }
                    int slot = tc.slotOf[k];
                    Position.Line line = tc.shape.slot(slot).position().getLine();
                    boolean fits;
                    switch (play.restartType()) {
                        case GOAL_KICK:
                            fits = line == Position.Line.BACK || pass == 1;
                            break;
                        case CORNER_KICK:
                        case KICK_OFF:
                            fits = line == Position.Line.FORWARD || pass == 1 && slot != 0;
                            break;
                        default:
                            fits = slot != 0 || pass == 1;
                            break;
                    }
                    if (!fits) {
                        continue;
                    }
                    double d = dist(body[j].x, body[j].y, play.restartX, play.restartY);
                    if (d < best) {
                        best = d;
                        pick = k;
                    }
                }
            }
        }
        tc.restartTaker = pick;
        tc.takerForRestart = play.readyTick;
    }

    private void cancelNonTakers() {
        for (int i = 0; i < cached; i++) {
            if (act[i].armed() && !isTaker(i)) {
                act[i].cancel();
            }
        }
    }

    private boolean isTaker(int i) {
        if (play.restartTeam < 0) {
            return false;
        }
        TeamTacticsComponent tc = tactics[play.restartTeam];
        return side[i] == tc.team.ordinal() && tc.restartTaker == body[i].slot && tc.takerForRestart == play.readyTick;
    }

    /** A keeper holding the ball stands still and lets it go in time (the 1883 two-step law). */
    private void keeperHolding(long tick) {
        if (!poss.held) {
            return;
        }
        int h = byIndex(poss.holderIndex);
        if (h < 0) {
            return;
        }
        move[h].stop();
        if (act[h].armed() && act[h].intent != ActionIntent.KEEPER_CATCH) {
            return;
        }
        double steps = poss.carrySteps(ap.stepLengthM);
        if (tick - poss.lastTouchTick < p.keeperHoldTicks && steps < p.keeperReleaseSteps) {
            return;
        }
        BodyComponent b = body[h];
        PerceptionComponent pc = perception[h];
        int s = tactics[side[h]].attackSign;
        int best = -1;
        double bestScore = 0;
        for (int j = 0; j < cached; j++) {
            if (j == h || side[j] != side[h] || !onField(j)) {
                continue;
            }
            double d = dist(b.x, b.y, pc.x[j], pc.y[j]);
            if (d < 8 || d > 30) {
                continue;
            }
            double open = nearestOpponent(pc, side[h], pc.x[j], pc.y[j]);
            double score = Math.min(1, open / 10) * (1 - d / 40);
            if (open > 7 && score > bestScore) {
                bestScore = score;
                best = j;
            }
        }
        if (best >= 0) {
            act[h].set(ActionIntent.THROW_IN, pc.x[best], pc.y[best], 0, 0.6, ap.armTicks);
        } else {
            double ty = b.y >= 0 ? hw * 0.5 : -hw * 0.5;
            act[h].set(ActionIntent.CLEAR, clampX(b.x + s * 50), ty, 0, 1, ap.armTicks);
        }
    }

    // ---- per think -----------------------------------------------------------------------------------------------------

    private void think(int i, long tick) {
        BodyComponent b = body[i];
        MoveCommand mv = move[i];
        TeamTacticsComponent tc = tactics[side[i]];
        int slot = tc.slotOf[b.slot];
        if (!onField(i) || slot == TeamTacticsComponent.NO_SLOT) {
            mv.stop();
            return;
        }
        PerceptionComponent pc = perception[i];
        if (pc.seenTick < 0) {
            return;
        }
        mv.charge = false;
        onside = onsideLine(i);
        if (poss.held) {
            int holder = byIndex(poss.holderIndex);
            if (holder >= 0 && holder != i) {
                anchor(i, slot, side[holder] == side[i], pc.ballX, pc.ballY);
                go(i, ax, ay, cruise(i));
            }
            return;
        }
        if (!play.inPlay()) {
            deadBall(i, slot, tick);
            return;
        }
        boolean ourBall = poss.lastTouchTeam == side[i];
        ranking(i, slot == 0);
        if (slot == 0 && athlete[i].isKeeper()) {
            keeper(i, ourBall);
            return;
        }
        if (slot == 0) {
            keeperPosition(i); // an outfielder in goal: may not handle, keeps the line
            return;
        }
        boolean passenger = injury[i] != null && injury[i].state == InjuryComponent.PASSENGER;
        if (rank == 0 && !passenger) {
            onBall(i, ourBall);
            return;
        }
        int chasers = tc.style == TeamStyle.DRIBBLING ? p.chasersDribbling : p.chasersCombination;
        if (!ourBall && rank < chasers && !passenger) {
            press(i);
            return;
        }
        if (ourBall) {
            support(i, slot, tc);
        } else {
            defend(i, slot);
        }
    }

    /**
     * Quick time-to-ball for every player from i's beliefs: sets {@link #rank} (team-mates quicker than him, keeper
     * left out of the outfield ranking), {@link #tMateBest} and {@link #tOppBest}; his own run target in ix, iy.
     */
    private void ranking(int i, boolean keeper) {
        PerceptionComponent pc = perception[i];
        double tMe = quickTime(pc, pc.x[i], pc.y[i], sprint(i));
        intercept(pc, body[i].x, body[i].y, sprint(i));
        rank = 0;
        tMateBest = INF;
        tOppBest = INF;
        TeamTacticsComponent tc = tactics[side[i]];
        for (int j = 0; j < cached; j++) {
            if (j == i || !onField(j)) {
                continue;
            }
            double t = quickTime(pc, pc.x[j], pc.y[j], sprint(j));
            if (side[j] == side[i]) {
                int sj = tc.slotOf[body[j].slot];
                if (!keeper && sj == 0) {
                    continue;
                }
                if (injury[j] != null && injury[j].state == InjuryComponent.PASSENGER
                        || play.offsidePosition(index[j])) {
                    continue;
                }
                if (t < tMe - p.rankMarginS || t <= tMe + p.rankMarginS && j < i) {
                    rank++;
                }
                if (t < tMateBest) {
                    tMateBest = t;
                }
            } else if (t < tOppBest) {
                tOppBest = t;
            }
        }
        if (play.offsidePosition(index[i])) {
            rank = cached; // the laws hold him offside: he leaves the ball to others
        }
    }

    // ---- with the ball, or going for it ---------------------------------------------------------------------------

    private void onBall(int i, boolean ourBall) {
        BodyComponent b = body[i];
        PerceptionComponent pc = perception[i];
        double d = dist(b.x, b.y, pc.ballX, pc.ballY);
        double footReach = b.radius + ap.footReachM;
        boolean low = pc.ballZ <= ap.footMaxZ * 0.9;
        boolean headable = pc.ballZ >= ap.headMinZ - 0.15 && pc.ballZ <= ap.headMaxZ + 0.4;
        if (low && d <= footReach + p.decideExtraM) {
            withBall(i, d);
            return;
        }
        if (headable && d <= b.radius + ap.headReachM + 1.2 && !act[i].armed()) {
            header(i);
        } else if (!low && !headable && d < 2.5 && pc.ballVz < 0 && !act[i].armed()) {
            // a dropping ball between foot and head: cushion it down when it comes into reach
            act[i].set(ActionIntent.DRIBBLE_TOUCH, b.x + tactics[side[i]].attackSign * 3, b.y, 0, 0.2, ap.armTicks);
        }
        go(i, ix, iy, sprint(i));
        if (!ourBall) {
            chargeCarrier(i);
        }
    }

    private void press(int i) {
        go(i, ix, iy, sprint(i));
        chargeCarrier(i);
    }

    /** Shoulder-charge the opponent on the ball when close (fair in 1888-89 from the front or side). */
    private void chargeCarrier(int i) {
        PerceptionComponent pc = perception[i];
        BodyComponent b = body[i];
        int carrier = -1;
        double best = 1.5;
        for (int j = 0; j < cached; j++) {
            if (side[j] == side[i] || !onField(j)) {
                continue;
            }
            double d = dist(pc.x[j], pc.y[j], pc.ballX, pc.ballY);
            if (d < best) {
                best = d;
                carrier = j;
            }
        }
        if (carrier < 0) {
            return;
        }
        double d = dist(b.x, b.y, pc.x[carrier], pc.y[carrier]);
        // his facing as believed: his run, or toward the ball when he stands
        double fx = pc.vx[carrier];
        double fy = pc.vy[carrier];
        double fv = Math.sqrt(fx * fx + fy * fy);
        if (fv < 1) {
            fx = pc.ballX - pc.x[carrier];
            fy = pc.ballY - pc.y[carrier];
            fv = Math.sqrt(fx * fx + fy * fy);
        }
        boolean behind = fv > 1e-6 && d > 1e-6
                && ((b.x - pc.x[carrier]) * fx + (b.y - pc.y[carrier]) * fy) / (fv * d) < p.chargeBehindCos;
        if (d < p.chargeRangeM && !behind) { // charging from behind is a foul (1869)
            move[i].charge = true;
            move[i].toward(b.x, b.y, pc.x[carrier], pc.y[carrier], sprint(i));
        }
    }

    private void header(int i) {
        BodyComponent b = body[i];
        PerceptionComponent pc = perception[i];
        int s = tactics[side[i]].attackSign;
        double fwd = s * pc.ballX;
        if (fwd > hl - 20 && Math.abs(pc.ballY) < 15) {
            act[i].set(ActionIntent.HEADER, s * hl, keeperSideAim(i), 1.0, 1, ap.armTicks);
        } else if (fwd < -hl + 30) {
            act[i].set(ActionIntent.HEADER, clampX(pc.ballX + s * 25), clampY(pc.ballY * 1.3), 0, 1, ap.armTicks);
        } else {
            int mate = nearestMateAhead(i);
            if (mate >= 0) {
                act[i].set(ActionIntent.HEADER, pc.x[mate], pc.y[mate], 0, 0.6, ap.armTicks);
            } else {
                act[i].set(ActionIntent.HEADER, clampX(b.x + s * 15), b.y, 0, 0.6, ap.armTicks);
            }
        }
    }

    /** The ball is (nearly) at his feet: pick and arm the best action, run onto the ball. */
    private void withBall(int i, double d) {
        BodyComponent b = body[i];
        ActionCommand ac = act[i];
        PerceptionComponent pc = perception[i];
        TeamTacticsComponent tc = tactics[side[i]];
        int s = tc.attackSign;
        if (ac.armed() && ac.intent.isKick()) {
            go(i, pc.ballX, pc.ballY, Math.max(cruise(i), 0.75 * athlete[i].topSpeed));
            return;
        }
        SkillComponent sk = skill[i];
        RandomSource r = whims[i];
        double bx = pc.ballX;
        double by = pc.ballY;
        double fwd = s * bx;
        double nearest = nearestOpponent(pc, side[i], b.x, b.y);
        double pressure = clamp01(1 - nearest / 5);
        bestU = -1;
        bestIntent = ActionIntent.NONE;

        // shoot
        double wShot = r.nextDouble() * p.whim;
        double gx = s * hl;
        double dg = dist(bx, by, gx, 0);
        double aimZ = 0.2 + 1.2 * r.nextDouble();
        if (dg < p.shotRangeM) {
            double angle = clamp01(Math.abs(gx - bx) / (dg + 1e-9) * 1.2);
            double u = p.wShot * (1 - dg / p.shotRangeM) * (0.5 + 0.5 * sk.kicking) * (0.7 + 0.3 * (1 - pressure))
                    * angle + (dg < 14 ? 0.4 : 0);
            consider(u + wShot, ActionIntent.SHOT, gx, keeperSideAim(i), aimZ, 0.85 + 0.15 * sk.kicking);
        }

        // pass, along the ground or lofted
        double wGround = r.nextDouble() * p.whim;
        double wLofted = r.nextDouble() * p.whim;
        double style = tc.style == TeamStyle.COMBINATION ? p.passStyleCombination : p.passStyleDribbling;
        double shortM = tc.style == TeamStyle.COMBINATION ? p.shortPassCombinationM : p.shortPassDribblingM;
        for (int j = 0; j < cached; j++) {
            if (j == i || side[j] != side[i] || !onField(j)) {
                continue;
            }
            int sj = tc.slotOf[body[j].slot];
            if (sj == 0 && pressure < 0.7) {
                continue; // back to the keeper only in trouble
            }
            double mx = pc.x[j] + pc.vx[j] * 0.6;
            double my = pc.y[j] + pc.vy[j] * 0.6;
            double dp = dist(bx, by, mx, my);
            if (dp < 6 || dp > 50 || Math.abs(mx) > hl - 1 || Math.abs(my) > hw - 1) {
                continue;
            }
            // progress: upfield, or in shooting range nearer the goal
            double gain = dg < p.shotRangeM + 8 ? dg - dist(mx, my, gx, 0) : s * (mx - bx);
            double progress = clamp(0.5 + gain / 30, 0.1, 1.5);
            double open = clamp01(nearestOpponent(pc, side[i], mx, my) / 8);
            double tech = 0.6 + 0.4 * sk.technique;
            double len = dp <= shortM ? 1 : shortM / dp;
            double lane = clamp01(laneClearance(pc, side[i], bx, by, mx, my) / 3);
            double offside = s * pc.x[j] > onside + 0.3 ? p.offsidePassFactor : 1;
            tech *= offside;
            double u = p.wPass * style * progress * open * lane * len * tech;
            consider(u + wGround, ActionIntent.PASS, mx, my, 0, 0.5);
            if (dp > 15) {
                double ul = p.wLoft * style * progress * open * tech * (dp <= 40 ? 1 : 40 / dp);
                consider(ul + wLofted, ActionIntent.PASS, mx, my, 1.0, 0.5);
            }
        }

        // cross from wide in the last third
        double wCross = r.nextDouble() * p.whim;
        if (fwd > hl - 28 && Math.abs(by) > 15) {
            int inBox = attackersNear(i, gx - s * 9, 0, 12);
            double u = p.wCross * (0.5 + 0.5 * sk.technique) * (inBox > 0 ? 1 : 0.3);
            consider(u + wCross, ActionIntent.CROSS, gx - s * 7, by > 0 ? -2 : 2, 1.7, 0.5);
        }

        // clear from the own third
        double wClear = r.nextDouble() * p.whim;
        if (fwd < -hl + 30) {
            double u = p.wClear * (0.25 + pressure);
            consider(u + wClear, ActionIntent.CLEAR, clampX(bx + s * 45), by >= 0 ? hw * 0.6 : -hw * 0.6, 0, 1);
        } else if (pressure > 0.8) {
            consider(0.35 * pressure + wClear, ActionIntent.CLEAR, clampX(bx + s * 40), clampY(by), 0, 1);
        }

        // dribble
        double wDribble = r.nextDouble() * p.whim;
        double dx = s;
        double dy = 0;
        if (fwd > hl - 30) {
            double ux = gx - bx;
            double uy = -by;
            double n = Math.sqrt(ux * ux + uy * uy) + 1e-9;
            dx = ux / n;
            dy = uy / n;
        }
        double space = 12;
        double avoidY = 0;
        for (int j = 0; j < cached; j++) {
            if (side[j] == side[i] || !onField(j)) {
                continue;
            }
            double ox = pc.x[j] - b.x;
            double oy = pc.y[j] - b.y;
            double ahead = ox * dx + oy * dy;
            if (ahead <= 0) {
                continue;
            }
            double od = Math.sqrt(ox * ox + oy * oy);
            if (ahead > 0.3 * od && od < space) {
                space = od;
                double across = -ox * dy + oy * dx;
                avoidY = across >= 0 ? -1 : 1;
            }
        }
        double space01 = space / 12;
        double ddx = dx - dy * avoidY * (1 - space01) * 0.8;
        double ddy = dy + dx * avoidY * (1 - space01) * 0.8;
        double dn = Math.sqrt(ddx * ddx + ddy * ddy);
        ddx /= dn;
        ddy /= dn;
        double dStyle = tc.style == TeamStyle.DRIBBLING ? p.dribbleStyleDribbling : p.dribbleStyleCombination;
        double u = p.wDribble * dStyle * (0.35 + 0.65 * sk.dribbling) * (0.25 + 0.75 * space01)
                * (1 - p.dribblePressurePenalty * pressure);
        double tx = clampX(bx + ddx * 8);
        double ty = clampY(by + ddy * 8);
        consider(u + wDribble, ActionIntent.DRIBBLE_TOUCH, tx, ty, 0, 0.2 + 0.5 * space01);

        ac.set(bestIntent, bestX, bestY, bestZ, bestPower, ap.armTicks);
        if (bestIntent == ActionIntent.DRIBBLE_TOUCH) {
            go(i, bx + ddx * 0.6, by + ddy * 0.6, sprint(i));
        } else {
            go(i, bx, by, Math.max(cruise(i), 0.75 * athlete[i].topSpeed));
        }
    }

    private void consider(double u, ActionIntent intent, double x, double y, double z, double power) {
        if (u > bestU) {
            bestU = u;
            bestIntent = intent;
            bestX = x;
            bestY = y;
            bestZ = z;
            bestPower = power;
        }
    }

    /** Aim inside the post on the side away from where he believes the opposing keeper stands. */
    private double keeperSideAim(int i) {
        PerceptionComponent pc = perception[i];
        TeamTacticsComponent opp = tactics[1 - side[i]];
        int k = opp.playerIn(0);
        int j = k < 0 ? -1 : member[1 - side[i]][k];
        double ky = j < 0 ? 0 : pc.y[j];
        double inside = postY - 0.7;
        return ky > 0 ? -inside : inside;
    }

    private int nearestMateAhead(int i) {
        PerceptionComponent pc = perception[i];
        int s = tactics[side[i]].attackSign;
        int best = -1;
        double bd = 30;
        for (int j = 0; j < cached; j++) {
            if (j == i || side[j] != side[i] || !onField(j)) {
                continue;
            }
            if (s * (pc.x[j] - body[i].x) < 0) {
                continue;
            }
            double d = dist(body[i].x, body[i].y, pc.x[j], pc.y[j]);
            if (d < bd) {
                bd = d;
                best = j;
            }
        }
        return best;
    }

    private int attackersNear(int i, double x, double y, double radius) {
        PerceptionComponent pc = perception[i];
        int c = 0;
        for (int j = 0; j < cached; j++) {
            if (j != i && side[j] == side[i] && onField(j) && dist(pc.x[j], pc.y[j], x, y) < radius) {
                c++;
            }
        }
        return c;
    }

    // ---- off the ball --------------------------------------------------------------------------------------------

    private void support(int i, int slot, TeamTacticsComponent tc) {
        PerceptionComponent pc = perception[i];
        BodyComponent b = body[i];
        anchor(i, slot, true, pc.ballX, pc.ballY);
        int s = tc.attackSign;
        Position.Line line = tc.shape.slot(slot).position().getLine();
        double tx = ax;
        double ty = ay;
        double dBall = dist(b.x, b.y, pc.ballX, pc.ballY);
        if (tc.style == TeamStyle.DRIBBLING && rank <= p.backersUp && line != Position.Line.BACK && dBall < 30) {
            // backing up: a pack behind and beside the dribbler
            double sideY = rank == 1 ? -1 : (rank == 2 ? 1 : 0);
            double behind = rank == 3 ? 2 * p.backUpBehindM : p.backUpBehindM;
            tx = pc.ballX - s * behind;
            ty = clampY(pc.ballY + s * sideY * p.backUpSideM);
        } else if (tc.style == TeamStyle.COMBINATION && rank == 1 && dBall < 30) {
            // the short option at an angle, on his own side of the ball
            double sideY = ay >= pc.ballY ? 1 : -1;
            tx = pc.ballX + s * p.shortOptionM * 0.6;
            ty = clampY(pc.ballY + sideY * p.shortOptionM * 0.8);
        }
        tx = clampX(tx);
        double far = dist(b.x, b.y, tx, ty);
        go(i, tx, ty, far > 12 && athlete[i].stamina > 0.5 ? 0.85 * athlete[i].topSpeed : cruise(i));
    }

    private void defend(int i, int slot) {
        PerceptionComponent pc = perception[i];
        BodyComponent b = body[i];
        TeamTacticsComponent tc = tactics[side[i]];
        anchor(i, slot, false, pc.ballX, pc.ballY);
        double tx = ax;
        double ty = ay;
        Position.Line line = tc.shape.slot(slot).position().getLine();
        if (line == Position.Line.BACK || line == Position.Line.HALF) {
            int mark = -1;
            double md = p.markRadiusM;
            for (int j = 0; j < cached; j++) {
                if (side[j] == side[i] || !onField(j)) {
                    continue;
                }
                double d = dist(ax, ay, pc.x[j], pc.y[j]);
                if (d < md) {
                    md = d;
                    mark = j;
                }
            }
            if (mark >= 0) {
                double gx = -tc.attackSign * hl;
                double ux = gx - pc.x[mark];
                double uy = -pc.y[mark];
                double n = Math.sqrt(ux * ux + uy * uy) + 1e-9;
                double mx = pc.x[mark] + ux / n * p.markGoalSideM;
                double my = pc.y[mark] + uy / n * p.markGoalSideM;
                tx = ax + (mx - ax) * p.markWeight;
                ty = ay + (my - ay) * p.markWeight;
            }
        }
        double far = dist(b.x, b.y, tx, ty);
        boolean danger = tc.attackSign * pc.ballX < -hl + 35;
        go(i, tx, ty, (far > 10 || danger) && athlete[i].stamina > 0.4 ? 0.85 * athlete[i].topSpeed : cruise(i));
    }

    /**
     * Anchor of shape slot {@code slot} (into ax, ay): its kick-off depth shifted with the ball, spread across and
     * pulled toward the ball; kept inside the pitch and in line order.
     */
    private void anchor(int i, int slot, boolean attacking, double ballX, double ballY) {
        TeamTacticsComponent tc = tactics[side[i]];
        Shape.Slot sl = tc.shape.slot(slot);
        int s = tc.attackSign;
        double bxT = s * ballX;
        double byT = s * ballY;
        double baseX = -hl + Math.min(sl.depth(), 1) * hl;
        Position.Line line = sl.position().getLine();
        double x;
        double lo;
        double hi;
        if (attacking) {
            x = baseX + p.shiftAttack * bxT + p.pushAttackM;
        } else {
            double shift = line == Position.Line.BACK ? p.shiftDefendBacks
                    : (line == Position.Line.HALF ? p.shiftDefendHalves : p.shiftDefendForwards);
            x = baseX + shift * bxT - p.dropDefendM;
        }
        switch (line) {
            case GOAL:
                lo = -hl + 0.5;
                hi = -hl + 6;
                break;
            case BACK:
                lo = -hl + 5;
                hi = attacking ? 12 : 0;
                break;
            case HALF:
                lo = -hl + 9;
                hi = hl - 22;
                break;
            default:
                lo = -hl + 14;
                hi = hl - 7;
                break;
        }
        x = clamp(x, lo, hi);
        if (line == Position.Line.FORWARD && x > onside - p.onsideMarginM) {
            x = Math.max(onside - p.onsideMarginM, lo); // keep onside of the third-last man
        }
        double spread = attacking ? (tc.style == TeamStyle.COMBINATION ? p.spreadCombination : p.spreadDribbling)
                : p.spreadDefend;
        double y = sl.lateral() * hw * spread + p.lateralPull * byT;
        y = clamp(y, -(hw - 2), hw - 2);
        ax = s * x;
        ay = s * y;
    }

    // ---- keeper ----------------------------------------------------------------------------------------------------

    private void keeper(int i, boolean ourBall) {
        BodyComponent b = body[i];
        PerceptionComponent pc = perception[i];
        ActionCommand ac = act[i];
        int s = tactics[side[i]].attackSign;
        double gx = -s * hl;
        double bx = pc.ballX;
        double by = pc.ballY;
        double d = dist(b.x, b.y, bx, by);
        double depth = s * bx + hl; // ball's distance from his goal line
        boolean loose = !ourBall || d < 3;
        // a shot or a ball rolling toward goal
        double toward = -s * pc.ballVx;
        if (toward > 4 && depth < 40) {
            double tg = depth / toward;
            double yl = by + pc.ballVy * tg;
            if (tg < 2.5 && Math.abs(yl) < postY + 2) {
                go(i, gx + s * 0.6, clamp(yl, -postY + 0.3, postY - 0.3), athlete[i].topSpeed);
                if (d < 7 && !ac.armed()) {
                    armSave(i, d);
                }
                return;
            }
        }
        // come out for a loose ball near goal when he is there first
        if (loose && depth < p.keeperRushM && Math.abs(by) < 22 && tMateBest + 0.3 > quickTime(pc, b.x, b.y, sprint(i))
                && quickTime(pc, b.x, b.y, sprint(i)) < tOppBest - 0.2) {
            go(i, ix, iy, sprint(i));
            if (d < 3 && pc.ballZ < 2.5 && !ac.armed()) {
                armSave(i, d);
            }
            return;
        }
        if (d < 2.5 && pc.ballZ < 2.5 && !ac.armed() && depth < hl) {
            armSave(i, d);
        }
        keeperPosition(i);
    }

    private void armSave(int i, double d) {
        PerceptionComponent pc = perception[i];
        BodyComponent b = body[i];
        int s = tactics[side[i]].attackSign;
        boolean crowded = nearestOpponent(pc, side[i], b.x, b.y) < 2.5;
        if (pc.ballZ > 1.9 && crowded) {
            act[i].set(ActionIntent.KEEPER_PUNCH, clampX(b.x + s * 25), pc.ballY >= 0 ? hw * 0.7 : -hw * 0.7, 0, 1,
                    ap.armTicks);
        } else {
            act[i].set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, 2 * ap.armTicks);
        }
    }

    /** On the line from the goal centre to the ball, a few metres out. */
    private void keeperPosition(int i) {
        BodyComponent b = body[i];
        PerceptionComponent pc = perception[i];
        int s = tactics[side[i]].attackSign;
        double gx = -s * hl;
        double ux = pc.ballX - gx;
        double uy = pc.ballY;
        double n = Math.sqrt(ux * ux + uy * uy) + 1e-9;
        double out = clamp(0.12 * n, 0.8, 5);
        double tx = gx + ux / n * out;
        double ty = clamp(uy / n * out, -postY, postY);
        go(i, tx, ty, Math.min(athlete[i].topSpeed * 0.7, 0.5 + 2 * dist(b.x, b.y, tx, ty)));
    }

    // ---- dead ball -----------------------------------------------------------------------------------------------

    private void deadBall(int i, int slot, long tick) {
        BodyComponent b = body[i];
        PerceptionComponent pc = perception[i];
        TeamTacticsComponent tc = tactics[side[i]];
        int s = tc.attackSign;
        boolean ours = play.restartTeam == side[i];
        if (isTaker(i)) {
            takeRestart(i, s, tick);
            return;
        }
        if (slot == 0 && athlete[i].isKeeper()) {
            keeperPosition(i);
            return;
        }
        anchor(i, slot, ours, play.restartX, play.restartY);
        double tx = ax;
        double ty = ay;
        Position.Line line = tc.shape.slot(slot).position().getLine();
        switch (play.restartType()) {
            case KICK_OFF:
                if (s * tx > -1.5) {
                    tx = -s * 1.5;
                }
                if (!ours) {
                    double d = dist(tx, ty, 0, 0);
                    double ring = 10 * 0.9144 + 1.5;
                    if (d < ring) {
                        double k = d < 1e-6 ? 1 : ring / d;
                        tx = d < 1e-6 ? -s * ring : tx * k;
                        ty = d < 1e-6 ? 0 : ty * k;
                    }
                }
                break;
            case CORNER_KICK:
                if (ours && line == Position.Line.FORWARD) {
                    double gx = s * hl;
                    tx = gx - s * (6 + 6 * Math.abs(tc.shape.slot(slot).lateral()));
                    ty = tc.shape.slot(slot).lateral() * 14 * s;
                } else if (!ours && line != Position.Line.FORWARD) {
                    double gx = -s * hl;
                    tx = gx + s * (4 + 4 * Math.abs(tc.shape.slot(slot).lateral()));
                    ty = -tc.shape.slot(slot).lateral() * 12 * s;
                }
                break;
            default:
                break;
        }
        go(i, tx, ty, dist(b.x, b.y, tx, ty) > 15 ? 0.8 * athlete[i].topSpeed : cruise(i));
    }

    private void takeRestart(int i, int s, long tick) {
        BodyComponent b = body[i];
        PerceptionComponent pc = perception[i];
        double sx = play.restartX;
        double sy = play.restartY;
        ActionIntent intent;
        double tx;
        double ty;
        double tz = 0;
        double power = 0.5;
        switch (play.restartType()) {
            case KICK_OFF: {
                intent = ActionIntent.PASS;
                int mate = nearestMate(i, sx, sy, 4, 25);
                tx = mate >= 0 ? pc.x[mate] + s * 3 : sx + s * 8;
                ty = mate >= 0 ? pc.y[mate] : 6;
                break;
            }
            case THROW_IN: {
                intent = ActionIntent.THROW_IN;
                int mate = openMate(i, sx, sy, 25);
                tx = mate >= 0 ? pc.x[mate] : clampX(sx + s * 12);
                ty = mate >= 0 ? pc.y[mate] : sy * 0.7;
                power = 0.6;
                break;
            }
            case GOAL_KICK:
                intent = ActionIntent.CLEAR;
                tx = clampX(sx + s * 45);
                ty = sy >= 0 ? hw * 0.5 : -hw * 0.5;
                power = 1;
                break;
            case FREE_KICK:
            case PENALTY_KICK: {
                double fwd = s * sx;
                if (play.restartType() == RestartType.PENALTY_KICK || play.restartGoalDirect && fwd > hl - 25) {
                    intent = ActionIntent.SHOT;
                    tx = s * hl;
                    ty = keeperSideAim(i);
                    tz = 0.6;
                    power = 0.9;
                } else if (fwd < -hl / 3) {
                    intent = ActionIntent.CLEAR;
                    tx = clampX(sx + s * 45);
                    ty = sy >= 0 ? hw * 0.5 : -hw * 0.5;
                    power = 1;
                } else if (fwd > hl - 35) {
                    // indirect near goal (every free kick in 1888-89): lift it into the goalmouth
                    intent = ActionIntent.CROSS;
                    tx = s * (hl - 7);
                    ty = 0;
                    tz = 1.7;
                } else {
                    intent = ActionIntent.PASS;
                    int mate = openMate(i, sx, sy, 30);
                    tx = mate >= 0 ? pc.x[mate] : clampX(sx + s * 15);
                    ty = mate >= 0 ? pc.y[mate] : sy;
                }
                break;
            }
            default: // corner
                intent = ActionIntent.CROSS;
                tx = s * (hl - 6);
                ty = sy > 0 ? -1.5 : 1.5;
                tz = 1.7;
                break;
        }
        // stand just behind the ball, facing the target
        double ux = tx - sx;
        double uy = ty - sy;
        double n = Math.sqrt(ux * ux + uy * uy) + 1e-9;
        double back = intent == ActionIntent.THROW_IN ? b.radius + 0.3 : b.radius + 0.25;
        double px = sx - ux / n * back;
        double py = sy - uy / n * back;
        double d = dist(b.x, b.y, px, py);
        if (!play.restartReady(tick) || d > 0.6) {
            go(i, px, py, d > 8 ? 0.85 * athlete[i].topSpeed : Math.max(1.0, cruise(i)));
            return;
        }
        go(i, sx, sy, 1.5);
        if (!act[i].armed()) {
            act[i].set(intent, tx, ty, tz, power, ap.armTicks);
        }
    }

    private int nearestMate(int i, double x, double y, double min, double max) {
        PerceptionComponent pc = perception[i];
        int best = -1;
        double bd = max;
        for (int j = 0; j < cached; j++) {
            if (j == i || side[j] != side[i] || !onField(j) || tactics[side[i]].slotOf[body[j].slot] == 0) {
                continue;
            }
            double d = dist(x, y, pc.x[j], pc.y[j]);
            if (d >= min && d < bd) {
                bd = d;
                best = j;
            }
        }
        return best;
    }

    private int openMate(int i, double x, double y, double max) {
        PerceptionComponent pc = perception[i];
        int best = -1;
        double bs = 0;
        for (int j = 0; j < cached; j++) {
            if (j == i || side[j] != side[i] || !onField(j) || tactics[side[i]].slotOf[body[j].slot] == 0) {
                continue;
            }
            double d = dist(x, y, pc.x[j], pc.y[j]);
            if (d < 4 || d > max) {
                continue;
            }
            double score = clamp01(nearestOpponent(pc, side[i], pc.x[j], pc.y[j]) / 6) * (1 - d / (max + 10));
            if (score > bs) {
                bs = score;
                best = j;
            }
        }
        return best;
    }

    /** Team-frame x of the offside line from i's beliefs: the third-last opponent, or the ball if it is further. */
    private double onsideLine(int i) {
        PerceptionComponent pc = perception[i];
        int s = tactics[side[i]].attackSign;
        double a = -INF;
        double b = -INF;
        double c = -INF;
        for (int j = 0; j < cached; j++) {
            if (side[j] == side[i] || !onField(j)) {
                continue;
            }
            double x = s * pc.x[j];
            if (x > a) {
                c = b;
                b = a;
                a = x;
            } else if (x > b) {
                c = b;
                b = x;
            } else if (x > c) {
                c = x;
            }
        }
        double ball = s * pc.ballX;
        double line = c > -INF ? c : hl;
        return Math.max(line, ball);
    }

    // ---- geometry and estimates --------------------------------------------------------------------------------

    /** Quick time for a man at (px, py) running at {@code speed} to reach the believed ball (one quadratic). */
    private double quickTime(PerceptionComponent pc, double px, double py, double speed) {
        double scale = pc.ballZ > 0.3 ? 0.85 : 0.7; // the ball slows: plan with part of its speed
        return p.startDelayS + solve(pc.ballX - px, pc.ballY - py, pc.ballVx * scale, pc.ballVy * scale, speed, 0.8);
    }

    /** Where (ix, iy) and when he meets the believed ball, with constant deceleration (three refinements). */
    private double intercept(PerceptionComponent pc, double px, double py, double speed) {
        double v = Math.sqrt(pc.ballVx * pc.ballVx + pc.ballVy * pc.ballVy);
        double a = pc.ballZ > 0.3 ? p.airDecelBelief : p.rollDecelBelief;
        double ux = v > 1e-6 ? pc.ballVx / v : 0;
        double uy = v > 1e-6 ? pc.ballVy / v : 0;
        double tStop = v / a;
        double t = 2.0;
        for (int k = 0; k < 3; k++) {
            double veff = t <= 0 ? v : travel(v, a, tStop, t) / t;
            double tn = solve(pc.ballX - px, pc.ballY - py, ux * veff, uy * veff, speed, 0.8);
            t = tn >= INF ? Math.max(tStop, 6) : tn;
        }
        double s = travel(v, a, tStop, t);
        ix = clampX(pc.ballX + ux * s);
        iy = clampY(pc.ballY + uy * s);
        return t + p.startDelayS;
    }

    private static double travel(double v, double a, double tStop, double t) {
        return t < tStop ? v * t - 0.5 * a * t * t : v * v / (2 * a);
    }

    /** Smallest t >= 0 with |r + v t| = u t + reach, INF when never. */
    private static double solve(double rx, double ry, double vx, double vy, double u, double reach) {
        double c = rx * rx + ry * ry - reach * reach;
        if (c <= 0) {
            return 0;
        }
        double a = vx * vx + vy * vy - u * u;
        double b = 2 * (rx * vx + ry * vy - u * reach);
        if (Math.abs(a) < 1e-9) {
            return b < 0 ? -c / b : INF;
        }
        double disc = b * b - 4 * a * c;
        if (disc < 0) {
            return INF;
        }
        double sq = Math.sqrt(disc);
        double t1 = (-b - sq) / (2 * a);
        double t2 = (-b + sq) / (2 * a);
        double lo = Math.min(t1, t2);
        double hi = Math.max(t1, t2);
        if (lo >= 0) {
            return lo;
        }
        return hi >= 0 ? hi : INF;
    }

    private double nearestOpponent(PerceptionComponent pc, int team, double x, double y) {
        double best = INF;
        for (int j = 0; j < cached; j++) {
            if (side[j] == team || !onField(j)) {
                continue;
            }
            double d = dist(pc.x[j], pc.y[j], x, y);
            if (d < best) {
                best = d;
            }
        }
        return best;
    }

    /** Closest an opponent stands to the segment from (x0, y0) to (x1, y1) (ends excluded behind the passer). */
    private double laneClearance(PerceptionComponent pc, int team, double x0, double y0, double x1, double y1) {
        double sx = x1 - x0;
        double sy = y1 - y0;
        double len2 = sx * sx + sy * sy;
        double best = INF;
        for (int j = 0; j < cached; j++) {
            if (side[j] == team || !onField(j)) {
                continue;
            }
            double t = ((pc.x[j] - x0) * sx + (pc.y[j] - y0) * sy) / len2;
            if (t <= 0 || t > 1.05) {
                continue;
            }
            double qx = x0 + sx * t - pc.x[j];
            double qy = y0 + sy * t - pc.y[j];
            double d = Math.sqrt(qx * qx + qy * qy);
            if (d < best) {
                best = d;
            }
        }
        return best;
    }

    /** Move toward (x, y) at up to {@code speed}, slowing on arrival. */
    private void go(int i, double x, double y, double speed) {
        BodyComponent b = body[i];
        double d = dist(b.x, b.y, x, y);
        if (d < 0.3) {
            move[i].set(0, 0);
            return;
        }
        move[i].toward(b.x, b.y, x, y, Math.min(speed, 0.4 + 1.5 * d));
    }

    private double sprint(int i) {
        AthleteComponent a = athlete[i];
        return a.stamina < 0.2 ? 0.8 * a.topSpeed : a.topSpeed;
    }

    private double cruise(int i) {
        AthleteComponent a = athlete[i];
        return a.topSpeed * cruiseShare * (0.75 + 0.25 * a.reserve);
    }

    private boolean onField(int j) {
        return injury[j] == null || !injury[j].isOff();
    }

    private int byIndex(int creationIndex) {
        for (int j = 0; j < cached; j++) {
            if (index[j] == creationIndex) {
                return j;
            }
        }
        return -1;
    }

    private double clampX(double x) {
        return clamp(x, -hl + 1, hl - 1);
    }

    private double clampY(double y) {
        return clamp(y, -hw + 1, hw - 1);
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static double clamp01(double v) {
        return clamp(v, 0, 1);
    }

    private static double dist(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void cache() {
        int n = players.size();
        body = new BodyComponent[n];
        athlete = new AthleteComponent[n];
        injury = new InjuryComponent[n];
        move = new MoveCommand[n];
        act = new ActionCommand[n];
        skill = new SkillComponent[n];
        perception = new PerceptionComponent[n];
        whims = new RandomSource[n];
        index = new int[n];
        side = new int[n];
        for (int t = 0; t < 2; t++) {
            java.util.Arrays.fill(member[t], -1);
            tactics[t] = null;
        }
        for (int t = 0; t < teams.size(); t++) {
            TeamTacticsComponent tc = tacticsMap.get(teams.get(t));
            tactics[tc.team.ordinal()] = tc;
        }
        for (int i = 0; i < n; i++) {
            Entity e = players.get(i);
            body[i] = bodyMap.get(e);
            athlete[i] = athleteMap.get(e);
            injury[i] = injuryMap.get(e);
            move[i] = moveMap.get(e);
            act[i] = actMap.get(e);
            SkillComponent s = skillMap.get(e);
            skill[i] = s != null ? s : SkillComponent.AVERAGE;
            perception[i] = perceptionMap.get(e);
            IdentityComponent id = idMap.get(e);
            index[i] = id == null ? i : id.index;
            side[i] = body[i].team.ordinal();
            whims[i] = world.random().fork("ai:decision:" + index[i]);
            if (athlete[i] == null || move[i] == null || act[i] == null) {
                throw new IllegalStateException("an AI player needs athlete, move and action components");
            }
            int k = body[i].slot;
            if (k >= 0 && k < 11) {
                member[side[i]][k] = i;
            }
        }
        cached = n;
        cachedTeams = teams.size();
    }
}
