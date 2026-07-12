package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * C-1 regression — each continue advances exactly one day (engine mirror of Continue button).
 */
@Tag("r1888")
class ContinueDayAdvanceIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void tenContinueCyclesAdvanceTenDays() {
        LocalDateTime start = harness.gameDate();
        for (int i = 0; i < 10; i++) {
            harness.continueOneDayAcknowledgingDraws();
        }
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), harness.gameDate().toLocalDate());
        assertEquals(10, days);
    }
}
