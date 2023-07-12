package com.rndmodgames.futtoboru.tables.squad;

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
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
    
    // Dynamic Components
    VisLabel clubNameValueLabel = new VisLabel("");
    VisLabel playersCountValueLabel = new VisLabel("");
    VisLabel averagePlayerAgeValueLabel = new VisLabel("");
    
    //
    PlayersListTable playersList;
    
    //
    DecimalFormat averageYearsFormat = new DecimalFormat("#.0"); 
    
    //
    public SquadScreenTable(Game parent) {
        
        // default vis spacing
        super (true);
        
        //
        this.game = ((Futtoboru)parent);
        this.currentGame = game.getCurrentGame();
        
        // 
        playersList = new PlayersListTable(game);
        
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
        VisLabel averageAgeLabel = new VisLabel(LanguageModLoader.getValue("average_player_age"));
        
        // Name
        this.add(nameLabel);
        this.add(clubNameValueLabel);
        this.row();
        
        // Players at Club Count
        this.add(playersAtClubLabel);
        this.add(playersCountValueLabel);
        this.row();
        
        // Average Players Age
        this.add(averageAgeLabel);
        this.add(averagePlayerAgeValueLabel);
        this.row();
        
        // Players List Component
        this.addSeparator().colspan(2);
        this.add(playersList).colspan(2);
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
            playersCountValueLabel.setText(LanguageModLoader.getValue("no_players_at_club"));
            
        } else {
            
            // Count of Players & Player List Component
            playersCountValueLabel.setText(displayedClub.getPlayers().size() + " " + LanguageModLoader.getValue("players_at_club"));
            
            /**
             * Average Player Age
             * 
             * TODO: this calculation might be used on many places, move elsewhere
             * 
             * TODO: count MONTHS for decimal years and a better year average
             */
            long accumulatedAge = 0;
            
            for (Player player : displayedClub.getPlayers()) {
                
                long yearsOld = ChronoUnit.YEARS.between(player.getPerson().getBirthDate(), game.getCurrentGame().getGameDate());
                
                // add up players age
                accumulatedAge += yearsOld;
            }
            
            double averagePlayerAge = (double) accumulatedAge / (double) displayedClub.getPlayers().size();
            
            //
            averagePlayerAgeValueLabel.setText(averageYearsFormat.format(averagePlayerAge) + " years old");
        }
        
        // Update Players List Component
        playersList.setCurrentPlayers(displayedClub.getPlayers());
        
        //
        playersList.updateDynamicComponents();
    }

    public Club getCurrentClub() {
        return displayedClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.displayedClub = currentClub;
    }
}