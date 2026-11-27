package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;

/**
 * P2-3: a 12-club cup (byes into round 2) is played through the single result path to a winner.
 * With the old generator the winner of R2M3 had no next tie and the cup never finished.
 */
class CupBracketPlayThroughTests extends BaseTest {

    @Test
    void twelveClubCupIsPlayedToAWinner() {
        EngineTestWorld world = new EngineTestWorld(application, 12);
        MatchResultRecorder recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
        EngineTestWorld.Cup cup = world.cup(world.clubs);

        int played = 0;
        boolean progress = true;
        while (progress) {
            progress = false;
            for (Match m : cup.matches) {
                if (!Boolean.TRUE.equals(m.getIsPlayed()) && m.getHomeClubId() != null && m.getAwayClubId() != null) {
                    EngineTestWorld.score(m, 2, 1); // home side wins: no replays
                    recorder.record(m);
                    played++;
                    progress = true;
                }
            }
        }

        assertEquals(11, played, "12 clubs: 11 ties");
        for (Match m : cup.matches) {
            assertTrue(Boolean.TRUE.equals(m.getIsPlayed()), m.getBracketPath() + " played");
        }
        Match fin = cup.byPath("R4M1");
        assertNotNull(fin, "the final is round 4");
        assertEquals(fin.getHomeClubId(), cup.edition.getChampionsId());
        assertEquals(fin.getAwayClubId(), cup.edition.getRunnersUpId());
    }
}
