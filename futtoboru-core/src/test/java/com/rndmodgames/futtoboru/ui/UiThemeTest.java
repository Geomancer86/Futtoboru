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
        assertEquals(Color.valueOf("B3B3B3"), theme.getColor("text.secondary"));
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
        Pattern call = Pattern.compile("UiTheme\\.(color|space|size|style|fontScale|font)\\(\"([^\"]+)\"\\)");
        List<String> missing = new ArrayList<>();
        try (Stream<Path> files = Files.walk(src)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                Matcher m = call.matcher(Files.readString(file));
                while (m.find()) {
                    String kind = m.group(1);
                    String token = m.group(2);
                    boolean known;
                    switch (kind) {
                        case "color": known = theme.colorTokens().contains(token); break;
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
}
