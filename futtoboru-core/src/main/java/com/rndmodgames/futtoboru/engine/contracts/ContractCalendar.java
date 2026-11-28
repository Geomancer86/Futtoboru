package com.rndmodgames.futtoboru.engine.contracts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;

/**
 * G-04: the dates of a one-season engagement (docs/data/history/RECRUITMENT_1888-1900.md 4.1-4.2, decision D28 Q8).
 * An engagement for season {@code S} (1888 = 1888-89) ends on 30 April of {@code S + 1}; the next one starts on
 * 1 May. The weeks from May to August are the close season, paid only to men with summer pay; September to April is
 * the playing season, paid at the weekly wage. The retained list opens two weeks before the season end.
 *
 * Retain and transfer (a club keeps a man's registration after his engagement ends) starts in 1893-94: before it a
 * man not re-engaged is free to join anyone ({@link #RETAIN_AND_TRANSFER_FROM}, sourced in the same note, section 3).
 */
public final class ContractCalendar {

    /** D28 Q8: the season's engagements end on 30 April */
    public static final MonthDay SEASON_END = MonthDay.of(Month.APRIL, 30);
    /** The close season: May to August (summer pay) */
    public static final Month CLOSE_SEASON_FROM = Month.MAY;
    public static final Month CLOSE_SEASON_TO = Month.AUGUST;
    /** Days before the season end the retained list opens (16 April) */
    public static final int RETAINED_LIST_DAYS = 14;
    /** Playing-season and close-season weeks of an engagement (35 + 17 = 52) */
    public static final int SEASON_WEEKS = 35;
    public static final int SUMMER_WEEKS = 17;
    /** The first season of retain and transfer; not modelled yet (no season after 1888-89 has data) */
    public static final int RETAIN_AND_TRANSFER_FROM = 1893;

    private ContractCalendar() {
    }

    /** @return the last moment of the engagements of season {@code season} (30 April of the next year, 23:59:59) */
    public static LocalDateTime seasonEnd(int season) {
        return SEASON_END.atYear(season + 1).atTime(23, 59, 59);
    }

    /** @return the first day of the next season's engagements (1 May of the next year, 00:00) */
    public static LocalDateTime nextSeasonStart(int season) {
        return SEASON_END.atYear(season + 1).plusDays(1).atStartOfDay();
    }

    /** @return the day the retained list of season {@code season} opens (16 April of the next year) */
    public static LocalDate retainedListOpens(int season) {
        return SEASON_END.atYear(season + 1).minusDays(RETAINED_LIST_DAYS);
    }

    /** @return the season whose engagements are running on {@code day}: 1 May 1888 - 30 April 1889 = 1888 */
    public static int seasonOf(LocalDate day) {
        return day.isAfter(SEASON_END.atYear(day.getYear())) ? day.getYear() : day.getYear() - 1;
    }

    /** @return true from May to August, when only men with summer pay are paid */
    public static boolean isCloseSeason(LocalDate day) {
        int m = day.getMonthValue();
        return m >= CLOSE_SEASON_FROM.getValue() && m <= CLOSE_SEASON_TO.getValue();
    }

    /** "1889-90" for 1889 */
    public static String seasonName(int season) {
        return season + "-" + String.format(java.util.Locale.ROOT, "%02d", (season + 1) % 100);
    }
}
