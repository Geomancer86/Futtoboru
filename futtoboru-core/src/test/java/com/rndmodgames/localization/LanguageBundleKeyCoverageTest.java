package com.rndmodgames.localization;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Regression test for a real bug found 2026-07-19 while end-to-end testing the New Game -> Start Game
 * flow by hand (see {@code docs/registries/blockers-resolved.yaml}): 36 keys referenced by
 * {@code LanguageModLoader.getValue("...")} calls across {@code MainGameScreen} and its tabs (Squad,
 * Authority, Personal Details, Schedule, Finances, etc.) had never been added to any language bundle,
 * so every one of them rendered as a raw, untranslated key in the live game the moment a player
 * actually reached those screens -- invisible to the full green test suite because nothing constructs
 * those UI classes in a test.
 *
 * <p>This test can't (and doesn't try to) construct the actual UI screens -- see
 * {@code GameTestHarness}'s Javadoc and {@code AGENTS.md} on why real Scene2D/VisUI screen construction
 * is out of scope for R-1888 automated tests. Instead it does the next best, fully headless thing: a
 * static-analysis pass over every {@code .java} source file for literal
 * {@code LanguageModLoader.getValue("some_key")} calls, cross-checked against every key actually
 * defined in every shipped language bundle. Any key referenced by code but missing from a bundle is
 * exactly the class of bug this test would have caught immediately.
 */
class LanguageBundleKeyCoverageTest {

    private static final Path SOURCE_ROOT = Path.of("src/main/java");
    private static final File LANGUAGE_DIR = new File("src/main/resources/mods/language");

    private static final Pattern GET_VALUE_CALL =
            Pattern.compile("LanguageModLoader\\.getValue\\(\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern PROPERTY_LINE = Pattern.compile("^\\s*([A-Za-z0-9_.]+)\\s*=");

    @Test
    void everyLiteralGetValueKeyExistsInEveryLanguageBundle() throws IOException {
        Set<String> usedKeys = findLiteralGetValueKeys();
        assertTrue(usedKeys.size() > 50,
                "Sanity check: expected well over 50 distinct getValue(...) keys in the codebase, found "
                        + usedKeys.size() + " -- SOURCE_ROOT may be wrong: " + SOURCE_ROOT.toAbsolutePath());

        for (Language language : LanguageModLoader.getAllAvailableLanguages()) {
            java.util.Locale locale = com.rndmodgames.PreferencesManager.parseLocaleTag(language.getId());
            String bundleFileName = "BaseBundle_" + locale.getLanguage() + "_" + locale.getCountry() + ".properties";
            File bundleFile = new File(LANGUAGE_DIR, bundleFileName);

            assertTrue(bundleFile.isFile(),
                    "Bundle file not found: " + bundleFile.getAbsolutePath()
                            + " (see LanguageBundleFileNamingTest for the case-sensitivity story here)");

            Set<String> definedKeys = readDefinedKeys(bundleFile);

            Set<String> missing = new HashSet<>(usedKeys);
            missing.removeAll(definedKeys);

            assertTrue(missing.isEmpty(),
                    "Bundle '" + bundleFileName + "' is missing " + missing.size() + " key(s) that are "
                            + "actually referenced via LanguageModLoader.getValue(...) in the codebase -- these "
                            + "would render as raw, untranslated keys in the live game: " + new TreeSet<>(missing));
        }
    }

    private static Set<String> findLiteralGetValueKeys() throws IOException {
        assertTrue(Files.isDirectory(SOURCE_ROOT),
                "Source root not found at " + SOURCE_ROOT.toAbsolutePath()
                        + " -- run tests from the futtoboru-core module root");

        Set<String> keys = new HashSet<>();
        try (Stream<Path> paths = Files.walk(SOURCE_ROOT)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                Matcher matcher = GET_VALUE_CALL.matcher(content);
                while (matcher.find()) {
                    keys.add(matcher.group(1));
                }
            }
        }
        return keys;
    }

    private static Set<String> readDefinedKeys(File bundleFile) throws IOException {
        // Keys are always plain ASCII; read as ISO-8859-1 (which never throws on any byte sequence,
        // unlike UTF-8) so this doesn't care whether a given bundle's *values* happen to be UTF-8,
        // Windows-1252, or a mix -- only the key names left of '=' matter here.
        Set<String> keys = new HashSet<>();
        for (String line : Files.readAllLines(bundleFile.toPath(), StandardCharsets.ISO_8859_1)) {
            Matcher matcher = PROPERTY_LINE.matcher(line);
            if (matcher.find()) {
                keys.add(matcher.group(1));
            }
        }
        return keys;
    }
}
