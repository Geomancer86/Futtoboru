package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;

class PlayerConditionDefaultsTest {

    @Test
    void newPlayerConditionDefaultsAreFit() {
        PlayerCondition condition = PlayerCondition.createFitDefault();
        assertEquals(PlayerCondition.DEFAULT_FITNESS, condition.getFitnessPct());
        assertEquals(PlayerCondition.DEFAULT_MORALE, condition.getMorale());
        assertEquals(PlayerCondition.DEFAULT_SHARPNESS, condition.getSharpnessPct());
        assertFalse(condition.isInjured(java.time.LocalDate.of(1888, 9, 1)));
    }

    @Test
    void conditionOfInitializesMissingConditionOnPlayer() {
        Player player = new Player();
        player.setId(1L);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        assertEquals(PlayerCondition.DEFAULT_FITNESS, condition.getFitnessPct());
        assertTrue(player.getCondition() != null);
    }
}
