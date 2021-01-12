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
import com.rndmodgames.futtoboru.game.Futtuboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Setup Screen
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
    
    List<Country> selectedCountries = new ArrayList<>();
    
    //
    public NewGameSetupScreen(Game parent, List<Country> selectedCountries) {
        
        this.game = parent;
        
        // Keep track of selected Countries
        this.selectedCountries = selectedCountries;

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
            
            activeCountriesTable.row();
            
            VisLabel continentLabel = new VisLabel(continent.getName());
            
            // Add Continent Name Label
            activeCountriesTable.add(continentLabel);
            
            /**
             * Show only the Countries with Active Leagues
             */
            for (final Country country : DatabaseLoader.getInstance().getCountriesByContinent(continent)) {

                if (DatabaseLoader.getInstance().getLeaguesByCountry(country).isEmpty()) {
                    
                    /**
                     * Ignore this Country as it doesn't have Active Leagues
                     */
                    continue;
                }
                
                VisLabel countryLabel = new VisLabel(country.getCommonName());
                
                final VisCheckBox countryCheckBox = new VisCheckBox("");
                
                /**
                 * TODO: set checked on DEFAULT/RECOMMENDED Countries
                 * TODO: add Country Flag ICON
                 */
                if (this.selectedCountries.contains(country)) {
                    
                    countryCheckBox.setChecked(true);
                }
                
                /**
                 * Set Checked to Default Countries
                 * 
                 * NOTE: this need to be done only once while setting up this screen, and not after switching screens back and forth with the selected countries list
                 */
                if (selectedCountries.isEmpty()) {
                    
                    if (country.getDefaultCountry()) {
                        
                        countryCheckBox.setChecked(true);
                        
                        // add to the selected list automatically
                        selectedCountries.add(country);
                    }
                }

                countryCheckBox.addListener(new ChangeListener() {
                    
                    @Override
                    public void changed (ChangeEvent event, Actor actor) {
                        
                        /**
                         * Add to selected countries list on check
                         *  - remove on uncheck
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

                activeCountriesTable.row();
                activeCountriesTable.add(countryCheckBox);
                activeCountriesTable.add(countryLabel).left();
            }
            
            // Add Horizontal Separator
            activeCountriesTable.row();
            activeCountriesTable.addSeparator();
            
            // 
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
                ((Futtuboru) game).changeScreen(Futtuboru.NEW_GAME_OVERVIEW_SCREEN, selectedCountries);
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