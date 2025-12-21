package com.rndmodgames.futtoboru.engine.cup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Cup Bracket Manager v1.0
 * 
 * Manages bracket state and winner advancement.
 * When a match completes, automatically advances winner to next round.
 * 
 * @author Geomancer86
 */
public class CupBracketManager {
    
    private SaveGame currentGame;
    
    public CupBracketManager(SaveGame currentGame) {
        this.currentGame = currentGame;
    }
    
    /**
     * Advance winner from completed match to next round
     * 
     * @param completedMatch The match that was just completed
     * @return The next round match that was updated, or null if this was the final
     */
    public Match advanceWinner(Match completedMatch) {
        if (completedMatch == null || completedMatch.getIsPlayed() == null || !completedMatch.getIsPlayed()) {
            Gdx.app.error("CupBracketManager", "Cannot advance winner: match not completed");
            return null;
        }
        
        // Determine winner
        Club winner = determineWinner(completedMatch);
        if (winner == null) {
            Gdx.app.error("CupBracketManager", "Cannot determine winner for match: " + completedMatch.getId());
            return null;
        }
        
        System.out.println("CupBracketManager: Match " + completedMatch.getBracketPath() + 
            " (ID: " + completedMatch.getId() + ") completed. Winner: " + winner.getName() + " (ID: " + winner.getId() + ")");
        
        // Find next round match that depends on this match
        Match nextRoundMatch = findMatchWithParent(completedMatch.getId(), completedMatch.getCompetitionEditionId());
        
        if (nextRoundMatch == null) {
            // This is the final - cup is complete
            System.out.println("CupBracketManager: Final match completed! Cup champion: " + winner.getName());
            Gdx.app.log("CupBracketManager", "Final match completed! Cup champion: " + winner.getName());
            return null;
        }
        
        System.out.println("CupBracketManager: Found next round match: " + nextRoundMatch.getBracketPath() + 
            " (ID: " + nextRoundMatch.getId() + ", Round: " + nextRoundMatch.getRound() + 
            ", Date: " + nextRoundMatch.getMatchDateTime() + ")");
        
        // Determine which parent slot this match fills
        if (nextRoundMatch.getParentMatch1Id() != null && nextRoundMatch.getParentMatch1Id().equals(completedMatch.getId())) {
            // This match's winner goes to home position
            nextRoundMatch.setHomeClubId(winner.getId());
            Gdx.app.log("CupBracketManager", "Winner " + winner.getName() + " advanced to " + 
                nextRoundMatch.getBracketPath() + " (Home)");
        } else if (nextRoundMatch.getParentMatch2Id() != null && nextRoundMatch.getParentMatch2Id().equals(completedMatch.getId())) {
            // This match's winner goes to away position
            nextRoundMatch.setAwayClubId(winner.getId());
            Gdx.app.log("CupBracketManager", "Winner " + winner.getName() + " advanced to " + 
                nextRoundMatch.getBracketPath() + " (Away)");
        } else {
            Gdx.app.error("CupBracketManager", "Match " + completedMatch.getId() + 
                " is not a parent of match " + nextRoundMatch.getId());
            return null;
        }
        
        // Check if both teams are now determined
        if (nextRoundMatch.getHomeClubId() != null && nextRoundMatch.getAwayClubId() != null) {
            // Both teams determined - ensure match is in clubs' scheduled matches
            // Note: Match might already be in lists from initial generation, but we ensure it's there
            Club homeClub = currentGame.getClubById(nextRoundMatch.getHomeClubId());
            Club awayClub = currentGame.getClubById(nextRoundMatch.getAwayClubId());
            
            // Use match ID to check if already in list (more reliable than contains())
            boolean homeHasMatch = false;
            boolean awayHasMatch = false;
            
            if (homeClub != null) {
                if (homeClub.getScheduledMatches() == null) {
                    homeClub.setScheduledMatches(new java.util.ArrayList<>());
                }
                // Check by ID to avoid object equality issues
                for (Match m : homeClub.getScheduledMatches()) {
                    if (m != null && m.getId() != null && m.getId().equals(nextRoundMatch.getId())) {
                        homeHasMatch = true;
                        break;
                    }
                }
                if (!homeHasMatch) {
                    homeClub.getScheduledMatches().add(nextRoundMatch);
                    Gdx.app.log("CupBracketManager", "Added match " + nextRoundMatch.getBracketPath() + " to " + homeClub.getName());
                }
            }
            
            if (awayClub != null) {
                if (awayClub.getScheduledMatches() == null) {
                    awayClub.setScheduledMatches(new java.util.ArrayList<>());
                }
                // Check by ID to avoid object equality issues
                for (Match m : awayClub.getScheduledMatches()) {
                    if (m != null && m.getId() != null && m.getId().equals(nextRoundMatch.getId())) {
                        awayHasMatch = true;
                        break;
                    }
                }
                if (!awayHasMatch) {
                    awayClub.getScheduledMatches().add(nextRoundMatch);
                    Gdx.app.log("CupBracketManager", "Added match " + nextRoundMatch.getBracketPath() + " to " + awayClub.getName());
                }
            }
            
            System.out.println("CupBracketManager: *** MATCH READY TO PLAY *** " + nextRoundMatch.getBracketPath() + 
                ": " + (homeClub != null ? homeClub.getName() : "TBD") + 
                " vs " + (awayClub != null ? awayClub.getName() : "TBD") + 
                " on " + nextRoundMatch.getMatchDateTime() + 
                " (ID: " + nextRoundMatch.getId() + ", Round: " + nextRoundMatch.getRound() + ")");
            
            // CRITICAL: Verify match is accessible in both clubs
            boolean homeHasIt = false;
            boolean awayHasIt = false;
            if (homeClub != null && homeClub.getScheduledMatches() != null) {
                for (Match m : homeClub.getScheduledMatches()) {
                    if (m != null && m.getId() != null && m.getId().equals(nextRoundMatch.getId())) {
                        homeHasIt = true;
                        break;
                    }
                }
            }
            if (awayClub != null && awayClub.getScheduledMatches() != null) {
                for (Match m : awayClub.getScheduledMatches()) {
                    if (m != null && m.getId() != null && m.getId().equals(nextRoundMatch.getId())) {
                        awayHasIt = true;
                        break;
                    }
                }
            }
            System.out.println("CupBracketManager: Match accessibility - Home club has it: " + homeHasIt + 
                ", Away club has it: " + awayHasIt);
            
            Gdx.app.log("CupBracketManager", "Match " + nextRoundMatch.getBracketPath() + 
                " is now READY TO PLAY: " + (homeClub != null ? homeClub.getName() : "TBD") + 
                " vs " + (awayClub != null ? awayClub.getName() : "TBD") + 
                " on " + nextRoundMatch.getMatchDateTime());
        } else {
            // Only one team determined so far
            System.out.println("CupBracketManager: Match " + nextRoundMatch.getBracketPath() + 
                " waiting for other team: Home=" + (nextRoundMatch.getHomeClubId() != null ? "Set (ID: " + nextRoundMatch.getHomeClubId() + ")" : "TBD") + 
                ", Away=" + (nextRoundMatch.getAwayClubId() != null ? "Set (ID: " + nextRoundMatch.getAwayClubId() + ")" : "TBD"));
            Gdx.app.log("CupBracketManager", "Match " + nextRoundMatch.getBracketPath() + 
                " waiting for other team: Home=" + (nextRoundMatch.getHomeClubId() != null ? "Set" : "TBD") + 
                ", Away=" + (nextRoundMatch.getAwayClubId() != null ? "Set" : "TBD"));
        }
        
        return nextRoundMatch;
    }
    
