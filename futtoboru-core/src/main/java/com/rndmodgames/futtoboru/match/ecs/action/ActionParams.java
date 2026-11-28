package com.rndmodgames.futtoboru.match.ecs.action;

import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;

/**
 * Parameters of ball actions (E-14, docs/design/MATCH_ENGINE_ECS.md §4.1): reach, control, kick power, launch
 * planning, input noise, headers, lace strikes, keeper handling and carrying. Plain public fields with defaults (D14:
 * later {@code gamedata/core/engine/physics.json}), part of {@link com.rndmodgames.futtoboru.match.ecs.PhysicsParams#action}.
 *
 * Confidence as in BALL_AND_KIT_PHYSICS.md (BKP): H measured/primary, M one good secondary source, D design estimate
 * (tune in calibration E-20). Almost everything here is D; the laws are M (MATCH_ENGINE_V2.md §1).
 */
public final class ActionParams {

    /** Where a keeper may use his hands (V2 §1: own half 1882/1887-1912, penalty area from 1912). */
    public enum KeeperHandling {
        /** Anywhere in his own half (FA 1882, IFAB 1887; M). */
        OWN_HALF,
        /** Inside his own penalty area (IFAB 1912; M). */
        PENALTY_AREA
    }

    // ---- reach (who can play the ball) -------------------------------------------------------------------------

    /** Foot reach beyond the body circle to the ball's centre, m (D: a stride's lunge). */
    public double footReachM = 0.45;
    /** Highest ball (lowest point) a foot can play, m (D: a high volley). */
    public double footMaxZ = 0.9;
    /** Head reach beyond the body circle, m (D). */
    public double headReachM = 0.30;
    /** Ball height band (lowest point) a standing player heads, m (D: a 1.70 m man). */
    public double headMinZ = 1.30, headMaxZ = 1.80;
    /** Extra height of a jump, worst / best heading rating, m (D). */
    public double jumpMinM = 0.20, jumpMaxM = 0.50;
    /** Keeper hand reach beyond the body circle and highest ball he can handle standing, m (D). */
    public double handReachM = 0.75, handMaxZ = 2.10;
    /** Extra keeper reach in height from a jump, worst / best handling, m (D). */
    public double keeperJumpMinM = 0.20, keeperJumpMaxM = 0.50;
    /** Reach multiplier while stumbling (D). */
    public double stumbleReachFactor = 0.6;

    // ---- control (first touch, dribble touches) ----------------------------------------------------------------

    /** Ball speed relative to the player that a touch fully cushions, worst / best control, m/s (D). */
    public double controlSpeedMin = 5.0, controlSpeedMax = 12.0;
    /** A saturated ball skids off the foot: controllable speed x (1 - this x saturation) (D). */
    public double wetControlPenalty = 0.25;
    /** Speed of a dribble touch ahead of the body at power 0 / 1, m/s added to the body's speed (D). */
    public double dribbleTouchMin = 0.8, dribbleTouchMax = 3.0;

    // ---- kicks (BKP §1.5 impulse model) -------------------------------------------------------------------------

    /**
     * Foot speed of a full swing, worst / best kicking rating, m/s (D: modern instep kicks 18-25 m/s; heavy toe-capped
     * 1888 boots, toe-punt; with the 1.2 kg foot a 400 g ball leaves at ~1.125 x foot speed).
     */
    public double footSpeedMin = 16.0, footSpeedMax = 23.0;
    /** Ground passes are planned to arrive at this speed, m/s (D: a receivable pass). */
    public double passArrivalMps = 3.0;
    /**
     * Share of the true roll drag (beyond firm turf) a kicker allows for when he plans a ground ball (D: the period's
     * complaint that the ball "held up in the mud"; 0 = plans for firm turf, 1 = reads the ground perfectly).
     */
    public double groundReading = 0.5;
    /** Initial launch slope (vz / horizontal speed) of a lofted pass, cross and clearance (D). */
    public double lobLoft = 0.35, crossLoft = 0.55, clearLoft = 0.85;
    /** Shot launch speed at power 0 / 1, share of the kicker's full launch speed (D). */
    public double shotPowerMin = 0.55, shotPowerMax = 1.0;
    /** Iterations of the launch solver (shooting method on the real ball model) and its tolerance, m. */
    public int solverIterations = 8;
    public double solverToleranceM = 0.02;
    /** Longest flight the solver simulates, s. */
    public double solverMaxSeconds = 6.0;

