package com.rndmodgames.futtoboru.tables.schedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

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
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);
    
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
        
        /**
         * WEEKLY CALENDAR PROTOTYPE
         */
        matchListTable.add(LanguageModLoader.getValue("MONDAY"));
        matchListTable.add(LanguageModLoader.getValue("TUESDAY"));
        matchListTable.add(LanguageModLoader.getValue("WEDNESDAY"));
        matchListTable.add(LanguageModLoader.getValue("THURSDAY"));
        matchListTable.add(LanguageModLoader.getValue("FRIDAY"));
        matchListTable.add(LanguageModLoader.getValue("SATURDAY"));
        matchListTable.add(LanguageModLoader.getValue("SUNDAY"));
        
        /**
         * Automatic Days By Current Date & Current Season Start Date
         */
        LocalDateTime seasonStartDate = currentGame.getGameStartDate();
        LocalDateTime currentDate = currentGame.getGameDate();
        
        LocalDateTime firstMonday = LocalDateTime.from(currentDate).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        
        //
        matchListTable.row();
        matchListTable.add("Season Start Date");
        matchListTable.add(formatter.format(seasonStartDate));
        
        //
        matchListTable.row();
        matchListTable.add("Current Date");
        matchListTable.add(formatter.format(currentDate));
        
        //
        matchListTable.row();
        matchListTable.add("First Season Monday");
        matchListTable.add(formatter.format(firstMonday));
        
        /**
         * - Calculate the earliest Monday to be the first day to show on the Calendar
         * - Mark the Current Day with a Highlight / Marker
         * - The Calendar cannot be infinite, mark the last day as the end of the Season or Start + 365 days
         * 
         * - Show Match Days
         * - Selected Calendar Cell will set a Selected Date.
         */
        
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