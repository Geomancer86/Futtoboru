package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

class PlayerFormTest {

    @Test
    void matchInXiRaisesSharpness() {
        Club club = testClub();
        MatchLineup lineup = MatchLineupBuilder.fromPlayerIds(club, List.of(1L), "2-3-5-1888");
        Match match = new Match();
        match.setHomeGoals(1);
        match.setAwayGoals(0);
        match.setMatchDateTime(java.time.LocalDateTime.of(1888, 9, 1, 15, 0));
        match.setHomePlayerRatings(Map.of(1L, 7.0f));

        PlayerCondition condition = PlayerConditionService.conditionOf(club.getPlayers().get(0));
        int before = condition.getSharpnessPct();
        PlayerConditionService.applyPostMatchEffects(match, lineup, club, null, null);
        assertTrue(condition.getSharpnessPct() > before);
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
