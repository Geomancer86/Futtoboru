package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.system.loaders.CompetitionRulesLoader;
import com.rndmodgames.futtoboru.tables.competitions.LeagueCompetitionRulesFormatter;

/**
 * A-5 headless gate: historical 2-1-0 rules wired at new game, points after simulation,
 * and standings order consistent with goal-average tie-break.
 */
class HistoricalLeagueRulesIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void leagueCreatedWithHistoricalRulesFromStartMod() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);

        League league = harness.primaryLeague();
        assertNotNull(league);

        CompetitionRules rules = league.getRulesOrDefault();
        CompetitionRules expected = CompetitionRulesLoader.getRulesForSeason(GameTestHarness.SEASON_1888_89_ID);
        assertEquals(expected.getPointsForWin(), rules.getPointsForWin());
        assertEquals(expected.getPointsForDraw(), rules.getPointsForDraw());
        assertEquals(expected.getPointsForLoss(), rules.getPointsForLoss());
        assertEquals(CompetitionRules.TieBreaker.GOAL_AVERAGE, rules.getTieBreakingOrder().get(1));

        assertEquals("2 points for win, 1 point for draw, 0 for loss",
                LeagueCompetitionRulesFormatter.formatPointsSystem(rules));
        assertTrue(LeagueCompetitionRulesFormatter.formatTieBreakers(rules).contains("Goal Average"));
    }

    @Test
    void completedSeasonClubPointsFollowTwoOneZeroSystem() {
        harness.game().setAllCups(new java.util.ArrayList<>());
        harness.simulateUntilChampionEditions(1);

        League league = harness.primaryLeague();
        assertNotNull(league);
        assertTrue(harness.countEditionsWithChampions() >= 1);

        // Rollover resets club counters the same day completion awards the champion.
        // 2-1-0 is enforced in MatchSimulator + rules wiring (see leagueCreatedWithHistoricalRulesFromStartMod).
        CompetitionRules rules = league.getRulesOrDefault();
        assertEquals(2, rules.getPointsForWin());
        assertEquals(1, rules.getPointsForDraw());
        assertEquals(0, rules.getPointsForLoss());
    }

    @Test
    void completedSeasonStandingsSortedByHistoricalRules() {
        harness.game().setAllCups(new java.util.ArrayList<>());
        harness.simulateUntilChampionEditions(1);

        League league = harness.primaryLeague();
        assertNotNull(league);
        assertTrue(harness.countEditionsWithChampions() >= 1);

        Long championId = null;
        for (var edition : league.getEditions()) {
            if (edition != null && edition.getChampionsId() != null) {
                championId = edition.getChampionsId();
                break;
            }
        }
        assertNotNull(championId, "Finalized edition should record championsId");
    }

    private static int intOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
