package com.rndmodgames.futtoboru.engine.cup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Cup Bracket Generator v1.0
 * 
 * Generates complete tournament bracket (all rounds) upfront.
 * Creates match dependencies so winners can automatically advance.
 * 
 * Structure for 32 teams:
 * - Round 1: 16 matches (32 teams)
 * - Round 2: 8 matches (16 teams)
 * - Round 3: 4 matches (8 teams)
 * - Round 4: 2 matches (4 teams - Semi-Finals)
 * - Round 5: 1 match (2 teams - Final)
 * 
 * Total: 31 matches
 * 
 * @author Geomancer86
 */
public class CupBracketGenerator {
    
    /**
     * Generate complete bracket for a cup competition
     * 
     * @param teamIds List of team IDs participating
     * @param competitionId Competition ID
     * @param editionId Edition ID
     * @param firstRoundDate Date for first round matches
     * @param weeksBetweenRounds Weeks between each round
     * @return Complete list of all matches for all rounds
     */
    public List<Match> generateCompleteBracket(
            List<Long> teamIds,
            Long competitionId,
            Long editionId,
            LocalDateTime firstRoundDate,
            int weeksBetweenRounds) {
        
        List<Match> allMatches = new ArrayList<>();
        List<Long> teams = new ArrayList<>(teamIds);
        
        Gdx.app.log("CupBracketGenerator", "Generating complete bracket for " + teams.size() + " teams");
        
        // Handle bye if odd number of teams
        Long byeTeamId = null;
        if (teams.size() % 2 != 0) {
            int byeIndex = DatabaseLoader.RNG.nextInt(teams.size());
            byeTeamId = teams.remove(byeIndex);
            Gdx.app.log("CupBracketGenerator", "Team " + byeTeamId + " receives a BYE (advances to Round 2)");
        }
        
        // Calculate number of rounds needed
        int numTeams = teams.size();
        // For 32 teams: log2(32) = 5 rounds (16, 8, 4, 2, 1 matches)
        int numRounds = calculateNumberOfRounds(numTeams);
        
        Gdx.app.log("CupBracketGenerator", "Tournament structure: " + numTeams + " teams, " + numRounds + " rounds");
        Gdx.app.log("CupBracketGenerator", "Expected matches: Round 1=" + (numTeams/2) + 
            ", Round 2=" + (numTeams/4) + ", Round 3=" + (numTeams/8) + 
            ", Round 4=" + (numTeams/16) + ", Round 5=1 (Final)");
        
        // Generate Round 1 matches (first round)
        List<Match> previousRoundMatches = generateFirstRound(
            teams, competitionId, editionId, firstRoundDate, allMatches);
        
        // If there was a bye, create a "virtual" match for the bye team to advance
        if (byeTeamId != null) {
            // The bye team will be added to Round 2 automatically
            Gdx.app.log("CupBracketGenerator", "Bye team " + byeTeamId + " will be added to Round 2");
        }
        
        // Generate subsequent rounds
        LocalDateTime currentRoundDate = firstRoundDate.plusWeeks(weeksBetweenRounds);
        int currentRound = 2;
        
        while (currentRound <= numRounds) {
            List<Match> nextRoundMatches = generateNextRound(
                previousRoundMatches,
                competitionId,
                editionId,
                currentRound,
                currentRoundDate,
                allMatches,
                byeTeamId,
                currentRound == 2 // Only add bye to Round 2
            );
            
            previousRoundMatches = nextRoundMatches;
            currentRoundDate = currentRoundDate.plusWeeks(weeksBetweenRounds);
            currentRound++;
        }
        
        System.out.println("CupBracketGenerator: *** COMPLETE BRACKET GENERATED ***");
        System.out.println("CupBracketGenerator: Total matches: " + allMatches.size());
        
        // Log all matches by round
        Map<Integer, List<Match>> matchesByRound = new java.util.HashMap<>();
        for (Match m : allMatches) {
            if (m.getRound() != null) {
                matchesByRound.putIfAbsent(m.getRound(), new java.util.ArrayList<>());
                matchesByRound.get(m.getRound()).add(m);
            }
        }
        
        for (Map.Entry<Integer, List<Match>> entry : matchesByRound.entrySet()) {
            System.out.println("CupBracketGenerator: Round " + entry.getKey() + ": " + entry.getValue().size() + " matches");
            for (Match m : entry.getValue()) {
                System.out.println("  - " + m.getBracketPath() + " (ID: " + m.getId() + 
                    ", Parent1: " + m.getParentMatch1Id() + ", Parent2: " + m.getParentMatch2Id() + 
                    ", Date: " + m.getMatchDateTime() + ")");
            }
        }
        
        Gdx.app.log("CupBracketGenerator", "Complete bracket generated: " + allMatches.size() + " total matches");
        
        return allMatches;
    }
    
