package com.rndmodgames.futtoboru.engine.simulation;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

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
    private Random random;
    
    public MatchSimulator(Futtoboru gameInstance) {
        this.currentGame = gameInstance.getCurrentGame();
        this.random = new Random();
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
        
        // Get clubs
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            System.err.println("MatchSimulator: Cannot simulate match - club not found");
            return false;
        }
        
        // Calculate team strengths
        double homeStrength = calculateTeamStrength(homeClub);
        double awayStrength = calculateTeamStrength(awayClub);
        
        // Apply home advantage
        homeStrength *= HOME_ADVANTAGE_MULTIPLIER;
        
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
        generateScorers(match, homeClub, awayClub, homeScore, awayScore);
        
        // Calculate attendance (simple: based on stadium capacity if available)
        if (homeClub.getStadium() != null && homeClub.getStadium().getCapacity() != null) {
            // 60-90% attendance for v1.0
            int capacity = homeClub.getStadium().getCapacity();
            int attendance = (int)(capacity * (0.6 + random.nextDouble() * 0.3));
            match.setAttendance(attendance);
        } else {
            // Default attendance if no stadium data
            match.setAttendance(1000 + random.nextInt(4000)); // 1000-5000
        }
        
        System.out.println("MatchSimulator: " + homeClub.getName() + " " + homeScore + 
                          " - " + awayScore + " " + awayClub.getName());
        
        // Update club statistics
        updateClubStatistics(match, homeClub, awayClub, homeScore, awayScore);
        
        // Advance winner to next round if this is a cup match (v1.0 - Complete Bracket System)
        if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH && 
            match.getCompetitionEditionId() != null) {
            System.out.println("MatchSimulator: Processing cup match completion - " + 
                (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                " (Round " + match.getRound() + ", ID: " + match.getId() + ")");
            
            com.rndmodgames.futtoboru.engine.cup.CupBracketManager bracketManager = 
                new com.rndmodgames.futtoboru.engine.cup.CupBracketManager(currentGame);
            Match nextRoundMatch = bracketManager.advanceWinner(match);
            
            if (nextRoundMatch == null) {
                if (match.getRound() != null && match.getRound() >= 5) {
                    // This was the final - cup is complete
                    System.out.println("MatchSimulator: *** CUP FINAL COMPLETED *** Champion determined!");
                } else {
                    System.out.println("MatchSimulator: *** WARNING *** Cup match " + match.getBracketPath() + 
                        " (Round " + match.getRound() + ") completed but NO NEXT ROUND MATCH FOUND!");
                    System.out.println("MatchSimulator: This should not happen - bracket should have all rounds generated upfront!");
                }
            } else {
                System.out.println("MatchSimulator: *** WINNER ADVANCED *** to " + 
                    (nextRoundMatch.getBracketPath() != null ? nextRoundMatch.getBracketPath() : "null") + 
                    " (Round " + nextRoundMatch.getRound() + ", ID: " + nextRoundMatch.getId() + 
                    "). Match ready: " + (nextRoundMatch.getHomeClubId() != null && nextRoundMatch.getAwayClubId() != null) + 
                    ", Home: " + nextRoundMatch.getHomeClubId() + ", Away: " + nextRoundMatch.getAwayClubId() + 
                    ", Date: " + nextRoundMatch.getMatchDateTime());
                
                // If this was Round 1 and Round 2 match is now ready, log it prominently
                if (match.getRound() != null && match.getRound() == 1 && 
                    nextRoundMatch.getRound() != null && nextRoundMatch.getRound() == 2 &&
                    nextRoundMatch.getHomeClubId() != null && nextRoundMatch.getAwayClubId() != null) {
                    System.out.println("MatchSimulator: *** ROUND 2 MATCH NOW READY *** " + nextRoundMatch.getBracketPath() + 
                        " will be played on " + nextRoundMatch.getMatchDateTime());
                }
            }
        }
        
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
                Player scorer = pickRandomScorer(homeClub.getPlayers());
                if (scorer != null && scorer.getPerson() != null) {
                    match.getHomeScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
        
        // Generate away scorers
        if (awayScore > 0 && awayClub.getPlayers() != null && !awayClub.getPlayers().isEmpty()) {
            for (int i = 0; i < awayScore; i++) {
                Player scorer = pickRandomScorer(awayClub.getPlayers());
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
     * Update club statistics after a match
     * 
     * @param match The match that was played
     * @param homeClub Home club
     * @param awayClub Away club
     * @param homeScore Home team score
     * @param awayScore Away team score
     */
    private void updateClubStatistics(Match match, Club homeClub, Club awayClub, int homeScore, int awayScore) {
        if (homeClub == null || awayClub == null) {
            return;
        }
        
        // Update matches played
        homeClub.setMatchesPlayed(homeClub.getMatchesPlayed() + 1);
        awayClub.setMatchesPlayed(awayClub.getMatchesPlayed() + 1);
        
        // Update goals scored and conceded
        homeClub.setGoalsScored(homeClub.getGoalsScored() + homeScore);
        homeClub.setGoalsConceded(homeClub.getGoalsConceded() + awayScore);
        awayClub.setGoalsScored(awayClub.getGoalsScored() + awayScore);
        awayClub.setGoalsConceded(awayClub.getGoalsConceded() + homeScore);
        
        // Determine result and update wins/draws/losses
        // Points are only awarded for league matches, not friendlies or cups
        boolean isLeagueMatch = match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH;
        
        // Get competition rules if this is a league match
        CompetitionRules rules = null;
        if (isLeagueMatch) {
            League league = findLeagueForMatch(match);
            if (league != null) {
                rules = league.getRulesOrDefault();
            } else {
                // Fallback to default rules if league not found
                rules = CompetitionRules.createDefaultRules();
            }
        }
        
        if (homeScore > awayScore) {
            // Home win
            homeClub.setMatchesWon(homeClub.getMatchesWon() + 1);
            if (isLeagueMatch && rules != null) {
                homeClub.setPoints(homeClub.getPoints() + rules.getPointsForWin());
                awayClub.setPoints(awayClub.getPoints() + rules.getPointsForLoss());
            }
            awayClub.setMatchesLost(awayClub.getMatchesLost() + 1);
        } else if (awayScore > homeScore) {
            // Away win
            awayClub.setMatchesWon(awayClub.getMatchesWon() + 1);
            if (isLeagueMatch && rules != null) {
                awayClub.setPoints(awayClub.getPoints() + rules.getPointsForWin());
                homeClub.setPoints(homeClub.getPoints() + rules.getPointsForLoss());
            }
            homeClub.setMatchesLost(homeClub.getMatchesLost() + 1);
        } else {
            // Draw
            homeClub.setMatchesDrawn(homeClub.getMatchesDrawn() + 1);
            if (isLeagueMatch && rules != null) {
                homeClub.setPoints(homeClub.getPoints() + rules.getPointsForDraw());
                awayClub.setPoints(awayClub.getPoints() + rules.getPointsForDraw());
            }
            awayClub.setMatchesDrawn(awayClub.getMatchesDrawn() + 1);
        }
    }
    
    /**
     * Calculate team strength based on average player attributes
     * 
     * For v1.0: Simple average of key attributes
     * Future: More sophisticated calculation with position weights
     */
    private double calculateTeamStrength(Club club) {
        if (club == null || club.getPlayers() == null || club.getPlayers().isEmpty()) {
            // Default strength if no players
            return 50.0;
        }
        
        double totalStrength = 0.0;
        int playerCount = 0;
        
        for (Player player : club.getPlayers()) {
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
    
    /**
     * Find the league for a given match
     * Searches through all leagues to find one that contains both clubs
     * 
     * @param match The match to find the league for
     * @return The league containing both clubs, or null if not found
     */
    private League findLeagueForMatch(Match match) {
        if (match == null || currentGame == null) {
            return null;
        }
        
        // Get both clubs
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            return null;
        }
        
        // Search through all leagues to find one containing both clubs
        if (currentGame.getMainAuthority() != null && currentGame.getMainAuthority().getLeagues() != null) {
            for (League league : currentGame.getMainAuthority().getLeagues()) {
                if (league == null || league.getLeagueClubs() == null) {
                    continue;
                }
                
                boolean hasHomeClub = false;
                boolean hasAwayClub = false;
                
                for (Club club : league.getLeagueClubs()) {
                    if (club != null && club.getId() != null) {
                        if (club.getId().equals(homeClub.getId())) {
                            hasHomeClub = true;
                        }
                        if (club.getId().equals(awayClub.getId())) {
                            hasAwayClub = true;
                        }
                    }
                }
                
                // If both clubs are in this league, this is the league for the match
                if (hasHomeClub && hasAwayClub) {
                    return league;
                }
            }
        }
        
        return null;
    }
}

