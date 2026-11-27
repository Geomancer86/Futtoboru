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
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

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
    com.rndmodgames.futtoboru.menu.MainMenuManager menuManager;
    
    public CompetitionsScreenTable(Game parent) {
    
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    public void setMenuManager(com.rndmodgames.futtoboru.menu.MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    //
    public void updateDynamicComponents() {

        //
        this.clear();
        
        // Debug logging
        Log.info(Log.UI_COMPETITIONS, "CompetitionsScreenTable.updateDynamicComponents() called");
        
        // Title
        this.row().colspan(3);
        VisLabel titleLabel = new VisLabel("COMPETITIONS");
        titleLabel.setFontScale(UiTheme.fontScale("heading"));
        this.add(titleLabel).pad(UiTheme.space("sm")).row();
        
        this.addSeparator().colspan(3).pad(UiTheme.space("xs")).row();
        
        // Check if mainAuthority exists
        if (currentGame == null) {
            this.row().colspan(3);
            this.add(new VisLabel("ERROR: SaveGame is null")).pad(UiTheme.space("sm")).row();
            return;
        }
        
        if (currentGame.getMainAuthority() == null) {
            this.row().colspan(3);
            this.add(new VisLabel("No Football Association found")).pad(UiTheme.space("sm")).row();
            return;
        }
        
        // Get leagues
        List<League> leagues = currentGame.getMainAuthority().getLeagues();
        
        if (leagues == null || leagues.isEmpty()) {
            this.row().colspan(3);
            this.add(new VisLabel("No leagues found. League creation script may not have executed yet.")).pad(UiTheme.space("sm")).row();
            this.row().colspan(3);
            this.add(new VisLabel("Check console logs for league creation messages.")).pad(UiTheme.space("xs")).row();
            return;
        }
        
        Log.info(Log.UI_COMPETITIONS, "Found " + leagues.size() + " leagues to display");
        
        // --- LEAGUES SECTION ---
        this.row().colspan(3);
        VisLabel leaguesTitle = new VisLabel("LEAGUES");
        leaguesTitle.setColor(UiTheme.color("text.heading"));
        this.add(leaguesTitle).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs")).row();
        
        // Header row
        this.row().pad(UiTheme.space("xs"));
        this.add(new VisLabel("League Name")).width(UiTheme.size("col.xwide"));
        this.add(new VisLabel("Clubs")).width(UiTheme.size("col.number"));
        this.add(new VisLabel("Country")).width(UiTheme.size("col.label"));
        this.row();
        this.addSeparator().colspan(3).pad(UiTheme.space("xxs")).row();
        
        // Display each league
        for (League league : leagues) {
            if (league == null) {
                continue;
            }
            
            // League name (clickable)
            VisTextButton leagueButton = new VisTextButton(league.getName() != null ? league.getName() : "Unnamed League");
            final League leagueForClick = league; // Final reference for inner class
            leagueButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    // Navigate to league detail screen
                    if (menuManager != null) {
                        menuManager.setSelectedLeague(leagueForClick);
                        menuManager.setActiveMainScreen(com.rndmodgames.futtoboru.menu.MainMenuManager.LEAGUE_DETAIL_SCREEN);
                    }
                }
            });
            
            this.row().pad(UiTheme.space("xxs"));
            this.add(leagueButton).width(UiTheme.size("col.xwide"));
            
            // Number of clubs
            int clubCount = (league.getLeagueClubs() != null) ? league.getLeagueClubs().size() : 0;
            this.add(new VisLabel(String.valueOf(clubCount))).width(UiTheme.size("col.number"));
            
            // Country
            String countryName = (league.getCountry() != null && league.getCountry().getCommonName() != null) 
                ? league.getCountry().getCommonName() 
                : "Unknown";
            this.add(new VisLabel(countryName)).width(UiTheme.size("col.label"));
        }
        
        // --- CUPS SECTION ---
        List<com.rndmodgames.futtoboru.data.Competition> cups = currentGame.getAllCups();
        if (cups != null && !cups.isEmpty()) {
            this.row().colspan(3);
            VisLabel cupsTitle = new VisLabel("CUPS");
            cupsTitle.setColor(UiTheme.color("text.heading"));
            this.add(cupsTitle).padTop(UiTheme.space("lg")).padBottom(UiTheme.space("xs")).row();
            
            // Header row
            this.row().pad(UiTheme.space("xs"));
            this.add(new VisLabel("Cup Name")).width(UiTheme.size("col.xwide"));
            this.add(new VisLabel("Teams")).width(UiTheme.size("col.number"));
            this.add(new VisLabel("Country")).width(UiTheme.size("col.label"));
            this.row();
            this.addSeparator().colspan(3).pad(UiTheme.space("xxs")).row();
            
            for (com.rndmodgames.futtoboru.data.Competition cup : cups) {
                if (cup == null) continue;
                
                VisTextButton cupButton = new VisTextButton(cup.getName() != null ? cup.getName() : "Unnamed Cup");
                final com.rndmodgames.futtoboru.data.Competition cupForClick = cup;
                cupButton.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return true;
                    }
                    
                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        if (menuManager != null) {
                            menuManager.setSelectedCup(cupForClick);
                            menuManager.setActiveMainScreen(com.rndmodgames.futtoboru.menu.MainMenuManager.CUP_DETAIL_SCREEN);
                        }
                    }
                });
                
                this.row().pad(UiTheme.space("xxs"));
                this.add(cupButton).width(UiTheme.size("col.xwide"));
                
                // Get participant count from latest edition
                int participantCount = 0;
                if (cup.getEditions() != null && !cup.getEditions().isEmpty()) {
                    com.rndmodgames.futtoboru.data.CompetitionEdition latestEdition = cup.getEditions().get(cup.getEditions().size() - 1);
                    if (latestEdition.getParticipantClubs() != null) {
                        participantCount = latestEdition.getParticipantClubs();
                    }
                }
                this.add(new VisLabel(String.valueOf(participantCount))).width(UiTheme.size("col.number"));
                
                // Country (assume first club's country or default)
                this.add(new VisLabel("England")).width(UiTheme.size("col.label"));
            }
        }
        
        // Add spacing at bottom
        this.row();
        this.add().height(UiTheme.space("lg")).colspan(3).row();
        
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