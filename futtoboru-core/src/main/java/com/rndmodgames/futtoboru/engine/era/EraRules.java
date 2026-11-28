package com.rndmodgames.futtoboru.engine.era;

import java.time.LocalDate;

import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.data.EraProfile;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * G-11: the one place game code asks "which law, ball, kit or league rule holds on this date". It reads the era
 * profile of the loaded game data ({@link GameData#era()}, core + mods); before the database is loaded (unit tests,
 * tools) it falls back to the shipped core files. Rules code reads {@code EraRules.at(date)} instead of a hard-coded
 * year (e.g. goal nets from 1891-92, points per win); the ECS engine can take {@code RulesProfile.fromEra(at(date),
 * year)} the same way.
 */
public final class EraRules {

    private static volatile EraProfile shipped;

    private EraRules() {
    }

    /** The era profile of the loaded data, or the shipped core one when nothing (valid) is loaded. */
    public static EraProfile profile() {
        GameData data = DatabaseLoader.getGameData();
        if (data != null && data.era() != null) {
            return data.era();
        }
        EraProfile p = shipped;
        if (p == null) {
            p = EraProfile.loadDefault();
            shipped = p;
        }
        return p;
    }

    /** Every parameter on {@code date} (all scoped events apply); a null date is the baseline (1888-89). */
    public static EraProfile.Resolved at(LocalDate date) {
        EraProfile p = profile();
        return date == null ? p.baseline() : p.at(date);
    }

    /** Every parameter on {@code date} for a competition data id (null: every scoped event applies). */
    public static EraProfile.Resolved at(LocalDate date, String competition) {
        EraProfile p = profile();
        return p.at(date == null ? p.baselineDate() : date, null, competition);
    }

    /** True when goal nets are fitted on {@code date} ({@code rules.goalNets}, League clubs from 1891-92). */
    public static boolean goalNets(LocalDate date) {
        return at(date).getBoolean("rules.goalNets");
    }
}
