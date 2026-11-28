package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Random;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * The viewer's per-frame context (docs/design/MATCH_VIEWER_PLAN.md §2.1): {@link MatchViewer} fills it before
 * {@code MatchWorld.draw}; every draw pass reads it instead of statics. Besides the {@link FrameContext} values it
 * carries the quality knobs, the camera and batch (null in headless tests) and the viewer's own cosmetic random source,
 * which is never the match fork and may be reseeded by tests. It also holds what the passes share: the pitch being
 * shown, the side-on {@link ViewCamera} (pure maths, also headless) and the {@link ViewArt} (built on the render
 * thread at the first draw with a batch; never in headless runs).
 */
public final class ViewFrame implements FrameContext {

    /** Seed of the cosmetic random source unless reseeded (fixed, so screenshots repeat). */
    public static final long DEFAULT_COSMETIC_SEED = 1888L;

    private final GraphicsQuality quality;
    private final Random cosmetic = new Random(DEFAULT_COSMETIC_SEED);
    private Camera camera;
    private Batch batch;
    private PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
    private PitchArt.Style pitchStyle = PitchArt.Style.of1888();
    private long groundSeed = DEFAULT_COSMETIC_SEED;
    private ActorSprites sprites = ActorSprites.NONE;
    private ViewCamera viewCamera = new ViewCamera(pitch);
    private ViewArt art;

    private double alpha;
    private double frameSeconds;
    private double viewSeconds;
    private long tick;
    private double matchSpeed = 1;
    private boolean paused;

    public ViewFrame(GraphicsQuality quality) {
        if (quality == null) {
            throw new IllegalArgumentException("quality is required");
        }
        this.quality = quality;
    }

    /** Starts a frame: real frame time (not counted while paused), interpolation share, current tick. */
    public void begin(double frameSeconds, double alpha, long tick) {
        this.frameSeconds = frameSeconds > 0 ? frameSeconds : 0;
        if (!paused) {
            this.viewSeconds += this.frameSeconds;
        }
        this.alpha = alpha;
        this.tick = tick;
    }

    @Override
    public double alpha() {
        return alpha;
    }

    @Override
    public double frameSeconds() {
        return frameSeconds;
    }

    @Override
    public double viewSeconds() {
        return viewSeconds;
    }

    @Override
    public long tick() {
        return tick;
    }

    public double matchSpeed() {
        return matchSpeed;
    }

    void setMatchSpeed(double matchSpeed) {
        this.matchSpeed = matchSpeed;
    }

    public boolean paused() {
        return paused;
    }

    void setPaused(boolean paused) {
        this.paused = paused;
    }

    public GraphicsQuality quality() {
        return quality;
    }

    /** Cosmetic randomness for draw passes (particles, flicker). Never used by the simulation. */
    public Random cosmetic() {
        return cosmetic;
    }

    public void reseedCosmetic(long seed) {
        cosmetic.setSeed(seed);
    }

    /** The match camera; null in headless tests. */
    public Camera camera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /** The batch the passes draw into; null in headless tests. */
    public Batch batch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    /** The pitch the viewer shows (1888-89 default ground unless set). */
    public PitchGeometry pitch() {
        return pitch;
    }

    /** Shows another pitch and look; rebuilds the camera rig and drops art built for the old one. */
    public void setPitch(PitchGeometry pitch, PitchArt.Style style, long groundSeed) {
        if (pitch == null || style == null) {
            throw new IllegalArgumentException("pitch and style are required");
        }
        int w = viewCamera.viewWidth();
        int h = viewCamera.viewHeight();
        this.pitch = pitch;
        this.pitchStyle = style;
        this.groundSeed = groundSeed;
        this.viewCamera = new ViewCamera(pitch);
        viewCamera.resizeView(w, h);
        disposeArt();
    }

    public PitchArt.Style pitchStyle() {
        return pitchStyle;
    }

    /** Actor pictures ({@link ActorSprites#NONE} until the sprite sheets land: placeholders). */
    public void setSprites(ActorSprites sprites) {
        this.sprites = sprites == null ? ActorSprites.NONE : sprites;
        disposeArt();
    }

    /** The side-on camera V3. */
    public ViewCamera viewCamera() {
        return viewCamera;
    }

    /** The viewer art, built on first use (render thread only; throws inside a tick or when art is forbidden). */
    public ViewArt art() {
        if (art == null) {
            art = new ViewArt(pitch, pitchStyle, groundSeed, sprites);
        }
        art.load();
        return art;
    }

    /** Whether a pass should draw this frame: a batch is set (never in headless runs). */
    public boolean draws() {
        return batch != null;
    }

    /** Frees the GPU art (the viewer's screen calls it on dispose). */
    public void disposeArt() {
        if (art != null) {
            art.dispose();
            art = null;
        }
    }
}
