package com.rndmodgames.futtoboru.system.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ManagerBirthDateValidator}, added alongside the pre-game menus UX audit fix
 * (2026-07-19) for the previously-silent invalid-birthdate path on {@code NewManagerScreen}.
 */
class ManagerBirthDateValidatorTest {

    private static final int SEASON_START_YEAR = 1888;

    @Test
    void validPlausibleDateParsesAndPassesValidation() {
        assertEquals(LocalDate.of(1850, 4, 12), ManagerBirthDateValidator.tryParse("12/04/1850"));
        assertTrue(ManagerBirthDateValidator.isValid("12/04/1850", SEASON_START_YEAR));
    }

    @Test
    void blankOrNullTextFailsToParse() {
        assertNull(ManagerBirthDateValidator.tryParse(""));
        assertNull(ManagerBirthDateValidator.tryParse(null));
        assertFalse(ManagerBirthDateValidator.isValid("", SEASON_START_YEAR));
    }

    @Test
    void garbageTextFailsToParse() {
        assertNull(ManagerBirthDateValidator.tryParse("not a date"));
        assertFalse(ManagerBirthDateValidator.isValid("not a date", SEASON_START_YEAR));
    }

    @Test
    void calendarInvalidDateIsRejectedRatherThanRolledOver() {
        // 31 Feb does not exist; SimpleDateFormat (previous implementation) would silently roll this
        // over into March. DateTimeFormatter must reject it outright.
        assertNull(ManagerBirthDateValidator.tryParse("31/02/1990"));
        assertFalse(ManagerBirthDateValidator.isValid("31/02/1990", SEASON_START_YEAR));
    }

    @Test
    void tooYoungAtSeasonStartFailsPlausibilityCheck() {
        // Born the same year the season starts — obviously not old enough to manage a club.
        assertFalse(ManagerBirthDateValidator.isValid("01/01/1888", SEASON_START_YEAR));
    }

    @Test
    void impliedFutureOrAbsurdlyOldBirthYearFailsPlausibilityCheck() {
        assertFalse(ManagerBirthDateValidator.isValid("01/01/3000", SEASON_START_YEAR));
        assertFalse(ManagerBirthDateValidator.isValid("01/01/1500", SEASON_START_YEAR));
    }

    @Test
    void exactlyMinimumAgeAtSeasonStartIsValid() {
        int birthYear = SEASON_START_YEAR - ManagerBirthDateValidator.MIN_AGE_AT_SEASON_START;
        assertTrue(ManagerBirthDateValidator.isValid("01/01/" + birthYear, SEASON_START_YEAR));
    }
}
