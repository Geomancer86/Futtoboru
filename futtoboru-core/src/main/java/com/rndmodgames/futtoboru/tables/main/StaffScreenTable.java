package com.rndmodgames.futtoboru.tables.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Staff Screen - Main Game
 * 
 * @author Geomancer86
 */
public class StaffScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public StaffScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable staffScreenTable = new VisTable(true);
        
        staffScreenTable.add(new VisTextButton("STAFF screen placeholder"));
        
        this.row();
        this.add(staffScreenTable);
    }
}