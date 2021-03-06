package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Schedule Screen - Main Game
 * 
 * @author Geomancer86
 */
public class ScheduleScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public ScheduleScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable scheduleScreenTable = new VisTable(true);
        
        scheduleScreenTable.add(new VisTextButton("SCHEDULE screen placeholder"));
        
        this.row();
        this.add(scheduleScreenTable);
    }
}