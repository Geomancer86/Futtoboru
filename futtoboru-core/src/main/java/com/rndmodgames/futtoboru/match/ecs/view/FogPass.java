package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * Fog banks and the overcast tint (E-21g, D26 W1/W2; the pattern of DarkBlade's {@code FogSystem} and
 * {@code SceneTint}). Banks of soft ellipses drift along the horizon and thin out near the ball (the camera follows
 * it, so the middle of the screen) to keep the play readable; then one flat wash tints the whole frame: cool grey-blue
 * for rain, pale for snow. The tint is all that is drawn at {@code low} quality (W2). Fog is module {@code fog}, tint
 * module {@code weather}. Thickness and tint come from {@link ViewWeather}, never from a seed.
 */
public final class FogPass extends EntitySystem implements DrawPass {

    public static final String MODULE = "fog";

    private static final int BANKS = 7;
    private static final int FOG_RGB = 0xC7D1DB;

    private final ViewFrame view;
    private int banksDrawn;
    private float tintAlpha;
    private double fog;

    public FogPass(ViewFrame view) {
        super(SystemPriority.DRAW_ATMOSPHERE);
        this.view = view;
    }

    @Override
    public void draw(FrameContext frame) {
        GraphicsQuality q = view.quality();
        ViewWeather.Kind kind = ViewWeather.of(view.wetness(), view.surface());
        tintAlpha = q.on(WeatherPass.MODULE) ? (float) kind.tint : 0;
        fog = q.on(MODULE) && !GraphicsQuality.LOW.equals(q.quality())
                ? ViewWeather.fog(view.wetness(), view.surface()) : 0;
        banksDrawn = 0;
        if (!view.draws() || (tintAlpha <= 0 && fog <= 0)) {
            return;
        }
        ViewCamera cam = view.viewCamera();
        ViewArt art = view.art();
        TextureRegion ellipse = art.ellipse();
        TextureRegion white = art.white();
        float w = (float) cam.orthoWidth();
        float h = (float) cam.orthoHeight();
        float horizon = (float) cam.horizonY();
        double t = view.viewSeconds();
        Batch batch = view.batch();
        batch.begin();
        if (fog > 0) {
            for (int i = 0; i < BANKS; i++) {
                float bw = w * (0.45f + 0.1f * (i % 3));
                float bh = h * (0.16f + 0.04f * (i % 2));
                double speed = 0.012 + 0.004 * (i % 3);
                float px = (float) (((i / (double) BANKS + t * speed) % 1.0) * (w + bw) - bw);
                float py = horizon - bh * 0.5f + h * 0.05f * (i % 4) - h * 0.08f;
                float away = Math.abs(px + bw / 2 - w / 2) / (w * 0.35f);
                float clear = Math.min(1f, away * away + 0.25f);
                ViewColors.set(batch, FOG_RGB, (float) fog * 0.55f * clear);
                batch.draw(ellipse, px, py, bw, bh);
                banksDrawn++;
            }
        }
        if (tintAlpha > 0) {
            ViewColors.set(batch, kind == ViewWeather.Kind.SNOW ? ViewWeather.SNOW_TINT_RGB : ViewWeather.TINT_RGB,
                    tintAlpha);
            batch.draw(white, 0, 0, w, h);
        }
        ViewColors.white(batch);
        batch.end();
    }

    /** Fog banks drawn in the last frame (0 headless). */
    public int banksDrawn() {
        return banksDrawn;
    }

    /** Overcast alpha of the last frame. */
    public float tintAlpha() {
        return tintAlpha;
    }

    /** Fog thickness of the last frame. */
    public double fog() {
        return fog;
    }
}
