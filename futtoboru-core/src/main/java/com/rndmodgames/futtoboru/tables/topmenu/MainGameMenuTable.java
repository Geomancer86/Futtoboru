package com.rndmodgames.futtoboru.tables.topmenu;

import java.time.LocalDateTime;

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
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.tables.widgets.CurrentDateAndTimeWidget;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Top Game Menu Table
 * 
 * 
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
    VisTextButton continueGameButton = new VisTextButton(LanguageModLoader.getValue("continue_game"));
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
        if (!((Futtoboru)(game)).getCurrentGame().getIsSaved()) {
            
            continueGameButton.addListener(new TextTooltip("Save Game Before Advancing the Simulation", VisUI.getSkin()));
            continueGameButton.setDisabled(true);
        }
        
        // dynamic menu table
        dynamicMainGameMenuTable = new VisTable(true);

        add(dynamicMainGameMenuTable).expandX().left();
        
        // Settings SelectBox & Continue Game Button
        VisTable rightMenu = new VisTable(true);
        
        rightMenu.add(gameSettingsSelectBox).right();
        rightMenu.add(dateTimeWidget).width(100).right();
        rightMenu.add(continueGameButton).right();
        
        add(rightMenu).expandX().right();
    }
    
    /**
     * Dynamically sets the Main Game Menu depending on the Selected Screen
     */
    public void setMainGameMenu(int screen, VisTable screenTable) {
        
//        dynamicMainGameMenuTable.clear();
//        
//        switch (screen) {
//        
//        case MainGameScreen.CLUB_SCREEN:
//            /**
//             * Profile, General, News, Facilities, Affiliates, History
//             */
//            dynamicMainGameMenuTable.add(((ClubScreenTable) screenTable).getClubScreenDynamicMenu());
//            break;
//        
//        default:
//            System.out.println("MAIN GAME MENU NOT IMPLEMENTED");
//            break;
//        }

    }
    
    /**
     * 
     */
    private void advanceGameTurn() {
        
        // Continue the game on the Game Engine
        ((Futtoboru)(game)).getGameEngine().continueGame();
                
        // Update the dynamic date widget
        dateTimeWidget.updateDynamicComponents();
    }
}