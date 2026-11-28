package com.rndmodgames.futtoboru.tables.widgets;

import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.futtoboru.game.Futtoboru;
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
    VisLabel dateValueLabel = new VisLabel();
    VisLabel timeValueLabel = new VisLabel();
    
    /**
     * TODO: date formats should be set on user settings and persisted
     */
    DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.NUMERIC); // U-12: the game language
    DateTimeFormatter timeFormatter = GameFormat.formatter(GameFormat.CLOCK);
    
    //
    public CurrentDateAndTimeWidget(Game game) {
        
        // layout
        super(false);
        
        //
        this.saveGame = ((Futtoboru)(game)).getCurrentGame();
                
        /**
         * TODO: format should come from Settings page
         */
        updateDynamicComponents();
        
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