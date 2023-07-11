package com.rndmodgames.futtoboru.tables.finances;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Finances Screen Table v1
 * 
 * @author Geomancer86
 *
 */
public class FinancesScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    public FinancesScreenTable(Game parent) {
        
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    /**
     * 
     */
    public void updateDynamicComponents() {
        
        add("FINANCES SCREEN PLACEHOLDER - WORK IN PROGRESS");
    }
}