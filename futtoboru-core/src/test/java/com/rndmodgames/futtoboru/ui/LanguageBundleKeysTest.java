package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * D18 / U-10: every label key a converted screen asks {@code LanguageModLoader.getValue("...")} for exists in all four
 * language bundles. A missing key is not an error at run time: the raw key ("continue_game") is drawn instead, which
 * is what this test prevents. Add a source file or folder to {@link #CHECKED} once its labels are converted.
 */
public class LanguageBundleKeysTest {

    static final List<String> BUNDLES = List.of("BaseBundle_en_us", "BaseBundle_en_uk", "BaseBundle_sp_sp",
            "BaseBundle_sp_la");

    /** Sources (relative to src/main/java) whose getValue keys must all exist. Folders are walked. */
    static final List<String> CHECKED = List.of(
            "com/rndmodgames/futtoboru/menu",
            "com/rndmodgames/futtoboru/tables/main",
            "com/rndmodgames/futtoboru/dialogs",
            "com/rndmodgames/futtoboru/screens/MainGameScreen.java",
            "com/rndmodgames/futtoboru/ui",
            "com/rndmodgames/futtoboru/tables/match/preview");

    private static final Pattern GET_VALUE = Pattern.compile("LanguageModLoader\\.getValue\\(\\s*\"([^\"]+)\"\\s*\\)");

    private static Map<String, Properties> bundles;

    static synchronized Map<String, Properties> bundles() throws IOException {
        if (bundles == null) {
            Map<String, Properties> loaded = new LinkedHashMap<>();
            for (String name : BUNDLES) {
                Properties properties = new Properties();
                try (InputStream in = LanguageBundleKeysTest.class.getResourceAsStream(
                        "/mods/language/" + name + ".properties")) {
                    if (in == null) {
                        fail("language bundle missing on the classpath: " + name);
                    }
                    properties.load(in);
                }
                loaded.put(name, properties);
            }
            bundles = loaded;
        }
        return bundles;
    }

    /** Fails unless {@code key} has a non-empty value in every bundle. */
    public static void assertKeyInAllBundles(String key) throws IOException {
        for (Map.Entry<String, Properties> bundle : bundles().entrySet()) {
            String value = bundle.getValue().getProperty(key);
            assertTrue(value != null && !value.isBlank(), "key '" + key + "' missing in " + bundle.getKey());
        }
    }

    @Test
    void convertedScreensUseOnlyExistingKeys() throws IOException {
        Path src = Paths.get("src", "main", "java");
        if (!Files.isDirectory(src)) {
            return; // not run from the module directory
        }
        List<String> missing = new ArrayList<>();
        for (String checked : CHECKED) {
            Path path = src.resolve(checked);
            assertTrue(Files.exists(path), "checked source moved or deleted, update CHECKED: " + checked);
            try (Stream<Path> files = Files.walk(path)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    Matcher m = GET_VALUE.matcher(Files.readString(file));
                    while (m.find()) {
                        for (Map.Entry<String, Properties> bundle : bundles().entrySet()) {
                            String value = bundle.getValue().getProperty(m.group(1));
                            if (value == null || value.isBlank()) {
                                missing.add(src.relativize(file) + ": '" + m.group(1) + "' missing in " + bundle.getKey());
                            }
                        }
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "label keys missing from the language bundles:\n  " + String.join("\n  ", missing));
    }
}
