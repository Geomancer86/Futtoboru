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

    private SaveGame game;

    @BeforeEach
    void setUp() {
        UnsavedProgress.reset();
        game = new SaveGame();
        game.setGameDate(LocalDateTime.of(1888, 9, 8, 9, 0));
    }

    @AfterEach
    void tearDown() {
        UnsavedProgress.reset();
    }

    @Test
    void newGameNeverSavedIsUnsaved() {
        assertTrue(UnsavedProgress.isUnsaved(game));
    }

    @Test
    void savedGameOnSameDayIsSaved() {
        UnsavedProgress.markSaved(game);
        assertFalse(UnsavedProgress.isUnsaved(game));
    }

    @Test
    void continuingAfterSaveIsUnsaved() {
        UnsavedProgress.markSaved(game);
        game.setGameDate(game.getGameDate().plusDays(1));
        assertTrue(UnsavedProgress.isUnsaved(game));
        UnsavedProgress.markSaved(game);
        assertFalse(UnsavedProgress.isUnsaved(game));
    }

    @Test
    void anotherGameOnTheSameDateIsUnsaved() {
        UnsavedProgress.markSaved(game);
        SaveGame other = new SaveGame();
        other.setGameDate(game.getGameDate());
        assertTrue(UnsavedProgress.isUnsaved(other));
    }

    @Test
    void noGameHasNothingToLose() {
        assertFalse(UnsavedProgress.isUnsaved(null));
    }
}
