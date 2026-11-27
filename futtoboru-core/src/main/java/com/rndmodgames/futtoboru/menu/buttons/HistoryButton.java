package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * History Button (U-07): champions, cup winners and final tables
 * 
 * @author Geomancer86
 */
public class HistoryButton extends VisTextButton {

    public HistoryButton(MainMenuManager menuManager) {
        
        super(LanguageModLoader.getValue("history"));
        
        //
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.HISTORY_SCREEN);
            }
        });
    }
}