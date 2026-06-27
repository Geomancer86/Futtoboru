package com.rndmodgames.futtoboru.tables.match.preview;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

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
            this.add("No upcoming match available");
            return;
        }
        
        // Get clubs with null checks
        Club homeClub = match.getHomeClubId() != null ? 
            DatabaseLoader.getClubById(match.getHomeClubId()) : null;
        Club awayClub = match.getAwayClubId() != null ? 
            DatabaseLoader.getClubById(match.getAwayClubId()) : null;
        
        if (homeClub == null || awayClub == null) {
            this.row();
            this.add("Match teams not yet determined");
            if (match.getBracketPath() != null) {
                this.row();
                this.add("Match: " + match.getBracketPath());
            }
            return;
        }

        //
        this.row();
        this.add(homeClub.getName() + " vs " + awayClub.getName());
    }
}