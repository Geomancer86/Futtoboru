package com.rndmodgames.futtoboru.match.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** The crowd sections (E-18): layout, homes and aways, the small confidence effect, a played match's moods. */
class CrowdSystemTest {

    private static MatchWorld world(int attendance, boolean neutral) {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, PitchConditions.DRY),
                RandomSource.seeded(1L));
        MatchSystems.installSimulation(w);
        CrowdSystem.spawn(w, new PitchGeometry(PitchParams.defaults1888()), attendance, neutral);
        return w;
    }

    @Test
    void sectionsSurroundThePitchAndFillWithTheAttendance() {
        CrowdSystem small = world(1000, false).engine().getSystem(CrowdSystem.class);
        CrowdSystem big = world(10000, false).engine().getSystem(CrowdSystem.class);
        assertEquals(CrowdSystem.SECTIONS, small.sections());
        CrowdComponent a = small.crowdForTest();
        CrowdComponent b = big.crowdForTest();
        for (int i = 0; i < a.count; i++) {
            assertTrue(b.density[i] > a.density[i]);
            assertTrue(b.density[i] <= 1);
            assertTrue(Math.abs(a.y[i]) >= 35 || Math.abs(a.x[i]) >= 55, "outside the pitch " + i);
        }
    }

    @Test
    void awayPeopleStandBehindOneGoalAtHomeAndSplitTheEndsWhenNeutral() {
        CrowdComponent home = world(5000, false).engine().getSystem(CrowdSystem.class).crowdForTest();
        double lo = 1;
        double hi = 0;
        for (int i = 0; i < home.count; i++) {
            lo = Math.min(lo, home.homeShare[i]);
            hi = Math.max(hi, home.homeShare[i]);
        }
        assertTrue(lo < 0.2 && hi > 0.8, lo + " " + hi);
    }

    @Test
    void aWorldWithoutACrowdIsUntouched() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, PitchConditions.DRY),
                RandomSource.seeded(1L));
        MatchSystems.installSimulation(w);
        CrowdSystem c = w.engine().getSystem(CrowdSystem.class);
        assertEquals(0, c.support(0, 40, Team.HOME), 0);
        assertEquals(0, c.moodAt(0, 40), 0);
        w.tick();
    }

    @Test
    void supportIsSmallAndFavoursTheSideTheNearSectionsCheer() {
        MatchWorld w = world(10000, false);
        CrowdSystem c = w.engine().getSystem(CrowdSystem.class);
        CrowdComponent s = c.crowdForTest();
        for (int i = 0; i < s.count; i++) {
            s.mood[i] = s.homeShare[i] > 0.5 ? 1 : 0; // the home sections cheer
        }
        double home = c.support(0, 35, Team.HOME);
        assertTrue(home > 0, "home players are lifted: " + home);
        assertEquals(-home, c.support(0, 35, Team.AWAY), 1e-12);
        assertTrue(CrowdSystem.CONFIDENCE_GAIN * Math.abs(home) <= 0.06, "a few per cent at most");
    }

    @Test
    void aPlayedMatchStirsTheCrowdAndRepeatsExactly() {
        EcsMatchEngine engine = new EcsMatchEngine();
        EcsMatchInput in = new EcsMatchInput("crowd", new UniformPitch(Surface.DRY, PitchConditions.DRY), 0, 10,
                EcsMatchInput.Side.stub(), EcsMatchInput.Side.stub(), false, 6000);
        double[] peak = new double[1];
        EcsMatchResult a = engine.play(in, RandomSource.seeded(7L).fork("match:crowd"), w -> {
            CrowdSystem c = w.engine().getSystem(CrowdSystem.class);
            for (int i = 0; i < c.sections(); i++) {
                peak[0] = Math.max(peak[0], Math.abs(c.mood(i)) + c.encroachment(i));
                assertTrue(Math.abs(c.mood(i)) <= 1 && c.encroachment(i) >= 0 && c.encroachment(i) <= 1);
            }
        });
        assertEquals(a, engine.play(in, RandomSource.seeded(7L).fork("match:crowd")));
        assertTrue(a.ballActions() > 100);
    }
}
