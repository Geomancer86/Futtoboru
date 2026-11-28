package com.rndmodgames.futtoboru.screens;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.NewGameClubs;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.ui.GroundArt;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiTooltips;
import com.rndmodgames.futtoboru.system.Log;

/**
 * New Game Overview Screen v1
 * 
 *  - List the Selected Active Countries
 *  - Game Start Date
 *  - Starting Country
 *  - Selected Profession
 *      - Selected Club
 * 
 * @author Geomancer86
 */
public class NewGameOverviewScreen implements Screen {

    Game game;
    Stage stage;
    SpriteBatch batch;
    Texture img;
    
    // Starting Season
    Season startingSeason = null;
    
    // Selected Countries List Setup
    List<Country> selectedCountries = new ArrayList<>();
    Country startingCountry = null;
    
    // Selected Player Professions Setup
    Profession primaryProfession = null;
    
    // Selected Starting Club
    Club startingClub = null;

    // Thumbnail of the selected club's season ground (U-11)
    final VisTable startingClubThumb = new VisTable();
    
    // Globally available components
    final VisLabel estimatedPlayerCountValueLabel = new VisLabel();
    final VisSelectBox<Club> startingClubSelectBox = new VisSelectBox<>();
    
    // 
    public NewGameOverviewScreen(Game parent, Season startingSeason, List<Country> selectedCountries) {
        
        this.game = parent;

        // Keep track of starting season
        this.startingSeason = startingSeason;
        
        // Keep track of selected Countries
        this.selectedCountries = selectedCountries;
        
        stage = new Stage(UiScale.newViewport());
        
        VisTable mainContainer = new VisTable(true);
        mainContainer.pad(UiTheme.space("xl")); // root of UiScale.addOverflowScroll below: scrolls when it does not fit (200%)
    
        // 
        final VisTable table = new VisTable(true);

        /**
         * Selected Countries Label
         */
        VisLabel selectedCountriesLabel = new VisLabel(LanguageModLoader.getValue("selected_countries"));
        
        /**
         * Selected Countries Table
         */
        final VisTable selectedCountriesTable = new VisTable(true);
        
        /**
         * Estimated Player Count
         *  - Count real players
         *  - Add x players per team without players (number to be generated at runtime)
         */
        final VisLabel estimatedPlayerCountLabel = new VisLabel(LanguageModLoader.getValue("estimate_player_count"));
        
        /**
         * Iterate all Competitions and count each Cup and League
         */
        int activeCups = 0;
        int activeLeagues = 0;
        int activeTeams = 0;
        int activePlayers = 0;
        
        List<Competition> allCompetitions = DatabaseLoader.getInstance().getCompetitions();
        
        for (Competition competition : allCompetitions) {
            
            switch(competition.getCompetitionType()) {
            
            case Competition.COMPETITION_LEAGUE:
                activeLeagues++;
                break;
            
            case Competition.COMPETITION_CUP:
            default:
                activeCups++;
                break;
            }
        }
        
        /**
         * Iterate active countries
         */
        for (Country country : selectedCountries) {
            
            // Count Active Teams
            activeTeams += DatabaseLoader.getInstance().getClubsByCountry().get(country.getId()).size();
            
            // Iterate country clubs and count players
            List<Club> allClubs = DatabaseLoader.getInstance().getClubsByCountry(country);
            
            // 
            for (Club club : allClubs) {
                
                // for clubs without real players
                if (club.getPlayers().isEmpty()) {
                 
                    // generated players should be this number
                    // TODO how many to generate should be recorded on club as a parameter
                    //       to generate new players if they arent loaded when game starts
                    activePlayers += 20;
                } else {
                    
                    //
                    activePlayers += club.getPlayers().size();
                }
            }
            
            VisLabel countryLabel = new VisLabel(country.getCommonName());
            
            /**
             * Country Label
             */
            selectedCountriesTable.row();
            selectedCountriesTable.add(countryLabel);
        }
        
        /**
         * Also we add some extra players as we will generate a pool of free agents
         * 
         * TODO: free agent pool size and other parameters
         */
        activePlayers = (int) (activePlayers * 1.5f);
        
        // Update active player label
        estimatedPlayerCountValueLabel.setText(LanguageModLoader.format("newgame_players_count", activePlayers));

        /**
         * New Game Details Table
         * 
         * Available Leagues & Teams
         */
        final VisTable newGameDetailsTable = new VisTable(true);

        VisLabel availableCupsLabel = new VisLabel(LanguageModLoader.getValue("active_cups"));
        VisLabel availableLeaguesLabel = new VisLabel(LanguageModLoader.getValue("active_leagues"));
        VisLabel availableTeamsLabel = new VisLabel(LanguageModLoader.getValue("active_teams"));
        
        // Available Cups
        newGameDetailsTable.row();
        newGameDetailsTable.add(availableCupsLabel);
        newGameDetailsTable.add(LanguageModLoader.format("newgame_cups_count", activeCups));
        
        // Available Leagues
        newGameDetailsTable.row();
        newGameDetailsTable.add(availableLeaguesLabel);
        newGameDetailsTable.add(LanguageModLoader.format("newgame_leagues_count", activeLeagues));
        
        // Available Teams
        newGameDetailsTable.row();
        newGameDetailsTable.add(availableTeamsLabel);
        newGameDetailsTable.add(LanguageModLoader.format("newgame_teams_count", activeTeams));
        
        newGameDetailsTable.row();
        newGameDetailsTable.add(estimatedPlayerCountLabel);
        newGameDetailsTable.add(estimatedPlayerCountValueLabel);
        
        /**
         * Game Start Date
         *  - Available Dates extracted from DataBase + Selected Countries
         * 
         * Start at Team/Start Unemployed
         */
        final VisTable gameStartConfigurationTable = new VisTable(true);
        
        //
        VisLabel gameStartCountryLabel = new VisLabel(LanguageModLoader.getValue("game_start_country"));
        
        //
        VisLabel gameStartDateLabel = new VisLabel(LanguageModLoader.getValue("game_start_date"));
        
        //
        VisLabel startingProfessionLabel = new VisLabel(LanguageModLoader.getValue("starting_profession"));
        
        //
        VisLabel startingClubLabel = new VisLabel(LanguageModLoader.getValue("starting_club"));
        
        /**
         * Available Starting Countries
         */
        final VisSelectBox<Country> startingCountrySelectBox = new VisSelectBox<>();
        
        startingCountrySelectBox.setItems(selectedCountries.toArray(new Country[selectedCountries.size()]));
        
        //
        startingCountrySelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                /**
                 * The selected Starting Country is Mandatory
                 * 
                 * TODO: save on save game / Person location
                 */
                startingCountry = startingCountrySelectBox.getSelected();
                
                //
                if (primaryProfession != null) {
                    updateSelectableTeamsByLeagueAndCountry();
                }
            }
        });
        
        // hacky
        if (startingCountrySelectBox.getItems().size > 1) {
            startingCountrySelectBox.setSelectedIndex(1);
            startingCountrySelectBox.setSelectedIndex(0);
        } else {
            
            startingCountry = selectedCountries.get(0);
        }
        
        /**
         * Available Starting Professions
         * 
         *  - Depending on the Selected Profession, the Player might choose to start Employed or Unemployed
         *  
         */
        final VisSelectBox<Profession> professionsSelectBox = new VisSelectBox<>();
        
        professionsSelectBox.setItems(DatabaseLoader.getInstance().getSelectableProfessions()
                .toArray(new Profession[DatabaseLoader.getInstance().getSelectableProfessions().size()]));
        
        /**
         * Initial Club Selection
         *  - This will be Toogled depending on the Selected Profession and the Selected Countries and Lowest Available Leagues
         */
        startingClubSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                startingClub = startingClubSelectBox.getSelected();
                updateStartingClubThumb();
            }
        });
        
        professionsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                /**
                 * The selected Profession
                 */
                primaryProfession = professionsSelectBox.getSelected();
                
                //
                if (primaryProfession != null) {
                  
                    // update the selectable countries
                    updateSelectableTeamsByLeagueAndCountry();
                }
            }
        });
        
        // default profession to unemployed
        primaryProfession = professionsSelectBox.getItems().get(0);
        updateSelectableTeamsByLeagueAndCountry();

        //
        gameStartConfigurationTable.add(gameStartDateLabel);
        gameStartConfigurationTable.add(GameFormat.longDate(startingSeason.getStartDate())); // U-12: the game language
        
        //
        gameStartConfigurationTable.row();
        gameStartConfigurationTable.add(gameStartCountryLabel);
        gameStartConfigurationTable.add(startingCountrySelectBox);        
        
        //
        gameStartConfigurationTable.row();
        gameStartConfigurationTable.add(startingProfessionLabel);
        gameStartConfigurationTable.add(professionsSelectBox);
        
        // 
        gameStartConfigurationTable.row();
        gameStartConfigurationTable.add(startingClubLabel);
        gameStartConfigurationTable.add(startingClubSelectBox);
        gameStartConfigurationTable.add(startingClubThumb).left();
        updateStartingClubThumb();
        
        /**
         * Back to Setup Button
         */
        final VisTextButton backButton = new VisTextButton(LanguageModLoader.getValue("back"));
        
        backButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                /**
                 * Move Back to New Game Setup Screen
                 * 
                 *  - Keep Track of Player Selections
                 *      - Selected Countries        [DONE]
                 *      - Selected Lowest Leagues   [TODO]
                 *      - Selected Profession       [TODO]
                 */
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SETUP_SCREEN, startingSeason, selectedCountries);
            }
        });
        
        /**
         * Start Game Button
         */
        final VisTextButton startGameButton = new VisTextButton(LanguageModLoader.getValue("start_game"));
        
        startGameButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
 
                Log.info(Log.UI_MENU, "NewGameOverviewScreen", "=== START GAME BUTTON CLICKED ===");
                
                try {
                    /**
                     * NEW_GAME_SETUP IS PERFORMED HERE!
                     * 
                     * Set the Selected Countries and Redirect to Main Game Screen:
                     * 
                     *      - Selected Countries        [DONE]
                     *      - Selected Profession       [DONE]
                     *      - Starting Country          [DONE]
                     *      - Starting Club             [DONE]
                     *      - Starting Scripts          [WIP]
                     */
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 1: Getting current game...");
                    SaveGame currentGame = ((Futtoboru) game).getCurrentGame();
                    
                    if (currentGame == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "currentGame is NULL!");
                        throw new IllegalStateException("SaveGame is null. Cannot start game without a SaveGame instance.");
                    }
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 1: OK - currentGame obtained");
                    
                    // Game Authority
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 2: Setting main authority...");
                    if (DatabaseLoader.getInstance().getMainAuthority() == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "MainAuthority is NULL!");
                        throw new IllegalStateException("MainAuthority is null. Database may not be loaded correctly.");
                    }
                    currentGame.setMainAuthority(DatabaseLoader.getInstance().getMainAuthority());
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 2: OK - main authority set");
                    
                    //
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 3: Setting selected countries...");
                    if (selectedCountries == null || selectedCountries.isEmpty()) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "selectedCountries is NULL or EMPTY!");
                        throw new IllegalStateException("Selected countries list is null or empty.");
                    }
                    currentGame.setSelectedCountries(selectedCountries);
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 3: OK - selected countries set: " + selectedCountries.size());
                    
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 4: Setting owner profession and country...");
                    if (currentGame.getOwner() == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "currentGame.getOwner() is NULL!");
                        throw new IllegalStateException("Game owner is null. Manager must be created before starting game.");
                    }
                    
                    // CRITICAL: Ensure owner has an ID for job system to work
                    // P2-4: always a fresh runtime id from this game's allocator (a manager profile loaded from
                    // disk may carry an id from another game)
                    {
                        currentGame.getOwner().setId(null);
                        currentGame.assignId(currentGame.getOwner());
                        Long newOwnerId = currentGame.getOwner().getId();
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 4a: Assigned owner ID: " + newOwnerId);
                        
                        // Add owner to allPersons list if not already there
                        if (currentGame.getAllPersons() == null) {
                            currentGame.setAllPersons(new ArrayList<>());
                        }
                        boolean ownerInList = false;
                        for (com.rndmodgames.futtoboru.data.Person p : currentGame.getAllPersons()) {
                            if (p == currentGame.getOwner()) {
                                ownerInList = true;
                                break;
                            }
                        }
                        if (!ownerInList) {
                            currentGame.getAllPersons().add(currentGame.getOwner());
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 4a: Added owner to allPersons list");
                        }
                    }
                    
                    if (primaryProfession == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "primaryProfession is NULL!");
                        throw new IllegalStateException("Primary profession is null.");
                    }
                    if (startingCountry == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "startingCountry is NULL!");
                        throw new IllegalStateException("Starting country is null.");
                    }
                    currentGame.getOwner().setPrimaryProfession(primaryProfession);
                    currentGame.getOwner().setCurrentCountry(startingCountry);
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 4: OK - owner profession and country set");
                    
                    // Starting Season
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 5: Setting game dates...");
                    if (startingSeason != null) {
                        if (startingSeason.getStartDate() == null) {
                            Log.error(Log.UI_MENU, "NewGameOverviewScreen", "startingSeason.getStartDate() is NULL!");
                            throw new IllegalStateException("Season start date is null.");
                        }
                        currentGame.setGameStartDate(startingSeason.getStartDate());
                        currentGame.setGameDate(startingSeason.getStartDate());
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 5: OK - game dates set: " + startingSeason.getStartDate());
                    } else {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "startingSeason is NULL!");
                        throw new IllegalStateException("Starting season is null.");
                    }
                    
                    /**
                     * Clubs Database
                     * 
                     * TODO: this should be loaded in a more automatic way, so we don't need to do that much setup on unit tests
                     */
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 6: Loading clubs database...");
                    currentGame.setAllClubs(new ArrayList<>());
                    
                    if (DatabaseLoader.getInstance().getClubsByCountry() == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "DatabaseLoader.getInstance().getClubsByCountry() is NULL!");
                        throw new IllegalStateException("Clubs by country map is null. Database may not be loaded correctly.");
                    }
                    
                    /**
                     * Competitions
                     * 
                     * CRITICAL: Ensure all cups and leagues from DatabaseLoader are added to SaveGame.
                     * Also ensure ALL participant clubs for those competitions are added to SaveGame,
                     * even if their country wasn't selected (e.g., Linfield in FA Cup).
                     * This ensures AuthorityManager can find and process them correctly.
                     */
                    // P2-13: the participants first, then the clubs of the selected countries not listed yet: each
                    // club once (it was twice for the selected countries, so the daily club steps ran twice)
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 6a: Adding competitions, their participants and the selected countries' clubs...");
                    NewGameClubs.addTo(currentGame, DatabaseLoader.getInstance().getCompetitions(), selectedCountries);
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 6: OK - Total cups: " + currentGame.getAllCups().size() + ", leagues: " + currentGame.getAllLeagues().size() + ", total clubs: " + currentGame.getAllClubs().size());

                    /**
                     * Starting Club
                     * 
                     * TODO: we also need to set the Person as working at Club level (owner, staff, etc.)
                     * 
                     * NOTE: starting club cannot be null because we insert a ghost UNAVAILABLE Club
                     */
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: Setting starting club...");
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: startingClub = " + (startingClub != null ? startingClub.getName() : "NULL"));
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: startingClub.getId() = " + (startingClub != null && startingClub.getId() != null ? startingClub.getId() : "NULL"));
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: primaryProfession = " + (primaryProfession != null ? primaryProfession.getName() + " (ID: " + primaryProfession.getId() + ")" : "NULL"));
                    
                    // Check if this is a profession that doesn't have a club (Player, Retired Player, Agent, Investor)
                    boolean professionRequiresNoClub = (primaryProfession != null && 
                        (primaryProfession.is(Profession.PLAYER) ||  // Player
                         primaryProfession.is(Profession.RETIRED_PLAYER) ||  // Retired Player
                         primaryProfession.is(Profession.AGENT) ||  // Agent
                         primaryProfession.is(Profession.INVESTOR)));  // Investor
                    
                    if (startingClub != null && startingClub.getId() != null && !professionRequiresNoClub) {
                        boolean clubFound = false;
                        /**
                         * Make sure current club is a club instance on currentGame.currentclubs to avoid split instances
                         */
                        for (Club club : currentGame.getAllClubs()) {
                            if (club == null) {
                                continue;
                            }
                            if (startingClub.getId().equals(club.getId())) {
                                // We only save the current club ID to avoid saving repeated times on JSON file
                                currentGame.getOwner().setCurrentClubId(club.getId());
                                clubFound = true;
                                Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: OK - Starting club set: " + club.getName() + " (ID: " + club.getId() + ")");
                                break;
                            }
                        }
                        if (!clubFound) {
                            Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Starting club ID " + startingClub.getId() + " not found in loaded clubs!");
                        }
                    } else {
                        if (professionRequiresNoClub) {
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: OK - Profession " + primaryProfession.getName() + " does not require a starting club");
                        } else if (startingClub == null) {
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: OK - No starting club selected (unemployed start)");
                        } else {
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 7: OK - Starting club is UNAVAILABLE placeholder (no club ID)");
                        }
                        // Explicitly set currentClubId to null for professions that don't have clubs
                        currentGame.getOwner().setCurrentClubId(null);
                    }
                    
                    /**
                     * Starting Scripts
                     * 
                     *  WIP:
                     *  
                     *      - add some basic scripted messages to appear on the inbox screen listing as the game starts
                     *      
                     *  - Basic way to do:
                     *      - Add a date to scripted messages
                     *      - If date is in the future, the message "is not sent" isSent = false
                     *      - Inbox Screen rendering ignores those directly
                     *      - 
                     *     
                     *  - UNEMPLOYED:
                     *      - Welcome Message, button to JOBS SCREEN
                     *      
                     *  - MANAGER:
                     *      - Welcome Message, buttons to SQUAD, LINEUP, TACTICS, TRAINING, ETC
                     *      
                     *  - GENERIC:
                     *      - The League will be formed on X_DATE
                     *      
                     *  TBD: audit save game stuff
                     *      - game real starting date
                     *      - game real last saved game date
                     *      - number of saves
                     *      - play time
                     *      - number of clicks
                     *      - number of key strokes
                     *      - most visited screens data
                     *      
                     *      
                     * -------------------------------------------------------------
                     * 
                     */
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 8: Loading season scripts...");
                    if (startingSeason.getSeasonScripts() == null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "startingSeason.getSeasonScripts() is NULL!");
                        throw new IllegalStateException("Season scripts list is null.");
                    }
                    
                    if (Futtoboru.DEBUG_MODE) {
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "--------------------------");
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "ITERATING SEASON SCRIPTS: ");
                        
                        for (BasicScript script : startingSeason.getSeasonScripts()) {
                            if (script == null) {
                                Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Found null script in season scripts!");
                                continue;
                            }
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "SEASON SCRIPT: " + script.getName());
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "DESCRIPTION  : " + script.getDescription());
                            
                            /**
                             * TEXT / SERIALIZED SCRIPT VERSION
                             */
                            if (script.getScriptValues() != null) {
                                Log.info(Log.UI_MENU, "NewGameOverviewScreen", "SCRIPT VALUES: " + script.getScriptValues().size());
                                
                                for (Map.Entry<String, Object> entry : script.getScriptValues().entrySet()) {
                                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Script Key = " + entry.getKey() + ", Script Value = " + entry.getValue());
                                }
                            } else {
                                Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Script values are null for script: " + script.getName());
                            }
                            
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "--------------------------");
                        }
                    }
                    
                    /**
                     * Add Season Scripts to SaveGame
                     */
                    currentGame.getGameScripts().addAll(startingSeason.getSeasonScripts());
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 8: OK - Added " + startingSeason.getSeasonScripts().size() + " season scripts");
                    
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 9: Initializing job system...");
                    try {
                        ((Futtoboru) game).initializeJobSystem();
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 9: OK - Job system initialized");
                    } catch (Exception e) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "ERROR in Step 9 (initializeJobSystem):", e);
                        Log.error(Log.UI_MENU, e);
                        throw e; // Re-throw to be caught by outer catch
                    }
                    
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 10: Creating initial job openings...");
                    try {
                        if (((Futtoboru) game).getJobManager() != null) {
                            ((Futtoboru) game).getJobManager().initializeJobOpenings();
                            Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 10: OK - Initial job openings created");
                        } else {
                            Log.error(Log.UI_MENU, "NewGameOverviewScreen", "ERROR in Step 10: JobManager is NULL!");
                            throw new IllegalStateException("JobManager is null after initialization");
                        }
                    } catch (Exception e) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "ERROR in Step 10 (initializeJobOpenings):", e);
                        Log.error(Log.UI_MENU, e);
                        throw e; // Re-throw to be caught by outer catch
                    }
                    
                    // Generate attributes for all players (v1.0)
                    try {
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 11: Generating player attributes...");
                        generateAttributesForAllPlayers(currentGame);
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 11: OK - Player attributes generated");
                    } catch (Exception e) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "ERROR in Step 11 (generateAttributesForAllPlayers):", e);
                        Log.error(Log.UI_MENU, e);
                        // Don't throw - attributes can be generated later if needed
                    }
                    
                    // Create initial attribute snapshots for change tracking (v1.0)
                    try {
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 12: Creating initial attribute snapshots...");
                        createInitialAttributeSnapshots(currentGame);
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 12: OK - Initial snapshots created");
                    } catch (Exception e) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "ERROR in Step 12 (createInitialAttributeSnapshots):", e);
                        Log.error(Log.UI_MENU, e);
                        // Don't throw - snapshots can be created later
                    }
                    
                    // Create Welcome message (v2.0)
                    try {
                        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 13: Creating welcome message...");
                        Futtoboru futtoboru = (Futtoboru) game;
                        if (futtoboru.getGameEngine() != null && futtoboru.getGameEngine().getMessageManager() != null) {
                            com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
                                futtoboru.getGameEngine().getMessageManager();
                            
                            com.rndmodgames.futtoboru.data.Message welcomeMessage = 
                                messageManager.createWelcomeMessage(
                                    currentGame.getOwner(),
                                    primaryProfession,
                                    startingCountry,
                                    startingClub,
                                    currentGame.getGameStartDate()
                                );
                            
                            if (welcomeMessage != null) {
                                // Deliver immediately
                                messageManager.deliverMessage(welcomeMessage);
                                Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 13: OK - Welcome message created and delivered");
                                Log.info(Log.UI_MENU, "NewGameOverviewScreen: Created and delivered welcome message ID " + welcomeMessage.getId());
                                
                                // Verify message was added
                                int messageCount = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
                                Log.info(Log.UI_MENU, "NewGameOverviewScreen: Total messages after welcome: " + messageCount);
                            } else {
                                Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Step 13: WARNING - Welcome message is null");
                            }
                        } else {
                            Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Step 13: ERROR - MessageManager not available");
                        }
                    } catch (Exception e) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "ERROR in Step 13 (createWelcomeMessage):", e);
                        Log.error(Log.UI_MENU, e);
                        // Don't throw - welcome message is nice to have but not critical
                    }
                    
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Step 14: Changing to GAME_SCREEN...");
                    ((Futtoboru) game).getScreenNavigation().reset();
                    ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);
                    Log.info(Log.UI_MENU, "NewGameOverviewScreen", "=== START GAME COMPLETED SUCCESSFULLY ===");
                    
                } catch (Exception e) {
                    // Print to console (System.out) so it shows in debug window
                    Log.error(Log.UI_MENU, "========================================");
                    Log.error(Log.UI_MENU, "CRITICAL ERROR DURING GAME STARTUP!");
                    Log.error(Log.UI_MENU, "========================================");
                    Log.error(Log.UI_MENU, "Exception type: " + e.getClass().getName());
                    Log.error(Log.UI_MENU, "Exception message: " + e.getMessage());
                    if (e.getCause() != null) {
                        Log.error(Log.UI_MENU, "Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                    }
                    Log.error(Log.UI_MENU, "----------------------------------------");
                    Log.error(Log.UI_MENU, e);
                    Log.error(Log.UI_MENU, "========================================");
                    
                    // Also log via Gdx for consistency
                    Log.error(Log.UI_MENU, "NewGameOverviewScreen", "CRITICAL ERROR during game startup!", e);
                    Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Exception type: " + e.getClass().getName());
                    Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Exception message: " + e.getMessage());
                    if (e.getCause() != null) {
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                    }
                    Log.error(Log.UI_MENU, e);
                    
                    // Print full stack trace to console
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    Log.error(Log.UI_MENU, "Full stack trace:");
                    Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Stack trace:");
                    for (int i = 0; i < Math.min(30, stackTrace.length); i++) {
                        Log.error(Log.UI_MENU, "  at " + stackTrace[i].toString());
                        Log.error(Log.UI_MENU, "NewGameOverviewScreen", "  at " + stackTrace[i].toString());
                    }
                    Log.error(Log.UI_MENU, "========================================");
                    
                    // Don't throw - show error dialog instead to prevent window from closing
                    // TODO: Show error dialog to user
                    Log.error(Log.UI_MENU, "NewGameOverviewScreen", "Game startup failed. Window will remain open for debugging.");
                    Log.error(Log.UI_MENU, "Game startup failed. Window will remain open for debugging.");
                    return; // Exit the button handler without changing screens
                }
            }
        });

        // selected countries label
        mainContainer.row();
        mainContainer.add(selectedCountriesLabel);
        
        // selected countries table
        mainContainer.row();
        mainContainer.addSeparator();
        mainContainer.add(selectedCountriesTable);
        
        // new game details
        mainContainer.row();
        mainContainer.addSeparator();
        mainContainer.add(newGameDetailsTable);
        
        // game start configuration
        mainContainer.row();
        mainContainer.addSeparator();
        mainContainer.add(gameStartConfigurationTable);
        
        // back button
        mainContainer.row();
        mainContainer.add(backButton);
        
        // start game button
        mainContainer.add(startGameButton);
        
        // 
        mainContainer.add(table);
        
        // Add Settings Screen Main Container to Stage
        UiScale.addOverflowScroll(stage, mainContainer);
    }

    @Override
    public void show() {

        // Add input capabilities
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        // Clear blit
        UiTheme.clearScreen();
        
        // Draw
        stage.act();
        stage.draw(); 
    }

    @Override
    public void resize(int width, int height) {

        // Update viewport on resize to keep event coordinate for buttons/widgets
        UiScale.updateStageViewport(stage, width, height);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Updates the Starting Club after changes on Starting Country, etc
     * 
     * TODO: fix
     */
    /** Shows the selected club's ground thumbnail (nothing while no club can be picked). */
    private void updateStartingClubThumb() {
        startingClubThumb.clearChildren();
        Club club = startingClubSelectBox.isDisabled() ? null : startingClubSelectBox.getSelected();
        if (club == null) {
            return;
        }
        Actor thumb = ((Futtoboru) game).getGroundArt().picture(GroundArt.groundId(club), true,
                UiTheme.size("art.thumb.w"), UiTheme.size("art.thumb.h"));
        if (club.getStadium() != null) {
            UiTooltips.tipText(thumb, club.getStadium().getName());
        }
        startingClubThumb.add(thumb);
    }

    private void updateSelectableTeamsByLeagueAndCountry() {
        
        /**
         * Set Starting Clubs SelectBox Items Depending on Selected Profession
         * 
         * 1, Player,false
         * 2, Retired Player,true
         * 3, Manager,true
         * 4, Director,true
         * 5, Scout,true
         * 6, Agent,true
         * 7, Investor,true
         * 8, Club Owner,true
         */
        switch (primaryProfession.getId().intValue()) {

        // No Selectable club
        case 1:
        case 2:
        case 6:
        case 7:
            // set starting club to UNAVAILABLE and DISABLE
            startingClubSelectBox.setItems(new Club(LanguageModLoader.getValue("newgame_club_unavailable")));
            startingClubSelectBox.setDisabled(true);
            updateStartingClubThumb();
            break;
            
        // Selectable Clubs
        case 3:
        case 4:
        case 5:
        case 8:
            
            /**
             * Set All Clubs By Country as Selectable
             * 
             *  - TODO: this needs to depend on the Lowest Selected League too
             */
            
            startingClubSelectBox.setItems(DatabaseLoader.getInstance().getClubsByCountry(startingCountry)
                    .toArray(new Club[DatabaseLoader.getInstance().getClubsByCountry(startingCountry).size()]));
            
            startingClubSelectBox.setDisabled(false);
            updateStartingClubThumb();
            break;
        
        // deault to No Selectable club
        default:
            break;
        }
    }
    
    @Override
    public void hide() {
        
        Log.info(Log.UI_MENU, "HIDE WAS CALLED ON NEW GAME OVERVIEW SCREEN - DISPOSE");
        
        // Hide will be called after switching to a separate screen
        dispose();
    }

    @Override
    public void dispose() {

        // Dispose on screen change
        stage.dispose();
    }
    
    /**
     * Generate attributes for all players in the game (v1.0)
     */
    private void generateAttributesForAllPlayers(SaveGame currentGame) {
        PlayerAttributeGenerator attrGen = new PlayerAttributeGenerator();
        LocalDateTime gameDate = currentGame.getGameDate();
        
        int playersGenerated = 0;
        for (Club club : currentGame.getAllClubs()) {
            for (Player player : club.getPlayers()) {
                if (player != null && player.getPerson() != null) {
                    // Only generate if attributes are null (not already generated)
                    if (player.getAcceleration() == null) {
                        attrGen.generatePlayerAttributes(player, player.getPerson(), gameDate,
                                ((Futtoboru) game).random().fork("attributes:" + player.getPerson().getId()));
                        playersGenerated++;
                    }
                }
            }
        }
        
        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Generated attributes for " + playersGenerated + " players");
    }
    
    /**
     * Create initial attribute snapshots for all players (v1.0)
     * This allows change tracking to work immediately from day 1
     */
    private void createInitialAttributeSnapshots(SaveGame currentGame) {
        LocalDateTime gameDate = currentGame.getGameDate();
        
        int snapshotsCreated = 0;
        for (Club club : currentGame.getAllClubs()) {
            for (Player player : club.getPlayers()) {
                if (player != null && player.getPerson() != null && player.getPerson().getId() != null) {
                    // Only create snapshot if player has attributes
                    if (player.getAcceleration() != null) {
                        PlayerAttributeSnapshot snapshot = PlayerAttributeSnapshot.fromPlayer(
                            player, 
                            gameDate, 
                            "INITIAL"
                        );
                        if (snapshot != null) {
                            currentGame.getPlayerAttributeSnapshots().add(snapshot);
                            snapshotsCreated++;
                        }
                    }
                }
            }
        }
        
        Log.info(Log.UI_MENU, "NewGameOverviewScreen", "Created " + snapshotsCreated + " initial attribute snapshots");
    }
}