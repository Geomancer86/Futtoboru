package com.rndmodgames.futtoboru.system.save;

import java.io.File;
import java.time.LocalDateTime;

/**
 * One row of the load list: the file and the summary fields written at the top of a save (P2-5). A file that is
 * not a loadable v1 save still gets a row ({@link #isLoadable()} false) so the player sees why.
 */
public class SaveSummary {

    private final File file;
    private final Integer saveVersion;
    private final String name;
    private final String manager;
    private final String club;
    private final LocalDateTime gameDate;
    private final LocalDateTime savedAt;
    private final LoadResult.Status status;

    public SaveSummary(File file, Integer saveVersion, String name, String manager, String club,
            LocalDateTime gameDate, LocalDateTime savedAt, LoadResult.Status status) {
        this.file = file;
        this.saveVersion = saveVersion;
        this.name = name;
        this.manager = manager;
        this.club = club;
        this.gameDate = gameDate;
        this.savedAt = savedAt;
        this.status = status;
    }

    public File getFile() {
        return file;
    }

    /** @return the save's format version, or null for a v0.4 save (no version) */
    public Integer getSaveVersion() {
        return saveVersion;
    }

    /** @return the name the player gave the save (the file name for old saves) */
    public String getName() {
        return name;
    }

    public String getManager() {
        return manager;
    }

    public String getClub() {
        return club;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    /** @return when it was saved (the file's time for old saves) */
    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    /** @return {@code OK} when it can be loaded, else why not */
    public LoadResult.Status getStatus() {
        return status;
    }

    public boolean isLoadable() {
        return status == LoadResult.Status.OK;
    }

    /** @return true for the rotating autosave slot (U-06; its name is the localized "Autosave" label) */
    public boolean isAutosave() {
        return com.rndmodgames.futtoboru.system.SaveLoadSystem.isAutosave(file);
    }

    @Override
    public String toString() {
        return name + " (" + file.getName() + ")";
    }
}
