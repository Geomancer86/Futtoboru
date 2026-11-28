package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Arrays;
import java.util.Random;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * Falling rain, heavy rain and snow (E-21g, D26 W1; the shape of DarkBlade's {@code PrecipitationSystem}: a capped
 * pool spawned above the view and fed by wind lean). What falls comes only from the match's pitch conditions
 * ({@link ViewWeather}); the cosmetic random only scatters the drops. Rain lands at a random depth on the ground and
 * splashes for a moment. Module {@code weather}: {@code cap} and {@code density} knobs, {@code ticks} sets the update
 * rate; at {@code low} quality no particles are drawn (W2: the tint alone, {@link FogPass}).
 */
public final class WeatherPass extends EntitySystem implements DrawPass {

    public static final String MODULE = "weather";

    /** Screens per second a drop falls. */
    private static final double RAIN_FALL = 1.7;
    private static final double SNOW_FALL = 0.14;
    private static final float SPLASH_SECONDS = 0.12f;

    private final ViewFrame view;
    private float[] x = new float[64];
    private float[] y = new float[64];
    private float[] fall = new float[64];
    private float[] land = new float[64];
    private float[] splash = new float[64];
    private int count;
    private double lean;
    private ViewWeather.Kind kind = ViewWeather.Kind.CLEAR;
    private double accumulated;
    private double lastView = -1;

    public WeatherPass(ViewFrame view) {
        super(SystemPriority.DRAW_PARTICLES);
        this.view = view;
    }

    @Override
    public void draw(FrameContext frame) {
        update();
        if (!view.draws() || count == 0) {
            return;
        }
        TextureRegion white = view.art().white();
        Batch batch = view.batch();
        batch.begin();
        boolean snow = kind == ViewWeather.Kind.SNOW;
        float tilt = (float) Math.toDegrees(Math.atan(lean));
        float len = 3 + 5 * (float) kind.intensity;
        for (int i = 0; i < count; i++) {
            if (splash[i] > 0) {
                batch.setColor(0.75f, 0.82f, 0.9f, 0.5f * splash[i] / SPLASH_SECONDS);
                batch.draw(white, x[i] - 1, land[i], 3, 1);
            } else if (snow) {
                batch.setColor(0.95f, 0.97f, 1f, 0.85f);
                batch.draw(white, x[i], y[i], fall[i] > 0.14f ? 2 : 1, fall[i] > 0.14f ? 2 : 1);
            } else {
                batch.setColor(0.72f, 0.8f, 0.9f, 0.55f);
                batch.draw(white, x[i], y[i], 0, 0, 1, len, 1, 1, tilt);
            }
        }
        ViewColors.white(batch);
        batch.end();
    }

    /** Spawns, moves and retires this frame's drops; also headless (tests). */
    void update() {
        GraphicsQuality q = view.quality();
        kind = ViewWeather.of(view.wetness(), view.surface());
        lean = ViewWeather.lean(view.wetness(), view.viewSeconds());
        if (!q.on(MODULE) || GraphicsQuality.LOW.equals(q.quality()) || kind == ViewWeather.Kind.CLEAR) {
            count = 0;
            return;
        }
        double now = view.viewSeconds();
        double dt = lastView < 0 ? 0 : Math.min(0.1, now - lastView);
        lastView = now;
        accumulated += dt;
        if (accumulated < q.ticks(MODULE) / 60.0 && lastView >= 0 && count > 0) {
            return;
        }
        double dts = accumulated;
        accumulated = 0;
        int cap = Math.round(q.knob(MODULE, "cap") * q.knob(MODULE, "density") * (float) kind.intensity);
        double width = view.viewCamera().orthoWidth();
        double height = view.viewCamera().orthoHeight();
        boolean snow = kind == ViewWeather.Kind.SNOW;
        double wind = snow ? lean * 0.5 : lean;
        int n = 0;
        for (int i = 0; i < count; i++) {
            if (splash[i] > 0) {
                splash[i] -= (float) dts;
                if (splash[i] <= 0) {
                    continue;
                }
            } else {
                double speed = fall[i] * height;
                y[i] -= (float) (speed * dts);
                x[i] += (float) (speed * wind * dts) + (snow ? (float) (Math.sin(now * 2 + i) * 4 * dts) : 0);
                if (y[i] <= land[i]) {
                    if (snow) {
                        continue;
                    }
                    y[i] = land[i];
                    splash[i] = SPLASH_SECONDS;
                }
            }
            move(n++, i);
        }
        count = n;
        Random random = view.cosmetic();
        int spawn = Math.min(cap - count, Math.max(8, cap / 4));
        for (int i = 0; i < spawn; i++) {
            if (count == x.length) {
                grow();
            }
            int k = count++;
            x[k] = (float) ((random.nextDouble() * 1.4 - 0.2) * width);
            y[k] = (float) (height * (1 + random.nextDouble() * 0.1));
            land[k] = (float) (height * (0.03 + 0.55 * random.nextDouble()));
            fall[k] = (float) ((snow ? SNOW_FALL : RAIN_FALL) * (0.8 + 0.4 * random.nextDouble()));
            splash[k] = 0;
        }
    }

    private void move(int to, int from) {
        if (to != from) {
            x[to] = x[from];
            y[to] = y[from];
            fall[to] = fall[from];
            land[to] = land[from];
            splash[to] = splash[from];
        }
    }

    private void grow() {
        int n = x.length * 2;
        x = Arrays.copyOf(x, n);
        y = Arrays.copyOf(y, n);
        fall = Arrays.copyOf(fall, n);
        land = Arrays.copyOf(land, n);
        splash = Arrays.copyOf(splash, n);
    }

    /** Drops alive this frame (0 when clear or the module is off). */
    public int particles() {
        return count;
    }

    public ViewWeather.Kind kind() {
        return kind;
    }
}
