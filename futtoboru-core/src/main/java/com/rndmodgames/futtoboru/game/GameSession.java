package com.rndmodgames.futtoboru.game;

import com.rndmodgames.futtoboru.menu.ScreenNavigation;
import com.rndmodgames.futtoboru.menu.UnsavedProgress;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * RF-01: what the screens share about the game being played, in one object the application owns
 * ({@link Futtoboru#getSession()}): the current game, the database it plays on, which in-game screen is shown and
 * what was last saved. Screens get all of it from here and never reach into static state.
 */
public final class GameSession {

    private SaveGame game;
    private final ScreenNavigation navigation = new ScreenNavigation();
    private final UnsavedProgress progress = new UnsavedProgress();

    /** @return the game being played, or null before a new game is made or a save is loaded */
    public SaveGame getGame() {
        return game;
    }

    /** Only {@link Futtoboru#setCurrentGame} sets it (it also gives the game its seed). */
    void setGame(SaveGame game) {
        this.game = game;
    }

    /** @return the game's database (clubs, people, competitions and the data they were built from) */
    public DatabaseLoader database() {
        return DatabaseLoader.getInstance();
    }

    /** @return which in-game screen is shown (outlives one MainGameScreen; reset for a new or loaded game) */
    public ScreenNavigation navigation() {
        return navigation;
    }

    /** @return which game was last saved or loaded, and on which in-game date (U-04d) */
    public UnsavedProgress progress() {
        return progress;
    }
}
