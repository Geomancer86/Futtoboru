package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;

/**
 * PL-5 / H-3 — played matches are available for history navigation data path.
 */
@Tag("r1888")
class MatchHistoryNavigationIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void playedMatchStoredWithScoreForHistoryView() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        harness.advanceUntilMatchPlayableForClub(club.getId(), 120);
        harness.setClubLineup(club.getId(), harness.bestElevenPlayerIds(club));
        Match match = harness.kickOffNextMatchForCurrentClub();
        assertNotNull(match);
        assertNotNull(match.getHomeGoals());
        assertNotNull(match.getAwayGoals());

        assertFalse(club.getPlayedMatches().isEmpty());
        Match latest = club.getPlayedMatches().get(club.getPlayedMatches().size() - 1);
        assertTrue(latest.getHomeGoals() >= 0);
        assertTrue(latest.getAwayGoals() >= 0);
    }
}