    // ---- input noise (§4.1: noise on inputs only, never dice on results) ----------------------------------------

    /** Global multiplier on all noise (1 = default; 0 for exact-physics tests). */
    public double noiseScale = 1.0;
    /** Direction error (1 sigma) of the best / worst skill, rad (D). */
    public double dirSigmaBest = 0.015, dirSigmaWorst = 0.080;
    /** Elevation error (1 sigma), best / worst, rad (D). */
    public double elevSigmaBest = 0.015, elevSigmaWorst = 0.070;
    /** Relative speed error (1 sigma), best / worst (D). */
    public double speedSigmaBest = 0.03, speedSigmaWorst = 0.12;
    /** Spin error (1 sigma), best / worst, rad/s (D). */
    public double spinSigmaBest = 1.0, spinSigmaWorst = 5.0;
    /** Noise x (1 + gain x factor): fatigue 0..1, pressure 0..1, ball saturation 0..1, awkward angle 0..1 (D). */
    public double fatigueGain = 1.0, pressureGain = 0.8, wetBallGain = 0.6, awkwardGain = 1.0;
    /** Noise x (1 + this) while stumbling (D). */
    public double stumbleGain = 1.0;
    /** An opponent this close (body centres) gives full pressure; none at all beyond, m (D). */
    public double pressureRadiusM = 2.5;

    // ---- headers (BKP §1.5) -------------------------------------------------------------------------------------

    /** Share of the incoming relative speed a header returns, and the neck drive of worst / best heading, m/s (D). */
    public double headRestitution = 0.45;
    public double headDriveMin = 4.0, headDriveMax = 9.0;
    /** The ball has a lace (until the laceless ball, BKP §1.1; M). */
    public boolean laced = true;
    /** Chance a header meets the lace (BKP §3 ball.laceStrikeShare: ~1 of 8 surface sectors; D). */
    public double laceStrikeShare = 0.125;
    /** Chance a lace strike cuts or grazes, dry ball (D: anecdotal only, LFCH; keep rare). */
    public double laceCutChance = 0.02;
    /** Multiplier on the cut chance for a saturated ball (BKP §1.5 "x2 when wet"; D). */
    public double laceCutWetFactor = 2.0;

    // ---- keeper -------------------------------------------------------------------------------------------------

    /** Penalty area for {@link KeeperHandling#PENALTY_AREA} (where the keeper may handle: {@code RulesProfile}): depth and half width, m (1902 area, 18 x 44 yd; H). */
    public double penaltyAreaDepthM = 18 * PitchParams.YARD, penaltyAreaHalfWidthM = 22 * PitchParams.YARD;
    /** Relative speed the keeper holds, worst / best handling, m/s; faster balls are parried (D). */
    public double catchSpeedMin = 12.0, catchSpeedMax = 22.0;
    /** A saturated ball slips: catchable speed x (1 - this x saturation) (D). */
    public double wetCatchPenalty = 0.3;
    /** Share of the relative speed a parried ball keeps, reflected (D). */
    public double parryRestitution = 0.35;
    /** Fist drive of worst / best handling, m/s, plus this share of the incoming relative speed (D). */
    public double punchDriveMin = 7.0, punchDriveMax = 13.0, punchRestitution = 0.3;
    /** Height of the ball in the keeper's hands, m (D). */
    public double holdZ = 1.0;
    /** Steps the keeper may carry the ball (1883 law, V2 §1; M) and the length of a step, m (D). */
    public double maxCarrySteps = 2, stepLengthM = 0.8;
    /** Downward speed of a keeper's bounce (releases the ball and restarts the step count), m/s (D). */
    public double bounceSpeedMps = 3.0;

