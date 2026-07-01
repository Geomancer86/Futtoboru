package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.InjurySeverity;
import com.rndmodgames.futtoboru.data.InjuryTypeDefinition;

/**
 * Injury type table for post-match rolls (B5.1). Loaded from {@code mods/injuries/types.txt}
 * with hardcoded defaults if missing.
 */
public final class InjuryTypesRegistry {

    private static final List<InjuryTypeDefinition> DEFAULT_TYPES = List.of(
            new InjuryTypeDefinition("strain", "Strain", 1, 2, InjurySeverity.MINOR, 40),
            new InjuryTypeDefinition("bruise", "Bruise", 1, 3, InjurySeverity.MINOR, 25),
            new InjuryTypeDefinition("twisted_ankle", "Twisted ankle", 2, 4, InjurySeverity.MODERATE, 20),
            new InjuryTypeDefinition("knee", "Knee injury", 3, 6, InjurySeverity.MODERATE, 10),
            new InjuryTypeDefinition("serious", "Serious injury", 6, 12, InjurySeverity.SERIOUS, 5));

    private static volatile List<InjuryTypeDefinition> types = DEFAULT_TYPES;

    static {
        reloadFromMod();
    }

    private InjuryTypesRegistry() {
    }

    public static List<InjuryTypeDefinition> getTypes() {
        return types;
    }

    public static InjuryTypeDefinition pickWeighted(Random random) {
        List<InjuryTypeDefinition> pool = types;
        if (pool.isEmpty()) {
            pool = DEFAULT_TYPES;
        }
        int totalWeight = 0;
        for (InjuryTypeDefinition type : pool) {
            totalWeight += Math.max(0, type.getWeight());
        }
        if (totalWeight <= 0) {
            return pool.get(0);
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (InjuryTypeDefinition type : pool) {
            cumulative += Math.max(0, type.getWeight());
            if (roll < cumulative) {
                return type;
            }
        }
        return pool.get(pool.size() - 1);
    }

    public static void reloadFromMod() {
        List<InjuryTypeDefinition> loaded = loadFromModFile();
        types = loaded.isEmpty() ? DEFAULT_TYPES : Collections.unmodifiableList(loaded);
    }

    private static List<InjuryTypeDefinition> loadFromModFile() {
        try {
            if (Gdx.files == null) {
                return List.of();
            }
            FileHandle file = Gdx.files.internal("mods/injuries/types.txt");
            if (!file.exists()) {
                return List.of();
            }
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(file.reader())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return parseLines(lines);
        } catch (IOException e) {
            if (Gdx.app != null) {
                Gdx.app.error("InjuryTypesRegistry", "Failed to load mods/injuries/types.txt", e);
            }
            return List.of();
        }
    }

    static List<InjuryTypeDefinition> parseLines(Iterable<String> lines) {
        List<InjuryTypeDefinition> parsed = new ArrayList<>();
        for (String raw : lines) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\|");
            if (parts.length < 6) {
                continue;
            }
            String id = parts[0].trim();
            String displayName = parts[1].trim();
            int minWeeks = Integer.parseInt(parts[2].trim());
            int maxWeeks = Integer.parseInt(parts[3].trim());
            InjurySeverity severity = InjurySeverity.valueOf(parts[4].trim().toUpperCase());
            int weight = Integer.parseInt(parts[5].trim());
            if (maxWeeks < minWeeks) {
                maxWeeks = minWeeks;
            }
            parsed.add(new InjuryTypeDefinition(id, displayName, minWeeks, maxWeeks, severity, weight));
        }
        return parsed;
    }
}
