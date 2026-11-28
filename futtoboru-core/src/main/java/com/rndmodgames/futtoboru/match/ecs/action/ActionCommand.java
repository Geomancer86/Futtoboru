package com.rndmodgames.futtoboru.match.ecs.action;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * What a player wants to do with the ball (E-14): an {@link ActionIntent}, a target point and height, power and
 * spin. Written by the AI (E-15) or tests with {@link #set}; consumed by {@link ActionSystem}, which executes it once
 * the ball is in reach (it stays armed for {@code armTicks}) and writes {@link #result}.
 */
public final class ActionCommand implements Component, HashedComponent {

    /** No command yet. */
    public static final int RESULT_NONE = 0;
    /** Armed, waiting for the ball to come into reach. */
    public static final int RESULT_PENDING = 1;
    /** Executed: the ball was played. */
    public static final int RESULT_DONE = 2;
    /** The ball never came into reach while armed. */
    public static final int RESULT_EXPIRED = 3;
    /** Refused: hands where the season's law forbids them, or hands by an outfield player. */
    public static final int RESULT_ILLEGAL_HANDLING = 4;
    /** Someone played the ball first this tick, or a keeper holds it. */
    public static final int RESULT_CONTESTED = 5;

    public ActionIntent intent = ActionIntent.NONE;
    /** Target point on the pitch and height above the ground, m (axes as the ball). */
    public double targetX, targetY, targetZ;
    /** 0..1: shot pace, dribble touch length, throw effort. */
    public double power;
    /** Wanted side spin, rad/s (positive curls left), for curled passes, crosses and shots. */
    public double spin;
    /** Ticks the command stays armed. */
    public int armTicksLeft;
    /** {@code RESULT_*} of the last command. */
    public int result = RESULT_NONE;
    /** Tick of the last result change. */
    public long resultTick;

    /** Arms an action for {@code armTicks} ticks (use {@code ActionParams.armTicks} for the default). */
    public ActionCommand set(ActionIntent intent, double targetX, double targetY, double targetZ, double power,
            int armTicks) {
        if (intent == null) {
            throw new IllegalArgumentException("intent");
        }
        this.intent = intent;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.power = power < 0 ? 0 : (power > 1 ? 1 : power);
        this.spin = 0;
        this.armTicksLeft = intent == ActionIntent.NONE ? 0 : armTicks;
        this.result = intent == ActionIntent.NONE ? RESULT_NONE : RESULT_PENDING;
        return this;
    }

    public ActionCommand withSpin(double spinRadS) {
        this.spin = spinRadS;
        return this;
    }

    public ActionCommand cancel() {
        intent = ActionIntent.NONE;
        armTicksLeft = 0;
        return this;
    }

    public boolean armed() {
        return intent != ActionIntent.NONE;
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("act.intent", intent.ordinal());
        h.mix("act.tx", targetX);
        h.mix("act.ty", targetY);
        h.mix("act.tz", targetZ);
        h.mix("act.power", power);
        h.mix("act.spin", spin);
        h.mix("act.arm", armTicksLeft);
        h.mix("act.result", result);
        h.mix("act.resultTick", resultTick);
    }
}
