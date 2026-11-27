package com.rndmodgames.futtoboru.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * E-00b + U-07: the shipped 1888-89 results (reference data, D17) give the published final table and the real FA
 * Cup ties. Headless: no libGDX application.
 */
class ReferenceResultsTests {

    private static GameData data;

    @BeforeAll
    static void load() {
        data = GameData.loadDefault();
    }

    /** Published 1888-89 table (Wikipedia, Steve's Football Stats): club, P W D L F A Pts. */
    private static final Object[][] PUBLISHED = {
            { "Preston North End", 22, 18, 4, 0, 74, 15, 40 },
            { "Aston Villa", 22, 12, 5, 5, 61, 43, 29 },
            { "Wolverhampton Wanderers", 22, 12, 4, 6, 51, 37, 28 },
            { "Blackburn Rovers", 22, 10, 6, 6, 66, 45, 26 },
            { "Bolton Wanderers", 22, 10, 2, 10, 63, 59, 22 },
            { "West Bromwich Albion", 22, 10, 2, 10, 40, 46, 22 },
            { "Accrington", 22, 6, 8, 8, 48, 48, 20 },
            { "Everton", 22, 9, 2, 11, 35, 47, 20 },
            { "Burnley", 22, 7, 3, 12, 42, 62, 17 },
            { "Derby County", 22, 7, 2, 13, 41, 61, 16 },
            { "Notts County", 22, 5, 2, 15, 40, 73, 12 },
            { "Stoke", 22, 4, 4, 14, 26, 51, 12 } };

    @Test
    void resultsLoadWithoutErrors() {
        assertEquals("", data.getReport().errorsText());
        assertEquals(170, data.records("result", "1888-89").size(), "132 league matches + 38 FA Cup rows");
        assertEquals(List.of("1888-89"), ReferenceResults.seasonsWithResults(data));
    }

    @Test
    void leagueTableIsThePublishedOne() {
        List<HistoryEntry> entries = ReferenceResults.entries(data, "1888-89");
        assertEquals(2, entries.size());
        HistoryEntry league = entries.get(0);
        assertFalse(league.cup);
        assertEquals("Football League", league.competition);
        assertEquals("1888-89", league.season);
        assertEquals(1888, league.startYear);
        assertEquals(HistoryEntry.Source.REFERENCE, league.source);
        assertEquals(12, league.table.size());
        for (int i = 0; i < PUBLISHED.length; i++) {
            Object[] p = PUBLISHED[i];
            HistoryEntry.TableRow r = league.table.get(i);
            String what = "row " + (i + 1);
            assertEquals(i + 1, r.position, what);
            assertEquals(p[0], r.club, what);
            assertEquals(List.of(p[1], p[2], p[3], p[4], p[5], p[6], p[7]),
                    List.of(r.played, r.won, r.drawn, r.lost, r.goalsFor, r.goalsAgainst, r.points), what);
        }
        assertEquals("Preston North End", league.winner);
        assertEquals("Aston Villa", league.runnerUp);
        assertTrue(league.ties.isEmpty());
    }

    @Test
    void cupHasEveryTieWalkoversAndTheFinal() {
        HistoryEntry cup = ReferenceResults.entries(data, "1888-89").get(1);
        assertTrue(cup.cup);
        assertEquals("FA Cup", cup.competition);
        assertEquals(38, cup.ties.size());
        assertEquals("Preston North End", cup.winner);
        assertEquals("Wolverhampton Wanderers", cup.runnerUp);

        assertEquals(2, cup.ties.stream().filter(t -> t.walkover != null).count());
        HistoryEntry.Tie linfield = cup.ties.stream().filter(t -> "Linfield".equals(t.away) && t.walkover != null)
                .findFirst().orElseThrow();
        assertEquals("Nottingham Forest", linfield.walkover);
        assertNull(linfield.homeGoals);
        assertNull(linfield.date);

        HistoryEntry.Tie fin = cup.ties.get(cup.ties.size() - 1);
        assertEquals("F", fin.round);
        assertEquals("1889-03-30", fin.date);
        assertEquals("Kennington Oval", fin.venue);
        assertEquals(Integer.valueOf(3), fin.homeGoals);
        assertEquals(Integer.valueOf(0), fin.awayGoals);
        // the renamed club comes out under its current id and name (witton-albion -> witton, aliases.json)
        assertTrue(cup.ties.stream().anyMatch(t -> "Witton".equals(t.away)));
    }
}
