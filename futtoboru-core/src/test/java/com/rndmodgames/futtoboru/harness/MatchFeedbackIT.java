package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.MatchRatingsCalculator;

class MatchFeedbackIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void kickOffProducesElevenRatings() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        harness.advanceUntilMatchPlayableForClub(club.getId(), 120);
        List<Long> lineup = harness.bestElevenPlayerIds(club);
        harness.setClubLineup(club.getId(), lineup);

        Match played = harness.kickOffNextMatchForCurrentClub();
        assertNotNull(played);
        Map<Long, Float> ratings = harness.getLastMatchRatings(club.getId());
        assertEquals(11, ratings.size());
        for (float rating : ratings.values()) {
            assertTrue(rating >= MatchRatingsCalculator.MIN_RATING);
            assertTrue(rating <= MatchRatingsCalculator.MAX_RATING);
        }
    }
}