    // ---- throws -------------------------------------------------------------------------------------------------

    /** Release height of a two-handed throw from above the head (1883 law; M), m (D). */
    public double throwReleaseZ = 2.0;
    /** Throw speed of worst / best technique, m/s (D). */
    public double throwSpeedMin = 10.0, throwSpeedMax = 16.0;

    // ---- commands -----------------------------------------------------------------------------------------------

    /** Ticks an action stays armed waiting for the ball to come into reach (D: half a second at 30 Hz). */
    public int armTicks = 15;

    /** Throws on values the engine cannot run with. Returns this. */
    public ActionParams validate() {
        req(footReachM > 0 && footMaxZ > 0 && headReachM > 0 && headMaxZ > headMinZ && headMinZ > 0, "reach");
        req(jumpMaxM >= jumpMinM && jumpMinM >= 0 && handReachM > 0 && handMaxZ > 0, "jump/hands");
        req(keeperJumpMaxM >= keeperJumpMinM && keeperJumpMinM >= 0, "keeper jump");
        req(stumbleReachFactor > 0 && stumbleReachFactor <= 1, "stumble reach");
        req(controlSpeedMax >= controlSpeedMin && controlSpeedMin > 0, "control");
        req(wetControlPenalty >= 0 && wetControlPenalty < 1 && wetCatchPenalty >= 0 && wetCatchPenalty < 1, "wet");
        req(dribbleTouchMax >= dribbleTouchMin && dribbleTouchMin >= 0, "dribble");
        req(footSpeedMax >= footSpeedMin && footSpeedMin > 0 && passArrivalMps >= 0, "kick");
        req(groundReading >= 0 && groundReading <= 1, "ground reading");
        req(lobLoft > 0 && crossLoft > 0 && clearLoft > 0, "loft");
        req(shotPowerMax >= shotPowerMin && shotPowerMin > 0, "shot power");
        req(solverIterations > 0 && solverToleranceM > 0 && solverMaxSeconds > 0, "solver");
        req(noiseScale >= 0 && dirSigmaBest >= 0 && dirSigmaWorst >= 0 && elevSigmaBest >= 0
                && elevSigmaWorst >= 0 && speedSigmaBest >= 0 && speedSigmaWorst >= 0 && spinSigmaBest >= 0
                && spinSigmaWorst >= 0, "noise");
        req(fatigueGain >= 0 && pressureGain >= 0 && wetBallGain >= 0 && awkwardGain >= 0 && stumbleGain >= 0
                && pressureRadiusM > 0, "noise gains");
        req(headRestitution >= 0 && headDriveMax >= headDriveMin && headDriveMin >= 0, "header");
        req(laceStrikeShare >= 0 && laceStrikeShare <= 1 && laceCutChance >= 0 && laceCutChance <= 1
                && laceCutWetFactor >= 0, "lace");
        req(penaltyAreaDepthM > 0 && penaltyAreaHalfWidthM > 0, "keeper law");
        req(catchSpeedMax >= catchSpeedMin && catchSpeedMin > 0 && parryRestitution >= 0 && parryRestitution <= 1,
                "catch");
        req(punchDriveMax >= punchDriveMin && punchDriveMin >= 0 && punchRestitution >= 0, "punch");
        req(holdZ > 0 && maxCarrySteps >= 0 && stepLengthM > 0 && bounceSpeedMps > 0, "carry");
        req(throwReleaseZ > 0 && throwSpeedMax >= throwSpeedMin && throwSpeedMin > 0, "throw");
        req(armTicks > 0, "arm ticks");
        return this;
    }

    private static void req(boolean ok, String what) {
        if (!ok) {
            throw new IllegalArgumentException("invalid action params: " + what);
        }
    }
}
