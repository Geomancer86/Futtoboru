package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.InjurySeverity;
import com.rndmodgames.futtoboru.data.MedicalEra;
import com.rndmodgames.futtoboru.system.ReleaseScopeRegistry;

/**
 * Resolves {@link MedicalEra} from calendar date with release ceiling (R-1888 = ME-1 only).
 */
public final class MedicalEraRegistry {

    private static volatile List<MedicalEra> eras = List.of(MedicalEra.defaultMe1(), MedicalEra.defaultMe6());
    private static volatile MedicalEra fallbackEra = MedicalEra.defaultMe1();

    static {
        reloadFromMod();
    }

    private MedicalEraRegistry() {
    }

    public static void reloadFromMod() {
        List<MedicalEra> loaded = new ArrayList<>();
        try {
            if (Gdx.files == null) {
                applyEras(loaded);
                return;
            }
            FileHandle file = Gdx.files.internal("mods/injuries/medical_eras.txt");
            if (!file.exists()) {
                applyEras(loaded);
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
                    if (parts.length < 11) {
                        continue;
                    }
                    String id = parts[0].trim();
                    int startYear = Integer.parseInt(parts[1].trim());
                    int endYear = Integer.parseInt(parts[2].trim());
                    String rehabFile = parts[3].trim();
                    String stringsKey = parts[4].trim();
                    double weeksMult = Double.parseDouble(parts[5].trim());
                    int minorWk = Integer.parseInt(parts[6].trim());
                    int modWk = Integer.parseInt(parts[7].trim());
                    boolean insure = Boolean.parseBoolean(parts[8].trim());
                    boolean lilleshall = Boolean.parseBoolean(parts[9].trim());
                    boolean surgeon = Boolean.parseBoolean(parts[10].trim());
                    Map<InjurySeverity, BigDecimal> costs = loadRehabCostsForFile(rehabFile);
                    loaded.add(new MedicalEra(id, startYear, endYear, rehabFile, stringsKey,
                            weeksMult, minorWk, modWk, insure, lilleshall, surgeon, costs));
                }
            }
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("MedicalEraRegistry", "Failed to load medical_eras.txt", e);
            }
        }
        applyEras(loaded);
    }

    private static void applyEras(List<MedicalEra> loaded) {
        if (loaded.isEmpty()) {
            eras = List.of(MedicalEra.defaultMe1(), MedicalEra.defaultMe6());
        } else {
            eras = List.copyOf(loaded);
        }
        fallbackEra = eras.get(0);
    }

    private static Map<InjurySeverity, BigDecimal> loadRehabCostsForFile(String rehabFile) {
        Map<InjurySeverity, BigDecimal> costs = new EnumMap<>(InjurySeverity.class);
        costs.put(InjurySeverity.MINOR, new BigDecimal("25"));
        costs.put(InjurySeverity.MODERATE, new BigDecimal("75"));
        if (rehabFile == null || rehabFile.isEmpty()) {
            return costs;
        }
        try {
            if (Gdx.files == null) {
                if ("rehab_costs_me1.txt".equals(rehabFile)) {
                    costs.put(InjurySeverity.MINOR, new BigDecimal("0.25"));
                    costs.put(InjurySeverity.MODERATE, new BigDecimal("0.75"));
                }
                return costs;
            }
            FileHandle file = Gdx.files.internal("mods/injuries/" + rehabFile);
            if (!file.exists()) {
                return costs;
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
                    costs.put(severity, new BigDecimal(parts[1].trim()));
                }
            }
        } catch (Exception e) {
            if (Gdx.app != null) {
                Gdx.app.error("MedicalEraRegistry", "Failed to load " + rehabFile, e);
            }
        }
        return costs;
    }

    /**
     * Calendar era from date, then clamped to release ceiling.
     */
    public static MedicalEra resolve(LocalDate date) {
        MedicalEra era = resolveCalendarEra(date);
        return clampToReleaseCeiling(era);
    }

    public static MedicalEra resolveCalendarEra(LocalDate date) {
        if (date == null) {
            return fallbackEra;
        }
        int year = date.getYear();
        for (MedicalEra era : eras) {
            if (year >= era.getStartYear() && year <= era.getEndYear()) {
                return era;
            }
        }
        return fallbackEra;
    }

    public static MedicalEra clampToReleaseCeiling(MedicalEra era) {
        if (era == null) {
            return fallbackEra;
        }
        String ceiling = ReleaseScopeRegistry.getMaxImplementedMedicalEra();
        if (ceiling == null || ceiling.isEmpty()) {
            return era;
        }
        int eraOrder = eraOrder(era.getId());
        int ceilingOrder = eraOrder(ceiling);
        if (eraOrder <= ceilingOrder) {
            return era;
        }
        for (MedicalEra candidate : eras) {
            if (ceiling.equals(candidate.getId())) {
                return candidate;
            }
        }
        return MedicalEra.defaultMe1();
    }

    private static int eraOrder(String id) {
        if (id == null) {
            return 0;
        }
        return switch (id) {
            case "ME-0" -> 0;
            case "ME-1" -> 1;
            case "ME-2" -> 2;
            case "ME-3" -> 3;
            case "ME-4" -> 4;
            case "ME-5" -> 5;
            case "ME-6" -> 6;
            case "ME-7" -> 7;
            default -> 0;
        };
    }

    /** Test hook — reset without Gdx. */
    static void resetForTests() {
        eras = List.of(MedicalEra.defaultMe1(), MedicalEra.defaultMe6());
        fallbackEra = MedicalEra.defaultMe1();
    }

    static void setErasForTests(List<MedicalEra> testEras) {
        eras = List.copyOf(testEras);
        fallbackEra = testEras.isEmpty() ? MedicalEra.defaultMe1() : testEras.get(0);
    }
}
