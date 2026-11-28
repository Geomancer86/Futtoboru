package com.rndmodgames.futtoboru.history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * Everything the history screens show (U-07), newest season first:
 * <ul>
 * <li>seasons <b>before</b> the game's start season, from the real results (E-00b, reference data, D17);</li>
 * <li>every season played in this game: the league's final table ({@link CompetitionEdition#getFinalStandings()})
 * and the cup's winner and runner-up.</li>
 * </ul>
 * The season the game starts in is never shown from the real results: it is being simulated.
 * Headless: needs no libGDX application.
 */
public final class HistoryProvider {

    private static final Pattern SEASON = Pattern.compile("(\\d{4})-(\\d{2})");

    private HistoryProvider() {
    }

    /** @return the history of a game (null-safe: an empty list when there is no game or data) */
    public static List<HistoryEntry> forGame(SaveGame game, GameData data) {
        if (game == null) {
            return Collections.emptyList();
        }
        List<League> leagues = game.getMainAuthority() != null ? game.getMainAuthority().getLeagues() : null;
        List<Club> clubs = game.getAllClubs();
        Function<Long, String> clubName = id -> {
            if (id == null || clubs == null) {
                return null;
            }
            for (Club c : clubs) {
                if (id.equals(c.getId())) {
                    return c.getName();
                }
            }
            return null;
        };
        return build(data, game.getGameStartDate(), leagues, game.getAllCups(), clubName);
    }

    /**
     * @param data      the game data (reference results), or null for none
     * @param gameStart the date the game started; reference seasons that start before it are history
     * @param leagues   the game's leagues (their editions' final tables), or null
     * @param cups      the game's cups (their editions' winners), or null
     * @param clubName  club runtime id -> name
     */
    public static List<HistoryEntry> build(GameData data, LocalDateTime gameStart, List<League> leagues,
            List<Competition> cups, Function<Long, String> clubName) {
        List<HistoryEntry> out = new ArrayList<>();
        if (data != null) {
            for (String season : ReferenceResults.seasonsWithResults(data)) {
                if (isBeforeStart(ReferenceResults.startDate(data, season), gameStart)) {
                    out.addAll(ReferenceResults.entries(data, season));
                }
            }
        }
        if (leagues != null) {
            for (League league : leagues) {
                for (CompetitionEdition ed : safe(league.getEditions())) {
                    if (ed != null && !ed.getFinalStandings().isEmpty()) {
                        out.add(playedLeague(league.getName(), ed, clubName));
                    }
                }
            }
        }
        if (cups != null) {
            for (Competition cup : cups) {
                for (CompetitionEdition ed : safe(cup.getEditions())) {
                    if (ed != null && ed.getChampionsId() != null) {
                        out.add(new HistoryEntry(season(ed), startYear(ed), cup.getName(), true,
                                HistoryEntry.Source.PLAYED, name(clubName, ed.getChampionsId()),
                                name(clubName, ed.getRunnersUpId()), new ArrayList<>(), new ArrayList<>()));
                    }
                }
            }
        }
        // newest first; in a season the league before the cup; stable for equal keys
        out.sort(Comparator.<HistoryEntry>comparingInt(e -> -e.startYear).thenComparing(e -> e.cup));
        return out;
    }

    /** @return the entries that have a winner, newest first (the champions and cup winners list) */
    public static List<HistoryEntry> winners(List<HistoryEntry> all) {
        List<HistoryEntry> out = new ArrayList<>();
        for (HistoryEntry e : all) {
            if (e.winner != null) {
                out.add(e);
            }
        }
        return out;
    }

    static boolean isBeforeStart(String seasonStart, LocalDateTime gameStart) {
        if (seasonStart == null || gameStart == null) {
            return false;
        }
        try {
            return LocalDateTime.parse(seasonStart).isBefore(gameStart);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static HistoryEntry playedLeague(String leagueName, CompetitionEdition ed, Function<Long, String> clubName) {
        List<HistoryEntry.TableRow> table = new ArrayList<>();
        for (LeagueStandingRow r : ed.getFinalStandings()) {
            table.add(new HistoryEntry.TableRow(n(r.getPosition()), name(clubName, r.getClubId()), n(r.getPlayed()),
                    n(r.getWon()), n(r.getDrawn()), n(r.getLost()), n(r.getGoalsFor()), n(r.getGoalsAgainst()),
                    n(r.getPoints())));
        }
        String winner = ed.getChampionsId() != null ? name(clubName, ed.getChampionsId())
                : table.isEmpty() ? null : table.get(0).club;
        String runnerUp = ed.getRunnersUpId() != null ? name(clubName, ed.getRunnersUpId())
                : table.size() < 2 ? null : table.get(1).club;
        return new HistoryEntry(season(ed), startYear(ed), leagueName, false, HistoryEntry.Source.PLAYED, winner,
                runnerUp, table, new ArrayList<>());
    }

    /** "1888-89" from the edition's name ("Football League 1889-90"), else from its start year (Authorities, U-12) */
    public static String season(CompetitionEdition ed) {
        Matcher m = ed.getName() != null ? SEASON.matcher(ed.getName()) : null;
        if (m != null && m.find()) {
            return m.group();
        }
        int y = startYear(ed);
        return y + "-" + String.format(java.util.Locale.ROOT, "%02d", (y + 1) % 100);
    }

    static int startYear(CompetitionEdition ed) {
        Matcher m = ed.getName() != null ? SEASON.matcher(ed.getName()) : null;
        if (m != null && m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return ed.getStartDate() != null ? ed.getStartDate().getYear() : 0;
    }

    private static String name(Function<Long, String> clubName, Long id) {
        if (id == null) {
            return null;
        }
        String n = clubName.apply(id);
        return n != null ? n : "#" + id;
    }

    private static int n(Integer v) {
        return v == null ? 0 : v;
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
