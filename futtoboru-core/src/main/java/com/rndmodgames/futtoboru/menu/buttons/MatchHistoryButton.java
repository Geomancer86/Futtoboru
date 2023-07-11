package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Match History Button
 * 
 * @author Geomancer86
 */
public class MatchHistoryButton extends VisTextButton {

    public MatchHistoryButton(MainMenuManager menuManager) {
        
        // TODO: i18n
        super(LanguageModLoader.getValue("match_history"));
        
        //
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.MATCH_HISTORY_SCREEN);
            }
        });
    }
}