package com.rndmodgames.futtoboru.engine.simulation;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.injuries.Availability;
import com.rndmodgames.futtoboru.engine.injuries.InjuryRules;
import com.rndmodgames.futtoboru.engine.injuries.MatchInjuries;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Match Simulator v1.0
 * 
 * Simulates football matches and generates results.
 * 
 * Algorithm (v1.0 - Simple):
 * - Calculate team strength based on average player attributes
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
     *             its goal minutes from {@code fork("minutes:" + id)} (G-03), its injuries from
     *             {@code fork("injuries:" + id)} (G-02),
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
        
        // G-02: only available men play (Availability); the first eleven take the field, a club that cannot raise
        // eleven plays short; injuries during the match on their own stream (the other draws stay the same)
        java.time.LocalDateTime day = currentGame.getGameDate() != null ? currentGame.getGameDate()
                : match.getMatchDateTime();
        java.util.List<Player> homeAvailable = Availability.available(homeClub, day);
        java.util.List<Player> awayAvailable = Availability.available(awayClub, day);
        java.util.List<Player> homeSide = Availability.matchSide(homeClub, day);
        java.util.List<Player> awaySide = Availability.matchSide(awayClub, day);
        InjuryRules injuryRules = InjuryRules.current();
        RandomSource injuries = root.fork("injuries:" + matchKey);
        match.setHomeInjuries(MatchInjuries.roll(homeSide, injuryRules, injuries.fork("home")));
        match.setAwayInjuries(MatchInjuries.roll(awaySide, injuryRules, injuries.fork("away")));

        // Calculate team strengths
        double homeStrength = calculateTeamStrength(homeClub, homeAvailable)
                * sideFactor(homeClub, homeSide.size(), match.getHomeInjuries(), injuryRules);
        double awayStrength = calculateTeamStrength(awayClub, awayAvailable)
                * sideFactor(awayClub, awaySide.size(), match.getAwayInjuries(), injuryRules);
        
        // Apply home advantage (P2-14: none at a neutral ground, a cup semi-final or final)
        if (!com.rndmodgames.futtoboru.engine.cup.NeutralVenues.isNeutral(match)) {
            homeStrength *= HOME_ADVANTAGE_MULTIPLIER;
        }
        
        // Add randomness
        double homeRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double awayRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        
        double homeFinal = homeStrength * homeRandom;
        double awayFinal = awayStrength * awayRandom;
        
        // Generate scores
        int homeScore = generateScore(homeFinal, awayFinal, true);
        int awayScore = generateScore(awayFinal, homeFinal, false);
        
        // Update match with results
        match.setHomeGoals(homeScore);
        match.setAwayGoals(awayScore);
        match.setIsPlayed(true);
        
        // Generate scorers (v1.0 - Awards System)
        generateScorers(match, scorerPool(homeAvailable, match.getHomeInjuries()),
                scorerPool(awayAvailable, match.getAwayInjuries()), homeScore, awayScore);

        // Goal minutes (G-03): after the scorers, on their own stream, so scores and scorers never depend on them
        GoalMinutes.assign(match, root.fork("minutes:" + matchKey));

        // Calculate attendance (simple: based on the capacity of the ground played at, P2-14: a neutral venue)
        com.rndmodgames.futtoboru.data.Stadium ground = com.rndmodgames.futtoboru.engine.cup.NeutralVenues.isNeutral(match)
                ? com.rndmodgames.futtoboru.system.DatabaseLoader.getGroundByDataId(match.getVenue()) : null;
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
        
        Log.info(Log.ENGINE_MATCH, "MatchSimulator: " + homeClub.getName() + " " + homeScore + 
                          " - " + awayScore + " " + awayClub.getName());
        
        // Club statistics, points, cup advancement and match lists: MatchResultRecorder.record (P2-2)
        
        return true;
    }
    
    /**
     * Generate goal scorers for a match
     */
    private void generateScorers(Match match, java.util.List<Player> homePlayers, java.util.List<Player> awayPlayers,
            int homeScore, int awayScore) {
        // Clear existing scorers
        match.getHomeScorerIds().clear();
        match.getAwayScorerIds().clear();
        
        // Generate home scorers
        if (homeScore > 0 && !homePlayers.isEmpty()) {
            for (int i = 0; i < homeScore; i++) {
                Player scorer = pickRandomScorer(homePlayers);
                if (scorer != null && scorer.getPerson() != null) {
                    match.getHomeScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
        
        // Generate away scorers
        if (awayScore > 0 && !awayPlayers.isEmpty()) {
            for (int i = 0; i < awayScore; i++) {
                Player scorer = pickRandomScorer(awayPlayers);
                if (scorer != null && scorer.getPerson() != null) {
                    match.getAwayScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
    }
    
    /**
     * Pick a random player to score a goal
     * Future: Weight by position and attributes
     */
    private Player pickRandomScorer(java.util.List<Player> players) {
        if (players == null || players.isEmpty()) return null;
        
        // Simple weight system: 
        // Forwards (if we had positions) would have higher weight
        // For now, just pick any random player
        return players.get(random.nextInt(players.size()));
    }
    
    /**
     * Calculate team strength based on average player attributes
     * 
     * For v1.0: Simple average of key attributes
     * Future: More sophisticated calculation with position weights
     */
    private double calculateTeamStrength(Club club, java.util.List<Player> players) {
        if (club == null || players == null || players.isEmpty()) {
            // Default strength if no players
            return 50.0;
        }
        
        double totalStrength = 0.0;
        int playerCount = 0;
        
        for (Player player : players) {
            if (player == null) continue;
            
            // Calculate player strength from key attributes
            // Using average of physical, mental, and technical attributes
            double playerStrength = 0.0;
            int attributeCount = 0;
            
            // Physical attributes
            if (player.getAcceleration() != null) {
                playerStrength += player.getAcceleration();
                attributeCount++;
            }
            if (player.getSpeed() != null) {
                playerStrength += player.getSpeed();
                attributeCount++;
            }
            if (player.getStrength() != null) {
                playerStrength += player.getStrength();
                attributeCount++;
            }
            if (player.getStamina() != null) {
                playerStrength += player.getStamina();
                attributeCount++;
            }
            
            // Mental attributes
            if (player.getDetermination() != null) {
                playerStrength += player.getDetermination();
                attributeCount++;
            }
            if (player.getPositioning() != null) {
                playerStrength += player.getPositioning();
                attributeCount++;
            }
            if (player.getTeamwork() != null) {
                playerStrength += player.getTeamwork();
                attributeCount++;
            }
            
            // Technical attributes
            if (player.getPassing() != null) {
                playerStrength += player.getPassing();
                attributeCount++;
            }
            if (player.getKicking() != null) {
                playerStrength += player.getKicking();
                attributeCount++;
            }
            if (player.getTackling() != null) {
                playerStrength += player.getTackling();
                attributeCount++;
            }
            
            if (attributeCount > 0) {
                totalStrength += (playerStrength / attributeCount);
                playerCount++;
            }
        }
        
        if (playerCount == 0) {
            return 50.0; // Default strength
        }
        
        return totalStrength / playerCount;
    }
    
    /**
     * G-02: what the side is worth next to a full, fit eleven: men on the field less what the hurt lost, over eleven
     * (1 when nobody is hurt and eleven play). A club with no players at all keeps the old default strength.
     */
    static double sideFactor(Club club, int menOnField, java.util.List<com.rndmodgames.futtoboru.data.MatchInjury> injuries,
            InjuryRules rules) {
        if (club.getPlayers() == null || club.getPlayers().isEmpty()) {
            return 1.0;
        }
        if (menOnField == Availability.SIDE && (injuries == null || injuries.isEmpty())) {
            return 1.0;
        }
        return MatchInjuries.effectiveMen(menOnField, injuries, rules) / Availability.SIDE;
    }

    /** G-02: the men who may score: the available ones, less a man carried off for good. */
    private static java.util.List<Player> scorerPool(java.util.List<Player> available,
            java.util.List<com.rndmodgames.futtoboru.data.MatchInjury> injuries) {
        if (injuries == null || injuries.isEmpty()) {
            return available;
        }
        java.util.Set<Long> gone = new java.util.HashSet<>();
        for (com.rndmodgames.futtoboru.data.MatchInjury i : injuries) {
            if (MatchInjuries.leftForGood(i) && i.getPersonId() != null) {
                gone.add(i.getPersonId());
            }
        }
        if (gone.isEmpty()) {
            return available;
        }
        java.util.List<Player> out = new java.util.ArrayList<>();
        for (Player p : available) {
            if (p.getPerson() == null || !gone.contains(p.getPerson().getId())) {
                out.add(p);
            }
        }
        return out;
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

