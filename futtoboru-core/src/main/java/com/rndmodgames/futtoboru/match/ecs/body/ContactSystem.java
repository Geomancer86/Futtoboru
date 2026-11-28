package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;

/**
 * Body-body contacts (E-13, §3.3), once per tick after locomotion and the ball.
 *
 * <ul>
 * <li><b>Detection:</b> every pair in creation order (A before B), swept over the tick (from {@code prev} to the
 * current position), so two sprinters closing at 18 m/s cannot pass through each other.</li>
 * <li><b>Impulse:</b> along the normal at the time of impact, restitution {@link BodyParams#bodyRestitution}, mass
 * includes kit, so momentum passes from the heavier and faster man. A {@link MoveCommand#charge} adds a shoulder drive
 * scaled by strength. Overlap is then removed, split by inverse mass.</li>
 * <li><b>Falls:</b> each side's speed change vs his balance: stumble or knock-down for a number of ticks. Deterministic
 * (no dice on the outcome, §4.1).</li>
 * <li><b>Injuries:</b> impulse above the threshold rolls on the {@code body:contact:injury} fork (draws only when the
 * chance is above zero, in pair order), shin pads lower it: KNOCK / PASSENGER / OFF.</li>
 * <li><b>Laws:</b> nothing is judged here. Each impact is written to a reused {@link ContactRecord} with
 * charge-from-behind and keeper flags; charging the keeper is fair in 1888-89 ({@link BodyParams#keeperChargeProtected}
 * false). The laws (E-16) and event detection (E-17) read {@link #contactCount()} / {@link #contact(int)} in the same
 * tick.</li>
 * </ul>
 * Bodies that are off the field take no part. No allocation per tick once the body set is fixed.
 */
public final class ContactSystem extends EntitySystem implements SimSystem {

    /** Impacts kept per tick; more are counted in {@link #droppedContacts()} but not recorded. */
    public static final int MAX_CONTACTS_PER_TICK = 64;

    private final ComponentMapper<BodyComponent> bodyMap = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<AthleteComponent> athleteMap = ComponentMapper.getFor(AthleteComponent.class);
    private final ComponentMapper<KitComponent> kitMap = ComponentMapper.getFor(KitComponent.class);
    private final ComponentMapper<InjuryComponent> injuryMap = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<MoveCommand> commandMap = ComponentMapper.getFor(MoveCommand.class);
    private final ComponentMapper<IdentityComponent> idMap = ComponentMapper.getFor(IdentityComponent.class);

    private final MatchWorld world;
    private final BodyParams p;
    private final RandomSource injuryRandom;
    private final double cosBehind;
    private final ContactRecord[] records = new ContactRecord[MAX_CONTACTS_PER_TICK];
    private int recordCount;
    private long dropped;
    private long totalContacts;

    private ImmutableArray<Entity> entities;
    private int cached = -1;
    private BodyComponent[] body = new BodyComponent[0];
    private AthleteComponent[] athlete = new AthleteComponent[0];
    private KitComponent[] kit = new KitComponent[0];
    private InjuryComponent[] injury = new InjuryComponent[0];
    private MoveCommand[] command = new MoveCommand[0];
    private int[] index = new int[0];

