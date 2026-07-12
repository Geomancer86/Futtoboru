package com.rndmodgames.futtoboru.system;

/**
 * Active release scope ceiling (R-1888). Calendar may advance past 1905 in a save,
 * but features above {@link #MAX_IMPLEMENTED_MEDICAL_ERA} stay disabled until a later release.
 */
public final class ReleaseScopeRegistry {

    public static final String RELEASE_ID = "R-1888";
    public static final String MAX_IMPLEMENTED_MEDICAL_ERA = "ME-1";

    private static volatile String activeMedicalEraCeiling = MAX_IMPLEMENTED_MEDICAL_ERA;

    private ReleaseScopeRegistry() {
    }

    public static String getMaxImplementedMedicalEra() {
        return activeMedicalEraCeiling;
    }

    /** Test hook — raise ceiling for CM93 economy regression tests. */
    public static void setMaxImplementedMedicalEraForTests(String era) {
        activeMedicalEraCeiling = era != null ? era : MAX_IMPLEMENTED_MEDICAL_ERA;
    }

    public static void resetForTests() {
        activeMedicalEraCeiling = MAX_IMPLEMENTED_MEDICAL_ERA;
    }
}
