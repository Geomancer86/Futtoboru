package com.rndmodgames.futtoboru.match.ecs.view;

import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.ui.gfx.CameraZoom;

/**
 * The side-on "broadcast" camera V3 (D26): world metres to art pixels, pure maths (no libGDX, tested headless).
 *
 * <p>World: origin at the centre spot, {@code x} along the pitch (to the right on screen), {@code y} across it,
 * {@code z} up. The camera stands behind the near touchline ({@code y = -halfWidth}) at {@code y = -distance},
 * {@code height} metres up, and slides along the touchline ({@link #x()}) following the ball. It looks level across
 * the pitch with a shifted lens (the art prototype's projection): for a point at depth {@code d = y + distance}
 *
 * <pre>
 *   screenX = viewWidth / 2 + (x - cameraX) * f / d
 *   screenY = horizonY - (height - z) * f / d          (art pixels, y up, origin bottom left)
 * </pre>
 *
 * with {@code f = pixelsPerMetre * distance}, so the halfway line across the middle of the pitch draws at exactly
 * {@link #pixelsPerMetre()}: {@link #PIXELS_PER_METRE} (10 px/m, D28: the 2x sheets) by default, or
 * {@link #WIDE_PIXELS_PER_METRE} (5 px/m, the 1x sheets) in the optional wide view ({@link #setWide(boolean)}); the
 * near touchline is larger, the far one smaller. Each screen row below the
 * horizon is one ground depth ({@link #groundYAtRow(double)}), which is how the pitch pass draws the ground texture one
 * row at a time.
 *
 * <p>Art pixels land on whole screen pixels: the screen is divided by an integer magnification
 * ({@link #magnification()}, {@link CameraZoom}); {@link #magnificationFor(int, int)} picks the one that shows about
 * {@link #TARGET_VIEW_M} metres of the halfway depth at the default scale (1280x720 and 1920x1080: 2, i.e. 64 and
 * 96 m; 2560x1440: 3, 85 m), never so large that the pitch no longer fits the window height.
 */
public final class ViewCamera {

    /** D28: art pixels per metre at the middle of the pitch (the 2x sprite sheets). */
    public static final double PIXELS_PER_METRE = 10.0;
    /** The optional wide view (D26 S1, the 1x sheets): half the default scale, twice the field of view. */
    public static final double WIDE_PIXELS_PER_METRE = 5.0;
    /** Metres of the halfway depth the automatic magnification aims to show across the window. */
    public static final double TARGET_VIEW_M = 80.0;
    /** How far behind the near touchline the camera stands. */
    public static final double BEHIND_TOUCHLINE_M = 30.0;
    /** Camera height above the pitch. */
    public static final double HEIGHT_M = 20.0;
    /** Where the near touchline sits, as a share of the view height from the bottom. */
    public static final double NEAR_TOUCHLINE_SHARE = 0.04;
    /** Art rows the view needs at least (pitch from the near touchline to the skyline at the default scale). */
    public static final int MIN_VIEW_ROWS = 340;
    /** Follow rate of the critically smoothed ball follow, per second. */
    public static final double FOLLOW_RATE = 2.2;
    /** How far past the goal line the near edge of the view may go. */
    public static final double END_MARGIN_M = 6.0;

    private final double halfLength;
    private final double halfWidth;
    private final double distance;
    private final double height;
    private double focal;
    private double pixelsPerMetre = PIXELS_PER_METRE;

    private int magnification = 1;
    private boolean fixedMagnification;
    private int viewWidth = 1;
    private int viewHeight = 1;
    private double orthoWidth = 1;
    private double orthoHeight = 1;
    private double horizonY;
    private double x;

    public ViewCamera(PitchGeometry pitch) {
        this(pitch.halfLength(), pitch.halfWidth(), pitch.halfWidth() + BEHIND_TOUCHLINE_M, HEIGHT_M);
    }

    /**
     * @param distance metres from the halfway line's middle ({@code y = 0}) back to the camera
     * @param height   metres above the ground
     */
    public ViewCamera(double halfLength, double halfWidth, double distance, double height) {
        if (!(halfLength > 0) || !(halfWidth > 0) || !(distance > halfWidth) || !(height > 0)) {
            throw new IllegalArgumentException("camera rig " + halfLength + "/" + halfWidth + "/" + distance + "/"
                    + height);
        }
        this.halfLength = halfLength;
        this.halfWidth = halfWidth;
        this.distance = distance;
        this.height = height;
        this.focal = pixelsPerMetre * distance;
        resizeView(320, 180);
    }

    /**
     * Integer magnification for a window: the one whose view is closest to {@link #TARGET_VIEW_M} metres wide at the
     * default scale, capped so the view keeps {@link #MIN_VIEW_ROWS} rows (at least 1).
     */
    public static int magnificationFor(int screenWidth, int screenHeight) {
        int byWidth = (int) Math.round(Math.max(1, screenWidth) / (TARGET_VIEW_M * PIXELS_PER_METRE));
        int byHeight = Math.max(1, screenHeight) / MIN_VIEW_ROWS;
        return Math.max(1, Math.min(byWidth, byHeight));
    }

    /** The wide view (5 px/m, 1x sheets) or the default (10 px/m); same magnification, takes effect at once. */
    public void setWide(boolean wide) {
        pixelsPerMetre = wide ? WIDE_PIXELS_PER_METRE : PIXELS_PER_METRE;
        focal = pixelsPerMetre * distance;
        horizonY = NEAR_TOUCHLINE_SHARE * viewHeight + height * focal / (distance - halfWidth);
        x = clampX(x);
    }

