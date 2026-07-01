package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * B2: table position affects home attendance after a full season.
 * Normalizes by table-position factor so opponent/form fixture noise does not invert the signal.
 */
@Tag("nightly")
class AttendanceModelIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void topTableClubHigherAverageHomeAttendanceThanBottom() {
        DatabaseLoader.RNG.setSeed(18880901L);
        harness.simulateUntilPrimaryLeagueComplete();

        League league = harness.primaryLeague();
        LeagueStandingsManager standingsManager = new LeagueStandingsManager();
        assertTrue(standingsManager.isLeagueComplete(league));

        List<Club> standings = standingsManager.calculateStandings(league);
        assertTrue(standings.size() >= 4, "Need at least four clubs for attendance spread check");

        int clubCount = standings.size();
        Club first = standings.get(0);
        Club last = standings.get(clubCount - 1);

        double firstNorm = harness.averageHomeAttendance(first) / tablePositionFactor(1, clubCount);
        double lastNorm = harness.averageHomeAttendance(last) / tablePositionFactor(clubCount, clubCount);

        assertTrue(firstNorm >= lastNorm * 0.85,
                () -> "position-normalized first=" + firstNorm + " last=" + lastNorm);
    }

    /** Mirrors {@code AttendanceCalculator.positionFactor} for E2E normalization. */
    private static double tablePositionFactor(int rank, int clubCount) {
        if (clubCount < 2 || rank < 1) {
            return 1.0;
        }
        double span = 0.12 + 0.08;
        double step = span / (clubCount - 1);
        return 1.12 - (rank - 1) * step;
    }
}
