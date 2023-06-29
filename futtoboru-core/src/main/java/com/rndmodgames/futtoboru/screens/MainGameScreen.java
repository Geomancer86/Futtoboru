package com.rndmodgames.futtoboru.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.tables.topmenu.MainGameMenuTable;

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
    public MainGameScreen(Game parent) {
     
        this.game = parent;
            
        // set saved game for easier access
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        //
        stage = new Stage(new ScreenViewport());
        
        // Full Game Window
        final VisTable gameWindowTable = new VisTable(true);
        gameWindowTable.setDebug(true);
        gameWindowTable.setFillParent(true);
        gameWindowTable.pad(5);
        
        // Top Menu (save, load, settings, advance time)
        mainGameMenu = new MainGameMenuTable(game, stage);
        
        //
        gameWindowTable.row().colspan(2);
        gameWindowTable.add(mainGameMenu).growX().top().right();
        
        // Takes care of Buttons Menu Dynamically
        mainTable = new VisTable(true);
        menuManager = new MainMenuManager(game, currentGame, mainTable);
        
        // Set the reference to allow dynamic screen updates from the Game Engine after data changes
        ((Futtoboru)game).getGameEngine().setMainMenuManager(menuManager);
        
        //
        gameWindowTable.row();
        gameWindowTable.add(menuManager.getButtonsMenu()).top();
        gameWindowTable.add(mainTable).grow();

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