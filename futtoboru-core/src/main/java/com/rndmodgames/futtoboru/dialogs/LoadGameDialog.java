package com.rndmodgames.futtoboru.dialogs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.menu.UnsavedProgress;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveSlots;
import com.rndmodgames.futtoboru.system.save.SaveSummary;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiDialogs;

/**
 * Load Game Dialog v2 (P2-5)
 *
 * Lists the saves, newest first, with their manager, club and in-game date. A save made with another version of
 * the game is listed with the reason it can't be loaded.
 *
 * @author Geomancer86
 */
public class LoadGameDialog extends VisDialog {

    private final Game game;
    private final VisTable table = new VisTable();

    //
    public LoadGameDialog(Game game) {

        super(LanguageModLoader.getValue("load_game"));

        this.game = game;

        VisScrollPane scrollPane = new VisScrollPane(table);

        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        final VisTextButton closeButton = new VisTextButton(LanguageModLoader.getValue("close"));

        closeButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (closeButton.isPressed()) {

                    hide();
                }
            }
        });

        this.add(scrollPane).grow();

        this.row();
        this.add(closeButton).pad(UiTheme.space("xs")).align(Align.bottom);

        refresh();
    }

    /** The list is read again every time the dialog opens. */
    @Override
    public VisDialog show(Stage stage) {
        refresh();
        UiDialogs.track(this);
        return super.show(stage);
    }

    /**
     * Iterate Saved Games and Show List
     */
    private void refresh() {

        table.clearChildren();

        for (final SaveSummary save : SaveLoadSystem.listSaves()) {

            table.row().grow();
            // the autosave row is marked: its localized label in the heading colour (U-06)
            VisLabel name = save.isAutosave()
                    ? new VisLabel(save.getName(), UiTheme.color("text.heading"))
                    : new VisLabel(save.getName());
            table.add(name).pad(UiTheme.space("xs"));
            table.add(new VisLabel(save.isLoadable() ? describe(save) : reason(save.getStatus()))).pad(UiTheme.space("xs"));

            if (!save.isLoadable()) {
                table.add().pad(UiTheme.space("xs"));
                table.add(deleteButton(save)).pad(UiTheme.space("xs"));
                continue;
            }

            final VisTextButton loadGame = new VisTextButton(LanguageModLoader.getValue("load"));

            loadGame.addCaptureListener(new InputListener() {

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                    /**
                     * Load Saved Game From File (after the running autosave, if any, is on disk)
                     */
                    ((Futtoboru) game).awaitAutosave();
                    LoadResult result = SaveLoadSystem.loadGame(save.getFile());

                    if (!result.isLoaded()) {
                        UiDialogs.info(getStage(), LanguageModLoader.getValue("load_game"), reason(result.getStatus()));
                        return;
                    }

                    ((Futtoboru) game).startLoadedGame(result.getGame());
                    UnsavedProgress.markSaved(result.getGame());
                    MainMenuManager.resetNavigation();

                    /**
                     * Redirect to Main Game Screen with Game Loaded
                     */
                    ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);

                    // hide if needed
                    hide();
                }
            });

            table.add(loadGame).pad(UiTheme.space("xs"));
            table.add(deleteButton(save)).pad(UiTheme.space("xs"));
        }
    }

    /**
     * Delete button of a row (U-06): asks first (the question replaces this dialog, one modal at a time), deletes
     * on Delete, then this dialog opens again with the list read anew. Cancel just brings the list back.
     */
    private VisTextButton deleteButton(final SaveSummary save) {

        final VisTextButton delete = new VisTextButton(LanguageModLoader.getValue("delete"));

        delete.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                final Stage stage = getStage();
                if (stage == null) {
                    return;
                }
                ((Futtoboru) game).awaitAutosave(); // the autosave row's file is not deleted mid-write
                UiDialogs.confirm(stage,
                        LanguageModLoader.getValue("delete_save"),
                        LanguageModLoader.getValue("delete_save_text") + "\n\n" + save.getName(),
                        new String[] { LanguageModLoader.getValue("delete"), LanguageModLoader.getValue("cancel") },
                        new Boolean[] { Boolean.TRUE, Boolean.FALSE },
                        confirmed -> {
                            if (Boolean.TRUE.equals(confirmed) && !SaveSlots.delete(save)) {
                                UiDialogs.info(stage, LanguageModLoader.getValue("delete_save"),
                                        LanguageModLoader.getValue("delete_failed"));
                                return;
                            }
                            show(stage);
                        });
            }
        });

        return delete;
    }

    /** @return "manager, club, in-game date" of a save */
    static String describe(SaveSummary save) {
        StringBuilder sb = new StringBuilder();
        for (Object part : new Object[] { save.getManager(), save.getClub(),
                save.getGameDate() != null ? save.getGameDate().toLocalDate() : null }) {
            if (part != null && !part.toString().isEmpty()) {
                sb.append(sb.length() > 0 ? ", " : "").append(part);
            }
        }
        return sb.toString();
    }

    private static String reason(LoadResult.Status status) {
        switch (status) {
        case OLDER_VERSION:
            return LanguageModLoader.getValue("save_older_version");
        case NEWER_VERSION:
            return LanguageModLoader.getValue("save_newer_version");
        default:
            return LanguageModLoader.getValue("save_unreadable");
        }
    }
}
