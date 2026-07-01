package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.InjurySeverity;

/**
 * Injury economy tables (B5.2): rehab, specialist treatment, insurance premiums.
 */
public final class InjuryEconomyRegistry {

    public static final long PHYSIO_PROFESSION_ID = 9L;
    public static final int DEFAULT_PHYSIO_ABILITY = 3;

    private static final Map<InjurySeverity, BigDecimal> DEFAULT_REHAB_COSTS = Map.of(
            InjurySeverity.MINOR, new BigDecimal("25"),
            InjurySeverity.MODERATE, new BigDecimal("75"));

    private static final BigDecimal DEFAULT_LILLESHALL_COST = new BigDecimal("300");
    private static final double DEFAULT_LILLESHALL_MULTIPLIER = 0.6;
    private static final BigDecimal DEFAULT_SURGEON_COST = new BigDecimal("500");
    private static final double DEFAULT_SURGEON_MULTIPLIER = 0.5;
    private static final BigDecimal DEFAULT_PREMIUM_FRACTION = new BigDecimal("0.05");
    private static final BigDecimal DEFAULT_SURGEON_MIN_BALANCE = new BigDecimal("500");

    private static volatile Map<InjurySeverity, BigDecimal> rehabCosts = DEFAULT_REHAB_COSTS;
    private static volatile BigDecimal lilleshallCost = DEFAULT_LILLESHALL_COST;
    private static volatile double lilleshallMultiplier = DEFAULT_LILLESHALL_MULTIPLIER;
    private static volatile BigDecimal surgeonCost = DEFAULT_SURGEON_COST;
    private static volatile double surgeonMultiplier = DEFAULT_SURGEON_MULTIPLIER;
    private static volatile BigDecimal premiumWageFraction = DEFAULT_PREMIUM_FRACTION;
    private static volatile BigDecimal surgeonMinBalance = DEFAULT_SURGEON_MIN_BALANCE;

    static {
        reloadFromMod();
    }

    private InjuryEconomyRegistry() {
    }

    public static BigDecimal getRehabCost(InjurySeverity severity) {
        if (severity == null) {
            return BigDecimal.ZERO;
        }
        return rehabCosts.getOrDefault(severity, BigDecimal.ZERO);
    }

    public static BigDecimal getLilleshallCost() {
        return lilleshallCost;
    }

    public static double getLilleshallMultiplier() {
        return lilleshallMultiplier;
    }

    public static BigDecimal getSurgeonCost() {
        return surgeonCost;
    }

    public static double getSurgeonMultiplier() {
        return surgeonMultiplier;
    }

    public static BigDecimal getPremiumWageFraction() {
        return premiumWageFraction;
    }

    public static BigDecimal getSurgeonMinBalance() {
        return surgeonMinBalance;
    }

    public static void reloadFromMod() {
        loadRehabCosts();
        loadTreatmentCosts();
        loadInsurance();
    }

    private static void loadRehabCosts() {
        Map<InjurySeverity, BigDecimal> loaded = new EnumMap<>(InjurySeverity.class);
        loaded.putAll(DEFAULT_REHAB_COSTS);
        try {
            if (Gdx.files == null) {
                rehabCosts = loaded;
                return;
            }
            FileHandle file = Gdx.files.internal("mods/injuries/rehab_costs.txt");
            if (!file.exists()) {
                rehabCosts = loaded;
                return;
            }
            try (BufferedReader reader = new BufferedReader(file.reader())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line == null) {
                        continue;
                    }
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\|");
                    if (parts.length < 2) {
                        continue;
                    }
                    InjurySeverity severity = InjurySeverity.valueOf(parts[0].trim().toUpperCase());
                    loaded.put(severity, new BigDecimal(parts[1].trim()));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            if (Gdx.app != null) {
                Gdx.app.error("InjuryEconomyRegistry", "Failed to load rehab_costs.txt", e);
            }
        }
        rehabCosts = loaded;
    }

