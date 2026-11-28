package com.rndmodgames.futtoboru.tables.match.result;

import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.engine.cup.NeutralVenues;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.ui.GroundArt;
import com.rndmodgames.futtoboru.ui.UiTheme;
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
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
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
        
        // Venue: the neutral ground of a cup semi-final or final (P2-14), else the home club's, with its picture
        Stadium ground = NeutralVenues.stadiumOf(match, game.getCurrentGame());
        this.row().padTop(UiTheme.space("xs"));
        this.add(LanguageModLoader.getValue("match_venue")).padRight(UiTheme.space("sm"));
        String venueName = ground != null && ground.getName() != null
            ? ground.getName()
            : LanguageModLoader.getValue("match_unknown_ground");
        if (NeutralVenues.isNeutral(match)) {
            venueName += " " + LanguageModLoader.getValue("ground_neutral_suffix");
        }
        VisTable venueCell = new VisTable(true);
        venueCell.add(game.getGroundArt().picture(GroundArt.groundId(ground), true, UiTheme.size("art.thumb.w"),
                UiTheme.size("art.thumb.h")));
        venueCell.add(new VisLabel(venueName)).left();
        this.add(venueCell).left();
        
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
        String attendance = match.getAttendance() != null ? String.valueOf(match.getAttendance()) : LanguageModLoader.getValue("unknown");
        this.add(attendance).left();
        
        // Capacity of the ground played at, if known
        if (ground != null && ground.getCapacity() != null) {
            this.row().padTop(UiTheme.space("xs"));
            this.add(LanguageModLoader.getValue("match_capacity")).padRight(UiTheme.space("sm"));
            this.add(String.valueOf(ground.getCapacity())).left();
        }

        // P2-14: who took the gate at a neutral ground (1888-89: the Football Association)
        if (NeutralVenues.isNeutral(match)) {
            this.row().padTop(UiTheme.space("xs"));
            this.add(LanguageModLoader.getValue("match_gate")).padRight(UiTheme.space("sm"));
            NeutralVenues.Gate gate = NeutralVenues.gateOf(match, game.getCurrentGame());
            this.add(LanguageModLoader.getValue(gate == NeutralVenues.Gate.ASSOCIATION ? "match_gate_association"
                    : gate == NeutralVenues.Gate.SHARED ? "match_gate_shared" : "match_gate_home")).left();
        }
    }
}