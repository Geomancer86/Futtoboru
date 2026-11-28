package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;
import com.rndmodgames.futtoboru.ui.gfx.atlas.RuntimeAtlas;

/**
 * The baked match sprites of D26 (E-21s) behind {@link ActorSprites}: reads {@code art/match/index.json}
 * ({@link SpriteIndex}), uploads the sheets this match needs into the viewer's {@link RuntimeAtlas} (both clubs' kits,
 * mud overlay, officials, touchline people, ball, crowd, props; 1x and 2x where baked) and picks every frame:
 *
 * <ul>
 * <li><b>Scale</b>: {@link SpriteScale#choose} from the perspective scale, magnification and the view's scale: 2x
 * everywhere in the default 10 px/m view (D28), the closest of 1x/2x in the wide view; texels always whole screen
 * pixels.</li>
 * <li><b>Animation</b> ({@link #motion}): down -> {@code fall} (once, from the moment he went down), then
 * {@code injured} (lying, looped) if he is hurt; injury "off" -> {@code injured}; moving -> {@code run} (or
 * {@code charge} while charging), its frame rate from the speed ({@link #RUN_REFERENCE_MPS} = the baked rate);
 * otherwise {@code idle}. Slot 0 uses the keeper set (G2; no charge or injured frames there: run, and the last fall
 * frame). Officials walk (rate from speed) or stand.</li>
 * <li><b>Direction</b> ({@link #direction(double)}): from the body's facing, else the motion, else toward the ball;
 * 8 directions, 0 = S (camera).</li>
 * <li><b>Kits</b>: one sheet per side from the match's clubs ({@link SpriteIndex#resolveKit}: a missing kit falls back
 * to another club's, never the same as the other side's).</li>
 * <li><b>Mud</b>: the overlay over every player frame, alpha from the pitch wetness ({@link #mudAlpha}).</li>
 * <li><b>Ball</b> (BA2): {@code high} above {@link #HIGH_BALL_M}, wetness variant from the ball's water share
 * ({@link #ballWetness}), lace spin phase advanced by roll distance and spin.</li>
 * </ul>
 * Animation state (run phase, when a player went down) is view-only, per entity key; time comes from the
 * {@link ViewFrame} (stops while paused). Every entry that touches art passes {@link ArtGuard#check(String)}.
 */
public final class IndexActorSprites implements ActorSprites {

    public static final String DEFAULT_SEASON = "1888-89";
    public static final String ART_ROOT = "art/match/";
    /** Below this speed an actor stands (idle). */
    public static final double IDLE_SPEED_MPS = 0.3;
    /** Speed at which the run cycle plays at its baked rate. */
    public static final double RUN_REFERENCE_MPS = 6.0;
    /** Speed at which officials' walk cycle plays at its baked rate. */
    public static final double WALK_REFERENCE_MPS = 1.6;
    /** The ball uses its larger "high" frames above this height. */
    public static final double HIGH_BALL_M = 1.0;
    /** Ball circumference, m (lace spin from roll distance). */
    public static final double BALL_CIRCUMFERENCE_M = 0.69;
    /** Mud overlay at full strength from this wetness on (rain / wet pitch). */
    public static final double MUD_FULL_WETNESS = PitchConditions.RAIN;
    public static final float MUD_MAX_ALPHA = 0.85f;

    /** What an actor is doing, as far as the pictures go. */
    public enum Motion {
        IDLE, RUN, CHARGE, WALK, FALL, INJURED
    }

    static final String[] OFFICIALS = { "referee", "umpire-a", "umpire-b" };
    static final String[] BALL_WETNESS = { "dry", "damp", "soaked", "muddy" };

    private final SpriteIndex index;
    private final String season;
    private final String homeClub;
    private final String awayClub;

    /** Loaded sheets by scale (index 1 and 2). */
    private final Loaded[] home = new Loaded[3];
    private final Loaded[] away = new Loaded[3];
    private final Loaded[] mud = new Loaded[3];
    private final Map<String, Loaded[]> byKind = new LinkedHashMap<>();
    private boolean loaded;

    private final Frame frame = new Frame();
    private double viewSeconds;
    private double frameSeconds;
    private boolean paused;
    private double wetness;
    private double[] phase = new double[64];
    private double[] downSince = new double[64];
    private boolean[] wasDown = new boolean[64];
    private double ballPhase;

