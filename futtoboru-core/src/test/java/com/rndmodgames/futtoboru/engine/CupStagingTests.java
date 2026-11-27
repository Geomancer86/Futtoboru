package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.CompetitionEdition;

/**
 * P2-3: a cup's next edition opens in May, not while the edition being played has no winner. It used to be
 * staged and drawn on 1 January (a 1889-90 FA Cup played in January 1889), and a gap between editions made an
 * ad hoc "FA Cup 1889" of the league clubs only.
 */
class CupStagingTests extends BaseTest {

    @Test
    void nextEditionOpensInMay() {
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

        // May 1889: the 1889-90 edition opens with the same entrants
        world.game.setGameDate(LocalDateTime.of(1889, 5, 1, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(2, cup.competition.getEditions().size());
        CompetitionEdition next = cup.competition.getEditions().get(1);
        assertTrue(next.getName().contains("1889-90"), next.getName());
        assertEquals(cup.edition.getParticipantClubsIds(), next.getParticipantClubsIds());

        // and only once
        world.game.setGameDate(LocalDateTime.of(1889, 5, 2, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(2, cup.competition.getEditions().size());
    }

    @Test
    void notWhileTheCurrentEditionIsUnfinished() {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        world.game.setGameDate(LocalDateTime.of(1888, 5, 1, 12, 0));
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        cup.edition.setName("1888-89 Test Cup");
        cup.edition.setEndDate(LocalDateTime.of(1889, 6, 30, 0, 0)); // still being played in May 1889

        world.game.setGameDate(LocalDateTime.of(1889, 5, 10, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(1, cup.competition.getEditions().size(), "the 1888-89 cup has no winner yet");

        cup.edition.setChampionsId(world.club(0).getId());
        world.game.setGameDate(LocalDateTime.of(1889, 5, 11, 4, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(2, cup.competition.getEditions().size(), "won: the 1889-90 cup opens");
    }
}
