package com.rndmodgames.futtoboru.engine.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;

class SimulationHealthMonitorTest {

    @Test
    void detectsTbdMatchInsideTicketWindow() {
        SaveGame save = new SaveGame();
        save.setGameDate(LocalDateTime.of(1888, 9, 1, 12, 0));

        Club club = new Club("Home");
        club.setId(1L);
        Match tbd = new Match();
        tbd.setId(100L);
        tbd.setHomeClubId(null);
        tbd.setAwayClubId(2L);
        tbd.setMatchDateTime(LocalDateTime.of(1888, 9, 5, 15, 0));
        club.getScheduledMatches().add(tbd);
        save.getAllClubs().add(club);

        SimulationHealthMonitor.HealthReport report = SimulationHealthMonitor.scan(save);
        assertEquals(1, report.getTbdMatchesInTicketWindow());
        assertTrue(report.getIssues().isEmpty());
    }

    @Test
    void flagsCupSemiFinalRoundWithWrongMatchCount() {
        SaveGame save = new SaveGame();
        save.setGameDate(LocalDateTime.of(1888, 12, 1, 12, 0));

        Competition cup = new Competition();
        cup.setId(99L);
        CompetitionEdition edition = new CompetitionEdition();
        edition.setId(10L);
        edition.setChampionsId(1L);
        cup.getEditions().add(edition);
        save.getAllCups().add(cup);

        Club club = new Club("Cup holder");
        club.setId(1L);
        List<Match> scheduled = new ArrayList<>();
        scheduled.add(cupMatch(1L, 10L, 2));
        scheduled.add(cupMatch(2L, 10L, 2));
        scheduled.add(cupMatch(3L, 10L, 2));
        scheduled.add(cupMatch(4L, 10L, 3));
        scheduled.get(0).setIsPlayed(true);
        club.setScheduledMatches(scheduled);
        save.getAllClubs().add(club);

        SimulationHealthMonitor.HealthReport report = SimulationHealthMonitor.scan(save, true);
        assertEquals(1, report.getIssues().size());
        assertTrue(report.getIssues().get(0).contains("3 matches"));
    }

    @Test
    void ignoresCupTbdPlaceholdersInTicketWindow() {
        SaveGame save = new SaveGame();
        save.setGameDate(LocalDateTime.of(1888, 9, 1, 12, 0));

        Club club = new Club("Home");
        club.setId(1L);
        Match tbdCup = new Match();
        tbdCup.setId(101L);
        tbdCup.setMatchType(Match.CUP_MATCH);
        tbdCup.setHomeClubId(null);
        tbdCup.setAwayClubId(null);
        tbdCup.setMatchDateTime(LocalDateTime.of(1888, 9, 5, 15, 0));
        club.getScheduledMatches().add(tbdCup);
        save.getAllClubs().add(club);

        SimulationHealthMonitor.HealthReport report = SimulationHealthMonitor.scan(save);
        assertEquals(0, report.getTbdMatchesInTicketWindow());
    }

    private static Match cupMatch(long id, long editionId, int round) {
        Match match = new Match();
        match.setId(id);
        match.setMatchType(Match.CUP_MATCH);
        match.setCompetitionEditionId(editionId);
        match.setRound(round);
        match.setHomeClubId(1L);
        match.setAwayClubId(2L);
        return match;
    }
}
