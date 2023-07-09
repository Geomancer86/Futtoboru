package com.rndmodgames.futtoboru.menu.topmenu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.dialogs.SaveGameDialog;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
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
    
    // Main Menu Manager reference to switch screens / etc
    private MainMenuManager mainMenuManager = null;
    
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
    VisTextButton matchResultButton = new VisTextButton(LanguageModLoader.getValue("match_result"));
    
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
                
                // if current screen is Match Result, get back to PREVIOUS SCREEN or OTHER / HOME SCREEN
                if (MainMenuManager.CURRENT_SCREEN == MainMenuManager.MATCH_RESULT_SCREEN) {
                    
                    // Return to previous screen
//                    mainMenuManager.setActiveMainScreen(MainMenuManager.SCHEDULE_SCREEN);
                    mainMenuManager.setActiveMainScreen(MainMenuManager.BEFORE_MATCH_SCREEN);
                }
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
            }
        });
        
        /**
         * Match Result Button
         */
        matchResultButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                matchResult();
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
        
        mainButtonContainer.clear();
        
        switch(nextGameAction) {
        
        //
        case FuttoboruGameEngine.CONTINUE_GAME_ACTION:
            
            // Set continue game button
            mainButtonContainer.add(continueGameButton);
            
            break;
            
        case FuttoboruGameEngine.MATCH_PREVIEW_ACTION:

            // Set match preview button
            mainButtonContainer.add(matchPreviewButton);
            
            break;
            
        default:
            System.out.println("GAME ACTION " + nextGameAction + " NOT IMPLEMENTED!");
        }
    }
    
    /**
     * 
     */
    public void matchResult() {
       
        //
        System.out.println("LOADING MATCH RESULT SCREEN!");
        
        // Simulate Match and Advance Time
        ((Futtoboru)(game)).getGameEngine().getMatchResult();
        
        mainMenuManager.setActiveMainScreen(MainMenuManager.MATCH_RESULT_SCREEN);
        
        mainButtonContainer.clear();
        mainButtonContainer.add(continueGameButton);
        
        // TODO: after a match the continue game button should take the player back to a screen
    }
    
    /**
     * 
     */
    public void matchPreview() {
        
        //
        System.out.println("LOADING MATCH PREVIEW SCREEN!");
        
        mainMenuManager.setActiveMainScreen(MainMenuManager.MATCH_PREVIEW_SCREEN);
        
        /**
         * TODO: RESET BUTTON TO MATCH RESULT
         *  - If the player goes back to any other screen, the BUTTON should be reset back to MATCH PREVIEW because
         *      there is no other way to go to the screen for now, no buttons to get there clicking on a match list
         */
        mainButtonContainer.clear();
        mainButtonContainer.add(matchResultButton);
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

    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }
}