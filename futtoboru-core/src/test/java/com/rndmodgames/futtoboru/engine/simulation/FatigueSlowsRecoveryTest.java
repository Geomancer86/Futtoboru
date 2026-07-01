package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.system.SaveGame;

class FatigueSlowsRecoveryTest {

    @Test
    void highFatigueReducesRestDayFitnessGain() {
        SaveGame restedGame = buildGame(20, 30);
        PlayerConditionService.tickDaily(restedGame);
        int fitnessAfterRested = restedGame.getAllClubs().get(0).getPlayers().get(0).getCondition().getFitnessPct();

        SaveGame tiredGame = buildGame(20, 60);
        PlayerConditionService.tickDaily(tiredGame);
        int fitnessAfterTired = tiredGame.getAllClubs().get(0).getPlayers().get(0).getCondition().getFitnessPct();

        assertTrue(fitnessAfterRested > fitnessAfterTired);
        assertEquals(22, fitnessAfterRested);
        assertEquals(21, fitnessAfterTired);
    }

    private static SaveGame buildGame(int fitness, int fatigue) {
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
        condition.setFitnessPct(fitness);
        condition.setFatiguePct(fatigue);
        player.setCondition(condition);
        club.getPlayers().add(player);
        game.setAllClubs(java.util.List.of(club));
        return game;
    }
}
