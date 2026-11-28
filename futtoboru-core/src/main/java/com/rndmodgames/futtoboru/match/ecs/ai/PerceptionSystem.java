package com.rndmodgames.futtoboru.match.ecs.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;

/**
 * Perception (E-15, §4.1): each player's noisy, delayed view of the ball and the other players.
 *
 * <ul>
 * <li>Every tick the true positions go into a short ring buffer; a player's beliefs come from the snapshot
 * {@link AiParams#reactionTicks} ago (reaction time), so he acts on where things were.</li>
 * <li>A player refreshes at the think rate ({@code PhysicsParams.thinkEveryTicks}: 10 Hz), staggered by player
 * ({@code (tick + i) % think == 0}), so the cost is flat over ticks.</li>
 * <li>Noise on position and velocity grows with distance; behind his back (outside the view cone) it is
 * {@link AiParams#behindFactor} times larger. Uniform noise with the sigma of the parameter (one draw per value, a
 * fixed count per refresh) from the player's own {@code ai:perception:<index>} fork. A perception rating (E-01) will
 * scale it later; everyone sees alike for now.</li>
 * </ul>
 * Only players with a {@link PerceptionComponent} take part (worlds without AI are untouched). No allocation per tick
 * once the player set is fixed.
 */
public final class PerceptionSystem extends EntitySystem implements SimSystem {

    private static final double SQRT3 = Math.sqrt(3);

    private final ComponentMapper<BodyComponent> bodyMap = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<PerceptionComponent> perceptionMap = ComponentMapper.getFor(PerceptionComponent.class);
    private final ComponentMapper<InjuryComponent> injuryMap = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<IdentityComponent> idMap = ComponentMapper.getFor(IdentityComponent.class);
    private final ComponentMapper<BallComponent> ballMap = ComponentMapper.getFor(BallComponent.class);

    private final MatchWorld world;
    private final AiParams p;
    private final int think;
    private final int history;

    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> balls;
    private int cached = -1;
    private BodyComponent[] body = new BodyComponent[0];
    private PerceptionComponent[] perception = new PerceptionComponent[0];
    private InjuryComponent[] injury = new InjuryComponent[0];
    private RandomSource[] noise = new RandomSource[0];
    /** [history][4 * n]: x, y, vx, vy per player. */
    private double[][] past = new double[0][0];
    /** [history][6]: ball centre x, y, z (lowest point) and velocity. */
    private final double[][] pastBall;
    private long recorded;

    public PerceptionSystem(MatchWorld world) {
        super(SystemPriority.PERCEPTION);
        this.world = world;
        this.p = world.params().ai;
        this.think = world.params().thinkEveryTicks;
        this.history = p.reactionTicks + 1;
        this.pastBall = new double[history][6];
    }

    /** Players that perceive: a body and a perception component. */
    public static Family family() {
        return Family.all(BodyComponent.class, PerceptionComponent.class).get();
    }

    @Override
    public void addedToEngine(Engine engine) {
        players = engine.getEntitiesFor(family());
        balls = engine.getEntitiesFor(Family.all(BallComponent.class).get());
    }

    @Override
    public void update(float ignoredFrameDelta) {
        if (players.size() != cached) {
            cache();
        }
        int n = cached;
        if (n == 0 || balls.size() == 0) {
            return;
        }
        BallComponent ball = ballMap.get(balls.get(0));
        long tick = world.tickCount();
        int at = (int) (tick % history);
        double[] snap = past[at];
        for (int i = 0; i < n; i++) {
            BodyComponent b = body[i];
            snap[4 * i] = b.x;
            snap[4 * i + 1] = b.y;
            snap[4 * i + 2] = b.vx;
            snap[4 * i + 3] = b.vy;
        }
        double[] sb = pastBall[at];
        sb[0] = ball.x;
        sb[1] = ball.y;
        sb[2] = ball.z;
        sb[3] = ball.vx;
        sb[4] = ball.vy;
        sb[5] = ball.vz;
        recorded++;

        long back = Math.min(p.reactionTicks, recorded - 1);
        int from = (int) ((tick - back) % history);
        for (int i = 0; i < n; i++) {
            if ((tick + i) % think != 0 || injury[i] != null && injury[i].isOff()) {
                continue;
            }
            refresh(i, past[from], pastBall[from], tick - back);
        }
    }

    private void refresh(int i, double[] snap, double[] sb, long seen) {
        BodyComponent me = body[i];
        PerceptionComponent pc = perception[i];
        RandomSource r = noise[i];
        pc.seenTick = seen;

        double d = dist(me, sb[0], sb[1]);
        double k = behind(me, sb[0], sb[1], d);
        double sp = (p.ballSigmaBaseM + p.ballSigmaPerM * d) * k;
        double sv = (p.velSigmaBase + p.velSigmaPerM * d) * k;
        pc.ballX = sb[0] + u(r) * sp;
        pc.ballY = sb[1] + u(r) * sp;
        double z = sb[2] + u(r) * sp * 0.5;
        pc.ballZ = z < 0 ? 0 : z;
        pc.ballVx = sb[3] + u(r) * sv;
        pc.ballVy = sb[4] + u(r) * sv;
        pc.ballVz = sb[5];

        for (int j = 0; j < cached; j++) {
            if (j == i) {
                pc.x[j] = me.x;
                pc.y[j] = me.y;
                pc.vx[j] = me.vx;
                pc.vy[j] = me.vy;
                continue;
            }
            double ox = snap[4 * j];
            double oy = snap[4 * j + 1];
            double dj = dist(me, ox, oy);
            double kj = behind(me, ox, oy, dj);
            double s = (p.playerSigmaBaseM + p.playerSigmaPerM * dj) * kj;
            double v = (p.velSigmaBase + p.velSigmaPerM * dj) * kj;
            pc.x[j] = ox + u(r) * s;
            pc.y[j] = oy + u(r) * s;
            pc.vx[j] = snap[4 * j + 2] + u(r) * v;
            pc.vy[j] = snap[4 * j + 3] + u(r) * v;
        }
    }

    /** Uniform noise with unit standard deviation. */
    private static double u(RandomSource r) {
        return (2 * r.nextDouble() - 1) * SQRT3;
    }

    private double behind(BodyComponent me, double x, double y, double d) {
        if (d < 1e-6) {
            return 1;
        }
        double c = ((x - me.x) * me.fx + (y - me.y) * me.fy) / d;
        return c < p.viewCosLimit ? p.behindFactor : 1;
    }

    private static double dist(BodyComponent me, double x, double y) {
        double dx = x - me.x;
        double dy = y - me.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void cache() {
        int n = players.size();
        body = new BodyComponent[n];
        perception = new PerceptionComponent[n];
        injury = new InjuryComponent[n];
        noise = new RandomSource[n];
        past = new double[history][4 * n];
        for (int i = 0; i < n; i++) {
            Entity e = players.get(i);
            body[i] = bodyMap.get(e);
            perception[i] = perceptionMap.get(e);
            injury[i] = injuryMap.get(e);
            IdentityComponent id = idMap.get(e);
            noise[i] = world.random().fork("ai:perception:" + (id == null ? i : id.index));
            if (perception[i].x.length != n) {
                throw new IllegalStateException("perception sized for " + perception[i].x.length + " players, " + n
                        + " perceive");
            }
        }
        recorded = 0;
        cached = n;
    }
}
