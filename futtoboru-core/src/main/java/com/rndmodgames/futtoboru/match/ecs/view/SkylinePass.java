package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Random;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * Skyline K1 (D26) above the far edge of the ground: the mill-town strip ({@link SkylineArt}) scrolled with parallax as
 * the camera slides, plain sky above it, and smoke drifting from the chimneys.
 *
 * <ul>
 * <li>Module {@code skyline} off: a plain sky colour only. Module {@code smoke} off: no smoke; its knobs cap the
 * smoking chimneys ({@code chimneys}) and thin the puffs ({@code density}).</li>
 * <li>Smoke puffs are a function of the view clock and per-chimney phases; the slow wind drift that bends them is
 * re-rolled now and then from the viewer's cosmetic random source ({@link ViewFrame#cosmetic()}), never the match
 * fork.</li>
 * </ul>
 */
public final class SkylinePass extends EntitySystem implements DrawPass {

    public static final String MODULE = "skyline";
    public static final String SMOKE = "smoke";
    /** Skyline scroll per metre of camera travel (the far touchline moves about 3 px/m: the town is further). */
    public static final double PARALLAX = 0.8;
    /** Puffs per chimney at full density. */
    public static final int PUFFS = 7;
    /** Seconds between wind changes, on average. */
    public static final double GUST_SECONDS = 5;

    private final ViewFrame view;
    private double[] phase;
    private double[] speed;
    private double drift = 1;
    private double driftTarget = 1;
    private double nextGust;
    private int puffsDrawn;

    public SkylinePass(ViewFrame view) {
        super(SystemPriority.DRAW_SKYLINE);
        this.view = view;
    }

    @Override
    public void draw(FrameContext frame) {
        puffsDrawn = 0;
        GraphicsQuality quality = view.quality();
        boolean skyline = quality.on(MODULE);
        boolean smoke = skyline && quality.on(SMOKE);
        if (smoke) {
            wind(frame.viewSeconds(), frame.frameSeconds());
        }
        if (!view.draws()) {
            return;
        }
        ViewArt art = view.art();
        ViewCamera cam = view.viewCamera();
        float width = (float) cam.orthoWidth();
        float top = (float) cam.orthoHeight();
        float base = (float) Math.floor(cam.screenY(art.pitchArt().maxY, 0));
        Batch batch = view.batch();
        batch.begin();
        TextureRegion white = art.white();
        ViewColors.set(batch, SkylineArt.SKY_TOP, 1f);
        if (!skyline) {
            batch.draw(white, 0, base, width, Math.max(0, top - base));
            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();
            return;
        }
        SkylineArt sky = art.skylineArt();
        float stripTop = base + sky.height;
        if (stripTop < top) {
            batch.draw(white, 0, stripTop, width, top - stripTop);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        int offset = Math.floorMod((int) Math.round(cam.x() * PARALLAX), sky.width);
        strip(batch, art.skyline(), sky, offset, base, width);
        if (smoke) {
            smoke(batch, white, sky, offset, base, width, frame.viewSeconds());
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    /** The strip from column {@code offset}, wrapping once. */
    private static void strip(Batch batch, TextureRegion region, SkylineArt sky, int offset, float base, float width) {
        Texture texture = region.getTexture();
        float iw = 1f / texture.getWidth();
        float ih = 1f / texture.getHeight();
        float vBottom = (region.getRegionY() + sky.height) * ih;
        float vTop = region.getRegionY() * ih;
        float x = 0;
        int column = offset;
        while (x < width) {
            float w = Math.min(width - x, sky.width - column);
            float u = (region.getRegionX() + column) * iw;
            float u2 = (region.getRegionX() + column + w) * iw;
            batch.draw(texture, x, base, w, sky.height, u, vBottom, u2, vTop);
            x += w;
            column = 0;
        }
    }

    /** Wind drift: eases toward a target re-rolled from the cosmetic random every few seconds. */
    void wind(double viewSeconds, double frameSeconds) {
        Random random = view.cosmetic();
        if (phase == null) {
            phase = new double[64];
            speed = new double[64];
            for (int i = 0; i < phase.length; i++) {
                phase[i] = random.nextDouble();
                speed[i] = 0.8 + random.nextDouble() * 0.5;
            }
            nextGust = viewSeconds + GUST_SECONDS * random.nextDouble();
        }
        if (viewSeconds >= nextGust) {
            driftTarget = 0.4 + random.nextDouble() * 1.4;
            nextGust = viewSeconds + GUST_SECONDS * (0.5 + random.nextDouble());
        }
        if (frameSeconds > 0) {
            drift += (driftTarget - drift) * (1 - Math.exp(-0.6 * frameSeconds));
        }
    }

    private void smoke(Batch batch, TextureRegion white, SkylineArt sky, int offset, float base, float width,
            double seconds) {
        GraphicsQuality quality = view.quality();
        int chimneys = Math.min(sky.chimneyCount, Math.round(quality.knob(SMOKE, "chimneys")));
        int puffs = Math.max(1, Math.round(PUFFS * quality.knob(SMOKE, "density")));
        for (int c = 0; c < chimneys; c++) {
            float sx = Math.floorMod(sky.chimneys[c * 2] - offset, sky.width);
            if (sx > width + 24) {
                sx -= sky.width;
            }
            if (sx < -40 || sx > width + 24) {
                continue;
            }
            float sy = base + (sky.height - 1 - sky.chimneys[c * 2 + 1]);
            for (int k = 0; k < puffs; k++) {
                double ph = frac(seconds * 0.35 * speed[c] + (double) k / puffs + phase[c]);
                float px = (float) Math.round(sx + ph * 14 * drift + Math.sin(ph * 6 + k) * 2);
                float py = (float) Math.round(sy + 1 + ph * 16);
                float r = 1 + Math.round(ph * 3);
                ViewColors.set(batch, ph < 0.5 ? ArtMath.SMOKE_DK : ArtMath.SMOKE, (float) (0.8 - ph * 0.7));
                batch.draw(white, px - r, py - r, r * 2, r * 2);
                puffsDrawn++;
            }
        }
    }

    private static double frac(double v) {
        return v - Math.floor(v);
    }

    /** Smoke puffs drawn in the last frame. */
    public int puffsDrawn() {
        return puffsDrawn;
    }

    /** Current wind drift of the smoke (1 = calm default). */
    public double drift() {
        return drift;
    }
}
