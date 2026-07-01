package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.engine.tactics.BestElevenSelector;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;

class InjuredPlayerLineupIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);
    }

    @Test
    void injuredPlayerExcludedFromAutoLineup() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        List<Long> lineup = harness.bestElevenPlayerIds(club);
        assertFalse(lineup.isEmpty());

        long injuredId = lineup.get(0);
        harness.setPlayerInjured(injuredId, 4);

        ClubTacticsSetup setup = BestElevenSelector.selectBestEleven(club, harness.game().getGameDate().toLocalDate());
        List<Long> autoXi = setup.orderedPlayerIds(
                FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId()));

        assertFalse(autoXi.contains(injuredId));
        assertTrue(autoXi.size() >= 10);
    }
}
