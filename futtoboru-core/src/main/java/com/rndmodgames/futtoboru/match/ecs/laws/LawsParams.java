package com.rndmodgames.futtoboru.match.ecs.laws;

/**
 * Tuning of the officials and appeals (E-16, docs/design/MATCH_ENGINE_ECS.md §4.2, MATCH_ENGINE_V2.md §2.4). Plain
 * public fields with defaults (D14: later {@code gamedata/core/engine/match_engine.json}), part of
 * {@link com.rndmodgames.futtoboru.match.ecs.PhysicsParams#laws}. The Laws themselves are in {@link RulesProfile}.
 *
 * Everything here is D (design estimate, tune in calibration E-20): how umpires split the work and how often they
 * favoured their own club is anecdotal only (V2 §1 "Uncertain").
 */
public final class LawsParams {

    /** Where the two club umpires stand (open question Q7: a parameter; no sourced default yet). */
    public enum UmpireDeployment {
        /** Each umpire in one half (the half his club defends), near his touchline, level with play. */
        EACH_HALF,
        /** Each umpire along one touchline over the whole length, level with the ball. */
        FLANK,
        /** Both follow play from a few yards away, on opposite sides of it. */
        FOLLOW
    }

    // ---- officials -------------------------------------------------------------------------------------------------

    /** Placeholder until lane T researches it (D). */
    public UmpireDeployment umpireDeployment = UmpireDeployment.EACH_HALF;
    /** How far the umpires stand inside their touchline, m (on the field in 1888-89; D). */
    public double umpireInsetM = 3.0;
    /** How far the referee (off the field until 1891) and linesmen (from 1891) stand outside the touchline, m (D). */
    public double offFieldOffsetM = 1.5;
    /** How far the off-field referee walks along the touchline either side of halfway, m (D). */
    public double refereeRoamM = 15.0;
    /** Share of the ball's x the off-field referee follows (D). */
    public double refereeFollowShare = 0.3;
    /** On-field referee (1891+): distance kept from the ball across the pitch, m (D). */
    public double refereeOnFieldGapM = 12.0;
    /** FOLLOW deployment: distance kept from the ball along and across the pitch, m (D). */
    public double followBackM = 8.0, followSideM = 12.0;
    /** Top speeds (umpires jog, the referee walks), m/s, and acceleration, m/s^2 (D). */
    public double umpireSpeedMps = 5.0, refereeSpeedMps = 3.0, officialAccel = 3.0;
    /** Desired speed per metre from the target (slows down as he arrives), 1/s (D). */
    public double officialArriveGain = 0.8;

    /** Lean toward his own club, 0 = neutral .. 1 = strongly partisan (D19-2; D). */
    public double umpirePartisanship = 0.5;
    /** Perception noise multiplier of an umpire / the referee, 1 = average (the referee is more experienced; D). */
    public double umpirePerception = 1.0, refereePerception = 0.7;

    // ---- judgement noise and lean per appeal (evidence units in brackets) ------------------------------------------

    /** Offside [metres beyond the line]: noise base, per metre of distance, per metre off the line; lean at 1. */
    public double offsideSigmaBaseM = 0.4, offsideSigmaPerM = 0.03, offsideSigmaPerLineM = 0.05, offsideLeanM = 1.5;
    /** Fouls and handball [+1 clear foul, -1 clearly fair]: noise base and per metre; lean at partisanship 1. */
    public double foulSigmaBase = 0.5, foulSigmaPerM = 0.02, foulLean = 0.8;
    /** Disputed goal [metres the ball was inside (+) or outside (-) the frame]: noise base and per metre; lean. */
    public double goalSigmaBaseM = 0.05, goalSigmaPerM = 0.004, goalLeanM = 0.15;
    /** Carrying [steps over the limit, minus a tolerance]: noise, tolerance and lean, steps. */
    public double carrySigmaSteps = 0.5, carryToleranceSteps = 0.5, carryLeanSteps = 0.6;

    // ---- appeals (how often the wronged side shouts "How's that?") -------------------------------------------------

    /** Offside: appeal on a real offside, and on a near miss within {@link #offsideNearMarginM} (scaled down). */
    public double appealOffside = 0.9, appealNearOffside = 0.3, offsideNearMarginM = 1.5;
    /** Fouls: appeal on a charge from behind / on a protected keeper, and on a fair charge that floored the man. */
    public double appealFoul = 0.85, appealFairChargeDown = 0.15;
    /** Handball by a player who reached for the ball where hands are not allowed. */
    public double appealHandball = 0.95;
    /** A keeper forced to let the ball go at the carrying limit. */
    public double appealCarry = 0.3;

    // ---- sendings-off (D19-4: very rare) ---------------------------------------------------------------------------

    /** Impulse above which a foul given counts as violent, N·s, and the chance it sends the offender off. */
    public double violentImpulseNs = 280, sendOffChance = 0.03;

    // ---- restarts ---------------------------------------------------------------------------------------------------

    /** Ticks of dead ball before a restart may be taken, s: throw-ins, corners, goal and free kicks (D). */
    public double restartDelaySeconds = 3.0;
    /** Before a kick-off after a goal or at half-time, s (D). */
    public double kickOffDelaySeconds = 10.0;

    /** Throws on values the engine cannot run with. Returns this. */
    public LawsParams validate() {
        req(umpireDeployment != null, "umpire deployment");
        req(umpireInsetM >= 0 && offFieldOffsetM >= 0 && refereeRoamM >= 0 && refereeOnFieldGapM >= 0, "placement");
        req(umpireSpeedMps > 0 && refereeSpeedMps > 0 && officialAccel > 0 && officialArriveGain > 0, "movement");
        req(umpirePartisanship >= 0 && umpirePartisanship <= 1, "partisanship");
        req(umpirePerception >= 0 && refereePerception >= 0, "perception");
        req(offsideSigmaBaseM >= 0 && offsideSigmaPerM >= 0 && offsideSigmaPerLineM >= 0 && offsideLeanM >= 0,
                "offside noise");
        req(foulSigmaBase >= 0 && foulSigmaPerM >= 0 && foulLean >= 0, "foul noise");
        req(goalSigmaBaseM >= 0 && goalSigmaPerM >= 0 && goalLeanM >= 0, "goal noise");
        req(carrySigmaSteps >= 0 && carryLeanSteps >= 0, "carry noise");
        req(p(appealOffside) && p(appealNearOffside) && p(appealFoul) && p(appealFairChargeDown)
                && p(appealHandball) && p(appealCarry) && p(sendOffChance) && offsideNearMarginM > 0, "appeals");
        req(restartDelaySeconds >= 0 && kickOffDelaySeconds >= 0, "restart delays");
        return this;
    }

    private static boolean p(double v) {
        return v >= 0 && v <= 1;
    }

    private static void req(boolean ok, String what) {
        if (!ok) {
            throw new IllegalArgumentException("invalid laws params: " + what);
        }
    }
}
