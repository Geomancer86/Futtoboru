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
         * 
         * CRITICAL FIX: Ensure league clubs are in SaveGame, not just DatabaseLoader.
         * This fixes the issue where fixture generator can't find clubs.
         */
        Array<Long> test = (Array<Long>) script.getScriptValues().get(ScriptsLoader.LEAGUE_FOUNDING_TEAMS);
        
        league.setLeagueClubs(new ArrayList<>());
        
        for (Long clubId : test) {
            
            // Try to get club from SaveGame first (preferred)
            Club club = currentGame.getClubById(clubId);
            
            if (club == null) {
                // Fallback to DatabaseLoader
                club = DatabaseLoader.getClubById(clubId);
                
                if (club != null) {
                    // Add to SaveGame so fixture generator can find it
                    System.out.println("Adding club " + club.getName() + " (ID: " + clubId + ") to SaveGame from DatabaseLoader");
                    if (currentGame.getAllClubs() == null) {
                        currentGame.setAllClubs(new ArrayList<>());
                    }
                    currentGame.getAllClubs().add(club);
                } else {
                    System.out.println("ERROR: Club ID " + clubId + " not found in DatabaseLoader or SaveGame!");
                    continue;
                }
            }
            
            // Add Club to League (now guaranteed to be in SaveGame)
            league.getLeagueClubs().add(club);
            System.out.println("Added club " + club.getName() + " (ID: " + clubId + ") to league " + league.getName());
        }
        
        // Save the created League on the current game
        currentGame.getMainAuthority().getLeagues().add(league);
        
        System.out.println("Added League to SaveGame: Total Leagues: " + currentGame.getMainAuthority().getLeagues().size());
        System.out.println("League " + league.getName() + " has " + league.getLeagueClubs().size() + " clubs");

        // Mark as executed to avoid running more than once
        script.setIsExecuted(true);
        
        /**
         * Generate fixtures immediately when league is created
         * This ensures fixtures are ready right away, not waiting for daily check
         */
        try {
            com.badlogic.gdx.Gdx.app.log("ScriptsManager", "Generating fixtures for newly created league: " + league.getName());
            
            // Get season dates (use game start date or current date)
            java.time.LocalDateTime seasonStart = currentGame.getGameStartDate();
            if (seasonStart == null) {
                seasonStart = currentGame.getGameDate();
            }
            // Season typically runs September to May (9 months)
            java.time.LocalDateTime seasonEnd = seasonStart.plusMonths(9);
            
            // Generate fixtures
            com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator fixtureGenerator = 
                new com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator(gameInstance);
            
            java.util.List<com.rndmodgames.futtoboru.data.Match> fixtures = 
                fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
            
            System.out.println("Generated " + fixtures.size() + " fixtures for league " + league.getName());
            com.badlogic.gdx.Gdx.app.log("ScriptsManager", "Successfully generated " + fixtures.size() + " fixtures for league " + league.getName());
            
        } catch (Exception e) {
            System.out.println("ERROR generating fixtures for league " + league.getName() + ": " + e.getMessage());
            e.printStackTrace();
            com.badlogic.gdx.Gdx.app.error("ScriptsManager", "Failed to generate fixtures for league " + league.getName(), e);
        }
        
        /**
         * Schedule league creation announcement message (v2.0)
         */
        try {
            if (gameInstance.getGameEngine() != null) {
                com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
                    gameInstance.getGameEngine().getMessageManager();
                
                if (messageManager != null) {
                    // Create league creation message
                    com.rndmodgames.futtoboru.data.Message leagueMessage = 
                        messageManager.createLeagueCreationMessage(league);
                    
                    // Schedule for league creation date (current date when league is created)
                    java.time.LocalDateTime leagueCreationDate = currentGame.getGameDate();
                    leagueMessage.setScheduledDate(leagueCreationDate);
                    messageManager.scheduleMessage(leagueMessage);
                    
                    System.out.println("Scheduled league creation message for " + leagueCreationDate);
                    
                    // Schedule fixture release message (1 day after league creation)
                    if (fixtures != null && fixtures.size() > 0) {
                        com.rndmodgames.futtoboru.data.Message fixtureMessage = 
                            messageManager.createFixtureReleaseMessage(league);
                        
                        java.time.LocalDateTime fixtureReleaseDate = currentGame.getGameDate().plusDays(1);
                        fixtureMessage.setScheduledDate(fixtureReleaseDate);
                        messageManager.scheduleMessage(fixtureMessage);
                        
                        System.out.println("Scheduled fixture release message for " + fixtureReleaseDate);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR scheduling league messages: " + e.getMessage());
            e.printStackTrace();
        }
    }
}