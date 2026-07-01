package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

class MatchInjuryTrackingTest {

    @Test
    void postMatchRecordsNewInjuriesOnMatch() {
        Club club = testClub();
        MatchLineup lineup = MatchLineupBuilder.fromPlayerIds(club, List.of(1L), "2-3-5-1888");
        Match match = new Match();
        match.setHomeGoals(1);
        match.setAwayGoals(0);
        match.setMatchDateTime(java.time.LocalDateTime.of(1888, 9, 1, 15, 0));
        match.setHomePlayerRatings(Map.of(1L, 7.0f));

        for (long matchId = 1; matchId < 500; matchId++) {
            match.setId(matchId);
            PlayerConditionService.conditionOf(club.getPlayers().get(0)).clearInjury();
            match.setHomeInjuredPlayerIds(List.of());
            PlayerConditionService.applyPostMatchEffects(match, lineup, club, null, null);
            if (!match.getHomeInjuredPlayerIds().isEmpty()) {
                assertEquals(1L, match.getHomeInjuredPlayerIds().get(0));
                assertTrue(PlayerConditionService.conditionOf(club.getPlayers().get(0)).isInjured(
                        java.time.LocalDate.of(1888, 9, 1)));
                return;
            }
        }
        fail("Expected at least one match seed to produce an injury within 500 attempts");
    }

    private static Club testClub() {
        Club club = new Club();
        club.setId(1L);
        Person person = new Person();
        person.setId(1L);
        Player player = new Player();
        player.setId(1L);
        player.setPerson(person);
        player.setCondition(PlayerCondition.createFitDefault());
        club.getPlayers().add(player);
        return club;
    }
}
