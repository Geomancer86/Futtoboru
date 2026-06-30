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
 * Probabilistic over one simulated season — run in nightly tier, not PR gate.
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

        int mid = standings.size() / 2;
        double topHalfAvg = 0;
        double bottomHalfAvg = 0;
        for (int i = 0; i < mid; i++) {
            topHalfAvg += harness.averageHomeAttendance(standings.get(i));
        }
        for (int i = mid; i < standings.size(); i++) {
            bottomHalfAvg += harness.averageHomeAttendance(standings.get(i));
        }
        topHalfAvg /= mid;
        bottomHalfAvg /= (standings.size() - mid);

        final double topAvgFinal = topHalfAvg;
        final double bottomAvgFinal = bottomHalfAvg;
        assertTrue(topAvgFinal >= bottomAvgFinal,
                () -> "topHalfAvg=" + topAvgFinal + " bottomHalfAvg=" + bottomAvgFinal);
    }
}
