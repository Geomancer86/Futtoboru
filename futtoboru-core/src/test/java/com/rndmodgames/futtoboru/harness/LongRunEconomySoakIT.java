package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.economy.EconomyHealthMonitor;

/**
 * Long-run gate: multi-season sim completes with champions and no negative club balances.
 */
@Tag("nightly")
class LongRunEconomySoakIT {

    private static final int TARGET_SEASONS = 3;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void threeSeasonsCompleteWithChampionsAndNonNegativeBalances() {
        MultiSeasonSimulationReport report = harness.simulateWithReport(
                TARGET_SEASONS, 700 * TARGET_SEASONS, true, true);

        assertEquals(TARGET_SEASONS, report.getSeasons().size());
        assertEquals(TARGET_SEASONS, harness.countEditionsWithChampions());
        assertTrue(harness.getSimulatedSeasonIndex() >= TARGET_SEASONS - 1);

        harness.assertNoNegativePrimaryLeagueBalances();
        harness.assertPlayerClubSolvent();
        assertTrue(report.getLowestLeagueClubBalance().compareTo(java.math.BigDecimal.ZERO) >= 0);
    }
}
