package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * B2: table position affects home attendance after a full season.
 * Normalizes by table-position factor so opponent/form fixture noise does not invert the signal.
 *
 * <p>QA fix 2026-07-15: this used to call {@code LeagueStandingsManager.calculateStandings(league)}
 * and read {@code Club} counters (points/goals) right after
 * {@code simulateUntilPrimaryLeagueComplete()} returned. That is unsafe: champion assignment and
 * the next season's stat reset happen in the same daily engine tick, so by the time control
 * returns to the test the live {@code Club} counters already belong to the <em>new</em>, barely-
 * started season, not the one that just finished — {@code standings.get(0)}/{@code get(last)} were
 * effectively meaningless (see {@code GameTestHarness#isPrimaryCupComplete} javadoc for the root
 * cause). This version derives standings and attendance directly from raw {@code Match} results
 * filtered to the completed edition's ID, which survives rollover untouched.
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
        assertNotNull(league);
        CompetitionEdition completedEdition = findEditionWithChampion(league);
        assertNotNull(completedEdition, "Expected a finalized league edition with a recorded champion");

        List<Club> clubs = league.getLeagueClubs();
        assertTrue(clubs.size() >= 4, "Need at least four clubs for attendance spread check");

        List<Club> standings = new ArrayList<>(clubs);
        standings.sort(Comparator.comparingInt((Club c) -> pointsInEdition(c, completedEdition.getId())).reversed());

        int clubCount = standings.size();
        Club first = standings.get(0);
        Club last = standings.get(clubCount - 1);

        double firstNorm = averageHomeAttendanceInEdition(first, completedEdition.getId()) / tablePositionFactor(1, clubCount);
        double lastNorm = averageHomeAttendanceInEdition(last, completedEdition.getId()) / tablePositionFactor(clubCount, clubCount);

        assertTrue(firstNorm >= lastNorm * 0.85,
                () -> "position-normalized first=" + firstNorm + " last=" + lastNorm);
    }

    private static CompetitionEdition findEditionWithChampion(League league) {
        if (league.getEditions() == null) {
            return null;
        }
        for (CompetitionEdition edition : league.getEditions()) {
            if (edition != null && edition.getChampionsId() != null) {
                return edition;
            }
        }
        return null;
    }

    /** Recomputes league points (2-1-0) for one club within one edition, from raw match results. */
    private static int pointsInEdition(Club club, Long editionId) {
        int points = 0;
        for (Match match : allMatchesOf(club)) {
            if (!isCompletedLeagueMatchInEdition(match, editionId)) {
                continue;
            }
            boolean isHome = club.getId().equals(match.getHomeClubId());
            int ownGoals = isHome ? match.getHomeGoals() : match.getAwayGoals();
            int oppGoals = isHome ? match.getAwayGoals() : match.getHomeGoals();
            if (ownGoals > oppGoals) {
                points += 2; // historical 1888-89 rules: 2 for a win
            } else if (ownGoals == oppGoals) {
                points += 1;
            }
        }
        return points;
    }

    private static double averageHomeAttendanceInEdition(Club club, Long editionId) {
        int total = 0;
        int count = 0;
        for (Match match : allMatchesOf(club)) {
            if (!isCompletedLeagueMatchInEdition(match, editionId)) {
                continue;
            }
            if (!club.getId().equals(match.getHomeClubId())) {
                continue;
            }
            total += match.getAttendance() != null ? match.getAttendance() : 0;
            count++;
        }
        return count == 0 ? 0 : (double) total / count;
    }

    private static boolean isCompletedLeagueMatchInEdition(Match match, Long editionId) {
        return match != null
                && Boolean.TRUE.equals(match.getIsPlayed())
                && match.getMatchType() != null
                && match.getMatchType() == Match.LEAGUE_MATCH
                && editionId.equals(match.getCompetitionEditionId())
                && match.getHomeGoals() != null
                && match.getAwayGoals() != null;
    }

    private static List<Match> allMatchesOf(Club club) {
        List<Match> matches = new ArrayList<>();
        if (club.getScheduledMatches() != null) {
            matches.addAll(club.getScheduledMatches());
        }
        if (club.getPlayedMatches() != null) {
            matches.addAll(club.getPlayedMatches());
        }
        return matches;
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
