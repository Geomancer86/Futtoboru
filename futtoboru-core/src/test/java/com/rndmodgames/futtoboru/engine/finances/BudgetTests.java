package com.rndmodgames.futtoboru.engine.finances;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.finances.Budget;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * G-12 on the real 1888-89 game: a club's season budget, actual against budget week by week, an overdraft with
 * interest, the inbox warnings at each debt level (the player's club only), the committee-concern hook, the same
 * rules for AI clubs (D31) and a save that keeps it all.
 */
@Tag("slow")
class BudgetTests extends BaseTest {

    private static final LocalDateTime WEEK1 = LocalDateTime.of(1888, 9, 8, 4, 0);

    @AfterAll
    static void restore() {
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    private static ClubExpenses week(String wages, String ground, String other) {
        ClubExpenses e = new ClubExpenses();
        e.setPlayerWages(new BigDecimal(wages));
        e.setStadiumRent(new BigDecimal(ground));
        e.setOtherExpenses(new BigDecimal(other));
        e.calculateTotal();
        return e;
    }

    private static Club aiClub(SaveGame game) {
        Map<Club, Boolean> seen = new IdentityHashMap<>();
        for (Club c : game.getAllClubs()) {
            if (c != null && c != game.getCurrentClub() && seen.put(c, Boolean.TRUE) == null) {
                return c;
            }
        }
        throw new AssertionError("no AI club");
    }

    private static List<Message> finance(SaveGame game) {
        List<Message> out = new ArrayList<>();
        for (Message m : game.getAllMessages()) {
            if (m.getMessageType() != null && m.getMessageType().startsWith("FINANCE_")) {
                out.add(m);
            }
        }
        return out;
    }

    /** One weekly count the way the engine does it */
    private static void count(BudgetManager budgets, Club club, ClubExpenses e, LocalDateTime now) {
        budgets.beforeBooking(club, e, now);
        club.setClubBalance(club.getClubBalance().subtract(e.getTotalExpenses()));
        club.setSeasonExpenditure(club.getSeasonExpenditure().add(e.getTotalExpenses()));
        budgets.afterBooking(club, e, now);
    }

    @Test
    void theFirstWeeklyCountSetsTheBudgetFromTheEngagementsAndTheWeeksLeft() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        Club club = game.getCurrentClub();
        BudgetManager budgets = application.getGameEngine().getBudgetManager();
        club.setClubBalance(new BigDecimal("500.00"));

        count(budgets, club, week("20.00", "2.00", "1.00"), WEEK1);

        Budget b = club.getBudget();
        assertNotNull(b);
        assertEquals(Integer.valueOf(1888), b.getSeason());
        // 8 September 1888 to 30 April 1889: 34 weeks, all playing weeks
        assertEquals(34, b.getWeeksPlanned());
        assertEquals(34, b.getPlayingWeeksPlanned());
        assertEquals(0, new BigDecimal("68.00").compareTo(b.getExpectedGround()));
        assertEquals(0, new BigDecimal("34.00").compareTo(b.getExpectedOther()));
        assertEquals(0, com.rndmodgames.futtoboru.engine.contracts.ContractTerms.wageBill(club, false).multiply(BigDecimal.valueOf(34))
                .compareTo(b.getExpectedWages()));
        assertEquals(0, b.expectedSpend().compareTo(b.getExpectedGates()), "a first budget aims to break even");
        assertEquals(1, b.getWeeksDone());
        assertEquals(0, new BigDecimal("20.00").compareTo(b.getActualWages()));
        assertEquals(0, new BigDecimal("2.00").compareTo(b.getActualGround()));
        assertEquals(0, new BigDecimal("1.00").compareTo(b.getActualOther()));
        assertEquals(0, b.spendToDate().compareTo(
                b.expectedSpend().divide(BigDecimal.valueOf(34), 2, java.math.RoundingMode.HALF_UP)));
        assertEquals(0, BudgetManager.debtLevel(club));
        assertTrue(finance(game).isEmpty());
    }

    @Test
    void anOverdraftPaysInterestAndTheInboxWarnsAtEachLevelOnce() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        Club club = game.getCurrentClub();
        BudgetManager budgets = application.getGameEngine().getBudgetManager();
        List<String> changes = new ArrayList<>();
        budgets.addListener((c, from, to) -> changes.add(c.getId() + ":" + from + ">" + to));
        club.setClubBalance(new BigDecimal("10.00"));

        count(budgets, club, week("5.00", "1.00", "1.00"), WEEK1);
        BigDecimal limit = club.getBudget().getFacilityLimit();
        assertTrue(limit.signum() > 0);

        // into the red: level 1, one letter
        club.setClubBalance(new BigDecimal("-20.00"));
        ClubExpenses w2 = week("5.00", "1.00", "1.00");
        count(budgets, club, w2, WEEK1.plusWeeks(1));
        // interest: 5% a year on the 20.00 the week opened with, a week's worth
        assertEquals(0, new BigDecimal("0.02").compareTo(w2.getInterestExpenses()));
        assertEquals(0, new BigDecimal("7.02").compareTo(w2.getTotalExpenses()));
        assertEquals(0, new BigDecimal("0.02").compareTo(club.getBudget().getActualInterest()));
        assertEquals(1, BudgetManager.debtLevel(club));
        assertEquals(1, finance(game).size());
        assertEquals("FINANCE_DEBT_1", finance(game).get(0).getMessageType());
        assertFalse(club.getBudget().isCommitteeConcern());

        // still level 1 next week: no second letter
        count(budgets, club, week("5.00", "1.00", "1.00"), WEEK1.plusWeeks(2));
        assertEquals(1, finance(game).size());

        // past half the facility
        club.setClubBalance(limit.multiply(new BigDecimal("0.6")).negate());
        count(budgets, club, week("1.00", "0.00", "0.00"), WEEK1.plusWeeks(3));
        assertEquals(2, BudgetManager.debtLevel(club));
        assertEquals("FINANCE_DEBT_2", finance(game).get(finance(game).size() - 1).getMessageType());

        // past the facility: the patrons lend, the committee is concerned
        club.setClubBalance(limit.multiply(new BigDecimal("1.5")).negate());
        count(budgets, club, week("1.00", "0.00", "0.00"), WEEK1.plusWeeks(4));
        assertEquals(3, BudgetManager.debtLevel(club));
        assertTrue(club.getBudget().isCommitteeConcern());
        assertEquals(0, limit.compareTo(BudgetManager.bankDebt(club)));
        assertTrue(BudgetManager.patronDebt(club).signum() > 0);
        assertEquals("FINANCE_DEBT_3", finance(game).get(finance(game).size() - 1).getMessageType());
        assertEquals(3, finance(game).size());
        assertTrue(changes.contains(club.getId() + ":2>3"), changes.toString());

        // back in credit: out of debt, concern gone, a later overdraft warns again
        club.setClubBalance(new BigDecimal("50.00"));
        count(budgets, club, week("1.00", "0.00", "0.00"), WEEK1.plusWeeks(5));
        assertEquals(0, BudgetManager.debtLevel(club));
        assertFalse(club.getBudget().isCommitteeConcern());
        assertEquals(0, club.getBudget().getWarnedLevel());
        club.setClubBalance(new BigDecimal("-1.00"));
        count(budgets, club, week("1.00", "0.00", "0.00"), WEEK1.plusWeeks(6));
        assertEquals(4, finance(game).size());
    }

    @Test
    void anAiClubRunsTheSameRulesWithoutLetters() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        Club ai = aiClub(game);
        BudgetManager budgets = application.getGameEngine().getBudgetManager();
        ai.setClubBalance(new BigDecimal("-5.00"));
        ClubExpenses e = week("5.00", "1.00", "1.00");
        count(budgets, ai, e, WEEK1);
        assertNotNull(ai.getBudget());
        assertTrue(BudgetManager.debtLevel(ai) >= 1);
        assertTrue(finance(game).isEmpty(), "the inbox is the player's");
    }

    @Test
    void aNewSeasonGetsAFreshBudgetFromLastSeasonsGates() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        Club club = game.getCurrentClub();
        BudgetManager budgets = application.getGameEngine().getBudgetManager();
        club.setClubBalance(new BigDecimal("100.00"));
        count(budgets, club, week("5.00", "1.00", "1.00"), WEEK1);
        club.setSeasonIncome(new BigDecimal("250.00"));
        count(budgets, club, week("5.00", "1.00", "1.00"), WEEK1.plusWeeks(1));
        assertEquals(0, new BigDecimal("250.00").compareTo(club.getBudget().getActualGates()));
        club.getBudget().setWeeksDone(30);

        // the 1889 close season: summer weeks count, gates are expected as last season's
        club.setSeasonIncome(BigDecimal.ZERO);
        count(budgets, club, week("5.00", "1.00", "1.00"), LocalDateTime.of(1889, 5, 6, 4, 0));
        Budget b = club.getBudget();
        assertEquals(Integer.valueOf(1889), b.getSeason());
        assertEquals(0, new BigDecimal("250.00").compareTo(b.getExpectedGates()));
        assertEquals(52, b.getWeeksPlanned());
        assertEquals(35, b.getPlayingWeeksPlanned());
        assertEquals(1, b.getWeeksDone());
        assertEquals(0, b.getPlayingWeeksDone());
    }

    @Test
    void theBudgetSurvivesASave() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        Club club = game.getCurrentClub();
        BudgetManager budgets = application.getGameEngine().getBudgetManager();
        club.setClubBalance(new BigDecimal("10.00"));
        count(budgets, club, week("5.00", "1.00", "1.00"), WEEK1);
        club.setClubBalance(club.getBudget().getFacilityLimit().multiply(new BigDecimal("2")).negate());
        count(budgets, club, week("5.00", "1.00", "1.00"), WEEK1.plusWeeks(1));
        assertTrue(club.getBudget().isCommitteeConcern());

        com.rndmodgames.futtoboru.system.generators.LanguageGenerator.fillMissing(game.getAllPersons(),
                DatabaseLoader.getInstance().getLanguageData(), DatabaseLoader.getInstance().getGameData(), DatabaseLoader.getRandom());
        String before = com.rndmodgames.futtoboru.system.save.GameFingerprint.of(game).toString();
        String text = SaveCodec.encode(game, "G-12", LocalDateTime.of(2000, 1, 1, 0, 0));
        LoadResult loaded = SaveCodec.decode(text);
        assertTrue(loaded.isLoaded(), loaded.toString());
        assertEquals(new ArrayList<String>(), loaded.getProblems());
        assertEquals(before, com.rndmodgames.futtoboru.system.save.GameFingerprint.of(loaded.getGame()).toString());
        assertEquals(text, SaveCodec.encode(loaded.getGame(), "G-12", LocalDateTime.of(2000, 1, 1, 0, 0)));
        Budget b = loaded.getGame().getCurrentClub().getBudget();
        assertNotNull(b);
        assertEquals(Integer.valueOf(1888), b.getSeason());
        assertTrue(b.isCommitteeConcern());
        assertEquals(3, b.getWarnedLevel());
        assertEquals(club.getBudget().getFacilityLimit(), b.getFacilityLimit());
        assertEquals(club.getBudget().getActualInterest(), b.getActualInterest());
        NewGameFixture.bind(application, game);
    }

    @Test
    void aMonthOfPlayGivesEveryClubABudget() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        NewGameFixture.play(application, game, 20, true);
        int checked = 0;
        Map<Club, Boolean> seen = new IdentityHashMap<>();
        for (Club c : game.getAllClubs()) {
            if (c == null || seen.put(c, Boolean.TRUE) != null || c.getPlayers().isEmpty()) {
                continue;
            }
            assertNotNull(c.getBudget(), c.getName());
            assertTrue(c.getBudget().getSeason() >= 1887, c.getName());
            assertTrue(c.getBudget().getWeeksDone() >= 1, c.getName());
            assertTrue(c.getBudget().getExpectedWages().signum() > 0, c.getName());
            checked++;
        }
        assertTrue(checked >= 12, "checked " + checked);
    }
}
