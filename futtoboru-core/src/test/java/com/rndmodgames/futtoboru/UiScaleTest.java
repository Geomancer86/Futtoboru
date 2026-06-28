package com.rndmodgames.futtoboru;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.ui.UiScale;

class UiScaleTest {

    @Test
    void parseAndFormatPercent() {
        assertEquals(1.5f, UiScale.parsePercent("150%"), 0.001f);
        assertEquals("150%", UiScale.formatPercent(1.5f));
        assertEquals("100%", UiScale.nearestPresetLabel(1.05f));
    }
}
