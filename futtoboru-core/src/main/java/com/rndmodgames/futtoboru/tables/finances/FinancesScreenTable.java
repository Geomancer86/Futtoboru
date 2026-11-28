package com.rndmodgames.futtoboru.tables.finances;

import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.BudgetManager;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.finances.Budget;
import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.contracts.ContractTerms;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

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
            this.add(new VisLabel(LanguageModLoader.getValue("finances_no_club"))).pad(UiTheme.space("lg")).row();
            return;
        }

        // ========================================
        // SUMMARY SECTION
        // ========================================
        this.row();
        VisLabel summaryLabel = new VisLabel(LanguageModLoader.getValue("finances_summary"));
        summaryLabel.setFontScale(UiTheme.fontScale("heading"));
        this.add(summaryLabel).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        
        // Overall Balance
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("finances_balance")));
        this.add(new VisLabel(Money.format(club.getClubBalance()))).left().row();
        
        // Profit/Loss (This Month)
        BigDecimal monthProfitLoss = club.getMonthIncome().subtract(club.getMonthExpenditure());
        this.row().padTop(UiTheme.space("xs"));
        this.add(new VisLabel(LanguageModLoader.getValue("finances_profit_month")));
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
        this.add(new VisLabel(LanguageModLoader.getValue("finances_profit_season")));
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
        VisLabel incomeLabel = new VisLabel(LanguageModLoader.getValue("finances_income_season"));
        incomeLabel.setFontScale(UiTheme.fontScale("subheading"));
        this.add(incomeLabel).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        
        // Match Day Income Table
        this.row();
        VisLabel matchIncomeLabel = new VisLabel(LanguageModLoader.getValue("finances_match_income"));
        matchIncomeLabel.setFontScale(UiTheme.fontScale("subheading.minor"));
        this.add(matchIncomeLabel).colspan(4).pad(UiTheme.space("xs")).row();
        
        // Table Header
        VisTable headerTable = new VisTable(true);
        headerTable.add(new VisLabel(LanguageModLoader.getValue("finances_match_type"))).width(UiTheme.size("col.label"));
        headerTable.add(new VisLabel(LanguageModLoader.getValue("finances_revenue"))).width(UiTheme.size("col.long"));
        headerTable.add(new VisLabel(LanguageModLoader.getValue("finances_avg_attendance"))).width(UiTheme.size("col.long"));
        headerTable.add(new VisLabel(LanguageModLoader.getValue("finances_matches"))).width(UiTheme.size("col.value"));
        this.add(headerTable).colspan(4).pad(UiTheme.space("xxs")).row();
        this.addSeparator().colspan(4).pad(UiTheme.space("xxs")).row();
        
        // Calculate and display League Matches
        MatchIncomeStats leagueStats = calculateMatchIncomeStats(club, Match.LEAGUE_MATCH);
        displayMatchIncomeRow(LanguageModLoader.getValue("finances_league_matches"), leagueStats);
        
        // Calculate and display Cup Matches
        MatchIncomeStats cupStats = calculateMatchIncomeStats(club, Match.CUP_MATCH);
        displayMatchIncomeRow(LanguageModLoader.getValue("finances_cup_matches"), cupStats);
        
        // Calculate and display Friendly Matches
        MatchIncomeStats friendlyStats = calculateMatchIncomeStats(club, Match.FRIENDLY_MATCH);
        displayMatchIncomeRow(LanguageModLoader.getValue("finances_friendly_matches"), friendlyStats);
        
        // Total Match Day Income
        BigDecimal matchDayIncome = calculateMatchDayIncome(club);
        this.addSeparator().colspan(4).pad(UiTheme.space("xxs")).row();
        VisTable matchIncomeTotalTable = new VisTable(true);
        matchIncomeTotalTable.add(new VisLabel(LanguageModLoader.getValue("finances_total_match_income"))).width(UiTheme.size("col.label"));
        matchIncomeTotalTable.add(new VisLabel(Money.format(matchDayIncome))).width(UiTheme.size("col.long"));
        matchIncomeTotalTable.add(new VisLabel("")).width(UiTheme.size("col.long")); // Empty for avg attendance
        matchIncomeTotalTable.add(new VisLabel("")).width(UiTheme.size("col.value")); // Empty for match count
        this.add(matchIncomeTotalTable).colspan(4).pad(UiTheme.space("xxs")).row();
        
        // Total Income
        BigDecimal totalIncome = club.getSeasonIncome();
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("finances_total_income")));
        this.add(new VisLabel(Money.format(totalIncome))).left().row();
        
        this.add().height(UiTheme.space("lg")).colspan(2).row();
        
        // ========================================
        // EXPENDITURE SECTION
        // ========================================
        this.row();
        VisLabel expenditureLabel = new VisLabel(LanguageModLoader.getValue("finances_expenditure_season"));
        expenditureLabel.setFontScale(UiTheme.fontScale("subheading"));
        this.add(expenditureLabel).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        
        // Calculate expense breakdown for current season
        ExpenseBreakdown breakdown = calculateExpenseBreakdown(club);
        
        // Expense Breakdown Table
        this.row();
        VisLabel breakdownLabel = new VisLabel(LanguageModLoader.getValue("finances_expense_breakdown"));
        breakdownLabel.setFontScale(UiTheme.fontScale("subheading.minor"));
        this.add(breakdownLabel).colspan(2).pad(UiTheme.space("xs")).row();
        
        // Table Header
        VisTable expenseHeaderTable = new VisTable(true);
        expenseHeaderTable.add(new VisLabel(LanguageModLoader.getValue("finances_category"))).width(UiTheme.size("col.wide"));
        expenseHeaderTable.add(new VisLabel(LanguageModLoader.getValue("finances_amount"))).width(UiTheme.size("col.long"));
        this.add(expenseHeaderTable).colspan(2).pad(UiTheme.space("xxs")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        
        // Display each expense category
        displayExpenseRow(LanguageModLoader.getValue("finances_stadium_maintenance"), breakdown.stadiumMaintenance);
        displayExpenseRow(LanguageModLoader.getValue("finances_pitch_maintenance"), breakdown.pitchMaintenance);
        displayExpenseRow(LanguageModLoader.getValue("finances_materials"), breakdown.materialsEquipment);
        displayExpenseRow(LanguageModLoader.getValue("finances_staff_wages"), breakdown.staffWages);
        displayExpenseRow(LanguageModLoader.getValue("finances_administrative"), breakdown.administrativeExpenses);
        displayExpenseRow(LanguageModLoader.getValue("finances_stadium_rent"), breakdown.stadiumRent);
        displayExpenseRow(LanguageModLoader.getValue("finances_other"), breakdown.otherExpenses);
        displayExpenseRow(LanguageModLoader.getValue("finances_interest"), breakdown.interest);
        displayExpenseRow(LanguageModLoader.getValue("finances_player_wages"), breakdown.playerWages, true); // Italic if zero (not implemented yet)
        
        // Total Expenditure
        BigDecimal totalExpenditure = breakdown.getTotal();
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        VisTable expenseTotalTable = new VisTable(true);
        expenseTotalTable.add(new VisLabel(LanguageModLoader.getValue("finances_total_expenditure"))).width(UiTheme.size("col.wide"));
        VisLabel totalLabel = new VisLabel(Money.format(totalExpenditure));
        totalLabel.setColor(UiTheme.color("state.negative")); // Red for expenses
        expenseTotalTable.add(totalLabel).width(UiTheme.size("col.long"));
        this.add(expenseTotalTable).colspan(2).pad(UiTheme.space("xxs")).row();
        
        // Verify total matches season expenditure
        BigDecimal seasonExpenditure = club.getSeasonExpenditure();
        if (totalExpenditure.compareTo(seasonExpenditure) != 0) {
            // Show both for debugging
            this.row();
            this.add(new VisLabel(LanguageModLoader.format("finances_season_total", Money.format(seasonExpenditure)))).colspan(2).left().padTop(UiTheme.space("xxs"));
        }

        // G-04: what the engagements cost each week
        this.add().height(UiTheme.space("lg")).colspan(2).row();
        displayWageBill(club);

        // G-12: the season's budget against what happened, and the debt
        this.add().height(UiTheme.space("lg")).colspan(2).row();
        displayBudget(club);
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
        BigDecimal interest = BigDecimal.ZERO;
        
        BigDecimal getTotal() {
            return playerWages.add(staffWages)
                             .add(stadiumMaintenance)
                             .add(pitchMaintenance)
                             .add(materialsEquipment)
                             .add(administrativeExpenses)
                             .add(stadiumRent)
                             .add(otherExpenses)
                             .add(interest);
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
                breakdown.interest = breakdown.interest.add(expense.getInterestExpenses());
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
            Log.info(Log.UI_CLUB, "FinancesScreenTable: No match incomes found for club");
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
            rowTable.add(new VisLabel(GameFormat.integer(avgAttendance))).width(UiTheme.size("col.long"));
        } else {
            rowTable.add(new VisLabel("-")).width(UiTheme.size("col.long"));
        }
        
        // Match count
        rowTable.add(new VisLabel(String.valueOf(stats.matchCount))).width(UiTheme.size("col.value"));
        
        this.add(rowTable).colspan(4).pad(UiTheme.space("xxs")).row();
    }
    
    /**
     * The start of the club's financial season (G-04): the last 1 May rollover of the engagements, or the game's start
     * in its first season (the club's season totals are reset at the rollover). It used to be the game start forever,
     * with the machine's clock as a fallback.
     */
    private LocalDateTime getCurrentSeasonStart() {
        SaveGame save = game.getCurrentGame();
        LocalDateTime gameStart = save.getGameStartDate();
        ContractManager contracts = game.getGameEngine() != null ? game.getGameEngine().getContractManager() : null;
        if (contracts != null && gameStart != null) {
            int running = contracts.runningSeason(save);
            if (running > SeasonCalendar.forDate(gameStart).getStartYear()) {
                return ContractCalendar.nextSeasonStart(running - 1);
            }
        }
        return gameStart != null ? gameStart : save.getGameDate();
    }

    /** G-04: the club's weekly wage bill from its engagements (playing season, close season) and its men by type */
    private void displayWageBill(Club club) {
        this.row();
        VisLabel title = new VisLabel(LanguageModLoader.getValue("finances_wage_bill"));
        title.setFontScale(UiTheme.fontScale("subheading.minor"));
        this.add(title).colspan(2).pad(UiTheme.space("xs")).row();
        int professionals = 0;
        int amateurs = 0;
        for (PlayerContract c : club.getPlayerContracts()) {
            if (c == null || !Boolean.TRUE.equals(c.getIsActive())) {
                continue;
            }
            if (c.isAmateur()) {
                amateurs++;
            } else {
                professionals++;
            }
        }
        displayExpenseRow(LanguageModLoader.getValue("finances_wage_bill_season"), ContractTerms.wageBill(club, false));
        displayExpenseRow(LanguageModLoader.getValue("finances_wage_bill_summer"), ContractTerms.wageBill(club, true));
        this.add(new VisLabel(LanguageModLoader.format("finances_wage_bill_men", professionals, amateurs)))
                .colspan(2).left().pad(UiTheme.space("xxs")).row();
    }

    private static final String[] DEBT_LEVEL = { null, "finances_debt_level1", "finances_debt_level2",
            "finances_debt_level3" };

    /** G-12: budget vs actual (whole season, to date, so far) and the club's debt */
    private void displayBudget(Club club) {
        Budget b = club.getBudget();
        VisLabel title = new VisLabel(LanguageModLoader.getValue("finances_budget"));
        title.setFontScale(UiTheme.fontScale("subheading"));
        this.add(title).colspan(2).pad(UiTheme.space("sm")).row();
        this.addSeparator().colspan(2).pad(UiTheme.space("xs")).row();
        if (b == null || !b.isPlanned()) {
            this.add(new VisLabel(LanguageModLoader.getValue("finances_budget_none"))).colspan(2).left()
                    .pad(UiTheme.space("xxs")).row();
            return;
        }
        this.add(new VisLabel(LanguageModLoader.format("finances_budget_season",
                ContractCalendar.seasonName(b.getSeason())))).colspan(2).left().pad(UiTheme.space("xxs")).row();
        budgetRow(LanguageModLoader.getValue("finances_category"),
                LanguageModLoader.getValue("finances_budget_season_col"),
                LanguageModLoader.getValue("finances_budget_to_date"),
                LanguageModLoader.getValue("finances_budget_actual"), null);
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        budgetRow(LanguageModLoader.getValue("finances_budget_gates"), Money.format(b.getExpectedGates()),
                Money.format(b.gatesToDate()), Money.format(b.getActualGates()),
                b.getActualGates().compareTo(b.gatesToDate()) >= 0);
        budgetRow(LanguageModLoader.getValue("finances_player_wages"), Money.format(b.getExpectedWages()),
                Money.format(b.wagesToDate()), Money.format(b.getActualWages()),
                b.getActualWages().compareTo(b.wagesToDate()) <= 0);
        budgetRow(LanguageModLoader.getValue("finances_budget_ground"), Money.format(b.getExpectedGround()),
                Money.format(b.groundToDate()), Money.format(b.getActualGround()),
                b.getActualGround().compareTo(b.groundToDate()) <= 0);
        budgetRow(LanguageModLoader.getValue("finances_other"), Money.format(b.getExpectedOther()),
                Money.format(b.otherToDate()), Money.format(b.getActualOther()),
                b.getActualOther().compareTo(b.otherToDate()) <= 0);
        budgetRow(LanguageModLoader.getValue("finances_interest"), Money.format(BigDecimal.ZERO),
                Money.format(BigDecimal.ZERO), Money.format(b.getActualInterest()), b.getActualInterest().signum() == 0);
        this.addSeparator().colspan(2).pad(UiTheme.space("xxs")).row();
        budgetRow(LanguageModLoader.getValue("finances_total_expenditure"), Money.format(b.expectedSpend()),
                Money.format(b.spendToDate()), Money.format(b.actualSpend()),
                b.actualSpend().compareTo(b.spendToDate()) <= 0);

        this.add().height(UiTheme.space("lg")).colspan(2).row();
        VisLabel debtTitle = new VisLabel(LanguageModLoader.getValue("finances_debt"));
        debtTitle.setFontScale(UiTheme.fontScale("subheading.minor"));
        this.add(debtTitle).colspan(2).pad(UiTheme.space("xs")).row();
        BigDecimal debt = BudgetManager.debt(club);
        if (debt.signum() == 0) {
            VisLabel none = new VisLabel(LanguageModLoader.getValue("finances_debt_none"));
            none.setColor(UiTheme.color("state.positive"));
            this.add(none).colspan(2).left().pad(UiTheme.space("xxs")).row();
        } else {
            VisLabel owed = new VisLabel(Money.format(debt));
            owed.setColor(UiTheme.color("state.negative"));
            VisTable owedRow = new VisTable(true);
            owedRow.add(new VisLabel(LanguageModLoader.getValue("finances_debt_owed"))).width(UiTheme.size("col.wide"));
            owedRow.add(owed).width(UiTheme.size("col.long"));
            this.add(owedRow).colspan(2).pad(UiTheme.space("xxs")).row();
            displayExpenseRow(LanguageModLoader.getValue("finances_debt_bank"), BudgetManager.bankDebt(club));
            displayExpenseRow(LanguageModLoader.getValue("finances_debt_patrons"), BudgetManager.patronDebt(club));
        }
        displayExpenseRow(LanguageModLoader.getValue("finances_debt_facility"), b.getFacilityLimit());
        displayExpenseRow(LanguageModLoader.getValue("finances_debt_interest"), b.getActualInterest());
        int level = BudgetManager.debtLevel(club);
        if (level > 0) {
            VisLabel state = new VisLabel(LanguageModLoader.getValue(DEBT_LEVEL[level]));
            state.setColor(UiTheme.color(level >= 3 ? "state.negative" : "text.secondary"));
            this.add(state).colspan(2).left().pad(UiTheme.space("xxs")).row();
        }
        if (b.isCommitteeConcern()) {
            VisLabel concern = new VisLabel(LanguageModLoader.getValue("finances_debt_concern"));
            concern.setColor(UiTheme.color("state.negative"));
            this.add(concern).colspan(2).left().pad(UiTheme.space("xxs")).row();
        }
    }

    /** One line of the budget table; {@code good} colours the actual column (null: a heading line) */
    private void budgetRow(String category, String season, String toDate, String actual, Boolean good) {
        VisTable row = new VisTable(true);
        row.add(new VisLabel(category)).width(UiTheme.size("col.wide"));
        row.add(new VisLabel(season)).width(UiTheme.size("col.long"));
        row.add(new VisLabel(toDate)).width(UiTheme.size("col.long"));
        VisLabel actualLabel = new VisLabel(actual);
        if (good != null) {
            actualLabel.setColor(UiTheme.color(good ? "state.positive" : "state.negative"));
        }
        row.add(actualLabel).width(UiTheme.size("col.long"));
        this.add(row).colspan(2).pad(UiTheme.space("xxs")).row();
    }
}