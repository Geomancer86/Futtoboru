package com.rndmodgames.futtoboru.match.ecs.body;

/**
 * One body-body impact in the current tick, for the laws (E-16: charge from behind, keeper charge) and event
 * detection (E-17: knock-downs, injuries). Instances are preallocated and reused by {@link ContactSystem}; copy what
 * you keep. Side A is the body created first.
 */
public final class ContactRecord {

    /** Fall outcome of one side. */
    public static final int NONE = 0, STUMBLE = 1, DOWN = 2;

    public long tick;
    /** Creation indices ({@code IdentityComponent.index}) of the two bodies; {@code a < b}. */
    public int a, b;
    /** Impulse along the contact normal, N·s (including charge drive). */
    public double impulseNs;
    /** Contact normal from A to B and the contact point (midway between the surfaces). */
    public double nx, ny, x, y;
    /** Closing speed along the normal before the impact, m/s. */
    public double closingSpeed;
    /** Who put his shoulder in. */
    public boolean aCharged, bCharged;
    /** The other came within {@link BodyParams#fromBehindHalfAngle} of this side's back (1869: charging from behind). */
    public boolean aHitFromBehind, bHitFromBehind;
    /** This side is a goalkeeper (charging the keeper: fair in 1888-89 unless {@link #keeperProtected}). */
    public boolean aKeeper, bKeeper;
    /** {@link BodyParams#keeperChargeProtected} at the time. */
    public boolean keeperProtected;
    /** {@link #NONE}, {@link #STUMBLE} or {@link #DOWN}. */
    public int aFall, bFall;
    /** {@link InjuryComponent} severity from this contact (0 = none). */
    public int aInjury, bInjury;

    /** A charge that the laws of the day would call a foul: from behind, or on a protected keeper. */
    public boolean foulSuspected() {
        return (bCharged && (aHitFromBehind || (aKeeper && keeperProtected)))
                || (aCharged && (bHitFromBehind || (bKeeper && keeperProtected)));
    }

    void clear() {
        tick = 0;
        a = b = 0;
        impulseNs = nx = ny = x = y = closingSpeed = 0;
        aCharged = bCharged = aHitFromBehind = bHitFromBehind = aKeeper = bKeeper = keeperProtected = false;
        aFall = bFall = aInjury = bInjury = 0;
    }
}
