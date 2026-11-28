package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Random;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;
import com.rndmodgames.futtoboru.ui.gfx.TickGate;

/**
 * Crowd CR1 (D26): individual figures on the bank behind the far touchline (this pass, drawn behind the players) and
 * a thinner line at the near touchline ({@link Near}, drawn in front of them). Spectators are arrays, not entities
 * (plan §5 rule 4): places on a grid of rows with a little jitter, filled at the share {@link ViewFrame#crowdSize()} /
 * {@link #CAPACITY}; each figure has one of the 12 baked looks, a mood threshold and an animation phase. Positions and
 * looks come from the viewer's cosmetic random source, never the match.
 *
 * <ul>
 * <li>Mood ({@link ViewFrame#crowdMood()}, -1..1, view-side for now): a figure cheers when the mood reaches his
 * threshold, turns angry when it falls that far below zero, and the least patient encroach when it is very low;
 * otherwise calm ({@link #mood(double, float)}).</li>
 * <li>Module {@code crowd} off: nothing. Its {@code ticks} knob gates the animation clock ({@link TickGate}); its
 * {@code animated} knob is the share of figures that move at all.</li>
 * <li>Sheet scale: 1x only, drawn at whole-screen-pixel multiples ({@link SpriteScale}); figures outside the view are
 * culled.</li>
 * </ul>
 */
public class CrowdPass extends EntitySystem implements DrawPass {

    public static final String MODULE = "crowd";
    public static final String KNOB_ANIMATED = "animated";
    /** Spectators that fill every place on both sides. */
    public static final int CAPACITY = 16000;
    public static final int LOOKS = 12;
    static final String[] MOODS = { "calm", "cheer", "angry", "encroach" };
    static final double[] MOOD_FPS = { 1, 6, 4, 4 };
    public static final int CALM = 0;
    public static final int CHEER = 1;
    public static final int ANGRY = 2;
    public static final int ENCROACH = 3;

    /** Far bank: first row behind the far touchline, last row, row and place spacing, metres. */
    static final double FAR_FRONT_M = 2.7;
    static final double FAR_BACK_M = 9.3;
    static final double ROW_M = 0.75;
    static final double PLACE_M = 0.55;
    /** Near side: rows in front of the near touchline. */
    static final double NEAR_FRONT_M = 2.4;
    static final int NEAR_ROWS = 2;
    /** The near line is thinner (it must not hide the play). */
    static final double NEAR_SHARE = 0.55;
    /** How far past each goal line the banks run. */
    static final double END_M = 14;

    protected final ViewFrame view;
    private final boolean near;
    private final TickGate gate = new TickGate(1);
    private double clock;

    private float[] xs = new float[0];
    private float[] ys = new float[0];
    private byte[] looks = new byte[0];
    private float[] phases = new float[0];
    private float[] thresholds = new float[0];
    private float[] rolls = new float[0];
    private int[] choices = new int[0];
    private int count;
    private int builtSize = -1;
    private PitchGeometry builtPitch;
    private int builtMagnification = -1;

    private ActorSprites builtSprites;
    /** [look][mood][frame]. */
    private ActorSprites.Sprite[][][] table;
    private int drawn;
    private final int[] moodCounts = new int[MOODS.length];

    /** The far bank (behind the players). */
    public CrowdPass(ViewFrame view) {
        this(view, false, SystemPriority.DRAW_CROWD_BACK);
    }

    protected CrowdPass(ViewFrame view, boolean near, int priority) {
        super(priority);
        this.view = view;
        this.near = near;
    }

    /** The near touchline line of spectators (in front of the players). */
    public static final class Near extends CrowdPass {
        public Near(ViewFrame view) {
            super(view, true, SystemPriority.DRAW_CROWD_FRONT);
        }
    }

