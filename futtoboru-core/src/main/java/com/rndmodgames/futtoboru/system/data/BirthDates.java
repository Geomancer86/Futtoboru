package com.rndmodgames.futtoboru.system.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Random;

/**
 * Birth dates in the data are partial: {@code yyyy}, {@code yyyy-mm} or {@code yyyy-mm-dd}. The game needs a whole
 * date, so the missing month and/or day are drawn from the random generator passed in (seed it to repeat a game).
 */
public final class BirthDates {

    private BirthDates() {
    }

    /**
     * @param born a date as validated by {@link GameData} ({@code yyyy}, {@code yyyy-mm} or {@code yyyy-mm-dd})
     * @param rng  draws the missing month (1-12) and day (within that month)
     * @return the birth date at the start of its day
     */
    public static LocalDateTime complete(String born, Random rng) {
        String[] parts = born.trim().split("-");
        int year = Integer.parseInt(parts[0]);
        int month = parts.length >= 2 ? Integer.parseInt(parts[1]) : rng.nextInt(12) + 1;
        int length = YearMonth.of(year, month).lengthOfMonth();
        int day = parts.length >= 3 ? Math.min(Integer.parseInt(parts[2]), length) : rng.nextInt(length) + 1;
        return LocalDate.of(year, month, day).atStartOfDay();
    }
}
