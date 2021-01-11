package com.rndmodgames.futtoboru.screens.main;

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
import com.rndmodgames.futtoboru.game.Futtuboru;
import com.rndmodgames.futtoboru.screens.MainGameScreen;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Main Game Menu Table
 * 
 * @author Geomancer86
 */
public class MainGameMenuTable extends VisTable {

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
    VisTextButton continueGameButton = new VisTextButton(LanguageModLoader.getValue("continue_game"));
    
    public MainGameMenuTable(Game game, Stage stage) {
        
        super(true);
        pad(5);

        // recursive debug test
        setDebug(true, true);

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
                        saveGameDialog = new SaveGameDialog(((Futtuboru)(game)).getCurrentGame(), continueGameButton);  
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
         * Continue Game Button
         * 
         * - This will be disabled until the Game is Saved Once
         */
        continueGameButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                /**
                 * Continue Game Simulation
                 */
                if (continueGameButton.isDisabled()) {

                    System.out.println("SAVE GAME BEFORE ADVANCING THE SIMULATION!");

                } else {

                    // 
                    advanceGameTurn();
                }
            }
        });

        /**
         * Continue Game Tooltip [SAVE GAME FIRST]
         * 
         * NOTE: button disabled until saving game at least once
         */
        if (!((Futtuboru)(game)).getCurrentGame().getIsSaved()) {
            
            continueGameButton.addListener(new TextTooltip("Save Game Before Advancing the Simulation", VisUI.getSkin()));
            continueGameButton.setDisabled(true);
        }
        
        // dynamic menu table
        dynamicMainGameMenuTable = new VisTable(true);

        add(dynamicMainGameMenuTable).expandX().left();
        
     // Settings SelectBox & Continue Game Button
        VisTable rightMenu = new VisTable(true);
        
        rightMenu.add(gameSettingsSelectBox).right();
        rightMenu.add(continueGameButton).right();
        
        add(rightMenu).expandX().right();
    }
    
    /**
     * Dynamically sets the Main Game Menu depending on the Selected Screen
     */
    public void setMainGameMenu(int screen) {
        
        dynamicMainGameMenuTable.clear();
        
        switch (screen) {
        
        case MainGameScreen.CLUB_SCREEN:
            /**
             * Profile, General, News, Facilities, Affiliates, History
             */
            VisTextButton clubProfileButton = new VisTextButton(LanguageModLoader.getValue("profile"));
            VisTextButton clubGeneralButton = new VisTextButton(LanguageModLoader.getValue("general"));
            VisTextButton clubNewsButton = new VisTextButton(LanguageModLoader.getValue("news"));
            VisTextButton clubFacilitiesButton = new VisTextButton(LanguageModLoader.getValue("facilities"));
            VisTextButton clubAffiliatesButton = new VisTextButton(LanguageModLoader.getValue("affiliates"));
            VisTextButton clubHistoryButton = new VisTextButton(LanguageModLoader.getValue("history"));
            
            dynamicMainGameMenuTable.add(clubProfileButton).expandX().left();
            dynamicMainGameMenuTable.add(clubGeneralButton).expandX().left();
            dynamicMainGameMenuTable.add(clubNewsButton).expandX().left();
            dynamicMainGameMenuTable.add(clubFacilitiesButton).expandX().left();
            dynamicMainGameMenuTable.add(clubAffiliatesButton).expandX().left();
            dynamicMainGameMenuTable.add(clubHistoryButton).expandX().left();
            
            break;
        
        default:
            System.out.println("MAIN GAME MENU NOT IMPLEMENTED");
            break;
        }

    }
    
    /**
     * TODO: move this to the Simulation
     */
    private void advanceGameTurn() {
        
        System.out.println("ADVANCE THE SIMULATION - 1 TURN");
       
        
        // update the buttons in case the Player got a new Job at a Club
//        updateMainButtonsVisibility();
    }
}