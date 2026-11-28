package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.ui.gfx.DepthSort;

/**
 * Everything standing on the pitch, depth-sorted together ({@link DepthSort}): players, officials and the ball (each
 * from its interpolated {@link ViewTransform}), the goal frames (posts and crossbar, no nets in 1888-89) and the corner
 * flags. The side-on camera sees the far side of the pitch first, so the depth is {@code -y}; the ball sorts at its
 * shadow point (its ground {@code y}) and is drawn lifted by its height.
 *
 * <p>Pictures come from {@link ActorSprites} ({@link IndexActorSprites}: the baked sheets of D26): the pass fills an
 * {@link ActorSprites.Pose} per actor from its view transform and, read only, its body (team slot, facing, velocity,
 * down, injury, charge) or ball (velocity, spin, water), and draws the chosen frame with its feet pivot on the
 * projected ground point (the ball lifted by its height), at the sheet scale and whole-screen-pixel texel size
 * {@link SpriteScale} picked from the perspective scale, then the mud overlay. Without a sprite an actor is a
 * placeholder figure in the team's colours (shirt, knickers, socks, head; the keeper with a cap), sized by its depth.
 * {@link #collect()} sorts also headless (tests read the order).
 */
public final class ActorPass extends EntitySystem implements DrawPass {

    /** What an entry is. */
    public static final int ACTOR = 0;
    public static final int BALL = 1;
    public static final int POST = 2;
    public static final int FLAG = 3;

    /** Placeholder kits: shirt, knickers, socks per role (home, away, official). */
    private static final int[][] KITS = { { ArtMath.RED, ArtMath.WHITE, ArtMath.NAVY },
            { ArtMath.WHITE, ArtMath.NAVY, ArtMath.BLACK }, { ArtMath.BLACK, ArtMath.SOOT, ArtMath.BLACK } };
    private static final int FRAME_PAINT = 0xF0ECE0;
    private static final double PERSON_HEIGHT_M = 1.75;
    private static final double PERSON_WIDTH_M = 0.45;
    private static final double FLAG_POLE_M = 1.6;
    /** BA2: the ball drawn at almost three times its size so it reads at 5 px/m. */
    private static final double BALL_DRAWN_M = 0.6;

    private final ViewFrame view;
    private final InterpolationPass interpolation;
    private final ComponentMapper<IdentityComponent> ids = ComponentMapper.getFor(IdentityComponent.class);
    private final ComponentMapper<BodyComponent> bodies = ComponentMapper.getFor(BodyComponent.class);
    private final ComponentMapper<InjuryComponent> injuries = ComponentMapper.getFor(InjuryComponent.class);
    private final ComponentMapper<MoveCommand> commands = ComponentMapper.getFor(MoveCommand.class);
    private final ComponentMapper<BallComponent> balls = ComponentMapper.getFor(BallComponent.class);
    private final ActorSprites.Pose pose = new ActorSprites.Pose();
    private ViewTransform ballView;
    private final DepthSort sort = new DepthSort();
    private int[] kinds = new int[64];
    private int[] refs = new int[64];
    private int[] kits = new int[64];
    private int count;

    public ActorPass(ViewFrame view, InterpolationPass interpolation) {
        super(SystemPriority.DRAW_ACTORS);
        this.view = view;
        this.interpolation = interpolation;
    }

