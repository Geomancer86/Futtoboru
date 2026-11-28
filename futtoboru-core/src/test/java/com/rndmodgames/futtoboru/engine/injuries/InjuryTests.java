package com.rndmodgames.futtoboru.engine.injuries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerInjury;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * G-02 without a game: the injury data, the match roll (kinds, minutes, days out, determinism), what the hurt cost a
 * side, and availability by date.
 */
class InjuryTests {

    private static List<Player> side(int n) {
        List<Player> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Person person = new Person();
            person.setId(100L + i);
            Player p = new Player();
            p.setId(1L + i);
            p.setPerson(person);
            out.add(p);
        }
        return out;
    }

    @Test
    void theShippedDataGivesTheThreeKinds() {
        GameData data = GameData.loadDefault();
        assertTrue(data.getReport().getErrors().isEmpty(), data.getReport().getErrors().toString());
        InjuryRules rules = InjuryRules.from(data);
        for (InjuryKind k : InjuryKind.values()) {
            InjuryRules.Rule r = rules.get(k);
            assertNotNull(r, k.name());
            assertTrue(r.chancePerMatch > 0 && r.chancePerMatch < 0.1, k + " " + r.chancePerMatch);
            assertTrue(r.minDays <= r.maxDays);
        }
        assertEquals(0, rules.get(InjuryKind.KNOCK).minDays, "a knock may cost nothing");
        assertTrue(rules.get(InjuryKind.CARRIED_OFF).maxDays > rules.get(InjuryKind.PASSENGER).maxDays);
        assertEquals(1, data.all(GameData.INJURY).stream().filter(r -> "carried-off".equals(r.str("kind"))).count());
    }

    @Test
    void theRollGivesEveryKindWithMinutesAndDaysInRange() {
        InjuryRules rules = InjuryRules.DEFAULTS;
        RandomSource root = RandomSource.seeded(42);
        Map<InjuryKind, Integer> counts = new EnumMap<>(InjuryKind.class);
        int sides = 4000;
        int returned = 0;
        for (int m = 0; m < sides; m++) {
            for (MatchInjury i : MatchInjuries.roll(side(11), rules, root.fork("injuries:" + m).fork("home"))) {
                InjuryKind k = i.getKindEnum();
                counts.merge(k, 1, Integer::sum);
                InjuryRules.Rule r = rules.get(k);
                assertTrue(i.getMinute() >= 1 && i.getMinute() <= MatchInjuries.MINUTES);
                assertTrue(i.getDaysOut() >= r.minDays && i.getDaysOut() <= r.maxDays, k + " " + i.getDaysOut());
                if (i.getReturnedMinute() != null) {
                    assertEquals(InjuryKind.CARRIED_OFF, k, "only a man carried off comes back on");
                    assertTrue(i.getReturnedMinute() > i.getMinute() && i.getReturnedMinute() < MatchInjuries.MINUTES);
                    returned++;
                }
            }
        }
        // per man per match: knock 3%, passenger 0.8%, carried off 0.6% (44 000 men)
        double men = sides * 11.0;
        assertEquals(0.03, counts.get(InjuryKind.KNOCK) / men, 0.004);
        assertEquals(0.008, counts.get(InjuryKind.PASSENGER) / men, 0.002);
        assertEquals(0.006, counts.get(InjuryKind.CARRIED_OFF) / men, 0.002);
        assertTrue(returned > 0 && returned < counts.get(InjuryKind.CARRIED_OFF));
    }

    @Test
    void theSameSeedGivesTheSameInjuries() {
        InjuryRules always = InjuryRules.of(new InjuryRules.Rule(InjuryKind.PASSENGER, 0.5, 7, 21, 0, 0.4));
        List<MatchInjury> a = MatchInjuries.roll(side(11), always, RandomSource.seeded(9).fork("injuries:5"));
        List<MatchInjury> b = MatchInjuries.roll(side(11), always, RandomSource.seeded(9).fork("injuries:5"));
        assertFalse(a.isEmpty());
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i).getPersonId(), b.get(i).getPersonId());
            assertEquals(a.get(i).getKind(), b.get(i).getKind());
            assertEquals(a.get(i).getMinute(), b.get(i).getMinute());
            assertEquals(a.get(i).getDaysOut(), b.get(i).getDaysOut());
        }
    }

    @Test
    void theHurtCostTheSideFromTheirMinuteOn() {
        InjuryRules rules = InjuryRules.DEFAULTS; // passenger worth 0.4 of a fit man
        assertEquals(11, MatchInjuries.effectiveMen(11, List.of(), rules), 1e-9);
        assertEquals(11, MatchInjuries.effectiveMen(11,
                List.of(new MatchInjury(1L, InjuryKind.KNOCK, 10, null, 3)), rules), 1e-9, "a knock plays on");
        assertEquals(11 - 0.6 * 0.5, MatchInjuries.effectiveMen(11,
                List.of(new MatchInjury(1L, InjuryKind.PASSENGER, 45, null, 9)), rules), 1e-9);
        assertEquals(10, MatchInjuries.effectiveMen(11,
                List.of(new MatchInjury(1L, InjuryKind.CARRIED_OFF, 0, null, 20)), rules), 1e-9,
                "carried off at once: ten men");
        // off from 30 to 60, then a passenger to the end
        assertEquals(11 - 30 / 90.0 - 0.6 * 30 / 90.0, MatchInjuries.effectiveMen(11,
                List.of(new MatchInjury(1L, InjuryKind.CARRIED_OFF, 30, 60, 20)), rules), 1e-9);
        assertTrue(MatchInjuries.leftForGood(new MatchInjury(1L, InjuryKind.CARRIED_OFF, 30, null, 20)));
        assertFalse(MatchInjuries.leftForGood(new MatchInjury(1L, InjuryKind.CARRIED_OFF, 30, 60, 20)));
    }

    @Test
    void aManIsUnavailableUntilHisDay() {
        Player p = side(1).get(0);
        LocalDateTime match = LocalDateTime.of(1888, 9, 8, 0, 0);
        assertTrue(Availability.isAvailable(p, match));
        assertTrue(Availability.isAvailable(p));
        p.setInjury(new PlayerInjury(InjuryKind.PASSENGER, match, 10, 77L));
        assertFalse(Availability.isAvailable(p));
        Availability.Status s = Availability.of(p, match.plusDays(3).withHour(15));
        assertFalse(s.isAvailable());
        assertEquals(Availability.Reason.INJURED, s.getReason());
        assertEquals(7, s.getDaysLeft());
        assertEquals(match.plusDays(10), s.getUntil());
        assertFalse(Availability.isAvailable(p, match.plusDays(9)));
        assertTrue(Availability.isAvailable(p, match.plusDays(10)), "fit again on his day");
        assertNull(Availability.of(p, match.plusDays(10)).getInjury());
        assertFalse(Availability.isAvailable(null));
    }
}
