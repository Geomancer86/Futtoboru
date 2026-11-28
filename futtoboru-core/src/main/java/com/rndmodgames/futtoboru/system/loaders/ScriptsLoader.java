package com.rndmodgames.futtoboru.system.loaders;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Map;

import com.badlogic.gdx.utils.Array;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;

/**
 * Scripts Loader v2
 * 
 *  - Builds the Season scripts from the season's editions in the game data (season.json): one league creation
 *    script per LEAGUE edition (its participants are the founders), one cup creation script per CUP edition
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
    public static final String LEAGUE_COMPETITION       = "LEAGUE_COMPETITION"; // competition runtime id
    public static final String LEAGUE_EDITION           = "LEAGUE_EDITION"; // the season's edition runtime id (P2-9)
    
    // Cup Support
    public static final String CUP_NAME                 = "CUP_NAME";
    public static final String CUP_COUNTRY              = "CUP_COUNTRY";
    public static final String CUP_PARTICIPANTS         = "CUP_PARTICIPANTS";
    public static final String CUP_COMPETITION          = "CUP_COMPETITION"; // competition runtime id
    public static final String CUP_EDITION              = "CUP_EDITION"; // the season's edition runtime id (P2-9)
    
    /**
     * Load the Scripts bundled with a Season
     * 
     * The league is founded on 17 April of the season's start year (the Football League, 1888); a cup opens on its
     * season calendar's opening day (P2-14, season.json `calendar`).
     * 
     * @param editions the season's editions (in data order) with their competition
     */
    public static void load(Season season, Map<CompetitionEdition, Competition> editions) {
        
        // Season Scripts List
        season.setSeasonScripts(new ArrayList<>());
        
        int year = season.getStartDate() != null ? season.getStartDate().getYear() : 1888;
        Long countryId = season.getCountries() != null && !season.getCountries().isEmpty()
                ? season.getCountries().get(0).getId() : null;
        
        for (Map.Entry<CompetitionEdition, Competition> entry : editions.entrySet()) {
            
            CompetitionEdition edition = entry.getKey();
            Competition competition = entry.getValue();
            
            Array<Long> clubIds = new Array<>();
            for (Long clubId : edition.getParticipantClubsIds()) {
                clubIds.add(clubId);
            }
            
            if (Competition.COMPETITION_LEAGUE.equals(competition.getCompetitionType())) {
                
                /**
                 * 1) LEAGUE_CREATION_SCRIPT
                 */
                BasicScript theLeagueCreationScript = new BasicScript();
                
                // Name & Description
                theLeagueCreationScript.setName("The " + competition.getName() + " Creation");
                theLeagueCreationScript.setDescription("This will create the " + edition.getName()
                        + " with its Rules and add the " + clubIds.size + " founder clubs");
                
                // Set Script Type
                theLeagueCreationScript.setScriptType(BasicScript.LEAGUE_CREATION_SCRIPT);
                
                /**
                 * Script execution date
                 */
                LocalDateTime creation = LocalDateTime.of(year, Month.APRIL, 17, 19, 30, 00); // 17 April 1888
                theLeagueCreationScript.setExecutionTime(creation);
                
                /**
                 * Add Required Script Values (clubs by runtime id, from the edition's participants)
                 */
                theLeagueCreationScript.getScriptValues().put(LEAGUE_NAME, competition.getName());
                theLeagueCreationScript.getScriptValues().put(LEAGUE_CREATION_DATE, creation);
                theLeagueCreationScript.getScriptValues().put(LEAGUE_COUNTRY, countryId);
                theLeagueCreationScript.getScriptValues().put(LEAGUE_COMPETITION, competition.getId());
                theLeagueCreationScript.getScriptValues().put(LEAGUE_EDITION, edition.getId());
                theLeagueCreationScript.getScriptValues().put(LEAGUE_FOUNDING_TEAMS, clubIds);
                theLeagueCreationScript.getScriptValues().put(LEAGUE_RULES, null);
                
                season.getSeasonScripts().add(theLeagueCreationScript);
                
            } else if (Competition.COMPETITION_CUP.equals(competition.getCompetitionType())) {
                
                /**
                 * 2) CUP_CREATION_SCRIPT (FA Cup)
                 * P2-14: executed on the day the season calendar opens the cup (season.json `calendar`): 6 Oct 1888,
                 * with the qualifying rounds; it was 1 May until then. The draw comes later (SeasonCalendar).
                 */
                BasicScript theCupCreationScript = new BasicScript();
                theCupCreationScript.setName("The " + edition.getName() + " Creation");
                theCupCreationScript.setDescription("This will create the " + edition.getName() + " edition and add all participants");
                theCupCreationScript.setScriptType(BasicScript.CUP_CREATION_SCRIPT);
                theCupCreationScript.setExecutionTime(com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar
                        .forSeason(year).withCup(competition.getDataId()).cupOpens().atStartOfDay());
                
                theCupCreationScript.getScriptValues().put(CUP_NAME, competition.getName());
                theCupCreationScript.getScriptValues().put(CUP_COUNTRY, countryId);
                theCupCreationScript.getScriptValues().put(CUP_COMPETITION, competition.getId());
                theCupCreationScript.getScriptValues().put(CUP_EDITION, edition.getId());
                theCupCreationScript.getScriptValues().put(CUP_PARTICIPANTS, clubIds);
                
                season.getSeasonScripts().add(theCupCreationScript);
            }
        }
    }
}
