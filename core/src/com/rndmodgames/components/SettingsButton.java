package com.rndmodgames.components;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.screens.SettingsScreen;
import com.rndmodgames.localization.LanguageModLoader;

public class SettingsButton extends VisTextButton {

	/**
	 * This Button will call the Settings Screen
	 * 
	 * @param game
	 * @param dispose
	 */
	public SettingsButton(final Game game) {
		
		super(LanguageModLoader.getValue("settings_menu"));
		
		this.addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (isPressed()){
					
					game.setScreen((Screen) new SettingsScreen(game));
				}
			}
		});
	}
	
	// 
	private SettingsButton(String text) {
		super(text);
	}
}