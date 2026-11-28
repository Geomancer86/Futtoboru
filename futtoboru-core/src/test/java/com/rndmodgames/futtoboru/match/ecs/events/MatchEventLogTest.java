package com.rndmodgames.futtoboru.match.ecs.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * E-17: the event log on its own. Minutes from ticks (1..45, 46..90, no added time), the level of each kind of event
 * (D19-9 key / extended / full), appeal votes packed and read back, player refs, growth past the first capacity, and
 * the compact saved form (negative positions, every field, damaged or foreign text read as null).
 */
class MatchEventLogTest {

    private static final int HZ = 30;
    private static final long MINUTE = 60L * HZ;

    @Test
    void minutesRunOneToFortyFiveThenFortySixToNinety() {
        MatchEventLog log = new MatchEventLog(HZ, 45, -1, 0);
        assertEquals(1, log.minuteOf(0));
        assertEquals(1, log.minuteOf(MINUTE - 1));
        assertEquals(2, log.minuteOf(MINUTE));
        assertEquals(45, log.minuteOf(45 * MINUTE - 1));
        assertEquals(46, log.minuteOf(45 * MINUTE));
        assertEquals(90, log.minuteOf(90 * MINUTE - 1));
        assertEquals(90, log.minuteOf(95 * MINUTE), "no added time");
    }

    @Test
    void eachKindOfEventHasItsLevel() {
        MatchEventLog log = new MatchEventLog(HZ, 45, 0, 0);
        int given = MatchEvent.appeal(1, 0, true, 1, true, 0);
        int refused = MatchEvent.appeal(0, 0, false, -1, false, 0);
        log.add(0, MatchEventType.KICK_OFF, 0, MatchEvent.ref(0, 8), -1, 0, 0, 1);
        log.add(10, MatchEventType.SHOT, 0, MatchEvent.ref(0, 8), -1, 30, 2, 250 << 1);
        log.add(20, MatchEventType.SAVE, 1, MatchEvent.ref(1, 0), MatchEvent.ref(0, 8), 49, 1, 4);
        log.add(30, MatchEventType.OFFSIDE_APPEAL, 1, MatchEvent.ref(0, 9), -1, 20, 5, given);
        log.add(40, MatchEventType.FOUL, 1, MatchEvent.ref(1, 3), MatchEvent.ref(0, 7), 10, -5,
                refused | (MatchEvent.FOUL_HANDBALL << 8));
        log.add(50, MatchEventType.INJURY, 0, MatchEvent.ref(0, 7), MatchEvent.ref(1, 3), 10, -5, MatchEvent.KNOCK);
        log.add(60, MatchEventType.INJURY, 0, MatchEvent.ref(0, 6), -1, 10, -5, MatchEvent.CARRIED_OFF);
        log.add(70, MatchEventType.GOAL, 0, MatchEvent.ref(0, 8), -1, 50, 0, 0);
        log.add(80, MatchEventType.KICK_OFF, 1, MatchEvent.ref(1, 8), -1, 0, 0, 0);
        log.add(90, MatchEventType.HEAVY_BALL, -1, -1, -1, 0, 0, 470);
        log.add(100, MatchEventType.HALF_TIME, -1, -1, -1, 0, 0, 1 | (0 << 8));
        List<MatchEvent> e = log.events();
        assertEquals(MatchEventLevel.KEY, e.get(0).level());
        assertEquals(MatchEventLevel.EXTENDED, e.get(1).level());
        assertEquals(MatchEventLevel.EXTENDED, e.get(2).level());
        assertEquals(MatchEventLevel.EXTENDED, e.get(3).level());
        assertEquals(MatchEventLevel.FULL, e.get(4).level());
        assertEquals(MatchEventLevel.EXTENDED, e.get(5).level());
        assertEquals(MatchEventLevel.KEY, e.get(6).level());
        assertEquals(MatchEventLevel.KEY, e.get(7).level());
        assertEquals(MatchEventLevel.FULL, e.get(8).level());
        assertEquals(MatchEventLevel.EXTENDED, e.get(9).level());
        assertEquals(MatchEventLevel.KEY, e.get(10).level());
        assertEquals(4, log.filtered(MatchEventLevel.KEY).size());
        assertEquals(9, log.filtered(MatchEventLevel.EXTENDED).size());
        assertEquals(11, log.filtered(MatchEventLevel.FULL).size());

        MatchEvent appeal = e.get(3);
        assertTrue(appeal.given() && appeal.refereeCalled());
        assertEquals(1, appeal.homeUmpireVote());
        assertEquals(0, appeal.awayUmpireVote());
        assertEquals(1, appeal.refereeVote());
        MatchEvent foul = e.get(4);
        assertFalse(foul.given() || foul.refereeCalled());
        assertEquals(-1, foul.refereeVote());
        assertEquals(MatchEvent.FOUL_HANDBALL, foul.foulKind());
        assertEquals(Team.HOME, MatchEvent.refTeam(foul.other()));
        assertEquals(7, MatchEvent.refSlot(foul.other()));
        assertEquals(1, e.get(10).homeScore());
        assertEquals(0, e.get(10).awayScore());
        assertEquals(250, e.get(1).data() >> 1);
        assertFalse(e.get(1).header());
        assertNull(MatchEvent.refTeam(-1));
        assertEquals(-1, MatchEvent.ref(-1, 3));
    }

    @Test
    void theSavedFormKeepsEveryFieldAndRejectsDamage() {
        MatchEventLog log = new MatchEventLog(HZ, 45, 2, 360);
        for (int i = 0; i < 700; i++) { // past the first capacity
            log.add(i * 37L, MatchEventType.values()[i % MatchEventType.values().length], i % 3 - 1,
                    i % 5 == 0 ? -1 : MatchEvent.ref(i % 2, i % 11), i % 7 == 0 ? -1 : MatchEvent.ref(1 - i % 2, 0),
                    -52.4 + i % 105, 33.3 - i % 67, i * 13);
        }
        String saved = log.encode();
        assertTrue(saved.startsWith(MatchEventLog.FORMAT));
        MatchEventLog back = MatchEventLog.decode(saved);
        assertEquals(log, back);
        assertEquals(log.events(), back.events());
        assertEquals(2, back.surface());
        assertEquals(0.36, back.wetness(), 1e-9);
        MatchEvent first = back.get(0);
        assertEquals(-52.4, first.x(), 1e-9);
        assertEquals(33.3, first.y(), 1e-9);
        assertTrue(saved.length() < 700 * 16, saved.length() + " chars");

        assertEquals(new MatchEventLog(HZ, 45, -1, 0), MatchEventLog.decode(new MatchEventLog(HZ, 45, -1, 0).encode()));
        assertNull(MatchEventLog.decode(null));
        assertNull(MatchEventLog.decode(""));
        assertNull(MatchEventLog.decode("E2:AAAA"));
        assertNull(MatchEventLog.decode(saved.substring(0, saved.length() - 3)));
        assertNull(MatchEventLog.decode(saved + "AAAA"));
        assertThrows(IllegalArgumentException.class,
                () -> log.add(0, MatchEventType.GOAL, 0, -1, -1, 0, 0, 0), "ticks never go back");
    }
}
