package com.rndmodgames.futtoboru.menu;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.system.SaveGame;

/** U-04d: when the in-game Options must ask before Load / Quit. */
class UnsavedProgressTests {

    private final UnsavedProgress progress = new UnsavedProgress();
    private SaveGame game;

    @BeforeEach
    void setUp() {
        progress.reset();
        game = new SaveGame();
        game.setGameDate(LocalDateTime.of(1888, 9, 8, 9, 0));
    }

    @AfterEach
    void tearDown() {
        progress.reset();
    }

    @Test
    void newGameNeverSavedIsUnsaved() {
        assertTrue(progress.isUnsaved(game));
    }

    @Test
    void savedGameOnSameDayIsSaved() {
        progress.markSaved(game);
        assertFalse(progress.isUnsaved(game));
    }

    @Test
    void continuingAfterSaveIsUnsaved() {
        progress.markSaved(game);
        game.setGameDate(game.getGameDate().plusDays(1));
        assertTrue(progress.isUnsaved(game));
        progress.markSaved(game);
        assertFalse(progress.isUnsaved(game));
    }

    @Test
    void anotherGameOnTheSameDateIsUnsaved() {
        progress.markSaved(game);
        SaveGame other = new SaveGame();
        other.setGameDate(game.getGameDate());
        assertTrue(progress.isUnsaved(other));
    }

    @Test
    void noGameHasNothingToLose() {
        assertFalse(progress.isUnsaved(null));
    }
}
