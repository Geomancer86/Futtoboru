package com.rndmodgames.futtoboru.tables.finances;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
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
        
        Club club = game.getCurrentGame().getCurrentClub();
        if (club == null) {
            this.add(new VisLabel("No club selected")).pad(20).row();
            return;
        }

        // ========================================
        // SUMMARY SECTION
        // ========================================
        this.row();
        VisLabel summaryLabel = new VisLabel("FINANCIAL SUMMARY");
        summaryLabel.setFontScale(1.2f);
        this.add(summaryLabel).colspan(2).pad(10).row();
        this.addSeparator().colspan(2).pad(5).row();
        
        // Overall Balance
        this.row();
        this.add(new VisLabel("Overall Balance:"));
        this.add(new VisLabel("$" + df.format(club.getClubBalance()))).left().row();
        
        // Profit/Loss (This Month)
        BigDecimal monthProfitLoss = club.getMonthIncome().subtract(club.getMonthExpenditure());
        this.row().padTop(5);
        this.add(new VisLabel("Profit/Loss (This Month):"));
        VisLabel profitLossLabel = new VisLabel("$" + df.format(monthProfitLoss));
        if (monthProfitLoss.compareTo(BigDecimal.ZERO) >= 0) {
            profitLossLabel.setColor(0.2f, 1.0f, 0.2f, 1.0f); // Green for profit
        } else {
            profitLossLabel.setColor(1.0f, 0.2f, 0.2f, 1.0f); // Red for loss
        }
        this.add(profitLossLabel).left().row();
        
        // Profit/Loss (This Season)
        BigDecimal seasonProfitLoss = club.getSeasonIncome().subtract(club.getSeasonExpenditure());
        this.row().padTop(5);
        this.add(new VisLabel("Profit/Loss (This Season):"));
        VisLabel seasonProfitLossLabel = new VisLabel("$" + df.format(seasonProfitLoss));
        if (seasonProfitLoss.compareTo(BigDecimal.ZERO) >= 0) {
            seasonProfitLossLabel.setColor(0.2f, 1.0f, 0.2f, 1.0f); // Green for profit
        } else {
            seasonProfitLossLabel.setColor(1.0f, 0.2f, 0.2f, 1.0f); // Red for loss
        }
        this.add(seasonProfitLossLabel).left().row();
        
        this.add().height(20).colspan(2).row();
        
        // ========================================
        // INCOME SECTION
        // ========================================
        this.row();
        VisLabel incomeLabel = new VisLabel("INCOME (This Season)");
        incomeLabel.setFontScale(1.1f);
        this.add(incomeLabel).colspan(2).pad(10).row();
        this.addSeparator().colspan(2).pad(5).row();
        
        // Match Day Income
        BigDecimal matchDayIncome = calculateMatchDayIncome(club);
        this.row();
        this.add(new VisLabel("Match Day Income:"));
        this.add(new VisLabel("$" + df.format(matchDayIncome))).left().row();
        
        // Breakdown by match type
        BigDecimal leagueIncome = calculateIncomeByType(club, Match.LEAGUE_MATCH);
        BigDecimal cupIncome = calculateIncomeByType(club, Match.CUP_MATCH);
        BigDecimal friendlyIncome = calculateIncomeByType(club, Match.FRIENDLY_MATCH);
        
        this.row().padTop(2);
        this.add(new VisLabel("  - League Matches:"));
        this.add(new VisLabel("$" + df.format(leagueIncome))).left().row();
        
        this.row().padTop(2);
        this.add(new VisLabel("  - Cup Matches:"));
        this.add(new VisLabel("$" + df.format(cupIncome))).left().row();
        
        this.row().padTop(2);
        this.add(new VisLabel("  - Friendly Matches:"));
        this.add(new VisLabel("$" + df.format(friendlyIncome))).left().row();
        
        // Total Income
        BigDecimal totalIncome = club.getSeasonIncome();
        this.addSeparator().colspan(2).pad(2).row();
        this.row();
        this.add(new VisLabel("Total Income:"));
        this.add(new VisLabel("$" + df.format(totalIncome))).left().row();
        
        this.add().height(20).colspan(2).row();
        
        // ========================================
        // EXPENDITURE SECTION
        // ========================================
        this.row();
        VisLabel expenditureLabel = new VisLabel("EXPENDITURE (This Season)");
        expenditureLabel.setFontScale(1.1f);
        this.add(expenditureLabel).colspan(2).pad(10).row();
        this.addSeparator().colspan(2).pad(5).row();
        
        // Total Expenditure
        BigDecimal totalExpenditure = club.getSeasonExpenditure();
        this.row();
        this.add(new VisLabel("Total Expenditure:"));
        this.add(new VisLabel("$" + df.format(totalExpenditure))).left().row();
        
        // TODO: Add breakdown when expenditure tracking is implemented
        // - Player Wages
        // - Staff Wages
        // - Facility Costs
        // - Other Expenditure
    }
    
    /**
     * Calculate total match day income for current season
     */
    private BigDecimal calculateMatchDayIncome(Club club) {
        BigDecimal total = BigDecimal.ZERO;
        List<MatchIncome> matchIncomes = club.getMatchIncomes();
        
        if (matchIncomes == null) {
            return total;
        }
        
        LocalDateTime seasonStart = getCurrentSeasonStart();
        
        for (MatchIncome income : matchIncomes) {
            if (income.getMatchDate() != null && 
                !income.getMatchDate().isBefore(seasonStart)) {
                total = total.add(income.getTicketRevenue());
            }
        }
        
        return total;
    }
    
    /**
     * Calculate income by match type for current season
     */
    private BigDecimal calculateIncomeByType(Club club, Integer matchType) {
        BigDecimal total = BigDecimal.ZERO;
        List<MatchIncome> matchIncomes = club.getMatchIncomes();
        
        if (matchIncomes == null) {
            return total;
        }
        
        LocalDateTime seasonStart = getCurrentSeasonStart();
        
        for (MatchIncome income : matchIncomes) {
            if (income.getMatchType() != null && 
                income.getMatchType().equals(matchType) &&
                income.getMatchDate() != null && 
                !income.getMatchDate().isBefore(seasonStart)) {
                total = total.add(income.getTicketRevenue());
            }
        }
        
        return total;
    }
    
    /**
     * Get current season start date
     * TODO: Get from actual season/competition data
     */
    private LocalDateTime getCurrentSeasonStart() {
        // For now, use game start date or current date minus 9 months
        LocalDateTime gameStart = currentGame.getGameStartDate();
        if (gameStart != null) {
            return gameStart;
        }
        // Default: 9 months ago (typical season length)
        return LocalDateTime.now().minusMonths(9);
    }
}