package com.rndmodgames.futtoboru.dialogs;

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
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Load Game Dialog v1
 * 
 * @author Geomancer86
 */
public class LoadGameDialog extends VisDialog{

    // 
    public LoadGameDialog(Game game) {
        
        super(LanguageModLoader.getValue("load_game"));
        
        /**
         * List Saved Games and add Load Game Button to Each
         */
        
        final VisTable table = new VisTable();
        
        VisScrollPane scrollPane = new VisScrollPane(table);
        
        scrollPane.setOverscroll(false, true);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        
        /**
         * Iterate Saved Games and Show List
         */
        List<String> savedGames = SaveLoadSystem.getSavedGames();

        for (String savedGame : savedGames) {
            
            final String savedGameName = savedGame.split("/")[savedGame.split("/").length - 1];
            
            table.row().grow();
            table.add(new VisLabel(savedGameName)).pad(5);
            
            final VisTextButton loadGame = new VisTextButton(LanguageModLoader.getValue("load"));
            
            loadGame.addCaptureListener(new InputListener() {
                
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    
                    /**
                     * Load Saved Game From File
                     */
                    ((Futtoboru)(game)).setCurrentGame(SaveLoadSystem.loadGame(savedGameName));

                    /**
                     * Redirect to Main Game Screen with Game Loaded
                     */
                    ((Futtoboru) game).changeScreen(Futtoboru.GAME_SCREEN);
                    
                    // hide if needed
                    hide();
                }
            });
            
            table.add(loadGame).pad(5);
        }
        
        final VisTextButton closeButton = new VisTextButton(LanguageModLoader.getValue("close"));

        closeButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                if (closeButton.isPressed()){
                    
                    hide();
                }
            }
        });

        this.add(scrollPane).grow();
        
        this.row();
        this.add(closeButton).pad(5).align(Align.bottom);
    }
}
