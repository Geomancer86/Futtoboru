package com.rndmodgames.futtoboru.match.ecs.view;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;
import com.rndmodgames.futtoboru.ui.gfx.atlas.RuntimeAtlas;

/**
 * The match viewer's art on one {@link RuntimeAtlas} page: the generated pitch (P1/A1/SL1), the skyline strip (K1),
 * the white piece every flat shape tints, a soft ellipse for shadows, the ball in a few sizes (BA2), and whatever the
 * {@link ActorSprites} pack. Built once, on the render thread, the first time a pass draws with a batch
 * ({@link #load()}); headless runs never build it. Every entry point passes {@link ArtGuard#check(String)}.
 */
public final class ViewArt implements Disposable {

    /** Ball sprite diameters, art pixels (index = diameter - {@link #BALL_MIN}). */
    public static final int BALL_MIN = 2;
    public static final int BALL_MAX = 8;
    public static final int ELLIPSE_W = 16;
    public static final int ELLIPSE_H = 8;

    private final PitchGeometry pitch;
    private final PitchArt.Style style;
    private final long groundSeed;
    private final ActorSprites sprites;

    private RuntimeAtlas atlas;
    private PitchArt pitchArt;
    private SkylineArt skylineArt;
    private TextureRegion white;
    private TextureRegion pitchRegion;
    private TextureRegion skylineRegion;
    private TextureRegion ellipse;
    private final TextureRegion[] balls = new TextureRegion[BALL_MAX - BALL_MIN + 1];

    public ViewArt(PitchGeometry pitch, PitchArt.Style style, long groundSeed, ActorSprites sprites) {
        if (pitch == null || style == null || sprites == null) {
            throw new IllegalArgumentException("pitch, style and sprites are required");
        }
        this.pitch = pitch;
        this.style = style;
        this.groundSeed = groundSeed;
        this.sprites = sprites;
    }

    /** Generates and uploads everything (once; later calls are free). */
    public void load() {
        ArtGuard.check("match viewer art");
        if (atlas != null) {
            return;
        }
        atlas = new RuntimeAtlas();
        white = atlas.white();
        pitchArt = new PitchArt(pitch, style);
        pitchRegion = upload(pitchArt.rgb, pitchArt.width, pitchArt.height);
        skylineArt = new SkylineArt(groundSeed);
        skylineRegion = upload(skylineArt.rgb, skylineArt.width, skylineArt.height);
        ellipse = upload(ellipsePixels(ELLIPSE_W, ELLIPSE_H), ELLIPSE_W, ELLIPSE_H, true);
        for (int d = BALL_MIN; d <= BALL_MAX; d++) {
            balls[d - BALL_MIN] = upload(ballPixels(d), d, d, true);
        }
        sprites.load(atlas);
    }

    public boolean loaded() {
        return atlas != null;
    }

    private TextureRegion upload(int[] rgb, int w, int h) {
        return upload(rgb, w, h, false);
    }

    /** {@code withAlpha}: values are 0xAARRGGBB (alpha 0 = transparent); otherwise opaque 0xRRGGBB. */
    private TextureRegion upload(int[] pixels, int w, int h, boolean withAlpha) {
        Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
        try {
            ByteBuffer buffer = pixmap.getPixels();
            buffer.clear();
            for (int i = 0; i < w * h; i++) {
                int c = pixels[i];
                buffer.put((byte) (c >> 16));
                buffer.put((byte) (c >> 8));
                buffer.put((byte) c);
                buffer.put(withAlpha ? (byte) (c >>> 24) : (byte) 0xFF);
            }
            buffer.flip();
            TextureRegion region = atlas.add(pixmap);
            if (region == null) {
                throw new IllegalStateException("art piece " + w + "x" + h + " is bigger than an atlas page");
            }
            return region;
        } finally {
            pixmap.dispose();
        }
    }

    /** A white ellipse, opaque inside (0xAARRGGBB). */
    static int[] ellipsePixels(int w, int h) {
        int[] px = new int[w * h];
        double rx = w / 2.0;
        double ry = h / 2.0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dx = (x + 0.5 - rx) / rx;
                double dy = (y + 0.5 - ry) / ry;
                px[y * w + x] = dx * dx + dy * dy <= 1 ? 0xFFFFFFFF : 0;
            }
        }
        return px;
    }

    /** BA2 ball: a leather circle lit from the top left with a cream lace (0xAARRGGBB). */
    static int[] ballPixels(int d) {
        int[] px = new int[d * d];
        double r = d / 2.0;
        int dark = ArtMath.mix(ArtMath.mul(ArtMath.LEATHER, 0.64), 0x262A50, 0.2);
        int light = ArtMath.mix(ArtMath.LEATHER, 0xFFF6DE, 0.24);
        for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
                double dx = x + 0.5 - r;
                double dy = y + 0.5 - r;
                if (dx * dx + dy * dy > r * r + r * 0.3) {
                    continue;
                }
                double l = -(dx + dy) / (r * 1.6);
                int c = l > 0.35 ? light : (l > -0.3 ? ArtMath.LEATHER : dark);
                px[y * d + x] = 0xFF000000 | c;
            }
        }
        if (d >= 4) {
            int cx = d / 2;
            px[(d / 2 - 1) * d + cx] = 0xFF000000 | ArtMath.CREAM;
        }
        return px;
    }

    // ---- pieces --------------------------------------------------------------------------------------------------

    public RuntimeAtlas atlas() {
        return atlas;
    }

    public TextureRegion white() {
        return white;
    }

    public PitchArt pitchArt() {
        return pitchArt;
    }

    public TextureRegion pitch() {
        return pitchRegion;
    }

    public SkylineArt skylineArt() {
        return skylineArt;
    }

    public TextureRegion skyline() {
        return skylineRegion;
    }

    public TextureRegion ellipse() {
        return ellipse;
    }

    /** The ball sprite nearest to a diameter in art pixels. */
    public TextureRegion ball(double diameter) {
        int d = (int) Math.round(diameter);
        d = d < BALL_MIN ? BALL_MIN : (d > BALL_MAX ? BALL_MAX : d);
        return balls[d - BALL_MIN];
    }

    public ActorSprites sprites() {
        return sprites;
    }

    @Override
    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
    }
}
