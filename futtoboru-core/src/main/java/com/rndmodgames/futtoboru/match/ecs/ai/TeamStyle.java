package com.rndmodgames.futtoboru.match.ecs.ai;

/**
 * A club's way of playing (D19-6, MATCH_ENGINE_V2.md §1): the English dribbling game with backing-up, or the Scottish
 * combination (short passing) game that Preston's Scots brought south. Weights the AI's utility scores.
 */
public enum TeamStyle {
    /** Run with the ball in packs, team-mates backing up behind the dribbler; long kicks forward. */
    DRIBBLING,
    /** Short passes between spread-out team-mates; fewer individual runs. */
    COMBINATION
}
