package com.rndmodgames.futtoboru.system.loaders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.harness.HeadlessGdxTestSupport;

class CompetitionRulesLoaderTest {

    @Test
    void season18RulesUseHistoricalTwoOneZero() {
        HeadlessGdxTestSupport.ensureInitialized();
        CompetitionRules rules = CompetitionRulesLoader.getRulesForSeason(18L);
        assertEquals(2, rules.getPointsForWin());
        assertEquals(1, rules.getPointsForDraw());
        assertEquals(0, rules.getPointsForLoss());
        assertEquals(CompetitionRules.MatchFormat.DOUBLE_ROUND_ROBIN, rules.getMatchFormat());
        assertEquals(CompetitionRules.TieBreaker.GOAL_AVERAGE, rules.getTieBreakingOrder().get(1));
    }
}
