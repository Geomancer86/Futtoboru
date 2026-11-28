package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.graphics.Color;

/** Headless: no Gdx application or GL context is created. */
class UiThemeTest {

    private static String themeJson;
    private static String skinJson;
    private static UiTheme theme;

    @BeforeAll
    static void loadResources() throws IOException {
        themeJson = resource(UiTheme.THEME_PATH);
        skinJson = resource(UiTheme.SKIN_PATH);
        theme = UiTheme.parse(themeJson, skinJson);
    }

    @AfterEach
    void uninstall() {
        UiTheme.install(null);
    }

    private static String resource(String path) throws IOException {
        try (InputStream in = UiThemeTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, "missing classpath resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void shippedThemeParsesAndEveryTokenResolves() {
        assertFalse(theme.colorTokens().isEmpty());
        for (String token : theme.colorTokens()) {
            assertNotNull(theme.getColor(token), token);
        }
        for (String role : theme.fontRoles()) {
            assertTrue(theme.getFontRole(role).scale > 0, role);
            assertNotNull(theme.getFontRole(role).font, role);
        }
        for (String token : theme.spacingTokens()) {
            assertTrue(theme.getSpace(token) >= 0, token);
        }
        for (String token : theme.sizeTokens()) {
            assertTrue(theme.getSize(token) >= 0, token);
        }
        for (String token : theme.styleTokens()) {
            assertNotNull(theme.getStyle(token), token);
        }
        assertEquals(1280, theme.getBaseWidth());
        assertEquals(720, theme.getBaseHeight());
        assertEquals("auto", theme.getDefaultScale());
    }

    @Test
    void starterTokensMatchTheLiteralsInUse() {
        assertEquals(Color.BLACK, theme.getColor("bg.screen"));
        assertEquals(Color.WHITE, theme.getColor("text.primary"));
        // hex values are what libGDX packs for the old float literals: (int) (0.7f * 255) = 178 = B2
        assertEquals(Color.valueOf("B2B2B2"), theme.getColor("text.secondary"));
        // skin reference: tixel t-highlight (0.85, 0.5, 0.05)
        Color accent = theme.getColor("accent");
        assertEquals(0.85f, accent.r, 1e-6);
        assertEquals(0.5f, accent.g, 1e-6);
        assertEquals(0.05f, accent.b, 1e-6);
        assertEquals(10f, theme.getSpace("sm"));
        assertEquals(150f, theme.getSize("col.label"));
        assertEquals(1.2f, theme.getFontRole("heading").scale);
        assertEquals("blue", theme.getStyle("button.primary"));
    }

    @Test
    void returnedColoursAreCopies() {
        theme.getColor("text.primary").set(Color.RED);
        assertEquals(Color.WHITE, theme.getColor("text.primary"));
    }

    @Test
    void staticAccessorsUseInstalledTheme() {
        assertThrows(IllegalStateException.class, () -> UiTheme.color("text.primary"));
        UiTheme.install(theme);
        assertEquals(Color.WHITE, UiTheme.color("text.primary"));
        assertEquals(5f, UiTheme.space("xs"));
        assertEquals(200f, UiTheme.size("col.wide"));
        assertEquals(1.3f, UiTheme.fontScale("title"));
        assertEquals("default-font", UiTheme.font("body"));
        assertEquals("link-label", UiTheme.style("label.link"));
    }

    @Test
    void unknownTokenThrowsWithClearMessage() {
        UiTheme.install(theme);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> UiTheme.color("text.primay"));
        assertTrue(e.getMessage().contains("text.primay"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> UiTheme.space("huge"));
        assertThrows(IllegalArgumentException.class, () -> UiTheme.size("col.nope"));
        assertThrows(IllegalArgumentException.class, () -> UiTheme.fontScale("headline"));
        assertThrows(IllegalArgumentException.class, () -> UiTheme.style("button.nope"));
    }

    @Test
    void malformedFileThrows() {
        assertThrows(IllegalArgumentException.class, () -> UiTheme.parse("{ \"palette\": { \"black\": ", skinJson));
        assertThrows(IllegalArgumentException.class, () -> UiTheme.parse("", skinJson));
        assertThrows(IllegalArgumentException.class, () -> UiTheme.parse("[1, 2]", skinJson));
    }

    @Test
    void schemaViolationsThrow() {
        // unknown top-level field
        assertInvalid(themeJson.replaceFirst("\"schemaVersion\": 1,", "\"schemaVersion\": 1, \"extra\": {},"));
        // wrong schema version
        assertInvalid(themeJson.replaceFirst("\"schemaVersion\": 1", "\"schemaVersion\": 2"));
        // missing section
        assertInvalid(themeJson.replaceFirst("\"styles\": \\{", "\"stylesX\": {"));
        // bad hex
        assertInvalid(themeJson.replace("\"black\": \"000000\"", "\"black\": \"00000G\""));
        // colour pointing at nothing
        assertInvalid(themeJson.replace("\"text.primary\": \"white\"", "\"text.primary\": \"ivory\""));
        assertInvalid(themeJson.replace("\"skin:t-highlight\"", "\"skin:t-nope\""));
        // duplicate key
        assertInvalid(themeJson.replace("\"none\": 0,", "\"none\": 0, \"none\": 1,"));
        // unknown font field, unknown skin font, unknown skin style
        assertInvalid(themeJson.replace("\"scale\": 1.5, \"file\": null", "\"scale\": 1.5, \"size\": 13"));
        assertInvalid(themeJson.replace("\"font\": \"small-font\"", "\"font\": \"tiny-font\""));
        assertInvalid(themeJson.replace("\"button.toggle\": \"toggle\"", "\"button.toggle\": \"toggel\""));
        // negative spacing, scaling default not in steps
        assertInvalid(themeJson.replace("\"xl\": 50", "\"xl\": -50"));
        assertInvalid(themeJson.replace("\"default\": \"auto\"", "\"default\": 1.5"));
    }

    private static void assertInvalid(String json) {
        assertFalse(json.equals(themeJson), "test edit did not apply");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> UiTheme.parse(json, skinJson));
        assertNotNull(e.getMessage());
    }

