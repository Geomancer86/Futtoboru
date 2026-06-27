package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Loads competition rules from the start-season mod folder.
 *
 * File: mods/seasons/{seasonId}/competition_rules.txt
 */
public final class CompetitionRulesLoader {

    private static final Map<Long, CompetitionRules> rulesBySeasonId = new HashMap<>();

    private CompetitionRulesLoader() {
    }

    public static CompetitionRules getRulesForSeason(long seasonId) {
        if (rulesBySeasonId.containsKey(seasonId)) {
            return rulesBySeasonId.get(seasonId);
        }
        CompetitionRules loaded = loadSeasonRules(seasonId);
        rulesBySeasonId.put(seasonId, loaded);
        return loaded;
    }

    public static void loadAll(Map<Long, CompetitionRules> target) {
        target.clear();
        target.putAll(rulesBySeasonId);
    }

    static CompetitionRules loadSeasonRules(long seasonId) {
        String path = "mods/seasons/" + seasonId + "/competition_rules.txt";
        FileHandle file = Gdx.files.internal(path);

        if (!file.exists()) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING,
                    "CompetitionRulesLoader", "No rules file at " + path + ", using historical defaults for 1888");
            if (seasonId == 18L) {
                return CompetitionRules.createHistoricalRules();
            }
            return CompetitionRules.createDefaultRules();
        }

        CompetitionRules rules = CompetitionRules.createDefaultRules();
        BufferedReader reader = new BufferedReader(file.reader());
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                applyProperty(rules, key, value);
            }
        } catch (IOException e) {
            Gdx.app.error("CompetitionRulesLoader", "Failed to read " + path, e);
        }
        return rules;
    }

    private static void applyProperty(CompetitionRules rules, String key, String value) {
        switch (key) {
            case "points_win":
                rules.setPointsForWin(parseInt(value, 2));
                break;
            case "points_draw":
                rules.setPointsForDraw(parseInt(value, 1));
                break;
            case "points_loss":
                rules.setPointsForLoss(parseInt(value, 0));
                break;
            case "match_format":
                if ("SINGLE_ROUND_ROBIN".equalsIgnoreCase(value)) {
                    rules.setMatchFormat(CompetitionRules.MatchFormat.SINGLE_ROUND_ROBIN);
                } else {
                    rules.setMatchFormat(CompetitionRules.MatchFormat.DOUBLE_ROUND_ROBIN);
                }
                break;
            case "tie_breaker":
                rules.getTieBreakingOrder().clear();
                for (String part : value.split(",")) {
                    rules.getTieBreakingOrder().add(CompetitionRules.TieBreaker.valueOf(part.trim()));
                }
                break;
            default:
                break;
        }
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
