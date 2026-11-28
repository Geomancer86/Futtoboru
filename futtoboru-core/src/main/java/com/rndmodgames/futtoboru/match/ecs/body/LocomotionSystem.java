package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;

/**
 * Locomotion (E-13, §3.3): steers every body toward its {@link MoveCommand} velocity within its limits, once per
 * tick.
 *
 * <ul>
 * <li><b>Speed:</b> top speed from the athlete, x surface speed factor, x boots (wet boots are heavier), x stamina and
 * reserve, x injury. Acceleration falls linearly to 15 % at top speed; braking is harder than accelerating.</li>
 * <li><b>Grip:</b> forward and sideways acceleration are both capped at grip x g, grip = surface x sole (bars dig in
 * on a heavy pitch, skate on frozen ground). Sideways: a turn at speed v needs v·ω ≤ grip·g.</li>
 * <li><b>Turn:</b> the athlete's turn rate falls with (v/top)^2; a target behind him is reached by braking while
 * turning (speed target x cos of the angle still to turn).</li>
 * <li><b>Stamina:</b> above the aerobic level (v/top)^2 drains short-term stamina, below it refills; every step
 * drains the match reserve; drain x surface fatigue x boots.</li>
 * <li>A body on the ground slides to a stop; a stumbling one has half its limits; an injured one has reduced limits;
 * one that is off stands still.</li>
 * </ul>
 * No allocation per tick: heading is a unit vector, turned with a short series ({@link #sinSmall}); StrictMath.atan2
 * only (on JDK 17 StrictMath.sin/cos allocate during argument reduction).
 */
public final class LocomotionSystem extends EntitySystem implements SimSystem {

    private static final double TWO_PI = 2 * StrictMath.PI;

    private final ComponentMapper<BodyComponent> bodies = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<AthleteComponent> athletes = ComponentMapper.getFor(AthleteComponent.class);
    private final ComponentMapper<KitComponent> kits = ComponentMapper.getFor(KitComponent.class);
    private final ComponentMapper<InjuryComponent> injuries = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<MoveCommand> commands = ComponentMapper.getFor(MoveCommand.class);

    private final MatchWorld world;
    private final BodyParams p;
    private final double g;
    private final double dt;
    private ImmutableArray<Entity> entities;

    public LocomotionSystem(MatchWorld world) {
        super(SystemPriority.LOCOMOTION);
        this.world = world;
        this.p = world.params().body;
        this.g = world.params().gravity;
        this.dt = world.tickSeconds();
    }

    /** The bodies this system moves: body, athlete, kit, injury and command. */
    public static Family family() {
        return Family.all(BodyComponent.class, AthleteComponent.class, KitComponent.class, InjuryComponent.class,
                MoveCommand.class).get();
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(family());
    }

