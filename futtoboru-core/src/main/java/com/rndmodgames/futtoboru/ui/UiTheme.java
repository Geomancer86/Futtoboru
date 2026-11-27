package com.rndmodgames.futtoboru.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * UI design tokens (colours, font roles, spacing, sizes, skin style names, scaling) read from
 * {@code ui/theme.json}. Screens ask for a token by name instead of hard-coding literals
 * (docs/design/UI_AUDIT.md, section 4.2).
 *
 * <p>Loading is strict: unknown fields, duplicate keys, bad hex values and colour references that
 * resolve to neither a palette entry nor a skin colour ({@code "skin:<name>"}) all throw
 * {@link IllegalArgumentException}. Asking for a token that does not exist also throws, so a typo
 * fails on the first screen that uses it instead of drawing the wrong colour.
 *
 * <p>Parsing needs no GL context: {@link #parse(String, String)} works in plain unit tests.
 */
public final class UiTheme {

    public static final String THEME_PATH = "ui/theme.json";
    public static final String SKIN_PATH = "skin/tixel.json";
    public static final int SCHEMA_VERSION = 1;

    private static final String SKIN_COLOR_SECTION = "com.badlogic.gdx.graphics.Color";
    private static final String SKIN_FONT_SECTION = "com.badlogic.gdx.graphics.g2d.BitmapFont";
    private static final Set<String> SKIN_NON_STYLE_SECTIONS = Set.of(SKIN_COLOR_SECTION, SKIN_FONT_SECTION,
            "com.badlogic.gdx.scenes.scene2d.ui.Skin$TintedDrawable", "com.kotcrab.vis.ui.Sizes");
    private static final String SKIN_PREFIX = "skin:";

    private static final Set<String> TOP_LEVEL_FIELDS = Set.of("schemaVersion", "palette", "colors", "fonts",
            "spacing", "sizes", "styles", "scaling");
    private static final Set<String> FONT_FIELDS = Set.of("font", "scale", "file");
    private static final Set<String> SCALING_FIELDS = Set.of("baseWidth", "baseHeight", "steps", "default");

    private static UiTheme current;

    /** A font role: skin font name, scale applied until real font files ship (U-03), optional font file. */
    public static final class FontRole {
        public final String font;
        public final float scale;
        public final String file;

        FontRole(String font, float scale, String file) {
            this.font = font;
            this.scale = scale;
            this.file = file;
        }
    }

    private final Map<String, Color> colors;
    private final Map<String, FontRole> fonts;
    private final Map<String, Float> spacing;
    private final Map<String, Float> sizes;
    private final Map<String, String> styles;
    private final int baseWidth;
    private final int baseHeight;
    private final List<Float> scaleSteps;
    private final String defaultScale;

    private UiTheme(Map<String, Color> colors, Map<String, FontRole> fonts, Map<String, Float> spacing,
            Map<String, Float> sizes, Map<String, String> styles, int baseWidth, int baseHeight,
            List<Float> scaleSteps, String defaultScale) {
        this.colors = Collections.unmodifiableMap(colors);
        this.fonts = Collections.unmodifiableMap(fonts);
        this.spacing = Collections.unmodifiableMap(spacing);
        this.sizes = Collections.unmodifiableMap(sizes);
        this.styles = Collections.unmodifiableMap(styles);
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.scaleSteps = Collections.unmodifiableList(scaleSteps);
        this.defaultScale = defaultScale;
    }

    // ------------------------------------------------------------------ loading

    /** Loads {@link #THEME_PATH} against {@link #SKIN_PATH} through {@code Gdx.files.internal}. */
    public static void loadDefault() {
        load(Gdx.files.internal(THEME_PATH), Gdx.files.internal(SKIN_PATH));
    }

    /** Parses and installs the theme. The skin file is read as text only (no textures, no GL). */
    public static void load(FileHandle themeJson, FileHandle skinJson) {
        current = parse(themeJson.readString("UTF-8"), skinJson.readString("UTF-8"));
    }

    /** Installs an already parsed theme (tests). */
    public static void install(UiTheme theme) {
        current = theme;
    }

    public static boolean isLoaded() {
        return current != null;
    }

    /** Returns the installed theme instance. */
    public static UiTheme get() {
        if (current == null) {
            throw new IllegalStateException("UiTheme not loaded: call UiTheme.loadDefault() in Futtoboru.create()");
        }
        return current;
    }

    /**
     * Parses theme JSON text, resolving {@code skin:} colour references and checking font and style
     * names against the skin JSON text. Throws {@link IllegalArgumentException} on any schema error.
     */
    public static UiTheme parse(String themeJson, String skinJson) {
        JsonValue skin = read(skinJson, SKIN_PATH);
        Map<String, Color> skinColors = readSkinColors(skin);
        Set<String> skinFonts = childNames(skin.get(SKIN_FONT_SECTION));
        Set<String> skinStyles = new HashSet<>();
        for (JsonValue section = skin.child; section != null; section = section.next) {
            if (!SKIN_NON_STYLE_SECTIONS.contains(section.name)) {
                skinStyles.addAll(childNames(section));
            }
        }

        JsonValue root = read(themeJson, THEME_PATH);
        requireObject(root, "root");
        checkFields(root, TOP_LEVEL_FIELDS, "root", true);

        JsonValue version = root.get("schemaVersion");
        if (!version.isLong() || version.asInt() != SCHEMA_VERSION) {
            throw fail("schemaVersion must be " + SCHEMA_VERSION + ", got " + version);
        }

        // palette: name -> hex
        Map<String, Color> palette = new LinkedHashMap<>();
        for (JsonValue entry : entries(root, "palette")) {
            palette.put(entry.name, parseHex(requireString(entry, "palette." + entry.name), "palette." + entry.name));
        }

        // colors: token -> palette name | skin:<colour>
        Map<String, Color> colors = new LinkedHashMap<>();
        for (JsonValue entry : entries(root, "colors")) {
            String where = "colors." + entry.name;
            String ref = requireString(entry, where);
            Color color;
            if (ref.startsWith(SKIN_PREFIX)) {
                color = skinColors.get(ref.substring(SKIN_PREFIX.length()));
                if (color == null) {
                    throw fail(where + ": skin colour '" + ref.substring(SKIN_PREFIX.length())
                            + "' not found in " + SKIN_PATH);
                }
            } else {
                color = palette.get(ref);
                if (color == null) {
                    throw fail(where + ": '" + ref + "' is not a palette key (or use 'skin:<name>')");
                }
            }
            colors.put(entry.name, new Color(color));
        }

        // fonts: role -> {font, scale, file}
        Map<String, FontRole> fonts = new LinkedHashMap<>();
        for (JsonValue entry : entries(root, "fonts")) {
            String where = "fonts." + entry.name;
            requireObject(entry, where);
            checkFields(entry, FONT_FIELDS, where, false);
            JsonValue fontName = entry.get("font");
            JsonValue scale = entry.get("scale");
            if (fontName == null || scale == null) {
                throw fail(where + ": 'font' and 'scale' are required");
            }
            String font = requireString(fontName, where + ".font");
            if (!skinFonts.contains(font)) {
                throw fail(where + ".font: '" + font + "' is not a font in " + SKIN_PATH);
            }
            float s = requirePositive(scale, where + ".scale", false);
            JsonValue file = entry.get("file");
            String fileName = null;
            if (file != null && !file.isNull()) {
                fileName = requireString(file, where + ".file");
            }
            fonts.put(entry.name, new FontRole(font, s, fileName));
        }

        Map<String, Float> spacing = readNumbers(root, "spacing");
        Map<String, Float> sizes = readNumbers(root, "sizes");

        // styles: token -> skin style name
        Map<String, String> styles = new LinkedHashMap<>();
        for (JsonValue entry : entries(root, "styles")) {
            String where = "styles." + entry.name;
            String style = requireString(entry, where);
            if (!skinStyles.contains(style)) {
                throw fail(where + ": '" + style + "' is not a style name in " + SKIN_PATH);
            }
            styles.put(entry.name, style);
        }

        // scaling
        JsonValue scaling = root.get("scaling");
        requireObject(scaling, "scaling");
        checkFields(scaling, SCALING_FIELDS, "scaling", true);
        int baseWidth = (int) requirePositive(scaling.get("baseWidth"), "scaling.baseWidth", false);
        int baseHeight = (int) requirePositive(scaling.get("baseHeight"), "scaling.baseHeight", false);
        JsonValue stepsJson = scaling.get("steps");
        if (!stepsJson.isArray() || stepsJson.size == 0) {
            throw fail("scaling.steps must be a non-empty array of numbers");
        }
        List<Float> steps = new ArrayList<>();
        for (JsonValue step = stepsJson.child; step != null; step = step.next) {
            steps.add(requirePositive(step, "scaling.steps[]", false));
        }
        JsonValue def = scaling.get("default");
        String defaultScale;
        if (def.isString() && "auto".equals(def.asString())) {
            defaultScale = "auto";
        } else if (def.isNumber() && steps.contains(def.asFloat())) {
            defaultScale = String.valueOf(def.asFloat());
        } else {
            throw fail("scaling.default must be \"auto\" or one of scaling.steps, got " + def);
        }

        return new UiTheme(colors, fonts, spacing, sizes, styles, baseWidth, baseHeight, steps, defaultScale);
    }

    // ------------------------------------------------------------------ static accessors (installed theme)

    /** A copy of the colour for a semantic token, e.g. {@code UiTheme.color("state.positive")}. */
    public static Color color(String token) {
        return get().getColor(token);
    }

    /** Spacing in design pixels, e.g. {@code .pad(UiTheme.space("sm"))}. */
    public static float space(String token) {
        return get().getSpace(token);
    }

    /** Size in design pixels, e.g. {@code .width(UiTheme.size("col.label"))}. */
    public static float size(String token) {
        return get().getSize(token);
    }

    /** Skin style name, e.g. {@code new VisTextButton(text, UiTheme.style("button.primary"))}. */
    public static String style(String token) {
        return get().getStyle(token);
    }

    /** Font scale for a role ({@code display, title, heading, subheading, body, small}). */
    public static float fontScale(String role) {
        return get().getFontRole(role).scale;
    }

    /** Skin font name for a role. */
    public static String font(String role) {
        return get().getFontRole(role).font;
    }

    /** Clears the screen with {@code bg.screen}. Needs a GL context. */
    public static void clearScreen() {
        clearScreen("bg.screen");
    }

    /** Clears the screen with the given colour token (e.g. {@code bg.splash}). Needs a GL context. */
    public static void clearScreen(String token) {
        Color bg = get().getColor(token);
        Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    // ------------------------------------------------------------------ instance accessors

    public Color getColor(String token) {
        return new Color(lookup(colors, token, "colour"));
    }

    public float getSpace(String token) {
        return lookup(spacing, token, "spacing");
    }

    public float getSize(String token) {
        return lookup(sizes, token, "size");
    }

    public String getStyle(String token) {
        return lookup(styles, token, "style");
    }

    public FontRole getFontRole(String role) {
        return lookup(fonts, role, "font role");
    }

    public Set<String> colorTokens() {
        return colors.keySet();
    }

    public Set<String> fontRoles() {
        return fonts.keySet();
    }

    public Set<String> spacingTokens() {
        return spacing.keySet();
    }

    public Set<String> sizeTokens() {
        return sizes.keySet();
    }

    public Set<String> styleTokens() {
        return styles.keySet();
    }

    public int getBaseWidth() {
        return baseWidth;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public List<Float> getScaleSteps() {
        return scaleSteps;
    }

    /** {@code "auto"} or one of {@link #getScaleSteps()} as a string. */
    public String getDefaultScale() {
        return defaultScale;
    }

    // ------------------------------------------------------------------ helpers

    private static <T> T lookup(Map<String, T> map, String token, String kind) {
        T value = map.get(token);
        if (value == null) {
            throw new IllegalArgumentException("Unknown UI theme " + kind + " token '" + token + "' (" + THEME_PATH
                    + " defines: " + new TreeSet<>(map.keySet()) + ")");
        }
        return value;
    }

    private static JsonValue read(String text, String what) {
        if (text == null) {
            throw fail(what + ": no content");
        }
        JsonValue value;
        try {
            value = new JsonReader().parse(text);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Malformed JSON in " + what + ": " + e.getMessage(), e);
        }
        if (value == null || !value.isObject()) {
            throw fail(what + ": top level must be a JSON object");
        }
        return value;
    }

    private static Map<String, Color> readSkinColors(JsonValue skin) {
        JsonValue section = skin.get(SKIN_COLOR_SECTION);
        Map<String, Color> result = new LinkedHashMap<>();
        if (section == null) {
            return result;
        }
        Map<String, JsonValue> raw = new LinkedHashMap<>();
        for (JsonValue entry = section.child; entry != null; entry = entry.next) {
            raw.put(entry.name, entry);
        }
        for (String name : raw.keySet()) {
            JsonValue entry = raw.get(name);
            Set<String> seen = new HashSet<>();
            while (entry != null && entry.isString()) { // alias, e.g. "white: t-white"
                if (!seen.add(entry.asString())) {
                    throw fail(SKIN_PATH + ": colour alias loop at '" + name + "'");
                }
                entry = raw.get(entry.asString());
            }
            if (entry != null && entry.isObject()) {
                result.put(name, new Color(entry.getFloat("r", 0f), entry.getFloat("g", 0f), entry.getFloat("b", 0f),
                        entry.getFloat("a", 1f)));
            }
        }
        return result;
    }

    private static Set<String> childNames(JsonValue section) {
        Set<String> names = new HashSet<>();
        if (section != null) {
            for (JsonValue entry = section.child; entry != null; entry = entry.next) {
                names.add(entry.name);
            }
        }
        return names;
    }

    /** Children of a required object section, with duplicate keys rejected. */
    private static List<JsonValue> entries(JsonValue root, String section) {
        JsonValue value = root.get(section);
        requireObject(value, section);
        List<JsonValue> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (JsonValue entry = value.child; entry != null; entry = entry.next) {
            if (!seen.add(entry.name)) {
                throw fail(section + ": duplicate key '" + entry.name + "'");
            }
            result.add(entry);
        }
        return result;
    }

    private static Map<String, Float> readNumbers(JsonValue root, String section) {
        Map<String, Float> result = new LinkedHashMap<>();
        for (JsonValue entry : entries(root, section)) {
            result.put(entry.name, requirePositive(entry, section + "." + entry.name, true));
        }
        return result;
    }

    private static void checkFields(JsonValue object, Set<String> allowed, String where, boolean allRequired) {
        Set<String> seen = new HashSet<>();
        for (JsonValue entry = object.child; entry != null; entry = entry.next) {
            if (!allowed.contains(entry.name)) {
                throw fail(where + ": unknown field '" + entry.name + "' (allowed: " + new TreeSet<>(allowed) + ")");
            }
            if (!seen.add(entry.name)) {
                throw fail(where + ": duplicate field '" + entry.name + "'");
            }
        }
        if (allRequired && !seen.containsAll(allowed)) {
            Set<String> missing = new TreeSet<>(allowed);
            missing.removeAll(seen);
            throw fail(where + ": missing field(s) " + missing);
        }
    }

    private static void requireObject(JsonValue value, String where) {
        if (value == null || !value.isObject()) {
            throw fail(where + " must be a JSON object");
        }
    }

    private static String requireString(JsonValue value, String where) {
        if (value == null || !value.isString()) {
            throw fail(where + " must be a string, got " + value);
        }
        return value.asString();
    }

    private static float requirePositive(JsonValue value, String where, boolean zeroAllowed) {
        if (value == null || !value.isNumber()) {
            throw fail(where + " must be a number, got " + value);
        }
        float f = value.asFloat();
        if (f < 0 || (!zeroAllowed && f == 0)) {
            throw fail(where + " must be " + (zeroAllowed ? ">= 0" : "> 0") + ", got " + f);
        }
        return f;
    }

    private static Color parseHex(String hex, String where) {
        if (!hex.matches("[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")) {
            throw fail(where + ": '" + hex + "' is not hex RRGGBB or RRGGBBAA");
        }
        return Color.valueOf(hex);
    }

    private static IllegalArgumentException fail(String message) {
        return new IllegalArgumentException("Invalid " + THEME_PATH + ": " + message);
    }
}
