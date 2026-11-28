package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;

/**
 * The ground under the side-on camera (D26 P1 + A1 + SL1). Each screen row below the far edge of the pitch image is one
 * ground depth, so the generated top-down image ({@link PitchArt}) is drawn one textured row at a time, sampled at
 * that depth: an exact perspective with one texture and a few hundred quads in one draw call. Bare earth fills
 * whatever the image does not cover.
 *
 * <p>The boundary lines (and the halfway line when the era has one) are drawn again on top as crisp spans at least
 * one art pixel wide: a line far away is thinner than a screen row and would flicker in and out of the sampled image.
 * Goal frames and flags stand up, so the actor pass draws them, depth-sorted with the players.
 */
public final class PitchPass extends EntitySystem implements DrawPass {

    private static final int EARTH = 0x5D5646;

    private final ViewFrame view;
    private static final int MUD = 0x4A3B26;

    private double mud;
    private int rowsDrawn;
    private int lineQuads;

    public PitchPass(ViewFrame view) {
        super(SystemPriority.DRAW_PITCH);
        this.view = view;
    }

    @Override
    public void draw(FrameContext frame) {
        rowsDrawn = 0;
        lineQuads = 0;
        if (!view.draws()) {
            return;
        }
        ViewArt art = view.art();
        ViewCamera cam = view.viewCamera();
        PitchArt image = art.pitchArt();
        TextureRegion region = art.pitch();
        TextureRegion white = art.white();
        Texture texture = region.getTexture();
        float iw = 1f / texture.getWidth();
        float ih = 1f / texture.getHeight();
        float width = (float) cam.orthoWidth();
        int farRow = (int) Math.ceil(cam.screenY(image.maxY, 0));
        Batch batch = view.batch();
        mud = ViewWeather.mud(view.wetness(), view.tick() * view.tickSeconds());

        batch.begin();
        ViewColors.set(batch, EARTH, 1f);
        batch.draw(white, 0, 0, width, Math.max(0, farRow));
        ViewColors.white(batch);
        for (int sy = 0; sy < farRow && sy < cam.viewHeight(); sy++) {
            double wy = cam.groundYAtRow(sy + 0.5);
            if (wy > image.maxY || wy < image.minY()) {
                continue;
            }
            int row = (int) Math.min(image.height - 1, Math.max(0, Math.floor(image.row(wy))));
            double ca = Math.max(0, image.column(cam.worldX(0, wy)));
            double cb = Math.min(image.width, image.column(cam.worldX(width, wy)));
            if (cb <= ca) {
                continue;
            }
            float sxa = (float) cam.screenX(image.minX + ca / PitchArt.PIXELS_PER_METRE, wy);
            float sxb = (float) cam.screenX(image.minX + cb / PitchArt.PIXELS_PER_METRE, wy);
            float u = (float) ((region.getRegionX() + ca) * iw);
            float u2 = (float) ((region.getRegionX() + cb) * iw);
            float v = (region.getRegionY() + row + 0.5f) * ih;
            batch.draw(texture, sxa, sy, sxb - sxa, 1, u, v, u2, v);
            rowsDrawn++;
            if (mud > 0) {
                mudRow(batch, white, cam, wy, sy, sxa, sxb);
            }
        }
        ViewColors.white(batch);
        drawLines(batch, white, cam, image);
        ViewColors.white(batch);
        batch.end();
    }

    /** Mud over one ground row (E-21g): a few brown spans, thicker in the goalmouths and the centre, as wet rises. */
    private void mudRow(Batch batch, TextureRegion white, ViewCamera cam, double wy, int sy, float sxa, float sxb) {
        double hl = view.pitch().halfLength();
        int parts = 6;
        float w = (sxb - sxa) / parts;
        for (int i = 0; i < parts; i++) {
            double wx = cam.worldX(sxa + (i + 0.5f) * w, wy);
            float a = ViewWeather.mudAlpha(mud, wx, wy, hl, view.groundSeed());
            if (a > 0.01f) {
                ViewColors.set(batch, MUD, a);
                batch.draw(white, sxa + i * w, sy, w + 1, 1);
            }
        }
        ViewColors.white(batch);
    }

    private void drawLines(Batch batch, TextureRegion white, ViewCamera cam, PitchArt image) {
        PitchGeometry pitch = view.pitch();
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        ViewColors.set(batch, image.lineColour(), 1f);
        // touchlines: one horizontal span each
        for (int side = -1; side <= 1; side += 2) {
            double y = side * hw;
            float row = (float) Math.floor(cam.screenY(y, 0));
            float x0 = (float) Math.floor(cam.screenX(-hl, y));
            float x1 = (float) Math.ceil(cam.screenX(hl, y));
            batch.draw(white, x0, row, x1 - x0, thickness(cam, y));
            lineQuads++;
        }
        // goal lines (and the halfway line when the era has one): a short span on every row between the touchlines
        int nearRow = (int) Math.floor(cam.screenY(-hw, 0));
        int farRow = (int) Math.floor(cam.screenY(hw, 0));
        for (int sy = nearRow; sy <= farRow; sy++) {
            double wy = cam.groundYAtRow(sy + 0.5);
            if (wy < -hw || wy > hw) {
                continue;
            }
            float w = (float) Math.max(1, Math.round(0.2 * cam.scale(wy)));
            for (int side = -1; side <= 1; side += 2) {
                span(batch, white, cam, side * hl, wy, sy, w);
            }
            if (image.style().halfwayLine()) {
                span(batch, white, cam, 0, wy, sy, w);
            }
        }
    }

    private void span(Batch batch, TextureRegion white, ViewCamera cam, double wx, double wy, int sy, float w) {
        float sx = (float) Math.floor(cam.screenX(wx, wy) - w / 2f + 0.5f);
        batch.draw(white, sx, sy, w, 1);
        lineQuads++;
    }

    /** A line across the view at depth {@code y}: 0.2 m tall on the ground, at least one row. */
    private static float thickness(ViewCamera cam, double y) {
        double rowsPerMetre = cam.height() * cam.focal() / (cam.depth(y) * cam.depth(y));
        return (float) Math.max(1, Math.round(0.2 * rowsPerMetre));
    }

    /** Ground rows drawn in the last frame (0 headless). */
    public int rowsDrawn() {
        return rowsDrawn;
    }

    /** Line quads drawn in the last frame (0 headless). */
    public int lineQuads() {
        return lineQuads;
    }
}
