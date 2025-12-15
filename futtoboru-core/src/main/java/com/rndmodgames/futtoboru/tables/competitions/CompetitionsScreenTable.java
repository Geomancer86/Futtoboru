package com.rndmodgames.futtoboru.tables.competitions;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.League;
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
        
        // Debug logging
        System.out.println("CompetitionsScreenTable.updateDynamicComponents() called");
        
        // Title
        this.row().colspan(3);
        VisLabel titleLabel = new VisLabel("COMPETITIONS");
        titleLabel.setFontScale(1.2f);
        this.add(titleLabel).pad(10).row();
        
        this.addSeparator().colspan(3).pad(5).row();
        
        // Check if mainAuthority exists
        if (currentGame == null) {
            this.row().colspan(3);
            this.add(new VisLabel("ERROR: SaveGame is null")).pad(10).row();
            return;
        }
        
        if (currentGame.getMainAuthority() == null) {
            this.row().colspan(3);
            this.add(new VisLabel("No Football Association found")).pad(10).row();
            return;
        }
        
        // Get leagues
        List<League> leagues = currentGame.getMainAuthority().getLeagues();
        
        if (leagues == null || leagues.isEmpty()) {
            this.row().colspan(3);
            this.add(new VisLabel("No leagues found. League creation script may not have executed yet.")).pad(10).row();
            this.row().colspan(3);
            this.add(new VisLabel("Check console logs for league creation messages.")).pad(5).row();
            return;
        }
        
        System.out.println("Found " + leagues.size() + " leagues to display");
        
        // Header row
        this.row().pad(5);
        this.add(new VisLabel("League Name")).width(300);
        this.add(new VisLabel("Clubs")).width(100);
        this.add(new VisLabel("Country")).width(150);
        this.row();
        this.addSeparator().colspan(3).pad(2).row();
        
        // Display each league
        for (League league : leagues) {
            if (league == null) {
                continue;
            }
            
            // League name (clickable in future)
            VisTextButton leagueButton = new VisTextButton(league.getName() != null ? league.getName() : "Unnamed League");
            leagueButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    // TODO: Navigate to league detail screen
                    System.out.println("Clicked on league: " + league.getName());
                }
            });
            
            this.row().pad(2);
            this.add(leagueButton).width(300);
            
            // Number of clubs
            int clubCount = (league.getLeagueClubs() != null) ? league.getLeagueClubs().size() : 0;
            this.add(new VisLabel(String.valueOf(clubCount))).width(100);
            
            // Country
            String countryName = (league.getCountry() != null && league.getCountry().getCommonName() != null) 
                ? league.getCountry().getCommonName() 
                : "Unknown";
            this.add(new VisLabel(countryName)).width(150);
        }
        
        // Add spacing at bottom
        this.row();
        this.add().height(20).colspan(3).row();
        
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