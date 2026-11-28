package com.rndmodgames.futtoboru.engine.temporal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import com.badlogic.gdx.utils.JsonValue;

/**
 * P2-14 (D30 A9): the rules of a season's calendar as data, read from the optional {@code calendar} block of
 * {@code seasons/<season>/season.json} (format in docs/DATA_MODEL.md, checked by {@code tools/data/validate.py} and
 * {@code GameData}). {@link SeasonCalendar} turns them into dates for one season.
 *
 * <p>A season without a calendar keeps the rules of the latest earlier season that has one (the same rules one year
 * on); before any, the code defaults {@link #DEFAULTS}, which are the 1888-89 rules (P2-11). The real 1888-89
 * fixture dates are never read here (D17): these are rules, the schedule is generated from them.
 */
public final class CalendarRules {

    /** A date of a season: the first {@code weekday} on or after a month-day, or the last one on or before it. */
    public static final class DayRule {
        final MonthDay anchor;
        final boolean onOrAfter;
        final DayOfWeek weekday;

        public DayRule(MonthDay anchor, boolean onOrAfter, DayOfWeek weekday) {
            this.anchor = anchor;
            this.onOrAfter = onOrAfter;
            this.weekday = weekday;
        }

        /** The date in the season starting in {@code startYear} (July to December that year, else the next). */
        public LocalDate in(int startYear) {
            LocalDate day = anchor.atYear(yearOf(anchor.getMonth(), startYear));
            return day.with(onOrAfter ? TemporalAdjusters.nextOrSame(weekday) : TemporalAdjusters.previousOrSame(weekday));
        }

        @Override
        public String toString() {
            return weekday + (onOrAfter ? " on or after " : " on or before ") + anchor;
        }
    }

    /** The calendar of one cup competition. */
    public static final class Cup {
        final String competition;
        final DayRule opens;
        final DayRule firstDraw;
        final boolean openDraw;
        final int drawDaysAfterRound;
        final DayRule finalDay;
        final MonthDay properFrom;
        final int daysBetweenRounds;
        final DayOfWeek earlyRoundsOn;
        final int maxEarlyRounds;

        Cup(String competition, DayRule opens, DayRule firstDraw, boolean openDraw, int drawDaysAfterRound,
                DayRule finalDay, MonthDay properFrom, int daysBetweenRounds, DayOfWeek earlyRoundsOn,
                int maxEarlyRounds) {
            this.competition = competition;
            this.opens = opens;
            this.firstDraw = firstDraw;
            this.openDraw = openDraw;
            this.drawDaysAfterRound = drawDaysAfterRound;
            this.finalDay = finalDay;
            this.properFrom = properFrom;
            this.daysBetweenRounds = daysBetweenRounds;
            this.earlyRoundsOn = earlyRoundsOn;
            this.maxEarlyRounds = maxEarlyRounds;
        }

        public String getCompetition() {
            return competition;
        }

        /** Each round drawn after the round before is decided (true), or paired by the first draw's bracket */
        public boolean isOpenDraw() {
            return openDraw;
        }

        public int getDrawDaysAfterRound() {
            return drawDaysAfterRound;
        }
    }

    /** A date from this month on belongs to the season that starts in its year */
    static final Month SEASON_TURNS = Month.APRIL;

    /**
     * A date rule's month-day from this month on falls in the season's first year, before it in the next: the
     * league opens in September 1888 and closes in April 1889, the cup opens in October 1888, is drawn in January
     * and ends in March 1889.
     */
    static final Month RULES_YEAR_TURNS = Month.JULY;

    /**
     * The 1888-89 rules (P2-11), for a game without data or a season before any calendar: league Saturdays from 8 Sep
     * to 20 Apr, Wednesdays when they run out, none on Christmas Day; the FA Cup opens with the qualifying rounds in
     * October, its proper rounds are drawn from January (placeholder), fortnightly Saturdays back from the last
     * Saturday of March from February on, extra rounds on Wednesdays (at most 8), each round drawn after the last.
     */
    public static final CalendarRules DEFAULTS = new CalendarRules(
            new DayRule(MonthDay.of(Month.SEPTEMBER, 8), true, DayOfWeek.SATURDAY),
            new DayRule(MonthDay.of(Month.APRIL, 20), false, DayOfWeek.SATURDAY),
            DayOfWeek.WEDNESDAY, List.of(MonthDay.of(Month.DECEMBER, 25)),
            List.of(new Cup("fa-cup", new DayRule(MonthDay.of(Month.OCTOBER, 1), true, DayOfWeek.SATURDAY),
                    new DayRule(MonthDay.of(Month.JANUARY, 1), true, DayOfWeek.MONDAY), true, 2,
                    new DayRule(MonthDay.of(Month.MARCH, 31), false, DayOfWeek.SATURDAY),
                    MonthDay.of(Month.FEBRUARY, 1), 14, DayOfWeek.WEDNESDAY, 8)));

    final DayRule leagueOpens;
    final DayRule leagueCloses;
    final DayOfWeek midweek;
    final List<MonthDay> noLeagueOn;
    /** Competition data id -> its cup calendar, in data order */
    final Map<String, Cup> cups;

