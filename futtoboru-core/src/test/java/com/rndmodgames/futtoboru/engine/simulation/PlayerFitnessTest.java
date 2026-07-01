package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;

class PlayerFitnessTest {

    @Test
    void lowFitnessReducesStrengthFactor() {
        Player player = new Player();
        player.setId(1L);
        PlayerCondition fit = PlayerCondition.createFitDefault();
        fit.setFitnessPct(100);
        player.setCondition(fit);
        double full = PlayerConditionService.fitnessStrengthFactor(player);

        fit.setFitnessPct(20);
        double low = PlayerConditionService.fitnessStrengthFactor(player);

        assertTrue(low < full);
        assertTrue(low >= 0.5);
    }
}
