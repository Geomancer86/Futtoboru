package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Save/load roundtrip via in-memory JSON (PL-7).
 */
class SaveLoadIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void midSeasonRoundTripPreservesDateAndLeague() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        harness.simulateDays(60);

        LocalDateTime dateBefore = harness.game().getGameDate();
        int leagueCountBefore = harness.game().getMainAuthority().getLeagues().size();

        harness.roundTripViaJson();

        assertEquals(dateBefore, harness.game().getGameDate());
        assertEquals(leagueCountBefore, harness.game().getMainAuthority().getLeagues().size());
        assertNotNull(harness.primaryLeague());

        harness.continueOneDayAcknowledgingDraws();
        assertTrue(harness.game().getGameDate().isAfter(dateBefore));
    }

    @Test
    void postRolloverRoundTripPreservesChampionEditions() {
        harness.simulateUntilChampionEditions(2, 1800);

        int championsBefore = harness.countEditionsWithChampions();
        int seasonIndexBefore = harness.getSimulatedSeasonIndex();
        assertTrue(championsBefore >= 2);

        SaveGame reloaded = harness.roundTripViaJson();

        assertEquals(championsBefore, harness.countEditionsWithChampions());
        assertEquals(seasonIndexBefore, reloaded.getSimulatedSeasonIndex());
        assertTrue(harness.getSimulatedSeasonIndex() >= 1);
    }
}