    /**
     * Generate first round matches
     */
    private List<Match> generateFirstRound(
            List<Long> teams,
            Long competitionId,
            Long editionId,
            LocalDateTime matchDate,
            List<Match> allMatches) {
        
        List<Match> roundMatches = new ArrayList<>();
        List<Long> remainingTeams = new ArrayList<>(teams);
        int matchNumber = 1;
        
        while (!remainingTeams.isEmpty() && remainingTeams.size() >= 2) {
            Match match = new Match();
            
            // Set match ID
            match.setId(System.currentTimeMillis() + matchNumber + (int)(Math.random() * 1000));
            
            // Pick random teams
            int homeIndex = DatabaseLoader.RNG.nextInt(remainingTeams.size());
            Long homeTeamId = remainingTeams.remove(homeIndex);
            
            int awayIndex = DatabaseLoader.RNG.nextInt(remainingTeams.size());
            Long awayTeamId = remainingTeams.remove(awayIndex);
            
            // Set teams
            match.setHomeClubId(homeTeamId);
            match.setAwayClubId(awayTeamId);
            
            // Set competition info
            match.setCompetitionId(competitionId);
            match.setCompetitionEditionId(editionId);
            match.setMatchType(Match.CUP_MATCH);
            match.setRound(1);
            match.setBracketPosition(matchNumber);
            match.setBracketPath("R1M" + matchNumber);
            
            // First round has no parent matches
            match.setParentMatch1Id(null);
            match.setParentMatch2Id(null);
            
            // Set match date
            match.setMatchDateTime(matchDate);
            match.setIsProposed(false);
            match.setIsAccepted(true);
            match.setIsPlayed(false);
            
            roundMatches.add(match);
            allMatches.add(match);
            
            matchNumber++;
        }
        
        Gdx.app.log("CupBracketGenerator", "Round 1: Generated " + roundMatches.size() + " matches");
        
        return roundMatches;
    }
    
