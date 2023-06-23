package com.rndmodgames.futtoboru.menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.screens.main.HomeScreenTable;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Main Menu Manager v1
 *
 *  - Create and set the Main Menu Buttons
 *      
 *      - Dynamically depending on Job/Club/Status/Etc
 *      - 
 * 
 * @author Geomancer86
 */
public class MainMenuManager {

    // main screen reference
    VisTable mainTable;
    
    //
    private VisTable buttonsMenu = new VisTable();
    
    // Screen Ids
    private static final int HOME_SCREEN =  1;
    
    // Main Game Buttons
    private VisTextButton homeButton = new VisTextButton(LanguageModLoader.getValue("home"));
    
    //
    private HomeScreenTable homeScreenTable = null;
    
    public MainMenuManager(Game game, SaveGame currentGame, VisTable mainTable) {
        
        //
        this.mainTable = mainTable;
        
        // other windows
        homeScreenTable = new HomeScreenTable(game);
        
        //
        homeButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(HOME_SCREEN);
            }
        });

        buttonsMenu.add(homeButton).fill();
        buttonsMenu.row();
    }

    /**
     * Handle the Table Switching for the Main Screen
     */
    private void setActiveMainScreen(int screen) {

        // Clear the main table
        mainTable.clear();

        switch (screen) {

        case HOME_SCREEN:
            mainTable.add(homeScreenTable).grow();
            break;

        //
        default:
            System.out.println("SCREEN NOT SET UP");
            break;
        }

    }
    
    //
    public VisTable getButtonsMenu() {
        return buttonsMenu;
    }

    public void setButtonsMenu(VisTable buttonsMenu) {
        this.buttonsMenu = buttonsMenu;
    }
}