    /** Every UiTheme.xxx("token") literal in main code must exist in theme.json (guards U-02 conversions). */
    @Test
    void tokenLiteralsInSourcesExist() throws IOException {
        Path src = Paths.get("src", "main", "java");
        if (!Files.isDirectory(src)) {
            return; // not run from the module directory
        }
        Pattern call = Pattern.compile(
                "UiTheme\\.(color|clearScreen|space|size|style|fontScale|font)\\(\"([^\"]+)\"\\)");
        List<String> missing = new ArrayList<>();
        try (Stream<Path> files = Files.walk(src)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                Matcher m = call.matcher(Files.readString(file));
                while (m.find()) {
                    String kind = m.group(1);
                    String token = m.group(2);
                    boolean known;
                    switch (kind) {
                        case "color":
                        case "clearScreen": known = theme.colorTokens().contains(token); break;
                        case "space": known = theme.spacingTokens().contains(token); break;
                        case "size": known = theme.sizeTokens().contains(token); break;
                        case "style": known = theme.styleTokens().contains(token); break;
                        default: known = theme.fontRoles().contains(token); break;
                    }
                    if (!known) {
                        missing.add(file.getFileName() + ": " + kind + "(\"" + token + "\")");
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "unknown UiTheme tokens in sources: " + missing);
    }

    // ------------------------------------------------------------------ U-02b lint: no style literals in UI code

    /** UI source roots (relative to src/main/java) checked for style literals. Engine/system code is not UI. */
    private static final List<String> UI_ROOTS = List.of(
            "com/rndmodgames/futtoboru/screens",
            "com/rndmodgames/futtoboru/tables",
            "com/rndmodgames/futtoboru/dialogs",
            "com/rndmodgames/futtoboru/menu",
            "com/rndmodgames/futtoboru/match",
            "com/rndmodgames/futtoboru/ui",
            "com/rndmodgames/components",
            "com/rndmodgames/darkblade");

    /**
     * Files allowed to keep literals, with the reason. Remove an entry once the file is converted.
     * {@code UiTheme} itself is the one place that turns tokens into GL/libGDX values.
     */
    private static final java.util.Map<String, String> LITERAL_ALLOW_LIST = java.util.Map.of(
            "com/rndmodgames/futtoboru/ui/UiTheme.java", "the token accessor itself");

    /** Literal patterns the UI must take from {@code UiTheme} instead (docs/design/UI_AUDIT.md 2.6). */
    static final java.util.Map<String, Pattern> STYLE_LITERALS = java.util.Map.of(
            "new Color(", Pattern.compile("\\bnew\\s+(?:com\\.badlogic\\.gdx\\.graphics\\.)?Color\\s*\\("),
            "Color.<CONST>", Pattern.compile("\\bColor\\.[A-Z][A-Z0-9_]*\\b"),
            "Color.valueOf(", Pattern.compile("\\bColor\\.valueOf\\s*\\("),
            "glClearColor(", Pattern.compile("\\bglClearColor\\s*\\("),
            "numeric setColor(", Pattern.compile("\\.setColor\\s*\\(\\s*[-0-9.]"),
            "numeric setFontScale(", Pattern.compile("\\.setFontScale\\s*\\(\\s*[-0-9.]"),
            "numeric pad/space(", Pattern.compile(
                    "\\.(?:pad|space)(?:Top|Bottom|Left|Right)?\\s*\\(\\s*[-0-9.]"),
            "numeric width/height(", Pattern.compile(
                    "\\.(?:width|height|minWidth|minHeight|prefWidth|prefHeight|maxWidth|maxHeight"
                            + "|setWidth|setHeight)\\s*\\(\\s*[-0-9.]"));

    /** Returns "file:line: kind: code" for every style literal outside comments. */
    static List<String> findStyleLiterals(String relPath, String source) {
        List<String> hits = new ArrayList<>();
        String[] lines = source.split("\\r?\\n", -1);
        boolean inBlockComment = false;
        for (int i = 0; i < lines.length; i++) {
            String code = lines[i];
            // strip block comments (simple: no string literal contains comment markers in UI code)
            StringBuilder sb = new StringBuilder();
            int pos = 0;
            while (pos < code.length()) {
                if (inBlockComment) {
                    int end = code.indexOf("*/", pos);
                    if (end < 0) {
                        pos = code.length();
                    } else {
                        inBlockComment = false;
                        pos = end + 2;
                    }
                } else {
                    int start = code.indexOf("/*", pos);
                    int line = code.indexOf("//", pos);
                    if (line >= 0 && (start < 0 || line < start)) {
                        sb.append(code, pos, line);
                        pos = code.length();
                    } else if (start >= 0) {
                        sb.append(code, pos, start);
                        inBlockComment = true;
                        pos = start + 2;
                    } else {
                        sb.append(code.substring(pos));
                        pos = code.length();
                    }
                }
            }
            String stripped = sb.toString();
            for (java.util.Map.Entry<String, Pattern> e : STYLE_LITERALS.entrySet()) {
                if (e.getValue().matcher(stripped).find()) {
                    hits.add(relPath + ":" + (i + 1) + ": " + e.getKey() + ": " + lines[i].trim());
                }
            }
        }
        return hits;
    }

    @Test
    void lintPatternsCatchLiterals() {
        String sample = String.join("\n",
                "label.setColor(0.7f, 0.7f, 0.7f, 1f);",
                "label.setColor(Color.GOLD);",
                "Color c = new Color(1, 0, 0, 1);",
                "Gdx.gl.glClearColor(0, 0, 0, 1);",
                "title.setFontScale(1.3f);",
                "table.add(x).pad(5).padTop(10);",
                "table.add(x).width(150);",
                "// label.setColor(1, 0, 0, 1); in a comment is fine",
                "/* table.add(x).pad(5); */",
                "label.setColor(UiTheme.color(\"text.primary\"));",
                "table.add(x).pad(UiTheme.space(\"xs\")).width(UiTheme.size(\"col.label\"));",
                "int n = list.size();");
        List<String> hits = findStyleLiterals("Sample.java", sample);
        assertEquals(7, hits.stream().map(h -> h.split(":")[1]).distinct().count(), hits.toString());
        assertTrue(hits.stream().noneMatch(h -> h.startsWith("Sample.java:8:") || h.startsWith("Sample.java:9:")
                || h.startsWith("Sample.java:10:") || h.startsWith("Sample.java:11:")
                || h.startsWith("Sample.java:12:")), hits.toString());
    }

    /**
     * CI lint (UI_AUDIT 4.3 step 6): UI code takes colours, clears, font scales, paddings and sizes from
     * {@code UiTheme} tokens. Add a token to theme.json instead of a literal; the allow-list is for files
     * under another lane's lock only.
     */
    @Test
    void noStyleLiteralsInUiSources() throws IOException {
        Path src = Paths.get("src", "main", "java");
        if (!Files.isDirectory(src)) {
            return; // not run from the module directory
        }
        List<String> hits = new ArrayList<>();
        for (String root : UI_ROOTS) {
            Path dir = src.resolve(root);
            assertTrue(Files.isDirectory(dir), "UI root moved or deleted, update UI_ROOTS: " + root);
            try (Stream<Path> files = Files.walk(dir)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    String rel = src.relativize(file).toString().replace('\\', '/');
                    if (LITERAL_ALLOW_LIST.containsKey(rel)) {
                        continue;
                    }
                    hits.addAll(findStyleLiterals(rel, Files.readString(file)));
                }
            }
        }
        assertTrue(hits.isEmpty(), "UI style literals found; use UiTheme.color/space/size/fontScale/clearScreen"
                + " with a theme.json token instead:\n  " + String.join("\n  ", hits));
    }
}
