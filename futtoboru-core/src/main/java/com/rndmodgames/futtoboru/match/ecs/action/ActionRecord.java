package com.rndmodgames.futtoboru.match.ecs.action;

/**
 * One ball action in the current tick (E-14), for the laws (E-16: who played the ball, handling, carrying) and
 * event detection (E-17: passes, shots, headers, saves, lace cuts). Instances are preallocated and reused by
 * {@link ActionSystem}; copy what you keep.
 */
public final class ActionRecord {

    /** Outcomes. */
    public static final int KICKED = 1, TOUCHED = 2, HEADED = 3, CAUGHT = 4, PARRIED = 5, PUNCHED = 6, THROWN = 7,
            BOUNCED = 8, RELEASED_CARRY_LIMIT = 9, DROPPED_KNOCKED_DOWN = 10;

    public long tick;
    /** Creation index of the player and his {@code Team.ordinal()}. */
    public int actor, team;
    public ActionIntent intent = ActionIntent.NONE;
    /** One of the outcome constants. */
    public int outcome;
    /** Ball state before and right after the action (before this tick's flight). */
    public double beforeX, beforeY, beforeZ, beforeVx, beforeVy, beforeVz;
    public double afterX, afterY, afterZ, afterVx, afterVy, afterVz, afterSpin;
    /** The command's target. */
    public double targetX, targetY, targetZ;
    /** Foot speed of a kick, m/s (0 otherwise). */
    public double footSpeed;
    /** Impulse given to the ball, N·s (magnitude). */
    public double impulseNs;
    /** Ball speed relative to the player when he met it, m/s. */
    public double incomingSpeed;
    /** The noise multiplier that applied besides skill: fatigue x pressure x wet ball x angle x stumble. */
    public double noiseFactor;
    /** Header: the lace met the head; it cut (the player took a knock). */
    public boolean laceStrike, laceCut;
    /** The player used his hands as keeper (catch, parry, punch, carry, keeper's throw). */
    public boolean handled;
    /** Steps carried when a keeper released the ball. */
    public double carrySteps;

    void clear() {
        tick = 0;
        actor = team = outcome = 0;
        intent = ActionIntent.NONE;
        beforeX = beforeY = beforeZ = beforeVx = beforeVy = beforeVz = 0;
        afterX = afterY = afterZ = afterVx = afterVy = afterVz = afterSpin = 0;
        targetX = targetY = targetZ = footSpeed = impulseNs = incomingSpeed = noiseFactor = carrySteps = 0;
        laceStrike = laceCut = handled = false;
    }
}
