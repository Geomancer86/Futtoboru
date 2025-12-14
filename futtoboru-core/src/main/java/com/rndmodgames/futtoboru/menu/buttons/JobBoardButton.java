package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

/**
 * Job Board Button v1
 * 
 * Switches to Job Board Screen
 * 
 * @author Geomancer86
 */
public class JobBoardButton extends VisTextButton {

    public JobBoardButton(MainMenuManager menuManager) {
        
        super("Job Board");
        
        this.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                menuManager.setActiveMainScreen(MainMenuManager.JOB_BOARD_SCREEN);
            }
        });
    }
}

