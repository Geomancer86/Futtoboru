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
        
        /**
         * In this screen we will list the Current Club Competitions
         * 
         * Clicking on a Competition Name Label will send the user to the Competition Details Screen for that competition
         * 
         * TODO:
         *  - competitions x clubs relationship
         *  
         *  - CompetitionEdition
         *      - Name
         *      - Description
         *      - Competition ID
         *      - Edition
         *      - Start Date
         *      - End Date
         *      - Participant Clubs
         *      - Rules
         *      
         *      // then you have the scheduled matches, match results, etc. (related objects)
         *      
         *      
         *  - get club affiliations: country/top level (we need to simulate the football association because that's different from the authority)
         *  - get club competitions: country/top level
         *  
         */
        
        this.row().colspan(2);
        this.add("CLUB COMPETITIONS SCREEN");
        
        /**
         * The Club might be associated or not, a club needs association to participate of most games / competitions
         */
        this.row();
        this.add("club_association");
        this.add("PLACEHOLDER - WIP");
        
//        currentGame.getCurrentClub()
        
        /**
         * The club can participate in many cup style competitions, as long as scheduling permits
         */
        this.row();
        this.add("club_cups");
        this.add("PLACEHOLDER - WIP");
        
        /**
         * The club can participate in many leagues but more than one doesn't make sense and usually it's just one
         * 
         * NOTE: historically in 1888 there were two leagues but just one complete (and is still played today) the other wasn't finished,
         *          but some teams played both leagues that year.
         */
        this.row();
        this.add("club_league");
        this.add("PLACEHOLDER - WIP");
        
        // 
//        this.row().colspan(2);
//        this.add("CLUB COMPETITIONS SCREEN - WORK IN PROGRESS!");
        
        /**
         * TODO: WIP:
         * 
         *  - Load existing competitions from file system: this will only load the existing FA CUP and previous seasons history as needed/loaded
         *  - We need a way to differentiate teams like:
         *      - PLAYABLE
         *      - GHOST
         *      
         *  - The 18th Edition of the FA CUP has hundreds of invited and participant teams
         *  
         *  - After we have everything loaded, we need to simulate/script the CUP DRAW:
         *  
         *  - randomly pick teams one by one according to the rules
         *      
         *      - THIS YEAR RULES: seems like the league teams might get the first round for free (research!)
         *      
         *      
         */
        
//        // Club Cups
//        this.row();
//        this.add("club_cups");
//        this.add("FA CUP");
//        
//        // Club Leagues
//        this.row();
//        this.add("club_leagues");
//        this.add("English League");
        

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