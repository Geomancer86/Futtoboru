package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.cup.NeutralVenues;
import com.rndmodgames.futtoboru.engine.injuries.InjuryRules;
import com.rndmodgames.futtoboru.engine.injuries.MatchInjuries;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Plays one match of the game (P2-2, E-17a): freezes both team sheets on the match at kick-off (G-01,
 * {@link TeamSheets#kickOff}), asks its {@link MatchEngine} for the score and scorers ({@link InstantMatchEngine} by
 * default, the ECS engine with {@code -Dfuttoboru.match.engine=ecs}, see {@link MatchEngines}), writes them into the
 * {@link Match}, then adds the goal minutes and the gate. Everything that follows from a result is
 * {@link MatchResultRecorder#record(Match)}.
 *
 * @author Geomancer86
 */
public class MatchSimulator {

    private final SaveGame currentGame;
    private final RandomSource root;
    private final MatchEngine engine;

    /** Draws from the game's random source ({@code game.random()}) with the selected engine. */
    public MatchSimulator(Futtoboru gameInstance) {
        this(gameInstance, Futtoboru.randomOf(gameInstance));
    }

    /**
     * @param root each match draws from {@code root.fork("match:" + id)}, its gate from {@code fork("gate:" + id)},
     *             its goal minutes from {@code fork("minutes:" + id)} (G-03), its injuries from
     *             {@code fork("injuries:" + id)} (G-02) and its weather from
     *             {@code fork("weather:" + date + ":" + ground)}, so a result depends on the seed and the match, not on
     *             the order the day's matches are played
     */
    public MatchSimulator(Futtoboru gameInstance, RandomSource root) {
        this(gameInstance, root, MatchEngines.selected());
    }

    public MatchSimulator(Futtoboru gameInstance, RandomSource root, MatchEngine engine) {
        this.currentGame = gameInstance.getCurrentGame();
        this.root = root;
        this.engine = engine;
    }

    /** The engine deciding this simulator's results. */
    public MatchEngine engine() {
        return engine;
    }

    /** A match readied for its engine (fixture built, kick-off sheets and injuries written on the match). */
    public static final class Prepared {
        private final Match match;
        private final Club homeClub;
        private final Club awayClub;
        private final String matchKey;
        private final boolean neutral;
        private final MatchFixture fixture;

        private Prepared(Match match, Club homeClub, Club awayClub, String matchKey, boolean neutral,
                MatchFixture fixture) {
            this.match = match;
            this.homeClub = homeClub;
            this.awayClub = awayClub;
            this.matchKey = matchKey;
            this.neutral = neutral;
            this.fixture = fixture;
        }

        public Match match() {
            return match;
        }
    }

    /** Simulate a match and generate result: prepare, compute, apply. */
    public boolean simulateMatch(Match match) {
        Prepared prepared = prepare(match);
        if (prepared == null) {
            return false;
        }
        apply(prepared, compute(prepared));
        return true;
    }

    /**
     * Step 1 (single thread): validates the match, freezes the team sheets and rolls the injuries on it, builds the
     * fixture. Null when the match cannot be played.
     */
    public Prepared prepare(Match match) {
        if (match == null || match.getIsPlayed()) {
            return null; // Match already played or invalid
        }

        // CRITICAL: Validate teams exist before simulation
        if (match.getHomeClubId() == null || match.getAwayClubId() == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - missing teams (Home: " +
                match.getHomeClubId() + ", Away: " + match.getAwayClubId() + ")");
            return null;
        }

        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());

        if (homeClub == null || awayClub == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - club not found (Home ID: " +
                match.getHomeClubId() + ", Away ID: " + match.getAwayClubId() + ")");
            return null;
        }

        // One stream per match (P2-0)
        String matchKey = match.getId() != null ? String.valueOf(match.getId())
                : match.getHomeClubId() + "-" + match.getAwayClubId() + "@" + match.getMatchDateTime();

        // G-01: the team sheets at kick-off, frozen on the match
        TeamSheet homeSheet = TeamSheets.kickOff(match, homeClub, true, currentGame, root);
        TeamSheet awaySheet = TeamSheets.kickOff(match, awayClub, false, currentGame, root);

        // G-02 (D19-3): men hurt during the match, on their own stream (the other draws stay the same); the engine
        // weighs them (the instant result: a passenger is worth less from his minute on, a man carried off nothing)
        InjuryRules injuryRules = InjuryRules.current();
        RandomSource injuries = root.fork("injuries:" + matchKey);
        match.setHomeInjuries(MatchInjuries.roll(InstantMatchEngine.onField(homeSheet, homeClub), injuryRules,
                injuries.fork("home")));
        match.setAwayInjuries(MatchInjuries.roll(InstantMatchEngine.onField(awaySheet, awayClub), injuryRules,
                injuries.fork("away")));

        // P2-14: a neutral ground (cup semi-final or final) has no home advantage and its own weather
        boolean neutral = NeutralVenues.isNeutral(match);
        String groundKey = neutral && match.getVenue() != null ? match.getVenue() : MatchWeather.groundOf(homeClub);
        MatchWeather weather = MatchWeather.of(root,
                match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate() : null, groundKey);
        MatchFixture fixture = new MatchFixture(matchKey, homeClub, awayClub, match.getMatchDateTime(),
                weather, neutral, homeSheet, awaySheet, match.getHomeInjuries(), match.getAwayInjuries(), injuryRules);
        return new Prepared(match, homeClub, awayClub, matchKey, neutral, fixture);
    }

    /**
     * Step 2 (any thread, E-19a): the engine's outcome for a prepared match. Reads only the fixture and the match's own
     * random stream, so the day's matches may be computed in any order or in parallel with the same results.
     */
    public MatchOutcome compute(Prepared p) {
        return engine.play(p.fixture, root.fork("match:" + p.matchKey));
    }

    /**
     * The outcomes of a day's prepared matches, in the same order. The ECS engine (slow, pure per match) is computed in
     * parallel on a shared daemon pool of {@code min(matches, processors - 1)} threads; the instant engine, or a single
     * match or processor, stays on the calling thread. Same results either way (E-19a).
     */
    public List<MatchOutcome> computeAll(List<Prepared> prepared) {
        int n = prepared.size();
        int threads = Math.min(n, Runtime.getRuntime().availableProcessors() - 1);
        if (!MatchEngine.ECS.equals(engine.id()) || threads < 2) {
            List<MatchOutcome> out = new ArrayList<>(n);
            for (Prepared p : prepared) {
                out.add(compute(p));
            }
            return out;
        }
        ExecutorService pool = pool();
        List<Future<MatchOutcome>> futures = new ArrayList<>(n);
        // never more tasks in flight than threads: the shared pool may be larger than this day needs
        Semaphore gate = new Semaphore(threads);
        for (Prepared p : prepared) {
            futures.add(pool.submit(() -> {
                gate.acquire();
                try {
                    return compute(p);
                } finally {
                    gate.release();
                }
            }));
        }
        List<MatchOutcome> out = new ArrayList<>(n);
        for (Future<MatchOutcome> f : futures) {
            try {
                out.add(f.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while playing the match day", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("match failed on the pool", e.getCause());
            }
        }
        return out;
    }

    private static ExecutorService sharedPool;

    /** Created once, sized to the machine (processors - 1, at least 2), daemon threads. */
    private static synchronized ExecutorService pool() {
        if (sharedPool == null) {
            int size = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
            AtomicInteger id = new AtomicInteger();
            sharedPool = Executors.newFixedThreadPool(size, r -> {
                Thread t = new Thread(r, "match-day-" + id.incrementAndGet());
                t.setDaemon(true);
                return t;
            });
        }
        return sharedPool;
    }

    /** Step 3 (single thread, in the day's order): writes the outcome and the gate on the match. */
    public void apply(Prepared p, MatchOutcome outcome) {
        Match match = p.match;
        Club homeClub = p.homeClub;
        Club awayClub = p.awayClub;
        String matchKey = p.matchKey;
        boolean neutral = p.neutral;
        RandomSource gate = root.fork("gate:" + matchKey);

        match.setHomeGoals(outcome.homeGoals());
        match.setAwayGoals(outcome.awayGoals());
        match.setIsPlayed(true);
        match.getHomeScorerIds().clear();
        match.getAwayScorerIds().clear();
        match.getHomeScorerIds().addAll(outcome.homeScorerIds());
        match.getAwayScorerIds().addAll(outcome.awayScorerIds());

        // Goal minutes: the engine's own (E-17, ECS events) when it timed its goals, else drawn (G-03) after the
        // scorers, on their own stream, so scores and scorers never depend on them
        if (outcome.hasGoalMinutes()) {
            match.setHomeGoalMinutes(new java.util.ArrayList<>(outcome.homeGoalMinutes()));
            match.setAwayGoalMinutes(new java.util.ArrayList<>(outcome.awayGoalMinutes()));
        } else {
            GoalMinutes.assign(match, root.fork("minutes:" + matchKey));
        }
        // E-17 (D19-9): every event, compact, for the ticker and the report (null from the instant engine)
        match.setEventLog(outcome.eventLog());

        // Calculate attendance (simple: based on the capacity of the ground played at, P2-14: a neutral venue)
        Stadium ground = neutral ? DatabaseLoader.getGroundByDataId(match.getVenue()) : null;
        if (ground == null || ground.getCapacity() == null) {
            ground = homeClub.getStadium();
        }
        if (ground != null && ground.getCapacity() != null) {
            // 60-90% attendance for v1.0
            int capacity = ground.getCapacity();
            int attendance = (int)(capacity * (0.6 + gate.nextDouble() * 0.3));
            match.setAttendance(attendance);
        } else {
            // Default attendance if no stadium data
            match.setAttendance(1000 + gate.nextInt(4000)); // 1000-5000
        }

        Log.info(Log.ENGINE_MATCH, "MatchSimulator: " + homeClub.getName() + " " + outcome.homeGoals() +
                " - " + outcome.awayGoals() + " " + awayClub.getName() + " (" + engine.id() + ")");

        // Club statistics, points, cup advancement and match lists: MatchResultRecorder.record (P2-2)
    }
}
