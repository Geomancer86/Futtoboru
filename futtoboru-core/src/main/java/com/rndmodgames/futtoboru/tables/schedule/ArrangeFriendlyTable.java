package com.rndmodgames.futtoboru.tables.schedule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.EngineParameters;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Arrange Friendly Table v1
 *  
 *  TODO WIP:
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
    Club selectedClub = null;
    Club selectedOpponentClub = null;
    
    /**
     * Selected Date NOTE: this is set on the FixturesTable Component
     */
    private LocalDateTime selectedDate;
    
    // Dynamic Components
    VisLabel selectedDateLabel = new VisLabel(LanguageModLoader.getValue("select_a_date"));
    VisLabel feeValueLabel = new VisLabel("$0");
    VisLabel incomeValueLabel = new VisLabel("$0");
    VisSelectBox<Club> opponentSelectBox = new VisSelectBox<Club>();
    VisTextButton confirmFriendlyButton = new VisTextButton(LanguageModLoader.getValue("confirm"));
    
    //
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    
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
         * Club Selectbox
         */
        opponentSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                // Disable Confirm if no Opponent Selected
                confirmFriendlyButton.setDisabled(opponentSelectBox.getSelected().getId() == null);
            }
        });
        
        //
        this.row();
        this.add("Type");
        this.add(matchTypesSelectBox).fill();
        
        //
        this.row();
        this.add("Date");
        this.add(selectedDateLabel);
        
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
        
        //
        this.row();
        this.add(feeValueLabel);
        this.add(incomeValueLabel);
        
        /**
         * Confirm Friendly Button  (adds Friendly Request)
         * Cancel Button            (clear forms)
         */
        VisTextButton cancelFriendlyButton = new VisTextButton(LanguageModLoader.getValue("cancel"));
        
        // Confirm Friendly Listener
        confirmFriendlyButton.addCaptureListener(new InputListener() {
            
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                
                // ADD NEW FRIENDLY DEPENDING ON THE SELECTED ELEMENTS ON THE FORM
                System.out.println("ADDING A NEW FRIENDLY MATCH!");
                System.out.println("selectedMatchType       : " + selectedMatchType);
                System.out.println("selectedVenueType       : " + selectedVenueType);
                System.out.println("selectedMatchRulesType  : " + selectedMatchRulesType);
                System.out.println("selectedOpponentClub    : " + opponentSelectBox.getSelected());
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
        confirmFriendlyButton.setDisabled(true);
        
        this.row().padTop(100);
        this.add(confirmFriendlyButton).bottom();
        this.add(cancelFriendlyButton).bottom();
        
        //
        updateDynamicComponents();
    }
    
    //
    public void updateDynamicComponents() {
        
        //
        if (selectedDate != null) {
            
            selectedDateLabel.setText(dateFormatter.format(selectedDate));
            
        } else {
            
            selectedDateLabel.setText(LanguageModLoader.getValue("select_a_date"));
        }
        
        // 
        if (currentClub != null) {
            
            /**
             * List of Clubs (do not include current club)
             * 
             *  - TODO: this component is difficult because should allow to do some filtering at least
             *  - TODO: component shows opponent level/fame
             *  
             *  ---
             *  TODO unhardcode available clubs for friendly
             *  TODO filter out current club
             *  TODO filter out clubs with matches on the selected day so they cannot play/accept other match
             *  
             *  TODO UNHARDCODE availableClubs BEFORE WE IMPLEMENT MORE TEAMS/LEAGUES
             */
            List<Club> availableClubs = new ArrayList<>();
            
            // 
            availableClubs.add(new Club("CHOOSE"));
            
            //
            availableClubs.addAll(DatabaseLoader.getClubsByCountry().get(1000L));
            
            // Remove Current Club Hack
            for (Club club : availableClubs) {
                
                // Ignore null id club (CHOOSE item)
                if (club.getId() != null
                        && club.getId().equals(currentClub.getId())) {
                    
                    //
                    availableClubs.remove(club);
                    break;
                }
            }
                    
            opponentSelectBox.setItems(availableClubs.toArray(new Club[availableClubs.size()]));
        }
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }

    public LocalDateTime getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDateTime selectedDate) {
        this.selectedDate = selectedDate;
    }
}