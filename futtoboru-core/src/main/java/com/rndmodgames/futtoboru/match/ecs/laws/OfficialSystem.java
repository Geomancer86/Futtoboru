package com.rndmodgames.futtoboru.match.ecs.laws;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;

/**
 * Moves the officials (E-16, priority OFFICIALS, before locomotion): simple steering toward a target that keeps a
 * view of play, capped speed and acceleration, slowing as they arrive. No trig, no allocation.
 *
 * <ul>
 * <li><b>Umpires (until 1891), on the field:</b> by {@link LawsParams#umpireDeployment}: EACH_HALF level with the ball
 * but inside the half their club defends, FLANK level with the ball along the whole touchline, FOLLOW a few yards
 * behind and beside play. Home umpire on the south side, away umpire on the north side, {@link LawsParams#umpireInsetM}
 * inside the touchline.</li>
 * <li><b>Referee (until 1891), off the field:</b> just outside the south touchline near halfway, following the ball a
 * little ({@link LawsParams#refereeRoamM}).</li>
 * <li><b>From 1891</b> ({@link RulesProfile#refereeOnField()}): the referee follows play on the field, the umpires
 * become linesmen just outside their touchline, each in the half his club defends.</li>
 * </ul>
 */
public final class OfficialSystem extends EntitySystem implements SimSystem {

    private final ComponentMapper<OfficialComponent> officialMap = ComponentMapper.getFor(OfficialComponent.class);
    private final ComponentMapper<BallComponent> ballMap = ComponentMapper.getFor(BallComponent.class);
    private final ComponentMapper<PlayStateComponent> stateMap = ComponentMapper.getFor(PlayStateComponent.class);
    private final MatchWorld world;
    private final LawsParams p;
    private final RulesProfile rules;
    private PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
    private ImmutableArray<Entity> officials;
    private ImmutableArray<Entity> balls;
    private double tx, ty;

    public OfficialSystem(MatchWorld world) {
        super(SystemPriority.OFFICIALS);
        this.world = world;
        this.p = world.params().laws;
        this.rules = world.params().rules;
    }

    @Override
    public void addedToEngine(Engine engine) {
        officials = engine.getEntitiesFor(Family.all(OfficialComponent.class).get());
        balls = engine.getEntitiesFor(Family.all(BallComponent.class).get());
    }

    public void setPitch(PitchGeometry pitch) {
        if (pitch == null) {
            throw new IllegalArgumentException("pitch");
        }
        this.pitch = pitch;
    }

    @Override
    public void update(float ignoredFrameDelta) {
        if (officials.size() == 0) {
            return;
        }
        double bx = 0;
        double by = 0;
        int west = 0;
        if (balls.size() > 0) {
            Entity be = balls.get(0);
            BallComponent b = ballMap.get(be);
            bx = b.x;
            by = b.y;
            PlayStateComponent s = stateMap.get(be);
            if (s != null) {
                west = s.westDefender;
            }
        }
        double dt = world.tickSeconds();
        for (int i = 0; i < officials.size(); i++) {
            OfficialComponent o = officialMap.get(officials.get(i));
            target(o, bx, by, west);
            steer(o, o.isReferee() ? p.refereeSpeedMps : p.umpireSpeedMps, dt);
        }
    }

    /** Where {@code o} wants to stand with the ball at (bx, by); written to {@link #tx}, {@link #ty}. */
    private void target(OfficialComponent o, double bx, double by, int westDefender) {
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        if (o.isReferee()) {
            if (rules.refereeOnField()) {
                tx = clamp(bx, -hl + 2, hl - 2);
                double side = by >= 0 ? -1 : 1;
                ty = clamp(by + side * p.refereeOnFieldGapM, -hw + 1, hw - 1);
            } else {
                tx = clamp(bx * p.refereeFollowShare, -p.refereeRoamM, p.refereeRoamM);
                ty = -(hw + p.offFieldOffsetM);
            }
            return;
        }
        double flank = o.club == 0 ? -1 : 1;
        // sign of the half his club defends: the west defender's half is x < 0
        double own = o.club == westDefender ? -1 : 1;
        if (rules.refereeOnField()) { // linesman
            tx = own < 0 ? clamp(bx, -hl, 0) : clamp(bx, 0, hl);
            ty = flank * (hw + p.offFieldOffsetM);
            return;
        }
        ty = flank * (hw - p.umpireInsetM);
        switch (p.umpireDeployment) {
            case FLANK:
                tx = clamp(bx, -hl + 2, hl - 2);
                break;
            case FOLLOW:
                tx = clamp(bx + own * p.followBackM, -hl + 2, hl - 2);
                ty = clamp(by + flank * p.followSideM, -hw + 1, hw - 1);
                break;
            default: // EACH_HALF
                tx = own < 0 ? clamp(bx, -hl + 2, -2) : clamp(bx, 2, hl - 2);
                break;
        }
    }

    private void steer(OfficialComponent o, double maxSpeed, double dt) {
        double dx = tx - o.x;
        double dy = ty - o.y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double wantVx = 0;
        double wantVy = 0;
        if (d > 1e-6) {
            double speed = Math.min(maxSpeed, d * p.officialArriveGain);
            wantVx = dx / d * speed;
            wantVy = dy / d * speed;
        }
        double ax = wantVx - o.vx;
        double ay = wantVy - o.vy;
        double a = Math.sqrt(ax * ax + ay * ay);
        double maxDv = p.officialAccel * dt;
        if (a > maxDv) {
            ax *= maxDv / a;
            ay *= maxDv / a;
        }
        o.vx += ax;
        o.vy += ay;
        o.x += o.vx * dt;
        o.y += o.vy * dt;
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
