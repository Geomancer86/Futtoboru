package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

/**
 * Club Browser Button v1
 * 
 * Switches to Club Browser Screen
 * 
 * @author Geomancer86
 */
public class ClubBrowserButton extends VisTextButton {

    public ClubBrowserButton(MainMenuManager menuManager) {
        
        super("Club Browser");
        
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.CLUB_BROWSER_SCREEN);
            }
        });
    }
}


