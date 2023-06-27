package com.rndmodgames.futtoboru.tables.inbox;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * Inbox Screen Table v1
 * 
 * @author Geomancer86
 */
public class InboxScreenTable extends VisTable {

    //
    Game game;
    
    public InboxScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.game = parent;
        
        //
        this.add(new VisTextButton("INBOX screen placeholder"));
    }
}