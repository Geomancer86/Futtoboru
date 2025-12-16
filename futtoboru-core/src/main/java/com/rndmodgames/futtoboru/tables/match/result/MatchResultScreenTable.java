package com.rndmodgames.futtoboru.tables.match.result;

import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;

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
            this.add("No match results available.");
            return;
        }
        
        // Get the most recently played match (last in chronological order)
        Match match = currentClub.getPlayedMatches().get(currentClub.getPlayedMatches().size() - 1);
        
        // Get clubs
        Club homeClub = game.getCurrentGame().getClubById(match.getHomeClubId());
        Club awayClub = game.getCurrentGame().getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            this.add("Error: Could not load match details.");
            Gdx.app.error("MatchResultScreenTable", "Home or away club not found for match");
            return;
        }
        
        // Format date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String matchDate = match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : "Unknown";
        String matchTime = match.getMatchDateTime() != null ? match.getMatchDateTime().format(timeFormatter) : "Unknown";
        
        // Match Header
        this.row().padTop(20);
        VisLabel headerLabel = new VisLabel("MATCH RESULT");
        headerLabel.setFontScale(1.5f);
        this.add(headerLabel).colspan(2).padBottom(20);
        
        // Match Date and Time
        this.row();
        this.add("Date:").padRight(10);
        this.add(matchDate + " at " + matchTime).left();
        
        // Venue
        this.row().padTop(5);
        this.add("Venue:").padRight(10);
        String venueName = homeClub.getStadium() != null && homeClub.getStadium().getName() != null 
            ? homeClub.getStadium().getName() 
            : "Unknown Stadium";
        this.add(venueName).left();
        
        // Separator
        this.row().padTop(15).padBottom(15);
        this.addSeparator().colspan(2);
        
        // Teams and Score
        this.row().padTop(10);
        
        // Home Team
        VisTable homeTeamTable = new VisTable();
        homeTeamTable.add(new VisLabel(homeClub.getName())).row();
        homeTeamTable.add(new VisLabel(String.valueOf(match.getHomeGoals() != null ? match.getHomeGoals() : 0)))
            .padTop(5);
        this.add(homeTeamTable).expandX().center();
        
        // VS
        this.add(new VisLabel("vs")).padLeft(20).padRight(20);
        
        // Away Team
        VisTable awayTeamTable = new VisTable();
        awayTeamTable.add(new VisLabel(awayClub.getName())).row();
        awayTeamTable.add(new VisLabel(String.valueOf(match.getAwayGoals() != null ? match.getAwayGoals() : 0)))
            .padTop(5);
        this.add(awayTeamTable).expandX().center();
        
        // Result
        this.row().padTop(15);
        String result;
        if (match.getHomeGoals() != null && match.getAwayGoals() != null) {
            if (match.getHomeGoals() > match.getAwayGoals()) {
                result = homeClub.getName() + " won";
            } else if (match.getAwayGoals() > match.getHomeGoals()) {
                result = awayClub.getName() + " won";
            } else {
                result = "Draw";
            }
        } else {
            result = "Result not available";
        }
        VisLabel resultLabel = new VisLabel(result);
        resultLabel.setFontScale(1.2f);
        this.add(resultLabel).colspan(3).padBottom(20);
        
        // Separator
        this.row().padTop(15).padBottom(15);
        this.addSeparator().colspan(3);
        
        // Match Details
        this.row().padTop(10);
        this.add("Match Type:").padRight(10);
        String matchType = match.getMatchType() != null && match.getMatchType() == Match.FRIENDLY_MATCH 
            ? "Friendly" 
            : "Competition";
        this.add(matchType).left();
        
        // Attendance
        this.row().padTop(5);
        this.add("Attendance:").padRight(10);
        String attendance = match.getAttendance() != null ? String.valueOf(match.getAttendance()) : "Unknown";
        this.add(attendance).left();
        
        // Stadium capacity if available
        if (homeClub.getStadium() != null && homeClub.getStadium().getCapacity() != null) {
            this.row().padTop(5);
            this.add("Capacity:").padRight(10);
            this.add(String.valueOf(homeClub.getStadium().getCapacity())).left();
        }
    }
}