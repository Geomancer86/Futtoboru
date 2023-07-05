package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;

import com.badlogic.gdx.Game;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
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
    
    public FuttoboruGameEngine(Game parent, ScriptsManager scriptsManager) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
        this.scriptsManager = scriptsManager;
    }
    
    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }

    /**
     * TODO: Continue Game should be fully automatic, the Engine will take care of how long the game has to move forward
     *  
     *  - Day
     *  - Hours
     *  - Etc
     */
    public void continueGame() {
        
        System.out.println("ADVANCING THE SIMULATION - 1 DAY");
        
        // Get Current Day
        LocalDateTime current = gameInstance.getCurrentGame().getGameDate();
        
        // Increment By Required Unit
        gameInstance.getCurrentGame().setGameDate(current.plusDays(1));
        
        // Check Game Scripts
        scriptsManager.checkGameScripts();
        
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

        clubAcceptsFriendlyMatchRequest(gameInstance.getCurrentGame().getOwner().getCurrentClub());
        
        for (Club club : gameInstance.getCurrentGame().getAllClubs()) {

            // ignore own club in this list
            if (!club.getId().equals(gameInstance.getCurrentGame().getOwner().getCurrentClub().getId())) {
                clubAcceptsFriendlyMatchRequest(club);
            }
        }

        /**
         * Update Current Screen Components After Data Changes
         */
        mainMenuManager.updateDynamicComponents();
    }
    
    /**
     * TODO PROTOTYPE - MOVE TO A SEPARATE CLASS
     * 
     *  - 
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
        System.out.println("CLUB PROPOSED MATCHES 2: " + gameInstance.getCurrentGame().getOwner().getCurrentClub().getProposedMatches().size());
    }
}