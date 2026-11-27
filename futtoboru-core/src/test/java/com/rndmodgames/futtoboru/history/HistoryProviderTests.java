package com.rndmodgames.futtoboru.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * U-07: what the history screen lists. The real results only for seasons before the game's start season (D17);
 * played seasons from the editions' final tables and cup winners. Headless.
 */
class HistoryProviderTests {

    private static final LocalDateTime START_1888 = LocalDateTime.of(1888, 4, 1, 4, 0);
    private static final LocalDateTime START_1889 = LocalDateTime.of(1889, 4, 1, 4, 0);

    private static GameData data;
    private final Map<Long, String> names = new HashMap<>();

    @BeforeAll
    static void load() {
        data = GameData.loadDefault();
    }

    @Test
    void startSeasonIsNeverShownFromTheRealResults() {
        assertTrue(HistoryProvider.build(data, START_1888, null, null, names::get).isEmpty(),
                "a game starting in 1888-89 simulates that season: no real 1888-89 table in its history");
    }

    @Test
    void realSeasonsBeforeTheStartAreHistory() {
        List<HistoryEntry> all = HistoryProvider.build(data, START_1889, null, null, names::get);
        assertEquals(2, all.size());
        assertEquals("Football League", all.get(0).competition);
        assertEquals("FA Cup", all.get(1).competition);
        assertTrue(all.stream().allMatch(e -> e.source == HistoryEntry.Source.REFERENCE && "1888-89".equals(e.season)));
        assertEquals("Preston North End", all.get(0).winner);
        assertEquals("Preston North End", all.get(1).winner);
    }

    @Test
    void playedSeasonsComeFromFinalStandingsAndCupWinnersNewestFirst() {
        Club a = club(1_000_001L, "Alpha"), b = club(1_000_002L, "Beta");

        League league = new League();
        league.setName("Football League");
        league.getEditions().add(leagueEdition("Football League 1888-89", a, b));
        league.getEditions().add(leagueEdition("Football League 1889-90", b, a));
        CompetitionEdition running = new CompetitionEdition(); // no final table yet: not history
        running.setName("Football League 1890-91");
        league.getEditions().add(running);

        Competition cup = new Competition();
        cup.setName("FA Cup");
        CompetitionEdition cupEd = new CompetitionEdition();
        cupEd.setName("1888-89 FA Cup");
        cupEd.setChampionsId(b.getId());
        cupEd.setRunnersUpId(a.getId());
        cup.getEditions().add(cupEd);
        CompetitionEdition cupRunning = new CompetitionEdition(); // no winner yet: not history
        cupRunning.setName("FA Cup 1889-90");
        cup.getEditions().add(cupRunning);

        List<HistoryEntry> all = HistoryProvider.build(data, START_1888, List.of(league), List.of(cup), names::get);

        assertEquals(3, all.size());
        HistoryEntry newest = all.get(0);
        assertEquals("1889-90", newest.season);
        assertEquals(HistoryEntry.Source.PLAYED, newest.source);
        assertEquals("Beta", newest.winner);
        assertEquals("Alpha", newest.runnerUp);
        assertEquals(2, newest.table.size());
        assertEquals("Beta", newest.table.get(0).club);
        assertEquals(3, newest.table.get(0).points);

        assertEquals("1888-89", all.get(1).season);
        assertEquals("Football League", all.get(1).competition, "in a season the league comes before the cup");
        assertEquals("Alpha", all.get(1).winner);
        HistoryEntry cupEntry = all.get(2);
        assertTrue(cupEntry.cup);
        assertEquals("Beta", cupEntry.winner);
        assertEquals("Alpha", cupEntry.runnerUp);

        assertEquals(3, HistoryProvider.winners(all).size());
    }

    @Test
    void realAndPlayedSeasonsTogether() {
        Club a = club(1_000_001L, "Alpha"), b = club(1_000_002L, "Beta");
        League league = new League();
        league.setName("Football League");
        league.getEditions().add(leagueEdition("Football League 1889-90", a, b));

        List<HistoryEntry> all = HistoryProvider.build(data, START_1889, List.of(league), new ArrayList<>(), names::get);

        assertEquals(List.of("1889-90", "1888-89", "1888-89"),
                all.stream().map(e -> e.season).collect(Collectors.toList()));
        assertEquals(HistoryEntry.Source.PLAYED, all.get(0).source);
        assertEquals(HistoryEntry.Source.REFERENCE, all.get(1).source);
    }

    @Test
    void missingClubNameAndNoGameAreSafe() {
        assertTrue(HistoryProvider.forGame(null, data).isEmpty());
        League league = new League();
        league.setName("L");
        league.getEditions().add(leagueEdition("L 1890-91", club(7L, null), club(8L, null)));
        HistoryEntry e = HistoryProvider.build(null, START_1888, List.of(league), null, id -> null).get(0);
        assertEquals("#7", e.winner);
    }

    private Club club(long id, String name) {
        Club c = new Club(name);
        c.setId(id);
        if (name != null) {
            names.put(id, name);
        }
        return c;
    }

    /** A finished two-club edition: the first club 1-1-0 (3 points at 2-1-0), the second 0-1-1. */
    private static CompetitionEdition leagueEdition(String name, Club first, Club second) {
        stats(first, 1, 1, 0, 3, 1, 3);
        stats(second, 0, 1, 1, 1, 3, 1);
        CompetitionEdition ed = new CompetitionEdition();
        ed.setName(name);
        List<LeagueStandingRow> table = new ArrayList<>();
        table.add(LeagueStandingRow.of(1, first));
        table.add(LeagueStandingRow.of(2, second));
        ed.setFinalStandings(table);
        ed.setChampionsId(first.getId());
        ed.setRunnersUpId(second.getId());
        return ed;
    }

    private static void stats(Club c, int w, int d, int l, int gf, int ga, int pts) {
        c.setMatchesPlayed(w + d + l);
        c.setMatchesWon(w);
        c.setMatchesDrawn(d);
        c.setMatchesLost(l);
        c.setGoalsScored(gf);
        c.setGoalsConceded(ga);
        c.setPoints(pts);
    }
}
