package com.rndmodgames.futtoboru.tables.schedule;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.EngineParameters;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

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
    
    // Selected Component Values
    String selectedMatchType = null;
    String selectedVenueType = null;
    String selectedMatchRulesType = null;
    
    // Dynamic Components
    
    public ArrangeFriendlyTable(Futtoboru parent) {
        
        // default vis spacing
        super(true);
        
        //
        this.game = parent;
        this.currentGame = game.getCurrentGame();
        
        //
        this.add("PLACEHOLDER ARRANGE FRIENDLY").colspan(2);
        
        // Match Types
        VisSelectBox<String> matchTypesSelectBox = new VisSelectBox<String>();
        matchTypesSelectBox.setItems(EngineParameters.matchTypes);

        // Venue Types
        VisSelectBox<String> venuesSelectBox = new VisSelectBox<String>();
        venuesSelectBox.setItems(EngineParameters.matchVenueTypes);
        
        // Match Rules
        VisSelectBox<String> matchRulesSelectBox = new VisSelectBox<String>();
        matchRulesSelectBox.setItems(EngineParameters.matchRulesTypes);
        
        /**
         * Set Default Selected Values
         */
        matchTypesSelectBox.setSelectedIndex(0);
        selectedMatchType = matchTypesSelectBox.getSelected();
        
        venuesSelectBox.setSelectedIndex(0);
        selectedVenueType = venuesSelectBox.getSelected();
        
        matchRulesSelectBox.setSelectedIndex(0);
        selectedMatchRulesType = matchRulesSelectBox.getSelected();
        
        /**
         * List of Clubs (do not include current club)
         * 
         *  - TODO: this component is difficult because should allow to do some filtering at least
         *  - TODO: component shows opponent level/fame
         */
        VisSelectBox<String> opponentSelectBox = new VisSelectBox<String>();
        
        //
        this.row();
        this.add("Type");
        this.add(matchTypesSelectBox).fill();
        
        //
        this.row();
        this.add("Date");
        this.add("[DATEPICKER WIP]");
        
        //
        this.row();
        this.add("Venue");
        this.add(venuesSelectBox).fill();
        
        //
        this.row();
        this.add("Match Rules");
        this.add(matchRulesSelectBox).fill();
        
        //
        this.row();
        this.add("Opponent");
        this.add(opponentSelectBox).fill();
        
        //
        this.row();
        this.addSeparator().colspan(2);
        this.row();
        this.add("Fee");
        this.add("Income");
        
        /**
         * Confirm Friendly Button  (adds Friendly Request)
         * Cancel Button            (clear forms)
         */
        VisTextButton confirmFriendlyButton = new VisTextButton(LanguageModLoader.getValue("confirm"));
        VisTextButton cancelFriendlyButton = new VisTextButton(LanguageModLoader.getValue("cancel"));
        
        confirmFriendlyButton.addCaptureListener(new InputListener() {
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                // ADD NEW FRIENDLY DEPENDING ON THE SELECTED ELEMENTS ON THE FORM
                System.out.println("ADDING A NEW FRIENDLY MATCH!");
                System.out.println("selectedMatchType:      " + selectedMatchType);
                System.out.println("selectedVenueType:      " + selectedVenueType);
                System.out.println("selectedMatchRulesType: " + selectedMatchRulesType);
            }
        });
        
        cancelFriendlyButton.addCaptureListener(new InputListener() {
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                // CLEAR UI
                System.out.println("CLEAR ARRANGE FRIENDLY COMPONENT UI!");
            }
        });
        
        // Confirm Friendly Button Disabled Until Valid Form
//        confirmFriendlyButton.setDisabled(true);
        
        this.row().padTop(100);
        this.add(confirmFriendlyButton).bottom();
        this.add(cancelFriendlyButton).bottom();
        
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