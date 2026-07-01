package com.rndmodgames.futtoboru.engine.tactics;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;

class FormationSimWeightTest {

    @Test
    void differentFormationsChangeLineupStrength() {
        Club club = testClub();
        List<Long> ids = BestElevenSelector.selectBestEleven(club).orderedPlayerIds(
                FormationRegistry.getDefaultTemplate());

        MatchLineup pyramid = MatchLineupBuilder.fromPlayerIds(club, ids, "2-3-5-1888");
        MatchLineup threeHalf = MatchLineupBuilder.fromPlayerIds(club, ids, "3-2-5-1888");

        double pyramidStrength = LineupStrengthCalculator.calculate(pyramid, club);
        double threeHalfStrength = LineupStrengthCalculator.calculate(threeHalf, club);

        assertNotEquals(pyramidStrength, threeHalfStrength, 0.001);
    }

    private static Club testClub() {
        Club club = new Club();
        club.setId(1L);
        for (long i = 1; i <= 11; i++) {
            Person person = new Person();
            person.setId(i);
            person.setName("P" + i);
            Player player = new Player();
            player.setId(i);
            player.setPerson(person);
            player.setPassing(8f + i * 0.1f);
            player.setTackling(8f);
            player.setKicking(8f);
            player.setSpeed(8f);
            player.setShotStopping(i == 1 ? 12f : 5f);
            club.getPlayers().add(player);
        }
        return club;
    }
}
