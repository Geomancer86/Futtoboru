package com.rndmodgames.futtoboru.engine.finances;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractTerms;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.finances.Budget;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-12: the season budget and the debt of every club (D31: the player's club and the AI's run on the same rules).
 *
 * <p>Each weekly count ({@code FuttoboruGameEngine}) calls {@link #beforeBooking} (plans the season's budget the first
 * time, charges interest on an overdraft) and, once the week's expenses are taken from the balance,
 * {@link #afterBooking} (tallies actual against budget, works out the debt level, warns the player's inbox).
 *
 * <p><b>Design estimates, none of these figures is sourced.</b> What is historical: 1888-89 clubs lived on gate money
 * and subscriptions, owed their wages and ground rent to guarantors and directors, and an overdraft with the local
 * bank, secured by a committee member's guarantee, was the usual way to carry a bad month; there was no insolvency
 * game over in this model.
 * <ul>
 *   <li>Expected spend: the engagements' wage bill week by week (playing weeks at the weekly wage and expenses,
 *       close-season weeks at the summer pay), plus the ground (rent and upkeep) and the other running costs at what
 *       that first week cost, for every week left of the season.</li>
 *   <li>Expected gates: what the club took in last season; in a club's first season the committee budgets to break
 *       even (gates = expected spend); the same when last season was a stub of under 26 weeks.</li>
 *   <li>Overdraft: any negative balance. Interest {@link #INTEREST_RATE} a year (bank rate of 1888 ran 2-5%;
 *       overdrafts cost a point or so over it), charged weekly on the balance the week opened with.</li>
 *   <li>Facility: the bank lets the club overdraw {@link #FACILITY_SHARE} of its expected annual spend on a
 *       guarantor's name; above that its patrons lend (same interest) and the committee takes alarm.</li>
 *   <li>Levels: 1 in overdraft, 2 at half the facility, 3 over the facility (committee concern, hook for G-05).</li>
 *   <li>Budget warnings: spend 10% over the budget to date after 8 weeks; gates under 75% of the budget to date
 *       after 20 playing weeks.</li>
 * </ul>
 */
public class BudgetManager {

    /** Design estimate: yearly interest on an overdraft */
    public static final BigDecimal INTEREST_RATE = new BigDecimal("0.05");
    /** Design estimate: share of the expected annual spend the bank lets a club overdraw */
    public static final BigDecimal FACILITY_SHARE = new BigDecimal("0.25");
    public static final int MIN_WEEKS_FOR_LAST_GATES = 26;
    public static final int WARN_SPEND_AFTER_WEEKS = 8;
    public static final BigDecimal WARN_SPEND_OVER = new BigDecimal("1.10");
    public static final int WARN_GATES_AFTER_WEEKS = 20;
    public static final BigDecimal WARN_GATES_UNDER = new BigDecimal("0.75");

    private static final String[] DEBT_TITLE = { null, "finances_msg_debt1_title", "finances_msg_debt2_title",
            "finances_msg_debt3_title" };
    private static final String[] DEBT_BODY = { null, "finances_msg_debt1_body", "finances_msg_debt2_body",
            "finances_msg_debt3_body" };

    public static final String MESSAGE_DEBT = "FINANCE_DEBT_";
    public static final String MESSAGE_SPEND = "FINANCE_BUDGET_SPEND";
    public static final String MESSAGE_GATES = "FINANCE_BUDGET_GATES";

    /** A club's debt level changed (G-05 listens for level 3: the committee's concern) */
    public interface DebtListener {
        void onDebtLevel(Club club, int from, int to);
    }

    private final Supplier<SaveGame> game;
    private final MessageManager messages;
    private final List<DebtListener> listeners = new ArrayList<>();

    /** @param messages the inbox for the player's club's warnings (null: no letters) */
    public BudgetManager(Supplier<SaveGame> game, MessageManager messages) {
        this.game = game;
        this.messages = messages;
    }

    public void addListener(DebtListener listener) {
        listeners.add(listener);
    }

    // -------------------------------------------------------------------------------------------- the week

    /** Before the week's expenses are taken: plan a new season's budget, charge interest on an overdraft */
    public void beforeBooking(Club club, ClubExpenses week, LocalDateTime now) {
        if (club == null || week == null || now == null) {
            return;
        }
        int season = ContractCalendar.seasonOf(now.toLocalDate());
        Budget budget = club.getBudget();
        if (budget == null || budget.getSeason() == null || budget.getSeason() != season) {
            budget = plan(club, week, now.toLocalDate(), season, budget);
            club.setBudget(budget);
        }
        BigDecimal balance = club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
        if (balance.signum() < 0) {
            BigDecimal interest = balance.negate().multiply(INTEREST_RATE)
                    .divide(BigDecimal.valueOf(52), 2, RoundingMode.HALF_UP);
            week.setInterestExpenses(interest);
            week.calculateTotal();
        }
    }

    /** After the week's expenses are taken: tally, level, warnings */
    public void afterBooking(Club club, ClubExpenses week, LocalDateTime now) {
        Budget budget = club != null ? club.getBudget() : null;
        if (budget == null || week == null || now == null) {
            return;
        }
        LocalDate day = now.toLocalDate();
        budget.setWeeksDone(budget.getWeeksDone() + 1);
        if (!ContractCalendar.isCloseSeason(day)) {
            budget.setPlayingWeeksDone(budget.getPlayingWeeksDone() + 1);
        }
        BigDecimal ground = week.getStadiumRent().add(week.getStadiumMaintenance()).add(week.getPitchMaintenance());
        BigDecimal interest = week.getInterestExpenses();
        budget.setActualWages(budget.getActualWages().add(week.getPlayerWages()));
        budget.setActualGround(budget.getActualGround().add(ground));
        budget.setActualInterest(budget.getActualInterest().add(interest));
        budget.setActualOther(budget.getActualOther()
                .add(week.getTotalExpenses().subtract(week.getPlayerWages()).subtract(ground).subtract(interest)));
        budget.setActualGates(club.getSeasonIncome());

        int previous = budget.getLastLevel();
        int to = debtLevel(club);
        budget.setCommitteeConcern(to >= 3);
        if (to > budget.getWarnedLevel()) {
            warnDebt(club, to, now);
            budget.setWarnedLevel(to);
        } else if (to == 0) {
            budget.setWarnedLevel(0);
        }
        if (to != previous) {
            for (DebtListener l : listeners) {
                l.onDebtLevel(club, previous, to);
            }
            Log.info(Log.ENGINE_GAME, "BudgetManager", club.getName() + " debt level " + previous + " -> " + to
                    + " (balance " + Money.format(club.getClubBalance()) + ")");
        }
        budget.setLastLevel(to);
        warnBudget(club, budget, now);
    }

    // ------------------------------------------------------------------------------------------- planning

    private Budget plan(Club club, ClubExpenses week, LocalDate from, int season, Budget last) {
        Budget b = new Budget();
        b.setSeason(season);
        int weeks = 0;
        int playing = 0;
        BigDecimal wages = BigDecimal.ZERO;
        BigDecimal season35 = ContractTerms.wageBill(club, false);
        BigDecimal summer = ContractTerms.wageBill(club, true);
        LocalDate end = ContractCalendar.seasonEnd(season).toLocalDate();
        for (LocalDate d = from; !d.isAfter(end); d = d.plusWeeks(1)) {
            weeks++;
            if (ContractCalendar.isCloseSeason(d)) {
                wages = wages.add(summer);
            } else {
                playing++;
                wages = wages.add(season35);
            }
        }
        BigDecimal groundWeek = week.getStadiumRent().add(week.getStadiumMaintenance()).add(week.getPitchMaintenance());
        BigDecimal otherWeek = week.getTotalExpenses().subtract(week.getPlayerWages()).subtract(groundWeek)
                .subtract(week.getInterestExpenses());
        b.setWeeksPlanned(weeks);
        b.setPlayingWeeksPlanned(playing);
        b.setExpectedWages(wages);
        b.setExpectedGround(groundWeek.multiply(BigDecimal.valueOf(weeks)));
        b.setExpectedOther(otherWeek.multiply(BigDecimal.valueOf(weeks)));
        BigDecimal spend = b.expectedSpend();
        // last season's gates count when a good part of it was played (a game starting in April has a 4-week stub)
        BigDecimal lastGates = last != null && last.getWeeksDone() >= MIN_WEEKS_FOR_LAST_GATES ? last.getActualGates()
                : BigDecimal.ZERO;
        b.setExpectedGates(lastGates != null && lastGates.signum() > 0 ? lastGates : spend);
        // the facility is a share of a year's spending; a part-year budget (the game's first) scales to 52 weeks
        BigDecimal yearSpend = weeks > 0 ? spend.multiply(BigDecimal.valueOf(52))
                .divide(BigDecimal.valueOf(weeks), 2, RoundingMode.HALF_UP) : spend;
        b.setFacilityLimit(yearSpend.multiply(FACILITY_SHARE).setScale(2, RoundingMode.HALF_UP));
        if (last != null) {
            b.setWarnedLevel(last.getWarnedLevel());
            b.setCommitteeConcern(last.isCommitteeConcern());
            b.setLastLevel(last.getLastLevel());
        }
        return b;
    }

    // ----------------------------------------------------------------------------------------------- debt

    /** What the club owes: its overdraft (a negative balance), else nothing */
    public static BigDecimal debt(Club club) {
        BigDecimal balance = club != null ? club.getClubBalance() : null;
        return balance != null && balance.signum() < 0 ? balance.negate() : BigDecimal.ZERO;
    }

    /** The part of the debt the bank carries (up to the facility) */
    public static BigDecimal bankDebt(Club club) {
        Budget b = club != null ? club.getBudget() : null;
        BigDecimal debt = debt(club);
        return b == null ? debt : debt.min(b.getFacilityLimit());
    }

    /** The part of the debt above the facility: the patrons' loans */
    public static BigDecimal patronDebt(Club club) {
        return debt(club).subtract(bankDebt(club));
    }

    /** 0 no debt, 1 in overdraft, 2 at half the facility, 3 over the facility */
    public static int debtLevel(Club club) {
        BigDecimal debt = debt(club);
        if (debt.signum() <= 0) {
            return 0;
        }
        Budget b = club.getBudget();
        BigDecimal limit = b != null ? b.getFacilityLimit() : BigDecimal.ZERO;
        if (limit.signum() <= 0) {
            return 1;
        }
        if (debt.compareTo(limit) > 0) {
            return 3;
        }
        return debt.compareTo(limit.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)) >= 0 ? 2 : 1;
    }

    // ------------------------------------------------------------------------------------------- warnings

    private boolean mine(Club club) {
        SaveGame g = game != null ? game.get() : null;
        return g != null && g.getCurrentClub() != null && g.getCurrentClub().getId() != null
                && g.getCurrentClub().getId().equals(club.getId());
    }

    private void warnDebt(Club club, int level, LocalDateTime now) {
        if (messages == null || !mine(club)) {
            return;
        }
        Budget b = club.getBudget();
        Object[] args = { MessageManager.formatDay(now.getDayOfMonth(), now.getMonthValue(), now.getYear()),
                Money.format(debt(club)), Money.format(b.getFacilityLimit()) };
        send(MESSAGE_DEBT + level, level >= 3 ? MessagePriority.URGENT : MessagePriority.NORMAL,
                LanguageModLoader.format(DEBT_TITLE[level], args), LanguageModLoader.format(DEBT_BODY[level], args), now);
    }

    private void warnBudget(Club club, Budget b, LocalDateTime now) {
        if (messages == null || !mine(club)) {
            return;
        }
        String season = ContractCalendar.seasonName(b.getSeason());
        if (!b.isSpendWarned() && b.getWeeksDone() >= WARN_SPEND_AFTER_WEEKS && b.spendToDate().signum() > 0
                && b.actualSpend().compareTo(b.spendToDate().multiply(WARN_SPEND_OVER)) > 0) {
            b.setSpendWarned(true);
            Object[] args = { season, Money.format(b.actualSpend()), Money.format(b.spendToDate()) };
            send(MESSAGE_SPEND, MessagePriority.NORMAL, LanguageModLoader.format("finances_msg_spend_title", args),
                    LanguageModLoader.format("finances_msg_spend_body", args), now);
        }
        if (!b.isGatesWarned() && b.getPlayingWeeksDone() >= WARN_GATES_AFTER_WEEKS && b.gatesToDate().signum() > 0
                && b.getActualGates().compareTo(b.gatesToDate().multiply(WARN_GATES_UNDER)) < 0) {
            b.setGatesWarned(true);
            Object[] args = { season, Money.format(b.getActualGates()), Money.format(b.gatesToDate()) };
            send(MESSAGE_GATES, MessagePriority.NORMAL, LanguageModLoader.format("finances_msg_gates_title", args),
                    LanguageModLoader.format("finances_msg_gates_body", args), now);
        }
    }

    private void send(String type, MessagePriority priority, String title, String body, LocalDateTime now) {
        Message m = new Message();
        m.setCategory(MessageCategory.SYSTEM);
        m.setMessageType(type);
        m.setPriority(priority);
        m.setTitle(title);
        m.setPlainTextMessage(body);
        m.setMessageTime(now);
        m.setActionScreen(GameScreenId.FINANCES);
        m.setIsRead(false);
        m.setIsDeleted(false);
        m.setIsMandatory(false);
        messages.deliverMessage(m);
    }
}
