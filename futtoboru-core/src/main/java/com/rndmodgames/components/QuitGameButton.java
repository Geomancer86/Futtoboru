package com.rndmodgames.components;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.dialogs.ConfirmDialog;
import com.rndmodgames.localization.LanguageModLoader;

public class QuitGameButton extends VisTextButton {

	/**
	 * This Button will exit the Game, behind a Yes/Cancel confirmation.
	 *
	 * <p>Fixed 2026-07-19 (pre-game menus UX audit, P0): this used to call
	 * {@code Gdx.app.exit()} directly on the first click with no confirmation at all.
	 * 
	 * @param game
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
				if (isPressed() && getStage() != null){

					new ConfirmDialog(
							LanguageModLoader.getValue("quit_game"),
							LanguageModLoader.getValue("quit_game_confirm_message"),
							Gdx.app::exit
					).show(getStage());
				}
			}
		});
	}
	
	// 
	private QuitGameButton(String text) {
		super(text);
	}
}