    /**
     * Determine winner of a completed match
     */
    private Club determineWinner(Match match) {
        if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
            return null;
        }
        
        if (match.getHomeGoals() > match.getAwayGoals()) {
            return currentGame.getClubById(match.getHomeClubId());
        } else if (match.getAwayGoals() > match.getHomeGoals()) {
            return currentGame.getClubById(match.getAwayClubId());
        } else {
            // Draw - for now, we'll need to handle replays later
            // For now, return null (draws need special handling)
            Gdx.app.log("CupBracketManager", "WARNING: Match ended in draw - replay needed (not yet implemented)");
            return null;
        }
    }
    
    /**
     * Find the match in the next round that depends on the given parent match
     */
    private Match findMatchWithParent(Long parentMatchId, Long editionId) {
        if (parentMatchId == null || editionId == null) {
            System.out.println("CupBracketManager: findMatchWithParent - null parameters");
            return null;
        }
        
        System.out.println("CupBracketManager: Searching for next round match with parent ID: " + parentMatchId + ", edition ID: " + editionId);
        
        int clubsChecked = 0;
        int matchesChecked = 0;
        
        // CRITICAL: Search BOTH scheduled AND played matches
        // Matches are moved from scheduled to played after simulation
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) continue;
            
            clubsChecked++;
            
            // Search scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match == null) continue;
                    
                    matchesChecked++;
                    
                    // Check if this match belongs to the same edition
                    if (match.getCompetitionEditionId() == null || !match.getCompetitionEditionId().equals(editionId)) {
                        continue;
                    }
                    
                    // Check if this match has the parent match
                    if ((match.getParentMatch1Id() != null && match.getParentMatch1Id().equals(parentMatchId)) ||
                        (match.getParentMatch2Id() != null && match.getParentMatch2Id().equals(parentMatchId))) {
                        System.out.println("CupBracketManager: Found next round match in scheduled! " + 
                            (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                            " (ID: " + match.getId() + ", Round: " + match.getRound() + 
                            ", Parent1: " + match.getParentMatch1Id() + ", Parent2: " + match.getParentMatch2Id() + ")");
                        return match;
                    }
                }
            }
            
            // Search played matches (matches are moved here after simulation)
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match == null) continue;
                    
                    matchesChecked++;
                    
                    // Check if this match belongs to the same edition
                    if (match.getCompetitionEditionId() == null || !match.getCompetitionEditionId().equals(editionId)) {
                        continue;
                    }
                    
                    // Check if this match has the parent match
                    if ((match.getParentMatch1Id() != null && match.getParentMatch1Id().equals(parentMatchId)) ||
                        (match.getParentMatch2Id() != null && match.getParentMatch2Id().equals(parentMatchId))) {
                        System.out.println("CupBracketManager: Found next round match in played! " + 
                            (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                            " (ID: " + match.getId() + ", Round: " + match.getRound() + 
                            ", Parent1: " + match.getParentMatch1Id() + ", Parent2: " + match.getParentMatch2Id() + ")");
                        return match;
                    }
                }
            }
        }
        
        System.out.println("CupBracketManager: Searched " + clubsChecked + " clubs, " + matchesChecked + " matches in club lists, not found. Trying getAllMatchesForEdition...");
        
        // Also search matches that might not be in clubs yet (future rounds)
        // We need to search all matches for this edition
        List<Match> allEditionMatches = getAllMatchesForEdition(editionId);
        System.out.println("CupBracketManager: getAllMatchesForEdition returned " + allEditionMatches.size() + " matches");
        
        System.out.println("CupBracketManager: Searching through " + allEditionMatches.size() + " matches for parent ID: " + parentMatchId);
        for (Match match : allEditionMatches) {
            if (match == null) continue;
            
            // Debug: Log all matches with their parent IDs
            if (match.getRound() != null && match.getRound() > 1) {
                System.out.println("CupBracketManager: Checking match " + match.getBracketPath() + 
                    " (Round " + match.getRound() + ", ID: " + match.getId() + 
                    ", Parent1: " + match.getParentMatch1Id() + ", Parent2: " + match.getParentMatch2Id() + 
                    ") against parent ID: " + parentMatchId);
            }
            
            if ((match.getParentMatch1Id() != null && match.getParentMatch1Id().equals(parentMatchId)) ||
                (match.getParentMatch2Id() != null && match.getParentMatch2Id().equals(parentMatchId))) {
                System.out.println("CupBracketManager: *** FOUND NEXT ROUND MATCH *** " + 
                    (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                    " (Round " + match.getRound() + ", ID: " + match.getId() + ")");
                return match;
            }
        }
        
        System.out.println("CupBracketManager: WARNING - Could not find next round match for parent ID: " + parentMatchId);
        return null;
    }
    
    /**
     * Get all matches for a competition edition
     * CRITICAL: Must search BOTH scheduledMatches AND playedMatches because
     * matches are moved from scheduled to played after simulation
     */
    private List<Match> getAllMatchesForEdition(Long editionId) {
        List<Match> matches = new java.util.ArrayList<>();
        Set<Long> matchIdsAdded = new HashSet<>(); // Use ID-based deduplication
        
        if (editionId == null) return matches;
        
        int clubsSearched = 0;
        int matchesChecked = 0;
        int matchesWithWrongEdition = 0;
        
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) continue;
            
            clubsSearched++;
            
            // Search scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match == null) continue;
                    
                    matchesChecked++;
                    
                    // Debug: Log matches with wrong edition ID
                    if (match.getCompetitionEditionId() != null && 
                        !match.getCompetitionEditionId().equals(editionId) &&
                        match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH) {
                        matchesWithWrongEdition++;
                        if (matchesWithWrongEdition <= 3) {
                            System.out.println("CupBracketManager: Found cup match with WRONG edition ID: " + 
                                match.getBracketPath() + " (Round " + match.getRound() + 
                                ", Expected: " + editionId + ", Found: " + match.getCompetitionEditionId() + ")");
                        }
                    }
                    
                    if (match.getId() != null &&
                        match.getCompetitionEditionId() != null && 
                        match.getCompetitionEditionId().equals(editionId)) {
                        
                        if (!matchIdsAdded.contains(match.getId())) {
                            matches.add(match);
                            matchIdsAdded.add(match.getId());
                        }
                    }
                }
            }
            
            // CRITICAL: Also search played matches (matches are moved here after simulation)
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match == null) continue;
                    
                    matchesChecked++;
                    
                    if (match.getId() != null &&
                        match.getCompetitionEditionId() != null && 
                        match.getCompetitionEditionId().equals(editionId)) {
                        
                        if (!matchIdsAdded.contains(match.getId())) {
                            matches.add(match);
                            matchIdsAdded.add(match.getId());
                        }
                    }
                }
            }
        }
        
        System.out.println("CupBracketManager: getAllMatchesForEdition - Searched " + clubsSearched + 
            " clubs, checked " + matchesChecked + " matches, found " + matchesWithWrongEdition + 
            " cup matches with wrong edition ID");
        
        System.out.println("CupBracketManager: getAllMatchesForEdition found " + matches.size() + 
            " matches for edition " + editionId + " (searched both scheduled and played)");
        
        // Debug: Log breakdown by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match m : matches) {
            if (m.getRound() != null) {
                matchesByRound.put(m.getRound(), matchesByRound.getOrDefault(m.getRound(), 0) + 1);
            }
        }
        System.out.println("CupBracketManager: Matches by round: " + matchesByRound);
        
        // Debug: Log a few Round 2+ matches if they exist
        int round2PlusCount = 0;
        for (Match m : matches) {
            if (m.getRound() != null && m.getRound() > 1) {
                round2PlusCount++;
                if (round2PlusCount <= 3) {
                    System.out.println("CupBracketManager: Round " + m.getRound() + " match found: " + 
                        m.getBracketPath() + " (ID: " + m.getId() + 
                        ", Edition: " + m.getCompetitionEditionId() + 
                        ", Parent1: " + m.getParentMatch1Id() + ", Parent2: " + m.getParentMatch2Id() + ")");
                }
            }
        }
        System.out.println("CupBracketManager: Total Round 2+ matches found: " + round2PlusCount);
        
        return matches;
    }
    
    /**
     * Check if a match is ready to be played (both teams determined)
     */
    public boolean isMatchReady(Match match) {
        if (match == null) return false;
        
        // First round matches are always ready (teams set at creation)
        if (match.getRound() != null && match.getRound() == 1) {
            return match.getHomeClubId() != null && match.getAwayClubId() != null;
        }
        
        // Future rounds: both parent matches must be completed
        return match.getHomeClubId() != null && match.getAwayClubId() != null;
    }
}
