package com.rndmodgames.localization;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.PreferencesManager;

/**
 * Regression test for a real launch-blocking bug found 2026-07-19 while investigating "build and run
 * doesn't work": every automated test loads game data straight off the filesystem/classpath and never
 * exercises the actual packaged jar, so a bundle filename whose case didn't match what
 * {@code com.badlogic.gdx.utils.I18NBundle} looks up (case-sensitive inside a jar's zip index, even on
 * case-insensitive filesystems like Windows/NTFS where a loose-classpath test run would never notice)
 * went undetected by the full green test suite — the desktop jar crashed on startup with
 * {@code MissingResourceException: Can't find bundle for base file handle mods/language/BaseBundle,
 * locale en_US} on every run, before the main menu ever appeared.
 *
 * <p>This test does not need libGDX/a headless app context at all — it deliberately uses plain
 * {@link File#list()} (which preserves the on-disk case exactly, on every OS) so it fails the same way
 * regardless of host filesystem case-sensitivity, unlike a test that goes through {@code Gdx.files}.
 */
class LanguageBundleFileNamingTest {

    private static final File LANGUAGE_DIR =
            new File("src/main/resources/mods/language");

    @Test
    void everyAvailableLanguageHasAnExactCaseMatchingBundleFile() {
        assertTrue(LANGUAGE_DIR.isDirectory(),
                "Language bundle directory not found at " + LANGUAGE_DIR.getAbsolutePath()
                        + " — run tests from the futtoboru-core module root");

        String[] listed = LANGUAGE_DIR.list();
        assertTrue(listed != null && listed.length > 0, "No files found in " + LANGUAGE_DIR);
        Set<String> actualFileNames = new HashSet<>(Arrays.asList(listed));

        for (Language language : LanguageModLoader.getAllAvailableLanguages()) {
            Locale locale = PreferencesManager.parseLocaleTag(language.getId());
            // Mirrors com.badlogic.gdx.utils.I18NBundle#toFileHandle's naming scheme exactly.
            String expectedFileName = "BaseBundle_" + locale.getLanguage() + "_" + locale.getCountry() + ".properties";

            if (!actualFileNames.contains(expectedFileName)) {
                fail("Missing or wrong-case bundle file for language '" + language.getId() + "': expected exact "
                        + "filename '" + expectedFileName + "' but the directory contains " + actualFileNames
                        + ". I18NBundle resource lookups are case-sensitive inside a packaged jar — this will crash "
                        + "the desktop build on startup even though a case-insensitive filesystem hides the problem "
                        + "here.");
            }
        }
    }
}
