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
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Continent;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Setup Screen v1
 * 
 * - Choose Starting Season
 *      - Alpha 1888-89 Season      [WIP]
 * 
 * - Choose Nations
 * 
 * @author Geomancer86
 *
 */
public class NewGameSetupScreen implements Screen {

    Game game;
    Stage stage;
    SpriteBatch batch;
    Texture img;
    
    //
    List<Country> selectedCountries = new ArrayList<>();
    Season startingSeason = null;
    
    //
    public NewGameSetupScreen(Game parent, Season startingSeason, List<Country> selectedCountries) {
        
        this.game = parent;
        
        // Keep track of selected Countries
        this.selectedCountries = selectedCountries;
        
        // Keep track of starting season
        this.startingSeason = startingSeason;

        stage = new Stage(new ScreenViewport());
        
        VisTable mainContainer = new VisTable();
        mainContainer.pad(50).setFillParent(true);
    
        // 
        final VisTable table = new VisTable(true);
        
        /**
         * Choose Countries Label
         * 
         *  - Load the Available Leagues from /mods/leagues Folder
         *      - Sort Countries by Continent Alphabetically
         *      - Show Countries sorted by Continent
         *          - Selected Country Checkmark
         *          - Country Flag
         *          - Country Name
         */
        VisLabel chooseCountriesLabel = new VisLabel(LanguageModLoader.getValue("choose_countries"));
        
        /**
         * Active Countries Label
         */
        final VisTable activeCountriesTable = new VisTable(true);
        
        /**
         * Show only the Continents with Active Countries
         */
        for (Continent continent : DatabaseLoader.getInstance().getContinents()) {

//          System.out.println("Continent Details: " + continent.getName());
            
            // An active Continent will have Countries with active Clubs
            boolean activeContinent = false;
            boolean activeCountry = false;

            /**
             * Show only the Countries with Active Clubs
             */
            for (final Country country : DatabaseLoader.getInstance().getCountriesByContinent(continent)) {
                
                //
                if (DatabaseLoader.getInstance().getClubsByCountry(country).isEmpty()) {
                    
                    // System.out.println("Country don't have active Clubs!");
                    
                } else {
                    
                    // ADD COMPONENTS
                    System.out.println("Continent Details: " + continent.getName());
                    System.out.println("Country Details: " + country.getCommonName());
                    System.out.println("Adding " + DatabaseLoader.getInstance().getClubsByCountry(country).size() + " Country Clubs!");
                 
                    // Set Continent as active
                    activeContinent = true;
                }
            }
            
            // Add a new Row if this Continent is active
            if (activeContinent) {
                
                activeCountriesTable.row();
                activeCountriesTable.add(new VisLabel(continent.getName()));
            }
            
            /**
             * Add the Active Countries to Pick
             */
            for (final Country country : DatabaseLoader.getInstance().getCountriesByContinent(continent)) {
                
                if (DatabaseLoader.getInstance().getClubsByCountry(country).isEmpty()) {
                    
                    // System.out.println("Country don't have active Clubs!");
                    
                } else {
                    
                    //
                    VisLabel countryLabel = new VisLabel(country.getCommonName());
                    final VisCheckBox countryCheckBox = new VisCheckBox("");
                    
                    // SET DEFAULT CHECKED COUNTRIES
                    if (this.selectedCountries.contains(country)) {
                  
                        countryCheckBox.setChecked(true);
                    }
                    
                    countryCheckBox.addListener(new ChangeListener() {

                        @Override
                        public void changed(ChangeEvent event, Actor actor) {

                            /**
                             * Add to selected countries list on check - remove on uncheck
                             */
                            if (countryCheckBox.isChecked()) {

                                if (!selectedCountries.contains(country)) {

                                    selectedCountries.add(country);
                                }

                            } else {

                                selectedCountries.remove(country);
                            }
                        }
                    });
                    
                    // TODO: unhardcode this default selection
                    countryCheckBox.setChecked(true);
                    
                    activeCountriesTable.row();
                    activeCountriesTable.add(countryCheckBox);
                    activeCountriesTable.add(countryLabel).left();
                }
            }
            
            /**
             * Add Horizontal Separator
             * 
             * TODO: only if active continents > 1
             */
            if (activeContinent) {
                activeCountriesTable.row();
                activeCountriesTable.addSeparator();
            }
        }

        /**
         * Next Screen
         */
        final VisTextButton nextButton = new VisTextButton(LanguageModLoader.getValue("next"));
        
        nextButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
 
                System.out.println("SELECTED COUNTRIES SIZE: " + selectedCountries.size());
                
                /**
                 * Move to New Game Overview Screen
                 */
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_OVERVIEW_SCREEN, startingSeason, selectedCountries);
            }
        });
        
        // active nations label
        mainContainer.row();
        mainContainer.add(chooseCountriesLabel);
        
        // active nations by continent table
        mainContainer.row();
        mainContainer.add(activeCountriesTable);
        
        mainContainer.row();
        mainContainer.add(nextButton).right();

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

    @Override
    public void hide() {
        
        System.out.println("HIDE WAS CALLED ON NEW GAME SETUP SCREEN - DISPOSE");
        
        // Hide will be called after switching to a separate screen
        dispose();
    }

    @Override
    public void dispose() {

        // Dispose on screen change
        stage.dispose();
    }
}