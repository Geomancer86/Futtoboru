package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Player;

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

    /**
     * Explicit proof for the acceptance criteria that used to be marked ❌/untested in
     * RELEASE_V1_DEFINITION.md §4.7 (PL-13, PL-14): before this test (added by QA review
     * 2026-07-15), player-ID and squad-size stability across a season rollover was only ever
     * an <em>inference</em> from "the 60-season soak didn't crash," never a direct assertion.
     */
    @Test
    void sameClubsAndPlayerIdsPersistAcrossRollover() {
        harness.simulateUntilPrimaryLeagueComplete();

        League leagueBefore = harness.primaryLeague();
        assertNotNull(leagueBefore);
        Set<Long> clubIdsBefore = new HashSet<>();
        Map<Long, Set<Long>> playerIdsByClubBefore = new HashMap<>();
        Map<Long, Integer> squadSizeByClubBefore = new HashMap<>();
        for (Club club : leagueBefore.getLeagueClubs()) {
            clubIdsBefore.add(club.getId());
            squadSizeByClubBefore.put(club.getId(), club.getPlayers() == null ? 0 : club.getPlayers().size());
            Set<Long> playerIds = new HashSet<>();
            if (club.getPlayers() != null) {
                for (Player player : club.getPlayers()) {
                    playerIds.add(player.getId());
                }
            }
            playerIdsByClubBefore.put(club.getId(), playerIds);
        }

        int championsAfterFirst = harness.countEditionsWithChampions();
        harness.simulateUntilChampionEditions(championsAfterFirst + 1, 900);

        League leagueAfter = harness.primaryLeague();
        assertNotNull(leagueAfter);
        Set<Long> clubIdsAfter = new HashSet<>();
        for (Club club : leagueAfter.getLeagueClubs()) {
            clubIdsAfter.add(club.getId());
        }
        assertEquals(clubIdsBefore, clubIdsAfter,
                "PL-13/PL-14: the same set of clubs must continue into the next procedural season");

        for (Club club : leagueAfter.getLeagueClubs()) {
            int squadSizeAfter = club.getPlayers() == null ? 0 : club.getPlayers().size();
            assertEquals(squadSizeByClubBefore.get(club.getId()), squadSizeAfter,
                    "PL-14: squad size for club " + club.getName() + " must stay stable across rollover"
                            + " (v1 has no youth intake/retirement/transfers)");

            Set<Long> playerIdsAfter = new HashSet<>();
            if (club.getPlayers() != null) {
                for (Player player : club.getPlayers()) {
                    playerIdsAfter.add(player.getId());
                }
            }
            assertEquals(playerIdsByClubBefore.get(club.getId()), playerIdsAfter,
                    "PL-13: club " + club.getName() + " must keep the exact same player IDs across rollover");
        }
    }
}
