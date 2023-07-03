package com.rndmodgames.futtoboru.tables.schedule;

import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Fixtures Table v1
 * 
 * @author Geomancer86
 */
public class FixturesTable extends VisTable {

    //
    Futtoboru game;
    SaveGame currentGame;
    
    //
    private Club currentClub;

    // Dynamic Components
    VisTable matchListTable = null;
    
    //
    public FixturesTable(Futtoboru parent) {
        
        // vis ui default spacing
        super(true);
        
        //
        this.game = parent;
        this.currentGame = game.getCurrentGame();
        
        /**
         * Club Current Matches
         */
        this.add("FIXTURES");
        this.row();
        
        /**
         * Match List Table
         */
        matchListTable = new VisTable(true);
        
        //
        this.add(matchListTable);
        
        //
        updateDynamicComponents();
        
        /**
         * WEEKLY COMPONENT / DAY TO DAY SELECTABLE COMPONENT
         * 
         * - Main functionality required:
         *      
         *      - Show the calendar view of the remainder of the Season days.
         *      - Show the matches already scheduled, with the option of selecting them and cancelling them.
         *      - Pick an empty day:
         *          - Arrange friendly match depending on parameters selected:
         *              - Match Type
         *              - Match Date (should be populated automatically depending on the selection on the calendar view).
         *              - Venue (Home, Away, Neutral Field)
         *              - Match Rules (90 minutes only, Extra Time, Penalties, 180 minutes, etc.)
         *              
         *              - Selected Team
         *              
         *              - Fee
         *              - Estimated Income
         *              
         *          - We can add a layer of negotiation, the other team won't say yes at the first ask
         *              - Fee can be disclosed on the reply
         *              - Relations with manager can let the game be free
         *              - Other team can propose an alternate date with some wiggle room (a day early or later for example)
         *              - They can refuse but say that they truly want but they cant, and propose a very late date or promise a match next year/season/etc.
         */
    }

    /**
     * 
     */
    public void updateDynamicComponents() {
        
        System.out.println("SHOWING CLUB SCHEDULED MATCH LIST!");
        
        //
        matchListTable.clear();
        
        matchListTable.add("The Club don't have any planned matches, pick and opponent and conditions and propose a friendly match to see it on this list");
        
        /**
         * Match List
         */
    }
    
    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}