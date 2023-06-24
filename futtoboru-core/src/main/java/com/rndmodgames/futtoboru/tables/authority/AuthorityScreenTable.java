package com.rndmodgames.futtoboru.tables.authority;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Authority Screen Table v1
 * 
 * 
 * 
 * @author Geomancer86
 */
public class AuthorityScreenTable extends VisTable {

    //
    Game game;
    
    public AuthorityScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.game = parent;
        
        /**
         * Authority Components
         * 
         *  - Name
         *  - Confederations
         *  
         */
        this.add(new VisTextButton("AUTHORITY screen placeholder"));
    }
}