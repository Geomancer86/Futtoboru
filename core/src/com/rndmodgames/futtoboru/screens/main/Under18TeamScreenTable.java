package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Under-18 Team Screen - Main Game
 * 
 * @author Geomancer86
 */
public class Under18TeamScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public Under18TeamScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable under18TeamScreenTable = new VisTable(true);
        
        under18TeamScreenTable.add(new VisTextButton("UNDER-18 TEAM screen placeholder"));
        
        this.row();
        this.add(under18TeamScreenTable);
    }
}