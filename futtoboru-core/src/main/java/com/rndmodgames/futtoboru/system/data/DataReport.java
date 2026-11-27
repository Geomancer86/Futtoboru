package com.rndmodgames.futtoboru.system.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Errors and warnings found while reading the game data packs (same rules as {@code tools/data/validate.py}).
 *
 * An error means a record (or a file) was skipped; a warning is suspicious but possible data for the human review.
 * Nothing here ever stops the game.
 */
public class DataReport {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public void error(String where, String message) {
        errors.add(where + ": " + message);
    }

    public void warn(String where, String message) {
        warnings.add(where + ": " + message);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /** @return every error on its own line (empty when there are none), handy for test messages */
    public String errorsText() {
        return String.join("\n", errors);
    }

    @Override
    public String toString() {
        return errors.size() + " errors, " + warnings.size() + " warnings";
    }
}
