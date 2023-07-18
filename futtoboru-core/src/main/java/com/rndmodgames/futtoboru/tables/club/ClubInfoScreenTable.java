package com.rndmodgames.futtoboru.tables.club;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Club Info Screen Table v1
 * 
 * @author Geomancer86
 */
public class ClubInfoScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    //
    public ClubInfoScreenTable(Game parent) {
        
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    /**
     * 
     */
    public void updateDynamicComponents() {
        
        //
        this.clear();

        // Basic Club Info
        this.row();
        this.add(currentGame.getCurrentClub().getName()).colspan(2);
        
        // Stadium Name
        this.row();
        this.add("stadium");
        this.add(currentGame.getCurrentClub().getStadium().getName());
        
        // Stadium Capacity
        this.row();
        this.add("capacity");
        this.add(currentGame.getCurrentClub().getStadium().getCapacity() + "");
    }
}