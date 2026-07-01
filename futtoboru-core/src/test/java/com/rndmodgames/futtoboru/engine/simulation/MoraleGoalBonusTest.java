package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;

class MoraleGoalBonusTest {

    @Test
    void scoringGoalBoostsMoraleBeyondWinAlone() {
        Club club = testClub();
        MatchLineup lineup = MatchLineupBuilder.fromPlayerIds(club, List.of(1L), "2-3-5-1888");

        Match winNoGoal = new Match();
        winNoGoal.setHomeGoals(1);
        winNoGoal.setAwayGoals(0);
        winNoGoal.setMatchDateTime(java.time.LocalDateTime.of(1888, 9, 1, 15, 0));
        winNoGoal.setHomePlayerRatings(Map.of(1L, 7.0f));
        winNoGoal.setHomeScorerIds(List.of(99L));

        PlayerConditionService.applyPostMatchEffects(winNoGoal, lineup, club, null, null);
        int moraleWinOnly = PlayerConditionService.conditionOf(club.getPlayers().get(0)).getMorale();

        PlayerConditionService.conditionOf(club.getPlayers().get(0)).setMorale(PlayerCondition.DEFAULT_MORALE);

        Match winWithGoal = new Match();
        winWithGoal.setHomeGoals(1);
        winWithGoal.setAwayGoals(0);
        winWithGoal.setMatchDateTime(java.time.LocalDateTime.of(1888, 9, 8, 15, 0));
        winWithGoal.setHomePlayerRatings(Map.of(1L, 7.0f));
        winWithGoal.setHomeScorerIds(List.of(1L));

        PlayerConditionService.applyPostMatchEffects(winWithGoal, lineup, club, null, null);
        int moraleWithGoal = PlayerConditionService.conditionOf(club.getPlayers().get(0)).getMorale();

        assertTrue(moraleWithGoal > moraleWinOnly);
    }

    private static Club testClub() {
        Club club = new Club();
        club.setId(1L);
        Person person = new Person();
        person.setId(1L);
        person.setName("Test");
        Player player = new Player();
        player.setId(1L);
        player.setPerson(person);
        player.setCondition(PlayerCondition.createFitDefault());
        club.getPlayers().add(player);
        return club;
    }
}
