package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.engine.simulation.PlayerConditionService;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;

class PlayerConditionSaveLoadIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void roundTripPreservesConditionFields() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Player player = club.getPlayers().get(0);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setFitnessPct(55);
        condition.setMorale(42);
        condition.setInjuryTypeId("strain");
        condition.setInjuryDisplayName("Strain");
        condition.setInjuryReturnDate(harness.game().getGameDate().toLocalDate().plusWeeks(2));

        SaveGame copy = SaveLoadSystem.copySaveGame(harness.game());
        Player loaded = copy.getClubById(club.getId()).getPlayers().get(0);
        PlayerCondition loadedCondition = loaded.getCondition();

        assertEquals(55, loadedCondition.getFitnessPct());
        assertEquals(42, loadedCondition.getMorale());
        assertEquals("strain", loadedCondition.getInjuryTypeId());
        assertTrue(loadedCondition.isInjured(copy.getGameDate().toLocalDate()));
    }
}
