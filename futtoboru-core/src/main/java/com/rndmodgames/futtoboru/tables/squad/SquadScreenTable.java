package com.rndmodgames.futtoboru.tables.squad;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Main Squad Screen Table v1
 * 
 * @author Geomancer86
 */
public class SquadScreenTable extends VisTable{

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    //
    public SquadScreenTable(Game parent) {
        
        // default vis spacing
        super (true);
        
        //
        this.game = ((Futtoboru)parent);
        this.currentGame = game.getCurrentGame();
        
        // MAIN SQUAD SCREEN WIP
        VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("main_squad"));
        
        // Name
        this.add(nameLabel);
//        this.add(nameValueLabel);
        this.row();
    }
}