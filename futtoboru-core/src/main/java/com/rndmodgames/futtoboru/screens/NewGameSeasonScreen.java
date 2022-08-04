package com.rndmodgames.futtoboru.screens;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Season Screen v1
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
        
        // Season Teams
        
        // Season Competitions
        
        // Season Rules
        
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