    /** Default season, Preston North End against Accrington. */
    public IndexActorSprites() {
        this(DEFAULT_SEASON, "preston-north-end", "accrington");
    }

    public IndexActorSprites(String season, String homeClub, String awayClub) {
        this(SpriteIndex.load(), season, homeClub, awayClub);
    }

    public IndexActorSprites(SpriteIndex index, String season, String homeClub, String awayClub) {
        this.index = index;
        this.season = season;
        this.homeClub = index.resolveKit(season, homeClub, null);
        this.awayClub = index.resolveKit(season, awayClub, this.homeClub);
        if (this.homeClub == null || this.awayClub == null) {
            throw new IllegalArgumentException("no kit sheets for season " + season + " in " + SpriteIndex.PATH);
        }
    }

    // ---- loading -------------------------------------------------------------------------------------------------

    @Override
    public void load(RuntimeAtlas atlas) {
        ArtGuard.check("match sprites");
        if (loaded) {
            return;
        }
        for (int s = 1; s <= 2; s++) {
            home[s] = load(atlas, index.kit(season, homeClub, s));
            away[s] = load(atlas, index.kit(season, awayClub, s));
            mud[s] = load(atlas, index.ofKind("overlay", s));
            for (String kind : new String[] { "officials", "people", "ball", "crowd", "touchline" }) {
                byKind.computeIfAbsent(kind, k -> new Loaded[3])[s] = load(atlas, index.ofKind(kind, s));
            }
        }
        loaded = true;
    }

    public boolean loaded() {
        return loaded;
    }

    private Loaded load(RuntimeAtlas atlas, SpriteIndex.Sheet sheet) {
        if (sheet == null) {
            return null;
        }
        byte[] png = SpriteIndex.resource(ART_ROOT + sheet.file());
        Pixmap pixmap = new Pixmap(png, 0, png.length);
        try {
            SpriteIndex.Layout layout = index.layout(sheet.layout());
            Loaded out = new Loaded(sheet.scale());
            for (Map.Entry<String, int[]> e : layout.regions().entrySet()) {
                int[] r = e.getValue();
                TextureRegion region = atlas.add(pixmap, r[0], r[1], r[2], r[3]);
                if (region == null) {
                    throw new IllegalStateException(sheet.id() + ":" + e.getKey() + " bigger than an atlas page");
                }
                out.regions.put(e.getKey(), new Sprite(region, r[2], r[3], r[4], r[3] - 1 - r[5], sheet.scale()));
            }
            for (SpriteIndex.Animation a : layout.animations().values()) {
                int dirs = a.dirs() > 0 ? ActorSprites.DIRECTIONS : 1;
                Sprite[][] frames = new Sprite[dirs][a.frames()];
                for (int d = 0; d < dirs; d++) {
                    for (int f = 0; f < a.frames(); f++) {
                        frames[d][f] = out.regions.get(index.regionName(a, d, f));
                    }
                }
                out.animations.put(a.name(), new Anim(a, frames));
            }
            return out;
        } finally {
            pixmap.dispose();
        }
    }

    // ---- per frame -----------------------------------------------------------------------------------------------

    @Override
    public void begin(ViewFrame view) {
        viewSeconds = view.viewSeconds();
        frameSeconds = view.paused() ? 0 : view.frameSeconds();
        paused = view.paused();
        wetness = view.wetness();
    }

