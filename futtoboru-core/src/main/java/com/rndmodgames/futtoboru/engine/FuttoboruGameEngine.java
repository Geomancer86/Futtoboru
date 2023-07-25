package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.temporal.CompetitionScheduler;
import com.rndmodgames.futtoboru.engine.temporal.MatchScheduler;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

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
    CompetitionScheduler competitionScheduler;
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
        this.competitionScheduler = new CompetitionScheduler(gameInstance);
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
        Gdx.app.debug("FuttoboruGameEngine", "ADVANCING THE SIMULATION");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        /**
         * Increment By Required Unit
         * 
         * TODO: make it AM/PM or advance in smaller amount of time depending on time of season/etc as in FM
         */
        gameInstance.getCurrentGame().setGameDate(current.plusDays(1));
        
        /**
         * Authorities AI
         * 
         *  - iterates all over the game authorities
         *      - check if cups are without scheduled draws and/or matches
         *          - schedule/draw accordingly
         *          
         *      - check if rounds are finished
         *          - advance rounds
         *          
         *      - check if rematches need to be scheduled
         *          - schedule rematches
         *          
         *      - give match prizes/entry to clubs (cup splits 50/50 and league is 70/30) TODO make it parametrizable
         *      
         *      - give competition prizes for winners/runners up at the end of competition
         *      
         *      - handle re-elections and relegation
         *      
         *      - make sure the competitions are perpetual (ie: they should be played once a year forever as long as no scripts change something).
         *      
         *  TODO: iterate hierarchy of authorities
         *  TODO: draw FA Cup
         *  TODO: play FA Cup
         *  TODO: finish/restart FA Cup
         */
        Authority mainAuthority = gameInstance.getCurrentGame().getMainAuthority();
        
        // FA Cup
//        Competition faCup = DatabaseLoader.getCompetitions().get(0);
//
//        // FA Cup, season 18 (first v1 game season)
//        CompetitionEdition faCupSeason = faCup.getEditions().get(0);

//        competitionScheduler.initializeCompetition();
        
        
        // Check Game Scripts
        scriptsManager.checkGameScripts();
        
        /**
         * Current Club
         * 
         * NOTE: player might not have a CURRENT_CLUB
         *       
         *       - simulate all clubs, not only player controlled
         */
        for (Club currentClub : gameInstance.getCurrentGame().getAllClubs()) {

            //
            Gdx.app.debug("FuttoboruGameEngine", "PROCESSING CLUB: " + currentClub.getName());
            
            /**
             * Check Proposed Friendlies
             */
            scheduler.checkClubProposedMatches(currentClub);
            
            /**
             * Check Scheduled Matches
             */
            scheduler.checkClubSheduledMatches(currentClub);
            
        }
        
        /**
         * Update UI
         * NOTE: this will be null on Unit Tests
         */
        if (mainMenuManager != null) {
            
            mainMenuManager.updateDynamicComponents();
        }
    }

    public CompetitionScheduler getCompetitionScheduler() {
        return competitionScheduler;
    }

    public void setCompetitionScheduler(CompetitionScheduler competitionScheduler) {
        this.competitionScheduler = competitionScheduler;
    }
}