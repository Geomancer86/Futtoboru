package com.rndmodgames.futtoboru.history;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * The real results of a season ({@code seasons/<id>/results.json}, E-00b) turned into history entries: the final
 * league table recomputed from the matches, and the cup ties with winner and runner-up.
 *
 * <p><b>Reference data only (decision D17).</b> This is read by the history screens and by calibration tests,
 * never to schedule, script or decide a match the player plays. {@code ReferenceDataGuardTest} fails when code
 * outside the {@code history} package reads the {@code result} records.
 */
public final class ReferenceResults {

    /** Points for a win and a draw. 2-1-0 from 1888-89 until 1981; change per season when a later season lands. */
    static final int WIN = 2, DRAW = 1;

    private ReferenceResults() {
    }

    /** @return the seasons of the data that have real results, in load order */
    public static List<String> seasonsWithResults(GameData data) {
        List<String> out = new ArrayList<>();
        for (DataRecord r : data.records("result", null)) {
            if (r.season() != null && !out.contains(r.season())) {
                out.add(r.season());
            }
        }
        return out;
    }

    /** @return one entry per edition of the season that has results (league first, as in season.json) */
    public static List<HistoryEntry> entries(GameData data, String season) {
        Map<String, List<DataRecord>> byEdition = new LinkedHashMap<>();
        for (DataRecord r : data.records("result", season)) {
            byEdition.computeIfAbsent(r.ref("edition"), k -> new ArrayList<>()).add(r);
        }
        List<HistoryEntry> out = new ArrayList<>();
        int startYear = startYear(data, season);
        for (Map.Entry<String, List<DataRecord>> e : byEdition.entrySet()) {
            DataRecord edition = data.get(GameData.EDITION, e.getKey());
            DataRecord competition = edition != null ? data.get(GameData.COMPETITION, edition.ref("competition")) : null;
            String name = competition != null ? competition.str("name") : e.getKey();
            boolean cup = competition != null && "CUP".equals(competition.str("type"));
            out.add(cup ? cup(data, season, startYear, name, e.getValue())
                    : league(data, season, startYear, name, e.getValue()));
        }
        return out;
    }

    /** The final table recomputed from the matches: points, then goal average (goals for / against), then name. */
    static HistoryEntry league(GameData data, String season, int startYear, String name, List<DataRecord> matches) {
        Map<String, int[]> totals = new LinkedHashMap<>(); // club -> P W D L F A Pts
        for (DataRecord m : matches) {
            Integer hg = m.integer("homeGoals"), ag = m.integer("awayGoals");
            if (hg == null || ag == null) {
                continue; // a league walkover would not count as played; 1888-89 has none
            }
            add(totals.computeIfAbsent(m.ref("home"), k -> new int[7]), hg, ag);
            add(totals.computeIfAbsent(m.ref("away"), k -> new int[7]), ag, hg);
        }
        List<String> order = new ArrayList<>(totals.keySet());
        order.sort(Comparator.<String>comparingInt(c -> -totals.get(c)[6])
                .thenComparing(c -> -goalAverage(totals.get(c)))
                .thenComparing(c -> clubName(data, c)));
        List<HistoryEntry.TableRow> table = new ArrayList<>();
        for (String c : order) {
            int[] t = totals.get(c);
            table.add(new HistoryEntry.TableRow(table.size() + 1, clubName(data, c), t[0], t[1], t[2], t[3], t[4], t[5],
                    t[6]));
        }
        String winner = table.isEmpty() ? null : table.get(0).club;
        String runnerUp = table.size() < 2 ? null : table.get(1).club;
        return new HistoryEntry(season, startYear, name, false, HistoryEntry.Source.REFERENCE, winner, runnerUp, table,
                new ArrayList<>());
    }

    /** The ties in file order; the winner is the winner of the last tie of the final round (replays included). */
    static HistoryEntry cup(GameData data, String season, int startYear, String name, List<DataRecord> matches) {
        List<HistoryEntry.Tie> ties = new ArrayList<>();
        HistoryEntry.Tie lastFinal = null;
        for (DataRecord m : matches) {
            String walkover = m.ref("walkover");
            HistoryEntry.Tie tie = new HistoryEntry.Tie(m.str("round", ""), m.str("date"), clubName(data, m.ref("home")),
                    clubName(data, m.ref("away")), m.integer("homeGoals"), m.integer("awayGoals"),
                    walkover != null ? clubName(data, walkover) : null, m.str("venue"), m.integer("attendance"));
            ties.add(tie);
            if (tie.round.startsWith("F") && tie.homeGoals != null && tie.awayGoals != null) {
                lastFinal = tie;
            }
        }
        String winner = null, runnerUp = null;
        if (lastFinal != null && !lastFinal.homeGoals.equals(lastFinal.awayGoals)) {
            boolean home = lastFinal.homeGoals > lastFinal.awayGoals;
            winner = home ? lastFinal.home : lastFinal.away;
            runnerUp = home ? lastFinal.away : lastFinal.home;
        }
        return new HistoryEntry(season, startYear, name, true, HistoryEntry.Source.REFERENCE, winner, runnerUp,
                new ArrayList<>(), ties);
    }

    private static void add(int[] t, int gf, int ga) {
        t[0]++;
        t[gf > ga ? 1 : gf == ga ? 2 : 3]++;
        t[4] += gf;
        t[5] += ga;
        t[6] += gf > ga ? WIN : gf == ga ? DRAW : 0;
    }

    private static double goalAverage(int[] t) {
        return t[5] == 0 ? Double.MAX_VALUE : (double) t[4] / t[5];
    }

    static String clubName(GameData data, String clubId) {
        DataRecord club = clubId != null ? data.get(GameData.CLUB, clubId) : null;
        return club != null ? club.str("name") : String.valueOf(clubId);
    }

    /** @return the year the season starts (from season.json {@code start}, else the folder name) */
    static int startYear(GameData data, String season) {
        DataRecord s = data.get(GameData.SEASON, season);
        String start = s != null ? s.str("start") : null;
        String text = start != null ? start : season;
        try {
            return Integer.parseInt(text.substring(0, 4));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /** @return the season's start date-time text from season.json ({@code yyyy-mm-ddThh:mm:ss}), or null */
    static String startDate(GameData data, String season) {
        DataRecord s = data.get(GameData.SEASON, season);
        return s != null ? s.str("start") : null;
    }
}
