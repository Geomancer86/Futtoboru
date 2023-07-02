package com.rndmodgames.futtoboru.tables.squad;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Main Squad Screen Table v1
 * 
 *  - This screen will be used for both the Player accessing it's own team (manager, owner, etc)
 *  - This screen will be used to see the Squad of other teams without access, but we are supposed to know the players from the public information
 *  
 *  - Design for Fog of War on Attributes should be done ASAP
 * 
 * ---
 * Sources:
 * 
 *  - https://3.bp.blogspot.com/-ADphk9k3mNs/VmiRVkpfgDI/AAAAAAAAA1g/lK3Ij3MtTmQ8D8Nkl3BrjRowVTbAGvFTgCPcB/s1600/Squad_.png
 * 
 * @author Geomancer86
 */
public class SquadScreenTable extends VisTable{

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    // Dynamic Club
    private Club displayedClub = null;
    
    VisLabel clubNameValueLabel = new VisLabel("");
    VisLabel playersCountLabel = new VisLabel("");
    
    //
    public SquadScreenTable(Game parent) {
        
        // default vis spacing
        super (true);
        
        //
        this.game = ((Futtoboru)parent);
        this.currentGame = game.getCurrentGame();
        
        /**
         * SQUAD SCREEN:
         * 
         *  TODO WIP:
         *  
         *      - No Players Label
         *      - List of Players Component
         *      
         *      - Players Design
         *      - Random Players Generator
         *      - Players Script Loader (for existing players/database)
         *      
         *          - Name
         *          - Nationality
         *          - Birthdate/Age
         *          - Attributes
         *          - Contracts/Salaries
         *          - History
         *          - Fitness
         *          - Training
         *          - Injuries
         *          
         * ------
         * Screen Headers:
         *  - 
         * 
         * Screen Content:
         * 
         */
        VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("main_squad"));
        VisLabel playersAtClubLabel = new VisLabel(LanguageModLoader.getValue("players_at_club"));
        
        // Name
        this.add(nameLabel);
        this.add(clubNameValueLabel);
        this.row();
        
        // Players at Club Count
        this.add(playersAtClubLabel);
        this.add(playersCountLabel);
        this.row();
        
        // Players List Component
    }
    
    /**
     * 
     */
    public void updateDynamicComponents() {
        
        //
        clubNameValueLabel.setText(displayedClub.getName());
        
        // No Players at Club
        if (displayedClub.getPlayers().isEmpty()) {
            
            //
            playersCountLabel.setText(LanguageModLoader.getValue("no_players_at_club"));
            
        } else {
            
            // Count of Players & Player List Component
            playersCountLabel.setText(displayedClub.getPlayers().size() + " " + LanguageModLoader.getValue("players_at_club"));
        }
        
        // Update Players List
    }

    public Club getCurrentClub() {
        return displayedClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.displayedClub = currentClub;
    }
}