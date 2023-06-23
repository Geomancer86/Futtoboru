package com.rndmodgames.futtoboru.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.screens.main.HomeScreenTable;
import com.rndmodgames.futtoboru.screens.main.MainGameMenuTable;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

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
    private MainGameMenuTable mainGameMenuTable = null;
    private VisTable mainTable = null;
    
    //
    public MainGameScreen(Game parent) {
     
        this.game = parent;
        
        // set saved game if exist for easier access
        if (((Futtoboru)game).getCurrentGame() != null){
            
            this.currentGame = ((Futtoboru)game).getCurrentGame();
        }
        
        stage = new Stage(new ScreenViewport());
        
        final VisTable gameWindowTable = new VisTable(true);
        gameWindowTable.pad(5);
        gameWindowTable.setFillParent(true);
        gameWindowTable.setDebug(true, true);

        /**
         * Top Game Menu (Top Menu)
         */
        mainGameMenuTable = new MainGameMenuTable(game, stage);
        
        /**
         * Main Game Screen
         */
        final VisTable mainScreenArea = new VisTable();

        mainTable = new VisTable(true);
        mainTable.setDebug(true);
        
         /**
          * Main Screen Area
          * 
          * Switched dinamically as the main game screen
          */
         mainScreenArea.add(mainGameMenuTable).fillX().top().left();
         
         mainScreenArea.row();
         mainScreenArea.add(mainTable).grow();
    
         // Takes care of Buttons Menu Dynamically
         menuManager = new MainMenuManager(game, currentGame, mainTable);

         // Main Game Menu Buttons
         gameWindowTable.add(menuManager.getButtonsMenu()).top();
         
         // 
         gameWindowTable.add(mainScreenArea).grow();

         //
         stage.addActor(gameWindowTable);
    }
    
    @Override
    public void show() {
        
        // Add input capabilities
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        
        // Clear blit
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        
        // Update viewport on resize to keep event coordinate for buttons/widgets
        stage.getViewport().update(width, height, true);
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
}