package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * E-21f: a match watched (some ticks one by one, as the viewer plays it, the rest skipped) is recorded exactly as the
 * unwatched path records it for the same seed: score, scorers, minutes, events, gate and the clubs' table lines.
 */
@Tag("slow")
public class WatchedMatchTests extends BaseTest {

    private String played(boolean watched) {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(7), new EcsMatchEngineAdapter());
        MatchResultRecorder recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
        Match match = world.leagueMatch(world.club(0), world.club(1));
        if (watched) {
            MatchSimulator.Prepared prepared = sim.prepare(match);
            assertNotNull(prepared);
            WatchedMatch w = sim.watch(prepared);
            assertNotNull(w);
            for (int i = 0; i < 3000; i++) { // "watched": the ticks the viewer's clock runs
                w.live().tick();
            }
            assertTrue(!w.live().finished());
            w.playToEnd(); // "skip to result"
            assertTrue(w.live().finished());
            sim.apply(prepared, w.outcome());
            recorder.record(match);
        } else {
            assertTrue(recorder.play(match, sim));
        }
        return match.getHomeGoals() + "-" + match.getAwayGoals() + " " + match.getHomeScorerIds()
                + match.getAwayScorerIds() + " " + match.getHomeGoalMinutes() + match.getAwayGoalMinutes() + " att="
                + match.getAttendance() + " " + match.getEventLog() + " " + world.club(0).getPoints() + " "
                + world.club(1).getPoints() + " " + world.club(0).getGoalsScored();
    }

    @Test
    void watchedAndSkippedMatchIsRecordedAsTheUnwatchedOne() {
        assertEquals(played(false), played(true));
    }

    @Test
    void theInstantEngineHasNothingToWatch() {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(7), new InstantMatchEngine());
        MatchSimulator.Prepared prepared = sim.prepare(world.leagueMatch(world.club(0), world.club(1)));
        assertNotNull(prepared);
        assertNull(sim.watch(prepared));
    }
}
