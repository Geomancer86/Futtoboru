package com.rndmodgames.futtoboru.tables.competitions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.harness.HeadlessGdxTestSupport;
import com.rndmodgames.futtoboru.system.loaders.CompetitionRulesLoader;

/**
 * A-5: automated checks for league detail rules text (2-1-0 + goal average tie-break).
 */
class LeagueCompetitionRulesFormatterTest {

    @Test
    void season18RulesTextMatchesLeagueDetailScreen() {
        HeadlessGdxTestSupport.ensureInitialized();
        CompetitionRules rules = CompetitionRulesLoader.getRulesForSeason(18L);

        assertEquals("2 points for win, 1 point for draw, 0 for loss",
                LeagueCompetitionRulesFormatter.formatPointsSystem(rules));
        assertEquals("Tie-breakers: Points → Goal Average → Goals Scored → Alphabetical",
                LeagueCompetitionRulesFormatter.formatTieBreakers(rules));
    }

    @Test
    void modernRulesTextUsesThreeOneZeroAndGoalDifference() {
        CompetitionRules rules = CompetitionRules.createDefaultRules();

        assertEquals("3 points for win, 1 point for draw, 0 for loss",
                LeagueCompetitionRulesFormatter.formatPointsSystem(rules));
        assertEquals("Tie-breakers: Points → Goal Difference → Goals Scored → Alphabetical",
                LeagueCompetitionRulesFormatter.formatTieBreakers(rules));
    }
}