    @Override
    public Frame actor(Pose p) {
        if (!loaded || p.role == null) {
            return null;
        }
        boolean official = p.role == SpawnRole.OFFICIAL;
        Loaded[] set = official ? byKind.get("officials")
                : p.role == SpawnRole.HOME_PLAYER ? home : p.role == SpawnRole.AWAY_PLAYER ? away : null;
        if (set == null || set[1] == null) {
            return null;
        }
        int choice = SpriteScale.choose(p.pixelsPerMetre, p.magnification, set[2] != null ? 2 : 1,
                p.viewPixelsPerMetre);
        Loaded sheet = set[SpriteScale.sheet(choice)];
        ensure(p.key);
        boolean down = !official && (p.down || p.injury == 3);
        if (down && !wasDown[p.key]) {
            downSince[p.key] = viewSeconds;
        }
        wasDown[p.key] = down;
        double speed = p.speed();
        boolean keeper = !official && p.index == 0;
        Motion m = motion(official, speed, down, p.injury, p.charge, viewSeconds - downSince[p.key],
                fallSeconds(sheet, keeper));
        String name = animationName(m, official, keeper, p.index);
        Anim anim = sheet.animations.get(name);
        if (anim == null) {
            return null;
        }
        int frames = anim.info.frames();
        int f;
        switch (m) {
            case RUN, CHARGE, WALK -> {
                double ref = m == Motion.WALK ? WALK_REFERENCE_MPS : RUN_REFERENCE_MPS;
                phase[p.key] += frameSeconds * anim.info.fps() * rate(speed, ref);
                f = (int) Math.floor(phase[p.key]) % frames;
            }
            case FALL -> f = (int) Math.min(frames - 1, Math.floor((viewSeconds - downSince[p.key]) * anim.info.fps()));
            case INJURED -> f = keeper ? frames - 1 : loopFrame(viewSeconds, anim.info.fps(), frames, 0);
            default -> f = loopFrame(viewSeconds, anim.info.fps(), frames, offset(p.key));
        }
        int dir = direction(p);
        frame.sprite = anim.frames[anim.frames.length == 1 ? 0 : dir][Math.max(0, f)];
        frame.texel = SpriteScale.texel(choice);
        frame.animation = name;
        frame.direction = dir;
        frame.frame = f;
        frame.overlay = null;
        frame.overlayAlpha = 0;
        float mudAlpha = official ? 0 : mudAlpha(wetness);
        Loaded overlay = mud[SpriteScale.sheet(choice)];
        if (mudAlpha > 0.02f && overlay != null) {
            Anim mudAnim = overlay.animations.get(name);
            if (mudAnim != null) {
                frame.overlay = mudAnim.frames[mudAnim.frames.length == 1 ? 0 : dir][Math.max(0, f)];
                frame.overlayAlpha = mudAlpha;
            }
        }
        return frame.sprite == null ? null : frame;
    }

    @Override
    public Frame ball(Pose p) {
        Loaded[] set = byKind.get("ball");
        if (!loaded || set == null || set[1] == null) {
            return null;
        }
        int choice = SpriteScale.choose(p.pixelsPerMetre, p.magnification, set[2] != null ? 2 : 1,
                p.viewPixelsPerMetre);
        Loaded sheet = set[SpriteScale.sheet(choice)];
        String name = ballAnimation(p.z, p.saturation);
        Anim anim = sheet.animations.get(name);
        if (anim == null) {
            return null;
        }
        double roll = Math.sqrt(p.vx * p.vx + p.vy * p.vy) / BALL_CIRCUMFERENCE_M;
        ballPhase += frameSeconds * (roll + Math.abs(p.spin) / (2 * Math.PI)) * anim.info.frames();
        int frames = anim.info.frames();
        int f = Math.floorMod((int) Math.floor(ballPhase), frames);
        frame.sprite = anim.frames[0][f];
        frame.texel = SpriteScale.texel(choice);
        frame.overlay = null;
        frame.overlayAlpha = 0;
        frame.animation = name;
        frame.direction = 0;
        frame.frame = f;
        return frame.sprite == null ? null : frame;
    }

    @Override
    public Sprite sprite(String kind, String name, int scale) {
        Loaded[] set = byKind.get(kind);
        if (set == null) {
            return null;
        }
        Loaded sheet = scale >= 2 && set[2] != null ? set[2] : set[1];
        return sheet == null ? null : sheet.regions.get(name);
    }

    // ---- rules (pure, tested) ------------------------------------------------------------------------------------

    /** What an actor is doing. */
    public static Motion motion(boolean official, double speed, boolean down, int injury, boolean charge,
            double downSeconds, double fallSeconds) {
        if (official) {
            return speed > IDLE_SPEED_MPS ? Motion.WALK : Motion.IDLE;
        }
        if (down) {
            return downSeconds < fallSeconds || injury == 0 ? Motion.FALL : Motion.INJURED;
        }
        if (speed > IDLE_SPEED_MPS) {
            return charge ? Motion.CHARGE : Motion.RUN;
        }
        return Motion.IDLE;
    }

