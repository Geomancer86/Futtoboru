package com.rndmodgames.futtoboru.screens;

import java.util.ArrayList;
import java.util.List;

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
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.DataBase;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Overview Screen v1
 * 
 *  - List the Selected Active Countries
 *  - Selectbox with Lowest Active League for each Country
 *  - Game Start Date
 *  - Database Size
 *  
 *  - Advanced Options:
 *      - Use Fake Players
 *      - Disable Player Atribute Masking
 *      
 *  - New Game Details
 *      - Approximate Player Count
 *      - Computer Performance
 *      - Estimated Game Speed
 * 
 * SOURCE SCREENS:
 *  - https://lparchive.org/Football-Manager-2014/Update%2001/2-Football_Manager_2014__New_Game_Choose_Nations.png
 *  - https://lparchive.org/Football-Manager-2014/Update%2001/3-Football_Manager_2014__New_Game_Choose_Nations-2.png
 *  - https://lparchive.org/Football-Manager-2014/Update%2001/4-Game___Untitled_Game___Game_Add_New_Head_Coach.png
 *  - https://lparchive.org/Football-Manager-2014/Update%2001/5-Game___Untitled_Game___Game_Add_New_Head_Coach-2.png
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
    
    Integer estimatedPlayers = 0;
    
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
         *  - Number of Countries x Number of Leagues x Number of Teams x 99 Players
         *  - 
         */
        final VisLabel estimatedPlayerCountLabel = new VisLabel(LanguageModLoader.getValue("estimate_player_count"));
        
        int activeLeagues = 0;
        int activeTeams = 0;
        
        for (Country country : selectedCountries) {
            
            // Count Active Teams
            activeTeams += DatabaseLoader.getClubsByCountry().get(country.getId()).size();
            
            VisLabel countryLabel = new VisLabel(country.getCommonName());
            
            /**
             * Country Label
             */
            selectedCountriesTable.row();
            selectedCountriesTable.add(countryLabel);
        }

        /**
         * New Game Details Table
         * 
         * TODO: Available Leagues & Teams
         */
        final VisTable newGameDetailsTable = new VisTable(true);

        VisLabel availableLeaguesLabel = new VisLabel(LanguageModLoader.getValue("active_leagues"));
        VisLabel availableTeamsLabel = new VisLabel(LanguageModLoader.getValue("active_teams"));
        
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
        
        // hacky TODO set first selected automatically 
        // hacky TODO set first selected automatically 
//        professionsSelectBox.setSelectedIndex(1);
//        professionsSelectBox.setSelectedIndex(0);
        
        //
        gameStartConfigurationTable.add(gameStartDateLabel);
        
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
 
                /**
                 * Set the Selected Countries and Redirect to Main Game Screen:
                 * 
                 *      - Selected Countries        [DONE]
                 *      - Selected Lowest Leagues   [TODO]
                 *      - Selected Profession       [DONE]
                 *      - Starting Country          [DONE]
                 */
                SaveGame currentGame = ((Futtoboru) game).getCurrentGame();
                
                //
                currentGame.setSelectedCountries(selectedCountries);
                currentGame.getOwner().setPrimaryProfession(primaryProfession);
                currentGame.getOwner().setCurrentCountry(startingCountry);
                
                //
                if (startingSeason != null) {
                    
                    System.out.println("DATE DEBUG 1:" + startingSeason.getStartDate());
                    currentGame.setGameStartDate(startingSeason.getStartDate());
                    currentGame.setGameDate(startingSeason.getStartDate());
                }
                
                ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);
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
    
    /**
     * Estimate the number of players in the database depending on countries and leagues selected
     */
    private void updateEstimatedPlayerCount() {

        // reset to zero to avoid adding up
        estimatedPlayers = 0;
        
        for (Country country : selectedCountries) {

            if (country.getLowestActiveLeague() != null) {
                
                System.out.println(country.getCommonName() + ", Lowest Active League: " + country.getLowestActiveLeague().getName() + ", level: "
                        + country.getLowestActiveLeague().getLevel());

                // update players x teams x divisions x countries
                estimatedPlayers += (99 * country.getLowestActiveLeague().getLevel() * 22); // 99 players * 22 teams per division
            }
        }
        
        estimatedPlayerCountValueLabel.setText(estimatedPlayers + "");
        
        System.out.println("NEW ESTIMATE PLAYER COUNT: " + estimatedPlayers);
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
}