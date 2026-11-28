package com.rndmodgames.futtoboru.engine.injuries;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * G-02: the injury kinds of {@code gamedata/core/injuries.json} (chance per man per match, days out, in-match effect).
 * A kind the data does not list keeps the code default (the same numbers as the shipped file, all {@code estimate}).
 */
public final class InjuryRules {

    /** One kind's numbers. */
    public static final class Rule {
        public final InjuryKind kind;
        public final double chancePerMatch;
        public final int minDays;
        public final int maxDays;
        /** Carried off: the chance he comes back on (as a passenger). */
        public final double returnChance;
        /** Passenger (and a carried-off man who came back): what he is worth next to a fit man, 0..1. */
        public final double strengthWhileHurt;

        public Rule(InjuryKind kind, double chancePerMatch, int minDays, int maxDays, double returnChance,
                double strengthWhileHurt) {
            this.kind = kind;
            this.chancePerMatch = chancePerMatch;
            this.minDays = minDays;
            this.maxDays = maxDays;
            this.returnChance = returnChance;
            this.strengthWhileHurt = strengthWhileHurt;
        }
    }

    public static final InjuryRules DEFAULTS = new InjuryRules(defaults());

    private static GameData cachedFor;
    private static InjuryRules cached;

    private final Map<InjuryKind, Rule> rules;

    private InjuryRules(Map<InjuryKind, Rule> rules) {
        this.rules = Collections.unmodifiableMap(rules);
    }

    private static Map<InjuryKind, Rule> defaults() {
        Map<InjuryKind, Rule> m = new EnumMap<>(InjuryKind.class);
        m.put(InjuryKind.KNOCK, new Rule(InjuryKind.KNOCK, 0.03, 0, 6, 0, 1));
        m.put(InjuryKind.PASSENGER, new Rule(InjuryKind.PASSENGER, 0.008, 7, 21, 0, 0.4));
        m.put(InjuryKind.CARRIED_OFF, new Rule(InjuryKind.CARRIED_OFF, 0.006, 14, 56, 0.3, 0.4));
        return m;
    }

    /** The rules of the loaded game data (code defaults when there is none). */
    public static synchronized InjuryRules current() {
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

    public static InjuryRules from(GameData data) {
        Map<InjuryKind, Rule> m = defaults();
        for (DataRecord r : data.all(GameData.INJURY)) {
            InjuryKind kind = InjuryKind.fromDataId(r.str("kind"));
            if (kind == null) {
                continue;
            }
            Rule d = m.get(kind);
            m.put(kind, new Rule(kind, num(r, "chancePerMatch", d.chancePerMatch),
                    r.integer("minDays") != null ? r.integer("minDays") : d.minDays,
                    r.integer("maxDays") != null ? r.integer("maxDays") : d.maxDays,
                    num(r, "returnChance", d.returnChance), num(r, "strengthWhileHurt", d.strengthWhileHurt)));
        }
        return new InjuryRules(m);
    }

    /** The defaults with these kinds replaced (tests, tuning). */
    public static InjuryRules of(Rule... replace) {
        Map<InjuryKind, Rule> m = defaults();
        for (Rule r : replace) {
            m.put(r.kind, r);
        }
        return new InjuryRules(m);
    }

    private static double num(DataRecord r, String field, double def) {
        BigDecimal v = r.has(field) ? r.decimal(field) : null;
        return v != null ? v.doubleValue() : def;
    }

    public Rule get(InjuryKind kind) {
        return rules.get(kind);
    }
}
