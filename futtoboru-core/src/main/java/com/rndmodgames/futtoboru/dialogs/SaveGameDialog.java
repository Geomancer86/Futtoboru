package com.rndmodgames.futtoboru.dialogs;

import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Save Game Dialog — named save with confirmation (PL-7).
 */
public class SaveGameDialog extends VisDialog {

    public SaveGameDialog(SaveGame saveGame, VisTextButton continueGameButton) {
        super(LanguageModLoader.getValue("save_game"));

        final VisTextField nameField = new VisTextField(defaultSaveName(saveGame));
        final VisLabel statusLabel = new VisLabel("");
        final VisTextButton saveGameButton = new VisTextButton(LanguageModLoader.getValue("save_game"));
        final VisTextButton cancelButton = new VisTextButton(LanguageModLoader.getValue("cancel"));

        saveGameButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (event.getButton() != 0 || saveGame == null) {
                    return;
                }
                String name = SaveLoadSystem.normalizeSaveBasename(nameField.getText());
                SaveLoadSystem.saveGame(saveGame, name);
                saveGame.setIsSaved(true);
                statusLabel.setText("Saved as " + name + ".json");
                if (continueGameButton != null) {
                    continueGameButton.setDisabled(false);
                }
            }
        });

        cancelButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                hide();
            }
        });

        row();
        add(new VisLabel("Save name:")).pad(5);
        row();
        add(nameField).width(280).pad(5);
        row();
        add(statusLabel).pad(5);
        row();
        add(cancelButton).pad(5);
        add(saveGameButton).pad(5);
    }

    private static String defaultSaveName(SaveGame saveGame) {
        if (saveGame == null || saveGame.getGameDate() == null) {
            return "savegame";
        }
        String date = saveGame.getGameDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (saveGame.getCurrentClub() != null && saveGame.getCurrentClub().getName() != null) {
            String club = saveGame.getCurrentClub().getName().replaceAll("[^A-Za-z0-9_-]", "_");
            return club + "_" + date;
        }
        return "savegame_" + date;
    }
}
