package com.rndmodgames.futtoboru.engine;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Array;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.loaders.ScriptsLoader;

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
    SaveGame currentGame;
    
    public ScriptsManager(Game parent) {
        
        // keep track for easier access
        this.gameInstance = (Futtoboru) parent;
        
    }
    
    /**
     * TODO: do not check scripts that executed before
     */
    public void checkGameScripts() {
        
        System.out.println("CHECKING GAME SCRIPTS! " + gameInstance.getCurrentGame().getGameScripts().size());
        
        // keep track for easier access
        this.currentGame = gameInstance.getCurrentGame();
        
        /**
         * Iterate all Scripts
         * 
         * TODO: iterate all NON EXECUTED scripts to avoid checking over and over
         */
        for (BasicScript script : gameInstance.getCurrentGame().getGameScripts()) {
            
            // Execute Scripts Just Once!
            if (!script.getIsExecuted()) {
                
                // Script Date Is Before or Equal to Current Game Date
                if (script.getExecutionTime().isBefore(currentGame.getGameDate())
                        || script.getExecutionTime().isEqual(currentGame.getGameDate())) {
                    
                    System.out.println("EXECUTING BASIC SCRIPT: " + script.getName());
                    
                    switch (script.getScriptType()) {
                    case BasicScript.LEAGUE_CREATION_SCRIPT:
                        
                        // Create A League
                        createLeague(script);
                        
                        break;
                    
                    default:
                        System.out.println("SCRIPT TYPE NOT IMPLEMENTED, IGNORING!");
                        break;
                    }
                }
            }
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
     *      - Clubs
     *      - Rules
     *          - Most important aspect
     *          - League Style (yearly, home-and-away matches, etc)
     *          - League standings (win, lose, draw, goal average and points)
     *          - Relegation and Re-Election
     *          - Etc.
     */
    @SuppressWarnings("unchecked")
    public void createLeague(BasicScript script) {
        
        //
        System.out.println("EXECUTING LEAGUE CREATION SCRIPT!");
        
        //
        League league = new League();
        
        league.setName((String) script.getScriptValues().get(ScriptsLoader.LEAGUE_NAME));
        league.setCountry(DatabaseLoader.getCountryById((Long) script.getScriptValues().get(ScriptsLoader.LEAGUE_COUNTRY)));
        
        /**
         * Iterate Teams and add them to the League
         * 
         * NOTE: LibGDX Array to avoid serialization/deserialization issues (script is created with the same class).
         */
        Array<Long> test = (Array<Long>) script.getScriptValues().get(ScriptsLoader.LEAGUE_FOUNDING_TEAMS);
        
        league.setLeagueClubs(new ArrayList<>());
        
        for (Long clubId : test) {
            
            Club club = DatabaseLoader.getClubById(clubId);
            
            // Add Club to League
            league.getLeagueClubs().add(club);
        }
        
        // Save the created League on the current game
        currentGame.getMainAuthority().getLeagues().add(league);
        
        System.out.println("Added League to SaveGame: Total Leagues: " + currentGame.getMainAuthority().getLeagues().size());

        // Mark as executed to avoid running more than once
        script.setIsExecuted(true);
    }
}