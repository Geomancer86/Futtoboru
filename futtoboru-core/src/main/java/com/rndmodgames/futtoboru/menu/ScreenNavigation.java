package com.rndmodgames.futtoboru.menu;

/**
 * RF-01: which in-game screen is shown (was MainMenuManager's static CURRENT_SCREEN, PREVIOUS_SCREEN and
 * BEFORE_MATCH_SCREEN). The game session owns it, so it outlives one MainGameScreen (back from Settings or the match
 * viewer opens the same screen); a new or loaded game {@link #reset()}s it to Home.
 */
public final class ScreenNavigation {

    private GameScreenId current = GameScreenId.HOME;
    private GameScreenId previous = GameScreenId.NONE;
    private GameScreenId beforeMatch = GameScreenId.NONE;

    /** A new or loaded game opens on Home. */
    public void reset() {
        current = GameScreenId.HOME;
        previous = GameScreenId.NONE;
        beforeMatch = GameScreenId.NONE;
    }

    /** @return the screen shown */
    public GameScreenId current() {
        return current;
    }

    /** @return the screen shown before the current one */
    public GameScreenId previous() {
        return previous;
    }

    /** @return the screen Continue goes back to from a match result */
    public GameScreenId beforeMatch() {
        return beforeMatch;
    }

    /** Shows {@code screen}: the current one becomes the previous one. */
    void show(GameScreenId screen) {
        previous = current;
        current = screen;
    }

    void setBeforeMatch(GameScreenId screen) {
        beforeMatch = screen;
    }
}
