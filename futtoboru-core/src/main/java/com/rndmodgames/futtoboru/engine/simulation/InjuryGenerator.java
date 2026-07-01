package com.rndmodgames.futtoboru.engine.simulation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.InjurySeverity;
import com.rndmodgames.futtoboru.data.InjuryTypeDefinition;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.system.loaders.InjuryEconomyRegistry;
import com.rndmodgames.futtoboru.system.loaders.InjuryTypesRegistry;

/**
 * Post-match injury rolls (CM93-style — B5.1 + B5.2 economy).
 */
public final class InjuryGenerator {

    private static final double BASE_INJURY_CHANCE = 0.02;

    private InjuryGenerator() {
    }

    /**
     * @return true if a new injury was applied
     */
    public static boolean maybeInjure(Player player, PlayerCondition condition, Club club,
            Random random, LocalDate today) {
        return applyInjury(player, condition, club, null, random, today).isApplied();
    }

    public static InjuryApplication applyInjury(Player player, PlayerCondition condition, Club club,
            ClubStaffManager staffManager, Random random, LocalDate today) {
        if (player == null || condition == null || condition.isInjured(today)) {
            return InjuryApplication.none();
        }
        double chance = BASE_INJURY_CHANCE;
        if (condition.getFitnessPct() < 60) {
            chance += 0.02;
        }
        if (condition.getFatiguePct() > 70) {
            chance += 0.02;
        }
        int proneness = resolveProneness(player);
        chance += (proneness - 10) * 0.001;
        if (random.nextDouble() >= chance) {
            return InjuryApplication.none();
        }

        InjuryTypeDefinition type = InjuryTypesRegistry.pickWeighted(random);
        int span = type.getMaxWeeks() - type.getMinWeeks();
        int baseWeeks = type.getMinWeeks() + (span > 0 ? random.nextInt(span + 1) : 0);
        int physioAbility = staffManager != null
                ? PhysioService.resolvePhysioAbility(club, staffManager)
                : InjuryEconomyRegistry.DEFAULT_PHYSIO_ABILITY;
        int weeks = PhysioService.applyPhysioWeeks(baseWeeks, physioAbility);

        condition.clearTreatmentState();
        condition.setInjuryTypeId(type.getId());
        condition.setInjuryDisplayName(type.getDisplayName());
        condition.setInjurySeverityEnum(type.getSeverity());
        condition.setInjuryReturnDate(today.plusWeeks(weeks));
        condition.setBaseWeeksBeforePhysio(baseWeeks);
        condition.setInjuryEstimateWeeks(weeks);
        condition.setPhysioAbilityAtInjury(physioAbility);
        condition.setRehabTreatmentActive(false);
        condition.setSurgeonAbroadUsed(false);
        condition.setLilleshallEndDate(null);

        return new InjuryApplication(true, type.getSeverity(), type.getDisplayName(), weeks,
                physioAbility, player.toString());
    }

    public static int resolveProneness(Player player) {
        if (player == null || player.getInjuryProneness() == null) {
            return 10;
        }
        return Math.max(1, Math.min(20, player.getInjuryProneness()));
    }

    public static void shortenInjuryByWeeks(PlayerCondition condition, LocalDate today, int weeksToRemove) {
        if (condition == null || today == null || condition.getInjuryReturnDate() == null) {
            return;
        }
        long days = ChronoUnit.DAYS.between(today, condition.getInjuryReturnDate());
        int remainingWeeks = Math.max(1, (int) Math.ceil(days / 7.0));
        int newWeeks = Math.max(1, remainingWeeks - weeksToRemove);
        condition.setInjuryReturnDate(today.plusWeeks(newWeeks));
        condition.setInjuryEstimateWeeks(newWeeks);
    }

    public static void multiplyRemainingRecovery(PlayerCondition condition, LocalDate today, double factor) {
        if (condition == null || today == null || condition.getInjuryReturnDate() == null) {
            return;
        }
        long days = ChronoUnit.DAYS.between(today, condition.getInjuryReturnDate());
        int newDays = Math.max(7, (int) Math.round(days * factor));
        condition.setInjuryReturnDate(today.plusDays(newDays));
        condition.setInjuryEstimateWeeks(Math.max(1, (int) Math.ceil(newDays / 7.0)));
    }

    public static BigDecimal calculateInsurancePremium(Player player, Club club) {
        if (player == null || club == null) {
            return BigDecimal.ZERO;
        }
        PlayerContract contract = null;
        if (player.getId() != null) {
            contract = club.getContractForPlayer(player.getId());
        }
        if (contract == null && player.getPerson() != null && player.getPerson().getId() != null) {
            contract = club.getContractForPlayer(player.getPerson().getId());
        }
        if (contract == null || contract.getWeeklyWage() == null) {
            return BigDecimal.ZERO;
        }
        return contract.getWeeklyWage()
                .multiply(InjuryEconomyRegistry.getPremiumWageFraction())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isPlayerInsured(Player player) {
        return player != null && Boolean.TRUE.equals(player.getInjuryInsured());
    }
}
