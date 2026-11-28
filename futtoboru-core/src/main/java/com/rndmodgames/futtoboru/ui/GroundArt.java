package com.rndmodgames.futtoboru.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Ground illustrations (U-11): {@code art/grounds/<ground-id>.png} (320x180) and {@code <ground-id>_thumb.png}
 * (64x36), drawn by {@code tools/art/ground_art.py}.
 *
 * <p>One cache per game ({@code Futtoboru.getGroundArt()}); textures are loaded on first use with the Nearest filter,
 * drawn at a whole-number scale only (pixel art stays crisp) and disposed with the game. A ground without a picture
 * (or a club without a ground) gets a plain bordered panel of the same size, so layouts do not jump.
 *
 * <p>The ground of a club is its season ground: {@code squads.json} links each club of the season to a ground and
 * {@code DatabaseLoader} copies it to {@link Club#getStadium()} with the ground's data id.
 */
public final class GroundArt implements Disposable {

    public static final String DIR = "art/grounds/";
    public static final int ART_WIDTH = 320;
    public static final int ART_HEIGHT = 180;
    public static final int THUMB_WIDTH = 64;
    public static final int THUMB_HEIGHT = 36;

    /** Skin drawable behind the placeholder panel. */
    static final String FALLBACK_DRAWABLE = "border";

    private final Predicate<String> exists;
    private final Function<String, Texture> loader;
    /** path -> texture; a path that is missing or failed maps to null, so it is looked up only once */
    private final Map<String, Texture> cache = new HashMap<>();

    /** Reads internal files (classpath / assets) through libGDX. */
    public GroundArt() {
        this(path -> Gdx.files != null && Gdx.files.internal(path).exists(),
                path -> new Texture(Gdx.files.internal(path)));
    }

    /** For tests: {@code exists} answers whether a path is there, {@code loader} makes its texture. */
    GroundArt(Predicate<String> exists, Function<String, Texture> loader) {
        this.exists = exists;
        this.loader = loader;
    }

    // ------------------------------------------------------------------ id and path resolution (headless)

    /** @return the data id of the club's season ground, or null when the club or its ground is unknown */
    public static String groundId(Club club) {
        return club == null ? null : groundId(club.getStadium());
    }

    /** @return the ground's data id (blank ids count as none) */
    public static String groundId(Stadium stadium) {
        if (stadium == null || stadium.getDataId() == null || stadium.getDataId().isBlank()) {
            return null;
        }
        return stadium.getDataId();
    }

    /** @return {@code art/grounds/<id>.png} or {@code art/grounds/<id>_thumb.png}; null for no id */
    public static String path(String groundId, boolean thumb) {
        if (groundId == null || groundId.isBlank()) {
            return null;
        }
        return DIR + groundId + (thumb ? "_thumb" : "") + ".png";
    }

    /**
     * Largest whole-number scale at which a {@code width} x {@code height} picture fits in {@code maxWidth} x
     * {@code maxHeight}; never below 1 (a too-small box shows the picture at 1x rather than blurring it).
     */
    public static int integerScale(int width, int height, float maxWidth, float maxHeight) {
        if (width <= 0 || height <= 0) {
            return 1;
        }
        int scale = (int) Math.min(Math.floor(maxWidth / width), Math.floor(maxHeight / height));
        return Math.max(1, scale);
    }

    // ------------------------------------------------------------------ textures

    /** @return the cached texture (Nearest filter), or null when the ground has no picture */
    public synchronized Texture texture(String groundId, boolean thumb) {
        String path = path(groundId, thumb);
        if (path == null) {
            return null;
        }
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        Texture texture = null;
        try {
            if (exists.test(path)) {
                texture = loader.apply(path);
                if (texture != null) {
                    texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
                }
            }
        } catch (RuntimeException e) {
            Log.warn(Log.UI_CLUB, "GroundArt", "cannot load " + path + ": " + e);
            texture = null;
        }
        cache.put(path, texture);
        return texture;
    }

    /** @return true when the ground has a picture (loads it) */
    public boolean hasPicture(String groundId, boolean thumb) {
        return texture(groundId, thumb) != null;
    }

    /** Number of paths looked up so far (tests). */
    synchronized int cachedPaths() {
        return cache.size();
    }

    @Override
    public synchronized void dispose() {
        for (Texture texture : cache.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        cache.clear();
    }

    // ------------------------------------------------------------------ widgets (need VisUI)

    /**
     * The picture of a ground at the largest whole-number scale that fits the box, or a plain bordered panel of
     * the same size when there is none.
     */
    public Actor picture(String groundId, boolean thumb, float maxWidth, float maxHeight) {
        int w = thumb ? THUMB_WIDTH : ART_WIDTH;
        int h = thumb ? THUMB_HEIGHT : ART_HEIGHT;
        Texture texture = texture(groundId, thumb);
        if (texture != null) {
            w = texture.getWidth();
            h = texture.getHeight();
        }
        int scale = integerScale(w, h, maxWidth, maxHeight);
        VisTable box = new VisTable();
        if (texture != null) {
            box.add(new Image(texture)).size(w * scale, h * scale);
        } else {
            VisTable panel = new VisTable();
            panel.setBackground(VisUI.getSkin().getDrawable(FALLBACK_DRAWABLE));
            if (!thumb) {
                VisLabel none = new VisLabel(LanguageModLoader.getValue("ground_no_picture"));
                none.setColor(UiTheme.color("text.muted"));
                none.setWrap(true);
                none.setAlignment(Align.center);
                panel.add(none).width(w * scale - UiTheme.space("lg")).center();
            }
            box.add(panel).size(w * scale, h * scale);
        }
        return box;
    }

    /** Ground panel: picture (or placeholder), then the ground's name and capacity below it. */
    public VisTable panel(Stadium stadium, float maxWidth, float maxHeight) {
        VisTable panel = new VisTable(true);
        panel.add(picture(groundId(stadium), false, maxWidth, maxHeight)).center();
        if (stadium != null) {
            panel.row();
            VisLabel name = new VisLabel(stadium.getName());
            name.setFontScale(UiTheme.fontScale("subheading"));
            panel.add(name).center();
            if (stadium.getCapacity() != null) {
                panel.row();
                VisLabel capacity = new VisLabel(LanguageModLoader.getValue("ground_capacity") + ": "
                        + String.format(java.util.Locale.UK, "%,d", stadium.getCapacity()));
                capacity.setColor(UiTheme.color("text.secondary"));
                panel.add(capacity).center();
            }
        }
        return panel;
    }
}
