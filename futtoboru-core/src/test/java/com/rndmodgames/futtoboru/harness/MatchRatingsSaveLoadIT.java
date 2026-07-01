package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;

class MatchRatingsSaveLoadIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void roundTripPreservesMatchPlayerRatings() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        harness.advanceUntilMatchPlayableForClub(club.getId(), 120);
        harness.setClubLineup(club.getId(), harness.bestElevenPlayerIds(club));

        Match played = harness.kickOffNextMatchForCurrentClub();
        assertNotNull(played);
        Map<Long, Float> before = harness.getLastMatchRatings(club.getId());
        assertEquals(11, before.size());

        SaveGame copy = SaveLoadSystem.copySaveGame(harness.game());
        Match loaded = findMatchInSave(copy, played.getId());
        assertNotNull(loaded);

        Map<Long, Float> ratings = club.getId().equals(loaded.getHomeClubId())
                ? loaded.getHomePlayerRatings()
                : loaded.getAwayPlayerRatings();
        assertEquals(11, ratings.size());
        float beforeTotal = 0f;
        float loadedTotal = 0f;
        for (Float value : before.values()) {
            beforeTotal += value;
        }
        for (Float value : ratings.values()) {
            loadedTotal += value;
        }
        assertEquals(beforeTotal, loadedTotal, 0.05f);
    }

    private static Match findMatchInSave(SaveGame save, Long matchId) {
        if (save.getAllClubs() == null || matchId == null) {
            return null;
        }
        for (Club club : save.getAllClubs()) {
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match != null && matchId.equals(match.getId())) {
                        return match;
                    }
                }
            }
        }
        return null;
    }
}
