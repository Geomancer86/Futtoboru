package com.rndmodgames.futtoboru.tables.competitions;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Competitions Screen Table v1
 * 
 * @author Geomancer86
 */
public class CompetitionsScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    public CompetitionsScreenTable(Game parent) {
    
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    //
    public void updateDynamicComponents() {

        //
        this.clear();

        //
        this.add("CLUB COMPETITIONS SCREEN PLACEHOLDER - WORK IN PROGRESS!");
    }
}