package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.Game;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.temporal.MatchScheduler;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.ScriptsManager;

/**
 * Game Engine v1
 *  
 *  - TODO WIP:
 *  
 *      - Get an instance of this running with the Game
 *      - Move the CONTINUE GAME Button functionality to this class
 *      - Implement Script Execution by LocalDateTime
 *      -
 *      -
 *  
 * @author Geomancer86
 */
public class FuttoboruGameEngine {

    // 
    private Futtoboru gameInstance;
    private ScriptsManager scriptsManager;
    private MainMenuManager mainMenuManager;
    
    //
    MatchScheduler scheduler;
    
    //
    public static final int CONTINUE_GAME_ACTION = 1;
    public static final int MATCH_PREVIEW_ACTION = 2;
    public static final int MATCH_RESULT_ACTION = 3;

    public FuttoboruGameEngine(Game parent, ScriptsManager scriptsManager) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
        this.scriptsManager = scriptsManager;
        
        //
        this.scheduler = new MatchScheduler(gameInstance);
    }
    
    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }
    
    /**
     * Check if the Game can CONTINUE or MATCH PREVIEW
     * 
     * 1: CONTINUE GAME
     * 2: MATCH PREVIEW
     */
    public int getNextGameAction() {

        // Current Club
        Club currentClub = gameInstance.getCurrentGame().getCurrentClub();
        
        /**
         * Check if Current Club has a MATCH TODAY
         */
        boolean matchDay = scheduler.checkClubMatchDay(currentClub);
        
        //
        if (matchDay) {

            return MATCH_PREVIEW_ACTION;
        }

        return CONTINUE_GAME_ACTION;
    }
    
    /**
     * 
     */
    public void getMatchResult() {
        
        //
        System.out.println("MATCH RESULT - ADVANCED THE SIMULATION - 90 MINUTES");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        // Increment By Required Unit
        // TODO: fix matches not appearing because we change the time of day
//        gameInstance.getCurrentGame().setGameDate(current.plusMinutes(90));
        
        // Mark match as Played
        Club currentClub = gameInstance.getCurrentGame().getCurrentClub();
        
        // TODO: do not recreate the comparator every time
        Comparator<Match> comparatorChronological = (match1, match2) -> match1.getMatchDateTime()
                                                             .compareTo(match2.getMatchDateTime());
        
        // TODO: not required to do on every turn, only on insert new scheduled match
        // Sort
        Collections.sort(currentClub.getScheduledMatches(), comparatorChronological);
        
        // Check we have at least one match
        if (!currentClub.getScheduledMatches().isEmpty()) {
            
            // First scheduled match on list will be the next
            Match nextMatch = currentClub.getScheduledMatches().get(0);
            
            nextMatch.setIsPlayed(true);
            
            // add to played
            currentClub.getPlayedMatches().add(nextMatch);
            
            // remove from scheduled
            currentClub.getScheduledMatches().remove(nextMatch);
        }
        
        System.out.println("CLUB PLAYED MATCHES: " + currentClub.getPlayedMatches().size());
        
        // Update UI
        mainMenuManager.updateDynamicComponents();
    }
    
    /**
     * TODO: Continue Game should be fully automatic, the Engine will take care of how long the game has to move forward
     *  
     *  - Day
     *  - Hours
     *  - Etc
     *  
     *  TODO: add a loading screen or processing indicator
     */
    public void continueGame() {
        
        //
        System.out.println("ADVANCING THE SIMULATION");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        // Increment By Required Unit
        gameInstance.getCurrentGame().setGameDate(current.plusDays(1));
        
        // Check Game Scripts
        scriptsManager.checkGameScripts();
        
        // Update UI
        mainMenuManager.updateDynamicComponents();
        
        // Current Club
        Club currentClub = gameInstance.getCurrentGame().getCurrentClub();

        /**
         * Check Proposed Friendlies
         */
        scheduler.checkClubProposedMatches(currentClub);
        
        /**
         * Check Scheduled Matches
         */
        scheduler.checkClubSheduledMatches(currentClub);
        
        /**
         * Update UI
         */
        mainMenuManager.updateDynamicComponents();

        
//        System.out.println("ADVANCING THE SIMULATION - 1 DAY");
//        
//        // Get Current Day
//        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
//        
//        // Increment By Required Unit
//        gameInstance.getCurrentGame().setGameDate(current.plusDays(1));
//        
//        // Check Game Scripts
//        scriptsManager.checkGameScripts();
//        
//        //
//        
        
        /**
         * TODO WIP:
         * 
         *  - Match Preview and Match Simulation
         *  
         *      - Calculate if we have a match TODAY
         *      - Show the Match Preview Button in that case
         *          - This will disable the continue game for 1 click avoiding missing the match
         *          - The match preview shows basic match data:
         *              - Clubs,
         *              - Average values for clubs
         *              - Key Players?
         *              - Odds
         *              
         *      - Show the Simulate Match/ Match Result buttons
         *          - Time goes forward 90 minutes (180-etc)
         *          - Game shows the Match Result Screen.
         *              - Continue Game Button is back as normal.
         */
        
        /**
         * TODO: WIP:
         * 
         *  - Check Proposed Friendly Matches (AI accepts or cancel) MVP: either hardcode it or accept always if schedule is OK
         *  - Check Scheduled Matches
         *      - On Scheduled Match Day
         *          - Continue Button Changes to Match Button
         *              - Call the Simulation Engine (fully random result)
         */
        
        
        
        /**
         * Iterate all clubs and solve friendly match requests
         *  - Each club will have the friendlies their manager requested
         *      - Initially only the player can request friendlies
         *      
         *  - Avoid conflicts if different clubs request friendlies, depending on the order of acceptance
         *  - Avoid scheduling conflicts, if a club accepts a friendly for X day, then it cannot accept the next requests, all should be cancelled by default
         */

//        // TODO: match requests need to be replied by the player
//        clubAcceptsFriendlyMatchRequest(currentClub);
//        
//        for (Club club : gameInstance.getCurrentGame().getAllClubs()) {
//
//            // ignore own club in this list
//            if (!club.getId().equals(currentClub.getId())) {
//                clubAcceptsFriendlyMatchRequest(club);
//            }
//        }

        /**
         * Update Current Screen Components After Data Changes
         */
//        mainMenuManager.updateDynamicComponents();
    }
    
    /**
     * TODO PROTOTYPE - MOVE TO A SEPARATE CLASS
     * 
     *  - TODO:
     *      - DO NOT LET TO REQUEST A FRIENDLY ON A GAME WITH EITHER AN EXISTING REQUEST OR AN SCHEDULED MATCH
     *      - 
     *      
     *  TODO: do not split the clubs between existing clubs list and current club because this serializes two objects and ends up
     *          with changes only done on a single side of them
     *          
     *          - just save the current club id on save game and always use the single club instance
     */
    public void clubAcceptsFriendlyMatchRequest(Club club) {
        
        // 
        System.out.println("SOLVING AI FOR CLUB: " + club.getName());
        System.out.println("CLUB PROPOSED MATCHES: " + club.getProposedMatches());
        
        //
        for (Match match : club.getProposedMatches()) {
            
            /**
             * Accept or Reject Friendly Match
             * 
             *  - TODO: Team shouldn't have a Match in the same day
             *  - TODO: Team shouldn't have other Matches x days before and after (parametrize, this would be how careful is the manager).
             */
            
            boolean freeSchedule = true;
            
            // 
            System.out.println("CHECKING FRIENDY PROPOSAL AGAINST " + club.getScheduledMatches().size() + " SCHEDULED MATCHES");
            
            for (Match scheduled : club.getScheduledMatches()) {
                
            }
            
            //
            if (freeSchedule) {
                
                //
                System.out.println("Schedule is free, accepting friendly request!");
                
                match.setIsAccepted(true);
                
                // add to accepted matches
                club.getScheduledMatches().add(match);
                
            } else {
                
                System.out.println("Friendly request cannot be accepted, scheduling conflicts!");
            }
            
//            LocalDateTime beforeDate = match.getProposeDateTime().plusDays(3).at;
        }
        
        // we clear the list and everything that is not accepted will be gone
        club.getProposedMatches().clear();
        System.out.println("CLUB PROPOSED MATCHES: " + club.getProposedMatches());
    }
}