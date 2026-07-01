package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;

class PlayerSharpnessFactorTest {

    @Test
    void sharpnessFactorScalesFromSeventyToOneHundredPercent() {
        Player low = playerWithSharpness(0);
        Player high = playerWithSharpness(100);
        assertEquals(0.7, PlayerConditionService.sharpnessStrengthFactor(low), 0.001);
        assertTrue(PlayerConditionService.sharpnessStrengthFactor(high) > PlayerConditionService.sharpnessStrengthFactor(low));
        assertEquals(1.0, PlayerConditionService.sharpnessStrengthFactor(high), 0.01);
    }

    private static Player playerWithSharpness(int sharpness) {
        Person person = new Person();
        person.setId(1L);
        Player player = new Player();
        player.setId(1L);
        player.setPerson(person);
        PlayerCondition condition = PlayerCondition.createFitDefault();
        condition.setSharpnessPct(sharpness);
        player.setCondition(condition);
        return player;
    }
}
