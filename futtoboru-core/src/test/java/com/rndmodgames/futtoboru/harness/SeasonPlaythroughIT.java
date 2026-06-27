package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;

/**
 * Headless E2E: one full league season completes and records a champion edition.
 */
class SeasonPlaythroughIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void fullLeagueSeasonCompletesWithChampionEdition() {
        harness.simulateUntilPrimaryLeagueComplete();

        League league = harness.primaryLeague();
        assertNotNull(league);

        LeagueStandingsManager standingsManager = new LeagueStandingsManager();
        assertTrue(standingsManager.isLeagueComplete(league));
        assertNotNull(standingsManager.getChampion(league));

        assertTrue(harness.countEditionsWithChampions() >= 1,
                "Expected at least one finalized league edition with a champion");
    }

    @Test
    void proceduralRolloverStartsSecondEditionAfterFirstSeason() {
        harness.simulateUntilPrimaryLeagueComplete();
        int championsAfterFirst = harness.countEditionsWithChampions();
        assertTrue(championsAfterFirst >= 1);

        harness.simulateUntilChampionEditions(championsAfterFirst + 1, 900);

        assertTrue(harness.countEditionsWithChampions() >= 2,
                "Expected procedural rollover to produce a second champion edition");
        assertTrue(harness.getSimulatedSeasonIndex() >= 1,
                "Simulated season index should advance after procedural rollover");
    }
}
