package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;

/**
 * Procedural multi-season soak — PR gate target (3 champion editions).
 */
class MultiSeasonSoakIT {

    private static final int TARGET_CHAMPION_EDITIONS = 3;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void threeProceduralSeasonsProduceThreeChampions() {
        harness.simulateUntilChampionEditions(TARGET_CHAMPION_EDITIONS);

        assertEquals(TARGET_CHAMPION_EDITIONS, harness.countEditionsWithChampions());
        assertTrue(harness.getSimulatedSeasonIndex() >= TARGET_CHAMPION_EDITIONS - 1);

        List<Long> championIds = championClubIds(harness.primaryLeague());
        assertEquals(TARGET_CHAMPION_EDITIONS, championIds.size());
        harness.assertNoNegativePrimaryLeagueBalances();
        harness.assertPlayerClubSolvent();
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
