package com.rndmodgames.futtoboru.tables.authority;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Authority Screen Table v1
 * 
 * TODO / WIP:
 *  
 *  - Data structure
 *  
 *  ----------------------------------
 *  FIFA: Sport Top Governing Body
 *  IFAB: Rules of the Game Management
 *  
 *  
 *  ----------------------------------
 *  Fédération internationale de football association [FIFA] https://en.wikipedia.org/wiki/FIFA
 *  
 *  - Foundation: 21 May 1904
 *  - HQ       : Zurich, Switzerland
 *  
 *  ----------------------------------
 *  
 *  International Football Association Board [IFAB] https://en.wikipedia.org/wiki/International_Football_Association_Board
 *  
 *  - Formation: 2 June 1886
 *  - Purpose  : Manage the [Laws of the Game]
 *  - HQ       : Zurich, Switzerland
 *  - Members  : FIFA,
 *               The Football Association [ENGLAND]
 *               Scotish Football Association
 *               Football Association of Wales
 *               Irish Football Association
 * 
 * @author Geomancer86
 */
public class AuthorityScreenTable extends VisTable {

    //
    Game game;
    SaveGame currentGame;
    
    //
    private static final String IFAB_WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/International_Football_Association_Board";
    
    // Dynamic Components
    VisTable leaguesListTable = new VisTable(true);
    VisTable teamsListTable = new VisTable(true);
    
    public AuthorityScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        /**
         * Main Authority
         */
        VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("name"));
        VisLabel nameValueLabel = new VisLabel(currentGame.getMainAuthority().getName());
        
        // Name
        this.add(nameLabel);
        this.add(nameValueLabel);
        this.row();
        
        /**
         * Authority Leagues
         */
        VisLabel leaguesLabel = new VisLabel(LanguageModLoader.getValue("leagues"));

        // Leagues
        this.row();
        this.addSeparator().colspan(2);
        this.add(leaguesLabel).colspan(2);
        this.row();
        this.add(leaguesListTable).colspan(2);
        
        VisLabel teamsLabel = new VisLabel(LanguageModLoader.getValue("teams"));

        this.row();
        this.addSeparator().colspan(2);
        this.add(teamsLabel).colspan(2);

        this.row();
        this.add(teamsListTable).colspan(2);
        
        //
        updateDynamicComponents();
    }
    
    public void updateDynamicComponents() {
        
        //
        listActiveLeagues();
        listActiveTeams();
    }
    
    /**
     * TODO WIP:
     * 
     * 
     */
    public void listActiveLeagues() {
        
        Authority main = currentGame.getMainAuthority();
        
        leaguesListTable.clear();
        
        leaguesListTable.row();
        
        // No existing Leagues
        if (main.getLeagues().isEmpty()) {
            
            leaguesListTable.add(new VisLabel("No Active Leagues"));
            
        } else {
            
            // Add Leagues Component
            leaguesListTable.add(new VisLabel(LanguageModLoader.getValue("top_level_league")));
            leaguesListTable.add(new VisLabel(main.getLeagues().get(0).getName()));
        }
    }
    
    /**
     * TODO: we need to set the Current Season depending on Player Selection on New Game
     *          and the current game DATE TIME
     */
    public void listActiveTeams() {
        
        // TODO: unhardcode active season
        Season activeSeason = DatabaseLoader.getInstance().getSeasons().get(0);
        
        System.out.println("LISTING SEASON " + activeSeason.getClubs().size() + " ACTIVE TEAMS");
        
        //
        teamsListTable.clear();
        
        for (Club club : activeSeason.getClubs()) {
            
            teamsListTable.add(new VisLabel(club.getFullName()));
            teamsListTable.add(new VisLabel(club.getYear().toString()));
            teamsListTable.add(new LinkLabel("(Wikipedia)", club.getUrlSource()));
            teamsListTable.row();
        }
    }
}