package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;

class MatchLineupPreviewIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void customLineupUsedAtKickOff() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        harness.advanceUntilMatchPlayableForClub(club.getId(), 120);
        List<Long> custom = harness.bestElevenPlayerIds(club);
        harness.setClubLineup(club.getId(), custom);

        Match played = harness.kickOffNextMatchForCurrentClub();
        assertNotNull(played);
        List<Long> used = played.getHomeClubId().equals(club.getId())
                ? played.getHomeLineupPlayerIds()
                : played.getAwayLineupPlayerIds();
        assertEquals(custom.size(), used.size());
        assertEquals(custom, used);
    }
}
