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
        
        //
        updateDynamicComponents();
    }
    
    // 
    public void updateDynamicComponents() {
        
        this.clear();
        
        // Current Match Preview will always be the first on the Scheduled Match List
        Match match = game.getCurrentGame().getCurrentClub().getScheduledMatches().get(0);
        
        //
        Club homeClub = DatabaseLoader.getClubById(match.getHomeClubId());
        Club awayClub = DatabaseLoader.getClubById(match.getAwayClubId());

        //
        this.row();
        this.add(homeClub.getName() + " vs " + awayClub.getName());
    }
}