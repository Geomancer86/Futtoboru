package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;

/**
 * P2-3: the league top scorer counts the league goals of the season being completed, not of every season
 * in the clubs' played lists, and is found among the league clubs' players.
 */
class TopScorerTests extends BaseTest {

    private EngineTestWorld world;

    private CompetitionEdition season(String name, LocalDateTime start) {
        CompetitionEdition e = new CompetitionEdition();
        e.setId(world.game.getIdAllocator().nextId());
        e.setName(name);
        e.setStartDate(start);
        e.setEndDate(start.plusMonths(9));
        world.league.getEditions().add(e);
        return e;
    }

    /** A played league match on the given date; every goal by the given scorers (person ids) of the home side */
    private void played(Club home, Club away, LocalDateTime date, Person... homeScorers) {
        Match m = world.leagueMatch(home, away);
        m.setMatchDateTime(date);
        for (Person p : homeScorers) {
            m.getHomeScorerIds().add(p.getId());
        }
        EngineTestWorld.score(m, homeScorers.length, 0);
        home.getScheduledMatches().remove(m);
        away.getScheduledMatches().remove(m);
        home.getPlayedMatches().add(m);
        away.getPlayedMatches().add(m);
    }

    @Test
    void topScorerCountsOnlyThisSeason() {
        world = new EngineTestWorld(application, 4);
        Person oldStar = world.club(0).getPlayers().get(9).getPerson();
        Person newStar = world.club(1).getPlayers().get(9).getPerson();

        CompetitionEdition first = season("Test League 1888-89", LocalDateTime.of(1888, 9, 1, 0, 0));
        CompetitionEdition second = season("Test League 1889-90", LocalDateTime.of(1889, 9, 1, 0, 0));

        // 1888-89: five goals for the old star
        played(world.club(0), world.club(2), LocalDateTime.of(1888, 9, 8, 15, 0), oldStar, oldStar, oldStar);
        played(world.club(0), world.club(3), LocalDateTime.of(1889, 3, 2, 15, 0), oldStar, oldStar);
        // 1889-90: two goals for the new star, one for the old one
        played(world.club(1), world.club(2), LocalDateTime.of(1889, 9, 7, 15, 0), newStar, newStar);
        played(world.club(0), world.club(1), LocalDateTime.of(1889, 10, 5, 15, 0), oldStar);

        Person top = world.authorityManager.calculateTopScorer(world.league, second);
        assertNotNull(top, "the top scorer is found among the league clubs' players");
        assertEquals(newStar.getId(), top.getId(), "1889-90 top scorer");
        assertEquals(2, world.authorityManager.getPlayerSeasonGoals(newStar.getId(), world.league, second));
        assertEquals(1, world.authorityManager.getPlayerSeasonGoals(oldStar.getId(), world.league, second),
                "last season's goals do not count");

        assertEquals(oldStar.getId(), world.authorityManager.calculateTopScorer(world.league, first).getId());
        assertEquals(5, world.authorityManager.getPlayerSeasonGoals(oldStar.getId(), world.league, first),
                "next season's goals do not count either");
    }
}
