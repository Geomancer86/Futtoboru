package com.rndmodgames.futtoboru.tables.match.history;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Match History Screen Table v1
 * 
 * TODO: WIP
 * 
 *  -
 * 
 * sources:
 *      - https://cdn.footballmanager.com/site/inline-images/Watford_%20Supporters.png
 *      - https://content.invisioncic.com/Msigames/monthly_2021_01/Example01.jpg.657daeda224e917f2763020b2c0e443e.jpg
 *      - https://fminside.net/resources/uploads/img/Home-screen-2-1536x932.png
 * 
 * @author Geomancer86
 */
public class MatchHistoryScreenTable extends VisTable {

    //
    Futtoboru game;
    
    // Dynamic Club
    private Club currentClub;
    
    // Dynamic Components
    VisTable mainTable = new VisTable(true);
    
    public MatchHistoryScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
    }
    
    // 
    public void updateDynamicComponents() {
        
        System.out.println("SHOWING MATCH HISTORY SCREEN - PLAYED MATCHES: " + currentClub.getPlayedMatches().size());
        
        this.clear();
        
        /**
         * TODO WIP:
         * 
         *      - iterate all played matches and show them on a basic list with opponent name, venue and result,
         *      - you can click them and will be redirected to the Match Result screen for the match details
         *      
         *  TODO:
         *      - the played matches list might become too big so we need a way to archive them
         *      - past seasons or years, paginated
         */
        for (Match match : currentClub.getPlayedMatches()) {

            //
            Club homeClub = DatabaseLoader.getClubById(match.getHomeClubId());
            Club awayClub = DatabaseLoader.getClubById(match.getAwayClubId());

            // New Row
            this.row();
            
            // Clubs
            this.add(homeClub.getName() + " vs " + awayClub.getName()).colspan(2);
            
            // Attendance
            this.row();
            this.add("Attendance");
            this.add(match.getAttendance() + "");
            
            // Match Separator
            this.row();
            this.addSeparator().colspan(2);
        }
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}