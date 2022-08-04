package com.rndmodgames.futtoboru.screens;

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
        
        // Seasons Select Box
        final VisSelectBox<Season> availableSeasonsSelectBox = new VisSelectBox<Season>();
//        availableResolutionsSelectBox.setItems(availableResolutions.toArray(new String[availableResolutions.size()]));
        
        // Set resolution from saved settings (or default)
//        availableResolutionsSelectBox.setSelected(Futtoboru.RESOLUTION);

        availableSeasonsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                // The selected resolution
//                String res = availableResolutionsSelectBox.getSelected();
//                
//                // Update Game Resolution
//                Futtoboru.RESOLUTION = res;
//                
//                // Update User Preferences
//                ((Futtoboru) game).updateScreenResolution(res);
//                
//                // Set to Windowed
//                fullScreenCheckBox.setChecked(false);
//                
//                if (Futtoboru.FULLSCREEN) {
//                    ((Futtoboru) game).toggleFullscreen();
//                } else {
//                    ((Futtoboru) game).setScreen();
//                }
            }
        });
        
        //
        table.row();
        table.add(seasonLabel);
        table.add(availableSeasonsSelectBox);
        
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
