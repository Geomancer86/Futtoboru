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
        
        System.out.println("========================================");
        System.out.println("EXECUTING LEAGUE CREATION SCRIPT!");
        System.out.println("========================================");
        
        // Verify SaveGame is available
        if (currentGame == null) {
            System.out.println("ERROR: currentGame is null! Cannot create league.");
            com.badlogic.gdx.Gdx.app.error("ScriptsManager", "currentGame is null in createLeague()");
            return;
        }
        
        // Verify mainAuthority exists
        if (currentGame.getMainAuthority() == null) {
            System.out.println("ERROR: mainAuthority is null! Cannot add league.");
            com.badlogic.gdx.Gdx.app.error("ScriptsManager", "mainAuthority is null in createLeague()");
            return;
        }
        
        // Initialize leagues list if null
        if (currentGame.getMainAuthority().getLeagues() == null) {
            System.out.println("WARNING: mainAuthority.getLeagues() is null, initializing...");
            currentGame.getMainAuthority().setLeagues(new java.util.ArrayList<>());
        }
        
        System.out.println("Current leagues count before creation: " + currentGame.getMainAuthority().getLeagues().size());
        
        //
        League league = new League();
        
        // Get league name
        String leagueName = (String) script.getScriptValues().get(ScriptsLoader.LEAGUE_NAME);
        if (leagueName == null) {
            System.out.println("ERROR: League name is null in script!");
            return;
        }
        league.setName(leagueName);
        System.out.println("Creating league: " + leagueName);
        
        // Get league country
        Long countryId = (Long) script.getScriptValues().get(ScriptsLoader.LEAGUE_COUNTRY);
        if (countryId == null) {
            System.out.println("ERROR: League country ID is null in script!");
            return;
        }
        league.setCountry(DatabaseLoader.getCountryById(countryId));
        if (league.getCountry() == null) {
            System.out.println("ERROR: Country with ID " + countryId + " not found!");
            return;
        }
        System.out.println("League country: " + league.getCountry().getCommonName());
        
        /**
         * Iterate Teams and add them to the League
         * 
         * NOTE: LibGDX Array to avoid serialization/deserialization issues (script is created with the same class).
         * 
         * CRITICAL FIX: Ensure league clubs are in SaveGame, not just DatabaseLoader.
         * This fixes the issue where fixture generator can't find clubs.
         */
        Array<Long> clubIds = (Array<Long>) script.getScriptValues().get(ScriptsLoader.LEAGUE_FOUNDING_TEAMS);
        
        if (clubIds == null || clubIds.size == 0) {
            System.out.println("ERROR: No club IDs found in script!");
            return;
        }
        
        System.out.println("Found " + clubIds.size + " club IDs in script");
        
        league.setLeagueClubs(new ArrayList<>());
        
        // Ensure allClubs list exists
        if (currentGame.getAllClubs() == null) {
            currentGame.setAllClubs(new ArrayList<>());
            System.out.println("Initialized allClubs list");
        }
        
        int clubsAdded = 0;
        for (Long clubId : clubIds) {
            if (clubId == null) {
                System.out.println("WARNING: Null club ID in list, skipping");
                continue;
            }
            
            // Try to get club from SaveGame first (preferred)
            Club club = currentGame.getClubById(clubId);
            
            if (club == null) {
                // Fallback to DatabaseLoader
                club = DatabaseLoader.getClubById(clubId);
                
                if (club != null) {
                    // Add to SaveGame so fixture generator can find it
                    System.out.println("Adding club " + club.getName() + " (ID: " + clubId + ") to SaveGame from DatabaseLoader");
                    currentGame.getAllClubs().add(club);
                } else {
                    System.out.println("ERROR: Club ID " + clubId + " not found in DatabaseLoader or SaveGame!");
                    continue;
                }
            }
            
            // Add Club to League (now guaranteed to be in SaveGame)
            league.getLeagueClubs().add(club);
            clubsAdded++;
            System.out.println("Added club " + club.getName() + " (ID: " + clubId + ") to league " + league.getName());
        }
        
        System.out.println("Total clubs added to league: " + clubsAdded + " / " + clubIds.size);
        
        if (league.getLeagueClubs().isEmpty()) {
            System.out.println("ERROR: No clubs were added to league! Cannot create empty league.");
            return;
        }
        
        // Save the created League on the current game
        currentGame.getMainAuthority().getLeagues().add(league);
        
        System.out.println("========================================");
        System.out.println("LEAGUE CREATION SUCCESSFUL!");
        System.out.println("League Name: " + league.getName());
        System.out.println("League Clubs: " + league.getLeagueClubs().size());
        System.out.println("Total Leagues in SaveGame: " + currentGame.getMainAuthority().getLeagues().size());
        System.out.println("========================================");

        // Mark as executed to avoid running more than once
        script.setIsExecuted(true);
        
        /**
         * Generate fixtures immediately when league is created
         * This ensures fixtures are ready right away, not waiting for daily check
         */
        java.util.List<com.rndmodgames.futtoboru.data.Match> fixtures = null;
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
            
            fixtures = fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
            
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
            System.out.println("ScriptsManager: Attempting to create messages for league: " + league.getName());
            
            if (gameInstance == null) {
                System.out.println("ERROR: gameInstance is null!");
                return;
            }
            
            if (gameInstance.getGameEngine() == null) {
                System.out.println("ERROR: gameEngine is null!");
                return;
            }
            
            com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
                gameInstance.getGameEngine().getMessageManager();
            
            if (messageManager == null) {
                System.out.println("ERROR: messageManager is null!");
                return;
            }
            
            System.out.println("ScriptsManager: MessageManager found, creating messages...");
            
            // Create league creation message
                    com.rndmodgames.futtoboru.data.Message leagueMessage = 
                        messageManager.createLeagueCreationMessage(league);
                    
                    // Schedule for current date (will be delivered immediately or next day)
                    // Use current date so it appears in inbox right away
                    java.time.LocalDateTime leagueCreationDate = currentGame.getGameDate();
                    leagueMessage.setScheduledDate(leagueCreationDate);
                    messageManager.scheduleMessage(leagueMessage);
                    
                    // Also deliver immediately to ensure it appears
                    messageManager.deliverMessage(leagueMessage);
                    
                    System.out.println("Created and delivered league creation message for " + leagueCreationDate);
                    System.out.println("Message ID: " + leagueMessage.getId() + ", Title: " + leagueMessage.getTitle());
                    
                    // Verify message was added
                    int messageCount = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
                    System.out.println("Total messages in SaveGame after delivery: " + messageCount);
                    
                    // Send welcome message to each club in the league
                    java.time.LocalDateTime seasonStart = currentGame.getGameStartDate();
                    if (seasonStart == null) {
                        seasonStart = currentGame.getGameDate();
                    }
                    java.time.LocalDateTime seasonEnd = seasonStart.plusMonths(9);
                    
                    // Only send league welcome message to player's club (if player has a club)
                    com.rndmodgames.futtoboru.data.Club playerClub = currentGame.getCurrentClub();
                    if (playerClub != null && league.getLeagueClubs() != null) {
                        // Check if player's club is in the league
                        boolean playerClubInLeague = false;
                        for (com.rndmodgames.futtoboru.data.Club leagueClub : league.getLeagueClubs()) {
                            if (leagueClub != null && leagueClub.getId() != null && 
                                playerClub.getId() != null && leagueClub.getId().equals(playerClub.getId())) {
                                playerClubInLeague = true;
                                break;
                            }
                        }
                        
                        // Only create welcome message if player's club is in the league
                        if (playerClubInLeague) {
                            com.rndmodgames.futtoboru.data.Message welcomeMessage = 
                                messageManager.createLeagueWelcomeMessage(league, playerClub, seasonStart, seasonEnd);
                            
                            if (welcomeMessage != null) {
                                // Schedule welcome message for same day as league creation
                                welcomeMessage.setScheduledDate(leagueCreationDate);
                                messageManager.scheduleMessage(welcomeMessage);
                                messageManager.deliverMessage(welcomeMessage);
                                System.out.println("Created and delivered league welcome message for player's club: " + playerClub.getName());
                                System.out.println("Welcome message ID: " + welcomeMessage.getId());
                            }
                        } else {
                            System.out.println("Player's club " + playerClub.getName() + " is not in league " + league.getName() + ", skipping welcome message");
                        }
                    } else {
                        System.out.println("Player has no club or league has no clubs, skipping league welcome messages");
                    }
                    
                    // Verify all messages were added
                    int finalMessageCount = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
                    System.out.println("Total messages after all deliveries: " + finalMessageCount);
                    
                    // Schedule fixture release message (1 day after league creation)
                    if (fixtures != null && fixtures.size() > 0) {
                        com.rndmodgames.futtoboru.data.Message fixtureMessage = 
                            messageManager.createFixtureReleaseMessage(league);
                        
                        java.time.LocalDateTime fixtureReleaseDate = currentGame.getGameDate().plusDays(1);
                        fixtureMessage.setScheduledDate(fixtureReleaseDate);
                        messageManager.scheduleMessage(fixtureMessage);
                        
                        System.out.println("Scheduled fixture release message for " + fixtureReleaseDate);
                        System.out.println("Fixture message ID: " + fixtureMessage.getId());
                    } else {
                        System.out.println("WARNING: No fixtures generated, skipping fixture release message");
                    }
        } catch (Exception e) {
            System.out.println("ERROR scheduling league messages: " + e.getMessage());
            e.printStackTrace();
            com.badlogic.gdx.Gdx.app.error("ScriptsManager", "Error creating messages", e);
        }
    }
}