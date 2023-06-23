package com.rndmodgames.components.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Home Button v1
 * 
 *  - Switches to Home Screen
 * 
 * @author Geomancer86
 */
public class HomeButton extends VisTextButton {

    public HomeButton(MainMenuManager menuManager) {
        
        // TODO: i18n
        super(LanguageModLoader.getValue("home"));
        
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.HOME_SCREEN);
            }
        });
    }
}
