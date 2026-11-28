package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;

/**
 * P2-3: a cup's next edition opens on its season's calendar, not while the edition being played has no winner. It
 * used to be staged and drawn on 1 January (a 1889-90 FA Cup played in January 1889), and a gap between editions
 * made an ad hoc "FA Cup 1889" of the league clubs only. P2-14 (D30 A9): the calendar opens it in October, with the
 * qualifying rounds (Sat 5 Oct 1889), not in May, and draws it from the first Monday of January, not at once.
 */
class CupStagingTests extends BaseTest {

    @Test
    void nextEditionOpensInOctoberAndIsDrawnInJanuary() {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        world.game.setGameDate(LocalDateTime.of(1888, 5, 1, 12, 0));
        EngineTestWorld.Cup cup = world.cup(world.clubs); // runs to 1 Nov 1888
        cup.edition.setName("1888-89 Test Cup");
        cup.edition.setChampionsId(world.club(0).getId());

        // New Year 1889: nothing new
        world.game.setGameDate(LocalDateTime.of(1889, 1, 1, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, cup.competition.getEditions().size(), "no 1889-90 cup in January 1889");

        // between editions in winter: no ad hoc edition either
        world.game.setGameDate(LocalDateTime.of(1889, 2, 15, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, cup.competition.getEditions().size(), "no ad hoc edition between editions");

        // May 1889: not yet (it opened in May until P2-14)
        world.game.setGameDate(LocalDateTime.of(1889, 5, 1, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, cup.competition.getEditions().size(), "the 1889-90 cup opens in October");

        // the calendar's opening day: the 1889-90 edition opens with the same entrants, undrawn
        world.game.setGameDate(LocalDateTime.of(1889, 10, 5, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(2, cup.competition.getEditions().size());
        CompetitionEdition next = cup.competition.getEditions().get(1);
        assertTrue(next.getName().contains("1889-90"), next.getName());
        assertEquals(cup.edition.getParticipantClubsIds(), next.getParticipantClubsIds());
        assertEquals(0, ties(world, next), "drawn later, not on the opening day");

        // and only once
        world.game.setGameDate(LocalDateTime.of(1889, 10, 6, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(2, cup.competition.getEditions().size());

        // still undrawn on New Year's Day 1890; drawn on the first Monday of January, for February and March
        world.game.setGameDate(LocalDateTime.of(1890, 1, 5, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(0, ties(world, next), "drawn from Monday 6 Jan 1890");
        world.game.setGameDate(LocalDateTime.of(1890, 1, 6, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertTrue(ties(world, next) > 0, "drawn");
        for (Club c : world.clubs) {
            for (Match m : c.getScheduledMatches()) {
                if (next.getId().equals(m.getCompetitionEditionId())) {
                    int month = m.getMatchDateTime().getMonthValue();
                    assertTrue(m.getMatchDateTime().getYear() == 1890 && (month == 2 || month == 3),
                            m.getBracketPath() + " on " + m.getMatchDateTime());
                }
            }
        }
        assertEquals(Integer.valueOf(1), next.getDrawnRounds(), "drawn round by round from the first round");
    }

    @Test
    void notWhileTheCurrentEditionIsUnfinished() {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        world.game.setGameDate(LocalDateTime.of(1888, 5, 1, 12, 0));
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        cup.edition.setName("1888-89 Test Cup");
        cup.edition.setEndDate(LocalDateTime.of(1889, 12, 31, 0, 0)); // still being played in October 1889

        world.game.setGameDate(LocalDateTime.of(1889, 10, 10, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, cup.competition.getEditions().size(), "the 1888-89 cup has no winner yet");

        cup.edition.setChampionsId(world.club(0).getId());
        world.game.setGameDate(LocalDateTime.of(1889, 10, 11, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(2, cup.competition.getEditions().size(), "won: the 1889-90 cup opens");
    }

    private static int ties(EngineTestWorld world, CompetitionEdition edition) {
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (Club c : world.clubs) {
            for (Match m : c.getScheduledMatches()) {
                if (edition.getId().equals(m.getCompetitionEditionId())) {
                    ids.add(m.getId());
                }
            }
        }
        return ids.size();
    }
}
