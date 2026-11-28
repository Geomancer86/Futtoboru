package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-03: goal minutes of the instant result. Every goal gets a distinct minute 1-90, each side ascending, the same for
 * the same seed and match; they come from their own stream, so scores, scorers and gates are what they were before.
 */
class GoalMinutesTests {

    private static Match played(int home, int away) {
        Match m = new Match();
        m.setHomeGoals(home);
        m.setAwayGoals(away);
        m.setIsPlayed(true);
        return m;
    }

    @Test
    void everyGoalGetsADistinctMinuteInOrder() {
        RandomSource root = RandomSource.seeded(1888);
        for (int i = 0; i < 2000; i++) {
            int home = i % 11;
            int away = (i / 11) % 8;
            Match m = played(home, away);
            GoalMinutes.assign(m, root.fork("minutes:" + i));

            assertEquals(home, m.getHomeGoalMinutes().size());
            assertEquals(away, m.getAwayGoalMinutes().size());
            assertTrue(GoalMinutes.hasMinutes(m));
            Set<Integer> all = new HashSet<>();
            for (List<Integer> side : List.of(m.getHomeGoalMinutes(), m.getAwayGoalMinutes())) {
                for (int g = 0; g < side.size(); g++) {
                    int minute = side.get(g);
                    assertTrue(minute >= 1 && minute <= GoalMinutes.FULL_TIME, "minute " + minute);
                    assertTrue(all.add(minute), "two goals in minute " + minute);
                    if (g > 0) {
                        assertTrue(side.get(g - 1) < minute, "ascending: " + side);
                    }
                }
            }
        }
    }

    @Test
    void sameSeedSameMinutesOtherSeedOthers() {
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        List<String> c = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Match ma = played(3, 2);
            Match mb = played(3, 2);
            Match mc = played(3, 2);
            GoalMinutes.assign(ma, RandomSource.seeded(7).fork("minutes:" + i));
            GoalMinutes.assign(mb, RandomSource.seeded(7).fork("minutes:" + i));
            GoalMinutes.assign(mc, RandomSource.seeded(8).fork("minutes:" + i));
            a.add(ma.getHomeGoalMinutes() + "/" + ma.getAwayGoalMinutes());
            b.add(mb.getHomeGoalMinutes() + "/" + mb.getAwayGoalMinutes());
            c.add(mc.getHomeGoalMinutes() + "/" + mc.getAwayGoalMinutes());
        }
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    /** Slightly more goals after the interval and in the last minutes of each half; none in a 91st minute. */
    @Test
    void theDistributionIsPlausible() {
        RandomSource root = RandomSource.seeded(42);
        int first = 0;
        int second = 0;
        int lastTen = 0;
        int firstTen = 0;
        for (int i = 0; i < 4000; i++) {
            Match m = played(2, 1);
            GoalMinutes.assign(m, root.fork("minutes:" + i));
            List<Integer> all = new ArrayList<>(m.getHomeGoalMinutes());
            all.addAll(m.getAwayGoalMinutes());
            for (int minute : all) {
                if (minute <= GoalMinutes.HALF) {
                    first++;
                } else {
                    second++;
                }
                lastTen += minute > 80 ? 1 : 0;
                firstTen += minute <= 10 ? 1 : 0;
            }
        }
        double share = (double) first / (first + second);
        assertTrue(share > 0.40 && share < 0.49, "first-half share " + share);
        assertTrue(lastTen > firstTen, "late goals " + lastTen + " vs early " + firstTen);
    }

    @Test
    void aGoallessMatchNeedsNoMinutesAndAnOldMatchHasNone() {
        Match goalless = played(0, 0);
        GoalMinutes.assign(goalless, RandomSource.seeded(1));
        assertTrue(goalless.getHomeGoalMinutes().isEmpty() && goalless.getAwayGoalMinutes().isEmpty());
        assertTrue(GoalMinutes.hasMinutes(goalless));

        Match old = played(2, 1); // played before G-03: no minutes stored
        assertFalse(GoalMinutes.hasMinutes(old));
    }

