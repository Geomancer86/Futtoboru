package com.rndmodgames.futtoboru.engine.cup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

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
    private com.rndmodgames.futtoboru.game.Futtoboru gameInstance; // Added to access AuthorityManager
    
    public CupBracketManager(SaveGame currentGame) {
        this.currentGame = currentGame;
        this.gameInstance = null; // Will be set via setter if needed
    }
    
    /**
     * Constructor with game instance for cup completion integration
     */
    public CupBracketManager(SaveGame currentGame, com.rndmodgames.futtoboru.game.Futtoboru gameInstance) {
        this.currentGame = currentGame;
        this.gameInstance = gameInstance;
    }
    
    /**
     * Advance winner from completed match to next round
     * 
     * @param completedMatch The match that was just completed
     * @return The next round match that was updated, or null if this was the final
     */
    public Match advanceWinner(Match completedMatch) {
        if (completedMatch == null || completedMatch.getIsPlayed() == null || !completedMatch.getIsPlayed()) {
            Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot advance winner: match not completed");
            return null;
        }
        
        // Check if this is a replay (has parent match)
        boolean isReplay = completedMatch.getParentMatch1Id() != null && 
                          completedMatch.getBracketPath() != null && 
                          completedMatch.getBracketPath().endsWith("R");
        
        // CRITICAL: Validate teams exist before determining winner
        if (completedMatch.getHomeClubId() == null || completedMatch.getAwayClubId() == null) {
            Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot advance winner: match has missing teams (Home: " + 
                completedMatch.getHomeClubId() + ", Away: " + completedMatch.getAwayClubId() + ")");
            Log.info(Log.ENGINE_CUP, "CupBracketManager: *** ERROR *** Match " + completedMatch.getBracketPath() + 
                " has missing teams - cannot determine winner");
            return null;
        }
        
        // Determine winner
        Club winner = determineWinner(completedMatch);
        
        // Get the original match's round (for final detection) - if this is a replay, we need the original match's round
        Integer originalRound = completedMatch.getRound();
        Match bracketMatch = completedMatch;
        if (isReplay && completedMatch.getParentMatch1Id() != null) {
            // Try to find the original match to get its round
            Match originalMatch = findMatchByIdInAllMatches(completedMatch.getParentMatch1Id());
            if (originalMatch != null && originalMatch.getRound() != null) {
                originalRound = originalMatch.getRound();
                bracketMatch = originalMatch;
            }
        }
        // P2-7: the final is the bracket's last tie, not "round 5" (a 4-club bracket has its final in round 2)
        boolean isFinal = isBracketFinal(bracketMatch);
        
        if (winner == null) {
            // Draw - schedule replay (unless this is already a replay, then we need extra time/penalties - for now, pick random winner)
            if (isReplay) {
                // Replay also ended in draw - for now, pick random winner (future: implement extra time/penalties)
                Log.info(Log.ENGINE_CUP, "CupBracketManager", "Replay also ended in draw - picking random winner (extra time/penalties not yet implemented)");
                boolean homeWins = com.rndmodgames.futtoboru.game.Futtoboru.randomOf(gameInstance)
                        .fork("replay-decider:" + completedMatch.getId()).nextBoolean();
                Club homeClub = currentGame.getClubById(completedMatch.getHomeClubId());
                Club awayClub = currentGame.getClubById(completedMatch.getAwayClubId());
                if (homeClub == null || awayClub == null) {
                    Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot resolve replay draw - clubs not found");
                    return null;
                }
                winner = homeWins ? homeClub : awayClub;
                Log.info(Log.ENGINE_CUP, "CupBracketManager: Replay draw resolved - winner: " + winner.getName() + 
                    " (Original match round: " + originalRound + ")");
                
                // CRITICAL FIX: If this is a final replay that ended in draw, we need to complete the cup
                // Check if the original match was the final (Round 5+)
                if (isFinal) {
                    Log.info(Log.ENGINE_CUP, "CupBracketManager: *** FINAL REPLAY DRAW RESOLVED *** Champion: " + winner.getName());
                    Log.info(Log.ENGINE_CUP, "CupBracketManager", "Final replay draw resolved! Cup champion: " + winner.getName());
                    
                    // Complete the cup immediately
                    if (gameInstance != null && gameInstance.getGameEngine() != null) {
                        com.rndmodgames.futtoboru.engine.AuthorityManager authorityManager = 
                            gameInstance.getGameEngine().getAuthorityManager();
                        if (authorityManager != null) {
                            com.rndmodgames.futtoboru.data.Competition cup = getCupForEdition(completedMatch.getCompetitionEditionId());
                            com.rndmodgames.futtoboru.data.CompetitionEdition edition = getEdition(completedMatch.getCompetitionEditionId());
                            if (cup != null && edition != null) {
                                // CRITICAL FIX: Pass currentGame to ensure it's not null
                                authorityManager.completeCupEdition(cup, edition, winner, currentGame);
                                Log.info(Log.ENGINE_CUP, "CupBracketManager: Cup completion triggered for final replay draw");
                                Log.info(Log.ENGINE_CUP, "CupBracketManager", "Cup completion triggered for: " + cup.getName());
                            }
                        }
                    }
                    return null; // Cup is complete, no next round match
                }
            } else {
                // Original match ended in draw - schedule replay
                Match replayMatch = scheduleReplay(completedMatch);
                if (replayMatch == null) {
                    Log.error(Log.ENGINE_CUP, "CupBracketManager", "Failed to schedule replay for match: " + completedMatch.getId());
                }
                return null; // No winner yet, replay will determine it
            }
        }
        
        // Additional validation: ensure winner club exists
        if (winner == null) {
            Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot advance winner: winner is null after determination");
            return null;
        }
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Match " + completedMatch.getBracketPath() + 
            " (ID: " + completedMatch.getId() + ") completed. Winner: " + winner.getName() + " (ID: " + winner.getId() + ")");
        
        // If this is a replay, find the original match to determine which next round match to advance to
        Long parentMatchId = completedMatch.getId();
        if (isReplay && completedMatch.getParentMatch1Id() != null) {
            // This is a replay - use the original match's ID to find next round match
            parentMatchId = completedMatch.getParentMatch1Id();
            Log.info(Log.ENGINE_CUP, "CupBracketManager: Replay completed, using original match ID: " + parentMatchId);
        }
        
        // Find next round match that depends on this match (or original match if replay)
        Match nextRoundMatch = findMatchWithParent(parentMatchId, completedMatch.getCompetitionEditionId());
        
        if (nextRoundMatch == null) {
            // Check if this is actually the final (the bracket's last tie) or if there's a bracket error
            if (isFinal) {
                // This is the final - cup is complete
                Log.info(Log.ENGINE_CUP, "CupBracketManager: *** CUP FINAL COMPLETED *** Champion: " + winner.getName());
                Log.info(Log.ENGINE_CUP, "CupBracketManager", "Final match completed! Cup champion: " + winner.getName());
                
                // CRITICAL: Trigger cup completion via AuthorityManager
                // This ensures champions/runners-up are saved to CompetitionEdition
                if (gameInstance != null && gameInstance.getGameEngine() != null) {
                    com.rndmodgames.futtoboru.engine.AuthorityManager authorityManager = 
                        gameInstance.getGameEngine().getAuthorityManager();
                    if (authorityManager != null) {
                        // Get cup and edition for completion
                        com.rndmodgames.futtoboru.data.Competition cup = getCupForEdition(completedMatch.getCompetitionEditionId());
                        com.rndmodgames.futtoboru.data.CompetitionEdition edition = getEdition(completedMatch.getCompetitionEditionId());
                        if (cup != null && edition != null) {
                            // CRITICAL FIX: Pass currentGame to ensure it's not null
                            authorityManager.completeCupEdition(cup, edition, winner, currentGame);
                            Log.info(Log.ENGINE_CUP, "CupBracketManager: Cup completion triggered via AuthorityManager");
                            Log.info(Log.ENGINE_CUP, "CupBracketManager", "Cup completion triggered for: " + cup.getName());
                        } else {
                            Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot complete cup: cup or edition not found for edition ID: " + completedMatch.getCompetitionEditionId());
                        }
                    } else {
                        Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot complete cup: AuthorityManager not available");
                    }
                } else {
                    Log.info(Log.ENGINE_CUP, "CupBracketManager", "Game instance not available - cup completion will be handled by AuthorityManager.checkCupRoundProgression()");
                }
                
                return null;
            } else {
                // Bracket error - match should have a next round match
                Log.info(Log.ENGINE_CUP, "CupBracketManager: *** CRITICAL ERROR *** Match " + completedMatch.getBracketPath() + 
                    " (Round " + completedMatch.getRound() + ", ID: " + completedMatch.getId() + 
                    ") completed but NO NEXT ROUND MATCH FOUND!");
                Log.info(Log.ENGINE_CUP, "CupBracketManager: Edition ID: " + completedMatch.getCompetitionEditionId());
                Log.info(Log.ENGINE_CUP, "CupBracketManager: Searching for parent match ID: " + parentMatchId);
                
                // Debug: List all matches for this edition to see what's available
                List<Match> allEditionMatches = getAllMatchesForEdition(completedMatch.getCompetitionEditionId());
                Log.info(Log.ENGINE_CUP, "CupBracketManager: Total matches found for edition: " + allEditionMatches.size());
                for (Match m : allEditionMatches) {
                    if (m.getRound() != null && m.getRound() == completedMatch.getRound() + 1) {
                        Log.info(Log.ENGINE_CUP, "  - Next round match: " + m.getBracketPath() + 
                            " (ID: " + m.getId() + ", Parent1: " + m.getParentMatch1Id() + 
                            ", Parent2: " + m.getParentMatch2Id() + ")");
                    }
                }
                
                Log.error(Log.ENGINE_CUP, "CupBracketManager", "CRITICAL: Match " + completedMatch.getBracketPath() + 
                    " has no next round match - bracket may be incomplete!");
                return null;
            }
        }
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Found next round match: " + nextRoundMatch.getBracketPath() +
            " (ID: " + nextRoundMatch.getId() + ", Round: " + nextRoundMatch.getRound() +
            ", Date: " + nextRoundMatch.getMatchDateTime() + ")");

        // P2-14: a cup drawn round by round: the winner waits for the next round's draw (AuthorityManager)
        // instead of taking the bracket slot of this tie
        com.rndmodgames.futtoboru.data.CompetitionEdition openDraw = getEdition(completedMatch.getCompetitionEditionId());
        if (openDraw != null && openDraw.isOpenDraw()) {
            if (!openDraw.getDrawPool().contains(winner.getId())) {
                openDraw.getDrawPool().add(winner.getId());
            }
            Log.info(Log.ENGINE_CUP, "CupBracketManager", winner.getName() + " through to the draw of round "
                    + nextRoundMatch.getRound() + " of " + openDraw.getName());
            return nextRoundMatch;
        }

        // Determine which parent slot this match fills (use parentMatchId which is original match ID for replays)
        if (nextRoundMatch.getParentMatch1Id() != null && nextRoundMatch.getParentMatch1Id().equals(parentMatchId)) {
            // This match's winner goes to home position
            nextRoundMatch.setHomeClubId(winner.getId());
            Log.info(Log.ENGINE_CUP, "CupBracketManager", "Winner " + winner.getName() + " advanced to " + 
                nextRoundMatch.getBracketPath() + " (Home)");
        } else if (nextRoundMatch.getParentMatch2Id() != null && nextRoundMatch.getParentMatch2Id().equals(parentMatchId)) {
            // This match's winner goes to away position
            nextRoundMatch.setAwayClubId(winner.getId());
            Log.info(Log.ENGINE_CUP, "CupBracketManager", "Winner " + winner.getName() + " advanced to " + 
                nextRoundMatch.getBracketPath() + " (Away)");
        } else {
            Log.error(Log.ENGINE_CUP, "CupBracketManager", "Match " + parentMatchId + 
                " (original: " + completedMatch.getId() + ") is not a parent of match " + nextRoundMatch.getId());
            return null;
        }
        
        // Check if both teams are now determined
        if (nextRoundMatch.getHomeClubId() != null && nextRoundMatch.getAwayClubId() != null) {
            // Both teams determined - ensure match is in clubs' scheduled matches
            // Note: Match might already be in lists from initial generation, but we ensure it's there
            Club homeClub = currentGame.getClubById(nextRoundMatch.getHomeClubId());
            Club awayClub = currentGame.getClubById(nextRoundMatch.getAwayClubId());

            // P2-14: a tie of a neutral round (semi-final, final) gets its ground now its clubs are known
            List<Match> editionMatches = getAllMatchesForEdition(nextRoundMatch.getCompetitionEditionId());
            int lastRound = 0;
            for (Match m : editionMatches) {
                lastRound = Math.max(lastRound, m.getRound() != null ? m.getRound() : 0);
            }
            NeutralVenues.assign(currentGame, getCupForEdition(nextRoundMatch.getCompetitionEditionId()),
                    nextRoundMatch, editionMatches, lastRound,
                    com.rndmodgames.futtoboru.game.Futtoboru.randomOf(gameInstance));
            
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
                    Log.info(Log.ENGINE_CUP, "CupBracketManager", "Added match " + nextRoundMatch.getBracketPath() + " to " + homeClub.getName());
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
                    Log.info(Log.ENGINE_CUP, "CupBracketManager", "Added match " + nextRoundMatch.getBracketPath() + " to " + awayClub.getName());
                }
            }
            
            Log.info(Log.ENGINE_CUP, "CupBracketManager: *** MATCH READY TO PLAY *** " + nextRoundMatch.getBracketPath() + 
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
            Log.info(Log.ENGINE_CUP, "CupBracketManager: Match accessibility - Home club has it: " + homeHasIt + 
                ", Away club has it: " + awayHasIt);
            
            Log.info(Log.ENGINE_CUP, "CupBracketManager", "Match " + nextRoundMatch.getBracketPath() + 
                " is now READY TO PLAY: " + (homeClub != null ? homeClub.getName() : "TBD") + 
                " vs " + (awayClub != null ? awayClub.getName() : "TBD") + 
                " on " + nextRoundMatch.getMatchDateTime());
        } else {
            // Only one team determined so far
            Log.info(Log.ENGINE_CUP, "CupBracketManager: Match " + nextRoundMatch.getBracketPath() + 
                " waiting for other team: Home=" + (nextRoundMatch.getHomeClubId() != null ? "Set (ID: " + nextRoundMatch.getHomeClubId() + ")" : "TBD") + 
                ", Away=" + (nextRoundMatch.getAwayClubId() != null ? "Set (ID: " + nextRoundMatch.getAwayClubId() + ")" : "TBD"));
            Log.info(Log.ENGINE_CUP, "CupBracketManager", "Match " + nextRoundMatch.getBracketPath() + 
                " waiting for other team: Home=" + (nextRoundMatch.getHomeClubId() != null ? "Set" : "TBD") + 
                ", Away=" + (nextRoundMatch.getAwayClubId() != null ? "Set" : "TBD"));
        }
        
        return nextRoundMatch;
    }
    
    /**
     * True if the match is the final of its bracket: a tie of the edition's last round that no later tie
     * takes its winner from. Replays are not bracket ties (pass the original tie).
     */
    public boolean isBracketFinal(Match bracketMatch) {
        if (bracketMatch == null || bracketMatch.getRound() == null || bracketMatch.getCompetitionEditionId() == null) {
            return false;
        }
        int lastRound = 0;
        for (Match m : getAllMatchesForEdition(bracketMatch.getCompetitionEditionId())) {
            if (m.getRound() != null) {
                lastRound = Math.max(lastRound, m.getRound());
            }
            if (bracketMatch.getId() != null
                    && (bracketMatch.getId().equals(m.getParentMatch2Id())
                        || (bracketMatch.getId().equals(m.getParentMatch1Id()) && !isReplayMatch(m)))) {
                return false; // a later tie takes this tie's winner
            }
        }
        return bracketMatch.getRound() == lastRound;
    }

    private static boolean isReplayMatch(Match m) {
        return m.getParentMatch1Id() != null && m.getBracketPath() != null && m.getBracketPath().endsWith("R");
    }

    /**
     * Determine winner of a completed match
     * Returns null if match is a draw (replay needed)
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
            // Draw - schedule replay
            return null; // Will be handled by scheduleReplay()
        }
    }
    
    /**
     * Schedule a replay for a drawn cup match
     * Replay is scheduled 7 days later at the away team's venue (venue swap)
     * CRITICAL: Always schedules replay in the future (current date + 7 days minimum)
     */
    private Match scheduleReplay(Match originalMatch) {
        if (originalMatch == null || originalMatch.getHomeClubId() == null || originalMatch.getAwayClubId() == null) {
            Log.error(Log.ENGINE_CUP, "CupBracketManager", "Cannot schedule replay: invalid match");
            return null;
        }
        
        // Check if replay already exists (to prevent duplicate replays)
        Match existingReplay = findExistingReplay(originalMatch);
        if (existingReplay != null) {
            Log.info(Log.ENGINE_CUP, "CupBracketManager: Replay already exists for " + originalMatch.getBracketPath() + 
                " - ID: " + existingReplay.getId() + ", Date: " + existingReplay.getMatchDateTime());
            return existingReplay;
        }
        
        // Create replay match - swap home/away (replay at away team's venue)
        Match replayMatch = new Match();
        replayMatch.setId(currentGame.getIdAllocator().nextId()); // runtime id from the game (P2-4)
        
        // Swap home/away for replay
        replayMatch.setHomeClubId(originalMatch.getAwayClubId());
        replayMatch.setAwayClubId(originalMatch.getHomeClubId());
        // P2-14: a tie at a neutral ground is replayed there (the 1889 semi-final replay was at Crewe again)
        replayMatch.setVenue(originalMatch.getVenue());
        
        // Set competition info
        replayMatch.setCompetitionId(originalMatch.getCompetitionId());
        replayMatch.setCompetitionEditionId(originalMatch.getCompetitionEditionId());
        replayMatch.setMatchType(Match.CUP_MATCH);
        replayMatch.setRound(originalMatch.getRound());
        replayMatch.setBracketPosition(originalMatch.getBracketPosition());
        
        // Handle multiple replays (replay of replay) - append "R" for each replay
        String originalPath = originalMatch.getBracketPath();
        if (originalPath == null) {
            originalPath = "R" + originalMatch.getRound() + "M" + originalMatch.getBracketPosition();
        }
        replayMatch.setBracketPath(originalPath + "R"); // Add "R" for Replay
        
        // Link to original match (for tracking)
        replayMatch.setParentMatch1Id(originalMatch.getId());
        replayMatch.setParentMatch2Id(null);
        
        // P2-11: the following Saturday when both clubs are free, else midweek of the week after the tie, before
        // the next round (the next cup date of the era calendar); never before today
        java.time.LocalDateTime currentDate = currentGame.getGameDate();
        java.time.LocalDateTime tieDate = originalMatch.getMatchDateTime() != null
                ? originalMatch.getMatchDateTime() : currentDate;
        java.time.LocalDateTime nextRound = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(tieDate)
                .nextCupDateAfter(tieDate.toLocalDate());
        java.time.LocalDateTime replayDate = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.replayDate(
                currentGame, originalMatch, currentDate.toLocalDate(), nextRound != null ? nextRound.toLocalDate() : null);
        
        replayMatch.setMatchDateTime(replayDate);
        replayMatch.setIsProposed(false);
        replayMatch.setIsAccepted(true);
        replayMatch.setIsPlayed(false);
        
        // Add replay to both clubs' scheduled matches
        Club homeClub = currentGame.getClubById(replayMatch.getHomeClubId());
        Club awayClub = currentGame.getClubById(replayMatch.getAwayClubId());
        
        if (homeClub != null) {
            if (homeClub.getScheduledMatches() == null) {
                homeClub.setScheduledMatches(new java.util.ArrayList<>());
            }
            // Check if already added (by ID)
            boolean alreadyAdded = false;
            for (Match m : homeClub.getScheduledMatches()) {
                if (m != null && m.getId() != null && m.getId().equals(replayMatch.getId())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                homeClub.getScheduledMatches().add(replayMatch);
            }
        }
        
        if (awayClub != null) {
            if (awayClub.getScheduledMatches() == null) {
                awayClub.setScheduledMatches(new java.util.ArrayList<>());
            }
            // Check if already added (by ID)
            boolean alreadyAdded = false;
            for (Match m : awayClub.getScheduledMatches()) {
                if (m != null && m.getId() != null && m.getId().equals(replayMatch.getId())) {
                    alreadyAdded = true;
                    break;
                }
            }
            if (!alreadyAdded) {
                awayClub.getScheduledMatches().add(replayMatch);
            }
        }
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: *** REPLAY SCHEDULED *** " + replayMatch.getBracketPath() + 
            " on " + replayMatch.getMatchDateTime() + " (Original: " + originalMatch.getBracketPath() + 
            ", Current Date: " + currentDate + ")");
        Log.info(Log.ENGINE_CUP, "CupBracketManager", "Replay scheduled: " + replayMatch.getBracketPath() + 
            " on " + replayMatch.getMatchDateTime());
        
        return replayMatch;
    }
    
    /**
     * Find a match by ID in all clubs' scheduled and played matches
     */
    private Match findMatchByIdInAllMatches(Long matchId) {
        if (matchId == null || currentGame == null) {
            return null;
        }
        
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) continue;
            
            // Check scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && match.getId() != null && match.getId().equals(matchId)) {
                        return match;
                    }
                }
            }
            
            // Check played matches
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match != null && match.getId() != null && match.getId().equals(matchId)) {
                        return match;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find existing replay for a match (to prevent duplicate replays)
     */
    private Match findExistingReplay(Match originalMatch) {
        if (originalMatch == null || originalMatch.getId() == null) {
            return null;
        }
        
        // Search all clubs for a replay match linked to this original match
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) continue;
            
            // Check scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && match.getParentMatch1Id() != null && 
                        match.getParentMatch1Id().equals(originalMatch.getId()) &&
                        match.getBracketPath() != null && match.getBracketPath().endsWith("R")) {
                        return match;
                    }
                }
            }
            
            // Check played matches (replay might have been played but not advanced)
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match != null && match.getParentMatch1Id() != null && 
                        match.getParentMatch1Id().equals(originalMatch.getId()) &&
                        match.getBracketPath() != null && match.getBracketPath().endsWith("R")) {
                        return match;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Find the match in the next round that depends on the given parent match
     */
    private Match findMatchWithParent(Long parentMatchId, Long editionId) {
        if (parentMatchId == null || editionId == null) {
            Log.info(Log.ENGINE_CUP, "CupBracketManager: findMatchWithParent - null parameters");
            return null;
        }
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Searching for next round match with parent ID: " + parentMatchId + ", edition ID: " + editionId);
        
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
                    if (!isReplayMatch(match) && ((match.getParentMatch1Id() != null && match.getParentMatch1Id().equals(parentMatchId)) ||
                        (match.getParentMatch2Id() != null && match.getParentMatch2Id().equals(parentMatchId)))) {
                        Log.info(Log.ENGINE_CUP, "CupBracketManager: Found next round match in scheduled! " + 
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
                    if (!isReplayMatch(match) && ((match.getParentMatch1Id() != null && match.getParentMatch1Id().equals(parentMatchId)) ||
                        (match.getParentMatch2Id() != null && match.getParentMatch2Id().equals(parentMatchId)))) {
                        Log.info(Log.ENGINE_CUP, "CupBracketManager: Found next round match in played! " + 
                            (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                            " (ID: " + match.getId() + ", Round: " + match.getRound() + 
                            ", Parent1: " + match.getParentMatch1Id() + ", Parent2: " + match.getParentMatch2Id() + ")");
                        return match;
                    }
                }
            }
        }
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Searched " + clubsChecked + " clubs, " + matchesChecked + " matches in club lists, not found. Trying getAllMatchesForEdition...");
        
        // Also search matches that might not be in clubs yet (future rounds)
        // We need to search all matches for this edition
        List<Match> allEditionMatches = getAllMatchesForEdition(editionId);
        Log.info(Log.ENGINE_CUP, "CupBracketManager: getAllMatchesForEdition returned " + allEditionMatches.size() + " matches");
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Searching through " + allEditionMatches.size() + " matches for parent ID: " + parentMatchId);
        for (Match match : allEditionMatches) {
            if (match == null) continue;
            
            // Debug: Log all matches with their parent IDs
            if (match.getRound() != null && match.getRound() > 1) {
                Log.info(Log.ENGINE_CUP, "CupBracketManager: Checking match " + match.getBracketPath() + 
                    " (Round " + match.getRound() + ", ID: " + match.getId() + 
                    ", Parent1: " + match.getParentMatch1Id() + ", Parent2: " + match.getParentMatch2Id() + 
                    ") against parent ID: " + parentMatchId);
            }
            
            if (!isReplayMatch(match) && ((match.getParentMatch1Id() != null && match.getParentMatch1Id().equals(parentMatchId)) ||
                (match.getParentMatch2Id() != null && match.getParentMatch2Id().equals(parentMatchId)))) {
                Log.info(Log.ENGINE_CUP, "CupBracketManager: *** FOUND NEXT ROUND MATCH *** " + 
                    (match.getBracketPath() != null ? match.getBracketPath() : "null") + 
                    " (Round " + match.getRound() + ", ID: " + match.getId() + ")");
                return match;
            }
        }
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: WARNING - Could not find next round match for parent ID: " + parentMatchId);
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
                            Log.info(Log.ENGINE_CUP, "CupBracketManager: Found cup match with WRONG edition ID: " + 
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
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: getAllMatchesForEdition - Searched " + clubsSearched + 
            " clubs, checked " + matchesChecked + " matches, found " + matchesWithWrongEdition + 
            " cup matches with wrong edition ID");
        
        Log.info(Log.ENGINE_CUP, "CupBracketManager: getAllMatchesForEdition found " + matches.size() + 
            " matches for edition " + editionId + " (searched both scheduled and played)");
        
        // Debug: Log breakdown by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match m : matches) {
            if (m.getRound() != null) {
                matchesByRound.put(m.getRound(), matchesByRound.getOrDefault(m.getRound(), 0) + 1);
            }
        }
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Matches by round: " + matchesByRound);
        
        // Debug: Log a few Round 2+ matches if they exist
        int round2PlusCount = 0;
        for (Match m : matches) {
            if (m.getRound() != null && m.getRound() > 1) {
                round2PlusCount++;
                if (round2PlusCount <= 3) {
                    Log.info(Log.ENGINE_CUP, "CupBracketManager: Round " + m.getRound() + " match found: " + 
                        m.getBracketPath() + " (ID: " + m.getId() + 
                        ", Edition: " + m.getCompetitionEditionId() + 
                        ", Parent1: " + m.getParentMatch1Id() + ", Parent2: " + m.getParentMatch2Id() + ")");
                }
            }
        }
        Log.info(Log.ENGINE_CUP, "CupBracketManager: Total Round 2+ matches found: " + round2PlusCount);
        
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
    
    /**
     * Get Competition (cup) for a given edition ID
     */
    private com.rndmodgames.futtoboru.data.Competition getCupForEdition(Long editionId) {
        if (editionId == null || currentGame == null) {
            return null;
        }
        
        // Search through all cups
        if (currentGame.getAllCups() != null) {
            for (com.rndmodgames.futtoboru.data.Competition cup : currentGame.getAllCups()) {
                if (cup != null && cup.getEditions() != null) {
                    for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : cup.getEditions()) {
                        if (edition != null && edition.getId() != null && edition.getId().equals(editionId)) {
                            return cup;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get CompetitionEdition for a given edition ID
     */
    private com.rndmodgames.futtoboru.data.CompetitionEdition getEdition(Long editionId) {
        if (editionId == null || currentGame == null) {
            return null;
        }
        
        // Search through all cups
        if (currentGame.getAllCups() != null) {
            for (com.rndmodgames.futtoboru.data.Competition cup : currentGame.getAllCups()) {
                if (cup != null && cup.getEditions() != null) {
                    for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : cup.getEditions()) {
                        if (edition != null && edition.getId() != null && edition.getId().equals(editionId)) {
                            return edition;
                        }
                    }
                }
            }
        }
        
        return null;
    }
}