    @Override
    public void draw(FrameContext frame) {
        drawn = 0;
        java.util.Arrays.fill(moodCounts, 0);
        GraphicsQuality quality = view.quality();
        if (!quality.on(MODULE)) {
            return;
        }
        build();
        gate.setTicks(quality.ticks(MODULE));
        if (!view.paused() && gate.open((float) frame.frameSeconds())) {
            clock += gate.period();
        }
        float animated = quality.knob(MODULE, KNOB_ANIMATED);
        double mood = view.crowdMood();
        ViewCamera cam = view.viewCamera();
        int mag = cam.magnification();
        if (mag != builtMagnification) {
            for (int i = 0; i < count; i++) {
                choices[i] = SpriteScale.choose(cam.scale(ys[i]), mag, 1);
            }
            builtMagnification = mag;
        }
        Batch batch = null;
        ViewArt art = null;
        if (view.draws()) {
            art = view.art();
            table(art.sprites());
            batch = view.batch();
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f);
        }
        double width = cam.orthoWidth();
        for (int i = 0; i < count; i++) {
            double sx = cam.screenX(xs[i], ys[i]);
            if (sx < -24 || sx > width + 24) {
                continue;
            }
            int m = mood(mood, thresholds[i]);
            moodCounts[m]++;
            drawn++;
            if (batch == null) {
                continue;
            }
            double sy = cam.screenY(ys[i], 0);
            ActorSprites.Sprite[] frames = table == null ? null : table[looks[i]][m];
            if (frames == null || frames.length == 0) {
                placeholder(batch, art.white(), looks[i], sx, sy, choices[i], mag);
                continue;
            }
            int f = 0;
            if (rolls[i] < animated) {
                f = Math.floorMod((int) Math.floor(clock * MOOD_FPS[m] + phases[i] * frames.length), frames.length);
            }
            SpriteScale.draw(batch, frames[f], sx, sy, SpriteScale.texel(choices[i]), mag);
        }
        if (batch != null) {
            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();
        }
    }

    /** A figure's mood for the crowd's mood and his threshold (0.15..0.85). */
    public static int mood(double crowdMood, float threshold) {
        if (crowdMood >= threshold) {
            return CHEER;
        }
        if (-crowdMood >= threshold) {
            return -crowdMood >= 0.85 && threshold < 0.45 ? ENCROACH : ANGRY;
        }
        return CALM;
    }

    /** (Re)builds the places when the attendance or the pitch changed. */
    private void build() {
        PitchGeometry pitch = view.pitch();
        int size = view.crowdSize();
        if (size == builtSize && pitch == builtPitch) {
            return;
        }
        builtSize = size;
        builtPitch = pitch;
        builtMagnification = -1;
        double share = Math.min(1.0, size / (double) CAPACITY) * (near ? NEAR_SHARE : 1);
        Random rng = view.cosmetic();
        double hl = pitch.halfLength() + END_M;
        double hw = pitch.halfWidth();
        int places = (int) Math.ceil(2 * hl / PLACE_M);
        int rows = near ? NEAR_ROWS : (int) Math.floor((FAR_BACK_M - FAR_FRONT_M) / ROW_M) + 1;
        int capacity = places * rows;
        ensure(capacity);
        count = 0;
        // far to near (descending y): the far bank from its back row, the near line from the touchline outward
        for (int r = 0; r < rows; r++) {
            double y = near ? -(hw + NEAR_FRONT_M + r * ROW_M) : hw + FAR_BACK_M - r * ROW_M;
            double offset = (r & 1) * PLACE_M * 0.5;
            for (int k = 0; k < places; k++) {
                double fill = rng.nextDouble();
                double jx = (rng.nextDouble() - 0.5) * PLACE_M * 0.6;
                double jy = (rng.nextDouble() - 0.5) * ROW_M * 0.3;
                int look = rng.nextInt(LOOKS);
                float phase = rng.nextFloat();
                float threshold = 0.15f + 0.7f * rng.nextFloat();
                float roll = rng.nextFloat();
                if (fill >= share) {
                    continue;
                }
                xs[count] = (float) (-hl + k * PLACE_M + offset + jx);
                ys[count] = (float) (y + jy);
                looks[count] = (byte) look;
                phases[count] = phase;
                thresholds[count] = threshold;
                rolls[count] = roll;
                count++;
            }
        }
    }

    private void ensure(int n) {
        if (xs.length < n) {
            xs = new float[n];
            ys = new float[n];
            looks = new byte[n];
            phases = new float[n];
            thresholds = new float[n];
            rolls = new float[n];
            choices = new int[n];
        }
    }

    /** Sprite table from the crowd sheet (once per sprite source). */
    private void table(ActorSprites sprites) {
        if (sprites == builtSprites) {
            return;
        }
        builtSprites = sprites;
        table = new ActorSprites.Sprite[LOOKS][MOODS.length][];
        boolean any = false;
        for (int v = 0; v < LOOKS; v++) {
            for (int m = 0; m < MOODS.length; m++) {
                String prefix = String.format("crowd/v%02d/%s/", v, MOODS[m]);
                table[v][m] = frames(sprites, "crowd", prefix, 1);
                any |= table[v][m].length > 0;
            }
        }
        if (!any) {
            table = null;
        }
    }

    /** Frames {@code prefix0, prefix1, ...} of a sheet while they exist. */
    static ActorSprites.Sprite[] frames(ActorSprites sprites, String kind, String prefix, int scale) {
        int n = 0;
        while (n < 64 && sprites.sprite(kind, prefix + n, scale) != null) {
            n++;
        }
        ActorSprites.Sprite[] out = new ActorSprites.Sprite[n];
        for (int f = 0; f < n; f++) {
            out[f] = sprites.sprite(kind, prefix + f, scale);
        }
        return out;
    }

    private static final int[] COATS = { ArtMath.SOOT, ArtMath.COAL, ArtMath.WOOD_DK, ArtMath.SLATE, ArtMath.NAVY,
            ArtMath.BRICK_DK };

    /** No sheet: a dark coat and a face, sized like the figure. */
    private static void placeholder(Batch batch, TextureRegion white, int look, double sx, double sy, int choice,
            int mag) {
        float k = SpriteScale.texel(choice) / (float) mag;
        float x = SpriteScale.snap(sx, mag);
        float y = SpriteScale.snap(sy, mag);
        ViewColors.set(batch, COATS[look % COATS.length], 1f);
        batch.draw(white, x - 2 * k, y, 4 * k, 7 * k);
        ViewColors.set(batch, ArtMath.SKIN, 1f);
        batch.draw(white, x - k, y + 7 * k, 2 * k, 2 * k);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // ---- inspection -------------------------------------------------------------------------------------------------

    /** Spectator figures placed. */
    public int figures() {
        return count;
    }

    /** Figures in view in the last frame. */
    public int drawn() {
        return drawn;
    }

    /** Figures in view in the last frame in a mood ({@link #CALM} .. {@link #ENCROACH}). */
    public int moodCount(int mood) {
        return moodCounts[mood];
    }

    /** Ground line of figure {@code i}. */
    public float figureY(int i) {
        return ys[i];
    }
}
