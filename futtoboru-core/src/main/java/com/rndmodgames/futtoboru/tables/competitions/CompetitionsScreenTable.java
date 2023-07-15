package com.rndmodgames.futtoboru.tables.competitions;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Competitions Screen Table v1
 * 
 *  - This screen will show all the Competitions the Club is registered to play on
 *  
 *      - National Cups
 *      - Leagues
 *      - International Cups (TBD)
 *      - Relegation/Promotion (TBD)
 *      - SuperCups (league winners vs cup winners) (TBD)
 *      - Etc.
 *  
 *  - Each Competition name will be a Link Type button/label and clicking it will redirect
 *      to the Competition Detail Screen showing:
 *          
 *          - Founding Year
 *          - Past editions history:
 *              - Invited Teams
 *              - Participating Teams
 *              - Match History         (TBD)
 *              - Winner, Runner Up
 *              - Prizes
 *              
 *      The Competition Detail Page will show 
 *          
 *          - When the next edition starts and ends
 *          - When the CUP DRAW will take place
 * 
 * @author Geomancer86
 */
public class CompetitionsScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    public CompetitionsScreenTable(Game parent) {
    
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    //
    public void updateDynamicComponents() {

        //
        this.clear();
        
        // 
        this.row().colspan(2);
        this.add("CLUB COMPETITIONS SCREEN - WORK IN PROGRESS!");
        
        // Club Cups
        this.row();
        this.add("club_cups");
        this.add("FA CUP");
        
        // Club Leagues
        this.row();
        this.add("club_leagues");
        this.add("English League");
        

        /**
         * TODO WIP:
         *  
         *      - Competitions Loader:
         *          - Load FA CUP from file
         *              - Load history, year created
         *              - Load past winners
         *                  - year
         *                  - invited clubs
         *                  - participant clubs
         *                  - winner, runner ups, etc
         *                  
         *              - We need hundreds of ghost teams
         *              
         *      - Authority:
         *          - Current Year (1888-89) Cup Rules
         *          - Show all invited teams
         *          - Show all participant teams
         *              - Needs to be preloaded on file system for historic reasons
         *              - Next cups are replied randomly (more reputation more %, try to get the historic % participating)
         *              
         *          - Cup Draw: interactive/automatic
         *          - Cup Match Scheduling: automatic
         *          
         *          - Cup Playoffs and Rules:
         *              - Byes
         *              - Indefinite Rematches (no penalties)
         *              
         *          - Competition Prizes
         */
        
    }
}