    // ------------------------------------------------------------------------------------- simulator streams

    /** A root that hands out another stream for the minutes only: the rest must not notice. */
    private static final class OtherMinutes implements RandomSource {
        private final RandomSource inner;

        OtherMinutes(RandomSource inner) {
            this.inner = inner;
        }

        @Override public long getSeed() { return inner.getSeed(); }
        @Override public double nextDouble() { return inner.nextDouble(); }
        @Override public float nextFloat() { return inner.nextFloat(); }
        @Override public int nextInt(int bound) { return inner.nextInt(bound); }
        @Override public int nextInt(int origin, int bound) { return inner.nextInt(origin, bound); }
        @Override public long nextLong() { return inner.nextLong(); }
        @Override public boolean nextBoolean() { return inner.nextBoolean(); }
        @Override public double nextGaussian() { return inner.nextGaussian(); }
        @Override public Random asRandom() { return inner.asRandom(); }

        @Override
        public RandomSource fork(String stream) {
            return stream.startsWith("minutes:") ? RandomSource.seeded(99).fork(stream) : inner.fork(stream);
        }
    }

    private static Futtoboru gameWithClubs() {
        SaveGame save = new SaveGame();
        Random attributes = new Random(5);
        for (int c = 0; c < 2; c++) {
            Club club = new Club("Club " + c);
            club.setId(save.getIdAllocator().nextId());
            Stadium ground = new Stadium();
            ground.setCapacity(6000);
            club.setStadium(ground);
            for (int p = 0; p < 11; p++) {
                Person person = new Person();
                person.setId(save.getIdAllocator().nextId());
                Player player = new Player();
                player.setPerson(person);
                player.setSpeed(5f + attributes.nextInt(15));
                player.setPassing(5f + attributes.nextInt(15));
                club.getPlayers().add(player);
            }
            save.getAllClubs().add(club);
        }
        Futtoboru game = new Futtoboru(2024L);
        game.setCurrentGame(save);
        return game;
    }

    private static Match fixture(SaveGame save, long id) {
        Match m = new Match();
        m.setId(id);
        m.setMatchType(Match.FRIENDLY_MATCH);
        m.setHomeClubId(save.getAllClubs().get(0).getId());
        m.setAwayClubId(save.getAllClubs().get(1).getId());
        m.setMatchDateTime(LocalDateTime.of(1888, 9, 8, 15, 0));
        return m;
    }

    @Test
    void minutesNeverChangeScoresScorersOrGates() {
        Futtoboru game = gameWithClubs();
        SaveGame save = game.getCurrentGame();
        MatchSimulator normal = new MatchSimulator(game, RandomSource.seeded(1888));
        MatchSimulator otherMinutes = new MatchSimulator(game, new OtherMinutes(RandomSource.seeded(1888)));
        int differ = 0;
        int withGoals = 0;
        for (long id = 1000; id < 1060; id++) {
            Match a = fixture(save, id);
            Match b = fixture(save, id);
            assertTrue(normal.simulateMatch(a));
            assertTrue(otherMinutes.simulateMatch(b));
            assertEquals(a.getHomeGoals(), b.getHomeGoals());
            assertEquals(a.getAwayGoals(), b.getAwayGoals());
            assertEquals(a.getHomeScorerIds(), b.getHomeScorerIds());
            assertEquals(a.getAwayScorerIds(), b.getAwayScorerIds());
            assertEquals(a.getAttendance(), b.getAttendance());
            assertTrue(GoalMinutes.hasMinutes(a), "a simulated match has its minutes");
            if (a.getHomeGoals() + a.getAwayGoals() > 0) {
                withGoals++;
                differ += a.getHomeGoalMinutes().equals(b.getHomeGoalMinutes())
                        && a.getAwayGoalMinutes().equals(b.getAwayGoalMinutes()) ? 0 : 1;
            }
        }
        assertTrue(withGoals > 20, "matches with goals: " + withGoals);
        assertTrue(differ > withGoals / 2, "the minutes come from their own stream: " + differ + " of " + withGoals);
    }
}
