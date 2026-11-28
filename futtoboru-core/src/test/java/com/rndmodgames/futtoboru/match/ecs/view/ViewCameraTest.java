package com.rndmodgames.futtoboru.match.ecs.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;

/** The side-on camera V3: projection, its inverse, framing, integer magnification and the ball follow. */
class ViewCameraTest {

    static ViewCamera camera() {
        ViewCamera cam = new ViewCamera(new PitchGeometry(PitchParams.defaults1888()));
        cam.resize(1280, 720);
        return cam;
    }

    @Test
    void halfwayDepthDrawsAtTenPixelsPerMetreAndFiveInTheWideView() {
        ViewCamera cam = camera();
        assertEquals(10.0, cam.scale(0), 1e-12, "D28 default");
        double a = cam.screenX(10, 0);
        double b = cam.screenX(11, 0);
        assertEquals(10.0, b - a, 1e-9, "one metre along the halfway depth");
        assertTrue(cam.scale(-cam.halfWidth()) > 18, "near touchline larger");
        assertTrue(cam.scale(cam.halfWidth()) < 8, "far touchline smaller");
        cam.setWide(true);
        assertTrue(cam.wide());
        assertEquals(5.0, cam.scale(0), 1e-12, "wide view: S1");
        assertEquals(ViewCamera.NEAR_TOUCHLINE_SHARE * cam.viewHeight(), cam.screenY(-cam.halfWidth(), 0), 1e-9,
                "the near touchline stays put");
        cam.setWide(false);
        assertEquals(10.0, cam.scale(0), 1e-12);
    }

    @Test
    void groundRowsAndColumnsInvertTheProjection() {
        ViewCamera cam = camera();
        for (double y = -cam.halfWidth(); y <= cam.halfWidth(); y += 3.7) {
            double row = cam.screenY(y, 0);
            assertEquals(y, cam.groundYAtRow(row), 1e-9);
            for (double x = -40; x <= 40; x += 13) {
                assertEquals(x, cam.worldX(cam.screenX(x, y), y), 1e-9);
            }
        }
        assertEquals(Double.POSITIVE_INFINITY, cam.groundYAtRow(cam.horizonY() + 1));
    }

    @Test
    void farIsHigherOnScreenAndHeightLifts() {
        ViewCamera cam = camera();
        double near = cam.screenY(-cam.halfWidth(), 0);
        double far = cam.screenY(cam.halfWidth(), 0);
        assertEquals(ViewCamera.NEAR_TOUCHLINE_SHARE * cam.viewHeight(), near, 1e-9);
        assertTrue(far > near && far < cam.viewHeight(), "far touchline above the near one, inside the view");
        assertEquals(cam.screenY(0, 0) + 2.44 * cam.scale(0), cam.screenY(0, 2.44), 1e-9, "z lifts by scale");
    }

    @Test
    void magnificationIsWholeAndShowsAbout64To96MetresAtHalfway() {
        assertEquals(2, ViewCamera.magnificationFor(1280, 720));
        assertEquals(2, ViewCamera.magnificationFor(1920, 1080));
        assertEquals(3, ViewCamera.magnificationFor(2560, 1440));
        assertEquals(1, ViewCamera.magnificationFor(100, 100));
        assertEquals(1, ViewCamera.magnificationFor(2560, 400), "a short window keeps the pitch in view");
        for (int[] window : new int[][] { { 1280, 720 }, { 1920, 1080 }, { 2560, 1440 }, { 1600, 900 } }) {
            ViewCamera cam = new ViewCamera(new PitchGeometry(PitchParams.defaults1888()));
            cam.resize(window[0], window[1]);
            double metres = cam.orthoWidth() / cam.scale(0);
            assertTrue(metres >= 64 - 1e-9 && metres <= 96 + 1e-9, window[0] + "x" + window[1] + ": " + metres + " m");
            assertTrue(cam.screenY(cam.halfWidth(), 0) < cam.orthoHeight(), "far touchline inside the view");
        }
        ViewCamera cam = camera();
        assertEquals(2, cam.magnification());
        assertEquals(1280 / 2.0, cam.orthoWidth(), 1e-9, "each art pixel exactly 2 screen pixels");
        assertEquals(640, cam.viewWidth());
        assertEquals(1f / 2, cam.zoom(), 1e-6);
        cam.setMagnification(3);
        cam.resize(1280, 720);
        assertEquals(427, cam.viewWidth());
        cam.autoMagnification();
        cam.resize(2560, 1440);
        assertEquals(3, cam.magnification());
    }

    @Test
    void followIsSmoothFrameRateIndependentAndClamped() {
        ViewCamera at60 = camera();
        ViewCamera at144 = camera();
        for (int i = 0; i < 60; i++) {
            at60.follow(20, 1.0 / 60);
        }
        for (int i = 0; i < 144; i++) {
            at144.follow(20, 1.0 / 144);
        }
        assertEquals(at60.x(), at144.x(), 1e-9, "same path after one second");
        assertTrue(at60.x() > 10 && at60.x() < 20, "eases toward the ball: " + at60.x());
        ViewCamera cam = camera();
        cam.snapTo(500);
        assertEquals(cam.maxX(), cam.x(), 1e-12, "never past the end");
        assertTrue(cam.maxX() < cam.halfLength(), "near edge stops a few metres past the goal line");
        double nearEdge = cam.worldX(cam.orthoWidth(), -cam.halfWidth());
        assertEquals(cam.halfLength() + ViewCamera.END_MARGIN_M, nearEdge, 0.5);
    }

    @Test
    void rejectsABadRig() {
        assertThrows(IllegalArgumentException.class, () -> new ViewCamera(50, 30, 20, 10));
    }
}
