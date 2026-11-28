// Adapted from DarkBlade (GraphicsQuality), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * The viewer's performance knobs as data (docs/design/MATCH_VIEWER_PLAN.md §2.4): {@code gfx/quality.json} on the
 * classpath.
 *
 * <ul>
 * <li>A knob belongs to a module and has one value per quality level ({@link #QUALITIES}): every how many view ticks
 * a pass works, caps, densities, the resolution of a pass. No pass writes its cost in code: it reads
 * {@link #knob(String, String)} or {@link #ticks(String)} every time, so {@link #select(String)} switches live.</li>
 * <li>{@link #on(String)}: the player's switches ({@link VfxSwitches}: per module, master, {@code -D} kill flag), then
 * the module's {@code on} knob.</li>
 * <li>Starting level: {@code -Dfuttoboru.gfx.quality=<level>} wins; else {@link #LOW} on a software GL renderer
 * ({@link #adoptRenderer(String)}); else the file's {@code default}.</li>
 * <li>Parsing is strict (unknown fields, wrong value counts and unknown levels throw) and needs no GL context.</li>
 * </ul>
 *
 * One instance per viewer (held by its {@code ViewFrame}); no global state.
 */
public final class GraphicsQuality {

    public static final String PATH = "gfx/quality.json";
    public static final int SCHEMA_VERSION = 1;

    public static final String LOW = "low";
    public static final String MEDIUM = "medium";
    public static final String HIGH = "high";
    public static final String ULTRA = "ultra";

    public static final List<String> QUALITIES = List.of(LOW, MEDIUM, HIGH, ULTRA);

    /** The knob that turns a module off: 1 on, 0 off. */
    public static final String ON = "on";
    /** The knob of every how many view ticks (1/60 s) a module works. */
    public static final String TICKS = "ticks";

    /** {@code -Dfuttoboru.gfx.quality=<low|medium|high|ultra>} picks the level whatever the renderer is. */
    public static final String QUALITY_PROPERTY = "futtoboru.gfx.quality";

    /** GL renderer names that draw with the CPU (Mesa llvmpipe, softpipe, swrast; Windows' own). */
    private static final String[] SOFTWARE_RENDERERS = { "llvmpipe", "softpipe", "swrast", "software rasterizer",
            "gdi generic", "microsoft basic render" };

    private static final Set<String> TOP_LEVEL_FIELDS = Set.of("schemaVersion", "default", "modules");

    // module -> knob -> one value per level
    private final Map<String, Map<String, float[]>> knobs;
    private final VfxSwitches switches;
    private final String fileDefault;
    private String quality;

    private GraphicsQuality(Map<String, Map<String, float[]>> knobs, String fileDefault, VfxSwitches switches) {
        this.knobs = knobs;
        this.fileDefault = fileDefault;
        this.switches = switches;
        this.quality = startingQuality(fileDefault, false);
    }

    // ---- loading --------------------------------------------------------------------------------------------------

    /** The shipped knobs ({@link #PATH} on the classpath). */
    public static GraphicsQuality load() {
        try (InputStream in = GraphicsQuality.class.getClassLoader().getResourceAsStream(PATH)) {
            if (in == null) {
                throw new IllegalStateException(PATH + " is missing from the classpath");
            }
            return parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + PATH, e);
        }
    }

    /** Knobs from this text; the kill flag comes from the system property. */
    public static GraphicsQuality parse(String json) {
        return parse(json, Boolean.getBoolean(VfxSwitches.OFF_PROPERTY));
    }

    /** Knobs from this text with the kill flag given (tests). */
    public static GraphicsQuality parse(String json, boolean forcedOff) {
        JsonValue root = new JsonReader().parse(json);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException(PATH + ": not a JSON object");
        }
        for (JsonValue field = root.child; field != null; field = field.next) {
            if (!TOP_LEVEL_FIELDS.contains(field.name)) {
                throw new IllegalArgumentException(PATH + ": unknown field " + field.name);
            }
        }
        int schema = root.getInt("schemaVersion", -1);
        if (schema != SCHEMA_VERSION) {
            throw new IllegalArgumentException(PATH + ": schemaVersion " + schema + ", expected " + SCHEMA_VERSION);
        }
        String byDefault = root.getString("default", HIGH);
        if (!QUALITIES.contains(byDefault)) {
            throw new IllegalArgumentException(PATH + ": default " + byDefault + " is not one of " + QUALITIES);
        }
        Map<String, Map<String, float[]>> parsed = new LinkedHashMap<>();
        JsonValue modules = root.get("modules");
        for (JsonValue module = modules != null ? modules.child : null; module != null; module = module.next) {
            if (parsed.containsKey(module.name)) {
                throw new IllegalArgumentException(PATH + ": module " + module.name + " twice");
            }
            Map<String, float[]> moduleKnobs = new LinkedHashMap<>();
            for (JsonValue knob = module.child; knob != null; knob = knob.next) {
                if (moduleKnobs.put(knob.name, values(module.name + "." + knob.name, knob)) != null) {
                    throw new IllegalArgumentException(PATH + ": knob " + module.name + "." + knob.name + " twice");
                }
            }
            parsed.put(module.name, moduleKnobs);
        }
        return new GraphicsQuality(parsed, byDefault, new VfxSwitches(parsed.keySet(), forcedOff));
    }

    /** One number for every level, or one number per level in the order of {@link #QUALITIES}. */
    private static float[] values(String name, JsonValue knob) {
        if (knob.isNumber()) {
            float value = knob.asFloat();
            return new float[] { value, value, value, value };
        }
        if (!knob.isArray()) {
            throw new IllegalArgumentException(PATH + ": " + name + " must be a number or an array");
        }
        float[] values = knob.asFloatArray();
        if (values.length != QUALITIES.size()) {
            throw new IllegalArgumentException(PATH + ": " + name + " needs " + QUALITIES.size() + " values " + QUALITIES);
        }
        return values;
    }

    // ---- reading --------------------------------------------------------------------------------------------------

    /**
     * The value of the module's knob at the current level.
     *
     * @throws IllegalArgumentException when the knob does not exist: a pass asking for one nobody declared is a bug
     */
    public float knob(String module, String name) {
        Map<String, float[]> moduleKnobs = knobs.get(module);
        float[] values = moduleKnobs != null ? moduleKnobs.get(name) : null;
        if (values == null) {
            throw new IllegalArgumentException("no graphics knob " + module + "." + name + " in " + PATH);
        }
        return values[QUALITIES.indexOf(quality)];
    }

    /** Every how many view ticks (1/60 s) the module works, at least 1. */
    public int ticks(String module) {
        return Math.max(1, Math.round(knob(module, TICKS)));
    }

    /** Whether the module draws at all: the player's switches and the kill flag, then the module's {@code on} knob. */
    public boolean on(String module) {
        if (!switches.on(module)) {
            return false;
        }
        Map<String, float[]> moduleKnobs = knobs.get(module);
        return moduleKnobs == null || !moduleKnobs.containsKey(ON) || knob(module, ON) > 0f;
    }

    public String quality() {
        return quality;
    }

    /** Changes the level while the match is shown; returns false (and keeps the level) for an unknown one. */
    public boolean select(String level) {
        if (QUALITIES.contains(level)) {
            this.quality = level;
            return true;
        }
        return false;
    }

    public VfxSwitches switches() {
        return switches;
    }

    public List<String> modules() {
        return new ArrayList<>(knobs.keySet());
    }

    public List<String> knobs(String module) {
        Map<String, float[]> moduleKnobs = knobs.get(module);
        return moduleKnobs != null ? new ArrayList<>(moduleKnobs.keySet()) : Collections.<String>emptyList();
    }

    // ---- renderer ---------------------------------------------------------------------------------------------------

    /** Whether a GL renderer name ({@code glGetString(GL_RENDERER)}) draws with the CPU, as Mesa's llvmpipe does. */
    public static boolean isSoftwareRenderer(String renderer) {
        if (renderer == null) {
            return false;
        }
        String name = renderer.toLowerCase(Locale.ROOT);
        for (String software : SOFTWARE_RENDERERS) {
            if (name.contains(software)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Told once when the viewer starts with GL: a renderer that draws with the CPU drops the level to {@link #LOW};
     * {@code -Dfuttoboru.gfx.quality} wins over both.
     *
     * @return the level the knobs are read at from now on
     */
    public String adoptRenderer(String renderer) {
        quality = startingQuality(fileDefault, isSoftwareRenderer(renderer));
        return quality;
    }

    /** The command line, else low on a CPU renderer, else the file's default. */
    private static String startingQuality(String byFile, boolean software) {
        String forced = System.getProperty(QUALITY_PROPERTY);
        if (forced != null && QUALITIES.contains(forced)) {
            return forced;
        }
        return software ? LOW : byFile;
    }
}
