package com.rndmodgames.futtoboru.tables.widgets;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Current Date And Time Widget v1
 * 
 *  - Current Date Label
 *  - Current Time Label
 *  
 *  - TODO: needs to be localized
 * 
 * @author Geomancer86
 */
public class CurrentDateAndTimeWidget extends VisTable {

    // keep track
    SaveGame saveGame;
    
    // Dynamic Components
    VisLabel dateValueLabel = null;
    VisLabel timeValueLabel = null;
    
    /**
     * TODO: date formats should be set on user settings and persisted
     */
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);
    
    //
    public CurrentDateAndTimeWidget(Game game) {
        
        //
        this.saveGame = ((Futtoboru)(game)).getCurrentGame();
        
        /**
         * TODO: format should come from Settings page
         */
        dateValueLabel = new VisLabel("01 Jun 1888");
        timeValueLabel = new VisLabel("FRI 08:00");
        
        // Date Value Label
        this.add(dateValueLabel);
        this.row();
        
        // Time Value Label
        this.add(timeValueLabel);
    }
    
    //
    public void updateDynamicComponents() {
        
        //
        if (saveGame.getGameDate() != null) {
            
            // DatabaseLoader.formatter.format(availableSeasonsSelectBox.getSelected().getStartDate())
            dateValueLabel.setText(dateFormatter.format(saveGame.getGameDate()));
            timeValueLabel.setText(timeFormatter.format(saveGame.getGameDate()));
        }
    }
}