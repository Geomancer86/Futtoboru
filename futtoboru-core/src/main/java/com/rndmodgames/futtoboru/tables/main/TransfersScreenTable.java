package com.rndmodgames.futtoboru.tables.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Transfers Screen - Main Game
 * 
 * @author Geomancer86
 */
public class TransfersScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public TransfersScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable transfersScreenTable = new VisTable(true);
        
        transfersScreenTable.add(new VisTextButton("TRANSFERS screen placeholder"));
        
        this.row();
        this.add(transfersScreenTable);
    }
}