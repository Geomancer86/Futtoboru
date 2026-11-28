package com.rndmodgames.futtoboru.system.save;

import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;

/**
 * U-06: the decisions behind the save and load dialogs, kept out of the widgets so they can be tested headless.
 * The Save dialog asks {@link #check} before writing; the Load dialog deletes a row with {@link #delete}; quitting
 * waits for a running autosave with {@link #awaitAutosave}.
 */
public final class SaveSlots {

    /** How long quitting waits for an autosave that is still being written. */
    public static final long QUIT_WAIT_MILLIS = 5000;

    /** What the Save button does with the typed name. */
    public enum NameCheck {
        /** Nothing typed (or only spaces): refused with a message. */
        EMPTY,
        /** No save has that name: written at once. */
        NEW,
        /** A save with that name exists: the player confirms the overwrite first. */
        OVERWRITE
    }

    private SaveSlots() {
    }

    /** @return what saving under {@code saveName} would do */
    public static NameCheck check(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            return NameCheck.EMPTY;
        }
        return SaveLoadSystem.saveExists(saveName) ? NameCheck.OVERWRITE : NameCheck.NEW;
    }

    /** Deletes the save of a load-list row (the autosave included). @return true when the file is gone */
    public static boolean delete(SaveSummary save) {
        return save != null && SaveLoadSystem.deleteSave(save.getFile());
    }

    /**
     * Waits up to {@code timeoutMillis} for the autosave being written, so the app never quits mid-write.
     *
     * @return false when the write was still running after the wait (logged)
     */
    public static boolean awaitAutosave(Autosave autosave, long timeoutMillis) {
        if (autosave == null || autosave.awaitWrite(timeoutMillis)) {
            return true;
        }
        Log.warn(Log.SYSTEM_INIT, "SaveSlots", "autosave still writing after " + timeoutMillis + " ms; quitting anyway");
        return false;
    }
}
