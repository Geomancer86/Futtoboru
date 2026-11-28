package com.rndmodgames.futtoboru.engine.simulation;

import java.util.List;

/**
 * Result of {@link MatchEngine#play}: the score and the scorers in goal order (person ids of each side's players;
 * fewer ids than goals when a goal has no known scorer).
 *
 * E-17: an engine that times its own goals gives each side's goal minutes (ascending, one per goal; empty lists
 * when it does not, and {@link MatchSimulator} draws them, G-03) and may give its whole event log in the compact
 * saved form ({@code MatchEventLog.encode()}, null when it has none). Both are stored on the match.
 */
public record MatchOutcome(int homeGoals, int awayGoals, List<Long> homeScorerIds, List<Long> awayScorerIds,
        List<Integer> homeGoalMinutes, List<Integer> awayGoalMinutes, String eventLog) {

    public MatchOutcome {
        if (homeGoals < 0 || awayGoals < 0) {
            throw new IllegalArgumentException("goals " + homeGoals + "-" + awayGoals);
        }
        homeScorerIds = List.copyOf(homeScorerIds);
        awayScorerIds = List.copyOf(awayScorerIds);
        if (homeScorerIds.size() > homeGoals || awayScorerIds.size() > awayGoals) {
            throw new IllegalArgumentException("more scorers than goals");
        }
        homeGoalMinutes = homeGoalMinutes == null ? List.of() : List.copyOf(homeGoalMinutes);
        awayGoalMinutes = awayGoalMinutes == null ? List.of() : List.copyOf(awayGoalMinutes);
        if ((!homeGoalMinutes.isEmpty() && homeGoalMinutes.size() != homeGoals)
                || (!awayGoalMinutes.isEmpty() && awayGoalMinutes.size() != awayGoals)) {
            throw new IllegalArgumentException("goal minutes must be one per goal or none");
        }
    }

    /** A result without minutes or events (the instant engine). */
    public MatchOutcome(int homeGoals, int awayGoals, List<Long> homeScorerIds, List<Long> awayScorerIds) {
        this(homeGoals, awayGoals, homeScorerIds, awayScorerIds, List.of(), List.of(), null);
    }

    /** True when the engine timed every goal (a goalless match needs no minutes). */
    public boolean hasGoalMinutes() {
        return homeGoalMinutes.size() == homeGoals && awayGoalMinutes.size() == awayGoals
                && (homeGoals + awayGoals > 0 || eventLog != null);
    }
}
