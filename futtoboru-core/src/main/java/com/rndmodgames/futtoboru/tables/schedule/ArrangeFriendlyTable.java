package com.rndmodgames.futtoboru.tables.schedule;

import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Arrange Friendly Table v1
 * 
 * @author Geomancer86
 */
public class ArrangeFriendlyTable extends VisTable {

    //
    Futtoboru game;
    SaveGame currentGame;
    
    //
    private Club currentClub;
    
    // Dynamic Components
    
    public ArrangeFriendlyTable(Futtoboru parent) {
        
        // default vis spacing
        super(true);
        
        //
        this.game = parent;
        this.currentGame = game.getCurrentGame();
        
        //
        this.add("PLACEHOLDER ARRANGE FRIENDLY");
        
        /**
         * Match Type
         * Match Date
         */
        VisSelectBox<String> matchTypesSelectBox = new VisSelectBox<String>();
        VisSelectBox<String> venuesSelectBox = new VisSelectBox<String>();
        VisSelectBox<String> matchRulesSelectBox = new VisSelectBox<String>();
        VisSelectBox<String> opponentSelectBox = new VisSelectBox<String>();
        
        //
        this.row();
        this.add("Type");
        this.add(matchTypesSelectBox);
        
        //
        this.row();
        this.add("Date");
        this.add("[DATEPICKER WIP]");
        
        //
        this.row();
        this.add("Venue");
        this.add(venuesSelectBox);
        
        //
        this.row();
        this.add("Match Rules");
        this.add(matchRulesSelectBox);
        
        //
        this.row();
        this.add("Opponent");
        this.add(opponentSelectBox);
        
        //
        this.row();
        this.add("Fee");
        this.add("Income");
        
        /**
         * TODO WIP:
         * 
         *  - ARRANGE FRIENDLY COMPONENT TITLE
         *  
         *  - Match Type Select (Friendly, Cup, Home and Away, etc) NOTE: only Friendly for this release
         *  - Date Picker (date might be selected directly/manually by clicking on the Fixtures Component when that portion is implemented
         *  - Venue (Home, Away, Neutral) needs stadiums to be implemented.
         *  - Match Rules (90 minutes only, penalties, etc.) NOTE: Only 90 minutes for this release
         *  
         *  - SELECTED DATE LABEL TITLE
         *  
         *  - Selected Team
         *  
         *  - Expected Fee & Income Widget
         *      - A good opportunity to get more data on this and the reasons for the fees & income
         *      - Might show a color or reaction score for the opponent (classic matches get more income)
         *      - Show the expected attendance (this might calculate other important matches the same day)
         *      
         */
    }
    
    //
    public void updateDynamicComponents() {
        
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}