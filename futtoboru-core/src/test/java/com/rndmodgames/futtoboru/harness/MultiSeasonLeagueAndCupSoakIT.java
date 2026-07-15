package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Nightly: league + FA Cup for many procedural seasons — catches cup bracket, replay, and
 * schedule defects that only surface after several years of cup draws.
 *
 * <p>QA review 2026-07-15 found two gaps here, both fixed in this revision:
 * <ol>
 *   <li>This was the <b>only</b> automated coverage of the cup subsystem beyond 2 seasons —
 *       the 60-season {@code LongRunSoakIT} north star deliberately disables cups entirely.
 *       Bumped to {@value #TARGET_SEASONS} seasons so cup replays/bracket advancement get
 *       real long-run exercise (more seasons -&gt; more drawn ties -&gt; more replays).</li>
 *   <li>The old call used {@code simulateWithReport(seasons, days, leagueOnly=false,
 *       suppressStdout=true)}, whose 4-arg overload sets {@code periodicHealthScans =
 *       !suppressStdout = false}. That made the {@code assertTrue(report.getHealthIssues()
 *       .isEmpty())} below pass <b>vacuously</b> — no health scan ever ran. Now explicitly
 *       requests {@code periodicHealthScans = true} so the assertion is real.</li>
 * </ol>
 */
@Tag("nightly")
class MultiSeasonLeagueAndCupSoakIT {

    private static final int TARGET_SEASONS = 15;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void manySeasonsLeagueAndCupCompleteWithHealthySimulation() {
        MultiSeasonSimulationReport report = harness.simulateWithReport(
                TARGET_SEASONS, 700 * TARGET_SEASONS, false, true, true);

        assertTrue(harness.countEditionsWithChampions() >= TARGET_SEASONS,
                "Expected at least " + TARGET_SEASONS + " league champions");
        assertTrue(harness.countCupEditionsWithChampions() >= TARGET_SEASONS,
                "Expected at least " + TARGET_SEASONS + " cup champions");
        // Player's own managed club must stay solvent. Whole-league solvency over this many
        // seasons is NOT asserted here on purpose: EconomyHealthMonitor is documented as an
        // "early-warning scan (no bankruptcy simulation yet)" — AI clubs can legitimately
        // drift into debt over many years with no relegation/wage-cut mechanic to correct it.
        // That is a real, pre-existing economy-depth gap (tracked in blockers.yaml as A-9,
        // found by extending this test from 2 to 15 seasons on 2026-07-15), not a cup-subsystem
        // regression — so it must not fail this test, which exists to catch bracket/replay bugs.
        harness.assertPlayerClubSolvent();
        assertTrue(report.getHealthIssues().isEmpty(), "Health issues: " + report.getHealthIssues());

        System.out.println("MultiSeasonLeagueAndCupSoakIT timing: " + report.getElapsedMs() + " ms, "
                + String.format("%.2f s/season", report.getSecondsPerSeason()));
    }
}
