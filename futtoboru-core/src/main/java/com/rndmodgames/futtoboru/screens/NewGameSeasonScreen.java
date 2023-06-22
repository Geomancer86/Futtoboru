package com.rndmodgames.futtoboru.screens;

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
 *  - 
 * 
 * @author Geomancer86
 */
public class NewGameSeasonScreen implements Screen {

    Game game;
    Stage stage;
    
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
        VisLabel seasonCountriesLabel = new VisLabel(LanguageModLoader.getValue("season_countries"));
        VisLabel seasonCountriesNumber = new VisLabel("0");
        
        // Get All Available Seasons
        List<Season> allSeasons = DatabaseLoader.getInstance().getSeasons();
        
        // Seasons Select Box
        final VisSelectBox<Season> availableSeasonsSelectBox = new VisSelectBox<>();
        availableSeasonsSelectBox.setItems(allSeasons.toArray(new Season[allSeasons.size()]));

        // Selected Season Description Label
        VisLabel selectedSeasonDescriptionLabel = new VisLabel("");        
        
        //
        availableSeasonsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                //
                if (availableSeasonsSelectBox.getSelected() != null) {
                    
                    // update season description label
                    selectedSeasonDescriptionLabel.setText(availableSeasonsSelectBox.getSelected().getDescription());
                    
                    // update season countries label
                    seasonCountriesNumber.setText(availableSeasonsSelectBox.getSelected().getCountries().size());
                }
            }
        });
        
        // set the default values as the Season will be selected automatically
        selectedSeasonDescriptionLabel.setText(availableSeasonsSelectBox.getSelected().getDescription());
        seasonCountriesNumber.setText(availableSeasonsSelectBox.getSelected().getCountries().size());
        
        // Season
        table.row();
        table.add(seasonLabel);
        table.add(availableSeasonsSelectBox);
        
        // Season Description
        table.row();
        table.add(seasonDescriptionLabel);
        table.add(selectedSeasonDescriptionLabel);
        
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
                
                // set game owner
//                Person gameOwner = SaveLoadSystem.loadPersonFromFile(savedGame);
//                currentGame.setOwner(gameOwner);
                
                ((Futtoboru) game).setCurrentGame(currentGame);
                
                /**
                 * Redirect to New Game Setup Screen
                 */
//                ((Futtoboru) game).changeScreen(Futtoboru.NEW_MANAGER_SCREEN);
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SETUP_SCREEN);
            }
        });
        
        //
        table.add(startGame).pad(5);
        
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
