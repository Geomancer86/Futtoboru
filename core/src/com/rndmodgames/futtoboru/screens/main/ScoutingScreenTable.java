package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Scouting Screen - Main Game
 * 
 * @author Geomancer86
 */
public class ScoutingScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public ScoutingScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable scoutingScreenTable = new VisTable(true);
        
        scoutingScreenTable.add(new VisTextButton("SCOUTING screen placeholder"));
        
        this.row();
        this.add(scoutingScreenTable);
    }
}