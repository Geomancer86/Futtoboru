package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Authority Manager v1
 * 
 *  - handle the decisions taken by the Game Authorities (ie: football associations, IFAB, FIFA, etc)
 *  
 *  - TODO:
 *      - iterate all authorities, this should be done in a hierarchical way as we have parent/children authorities
 *          
 *          - authority checks if existing competitions are scheduled/playing/finished
 *              
 *              - if a competition dont have a competition edition for the current season, the authority creates one
 *                  - this needs to take care of (as needed):
 *                      - inviting clubs
 *                      - automatically promoting clubs in case of cups/first round propers
 *                      - scheduling playoffs and rematches
 *                      - scheduling league matches
 *                      - changing the game rules (position table sorting rules, ie: 2 points for win changed to 3 points for win, etc. /offsides/redcards/etc / scripted)
 *                      - awarding prize money to champions/runnersups/participants/etc
 *                      - splitting money for games (setting the ruling)
 *                      - bans/fines
 *                      - 
 * 
 * @author Geomancer86
 */
public class AuthorityManager {

    // for quick reference
    Futtoboru game;
    SaveGame currentGame;
    Authority mainAuthority;
    LeagueFixtureGenerator fixtureGenerator;
    
    //
    public AuthorityManager(Futtoboru parent) {
        
        //
        this.game = parent;
        this.currentGame = game.getCurrentGame();
        this.fixtureGenerator = new LeagueFixtureGenerator(parent);
    }
    
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
    public void checkCompetitionsSchedule() {

        //
        Gdx.app.debug("AuthorityManager", "checkCompetitionsSchedule()");
        
        if (currentGame == null || currentGame.getMainAuthority() == null) {
            return;
        }
        
        mainAuthority = currentGame.getMainAuthority();
        
        /**
         * Iterate all over the game current/existing competitions (hierarchical)
         *  - if the season doesnt exist, create one
         *  - if the season exist:
         *      - check if matches are scheduled
         *      - draw matches
         */
        
        // Check leagues and generate fixtures if needed
        checkAndScheduleLeagueFixtures();
        
        // TODO: Check cups and generate draws if needed
        // checkAndScheduleCupDraws();
    }
    
    /**
     * Check all leagues and generate fixtures if they don't have any scheduled
     */
    private void checkAndScheduleLeagueFixtures() {
        Gdx.app.log("AuthorityManager", "checkAndScheduleLeagueFixtures() called");
        
        if (mainAuthority == null) {
            Gdx.app.error("AuthorityManager", "mainAuthority is null!");
            return;
        }
        
        if (mainAuthority.getLeagues() == null) {
            Gdx.app.error("AuthorityManager", "mainAuthority.getLeagues() is null!");
            return;
        }
        
        List<League> leagues = mainAuthority.getLeagues();
        
        Gdx.app.log("AuthorityManager", "Found " + leagues.size() + " leagues in main authority");
        
        if (leagues.isEmpty()) {
            Gdx.app.log("AuthorityManager", "No leagues found in main authority");
            return;
        }
        
        // Get season start date (use game start date or season start date)
        LocalDateTime seasonStart = getSeasonStartDate();
        LocalDateTime seasonEnd = getSeasonEndDate(seasonStart);
        
        Gdx.app.log("AuthorityManager", "Season dates: " + seasonStart + " to " + seasonEnd);
        
        for (League league : leagues) {
            if (league == null) {
                Gdx.app.error("AuthorityManager", "Found null league in list");
                continue;
            }
            
            Gdx.app.log("AuthorityManager", "Checking league: " + league.getName());
            
            if (league.getLeagueClubs() == null) {
                Gdx.app.error("AuthorityManager", "League " + league.getName() + " has null leagueClubs list");
                continue;
            }
            
            if (league.getLeagueClubs().isEmpty()) {
                Gdx.app.error("AuthorityManager", "League " + league.getName() + " has no clubs");
                continue;
            }
            
            Gdx.app.log("AuthorityManager", "League " + league.getName() + " has " + league.getLeagueClubs().size() + " clubs");
            
            // Check if fixtures are already scheduled
            boolean hasFixtures = fixtureGenerator.hasFixturesScheduled(league);
            Gdx.app.log("AuthorityManager", "League " + league.getName() + " has fixtures scheduled: " + hasFixtures);
            
            if (!hasFixtures) {
                Gdx.app.log("AuthorityManager", "Generating fixtures for league: " + league.getName());
                
                // Generate fixtures
                List<com.rndmodgames.futtoboru.data.Match> fixtures = 
                    fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
                
                Gdx.app.log("AuthorityManager", "Generated " + fixtures.size() + " fixtures for " + league.getName());
            } else {
                Gdx.app.debug("AuthorityManager", "League " + league.getName() + " already has fixtures scheduled");
            }
        }
    }
    
    /**
     * Get season start date
     * Uses game start date or season start date if available
     */
    private LocalDateTime getSeasonStartDate() {
        // Try to get from current season if available
        // For now, use game start date or current game date
        if (currentGame.getGameStartDate() != null) {
            return currentGame.getGameStartDate();
        }
        if (currentGame.getGameDate() != null) {
            return currentGame.getGameDate();
        }
        // Default: September 1st of current year (typical season start)
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), 9, 1, 15, 0); // September 1st, 3 PM
    }
    
    /**
     * Get season end date (typically 9 months after start, around May/June)
     */
    private LocalDateTime getSeasonEndDate(LocalDateTime seasonStart) {
        // Typical season: September to May (9 months)
        return seasonStart.plusMonths(9);
    }
}