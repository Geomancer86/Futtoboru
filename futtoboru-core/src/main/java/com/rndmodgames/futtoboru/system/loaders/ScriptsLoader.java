package com.rndmodgames.futtoboru.system.loaders;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;

/**
 * Scripts Loader v1
 * 
 *  - Load the Season related game scripts  [WIP]
 *  - Load the generic game scripts         [TBD]
 *  
 * 
 * @author Geomancer86
 */
public class ScriptsLoader {

    /**
     * Load the Scripts bundled with a Season
     */
    public static void load(List<Season> seasons) {
        
        // iterate the seasons and load the existing scripts from data folders
        for (Season season : seasons) {
            
            /**
             * Hardcoded LEAGUE_CREATION_SCRIPT
             * 
             * TODO WIP
             */
            BasicScript theLeagueCreationScript = new BasicScript();
            
            // Name & Description
            theLeagueCreationScript.setName("The English Football League Creation");
            theLeagueCreationScript.setDescription("This will create The 1888–89 Football League with it's Rules and add the initial 12 English Teams");
            
            // Set Script Type
            theLeagueCreationScript.setScriptType(BasicScript.LEAGUE_CREATION_SCRIPT);
            
            /**
             * Script Dates:
             * 
             *  - The script executes on a predetermined date, this might or might not show a message (parametrizable).
             *  - The script result will execute on another date, this might or might not show a message (parametrizable).
             *  
             *  ---
             *  ACTUAL WORKFLOW
             *  
             *      on game start, we need a hardcoded script telling the player the league will be created on a meeting on [17 April 1888]
             *      the season creating script will run on [17 April 1888] and will add the league, teams and rules
             *          - optional original match ups (script accepted by player)
             *          - random match ups            (by the league scripted rules)
             *  
             *      an inbox message will tell the player about the first season of the league (needs Season Leagues Support)
             *  
             *  ---
             *  
             *  For example:
             *      - 17 April 1888. Manchester
             *          - The Football League is Created
             *          
             *      - 8 September 1888
             *          - Season Begins
             */
            
            
            
            // Season Scripts List
            season.setSeasonScripts(new ArrayList<>());
            
            // Add the League Creation Script to the League
            season.getSeasonScripts().add(theLeagueCreationScript);
        }
    }
}