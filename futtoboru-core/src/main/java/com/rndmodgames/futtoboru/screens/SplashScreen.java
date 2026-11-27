package com.rndmodgames.futtoboru.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

public class SplashScreen implements Screen {

	Game game;
	
	private float timeToShowSplashScreen = 0.01f; // seconds
	
	public SplashScreen(Game game) {
		this.game = game;
	}
	
	@Override
	public void show() {

	}
	
	@Override
	public void render(float delta) {

		UiTheme.clearScreen("bg.splash");
		
		/**
		 * Renders the Splash Screen
		 * 
		 * TODO: might start loading the game here
		 */
//		((Futtuboru)game).batch.begin();
//		((Futtuboru)game).batch.draw(((Futtuboru)game).img, 0, 0);
//		((Futtuboru)game).batch.end();
		
		// remove delta from time
		timeToShowSplashScreen -= delta;
		
		if(timeToShowSplashScreen <= 0){
		    
			// splash time is up
			// tell parent to change screen
			((Futtoboru)game).changeScreen(Futtoboru.MENU_SCREEN);
		}
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
		
        Log.info(Log.SYSTEM_INIT, "HIDE WAS CALLED ON SPLASH SCREEN - DISPOSE");
        
        // Hide will be called after switching to a separate screen
        this.dispose();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
//		batch.dispose();
//		img.dispose();
	}
}