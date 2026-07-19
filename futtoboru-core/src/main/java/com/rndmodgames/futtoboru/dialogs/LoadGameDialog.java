package com.rndmodgames.futtoboru.dialogs;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Load Game Dialog — lists saves by name, most-recent-first (PL-7).
 *
 * <p>Fixed 2026-07-19 per the pre-game menus UX audit
 * ({@code docs/planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md}):
 * <ul>
 *   <li>Each row now shows real metadata (in-game date, manager, club) instead of just the raw
 *       filename — small save counts make a full {@code loadGame(...)} per row for display cheap
 *       enough, per the spec.</li>
 *   <li>Added a per-row Delete control behind {@link ConfirmDialog}.</li>
 *   <li>Attempting to load a corrupted save now shows an {@link ErrorDialog} instead of silently
 *       doing nothing (P1 — {@code SaveLoadSystem.loadGame} already returns {@code null} gracefully on
 *       corruption, this dialog just never told the player).</li>
 *   <li>Localized the empty-list message.</li>
 * </ul>
 *
 * <p>Callers should construct a fresh instance each time this is shown (do not cache across opens) so
 * deletions/new saves are always reflected — see {@code MenuScreen}/{@code MainGameMenuTable}.
 */
public class LoadGameDialog extends VisDialog {

    private static final DateTimeFormatter GAME_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final Game game;
    private final VisTable rowsTable = new VisTable();

    public LoadGameDialog(Game game) {
        super(LanguageModLoader.getValue("load_game"));

        this.game = game;

        VisScrollPane scrollPane = new VisScrollPane(rowsTable);
        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        rebuildRows();

        VisTextButton closeButton = new VisTextButton(LanguageModLoader.getValue("close"));
        closeButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                hide();
            }
        });

        add(scrollPane).grow().width(420);
        row();
        add(closeButton).pad(5).align(Align.bottom);
    }

    /**
     * Callers cache a single {@code LoadGameDialog} instance across opens (see {@code MenuScreen} /
     * {@code MainGameMenuTable}), so rebuild the row list on every show — otherwise a save created or
     * deleted since the dialog was first constructed would not appear until the app restarted.
     */
    @Override
    public VisDialog show(Stage stage) {
        rebuildRows();
        return super.show(stage);
    }

    private void rebuildRows() {
        rowsTable.clearChildren();

        List<String> savedGames = SaveLoadSystem.getSavedGames();
        if (savedGames.isEmpty()) {
            rowsTable.row();
            rowsTable.add(new VisLabel(LanguageModLoader.getValue("no_saved_games"))).pad(10);
            return;
        }

        for (String savedGameName : savedGames) {

            rowsTable.row().grow();
            rowsTable.add(new VisLabel(describeSave(savedGameName))).pad(5).expandX().left();

            VisTextButton loadGame = new VisTextButton(LanguageModLoader.getValue("load"));
            loadGame.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (event.getButton() != 0) {
                        return;
                    }
                    SaveGame loaded = SaveLoadSystem.loadGame(savedGameName);
                    if (loaded != null) {
                        ((Futtoboru) game).activateSaveGame(loaded);
                        ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);
                        hide();
                    } else {
                        new ErrorDialog(
                                LanguageModLoader.getValue("load_game"),
                                LanguageModLoader.getValue("load_game_failed_message")
                        ).show(getStage());
                    }
                }
            });
            rowsTable.add(loadGame).pad(5);

            VisTextButton deleteButton = new VisTextButton(LanguageModLoader.getValue("delete"));
            deleteButton.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    new ConfirmDialog(
                            LanguageModLoader.getValue("delete"),
                            LanguageModLoader.getValue("delete_save_confirm_message"),
                            () -> {
                                SaveLoadSystem.deleteSaveGame(savedGameName);
                                rebuildRows();
                            }
                    ).show(getStage());
                }
            });
            rowsTable.add(deleteButton).pad(5);
        }
    }

    private static String describeSave(String savedGameName) {
        SaveGame saveGame = SaveLoadSystem.loadGame(savedGameName);
        if (saveGame == null) {
            return savedGameName + " (" + LanguageModLoader.getValue("save_unreadable") + ")";
        }

        StringBuilder description = new StringBuilder(savedGameName);

        if (saveGame.getGameDate() != null) {
            description.append(" — ").append(GAME_DATE_FORMAT.format(saveGame.getGameDate()));
        }

        Person owner = saveGame.getOwner();
        if (owner != null) {
            String ownerName = ((owner.getName() != null ? owner.getName() : "") + " "
                    + (owner.getLastname() != null ? owner.getLastname() : "")).trim();
            if (!ownerName.isEmpty()) {
                description.append(" — ").append(ownerName);
            }
        }

        if (saveGame.getCurrentClub() != null && saveGame.getCurrentClub().getName() != null) {
            description.append(" (").append(saveGame.getCurrentClub().getName()).append(')');
        }

        return description.toString();
    }
}
