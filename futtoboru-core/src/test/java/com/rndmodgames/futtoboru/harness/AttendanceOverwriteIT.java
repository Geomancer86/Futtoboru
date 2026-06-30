package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;

/**
 * H-1: MatchSimulator must not overwrite ticket-sales attendance.
 */
class AttendanceOverwriteIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void simulatorPreservesPreSoldAttendance() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club home = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Match match = harness.findNextLeagueMatchForClub(home.getId());
        assertNotNull(match);

        int preSold = 7500;
        match.setAttendance(preSold);

        Club away = harness.game().getClubById(match.getAwayClubId());
        assertNotNull(away);
        new MatchSimulator(harness.application()).simulateMatch(match);

        assertTrue(match.getIsPlayed());
        assertEquals(preSold, match.getAttendance(),
                "Ticket sales attendance must survive simulation (H-1)");
    }
}
