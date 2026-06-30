package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.tactics.LineupStrengthCalculator;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Match Simulator v2.0
 *
 * Simulates football matches using starting XI lineups and formation slot weights (MVP-3).
 * Never uses full-squad average for strength.
 */
public class MatchSimulator {

    public static final double HOME_ADVANTAGE_MULTIPLIER = 1.15;
    public static final double V2_RANDOMNESS_MIN = 0.85;
    public static final double V2_RANDOMNESS_MAX = 1.15;
    private static final int MAX_GOALS = 5;

    private SaveGame currentGame;
    private Futtoboru gameInstance;
    private Random random;

    private MatchLineup lastHomeLineup;
    private MatchLineup lastAwayLineup;

    public MatchSimulator(Futtoboru gameInstance) {
        this(gameInstance, new Random());
    }

    public MatchSimulator(Futtoboru gameInstance, Random random) {
        this.currentGame = gameInstance.getCurrentGame();
        this.gameInstance = gameInstance;
        this.random = random != null ? random : new Random();
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
            Gdx.app.error("MatchSimulator", "Cannot simulate match - missing teams (Home: " + 
                match.getHomeClubId() + ", Away: " + match.getAwayClubId() + ")");
            System.err.println("MatchSimulator: Cannot simulate match - missing teams");
            return false;
        }
        