    @Override
    public void draw(FrameContext frame) {
        collect();
        if (!view.draws()) {
            return;
        }
        ViewArt art = view.art();
        art.sprites().begin(view);
        Batch batch = view.batch();
        batch.begin();
        for (int i = 0; i < sort.size(); i++) {
            int e = sort.order(i);
            switch (kinds[e]) {
                case ACTOR -> actor(batch, art, interpolation.get(refs[e]), kits[e]);
                case BALL -> ball(batch, art, interpolation.get(refs[e]));
                case POST -> post(batch, art.white(), refs[e]);
                default -> flag(batch, art.white(), refs[e], frame.viewSeconds());
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /** Sorts this frame's entries back to front. */
    public void collect() {
        sort.clear();
        count = 0;
        int home = 0;
        int away = 0;
        int officials = 0;
        ballView = null;
        for (int i = 0; i < interpolation.size(); i++) {
            ViewTransform t = interpolation.get(i);
            IdentityComponent id = ids.get(t.entity);
            if (id == null || id.role == SpawnRole.CROWD_SECTION) {
                continue;
            }
            if (id.role == SpawnRole.BALL && ballView == null) {
                ballView = t;
            }
            BodyComponent body = bodies.get(t.entity);
            int kit = body != null ? body.slot : switch (id.role) {
                case HOME_PLAYER -> home++;
                case AWAY_PLAYER -> away++;
                case OFFICIAL -> officials++;
                default -> 0;
            };
            entry(id.role == SpawnRole.BALL ? BALL : ACTOR, i, kit, -t.y, id.index);
        }
        PitchGeometry pitch = view.pitch();
        for (int k = 0; k < 4; k++) {
            entry(POST, k, 0, -postY(pitch, k), 100_000 + k);
            entry(FLAG, k, 0, -flagY(pitch, k), 200_000 + k);
        }
        sort.sort();
    }

    private void entry(int kind, int ref, int kit, double depth, int tie) {
        if (count == kinds.length) {
            kinds = java.util.Arrays.copyOf(kinds, count * 2);
            refs = java.util.Arrays.copyOf(refs, count * 2);
            kits = java.util.Arrays.copyOf(kits, count * 2);
        }
        kinds[count] = kind;
        refs[count] = ref;
        kits[count] = kit;
        count++;
        sort.add((float) depth, tie);
    }

    // posts and flags: k = end * 2 + flank (end 0 = left goal, flank 0 = near side)
    private static double postX(PitchGeometry p, int k) {
        return (k < 2 ? -1 : 1) * p.halfLength();
    }

    private static double postY(PitchGeometry p, int k) {
        return (k % 2 == 0 ? -1 : 1) * p.postCentreY();
    }

    private static double flagY(PitchGeometry p, int k) {
        return (k % 2 == 0 ? -1 : 1) * p.halfWidth();
    }

    // ---- drawing --------------------------------------------------------------------------------------------------

    private void actor(Batch batch, ViewArt art, ViewTransform t, int kit) {
        IdentityComponent id = ids.get(t.entity);
        ViewCamera cam = view.viewCamera();
        double scale = cam.scale(t.y);
        ActorSprites.Pose p = pose(t, id, kit, scale);
        double sx = cam.screenX(t.x, t.y);
        double sy = cam.screenY(t.y, Math.max(0, t.z));
        ActorSprites.Frame sprite = art.sprites().actor(p);
        if (sprite != null) {
            drawFrame(batch, sprite, sx, sy, p.magnification);
            return;
        }
        int direction = IndexActorSprites.direction(p);
        int step = (int) Math.floor((t.x + t.y) * 2.5);
        placeholder(batch, art.white(), id.role, kit, Math.round(sx), Math.round(sy), scale, direction, step);
    }

    /** Fills the reused pose of one actor: view position, and read-only copies of its body or ball state. */
    ActorSprites.Pose pose(ViewTransform t, IdentityComponent id, int kit, double scale) {
        ActorSprites.Pose p = pose.clear();
        p.key = id.index;
        p.role = id.role;
        p.index = kit;
        p.x = t.x;
        p.y = t.y;
        p.z = t.z;
        p.pixelsPerMetre = scale;
        p.magnification = view.viewCamera().magnification();
        if (ballView != null) {
            p.ballX = ballView.x;
            p.ballY = ballView.y;
        }
        BodyComponent body = bodies.get(t.entity);
        BallComponent ball = balls.get(t.entity);
        if (body != null) {
            p.vx = body.vx;
            p.vy = body.vy;
            p.facing = body.facing;
            p.hasFacing = true;
            p.down = body.isDown();
            InjuryComponent injury = injuries.get(t.entity);
            p.injury = injury == null ? 0 : injury.state;
            MoveCommand cmd = commands.get(t.entity);
            p.charge = cmd != null && cmd.charge;
        } else if (ball != null) {
            p.vx = ball.vx;
            p.vy = ball.vy;
            p.vz = ball.vz;
            p.spin = ball.spin;
            p.saturation = ball.saturation;
        } else {
            double dt = view.tickSeconds();
            p.vx = (t.currX() - t.prevX()) / dt;
            p.vy = (t.currY() - t.prevY()) / dt;
        }
        return p;
    }

    /** A chosen frame (and its overlay) with the feet pivot at (sx, sy), texels on whole screen pixels. */
    static void drawFrame(Batch batch, ActorSprites.Frame f, double sx, double sy, int magnification) {
        batch.setColor(1f, 1f, 1f, 1f);
        SpriteScale.draw(batch, f.sprite, sx, sy, f.texel, magnification);
        if (f.overlay != null && f.overlayAlpha > 0) {
            batch.setColor(1f, 1f, 1f, f.overlayAlpha);
            SpriteScale.draw(batch, f.overlay, sx, sy, f.texel, magnification);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /** A blocky figure in the role's colours, feet at (fx, fy). */
    private static void placeholder(Batch batch, TextureRegion white, SpawnRole role, int kit, float fx, float fy,
            double scale, int direction, int step) {
        int[] colours = KITS[role == SpawnRole.HOME_PLAYER ? 0 : (role == SpawnRole.AWAY_PLAYER ? 1 : 2)];
        int h = (int) Math.max(6, Math.round(PERSON_HEIGHT_M * scale));
        int w = (int) Math.max(2, Math.round(PERSON_WIDTH_M * scale));
        float x0 = fx - w / 2;
        int legs = Math.round(h * 0.42f);
        int knickers = Math.max(1, Math.round(h * 0.14f));
        int head = Math.max(2, Math.round(h * 0.15f));
        int shirt = h - legs - knickers - head;
        // legs: two socks, apart on alternate steps when running sideways
        boolean apart = (step & 1) == 1 && direction % 4 != 0;
        int leg = Math.max(1, w / 3);
        ViewColors.set(batch, colours[2], 1f);
        batch.draw(white, x0 - (apart ? 1 : 0), fy, leg, legs);
        batch.draw(white, x0 + w - leg + (apart ? 1 : 0), fy, leg, legs);
        ViewColors.set(batch, colours[1], 1f);
        batch.draw(white, x0, fy + legs, w, knickers);
        ViewColors.set(batch, colours[0], 1f);
        batch.draw(white, x0, fy + legs + knickers, w, shirt);
        // arms one pixel outside the shirt when the figure is big enough
        if (w >= 3) {
            batch.draw(white, x0 - 1, fy + legs + knickers + 1, 1, shirt - 1);
            batch.draw(white, x0 + w, fy + legs + knickers + 1, 1, shirt - 1);
        }
        float headW = Math.max(1, w - 2);
        float hx = fx - headW / 2;
        ViewColors.set(batch, ArtMath.SKIN, 1f);
        batch.draw(white, hx, fy + legs + knickers + shirt, headW, head);
        boolean keeper = kit == 0 && role != SpawnRole.OFFICIAL;
        if (keeper || role == SpawnRole.OFFICIAL) {
            ViewColors.set(batch, keeper ? ArtMath.mul(colours[0], 0.6) : ArtMath.INK, 1f);
            batch.draw(white, hx - 1, fy + h - 1, headW + 2, 1);
        }
    }

    private void ball(Batch batch, ViewArt art, ViewTransform t) {
        ViewCamera cam = view.viewCamera();
        ActorSprites.Pose p = pose(t, ids.get(t.entity), 0, cam.scale(t.y));
        ActorSprites.Frame sprite = art.sprites().ball(p);
        if (sprite != null) {
            drawFrame(batch, sprite, cam.screenX(t.x, t.y), cam.screenY(t.y, Math.max(0, t.z)), p.magnification);
            return;
        }
        double d = BALL_DRAWN_M * cam.scale(t.y);
        TextureRegion ball = art.ball(d);
        int size = ball.getRegionWidth();
        float x = (float) Math.round(cam.screenX(t.x, t.y) - size / 2.0);
        float y = (float) Math.round(cam.screenY(t.y, Math.max(0, t.z)));
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(ball, x, y);
    }

    private void post(Batch batch, TextureRegion white, int k) {
        PitchGeometry p = view.pitch();
        ViewCamera cam = view.viewCamera();
        double gx = postX(p, k);
        double gy = postY(p, k);
        double barTop = p.crossbarUndersideZ() + 2 * p.params().crossbarRadiusM();
        float w = (float) Math.max(1, Math.round(2 * p.params().postRadiusM() * cam.scale(gy) * 1.5));
        float x = (float) Math.round(cam.screenX(gx, gy) - w / 2);
        float bottom = (float) Math.round(cam.screenY(gy, 0));
        float top = (float) Math.round(cam.screenY(gy, barTop));
        ViewColors.set(batch, FRAME_PAINT, 1f);
        batch.draw(white, x, bottom, w, top - bottom);
        if (k % 2 == 0) {
            // the near post carries the crossbar back to the far post's top
            double fy = -gy;
            float fx = (float) Math.round(cam.screenX(gx, fy) - w / 2);
            float ftop = (float) Math.round(cam.screenY(fy, barTop));
            line(batch, white, fx, ftop - 1, x, top - 1, w);
        }
    }

    /** A pixel line (Bresenham) of square dots {@code size} wide. */
    private static void line(Batch batch, TextureRegion white, float x0f, float y0f, float x1f, float y1f,
            float size) {
        int x0 = Math.round(x0f);
        int y0 = Math.round(y0f);
        int x1 = Math.round(x1f);
        int y1 = Math.round(y1f);
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        for (int guard = 0; guard < 4096; guard++) {
            batch.draw(white, x0, y0, size, size);
            if (x0 == x1 && y0 == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private void flag(Batch batch, TextureRegion white, int k, double seconds) {
        PitchGeometry p = view.pitch();
        ViewCamera cam = view.viewCamera();
        double fxw = postX(p, k);
        double fyw = flagY(p, k);
        double scale = cam.scale(fyw);
        float x = (float) Math.round(cam.screenX(fxw, fyw));
        float bottom = (float) Math.round(cam.screenY(fyw, 0));
        float top = (float) Math.round(cam.screenY(fyw, FLAG_POLE_M));
        ViewColors.set(batch, ArtMath.WOOD_DK, 1f);
        batch.draw(white, x, bottom, 1, top - bottom);
        int size = (int) Math.max(2, Math.round(0.35 * scale));
        boolean flap = ((int) Math.floor(seconds * 4 + k)) % 2 == 0;
        ViewColors.set(batch, ArtMath.RED, 1f);
        batch.draw(white, x + 1, top - size, size + (flap ? 1 : 0), size);
    }

    // ---- inspection -------------------------------------------------------------------------------------------------

    /** Entries sorted in the last frame. */
    public int size() {
        return sort.size();
    }

    /** Kind of the entry drawn in place {@code i} ({@link #ACTOR}, {@link #BALL}, {@link #POST}, {@link #FLAG}). */
    public int kindAt(int i) {
        return kinds[sort.order(i)];
    }

    /** The view transform drawn in place {@code i}, or null for a post or flag. */
    public ViewTransform transformAt(int i) {
        int e = sort.order(i);
        return kinds[e] == ACTOR || kinds[e] == BALL ? interpolation.get(refs[e]) : null;
    }
}
