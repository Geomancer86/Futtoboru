package com.rndmodgames.futtoboru.ui;

import java.io.IOException;

import org.junit.jupiter.api.Test;

/** U-09: the tooltip keys built at run time (not visible to the source scan in LanguageBundleKeysTest) exist. */
class UiTooltipsKeysTest {

    @Test
    void leagueColumnLabelsAndTooltipsExist() throws IOException {
        for (String[] column : UiTooltips.LEAGUE_COLUMNS) {
            LanguageBundleKeysTest.assertKeyInAllBundles(column[0]);
            if (column[1] != null) {
                LanguageBundleKeysTest.assertKeyInAllBundles(column[1]);
            }
        }
    }

    @Test
    void squadAttributeTooltipsExist() throws IOException {
        for (String column : UiTooltips.SQUAD_ATTRIBUTE_COLUMNS) {
            LanguageBundleKeysTest.assertKeyInAllBundles("tip_attr_" + column);
        }
    }

    @Test
    void historyTableTooltipsExist() throws IOException {
        for (String key : new String[] { "tip_pos", "tip_played", "tip_won", "tip_drawn", "tip_lost", "tip_goals_for",
                "tip_goals_against", "tip_points" }) {
            LanguageBundleKeysTest.assertKeyInAllBundles(key);
        }
    }
}
