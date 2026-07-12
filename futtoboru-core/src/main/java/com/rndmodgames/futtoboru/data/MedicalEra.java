package com.rndmodgames.futtoboru.data;

import java.util.EnumMap;
import java.util.Map;

/**
 * Historical medical era overlay for injury presentation and economy (M-B5.3-HIST).
 */
public final class MedicalEra {

    private final String id;
    private final int startYear;
    private final int endYear;
    private final String rehabCostsFile;
    private final String stringsKey;
    private final double baseWeeksMultiplier;
    private final int rehabMinorWeeksReduction;
    private final int rehabModerWeeksReduction;
    private final boolean insureAllowed;
    private final boolean lilleshallAllowed;
    private final boolean surgeonAbroadAllowed;
    private final Map<InjurySeverity, java.math.BigDecimal> rehabCosts;

    public MedicalEra(String id, int startYear, int endYear, String rehabCostsFile, String stringsKey,
            double baseWeeksMultiplier, int rehabMinorWeeksReduction, int rehabModerWeeksReduction,
            boolean insureAllowed, boolean lilleshallAllowed, boolean surgeonAbroadAllowed,
            Map<InjurySeverity, java.math.BigDecimal> rehabCosts) {
        this.id = id;
        this.startYear = startYear;
        this.endYear = endYear;
        this.rehabCostsFile = rehabCostsFile;
        this.stringsKey = stringsKey;
        this.baseWeeksMultiplier = baseWeeksMultiplier;
        this.rehabMinorWeeksReduction = rehabMinorWeeksReduction;
        this.rehabModerWeeksReduction = rehabModerWeeksReduction;
        this.insureAllowed = insureAllowed;
        this.lilleshallAllowed = lilleshallAllowed;
        this.surgeonAbroadAllowed = surgeonAbroadAllowed;
        this.rehabCosts = rehabCosts != null ? rehabCosts : Map.of();
    }

    public String getId() {
        return id;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public String getRehabCostsFile() {
        return rehabCostsFile;
    }

    public String getStringsKey() {
        return stringsKey;
    }

    public double getBaseWeeksMultiplier() {
        return baseWeeksMultiplier;
    }

    public int getRehabMinorWeeksReduction() {
        return rehabMinorWeeksReduction;
    }

    public int getRehabModerWeeksReduction() {
        return rehabModerWeeksReduction;
    }

    public boolean isInsureAllowed() {
        return insureAllowed;
    }

    public boolean isLilleshallAllowed() {
        return lilleshallAllowed;
    }

    public boolean isSurgeonAbroadAllowed() {
        return surgeonAbroadAllowed;
    }

    public java.math.BigDecimal getRehabCost(InjurySeverity severity) {
        if (severity == null) {
            return java.math.BigDecimal.ZERO;
        }
        return rehabCosts.getOrDefault(severity, java.math.BigDecimal.ZERO);
    }

    public static MedicalEra defaultMe1() {
        Map<InjurySeverity, java.math.BigDecimal> costs = new EnumMap<>(InjurySeverity.class);
        costs.put(InjurySeverity.MINOR, new java.math.BigDecimal("0.25"));
        costs.put(InjurySeverity.MODERATE, new java.math.BigDecimal("0.75"));
        return new MedicalEra("ME-1", 1885, 1905, "rehab_costs_me1.txt", "ME-1",
                1.20, 0, 1, false, false, false, costs);
    }

    public static MedicalEra defaultMe6() {
        Map<InjurySeverity, java.math.BigDecimal> costs = new EnumMap<>(InjurySeverity.class);
        costs.put(InjurySeverity.MINOR, new java.math.BigDecimal("25"));
        costs.put(InjurySeverity.MODERATE, new java.math.BigDecimal("75"));
        return new MedicalEra("ME-6", 1993, 1999, "rehab_costs.txt", "ME-6",
                1.00, 1, 2, true, true, true, costs);
    }
}
