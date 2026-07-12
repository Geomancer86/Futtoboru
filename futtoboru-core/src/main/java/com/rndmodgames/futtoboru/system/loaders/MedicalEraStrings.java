package com.rndmodgames.futtoboru.system.loaders;

import java.time.LocalDate;

import com.rndmodgames.futtoboru.data.MedicalEra;
import com.rndmodgames.futtoboru.engine.simulation.PhysioService;

/**
 * Era-aware UI strings for injury care (Trainer vs Physio).
 */
public final class MedicalEraStrings {

    private MedicalEraStrings() {
    }

    public static String staffRole(LocalDate date) {
        return isVictorian(date) ? "Trainer" : "Physio";
    }

    public static String estimateColumnHeader(LocalDate date) {
        return isVictorian(date) ? "Trainer's estimate" : "Return";
    }

    public static String actionRehabilitate(LocalDate date) {
        return isVictorian(date) ? "Send to trainer" : "Rehabilitate";
    }

    public static String conditionSectionTitle(LocalDate date) {
        return isVictorian(date) ? "Condition & Trainer care" : "Condition & Treatment";
    }

    public static String formatEstimate(int weeks, int ability, LocalDate date) {
        if (isVictorian(date)) {
            return "~" + weeks + " wk (club trainer)";
        }
        return PhysioService.formatEstimate(weeks, ability);
    }

    public static String inboxInjuryLead(String playerName, int weeks, LocalDate date) {
        if (isVictorian(date)) {
            return playerName + " is laid up — trainer says ~" + weeks + " weeks.";
        }
        return playerName + " is injured — physio estimates ~" + weeks + " weeks.";
    }

    public static String abilitySuffix(int ability, LocalDate date) {
        if (isVictorian(date)) {
            return ", club trainer";
        }
        return ", " + PhysioService.abilityLabel(ability) + " physio";
    }

    private static boolean isVictorian(LocalDate date) {
        MedicalEra era = MedicalEraRegistry.resolve(date);
        return "ME-1".equals(era.getId());
    }
}
