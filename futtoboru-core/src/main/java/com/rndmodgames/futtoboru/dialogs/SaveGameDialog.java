package com.rndmodgames.futtoboru.dialogs;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Save Game Dialog v1
 * 
 * @author Geomancer86
 */
public class SaveGameDialog extends VisDialog {

    public SaveGameDialog(SaveGame saveGame, VisTextButton continueGameButton) {

        super(LanguageModLoader.getValue("save_game"));

        /**
         * List existing saved games, game owner and save game date & time
         */
        
        final VisTextButton saveGameButton = new VisTextButton(LanguageModLoader.getValue("save_game"));
        final VisTextButton cancelButton = new VisTextButton(LanguageModLoader.getValue("cancel"));
        
        saveGameButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                /**
                 * SAVE GAME FILE
                 * 
                 * TODO: input name
                 * TODO: check for existing save game overwrite
                 * TODO: rolling save game file
                 * TODO: confirmation dialog/toast/notification
                 * 
                 */
                if (saveGame != null) {
                    
                    // Set to SAVED on success
                    saveGame.setIsSaved(true);
                    
                    /**
                     * TODO: avoid renaming savegame!
                     * 
                     *  save game format:
                     *      
                     *      - manager name/last name
                     *      - in game date
                     *      
                     */
                    SaveLoadSystem.saveGame(saveGame, "savegame");

                    // Enable the Continue Game Button
                    continueGameButton.setDisabled(false);
                    
                    //
                    hide();
                }
            }
        });
        
        // Hide screen on Cancel
        cancelButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                hide();
            }
        });

        this.row();
        this.add(cancelButton);
        this.add(saveGameButton);
    }
}