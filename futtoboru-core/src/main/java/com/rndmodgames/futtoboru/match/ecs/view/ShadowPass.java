package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.FloatArray;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;

/**
 * Contact shadows (E-21c lite; the pattern of DarkBlade's contact shadow system: one flat ellipse per standing actor,
 * dark violet at 50 %, drawn over the ground and under everything standing). The ball's shadow stays on the ground
 * under it and shrinks and fades as the ball climbs, which is what tells the eye how high it is.
 *
 * <p>Module {@code shadows} off: nothing is drawn. {@link #collect()} fills the frame's quads also headless (tests).
 */
public final class ShadowPass extends EntitySystem implements DrawPass {

    public static final String MODULE = "shadows";

    /** Shadow colour: dark violet (shadows run to blue, never grey). */
    static final float RED = 0.06f;
    static final float GREEN = 0.04f;
    static final float BLUE = 0.14f;
    static final float PERSON_ALPHA = 0.5f;
    static final float BALL_ALPHA = 0.45f;
    /** Shadow width of a standing person, metres. */
    static final double PERSON_WIDTH_M = 0.7;
    /** Shadow width of the ball on the ground, metres (the drawn ball is enlarged, BA2). */
    static final double BALL_WIDTH_M = 0.5;
    /** Height at which the ball's shadow is half its ground width. */
    static final double BALL_HALF_HEIGHT_M = 4;
    /** Height at which the ball's shadow has faded to its faintest. */
    static final double BALL_FADE_HEIGHT_M = 15;

    private final ViewFrame view;
    private final InterpolationPass interpolation;
    private final ComponentMapper<IdentityComponent> ids = ComponentMapper.getFor(IdentityComponent.class);
    /** This frame's shadows: x, y, width, height, alpha (art pixels). */
    private final FloatArray quads = new FloatArray(5 * 32);

    public ShadowPass(ViewFrame view, InterpolationPass interpolation) {
        super(SystemPriority.DRAW_SHADOWS);
        this.view = view;
        this.interpolation = interpolation;
    }

    @Override
    public void draw(FrameContext frame) {
        collect();
        if (!view.draws() || quads.size == 0) {
            return;
        }
        ViewArt art = view.art();
        TextureRegion ellipse = art.ellipse();
        Batch batch = view.batch();
        batch.begin();
        for (int i = 0; i < quads.size; i += 5) {
            batch.setColor(RED, GREEN, BLUE, quads.get(i + 4));
            batch.draw(ellipse, quads.get(i), quads.get(i + 1), quads.get(i + 2), quads.get(i + 3));
        }
        ViewColors.white(batch);
        batch.end();
    }

    /** Fills this frame's shadow quads from the interpolated transforms. */
    public void collect() {
        quads.clear();
        if (!view.quality().on(MODULE)) {
            return;
        }
        ViewCamera cam = view.viewCamera();
        for (int i = 0; i < interpolation.size(); i++) {
            ViewTransform t = interpolation.get(i);
            IdentityComponent id = ids.get(t.entity);
            if (id == null || id.role == SpawnRole.CROWD_SECTION) {
                continue;
            }
            if (id.role == SpawnRole.BALL) {
                double z = Math.max(0, t.z);
                double k = 1 / (1 + z / BALL_HALF_HEIGHT_M);
                double fade = Math.max(0.25, 1 - z / BALL_FADE_HEIGHT_M);
                add(cam, t.x, t.y, BALL_WIDTH_M * k, (float) (BALL_ALPHA * fade));
            } else {
                add(cam, t.x, t.y, PERSON_WIDTH_M, PERSON_ALPHA);
            }
        }
    }

    /**
     * One ground ellipse {@code widthM} across at (x, y): a circle on the ground seen from the camera's height, so it is
     * flattened by height / depth.
     */
    private void add(ViewCamera cam, double x, double y, double widthM, float alpha) {
        double scale = cam.scale(y);
        float w = (float) Math.max(1, Math.round(widthM * scale));
        float h = (float) Math.max(1, Math.round(widthM * scale * cam.height() / cam.depth(y)));
        float sx = (float) Math.round(cam.screenX(x, y) - w / 2);
        float sy = (float) Math.round(cam.screenY(y, 0) - h / 2);
        quads.add(sx);
        quads.add(sy);
        quads.add(w);
        quads.add(h);
        quads.add(alpha);
    }

    /** Shadows collected in the last frame. */
    public int count() {
        return quads.size / 5;
    }

    /** Quad {@code i}: x, y, width, height, alpha. */
    public float quad(int i, int field) {
        return quads.get(i * 5 + field);
    }
}
