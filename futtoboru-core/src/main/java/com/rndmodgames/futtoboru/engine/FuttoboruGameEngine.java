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
        // TODO: not used? where do the date comparison is done?
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
            
            /**
             * TODO WIP:
             * 
             *      - simulate match
             *      - save MatchResult objects
             */
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
         * 
         * TODO: this does nothing because the matches are played elsewhere
         */
        scheduler.checkClubSheduledMatches(currentClub);
        
        /**
         * TODO WIP:
         * 
         *  - Club Ticket Sales:
         *      - Iterate future incoming matches
         *          - If the match is close enough (7-10 days, parametrizable) there is a chance of ticket sales
         *          - If the match is completely sold, no tickets are sold, but we might save this uncovered demand number to show the player
         *          - If there is space, tickets are randomly sold
         *              - ticket demand multiplier:
         *                  - league match  : 100%
         *                  - friendly match: 50% or less
         *                  - cup match     : 150-300%
         *                  - classic match : 150-200%
         *                  - bonuses add
         *                  - cheaper and expensive tickets with more demand (half cost for friendly, double or triple cost for cups, finals might be even more)
         *                  
         *          
         *          - tickets sold this thay for this match must be saved
         *              - if we save them directly to the match is easier but we won't have a record for daily sales
         *              - if we create a daily sales object the addition is more difficult and more logic involved but we can show a chart
         *                  - doesn't make lots of sense for individual matches if sales are starting 10 days before, too much effort for very little info
         *                      - also the user cannot change ticket prices or influence in any direct way besides winning more matches
         *                      
         *          - charting TBD
         */                 
        
        /**
         * Update UI
         */
        mainMenuManager.updateDynamicComponents();
    }
}