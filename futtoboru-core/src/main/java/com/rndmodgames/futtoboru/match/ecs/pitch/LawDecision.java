package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * What the Laws make of a ball going out of play.
 *
 * @param goal        a goal stands (in geometry and by the "no goal direct from" rules)
 * @param scoringTeam the side credited with the goal (the side attacking that goal, own goals included), else null
 * @param disputed    a close call with no nets: the claim goes to the umpires on appeal (E-16), who may reverse it
 * @param restart     how play restarts if the decision stands (after a goal: kick-off by the side that lost it)
 * @param ifReversed  disputed only: the decision if the umpires reverse it (goal ↔ no goal), else null
 */
public record LawDecision(boolean goal, Team scoringTeam, boolean disputed, Restart restart, LawDecision ifReversed) {
}
