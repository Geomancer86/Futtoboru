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
    DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
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
         * 
         * TODO: MAKE SCROLLABLE TO AVOID FILLING THE SCREEN ON LONG SEASONS
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
         * Automatic Days By Current Date & Current Season Start Date
         */
        int renderWeeks = 10;
        int renderDays = renderWeeks * 7; // TODO: render until the end of season or until no more schedules can be done
        
        LocalDateTime seasonStartDate = currentGame.getGameStartDate();
        LocalDateTime currentDate = currentGame.getGameDate();
        
        // Calculated Earlier Monday (Start of Week)
        LocalDateTime firstMonday = LocalDateTime.from(currentDate).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        
        // Calculated Season End (Number of Weeks)
        LocalDateTime seasonEnd = firstMonday.plusDays(renderDays);
        
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
         * WEEKLY CALENDAR PROTOTYPE
         */
        matchListTable.row();
        matchListTable.add(LanguageModLoader.getValue("MONDAY"));
        matchListTable.add(LanguageModLoader.getValue("TUESDAY"));
        matchListTable.add(LanguageModLoader.getValue("WEDNESDAY"));
        matchListTable.add(LanguageModLoader.getValue("THURSDAY"));
        matchListTable.add(LanguageModLoader.getValue("FRIDAY"));
        matchListTable.add(LanguageModLoader.getValue("SATURDAY"));
        matchListTable.add(LanguageModLoader.getValue("SUNDAY"));
        
        /**
         * Iterate days starting from firstMonday
         */
        int renderedDays = 0;
        boolean isCurrentDay = false;
        
        //
        matchListTable.row();
        
        // Iterate Season Dates
        // Keep count of days for Week Rendering
        for (LocalDateTime renderDate = firstMonday; renderDate.isBefore(seasonEnd); renderDate = renderDate.plusDays(1)) {
            
            // Current Day will be selected by default
            isCurrentDay = renderDate.isEqual(currentDate);
            
            // Add Calendar Cell
            matchListTable.add(getDailyCalendarTable(shortFormatter.format(renderDate), null, isCurrentDay));
            
            // Keep track of rendered days for weekly breaks / new rows
            renderedDays++;
            
            System.out.println("RENDERED " + renderedDays + " DAYS");
            
            // New Week, New Row
            if (renderedDays % 7 == 0) {

                System.out.println("END OF WEEK NEW ROW");
                matchListTable.row();
            }
        }
    }
    
    public VisTable getDailyCalendarTable(String dateLabel, String matchLabel, Boolean selected) {
        
        VisTable table = new VisTable(true);
        
        table.add(dateLabel);
        
        return table;
    }
    
    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}