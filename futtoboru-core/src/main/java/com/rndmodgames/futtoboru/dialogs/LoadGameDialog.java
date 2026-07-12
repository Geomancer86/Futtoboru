package com.rndmodgames.futtoboru.dialogs;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Load Game Dialog — lists saves by name (PL-7).
 */
public class LoadGameDialog extends VisDialog {

    public LoadGameDialog(Game game) {
        super(LanguageModLoader.getValue("load_game"));

        final VisTable table = new VisTable();
        VisScrollPane scrollPane = new VisScrollPane(table);
        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        List<String> savedGames = SaveLoadSystem.getSavedGames();
        if (savedGames.isEmpty()) {
            table.row();
            table.add(new VisLabel("No saved games found.")).pad(10);
        } else {
            for (String savedGameName : savedGames) {
                table.row().grow();
                table.add(new VisLabel(savedGameName)).pad(5).expandX().left();

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
                        var loaded = SaveLoadSystem.loadGame(savedGameName);
                        if (loaded != null) {
                            ((Futtoboru) game).activateSaveGame(loaded);
                            ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);
                            hide();
                        }
                    }
                });
                table.add(loadGame).pad(5);
            }
        }

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

        add(scrollPane).grow();
        row();
        add(closeButton).pad(5).align(Align.bottom);
    }
}
