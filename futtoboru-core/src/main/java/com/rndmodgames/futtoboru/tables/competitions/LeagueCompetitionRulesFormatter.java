package com.rndmodgames.futtoboru.tables.competitions;

import java.util.List;

import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.CompetitionRules.TieBreaker;

/**
 * Text formatting for competition rules shown on the league detail screen (A-5).
 */
public final class LeagueCompetitionRulesFormatter {

    private LeagueCompetitionRulesFormatter() {
    }

    public static String formatPointsSystem(CompetitionRules rules) {
        if (rules == null) {
            return "";
        }
        return rules.getPointsForWin() + " points for win, "
                + rules.getPointsForDraw() + " point" + (rules.getPointsForDraw() != 1 ? "s" : "")
                + " for draw, " + rules.getPointsForLoss() + " for loss";
    }

    public static String formatTieBreakers(CompetitionRules rules) {
        if (rules == null || rules.getTieBreakingOrder() == null) {
            return "Tie-breakers: ";
        }
        StringBuilder tieBreakers = new StringBuilder("Tie-breakers: ");
        List<TieBreaker> order = rules.getTieBreakingOrder();
        for (int i = 0; i < order.size(); i++) {
            if (i > 0) {
                tieBreakers.append(" → ");
            }
            tieBreakers.append(formatTieBreaker(order.get(i)));
        }
        return tieBreakers.toString();
    }

    public static String formatTieBreaker(TieBreaker breaker) {
        if (breaker == null) {
            return "";
        }
        switch (breaker) {
            case POINTS:
                return "Points";
            case GOAL_DIFFERENCE:
                return "Goal Difference";
            case GOAL_AVERAGE:
                return "Goal Average";
            case GOALS_SCORED:
                return "Goals Scored";
            case GOALS_CONCEDED:
                return "Goals Conceded";
            case HEAD_TO_HEAD:
                return "Head-to-Head";
            case ALPHABETICAL:
                return "Alphabetical";
            default:
                return breaker.toString();
        }
    }
}
