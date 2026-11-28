package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * V-01: on a match day the Match Result button plays the manager's match. The game clock stands at 04:00 and
 * kick-off is 15:00 (P2-11), so the match used to count as "in the future" and the result screen stayed empty.
 */
class MatchResultButtonTests extends BaseTest {

    @AfterAll
    static void restore() {
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void matchResultPlaysTheMatchOfTheDay() {
        SaveGame game = NewGameFixture.newGame(application);
        FuttoboruGameEngine engine = application.getGameEngine();
        Club club = game.getCurrentClub();

        int days = 0;
        while (engine.getNextGameAction() != FuttoboruGameEngine.MATCH_PREVIEW_ACTION && days < 250) {
            NewGameFixture.play(application, game, 1, true);
            days++;
        }
        assertEquals(FuttoboruGameEngine.MATCH_PREVIEW_ACTION, engine.getNextGameAction(), "no match day in 250 days");
        assertTrue(game.getGameDate().getHour() < 15, "the clock is before kick-off: " + game.getGameDate());

        int played = club.getPlayedMatches().size();
        engine.getMatchResult();
        assertEquals(played + 1, club.getPlayedMatches().size(), "Match Result must play today's match");
    }
}