    @Override
    public void update(float ignoredFrameDelta) {
        PitchConditions pitch = world.pitch();
        double wetness = pitch.wetness();
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            step(bodies.get(e), athletes.get(e), kits.get(e), injuries.get(e), commands.get(e), pitch, wetness);
        }
    }

    private void step(BodyComponent b, AthleteComponent a, KitComponent kit, InjuryComponent inj, MoveCommand cmd,
            PitchConditions pitch, double wetness) {
        b.prevX = b.x;
        b.prevY = b.y;
        kit.soak(wetness, dt);
        b.massKg = b.bodyMassKg + kit.carriedMassKg();
        inj.tick();
        if (inj.isOff()) {
            b.vx = 0;
            b.vy = 0;
            return;
        }
        BodyParams.SurfaceBody ground = p.surface(pitch.surfaceAt(b.x, b.y));
        double grip = ground.grip(kit.studs) * g;
        double excess = kit.pairExcessKg(p);
        double limit = inj.limitFactor(p);
        double v = Math.sqrt(b.vx * b.vx + b.vy * b.vy);

        if (b.downTicks > 0) {
            b.downTicks--;
            double keep = Math.max(0, 1 - p.groundFrictionPerS * dt);
            b.vx *= keep;
            b.vy *= keep;
            b.x += b.vx * dt;
            b.y += b.vy * dt;
            stamina(a, v * keep, ground, excess);
            return;
        }

        double vMax = a.topSpeed * ground.speedFactor * (1 - p.topSpeedPerBootKg * excess)
                * lerp(p.speedAtEmptyStamina, 1, a.stamina) * lerp(p.speedAtEmptyReserve, 1, a.reserve) * limit;
        double accel = Math.min(a.accel * (1 - p.accelPerBootKg * excess) * lerp(p.accelAtEmptyStamina, 1, a.stamina)
                * limit, grip);
        double decel = Math.min(a.decel * limit, grip);
        double turn = a.turnRate(v);
        if (b.stumbleTicks > 0) {
            b.stumbleTicks--;
            vMax *= 0.5;
            accel *= 0.5;
            turn *= 0.5;
        }
        if (v > 0.1) {
            turn = Math.min(turn, grip / v); // sideways grip: v * omega <= grip * g
        }

        // Heading as a unit vector: StrictMath.sin/cos allocate on this JDK (argument reduction), atan2 does not.
        double hx;
        double hy;
        if (v > 1e-6) {
            hx = b.vx / v;
            hy = b.vy / v;
        } else {
            hx = b.fx;
            hy = b.fy;
        }
        double wanted = Math.sqrt(cmd.targetVx * cmd.targetVx + cmd.targetVy * cmd.targetVy);
        double speedTarget = Math.min(wanted, vMax);
        if (wanted > 1e-9) {
            double dx = cmd.targetVx / wanted;
            double dy = cmd.targetVy / wanted;
            double delta = StrictMath.atan2(hx * dy - hy * dx, hx * dx + hy * dy); // signed angle still to turn
            double maxTurn = Math.min(turn * dt, MAX_TURN_PER_TICK);
            if (delta <= maxTurn && delta >= -maxTurn) {
                hx = dx;
                hy = dy;
            } else {
                double step = delta > 0 ? maxTurn : -maxTurn;
                double c = cosSmall(step);
                double s = sinSmall(step);
                double rx = hx * c - hy * s;
                double ry = hx * s + hy * c;
                double n = Math.sqrt(rx * rx + ry * ry);
                hx = rx / n;
                hy = ry / n;
            }
            speedTarget *= Math.max(0, hx * dx + hy * dy); // brake for what is still to turn
        }
        if (v < speedTarget) {
            double a01 = Math.max(0.15, 1 - v / vMax);
            v = Math.min(speedTarget, v + accel * a01 * dt);
        } else {
            v = Math.max(speedTarget, v - decel * dt);
        }
        if (hx != b.fx || hy != b.fy) {
            b.fx = hx;
            b.fy = hy;
            b.facing = StrictMath.atan2(hy, hx);
        }
        b.vx = v * hx;
        b.vy = v * hy;
        b.x += b.vx * dt;
        b.y += b.vy * dt;
        stamina(a, v, ground, excess);
    }

    private void stamina(AthleteComponent a, double v, BodyParams.SurfaceBody ground, double bootExcess) {
        double rel = v / a.topSpeed;
        double power = rel * rel;
        double fatigue = ground.fatigueFactor * (1 + p.fatiguePerBootKg * bootExcess);
        double aer = p.aerobicPowerShare;
        if (power > aer) {
            a.stamina -= p.sprintDrainPerS * (power - aer) / (1 - aer) * fatigue / a.capacity * dt;
        } else {
            a.stamina += p.recoverPerS * a.recovery * (1 - power / aer) * (0.5 + 0.5 * a.reserve) * dt;
        }
        a.stamina = a.stamina < 0 ? 0 : (a.stamina > 1 ? 1 : a.stamina);
        a.reserve = Math.max(0, a.reserve - p.reserveDrainPerS * power * fatigue / a.recovery * dt);
    }

    /** Largest turn in one tick, rad: keeps the series below exact to ~1e-8 even at low tick rates. */
    private static final double MAX_TURN_PER_TICK = StrictMath.PI / 2;

    /** sin(x) for |x| <= pi/2 by its Taylor series to x^13: plain arithmetic, same bits everywhere, no allocation. */
    static double sinSmall(double x) {
        double x2 = x * x;
        return x * (1 - x2 / 6 * (1 - x2 / 20 * (1 - x2 / 42 * (1 - x2 / 72 * (1 - x2 / 110 * (1 - x2 / 156))))));
    }

    /** cos(x) for |x| <= pi/2 by its Taylor series to x^14. */
    static double cosSmall(double x) {
        double x2 = x * x;
        return 1 - x2 / 2 * (1 - x2 / 12 * (1 - x2 / 30 * (1 - x2 / 56 * (1 - x2 / 90 * (1 - x2 / 132 * (1 - x2
                / 182))))));
    }

    /** Angle in [-pi, pi). */
    static double wrap(double angle) {
        return angle - TWO_PI * Math.floor((angle + StrictMath.PI) / TWO_PI);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
