package com.rndmodgames.futtoboru.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.kotcrab.vis.ui.util.ToastManager;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.tools.SharingUtils;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Main Game Screen v1
 * 
 *  - Day of Week/Time of Day Widget
 *  - Continue Game Button
 *  - Settings Button
 *  
 *  - Main Game Area
 *      - Tab Panels for Main Game Screens
 *      - Active Main Game Screen
 *      
 *  - TODO/WIP:
 *  
 *      - Depending on the Player Role / Status / Team / Etc, the main screen buttons will be different, current model doesn't work
 * 
 * @author Geomancer86
 */
public class MainGameScreen implements Screen {

    Game game;
    Stage stage;
    SaveGame currentGame; // reference for easy access
    
    /**
     * Main Menu Manager
     */
    MainMenuManager menuManager;
    
    //
    private MainGameMenuTable mainGameMenu = null;
    private VisTable mainTable = null;
    
    //
    private ToastManager toastManager;
    
    //
    public MainGameScreen(Game parent) {
     
        this.game = parent;
            
        // set saved game for easier access
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        // Validate that a game is loaded
        if (this.currentGame == null) {
            Log.error(Log.UI_MENU, "MainGameScreen", "No game loaded! Cannot initialize MainGameScreen without a SaveGame.");
            throw new IllegalStateException("MainGameScreen requires a loaded SaveGame. Please start a new game or load an existing one.");
        }
        
        //
        stage = new Stage(UiScale.newViewport());
        
        // Full Game Window
        final VisTable gameWindowTable = new VisTable(true);
        gameWindowTable.setFillParent(true);
        gameWindowTable.pad(UiTheme.space("xs"));
        
        // Top Menu (save, load, settings, advance time)
        mainGameMenu = new MainGameMenuTable(game, stage);
        
        //
        gameWindowTable.row().colspan(2);
        gameWindowTable.add(mainGameMenu).growX().top().right();
        
        // Takes care of Buttons Menu Dynamically
        mainTable = new VisTable(true);
        menuManager = new MainMenuManager(game, currentGame, mainTable);
        
        // Set the reference to allow dynamic screen updates from the Game Engine after data changes
        FuttoboruGameEngine gameEngine = ((Futtoboru)game).getGameEngine();
        if (gameEngine != null) {
            gameEngine.setMainMenuManager(menuManager);
        } else {
            Log.error(Log.UI_MENU, "MainGameScreen", "GameEngine is null! Some features may not work correctly.");
        }
        
        /**
         * Set the reference to allow calling screen changes from the top menu
         * TODO: fix / decouple / couple the menu managers to avoid too much passing objects around
         */
        mainGameMenu.setMainMenuManager(menuManager);
        
        // Set the reference to allow MainMenuManager to refresh button state
        menuManager.setTopMenuTable(mainGameMenu);
        
        //
        gameWindowTable.row();
        gameWindowTable.add(menuManager.getButtonsMenu()).top();
        gameWindowTable.add(UiScale.newWideOverflowScrollPane(mainTable)).grow(); // U-03a: a too-wide panel scrolls, the top bar stays

        // Initialize Toast Manager
        toastManager = new ToastManager(stage);
        toastManager.setScreenPadding((int) UiTheme.size("toast.padX"), (int) UiTheme.size("toast.padY")); // hardcoded to let the toast show on not under the top menu, 
        toastManager.toFront(); // TODO FIX: showing the toast in front/foreground is not priority as they are mostly for debug
        
        //
        stage.addActor(gameWindowTable);
    }
    
    @Override
    public void show() {
        
        // Multiplex Input Processors
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        
        // UI Stage Adapter
        inputMultiplexer.addProcessor(stage);
        
        // Add Custom Key Adapter
        InputAdapter keyboardKeysAdapter = new InputAdapter() {
            
            /**
             * WASD + Other Keys
             * 
             * TODO: customize key bindings on Settings
             */
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                
                /**
                 * F12 - SCREENSHOT / SCREEN CAPTURE
                 */ 
                case Keys.F12:
                    // Screenshot

                    // TODO: unhardcode screenshot folders
                    SharingUtils.screenshot();

                    // Toast TODO: add folder name to toast text
                    toastShort(LanguageModLoader.getValue("screenshot_taken"));

                    return true;

                /**
                 * ESC - BACK (U-04b), unless a dialog is open (the dialog handles its own keys)
                 */
                case Keys.ESCAPE:
                    if (isDialogOpen()) {
                        return false;
                    }
                    return menuManager.goBack();

                default:
                    return false;
                }
            }
        };
        
        // Add the Keyboard Keys Adapter to Input Multiplexer
        inputMultiplexer.addProcessor(keyboardKeysAdapter);
        
        // Set multiplexer to take care of all input
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {
        
        // Clear blit
        UiTheme.clearScreen();
        
        // Draw
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        
        // Update viewport on resize to keep event coordinate for buttons/widgets
        UiScale.updateStageViewport(stage, width, height);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
    
    /** @return true while a dialog (save, load, confirm) is on the stage */
    private boolean isDialogOpen() {
        for (Actor actor : stage.getRoot().getChildren()) {
            if (actor instanceof Dialog && actor.isVisible()) {
                return true;
            }
        }
        return false;
    }

    // Toasts
    /**
     * Displays short toast
     */
    public void toastShort(String text) {
        
        // 
        toastManager.show(text, 5);
        
        //
        toastManager.toFront();
    }
}