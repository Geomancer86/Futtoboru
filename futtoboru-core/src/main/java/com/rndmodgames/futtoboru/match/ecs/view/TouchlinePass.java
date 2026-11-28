package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;

/**
 * Touchline strip T1 (D26) along the far touchline. This pass draws the strip in front of the bank (after the far
 * crowd, before the players): the rope, the press table with its chairs and the trainer's bucket, and the touchline
 * people: two committee men, the trainer, the pressman, a constable pacing the rope and the telegraph boy who runs
 * the copy from the press table to the telegraph pole and back. {@link Buildings} draws the pavilion, shed, tent,
 * carriage and telegraph pole at the back of the bank, behind the crowd. No advertising boards and no bench (D26).
 *
 * <ul>
 * <li>All motion is a function of the view clock (deterministic, stops while paused); standing people face the play
 * (the camera's follow point).</li>
 * <li>People come from the people sheet (1x/2x), props from the touchline sheet (1x, drawn at whole-screen-pixel
 * multiples, {@link SpriteScale}); without sheets the people are small blocks and the props are skipped.</li>
 * <li>Module {@code touchline} off: nothing.</li>
 * </ul>
 */
public class TouchlinePass extends EntitySystem implements DrawPass {

    public static final String MODULE = "touchline";
    /** Ground line of the rope, metres beyond the far touchline. */
    static final double ROPE_M = 1.9;
    /** Ground line of the people standing at the rope. */
    static final double PEOPLE_M = 1.1;
    /** The constable and the telegraph boy walk on the bank side of the rope. */
    static final double BANK_PATH_M = 2.35;
    static final double CONSTABLE_MPS = 1.1;
    static final double BOY_MPS = 5.0;
    /** Telegraph boy: seconds idle at the press table, then at the pole. */
    static final double BOY_WAIT_TABLE_S = 18;
    static final double BOY_WAIT_POLE_S = 5;

    static final String[] PEOPLE = { "committee", "committee-b", "trainer", "press", "constable", "telegraph-boy" };
    static final int COMMITTEE = 0;
    static final int COMMITTEE_B = 1;
    static final int TRAINER = 2;
    static final int PRESS = 3;
    static final int CONSTABLE = 4;
    static final int BOY = 5;

    static final String[] PROPS = { "telegraph-pole", "pavilion", "shed", "tent", "carriage", "chair", "press-table",
            "bucket" };
    static final int POLE = 0;
    static final int PAVILION = 1;
    static final int SHED = 2;
    static final int TENT = 3;
    static final int CARRIAGE = 4;
    static final int CHAIR = 5;
    static final int PRESS_TABLE = 6;
    static final int BUCKET = 7;

    private final ViewFrame view;
    private final boolean buildings;
    private ActorSprites builtSprites;
    /** People: [person][0 idle, 1 moving][scale 1..2][dir][frame]. */
    private ActorSprites.Sprite[][][][][] people;
    /** Props: [prop][frame]. */
    private ActorSprites.Sprite[][] props;
    private ActorSprites.Sprite rope;
    private int pieces;

    /** The strip in front of the bank. */
    public TouchlinePass(ViewFrame view) {
        this(view, false, SystemPriority.DRAW_TOUCHLINE);
    }

    protected TouchlinePass(ViewFrame view, boolean buildings, int priority) {
        super(priority);
        this.view = view;
        this.buildings = buildings;
    }

    /** Buildings at the back of the far bank (behind the crowd). */
    public static final class Buildings extends TouchlinePass {
        public Buildings(ViewFrame view) {
            super(view, true, SystemPriority.DRAW_TOUCHLINE_BACK);
        }
    }

