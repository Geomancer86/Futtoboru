package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Nightly: league + FA Cup for two procedural seasons — catches cup bracket and schedule defects.
 */
@Tag("nightly")
class MultiSeasonLeagueAndCupSoakIT {

    private static final int TARGET_SEASONS = 2;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void twoSeasonsLeagueAndCupCompleteWithHealthySimulation() {
        MultiSeasonSimulationReport report = harness.simulateWithReport(
                TARGET_SEASONS, 700 * TARGET_SEASONS, false, true);

        assertEquals(TARGET_SEASONS, report.getSeasons().size());
        assertEquals(TARGET_SEASONS, harness.countEditionsWithChampions());
        assertTrue(harness.countCupEditionsWithChampions() >= TARGET_SEASONS,
                "Expected at least " + TARGET_SEASONS + " cup champions");
        harness.assertNoNegativePrimaryLeagueBalances();
        harness.assertPlayerClubSolvent();
        assertTrue(report.getHealthIssues().isEmpty(), "Health issues: " + report.getHealthIssues());
    }
}
