package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;

class MatchRatingsTest {

    @Test
    void scorerRatesHigherThanNonScorerOnWin() {
        Club club = clubWithElevenPlayers();
        List<Long> ids = lineupIds(club);
        Match match = new Match();
        match.setHomeClubId(club.getId());
        match.setAwayClubId(99L);
        match.setHomeGoals(2);
        match.setAwayGoals(0);
        match.getHomeScorerIds().add(ids.get(10));
        match.getHomeScorerIds().add(ids.get(10));

        MatchLineup lineup = MatchLineupBuilder.fromPlayerIds(club, ids, FormationRegistry.DEFAULT_FORMATION_ID);
        Map<Long, Float> ratings = MatchRatingsCalculator.rateLineup(match, lineup, club, true);

        assertEquals(11, ratings.size());
        float scorerRating = ratings.get(ids.get(10));
        float otherRating = ratings.get(ids.get(0));
        assertTrue(scorerRating > otherRating);
        assertTrue(scorerRating >= 6.8f);
    }

    @Test
    void ratingsClampBetweenFourAndNinePointFive() {
        Club club = clubWithElevenPlayers();
        List<Long> ids = lineupIds(club);
        Match match = new Match();
        match.setHomeClubId(club.getId());
        match.setHomeGoals(0);
        match.setAwayGoals(5);

        MatchLineup lineup = MatchLineupBuilder.fromPlayerIds(club, ids, FormationRegistry.DEFAULT_FORMATION_ID);
        Map<Long, Float> ratings = MatchRatingsCalculator.rateLineup(match, lineup, club, true);

        for (float rating : ratings.values()) {
            assertTrue(rating >= MatchRatingsCalculator.MIN_RATING);
            assertTrue(rating <= MatchRatingsCalculator.MAX_RATING);
        }
    }

    private static Club clubWithElevenPlayers() {
        Club club = new Club();
        club.setId(1L);
        club.setName("Test FC");
        List<Player> players = new ArrayList<>();
        for (long i = 1; i <= 11; i++) {
            Person person = new Person();
            person.setId(i);
            person.setName("P" + i);
            Player player = new Player();
            player.setId(i);
            player.setPerson(person);
            player.setPassing(10f);
            player.setTackling(10f);
            players.add(player);
        }
        club.setPlayers(players);
        return club;
    }

    private static List<Long> lineupIds(Club club) {
        List<Long> ids = new ArrayList<>();
        for (Player player : club.getPlayers()) {
            ids.add(player.getId());
        }
        return ids;
    }
}
