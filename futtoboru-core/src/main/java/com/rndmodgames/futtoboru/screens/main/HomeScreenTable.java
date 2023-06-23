package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Home Screen - Main Game
 * 
 *  - All in one place
 *      - Gadgets / Charts
 *      - 
 * 
 * @author Geomancer86
 */
public class HomeScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public HomeScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable homeScreenTable = new VisTable(true);
        
        homeScreenTable.add(new VisTextButton("HOME screen placeholder"));
        
        this.row();
        this.add(homeScreenTable);
    }
}