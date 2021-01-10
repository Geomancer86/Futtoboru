package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Team Report Screen - Main Game
 * 
 * @author Geomancer86
 */
public class TeamReportScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public TeamReportScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable teamReportScreenTable = new VisTable(true);
        
        teamReportScreenTable.add(new VisTextButton("TEAM REPORT screen placeholder"));
        
        this.row();
        this.add(teamReportScreenTable);
    }
}