package com.rndmodgames.futtoboru.engine.finances;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * One place for money text (P2-7): pounds sterling, "£1,234.50", whatever the machine's locale.
 * The game font has the £ glyph (droid-sans-13, added for P2-7).
 */
public final class Money {

    public static final String SYMBOL = "£";

    private Money() {
    }

    /** "£1,234.50" ("£0.00" for null); negative amounts as "-£12.00" */
    public static String format(BigDecimal amount) {
        BigDecimal value = amount != null ? amount : BigDecimal.ZERO;
        String digits = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.UK)).format(value.abs());
        return (value.signum() < 0 ? "-" : "") + SYMBOL + digits;
    }

    /** Same as {@link #format(BigDecimal)} for any number */
    public static String format(Number amount) {
        return format(amount == null ? null : new BigDecimal(amount.toString()));
    }
}
