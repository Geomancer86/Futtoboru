package com.rndmodgames.futtoboru.match.ecs.ball;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.BallSegment;
import com.rndmodgames.futtoboru.match.ecs.pitch.FrameContact;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Vec3;

/**
 * Ball system (E-11): per tick, soaks every ball by the pitch's wetness, then integrates it in
 * {@link PhysicsParams#ballSubsteps} substeps (30 Hz x 2 = 60 Hz by default, D22). Kicks come from the action
 * system (E-14) through {@link #physics()}. A ball a keeper holds ({@link PossessionComponent#held}) still soaks but
 * does not fly: the action system keeps it in his hands.
 *
 * <p><b>Goal frames (E-16b).</b> After each substep the path of the ball centre is swept against the posts and bars
 * ({@link PitchGeometry#frameContact}, only near a goal: {@link PitchGeometry#nearFrame}). On a contact the ball is put
 * back where it touched, its velocity along the contact normal is reflected with the rigid restitution
 * ({@link PhysicsParams#ballRestitutionRigid}, scaled down for a wet ball like a ground bounce:
 * {@code v - (1 + e)(v.n)n}) and the rest of the substep is integrated from there (at most
 * {@link #MAX_FRAME_CONTACTS} contacts per substep). The laws still judge line crossings:
 * {@link #frameContactThisTick()} tells them to sweep this tick's path from the last contact. Allocation only near a
 * goal frame (the swept test builds its records).
 */
public final class BallSystem extends EntitySystem implements SimSystem {

    /** Contacts with the frame handled within one substep (a ball wedged in the post-bar corner stops there). */
    public static final int MAX_FRAME_CONTACTS = 3;

    private final ComponentMapper<BallComponent> balls = ComponentMapper.getFor(BallComponent.class);
    private final ComponentMapper<PossessionComponent> possession = ComponentMapper.getFor(PossessionComponent.class);
    private final MatchWorld world;
    private final PhysicsParams params;
    private final BallPhysics physics;
    private final int substeps;
    private final double tickSeconds;
    private final double substepSeconds;
    private ImmutableArray<Entity> entities;
    private PitchGeometry geometry = new PitchGeometry(PitchParams.defaults1888());
    private boolean contactThisTick;
    private double contactX, contactY, contactZ;
    private long frameContacts;

    public BallSystem(MatchWorld world) {
        super(SystemPriority.BALL);
        this.world = world;
        this.params = world.params();
        this.physics = new BallPhysics(params);
        this.substeps = params.ballSubsteps;
        this.tickSeconds = world.tickSeconds();
        this.substepSeconds = tickSeconds / substeps;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(BallComponent.class).get());
    }

    @Override
    public void update(float ignoredFrameDelta) {
        PitchConditions pitch = world.pitch();
        double wetness = pitch.wetness();
        contactThisTick = false;
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            BallComponent b = balls.get(e);
            physics.soak(b, wetness, tickSeconds);
            PossessionComponent held = possession.get(e);
            if (held != null && held.held) {
                continue;
            }
            for (int k = 0; k < substeps; k++) {
                substep(b, pitch, substepSeconds);
            }
        }
    }

    /** One substep: integrate, then bounce off the goal frame if the path touched it and integrate the rest. */
    private void substep(BallComponent b, PitchConditions conditions, double dt) {
        double r = params.ballRadiusM;
        double left = dt;
        for (int c = 0; c <= MAX_FRAME_CONTACTS && left > 0; c++) {
            double x0 = b.x;
            double y0 = b.y;
            double z0 = b.z + r;
            double vx0 = b.vx;
            double vy0 = b.vy;
            double vz0 = b.vz;
            physics.step(b, params.surface(conditions.surfaceAt(b.x, b.y)), left);
            if (c == MAX_FRAME_CONTACTS || !geometry.nearFrame(x0, y0, z0, b.x, b.y, b.z + r, r)) {
                return;
            }
            FrameContact hit = geometry.frameContact(BallSegment.of(x0, y0, z0, b.x, b.y, b.z + r, r));
            if (hit == null) {
                return;
            }
            double t = hit.t();
            // Velocity at the contact: linear between the start and the end of the step (exact under constant
            // acceleration, close enough with drag).
            double vx = vx0 + (b.vx - vx0) * t;
            double vy = vy0 + (b.vy - vy0) * t;
            double vz = vz0 + (b.vz - vz0) * t;
            Vec3 n = hit.normal();
            double vn = vx * n.x() + vy * n.y() + vz * n.z();
            if (vn < 0) {
                double e = params.ballRestitutionRigid
                        * (1.0 - (1.0 - params.ballWetRestitutionScale) * b.saturation);
                double k = (1.0 + e) * vn;
                vx -= k * n.x();
                vy -= k * n.y();
                vz -= k * n.z();
            }
            Vec3 at = hit.ballCentre();
            b.x = at.x();
            b.y = at.y();
            b.z = Math.max(0, at.z() - r);
            b.vx = vx;
            b.vy = vy;
            b.vz = vz;
            if (b.z > 0 || b.vz > 0) {
                b.state = BallComponent.AIR;
            } else {
                b.vz = 0;
                b.state = b.vx != 0 || b.vy != 0 ? BallComponent.ROLLING : BallComponent.REST;
            }
            contactThisTick = true;
            contactX = b.x;
            contactY = b.y;
            contactZ = b.z;
            frameContacts++;
            left *= 1 - t;
        }
    }

    public BallPhysics physics() {
        return physics;
    }

    /** The ground whose goal frames the ball bounces off. Default: 110 x 70 yd, 1888-89. */
    public void setPitch(PitchGeometry pitch) {
        if (pitch == null) {
            throw new IllegalArgumentException("pitch");
        }
        this.geometry = pitch;
    }

    /** True if the ball touched a post or the bar during this tick (the laws then sweep from {@link #contactX()}). */
    public boolean frameContactThisTick() {
        return contactThisTick;
    }

    /** Ball position (z = lowest point, as in {@link BallComponent}) at the last frame contact of this tick. */
    public double contactX() {
        return contactX;
    }

    public double contactY() {
        return contactY;
    }

    public double contactZ() {
        return contactZ;
    }

    /** Frame contacts (posts and bars) since the start, for stats and the report. */
    public long frameContacts() {
        return frameContacts;
    }
}
