// Adapted from DarkBlade (CameraZoom), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

/**
 * Camera zoom that lands art pixels on whole screen pixels (an {@code OrthographicCamera.zoom} value).
 *
 * <ul>
 * <li>At zoom 1, one art pixel is one screen pixel (the viewport is the screen divided by the art's pixels per metre,
 * which the art prototype checklist fixes).</li>
 * <li>Zooming in uses 1/n (2x, 3x, 4x...). Zooming out uses a whole number (2, 3, 4...).</li>
 * <li>A camera director may glide through fractional zooms; it snaps with {@link #wholePixel(float)} at rest.</li>
 * </ul>
 */
public final class CameraZoom {

    private CameraZoom() {
    }

    /** The nearest zoom at which art pixels land on whole screen pixels. */
    public static float wholePixel(float zoom) {
        if (zoom <= 1f) {
            int magnification = Math.max(1, Math.round(1f / Math.max(0.0001f, zoom)));
            return 1f / magnification;
        }
        return Math.max(1, Math.round(zoom));
    }

    /** Zoom for a whole magnification: 2 means each art pixel is 2 screen pixels. */
    public static float forMagnification(int pixelsPerArtPixel) {
        return 1f / Math.max(1, pixelsPerArtPixel);
    }
}
