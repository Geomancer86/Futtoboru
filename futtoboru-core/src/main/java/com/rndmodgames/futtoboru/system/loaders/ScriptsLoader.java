package com.rndmodgames.futtoboru.system.loaders;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;
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
     * Supported
     */
    public static final String LEAGUE_NAME              = "LEAGUE_NAME";
    public static final String LEAGUE_CREATION_DATE     = "LEAGUE_CREATION_DATE";
    public static final String LEAGUE_COUNTRY           = "LEAGUE_COUNTRY";
    public static final String LEAGUE_FOUNDING_TEAMS    = "LEAGUE_FOUNDING_TEAMS";
    public static final String LEAGUE_RULES             = "LEAGUE_RULES";
    
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
             * 
             *  - 1) this should be loaded from the file system, with files flexible enough to be easy to be edited by game designers
             */
            BasicScript theLeagueCreationScript = new BasicScript();
            
            // Name & Description
            theLeagueCreationScript.setName("The English Football League Creation");
            theLeagueCreationScript.setDescription("This will create The 1888–89 Football League with it's Rules and add the initial 12 English Teams");
            
            // Set Script Type
            theLeagueCreationScript.setScriptType(BasicScript.LEAGUE_CREATION_SCRIPT);
            
            /**
             * Script execution date (note this might differ from any dates inside the data)
             */
            theLeagueCreationScript.setExecutionTime(LocalDateTime.of(1888, Month.JANUARY, 17, 19, 30, 00));
            
            /**
             * Add Required Script Values
             */
            theLeagueCreationScript.getScriptValues().put(LEAGUE_NAME, "English Football League");
            theLeagueCreationScript.getScriptValues().put(LEAGUE_CREATION_DATE, LocalDateTime.of(1888, Month.APRIL, 17, 19, 30, 00)); // Created and named in Manchester during a meeting on 17 April 1888
//            theLeagueCreationScript.getScriptValues().put(LEAGUE_CREATION_DATE, LocalDateTime.of(1888, Month.JANUARY, 17, 19, 30, 00)); // EARLIER DATE FOR QUICKER TESTING!
            theLeagueCreationScript.getScriptValues().put(LEAGUE_COUNTRY, 1000L); // By ID
            
            /**
             * League Clubs by ID
             * 
             * NOTE: LibGDX Array to avoid serialization/deserialization issues (script is created with the same class).
             */
            Array<Long> leagueClubIds = new Array<>();
            leagueClubIds.add(1L);
            leagueClubIds.add(2L);
            leagueClubIds.add(3L);
            leagueClubIds.add(4L);
            leagueClubIds.add(5L);
            leagueClubIds.add(6L);
            leagueClubIds.add(7L);
            leagueClubIds.add(8L);
            leagueClubIds.add(9L);
            leagueClubIds.add(10L);
            leagueClubIds.add(11L);
            leagueClubIds.add(12L);
            
            theLeagueCreationScript.getScriptValues().put(LEAGUE_FOUNDING_TEAMS, leagueClubIds); // List of IDs
            theLeagueCreationScript.getScriptValues().put(LEAGUE_RULES, null); // List of League Rule Objects TBD
            
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