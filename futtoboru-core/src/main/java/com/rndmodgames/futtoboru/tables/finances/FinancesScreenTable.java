package com.rndmodgames.futtoboru.tables.finances;

import java.text.DecimalFormat;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Finances Screen Table v1
 * 
 * Sources:
 * 
 *      - https://1.bp.blogspot.com/-3nyvCcmf3E4/WorvJ19hyEI/AAAAAAAAZmE/670HWamx3EQ1DOT1F999nHh7nIhqO0Y-gCLcBGAs/s1600/FM18-Money-Cheat-4.gif
 * 
 * @author Geomancer86
 */
public class FinancesScreenTable extends VisTable {

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    // TODO: international formats in Settings Menu saved on User Properties File
    DecimalFormat df = new DecimalFormat("#,###.00");
    
    public FinancesScreenTable(Game parent) {
        
        // automatic vis spacing
        super(true);
        
        //
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
    }
    
    /**
     * TODO: skin doesn't support pound symbol
     */
    public void updateDynamicComponents() {
        
        //
        this.clear();

        // Overall Balance
        this.row();
        this.add(LanguageModLoader.getValue("overall_balance"));
        this.add("$" + df.format(game.getCurrentGame().getCurrentClub().getClubBalance()));
        
        /**
         * Income (This Season)
         * 
         * TODO: 
         *  - sell tickets for scheduled matches
         *  - 
         */
        
        // Expenditure (This Season)
        
        
        /**
         * - Club Balance
         * 
         * - PROFIT/LOSS (This Month)
         * - INCOME (This Month)
         * - EXPENSES (This Month)
         * 
         * - This Season / Last Season
         * 
         * - BREAKDOWN:
         *  - gate receipts / ticket sales
         *  - corporate facilities income
         *  - ticket season holders
         *  - match day income
         *  - players sold
         *  
         *  - salary
         *  - bonuses
         *  - tax
         *  - stadium costs
         *  - facilities cost
         *  - match day expenses
         *  
         *  SUMMARY / INCOME / EXPENDITURE / WAGES / DEBT AND LOANS / SPONSORS AND OTHER / PROJECTION
         *  
         */
    }
}