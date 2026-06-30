package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Derby pairs for attendance boost (MVP-4). Loaded from {@code mods/derbies.txt}.
 */
public final class DerbyRegistry {

    private static final Set<String> PAIRS = loadPairs();

    private DerbyRegistry() {
    }

    public static boolean isDerby(Long clubIdA, Long clubIdB) {
        if (clubIdA == null || clubIdB == null || clubIdA.equals(clubIdB)) {
            return false;
        }
        long a = Math.min(clubIdA, clubIdB);
        long b = Math.max(clubIdA, clubIdB);
        return PAIRS.contains(pairKey(a, b));
    }

    public static int pairCount() {
        return PAIRS.size();
    }

    private static Set<String> loadPairs() {
        Set<String> pairs = new HashSet<>();
        try {
            FileHandle file = Gdx.files.internal("mods/derbies.txt");
            if (!file.exists()) {
                return Collections.unmodifiableSet(pairs);
            }
            try (BufferedReader reader = new BufferedReader(file.reader())) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        long idA = Long.parseLong(parts[0].trim());
                        long idB = Long.parseLong(parts[1].trim());
                        pairs.add(pairKey(Math.min(idA, idB), Math.max(idA, idB)));
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            if (Gdx.app != null) {
                Gdx.app.error("DerbyRegistry", "Failed to load mods/derbies.txt", e);
            }
        }
        return Collections.unmodifiableSet(pairs);
    }

    private static String pairKey(long a, long b) {
        return a + ":" + b;
    }
}
