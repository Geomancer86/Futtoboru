package com.rndmodgames.futtoboru.tables.squad;

import java.util.List;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Player;
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
        
        // iterate players and fill the list,
        for (Player player : currentPlayers) {
            
            this.row();
            this.add(player.getPerson().getName() + " " + player.getPerson().getLastname());
        }
    }
    
    public List<Player> getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(List<Player> currentPlayers) {
        this.currentPlayers = currentPlayers;
    }
}