package com.rndmodgames.futtoboru.tables.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Board Screen - Main Game
 * 
 * @author Geomancer86
 */
public class BoardScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public BoardScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable boardScreenTable = new VisTable(true);
        
        boardScreenTable.add(new VisTextButton("Board screen placeholder"));
        
        this.row();
        this.add(boardScreenTable);
    }
}