    /** The animation name in the index ({@code player/run}, {@code keeper/fall}, {@code referee/walk} ...). */
    public static String animationName(Motion m, boolean official, boolean keeper, int index) {
        if (official) {
            String who = OFFICIALS[Math.floorMod(index, OFFICIALS.length)];
            return m == Motion.WALK ? who + "/walk" : who + "/idle";
        }
        if (keeper) {
            return switch (m) {
                case RUN, CHARGE, WALK -> "keeper/run";
                case FALL, INJURED -> "keeper/fall";
                default -> "keeper/idle";
            };
        }
        return switch (m) {
            case RUN, WALK -> "player/run";
            case CHARGE -> "player/charge";
            case FALL -> "player/fall";
            case INJURED -> "player/injured";
            default -> "player/idle";
        };
    }

    /** Direction 0..7 (0 = S, 2 = E, 4 = N, 6 = W) for a heading in rad (0 = +x, counter-clockwise, +y away). */
    public static int direction(double headingRad) {
        int d = (int) Math.round(headingRad / (Math.PI / 4));
        return Math.floorMod(d + 2, ActorSprites.DIRECTIONS);
    }

    /** Direction of an actor: facing, else motion, else toward the ball (S when on it). */
    public static int direction(Pose p) {
        if (p.hasFacing) {
            return direction(p.facing);
        }
        if (p.speed() > IDLE_SPEED_MPS) {
            return direction(Math.atan2(p.vy, p.vx));
        }
        double dx = p.ballX - p.x;
        double dy = p.ballY - p.y;
        return dx * dx + dy * dy < 0.25 ? 0 : direction(Math.atan2(dy, dx));
    }

    /** Frame-rate multiplier from speed: the baked rate at the reference speed, clamped to 0.4..1.6. */
    public static double rate(double speed, double reference) {
        double r = speed / reference;
        return r < 0.4 ? 0.4 : (r > 1.6 ? 1.6 : r);
    }

    /** Mud overlay alpha for a pitch wetness (0 dry, {@link #MUD_MAX_ALPHA} from {@link #MUD_FULL_WETNESS}). */
    public static float mudAlpha(double wetness) {
        double k = wetness / MUD_FULL_WETNESS;
        k = k < 0 ? 0 : (k > 1 ? 1 : k);
        return (float) (k * MUD_MAX_ALPHA);
    }

    /** Ball wetness variant 0..3 (dry, damp, soaked, muddy) from its water share 0..1. */
    public static int ballWetness(double saturation) {
        return saturation < 0.1 ? 0 : saturation < 0.35 ? 1 : saturation < 0.7 ? 2 : 3;
    }

    /** {@code ball/<ground|high>/<wetness>}. */
    public static String ballAnimation(double z, double saturation) {
        return (z > HIGH_BALL_M ? "ball/high/" : "ball/ground/") + BALL_WETNESS[ballWetness(saturation)];
    }

    private static int loopFrame(double seconds, double fps, int frames, double offset) {
        return Math.floorMod((int) Math.floor((seconds + offset) * fps), frames);
    }

    /** A per-actor idle offset so a team does not breathe in step. */
    private static double offset(int key) {
        return ((key * 0.6180339887) % 1.0) * 2.0;
    }

    private static double fallSeconds(Loaded sheet, boolean keeper) {
        Anim a = sheet.animations.get(keeper ? "keeper/fall" : "player/fall");
        return a == null ? 0.5 : a.info.frames() / a.info.fps();
    }

    private void ensure(int key) {
        if (key >= phase.length) {
            int n = Math.max(key + 1, phase.length * 2);
            phase = Arrays.copyOf(phase, n);
            downSince = Arrays.copyOf(downSince, n);
            wasDown = Arrays.copyOf(wasDown, n);
        }
    }

    public String homeClub() {
        return homeClub;
    }

    public String awayClub() {
        return awayClub;
    }

    public SpriteIndex index() {
        return index;
    }

    /** Whether frames are paused (tests). */
    boolean paused() {
        return paused;
    }

    private static final class Loaded {
        final int scale;
        final Map<String, Sprite> regions = new LinkedHashMap<>();
        final Map<String, Anim> animations = new LinkedHashMap<>();

        Loaded(int scale) {
            this.scale = scale;
        }
    }

    private record Anim(SpriteIndex.Animation info, Sprite[][] frames) {
    }
}
