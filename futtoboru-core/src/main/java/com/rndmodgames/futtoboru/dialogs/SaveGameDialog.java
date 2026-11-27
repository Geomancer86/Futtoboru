package com.rndmodgames.futtoboru.dialogs;

import java.io.IOException;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rndmodgames.futtoboru.menu.UnsavedProgress;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.save.SaveSummary;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiTheme;

/**
 * Save Game Dialog v2 (P2-5)
 *
 * A name for the save slot: a new save, or an existing one picked from the list to overwrite it.
 *
 * @author Geomancer86
 */
public class SaveGameDialog extends VisDialog {

    private final SaveGame saveGame;

    private final VisTextField nameField = new VisTextField();
    private final VisTable savesTable = new VisTable();
    private final VisTextButton saveGameButton = new VisTextButton(LanguageModLoader.getValue("save_game"));

    /** Run once after the next successful save (U-04d "save first, then quit"); cleared on every show. */
    private Runnable onSaved;

    public SaveGameDialog(SaveGame saveGame, VisTextButton continueGameButton) {

        super(LanguageModLoader.getValue("save_game"));

        this.saveGame = saveGame;

        final VisTextButton cancelButton = new VisTextButton(LanguageModLoader.getValue("cancel"));

        nameField.setTextFieldListener((field, c) -> updateSaveButton());

        saveGameButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (saveGame == null) {
                    return;
                }
                try {
                    SaveLoadSystem.saveGame(saveGame, nameField.getText());
                    UnsavedProgress.markSaved(saveGame);

                    // Enable the Continue Game Button
                    continueGameButton.setDisabled(false);

                    hide();

                    Runnable next = onSaved;
                    onSaved = null;
                    if (next != null) {
                        next.run();
                    }
                } catch (IOException | RuntimeException e) {
                    Log.error(Log.SYSTEM_INIT, "SaveGameDialog",
                            "save failed: " + e);
                    if (getStage() != null) {
                        Dialogs.showOKDialog(getStage(), LanguageModLoader.getValue("save_game"),
                                LanguageModLoader.getValue("save_failed"));
                    }
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
                hide();
            }
        });

        VisScrollPane scrollPane = new VisScrollPane(savesTable);
        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        VisTable nameRow = new VisTable();
        nameRow.add(new VisLabel(LanguageModLoader.getValue("save_name"))).pad(UiTheme.space("xs"));
        nameRow.add(nameField).pad(UiTheme.space("xs")).growX();

        this.add(nameRow).colspan(2).growX();
        this.row();
        this.add(scrollPane).colspan(2).grow();
        this.row();
        this.add(cancelButton);
        this.add(saveGameButton);

        refresh();
    }

    /** The list of saves and the suggested name are read again every time the dialog opens. */
    @Override
    public VisDialog show(Stage stage) {
        return showThen(stage, null);
    }

    /** Shows the dialog; {@code afterSave} runs once if the player saves (not on Cancel). */
    public VisDialog showThen(Stage stage, Runnable afterSave) {
        onSaved = afterSave;
        refresh();
        return super.show(stage);
    }

    private void refresh() {
        nameField.setText(saveGame != null && saveGame.getSaveName() != null ? saveGame.getSaveName() : defaultName());

        savesTable.clearChildren();
        for (SaveSummary save : SaveLoadSystem.listSaves()) {
            if (!save.isLoadable()) {
                continue;
            }
            final String name = save.getName();
            final VisTextButton pick = new VisTextButton(name);
            pick.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    nameField.setText(name);
                    updateSaveButton();
                }
            });
            savesTable.row();
            savesTable.add(pick).pad(UiTheme.space("xs")).growX();
            savesTable.add(new VisLabel(LoadGameDialog.describe(save))).pad(UiTheme.space("xs"));
        }
        updateSaveButton();
    }

    /** "Save Game" for a new slot, "Overwrite" when that slot's file exists. */
    private void updateSaveButton() {
        boolean exists = SaveLoadSystem.saveFileFor(nameField.getText()).exists();
        saveGameButton.setText(LanguageModLoader.getValue(exists ? "overwrite" : "save_game"));
    }

    /** @return the manager's name and the in-game date, e.g. "Test Manager 1888-05-30" */
    private String defaultName() {
        StringBuilder sb = new StringBuilder();
        if (saveGame != null && saveGame.getOwner() != null) {
            if (saveGame.getOwner().getName() != null) {
                sb.append(saveGame.getOwner().getName()).append(' ');
            }
            if (saveGame.getOwner().getLastname() != null) {
                sb.append(saveGame.getOwner().getLastname()).append(' ');
            }
        }
        if (saveGame != null && saveGame.getGameDate() != null) {
            sb.append(saveGame.getGameDate().toLocalDate());
        }
        String name = sb.toString().trim();
        return name.isEmpty() ? "save" : name;
    }
}
