package com.rndmodgames.futtoboru.engine.fans;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * G-10: the fan-love meter's rules from {@code gamedata/core/fans.json} (points per event, the tier band, hysteresis,
 * legend damping, the seeded jitter). A rule the data does not list keeps the code default (the shipped numbers, all
 * {@code estimate}).
 */
public final class FanRules {

    public static final String APPEARANCE = "appearance";
    public static final String GOAL = "goal";
    public static final String DERBY_GOAL = "derby-goal";
    public static final String CUP_TIE_WON = "cup-tie-won";
    public static final String CUP_FINAL_WON = "cup-final-won";
    public static final String ABSENCE = "absence";
    public static final String BAND = "band";
    public static final String HYSTERESIS = "hysteresis";
    public static final String LEGEND_DAMPING = "legend-damping";
    public static final String JITTER = "jitter";

    public static final FanRules DEFAULTS = new FanRules(defaults());

    private static GameData cachedFor;
    private static FanRules cached;

    private final Map<String, Double> values;

    private FanRules(Map<String, Double> values) {
        this.values = values;
    }

    private static Map<String, Double> defaults() {
        Map<String, Double> m = new HashMap<>();
        m.put(APPEARANCE, 3.0);
        m.put(GOAL, 8.0);
        m.put(DERBY_GOAL, 6.0);
        m.put(CUP_TIE_WON, 6.0);
        m.put(CUP_FINAL_WON, 40.0);
        m.put(ABSENCE, -1.5);
        m.put(BAND, 100.0);
        m.put(HYSTERESIS, 20.0);
        m.put(LEGEND_DAMPING, 0.25);
        m.put(JITTER, 0.25);
        return m;
    }

    /** The rules of the loaded game data (code defaults when there is none). */
    public static synchronized FanRules current() {
        GameData data = DatabaseLoader.getGameData();
        if (data == null) {
            return DEFAULTS;
        }
        if (data != cachedFor) {
            cached = from(data);
            cachedFor = data;
        }
        return cached;
    }

    public static FanRules from(GameData data) {
        Map<String, Double> m = defaults();
        for (DataRecord r : data.all(GameData.FAN_RULE)) {
            BigDecimal v = r.has("value") ? r.decimal("value") : null;
            if (r.str("rule") != null && v != null) {
                m.put(r.str("rule"), v.doubleValue());
            }
        }
        return new FanRules(m);
    }

    /** The defaults with one rule replaced (tests, tuning). */
    public FanRules with(String rule, double value) {
        Map<String, Double> m = new HashMap<>(values);
        m.put(rule, value);
        return new FanRules(m);
    }

    public double get(String rule) {
        Double v = values.get(rule);
        return v != null ? v : 0;
    }

    public double band() {
        return get(BAND) > 0 ? get(BAND) : 100;
    }

    public double hysteresis() {
        return Math.max(0, Math.min(get(HYSTERESIS), band() / 2 - 1e-9));
    }
}
