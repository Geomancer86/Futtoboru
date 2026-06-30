package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;

/**
 * B1 exit gate: sim uses persisted XI; strong XI outperforms weak XI.
 */
class StartingElevenIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void simulatedMatchRecordsElevenPlayerLineup() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        harness.simulateDays(30);

        Match played = harness.findLastPlayedLeagueMatch(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        if (played == null) {
            harness.simulateUntilPrimaryLeagueComplete();
            played = harness.findLastPlayedLeagueMatch(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        }
        assertNotNull(played);

        List<Long> lineupIds = played.getHomeClubId().equals(GameTestHarness.PRESTON_NORTH_END_CLUB_ID)
                ? played.getHomeLineupPlayerIds()
                : played.getAwayLineupPlayerIds();
        assertEquals(11, lineupIds.size());
    }

    @Test
    void weakLineupLosesMoreOftenThanStrongLineup() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club home = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Club away = harness.game().getClubById(2L);
        assertNotNull(home);
        assertNotNull(away);

        List<Long> strongIds = harness.bestElevenPlayerIds(home);
        List<Long> weakIds = harness.worstElevenPlayerIds(home);

        int strongWins = harness.countHomeWinsWithLineup(home.getId(), away.getId(), strongIds, 150, 42L);
        int weakWins = harness.countHomeWinsWithLineup(home.getId(), away.getId(), weakIds, 150, 42L);
        assertTrue(strongWins > weakWins,
                "strongWins=" + strongWins + " weakWins=" + weakWins);
    }

    @Test
    void setClubLineupIsUsedOnNextSimulation() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        List<Long> customLineup = harness.bestElevenPlayerIds(club);
        harness.setClubLineup(club.getId(), customLineup);

        MatchSimulator simulator = new MatchSimulator(harness.application());
        Match next = harness.findNextLeagueMatchForClub(club.getId());
        assertNotNull(next);
        simulator.simulateMatch(next);

        List<Long> used = next.getHomeClubId().equals(club.getId())
                ? next.getHomeLineupPlayerIds()
                : next.getAwayLineupPlayerIds();
        FormationTemplate template = FormationRegistry.getDefaultTemplate();
        assertEquals(customLineup.size(), used.size());
        assertTrue(club.getTacticsSetup().hasCompleteValidLineup(club, template));
    }
}
