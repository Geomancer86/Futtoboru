package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;

/**
 * A-01 (PlayerBot, long scenario seed 101): once a cup's ties were all played no club listed one of them any more,
 * and the daily check took the won edition for one never drawn: a second bracket of the 1888-89 FA Cup, on the
 * 1889-90 dates, was played alongside the 1889-90 edition (clubs with two cup ties on 1 February 1890).
 */
class CupDrawnOnceTests extends BaseTest {

    @Test
    void aCupWhoseTiesArePlayedOrWonIsNotDrawnAgain() {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        assertFalse(world.authorityManager.needsCupDraw(cup.edition), "drawn: its ties are in the fixtures");

        MatchResultRecorder recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
        for (String path : new String[] { "R1M1", "R1M2", "R2M1" }) {
            Match m = cup.byPath(path);
            EngineTestWorld.score(m, 1, 0);
            recorder.record(m);
        }
        for (Club c : world.clubs) {
            for (Match m : c.getScheduledMatches()) {
                assertFalse(cup.edition.getId().equals(m.getCompetitionEditionId()), "a tie left at " + c.getName());
            }
        }
        assertFalse(world.authorityManager.needsCupDraw(cup.edition), "every tie played: drawn already");

        cup.edition.setChampionsId(world.club(0).getId());
        for (Club c : world.clubs) {
            c.getPlayedMatches().clear();
        }
        assertFalse(world.authorityManager.needsCupDraw(cup.edition), "won: never drawn again");

        cup.edition.setChampionsId(null);
        assertTrue(world.authorityManager.needsCupDraw(cup.edition), "no ties anywhere and no winner: to draw");
    }
}
