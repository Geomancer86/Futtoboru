package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * E-19a: a match day on the ECS engine computed on a thread pool gives the results of the sequential run.
 */
@Tag("slow")
public class MatchDayPoolTests extends BaseTest {

    private static final int CLUBS = 12;

    private List<Match> day(EngineTestWorld world) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < CLUBS; i += 2) {
            matches.add(world.leagueMatch(world.club(i), world.club(i + 1)));
        }
        return matches;
    }

    @Test
    void parallelOutcomesEqualSequentialRepeatedly() {
        EngineTestWorld world = new EngineTestWorld(application, CLUBS);
        MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(1888), new EcsMatchEngineAdapter());
        List<MatchSimulator.Prepared> prepared = new ArrayList<>();
        for (Match m : day(world)) {
            MatchSimulator.Prepared p = sim.prepare(m);
            assertNotNull(p);
            prepared.add(p);
        }
        long t0 = System.nanoTime();
        List<MatchOutcome> sequential = new ArrayList<>();
        for (MatchSimulator.Prepared p : prepared) {
            sequential.add(sim.compute(p));
        }
        long seqMs = (System.nanoTime() - t0) / 1_000_000;
        long parMs = Long.MAX_VALUE;
        for (int run = 0; run < 8; run++) {
            long t = System.nanoTime();
            List<MatchOutcome> parallel = sim.computeAll(prepared);
            parMs = Math.min(parMs, (System.nanoTime() - t) / 1_000_000);
            assertEquals(sequential, parallel, "run " + run);
        }
        System.out.println("E-19a matchday of " + prepared.size() + " ECS matches: sequential " + seqMs
                + " ms, parallel (best of 8) " + parMs + " ms, " + Runtime.getRuntime().availableProcessors()
                + " processors");
    }

    @Test
    void appliedResultsEqualTheSequentialPath() {
        List<String> sequential = results(false);
        List<String> parallel = results(true);
        assertEquals(sequential, parallel);
    }

    private List<String> results(boolean pooled) {
        EngineTestWorld world = new EngineTestWorld(application, CLUBS);
        MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(42), new EcsMatchEngineAdapter());
        MatchResultRecorder recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
        List<Match> matches = day(world);
        if (pooled) {
            List<MatchSimulator.Prepared> prepared = new ArrayList<>();
            for (Match m : matches) {
                prepared.add(sim.prepare(m));
            }
            List<MatchOutcome> outcomes = sim.computeAll(prepared);
            for (int i = 0; i < prepared.size(); i++) {
                sim.apply(prepared.get(i), outcomes.get(i));
                recorder.record(matches.get(i));
            }
        } else {
            for (Match m : matches) {
                recorder.play(m, sim);
            }
        }
        List<String> out = new ArrayList<>();
        for (Match m : matches) {
            out.add(m.getHomeGoals() + "-" + m.getAwayGoals() + " " + m.getHomeScorerIds() + m.getAwayScorerIds()
                    + " " + m.getHomeGoalMinutes() + m.getAwayGoalMinutes() + " att=" + m.getAttendance() + " "
                    + m.getEventLog());
        }
        for (var c : world.clubs) {
            out.add(c.getName() + " " + c.getPoints() + " " + c.getGoalsScored());
        }
        return out;
    }
}
