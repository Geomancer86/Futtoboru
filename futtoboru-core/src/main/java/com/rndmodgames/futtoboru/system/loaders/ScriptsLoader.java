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
    
    // Cup Support
    public static final String CUP_NAME                 = "CUP_NAME";
    public static final String CUP_COUNTRY              = "CUP_COUNTRY";
    public static final String CUP_PARTICIPANTS         = "CUP_PARTICIPANTS";
    
    /**
     * Load the Scripts bundled with a Season
     */
    public static void load(List<Season> seasons) {
        
        // iterate the seasons and load the existing scripts from data folders
        for (Season season : seasons) {
            
            /**
             * 1) LEAGUE_CREATION_SCRIPT
             */
            BasicScript theLeagueCreationScript = new BasicScript();
            
            // Name & Description
            theLeagueCreationScript.setName("The English Football League Creation");
            theLeagueCreationScript.setDescription("This will create The 1888-89 Football League with it's Rules and add the initial 12 English Teams");
            
            // Set Script Type
            theLeagueCreationScript.setScriptType(BasicScript.LEAGUE_CREATION_SCRIPT);
            
            /**
             * Script execution date
             */
            theLeagueCreationScript.setExecutionTime(LocalDateTime.of(1888, Month.APRIL, 17, 19, 30, 00)); // 17 April 1888
            
            /**
             * Add Required Script Values
             */
            theLeagueCreationScript.getScriptValues().put(LEAGUE_NAME, "English Football League");
            theLeagueCreationScript.getScriptValues().put(LEAGUE_CREATION_DATE, LocalDateTime.of(1888, Month.APRIL, 17, 19, 30, 00)); 
            theLeagueCreationScript.getScriptValues().put(LEAGUE_COUNTRY, 1000L); // By ID
            
            /**
             * League Clubs by ID
             */
            Array<Long> leagueClubIds = new Array<>();
            for (long i = 1; i <= 12; i++) {
                leagueClubIds.add(i);
            }
            
            theLeagueCreationScript.getScriptValues().put(LEAGUE_FOUNDING_TEAMS, leagueClubIds); 
            theLeagueCreationScript.getScriptValues().put(LEAGUE_RULES, null); 
            
            /**
             * 2) CUP_CREATION_SCRIPT (FA Cup)
             * Executed on May 1st, 1888
             */
            BasicScript theCupCreationScript = new BasicScript();
            theCupCreationScript.setName("The FA Cup 1888-89 Creation");
            theCupCreationScript.setDescription("This will create the 1888-89 FA Cup edition and add all participants");
            theCupCreationScript.setScriptType(BasicScript.CUP_CREATION_SCRIPT);
            theCupCreationScript.setExecutionTime(LocalDateTime.of(1888, Month.MAY, 1, 10, 0, 0));
            
            theCupCreationScript.getScriptValues().put(CUP_NAME, "FA Cup");
            theCupCreationScript.getScriptValues().put(CUP_COUNTRY, 1000L);
            
            Array<Long> cupClubsIds = new Array<>();
            // 1888-89 FA Cup First Round had exactly 32 teams (16 matches)
            // Historical participants: 12 League teams + 20 non-league teams = 32 teams
            // Using IDs 1-32 (excluding IDs 33-35 which were not in the first round)
            // Note: Some teams may have been eliminated in qualifying rounds
            for (long i = 1; i <= 32; i++) {
                cupClubsIds.add(i);
            }
            theCupCreationScript.getScriptValues().put(CUP_PARTICIPANTS, cupClubsIds);
            
            // Season Scripts List
            season.setSeasonScripts(new ArrayList<>());
            
            // Add the Scripts to the Season
            season.getSeasonScripts().add(theLeagueCreationScript);
            season.getSeasonScripts().add(theCupCreationScript);
        }
    }
}