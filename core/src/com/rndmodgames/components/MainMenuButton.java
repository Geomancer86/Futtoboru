package com.rndmodgames.components;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.game.Futtuboru;
import com.rndmodgames.localization.LanguageModLoader;

public class MainMenuButton extends VisTextButton {

	/**
	 * This Button will switch to the Main Menu Screen
	 * 
	 * @param game
	 * @param dispose
	 */
	public MainMenuButton(final Game game) {
		
		super(LanguageModLoader.getValue("main_menu"));
		
		this.addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (isPressed()){
					
				    ((Futtuboru) game).changeScreen(Futtuboru.MENU_SCREEN);
				}
			}
		});
	}
	
	// 
	private MainMenuButton(String text) {
		super(text);
	}
}