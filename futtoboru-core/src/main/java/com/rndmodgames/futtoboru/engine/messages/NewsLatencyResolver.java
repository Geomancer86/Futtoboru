package com.rndmodgames.futtoboru.engine.messages;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Computes news delivery delay by era band and region pair (NEWS-8).
 */
public class NewsLatencyResolver {

    public static final String BRITISH_ISLES = "BRITISH_ISLES";
    public static final String WESTERN_EUROPE = "WESTERN_EUROPE";
    public static final String SOUTH_AMERICA = "SOUTH_AMERICA";

    private static final Map<String, Integer> DEFAULT_DAYS = new HashMap<>();

    static {
        putPair(BRITISH_ISLES, BRITISH_ISLES, 0);
        putPair(BRITISH_ISLES, WESTERN_EUROPE, 5);
        putPair(BRITISH_ISLES, SOUTH_AMERICA, 21);
        putPair(WESTERN_EUROPE, BRITISH_ISLES, 5);
        putPair(SOUTH_AMERICA, BRITISH_ISLES, 21);
        putPair(SOUTH_AMERICA, SOUTH_AMERICA, 3);
    }

    private final Map<String, Integer> daysByPair;

    public NewsLatencyResolver() {
        this.daysByPair = new HashMap<>(DEFAULT_DAYS);
        loadFromModFile();
    }

    /**
     * Delivery date at the recipient region for a world event.
     */
    public LocalDateTime deliveryDate(LocalDateTime eventDate, String originRegion, String recipientRegion) {
        if (eventDate == null) {
            return null;
        }
        int days = deliveryDays(originRegion, recipientRegion, eventDate.getYear());
        return eventDate.plusDays(days);
    }

    public int deliveryDays(String originRegion, String recipientRegion, int eventYear) {
        String from = normalizeRegion(originRegion);
        String to = normalizeRegion(recipientRegion);
        if (from.equals(to)) {
            return 0;
        }
        String key = pairKey(from, to);
        int days = daysByPair.getOrDefault(key, daysByPair.getOrDefault(pairKey(to, from), 14));
        if (eventYear >= 1901 && eventYear <= 1914) {
            days = Math.max(0, (int) Math.round(days * 0.7));
        }
        return days;
    }

    private void loadFromModFile() {
        if (Gdx.files == null) {
            return;
        }
        FileHandle file = Gdx.files.internal("mods/news_latency.txt");
        if (!file.exists()) {
            return;
        }
        for (String line : file.readString().split("\\r?\\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                daysByPair.put(pairKey(parts[0].trim(), parts[1].trim()), Integer.parseInt(parts[2].trim()));
            }
        }
    }

    private static void putPair(String from, String to, int days) {
        DEFAULT_DAYS.put(pairKey(from, to), days);
    }

    private static String pairKey(String from, String to) {
        return normalizeRegion(from) + ">" + normalizeRegion(to);
    }

    private static String normalizeRegion(String region) {
        if (region == null || region.isEmpty()) {
            return BRITISH_ISLES;
        }
        return region.trim().toUpperCase();
    }
}
