package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * U-06 / D18: the rotating autosave slot is written when the game reaches the 1st of a month and on the season
 * rollover day, never on other days and never twice for one date; it is listed and loads like any save; saves can
 * be deleted and an existing slot is detected before overwriting. Every file goes to a temporary folder.
 */
class AutosaveTests {

    private static Futtoboru application;

    @TempDir
    static Path folder;

    @BeforeAll
    static void preload() {
        SaveLoadSystem.setGameFolder(folder.toFile());
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru(SeasonMemo.SEED); // T-01: the seed of the shared season memo
        new HeadlessApplication(application, config);
        BaseTest.awaitHeadlessCreate();
    }

    @AfterEach
    void backToTheTestFolder() {
        SaveLoadSystem.setGameFolder(folder.toFile());
    }

    @AfterAll
    static void restore() {
        SaveLoadSystem.setGameFolder(null);
    }

    private static FuttoboruGameEngine engine() {
        return application.getGameEngine();
    }

    /** A new game (1 Apr 1888) with the autosave on (it is off without a window). */
    private static SaveGame newGame() {
        SaveGame game = NewGameFixture.newGame(application);
        assertFalse(engine().getAutosave().isEnabled(), "off in headless tests unless turned on");
        engine().getAutosave().setEnabled(true);
        return game;
    }

    /** Plays day by day until {@code stop} is true or {@code maxDays} pass; @return the dates autosaved */
    private static List<LocalDate> playRecording(SaveGame game, int maxDays, java.util.function.BooleanSupplier stop) {
        List<LocalDate> written = new ArrayList<>();
        Autosave autosave = engine().getAutosave();
        for (int day = 0; day < maxDays && !stop.getAsBoolean(); day++) {
            int before = autosave.getWrites();
            NewGameFixture.play(application, game, 1, true);
            if (autosave.getWrites() > before) {
                assertEquals(before + 1, autosave.getWrites(), "one autosave per day at most");
                assertEquals(game.getGameDate().toLocalDate(), autosave.getLastDate());
                written.add(autosave.getLastDate());
            }
        }
        assertTrue(autosave.awaitWrite(10_000), "the autosave file was written");
        return written;
    }

    @Test
    void dueOnTheFirstOfAMonthAndOnARolloverOnly() {
        assertTrue(Autosave.isDue(LocalDate.of(1888, 5, 1), false));
        assertTrue(Autosave.isDue(LocalDate.of(1889, 1, 1), false));
        assertFalse(Autosave.isDue(LocalDate.of(1888, 5, 2), false));
        assertFalse(Autosave.isDue(LocalDate.of(1888, 4, 30), false));
        assertTrue(Autosave.isDue(LocalDate.of(1889, 4, 21), true));
        assertFalse(Autosave.isDue(null, true));
    }

    @Test
    @Tag("slow")
    void writtenOnTheFirstOfEachMonthOnceAndLoadsAsTheSameGame() throws IOException {
        SaveGame game = newGame();
        SaveLoadSystem.deleteSave(SaveLoadSystem.autosaveFile());

        // 1 Apr -> 1 Jun 1888: the 1st of May and of June, no other day
        List<LocalDate> written = playRecording(game, 61, () -> false);
        assertEquals(List.of(LocalDate.of(1888, 5, 1), LocalDate.of(1888, 6, 1)), written);
        assertEquals(LocalDate.of(1888, 6, 1), game.getGameDate().toLocalDate());

        // idempotent per date: asking again on the same day writes nothing
        Autosave autosave = engine().getAutosave();
        int writes = autosave.getWrites();
        assertFalse(autosave.afterDay(game, engine().getAuthorityManager().getSeasonRollovers()));
        assertEquals(writes, autosave.getWrites());

        // the autosave does not take over the player's save slot
        assertNull(game.getSaveName());
        assertFalse(Boolean.TRUE.equals(game.getIsSaved()));

        // listed like any save, under the localized label
        File file = SaveLoadSystem.autosaveFile();
        assertTrue(file.isFile());
        SaveSummary row = SaveLoadSystem.listSaves().stream().filter(SaveSummary::isAutosave).findFirst().orElse(null);
        assertNotNull(row, "the autosave is in the load list");
        assertEquals(LanguageModLoader.getValue(SaveLoadSystem.AUTOSAVE_LABEL_KEY), row.getName());
        assertEquals(game.getGameDate(), row.getGameDate());
        assertTrue(row.isLoadable());

        // round trip: the same game, and no player slot name (the next Save asks for one)
        GameFingerprint before = GameFingerprint.of(game);
        LoadResult result = SaveLoadSystem.loadGame(file);
        assertTrue(result.isLoaded(), result.toString());
        assertEquals(new ArrayList<String>(), result.getProblems());
        assertEquals(before.toString(), GameFingerprint.of(result.getGame()).toString());
        assertNull(result.getGame().getSaveName());

        // a loaded game reaching the same 1st again is autosaved (another game object)
        NewGameFixture.bind(application, result.getGame());
        engine().getAutosave().setEnabled(true);
        assertTrue(engine().getAutosave().afterDay(result.getGame(), 0));
        assertTrue(engine().getAutosave().awaitWrite(10_000));
    }

