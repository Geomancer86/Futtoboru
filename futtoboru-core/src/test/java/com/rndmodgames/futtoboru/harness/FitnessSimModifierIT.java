package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FitnessSimModifierIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void lowFitnessLineupUnderperformsFullFitness() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        var club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        var lineup = harness.bestElevenPlayerIds(club);
        if (lineup.isEmpty()) {
            return;
        }
        long playerId = lineup.get(0);
        harness.setClubLineup(club.getId(), lineup);

        int fullFitnessWins = harness.countHomeWinsWithLineup(club.getId(), 2L, lineup, 80, 99L);
        harness.setPlayerFitness(playerId, 15);
        int lowFitnessWins = harness.countHomeWinsWithLineup(club.getId(), 2L, lineup, 80, 99L);

        assertTrue(fullFitnessWins >= lowFitnessWins);
    }
}
