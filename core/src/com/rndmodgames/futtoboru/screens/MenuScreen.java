package com.rndmodgames.futtoboru.screens;

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
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.components.QuitGameButton;
import com.rndmodgames.components.SettingsButton;
import com.rndmodgames.futtoboru.dialogs.LoadGameDialog;
import com.rndmodgames.futtoboru.game.Futtuboru;
import com.rndmodgames.localization.LanguageModLoader;

public class MenuScreen implements Screen {

	Game game;
	Stage stage;
	SpriteBatch batch;
	Texture img;
	
	/**
	 * Load Game Dialog
	 */
	LoadGameDialog loadGameDialog = null;
	
	/**
	 * Main Menu Screen
	 * 
	 * 		- This will be the first Screen Loaded
	 * 
	 * @param parent
	 */
	public MenuScreen(Game parent) {
		
		this.game = parent;
		
		stage = new Stage(new ScreenViewport());

		final VisTable table = new VisTable();
		table.setFillParent(true);
		
		/**
		 * New Game: 
		 * 	- New Game Screen
		 */
		final VisTextButton newGameButton = new VisTextButton(LanguageModLoader.getValue("new_game"));
		
		newGameButton.addCaptureListener(new InputListener() {
			
		    @Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				
			    if (newGameButton.isPressed()) {

					((Futtuboru)game).changeScreen(Futtuboru.NEW_GAME_SCREEN);
				}
			}
		});
		
		/**
		 * Load Game:
		 * 	- Load Game Screen
		 */
		final VisTextButton loadGameButton = new VisTextButton(LanguageModLoader.getValue("load_game"));
		
		loadGameButton.addCaptureListener(new InputListener() {
			
		    @Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				
			    if (loadGameButton.isPressed()) {
					
			        if (loadGameDialog == null) {
			            
			            loadGameDialog = new LoadGameDialog(game);
			            
			        }
			        
			        loadGameDialog.show(stage);
				}
			}
		});
		
		/**
		 * Settings Button
		 */
		final VisTextButton settingsButton = new SettingsButton(game);

		/**
		 * Exit Button
		 */
		final VisTextButton exitButton = new QuitGameButton(game);
		
		// 
		table.row();
		table.add(newGameButton).fill();
		
		//
		table.row();
		table.add(loadGameButton).fill();
		
		//
		table.row();
		table.add(settingsButton).fill();
		
		//
		table.row();
		table.add(exitButton).fill();
		
		stage.addActor(table);
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
        
	    System.out.println("HIDE WAS CALLED ON MENU SCREEN - DISPOSE");
	    
        // Hide will be called after switching to a separate screen
        dispose();
	}

	@Override
	public void dispose() {

		stage.dispose();
	}
}