package com.rndmodgames.futtoboru.tables.authority;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
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
 *  F�d�ration internationale de football association [FIFA] https://en.wikipedia.org/wiki/FIFA
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
    
    //
    private static final String IFAB_WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/International_Football_Association_Board";
    
    // Dynamic Components
    VisTable teamsListTable = new VisTable(true);
    
    public AuthorityScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.game = parent;
        
        /**
         * International Football Association Board [IFAB]
         * 
         *  - this will be always the same so not a lot of need to take it out to a file
         */
        VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("name"));
        VisLabel nameValueLabel = new VisLabel("International Football Association Board");
        
        VisLabel formationLabel = new VisLabel(LanguageModLoader.getValue("formation"));
        VisLabel formationValueLabel = new VisLabel("2 June 1886");
        
        VisLabel sourceLabel = new VisLabel(LanguageModLoader.getValue("source"));
        LinkLabel sourceLinkLabel = new LinkLabel("IFAB (Wikipedia)", IFAB_WIKIPEDIA_URL);
        
        // Name
        this.add(nameLabel);
        this.add(nameValueLabel);
        this.row();
        
        // Formation
        this.add(formationLabel);
        this.add(formationValueLabel);
        this.row();
        
        // Source
        this.add(sourceLabel);
        this.add(sourceLinkLabel);
        
        /**
         * Leagues
         * 
         * NOTE: Leagues won't appear until the first league is founded, need scripts!
         */
        VisLabel leaguesLabel = new VisLabel(LanguageModLoader.getValue("leagues"));
        
        this.row();
        this.addSeparator().colspan(2);
        this.add(leaguesLabel).colspan(2);
        
        /**
         * Teams
         * 
         * TODO: WIP
         * 
         *  - Existing Teams Widget
         */
        VisLabel teamsLabel = new VisLabel(LanguageModLoader.getValue("teams"));
        
        // update active teams list table
        listActiveTeams();
        
        this.row();
        this.addSeparator().colspan(2);
        this.add(teamsLabel).colspan(2);
        
        this.row();
        this.add(teamsListTable).colspan(2);
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