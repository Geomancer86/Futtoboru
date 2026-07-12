package com.rndmodgames.futtoboru.system.loaders;

import java.time.LocalDate;

import com.rndmodgames.futtoboru.data.MedicalEra;

/**
 * Feature gates for injury actions by medical era (M-B5.3-HIST).
 */
public final class MedicalEraFeatureGate {

    private MedicalEraFeatureGate() {
    }

    public static MedicalEra eraFor(LocalDate date) {
        return MedicalEraRegistry.resolve(date);
    }

    public static boolean canInsure(LocalDate date) {
        return eraFor(date).isInsureAllowed();
    }

    public static boolean canRehabilitate(LocalDate date) {
        return true;
    }

    public static boolean canLilleshall(LocalDate date) {
        return eraFor(date).isLilleshallAllowed();
    }

    public static boolean canSurgeonAbroad(LocalDate date) {
        return eraFor(date).isSurgeonAbroadAllowed();
    }
}
