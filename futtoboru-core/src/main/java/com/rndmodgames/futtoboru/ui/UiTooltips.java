package com.rndmodgames.futtoboru.ui;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * The one tooltip layer (U-09): VisUI {@link Tooltip} with the skin's default tooltip style, text from the language
 * files. Use {@link #tip(Actor, String)} on any widget, {@link #header(String, String)} for an abbreviated table
 * column header.
 */
public final class UiTooltips {

    /** League table columns: header label key and tooltip key, in display order (Pos, Club, P, W, D, L, GF, GA, GD, Pts). */
    public static final List<String[]> LEAGUE_COLUMNS = List.of(
            new String[] { "table_pos", "tip_pos" },
            new String[] { "table_club", null },
            new String[] { "table_played", "tip_played" },
            new String[] { "table_won", "tip_won" },
            new String[] { "table_drawn", "tip_drawn" },
            new String[] { "table_lost", "tip_lost" },
            new String[] { "table_gf", "tip_goals_for" },
            new String[] { "table_ga", "tip_goals_against" },
            new String[] { "table_gd", "tip_goal_difference" },
            new String[] { "table_pts", "tip_points" });

    /** Squad list attribute columns with an abbreviated header; tooltip key = {@code "tip_attr_" + column}. */
    public static final List<String> SQUAD_ATTRIBUTE_COLUMNS = List.of("acceleration", "speed", "strength",
            "stamina", "jumping", "passing", "longShots", "heading", "tackling", "marking", "determination",
            "positioning", "teamwork");

    private UiTooltips() {
    }

    /** Attaches the tooltip text of {@code tipKey} to {@code target}; a null key leaves it without one. */
    public static <T extends Actor> T tip(T target, String tipKey) {
        if (target != null && tipKey != null) {
            new Tooltip.Builder(LanguageModLoader.getValue(tipKey)).target(target).build();
        }
        return target;
    }

    /** Attaches {@code text} as it is (a data value such as a ground name, not a label). */
    public static <T extends Actor> T tipText(T target, String text) {
        if (target != null && text != null && !text.isBlank()) {
            new Tooltip.Builder(text).target(target).build();
        }
        return target;
    }

    /** A header label from {@code labelKey} with the tooltip {@code tipKey} (may be null). */
    public static VisLabel header(String labelKey, String tipKey) {
        return tip(new VisLabel(LanguageModLoader.getValue(labelKey)), tipKey);
    }
}
