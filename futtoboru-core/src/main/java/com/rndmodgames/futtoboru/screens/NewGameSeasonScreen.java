package com.rndmodgames.futtoboru.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Season Screen v1
 * 
 *  - TODO WIP
 *  
 *      - add/show URL Source to Season
 *      - add/show Starting Date to Season
 *      
 * 
 * @author Geomancer86
 */
public class NewGameSeasonScreen implements Screen {

    Game game;
    Stage stage;
    
    // Dynamic Components
    VisLabel selectedSeasonDescriptionLabel = new VisLabel("");
    VisLabel seasonCountriesNumber = new VisLabel("");
    VisLabel seasonStartDateValueLabel = new VisLabel("");
    LinkLabel seasonUrlSourceValueLabel = new LinkLabel("");
    
    VisSelectBox<Season> availableSeasonsSelectBox = null;
    
    //
    private Person selectedManager;
    private Season selectedSeason;
    
    public NewGameSeasonScreen(Game parent) {
        
        //
        this.game = parent;
        
        stage = new Stage(new ScreenViewport());
        
        VisTable mainContainer = new VisTable();
        mainContainer.pad(50).setFillParent(true);
    
        // 
        final VisTable table = new VisTable(true);
        
        //
        VisLabel seasonLabel = new VisLabel(LanguageModLoader.getValue("starting_season"));
        VisLabel seasonDescriptionLabel = new VisLabel(LanguageModLoader.getValue("season_description"));
        VisLabel seasonStartDateLabel = new VisLabel(LanguageModLoader.getValue("season_start_date")); 
        VisLabel seasonUrlSourceLabel = new VisLabel(LanguageModLoader.getValue("season_url_source"));
        VisLabel seasonCountriesLabel = new VisLabel(LanguageModLoader.getValue("season_countries"));
        
        // Get All Available Seasons
        List<Season> allSeasons = DatabaseLoader.getInstance().getSeasons();
        
        // Seasons Select Box
        availableSeasonsSelectBox = new VisSelectBox<>();
        availableSeasonsSelectBox.setItems(allSeasons.toArray(new Season[allSeasons.size()]));

        //
        availableSeasonsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                //
                if (availableSeasonsSelectBox.getSelected() != null) {
                    
                    //
                    selectedSeason = availableSeasonsSelectBox.getSelected();
                    
                    //
                    updateDynamicComponents();
                }
            }
        });
        
        // if there is only a single season select it automatically
        if (allSeasons.size() == 1) {
            selectedSeason = allSeasons.get(0);
        }
        
        // set the default values as the Season will be selected automatically
        updateDynamicComponents();
        
        // Season
        table.row();
        table.add(seasonLabel);
        table.add(availableSeasonsSelectBox);
        
        // Season Description
        table.row();
        table.add(seasonDescriptionLabel);
        table.add(selectedSeasonDescriptionLabel);
        
        // Season Start Date
        table.row();
        table.add(seasonStartDateLabel);
        table.add(seasonStartDateValueLabel);
        
        // Season Source URL
        table.row();
        table.add(seasonUrlSourceLabel);
        table.add(seasonUrlSourceValueLabel);
        
        // Season Countries
        table.row();
        table.add(seasonCountriesLabel);
        table.add(seasonCountriesNumber);
        
        // Season Leagues
        // TODO: show
        
        // Season Teams
        // TODO: show
        
        // Season Competitions
        // TODO: show
        
        // Season Rules
        // TODO: show
        
        /**
         * Load Existing Manager - Create New Manager
         */
        VisLabel managerLabel = new VisLabel(LanguageModLoader.getValue("manager"));
        
        //
        int existingManagers = SaveLoadSystem.listSavedManagers().size(); 
        
        table.row();
        table.add(managerLabel);
        
        if (existingManagers > 0) {
        
            // EXISTING MANAGERS LIST
            List<String> savedManagers = SaveLoadSystem.listSavedManagers();
            
            //
            final VisSelectBox<String> savedManagersSelectBox = new VisSelectBox<>();
            savedManagersSelectBox.setItems(savedManagers.toArray(new String[allSeasons.size()]));
            
            // SELECT MANAGER BUTTON
            final VisTextButton selectManager = new VisTextButton(LanguageModLoader.getValue("select_manager"));
            
            selectManager.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    
                    //
                    System.out.println("MANAGER SELECTED!");
                    
                    // Set Selected Manager
                    Person manager = SaveLoadSystem.loadPersonFromFile(savedManagersSelectBox.getSelected());
                    
                    //
                    selectedManager = manager;
                }
            });
            
            // automatically select first manager on the list
            selectedManager = SaveLoadSystem.loadPersonFromFile(savedManagersSelectBox.getSelected());
            
            table.add(savedManagersSelectBox);
            table.add(selectManager);
        }
            
        // CREATE MANAGER BUTTON
        final VisTextButton createManager = new VisTextButton(LanguageModLoader.getValue("create_manager"));
        createManager.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                System.out.println("SWITCHING TO CREATE MANAGER SCREEN");
                
                /**
                 * Set the Selected Person as Game Owner
                 */
                SaveGame currentGame = new SaveGame();

                ((Futtoboru) game).setCurrentGame(currentGame);
                
                /**
                 * Redirect to NEW_MANAGER_SCREEN
                 */
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_MANAGER_SCREEN);
            }
        });
        
        table.add(createManager);
        
        /**
         * NEXT/NEW GAME GAME BUTTON
         */
        final VisTextButton startGame = new VisTextButton(LanguageModLoader.getValue("start"));
        startGame.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                System.out.println("LOADING EXISTING MANAGER FROM FILE - CHANGING TO NEW GAME SETUP SCREEN");
                
                /**
                 * Set the Selected Person as Game Owner
                 */
                SaveGame currentGame = new SaveGame();

                //
                ((Futtoboru) game).setCurrentGame(currentGame);
                
                // set game owner
                ((Futtoboru) game).getCurrentGame().setOwner(selectedManager);
                
                // Redirect to New Game Setup Screen
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SETUP_SCREEN, selectedSeason, new ArrayList<>());
            }
        });
        
        //
        table.add(startGame).pad(5);
        
        // 
        mainContainer.add(table);
        
        // Add Settings Screen Main Container to Stage
        stage.addActor(mainContainer);
    }
    
    /**
     * 
     */
    public void updateDynamicComponents() {
        
        // update season description label
        selectedSeasonDescriptionLabel.setText(availableSeasonsSelectBox.getSelected().getDescription());
        
        // update season countries label
        seasonCountriesNumber.setText(availableSeasonsSelectBox.getSelected().getCountries().size());
        
        // update season start date label
        seasonStartDateValueLabel.setText(DatabaseLoader.formatter.format(availableSeasonsSelectBox.getSelected().getStartDate()));
        
        // update season source url label
        seasonUrlSourceValueLabel.setText("Wikipedia");
        seasonUrlSourceValueLabel.setUrl(availableSeasonsSelectBox.getSelected().getUrlSource());
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}
