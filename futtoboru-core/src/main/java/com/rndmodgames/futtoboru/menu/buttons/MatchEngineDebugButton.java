package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

/**
 * Match Engine Debug Button v1
 * 
 *  - Switches to Match Engine Debug Screen
 *  - Development/Testing screen for match engine
 * 
 * @author Geomancer86
 */
public class MatchEngineDebugButton extends VisTextButton {

    public MatchEngineDebugButton(MainMenuManager menuManager) {
        
        // TODO: i18n
        super("Match Engine Debug");
        
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.MATCH_ENGINE_DEBUG_SCREEN);
            }
        });
    }
}
