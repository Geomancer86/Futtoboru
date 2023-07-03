package com.rndmodgames.futtoboru.tables.schedule;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Schedule Screen Table v1
 * 
 * SOURCE:
 *  - https://dictatethegame.com/wp-content/uploads/2022/12/20221212112104_1.jpg
 *  - https://cdn.footballmanager.com/site/inline-images/1%20-.png
 * 
 * @author Geomancer86
 */
public class ScheduleScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    public ScheduleScreenTable(Game parent) {
        
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru)parent);
        this.currentGame = game.getCurrentGame();
        
        this.add(new VisLabel("SCHEDULE SCREEN PLACEHOLDER - WORK IN PROGRESS!"));
    }
    
    /**
     * TODO WIP:
     * 
     *  - Add Friendly Button
     *  - Scheduled Events / Matches List
     *  - 
     */
    public void updateDynamicComponents() {
        
    }
}