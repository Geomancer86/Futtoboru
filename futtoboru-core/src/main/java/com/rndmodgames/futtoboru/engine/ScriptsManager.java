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
import com.rndmodgames.futtoboru.system.Log;

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
        
        Log.info(Log.ENGINE_SCRIPTS, "CHECKING GAME SCRIPTS! " + gameInstance.getCurrentGame().getGameScripts().size());
        
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
                    
                    Log.info(Log.ENGINE_SCRIPTS, "EXECUTING BASIC SCRIPT: " + script.getName());
                    
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
                        Log.info(Log.ENGINE_SCRIPTS, "SCRIPT TYPE NOT IMPLEMENTED, IGNORING!");
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
        
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        Log.info(Log.ENGINE_SCRIPTS, "EXECUTING LEAGUE CREATION SCRIPT!");
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        
        // Verify SaveGame is available
        if (currentGame == null) {
            Log.error(Log.ENGINE_SCRIPTS, "currentGame is null! Cannot create league.");
            Log.error(Log.ENGINE_SCRIPTS, "ScriptsManager", "currentGame is null in createLeague()");
            return;
        }
        
        // Verify mainAuthority exists
        if (currentGame.getMainAuthority() == null) {
            Log.error(Log.ENGINE_SCRIPTS, "mainAuthority is null! Cannot add league.");
            Log.error(Log.ENGINE_SCRIPTS, "ScriptsManager", "mainAuthority is null in createLeague()");
            return;
        }
        
        // Initialize leagues list if null
        if (currentGame.getMainAuthority().getLeagues() == null) {
            Log.warn(Log.ENGINE_SCRIPTS, "mainAuthority.getLeagues() is null, initializing...");
            currentGame.getMainAuthority().setLeagues(new java.util.ArrayList<>());
        }
        
        Log.info(Log.ENGINE_SCRIPTS, "Current leagues count before creation: " + currentGame.getMainAuthority().getLeagues().size());
        
        //
        League league = new League();
        
        // Get league name
        String leagueName = (String) script.getScriptValues().get(ScriptsLoader.LEAGUE_NAME);
        if (leagueName == null) {
            Log.error(Log.ENGINE_SCRIPTS, "League name is null in script!");
            return;
        }
        league.setName(leagueName);
        Log.info(Log.ENGINE_SCRIPTS, "Creating league: " + leagueName);
        
        // Get league country
        Long countryId = (Long) script.getScriptValues().get(ScriptsLoader.LEAGUE_COUNTRY);
        if (countryId == null) {
            Log.error(Log.ENGINE_SCRIPTS, "League country ID is null in script!");
            return;
        }
        league.setCountry(DatabaseLoader.getCountryById(countryId));
        if (league.getCountry() == null) {
            Log.error(Log.ENGINE_SCRIPTS, "Country with ID " + countryId + " not found!");
            return;
        }
        Log.info(Log.ENGINE_SCRIPTS, "League country: " + league.getCountry().getCommonName());
        
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
            Log.error(Log.ENGINE_SCRIPTS, "No club IDs found in script!");
            return;
        }
        
        Log.info(Log.ENGINE_SCRIPTS, "Found " + clubIds.size + " club IDs in script: " + clubIds.toString());
        
        // Use a Set to detect duplicates in the script itself
        java.util.Set<Long> uniqueIdsInScript = new java.util.HashSet<>();
        for (Long id : clubIds) {
            if (!uniqueIdsInScript.add(id)) {
                Log.info(Log.ENGINE_SCRIPTS, "CRITICAL WARNING: Duplicate club ID " + id + " found in script!");
            }
        }
        
        league.setLeagueClubs(new ArrayList<>());
        int clubsAdded = 0;
        for (Long clubId : clubIds) {
            if (clubId == null) {
                Log.warn(Log.ENGINE_SCRIPTS, "Null club ID in list, skipping");
                continue;
            }
            
            // Try to get club from SaveGame first (preferred)
            Club club = currentGame.getClubById(clubId);
            
            if (club == null) {
                // Fallback to DatabaseLoader
                club = DatabaseLoader.getClubById(clubId);
                
                if (club != null) {
                    // Add to SaveGame so fixture generator can find it
                    Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: Adding club " + club.getName() + " (ID: " + clubId + ") to SaveGame from DatabaseLoader");
                    currentGame.getAllClubs().add(club);
                } else {
                    String errorMsg = "CRITICAL ERROR: Club ID " + clubId + " not found in DatabaseLoader or SaveGame! The league MUST have all historical clubs.";
                    Log.info(Log.ENGINE_SCRIPTS, errorMsg);
                    Log.error(Log.ENGINE_SCRIPTS, "ScriptsManager", errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            }
            
            // Final sanity check: club MUST have players
            if (club.getPlayers() == null || club.getPlayers().isEmpty()) {
                String errorMsg = "CRITICAL ERROR: Club " + club.getName() + " (ID: " + clubId + ") has NO PLAYERS loaded. Cannot generate league matches.";
                Log.info(Log.ENGINE_SCRIPTS, errorMsg);
                Log.error(Log.ENGINE_SCRIPTS, "ScriptsManager", errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            // Add Club to League (now guaranteed to be in SaveGame and have players)
            if (!league.getLeagueClubs().contains(club)) {
                league.getLeagueClubs().add(club);
                clubsAdded++;
                Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: Successfully added club " + club.getName() + " (ID: " + clubId + ") to league " + league.getName());
            } else {
                Log.warn(Log.ENGINE_SCRIPTS, "Club " + club.getName() + " (ID: " + clubId + ") was already in the league list. Skipping duplicate.");
            }
        }
        
        Log.info(Log.ENGINE_SCRIPTS, "Total unique clubs added to league: " + clubsAdded + " (Expected: " + clubIds.size + ")");
        
        if (clubsAdded != clubIds.size) {
            String errorMsg = "CRITICAL ERROR: Expected " + clubIds.size + " unique clubs in league, but only " + clubsAdded + " were added.";
            Log.info(Log.ENGINE_SCRIPTS, errorMsg);
        }
        
        if (league.getLeagueClubs().isEmpty()) {
            Log.error(Log.ENGINE_SCRIPTS, "No clubs were added to league! Cannot create empty league.");
            return;
        }
        
        // Assign default competition rules to the league
        // For historical leagues (1888), use historical rules (2-1-0, goal average)
        // For modern leagues, use modern rules (3-1-0, goal difference)
        com.rndmodgames.futtoboru.data.CompetitionRules rules;
        java.time.LocalDateTime startDate = currentGame.getGameStartDate();
        if (startDate != null && startDate.getYear() == 1888) {
            rules = com.rndmodgames.futtoboru.data.CompetitionRules.createHistoricalRules();
            Log.info(Log.ENGINE_SCRIPTS, "Assigned HISTORICAL competition rules to league (1888-89)");
        } else {
            rules = com.rndmodgames.futtoboru.data.CompetitionRules.createDefaultRules();
            Log.info(Log.ENGINE_SCRIPTS, "Assigned DEFAULT competition rules to league");
        }
        league.setRules(rules);
        Log.info(Log.ENGINE_SCRIPTS, "Assigned competition rules to league (points: " + rules.getPointsForWin() + 
                          "-" + rules.getPointsForDraw() + "-" + rules.getPointsForLoss() + ")");
        
        // Set league ID before adding to game (needed for draw message actionData)
        // Use index in leagues list as ID, or timestamp if needed
        league.setId(currentGame.getIdAllocator().nextId());
        
        // Save the created League on the current game
        currentGame.getMainAuthority().getLeagues().add(league);
        
        // Ensure the league is also in the global allLeagues list for UI visibility
        if (currentGame.getAllLeagues() == null) {
            currentGame.setAllLeagues(new java.util.ArrayList<>());
        }
        
        // We need to find or create a Competition object for this League (by the data's competition id first)
        Competition leagueCompetition = findCompetition(currentGame.getAllLeagues(),
                (Long) script.getScriptValues().get(ScriptsLoader.LEAGUE_COMPETITION), leagueName);
        
        if (leagueCompetition == null) {
            leagueCompetition = new Competition();
            leagueCompetition.setId(currentGame.getIdAllocator().nextId());
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
                Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: Adding missing league club " + club.getName() + " (ID: " + club.getId() + ") to SaveGame");
                currentGame.getAllClubs().add(club);
            }
        }
        
        // The season's edition: the one from the game data (season.json) if there is one (P2-9: a second,
        // script-made edition was never drawn or finished), else a new one
        String seasonName = (startDate != null) ? com.rndmodgames.futtoboru.data.Season.label(startDate.getYear()) : "Season 1";
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = findSeasonEdition(leagueCompetition,
                (Long) script.getScriptValues().get(ScriptsLoader.LEAGUE_EDITION), startDate);
        boolean fromData = edition != null;
        if (!fromData) {
            edition = new com.rndmodgames.futtoboru.data.CompetitionEdition();
            edition.setId(currentGame.getIdAllocator().nextId());
            edition.setName(leagueName + " " + seasonName);
        }
        edition.setStartDate(startDate);
        // P2-11: the season runs to the league's last match day of the era calendar (mid-April)
        edition.setEndDate(edition.getStartDate() != null
                ? com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(edition.getStartDate()).leagueEnd() : null);

        setParticipants(edition, clubIds);

        if (!league.getEditions().contains(edition)) {
            league.getEditions().add(edition);
        }

        // Also add edition to the competition object (a data edition is already there)
        if (leagueCompetition.getEditions() == null) {
            leagueCompetition.setEditions(new java.util.ArrayList<>());
        }
        if (!leagueCompetition.getEditions().contains(edition)) {
            leagueCompetition.getEditions().add(edition);
        }

        Log.info(Log.ENGINE_SCRIPTS, (fromData ? "Opened the data edition of league: " : "Created first edition for league: ")
                + edition.getName());
        
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        Log.info(Log.ENGINE_SCRIPTS, "LEAGUE CREATION SUCCESSFUL!");
        Log.info(Log.ENGINE_SCRIPTS, "League Name: " + league.getName());
        Log.info(Log.ENGINE_SCRIPTS, "League Clubs: " + league.getLeagueClubs().size());
        Log.info(Log.ENGINE_SCRIPTS, "Total Leagues in SaveGame: " + currentGame.getMainAuthority().getLeagues().size());
        Log.info(Log.ENGINE_SCRIPTS, "========================================");

        // Mark as executed to avoid running more than once
        script.setIsExecuted(true);
        
        /**
         * STEP 2: Generate fixtures immediately when league is created
         * This ensures fixtures are ready right away, not waiting for daily check
         */
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        Log.info(Log.ENGINE_SCRIPTS, "STEP 2: GENERATING FIXTURES");
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        
        java.util.List<com.rndmodgames.futtoboru.data.Match> fixtures = null;
        try {
            Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager", "Generating fixtures for newly created league: " + league.getName());
            Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: Generating fixtures for league: " + league.getName());
            
            // Get season dates (use game start date or current date)
            java.time.LocalDateTime seasonStart = currentGame.getGameStartDate();
            if (seasonStart == null) {
                seasonStart = currentGame.getGameDate();
                Log.warn(Log.ENGINE_SCRIPTS, "gameStartDate is null, using current date: " + seasonStart);
            }
            Log.info(Log.ENGINE_SCRIPTS, "Season start date: " + seasonStart);
            
            // P2-11: the fixtures follow the era calendar (Saturdays, September to mid-April); see SeasonCalendar
            java.time.LocalDateTime seasonEnd = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar
                    .forDate(seasonStart).leagueEnd();
            Log.info(Log.ENGINE_SCRIPTS, "Season end date: " + seasonEnd);
            
            // Generate fixtures
            Log.info(Log.ENGINE_SCRIPTS, "Creating LeagueFixtureGenerator...");
            com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator fixtureGenerator = 
                new com.rndmodgames.futtoboru.engine.temporal.LeagueFixtureGenerator(gameInstance);
            
            Log.info(Log.ENGINE_SCRIPTS, "Calling generateLeagueFixtures()...");
            fixtures = fixtureGenerator.generateLeagueFixtures(league, seasonStart, seasonEnd);
            
            Log.info(Log.ENGINE_SCRIPTS, "========================================");
            Log.info(Log.ENGINE_SCRIPTS, "FIXTURE GENERATION RESULT:");
            Log.info(Log.ENGINE_SCRIPTS, "Generated " + (fixtures != null ? fixtures.size() : 0) + " fixtures for league " + league.getName());
            Log.info(Log.ENGINE_SCRIPTS, "========================================");
            
            if (fixtures == null) {
                Log.error(Log.ENGINE_SCRIPTS, "fixtures list is NULL!");
            } else if (fixtures.isEmpty()) {
                Log.error(Log.ENGINE_SCRIPTS, "fixtures list is EMPTY! No matches were generated.");
            } else {
                Log.info(Log.ENGINE_SCRIPTS, "SUCCESS: " + fixtures.size() + " fixtures generated");
                // Verify matches were added to clubs
                int totalMatchesInClubs = 0;
                for (com.rndmodgames.futtoboru.data.Club club : league.getLeagueClubs()) {
                    if (club != null && club.getScheduledMatches() != null) {
                        totalMatchesInClubs += club.getScheduledMatches().size();
                    }
                }
                Log.info(Log.ENGINE_SCRIPTS, "Total match references in clubs' scheduledMatches: " + totalMatchesInClubs + " (expected: " + (fixtures.size() * 2) + ")");
            }
            
            Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager", "Successfully generated " + fixtures.size() + " fixtures for league " + league.getName());
            
        } catch (Exception e) {
            Log.info(Log.ENGINE_SCRIPTS, "========================================");
            Log.info(Log.ENGINE_SCRIPTS, "ERROR GENERATING FIXTURES!");
            Log.info(Log.ENGINE_SCRIPTS, "Exception: " + e.getMessage());
            Log.info(Log.ENGINE_SCRIPTS, "========================================");
            Log.error(Log.ENGINE_SCRIPTS, e);
            Log.error(Log.ENGINE_SCRIPTS, "ScriptsManager", "Failed to generate fixtures for league " + league.getName(), e);
        }
        
        /**
         * STEP 3: Schedule league creation announcement message and draw message
         */
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        Log.info(Log.ENGINE_SCRIPTS, "STEP 3: CREATING MESSAGES");
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        
        try {
            Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: Attempting to create messages for league: " + league.getName());
            
            if (gameInstance == null) {
                Log.error(Log.ENGINE_SCRIPTS, "gameInstance is null!");
                return;
            }
            
            if (gameInstance.getGameEngine() == null) {
                Log.error(Log.ENGINE_SCRIPTS, "gameEngine is null!");
                return;
            }
            
            com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
                gameInstance.getGameEngine().getMessageManager();
            
            if (messageManager == null) {
                Log.error(Log.ENGINE_SCRIPTS, "messageManager is null!");
                return;
            }
            
            Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: MessageManager found, creating messages...");
            
            // P2-13: the day of the first league matches, from the fixtures just generated (era calendar)
            java.time.LocalDateTime firstLeagueDay = null;
            if (fixtures != null) {
                for (com.rndmodgames.futtoboru.data.Match fixture : fixtures) {
                    if (fixture != null && fixture.getMatchDateTime() != null
                            && (firstLeagueDay == null || fixture.getMatchDateTime().isBefore(firstLeagueDay))) {
                        firstLeagueDay = fixture.getMatchDateTime();
                    }
                }
            }

            // Create league creation message
                    com.rndmodgames.futtoboru.data.Message leagueMessage =
                        messageManager.createLeagueCreationMessage(league, firstLeagueDay);
                    
                    // Schedule for current date (will be delivered immediately or next day)
                    // Use current date so it appears in inbox right away
                    java.time.LocalDateTime leagueCreationDate = currentGame.getGameDate();
                    leagueMessage.setScheduledDate(leagueCreationDate);
                    messageManager.scheduleMessage(leagueMessage);
                    
                    // Also deliver immediately to ensure it appears
                    messageManager.deliverMessage(leagueMessage);
                    
                    Log.info(Log.ENGINE_SCRIPTS, "Created and delivered league creation message for " + leagueCreationDate);
                    Log.info(Log.ENGINE_SCRIPTS, "Message ID: " + leagueMessage.getId() + ", Title: " + leagueMessage.getTitle());
                    
                    // Verify message was added
                    int messageCount = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
                    Log.info(Log.ENGINE_SCRIPTS, "Total messages in SaveGame after delivery: " + messageCount);
                    
                    // Send welcome message to each club in the league
                    java.time.LocalDateTime seasonStart = currentGame.getGameStartDate();
                    if (seasonStart == null) {
                        seasonStart = currentGame.getGameDate();
                    }
                    // P2-11: the dates the fixtures are played between (era calendar)
                    com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar calendar =
                            com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(seasonStart);
                    if (seasonStart.toLocalDate().isBefore(calendar.leagueOpening())) {
                        seasonStart = calendar.leagueOpening().atTime(com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.KICK_OFF);
                    }
                    java.time.LocalDateTime seasonEnd = calendar.leagueClosing()
                            .atTime(com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.KICK_OFF);
                    
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
                                Log.info(Log.ENGINE_SCRIPTS, "Created and delivered league welcome message for player's club: " + playerClub.getName());
                                Log.info(Log.ENGINE_SCRIPTS, "Welcome message ID: " + welcomeMessage.getId());
                            }
                        } else {
                            Log.info(Log.ENGINE_SCRIPTS, "Player's club " + playerClub.getName() + " is not in league " + league.getName() + ", skipping welcome message");
                        }
                    } else {
                        Log.info(Log.ENGINE_SCRIPTS, "Player has no club or league has no clubs, skipping league welcome messages");
                    }
                    
                    // Verify all messages were added
                    int finalMessageCount = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
                    Log.info(Log.ENGINE_SCRIPTS, "Total messages after all deliveries: " + finalMessageCount);
                    
                    // STEP 3.1: Create mandatory draw message (1 day after league creation)
                    // This message blocks time advancement until the draw is viewed
                    Log.info(Log.ENGINE_SCRIPTS, "STEP 3.1: Creating draw message...");
                    Log.info(Log.ENGINE_SCRIPTS, "Fixtures status: " + (fixtures != null ? "NOT NULL" : "NULL") + 
                                     ", Size: " + (fixtures != null ? fixtures.size() : 0));
                    
                    if (fixtures != null && fixtures.size() > 0) {
                        Log.info(Log.ENGINE_SCRIPTS, "Calling createLeagueDrawMessage()...");
                        com.rndmodgames.futtoboru.data.Message drawMessage = 
                            messageManager.createLeagueDrawMessage(league);
                        
                        if (drawMessage != null) {
                            Log.info(Log.ENGINE_SCRIPTS, "Draw message created successfully!");
                            // CRITICAL: Schedule draw message for CURRENT date, not tomorrow
                            // Fixtures are ready NOW, so the draw message should be available immediately
                            // This ensures the message appears in inbox and the button shows correctly
                            java.time.LocalDateTime drawDate = currentGame.getGameDate();
                            Log.info(Log.ENGINE_SCRIPTS, "Setting scheduled date to: " + drawDate);
                            drawMessage.setScheduledDate(drawDate);
                            
                            Log.info(Log.ENGINE_SCRIPTS, "Scheduling message...");
                            messageManager.scheduleMessage(drawMessage);
                            
                            // CRITICAL: Deliver the draw message immediately since fixtures are ready
                            // This ensures the message appears in the inbox right away
                            messageManager.deliverMessage(drawMessage);
                            
                            // Verify message was scheduled
                            int scheduledCount = (currentGame.getScheduledMessages() != null) ? currentGame.getScheduledMessages().size() : 0;
                            Log.info(Log.ENGINE_SCRIPTS, "Total scheduled messages after draw message: " + scheduledCount);
                            
                            Log.info(Log.ENGINE_SCRIPTS, "========================================");
                            Log.info(Log.ENGINE_SCRIPTS, "MANDATORY DRAW MESSAGE CREATED!");
                            Log.info(Log.ENGINE_SCRIPTS, "Draw message scheduled for: " + drawDate);
                            Log.info(Log.ENGINE_SCRIPTS, "Draw message ID: " + drawMessage.getId());
                            Log.info(Log.ENGINE_SCRIPTS, "Is Mandatory: " + drawMessage.getIsMandatory());
                            Log.info(Log.ENGINE_SCRIPTS, "Message Type: " + drawMessage.getMessageType());
                            Log.info(Log.ENGINE_SCRIPTS, "Action Screen: " + drawMessage.getActionScreen());
                            Log.info(Log.ENGINE_SCRIPTS, "Action Data (League ID): " + drawMessage.getActionData());
                            Log.info(Log.ENGINE_SCRIPTS, "========================================");
                            
                            // Also create a non-mandatory fixture release message for reference
                            com.rndmodgames.futtoboru.data.Message fixtureMessage = 
                                messageManager.createFixtureReleaseMessage(league, firstLeagueDay);
                            if (fixtureMessage != null) {
                                fixtureMessage.setScheduledDate(drawDate);
                                messageManager.scheduleMessage(fixtureMessage);
                                Log.info(Log.ENGINE_SCRIPTS, "Scheduled fixture release message for " + drawDate);
                            }
                        } else {
                            Log.info(Log.ENGINE_SCRIPTS, "========================================");
                            Log.error(Log.ENGINE_SCRIPTS, "createLeagueDrawMessage returned null!");
                            Log.info(Log.ENGINE_SCRIPTS, "========================================");
                        }
                    } else {
                        Log.info(Log.ENGINE_SCRIPTS, "========================================");
                        Log.warn(Log.ENGINE_SCRIPTS, "No fixtures generated, skipping draw message");
                        Log.info(Log.ENGINE_SCRIPTS, "Fixtures is null: " + (fixtures == null));
                        if (fixtures != null) {
                            Log.info(Log.ENGINE_SCRIPTS, "Fixtures size: " + fixtures.size());
                        }
                        Log.info(Log.ENGINE_SCRIPTS, "========================================");
                    }
        } catch (Exception e) {
            Log.info(Log.ENGINE_SCRIPTS, "ERROR scheduling league messages: " + e.getMessage());
            Log.error(Log.ENGINE_SCRIPTS, e);
            Log.error(Log.ENGINE_SCRIPTS, "ScriptsManager", "Error creating messages", e);
        }
    }

    /**
     * Create A Cup based on a script
     */
    @SuppressWarnings("unchecked")
    public void createCup(BasicScript script) {
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        Log.info(Log.ENGINE_SCRIPTS, "EXECUTING CUP CREATION SCRIPT!");
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
        
        if (currentGame == null) {
            Log.error(Log.ENGINE_SCRIPTS, "currentGame is null! Cannot create cup.");
            return;
        }
        
        // Get cup name
        String cupName = (String) script.getScriptValues().get(ScriptsLoader.CUP_NAME);
        if (cupName == null) {
            Log.error(Log.ENGINE_SCRIPTS, "Cup name is null in script!");
            return;
        }
        
        // Find or create Competition object
        com.rndmodgames.futtoboru.data.Competition cup = null;
        if (currentGame.getAllCups() == null) {
            currentGame.setAllCups(new java.util.ArrayList<>());
        }
        
        cup = findCompetition(currentGame.getAllCups(),
                (Long) script.getScriptValues().get(ScriptsLoader.CUP_COMPETITION), cupName);
        
        if (cup == null) {
            cup = new com.rndmodgames.futtoboru.data.Competition();
            cup.setId(currentGame.getIdAllocator().nextId());
            cup.setName(cupName);
            cup.setCompetitionType(com.rndmodgames.futtoboru.data.Competition.COMPETITION_CUP);
            currentGame.getAllCups().add(cup);
            Log.info(Log.ENGINE_SCRIPTS, "Created new competition object for: " + cupName);
        }
        
        // Get participants
        Array<Long> participantIds = (Array<Long>) script.getScriptValues().get(ScriptsLoader.CUP_PARTICIPANTS);
        if (participantIds == null || participantIds.size == 0) {
            Log.error(Log.ENGINE_SCRIPTS, "No participants found for cup script!");
            return;
        }
        
        // The season's edition: the one from the game data (season.json) if there is one (P2-9: a second,
        // script-made edition was never drawn or finished), else a new one
        java.time.LocalDateTime gameDate = currentGame.getGameDate();
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = findSeasonEdition(cup,
                (Long) script.getScriptValues().get(ScriptsLoader.CUP_EDITION), gameDate);
        boolean fromData = edition != null;
        if (!fromData) {
            edition = new com.rndmodgames.futtoboru.data.CompetitionEdition();
            edition.setId(currentGame.getIdAllocator().nextId());
            String seasonName = com.rndmodgames.futtoboru.data.Season.label(gameDate.getYear());
            edition.setName(cupName + " " + seasonName);
        }
        edition.setStartDate(gameDate);
        edition.setEndDate(com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(gameDate).cupEnd()); // P2-11

        if (currentGame.getAllClubs() == null) {
            currentGame.setAllClubs(new java.util.ArrayList<>());
        }

        for (int i = 0; i < participantIds.size; i++) {
            Long id = participantIds.get(i);
            // Ensure club is in SaveGame
            if (currentGame.getClubById(id) == null) {
                Club club = DatabaseLoader.getClubById(id);
                if (club != null) {
                    Log.info(Log.ENGINE_SCRIPTS, "ScriptsManager: Adding missing cup club " + club.getName() + " (ID: " + id + ") to SaveGame");
                    currentGame.getAllClubs().add(club);
                }
            }
        }
        setParticipants(edition, participantIds);
        if (!cup.getEditions().contains(edition)) {
            cup.getEditions().add(edition);
        }

        Log.info(Log.ENGINE_SCRIPTS, (fromData ? "Opened the data cup edition: " : "Created cup edition: ")
                + edition.getName() + " with " + participantIds.size + " teams");

        // Deliver Message (Informing User)
        if (gameInstance != null && gameInstance.getGameEngine() != null) {
            com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
                gameInstance.getGameEngine().getMessageManager();
            
            if (messageManager != null) {
                // Creation Announcement
                com.rndmodgames.futtoboru.data.Message creationMsg = new com.rndmodgames.futtoboru.data.Message();
                creationMsg.setCategory(com.rndmodgames.futtoboru.data.MessageCategory.CUP);
                creationMsg.setPriority(com.rndmodgames.futtoboru.data.MessagePriority.URGENT);
                creationMsg.setTitle(com.rndmodgames.localization.LanguageModLoader.format("msg_cup_created_title", cupName));
                // P2-14: the cup opens on its calendar day (October); its first round is drawn later (January), by
                // AuthorityManager on the calendar's draw day, which then sends the mandatory draw message
                java.time.LocalDate drawDay = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(gameDate)
                        .withCup(cup.getDataId()).cupFirstDraw();
                creationMsg.setPlainTextMessage(com.rndmodgames.localization.LanguageModLoader.format("msg_cup_opened_text",
                        edition.getName(), edition.getParticipantClubsIds().size(),
                        com.rndmodgames.futtoboru.engine.messages.MessageManager.formatDay(drawDay.getDayOfMonth(),
                                drawDay.getMonthValue(), drawDay.getYear())));
                creationMsg.setScheduledDate(gameDate);
                creationMsg.setIsMandatory(false);
                messageManager.deliverMessage(creationMsg);
            }
        }
        
        script.setIsExecuted(true);
        Log.info(Log.ENGINE_SCRIPTS, "CUP CREATION SUCCESSFUL!");
        Log.info(Log.ENGINE_SCRIPTS, "========================================");
    }

    /**
     * P2-9: the season's edition of a competition from the game data, so a creation script opens it instead of
     * adding a second edition of the same season. By the edition id the script carries; a script without it (saves
     * made before P2-9) takes the competition's data edition of the season of {@code date}. Null if there is none.
     */
    static com.rndmodgames.futtoboru.data.CompetitionEdition findSeasonEdition(Competition competition, Long editionId,
            java.time.LocalDateTime date) {
        if (competition == null || competition.getEditions() == null) {
            return null;
        }
        for (com.rndmodgames.futtoboru.data.CompetitionEdition e : competition.getEditions()) {
            if (e != null && editionId != null && editionId.equals(e.getId())) {
                return e;
            }
        }
        if (editionId != null || date == null) {
            return null;
        }
        int startYear = com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(date).getStartYear();
        for (com.rndmodgames.futtoboru.data.CompetitionEdition e : competition.getEditions()) {
            if (e == null || e.getDataId() == null) {
                continue;
            }
            if (e.getStartDate() == null
                    || com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forDate(e.getStartDate()).getStartYear() == startYear) {
                return e;
            }
        }
        return null;
    }

    /** The script's participants on the edition (a data edition already lists them: no duplicates) */
    private static void setParticipants(com.rndmodgames.futtoboru.data.CompetitionEdition edition, Array<Long> clubIds) {
        if (clubIds == null) {
            return;
        }
        for (Long clubId : clubIds) {
            if (clubId != null && !edition.getParticipantClubsIds().contains(clubId)) {
                edition.getParticipantClubsIds().add(clubId);
            }
        }
        edition.setParticipantClubs(edition.getParticipantClubsIds().size());
    }

    /**
     * Finds a competition of the game: by runtime id (the data's competition, from the script), else by name; a
     * data competition not yet in the list is taken from the database and added to it.
     */
    private Competition findCompetition(java.util.List<Competition> competitions, Long competitionId, String name) {
        if (competitionId != null) {
            for (Competition comp : competitions) {
                if (competitionId.equals(comp.getId())) {
                    return comp;
                }
            }
            for (Competition comp : DatabaseLoader.getCompetitions()) {
                if (competitionId.equals(comp.getId())) {
                    competitions.add(comp);
                    return comp;
                }
            }
        }
        for (Competition comp : competitions) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                return comp;
            }
        }
        return null;
    }
}
