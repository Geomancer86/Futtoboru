package com.rndmodgames.futtoboru.engine.simulation;

import java.time.LocalDate;
import java.util.Random;

import com.rndmodgames.futtoboru.data.InjuryTypeDefinition;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.system.loaders.InjuryTypesRegistry;

/**
 * Post-match injury rolls (CM93-style weighted types — B5.1).
 */
public final class InjuryGenerator {

    private static final double BASE_INJURY_CHANCE = 0.02;

    private InjuryGenerator() {
    }

    /**
     * @return true if a new injury was applied
     */
    public static boolean maybeInjure(Player player, PlayerCondition condition, Random random, LocalDate today) {
        if (player == null || condition == null || condition.isInjured(today)) {
            return false;
        }
        double chance = BASE_INJURY_CHANCE;
        if (condition.getFitnessPct() < 60) {
            chance += 0.02;
        }
        if (condition.getFatiguePct() > 70) {
            chance += 0.02;
        }
        if (random.nextDouble() >= chance) {
            return false;
        }
        InjuryTypeDefinition type = InjuryTypesRegistry.pickWeighted(random);
        int span = type.getMaxWeeks() - type.getMinWeeks();
        int weeks = type.getMinWeeks() + (span > 0 ? random.nextInt(span + 1) : 0);
        condition.setInjuryTypeId(type.getId());
        condition.setInjuryDisplayName(type.getDisplayName());
        condition.setInjurySeverityEnum(type.getSeverity());
        condition.setInjuryReturnDate(today.plusWeeks(weeks));
        return true;
    }
}
