package com.rndmodgames.futtoboru.screens;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
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
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.localization.LanguageModLoader;

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
        
        stage = new Stage(new ScreenViewport());
        
        VisTable mainContainer = new VisTable(true);
        mainContainer.pad(50).setFillParent(true);
    
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
        
        List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
        
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
            activeTeams += DatabaseLoader.getClubsByCountry().get(country.getId()).size();
            
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
        estimatedPlayerCountValueLabel.setText(activePlayers + " Players");

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
        newGameDetailsTable.add(activeCups + " Cups");
        
        // Available Leagues
        newGameDetailsTable.row();
        newGameDetailsTable.add(availableLeaguesLabel);
        newGameDetailsTable.add(activeLeagues + " Leagues");
        
        // Available Teams
        newGameDetailsTable.row();
        newGameDetailsTable.add(availableTeamsLabel);
        newGameDetailsTable.add(activeTeams + " Teams");
        
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
        gameStartConfigurationTable.add(DatabaseLoader.formatter.format(startingSeason.getStartDate()));
        
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
 
                Gdx.app.log("NewGameOverviewScreen", "=== START GAME BUTTON CLICKED ===");
                
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
                    Gdx.app.log("NewGameOverviewScreen", "Step 1: Getting current game...");
                    SaveGame currentGame = ((Futtoboru) game).getCurrentGame();
                    
                    if (currentGame == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: currentGame is NULL!");
                        throw new IllegalStateException("SaveGame is null. Cannot start game without a SaveGame instance.");
                    }
                    Gdx.app.log("NewGameOverviewScreen", "Step 1: OK - currentGame obtained");
                    
                    // Game Authority
                    Gdx.app.log("NewGameOverviewScreen", "Step 2: Setting main authority...");
                    if (DatabaseLoader.getMainAuthority() == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: MainAuthority is NULL!");
                        throw new IllegalStateException("MainAuthority is null. Database may not be loaded correctly.");
                    }
                    currentGame.setMainAuthority(DatabaseLoader.getMainAuthority());
                    Gdx.app.log("NewGameOverviewScreen", "Step 2: OK - main authority set");
                    
                    //
                    Gdx.app.log("NewGameOverviewScreen", "Step 3: Setting selected countries...");
                    if (selectedCountries == null || selectedCountries.isEmpty()) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: selectedCountries is NULL or EMPTY!");
                        throw new IllegalStateException("Selected countries list is null or empty.");
                    }
                    currentGame.setSelectedCountries(selectedCountries);
                    Gdx.app.log("NewGameOverviewScreen", "Step 3: OK - selected countries set: " + selectedCountries.size());
                    
                    Gdx.app.log("NewGameOverviewScreen", "Step 4: Setting owner profession and country...");
                    if (currentGame.getOwner() == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: currentGame.getOwner() is NULL!");
                        throw new IllegalStateException("Game owner is null. Manager must be created before starting game.");
                    }
                    
                    // CRITICAL: Ensure owner has an ID for job system to work
                    if (currentGame.getOwner().getId() == null) {
                        Gdx.app.log("NewGameOverviewScreen", "Step 4a: Owner has no ID, assigning unique ID...");
                        // Generate a unique ID (use negative range to avoid conflicts with database-loaded persons)
                        // Find the highest existing person ID, or use -1 as starting point
                        Long maxId = -1L;
                        if (currentGame.getAllPersons() != null && !currentGame.getAllPersons().isEmpty()) {
                            for (com.rndmodgames.futtoboru.data.Person p : currentGame.getAllPersons()) {
                                if (p != null && p.getId() != null && p.getId() < 0 && p.getId() < maxId) {
                                    maxId = p.getId();
                                }
                            }
                        }
                        // Assign ID (decrement from maxId, so -1, -2, -3, etc.)
                        Long newOwnerId = maxId - 1;
                        currentGame.getOwner().setId(newOwnerId);
                        Gdx.app.log("NewGameOverviewScreen", "Step 4a: Assigned owner ID: " + newOwnerId);
                        System.out.println("[NewGameOverviewScreen] Assigned owner ID: " + newOwnerId);
                        
                        // Add owner to allPersons list if not already there
                        if (currentGame.getAllPersons() == null) {
                            currentGame.setAllPersons(new ArrayList<>());
                        }
                        boolean ownerInList = false;
                        for (com.rndmodgames.futtoboru.data.Person p : currentGame.getAllPersons()) {
                            if (p != null && p.getId() != null && p.getId().equals(newOwnerId)) {
                                ownerInList = true;
                                break;
                            }
                        }
                        if (!ownerInList) {
                            currentGame.getAllPersons().add(currentGame.getOwner());
                            Gdx.app.log("NewGameOverviewScreen", "Step 4a: Added owner to allPersons list");
                        }
                    } else {
                        Gdx.app.log("NewGameOverviewScreen", "Step 4a: Owner already has ID: " + currentGame.getOwner().getId());
                    }
                    
                    if (primaryProfession == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: primaryProfession is NULL!");
                        throw new IllegalStateException("Primary profession is null.");
                    }
                    if (startingCountry == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: startingCountry is NULL!");
                        throw new IllegalStateException("Starting country is null.");
                    }
                    currentGame.getOwner().setPrimaryProfession(primaryProfession);
                    currentGame.getOwner().setCurrentCountry(startingCountry);
                    Gdx.app.log("NewGameOverviewScreen", "Step 4: OK - owner profession and country set");
                    
                    // Starting Season
                    Gdx.app.log("NewGameOverviewScreen", "Step 5: Setting game dates...");
                    if (startingSeason != null) {
                        if (startingSeason.getStartDate() == null) {
                            Gdx.app.error("NewGameOverviewScreen", "ERROR: startingSeason.getStartDate() is NULL!");
                            throw new IllegalStateException("Season start date is null.");
                        }
                        currentGame.setGameStartDate(startingSeason.getStartDate());
                        currentGame.setGameDate(startingSeason.getStartDate());
                        Gdx.app.log("NewGameOverviewScreen", "Step 5: OK - game dates set: " + startingSeason.getStartDate());
                    } else {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: startingSeason is NULL!");
                        throw new IllegalStateException("Starting season is null.");
                    }
                    
                    /**
                     * Clubs Database
                     * 
                     * TODO: this should be loaded in a more automatic way, so we don't need to do that much setup on unit tests
                     */
                    Gdx.app.log("NewGameOverviewScreen", "Step 6: Loading clubs database...");
                    currentGame.setAllClubs(new ArrayList<>());
                    
                    if (DatabaseLoader.getClubsByCountry() == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: DatabaseLoader.getClubsByCountry() is NULL!");
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
                    Gdx.app.log("NewGameOverviewScreen", "Step 6a: Adding competitions and participant clubs to SaveGame...");
                    List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
                    if (allCompetitions != null) {
                        for (Competition comp : allCompetitions) {
                            if (comp == null) continue;
                            
                            // Add competition to SaveGame lists
                            if (Competition.COMPETITION_CUP.equals(comp.getCompetitionType())) {
                                if (!currentGame.getAllCups().contains(comp)) {
                                    currentGame.getAllCups().add(comp);
                                }
                            } else if (Competition.COMPETITION_LEAGUE.equals(comp.getCompetitionType())) {
                                if (!currentGame.getAllLeagues().contains(comp)) {
                                    currentGame.getAllLeagues().add(comp);
                                }
                            }
                            
                            // Ensure all participant clubs from all editions are in SaveGame.allClubs
                            if (comp.getEditions() != null) {
                                for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : comp.getEditions()) {
                                    if (edition != null && edition.getParticipantClubsIds() != null) {
                                        for (Long clubId : edition.getParticipantClubsIds()) {
                                            if (clubId != null && currentGame.getClubById(clubId) == null) {
                                                Club club = DatabaseLoader.getClubById(clubId);
                                                if (club != null) {
                                                    Gdx.app.log("NewGameOverviewScreen", "Adding participant club " + club.getName() + " (ID: " + clubId + ") to SaveGame for competition: " + comp.getName());
                                                    currentGame.getAllClubs().add(club);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Gdx.app.log("NewGameOverviewScreen", "Step 6a: OK - Total cups: " + currentGame.getAllCups().size() + ", leagues: " + currentGame.getAllLeagues().size() + ", total clubs: " + currentGame.getAllClubs().size());
                    }
                    
                    for (Country country : selectedCountries) {
                        if (country == null) {
                            Gdx.app.error("NewGameOverviewScreen", "ERROR: country in selectedCountries is NULL!");
                            continue;
                        }
                        if (country.getId() == null) {
                            Gdx.app.error("NewGameOverviewScreen", "ERROR: country.getId() is NULL for country: " + country.getCommonName());
                            continue;
                        }
                        
                        List<Club> clubsForCountry = DatabaseLoader.getClubsByCountry().get(country.getId());
                        if (clubsForCountry == null) {
                            Gdx.app.error("NewGameOverviewScreen", "ERROR: No clubs found for country ID: " + country.getId() + " (" + country.getCommonName() + ")");
                            continue;
                        }
                        
                        // Add all Clubs for the selected countries to be simulated
                        currentGame.getAllClubs().addAll(clubsForCountry);
                        Gdx.app.log("NewGameOverviewScreen", "Step 6: Added " + clubsForCountry.size() + " clubs for country: " + country.getCommonName());
                    }
                    Gdx.app.log("NewGameOverviewScreen", "Step 6: OK - Total clubs loaded: " + currentGame.getAllClubs().size());

                    /**
                     * Starting Club
                     * 
                     * TODO: we also need to set the Person as working at Club level (owner, staff, etc.)
                     * 
                     * NOTE: starting club cannot be null because we insert a ghost UNAVAILABLE Club
                     */
                    Gdx.app.log("NewGameOverviewScreen", "Step 7: Setting starting club...");
                    Gdx.app.log("NewGameOverviewScreen", "Step 7: startingClub = " + (startingClub != null ? startingClub.getName() : "NULL"));
                    Gdx.app.log("NewGameOverviewScreen", "Step 7: startingClub.getId() = " + (startingClub != null && startingClub.getId() != null ? startingClub.getId() : "NULL"));
                    Gdx.app.log("NewGameOverviewScreen", "Step 7: primaryProfession = " + (primaryProfession != null ? primaryProfession.getName() + " (ID: " + primaryProfession.getId() + ")" : "NULL"));
                    
                    // Check if this is a profession that doesn't have a club (Player, Retired Player, Agent, Investor)
                    boolean professionRequiresNoClub = (primaryProfession != null && 
                        (primaryProfession.getId().equals(1L) ||  // Player
                         primaryProfession.getId().equals(2L) ||  // Retired Player
                         primaryProfession.getId().equals(6L) ||  // Agent
                         primaryProfession.getId().equals(7L)));  // Investor
                    
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
                                Gdx.app.log("NewGameOverviewScreen", "Step 7: OK - Starting club set: " + club.getName() + " (ID: " + club.getId() + ")");
                                break;
                            }
                        }
                        if (!clubFound) {
                            Gdx.app.error("NewGameOverviewScreen", "WARNING: Starting club ID " + startingClub.getId() + " not found in loaded clubs!");
                        }
                    } else {
                        if (professionRequiresNoClub) {
                            Gdx.app.log("NewGameOverviewScreen", "Step 7: OK - Profession " + primaryProfession.getName() + " does not require a starting club");
                        } else if (startingClub == null) {
                            Gdx.app.log("NewGameOverviewScreen", "Step 7: OK - No starting club selected (unemployed start)");
                        } else {
                            Gdx.app.log("NewGameOverviewScreen", "Step 7: OK - Starting club is UNAVAILABLE placeholder (no club ID)");
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
                    Gdx.app.log("NewGameOverviewScreen", "Step 8: Loading season scripts...");
                    if (startingSeason.getSeasonScripts() == null) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR: startingSeason.getSeasonScripts() is NULL!");
                        throw new IllegalStateException("Season scripts list is null.");
                    }
                    
                    if (Futtoboru.DEBUG_MODE) {
                        Gdx.app.log("NewGameOverviewScreen", "--------------------------");
                        Gdx.app.log("NewGameOverviewScreen", "ITERATING SEASON SCRIPTS: ");
                        
                        for (BasicScript script : startingSeason.getSeasonScripts()) {
                            if (script == null) {
                                Gdx.app.error("NewGameOverviewScreen", "WARNING: Found null script in season scripts!");
                                continue;
                            }
                            Gdx.app.log("NewGameOverviewScreen", "SEASON SCRIPT: " + script.getName());
                            Gdx.app.log("NewGameOverviewScreen", "DESCRIPTION  : " + script.getDescription());
                            
                            /**
                             * TEXT / SERIALIZED SCRIPT VERSION
                             */
                            if (script.getScriptValues() != null) {
                                Gdx.app.log("NewGameOverviewScreen", "SCRIPT VALUES: " + script.getScriptValues().size());
                                
                                for (Map.Entry<String, Object> entry : script.getScriptValues().entrySet()) {
                                    Gdx.app.log("NewGameOverviewScreen", "Script Key = " + entry.getKey() + ", Script Value = " + entry.getValue());
                                }
                            } else {
                                Gdx.app.error("NewGameOverviewScreen", "WARNING: Script values are null for script: " + script.getName());
                            }
                            
                            Gdx.app.log("NewGameOverviewScreen", "--------------------------");
                        }
                    }
                    
                    /**
                     * Add Season Scripts to SaveGame
                     */
                    currentGame.getGameScripts().addAll(startingSeason.getSeasonScripts());
                    Gdx.app.log("NewGameOverviewScreen", "Step 8: OK - Added " + startingSeason.getSeasonScripts().size() + " season scripts");
                    
                    Gdx.app.log("NewGameOverviewScreen", "Step 9: Initializing job system...");
                    try {
                        ((Futtoboru) game).initializeJobSystem();
                        Gdx.app.log("NewGameOverviewScreen", "Step 9: OK - Job system initialized");
                    } catch (Exception e) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR in Step 9 (initializeJobSystem):", e);
                        e.printStackTrace();
                        throw e; // Re-throw to be caught by outer catch
                    }
                    
                    Gdx.app.log("NewGameOverviewScreen", "Step 10: Creating initial job openings...");
                    try {
                        if (((Futtoboru) game).getJobManager() != null) {
                            ((Futtoboru) game).getJobManager().initializeJobOpenings();
                            Gdx.app.log("NewGameOverviewScreen", "Step 10: OK - Initial job openings created");
                        } else {
                            Gdx.app.error("NewGameOverviewScreen", "ERROR in Step 10: JobManager is NULL!");
                            throw new IllegalStateException("JobManager is null after initialization");
                        }
                    } catch (Exception e) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR in Step 10 (initializeJobOpenings):", e);
                        e.printStackTrace();
                        throw e; // Re-throw to be caught by outer catch
                    }
                    
                    // Generate attributes for all players (v1.0)
                    try {
                        Gdx.app.log("NewGameOverviewScreen", "Step 11: Generating player attributes...");
                        generateAttributesForAllPlayers(currentGame);
                        Gdx.app.log("NewGameOverviewScreen", "Step 11: OK - Player attributes generated");
                    } catch (Exception e) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR in Step 11 (generateAttributesForAllPlayers):", e);
                        e.printStackTrace();
                        // Don't throw - attributes can be generated later if needed
                    }
                    
                    // Create initial attribute snapshots for change tracking (v1.0)
                    try {
                        Gdx.app.log("NewGameOverviewScreen", "Step 12: Creating initial attribute snapshots...");
                        createInitialAttributeSnapshots(currentGame);
                        Gdx.app.log("NewGameOverviewScreen", "Step 12: OK - Initial snapshots created");
                    } catch (Exception e) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR in Step 12 (createInitialAttributeSnapshots):", e);
                        e.printStackTrace();
                        // Don't throw - snapshots can be created later
                    }
                    
                    // Create Welcome message (v2.0)
                    try {
                        Gdx.app.log("NewGameOverviewScreen", "Step 13: Creating welcome message...");
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
                                Gdx.app.log("NewGameOverviewScreen", "Step 13: OK - Welcome message created and delivered");
                                System.out.println("NewGameOverviewScreen: Created and delivered welcome message ID " + welcomeMessage.getId());
                                
                                // Verify message was added
                                int messageCount = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
                                System.out.println("NewGameOverviewScreen: Total messages after welcome: " + messageCount);
                            } else {
                                Gdx.app.error("NewGameOverviewScreen", "Step 13: WARNING - Welcome message is null");
                            }
                        } else {
                            Gdx.app.error("NewGameOverviewScreen", "Step 13: ERROR - MessageManager not available");
                        }
                    } catch (Exception e) {
                        Gdx.app.error("NewGameOverviewScreen", "ERROR in Step 13 (createWelcomeMessage):", e);
                        e.printStackTrace();
                        // Don't throw - welcome message is nice to have but not critical
                    }
                    
                    Gdx.app.log("NewGameOverviewScreen", "Step 14: Changing to GAME_SCREEN...");
                    ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);
                    Gdx.app.log("NewGameOverviewScreen", "=== START GAME COMPLETED SUCCESSFULLY ===");
                    
                } catch (Exception e) {
                    // Print to console (System.out) so it shows in debug window
                    System.err.println("========================================");
                    System.err.println("CRITICAL ERROR DURING GAME STARTUP!");
                    System.err.println("========================================");
                    System.err.println("Exception type: " + e.getClass().getName());
                    System.err.println("Exception message: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                    }
                    System.err.println("----------------------------------------");
                    e.printStackTrace(System.err);
                    System.err.println("========================================");
                    
                    // Also log via Gdx for consistency
                    Gdx.app.error("NewGameOverviewScreen", "CRITICAL ERROR during game startup!", e);
                    Gdx.app.error("NewGameOverviewScreen", "Exception type: " + e.getClass().getName());
                    Gdx.app.error("NewGameOverviewScreen", "Exception message: " + e.getMessage());
                    if (e.getCause() != null) {
                        Gdx.app.error("NewGameOverviewScreen", "Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
                    }
                    e.printStackTrace();
                    
                    // Print full stack trace to console
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    System.err.println("Full stack trace:");
                    Gdx.app.error("NewGameOverviewScreen", "Stack trace:");
                    for (int i = 0; i < Math.min(30, stackTrace.length); i++) {
                        System.err.println("  at " + stackTrace[i].toString());
                        Gdx.app.error("NewGameOverviewScreen", "  at " + stackTrace[i].toString());
                    }
                    System.err.println("========================================");
                    
                    // Don't throw - show error dialog instead to prevent window from closing
                    // TODO: Show error dialog to user
                    Gdx.app.error("NewGameOverviewScreen", "Game startup failed. Window will remain open for debugging.");
                    System.err.println("Game startup failed. Window will remain open for debugging.");
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
        stage.addActor(mainContainer);
    }

    @Override
    public void show() {

        // Add input capabilities
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        // Clear blit
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw
        stage.act();
        stage.draw(); 
    }

    @Override
    public void resize(int width, int height) {

        // Update viewport on resize to keep event coordinate for buttons/widgets
        stage.getViewport().update(width, height, true);
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
            startingClubSelectBox.setItems(new Club("UNAVAILABLE"));
            startingClubSelectBox.setDisabled(true);
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
            break;
        
        // deault to No Selectable club
        default:
            break;
        }
    }
    
    @Override
    public void hide() {
        
        System.out.println("HIDE WAS CALLED ON NEW GAME OVERVIEW SCREEN - DISPOSE");
        
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
                        attrGen.generatePlayerAttributes(player, player.getPerson(), gameDate);
                        playersGenerated++;
                    }
                }
            }
        }
        
        Gdx.app.log("NewGameOverviewScreen", "Generated attributes for " + playersGenerated + " players");
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
        
        Gdx.app.log("NewGameOverviewScreen", "Created " + snapshotsCreated + " initial attribute snapshots");
    }
}