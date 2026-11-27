package com.rndmodgames.futtoboru.engine.temporal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * P2-11: the era calendar rules (dates generated, D17).
 */
class SeasonCalendarTests {

    final SeasonCalendar season = SeasonCalendar.forSeason(1888);

    @Test
    void aDateBelongsToTheSeasonFromAprilOn() {
        assertEquals(1888, SeasonCalendar.forDate(LocalDate.of(1888, 4, 1)).getStartYear(), "game start");
        assertEquals(1888, SeasonCalendar.forDate(LocalDate.of(1889, 2, 2)).getStartYear(), "mid-season");
        assertEquals(1889, SeasonCalendar.forDate(LocalDate.of(1889, 5, 1)).getStartYear(), "next cup staging");
    }

    @Test
    void leagueFromSeptemberToMidApril() {
        assertEquals(LocalDate.of(1888, 9, 8), season.leagueOpening());
        assertEquals(LocalDate.of(1889, 4, 20), season.leagueClosing());
        List<LocalDateTime> days = season.leagueMatchDays(22, LocalDate.of(1888, 4, 1));
        assertEquals(22, days.size());
        assertEquals(LocalDate.of(1888, 9, 8), days.get(0).toLocalDate());
        assertEquals(LocalDate.of(1889, 4, 20), days.get(21).toLocalDate());
        Set<LocalDate> cup = new HashSet<>(season.cupSaturdays());
        Set<LocalDate> distinct = new HashSet<>();
        for (LocalDateTime d : days) {
            assertEquals(DayOfWeek.SATURDAY, d.getDayOfWeek(), d.toString());
            assertFalse(cup.contains(d.toLocalDate()), "no league on a cup Saturday: " + d);
            assertTrue(distinct.add(d.toLocalDate()), "one round per day: " + d);
        }
    }

    @Test
    void aBigLeagueAddsMidweekDates() {
        List<LocalDateTime> days = season.leagueMatchDays(38, null);
        assertEquals(38, days.size());
        long midweek = days.stream().filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY).count();
        assertTrue(midweek > 0 && midweek < 38 / 2, "few midweek dates: " + midweek);
        assertTrue(new HashSet<>(days).size() == 38, "distinct days");
    }

    @Test
    void cupProperRoundsInFebruaryAndMarchFinalLastSaturdayOfMarch() {
        List<LocalDateTime> rounds = season.cupRoundDates(5);
        List<LocalDate> expected = List.of(LocalDate.of(1889, 2, 2), LocalDate.of(1889, 2, 16),
                LocalDate.of(1889, 3, 2), LocalDate.of(1889, 3, 16), LocalDate.of(1889, 3, 30));
        List<LocalDate> actual = new ArrayList<>();
        rounds.forEach(d -> actual.add(d.toLocalDate()));
        assertEquals(expected, actual);
        assertEquals(LocalDate.of(1889, 3, 30), season.cupFinal());

        // a 64-club bracket: the extra first round in midweek, January
        List<LocalDateTime> six = season.cupRoundDates(6);
        assertEquals(DayOfWeek.WEDNESDAY, six.get(0).getDayOfWeek());
        assertEquals(LocalDate.of(1889, 1, 30), six.get(0).toLocalDate());
        assertEquals(LocalDate.of(1889, 2, 2), six.get(1).toLocalDate());
    }

    @Test
    void replayTheFollowingSaturdayElseMidweek() {
        SaveGame game = new SaveGame();
        game.setAllClubs(new ArrayList<>());
        Club a = club(game, 1L);
        Club b = club(game, 2L);
        Match tie = match(10L, 1L, 2L, LocalDate.of(1889, 2, 2));
        a.getPlayedMatches().add(tie);
        b.getPlayedMatches().add(tie);
        LocalDate next = LocalDate.of(1889, 2, 16);

        assertEquals(LocalDate.of(1889, 2, 9),
                SeasonCalendar.replayDate(game, tie, LocalDate.of(1889, 2, 2), next).toLocalDate(), "both free");

        // b has a league match the following Saturday: the replay goes midweek (Wednesday)
        Match league = match(11L, 2L, 3L, LocalDate.of(1889, 2, 9));
        b.getScheduledMatches().add(league);
        LocalDateTime replay = SeasonCalendar.replayDate(game, tie, LocalDate.of(1889, 2, 2), next);
        assertEquals(LocalDate.of(1889, 2, 6), replay.toLocalDate());
        assertEquals(DayOfWeek.WEDNESDAY, replay.getDayOfWeek());
    }

    private static Club club(SaveGame game, Long id) {
        Club c = new Club();
        c.setId(id);
        c.setScheduledMatches(new ArrayList<>());
        c.setPlayedMatches(new ArrayList<>());
        game.getAllClubs().add(c);
        return c;
    }

    private static Match match(Long id, Long home, Long away, LocalDate day) {
        Match m = new Match();
        m.setId(id);
        m.setHomeClubId(home);
        m.setAwayClubId(away);
        m.setMatchDateTime(day.atTime(SeasonCalendar.KICK_OFF));
        return m;
    }
}
