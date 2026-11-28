package com.rndmodgames.futtoboru.match.ecs.ai;

/**
 * Parameters of the player AI (E-15, docs/design/MATCH_ENGINE_ECS.md §4.1): perception noise and delay, interception
 * guesses, utility weights by club style, keeper release. Plain public fields with defaults (D14:
 * later {@code gamedata/core/engine/match_engine.json}), part of
 * {@link com.rndmodgames.futtoboru.match.ecs.PhysicsParams#ai}. Everything here is D (design estimate, tuned by the
 * calibration E-20).
 */
public final class AiParams {

    // ---- perception ----------------------------------------------------------------------------------------------

    /** What a player acts on is this many ticks old (reaction time: 4 ticks = 133 ms at 30 Hz). */
    public int reactionTicks = 4;
    /** Position noise (1 sigma) on the ball: base and growth per metre of distance, m. */
    public double ballSigmaBaseM = 0.10, ballSigmaPerM = 0.015;
    /** Position noise (1 sigma) on other players: base and growth per metre of distance, m. */
    public double playerSigmaBaseM = 0.20, playerSigmaPerM = 0.03;
    /** Velocity noise (1 sigma), m/s: base and growth per metre of distance. */
    public double velSigmaBase = 0.20, velSigmaPerM = 0.01;
    /** Noise multiplier for what is behind him (outside the view cone). */
    public double behindFactor = 2.5;
    /** Cosine of the angle from his facing beyond which a thing is "behind" (-0.2 = about 100 degrees). */
    public double viewCosLimit = -0.2;

    // ---- interception ----------------------------------------------------------------------------------------------

    /** Deceleration a player expects of a rolling ball, m/s^2 (the real one depends on the ground). */
    public double rollDecelBelief = 1.2;
    /** Same for a ball in the air (drag only), m/s^2. */
    public double airDecelBelief = 0.4;
    /** Time to react and turn before running, s. */
    public double startDelayS = 0.15;
    /** A team-mate this much quicker to the ball takes it; ties are broken by creation order, s. */
    public double rankMarginS = 0.05;

    // ---- shape -----------------------------------------------------------------------------------------------------

    /** How far the block follows the ball along the pitch, share of the ball's position (attacking / defending). */
    public double shiftAttack = 0.55, shiftDefendBacks = 0.45, shiftDefendHalves = 0.50, shiftDefendForwards = 0.40;
    /** Extra push upfield when attacking and drop when defending, m. */
    public double pushAttackM = 8, dropDefendM = 5;
    /** Lateral spread of the shape when attacking (combination / dribbling) and defending, share of half the width. */
    public double spreadCombination = 0.95, spreadDribbling = 0.80, spreadDefend = 0.70;
    /** Lateral pull toward the ball, share of its y. */
    public double lateralPull = 0.25;
    /** Defenders blend their slot with goal-side marking of the nearest opponent within this radius, m. */
    public double markRadiusM = 18, markWeight = 0.45, markGoalSideM = 2.0;

    // ---- chasing and support ---------------------------------------------------------------------------------------

    /** Players who go for the ball when the other side has it (dribbling sides press in pairs). */
    public int chasersDribbling = 2, chasersCombination = 2;
    /** Backers-up behind a dribbler of a dribbling side: how many, how far behind and to the side, m. */
    public int backersUp = 3;
    public double backUpBehindM = 5, backUpSideM = 6;
    /** Combination: the nearest team-mate offers a short pass this far from the carrier, m. */
    public double shortOptionM = 11;
    /** Forwards stay this far behind the offside line (third-last opponent) as they believe it, m. */
    public double onsideMarginM = 1.0;
    /** Score multiplier for a pass to a man believed offside. */
    public double offsidePassFactor = 0.1;
    /** Distance at which a chaser shoulder-charges the man on the ball, m. */
    public double chargeRangeM = 2.0;
    /** No charge when he comes from behind the man: cosine of the angle off his back beyond which he holds off. */
    public double chargeBehindCos = 0.2;

    // ---- on the ball -----------------------------------------------------------------------------------------------

    /** Extra distance beyond foot reach at which the man on the ball decides what to do with it, m. */
    public double decideExtraM = 1.4;
    /** Shots from inside this distance to the goal centre, m. */
    public double shotRangeM = 28;
    /** Utility weights (before style). */
    public double wShot = 1.4, wPass = 1.0, wLoft = 0.65, wDribble = 1.0, wCross = 0.9, wClear = 1.1;
    /** Style multipliers on dribbling and on passing (dribbling side / combination side). */
    public double dribbleStyleDribbling = 1.35, dribbleStyleCombination = 0.95;
    public double passStyleDribbling = 0.80, passStyleCombination = 1.30;
    /** Share of the dribble score lost with an opponent on top of him (pressure 1). */
    public double dribblePressurePenalty = 0.4;
    /** Longest ground pass a combination / dribbling side tries without penalty, m. */
    public double shortPassCombinationM = 22, shortPassDribblingM = 30;
    /** Random whim added to every option score (uniform 0..whim), so equal options do not lock step. */
    public double whim = 0.12;

    // ---- keeper ---------------------------------------------------------------------------------------------------

    /** Ticks a keeper holds the ball before he releases it (well inside the 2 steps: he stands still). */
    public int keeperHoldTicks = 24;
    /** He releases at once when he has carried this many steps (the law allows 2, 1883). */
    public double keeperReleaseSteps = 1.2;
    /** He comes off his line for a loose ball within this distance of the goal line, m. */
    public double keeperRushM = 18;

    /** Throws on values the AI cannot run with. Returns this. */
    public AiParams validate() {
        req(reactionTicks >= 0 && reactionTicks <= 30, "reaction ticks");
        req(ballSigmaBaseM >= 0 && ballSigmaPerM >= 0 && playerSigmaBaseM >= 0 && playerSigmaPerM >= 0
                && velSigmaBase >= 0 && velSigmaPerM >= 0 && behindFactor >= 1, "perception noise");
        req(rollDecelBelief > 0 && airDecelBelief > 0 && startDelayS >= 0 && rankMarginS >= 0, "interception");
        req(chasersDribbling >= 1 && chasersCombination >= 1 && backersUp >= 0, "chasers");
        req(shotRangeM > 0 && wShot >= 0 && wPass >= 0 && wLoft >= 0 && wDribble >= 0 && wCross >= 0 && wClear >= 0
                && whim >= 0, "weights");
        req(onsideMarginM >= 0 && offsidePassFactor >= 0, "offside");
        req(keeperHoldTicks >= 0 && keeperReleaseSteps > 0, "keeper");
        return this;
    }

    private static void req(boolean ok, String what) {
        if (!ok) {
            throw new IllegalArgumentException("invalid ai params: " + what);
        }
    }
}
