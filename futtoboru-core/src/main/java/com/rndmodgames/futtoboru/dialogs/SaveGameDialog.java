package com.rndmodgames.futtoboru.dialogs;

import java.io.IOException;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rndmodgames.futtoboru.menu.UnsavedProgress;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.save.SaveSlots;
import com.rndmodgames.futtoboru.system.save.SaveSummary;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiDialogs;

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

    /** Why the typed name was refused (an empty name, U-06); empty otherwise. */
    private final VisLabel problemLabel = new VisLabel("", UiTheme.color("state.negative"));
    private final VisTextButton continueGameButton;

    /** Run once after the next successful save (U-04d "save first, then quit"); cleared on every show. */
    private Runnable onSaved;

    public SaveGameDialog(SaveGame saveGame, VisTextButton continueGameButton) {

        super(LanguageModLoader.getValue("save_game"));

        this.saveGame = saveGame;
        this.continueGameButton = continueGameButton;

        final VisTextButton cancelButton = new VisTextButton(LanguageModLoader.getValue("cancel"));

        nameField.setTextFieldListener((field, c) -> updateSaveButton());

        saveGameButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (saveGame != null) {
                    attemptSave();
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
        this.add(problemLabel).colspan(2).pad(UiTheme.space("xs"));
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
        UiDialogs.track(this);
        return super.show(stage);
    }

    /**
     * The Save button (U-06): an empty name is refused with a message; an existing save's name asks "Overwrite?"
     * first (the question replaces this dialog; Cancel brings it back with the typed name); a new name is written.
     */
    private void attemptSave() {
        final Stage stage = getStage();
        final String name = nameField.getText();
        final Runnable after = onSaved;

        switch (SaveSlots.check(name)) {
        case EMPTY:
            problemLabel.setText(LanguageModLoader.getValue("save_name_empty"));
            return;
        case OVERWRITE:
            UiDialogs.confirm(stage,
                    LanguageModLoader.getValue("overwrite_save"),
                    LanguageModLoader.getValue("overwrite_save_text") + "\n\n" + name.trim(),
                    new String[] { LanguageModLoader.getValue("overwrite"), LanguageModLoader.getValue("cancel") },
                    new Boolean[] { Boolean.TRUE, Boolean.FALSE },
                    confirmed -> {
                        if (Boolean.TRUE.equals(confirmed)) {
                            write(stage, name, after);
                        } else {
                            reopen(stage, name, after);
                        }
                    });
            return;
        default:
            write(stage, name, after);
        }
    }

    /** Writes the save; on success closes the dialog and runs {@code after}, on failure tells the player. */
    private void write(Stage stage, String name, Runnable after) {
        try {
            SaveLoadSystem.saveGame(saveGame, name);
            UnsavedProgress.markSaved(saveGame);

            // Enable the Continue Game Button
            if (continueGameButton != null) {
                continueGameButton.setDisabled(false);
            }

            onSaved = null;
            if (hasParent()) {
                hide();
            }
            if (after != null) {
                after.run();
            }
        } catch (IOException | RuntimeException e) {
            Log.error(Log.SYSTEM_INIT, "SaveGameDialog", "save failed: " + name, e);
            if (stage != null) {
                UiDialogs.info(stage, LanguageModLoader.getValue("save_game"),
                        LanguageModLoader.getValue("save_failed"));
            }
        }
    }

    /** Shows the dialog again after a cancelled overwrite, with the name the player typed. */
    private void reopen(Stage stage, String name, Runnable after) {
        if (stage == null) {
            return;
        }
        showThen(stage, after);
        nameField.setText(name);
        updateSaveButton();
    }

    private void refresh() {
        problemLabel.setText("");
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
        SaveSlots.NameCheck check = SaveSlots.check(nameField.getText());
        if (check != SaveSlots.NameCheck.EMPTY) {
            problemLabel.setText("");
        }
        boolean exists = check == SaveSlots.NameCheck.OVERWRITE;
        saveGameButton.setText(exists ? LanguageModLoader.getValue("overwrite") : LanguageModLoader.getValue("save_game"));
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
