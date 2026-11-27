package com.rndmodgames.futtoboru.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One competition in one season for the history screens (U-07): who won, who came second, and the final table
 * (league) or the ties (cup) when they are known. Built by {@link HistoryProvider}; plain data, no libGDX.
 */
public final class HistoryEntry {

    /** Where the entry comes from: the real results (D17 reference data) or a season played in this game. */
    public enum Source {
        REFERENCE, PLAYED
    }

    /** A row of a final league table. */
    public static final class TableRow {
        public final int position;
        public final String club;
        public final int played, won, drawn, lost, goalsFor, goalsAgainst, points;

        public TableRow(int position, String club, int played, int won, int drawn, int lost, int goalsFor,
                int goalsAgainst, int points) {
            this.position = position;
            this.club = club;
            this.played = played;
            this.won = won;
            this.drawn = drawn;
            this.lost = lost;
            this.goalsFor = goalsFor;
            this.goalsAgainst = goalsAgainst;
            this.points = points;
        }
    }

    /** A cup tie: a played match (both goals) or a walkover ({@link #walkover} = the club that went through). */
    public static final class Tie {
        public final String round;
        public final String date;
        public final String home, away;
        public final Integer homeGoals, awayGoals;
        public final String walkover;
        public final String venue;
        public final Integer attendance;

        public Tie(String round, String date, String home, String away, Integer homeGoals, Integer awayGoals,
                String walkover, String venue, Integer attendance) {
            this.round = round;
            this.date = date;
            this.home = home;
            this.away = away;
            this.homeGoals = homeGoals;
            this.awayGoals = awayGoals;
            this.walkover = walkover;
            this.venue = venue;
            this.attendance = attendance;
        }
    }

    public final String season;
    public final int startYear;
    public final String competition;
    public final boolean cup;
    public final Source source;
    public final String winner;
    public final String runnerUp;
    public final List<TableRow> table;
    public final List<Tie> ties;

    public HistoryEntry(String season, int startYear, String competition, boolean cup, Source source, String winner,
            String runnerUp, List<TableRow> table, List<Tie> ties) {
        this.season = season;
        this.startYear = startYear;
        this.competition = competition;
        this.cup = cup;
        this.source = source;
        this.winner = winner;
        this.runnerUp = runnerUp;
        this.table = Collections.unmodifiableList(new ArrayList<>(table));
        this.ties = Collections.unmodifiableList(new ArrayList<>(ties));
    }

    @Override
    public String toString() {
        return season + " " + competition + " (" + source + "): " + winner;
    }
}
