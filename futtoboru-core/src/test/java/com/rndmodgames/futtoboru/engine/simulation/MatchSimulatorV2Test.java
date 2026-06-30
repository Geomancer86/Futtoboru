package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.tactics.BestElevenSelector;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;
import com.rndmodgames.futtoboru.engine.tactics.LineupStrengthCalculator;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;

class MatchSimulatorV2Test {

    private Club homeClub;
    private Club awayClub;

    @BeforeEach
    void setUp() {
        homeClub = buildClub(1L, 20, 50f);
        awayClub = buildClub(2L, 20, 50f);
    }

    @Test
    void strongerLineupHasHigherCalculatedStrength() {
        MatchLineup strong = lineupFromSetup(homeClub, BestElevenSelector.selectBestEleven(homeClub));
        MatchLineup weak = lineupFromSetup(homeClub, BestElevenSelector.selectWorstEleven(homeClub,
                FormationRegistry.DEFAULT_FORMATION_ID));

        double strongStrength = LineupStrengthCalculator.calculate(strong, homeClub);
        double weakStrength = LineupStrengthCalculator.calculate(weak, homeClub);
        assertTrue(strongStrength > weakStrength);
    }

    @Test
    void strongerLineupWinsMoreOftenOverManySimulations() {
        MatchLineup strongHome = lineupFromSetup(homeClub, BestElevenSelector.selectBestEleven(homeClub));
        MatchLineup weakHome = lineupFromSetup(homeClub, BestElevenSelector.selectWorstEleven(homeClub,
                FormationRegistry.DEFAULT_FORMATION_ID));
        MatchLineup awayLineup = MatchLineupBuilder.buildForClub(awayClub, null);

        int strongWins = 0;
        int weakWins = 0;
        for (int i = 0; i < 200; i++) {
            Random random = new Random(1000L + i);
            int[] strongScore = MatchSimulator.simulateScore(strongHome, homeClub, awayLineup, awayClub, random);
            random = new Random(1000L + i);
            int[] weakScore = MatchSimulator.simulateScore(weakHome, homeClub, awayLineup, awayClub, random);
            if (strongScore[0] > strongScore[1]) {
                strongWins++;
            }
            if (weakScore[0] > weakScore[1]) {
                weakWins++;
            }
        }
        assertTrue(strongWins > weakWins, "strongWins=" + strongWins + " weakWins=" + weakWins);
    }

    private static Club buildClub(long clubId, int squadSize, float baseAttr) {
        Club club = new Club();
        club.setId(clubId);
        for (long id = 1; id <= squadSize; id++) {
            Player player = new Player();
            player.setId(clubId * 100 + id);
            player.setSpeed(baseAttr + id);
            player.setKicking(baseAttr + id);
            player.setTackling(baseAttr + id);
            player.setPassing(baseAttr + id);
            player.setShotStopping(id == 1 ? baseAttr + 40f : baseAttr);
            club.getPlayers().add(player);
        }
        return club;
    }

    private static MatchLineup lineupFromSetup(Club club,
            com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup setup) {
        club.setTacticsSetup(setup);
        List<Long> ids = setup.orderedPlayerIds(FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId()));
        return MatchLineupBuilder.fromPlayerIds(club, new ArrayList<>(ids), setup.getFormationTemplateId());
    }
}
