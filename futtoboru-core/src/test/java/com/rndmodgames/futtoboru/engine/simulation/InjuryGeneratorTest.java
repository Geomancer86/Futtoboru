package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;

class InjuryGeneratorTest {

    @Test
    void forcesInjuryWhenRandomAlwaysHits() {
        Player player = new Player();
        player.setId(1L);
        PlayerCondition condition = PlayerCondition.createFitDefault();
        Random alwaysInjure = new Random(0) {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        };

        InjuryGenerator.maybeInjure(player, condition, alwaysInjure, LocalDate.of(1888, 9, 1));

        assertTrue(condition.isInjured(LocalDate.of(1888, 9, 1)));
        assertTrue(condition.getInjuryTypeId() != null && !condition.getInjuryTypeId().isEmpty());
        assertTrue(condition.getInjuryReturnDate().isAfter(LocalDate.of(1888, 9, 1)));
        assertTrue(condition.getInjurySeverityEnum() != null);
    }
}
