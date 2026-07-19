package com.rndmodgames.futtoboru.system.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Parses and plausibility-checks the {@code dd/MM/yyyy} birth-date text entered on
 * {@code NewManagerScreen}.
 *
 * <p>Added 2026-07-19 per the pre-game menus UX audit (P0): the screen previously only guarded against
 * an <b>empty</b> field via {@code FormValidator.notEmpty}. Garbage input ("abc", "31/02/1990",
 * "01/01/3000") parsed as a {@link java.text.ParseException}, was swallowed by an empty
 * {@code catch} block, and the manager still saved with a {@code null} birth date — silently corrupting
 * save data with no feedback to the player.
 *
 * <p>Kept dependency-free (no libGDX/VisUI) so it is unit-testable without a headless GL context.
 */
public final class ManagerBirthDateValidator {

    // STRICT resolver style: SMART (the default) clamps an out-of-range day like 31 Feb down to the
    // month's actual last day (28/29) instead of rejecting it, which is exactly the silent-rollover
    // behavior this validator exists to prevent — see the class doc and calendarInvalidDate test.
    // Pattern uses "uuuu" (proleptic year) rather than "yyyy" (year-of-era): STRICT mode requires an
    // explicit era to resolve a year-of-era field and has none here, so "yyyy" fails to resolve at all
    // under STRICT — "uuuu" needs no era and resolves directly.
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /** Earliest plausible manager birth year — anyone claiming to be older is not a data-entry typo, it's garbage. */
    public static final int MIN_BIRTH_YEAR = 1780;

    /** A manager must be at least this old at the season's start date to plausibly hold the role. */
    public static final int MIN_AGE_AT_SEASON_START = 16;

    private ManagerBirthDateValidator() {
    }

    /**
     * @param text            raw field text
     * @param seasonStartYear the calendar year the selected season starts (used for the "not too young,
     *                        not from the future" plausibility check); pass any reasonable default
     *                        (e.g. the current season's start year) if no season is selected yet
     */
    public static boolean isValid(String text, int seasonStartYear) {
        LocalDate parsed = tryParse(text);
        if (parsed == null) {
            return false;
        }
        if (parsed.getYear() < MIN_BIRTH_YEAR) {
            return false;
        }
        return parsed.getYear() <= seasonStartYear - MIN_AGE_AT_SEASON_START;
    }

    /**
     * @return the parsed date, or {@code null} if {@code text} is blank or not a valid {@code dd/MM/yyyy}
     *         calendar date (rejects e.g. {@code 31/02/1990} — {@link DateTimeFormatter}, unlike the
     *         {@code java.text.SimpleDateFormat} this replaces, does not silently roll invalid dates over
     *         into the next month).
     */
    public static LocalDate tryParse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
