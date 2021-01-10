package com.rndmodgames.futtoboru.screens;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.game.Futtuboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Screen v1
 * 
 *  - Add New Manager
 *  - Select Existing Manager
 *  - Select Playable Regions/Leagues
 *  - Select other settings
 * 
 * @author Geomancer86
 * 
 */
public class NewGameScreen implements Screen {

	Game game;
	Stage stage;
	SpriteBatch batch;
	Texture img;

	public NewGameScreen (Game parent) {

		this.game = parent;
		
		stage = new Stage(new ScreenViewport());
		
		VisTable mainContainer = new VisTable();
		mainContainer.pad(50).setFillParent(true);
    
		// 
		final VisTable table = new VisTable();
		
		/**
		 * Check if Managers exist in File System
		 */
        List<String> savedGames = SaveLoadSystem.listSavedManagers();
		
		/**
		 * Create New Manager
		 */
        final VisTextButton addManagerButton = new VisTextButton(LanguageModLoader.getValue("add_manager"));
        
        addManagerButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                /**
                 *  - Switch to New Manager Screen
                 */
                ((Futtuboru) game).changeScreen(Futtuboru.NEW_MANAGER_SCREEN);
            }
        });

        table.add(addManagerButton);
        
		/**
		 * Select Existing manager
		 */
        
        VisScrollPane scrollPane = new VisScrollPane(table);
        
        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        
        for (String savedGame : savedGames) {
            
            final String savedManagerName = savedGame.split("/")[savedGame.split("/").length - 1];
            
            table.row().grow();
            table.add(new VisLabel(savedManagerName)).pad(5);
            
            final VisTextButton loadGame = new VisTextButton(LanguageModLoader.getValue("load"));
            
            loadGame.addCaptureListener(new InputListener() {
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
                    Person gameOwner = SaveLoadSystem.loadPersonFromFile(savedGame);
                    currentGame.setOwner(gameOwner);
                    
                    ((Futtuboru) game).setCurrentGame(currentGame);
                    
                    /**
                     * Redirect to New Game Setup Screen
                     */
                    ((Futtuboru) game).changeScreen(Futtuboru.NEW_GAME_SETUP_SCREEN);
                }
            });
            
            table.add(loadGame).pad(5);
        }
		
		// add
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

	/**
	 * Utility method used to return all the available World Size on 
	 */
	
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
        
	    System.out.println("HIDE WAS CALLED ON NEW GAME SCREEN - DISPOSE");
	    
        // Hide will be called after switching to a separate screen
        dispose();
	}

	@Override
	public void dispose() {

		// Dispose on screen change
		stage.dispose();
	}
}