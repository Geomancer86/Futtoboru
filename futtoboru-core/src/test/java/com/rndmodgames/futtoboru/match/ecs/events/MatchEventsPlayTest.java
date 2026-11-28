package com.rndmodgames.futtoboru.match.ecs.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchEngine;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchInput;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchResult;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * E-17: the events of two whole matches (dry and rain, the golden matches' inputs). The goals of the log are the
 * result's goals; a match opens with a key kick-off and has half-time and full time with the score; minutes run 1..90
 * in tick order; the filters nest; the rain match's ball grows heavy and the dry one's does not; the log is the same
 * on a second run and survives its saved form.
 */
@Tag("slow")
class MatchEventsPlayTest {

    private static EcsMatchResult dry;
    private static EcsMatchResult rain;

    @BeforeAll
    static void play() {
        EcsMatchEngine engine = new EcsMatchEngine();
        RandomSource game = RandomSource.seeded(1888L);
        dry = engine.play(new EcsMatchInput("m1", new UniformPitch(Surface.DRY, PitchConditions.DRY)),
                game.fork("match:m1"));
        rain = engine.play(new EcsMatchInput("m2", new UniformPitch(Surface.WET, PitchConditions.RAIN)),
                game.fork("match:m2"));
    }

    private static Map<MatchEventType, Integer> counts(MatchEventLog log) {
        Map<MatchEventType, Integer> c = new EnumMap<>(MatchEventType.class);
        for (MatchEvent e : log.events()) {
            c.merge(e.type(), 1, Integer::sum);
        }
        return c;
    }

    @Test
    void goalsOfTheLogAreTheResultsGoals() {
        for (EcsMatchResult r : List.of(dry, rain)) {
            List<MatchEvent> goals = r.events().of(MatchEventType.GOAL);
            assertEquals(r.goals().size(), goals.size(), counts(r.events()).toString());
            int home = 0;
            for (int i = 0; i < goals.size(); i++) {
                MatchEvent g = goals.get(i);
                EcsMatchResult.Goal expected = r.goals().get(i);
                assertEquals(expected.scoringTeam(), g.side());
                assertEquals(expected.tick() - 1, g.tick());
                assertEquals(expected.hasScorer(), !g.ownGoal() && g.actor() >= 0);
                if (expected.hasScorer()) {
                    assertEquals(expected.slot(), MatchEvent.refSlot(g.actor()));
                    assertEquals(expected.scoringTeam(), MatchEvent.refTeam(g.actor()));
                }
                assertEquals(MatchEventLevel.KEY, g.level());
                home += g.side() == Team.HOME ? 1 : 0;
            }
            assertEquals(r.homeGoals(), home);
            assertEquals(r.awayGoals(), goals.size() - home);
        }
    }

    @Test
    void aMatchOpensWithAKickOffAndHasHalfTimeAndFullTime() {
        for (EcsMatchResult r : List.of(dry, rain)) {
            MatchEventLog log = r.events();
            assertTrue(log.size() > 20, counts(log).toString());
            MatchEvent first = log.get(0);
            assertEquals(MatchEventType.KICK_OFF, first.type());
            assertEquals(Team.HOME, first.side());
            assertEquals(MatchEventLevel.KEY, first.level());
            assertEquals(1, first.minute());
            List<MatchEvent> ht = log.of(MatchEventType.HALF_TIME);
            List<MatchEvent> ft = log.of(MatchEventType.FULL_TIME);
            assertEquals(1, ht.size());
            assertEquals(1, ft.size());
            assertEquals(45, ht.get(0).minute());
            assertEquals(90, ft.get(0).minute());
            assertEquals(r.homeGoals(), ft.get(0).homeScore());
            assertEquals(r.awayGoals(), ft.get(0).awayScore());
            long goalsBeforeHalf = log.of(MatchEventType.GOAL).stream().filter(g -> g.minute() <= 45).count();
            assertEquals(goalsBeforeHalf, ht.get(0).homeScore() + ht.get(0).awayScore());
            // the second half opens with a key kick-off too; kick-offs after goals are full-level only
            long keyKickOffs = log.of(MatchEventType.KICK_OFF).stream()
                    .filter(k -> k.level() == MatchEventLevel.KEY).count();
            assertEquals(2, keyKickOffs);
            assertEquals(log.count(MatchEventType.KICK_OFF), 2 + log.count(MatchEventType.GOAL), counts(log)
                    .toString());
        }
    }

    @Test
    void minutesRunInTickOrderAndTheFiltersNest() {
        for (EcsMatchResult r : List.of(dry, rain)) {
            MatchEventLog log = r.events();
            long prev = -1;
            for (MatchEvent e : log.events()) {
                assertNotNull(e.type());
                assertTrue(e.tick() >= prev);
                assertTrue(e.minute() >= 1 && e.minute() <= 90, e.toString());
                assertEquals(e.minute() <= 45, e.tick() < 81_000, e.toString());
                prev = e.tick();
            }
            List<MatchEvent> key = log.filtered(MatchEventLevel.KEY);
            List<MatchEvent> extended = log.filtered(MatchEventLevel.EXTENDED);
            List<MatchEvent> full = log.filtered(MatchEventLevel.FULL);
            assertTrue(extended.containsAll(key) && full.containsAll(extended));
            assertTrue(key.size() < extended.size() && extended.size() <= full.size());
            assertEquals(log.size(), full.size());
        }
    }

    @Test
    void shotsAndSavesAndAppealsAreSeen() {
        Map<MatchEventType, Integer> both = counts(rain.events());
        counts(dry.events()).forEach((k, v) -> both.merge(k, v, Integer::sum));
        assertTrue(both.getOrDefault(MatchEventType.SHOT, 0) >= both.getOrDefault(MatchEventType.GOAL, 0),
                both.toString());
        assertTrue(both.getOrDefault(MatchEventType.SHOT, 0) > 0, both.toString());
        assertTrue(both.getOrDefault(MatchEventType.OFFSIDE_APPEAL, 0) > 0, both.toString());
        for (MatchEvent s : dry.events().of(MatchEventType.SAVE)) {
            assertTrue(s.actor() >= 0 && s.other() >= 0 && s.side() != MatchEvent.refTeam(s.other()), s.toString());
        }
        for (MatchEvent a : dry.events().of(MatchEventType.OFFSIDE_APPEAL)) {
            assertNotNull(a.side());
            assertEquals(a.side().other(), MatchEvent.refTeam(a.actor()), a.toString());
            assertTrue(a.homeUmpireVote() >= 0 || a.awayUmpireVote() >= 0 || a.refereeVote() >= 0, a.toString());
        }
    }

    @Test
    void theRainMatchsBallGrowsHeavyAndTheDryOneDoesNot() {
        assertEquals(0, dry.events().count(MatchEventType.HEAVY_BALL));
        List<MatchEvent> heavy = rain.events().of(MatchEventType.HEAVY_BALL);
        assertEquals(1, heavy.size());
        assertTrue(heavy.get(0).data() >= 460, heavy.toString());
        assertTrue(heavy.get(0).minute() <= 45, heavy.toString());
        assertEquals(Surface.WET.ordinal(), rain.events().surface());
        assertEquals(PitchConditions.RAIN, rain.events().wetness(), 0.001);
    }

    @Test
    void theLogIsDeterministicAndSurvivesItsSavedForm() {
        EcsMatchResult again = new EcsMatchEngine().play(
                new EcsMatchInput("m1", new UniformPitch(Surface.DRY, PitchConditions.DRY)),
                RandomSource.seeded(1888L).fork("match:m1"));
        assertEquals(dry.events(), again.events());
        for (EcsMatchResult r : List.of(dry, rain)) {
            String saved = r.events().encode();
            assertTrue(saved.length() < 16 * r.events().size() + 32, saved.length() + " chars");
            MatchEventLog back = MatchEventLog.decode(saved);
            assertEquals(r.events(), back);
            assertEquals(r.events().events(), back.events());
        }
        assertNull(MatchEventLog.decode("E9:xyz"));
        assertNull(MatchEventLog.decode("E1:!!"));
        assertFalse(dry.events().events().isEmpty());
        System.out.println("E-17 dry " + counts(dry.events()) + " " + dry.events().encode().length() + " chars");
        System.out.println("E-17 rain " + counts(rain.events()) + " " + rain.events().encode().length() + " chars");
    }
}
