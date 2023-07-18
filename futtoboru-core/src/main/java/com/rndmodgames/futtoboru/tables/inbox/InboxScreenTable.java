package com.rndmodgames.futtoboru.tables.inbox;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Inbox Screen Table v1
 * 
 * @author Geomancer86
 */
public class InboxScreenTable extends VisTable {

    //
    Game game;
    SaveGame currentGame;
    
    // Layout
    VisTable searchAndFiltersTable = null;
    VisTable inboxListScrollTable = null;
    VisTable inboxDetailTable = null;
    
    // Dynamic Components
    
    public InboxScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.setDebug(true);
        
        //
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        /**
         * INBOX
         * 
         * TODO: WIP
         * 
         *  - We need a way to do something like SaveGame.getInbox()
         *  
         *  - Data Structure for Inbox Messages
         *  
         *  - Window might be anything (player data, match results, newspaper, etc. needs to be flexible)
         *  
         *  - When time does pass, events might fire triggering new messages/inboxes
         */
        
        searchAndFiltersTable = new VisTable(true);
        inboxListScrollTable = new VisTable(true);
        inboxDetailTable = new VisTable(true);
        
        this.row();
        this.add(searchAndFiltersTable);
        this.add(inboxListScrollTable);
        this.add(inboxDetailTable);
        
        // load screen
        updateDynamicComponents();
    }
    
    /**
     * Prototype Inbox Screen Layout v1
     * 
     *  - Requirements:
     *      
     *      - Filters & Search      (low priority)
     *      - Good Scrolling        (high priority)
     *      - Pagination            (low priority)
     *      - Delete Messages       (low priority)
     *      - Mark as Read/Unread   (low priority)
     */
    public void updateDynamicComponents() {
        
        /**
         * Iterate existing messages and populate scroll pane
         */
//        currentGame
    }
}