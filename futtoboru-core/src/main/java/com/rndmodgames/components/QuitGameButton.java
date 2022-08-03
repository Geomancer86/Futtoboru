package com.rndmodgames.components;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.localization.LanguageModLoader;

public class QuitGameButton extends VisTextButton {

	/**
	 * This Button will exit the Game
	 * 
	 * @param game
	 * @param dispose
	 */
	public QuitGameButton(final Game game) {
		
		super(LanguageModLoader.getValue("quit_game"));
		
		this.addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (isPressed()){
					
					// TODO: validate with Dialog?
					Gdx.app.exit();
				}
			}
		});
	}
	
	// 
	private QuitGameButton(String text) {
		super(text);
	}
}