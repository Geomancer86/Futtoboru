package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Training Screen - Main Game
 * 
 * @author Geomancer86
 */
public class TrainingScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * 
     * @param parent
     */
    public TrainingScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());
        
        VisTable trainingScreenTable = new VisTable(true);
        
        trainingScreenTable.add(new VisTextButton("TRAINING screen placeholder"));
        
        this.row();
        this.add(trainingScreenTable);
    }
}