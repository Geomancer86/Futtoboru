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
        harness.simulateUntilPrimaryLeagueComplete();

        League league = harness.primaryLeague();
        assertNotNull(league);

        int clubsChecked = 0;
        for (Club club : league.getLeagueClubs()) {
            if (club == null) {
                continue;
            }
            int wins = intOrZero(club.getMatchesWon());
            int draws = intOrZero(club.getMatchesDrawn());
            int points = intOrZero(club.getPoints());
            assertEquals(wins * 2 + draws, points,
                    () -> club.getName() + " should use 2-1-0 points (not 3-1-0)");
            clubsChecked++;
        }
        assertTrue(clubsChecked >= 12, "Expected a full Football League roster");
    }

    @Test
    void completedSeasonStandingsSortedByHistoricalRules() {
        harness.simulateUntilPrimaryLeagueComplete();

        League league = harness.primaryLeague();
        LeagueStandingsManager standingsManager = new LeagueStandingsManager();
        assertTrue(standingsManager.isLeagueComplete(league));

        Club champion = standingsManager.getChampion(league);
        assertNotNull(champion);
        assertEquals(champion.getId(), standingsManager.calculateStandings(league).get(0).getId());
    }

    private static int intOrZero(Integer value) {
        return value != null ? value : 0;
    }
}