    CalendarRules(DayRule leagueOpens, DayRule leagueCloses, DayOfWeek midweek, List<MonthDay> noLeagueOn,
            List<Cup> cupList) {
        this.leagueOpens = leagueOpens;
        this.leagueCloses = leagueCloses;
        this.midweek = midweek;
        this.noLeagueOn = Collections.unmodifiableList(new ArrayList<>(noLeagueOn));
        Map<String, Cup> m = new LinkedHashMap<>();
        for (Cup c : cupList) {
            m.put(c.competition, c);
        }
        this.cups = Collections.unmodifiableMap(m);
    }

    /** The cup of a competition (data id), else the season's first cup, else the default FA Cup calendar */
    public Cup cup(String competitionDataId) {
        if (competitionDataId != null && cups.containsKey(competitionDataId)) {
            return cups.get(competitionDataId);
        }
        if (!cups.isEmpty()) {
            return cups.values().iterator().next();
        }
        return DEFAULTS.cups.values().iterator().next();
    }

    /** @return the year a date rule's month falls in for the season starting in {@code startYear} (July-June) */
    static int yearOf(Month month, int startYear) {
        return month.getValue() >= RULES_YEAR_TURNS.getValue() ? startYear : startYear + 1;
    }

    // ------------------------------------------------------------------------------------------- registry

    /** Season start year -> its rules, from the game data (DatabaseLoader installs them at load) */
    private static volatile NavigableMap<Integer, CalendarRules> bySeason = new TreeMap<>();

    /** Replaces the rules read from the game data (season start year -> rules); an empty map = code defaults. */
    public static void install(Map<Integer, CalendarRules> rules) {
        bySeason = new TreeMap<>(rules);
    }

    /** The rules installed now (tests restore them after installing their own) */
    static Map<Integer, CalendarRules> installed() {
        return new TreeMap<>(bySeason);
    }

    /** The rules of the season starting in {@code startYear}: its own, else the latest earlier season's, else defaults */
    public static CalendarRules forSeason(int startYear) {
        Map.Entry<Integer, CalendarRules> e = bySeason.floorEntry(startYear);
        return e != null ? e.getValue() : DEFAULTS;
    }

    // ------------------------------------------------------------------------------------------- reading

    /**
     * Reads a {@code calendar} block (already checked by GameData). A part the block leaves out keeps the default.
     *
     * @param competitionRef the canonical competition id of {@code cups[i]} (references resolve through aliases)
     */
    public static CalendarRules read(JsonValue calendar, Function<Integer, String> competitionRef) {
        if (calendar == null) {
            return DEFAULTS;
        }
        DayRule opens = DEFAULTS.leagueOpens;
        DayRule closes = DEFAULTS.leagueCloses;
        DayOfWeek midweek = DEFAULTS.midweek;
        List<MonthDay> noMatch = DEFAULTS.noLeagueOn;
        JsonValue league = calendar.get("league");
        if (league != null) {
            opens = dayRule(league.get("opens"), opens);
            closes = dayRule(league.get("closes"), closes);
            midweek = weekday(league.getString("midweek", null), midweek);
            JsonValue days = league.get("noMatchOn");
            if (days != null) {
                noMatch = new ArrayList<>();
                for (JsonValue d = days.child; d != null; d = d.next) {
                    noMatch.add(monthDay(d.asString()));
                }
            }
        }
        List<Cup> cups = new ArrayList<>();
        JsonValue list = calendar.get("cups");
        int i = 0;
        for (JsonValue c = list == null ? null : list.child; c != null; c = c.next, i++) {
            String competition = competitionRef.apply(i);
            if (competition == null) {
                continue; // an unknown competition (reported by GameData)
            }
            Cup d = DEFAULTS.cup(null);
            String early = c.getString("earlyRoundsOn", null);
            cups.add(new Cup(competition, dayRule(c.get("opens"), d.opens), dayRule(c.get("firstDraw"), d.firstDraw),
                    "open-draw".equals(c.getString("laterDraws", "open-draw")),
                    c.getInt("drawDaysAfterRound", d.drawDaysAfterRound), dayRule(c.get("final"), d.finalDay),
                    c.has("properFrom") ? monthDay(c.getString("properFrom")) : d.properFrom,
                    c.getInt("daysBetweenRounds", d.daysBetweenRounds), weekday(early, d.earlyRoundsOn),
                    c.getInt("maxEarlyRounds", d.maxEarlyRounds)));
        }
        if (list == null) {
            cups.addAll(DEFAULTS.cups.values());
        }
        return new CalendarRules(opens, closes, midweek, noMatch, cups);
    }

    private static DayRule dayRule(JsonValue v, DayRule def) {
        if (v == null) {
            return def;
        }
        boolean after = v.has("onOrAfter");
        return new DayRule(monthDay(v.getString(after ? "onOrAfter" : "onOrBefore")), after,
                weekday(v.getString("weekday", null), def.weekday));
    }

    private static DayOfWeek weekday(String name, DayOfWeek def) {
        return name == null ? def : DayOfWeek.valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static MonthDay monthDay(String mmdd) {
        return MonthDay.of(Integer.parseInt(mmdd.substring(0, 2)), Integer.parseInt(mmdd.substring(3)));
    }
}
