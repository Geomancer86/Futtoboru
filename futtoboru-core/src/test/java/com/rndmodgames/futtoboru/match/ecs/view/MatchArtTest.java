package com.rndmodgames.futtoboru.match.ecs.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;

/** The code-generated pitch (P1/A1/SL1) and skyline (K1): deterministic, era markings, slope, wrap. */
class MatchArtTest {

    static final PitchGeometry PITCH = new PitchGeometry(PitchParams.defaults1888());

    static boolean isLine(PitchArt art, double wx, double wy) {
        int px = (int) Math.floor(art.column(wx));
        int py = (int) Math.floor(art.row(wy));
        return art.at(px, py) == art.lineColour();
    }

    @Test
    void pitchIsFivePixelsPerMetreAndDeterministic() {
        PitchArt a = new PitchArt(PITCH, PitchArt.Style.of1888());
        PitchArt b = new PitchArt(PITCH, PitchArt.Style.of1888());
        assertArrayEquals(a.rgb, b.rgb, "same style and seed, same pixels");
        assertEquals(Math.ceil((PITCH.length() + 2 * PitchArt.END_MARGIN_M) * 5), a.width);
        assertEquals(PITCH.halfWidth() + PitchArt.FAR_MARGIN_M, a.maxY, 1e-12);
        PitchArt other = new PitchArt(PITCH, new PitchArt.Style(PitchArt.Turf.WORN, 0.6, 0, true, false, 8));
        assertFalse(java.util.Arrays.equals(a.rgb, other.rgb), "another seed, another pitch");
    }

    @Test
    void markingsOf1888AreTheBoundaryLinesOnly() {
        PitchArt art = new PitchArt(PITCH, PitchArt.Style.of1888());
        assertTrue(isLine(art, 10, PITCH.halfWidth()), "far touchline");
        assertTrue(isLine(art, -20, -PITCH.halfWidth()), "near touchline");
        assertTrue(isLine(art, PITCH.halfLength(), 5), "goal line");
        assertFalse(isLine(art, 0, 5), "no halfway line in 1888-89");
        assertFalse(isLine(art, 9.144, 0.1), "no centre circle in 1888-89");
        PitchArt halfway = new PitchArt(PITCH, PitchArt.Style.of1888().withHalfwayLine(true));
        assertTrue(isLine(halfway, 0, 5), "halfway line as an era parameter");
        PitchArt plain = new PitchArt(PITCH, PitchArt.Style.of1888().withMarkings(false));
        assertFalse(isLine(plain, 10, PITCH.halfWidth()), "lines off");
        PitchGeometry of1891 = new PitchGeometry(PitchParams.forSeason(1891, 110, 70));
        PitchArt later = new PitchArt(of1891, PitchArt.Style.of1888());
        double r = of1891.centreCircleRadius().getAsDouble();
        assertTrue(isLine(later, r, 0.0) || isLine(later, r - 0.1, 0.0) || isLine(later, r + 0.1, 0.0),
                "centre circle from 1891-92");
    }

    @Test
    void slopeDarkensTheDownhillEnd() {
        PitchArt flat = new PitchArt(PITCH, PitchArt.Style.of1888().withMarkings(false).withTurf(PitchArt.Turf.NORMAL,
                0));
        PitchArt sloped = new PitchArt(PITCH, flat.style().withSlope(3));
        int downhillChanged = changed(flat, sloped, PITCH.halfLength() - 10);
        int uphillChanged = changed(flat, sloped, -PITCH.halfLength() + 10);
        assertTrue(downhillChanged > 50, "downhill band darker: " + downhillChanged);
        assertEquals(0, uphillChanged, "uphill end untouched");
        PitchArt reversed = new PitchArt(PITCH, flat.style().withSlope(-3));
        assertTrue(changed(flat, reversed, -PITCH.halfLength() + 10) > 50, "a negative fall darkens the left end");
    }

    /** Pixels that differ in a 4 m column around x. */
    private static int changed(PitchArt a, PitchArt b, double x) {
        int n = 0;
        int c0 = (int) a.column(x - 2);
        int c1 = (int) a.column(x + 2);
        for (int py = (int) a.row(PITCH.halfWidth() - 1); py < (int) a.row(-PITCH.halfWidth() + 1); py++) {
            for (int px = c0; px < c1; px++) {
                if (a.at(px, py) != b.at(px, py)) {
                    n++;
                }
            }
        }
        return n;
    }

    @Test
    void wornAndMuddyTurfShowMudInTheGoalmouths() {
        PitchArt normal = new PitchArt(PITCH, PitchArt.Style.of1888().withTurf(PitchArt.Turf.NORMAL, 0));
        PitchArt muddy = new PitchArt(PITCH, PitchArt.Style.of1888().withTurf(PitchArt.Turf.MUDDY, 0.5));
        int px = (int) normal.column(PITCH.halfLength() - 3);
        int py = (int) normal.row(0);
        assertNotEquals(normal.at(px, py), muddy.at(px, py));
    }

    @Test
    void skylineIsDeterministicWrapsAndHasSmokingChimneys() {
        SkylineArt a = new SkylineArt(42);
        SkylineArt b = new SkylineArt(42);
        assertArrayEquals(a.rgb, b.rgb);
        assertTrue(a.chimneyCount >= 4, "a mill town: " + a.chimneyCount);
        int capped = 0;
        for (int c = 0; c < a.chimneyCount; c++) {
            int x = a.chimneys[c * 2];
            int y = a.chimneys[c * 2 + 1];
            assertTrue(x >= 0 && x < a.width && y > 0 && y < a.height, "chimney top inside the strip");
            capped += a.at(x, y + 1) == ArtMath.BRICK_HI ? 1 : 0;
        }
        assertTrue(capped >= a.chimneyCount - 1, "smoke rises from chimney caps (a spire may cover one)");
        assertEquals(a.at(a.width, 5), a.at(0, 5), "wraps");
        assertEquals(SkylineArt.SKY_TOP, a.at(7, 0), "sky at the top");
        assertFalse(java.util.Arrays.equals(a.rgb, new SkylineArt(43).rgb), "per ground");
    }
}