    /** T-01: the seeded season is played once per test JVM ({@link SeasonMemo}), recording the autosave days. */
    @Test
    @Tag("slow")
    void writtenOnTheSeasonRolloverDay() {
        SeasonMemo season = SeasonMemo.get(application);
        List<LocalDate> written = season.autosaveDates();
        assertTrue(season.rollovers() > 0, "the season rolled over");
        LocalDate rollover = season.rolloverDate();

        assertEquals(rollover, written.get(written.size() - 1), "autosaved on the rollover day " + written);
        LocalDate first = LocalDate.of(1888, 5, 1);
        List<LocalDate> expected = new ArrayList<>();
        for (LocalDate d = first; !d.isAfter(rollover); d = d.plusMonths(1)) {
            expected.add(d);
        }
        if (!expected.contains(rollover)) {
            expected.add(rollover);
        }
        assertEquals(expected, written, "every 1st of a month, and the rollover day, once each");
    }

    @Test
    @Tag("slow")
    void aFailedAutosaveIsLoggedAndTheGameGoesOn() throws IOException {
        SaveGame game = newGame();
        // the saves folder can't be made: a file is in its place
        Path broken = Files.createDirectories(folder.resolve("broken-game-folder"));
        Files.write(broken.resolve("savegames"), "not a folder".getBytes(StandardCharsets.UTF_8));
        SaveLoadSystem.setGameFolder(broken.toFile());
        List<String> errors = new ArrayList<>();
        Log.ErrorListener recorder = (category, tag, message, error) -> errors.add(message);
        Log.addErrorListener(recorder);
        try {
            NewGameFixture.play(application, game, 30, true); // 1 Apr -> 1 May; no exception out of the day
            assertTrue(engine().getAutosave().awaitWrite(10_000));
        } finally {
            Log.removeErrorListener(recorder);
        }
        assertEquals(LocalDate.of(1888, 5, 1), game.getGameDate().toLocalDate(), "the day was played");
        assertEquals(LocalDate.of(1888, 5, 1), engine().getAutosave().getLastDate(), "the autosave was tried");
        assertTrue(errors.stream().anyMatch(m -> m.contains("autosave")), errors.toString());
    }

    @Test
    void savesAreDeletedAndAnExistingSlotIsDetected() throws IOException {
        SaveGame game = NewGameFixture.newGame(application);
        assertFalse(SaveLoadSystem.saveExists("To delete"));
        File file = SaveLoadSystem.saveGame(game, "To delete");
        assertTrue(SaveLoadSystem.saveExists("To delete"), "overwrite check");
        assertTrue(SaveLoadSystem.saveExists("  To delete "), "same slot");
        assertTrue(SaveLoadSystem.listSaves().stream().anyMatch(s -> s.getFile().equals(file)));

        assertTrue(SaveLoadSystem.deleteSave(file));
        assertFalse(file.exists());
        assertFalse(SaveLoadSystem.saveExists("To delete"));
        assertFalse(SaveLoadSystem.listSaves().stream().anyMatch(s -> s.getFile().equals(file)));

        // only save files of the saves folder are deleted
        File outside = folder.resolve("outside.json").toFile();
        Files.write(outside.toPath(), "{}".getBytes(StandardCharsets.UTF_8));
        assertFalse(SaveLoadSystem.deleteSave(outside));
        assertTrue(outside.isFile());
        File notASave = new File(SaveLoadSystem.getSavesFolder(), "notes.txt");
        Files.write(notASave.toPath(), "x".getBytes(StandardCharsets.UTF_8));
        assertFalse(SaveLoadSystem.deleteSave(notASave));
        assertTrue(notASave.isFile());
        assertFalse(SaveLoadSystem.deleteSave(null));

        // a player's slot never lands on the autosave file
        assertEquals("save_Autosave.json", SaveLoadSystem.fileNameFor("Autosave"));
        assertFalse(SaveLoadSystem.isAutosave(SaveLoadSystem.saveFileFor("autosave")));
    }
}
