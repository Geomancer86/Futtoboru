package com.rndmodgames.futtoboru.match.ecs;

import java.util.List;

import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLog;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Result of {@link EcsMatchEngine#play}. Stand-in for the V2 {@code MatchResult}: the score, the goals and every event
 * of the match (E-17, {@link MatchEventLog}); it carries what saves and replays need (§5.3) and the ball's mass for the report ("the leather
 * grew heavy").
 *
 * @param homeGoals   goals by the home side (own goals by the away side included), from the laws (E-16)
 * @param ballActions ball actions played (kicks, touches, headers, keeper handling), a rough measure of play
 * @param offsides    offsides given on appeal
 * @param fouls       fouls given on appeal
 * @param goals       each goal with the side and slot of the last touch (E-17a)
 * @param events      every event of the match in tick order (E-17), never null
 */
public record EcsMatchResult(String matchKey, String engineVersion, long seed, long ticks, long finalStateHash,
        double halfTimeBallMassKg, double fullTimeBallMassKg, int homeGoals, int awayGoals, long ballActions,
        int offsides, int fouls, List<Goal> goals, MatchEventLog events) {

    public EcsMatchResult {
        goals = goals == null ? List.of() : List.copyOf(goals);
        events = events == null ? new MatchEventLog(30, 45, -1, 0) : events;
    }

    /**
     * One goal as given (E-17a), in match order.
     *
     * @param scoringTeam side it counts for
     * @param lastTouch   side of the last player to play the ball, null if nobody had (an own goal when it is not
     *                    {@code scoringTeam})
     * @param slot        that player's slot in his team sheet, -1 if nobody
     * @param tick        tick the goal was given on
     */
    public record Goal(Team scoringTeam, Team lastTouch, int slot, long tick) {

        /** True when the last touch was by a player of the scoring side, who is then the scorer. */
        public boolean hasScorer() {
            return lastTouch == scoringTeam && slot >= 0;
        }
    }
}
