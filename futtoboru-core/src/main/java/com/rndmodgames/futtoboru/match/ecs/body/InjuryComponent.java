package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * Injury state from contacts (D19-3): a knock (reduced limits for a while), a passenger (reduced limits for the rest
 * of the match) or off (leaves the field: no substitutes in 1888-89, D7; return later is E-16/E-17's call). The worst
 * state wins; a knock on a passenger does not heal him.
 */
public final class InjuryComponent implements Component, HashedComponent {

    public static final int NONE = 0;
    public static final int KNOCK = 1;
    public static final int PASSENGER = 2;
    public static final int OFF = 3;

    /** {@link #NONE}, {@link #KNOCK}, {@link #PASSENGER} or {@link #OFF}. */
    public int state = NONE;
    /** Ticks left of a knock. */
    public int knockTicksLeft;
    /** Injuries suffered this match. */
    public int count;
    /** Contact impulse of the last injury, N·s. */
    public double lastImpulseNs;

    /** Applies a new injury of {@code severity} (KNOCK..OFF). */
    public void injure(int severity, double impulseNs, BodyParams p) {
        count++;
        lastImpulseNs = impulseNs;
        if (severity == KNOCK) {
            if (state <= KNOCK) {
                state = KNOCK;
                knockTicksLeft = p.knockTicks;
            }
        } else if (severity > state) {
            state = severity;
            knockTicksLeft = 0;
        }
    }

    /** One tick: a knock wears off. */
    public void tick() {
        if (state == KNOCK && --knockTicksLeft <= 0) {
            knockTicksLeft = 0;
            state = NONE;
        }
    }

    public boolean isOff() {
        return state == OFF;
    }

    /** Multiplier on speed and acceleration limits. */
    public double limitFactor(BodyParams p) {
        switch (state) {
            case KNOCK:
                return p.knockedLimitFactor;
            case PASSENGER:
                return p.passengerLimitFactor;
            case OFF:
                return 0;
            default:
                return 1;
        }
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("inj.state", state);
        h.mix("inj.knock", knockTicksLeft);
        h.mix("inj.count", count);
        h.mix("inj.lastImpulse", lastImpulseNs);
    }
}
