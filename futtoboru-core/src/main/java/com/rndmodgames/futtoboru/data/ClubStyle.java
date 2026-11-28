package com.rndmodgames.futtoboru.data;

import java.util.Locale;

/**
 * How a club plays (D19-6, G-01): the English dribbling game (a forward runs the ball at the defence, his fellows
 * backing up) or the combination game of short passing that Scottish clubs brought south.
 */
public enum ClubStyle {

    DRIBBLING,
    COMBINATION;

    /** @return the language bundle key of the style's name ({@code style_dribbling}) */
    public String labelKey() {
        return "style_" + name().toLowerCase(Locale.ROOT);
    }

    /** @return the language bundle key of the tooltip that describes the style */
    public String tipKey() {
        return "tip_style_" + name().toLowerCase(Locale.ROOT);
    }
}
