package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;

/**
 * Smoke tests for {@link GameTestHarness} setup and day advancement.
 */
class GameTestHarnessTest {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void newGameSetsEmployedClubAndStartDate() {
        assertNotNull(harness.game());
        assertEquals(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, harness.game().getOwner().getCurrentClubId());
        assertEquals(LocalDateTime.of(1888, 4, 1, 4, 0, 0), harness.game().getGameDate());
        assertNotNull(harness.game().getCurrentClub());
    }

    @Test
    void continueOneDayAdvancesCalendar() {
        LocalDateTime before = harness.game().getGameDate();
        harness.continueOneDay();
        assertEquals(before.plusDays(1), harness.game().getGameDate());
    }

    @Test
    void advanceUntilFindsLeagueCreation() {
        harness.advanceUntil(save -> {
            if (save.getMainAuthority() == null || save.getMainAuthority().getLeagues() == null) {
                return false;
            }
            return !save.getMainAuthority().getLeagues().isEmpty();
        }, 30);

        League league = harness.game().getMainAuthority().getLeagues().get(0);
        assertNotNull(league.getName());
        assertFalse(league.getLeagueClubs().isEmpty());
    }
}
