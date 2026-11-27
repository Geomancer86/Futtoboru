package com.rndmodgames.futtoboru;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.ui.UiScale;

class UiScaleTest {

    @Test
    void parseAndFormatPercent() {
        assertEquals(1.5f, UiScale.parsePercent("150%"), 0.001f);
        assertEquals("150%", UiScale.formatPercent(1.5f));
        assertEquals("100%", UiScale.nearestPresetLabel(1.05f));
    }

    @Test
    void autoFollowsHiDpiMonitor() {
        // BOOK3PRO: 2880x1800 at Windows 200%, whatever the window
        assertEquals(2f, UiScale.autoScale(2f, 1280, 1024), 0.001f);
        assertEquals(2f, UiScale.autoScale(2f, 2880, 1800), 0.001f);
        assertEquals(1.5f, UiScale.autoScale(1.5f, 1920, 1080), 0.001f);
        assertEquals(UiScale.MAX_AUTO_SCALE, UiScale.autoScale(4f, 3840, 2160), 0.001f);
    }

    @Test
    void autoOnNormalMonitorKeeps960x540Logical() {
        assertEquals(2f, UiScale.autoScale(1f, 1920, 1080), 0.001f);
        assertEquals(1f, UiScale.autoScale(1f, 1280, 1024), 0.001f);
        assertEquals(1f, UiScale.autoScale(1f, 1919, 1080), 0.001f);
    }

    @Test
    void zeroOrLessMeansAuto() {
        assertTrue(UiScale.isAuto(UiScale.AUTO));
        assertFalse(UiScale.isAuto(1f));
    }
}
