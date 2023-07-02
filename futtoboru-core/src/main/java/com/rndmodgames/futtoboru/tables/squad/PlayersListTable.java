package com.rndmodgames.futtoboru.tables.squad;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Players List Table v1
 * 
 * SOURCE:
 * https://3.bp.blogspot.com/-ADphk9k3mNs/VmiRVkpfgDI/AAAAAAAAA1g/lK3Ij3MtTmQ8D8Nkl3BrjRowVTbAGvFTgCPcB/s1600/Squad_.png
 * 
 * @author Geomancer86
 */
public class PlayersListTable extends VisTable {

    private List<Player> currentPlayers;
    
    // Dynamic Components
    VisTable mainTable;
    
    //
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    
    public PlayersListTable() {
        
        // default table spacing
        super(true);
        
        // 
        
        //
        updateDynamicComponents();
    }

    /**
     * 
     */
    public void updateDynamicComponents() {
        
        // clear
        this.clear();
        
        // No Players
        if (currentPlayers == null
                || currentPlayers.isEmpty()) {
            
            this.row();
            this.add(new VisLabel(LanguageModLoader.getValue("no_registered_players_at_club")));
            return;
        }
        
        /**
         * Player List Table Header:
         * 
         *  - Column Titles                 [WIP]
         *  - Sort by Clicking on Title     [TBD]
         *  - Filters                       [TBD]
         *  - Add and Remove Columns        [TBD] // customization by player saved per screen TBD
         */
        
        // iterate players and fill the list,
        for (Player player : currentPlayers) {
            
            this.row();
            
            // Age / Birthdate
            this.add(dateFormatter.format(player.getPerson().getBirthDate()));
            
            // Full Name
            this.add(player.getPerson().getName() + " " + player.getPerson().getLastname());
            
            
            // DatabaseLoader.formatter.format(startingSeason.getStartDate())
        }
    }
    
    public List<Player> getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(List<Player> currentPlayers) {
        this.currentPlayers = currentPlayers;
    }
}