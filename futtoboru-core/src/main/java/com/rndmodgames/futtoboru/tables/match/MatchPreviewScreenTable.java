package com.rndmodgames.futtoboru.tables.match;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Match Preview Screen Table v1
 * 
 *  - basic:
 *      - home and away clubs
 *      - venue
 *      - tickets/attendance/money
 *      - basic players/squads/lineups/etc
 *      - estimated odds
 *      
 *      getting into this screen will change the button to MATCH RESULT
 *          
 *      after match result screen gets back to CONTINUE GAME
 * 
 * @author Geomancer86
 */
public class MatchPreviewScreenTable extends VisTable {

    //
    Futtoboru game;
    
    public MatchPreviewScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
        
        this.add("PREVIEW SCREEN PLACEHOLDER - WORK IN PROGRESS!");
    }
}