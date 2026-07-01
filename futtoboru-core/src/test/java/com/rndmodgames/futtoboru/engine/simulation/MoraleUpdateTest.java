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

class MoraleUpdateTest {

    @Test
    void winBoostsMoraleLossDropsIt() {
        Club club = testClub();
        MatchLineup lineup = MatchLineupBuilder.fromPlayerIds(club, List.of(1L), "2-3-5-1888");
        Match win = new Match();
        win.setHomeGoals(2);
        win.setAwayGoals(0);
        win.setMatchDateTime(java.time.LocalDateTime.of(1888, 9, 1, 15, 0));

        Map<Long, Float> ratings = new HashMap<>();
        ratings.put(1L, 7.5f);
        win.setHomePlayerRatings(ratings);

        int before = PlayerConditionService.conditionOf(club.getPlayers().get(0)).getMorale();
        MatchLineup awayLineup = MatchLineupBuilder.fromPlayerIds(null, List.of(), "2-3-5-1888");
        PlayerConditionService.applyPostMatchEffects(win, lineup, club, awayLineup, null);
        int afterWin = PlayerConditionService.conditionOf(club.getPlayers().get(0)).getMorale();
        assertTrue(afterWin > before);

        Match loss = new Match();
        loss.setHomeGoals(0);
        loss.setAwayGoals(3);
        loss.setMatchDateTime(java.time.LocalDateTime.of(1888, 9, 8, 15, 0));
        loss.setHomePlayerRatings(Map.of(1L, 4.5f));
        PlayerConditionService.applyPostMatchEffects(loss, lineup, club, awayLineup, null);
        int afterLoss = PlayerConditionService.conditionOf(club.getPlayers().get(0)).getMorale();
        assertTrue(afterLoss < afterWin);
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
