package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.system.SaveGame;

class FatigueRecoveryTest {

    @Test
    void dailyTickReducesFatigue() {
        SaveGame game = new SaveGame();
        game.setGameDate(java.time.LocalDateTime.of(1888, 9, 10, 9, 0));
        Club club = new Club();
        club.setId(1L);
        Person person = new Person();
        person.setId(1L);
        Player player = new Player();
        player.setId(1L);
        player.setPerson(person);
        PlayerCondition condition = PlayerCondition.createFitDefault();
        condition.setFatiguePct(40);
        player.setCondition(condition);
        club.getPlayers().add(player);
        game.setAllClubs(java.util.List.of(club));

        PlayerConditionService.tickDaily(game);
        assertTrue(condition.getFatiguePct() < 40);
    }
}
