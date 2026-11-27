package com.rndmodgames.futtoboru.system.save;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * What loading a save file gave: the game (when it could be loaded), why not otherwise, and the references that
 * didn't resolve (data removed since the save was made). Loading never throws.
 */
public class LoadResult {

    public enum Status {
        /** Loaded (maybe with {@link #getProblems() problems}: references that didn't resolve were left out). */
        OK,
        /** Made with an older version of Futtoboru (a v0.4 save, or a version no migration step reaches). */
        OLDER_VERSION,
        /** Made with a newer version of Futtoboru. */
        NEWER_VERSION,
        /** Not a Futtoboru save, or damaged. */
        UNREADABLE
    }

    private final Status status;
    private final SaveGame game;
    private final String message;
    private final List<String> problems;

    LoadResult(Status status, SaveGame game, String message, List<String> problems) {
        this.status = status;
        this.game = game;
        this.message = message;
        this.problems = problems == null ? new ArrayList<>() : problems;
    }

    static LoadResult failed(Status status, String message) {
        return new LoadResult(status, null, message, null);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isLoaded() {
        return status == Status.OK && game != null;
    }

    /** @return the loaded game, or null when it couldn't be loaded */
    public SaveGame getGame() {
        return game;
    }

    /** @return a sentence for the player when the save couldn't be loaded (null when it was) */
    public String getMessage() {
        return message;
    }

    /** @return the references that didn't resolve on load (empty when everything resolved) */
    public List<String> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    @Override
    public String toString() {
        return status + (message != null ? ": " + message : "") + (problems.isEmpty() ? "" : " " + problems);
    }
}