    private static void loadTreatmentCosts() {
        BigDecimal lilCost = DEFAULT_LILLESHALL_COST;
        double lilMult = DEFAULT_LILLESHALL_MULTIPLIER;
        BigDecimal surgCost = DEFAULT_SURGEON_COST;
        double surgMult = DEFAULT_SURGEON_MULTIPLIER;
        try {
            if (Gdx.files == null) {
                applyTreatmentDefaults(lilCost, lilMult, surgCost, surgMult);
                return;
            }
            FileHandle file = Gdx.files.internal("mods/injuries/treatment_costs.txt");
            if (!file.exists()) {
                applyTreatmentDefaults(lilCost, lilMult, surgCost, surgMult);
                return;
            }
            try (BufferedReader reader = new BufferedReader(file.reader())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line == null) {
                        continue;
                    }
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\|");
                    if (parts.length < 3) {
                        continue;
                    }
                    String key = parts[0].trim().toUpperCase();
                    BigDecimal cost = new BigDecimal(parts[1].trim());
                    double multiplier = Double.parseDouble(parts[2].trim());
                    if ("LILLESHALL".equals(key)) {
                        lilCost = cost;
                        lilMult = multiplier;
                    } else if ("SURGEON_ABROAD".equals(key)) {
                        surgCost = cost;
                        surgMult = multiplier;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            if (Gdx.app != null) {
                Gdx.app.error("InjuryEconomyRegistry", "Failed to load treatment_costs.txt", e);
            }
        }
        applyTreatmentDefaults(lilCost, lilMult, surgCost, surgMult);
    }

    private static void applyTreatmentDefaults(BigDecimal lilCost, double lilMult,
            BigDecimal surgCost, double surgMult) {
        lilleshallCost = lilCost;
        lilleshallMultiplier = lilMult;
        surgeonCost = surgCost;
        surgeonMultiplier = surgMult;
    }

    private static void loadInsurance() {
        BigDecimal fraction = DEFAULT_PREMIUM_FRACTION;
        BigDecimal minBalance = DEFAULT_SURGEON_MIN_BALANCE;
        try {
            if (Gdx.files == null) {
                premiumWageFraction = fraction;
                surgeonMinBalance = minBalance;
                return;
            }
            FileHandle file = Gdx.files.internal("mods/injuries/insurance.txt");
            if (!file.exists()) {
                premiumWageFraction = fraction;
                surgeonMinBalance = minBalance;
                return;
            }
            Map<String, String> values = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(file.reader())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line == null) {
                        continue;
                    }
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\|");
                    if (parts.length >= 2) {
                        values.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
            if (values.containsKey("premium_wage_fraction")) {
                fraction = new BigDecimal(values.get("premium_wage_fraction"));
            }
            if (values.containsKey("surgeon_abroad_min_balance")) {
                minBalance = new BigDecimal(values.get("surgeon_abroad_min_balance"));
            }
        } catch (IOException | NumberFormatException e) {
            if (Gdx.app != null) {
                Gdx.app.error("InjuryEconomyRegistry", "Failed to load insurance.txt", e);
            }
        }
        premiumWageFraction = fraction;
        surgeonMinBalance = minBalance;
    }

    /** Test hook — reset to defaults without Gdx files. */
    static void resetToDefaultsForTests() {
        rehabCosts = new EnumMap<>(DEFAULT_REHAB_COSTS);
        lilleshallCost = DEFAULT_LILLESHALL_COST;
        lilleshallMultiplier = DEFAULT_LILLESHALL_MULTIPLIER;
        surgeonCost = DEFAULT_SURGEON_COST;
        surgeonMultiplier = DEFAULT_SURGEON_MULTIPLIER;
        premiumWageFraction = DEFAULT_PREMIUM_FRACTION;
        surgeonMinBalance = DEFAULT_SURGEON_MIN_BALANCE;
    }
}
