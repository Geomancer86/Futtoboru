package com.rndmodgames.futtoboru.engine;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Array;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
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
                    case BasicScript.CUP_CREATION_SCRIPT:
                        // Create A Cup
                        createCup(script);
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
        
        System.out.println("Found " + clubIds.size + " club IDs in script: " + clubIds.toString());
        
        // Use a Set to detect duplicates in the script itself
        java.util.Set<Long> uniqueIdsInScript = new java.util.HashSet<>();
        for (Long id : clubIds) {
            if (!uniqueIdsInScript.add(id)) {
                System.out.println("CRITICAL WARNING: Duplicate club ID " + id + " found in script!");
            }
        }
        
        league.setLeagueClubs(new ArrayList<>());
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
                    System.out.println("ScriptsManager: Adding club " + club.getName() + " (ID: " + clubId + ") to SaveGame from DatabaseLoader");
                    currentGame.getAllClubs().add(club);
                } else {
                    String errorMsg = "CRITICAL ERROR: Club ID " + clubId + " not found in DatabaseLoader or SaveGame! The league MUST have all historical clubs.";
                    System.out.println(errorMsg);
                    com.badlogic.gdx.Gdx.app.error("ScriptsManager", errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
            
            // Final sanity check: club MUST have players
            if (club.getPlayers() == null || club.getPlayers().isEmpty()) {
                String errorMsg = "CRITICAL ERROR: Club " + club.getName() + " (ID: " + clubId + ") has NO PLAYERS loaded. Cannot generate league matches.";
                System.out.println(errorMsg);
                com.badlogic.gdx.Gdx.app.error("ScriptsManager", errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Add Club to League (now guaranteed to be in SaveGame and have players)
            if (!league.getLeagueClubs().contains(club)) {
                league.getLeagueClubs().add(club);
                clubsAdded++;
                System.out.println("ScriptsManager: Successfully added club " + club.getName() + " (ID: " + clubId + ") to league " + league.getName());
            } else {
                System.out.println("WARNING: Club " + club.getName() + " (ID: " + clubId + ") was already in the league list. Skipping duplicate.");
            }
        }
        
        System.out.println("Total unique clubs added to league: " + clubsAdded + " (Expected: " + clubIds.size + ")");
        
        if (clubsAdded != clubIds.size) {
            String errorMsg = "CRITICAL ERROR: Expected " + clubIds.size + " unique clubs in league, but only " + clubsAdded + " were added.";
            System.out.println(errorMsg);
        }
        
        if (league.getLeagueClubs().isEmpty()) {
            System.out.println("ERROR: No clubs were added to league! Cannot create empty league.");
            return;
        }
        
        // Assign default competition rules to the league
        // For historical leagues (1888), use historical rules (2-1-0, goal average)
        // For modern leagues, use modern rules (3-1-0, goal difference)
        com.rndmodgames.futtoboru.data.CompetitionRules rules;
        java.time.LocalDateTime startDate = currentGame.getGameStartDate();
        if (startDate != null && startDate.getYear() == 1888) {
            rules = com.rndmodgames.futtoboru.data.CompetitionRules.createHistoricalRules();
            System.out.println("Assigned HISTORICAL competition rules to league (1888-89)");
        } else {
            rules = com.rndmodgames.futtoboru.data.CompetitionRules.createDefaultRules();
            System.out.println("Assigned DEFAULT competition rules to league");
        }
        league.setRules(rules);
        System.out.println("Assigned competition rules to league (points: " + rules.getPointsForWin() + 
                          "-" + rules.getPointsForDraw() + "-" + rules.getPointsForLoss() + ")");
        
        // Set league ID before adding to game (needed for draw message actionData)
        // Use index in leagues list as ID, or timestamp if needed
        league.setId((long) currentGame.getMainAuthority().getLeagues().size());
        
        // Save the created League on the current game
        currentGame.getMainAuthority().getLeagues().add(league);
        
        // Ensure the league is also in the global allLeagues list for UI visibility
        if (currentGame.getAllLeagues() == null) {
            currentGame.setAllLeagues(new java.util.ArrayList<>());
        }
        
        // We need to find or create a Competition object for this League
        Competition leagueCompetition = null;
        for (Competition comp : currentGame.getAllLeagues()) {
            if (comp.getName() != null && comp.getName().equals(leagueName)) {
                leagueCompetition = comp;
                break;
            }
        }
        
        if (leagueCompetition == null) {
            leagueCompetition = new Competition();
            leagueCompetition.setId(System.currentTimeMillis()); // TODO: Better ID
            leagueCompetition.setName(leagueName);
            leagueCompetition.setCompetitionType(Competition.COMPETITION_LEAGUE);
            currentGame.getAllLeagues().add(leagueCompetition);
        }
        
        // CRITICAL: Ensure all league clubs are in SaveGame.allClubs
        // This prevents the "20 matches" bug caused by invalid/missing clubs
        if (currentGame.getAllClubs() == null) {
            currentGame.setAllClubs(new ArrayList<>());
        }
        for (Club club : league.getLeagueClubs()) {
            if (currentGame.getClubById(club.getId()) == null) {
                System.out.println("ScriptsManager: Adding missing league club " + club.getName() + " (ID: " + club.getId() + ") to SaveGame");
                currentGame.getAllClubs().add(club);
            }
        }
        
        // Create the first edition for this league
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = new com.rndmodgames.futtoboru.data.CompetitionEdition();
        edition.setId(System.currentTimeMillis()); // TODO: Better ID generation
        String seasonName = (startDate != null) ? startDate.getYear() + "-" + ((startDate.getYear() + 1) % 100) : "Season 1";
        edition.setName(leagueName + " " + seasonName);
        edition.setStartDate(startDate);
        edition.setEndDate(edition.getStartDate() != null ? edition.getStartDate().plusMonths(9) : null);
        
        if (clubIds != null) {
            for (Long clubId : clubIds) {
                edition.getParticipantClubsIds().add(clubId);
            }
            edition.setParticipantClubs(clubIds.size);
        }
        
        league.getEditions().add(edition);
        
        // Also add edition to the competition object
        if (leagueCompetition != null) {
            if (leagueCompetition.getEditions() == null) {
                leagueCompetition.setEditions(new java.util.ArrayList<>());
            }
            leagueCompetition.getEditions().add(edition);
        }
        
        System.out.println("Created first edition for league: " + edition.getName());
        
        System.out.println("========================================");
        System.out.println("LEAGUE CREATION SUCCESSFUL!");
        System.out.println("League Name: " + league.getName());
        System.out.println("League Clubs: " + league.getLeagueClubs().size());
        System.out.println("Total Leagues in SaveGame: " + currentGame.getMainAuthority().getLeagues().size());
        System.out.println("========================================");

        // Mark as executed to avoid running more than once
        script.setIsExecuted(true);
        
        /**
         * STEP 2: Generate fixtures immediately when league is created
         * This ensures fixtures are ready right away, not waiting for daily check
         */
        System.out.println("========================================");
        System.out.println("STEP 2: GENERATING FIXTURES");
        System.out.println("========================================");
        
        java.util.List<com.rndmodgames.futtoboru.data.Match> fixtures = null;
        try {
            com.badlogic.gdx.Gdx.app.log("ScriptsManager", "Generating fixtures for newly created league: " + league.getName());
            System.out.println("ScriptsManager: Generating fixtures for league: " + league.getName());
            
            // Get season dates (use game start date or current date)
            java.time.LocalDateTime seasonStart = currentGame.getGameStartDate();
            if (seasonStart == null) {
                seasonStart = currentGame.getGameDate();
                System.out.println("WARNING: gameStartDate is null, using current date: " + seasonStart);
            }
            System.out.println("Season start date: " + seasonStart);
            
            // Season typically runs September to May (9 months)
            // Extending to 10 months to ensure all 22 matches fit before awards in June
            java.time.LocalDateTime seasonEnd = seasonStart.plusMonths(10);
            System.out.println("Season end date: " + seasonEnd);
            
            // Generate fixtures
            System.out.println("Creating LeagueFixtureGenerator...");
            com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator fixtureGenerator = 
                new com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator(gameInstance);
            
            System.out.println("Calling generateLeagueFixtures()...");
            fixtures = fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
            
            System.out.println("========================================");
            System.out.println("FIXTURE GENERATION RESULT:");
            System.out.println("Generated " + (fixtures != null ? fixtures.size() : 0) + " fixtures for league " + league.getName());
            System.out.println("========================================");
            
            if (fixtures == null) {
                System.out.println("ERROR: fixtures list is NULL!");
            } else if (fixtures.isEmpty()) {
                System.out.println("ERROR: fixtures list is EMPTY! No matches were generated.");
            } else {
                System.out.println("SUCCESS: " + fixtures.size() + " fixtures generated");
                // Verify matches were added to clubs
                int totalMatchesInClubs = 0;
                for (com.rndmodgames.futtoboru.data.Club club : league.getLeagueClubs()) {
                    if (club != null && club.getScheduledMatches() != null) {
                        totalMatchesInClubs += club.getScheduledMatches().size();
                    }
                }
                System.out.println("Total match references in clubs' scheduledMatches: " + totalMatchesInClubs + " (expected: " + (fixtures.size() * 2) + ")");
            }
            
            com.badlogic.gdx.Gdx.app.log("ScriptsManager", "Successfully generated " + fixtures.size() + " fixtures for league " + league.getName());
            
        } catch (Exception e) {
            System.out.println("========================================");
            System.out.println("ERROR GENERATING FIXTURES!");
            System.out.println("Exception: " + e.getMessage());
            System.out.println("========================================");
            e.printStackTrace();
            com.badlogic.gdx.Gdx.app.error("ScriptsManager", "Failed to generate fixtures for league " + league.getName(), e);
        }
        
        /**
         * STEP 3: Schedule league creation announcement message and draw message
         */
        System.out.println("========================================");
        System.out.println("STEP 3: CREATING MESSAGES");
        System.out.println("========================================");
        
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
                    
                    // STEP 3.1: Create mandatory draw message (1 day after league creation)
                    // This message blocks time advancement until the draw is viewed
                    System.out.println("STEP 3.1: Creating draw message...");
                    System.out.println("Fixtures status: " + (fixtures != null ? "NOT NULL" : "NULL") + 
                                     ", Size: " + (fixtures != null ? fixtures.size() : 0));
                    
                    if (fixtures != null && fixtures.size() > 0) {
                        System.out.println("Calling createLeagueDrawMessage()...");
                        com.rndmodgames.futtoboru.data.Message drawMessage = 
                            messageManager.createLeagueDrawMessage(league);
                        
                        if (drawMessage != null) {
                            System.out.println("Draw message created successfully!");
                            // CRITICAL: Schedule draw message for CURRENT date, not tomorrow
                            // Fixtures are ready NOW, so the draw message should be available immediately
                            // This ensures the message appears in inbox and the button shows correctly
                            java.time.LocalDateTime drawDate = currentGame.getGameDate();
                            System.out.println("Setting scheduled date to: " + drawDate);
                            drawMessage.setScheduledDate(drawDate);
                            
                            System.out.println("Scheduling message...");
                            messageManager.scheduleMessage(drawMessage);
                            
                            // CRITICAL: Deliver the draw message immediately since fixtures are ready
                            // This ensures the message appears in the inbox right away
                            messageManager.deliverMessage(drawMessage);
                            
                            // Verify message was scheduled
                            int scheduledCount = (currentGame.getScheduledMessages() != null) ? currentGame.getScheduledMessages().size() : 0;
                            System.out.println("Total scheduled messages after draw message: " + scheduledCount);
                            
                            System.out.println("========================================");
                            System.out.println("MANDATORY DRAW MESSAGE CREATED!");
                            System.out.println("Draw message scheduled for: " + drawDate);
                            System.out.println("Draw message ID: " + drawMessage.getId());
                            System.out.println("Is Mandatory: " + drawMessage.getIsMandatory());
                            System.out.println("Message Type: " + drawMessage.getMessageType());
                            System.out.println("Action Screen: " + drawMessage.getActionScreen());
                            System.out.println("Action Data (League ID): " + drawMessage.getActionData());
                            System.out.println("========================================");
                            
                            // Also create a non-mandatory fixture release message for reference
                            com.rndmodgames.futtoboru.data.Message fixtureMessage = 
                                messageManager.createFixtureReleaseMessage(league);
                            if (fixtureMessage != null) {
                                fixtureMessage.setScheduledDate(drawDate);
                                messageManager.scheduleMessage(fixtureMessage);
                                System.out.println("Scheduled fixture release message for " + drawDate);
                            }
                        } else {
                            System.out.println("========================================");
                            System.out.println("ERROR: createLeagueDrawMessage returned null!");
                            System.out.println("========================================");
                        }
                    } else {
                        System.out.println("========================================");
                        System.out.println("WARNING: No fixtures generated, skipping draw message");
                        System.out.println("Fixtures is null: " + (fixtures == null));
                        if (fixtures != null) {
                            System.out.println("Fixtures size: " + fixtures.size());
                        }
                        System.out.println("========================================");
                    }
        } catch (Exception e) {
            System.out.println("ERROR scheduling league messages: " + e.getMessage());
            e.printStackTrace();
            com.badlogic.gdx.Gdx.app.error("ScriptsManager", "Error creating messages", e);
        }
    }

    /**
     * Create A Cup based on a script
     */
    @SuppressWarnings("unchecked")
    public void createCup(BasicScript script) {
        System.out.println("========================================");
        System.out.println("EXECUTING CUP CREATION SCRIPT!");
        System.out.println("========================================");
        
        if (currentGame == null) {
            System.out.println("ERROR: currentGame is null! Cannot create cup.");
            return;
        }
        
        // Get cup name
        String cupName = (String) script.getScriptValues().get(ScriptsLoader.CUP_NAME);
        if (cupName == null) {
            System.out.println("ERROR: Cup name is null in script!");
            return;
        }
        
        // Find or create Competition object
        com.rndmodgames.futtoboru.data.Competition cup = null;
        if (currentGame.getAllCups() == null) {
            currentGame.setAllCups(new java.util.ArrayList<>());
        }
        
        for (com.rndmodgames.futtoboru.data.Competition existingCup : currentGame.getAllCups()) {
            if (existingCup.getName() != null && existingCup.getName().equals(cupName)) {
                cup = existingCup;
                break;
            }
        }
        
        if (cup == null) {
            cup = new com.rndmodgames.futtoboru.data.Competition();
            cup.setId(System.currentTimeMillis());
            cup.setName(cupName);
            cup.setCompetitionType(com.rndmodgames.futtoboru.data.Competition.COMPETITION_CUP);
            currentGame.getAllCups().add(cup);
            System.out.println("Created new competition object for: " + cupName);
        }
        
        // Get participants
        Array<Long> participantIds = (Array<Long>) script.getScriptValues().get(ScriptsLoader.CUP_PARTICIPANTS);
        if (participantIds == null || participantIds.size == 0) {
            System.out.println("ERROR: No participants found for cup script!");
            return;
        }
        
        // Create Edition
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = new com.rndmodgames.futtoboru.data.CompetitionEdition();
        edition.setId(System.currentTimeMillis() + 1);
        java.time.LocalDateTime gameDate = currentGame.getGameDate();
        String seasonName = gameDate.getYear() + "-" + ((gameDate.getYear() + 1) % 100);
        edition.setName(cupName + " " + seasonName);
        edition.setStartDate(gameDate);
        edition.setEndDate(gameDate.plusMonths(9));
        
        if (currentGame.getAllClubs() == null) {
            currentGame.setAllClubs(new java.util.ArrayList<>());
        }
        
        for (int i = 0; i < participantIds.size; i++) {
            Long id = participantIds.get(i);
            edition.getParticipantClubsIds().add(id);
            // Ensure club is in SaveGame
            if (currentGame.getClubById(id) == null) {
                Club club = DatabaseLoader.getClubById(id);
                if (club != null) {
                    System.out.println("ScriptsManager: Adding missing cup club " + club.getName() + " (ID: " + id + ") to SaveGame");
                    currentGame.getAllClubs().add(club);
                }
            }
        }
        edition.setParticipantClubs(participantIds.size);
        cup.getEditions().add(edition);
        
        System.out.println("Created cup edition: " + edition.getName() + " with " + participantIds.size + " teams");
        
        // Deliver Message (Informing User)
        if (gameInstance != null && gameInstance.getGameEngine() != null) {
            com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
                gameInstance.getGameEngine().getMessageManager();
            
            if (messageManager != null) {
                // Creation Announcement
                com.rndmodgames.futtoboru.data.Message creationMsg = new com.rndmodgames.futtoboru.data.Message();
                creationMsg.setCategory(com.rndmodgames.futtoboru.data.MessageCategory.CUP);
                creationMsg.setPriority(com.rndmodgames.futtoboru.data.MessagePriority.URGENT);
                creationMsg.setTitle(cupName + " Created");
                creationMsg.setPlainTextMessage("The " + edition.getName() + " has been established. All participant clubs have been registered, and the first round draw is ready to be conducted.");
                creationMsg.setScheduledDate(gameDate);
                creationMsg.setIsMandatory(false);
                messageManager.deliverMessage(creationMsg);
                
                // Mandatory Draw Message (blocks time advancement)
                int roundNumber = 1;
                com.rndmodgames.futtoboru.data.Message drawMessage = messageManager.createCupDrawMessage(cup, edition, roundNumber, "First Round");
                if (drawMessage != null) {
                    drawMessage.setScheduledDate(gameDate); 
                    messageManager.deliverMessage(drawMessage);
                    System.out.println("Scheduled cup draw message for " + cupName);
                }
            }
        }
        
        script.setIsExecuted(true);
        System.out.println("CUP CREATION SUCCESSFUL!");
        System.out.println("========================================");
    }
}