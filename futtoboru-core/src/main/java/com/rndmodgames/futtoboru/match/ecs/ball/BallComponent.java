package com.rndmodgames.futtoboru.match.ecs.ball;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * Simulation state of the ball (2.5D, SI units, doubles). Axes: x along the pitch, y across, z up; origin at the
 * centre spot on the ground; z is the height of the ball's lowest point (0 = on the ground).
 */
public final class BallComponent implements Component, HashedComponent {

    /** In the air (flying or bouncing). */
    public static final int AIR = 0;
    /** On the ground, moving. */
    public static final int ROLLING = 1;
    /** On the ground, still. */
    public static final int REST = 2;

    public double x, y, z;
    public double vx, vy, vz;
    /** Spin about the vertical axis, rad/s; positive = counter-clockwise seen from above, curls the ball left. */
    public double spin;
    /** Mass at kick-off before this match's soaking (dry mass times 1 + carry-over water), kg. */
    public double startMassKg;
    /** Minutes of effective submersion so far (integral of wetness over time), BKP §1.3 "E". */
    public double exposureMin;
    /** Current mass, kg (derived from start mass and exposure once per tick). */
    public double massKg;
    /** Share of the water ceiling taken up, 0..1 (derived with the mass). */
    public double saturation;
    /** {@link #AIR}, {@link #ROLLING} or {@link #REST}. */
    public int state = REST;
    /** Ground contacts from the air so far (bounces and landings), for event detection (E-17) and effects. */
    public int groundContacts;
    /** Where the last ground contact happened. */
    public double contactX, contactY;

    /** A ball at rest on the ground at (x, y). */
    public static BallComponent atRest(double x, double y, double startMassKg) {
        BallComponent b = new BallComponent();
        b.x = x;
        b.y = y;
        b.startMassKg = startMassKg;
        b.massKg = startMassKg;
        return b;
    }

    public double speed() {
        return Math.sqrt(vx * vx + vy * vy + vz * vz);
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("ball.x", x);
        h.mix("ball.y", y);
        h.mix("ball.z", z);
        h.mix("ball.vx", vx);
        h.mix("ball.vy", vy);
        h.mix("ball.vz", vz);
        h.mix("ball.spin", spin);
        h.mix("ball.startMass", startMassKg);
        h.mix("ball.exposure", exposureMin);
        h.mix("ball.mass", massKg);
        h.mix("ball.saturation", saturation);
        h.mix("ball.state", state);
        h.mix("ball.groundContacts", groundContacts);
        h.mix("ball.contactX", contactX);
        h.mix("ball.contactY", contactY);
    }
}
