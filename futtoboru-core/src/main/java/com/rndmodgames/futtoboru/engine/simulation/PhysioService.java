package com.rndmodgames.futtoboru.engine.simulation;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.loaders.InjuryEconomyRegistry;

/**
 * Resolves club physio staff quality and recovery multipliers (B5.2a).
 */
public final class PhysioService {

    private PhysioService() {
    }

    public static int resolvePhysioAbility(Club club, ClubStaffManager staffManager) {
        if (club == null || staffManager == null) {
            return InjuryEconomyRegistry.DEFAULT_PHYSIO_ABILITY;
        }
        Profession physioProfession = DatabaseLoader.getProfessionById(InjuryEconomyRegistry.PHYSIO_PROFESSION_ID);
        if (physioProfession == null) {
            return InjuryEconomyRegistry.DEFAULT_PHYSIO_ABILITY;
        }
        Person physio = staffManager.getClubStaff(club, physioProfession);
        if (physio == null) {
            return InjuryEconomyRegistry.DEFAULT_PHYSIO_ABILITY;
        }
        return clampAbility(physio.getStaffAbility());
    }

    public static int clampAbility(Integer ability) {
        if (ability == null) {
            return InjuryEconomyRegistry.DEFAULT_PHYSIO_ABILITY;
        }
        return Math.max(1, Math.min(5, ability));
    }

    public static double recoveryMultiplier(int ability) {
        int index = clampAbility(ability);
        return 1.20 - (index * 0.07);
    }

    public static int applyPhysioWeeks(int baseWeeks, int ability) {
        int weeks = Math.max(1, (int) Math.round(baseWeeks * recoveryMultiplier(ability)));
        return Math.max(1, weeks);
    }

    public static String abilityLabel(int ability) {
        return switch (clampAbility(ability)) {
            case 1 -> "Fair";
            case 2 -> "Average";
            case 3 -> "OK";
            case 4 -> "Good";
            default -> "Superb";
        };
    }

    public static String formatEstimate(int weeks, int ability) {
        return "~" + weeks + " wk (" + abilityLabel(ability) + " physio)";
    }
}
