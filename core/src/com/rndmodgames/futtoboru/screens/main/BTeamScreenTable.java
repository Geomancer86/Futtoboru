package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * B-Team Screen - Main Game
 * 
 * @author Geomancer86
 */
public class BTeamScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public BTeamScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable bTeamScreenTable = new VisTable(true);
        
        bTeamScreenTable.add(new VisTextButton("B-TEAM screen placeholder"));
        
        this.row();
        this.add(bTeamScreenTable);
    }
}