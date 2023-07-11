package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Finances Button
 * 
 * @author Geomancer86
 */
public class FinancesButton extends VisTextButton {

    public FinancesButton(MainMenuManager menuManager) {
        
        // TODO: i18n
        super(LanguageModLoader.getValue("finances"));
        
        //
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.FINANCES_SCREEN);
            }
        });
    }
}