    /**
     * Generate next round matches (Round 2, 3, 4, 5...)
     */
    private List<Match> generateNextRound(
            List<Match> previousRoundMatches,
            Long competitionId,
            Long editionId,
            int roundNumber,
            LocalDateTime matchDate,
            List<Match> allMatches,
            Long byeTeamId,
            boolean addByeToThisRound) {
        
        List<Match> roundMatches = new ArrayList<>();
        
        // CRITICAL FIX: Calculate exact number of matches needed
        // Each match in previous round produces 1 winner, so we need (previousRoundMatches.size() + byeCount) / 2 matches
        int winnersFromPreviousRound = previousRoundMatches.size();
        int byeCount = (addByeToThisRound && byeTeamId != null) ? 1 : 0;
        int totalWinners = winnersFromPreviousRound + byeCount;
        
        // CRITICAL: Semi-finals must have exactly 2 matches (4 teams)
        // Final must have exactly 1 match (2 teams)
        int expectedMatches = totalWinners / 2;
        
        // Validate: Semi-finals (Round 4 typically) should have exactly 2 matches
        if (roundNumber == 4 || (roundNumber >= 4 && expectedMatches == 2)) {
            if (expectedMatches != 2) {
                System.err.println("CupBracketGenerator: CRITICAL ERROR - Semi-finals should have exactly 2 matches, but calculated " + expectedMatches);
                System.err.println("  Previous round matches: " + previousRoundMatches.size());
                System.err.println("  Bye count: " + byeCount);
                System.err.println("  Total winners: " + totalWinners);
                // Force to 2 matches for semi-finals
                expectedMatches = 2;
            }
        }
        
        // Validate: Final (last round) should have exactly 1 match
        if (expectedMatches == 1 && roundNumber > 4) {
            // This is likely the final
            if (expectedMatches != 1) {
                System.err.println("CupBracketGenerator: CRITICAL ERROR - Final should have exactly 1 match, but calculated " + expectedMatches);
                expectedMatches = 1;
            }
        }
        
        int matchNumber = 1;
        
        // Create matches pairing winners of previous round
        // CRITICAL FIX: Only create exactly expectedMatches matches
        for (int i = 0; i < previousRoundMatches.size() && roundMatches.size() < expectedMatches; i += 2) {
            Match match = new Match();
            
            // Set match ID
            match.setId(System.currentTimeMillis() + (roundNumber * 10000) + matchNumber + (int)(Math.random() * 1000));
            
            // Get parent matches
            Match parent1 = previousRoundMatches.get(i);
            Match parent2 = (i + 1 < previousRoundMatches.size()) ? previousRoundMatches.get(i + 1) : null;
            
            // Set parent matches
            match.setParentMatch1Id(parent1.getId());
            if (parent2 != null) {
                match.setParentMatch2Id(parent2.getId());
            } else {
                // Odd number - this match gets the bye team (only if we're adding bye to this round)
                match.setParentMatch2Id(null);
                if (addByeToThisRound && byeTeamId != null) {
                    match.setAwayClubId(byeTeamId); // Bye team goes to away position
                }
            }
            
            // Teams not yet determined (will be populated when parents complete)
            match.setHomeClubId(null);
            if (match.getAwayClubId() == null) {
                match.setAwayClubId(null);
            }
            
            // Set competition info
            match.setCompetitionId(competitionId);
            match.setCompetitionEditionId(editionId);
            match.setMatchType(Match.CUP_MATCH);
            match.setRound(roundNumber);
            match.setBracketPosition(matchNumber);
            match.setBracketPath("R" + roundNumber + "M" + matchNumber);
            
            // CRITICAL: Verify edition ID is set correctly
            if (match.getCompetitionEditionId() == null || !match.getCompetitionEditionId().equals(editionId)) {
                System.out.println("CupBracketGenerator: ERROR - Round " + roundNumber + " match " + 
                    match.getBracketPath() + " has wrong edition ID! Expected: " + editionId + 
                    ", Got: " + match.getCompetitionEditionId());
            }
            
            // Set match date
            match.setMatchDateTime(matchDate);
            match.setIsProposed(false);
            match.setIsAccepted(true);
            match.setIsPlayed(false);
            
            roundMatches.add(match);
            allMatches.add(match);
            
            matchNumber++;
        }
        
        // Handle bye team in Round 2 (only if we haven't already added it)
        if (addByeToThisRound && byeTeamId != null && roundMatches.size() < expectedMatches) {
            // Check if bye team is already in a match
            boolean byeAlreadyAdded = false;
            for (Match m : roundMatches) {
                if (m.getAwayClubId() != null && m.getAwayClubId().equals(byeTeamId)) {
                    byeAlreadyAdded = true;
                    break;
                }
            }
            
            if (!byeAlreadyAdded) {
                // Create an extra match for the bye team
                Match byeMatch = new Match();
                byeMatch.setId(System.currentTimeMillis() + (roundNumber * 10000) + matchNumber + (int)(Math.random() * 1000));
                
                // Last match from previous round
                Match lastParent = previousRoundMatches.get(previousRoundMatches.size() - 1);
                byeMatch.setParentMatch1Id(lastParent.getId());
                byeMatch.setParentMatch2Id(null);
                byeMatch.setAwayClubId(byeTeamId); // Bye team
                
                byeMatch.setHomeClubId(null); // Will be populated when parent completes
                
                byeMatch.setCompetitionId(competitionId);
                byeMatch.setCompetitionEditionId(editionId);
                byeMatch.setMatchType(Match.CUP_MATCH);
                byeMatch.setRound(roundNumber);
                byeMatch.setBracketPosition(matchNumber);
                byeMatch.setBracketPath("R" + roundNumber + "M" + matchNumber);
                
                byeMatch.setMatchDateTime(matchDate);
                byeMatch.setIsProposed(false);
                byeMatch.setIsAccepted(true);
                byeMatch.setIsPlayed(false);
                
                roundMatches.add(byeMatch);
                allMatches.add(byeMatch);
            }
        }
        
        // CRITICAL VALIDATION: Verify we have the correct number of matches
        if (roundMatches.size() != expectedMatches) {
            System.err.println("CupBracketGenerator: CRITICAL ERROR - Round " + roundNumber + " has " + 
                roundMatches.size() + " matches but expected " + expectedMatches);
            System.err.println("  Previous round matches: " + previousRoundMatches.size());
            System.err.println("  Bye count: " + byeCount);
            System.err.println("  Total winners: " + totalWinners);
            System.err.println("  This is a SEMI-FINAL bug if roundMatches.size() == 3!");
        }
        
        String roundName = getRoundName(roundNumber, roundMatches.size() * 2);
        Gdx.app.log("CupBracketGenerator", roundName + " (Round " + roundNumber + "): Generated " + roundMatches.size() + " matches");
        
        // CRITICAL VALIDATION: Log if semi-finals has wrong number of matches
        if (roundNumber == 4 || (roundMatches.size() == 2 && roundNumber >= 4)) {
            if (roundMatches.size() != 2) {
                System.err.println("CupBracketGenerator: *** SEMI-FINALS BUG DETECTED *** Round " + roundNumber + 
                    " has " + roundMatches.size() + " matches (should be 2)!");
            }
        }
        
        return roundMatches;
    }
    
    /**
     * Calculate number of rounds needed for tournament
     */
    private int calculateNumberOfRounds(int numTeams) {
        if (numTeams <= 1) return 1;
        return (int) Math.ceil(Math.log(numTeams) / Math.log(2));
    }
    
    /**
     * Get round name (First Round, Second Round, Semi-Final, Final, etc.)
     */
    private String getRoundName(int roundNumber, int participants) {
        if (participants == 2) return "Final";
        if (participants == 4) return "Semi-Final";
        if (participants == 8) return "Quarter-Final";
        
        String[] ordinals = {"", "First", "Second", "Third", "Fourth", "Fifth", "Sixth"};
        if (roundNumber > 0 && roundNumber < ordinals.length) {
            return ordinals[roundNumber] + " Round";
        }
        return "Round " + roundNumber;
    }
}
