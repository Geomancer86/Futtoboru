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
        
        //
        this.add("MATCH HISTORY PLACEHOLDER - WORK IN PROGRESS");
        
        /**
         * Quick and dirty match history
         * 
         * NOTE:
         *  
         */
        for (Match match : currentClub.getPlayedMatches()) {

            //
            Club homeClub = DatabaseLoader.getClubById(match.getHomeClubId());
            Club awayClub = DatabaseLoader.getClubById(match.getAwayClubId());

            //
            this.row();
            this.add(homeClub.getName() + " vs " + awayClub.getName());
        }
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
}