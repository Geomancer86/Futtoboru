package com.rndmodgames.futtoboru.engine.simulation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;

/**
 * Post-match 0–10 ratings for starting XI (MVP-5, CM1 genre hook).
 */
public final class MatchRatingsCalculator {

    public static final float BASE_RATING = 6.0f;
    public static final float GOAL_BONUS = 0.5f;
    public static final float WIN_BONUS = 0.3f;
    public static final float LOSS_PENALTY = 0.3f;
    public static final float MIN_RATING = 4.0f;
    public static final float MAX_RATING = 9.5f;

    private MatchRatingsCalculator() {
    }

    public static Map<Long, Float> rateLineup(Match match, MatchLineup lineup, Club club, boolean isHomeClub) {
        if (match == null || lineup == null || club == null) {
            return Collections.emptyMap();
        }
        List<MatchLineupBuilder.PlayerRef> players = MatchLineupBuilder.playersInLineup(lineup, club);
        if (players.isEmpty()) {
            return Collections.emptyMap();
        }

        int homeGoals = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
        int awayGoals = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
        boolean won;
        boolean drew;
        if (isHomeClub) {
            won = homeGoals > awayGoals;
            drew = homeGoals == awayGoals;
        } else {
            won = awayGoals > homeGoals;
            drew = homeGoals == awayGoals;
        }

        List<Long> scorerIds = isHomeClub ? match.getHomeScorerIds() : match.getAwayScorerIds();
        Map<Long, Integer> goalsByPlayer = countGoals(scorerIds);

        Map<Long, Float> ratings = new LinkedHashMap<>();
        for (MatchLineupBuilder.PlayerRef ref : players) {
            if (ref.player == null || ref.player.getId() == null) {
                continue;
            }
            float rating = BASE_RATING;
            if (won) {
                rating += WIN_BONUS;
            } else if (!drew) {
                rating -= LOSS_PENALTY;
            }
            int goals = goalsByPlayer.getOrDefault(ref.player.getId(), 0);
            rating += goals * GOAL_BONUS;
            rating = clamp(rating);
            ratings.put(ref.player.getId(), rating);
        }
        return ratings;
    }

    private static Map<Long, Integer> countGoals(List<Long> scorerIds) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        if (scorerIds == null) {
            return counts;
        }
        for (Long id : scorerIds) {
            if (id == null) {
                continue;
            }
            counts.merge(id, 1, Integer::sum);
        }
        return counts;
    }

    static float clamp(float rating) {
        return Math.max(MIN_RATING, Math.min(MAX_RATING, rating));
    }
}
