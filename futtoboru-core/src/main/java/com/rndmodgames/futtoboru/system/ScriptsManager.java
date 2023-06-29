package com.rndmodgames.futtoboru.system;

import com.badlogic.gdx.Game;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Scripts Manager v1
 * 
 *  - Scripts will be bundled/loaded with each Season Data / Folder
 *  
 *  - Some scripts will be generic and should be bundled separately to avoid hardcoding them in here
 *  
 *  - We can hardcode scripts to quickly test stuff
 *  
 *      - Required for MVP:
 *          
 *          - Game just started / Welcome script:
 *              
 *              - Different scripts depending on CURRENT JOB picked on new game (UNEMPLOYED, OWNER, MANAGER)
 *              
 *          - 
 * 
 * @author Geomancer86
 */
public class ScriptsManager {

    //
    Futtoboru gameInstance;
    
    public ScriptsManager(Game parent) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
    }
    
    /**
     * TODO: do not check scripts that executed before
     */
    public void checkGameScripts() {
        
        System.out.println("CHECKING GAME SCRIPTS!");
        
        /**
         * Iterate all Scripts
         * 
         * TODO: iterate all NON EXECUTED scripts to avoid checking over and over
         */
        for (BasicScript script : gameInstance.getCurrentGame().getGameScripts()) {
            
            //
            System.out.println("SCRIPT: " + script.toString());
            
            /**
             * TODO:
             *  - Compare CURRENT_GAME_DATE and SCRIPT_DATE
             *  - Execute SCRIPT if dates are OK
             */
            
        }
    }
    
    /**
     * TODO WIP
     * 
     *  - This will create a League
     *  
     *  - Requires:
     *      - League Name
     *      - Country the league is based on
     *      - Division number (or automatic)
     *      - Teams
     *      - Rules
     *          - Most important aspect
     *          - League Style (yearly, home-and-away matches, etc)
     *          - League standings (win, lose, draw, goal average and points)
     *          - Relegation and Re-Election
     *          - Etc.
     */
    public void createLeague(String leagueName,
                             String leagueDescription,
                             Country leagueCountry) {
        
        //
        
    }
    
    
}