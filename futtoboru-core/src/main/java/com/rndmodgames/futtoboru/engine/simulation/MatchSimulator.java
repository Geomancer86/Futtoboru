package com.rndmodgames.futtoboru.engine.simulation;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Plays one match of the game (P2-2, E-17a): asks its {@link MatchEngine} for the score and scorers
 * ({@link InstantMatchEngine} by default, the ECS engine with {@code -Dfuttoboru.match.engine=ecs}, see
 * {@link MatchEngines}), writes them into the {@link Match}, then adds the goal minutes and the gate. Everything that
 * follows from a result is {@link MatchResultRecorder#record(Match)}.
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
     *             its goal minutes from {@code fork("minutes:" + id)} (G-03) and its weather from
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

    /**
     * Simulate a match and generate result
     *
     * @param match The match to simulate
     * @return true if simulation was successful, false otherwise
     */
    public boolean simulateMatch(Match match) {
        if (match == null || match.getIsPlayed()) {
            return false; // Match already played or invalid
        }

        // CRITICAL: Validate teams exist before simulation
        if (match.getHomeClubId() == null || match.getAwayClubId() == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - missing teams (Home: " +
                match.getHomeClubId() + ", Away: " + match.getAwayClubId() + ")");
            return false;
        }

        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());

        if (homeClub == null || awayClub == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - club not found (Home ID: " +
                match.getHomeClubId() + ", Away ID: " + match.getAwayClubId() + ")");
            return false;
        }

        // One stream per match (P2-0)
        String matchKey = match.getId() != null ? String.valueOf(match.getId())
                : match.getHomeClubId() + "-" + match.getAwayClubId() + "@" + match.getMatchDateTime();
        RandomSource gate = root.fork("gate:" + matchKey);
        MatchWeather weather = MatchWeather.of(root,
                match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate() : null,
                MatchWeather.groundOf(homeClub));
        MatchOutcome outcome = engine.play(
                new MatchFixture(matchKey, homeClub, awayClub, match.getMatchDateTime(), weather),
                root.fork("match:" + matchKey));

        match.setHomeGoals(outcome.homeGoals());
        match.setAwayGoals(outcome.awayGoals());
        match.setIsPlayed(true);
        match.getHomeScorerIds().clear();
        match.getAwayScorerIds().clear();
        match.getHomeScorerIds().addAll(outcome.homeScorerIds());
        match.getAwayScorerIds().addAll(outcome.awayScorerIds());

        // Goal minutes (G-03): after the scorers, on their own stream, so scores and scorers never depend on them
        GoalMinutes.assign(match, root.fork("minutes:" + matchKey));

        // Calculate attendance (simple: based on stadium capacity if available)
        if (homeClub.getStadium() != null && homeClub.getStadium().getCapacity() != null) {
            // 60-90% attendance for v1.0
            int capacity = homeClub.getStadium().getCapacity();
            int attendance = (int)(capacity * (0.6 + gate.nextDouble() * 0.3));
            match.setAttendance(attendance);
        } else {
            // Default attendance if no stadium data
            match.setAttendance(1000 + gate.nextInt(4000)); // 1000-5000
        }

        Log.info(Log.ENGINE_MATCH, "MatchSimulator: " + homeClub.getName() + " " + outcome.homeGoals() +
                " - " + outcome.awayGoals() + " " + awayClub.getName() + " (" + engine.id() + ")");

        // Club statistics, points, cup advancement and match lists: MatchResultRecorder.record (P2-2)

        return true;
    }
}
