package com.rndmodgames.futtoboru.tables.match.result;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Match Result Screen Table v1
 * 
 *  - This screen will be accessed immediately after a Match is played with the relevant Match Results
 *  
 *  - Should this screen used to review any other match result as in setMatch() and updateComponents()? 
 * 
 * @author Geomancer86
 */
public class MatchResultScreenTable extends VisTable {

    //
    Futtoboru game;
    
    // Dynamic Components
    VisTable mainTable = new VisTable(true);
    
    //
    public MatchResultScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
        
        //
        updateDynamicComponents();
    }
    
    //
    public void updateDynamicComponents() {
        
        //
        this.clear();
        
        //
        this.add("MATCH RESULT SCREEN PLACEHOLDER!");
        
        // TODO: match result and display relevant data
    }
}