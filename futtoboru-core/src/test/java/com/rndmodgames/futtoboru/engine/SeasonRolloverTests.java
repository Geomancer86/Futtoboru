package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.data.Message;

/**
 * P2-6: season rollover resets the league table and keeps the old season's final table.
 */
public class SeasonRolloverTests extends BaseTest {

    private EngineTestWorld world;
    private CompetitionEdition firstSeason;

    @BeforeEach
    void setUp() {
        world = new EngineTestWorld(application, 4);
        firstSeason = new CompetitionEdition();
        firstSeason.setId(world.game.getIdAllocator().nextId());
        firstSeason.setName("Test League 1888-89");
        firstSeason.setStartDate(LocalDateTime.of(1888, 9, 1, 0, 0));
        firstSeason.setEndDate(firstSeason.getStartDate().plusMonths(9));
        for (Club c : world.clubs) {
            firstSeason.getParticipantClubsIds().add(c.getId());
        }
        world.league.getEditions().add(firstSeason);
    }

    /** Give each club a complete double round robin (6 matches) with the given points, best first */
    private void finishSeason(int... points) {
        for (int i = 0; i < world.clubs.size(); i++) {
            Club c = world.club(i);
            c.setMatchesPlayed(6);
            c.setMatchesWon(points[i] / 2);
            c.setMatchesDrawn(points[i] % 2);
            c.setMatchesLost(6 - points[i] / 2 - points[i] % 2);
            c.setGoalsScored(10 - i);
            c.setGoalsConceded(5 + i);
            c.setPoints(points[i]);
        }
    }

    @Test
    void rolloverResetsTableAndKeepsFinalTable() {
        finishSeason(10, 7, 5, 2);
        world.game.setGameDate(LocalDateTime.of(1889, 4, 30, 12, 0));

        world.authorityManager.checkCompetitionsSchedule();

        for (Club c : world.clubs) {
            assertEquals(0, c.getMatchesPlayed(), c.getName() + " played");
            assertEquals(0, c.getPoints(), c.getName() + " points");
            assertEquals(0, c.getMatchesWon() + c.getMatchesDrawn() + c.getMatchesLost());
            assertEquals(0, c.getGoalsScored() + c.getGoalsConceded());
        }

        assertEquals(world.club(0).getId(), firstSeason.getChampionsId());
        assertEquals(world.club(1).getId(), firstSeason.getRunnersUpId());
        assertEquals(4, firstSeason.getFinalStandings().size(), "old season's final table kept");
        LeagueStandingRow top = firstSeason.getFinalStandings().get(0);
        assertEquals(1, top.getPosition());
        assertEquals(world.club(0).getId(), top.getClubId());
        assertEquals(10, top.getPoints());
        assertEquals(6, top.getPlayed());
        assertEquals(2, firstSeason.getFinalStandings().get(3).getPoints());

        assertTrue(world.league.getEditions().size() >= 2, "next season generated");

        // another day: nothing is completed again
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, countMessages("LEAGUE_COMPLETE"));
    }

    @Test
    void secondSeasonCompletesToo() {
        finishSeason(10, 7, 5, 2);
        world.game.setGameDate(LocalDateTime.of(1889, 4, 30, 12, 0));
        world.authorityManager.checkCompetitionsSchedule();

        CompetitionEdition secondSeason = world.league.getEditions().get(1);
        assertTrue(secondSeason.getName().contains("1889-90"), secondSeason.getName());

        finishSeason(3, 9, 6, 6);
        world.game.setGameDate(LocalDateTime.of(1890, 4, 30, 12, 0));
        world.authorityManager.checkCompetitionsSchedule();

        assertEquals(2, countMessages("LEAGUE_COMPLETE"), "the first season's message must not block the second");
        assertEquals(world.club(1).getId(), secondSeason.getChampionsId());
        assertEquals(world.club(1).getId(), secondSeason.getFinalStandings().get(0).getClubId());
        assertEquals(world.club(0).getId(), firstSeason.getChampionsId(), "first season unchanged");
        assertEquals(10, firstSeason.getFinalStandings().get(0).getPoints());
        assertEquals(0, world.club(1).getPoints());
    }

    private int countMessages(String type) {
        int n = 0;
        for (Message m : world.game.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                n++;
            }
        }
        return n;
    }
}
