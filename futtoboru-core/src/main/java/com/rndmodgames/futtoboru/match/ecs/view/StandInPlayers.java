package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;

/**
 * Debug viewer only: view-side stand-in positions for player and official entities that have no simulated body yet
 * (until the E-13 bodies bring their own pose source). Each side lines up in the 1888 2-3-5, shifts with the ball and
 * the nearest forward chases it; officials follow play from the touchlines. Pure view state, stepped once per captured
 * tick at a running pace; it reads the ball and writes nothing of the simulation.
 */
public final class StandInPlayers implements PoseSource {

    private static final Family FAMILY = Family.all(IdentityComponent.class).exclude(BallComponent.class).get();
    /** 2-3-5 (keeper, two backs, three half-backs, five forwards) for the side attacking +x: x, y in metres. */
    private static final double[][] SHAPE = { { -48, 0 }, { -34, -10 }, { -34, 10 }, { -18, -18 }, { -18, 0 },
            { -18, 18 }, { -3, -26 }, { -3, -12 }, { -3, 0 }, { -3, 12 }, { -3, 26 } };
    private static final double RUN_MPS = 6.5;

    private final MatchWorld world;
    private final ComponentMapper<IdentityComponent> ids = ComponentMapper.getFor(IdentityComponent.class);
    private final ComponentMapper<BallComponent> balls = ComponentMapper.getFor(BallComponent.class);
    private final double halfLength;
    private final double halfWidth;
    private double[] xs = new double[32];
    private double[] ys = new double[32];
    private long[] ticks = new long[32];
    private BallComponent ball;

    public StandInPlayers(MatchWorld world, double halfLength, double halfWidth) {
        this.world = world;
        this.halfLength = halfLength;
        this.halfWidth = halfWidth;
        java.util.Arrays.fill(ticks, -1);
    }

    @Override
    public Family family() {
        return FAMILY;
    }

    @Override
    public void read(Entity entity, double[] xyz) {
        IdentityComponent id = ids.get(entity);
        int i = id.index;
        if (i >= xs.length) {
            xs = java.util.Arrays.copyOf(xs, i * 2);
            ys = java.util.Arrays.copyOf(ys, i * 2);
            int old = ticks.length;
            ticks = java.util.Arrays.copyOf(ticks, i * 2);
            java.util.Arrays.fill(ticks, old, ticks.length, -1);
        }
        double bx = 0;
        double by = 0;
        BallComponent b = ball();
        if (b != null) {
            bx = b.x;
            by = b.y;
        }
        double[] target = target(id, bx, by);
        long now = world.tickCount();
        if (ticks[i] < 0) {
            xs[i] = target[0];
            ys[i] = target[1];
        } else if (ticks[i] != now) {
            double step = RUN_MPS * world.tickSeconds() * (now - ticks[i]);
            double dx = target[0] - xs[i];
            double dy = target[1] - ys[i];
            double d = Math.hypot(dx, dy);
            if (d <= step) {
                xs[i] = target[0];
                ys[i] = target[1];
            } else {
                xs[i] += dx / d * step;
                ys[i] += dy / d * step;
            }
        }
        ticks[i] = now;
        xyz[0] = xs[i];
        xyz[1] = ys[i];
        xyz[2] = 0;
    }

    private double[] target(IdentityComponent id, double bx, double by) {
        int k = id.index;
        if (id.role == SpawnRole.OFFICIAL) {
            int o = k - 23;
            if (o == 0) {
                return new double[] { bx * 0.8, by * 0.5 - 6 }; // referee near play
            }
            double side = o == 1 ? -1 : 1; // umpires on the touchlines, one per half
            return new double[] { clamp(bx, side < 0 ? -halfLength : 0, side < 0 ? 0 : halfLength),
                    side * (halfWidth + 1.5) };
        }
        boolean home = id.role == SpawnRole.HOME_PLAYER;
        int slot = home ? k : k - 11;
        slot = Math.max(0, Math.min(10, slot));
        double dir = home ? 1 : -1;
        double x = SHAPE[slot][0] * dir * (halfLength / 52) + bx * (slot == 0 ? 0.1 : 0.55);
        double y = SHAPE[slot][1] * (halfWidth / 32) + by * (slot == 0 ? 0.2 : 0.25);
        // the centre forward of each side chases the ball
        if (slot == 8) {
            x = bx - dir * 0.8;
            y = by;
        }
        return new double[] { clamp(x, -halfLength, halfLength), clamp(y, -halfWidth, halfWidth) };
    }

    private BallComponent ball() {
        if (ball == null) {
            var list = world.engine().getEntitiesFor(Family.all(BallComponent.class).get());
            if (list.size() > 0) {
                ball = balls.get(list.first());
            }
        }
        return ball;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
