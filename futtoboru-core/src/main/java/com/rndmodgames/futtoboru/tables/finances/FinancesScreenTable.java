package com.rndmodgames.futtoboru.tables.finances;

import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;

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
            this.add(new VisLabel("No club selected")).pad(UiTheme.space("lg")).row();
            return;
        }

        // ========================================
        // SUMMARY SECTION
        // ========================================
        this.row();
        VisLabel summaryLabel = new VisLabel("FINANCIAL SUMMARY");
        summaryLabel.setFontScale(UiTheme.fontScale("heading"));
        this.add(summaryLabel).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        
        // Overall Balance
        this.row();
        this.add(new VisLabel("Overall Balance:"));
        this.add(new VisLabel(Money.format(club.getClubBalance()))).left().row();
        
        // Profit/Loss (This Month)
        BigDecimal monthProfitLoss = club.getMonthIncome().subtract(club.getMonthExpenditure());
        this.row().padTop(UiTheme.space("xs"));
        this.add(new VisLabel("Profit/Loss (This Month):"));
        VisLabel profitLossLabel = new VisLabel(Money.format(monthProfitLoss));
        if (monthProfitLoss.compareTo(BigDecimal.ZERO) >= 0) {
            profitLossLabel.setColor(UiTheme.color("state.positive")); // Green for profit
        } else {
            profitLossLabel.setColor(UiTheme.color("state.negative")); // Red for loss
        }
        this.add(profitLossLabel).left().row();
        
        // Profit/Loss (This Season)
        BigDecimal seasonProfitLoss = club.getSeasonIncome().subtract(club.getSeasonExpenditure());
        this.row().padTop(UiTheme.space("xs"));
        this.add(new VisLabel("Profit/Loss (This Season):"));
        VisLabel seasonProfitLossLabel = new VisLabel(Money.format(seasonProfitLoss));
        if (seasonProfitLoss.compareTo(BigDecimal.ZERO) >= 0) {
            seasonProfitLossLabel.setColor(UiTheme.color("state.positive")); // Green for profit
        } else {
            seasonProfitLossLabel.setColor(UiTheme.color("state.negative")); // Red for loss
        }
        this.add(seasonProfitLossLabel).left().row();
        
        this.add().height(UiTheme.space("lg")).colspan(2).row();
        
        // ========================================
        // INCOME SECTION
        // ========================================
        this.row();
        VisLabel incomeLabel = new VisLabel("INCOME (This Season)");
        incomeLabel.setFontScale(UiTheme.fontScale("subheading"));
        this.add(incomeLabel).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        
        // Match Day Income Table
        this.row();
        VisLabel matchIncomeLabel = new VisLabel("Match Day Income Breakdown");
        matchIncomeLabel.setFontScale(UiTheme.fontScale("subheading.minor"));
        this.add(matchIncomeLabel).colspan(4).pad(UiTheme.space("xs")).row();
        
        // Table Header
        VisTable headerTable = new VisTable(true);
        headerTable.add(new VisLabel("Match Type")).width(UiTheme.size("col.label"));
        headerTable.add(new VisLabel("Revenue")).width(UiTheme.size("col.long"));
        headerTable.add(new VisLabel("Avg Attendance")).width(UiTheme.size("col.long"));
        headerTable.add(new VisLabel("Matches")).width(UiTheme.size("col.value"));
        this.add(headerTable).colspan(4).pad(UiTheme.space("xxs")).row();
        this.addSeparator().colspan(4).pad(UiTheme.space("xxs")).row();
        
        // Calculate and display League Matches
        MatchIncomeStats leagueStats = calculateMatchIncomeStats(club, Match.LEAGUE_MATCH);
        displayMatchIncomeRow("League Matches", leagueStats);
        
        // Calculate and display Cup Matches
        MatchIncomeStats cupStats = calculateMatchIncomeStats(club, Match.CUP_MATCH);
        displayMatchIncomeRow("Cup Matches", cupStats);
        
        // Calculate and display Friendly Matches
        MatchIncomeStats friendlyStats = calculateMatchIncomeStats(club, Match.FRIENDLY_MATCH);
        displayMatchIncomeRow("Friendly Matches", friendlyStats);
        
        // Total Match Day Income
        BigDecimal matchDayIncome = calculateMatchDayIncome(club);
        this.addSeparator().colspan(4).pad(UiTheme.space("xxs")).row();
        VisTable matchIncomeTotalTable = new VisTable(true);
        matchIncomeTotalTable.add(new VisLabel("Total Match Day Income:")).width(UiTheme.size("col.label"));
        matchIncomeTotalTable.add(new VisLabel(Money.format(matchDayIncome))).width(UiTheme.size("col.long"));
        matchIncomeTotalTable.add(new VisLabel("")).width(UiTheme.size("col.long")); // Empty for avg attendance
        matchIncomeTotalTable.add(new VisLabel("")).width(UiTheme.size("col.value")); // Empty for match count
        this.add(matchIncomeTotalTable).colspan(4).pad(UiTheme.space("xxs")).row();
        
        // Total Income
        BigDecimal totalIncome = club.getSeasonIncome();
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        this.row();
        this.add(new VisLabel("Total Income:"));
        this.add(new VisLabel(Money.format(totalIncome))).left().row();
        
        this.add().height(UiTheme.space("lg")).colspan(2).row();
        
        // ========================================
        // EXPENDITURE SECTION
        // ========================================
        this.row();
        VisLabel expenditureLabel = new VisLabel("EXPENDITURE (This Season)");
        expenditureLabel.setFontScale(UiTheme.fontScale("subheading"));
        this.add(expenditureLabel).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        
        // Calculate expense breakdown for current season
        ExpenseBreakdown breakdown = calculateExpenseBreakdown(club);
        
        // Expense Breakdown Table
        this.row();
        VisLabel breakdownLabel = new VisLabel("Expense Breakdown");
        breakdownLabel.setFontScale(UiTheme.fontScale("subheading.minor"));
        this.add(breakdownLabel).colspan(2).pad(UiTheme.space("xs")).row();
        
        // Table Header
        VisTable expenseHeaderTable = new VisTable(true);
        expenseHeaderTable.add(new VisLabel("Category")).width(UiTheme.size("col.wide"));
        expenseHeaderTable.add(new VisLabel("Amount")).width(UiTheme.size("col.long"));
        this.add(expenseHeaderTable).colspan(2).pad(UiTheme.space("xxs")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        
        // Display each expense category
        displayExpenseRow("Stadium Maintenance", breakdown.stadiumMaintenance);
        displayExpenseRow("Pitch Maintenance", breakdown.pitchMaintenance);
        displayExpenseRow("Materials & Equipment", breakdown.materialsEquipment);
        displayExpenseRow("Staff Wages", breakdown.staffWages);
        displayExpenseRow("Administrative", breakdown.administrativeExpenses);
        displayExpenseRow("Stadium Rent", breakdown.stadiumRent);
        displayExpenseRow("Other Expenses", breakdown.otherExpenses);
        displayExpenseRow("Player Wages", breakdown.playerWages, true); // Italic if zero (not implemented yet)
        
        // Total Expenditure
        BigDecimal totalExpenditure = breakdown.getTotal();
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        VisTable expenseTotalTable = new VisTable(true);
        expenseTotalTable.add(new VisLabel("Total Expenditure:")).width(UiTheme.size("col.wide"));
        VisLabel totalLabel = new VisLabel(Money.format(totalExpenditure));
        totalLabel.setColor(UiTheme.color("state.negative")); // Red for expenses
        expenseTotalTable.add(totalLabel).width(UiTheme.size("col.long"));
        this.add(expenseTotalTable).colspan(2).pad(UiTheme.space("xxs")).row();
        
        // Verify total matches season expenditure
        BigDecimal seasonExpenditure = club.getSeasonExpenditure();
        if (totalExpenditure.compareTo(seasonExpenditure) != 0) {
            // Show both for debugging
            this.row();
            this.add(new VisLabel("(Season Total: " + Money.format(seasonExpenditure) + ")")).colspan(2).left().padTop(UiTheme.space("xxs"));
        }
    }
    
    /**
     * Expense Breakdown helper class
     */
    private static class ExpenseBreakdown {
        BigDecimal playerWages = BigDecimal.ZERO;
        BigDecimal staffWages = BigDecimal.ZERO;
        BigDecimal stadiumMaintenance = BigDecimal.ZERO;
        BigDecimal pitchMaintenance = BigDecimal.ZERO;
        BigDecimal materialsEquipment = BigDecimal.ZERO;
        BigDecimal administrativeExpenses = BigDecimal.ZERO;
        BigDecimal stadiumRent = BigDecimal.ZERO;
        BigDecimal otherExpenses = BigDecimal.ZERO;
        
        BigDecimal getTotal() {
            return playerWages.add(staffWages)
                             .add(stadiumMaintenance)
                             .add(pitchMaintenance)
                             .add(materialsEquipment)
                             .add(administrativeExpenses)
                             .add(stadiumRent)
                             .add(otherExpenses);
        }
    }
    
    /**
     * Calculate expense breakdown for current season
     */
    private ExpenseBreakdown calculateExpenseBreakdown(Club club) {
        ExpenseBreakdown breakdown = new ExpenseBreakdown();
        List<ClubExpenses> expensesHistory = club.getExpensesHistory();
        
        if (expensesHistory == null) {
            return breakdown;
        }
        
        LocalDateTime seasonStart = getCurrentSeasonStart();
        
        for (ClubExpenses expense : expensesHistory) {
            if (expense != null && 
                expense.getPeriodStart() != null && 
                !expense.getPeriodStart().isBefore(seasonStart)) {
                
                breakdown.playerWages = breakdown.playerWages.add(expense.getPlayerWages());
                breakdown.staffWages = breakdown.staffWages.add(expense.getStaffWages());
                breakdown.stadiumMaintenance = breakdown.stadiumMaintenance.add(expense.getStadiumMaintenance());
                breakdown.pitchMaintenance = breakdown.pitchMaintenance.add(expense.getPitchMaintenance());
                breakdown.materialsEquipment = breakdown.materialsEquipment.add(expense.getMaterialsEquipment());
                breakdown.administrativeExpenses = breakdown.administrativeExpenses.add(expense.getAdministrativeExpenses());
                breakdown.stadiumRent = breakdown.stadiumRent.add(expense.getStadiumRent());
                breakdown.otherExpenses = breakdown.otherExpenses.add(expense.getOtherExpenses());
            }
        }
        
        return breakdown;
    }
    
    /**
     * Display an expense row in the breakdown table
     */
    private void displayExpenseRow(String category, BigDecimal amount) {
        displayExpenseRow(category, amount, false);
    }
    
    /**
     * Display an expense row in the breakdown table
     */
    private void displayExpenseRow(String category, BigDecimal amount, boolean italicIfZero) {
        VisTable rowTable = new VisTable(true);
        VisLabel categoryLabel = new VisLabel(category);
        if (italicIfZero && amount.compareTo(BigDecimal.ZERO) == 0) {
            categoryLabel.setColor(UiTheme.color("text.secondary")); // Gray if not implemented
        }
        rowTable.add(categoryLabel).width(UiTheme.size("col.wide"));
        rowTable.add(new VisLabel(Money.format(amount))).width(UiTheme.size("col.long"));
        this.add(rowTable).colspan(2).pad(UiTheme.space("xxs")).row();
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
        MatchIncomeStats stats = calculateMatchIncomeStats(club, matchType);
        return stats.totalRevenue;
    }
    
    /**
     * Match Income Statistics helper class
     */
    private static class MatchIncomeStats {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalAttendance = 0;
        int matchCount = 0;
        
        double getAverageAttendance() {
            return matchCount > 0 ? (double)totalAttendance / matchCount : 0.0;
        }
    }
    
    /**
     * Calculate match income statistics by match type for current season
     * 
     * Counts all MatchIncome records for the specified match type in the current season.
     * If MatchIncome exists, it means tickets were sold, so we count it.
     */
    private MatchIncomeStats calculateMatchIncomeStats(Club club, Integer matchType) {
        MatchIncomeStats stats = new MatchIncomeStats();
        List<MatchIncome> matchIncomes = club.getMatchIncomes();
        
        if (matchIncomes == null) {
            System.out.println("FinancesScreenTable: No match incomes found for club");
            return stats;
        }
        
        LocalDateTime seasonStart = getCurrentSeasonStart();
        LocalDateTime currentDate = currentGame.getGameDate();
        
        // Track which matches we've already counted (by match ID) to avoid duplicates
        java.util.Set<Long> countedMatchIds = new java.util.HashSet<Long>();
        
        for (MatchIncome income : matchIncomes) {
            // Check match type match (use == for Integer comparison to handle null)
            boolean typeMatches = false;
            if (income.getMatchType() != null && matchType != null) {
                typeMatches = income.getMatchType().equals(matchType);
            } else if (income.getMatchType() == null && matchType == null) {
                typeMatches = true;
            }
            
            if (typeMatches &&
                income.getMatchDate() != null && 
                !income.getMatchDate().isBefore(seasonStart)) {
                
                // Only count each match once (by match ID)
                if (income.getMatchId() != null && !countedMatchIds.contains(income.getMatchId())) {
                    // If match date is in the past, assume it was played (tickets were sold)
                    // If match date is in the future, it's still selling tickets, so we can count it too
                    // (income represents tickets sold so far)
                    boolean shouldCount = true;
                    
                    // For matches in the future, only count if match date has passed
                    // This prevents counting matches that haven't happened yet
                    if (income.getMatchDate() != null && currentDate != null) {
                        if (income.getMatchDate().isAfter(currentDate)) {
                            // Match is in the future, don't count it yet
                            shouldCount = false;
                        }
                    }
                    
                    if (shouldCount) {
                        // Include in statistics
                        stats.totalRevenue = stats.totalRevenue.add(income.getTicketRevenue());
                        if (income.getAttendance() != null) {
                            stats.totalAttendance += income.getAttendance();
                        }
                        stats.matchCount++;
                        countedMatchIds.add(income.getMatchId());
                    }
                }
            }
        }
        
        return stats;
    }
    
    /**
     * Display a row in the match income table
     */
    private void displayMatchIncomeRow(String matchTypeLabel, MatchIncomeStats stats) {
        VisTable rowTable = new VisTable(true);
        rowTable.add(new VisLabel(matchTypeLabel)).width(UiTheme.size("col.label"));
        rowTable.add(new VisLabel(Money.format(stats.totalRevenue))).width(UiTheme.size("col.long"));
        
        // Average attendance
        if (stats.matchCount > 0) {
            int avgAttendance = (int)Math.round(stats.getAverageAttendance());
            rowTable.add(new VisLabel(String.valueOf(avgAttendance))).width(UiTheme.size("col.long"));
        } else {
            rowTable.add(new VisLabel("-")).width(UiTheme.size("col.long"));
        }
        
        // Match count
        rowTable.add(new VisLabel(String.valueOf(stats.matchCount))).width(UiTheme.size("col.value"));
        
        this.add(rowTable).colspan(4).pad(UiTheme.space("xxs")).row();
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