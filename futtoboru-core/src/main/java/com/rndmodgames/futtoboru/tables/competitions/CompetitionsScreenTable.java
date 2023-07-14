package com.rndmodgames.futtoboru.tables.competitions;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Competitions Screen Table v1
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