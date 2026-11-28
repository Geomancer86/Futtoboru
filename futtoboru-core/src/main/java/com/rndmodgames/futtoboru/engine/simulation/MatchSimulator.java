package com.rndmodgames.futtoboru.engine.simulation;

import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.engine.team.TeamStrength;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Match Simulator v1.0
 * 
 * Simulates football matches and generates results.
 * 
 * Algorithm (v1.0 - Simple; G-01 team sheets):
 * - Each side's team sheet is frozen on the match at kick-off ({@link TeamSheets#kickOff}: the player's XI, an AI
 *   club's own pick); strength comes from the eleven who play ({@link TeamStrength}: attack and defence by place,
 *   out of position and playing short cost, small shape / style / captain modifiers)
 * - Apply home advantage (15% boost)
 * - Add randomness (20-30%)
 * - Generate scores based on strength difference
 * 
 * @author Geomancer86
 */
public class MatchSimulator {
    
    private static final double HOME_ADVANTAGE_MULTIPLIER = 1.15; // 15% home advantage
    private static final double RANDOMNESS_MIN = 0.7;
    private static final double RANDOMNESS_MAX = 1.3;
    private static final int MAX_GOALS = 5; // Cap goals at 5 for v1.0
    
    private SaveGame currentGame;
    private Futtoboru gameInstance;
    private final RandomSource root;
    private RandomSource random;
    
    /** Draws from the game's random source ({@code game.random()}). */
    public MatchSimulator(Futtoboru gameInstance) {
        this(gameInstance, Futtoboru.randomOf(gameInstance));
    }
    
    /**
     * @param root each match draws from {@code root.fork("match:" + id)}, its gate from {@code fork("gate:" + id)} and
     *             its goal minutes from {@code fork("minutes:" + id)} (G-03),
     *             so a result depends on the seed and the match, not on the order the day's matches are played
     */
    public MatchSimulator(Futtoboru gameInstance, RandomSource root) {
        this.currentGame = gameInstance.getCurrentGame();
        this.gameInstance = gameInstance;
        this.root = root;
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
            Log.error(Log.ENGINE_MATCH, "MatchSimulator: Cannot simulate match - missing teams");
            return false;
        }
        
