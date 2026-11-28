package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * U-12 guard: dates and numbers on screen and in inbox letters follow the <b>game language</b>
 * ({@code com.rndmodgames.localization.GameFormat}), never the machine's locale nor a hard-coded English one.
 * <ul>
 * <li>In the UI packages ({@link LanguageBundleKeysTest#UI}) and the inbox letters ({@link #MESSAGES}), a
 * {@code DateTimeFormatter.ofPattern}, {@code SimpleDateFormat}, {@code DecimalFormat}, {@code NumberFormat},
 * {@code String.format} or {@code .formatted} fails, unless the same line says {@code Locale.ROOT} (text that is
 * not shown in a language: typed input, a file name). Use {@code GameFormat.mediumDate / numericDate / longDate /
 * decimal / integer / format}.</li>
 * <li>Every bundle pattern key the code gives {@code GameFormat.formatter("...")} / {@code GameFormat.date(..,
 * "...")} exists in all four bundles.</li>
 * </ul>
 * Dev-only screens ({@link LanguageBundleKeysTest#DEV_ONLY}) and {@link #PENDING} sources are skipped.
 */
class GameFormatGuardTest {

    /** Inbox letter builders: their dates and numbers are read by the player too. */
    static final List<String> MESSAGES = List.of("com/rndmodgames/futtoboru/engine/messages");

    /**
     * Sources another lane owns right now, skipped until it merges (then remove the entry).
     */
    static final Map<String, String> PENDING = Map.of(
            "com/rndmodgames/futtoboru/tables/jobs",
            "J-01 owns the job screens (their unused DecimalFormat fields go with the salary-as-money fix)");

    static final Pattern MACHINE_LOCALE_FORMAT = Pattern.compile("DateTimeFormatter\\s*\\.\\s*ofPattern\\s*\\("
            + "|DateTimeFormatter\\s*\\.\\s*ofLocalized\\w*\\s*\\(|new\\s+SimpleDateFormat\\s*\\(|new\\s+DecimalFormat\\s*\\("
            + "|\\bNumberFormat\\s*\\.\\s*get\\w*\\s*\\(|\\bDateFormat\\s*\\.\\s*get\\w*\\s*\\(|\\bString\\s*\\.\\s*format\\s*\\("
            + "|\\.formatted\\s*\\(");

    private static final Pattern PATTERN_KEY = Pattern
            .compile("GameFormat\\s*\\.\\s*(?:formatter|date)\\s*\\((?:[^\"()]*,\\s*)?\"([^\"]+)\"");

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

    private static boolean skipped(String relative) {
        return LanguageBundleKeysTest.DEV_ONLY.keySet().stream().anyMatch(relative::startsWith)
                || PENDING.keySet().stream().anyMatch(relative::startsWith);
    }

    /** The offending lines of one file ("file:line: code"). */
    static List<String> scan(String file, List<String> lines) {
        List<String> found = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String code = lines.get(i).trim();
            if (code.startsWith("//") || code.startsWith("*") || code.startsWith("/*")) {
                continue;
            }
            int comment = code.indexOf("//");
            String statement = comment >= 0 ? code.substring(0, comment) : code;
            if (MACHINE_LOCALE_FORMAT.matcher(statement).find() && !statement.contains("Locale.ROOT")) {
                found.add(file + ":" + (i + 1) + ": " + code);
            }
        }
        return found;
    }

    @Test
    void uiAndLettersFormatInTheGameLanguage() throws IOException {
        Path src = sources();
        if (src == null) {
            return;
        }
        List<String> found = new ArrayList<>();
        List<String> folders = new ArrayList<>(LanguageBundleKeysTest.UI);
        folders.addAll(MESSAGES);
        for (String folder : folders) {
            Path root = src.resolve(folder);
            assertTrue(Files.exists(root), "folder moved or deleted, update the guard: " + folder);
            for (Path file : javaFiles(root)) {
                String relative = rel(src, file);
                if (!skipped(relative)) {
                    found.addAll(scan(relative, Files.readAllLines(file, StandardCharsets.UTF_8)));
                }
            }
        }
        assertTrue(found.isEmpty(), "dates/numbers formatted with the machine's (or a fixed English) locale; use "
                + "GameFormat (the game language, U-12):\n  " + String.join("\n  ", found));
    }

    @Test
    void everyPatternKeyGivenToGameFormatIsInAllBundles() throws IOException {
        Path src = sources();
        if (src == null) {
            return;
        }
        for (Path file : javaFiles(src)) {
            Matcher m = PATTERN_KEY.matcher(Files.readString(file, StandardCharsets.UTF_8));
            while (m.find()) {
                LanguageBundleKeysTest.assertKeyInAllBundles(m.group(1));
            }
        }
    }

    @Test
    void pendingEntriesStillExist() {
        Path src = sources();
        if (src == null) {
            return;
        }
        for (String entry : PENDING.keySet()) {
            assertTrue(Files.exists(src.resolve(entry)), "PENDING entry moved or deleted, update PENDING: " + entry);
        }
    }

    @Test
    void theGuardCatchesMachineLocaleFormatting() {
        List<String> bad = scan("X.java", List.of(
                "DateTimeFormatter f = DateTimeFormatter.ofPattern(\"dd MMM yyyy\");",
                "DateTimeFormatter f = DateTimeFormatter.ofPattern(\"dd MMM yyyy\", Locale.ENGLISH);",
                "DecimalFormat d = new DecimalFormat(\"#0.0\");",
                "String s = String.format(\"%.1f\", v);",
                "String s = \"%,d\".formatted(n);",
                "NumberFormat n = NumberFormat.getInstance();"));
        assertTrue(bad.size() == 6, "all six caught: " + bad);
        assertTrue(scan("X.java", List.of(
                "String s = String.format(Locale.ROOT, \"%02d\", n); // a file name",
                "// DateTimeFormatter.ofPattern(\"dd MMM\") in a comment",
                "String s = GameFormat.mediumDate(date);",
                "String s = LanguageModLoader.format(\"key\", n);")).isEmpty());
        assertFalse(scan("X.java", List.of("x = String.format(\"%d\", n); // Locale.ROOT only in the comment")).isEmpty());
    }
}
