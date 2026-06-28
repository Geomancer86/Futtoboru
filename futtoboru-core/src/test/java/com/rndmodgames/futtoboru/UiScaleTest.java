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

    @Test
    void scaledPixelsFollowCurrentScale() {
        float previous = com.rndmodgames.futtoboru.game.Futtoboru.UI_SCALE;
        try {
            com.rndmodgames.futtoboru.game.Futtoboru.UI_SCALE = 1.5f;
            assertEquals(15, UiScale.scaled(10));
            assertEquals(1.5f, UiScale.currentScale(), 0.001f);
        } finally {
            com.rndmodgames.futtoboru.game.Futtoboru.UI_SCALE = previous;
        }
    }
}
