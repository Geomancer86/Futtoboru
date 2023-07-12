package com.rndmodgames.futtoboru.tables.schedule;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.components.AutoFocusScrollPane;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Schedule Screen Table v1
 * 
 * SOURCE:
 *  - https://dictatethegame.com/wp-content/uploads/2022/12/20221212112104_1.jpg
 *  - https://cdn.footballmanager.com/site/inline-images/1%20-.png
 * 
 * @author Geomancer86
 */
public class ScheduleScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    //
    private Club currentClub;
    
    // Dynamic Components
    FixturesTable fixturesTable = null;
    ArrangeFriendlyTable arrangeFriendlyTable = null;
    
    public ScheduleScreenTable(Game parent) {
        
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();

        // Fixtures Table
        fixturesTable = new FixturesTable(game, this);
        
        // Fixtures ScrollPane (auto scrollable without clicking)
        AutoFocusScrollPane fixturesScrollPane = new AutoFocusScrollPane(fixturesTable);
        
        // Arrange Friendly Table
        arrangeFriendlyTable = new ArrangeFriendlyTable(game, this);
       
        this.row();
        this.add(fixturesScrollPane).fill();
        this.add(arrangeFriendlyTable);
        
        /**
         * TODO 
         *  
         *  The main difference between scheduled and scripted is that we can see the schedules in-game.
         * 
         *  - Schedule Data Structure:
         *      
         *      - Scheduled Match
         *      - Scheduled Transfer
         *      - Etc.
         *      
         *      Match:
         *          - LocalDateTime
         *          - Home Team
         *          - Away Team
         *          - Venue
         *          - isScheduled (is time set)
         *          - Competition
         *          - Match Type
         *          - Rules
         *          - Fees
         *          - Etc
         *          
         *  ---
         *  League Scheduler
         *  
         *      - Venue cannot be used twice the same day / time
         *      - Teams should not play twice the same day or without proper rest in between matches
         *      - Scheduler will "ask" the teams and be "fair"? maybe that is a parameter or AI
         *      - Best standing teams should be given best timeslots
         *      - Sell tickets through the week until match day
         *  
         *  Board
         *      - Budget / Account the ticket sales
         *      - Salary Budged calculated by the Board doing some quick average numbers
         *          - Seats x Tickets x Matchess x Average Position multiplier x Expected Position x Prices % Expected (etc) 
         *          - Release players to be under budget
         *          - Loans
         *          - Free players
         *          - Etc
         *          
         */
    }
    
    /**
     * TODO WIP:
     * 
     *  - Add Friendly Button
     *  - Scheduled Events / Matches List
     *  - 
     *  
     *  
     *  
     */
    public void updateDynamicComponents() {
        
        //
        fixturesTable.setCurrentClub(currentClub);
        arrangeFriendlyTable.setCurrentClub(currentClub);
        
        //
        arrangeFriendlyTable.setSelectedDate(fixturesTable.getSelectedDate());
        
        //
        fixturesTable.updateDynamicComponents();
        arrangeFriendlyTable.updateDynamicComponents();
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}