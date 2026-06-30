package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;

/**
 * B2: table position affects home attendance after a full season.
 */
class AttendanceModelIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void topTableClubHigherAverageHomeAttendanceThanBottom() {
        harness.simulateUntilPrimaryLeagueComplete();

        League league = harness.primaryLeague();
        LeagueStandingsManager standingsManager = new LeagueStandingsManager();
        assertTrue(standingsManager.isLeagueComplete(league));

        Club top = standingsManager.calculateStandings(league).get(0);
        Club bottom = standingsManager.calculateStandings(league)
                .get(standingsManager.calculateStandings(league).size() - 1);

        double topAvg = harness.averageHomeAttendance(top);
        double bottomAvg = harness.averageHomeAttendance(bottom);
        assertTrue(topAvg > bottomAvg,
                () -> "top=" + top.getName() + " avg=" + topAvg + " bottom=" + bottom.getName() + " avg=" + bottomAvg);
    }
}
