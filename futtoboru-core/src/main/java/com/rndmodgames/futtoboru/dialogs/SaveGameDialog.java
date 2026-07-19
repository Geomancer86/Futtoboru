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
 *
 * <p>Fixed 2026-07-19 per the pre-game menus UX audit
 * ({@code docs/planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md}):
 * <ul>
 *   <li>Saving over an existing filename now asks to overwrite via {@link ConfirmDialog} first
 *       (P0 — previously silent).</li>
 *   <li>After a successful save the dialog switches to a clear "Close" state instead of staying open
 *       indefinitely with a Cancel button that implies the save didn't happen.</li>
 *   <li>Localized the remaining hardcoded strings ("Save name:", "Saved as...").</li>
 * </ul>
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

                if (SaveLoadSystem.getSavedGames().contains(name)) {
                    new ConfirmDialog(
                            LanguageModLoader.getValue("save_game"),
                            LanguageModLoader.getValue("save_overwrite_confirm_message"),
                            () -> performSave(name, saveGame, statusLabel, cancelButton, continueGameButton)
                    ).show(getStage());
                } else {
                    performSave(name, saveGame, statusLabel, cancelButton, continueGameButton);
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
        add(new VisLabel(LanguageModLoader.getValue("save_name_label"))).pad(5);
        row();
        add(nameField).width(280).pad(5);
        row();
        add(statusLabel).pad(5);
        row();
        add(cancelButton).pad(5);
        add(saveGameButton).pad(5);
    }

    private static void performSave(String name, SaveGame saveGame, VisLabel statusLabel,
            VisTextButton cancelButton, VisTextButton continueGameButton) {

        SaveLoadSystem.saveGame(saveGame, name);
        saveGame.setIsSaved(true);

        statusLabel.setText(LanguageModLoader.getValue("saved_as_message").replace("{name}", name));

        // Relabel to a clear "Close" affordance after a successful save instead of leaving a Cancel
        // button that implies the save didn't happen (P1 fix, pre-game menus audit 2026-07-19). Save
        // stays enabled since this dialog instance is cached and reused across opens by callers, and
        // the player may legitimately want to save again under a different name later.
        cancelButton.setText(LanguageModLoader.getValue("close"));

        if (continueGameButton != null) {
            continueGameButton.setDisabled(false);
        }
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
