package com.rndmodgames.futtoboru.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * U-03a / D18: UI scale steps, the Auto rule, and the 1280x720 minimum window.
 */
class UiScaleTest {

    @AfterEach
    void reset() {
        UiScale.setSetting(UiScale.AUTO);
        UiScale.setSystemScale(1f);
    }

    @Test
    void onlyIntegerSteps() {
        assertArrayEquals(new float[] {1f, 2f}, UiScale.PRESET_SCALES, 0f);
        assertEquals(1f, UiScale.normalize(1.25f), 0f);
        assertEquals(2f, UiScale.normalize(1.5f), 0f);
        assertEquals(2f, UiScale.normalize(3f), 0f);
        assertEquals(UiScale.AUTO, UiScale.normalize(0f), 0f);
        assertEquals(UiScale.AUTO, UiScale.normalize(-1f), 0f);
        assertEquals(UiScale.AUTO, UiScale.normalize(Float.NaN), 0f);
        assertEquals("100%", UiScale.formatPercent(1f));
        assertEquals("200%", UiScale.formatPercent(2f));
    }

    @Test
    void zeroOrLessMeansAuto() {
        assertTrue(UiScale.isAuto(UiScale.AUTO));
        assertFalse(UiScale.isAuto(1f));
        UiScale.setSetting(2f);
        assertEquals(2f, UiScale.currentScale(), 0f);
        UiScale.setSetting(1f);
        assertEquals(1f, UiScale.currentScale(), 0f);
    }

    @Test
    void autoFollowsHiDpiMonitorRoundedToOneOrTwo() {
        // a 2880x1800 laptop at Windows 200%: 2x whatever the window
        assertEquals(2f, UiScale.autoScale(2f, 1f, 1280, 720), 0f);
        assertEquals(2f, UiScale.autoScale(2f, 1f, 2880, 1800), 0f);
        // 150% and 175% round to 2, 125% to 1 (then the window decides)
        assertEquals(2f, UiScale.autoScale(1.5f, 1f, 1280, 720), 0f);
        assertEquals(2f, UiScale.autoScale(1.75f, 1f, 1280, 720), 0f);
        assertEquals(1f, UiScale.autoScale(1.25f, 1f, 1280, 720), 0f);
        // never above 2x: the atlas only has crisp integer steps and 1280x720 must stay usable
        assertEquals(2f, UiScale.autoScale(4f, 1f, 3840, 2160), 0f);
    }

    @Test
    void autoOnNormalMonitorKeeps960x540Logical() {
        assertEquals(2f, UiScale.autoScale(1f, 1f, 1920, 1080), 0f);
        assertEquals(2f, UiScale.autoScale(1f, 1f, 2560, 1440), 0f);
        assertEquals(1f, UiScale.autoScale(1f, 1f, 1280, 720), 0f);
        assertEquals(1f, UiScale.autoScale(1f, 1f, 1280, 1024), 0f);
        assertEquals(1f, UiScale.autoScale(1f, 1f, 1919, 1080), 0f);
        assertEquals(1f, UiScale.autoScale(1f, 1f, 1920, 1079), 0f);
    }

    @Test
    void autoDoesNotApplyARetinaScaleTwice() {
        // macOS Retina: the OS already draws the window at 2x (back buffer twice the window), content scale 2
        assertEquals(1f, UiScale.autoScale(2f, 2f, 1280, 720), 0f);
        assertEquals(2f, UiScale.autoScale(2f, 2f, 1920, 1080), 0f);
    }

    @Test
    void autoWithoutAWindowIsOneX() {
        // plain unit test: no Gdx.graphics
        assertEquals(1f, UiScale.currentScale(), 0f);
    }

    @Test
    void windowIsAtLeast1280x720() {
        assertArrayEquals(new int[] {1280, 720}, UiScale.windowSize(null));
        assertArrayEquals(new int[] {1280, 720}, UiScale.windowSize(""));
        assertArrayEquals(new int[] {1280, 720}, UiScale.windowSize("640x480"));
        assertArrayEquals(new int[] {1280, 720}, UiScale.windowSize("1280x480"));
        assertArrayEquals(new int[] {1280, 720}, UiScale.windowSize("wide"));
        assertArrayEquals(new int[] {1920, 1080}, UiScale.windowSize("1920x1080"));
        assertArrayEquals(new int[] {1280, 720}, UiScale.windowSize(" 1280x720 "));
        assertEquals("1280x720", UiScale.sanitizeResolution("800x600"));
        assertEquals("2560x1440", UiScale.sanitizeResolution("2560x1440"));
        assertEquals(UiScale.DEFAULT_RESOLUTION, "1280x720");
    }

    @Test
    void shippedResolutionsAreAllAtLeast1280x720() throws IOException {
        boolean has720p = false;
        try (InputStream in = UiScaleTest.class.getResourceAsStream("/mods/resolutions.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String entry = line.trim();
                if (entry.isEmpty() || entry.startsWith("#")) {
                    continue;
                }
                assertEquals(entry, UiScale.sanitizeResolution(entry), "below the 1280x720 minimum (D18): " + entry);
                has720p |= entry.equals(UiScale.DEFAULT_RESOLUTION);
            }
        }
        assertTrue(has720p, "resolutions.txt must offer the default 1280x720");
    }
}
