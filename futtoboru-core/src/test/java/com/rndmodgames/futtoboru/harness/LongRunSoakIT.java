package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;

/**
 * 60-season procedural north-star soak — run with {@code mvn test -Psoak -pl futtoboru-core}.
 */
@Tag("soak")
class LongRunSoakIT {

    private static final int TARGET_CHAMPION_EDITIONS = 60;
    private static final int MAX_DAYS = 700 * TARGET_CHAMPION_EDITIONS;

    /**
     * Performance regression gate. Baseline on 2026-07-19, after fixing a sixth O(all-time-history)
     * hotspot ({@code JobManager.getAvailableJobs()} re-filtering every job opening ever created,
     * found via JFR profiling — see docs/planning/ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md
     * PERF-4): the full 60-season run dropped from ~74-93 s/season to **1.24 s/season** (a ~60-75x
     * speedup; total run time from ~80-90 minutes to ~74 seconds). Earlier same-week baselines
     * (69.96, then 74.26, then 80-93 s/season) were all still paying this cost without realizing it.
     * Set with generous headroom above the new baseline so this only trips on a real reintroduced
     * O(history) scan, not normal machine-to-machine variance — but tight enough that a regression
     * anywhere near the old ~70-90 s/season baseline still fails loudly instead of silently passing.
     */
    private static final double MAX_SECONDS_PER_SEASON = 10.0;

    /**
     * Slowest-single-season gate. The run-wide average above can hide a late-season-only blowup —
     * this catches that even if the average would otherwise pass. Baseline on 2026-07-19 (same run
     * as above): slowest season was **1.93 s** (previously ~165-214 s before the JobManager fix).
     * See docs/planning/ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md.
     */
    private static final double MAX_SECONDS_FOR_SLOWEST_SEASON = 20.0;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
        // North star is 60 league champions; skip parallel cup sim to keep soak runtime bounded.
        harness.game().setAllCups(new ArrayList<>());
    }

    @Test
    void sixtyProceduralSeasonsProduceSixtyLeagueChampions() {
        MultiSeasonSimulationReport report =
                harness.simulateWithReport(TARGET_CHAMPION_EDITIONS, MAX_DAYS, true, true, false);

        assertEquals(TARGET_CHAMPION_EDITIONS, harness.countEditionsWithChampions());
        assertTrue(harness.getSimulatedSeasonIndex() >= TARGET_CHAMPION_EDITIONS - 1);

        List<Long> championIds = championClubIds(harness.primaryLeague());
        assertEquals(TARGET_CHAMPION_EDITIONS, championIds.size());

        System.out.println("LongRunSoakIT timing: " + report.getElapsedMs() + " ms, "
                + String.format("%.2f s/season", report.getSecondsPerSeason()));

        assertTrue(report.getSecondsPerSeason() < MAX_SECONDS_PER_SEASON,
                String.format(
                        "Soak performance regression: %.2f s/season exceeds the %.0f s/season gate "
                                + "(baseline 1.24 s/season on 2026-07-19, after fixing JobManager's "
                                + "all-time job-openings scan). Look for a reintroduced O(history) "
                                + "scan over an unbounded list (matches, messages, job openings, ...).",
                        report.getSecondsPerSeason(), MAX_SECONDS_PER_SEASON));

        System.out.println("LongRunSoakIT slowest season: "
                + String.format("%.2f s", report.getMaxSecondsPerSeason()));

        assertTrue(report.getMaxSecondsPerSeason() < MAX_SECONDS_FOR_SLOWEST_SEASON,
                String.format(
                        "Soak performance regression: the single slowest season took %.2f s, "
                                + "exceeding the %.0f s per-season gate (baseline worst season was "
                                + "1.93 s on 2026-07-19). The run-wide average can pass even when "
                                + "this fails, because late seasons are the slow ones and early "
                                + "seasons pull the average down — this gate exists specifically "
                                + "to catch that case.",
                        report.getMaxSecondsPerSeason(), MAX_SECONDS_FOR_SLOWEST_SEASON));
    }

    private static List<Long> championClubIds(League league) {
        List<Long> ids = new ArrayList<>();
        if (league == null || league.getEditions() == null) {
            return ids;
        }
        for (CompetitionEdition edition : league.getEditions()) {
            if (edition != null && edition.getChampionsId() != null) {
                ids.add(edition.getChampionsId());
            }
        }
        return ids;
    }
}
