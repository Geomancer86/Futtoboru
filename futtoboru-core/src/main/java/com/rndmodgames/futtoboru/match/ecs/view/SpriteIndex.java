package com.rndmodgames.futtoboru.match.ecs.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * The match sprite index ({@code art/match/index.json}, format 1; contract in {@code tools/art/match/README.md}): the
 * sheets, their layouts, animations and regions {@code [x, y, w, h, pivotX, pivotY]}. Pure Java (no GL), parsed
 * once; {@link IndexActorSprites} uploads the sheets it needs.
 */
public final class SpriteIndex {

    public static final String PATH = "art/match/index.json";
    public static final int FORMAT = 1;

    /** One sheet (a PNG with one layout). */
    public record Sheet(String id, String file, String kind, String layout, int scale, double pixelsPerMetre,
            String club, String season) {
    }

    /** One animation of a layout: {@code dirs} 8 for directional animations, else 0. */
    public record Animation(String name, int frames, double fps, boolean loop, int dirs) {
    }

    /** One layout: animations and regions by name. */
    public record Layout(String name, Map<String, Animation> animations, Map<String, int[]> regions) {
    }

    private final int pixelsPerMetre;
    private final List<String> directions;
    private final List<Sheet> sheets;
    private final Map<String, Sheet> sheetsById;
    private final Map<String, Layout> layouts;

    private SpriteIndex(int pixelsPerMetre, List<String> directions, List<Sheet> sheets, Map<String, Layout> layouts) {
        this.pixelsPerMetre = pixelsPerMetre;
        this.directions = Collections.unmodifiableList(directions);
        this.sheets = Collections.unmodifiableList(sheets);
        Map<String, Sheet> byId = new LinkedHashMap<>();
        for (Sheet s : sheets) {
            if (byId.put(s.id(), s) != null) {
                throw new IllegalArgumentException(PATH + ": sheet " + s.id() + " twice");
            }
            if (!layouts.containsKey(s.layout())) {
                throw new IllegalArgumentException(PATH + ": sheet " + s.id() + " has no layout " + s.layout());
            }
        }
        this.sheetsById = Collections.unmodifiableMap(byId);
        this.layouts = Collections.unmodifiableMap(layouts);
    }

    /** The shipped index from the classpath. */
    public static SpriteIndex load() {
        return parse(new String(resource(PATH), StandardCharsets.UTF_8));
    }

    /** Bytes of a classpath resource under the art root (index, sheets). */
    static byte[] resource(String path) {
        try (InputStream in = SpriteIndex.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(path + " is missing from the classpath");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(1024, in.available()));
            byte[] buffer = new byte[16384];
            for (int n; (n = in.read(buffer)) > 0;) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + path, e);
        }
    }

    public static SpriteIndex parse(String json) {
        JsonValue root = new JsonReader().parse(json);
        int format = root.getInt("format", -1);
        if (format != FORMAT) {
            throw new IllegalArgumentException(PATH + ": format " + format + ", expected " + FORMAT);
        }
        List<String> directions = new ArrayList<>();
        for (JsonValue d = root.get("directions").child; d != null; d = d.next) {
            directions.add(d.asString());
        }
        if (directions.size() != ActorSprites.DIRECTIONS) {
            throw new IllegalArgumentException(PATH + ": " + directions.size() + " directions");
        }
        List<Sheet> sheets = new ArrayList<>();
        for (JsonValue s = root.get("sheets").child; s != null; s = s.next) {
            sheets.add(new Sheet(s.getString("id"), s.getString("file"), s.getString("kind"), s.getString("layout"),
                    s.getInt("scale", 1), s.getDouble("pixelsPerMetre", 5), s.getString("club", null),
                    s.getString("season", null)));
        }
        Map<String, Layout> layouts = new LinkedHashMap<>();
        for (JsonValue l = root.get("layouts").child; l != null; l = l.next) {
            Map<String, Animation> animations = new LinkedHashMap<>();
            for (JsonValue a = l.get("animations").child; a != null; a = a.next) {
                animations.put(a.name, new Animation(a.name, a.getInt("frames"), a.getDouble("fps", 1),
                        a.getBoolean("loop", true), a.getInt("dirs", 0)));
            }
            Map<String, int[]> regions = new LinkedHashMap<>();
            for (JsonValue r = l.get("regions").child; r != null; r = r.next) {
                int[] v = r.asIntArray();
                if (v.length != 6 || v[2] <= 0 || v[3] <= 0) {
                    throw new IllegalArgumentException(PATH + ": region " + l.name + ":" + r.name);
                }
                regions.put(r.name, v);
            }
            layouts.put(l.name, new Layout(l.name, Collections.unmodifiableMap(animations),
                    Collections.unmodifiableMap(regions)));
        }
        return new SpriteIndex(root.getInt("pixelsPerMetre", 5), directions, sheets, layouts);
    }

    /** Region name of one frame: {@code <anim>/<dir>/<frame>} or {@code <anim>/<frame>}. */
    public String regionName(Animation a, int direction, int frame) {
        return a.dirs() > 0 ? a.name() + "/" + directions.get(direction) + "/" + frame : a.name() + "/" + frame;
    }

    public int pixelsPerMetre() {
        return pixelsPerMetre;
    }

    /** Direction names, index 0..7 (S faces the camera, E screen right; see the README). */
    public List<String> directions() {
        return directions;
    }

    public List<Sheet> sheets() {
        return sheets;
    }

    public Sheet sheet(String id) {
        return sheetsById.get(id);
    }

    public Layout layout(String name) {
        return layouts.get(name);
    }

    /** The kit sheet of a club in a season at a scale, or null. */
    public Sheet kit(String season, String club, int scale) {
        for (Sheet s : sheets) {
            if ("kit".equals(s.kind()) && s.scale() == scale && club != null && club.equals(s.club())
                    && (season == null || season.equals(s.season()))) {
                return s;
            }
        }
        return null;
    }

    /** The first sheet of a kind at a scale (officials, people, ball, crowd, touchline, overlay), or null. */
    public Sheet ofKind(String kind, int scale) {
        for (Sheet s : sheets) {
            if (kind.equals(s.kind()) && s.scale() == scale) {
                return s;
            }
        }
        return null;
    }

    /**
     * The club whose kit is drawn for {@code club}: itself when the season has its kit, otherwise the first club of
     * the season (index order) that is not {@code avoid} (the other side's resolved kit), so two missing kits never
     * draw both teams alike. Null when the season has no kit at all.
     */
    public String resolveKit(String season, String club, String avoid) {
        if (kit(season, club, 1) != null) {
            return club;
        }
        String first = null;
        for (Sheet s : sheets) {
            if ("kit".equals(s.kind()) && s.scale() == 1 && (season == null || season.equals(s.season()))) {
                if (first == null) {
                    first = s.club();
                }
                if (!s.club().equals(avoid)) {
                    return s.club();
                }
            }
        }
        return first;
    }
}
