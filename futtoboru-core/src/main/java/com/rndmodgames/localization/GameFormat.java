package com.rndmodgames.localization;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dates and numbers in the <b>game language</b>, never the machine's (U-12). A Spanish Windows running an English
 * game must show "Oct 13, 1888" and "11.0", not "13 oct. 1888" and "11,0".
 * <ul>
 * <li>The game language is the loaded bundle's ({@link LanguageModLoader#setBundle}); with no bundle (headless tests,
 * early statics) it is English (US), like the label fallback.</li>
 * <li>Date patterns come from the bundles ({@code date_*_pattern}, {@code time_pattern}), so each language orders day,
 * month and year its own way; month and day names come from the matching {@link Locale} ({@link #locale()}).</li>
 * <li>Money stays in {@code Money.format} (pounds sterling, the same in every language).</li>
 * </ul>
 * UI code, screens and inbox texts format through here; {@code GameFormatGuardTest} fails on a new
 * {@code DateTimeFormatter.ofPattern}, {@code String.format}, {@code DecimalFormat} or {@code SimpleDateFormat} in them.
 */
public final class GameFormat {

    /** Numeric date, "10/13/1888" (US) or "13/10/1888". */
    public static final String NUMERIC = "date_numeric_pattern";
    /** Date with a short month name, "Oct 13, 1888" (US), "13 Oct 1888", "13 oct 1888". */
    public static final String MEDIUM = "date_medium_pattern";
    /** Date with the day of the week and the month in full, "Saturday, October 13, 1888". */
    public static final String LONG = "date_long_pattern";
    /** Date with short day and month names, "Sat, Oct 13, 1888". */
    public static final String WEEKDAY = "date_weekday_pattern";
    /** Time of day, "3:00 PM" (US) or "15:00". */
    public static final String TIME = "time_pattern";
    /** The top bar clock: short day name and time, "Sat 03:00:00 PM" (US) or "sáb 15:00:00". */
    public static final String CLOCK = "clock_pattern";

    /** Built-in patterns when a bundle lacks a key or holds a bad pattern (English US, like the label fallback). */
    private static final Map<String, String> FALLBACK = Map.of(
            NUMERIC, "MM/dd/yyyy",
            MEDIUM, "MMM d, yyyy",
            LONG, "EEEE, MMMM d, yyyy",
            WEEKDAY, "EEE, MMM d, yyyy",
            TIME, "h:mm a",
            CLOCK, "EEE hh:mm:ss a");

    private static final Locale SPANISH_SPAIN = new Locale("es", "ES");
    private static final Locale SPANISH_LATIN_AMERICA = Locale.forLanguageTag("es-419");

    private static volatile Locale locale = Locale.US;

    private static final Map<String, DateTimeFormatter> FORMATTERS = new ConcurrentHashMap<>();

    private GameFormat() {
    }

    /** @return the {@link Locale} of the game language (month and day names, decimal and grouping marks) */
    public static Locale locale() {
        return locale;
    }

    /** Sets the game language from a bundle locale or a language id; null means English (US). */
    static void setLanguage(Locale bundleLocale) {
        locale = bundleLocale == null ? Locale.US : localeOf(bundleLocale.toString());
    }

    /**
     * The {@link Locale} of a game language id: {@code en_US}, {@code en_UK}, {@code sp_SP}, {@code sp_LA} (any case,
     * as the preferences and the bundle loader spell them); an unknown id is English (US).
     */
    public static Locale localeOf(String languageId) {
        String id = languageId == null ? "" : languageId.toLowerCase(Locale.ROOT).replace('-', '_');
        switch (id) {
        case "en_uk":
        case "en_gb":
            return Locale.UK;
        case "sp_sp":
        case "es_es":
        case "sp":
        case "es":
            return SPANISH_SPAIN;
        case "sp_la":
        case "es_419":
            return SPANISH_LATIN_AMERICA;
        default:
            return Locale.US;
        }
    }

    /**
     * A formatter for the bundle pattern {@code patternKey} in the game language (falls back to the English (US)
     * pattern when the key is missing or the pattern is bad).
     */
    public static DateTimeFormatter formatter(String patternKey) {
        String fallback = FALLBACK.getOrDefault(patternKey, FALLBACK.get(MEDIUM));
        return formatter(patternKey, fallback);
    }

    /** A formatter for the bundle pattern {@code patternKey}, or {@code fallbackPattern} when it is missing or bad. */
    public static DateTimeFormatter formatter(String patternKey, String fallbackPattern) {
        String pattern = LanguageModLoader.getValue(patternKey);
        if (pattern == null || pattern.isBlank() || pattern.equals(patternKey)) {
            pattern = fallbackPattern;
        }
        Locale current = locale;
        try {
            return ofPattern(pattern, current);
        } catch (IllegalArgumentException badPattern) { // a bad pattern in a language file
            return ofPattern(fallbackPattern, current);
        }
    }

    private static DateTimeFormatter ofPattern(String pattern, Locale in) {
        return FORMATTERS.computeIfAbsent(in.toLanguageTag() + "|" + pattern, k -> DateTimeFormatter.ofPattern(pattern, in));
    }

    /** {@code value} (a date or date-time) with the bundle pattern {@code patternKey}; "" for null. */
    public static String date(TemporalAccessor value, String patternKey) {
        return value == null ? "" : formatter(patternKey).format(value);
    }

    /** "10/13/1888" (US) or "13/10/1888" */
    public static String numericDate(TemporalAccessor value) {
        return date(value, NUMERIC);
    }

    /** "Oct 13, 1888" (US), "13 Oct 1888" (UK), "13 oct 1888" */
    public static String mediumDate(TemporalAccessor value) {
        return date(value, MEDIUM);
    }

    /** "Saturday, October 13, 1888" (US), "Saturday 13 October 1888" (UK), "sábado 13 de octubre de 1888" */
    public static String longDate(TemporalAccessor value) {
        return date(value, LONG);
    }

    /** "Sat, Oct 13, 1888" (US), "Sat 13 Oct 1888" (UK), "sáb 13 oct 1888" */
    public static String weekdayDate(TemporalAccessor value) {
        return date(value, WEEKDAY);
    }

    /** "3:00 PM" (US) or "15:00" */
    public static String time(TemporalAccessor value) {
        return date(value, TIME);
    }

    /** {@code value} with exactly {@code fractionDigits} decimals: "11.0" (English), "11,0" (Spanish, Spain). */
    public static String decimal(double value, int fractionDigits) {
        DecimalFormat format = new DecimalFormat("0", DecimalFormatSymbols.getInstance(locale));
        format.setMinimumFractionDigits(fractionDigits);
        format.setMaximumFractionDigits(fractionDigits);
        return format.format(value);
    }

    /** A whole number with the language's grouping mark: "12,000" (English), "12.000" (Spanish, Spain). */
    public static String integer(long value) {
        return new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(locale)).format(value);
    }

    /** {@link String#format} in the game language (for {@code %.1f}, {@code %,d}, {@code %+.1f}). */
    public static String format(String pattern, Object... args) {
        return String.format(locale, pattern, args);
    }
}