    @Override
    public void draw(FrameContext frame) {
        pieces = 0;
        if (!view.quality().on(MODULE)) {
            return;
        }
        PitchGeometry pitch = view.pitch();
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        double t = frame.viewSeconds();
        if (!view.draws()) {
            pieces = buildings ? 5 : PEOPLE.length + 5;
            return;
        }
        ViewArt art = view.art();
        ActorSprites sprites = art.sprites();
        table(sprites);
        Batch batch = view.batch();
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        if (buildings) {
            // back to front
            prop(batch, POLE, 0.12 * hl, hw + 9.8, t);
            prop(batch, PAVILION, -0.42 * hl, hw + 9.6, t);
            prop(batch, SHED, 0.38 * hl, hw + 9.4, t);
            prop(batch, TENT, 0.62 * hl, hw + 9.1, t);
            prop(batch, CARRIAGE, -0.8 * hl, hw + 8.9, t);
        } else {
            double tableX = 0.22 * hl;
            double poleX = 0.12 * hl;
            // bank side of the rope first (further away), then the rope, then the people at it
            double boyX = boyX(t, tableX + 1.5, poleX);
            boolean boyRuns = boyRunning(t, tableX + 1.5, poleX);
            person(batch, sprites, art.white(), BOY, boyX, hw + BANK_PATH_M + 0.2, boyRuns,
                    boyRuns ? (boyDirectionWest(t, tableX + 1.5, poleX) ? 6 : 2) : -1, t);
            double cx = constableX(t, -0.3 * hl, 0.3 * hl);
            boolean west = constableWest(t, -0.3 * hl, 0.3 * hl);
            person(batch, sprites, art.white(), CONSTABLE, cx, hw + BANK_PATH_M, true, west ? 6 : 2, t);
            rope(batch, -hl - 8, hl + 8, hw + ROPE_M);
            person(batch, sprites, art.white(), PRESS, tableX, hw + PEOPLE_M + 0.5, false, -1, t);
            prop(batch, CHAIR, tableX - 1.6, hw + PEOPLE_M + 0.4, t);
            prop(batch, CHAIR, tableX + 1.6, hw + PEOPLE_M + 0.4, t);
            prop(batch, PRESS_TABLE, tableX, hw + PEOPLE_M + 0.2, t);
            person(batch, sprites, art.white(), COMMITTEE, -0.42 * hl + 2, hw + PEOPLE_M + 0.1, false, -1, t);
            person(batch, sprites, art.white(), COMMITTEE_B, -0.42 * hl + 3.3, hw + PEOPLE_M, false, -1, t);
            person(batch, sprites, art.white(), TRAINER, -7, hw + PEOPLE_M, false, -1, t);
            prop(batch, BUCKET, -6.2, hw + PEOPLE_M - 0.1, t);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    // ---- motion (pure functions of the view clock) ----------------------------------------------------------------

    /** The constable paces between {@code a} and {@code b} (ping-pong). */
    static double constableX(double t, double a, double b) {
        double span = b - a;
        double s = (t * CONSTABLE_MPS) % (2 * span);
        return s < span ? a + s : b - (s - span);
    }

    static boolean constableWest(double t, double a, double b) {
        double span = b - a;
        return (t * CONSTABLE_MPS) % (2 * span) >= span;
    }

    private static double boyCycle(double table, double pole) {
        return BOY_WAIT_TABLE_S + BOY_WAIT_POLE_S + 2 * Math.abs(table - pole) / BOY_MPS;
    }

    /** The telegraph boy: waits at the table, runs to the pole, waits, runs back. */
    static double boyX(double t, double table, double pole) {
        double run = Math.abs(table - pole) / BOY_MPS;
        double s = t % boyCycle(table, pole);
        if (s < BOY_WAIT_TABLE_S) {
            return table;
        }
        s -= BOY_WAIT_TABLE_S;
        if (s < run) {
            return table + (pole - table) * s / run;
        }
        s -= run;
        if (s < BOY_WAIT_POLE_S) {
            return pole;
        }
        s -= BOY_WAIT_POLE_S;
        return pole + (table - pole) * Math.min(1, s / run);
    }

    static boolean boyRunning(double t, double table, double pole) {
        double run = Math.abs(table - pole) / BOY_MPS;
        double s = t % boyCycle(table, pole);
        return (s >= BOY_WAIT_TABLE_S && s < BOY_WAIT_TABLE_S + run) || s >= BOY_WAIT_TABLE_S + run + BOY_WAIT_POLE_S;
    }

    private static boolean boyDirectionWest(double t, double table, double pole) {
        double run = Math.abs(table - pole) / BOY_MPS;
        double s = t % boyCycle(table, pole);
        boolean outward = s < BOY_WAIT_TABLE_S + run;
        return outward == (pole < table);
    }

    // ---- drawing ---------------------------------------------------------------------------------------------------

    private void prop(Batch batch, int prop, double x, double y, double t) {
        ActorSprites.Sprite[] frames = props == null ? null : props[prop];
        if (frames == null || frames.length == 0) {
            return;
        }
        ActorSprites.Sprite s = frames[Math.floorMod((int) Math.floor(t * 2), frames.length)];
        ViewCamera cam = view.viewCamera();
        int mag = cam.magnification();
        int choice = SpriteScale.choose(cam.scale(y), mag, 1);
        SpriteScale.draw(batch, s, cam.screenX(x, y), cam.screenY(y, 0), SpriteScale.texel(choice), mag);
        pieces++;
    }

    /** Rope tiles from x0 to x1 on ground line y (the tile's pivot is its left post). */
    private void rope(Batch batch, double x0, double x1, double y) {
        ActorSprites.Sprite s = rope;
        if (s == null) {
            return;
        }
        ViewCamera cam = view.viewCamera();
        int mag = cam.magnification();
        int texel = SpriteScale.texel(SpriteScale.choose(cam.scale(y), mag, 1));
        double step = s.width * texel / (double) mag;
        double sy = cam.screenY(y, 0);
        double from = Math.max(cam.screenX(x0, y), -step);
        double to = Math.min(cam.screenX(x1, y), cam.orthoWidth() + step);
        // tiles stay on a fixed lattice in world x so the rope does not crawl as the camera slides
        double origin = cam.screenX(x0, y);
        double first = origin + Math.floor((from - origin) / step) * step;
        for (double sx = first; sx < to; sx += step) {
            SpriteScale.draw(batch, s, sx, sy, texel, mag);
            pieces++;
        }
    }

    /**
     * One touchline person at (x, y); {@code moving} plays the walk (run for the boy), {@code dir} -1 faces the play.
     */
    private void person(Batch batch, ActorSprites sprites, TextureRegion white, int who, double x, double y,
            boolean moving, int dir, double t) {
        ViewCamera cam = view.viewCamera();
        int mag = cam.magnification();
        int choice = SpriteScale.choose(cam.scale(y), mag, 2);
        double sx = cam.screenX(x, y);
        double sy = cam.screenY(y, 0);
        int d = dir >= 0 ? dir : IndexActorSprites.direction(Math.atan2(-y, cam.x() - x));
        ActorSprites.Sprite[] frames = people == null ? null : people[who][moving ? 1 : 0][SpriteScale.sheet(choice)][d];
        if (frames == null || frames.length == 0) {
            float k = SpriteScale.texel(choice) * SpriteScale.sheet(choice) / (float) mag;
            ViewColors.set(batch, who == CONSTABLE ? ArtMath.NAVY : ArtMath.SOOT, 1f);
            batch.draw(white, SpriteScale.snap(sx, mag) - 2 * k, SpriteScale.snap(sy, mag), 4 * k, 8 * k);
            batch.setColor(1f, 1f, 1f, 1f);
            pieces++;
            return;
        }
        double fps = moving ? (who == BOY ? 12 : 8) : 4;
        int f = Math.floorMod((int) Math.floor(t * fps + who * 0.7), frames.length);
        SpriteScale.draw(batch, frames[f], sx, sy, SpriteScale.texel(choice), mag);
        pieces++;
    }

    private void table(ActorSprites sprites) {
        if (sprites == builtSprites) {
            return;
        }
        builtSprites = sprites;
        props = new ActorSprites.Sprite[PROPS.length][];
        for (int i = 0; i < PROPS.length; i++) {
            props[i] = CrowdPass.frames(sprites, "touchline", "prop/" + PROPS[i] + "/", 1);
        }
        rope = sprites.sprite("touchline", "prop/rope/0", 1);
        people = new ActorSprites.Sprite[PEOPLE.length][2][3][ActorSprites.DIRECTIONS][];
        boolean any = false;
        for (int p = 0; p < PEOPLE.length; p++) {
            for (int a = 0; a < 2; a++) {
                String anim = a == 0 ? "idle" : (p == BOY ? "run" : "walk");
                for (int s = 1; s <= 2; s++) {
                    for (int d = 0; d < ActorSprites.DIRECTIONS; d++) {
                        String prefix = PEOPLE[p] + "/" + anim + "/" + DIRS[d] + "/";
                        people[p][a][s][d] = CrowdPass.frames(sprites, "people", prefix, s);
                        any |= people[p][a][s][d].length > 0;
                    }
                }
            }
        }
        if (!any) {
            people = null;
        }
    }

    /** Index direction names (README order). */
    static final String[] DIRS = { "S", "SE", "E", "NE", "N", "NW", "W", "SW" };

    /** Pieces drawn (or, headless, that would be) in the last frame. */
    public int pieces() {
        return pieces;
    }
}
