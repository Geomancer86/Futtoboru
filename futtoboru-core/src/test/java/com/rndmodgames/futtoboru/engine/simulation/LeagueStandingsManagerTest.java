package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;

class LeagueStandingsManagerTest {

    private LeagueStandingsManager manager;
    private League league;

    @BeforeEach
    void setUp() {
        manager = new LeagueStandingsManager();
        league = new League();
        league.setRules(CompetitionRules.createHistoricalRules());
        league.setLeagueClubs(new ArrayList<>());
        for (long id = 1; id <= 12; id++) {
            Club club = new Club();
            club.setId(id);
            club.setName("Club " + id);
            club.setScheduledMatches(new ArrayList<>());
            league.getLeagueClubs().add(club);
        }
    }

    @Test
    void friendliesDoNotCountTowardLeagueCompletion() {
        for (Club club : league.getLeagueClubs()) {
            for (int i = 0; i < 22; i++) {
                club.getScheduledMatches().add(playedFriendly(club.getId()));
            }
        }
        assertFalse(manager.isLeagueComplete(league));
        assertEquals(0, manager.countCompletedLeagueMatches(league.getLeagueClubs().get(0)));
    }

    @Test
    void standingsSortByPointsDescending() {
        Club leader = league.getLeagueClubs().get(0);
        Club follower = league.getLeagueClubs().get(1);
        leader.setPoints(6);
        follower.setPoints(4);

        List<Club> standings = manager.calculateStandings(league);

        assertEquals(leader.getId(), standings.get(0).getId());
        assertEquals(follower.getId(), standings.get(1).getId());
    }

    @Test
    void goalAverageBreaksTieWhenPointsAndGoalDifferenceEqual() {
        Club lowerGoalAverage = league.getLeagueClubs().get(0);
        Club higherGoalAverage = league.getLeagueClubs().get(1);
        lowerGoalAverage.setPoints(4);
        higherGoalAverage.setPoints(4);
        lowerGoalAverage.setGoalsScored(10);
        lowerGoalAverage.setGoalsConceded(4);
        higherGoalAverage.setGoalsScored(8);
        higherGoalAverage.setGoalsConceded(2);

        List<Club> standings = manager.calculateStandings(league);

        assertEquals(higherGoalAverage.getId(), standings.get(0).getId());
        assertEquals(lowerGoalAverage.getId(), standings.get(1).getId());
    }

    @Test
    void goalsScoredBreaksTieWhenPointsAndGoalAverageEqual() {
        Club moreGoals = league.getLeagueClubs().get(0);
        Club fewerGoals = league.getLeagueClubs().get(1);
        moreGoals.setPoints(4);
        fewerGoals.setPoints(4);
        moreGoals.setGoalsScored(12);
        moreGoals.setGoalsConceded(6);
        fewerGoals.setGoalsScored(10);
        fewerGoals.setGoalsConceded(5);

        List<Club> standings = manager.calculateStandings(league);

        assertEquals(moreGoals.getId(), standings.get(0).getId());
        assertEquals(fewerGoals.getId(), standings.get(1).getId());
    }

    @Test
    void leagueCompleteWhenTwentyTwoLeagueMatchesPlayedPerClub() {
        for (Club club : league.getLeagueClubs()) {
            for (int i = 0; i < 22; i++) {
                club.getScheduledMatches().add(playedLeagueMatch(club.getId(), i));
            }
        }
        assertTrue(manager.isLeagueComplete(league));
    }

    @Test
    void leagueCompleteIgnoresMatchesBeforeSeasonBoundary() {
        for (Club club : league.getLeagueClubs()) {
            for (int i = 0; i < 500; i++) {
                club.getPlayedMatches().add(playedLeagueMatch(club.getId(), i));
            }
            club.markPlayedMatchesSeasonBoundary();
        }
        assertFalse(manager.isLeagueComplete(league));

        for (Club club : league.getLeagueClubs()) {
            for (int i = 0; i < 22; i++) {
                club.getPlayedMatches().add(playedLeagueMatch(club.getId(), 1000 + i));
            }
        }
        assertTrue(manager.isLeagueComplete(league));
    }

    private static Match playedLeagueMatch(long clubId, int seq) {
        Match match = new Match();
        match.setId(clubId * 100 + seq);
        match.setMatchType(Match.LEAGUE_MATCH);
        match.setIsPlayed(true);
        match.setHomeClubId(clubId);
        match.setAwayClubId(clubId == 1 ? 2L : 1L);
        match.setMatchDateTime(LocalDateTime.of(1888, 9, 1, 15, 0));
        return match;
    }

    private static Match playedFriendly(long clubId) {
        Match match = new Match();
        match.setId(clubId * 1000);
        match.setMatchType(Match.FRIENDLY_MATCH);
        match.setIsPlayed(true);
        match.setHomeClubId(clubId);
        match.setAwayClubId(99L);
        return match;
    }
}
