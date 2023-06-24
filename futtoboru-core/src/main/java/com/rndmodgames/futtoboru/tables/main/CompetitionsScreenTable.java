package com.rndmodgames.futtoboru.tables.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Competitions Screen - Main Game
 * 
 * @author Geomancer86
 */
public class CompetitionsScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public CompetitionsScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable competitionsScreenTable = new VisTable(true);
        
        competitionsScreenTable.add(new VisTextButton("COMPETITIONS screen placeholder"));
        
        this.row();
        this.add(competitionsScreenTable);
    }
}