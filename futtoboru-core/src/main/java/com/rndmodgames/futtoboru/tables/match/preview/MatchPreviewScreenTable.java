package com.rndmodgames.futtoboru.tables.match.preview;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.cup.NeutralVenues;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.tables.squad.TeamSheetEditor;
import com.rndmodgames.futtoboru.ui.GroundArt;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Match Preview Screen Table v1
 * 
 *  - basic:
 *      - home and away clubs TBD
 *      - venue TBD
 *      - tickets/attendance/money TBD
 *      - basic players/squads/lineups/etc TBD
 *      
 *      - estimated odds TBD
 *      - league position TBD
 *      - recent form TBD
 *      
 *      - prefered formation TBD
 *      
 *      - previous meetings TBD
 *      
 *      - team news TBD 
 *          - injuries TBD 
 *          - red cards TBD
 *          
 *      - opponent report component TBD
 *      
 *      - match information TBD
 *          - kick off time at stadium TBD
 *          - tickets sold out of / ticket capacity TBD
 *          - pitch status / etc TBD
 *          
 *          - weather TBD
 *          
 *          - referee TBD
 *          
 *      - fixtures component per team TBD
 *      
 *      - todays fixtures component TBD
 *      
 *      
 *      
 *      getting into this screen will change the button to MATCH RESULT
 *          
 *      after match result screen gets back to CONTINUE GAME
 * 
 * Sources:
 * 
 *  - https://2.bp.blogspot.com/-WhZ0gY2RBG0/WeoXOMZDBgI/AAAAAAAAYrg/xmfeVwfNXHcrHaJKmlGwOb8-IV3xebDlACLcBGAs/s1600/Match%2BPreview.png
 *  - https://content.invisioncic.com/Msigames/monthly_2020_12/A1FB6613-5699-42A5-96C5-7C88795EBF61.png.7a77a8b94d1f3590ae38c7d64c248d3d.png
 *  - https://assets2.ignimgs.com/2007/10/12/worldwide-soccer-manager-2008-20071012055657770-2159799.jpg
 * 
 * @author Geomancer86
 */
public class MatchPreviewScreenTable extends VisTable {

    //
    Futtoboru game;
    
    // Dynamic Components
    VisTable mainTable = new VisTable(true);
    
    public MatchPreviewScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
    }
    
    // 
    public void updateDynamicComponents() {
        
        this.clear();
        
        // Find the next playable match (both teams determined, not played, date is today or past)
        Match match = null;
        Club currentClub = game.getCurrentGame().getCurrentClub();
        java.time.LocalDateTime today = game.getCurrentGame().getGameDate();
        
        if (currentClub.getScheduledMatches() != null) {
            for (Match m : currentClub.getScheduledMatches()) {
                if (m == null) continue;
                
                // Skip matches where teams are not yet determined (future round cup matches)
                if (m.getHomeClubId() == null || m.getAwayClubId() == null) {
                    continue;
                }
                
                // Skip matches already played
                if (m.getIsPlayed() != null && m.getIsPlayed()) {
                    continue;
                }
                
                // Check if match date is today or in the past
                if (m.getMatchDateTime() != null && 
                    (m.getMatchDateTime().toLocalDate().isBefore(today.toLocalDate()) ||
                     m.getMatchDateTime().toLocalDate().isEqual(today.toLocalDate()))) {
                    match = m;
                    break; // Use first playable match found
                }
            }
        }
        
        if (match == null) {
            this.row();
            this.add(LanguageModLoader.getValue("match_no_upcoming"));
            return;
        }
        
        // Get clubs with null checks
        Club homeClub = match.getHomeClubId() != null ? 
            DatabaseLoader.getClubById(match.getHomeClubId()) : null;
        Club awayClub = match.getAwayClubId() != null ? 
            DatabaseLoader.getClubById(match.getAwayClubId()) : null;
        
        if (homeClub == null || awayClub == null) {
            this.row();
            this.add(LanguageModLoader.getValue("match_teams_undecided"));
            if (match.getBracketPath() != null) {
                this.row();
                this.add(LanguageModLoader.format("match_bracket", match.getBracketPath()));
            }
            return;
        }

        // Header: the two clubs, then the venue with its picture (U-11)
        this.row();
        VisLabel fixture = new VisLabel(homeClub.getName() + " " + LanguageModLoader.getValue("match_vs") + " "
                + awayClub.getName());
        fixture.setFontScale(UiTheme.fontScale("heading"));
        this.add(fixture).center();

        // Venue: the match's neutral ground (P2-14: a cup semi-final or final, e.g. Kennington Oval), else the home
        // club's season ground
        Stadium venue = NeutralVenues.stadiumOf(match, game.getCurrentGame());
        if (venue != null) {
            this.row();
            String line = LanguageModLoader.getValue("ground_venue") + ": " + venue.getName();
            if (NeutralVenues.isNeutral(match)) {
                line += " " + LanguageModLoader.getValue("ground_neutral_suffix");
            }
            this.add(new VisLabel(line)).center();
        }
        this.row();
        this.add(game.getGroundArt().picture(GroundArt.groundId(venue), false, UiTheme.size("art.ground.panel.w"),
                UiTheme.size("art.ground.panel.h"))).center().pad(UiTheme.space("sm"));

        // E-21f: with the ECS engine the match can be watched (the instant engine has nothing to show)
        if (game.getGameEngine() != null && game.getGameEngine().canWatchMatch()) {
            VisTextButton watch = new VisTextButton(LanguageModLoader.getValue("watch_match"),
                    UiTheme.style("button.primary"));
            watch.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    com.rndmodgames.futtoboru.menu.MainMenuManager menu = game.getGameEngine().getMainMenuManager();
                    if (menu != null) {
                        menu.watchMatch();
                    }
                }
            });
            this.row();
            this.add(watch).center();
        }

        // G-01: both team sheets; the player's club's stays editable until kick-off, the other is the opponent's pick
        VisTable sheets = new VisTable(true);
        addSheet(sheets, match, homeClub, true);
        addSheet(sheets, match, awayClub, false);
        this.row();
        this.add(sheets).center().top();
    }

    private void addSheet(VisTable sheets, Match match, Club fallback, boolean home) {
        SaveGame save = game.getCurrentGame();
        Club club = save.getClubById(home ? match.getHomeClubId() : match.getAwayClubId());
        if (club == null) {
            club = fallback;
        }
        VisTable column = new VisTable(true);
        VisLabel title = new VisLabel(club.getName());
        title.setFontScale(UiTheme.fontScale("subheading"));
        title.setColor(UiTheme.color("text.heading"));
        column.add(title).left();
        column.row();
        boolean own = TeamSheets.isManaged(club, save);
        TeamSheetEditor editor = new TeamSheetEditor();
        editor.show(club, TeamSheets.preview(match, club, home, save, game.random()), own, null);
        column.add(editor).left().top();
        sheets.add(column).top().left().padLeft(UiTheme.space("md")).padRight(UiTheme.space("md"));
    }
}