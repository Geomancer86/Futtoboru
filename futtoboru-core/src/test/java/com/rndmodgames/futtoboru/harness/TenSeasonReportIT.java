package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs 10 procedural league seasons and writes a champion/finance report for v1 review.
 */
class TenSeasonReportIT {

    private static final int TARGET_SEASONS = 10;
    private static final int MAX_DAYS = 700 * TARGET_SEASONS;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void tenSeasonsProduceReportWithChampionsAndFinances() throws Exception {
        MultiSeasonSimulationReport report =
                harness.simulateWithReport(TARGET_SEASONS, MAX_DAYS, true, true);

        assertEquals(TARGET_SEASONS, report.getSeasons().size());
        assertEquals(TARGET_SEASONS, harness.countEditionsWithChampions());
        assertTrue(report.getElapsedMs() < 600_000L,
                "10 league seasons should finish well under 10 minutes without stdout logging (was "
                        + report.getElapsedMs() + " ms)");
        harness.assertNoNegativePrimaryLeagueBalances();
        harness.assertPlayerClubSolvent();
        assertTrue(report.getLowestLeagueClubBalance().compareTo(java.math.BigDecimal.ZERO) >= 0);

        Path reportPath = Path.of("target", "ten-season-report.md");
        report.writeTo(reportPath);
        System.out.println(report.toMarkdown());
    }
}
