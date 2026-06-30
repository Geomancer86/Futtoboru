package com.rndmodgames.futtoboru.engine.tactics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;

class BestElevenSelectorTest {

    private Club club;

    @BeforeEach
    void setUp() {
        club = new Club();
        club.setId(1L);
        for (long id = 1; id <= 15; id++) {
            Player player = new Player();
            player.setId(id);
            player.setKicking(40f + id);
            player.setSpeed(30f + id);
            player.setTackling(20f + id);
            player.setShotStopping(id == 1 ? 90f : 10f);
            club.getPlayers().add(player);
        }
    }

    @Test
    void selectsElevenDistinctPlayers() {
        ClubTacticsSetup setup = BestElevenSelector.selectBestEleven(club);
        FormationTemplate template = FormationRegistry.getDefaultTemplate();
        assertTrue(setup.hasCompleteValidLineup(club, template));
        assertEquals(11, setup.orderedPlayerIds(template).size());
        assertEquals(11, new HashSet<>(setup.orderedPlayerIds(template)).size());
    }

    @Test
    void handlesSquadSmallerThanEleven() {
        Club small = new Club();
        small.setId(2L);
        for (long id = 1; id <= 7; id++) {
            Player player = new Player();
            player.setId(id);
            player.setSpeed(50f);
            small.getPlayers().add(player);
        }
        ClubTacticsSetup setup = BestElevenSelector.selectBestEleven(small);
        assertTrue(setup.getSlotAssignments().size() <= 7);
    }

    @Test
    void worstElevenDiffersFromBestEleven() {
        ClubTacticsSetup best = BestElevenSelector.selectBestEleven(club);
        ClubTacticsSetup worst = BestElevenSelector.selectWorstEleven(club, FormationRegistry.DEFAULT_FORMATION_ID);
        FormationTemplate template = FormationRegistry.getDefaultTemplate();
        Set<Long> bestIds = new HashSet<>(best.orderedPlayerIds(template));
        Set<Long> worstIds = new HashSet<>(worst.orderedPlayerIds(template));
        assertTrue(bestIds.size() == 11 && worstIds.size() == 11);
        assertTrue(!bestIds.equals(worstIds));
    }
}
