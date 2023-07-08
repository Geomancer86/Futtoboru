package com.rndmodgames.futtoboru.tables.topmenu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.dialogs.SaveGameDialog;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.tables.widgets.CurrentDateAndTimeWidget;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Top Game Menu Table
 * 
 * TODO:
 * 
 * @author Geomancer86
 */
public class MainGameMenuTable extends VisTable {

    //
    Game game;
    
    /**
     * Dialogs
     */
    private SaveGameDialog saveGameDialog = null;
    
    /**
     * Dynamic Main Game Menu table
     */
    private VisTable dynamicMainGameMenuTable = null;   
    
    /**
     * Screen Components
     */
    VisTable mainButtonContainer = new VisTable(true);
    VisTextButton continueGameButton = new VisTextButton(LanguageModLoader.getValue("continue_game"));
    VisTextButton matchPreviewButton = new VisTextButton(LanguageModLoader.getValue("match_preview"));
    
    CurrentDateAndTimeWidget dateTimeWidget = null;
    
    public MainGameMenuTable(Game game, Stage stage) {
        
        super(true);
        pad(5);

        //
        this.game = game;
        
        /**
         * Open Options Drop Down Menu Button/SelectBox -
         */
        final VisSelectBox<String> gameSettingsSelectBox = new VisSelectBox<>();
        
        gameSettingsSelectBox.setItems(LanguageModLoader.getValue("options"),
                                       LanguageModLoader.getValue("save_game"),
                                       LanguageModLoader.getValue("load_game"),
                                       LanguageModLoader.getValue("settings"),
                                       LanguageModLoader.getValue("quit_to_menu"),
                                       LanguageModLoader.getValue("quit_to_desktop"));
        
        gameSettingsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                /**
                 * Available Game Settings - Save Game v1
                 */
                int selectedOptionIndex = gameSettingsSelectBox.getSelectedIndex();
                
                switch(selectedOptionIndex) {
                
                case 0:
                    // ignore, main options button
                    break;
                
                case 1:
                    /**
                     * Show Save Game Dialog Window
                     */
                    if (saveGameDialog == null) {
                        
                        /**
                         * Pass the reference to the Continue Game button so we can enable that after saving the game once
                         */
                        saveGameDialog = new SaveGameDialog(((Futtoboru)(game)).getCurrentGame(), continueGameButton);  
                    } 

                    // 
                    saveGameDialog.show(stage);
                    break;
                
                default:
                    System.out.println("OPTION NOT IMPLEMENTED!");
                    break;
                }
                
                /**
                 * Clear the selection after click
                 */
                gameSettingsSelectBox.setSelectedIndex(0);
            }
        });
        
        /**
         * Current Date & Time Widget
         */
        dateTimeWidget = new CurrentDateAndTimeWidget(game);

        /**
         * Continue Game Button
         */
        continueGameButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                continueGame();
                
                //
                setMainContainerButton();
            }
        });
        
        /**
         * Match Preview Button
         */
        matchPreviewButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                matchPreview();
                
                //
                setMainContainerButton();
            }
        });
        
        // dynamic main button
        setMainContainerButton();
        
        // dynamic menu table
        dynamicMainGameMenuTable = new VisTable(true);

        add(dynamicMainGameMenuTable).expandX().left();
        
        // Settings SelectBox & Continue Game Button
        VisTable rightMenu = new VisTable(true);
        
        rightMenu.add(gameSettingsSelectBox).right();
        rightMenu.add(dateTimeWidget).width(100).right();
        rightMenu.add(mainButtonContainer).right();
        
        //
        add(rightMenu).expandX().right();
    }
    
    /**
     * Sets the Main Button depending on the state of the game
     */
    public void setMainContainerButton() {
        
        // get next action from game engine
        int nextGameAction = ((Futtoboru)(game)).getGameEngine().getNextGameAction();
        
        System.out.println("SETTING NEXT GAME ACTION BUTTON: " + nextGameAction);
        
        switch(nextGameAction) {
        
        //
        case FuttoboruGameEngine.CONTINUE_GAME_ACTION:
            
            // Set continue game button
            mainButtonContainer.clear();
            mainButtonContainer.add(continueGameButton);
            
            break;
            
        case FuttoboruGameEngine.MATCH_DAY_ACTION:

            // Set match preview button
            mainButtonContainer.clear();
            mainButtonContainer.add(matchPreviewButton);
            
            break;
        
        default:
            System.out.println("GAME ACTION " + nextGameAction + " NOT IMPLEMENTED!");
        }
    }
    
    /**
     * 
     */
    public void matchPreview() {
        
        //
        System.out.println("LOADING MATCH PREVIEW SCREEN!");
    }
    
    /**
     * 
     */
    private void continueGame() {
        
        // Continue the game on the Game Engine
        ((Futtoboru)(game)).getGameEngine().continueGame();
                
        // Update the dynamic date widget
        dateTimeWidget.updateDynamicComponents();
    }
}