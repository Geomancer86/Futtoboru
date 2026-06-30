package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.engine.tactics.BestElevenSelector;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;

class ClubTacticsSetupSaveLoadIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void roundTripPreservesClubTacticsSetup() {
        harness.advanceUntil(save -> harness.primaryLeague() != null, 30);

        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        assertNotNull(club);
        ClubTacticsSetup setup = BestElevenSelector.selectBestEleven(club);
        club.setTacticsSetup(setup);

        FormationTemplate template = FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId());
        assertTrue(setup.hasCompleteValidLineup(club, template));

        harness.roundTripViaJson();

        Club reloaded = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        assertNotNull(reloaded.getTacticsSetup());
        assertEquals(11, reloaded.getTacticsSetup().orderedPlayerIds(template).size());
        assertTrue(reloaded.getTacticsSetup().hasCompleteValidLineup(reloaded, template));
    }
}
