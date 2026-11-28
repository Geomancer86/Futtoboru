package com.rndmodgames.futtoboru.engine.simulation;

import java.util.List;

/**
 * Result of {@link MatchEngine#play}: the score and the scorers in goal order (person ids of each side's players;
 * fewer ids than goals when a goal has no known scorer).
 */
public record MatchOutcome(int homeGoals, int awayGoals, List<Long> homeScorerIds, List<Long> awayScorerIds) {

    public MatchOutcome {
        if (homeGoals < 0 || awayGoals < 0) {
            throw new IllegalArgumentException("goals " + homeGoals + "-" + awayGoals);
        }
        homeScorerIds = List.copyOf(homeScorerIds);
        awayScorerIds = List.copyOf(awayScorerIds);
        if (homeScorerIds.size() > homeGoals || awayScorerIds.size() > awayGoals) {
            throw new IllegalArgumentException("more scorers than goals");
        }
    }
}
