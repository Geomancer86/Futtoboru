package com.rndmodgames.futtoboru.engine.simulation;

import java.time.LocalDate;
import java.util.Random;

import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;

/**
 * Basic post-match injury rolls (CM93-style, small table).
 */
public final class InjuryGenerator {

    private static final double BASE_INJURY_CHANCE = 0.02;

    private InjuryGenerator() {
    }

    public static void maybeInjure(Player player, PlayerCondition condition, Random random, LocalDate today) {
        if (player == null || condition == null || condition.isInjured(today)) {
            return;
        }
        double chance = BASE_INJURY_CHANCE;
        if (condition.getFitnessPct() < 60) {
            chance += 0.02;
        }
        if (condition.getFatiguePct() > 70) {
            chance += 0.02;
        }
        if (random.nextDouble() >= chance) {
            return;
        }
        int weeks = random.nextDouble() < 0.85 ? 1 + random.nextInt(2) : 3 + random.nextInt(3);
        condition.setInjuryTypeId("strain");
        condition.setInjuryDisplayName("Strain");
        condition.setInjuryReturnDate(today.plusWeeks(weeks));
    }
}
