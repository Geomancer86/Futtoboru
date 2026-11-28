package com.rndmodgames.futtoboru.system.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * E-01b: positions for players the data gives none (squad fill, generated players) and dribbling in the daily drift,
 * the age decline and the attribute snapshots.
 */
class PositionsAndDribblingTests {

    private static final float AVG = 10f;
    private static final LocalDateTime DAY = LocalDateTime.of(1888, 9, 8, 0, 0);

    @Test
    void elevenPlayersWithoutPositionsFillTheTwoThreeFive() {
        List<Player> squad = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            squad.add(new Player());
        }
        assertFalse(PositionAssigner.canFieldTwoThreeFive(squad));
        assertEquals(11, PositionAssigner.fillMissing(squad));
        assertTrue(PositionAssigner.canFieldTwoThreeFive(squad));
        Map<Position, Integer> count = count(squad);
        assertEquals(1, count.get(Position.GOALKEEPER));
        assertEquals(2, count.get(Position.FULL_BACK));
        assertEquals(3, count.get(Position.HALF_BACK));
        for (Position p : new Position[] { Position.RIGHT_WING, Position.INSIDE_RIGHT, Position.CENTRE_FORWARD,
                Position.INSIDE_LEFT, Position.LEFT_WING }) {
            assertEquals(1, count.get(p), p.name());
        }
        assertEquals(0, PositionAssigner.fillMissing(squad), "nothing left to fill");
    }

    @Test
    void theFillTakesTheSlotsTheSquadIsShortOf() {
        List<Player> squad = new ArrayList<>();
        // nine forwards and a goalkeeper: the gaps are the backs and halves
        squad.add(at(Position.GOALKEEPER));
        for (int i = 0; i < 9; i++) {
            squad.add(at(Position.CENTRE_FORWARD));
        }
        for (int i = 0; i < 5; i++) {
            squad.add(new Player());
        }
        PositionAssigner.fillMissing(squad);
        Map<Position, Integer> count = count(squad);
        assertEquals(2, count.get(Position.FULL_BACK));
        assertEquals(3, count.get(Position.HALF_BACK));
        assertTrue(PositionAssigner.canFieldTwoThreeFive(squad));
    }

    @Test
    void generatedPlayersGetAPositionDeterministicPerSeed() {
        List<Position> a = generated(1888L);
        List<Position> b = generated(1888L);
        assertEquals(a, b, "same seed, same positions");
        for (Position p : a) {
            assertNotNull(p);
        }
        assertTrue(a.stream().distinct().count() > 1, "not everyone the same position: " + a);
    }

    @Test
    void dribblingDriftsLikeTheOtherTechnicalAttributes() {
        PlayerAttributeGenerator generator = new PlayerAttributeGenerator();
        Player player = player(10f);
        int changed = 0;
        for (int day = 0; day < 400; day++) {
            float before = player.getDribbling();
            generator.applyDailyAttributeChanges(player, DAY.plusDays(day),
                    RandomSource.seeded(1888L).fork("training:1:" + day));
            assertTrue(player.getDribbling() >= 3 && player.getDribbling() <= 23);
            changed += before != player.getDribbling() ? 1 : 0;
        }
        assertTrue(changed > 0, "dribbling never drifted in 400 days");

        // the same streams give the same values
        Player again = player(10f);
        for (int day = 0; day < 400; day++) {
            generator.applyDailyAttributeChanges(again, DAY.plusDays(day),
                    RandomSource.seeded(1888L).fork("training:1:" + day));
        }
        assertEquals(player.getDribbling(), again.getDribbling());
    }

    /**
     * P2-13: the age decline is a year's rate spread over the days (1-2 steps a year from 40). It used to take that
     * whole step every day: a 45-year-old lost one point a day in every physical, technical and keeping attribute.
     * Same streams for both players, so the daily drift is the same and the difference is the decline.
     */
    @Test
    void dribblingDeclinesWithAgeAtAYearlyRate() {
        PlayerAttributeGenerator generator = new PlayerAttributeGenerator();
        Player veteran = player(15f);
        veteran.getPerson().setBirthDate(DAY.minusYears(45));
        Player young = player(15f);
        for (int day = 0; day < 2 * 365; day++) {
            generator.applyDailyAttributeChanges(veteran, DAY.plusDays(day),
                    RandomSource.seeded(7L).fork("training:1:" + day));
            generator.applyDailyAttributeChanges(young, DAY.plusDays(day),
                    RandomSource.seeded(7L).fork("training:1:" + day));
            if (day == 364) {
                // keeping attributes do not drift: only the decline moves them
                float lost = AVG - veteran.getShotStopping();
                assertTrue(lost >= 1 && lost <= 4, "a 45-year-old's year of decline: " + lost + " steps");
                assertEquals(AVG, young.getShotStopping(), "no decline at 24");
            }
        }
        float lost = young.getDribbling() - veteran.getDribbling();
        assertTrue(lost >= 1 && lost <= 8, "a 45-year-old's dribbling declines over two years: " + lost + " steps");
    }

    @Test
    void theSnapshotKeepsDribbling() {
        Player player = player(17f);
        player.getPerson().setId(5L);
        PlayerAttributeSnapshot snapshot = PlayerAttributeSnapshot.fromPlayer(player, DAY, "WEEKLY");
        assertEquals(17f, snapshot.getDribbling());
        assertEquals(17f, snapshot.getAttributeValue("Dribbling"));

        // a snapshot from a save before E-01b has none: read back as derived from its own attributes
        snapshot.setDribbling(null);
        assertEquals(PlayerAttributeGenerator.deriveDribbling(snapshot.getDexterity(), snapshot.getAcceleration(),
                snapshot.getTrickShots(), snapshot.getSpeed()), snapshot.getAttributeValue("dribbling"));
    }

    // ------------------------------------------------------------------------------------------------- helpers

    private static List<Position> generated(long seed) {
        Futtoboru game = new Futtoboru(seed);
        PlayerGenerator generator = new PlayerGenerator(game);
        List<Position> out = new ArrayList<>();
        for (long id = 1; id <= 40; id++) {
            Person person = new Person();
            person.setId(id);
            person.setName("Gen");
            person.setLastname("Player" + id);
            person.setBirthDate(DAY.minusYears(18 + id % 15));
            out.add(generator.generateRandomPlayer(person).getPosition());
        }
        return out;
    }

    private static Player at(Position position) {
        Player p = new Player();
        p.setPosition(position);
        return p;
    }

    private static Player player(float dribbling) {
        Person person = new Person();
        person.setBirthDate(DAY.minusYears(24));
        Player p = new Player();
        p.setPerson(person);
        p.setAcceleration(AVG); p.setSpeed(AVG); p.setStamina(AVG); p.setStrength(AVG); p.setEndurance(AVG);
        p.setJumping(AVG); p.setDexterity(AVG);
        p.setConcentration(AVG); p.setCourage(AVG); p.setDetermination(AVG); p.setLeadership(AVG);
        p.setPerception(AVG); p.setPositioning(AVG); p.setTeamwork(AVG);
        p.setPassing(AVG); p.setKicking(AVG); p.setLongShots(AVG); p.setTrickShots(AVG); p.setHeading(AVG);
        p.setOneTwos(AVG); p.setFreeKicks(AVG); p.setCornerKicks(AVG); p.setPenaltyKicks(AVG);
        p.setThrowIns(AVG); p.setMarking(AVG); p.setTackling(AVG);
        p.setShotStopping(AVG); p.setAreaControl(AVG); p.setPunching(AVG); p.setHandToHand(AVG);
        p.setRushingOut(AVG); p.setAreaPositioning(AVG);
        p.setDribbling(dribbling);
        return p;
    }

    private static Map<Position, Integer> count(List<Player> squad) {
        Map<Position, Integer> count = new EnumMap<>(Position.class);
        for (Position p : Position.values()) {
            count.put(p, 0);
        }
        for (Player p : squad) {
            count.merge(p.getPosition(), 1, Integer::sum);
        }
        return count;
    }
}
