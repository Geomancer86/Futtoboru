package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * P2-8: the game builds its AuthorityManager in {@code Futtoboru.create()}, before any game exists. The manager must
 * still work on the game started (or loaded) later; it used to keep the empty game from startup, so the daily
 * competition check returned at once in real play.
 */
class AuthorityManagerCurrentGameTests extends BaseTest {

    @Test
    void managerBuiltBeforeTheGameUsesTheGameStartedLater() {
        SaveGame previous = application.getCurrentGame();
        try {
            application.setCurrentGame(null);
            AuthorityManager manager = new AuthorityManager(application);   // as in Futtoboru.create()
            assertNull(manager.currentGame());

            SaveGame started = NewGameFixture.newGame(application);
            assertSame(started, application.getCurrentGame());
            assertSame(started, manager.currentGame(), "the manager must follow the game being played");
            assertDoesNotThrow(manager::checkCompetitionsSchedule);
            assertSame(started.getMainAuthority(), manager.mainAuthority, "the daily check ran on the started game");

            SaveGame loaded = new SaveGame();
            loaded.setMainAuthority(started.getMainAuthority());
            application.setCurrentGame(loaded);                               // as after loading a save
            assertSame(loaded, manager.currentGame(), "the manager must follow a loaded game");
        } finally {
            application.setCurrentGame(previous);
        }
    }
}
