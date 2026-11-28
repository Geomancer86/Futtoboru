package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.save.SaveSlots.NameCheck;

/**
 * U-06: the Save dialog's name check (empty refused, an existing save asks before overwriting), the Load dialog's
 * delete of a row (autosave included, nothing outside the saves folder) and the wait for an autosave on quit.
 * Every file goes to a temporary folder.
 */
class SaveSlotsTests {

    @TempDir
    Path folder;

    @BeforeEach
    void useTheTestFolder() {
        SaveLoadSystem.setGameFolder(folder.toFile());
    }

    @AfterEach
    void restore() {
        SaveLoadSystem.setGameFolder(null);
    }

    private static File write(File file) throws IOException {
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), "{}".getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static SaveSummary row(File file) {
        return new SaveSummary(file, SaveCodec.SAVE_VERSION, file.getName(), null, null, null, LocalDateTime.now(),
                LoadResult.Status.OK);
    }

    @Test
    void anEmptyNameIsRefused() {
        assertEquals(NameCheck.EMPTY, SaveSlots.check(null));
        assertEquals(NameCheck.EMPTY, SaveSlots.check(""));
        assertEquals(NameCheck.EMPTY, SaveSlots.check("   "));
    }

    @Test
    void aNewNameIsWrittenAndAnExistingOneAsksFirst() throws IOException {
        assertEquals(NameCheck.NEW, SaveSlots.check("Preston 1888"));
        write(SaveLoadSystem.saveFileFor("Preston 1888"));
        assertEquals(NameCheck.OVERWRITE, SaveSlots.check("Preston 1888"));
        assertEquals(NameCheck.OVERWRITE, SaveSlots.check("  Preston 1888  "), "the name is trimmed like the save");
        assertEquals(NameCheck.NEW, SaveSlots.check("Preston 1889"));
    }

    @Test
    void theAutosaveSlotIsNeverOverwrittenByAPlayerName() throws IOException {
        write(SaveLoadSystem.autosaveFile());
        assertEquals(NameCheck.NEW, SaveSlots.check("autosave"), "a typed 'autosave' goes to save_autosave.json");
    }

    @Test
    void aRowAndTheAutosaveAreDeleted() throws IOException {
        File save = write(SaveLoadSystem.saveFileFor("Everton"));
        File autosave = write(SaveLoadSystem.autosaveFile());

        assertTrue(SaveSlots.delete(row(save)));
        assertFalse(save.exists());
        assertEquals(NameCheck.NEW, SaveSlots.check("Everton"), "the deleted name is free again");

        assertTrue(SaveSlots.delete(row(autosave)));
        assertFalse(autosave.exists());
    }

    @Test
    void nothingOutsideTheSavesFolderIsDeleted() throws IOException {
        File outside = write(new File(folder.toFile(), "notes.json"));
        assertFalse(SaveSlots.delete(row(outside)));
        assertTrue(outside.exists());
        assertFalse(SaveSlots.delete(null));
    }

    @Test
    void quittingWithoutARunningAutosaveDoesNotWait() {
        assertTrue(SaveSlots.awaitAutosave(null, 10));
        assertTrue(SaveSlots.awaitAutosave(new Autosave(), 10), "no autosave started");
    }
}