        // Get clubs
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - club not found (Home ID: " + 
                match.getHomeClubId() + ", Away ID: " + match.getAwayClubId() + ")");
            Log.error(Log.ENGINE_MATCH, "MatchSimulator: Cannot simulate match - club not found");
            return false;
        }
        
        // One stream per match (P2-0)
        String matchKey = match.getId() != null ? String.valueOf(match.getId())
                : match.getHomeClubId() + "-" + match.getAwayClubId() + "@" + match.getMatchDateTime();
        this.random = root.fork("match:" + matchKey);
        RandomSource gate = root.fork("gate:" + matchKey);
        
        // G-01: the team sheets at kick-off, frozen on the match; strength from the eleven who play
        TeamSheet homeSheet = TeamSheets.kickOff(match, homeClub, true, currentGame, root);
        TeamSheet awaySheet = TeamSheets.kickOff(match, awayClub, false, currentGame, root);
        TeamStrength homeStrength = TeamStrength.of(homeSheet, TeamSheets.squadById(homeClub));
        TeamStrength awayStrength = TeamStrength.of(awaySheet, TeamSheets.squadById(awayClub));
        
        // Apply home advantage
        double homeAdvantage = HOME_ADVANTAGE_MULTIPLIER;
        
        // Add randomness
        double homeRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double awayRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        
        double homeAttack = homeStrength.attack() * homeAdvantage * homeRandom;
        double homeDefence = homeStrength.defence() * homeAdvantage * homeRandom;
        double awayAttack = awayStrength.attack() * awayRandom;
        double awayDefence = awayStrength.defence() * awayRandom;
        
        // Generate scores: each side's attack against the other side's defence
        int homeScore = generateScore(homeAttack, awayDefence, true);
        int awayScore = generateScore(awayAttack, homeDefence, false);
        
        // Update match with results
        match.setHomeGoals(homeScore);
        match.setAwayGoals(awayScore);
        match.setIsPlayed(true);
        
        // Generate scorers (v1.0 - Awards System)
        generateScorers(match, homeClub, awayClub, homeScore, awayScore);

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
        
        Log.info(Log.ENGINE_MATCH, "MatchSimulator: " + homeClub.getName() + " " + homeScore + 
                          " - " + awayScore + " " + awayClub.getName());
        
        // Club statistics, points, cup advancement and match lists: MatchResultRecorder.record (P2-2)
        
        return true;
    }
    
    /**
     * Generate goal scorers for a match
     */
    private void generateScorers(Match match, Club homeClub, Club awayClub, int homeScore, int awayScore) {
        // Clear existing scorers
        match.getHomeScorerIds().clear();
        match.getAwayScorerIds().clear();
        
        // Generate home scorers
        if (homeScore > 0 && homeClub.getPlayers() != null && !homeClub.getPlayers().isEmpty()) {
            for (int i = 0; i < homeScore; i++) {
                Player scorer = pickScorer(match.getHomeSheet(), homeClub);
                if (scorer != null && scorer.getPerson() != null) {
                    match.getHomeScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
        
        // Generate away scorers
        if (awayScore > 0 && awayClub.getPlayers() != null && !awayClub.getPlayers().isEmpty()) {
            for (int i = 0; i < awayScore; i++) {
                Player scorer = pickScorer(match.getAwaySheet(), awayClub);
                if (scorer != null && scorer.getPerson() != null) {
                    match.getAwayScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
    }
    
    /**
     * G-01: one of the eleven who played scores, weighted by the position of his place (forwards most, the keeper
     * almost never) and his finishing; one draw per goal ({@link TeamStrength#pickScorer}).
     */
    private Player pickScorer(TeamSheet sheet, Club club) {
        Map<Long, Player> squad = TeamSheets.squadById(club);
        return TeamStrength.pickScorer(sheet, squad, random);
    }
    
    /**
     * Generate score for a team based on strength
     * 
     * Score distribution (v1.0):
     * - 30% chance: 0 goals
     * - 30% chance: 1 goal
     * - 25% chance: 2 goals
     * - 10% chance: 3 goals
     * - 5% chance: 4-5 goals
     * 
     * Adjusted by team strength vs opponent
     */
    private int generateScore(double teamStrength, double opponentStrength, boolean isHome) {
        // Calculate strength ratio
        double totalStrength = teamStrength + opponentStrength;
        double strengthRatio = totalStrength > 0 ? teamStrength / totalStrength : 0.5;
        
        // Base goal distribution
        double random = this.random.nextDouble();
        int baseGoals = 0;
        
        if (random < 0.3) {
            baseGoals = 0;      // 30% chance: 0 goals
        } else if (random < 0.6) {
            baseGoals = 1;      // 30% chance: 1 goal
        } else if (random < 0.85) {
            baseGoals = 2;      // 25% chance: 2 goals
        } else if (random < 0.95) {
            baseGoals = 3;      // 10% chance: 3 goals
        } else {
            baseGoals = 4 + this.random.nextInt(2); // 5% chance: 4-5 goals
        }
        
        // Adjust based on strength ratio
        if (strengthRatio > 0.6) {
            // Strong team: +1 goal bonus
            baseGoals += 1;
        } else if (strengthRatio > 0.55) {
            // Slightly stronger: +0.5 goal (round up 50% of the time)
            if (this.random.nextDouble() < 0.5) {
                baseGoals += 1;
            }
        } else if (strengthRatio < 0.4) {
            // Weak team: -1 goal penalty
            baseGoals = Math.max(0, baseGoals - 1);
        } else if (strengthRatio < 0.45) {
            // Slightly weaker: -0.5 goal (round down 50% of the time)
            if (this.random.nextDouble() < 0.5) {
                baseGoals = Math.max(0, baseGoals - 1);
            }
        }
        
        // Cap at maximum
        return Math.min(MAX_GOALS, baseGoals);
    }
    
}

