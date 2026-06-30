package com.rndmodgames.futtoboru.harness;

import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Test logging profiles: quiet for fast PR gate, verbose for nightly/soak debugging.
 *
 * <p>Set {@code -Dfuttoboru.test.logProfile=verbose} (nightly/soak Maven profiles).
 * Default {@code quiet} keeps master debug switch off; {@code error()} still emits.
 */
public final class HarnessLogProfile {

    public static final String PROPERTY = "futtoboru.test.logProfile";
    public static final String QUIET = "quiet";
    public static final String VERBOSE = "verbose";

    private HarnessLogProfile() {
    }

    public static void applyFromSystemProperty() {
        if (VERBOSE.equalsIgnoreCase(System.getProperty(PROPERTY, QUIET))) {
            applyVerbose();
        } else {
            applyQuiet();
        }
    }

    public static void applyQuiet() {
        DebugLogManager.getInstance().setAllEnabled(false);
    }

    public static void applyVerbose() {
        DebugLogManager.getInstance().setAllEnabled(true);
        for (String category : DebugLogManager.getInstance().getAllCategories()) {
            DebugLogManager.getInstance().setCategoryEnabled(category, true);
        }
    }
}
