package com.rndmodgames.futtoboru.history;

import java.util.HashMap;
import java.util.Map;

import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * D19-1: a per-club strength seeded from the real 1888-89 final league table (points per game). It only shapes the
 * odds of a simulated match (the engine adapter shifts the club's ratings by {@link #ratingShift}); it never decides
 * a result (D17). It lives in the history package because it reads the real results.
 *
 * <p>Strength is {@code 2 * (ppg - 0.5)}, in -1..1 (0 = a mid-table side, 1 = won every game); clubs outside the
 * League have 0 (cup-only clubs: no estimate, documented gap). {@link #STRENGTH} is the single tunable: the rating
 * points the best conceivable side gains (and the worst loses).
 */
public final class ClubStrength {

    static final String SEASON = "1888-89";
    /** Rating points (on the 3..23 scale) shifted for a strength of 1; overridable with -Dfuttoboru.strength. */
    public static final double STRENGTH = Double.parseDouble(System.getProperty("futtoboru.strength", "3"));

    private static Map<String, Double> table;

    private ClubStrength() {
    }

    /** @return the club's strength in -1..1 from the 1888-89 League table, 0 when it has none */
    public static synchronized double of(String clubDataId) {
        if (table == null) {
            table = compute(GameData.loadDefault());
        }
        return clubDataId == null ? 0 : table.getOrDefault(clubDataId, 0.0);
    }

    /** @return the rating shift of the club: {@link #STRENGTH} times its strength */
    public static double ratingShift(String clubDataId) {
        return STRENGTH == 0 ? 0 : STRENGTH * of(clubDataId);
    }

    static Map<String, Double> compute(GameData data) {
        Map<String, int[]> pts = new HashMap<>(); // club -> points, played
        for (DataRecord m : data.records("result", SEASON)) {
            DataRecord edition = data.get(GameData.EDITION, m.ref("edition"));
            DataRecord comp = edition != null ? data.get(GameData.COMPETITION, edition.ref("competition")) : null;
            Integer hg = m.integer("homeGoals"), ag = m.integer("awayGoals");
            if (comp == null || "CUP".equals(comp.str("type")) || hg == null || ag == null) {
                continue;
            }
            add(pts, m.ref("home"), hg, ag);
            add(pts, m.ref("away"), ag, hg);
        }
        Map<String, Double> out = new HashMap<>();
        pts.forEach((c, t) -> out.put(c, 2 * (t[0] / (double) (ReferenceResults.WIN * t[1]) - 0.5)));
        return out;
    }

    private static void add(Map<String, int[]> pts, String club, int gf, int ga) {
        int[] t = pts.computeIfAbsent(club, k -> new int[2]);
        t[0] += gf > ga ? ReferenceResults.WIN : gf == ga ? ReferenceResults.DRAW : 0;
        t[1]++;
    }
}
