package com.rndmodgames.futtoboru.tables.match.result;

import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Match Result Screen Table v1
 * 
 *  - This screen will be accessed immediately after a Match is played with the relevant Match Results
 *  
 *  - Displays match details: teams, score, date, attendance, venue
 * 
 * @author Geomancer86
 */
public class MatchResultScreenTable extends VisTable {

    //
    Futtoboru game;
    
    // Dynamic Components
    VisTable mainTable = new VisTable(true);
    
    //
    public MatchResultScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
    }
    
    //
    public void updateDynamicComponents() {
        
        //
        this.clear();
        
        // Get current club
        Club currentClub = game.getCurrentGame().getCurrentClub();
        
        if (currentClub == null || currentClub.getPlayedMatches() == null || currentClub.getPlayedMatches().isEmpty()) {
            this.add(LanguageModLoader.getValue("match_no_results"));
            return;
        }
        
        // Get the most recently played match (last in chronological order)
        Match match = currentClub.getPlayedMatches().get(currentClub.getPlayedMatches().size() - 1);
        
        // Get clubs
        Club homeClub = game.getCurrentGame().getClubById(match.getHomeClubId());
        Club awayClub = game.getCurrentGame().getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            this.add(LanguageModLoader.getValue("match_load_error"));
            Log.error(Log.UI_MATCH, "MatchResultScreenTable", "Home or away club not found for match");
            return;
        }
        
        // Format date
        DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.MEDIUM); // U-12: the game language, not the machine's
        DateTimeFormatter timeFormatter = GameFormat.formatter(GameFormat.TIME);
        String matchDate = match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : LanguageModLoader.getValue("unknown");
        String matchTime = match.getMatchDateTime() != null ? match.getMatchDateTime().format(timeFormatter) : LanguageModLoader.getValue("unknown");
        
        // Match Header
        this.row().padTop(UiTheme.space("lg"));
        VisLabel headerLabel = new VisLabel(LanguageModLoader.getValue("match_result_title"));
        headerLabel.setFontScale(UiTheme.fontScale("display"));
        this.add(headerLabel).colspan(2).padBottom(UiTheme.space("lg"));
        
        // Match Date and Time
        this.row();
        this.add(LanguageModLoader.getValue("match_date")).padRight(UiTheme.space("sm"));
        this.add(LanguageModLoader.format("match_date_time", matchDate, matchTime)).left();
        
        // Venue
        this.row().padTop(UiTheme.space("xs"));
        this.add(LanguageModLoader.getValue("match_venue")).padRight(UiTheme.space("sm"));
        String venueName = homeClub.getStadium() != null && homeClub.getStadium().getName() != null 
            ? homeClub.getStadium().getName() 
            : LanguageModLoader.getValue("match_unknown_ground");
        this.add(venueName).left();
        
        // Separator
        this.row().padTop(UiTheme.space("md")).padBottom(UiTheme.space("md"));
        this.addSeparator().colspan(2);
        
        // Teams and Score
        this.row().padTop(UiTheme.space("sm"));
        
        // Home Team
        VisTable homeTeamTable = new VisTable();
        homeTeamTable.add(new VisLabel(homeClub.getName())).row();
        homeTeamTable.add(new VisLabel(String.valueOf(match.getHomeGoals() != null ? match.getHomeGoals() : 0)))
            .padTop(UiTheme.space("xs"));
        this.add(homeTeamTable).expandX().center();
        
        // VS
        this.add(new VisLabel(LanguageModLoader.getValue("match_vs"))).padLeft(UiTheme.space("lg")).padRight(UiTheme.space("lg"));
        
        // Away Team
        VisTable awayTeamTable = new VisTable();
        awayTeamTable.add(new VisLabel(awayClub.getName())).row();
        awayTeamTable.add(new VisLabel(String.valueOf(match.getAwayGoals() != null ? match.getAwayGoals() : 0)))
            .padTop(UiTheme.space("xs"));
        this.add(awayTeamTable).expandX().center();
        
        // Result
        this.row().padTop(UiTheme.space("md"));
        String result;
        if (match.getHomeGoals() != null && match.getAwayGoals() != null) {
            if (match.getHomeGoals() > match.getAwayGoals()) {
                result = LanguageModLoader.format("match_won", homeClub.getName());
            } else if (match.getAwayGoals() > match.getHomeGoals()) {
                result = LanguageModLoader.format("match_won", awayClub.getName());
            } else {
                result = LanguageModLoader.getValue("match_draw");
            }
        } else {
            result = LanguageModLoader.getValue("match_no_result");
        }
        VisLabel resultLabel = new VisLabel(result);
        resultLabel.setFontScale(UiTheme.fontScale("heading"));
        this.add(resultLabel).colspan(3).padBottom(UiTheme.space("lg"));
        
        // Separator
        this.row().padTop(UiTheme.space("md")).padBottom(UiTheme.space("md"));
        this.addSeparator().colspan(3);
        
        // Match Details
        this.row().padTop(UiTheme.space("sm"));
        this.add(LanguageModLoader.getValue("match_type_label")).padRight(UiTheme.space("sm"));
        String matchType = match.getMatchType() != null && match.getMatchType() == Match.FRIENDLY_MATCH 
            ? LanguageModLoader.getValue("match_type_friendly")
            : LanguageModLoader.getValue("match_type_competition");
        this.add(matchType).left();
        
        // Attendance
        this.row().padTop(UiTheme.space("xs"));
        this.add(LanguageModLoader.getValue("match_attendance")).padRight(UiTheme.space("sm"));
        String attendance = match.getAttendance() != null ? GameFormat.integer(match.getAttendance()) : LanguageModLoader.getValue("unknown");
        this.add(attendance).left();
        
        // Stadium capacity if available
        if (homeClub.getStadium() != null && homeClub.getStadium().getCapacity() != null) {
            this.row().padTop(UiTheme.space("xs"));
            this.add(LanguageModLoader.getValue("match_capacity")).padRight(UiTheme.space("sm"));
            this.add(GameFormat.integer(homeClub.getStadium().getCapacity())).left();
        }
    }
}