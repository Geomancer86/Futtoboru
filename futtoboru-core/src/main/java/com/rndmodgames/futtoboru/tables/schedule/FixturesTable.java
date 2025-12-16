package com.rndmodgames.futtoboru.tables.schedule;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.EngineParameters;
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
    ScheduleScreenTable parentTable;
    
    //
    private Club currentClub;
    private LocalDateTime selectedDate;
    
    // Dynamic Components
    VisTable matchListTable = null;
    
    //
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);
    
    //
    public FixturesTable(Futtoboru parent, ScheduleScreenTable scheduleScreenTable) {
        
        // vis ui default spacing
        super(true);
        
        //
        this.game = parent;
        this.currentGame = game.getCurrentGame();
        
        //
        this.parentTable = scheduleScreenTable;
        
        /**
         * Club Current Matches
         */
//        this.add("FIXTURES");
//        this.row();
        
        /**
         * Match List Table
         */
        matchListTable = new VisTable(true);
        
        //
        this.add(matchListTable);
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
        int renderDays = renderWeeks * 12; // TODO: render until the end of season or until no more schedules can be done
        
//        LocalDateTime seasonStartDate = currentGame.getGameStartDate();
        LocalDateTime currentDate = currentGame.getGameDate();
        
        // Calculated Earlier Monday (Start of Week)
        LocalDateTime firstMonday = LocalDateTime.from(currentDate).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        
        // Calculated Season End (Number of Weeks)
        LocalDateTime seasonEnd = firstMonday.plusDays(renderDays);
        
        //
//        matchListTable.row();
//        matchListTable.add("Season Start Date");
//        matchListTable.add(formatter.format(seasonStartDate));
//        
//        //
//        matchListTable.row();
//        matchListTable.add("Current Date");
//        matchListTable.add(formatter.format(currentDate));
//        
//        //
//        matchListTable.row();
//        matchListTable.add("First Season Monday");
//        matchListTable.add(formatter.format(firstMonday));
        
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
        
        Club currentClub = currentGame.getCurrentClub();
        
        // Debug logging
        if (currentClub != null) {
            System.out.println("FixturesTable: Current club: " + currentClub.getName());
            System.out.println("FixturesTable: Proposed matches: " + (currentClub.getProposedMatches() != null ? currentClub.getProposedMatches().size() : 0));
            System.out.println("FixturesTable: Scheduled matches: " + (currentClub.getScheduledMatches() != null ? currentClub.getScheduledMatches().size() : 0));
        } else {
            System.out.println("FixturesTable: Current club is null (unemployed player)");
        }
        
        // Get all league matches if player is in a league
        java.util.List<Match> allLeagueMatches = new java.util.ArrayList<>();
        if (currentClub != null && currentGame.getMainAuthority() != null && currentGame.getMainAuthority().getLeagues() != null) {
            for (com.rndmodgames.futtoboru.data.League league : currentGame.getMainAuthority().getLeagues()) {
                if (league != null && league.getLeagueClubs() != null) {
                    // Check if current club is in this league
                    boolean isInLeague = false;
                    for (Club club : league.getLeagueClubs()) {
                        if (club != null && club.getId() != null && club.getId().equals(currentClub.getId())) {
                            isInLeague = true;
                            break;
                        }
                    }
                    
                    if (isInLeague) {
                        // Get all matches from all clubs in this league
                        for (Club club : league.getLeagueClubs()) {
                            if (club != null && club.getScheduledMatches() != null) {
                                for (Match match : club.getScheduledMatches()) {
                                    if (match != null && match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH) {
                                        // Check if not already in list
                                        boolean alreadyAdded = false;
                                        for (Match existing : allLeagueMatches) {
                                            if (existing.getId() != null && existing.getId().equals(match.getId())) {
                                                alreadyAdded = true;
                                                break;
                                            }
                                        }
                                        if (!alreadyAdded) {
                                            allLeagueMatches.add(match);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("FixturesTable: Found " + allLeagueMatches.size() + " league matches");
        
        // Iterate Season Dates
        // Keep count of days for Week Rendering
        for (LocalDateTime renderDate = firstMonday; renderDate.isBefore(seasonEnd); renderDate = renderDate.plusDays(1)) {
            
            // Current Day will be selected by default
            isCurrentDay = renderDate.isEqual(currentDate);
            
            /**
             * Find matches for this day
             */
            Match match = null;
            
            // First, check league matches
            for (Match leagueMatch : allLeagueMatches) {
                if (leagueMatch != null && leagueMatch.getMatchDateTime() != null && 
                    leagueMatch.getMatchDateTime().toLocalDate().equals(renderDate.toLocalDate())) {
                    match = leagueMatch;
                    break; // Found a match for this day
                }
            }
            
            // If no league match, check current club's proposed matches
            if (match == null && currentClub != null && currentClub.getProposedMatches() != null) {
                for (Match proposed : currentClub.getProposedMatches()) {
                    if (proposed != null && proposed.getMatchDateTime() != null &&
                        proposed.getMatchDateTime().toLocalDate().equals(renderDate.toLocalDate())) {
                        match = proposed;
                        break;
                    }
                }
            }
            
            // If still no match, check current club's scheduled matches
            if (match == null && currentClub != null && currentClub.getScheduledMatches() != null) {
                for (Match scheduled : currentClub.getScheduledMatches()) {
                    if (scheduled != null && scheduled.getMatchDateTime() != null &&
                        scheduled.getMatchDateTime().toLocalDate().equals(renderDate.toLocalDate())) {
                        match = scheduled;
                        break;
                    }
                }
            }
            
            // Add Calendar Cell
            matchListTable.add(getDailyCalendarTable(renderDate, shortFormatter.format(renderDate), match, isCurrentDay));

            // Keep track of rendered days for weekly breaks / new rows
            renderedDays++;

            // New Week, New Row
            if (renderedDays % 7 == 0) {

                // new week
                matchListTable.row();
            }
        }
        
//        matchListTable.pack();
    }
    
    /**
     * Clickable Table / Cell
     * 
     *  - Selected Day Checkbox or Button (for quick ID).
     *  - 
     */
    public VisTable getDailyCalendarTable(LocalDateTime date, String dateLabel, Match match, Boolean selected) {
        
        VisTable table = new VisTable(true);
        
        table.setTouchable(Touchable.enabled); 
        
        table.addListener(new ClickListener(){
            
            @Override
            public void clicked(InputEvent event, float x, float y) {
                
//                System.out.println("CLICKED DATE: " + formatter.format(date));
                
                // Set to be retrieved on other components
                selectedDate = date;
                
                //
                parentTable.updateDynamicComponents();
            }
        });
        
        /**
         * Calendar Cell Component
         * 
         *  Date
         *  Selected
         *  Match TYpe
         *  Vs Team
         *  Venue
         *  
         */
        table.add(dateLabel);
        
        /**
         * SELECTED DATE CHECK
         * 
         * defaults to Current Game Date on unselected
         */
        table.row();
        
        if (selectedDate == null) {
            if (date.isEqual(currentGame.getGameDate())) {
                
                table.add(new VisCheckBox("TODAY", true));
                
            }
        } else {
            if (selectedDate.isEqual(date)) {
                
                table.add(new VisCheckBox("SELECTED", true));
            }
        }
        
        //
        if (match != null) {
            
            // Match Type
            table.row();
            table.add(EngineParameters.getMatchType(match.getMatchType()));
            
            // Vs Team
            table.row();
            
            if (match.getHomeClubId().equals(currentClub.getId())) {
                
                // Show Away Team
                // TODO get club by id
                table.add(DatabaseLoader.getClubById(match.getAwayClubId()).getName());
                
                // Home Venue
                table.row();
                table.add("Home");
            } else {
                
                // Show Home Team
                // TODO get club by id
                table.add(DatabaseLoader.getClubById(match.getHomeClubId()).getName());
                
                // Away Venue
                table.row();
                table.add("Away");
            }
            
            // REQUEST v SCHEDULED
            if (match.getIsAccepted()) {
                
                table.row();
                table.add("SCHEDULED");
            } else {
                
                table.row();
                table.add("PROPOSED");
            }
        }
        
        return table;
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