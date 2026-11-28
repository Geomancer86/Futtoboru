package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
 * D18 / U-10 guard for the language files.
 * <ul>
 * <li>Every key the code asks {@code LanguageModLoader.getValue("...")} / {@code LanguageModLoader.format("...", ...)}
 * for (and, in the UI packages, a class's own {@code text("...")} / {@code t("...")} helper) exists, non-empty, in all
 * four bundles (a missing key draws the raw key, "continue_game", on screen).</li>
 * <li>No hard-coded text in the UI packages ({@link #UI}): a string literal with a letter in it, given to a label,
 * button, text field, check box, dialog or tooltip ({@link #TEXT_CALL}), fails. Empty strings and symbols (":" "-"
 * "%" " / ") are fine. Put the text in the four bundles and use {@code LanguageModLoader.getValue("key")}.</li>
 * <li>No hard-coded in-game message title or text ({@code Message.setTitle/setPlainTextMessage("...")}) anywhere.</li>
 * </ul>
 * {@link #DEV_ONLY} lists the classes a player never sees in a release build (they may keep English literals).
 */
public class LanguageBundleKeysTest {

    static final List<String> BUNDLES = List.of("BaseBundle_en_us", "BaseBundle_en_uk", "BaseBundle_sp_sp",
            "BaseBundle_sp_la");

    /** UI sources (relative to src/main/java) the hard-coded-text guard walks. */
    static final List<String> UI = List.of(
            "com/rndmodgames/components",
            "com/rndmodgames/darkblade/screens",
            "com/rndmodgames/futtoboru/dialogs",
            "com/rndmodgames/futtoboru/menu",
            "com/rndmodgames/futtoboru/screens",
            "com/rndmodgames/futtoboru/tables",
            "com/rndmodgames/futtoboru/ui");

    /**
     * Classes that may keep hard-coded English, with the reason. Keep this list short: a dev tool only, never a screen
     * the player reaches in normal play.
     */
    static final Map<String, String> DEV_ONLY = Map.of(
            "com/rndmodgames/futtoboru/screens/MatchEngineDebugScreen.java",
            "dev-only match engine test bench (main menu 'Match Engine Debug'), replaced by the E-21 viewer");

    /**
     * Sources another lane is converting right now: skipped by both checks until it merges. Remove the entry then
     * (the guard covers them from that moment).
     */
    static final Map<String, String> PENDING = Map.of();

    /** A call that puts its text argument on screen. The literal must come after it on the same line. */
    static final Pattern TEXT_CALL = Pattern.compile("new\\s+(VisLabel|VisTextButton|VisTextField|VisTextArea"
            + "|VisCheckBox|VisRadioButton|VisDialog|VisWindow|VisImageTextButton|LinkLabel|TextButton|Label"
            + "|Tooltip\\.Builder)\\s*\\(|\\.(setText|setMessageText|setTitle|setItems|setPlainTextMessage)\\s*\\("
            + "|\\bUiDialogs\\.\\w+\\s*\\(|\\bDialogs\\.show\\w*\\s*\\(");

    /** Message titles/texts built in code anywhere in the game. */
    static final Pattern MESSAGE_CALL = Pattern.compile("\\.(setTitle|setPlainTextMessage)\\s*\\(");

    private static final Pattern KEY_CALL = Pattern
            .compile("LanguageModLoader\\.(?:getValue|format)\\(\\s*\"([^\"]+)\"");

    /** A UI class's own lookup helper ({@code text("key")}, {@code t("key")}): its keys are checked too. */
    private static final Pattern LOCAL_KEY_CALL = Pattern.compile("(?<![\\w.])(?:text|t)\\(\\s*\"([^\"]+)\"");

    /** Literals that are not text: a key, a skin/theme token, a colour or size name. */
    private static final Pattern NOT_TEXT = Pattern.compile("(?<![\\w.])(?:text|t)\\(\\s*\"[^\"]*\""
            + "|(?:LanguageModLoader\\.)?(?:getValue|format)\\(\\s*\"[^\"]*\""
            + "|UiTheme\\.\\w+\\(\\s*\"[^\"]*\"|UiTooltips\\.\\w+\\([^)]*\\)|getDrawable\\(\\s*\"[^\"]*\"|style\\s*\\(\\s*\"[^\"]*\"");

    private static final Pattern LITERAL = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    private static final Pattern LETTER = Pattern.compile("\\p{L}");

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
                    // the game reads the bundles as UTF-8 (I18NBundleLoader default)
                    properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
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

    private static Path sources() {
        Path src = Paths.get("src", "main", "java");
        return Files.isDirectory(src) ? src : null; // null: not run from the module directory
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static String rel(Path src, Path file) {
        return src.relativize(file).toString().replace('\\', '/');
    }

    private static boolean devOnly(String relative) {
        return pending(relative) || DEV_ONLY.keySet().stream().anyMatch(relative::startsWith);
    }

    private static boolean pending(String relative) {
        return PENDING.keySet().stream().anyMatch(relative::startsWith);
    }

    @Test
    void everyKeyTheCodeUsesExistsInAllBundles() throws IOException {
        Path src = sources();
        if (src == null) {
            return;
        }
        List<String> missing = new ArrayList<>();
        for (Path file : javaFiles(src)) {
            String relative = rel(src, file);
            if (pending(relative)) {
                continue;
            }
            String code = Files.readString(file);
            List<String> keys = new ArrayList<>();
            Matcher k = KEY_CALL.matcher(code);
            while (k.find()) {
                keys.add(k.group(1));
            }
            if (UI.stream().anyMatch(relative::startsWith)) {
                Matcher local = LOCAL_KEY_CALL.matcher(code);
                while (local.find()) {
                    keys.add(local.group(1));
                }
            }
            for (String key : keys) {
                for (Map.Entry<String, Properties> bundle : bundles().entrySet()) {
                    String value = bundle.getValue().getProperty(key);
                    if (value == null || value.isBlank()) {
                        missing.add(relative + ": '" + key + "' missing in " + bundle.getKey());
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "label keys missing from the language bundles:\n  " + String.join("\n  ", missing));
    }

    @Test
    void noHardCodedTextInTheUi() throws IOException {
        Path src = sources();
        if (src == null) {
            return;
        }
        List<String> found = new ArrayList<>();
        for (String folder : UI) {
            Path root = src.resolve(folder);
            assertTrue(Files.exists(root), "UI folder moved or deleted, update UI: " + folder);
            for (Path file : javaFiles(root)) {
                String relative = rel(src, file);
                if (!devOnly(relative)) {
                    scan(relative, Files.readAllLines(file, StandardCharsets.UTF_8), TEXT_CALL, found);
                }
            }
        }
        for (Path file : javaFiles(src)) {
            String relative = rel(src, file);
            if (!devOnly(relative) && UI.stream().noneMatch(relative::startsWith)) {
                scan(relative, Files.readAllLines(file, StandardCharsets.UTF_8), MESSAGE_CALL, found);
            }
        }
        assertTrue(found.isEmpty(), "hard-coded text on screen; move it to the 4 language bundles (D18/U-10) and use "
                + "LanguageModLoader.getValue(\"key\"):\n  " + String.join("\n  ", found));
    }

    @Test
    void devOnlyEntriesStillExist() {
        Path src = sources();
        if (src == null) {
            return;
        }
        for (String entry : DEV_ONLY.keySet()) {
            assertTrue(Files.exists(src.resolve(entry)), "DEV_ONLY entry moved or deleted, update DEV_ONLY: " + entry);
        }
        for (String entry : PENDING.keySet()) {
            assertTrue(Files.exists(src.resolve(entry)), "PENDING entry moved or deleted, update PENDING: " + entry);
        }
    }

    static void scan(String file, List<String> lines, Pattern call, List<String> found) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
                continue;
            }
            Matcher m = call.matcher(line);
            if (!m.find()) {
                continue;
            }
            String rest = NOT_TEXT.matcher(line.substring(m.start())).replaceAll("");
            int comment = rest.indexOf("//");
            if (comment >= 0 && rest.lastIndexOf('"', comment) < 0) {
                rest = rest.substring(0, comment);
            }
            Matcher lit = LITERAL.matcher(rest);
            while (lit.find()) {
                if (isText(lit.group(1))) {
                    found.add(file + ":" + (i + 1) + ": \"" + lit.group(1) + "\"");
                }
            }
        }
    }

    /** True for a literal a player would read: it has a letter (escape sequences such as \n do not count). */
    static boolean isText(String literal) {
        return LETTER.matcher(literal.replaceAll("\\\\u[0-9a-fA-F]{4}|\\\\.", "")).find();
    }
}