    public ContactSystem(MatchWorld world) {
        super(SystemPriority.CONTACT);
        this.world = world;
        this.p = world.params().body;
        this.injuryRandom = world.random().fork("body:contact:injury");
        this.cosBehind = StrictMath.cos(p.fromBehindHalfAngle);
        for (int i = 0; i < records.length; i++) {
            records[i] = new ContactRecord();
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(LocomotionSystem.family());
    }

    @Override
    public void update(float ignoredFrameDelta) {
        if (entities.size() != cached) {
            cache();
        }
        recordCount = 0;
        int n = cached;
        for (int i = 0; i < n; i++) {
            if (injury[i].isOff()) {
                continue;
            }
            for (int j = i + 1; j < n; j++) {
                if (!injury[j].isOff()) {
                    pair(i, j);
                }
            }
        }
    }

    private void cache() {
        int n = entities.size();
        body = new BodyComponent[n];
        athlete = new AthleteComponent[n];
        kit = new KitComponent[n];
        injury = new InjuryComponent[n];
        command = new MoveCommand[n];
        index = new int[n];
        for (int i = 0; i < n; i++) {
            Entity e = entities.get(i);
            body[i] = bodyMap.get(e);
            athlete[i] = athleteMap.get(e);
            kit[i] = kitMap.get(e);
            injury[i] = injuryMap.get(e);
            command[i] = commandMap.get(e);
            IdentityComponent id = idMap.get(e);
            index[i] = id == null ? i : id.index;
        }
        cached = n;
    }

    private void pair(int i, int j) {
        BodyComponent a = body[i];
        BodyComponent b = body[j];
        double r = a.radius + b.radius;
        double dx0 = b.prevX - a.prevX;
        double dy0 = b.prevY - a.prevY;
        double dx1 = b.x - a.x;
        double dy1 = b.y - a.y;
        double ex = dx1 - dx0;
        double ey = dy1 - dy0;
        double rr = r * r;
        double ee = ex * ex + ey * ey;
        double d0e = dx0 * ex + dy0 * ey;
        double d00 = dx0 * dx0 + dy0 * dy0;
        if (dx1 * dx1 + dy1 * dy1 >= rr) {
            // apart at the end: did they meet during the tick?
            if (ee <= 0) {
                return;
            }
            double t = -d0e / ee;
            t = t < 0 ? 0 : (t > 1 ? 1 : t);
            double mx = dx0 + ex * t;
            double my = dy0 + ey * t;
            if (mx * mx + my * my >= rr) {
                return;
            }
        }
        // contact normal at the time of impact
        double nx;
        double ny;
        if (d00 >= rr && ee > 0) {
            double disc = d0e * d0e - ee * (d00 - rr);
            double t = (-d0e - Math.sqrt(Math.max(0, disc))) / ee;
            nx = dx0 + ex * t;
            ny = dy0 + ey * t;
        } else {
            nx = dx1;
            ny = dy1;
            if (nx * nx + ny * ny < 1e-18) {
                nx = dx0;
                ny = dy0;
            }
        }
        double len = Math.sqrt(nx * nx + ny * ny);
        if (len < 1e-9) {
            nx = 1; // same spot: push along +x, A west of B (deterministic)
            ny = 0;
        } else {
            nx /= len;
            ny /= len;
        }

        double invA = 1 / a.massKg;
        double invB = 1 / b.massKg;
        double vn = (b.vx - a.vx) * nx + (b.vy - a.vy) * ny;
        if (vn < 0) {
            double impulse = -(1 + p.bodyRestitution) * vn / (invA + invB);
            boolean aCharges = command[i].charge && !a.isDown() && a.vx * nx + a.vy * ny > 0.5;
            boolean bCharges = command[j].charge && !b.isDown() && -(b.vx * nx + b.vy * ny) > 0.5;
            if (aCharges) {
                impulse += p.chargeDriveNs * (0.5 + athlete[i].strength01);
            }
            if (bCharges) {
                impulse += p.chargeDriveNs * (0.5 + athlete[j].strength01);
            }
            a.vx -= impulse * invA * nx;
            a.vy -= impulse * invA * ny;
            b.vx += impulse * invB * nx;
            b.vy += impulse * invB * ny;
            impact(i, j, nx, ny, -vn, impulse, aCharges, bCharges);
        }
        // remove the overlap at the end of the tick, along the normal, split by inverse mass
        double pen = r - (dx1 * nx + dy1 * ny);
        if (pen > 0) {
            double shareA = invA / (invA + invB);
            a.x -= nx * pen * shareA;
            a.y -= ny * pen * shareA;
            b.x += nx * pen * (1 - shareA);
            b.y += ny * pen * (1 - shareA);
        }
    }

    private void impact(int i, int j, double nx, double ny, double closing, double impulse, boolean aCharges,
            boolean bCharges) {
        BodyComponent a = body[i];
        BodyComponent b = body[j];
        totalContacts++;
        int aFall = fall(a, athlete[i], impulse / a.massKg);
        int bFall = fall(b, athlete[j], impulse / b.massKg);
        int aInjury = injure(i, impulse);
        int bInjury = injure(j, impulse);
        if (recordCount == records.length) {
            dropped++;
            return;
        }
        ContactRecord c = records[recordCount++];
        c.clear();
        c.tick = world.tickCount();
        c.a = index[i];
        c.b = index[j];
        c.impulseNs = impulse;
        c.nx = nx;
        c.ny = ny;
        c.x = a.x + nx * a.radius;
        c.y = a.y + ny * a.radius;
        c.closingSpeed = closing;
        c.aCharged = aCharges;
        c.bCharged = bCharges;
        // A is hit from behind when B (direction +n from A) is behind A's facing; B likewise with -n.
        c.aHitFromBehind = a.fx * nx + a.fy * ny < -cosBehind;
        c.bHitFromBehind = -(b.fx * nx + b.fy * ny) < -cosBehind;
        c.aKeeper = athlete[i].isKeeper();
        c.bKeeper = athlete[j].isKeeper();
        c.keeperProtected = p.keeperChargeProtected;
        c.aFall = aFall;
        c.bFall = bFall;
        c.aInjury = aInjury;
        c.bInjury = bInjury;
    }

    /** Stumble or knock-down from a speed change {@code dv}; thresholds scale with balance. */
    private int fall(BodyComponent body, AthleteComponent athlete, double dv) {
        if (body.isDown()) {
            return ContactRecord.NONE;
        }
        double scale = 1 + p.balanceSpread * (athlete.balance01 - 0.5);
        double knock = p.knockDownDeltaV * scale;
        if (dv >= knock) {
            body.downTicks = p.knockDownTicks + (int) (p.knockDownTicksPerDeltaV * (dv - knock));
            body.stumbleTicks = 0;
            return ContactRecord.DOWN;
        }
        if (dv >= p.stumbleDeltaV * scale) {
            body.stumbleTicks = Math.max(body.stumbleTicks, p.stumbleTicks);
            return ContactRecord.STUMBLE;
        }
        return ContactRecord.NONE;
    }

    private int injure(int i, double impulse) {
        double risk = InjuryModel.risk(impulse, athlete[i].fragility, kit[i].shinPads, p);
        if (risk <= 0 || injuryRandom.nextDouble() >= risk) {
            return 0;
        }
        int severity = InjuryModel.severity(injuryRandom.nextDouble(), impulse, p);
        injury[i].injure(severity, impulse, p);
        if (injury[i].isOff()) {
            body[i].vx = 0;
            body[i].vy = 0;
        }
        return severity;
    }

    // ---- results of this tick ---------------------------------------------------------------------------------------

    /** Impacts recorded in the current tick. */
    public int contactCount() {
        return recordCount;
    }

    /** The k-th impact of this tick (reused next tick). */
    public ContactRecord contact(int k) {
        if (k < 0 || k >= recordCount) {
            throw new IndexOutOfBoundsException(k);
        }
        return records[k];
    }

    /** Impacts beyond {@link #MAX_CONTACTS_PER_TICK} so far (not recorded). */
    public long droppedContacts() {
        return dropped;
    }

    /** Impacts so far this match. */
    public long totalContacts() {
        return totalContacts;
    }
}
