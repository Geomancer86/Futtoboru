package com.rndmodgames.components;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * This Button will Close/Hide a Window
 * 
 * @author Geomancer86
 */
public class CloseWindowButton extends VisTextButton{

	public CloseWindowButton(final Window parent) {
		
		super(LanguageModLoader.getValue("close"));
		
		this.addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				
				if (isPressed()){
					
					parent.setVisible(false);
				}
			}
		});
	}
}