    public boolean wide() {
        return pixelsPerMetre < PIXELS_PER_METRE;
    }

    /** Art pixels per metre at the halfway depth ({@link #PIXELS_PER_METRE}, or {@link #WIDE_PIXELS_PER_METRE}). */
    public double pixelsPerMetre() {
        return pixelsPerMetre;
    }

    /** Fits the view to a window: picks the magnification (unless fixed) and the art-pixel view size. */
    public void resize(int screenWidth, int screenHeight) {
        if (!fixedMagnification) {
            magnification = magnificationFor(screenWidth, screenHeight);
        }
        resizeView(ceilDiv(Math.max(1, screenWidth), magnification), ceilDiv(Math.max(1, screenHeight), magnification));
        orthoWidth = Math.max(1, screenWidth) / (double) magnification;
        orthoHeight = Math.max(1, screenHeight) / (double) magnification;
    }

    /** Sets the view size in art pixels directly (tests, screenshots). */
    public void resizeView(int artWidth, int artHeight) {
        if (artWidth < 1 || artHeight < 1) {
            throw new IllegalArgumentException("view " + artWidth + "x" + artHeight);
        }
        viewWidth = artWidth;
        viewHeight = artHeight;
        orthoWidth = artWidth;
        orthoHeight = artHeight;
        horizonY = NEAR_TOUCHLINE_SHARE * viewHeight + height * focal / (distance - halfWidth);
        x = clampX(x);
    }

    /** Fixes the magnification (1, 2, 3...) instead of picking it from the window; takes effect at the next resize. */
    public void setMagnification(int pixelsPerArtPixel) {
        magnification = Math.max(1, pixelsPerArtPixel);
        fixedMagnification = true;
    }

    /** Back to the magnification picked from the window height. */
    public void autoMagnification() {
        fixedMagnification = false;
    }

    public int magnification() {
        return magnification;
    }

    /** {@code OrthographicCamera.zoom} of the final blit ({@link CameraZoom#forMagnification(int)}). */
    public float zoom() {
        return CameraZoom.forMagnification(magnification);
    }

    // ---- projection ------------------------------------------------------------------------------------------------

    /** Depth of a ground line {@code y} from the camera (metres; positive in front of it). */
    public double depth(double y) {
        return y + distance;
    }

    /** Art pixels per metre at ground line {@code y} (5 at {@code y = 0}). */
    public double scale(double y) {
        return focal / depth(y);
    }

    public double screenX(double wx, double wy) {
        return orthoWidth / 2.0 + (wx - x) * focal / depth(wy);
    }

    public double screenY(double wy, double wz) {
        return horizonY - (height - wz) * focal / depth(wy);
    }

    /** The ground line {@code y} seen at screen row {@code sy} (a row centre); +infinity at or above the horizon. */
    public double groundYAtRow(double sy) {
        double below = horizonY - sy;
        if (!(below > 0)) {
            return Double.POSITIVE_INFINITY;
        }
        return height * focal / below - distance;
    }

    /** The world {@code x} seen at screen column {@code sx} on ground line {@code wy}. */
    public double worldX(double sx, double wy) {
        return x + (sx - orthoWidth / 2.0) * depth(wy) / focal;
    }

    // ---- follow ----------------------------------------------------------------------------------------------------

    /**
     * Moves toward the target {@code x} (the interpolated ball) with frame-rate independent exponential smoothing:
     * the same path at 30, 60 or 144 frames per second. Clamped so the view never runs far past the goal lines.
     */
    public void follow(double targetX, double frameSeconds) {
        double target = clampX(targetX);
        if (frameSeconds <= 0) {
            return;
        }
        double k = 1 - Math.exp(-FOLLOW_RATE * frameSeconds);
        x = clampX(x + (target - x) * k);
    }

    /** Jumps to the target {@code x} (kick-off, replays). */
    public void snapTo(double targetX) {
        x = clampX(targetX);
    }

    /** The furthest the camera may slide from the halfway line. */
    public double maxX() {
        double nearHalfSpan = (viewWidth / 2.0) * depth(-halfWidth) / focal;
        return Math.max(0, halfLength + END_MARGIN_M - nearHalfSpan);
    }

    private double clampX(double value) {
        double max = maxX();
        return value < -max ? -max : (value > max ? max : value);
    }

    private static int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }

    public double x() {
        return x;
    }

    public int viewWidth() {
        return viewWidth;
    }

    public int viewHeight() {
        return viewHeight;
    }

    /**
     * Width of the batch projection in art pixels: the window width divided by the magnification, so every art pixel
     * covers exactly {@link #magnification()} screen pixels ({@link #viewWidth()} rounds it up, for layout).
     */
    public double orthoWidth() {
        return orthoWidth;
    }

    public double orthoHeight() {
        return orthoHeight;
    }

    /** Screen row of the horizon (where ground depth reaches infinity), art pixels from the bottom. */
    public double horizonY() {
        return horizonY;
    }

    public double distance() {
        return distance;
    }

    public double height() {
        return height;
    }

    public double focal() {
        return focal;
    }

    public double halfLength() {
        return halfLength;
    }

    public double halfWidth() {
        return halfWidth;
    }
}
