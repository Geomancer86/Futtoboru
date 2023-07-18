package com.rndmodgames.futtoboru.tables.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Squad Screen - Main Game
 * 
 * @author Geomancer86
 */
public class SquadScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public SquadScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable squadScreenTable = new VisTable(true);
        
        squadScreenTable.add(new VisTextButton("SQUAD screen placeholder"));
        
        this.row();
        this.add(squadScreenTable);
    }
}