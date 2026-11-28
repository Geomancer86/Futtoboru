package com.rndmodgames.futtoboru.menu;

import java.time.LocalDateTime;
import java.util.Objects;

import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Remembers which game was last saved or loaded, and at which in-game date (U-04d). RF-01: one per
 * {@code GameSession} (was static).
 *
 * <p>A game has unsaved progress when it was never saved or loaded in this session, or when its date moved since
 * (every Continue advances the date). The in-game Options use it to ask before Load / Quit throws progress away.
 * Changes made on the same day as the save (a draw, a job reply) are not tracked: day granularity is enough for a
 * "you will lose progress" warning.
 */
public final class UnsavedProgress {

    private SaveGame savedGame;
    private LocalDateTime savedDate;

    /** Call after the game was written to disk or read from it. */
    public synchronized void markSaved(SaveGame game) {
        savedGame = game;
        savedDate = game != null ? game.getGameDate() : null;
    }

    /** @return true if {@code game} has progress that a load or quit would lose */
    public synchronized boolean isUnsaved(SaveGame game) {
        if (game == null) {
            return false;
        }
        return game != savedGame || !Objects.equals(savedDate, game.getGameDate());
    }

    /** Forgets the last save (tests). */
    synchronized void reset() {
        savedGame = null;
        savedDate = null;
    }
}