        // Get clubs
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            Gdx.app.error("MatchSimulator", "Cannot simulate match - club not found (Home ID: " + 
                match.getHomeClubId() + ", Away ID: " + match.getAwayClubId() + ")");
            System.err.println("MatchSimulator: Cannot simulate match - club not found");
            return false;
        }

        MatchLineup homeLineup = MatchLineupBuilder.buildForClub(homeClub, match);
        MatchLineup awayLineup = MatchLineupBuilder.buildForClub(awayClub, match);
        lastHomeLineup = homeLineup;
        lastAwayLineup = awayLineup;
        recordLineupOnMatch(match, homeLineup, awayLineup);

        double homeStrength = LineupStrengthCalculator.calculate(homeLineup, homeClub) * HOME_ADVANTAGE_MULTIPLIER;
        double awayStrength = LineupStrengthCalculator.calculate(awayLineup, awayClub);
        double homeRandom = V2_RANDOMNESS_MIN + (random.nextDouble() * (V2_RANDOMNESS_MAX - V2_RANDOMNESS_MIN));
        double awayRandom = V2_RANDOMNESS_MIN + (random.nextDouble() * (V2_RANDOMNESS_MAX - V2_RANDOMNESS_MIN));
        int homeScore = generateScore(homeStrength * homeRandom, awayStrength * awayRandom, random);
        int awayScore = generateScore(awayStrength * awayRandom, homeStrength * homeRandom, random);
        
        // Update match with results
        match.setHomeGoals(homeScore);
        match.setAwayGoals(awayScore);
        match.setIsPlayed(true);
        
        // Generate scorers from starting XI (MVP-3)
        generateScorers(match, homeLineup, homeClub, awayLineup, awayClub, homeScore, awayScore);
        
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
            
            // Pass gameInstance to CupBracketManager so it can trigger cup completion
            com.rndmodgames.futtoboru.engine.cup.CupBracketManager bracketManager = 
                new com.rndmodgames.futtoboru.engine.cup.CupBracketManager(currentGame, gameInstance);
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

    public MatchLineup getLastHomeLineup() {
        return lastHomeLineup;
    }

    public MatchLineup getLastAwayLineup() {
        return lastAwayLineup;
    }

    /**
     * Score-only simulation for tests (no club stat side effects).
     */
    public static int[] simulateScore(MatchLineup homeLineup, Club homeClub,
            MatchLineup awayLineup, Club awayClub, Random random) {
        Random rng = random != null ? random : new Random();
        double homeStrength = LineupStrengthCalculator.calculate(homeLineup, homeClub) * HOME_ADVANTAGE_MULTIPLIER;
        double awayStrength = LineupStrengthCalculator.calculate(awayLineup, awayClub);
        double homeRandom = V2_RANDOMNESS_MIN + (rng.nextDouble() * (V2_RANDOMNESS_MAX - V2_RANDOMNESS_MIN));
        double awayRandom = V2_RANDOMNESS_MIN + (rng.nextDouble() * (V2_RANDOMNESS_MAX - V2_RANDOMNESS_MIN));
        int homeScore = generateScore(homeStrength * homeRandom, awayStrength * awayRandom, rng);
        int awayScore = generateScore(awayStrength * awayRandom, homeStrength * homeRandom, rng);
        return new int[] { homeScore, awayScore };
    }

    private static void recordLineupOnMatch(Match match, MatchLineup homeLineup, MatchLineup awayLineup) {
        if (match == null) {
            return;
        }
        if (homeLineup != null) {
            match.setHomeLineupPlayerIds(homeLineup.getPlayerIdsInSlotOrder());
            match.setHomeFormationTemplateId(homeLineup.getFormationTemplateId());
        }
        if (awayLineup != null) {
            match.setAwayLineupPlayerIds(awayLineup.getPlayerIdsInSlotOrder());
            match.setAwayFormationTemplateId(awayLineup.getFormationTemplateId());
        }
    }
    
    /**
     * Generate goal scorers from lineups (forwards weighted higher).
     */
    private void generateScorers(Match match, MatchLineup homeLineup, Club homeClub,
            MatchLineup awayLineup, Club awayClub, int homeScore, int awayScore) {
        match.getHomeScorerIds().clear();
        match.getAwayScorerIds().clear();

        for (int i = 0; i < homeScore; i++) {
            Player scorer = pickWeightedScorer(homeLineup, homeClub);
            if (scorer != null && scorer.getPerson() != null) {
                match.getHomeScorerIds().add(scorer.getPerson().getId());
            }
        }
        for (int i = 0; i < awayScore; i++) {
            Player scorer = pickWeightedScorer(awayLineup, awayClub);
            if (scorer != null && scorer.getPerson() != null) {
                match.getAwayScorerIds().add(scorer.getPerson().getId());
            }
        }
    }

    private Player pickWeightedScorer(MatchLineup lineup, Club club) {
        List<MatchLineupBuilder.PlayerRef> players = MatchLineupBuilder.playersInLineup(lineup, club);
        if (players.isEmpty()) {
            return null;
        }
        double totalWeight = 0.0;
        for (MatchLineupBuilder.PlayerRef ref : players) {
            totalWeight += scorerWeight(ref.role);
        }
        if (totalWeight <= 0) {
            return players.get(random.nextInt(players.size())).player;
        }
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (MatchLineupBuilder.PlayerRef ref : players) {
            cumulative += scorerWeight(ref.role);
            if (roll <= cumulative) {
                return ref.player;
            }
        }
        return players.get(players.size() - 1).player;
    }

    private static double scorerWeight(MatchLineupBuilder.SlotRoleRef role) {
        if (role == null) {
            return 1.0;
        }
        switch (role) {
            case FWD:
                return 3.0;
            case MID:
                return 1.5;
            case DEF:
                return 0.5;
            case GK:
                return 0.1;
            default:
                return 1.0;
        }
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
        
        // CRITICAL FIX: Only count league matches in matchesPlayed for league standings
        // Cup matches and friendlies should not be counted in league table
        boolean isLeagueMatch = match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH;
        
        // Update matches played (only for league matches)
        if (isLeagueMatch) {
            homeClub.setMatchesPlayed(homeClub.getMatchesPlayed() + 1);
            awayClub.setMatchesPlayed(awayClub.getMatchesPlayed() + 1);
        }
        
        // Update goals scored and conceded (league matches only — RB-7)
        if (isLeagueMatch) {
            homeClub.setGoalsScored(homeClub.getGoalsScored() + homeScore);
            homeClub.setGoalsConceded(homeClub.getGoalsConceded() + awayScore);
            awayClub.setGoalsScored(awayClub.getGoalsScored() + awayScore);
            awayClub.setGoalsConceded(awayClub.getGoalsConceded() + homeScore);
        }

        // Determine result and update wins/draws/losses / points (league only)
        
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
            if (isLeagueMatch && rules != null) {
                homeClub.setMatchesWon(homeClub.getMatchesWon() + 1);
                homeClub.setPoints(homeClub.getPoints() + rules.getPointsForWin());
                awayClub.setPoints(awayClub.getPoints() + rules.getPointsForLoss());
                awayClub.setMatchesLost(awayClub.getMatchesLost() + 1);
            }
        } else if (awayScore > homeScore) {
            // Away win
            if (isLeagueMatch && rules != null) {
                awayClub.setMatchesWon(awayClub.getMatchesWon() + 1);
                awayClub.setPoints(awayClub.getPoints() + rules.getPointsForWin());
                homeClub.setPoints(homeClub.getPoints() + rules.getPointsForLoss());
                homeClub.setMatchesLost(homeClub.getMatchesLost() + 1);
            }
        } else {
            // Draw
            if (isLeagueMatch && rules != null) {
                homeClub.setMatchesDrawn(homeClub.getMatchesDrawn() + 1);
                awayClub.setMatchesDrawn(awayClub.getMatchesDrawn() + 1);
                homeClub.setPoints(homeClub.getPoints() + rules.getPointsForDraw());
                awayClub.setPoints(awayClub.getPoints() + rules.getPointsForDraw());
            }
        }
    }
    
    /**
     * Generate score for a team based on strength.
     */
    private static int generateScore(double teamStrength, double opponentStrength, Random random) {
        Random rng = random != null ? random : new Random();
        // Calculate strength ratio
        double totalStrength = teamStrength + opponentStrength;
        double strengthRatio = totalStrength > 0 ? teamStrength / totalStrength : 0.5;
        
        // Base goal distribution
        double randomValue = rng.nextDouble();
        int baseGoals = 0;
        
        if (randomValue < 0.3) {
            baseGoals = 0;
        } else if (randomValue < 0.6) {
            baseGoals = 1;
        } else if (randomValue < 0.85) {
            baseGoals = 2;
        } else if (randomValue < 0.95) {
            baseGoals = 3;
        } else {
            baseGoals = 4 + rng.nextInt(2);
        }

        if (strengthRatio > 0.6) {
            baseGoals += 1;
        } else if (strengthRatio > 0.55) {
            if (rng.nextDouble() < 0.5) {
                baseGoals += 1;
            }
        } else if (strengthRatio < 0.4) {
            baseGoals = Math.max(0, baseGoals - 1);
        } else if (strengthRatio < 0.45) {
            if (rng.nextDouble() < 0.5) {
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

