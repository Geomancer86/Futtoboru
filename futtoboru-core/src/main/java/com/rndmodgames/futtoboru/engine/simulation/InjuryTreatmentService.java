package com.rndmodgames.futtoboru.engine.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.InjurySeverity;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.system.loaders.InjuryEconomyRegistry;

/**
 * Player injury treatments and insurance actions (B5.2b–e).
 */
public final class InjuryTreatmentService {

    public enum TreatmentResult {
        SUCCESS,
        NOT_INJURED,
        WRONG_SEVERITY,
        ALREADY_USED,
        INSUFFICIENT_FUNDS,
        ALREADY_INSURED,
        NOT_INSURED,
        NOT_FIT_FOR_INSURANCE
    }

    private InjuryTreatmentService() {
    }

    public static TreatmentResult rehabilitate(Club club, Player player, LocalDate today) {
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        if (!condition.isInjured(today)) {
            return TreatmentResult.NOT_INJURED;
        }
        InjurySeverity severity = condition.getInjurySeverityEnum();
        if (severity != InjurySeverity.MINOR && severity != InjurySeverity.MODERATE) {
            return TreatmentResult.WRONG_SEVERITY;
        }
        BigDecimal cost = InjuryEconomyRegistry.getRehabCost(severity);
        if (!InjuryGenerator.isPlayerInsured(player) && !deductCost(club, cost)) {
            return TreatmentResult.INSUFFICIENT_FUNDS;
        }
        int weeksOff = severity == InjurySeverity.MINOR ? 1 : 2;
        InjuryGenerator.shortenInjuryByWeeks(condition, today, weeksOff);
        condition.setRehabTreatmentActive(true);
        return TreatmentResult.SUCCESS;
    }

    public static TreatmentResult insure(Club club, Player player, LocalDate today) {
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        if (condition.isInjured(today)) {
            return TreatmentResult.NOT_FIT_FOR_INSURANCE;
        }
        if (InjuryGenerator.isPlayerInsured(player)) {
            return TreatmentResult.ALREADY_INSURED;
        }
        BigDecimal premium = InjuryGenerator.calculateInsurancePremium(player, club);
        if (premium.compareTo(BigDecimal.ZERO) <= 0) {
            return TreatmentResult.NOT_FIT_FOR_INSURANCE;
        }
        player.setInjuryInsured(true);
        player.setInjuryInsuranceWeekly(premium);
        return TreatmentResult.SUCCESS;
    }

    public static TreatmentResult uninsure(Player player) {
        if (!InjuryGenerator.isPlayerInsured(player)) {
            return TreatmentResult.NOT_INSURED;
        }
        player.setInjuryInsured(false);
        player.setInjuryInsuranceWeekly(null);
        return TreatmentResult.SUCCESS;
    }

    public static TreatmentResult sendToLilleshall(Club club, Player player, LocalDate today) {
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        if (!condition.isInjured(today)) {
            return TreatmentResult.NOT_INJURED;
        }
        if (condition.getInjurySeverityEnum() != InjurySeverity.SERIOUS) {
            return TreatmentResult.WRONG_SEVERITY;
        }
        if (condition.getLilleshallEndDate() != null) {
            return TreatmentResult.ALREADY_USED;
        }
        BigDecimal cost = InjuryEconomyRegistry.getLilleshallCost();
        if (!InjuryGenerator.isPlayerInsured(player) && !deductCost(club, cost)) {
            return TreatmentResult.INSUFFICIENT_FUNDS;
        }
        InjuryGenerator.multiplyRemainingRecovery(condition, today,
                InjuryEconomyRegistry.getLilleshallMultiplier());
        condition.setLilleshallEndDate(condition.getInjuryReturnDate());
        return TreatmentResult.SUCCESS;
    }

    public static TreatmentResult surgeonAbroad(Club club, Player player, LocalDate today, Random random) {
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        if (!condition.isInjured(today)) {
            return TreatmentResult.NOT_INJURED;
        }
        if (condition.getInjurySeverityEnum() != InjurySeverity.SERIOUS) {
            return TreatmentResult.WRONG_SEVERITY;
        }
        if (Boolean.TRUE.equals(condition.getSurgeonAbroadUsed())) {
            return TreatmentResult.ALREADY_USED;
        }
        BigDecimal balance = club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
        if (balance.compareTo(InjuryEconomyRegistry.getSurgeonMinBalance()) < 0) {
            return TreatmentResult.INSUFFICIENT_FUNDS;
        }
        BigDecimal cost = InjuryEconomyRegistry.getSurgeonCost();
        if (!InjuryGenerator.isPlayerInsured(player) && !deductCost(club, cost)) {
            return TreatmentResult.INSUFFICIENT_FUNDS;
        }
        condition.setSurgeonAbroadUsed(true);
        if (random != null && random.nextDouble() < 0.10) {
            return TreatmentResult.SUCCESS;
        }
        InjuryGenerator.multiplyRemainingRecovery(condition, today,
                InjuryEconomyRegistry.getSurgeonMultiplier());
        return TreatmentResult.SUCCESS;
    }

    public static BigDecimal sumWeeklyInsurancePremiums(Club club) {
        if (club == null || club.getPlayers() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Player player : club.getPlayers()) {
            if (player != null && InjuryGenerator.isPlayerInsured(player)
                    && player.getInjuryInsuranceWeekly() != null) {
                total = total.add(player.getInjuryInsuranceWeekly());
            }
        }
        return total;
    }

    static boolean deductCost(Club club, BigDecimal cost) {
        if (club == null || cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        BigDecimal balance = club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
        if (balance.compareTo(cost) < 0) {
            return false;
        }
        club.setClubBalance(balance.subtract(cost));
        return true;
    }
}
