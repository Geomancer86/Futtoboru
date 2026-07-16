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
     * Performance regression gate. Baseline on 2026-07-15 (after fixing the O(history) late-season
     * slowdown — see ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md) was 69.96 s/season; the pre-fix
     * regression this guard is meant to catch ran at 87.23 s/season. Set with headroom above normal
     * machine-to-machine variance so this only trips on a real reintroduced slowdown, not noise.
     */
    private static final double MAX_SECONDS_PER_SEASON = 150.0;

    /**
     * Slowest-single-season gate. The run-wide average above can hide the fact that per-season
     * cost still grows noticeably from season 1 to season 60 (roughly 1s -&gt; ~145-165s observed
     * on 2026-07-15, run after both known O(history) scans were fixed) — this catches a *worse*
     * blow-up even if the average would otherwise pass. Set with headroom above the ~165s worst
     * season observed on that run so this only trips on a real further regression, not noise.
     * See docs/planning/ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md.
     */
    private static final double MAX_SECONDS_FOR_SLOWEST_SEASON = 260.0;

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
                                + "(baseline 69.96 s/season on 2026-07-15). "
                                + "Look for a reintroduced O(history) scan over unbounded per-club match lists.",
                        report.getSecondsPerSeason(), MAX_SECONDS_PER_SEASON));

        System.out.println("LongRunSoakIT slowest season: "
                + String.format("%.2f s", report.getMaxSecondsPerSeason()));

        assertTrue(report.getMaxSecondsPerSeason() < MAX_SECONDS_FOR_SLOWEST_SEASON,
                String.format(
                        "Soak performance regression: the single slowest season took %.2f s, "
                                + "exceeding the %.0f s per-season gate (baseline worst season was "
                                + "~165 s on 2026-07-15). The run-wide average can pass even when "
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
