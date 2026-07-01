package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.simulation.PlayerConditionService;

@Tag("nightly")
class LayerBMultiSeasonSoakIT {

    private static final int TARGET_CHAMPION_EDITIONS = 5;

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void fiveSeasonsKeepRatingsAndConditionHealthy() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        harness.setClubLineup(club.getId(), harness.bestElevenPlayerIds(club));

        harness.simulateUntilChampionEditions(TARGET_CHAMPION_EDITIONS);

        assertEquals(TARGET_CHAMPION_EDITIONS, harness.countEditionsWithChampions());
        harness.assertSimulationHealthy(false);

        int ratedMatches = 0;
        if (club.getPlayedMatches() != null) {
            for (Match match : club.getPlayedMatches()) {
                if (match == null || !Boolean.TRUE.equals(match.getIsPlayed())) {
                    continue;
                }
                var ratings = match.getHomeClubId().equals(club.getId())
                        ? match.getHomePlayerRatings()
                        : match.getAwayPlayerRatings();
                if (ratings != null && ratings.size() == 11) {
                    ratedMatches++;
                }
            }
        }
        assertTrue(ratedMatches > 0, "player club should have rated league matches");

        for (Player player : club.getPlayers()) {
            if (player == null) {
                continue;
            }
            var condition = PlayerConditionService.conditionOf(player);
            assertNotNull(condition.getFitnessPct());
            assertTrue(condition.getFitnessPct() >= 0 && condition.getFitnessPct() <= 100);
            assertTrue(condition.getMorale() >= 0 && condition.getMorale() <= 100);
            assertFalse(Double.isNaN(condition.getFitnessPct()));
        }
    }
}
