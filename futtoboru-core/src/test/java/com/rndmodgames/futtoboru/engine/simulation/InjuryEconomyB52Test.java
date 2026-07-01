package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.InjurySeverity;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.engine.finances.ExpenseCalculator;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.simulation.InjuryTreatmentService.TreatmentResult;
import com.rndmodgames.futtoboru.harness.GameTestHarness;

class InjuryEconomyB52Test {

    private GameTestHarness harness;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
        today = harness.game().getGameDate().toLocalDate();
        resetSquadForTests(harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID));
    }

    /** DatabaseLoader clubs are shared across harness runs — reset squad state each test. */
    private void resetSquadForTests(Club club) {
        if (club == null || club.getPlayers() == null) {
            return;
        }
        for (Player player : club.getPlayers()) {
            if (player == null) {
                continue;
            }
            player.setInjuryInsured(false);
            player.setInjuryInsuranceWeekly(null);
            PlayerConditionService.conditionOf(player).clearInjury();
        }
    }

    private Player firstPlayerWithPositiveWage(Club club) {
        for (Player player : club.getPlayers()) {
            PlayerContract contract = findContract(club, player);
            if (contract != null && contract.getWeeklyWage() != null
                    && contract.getWeeklyWage().compareTo(BigDecimal.ZERO) > 0) {
                return player;
            }
        }
        Player fallback = club.getPlayers().get(0);
        PlayerContract contract = findContract(club, fallback);
        if (contract == null) {
            contract = new PlayerContract(fallback.getId(), club.getId(),
                    com.rndmodgames.futtoboru.data.ContractType.PROFESSIONAL);
            contract.setIsActive(true);
            club.addPlayerContract(contract);
        }
        contract.setWeeklyWage(new BigDecimal("2.00"));
        return fallback;
    }

    private PlayerContract findContract(Club club, Player player) {
        if (player == null || club == null) {
            return null;
        }
        if (player.getId() != null) {
            PlayerContract contract = club.getContractForPlayer(player.getId());
            if (contract != null) {
                return contract;
            }
        }
        if (player.getPerson() != null && player.getPerson().getId() != null) {
            return club.getContractForPlayer(player.getPerson().getId());
        }
        return null;
    }

    @Test
    void highPronenessIncreasesInjuryChance() {
        Player low = new Player();
        low.setInjuryProneness(5);
        Player high = new Player();
        high.setInjuryProneness(18);
        Random rollMiss = new Random(0) {
            @Override
            public double nextDouble() {
                return 0.025;
            }
        };
        PlayerCondition lowCondition = PlayerCondition.createFitDefault();
        PlayerCondition highCondition = PlayerCondition.createFitDefault();
        assertFalse(InjuryGenerator.maybeInjure(low, lowCondition, null, rollMiss, today));
        assertTrue(InjuryGenerator.maybeInjure(high, highCondition, null, rollMiss, today));
    }

    @Test
    void superbPhysioShortensRecovery() {
        int baseWeeks = 6;
        int okWeeks = PhysioService.applyPhysioWeeks(baseWeeks, 3);
        int superbWeeks = PhysioService.applyPhysioWeeks(baseWeeks, 5);
        assertTrue(superbWeeks < okWeeks);
        assertEquals(6, okWeeks);
        assertEquals(5, superbWeeks);
    }

    @Test
    void rehabilitateShortensModerateInjury() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        club.setClubBalance(new BigDecimal("1000"));
        Player player = club.getPlayers().get(0);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("knee");
        condition.setInjuryDisplayName("Knee injury");
        condition.setInjurySeverityEnum(InjurySeverity.MODERATE);
        condition.setInjuryReturnDate(today.plusWeeks(4));
        condition.setInjuryEstimateWeeks(4);

        assertEquals(TreatmentResult.SUCCESS, InjuryTreatmentService.rehabilitate(club, player, today));
        assertTrue(condition.getInjuryReturnDate().isBefore(today.plusWeeks(4)));
    }

    @Test
    void insuredPlayerSkipsWagesWhileInjured() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Player player = firstPlayerWithPositiveWage(club);
        PlayerContract contract = findContract(club, player);
        BigDecimal playerWage = contract.getWeeklyWage();

        ExpenseCalculator calculator = new ExpenseCalculator();
        BigDecimal wagesWhenFit = calculator.calculateWeeklyExpenses(club, harness.game().getGameDate()).getPlayerWages();

        player.setInjuryInsured(true);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("strain");
        condition.setInjurySeverityEnum(InjurySeverity.MINOR);
        condition.setInjuryReturnDate(today.plusWeeks(1));

        BigDecimal wagesWhenInjured = calculator.calculateWeeklyExpenses(club, harness.game().getGameDate()).getPlayerWages();
        assertEquals(wagesWhenFit.subtract(playerWage).setScale(2, java.math.RoundingMode.HALF_UP), wagesWhenInjured);
    }

    @Test
    void insureSetsWeeklyPremium() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Player player = firstPlayerWithPositiveWage(club);
        assertEquals(TreatmentResult.SUCCESS, InjuryTreatmentService.insure(club, player, today));
        assertTrue(InjuryGenerator.isPlayerInsured(player));
        assertTrue(player.getInjuryInsuranceWeekly().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void lilleshallReducesSeriousInjuryTimeline() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        club.setClubBalance(new BigDecimal("2000"));
        Player player = club.getPlayers().get(0);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("serious");
        condition.setInjurySeverityEnum(InjurySeverity.SERIOUS);
        condition.setInjuryReturnDate(today.plusWeeks(10));
        condition.setInjuryEstimateWeeks(10);

        assertEquals(TreatmentResult.SUCCESS, InjuryTreatmentService.sendToLilleshall(club, player, today));
        assertTrue(condition.getInjuryReturnDate().isBefore(today.plusWeeks(10)));
        assertTrue(condition.getLilleshallEndDate() != null);
    }

    @Test
    void surgeonAbroadRequiresRichClub() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Player player = club.getPlayers().get(0);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("serious");
        condition.setInjurySeverityEnum(InjurySeverity.SERIOUS);
        condition.setInjuryReturnDate(today.plusWeeks(8));

        club.setClubBalance(new BigDecimal("100"));
        assertEquals(TreatmentResult.INSUFFICIENT_FUNDS,
                InjuryTreatmentService.surgeonAbroad(club, player, today, new Random(1)));

        club.setClubBalance(new BigDecimal("2000"));
        Random alwaysSuccess = new Random(1) {
            @Override
            public double nextDouble() {
                return 0.5;
            }
        };
        assertEquals(TreatmentResult.SUCCESS,
                InjuryTreatmentService.surgeonAbroad(club, player, today, alwaysSuccess));
        assertTrue(condition.getSurgeonAbroadUsed());
    }

    @Test
    void moderateInjuryCreatesInboxMessage() {
        Club club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        Player player = club.getPlayers().get(0);
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("knee");
        condition.setInjuryDisplayName("Knee injury");
        condition.setInjurySeverityEnum(InjurySeverity.MODERATE);
        condition.setInjuryEstimateWeeks(3);
        condition.setInjuryReturnDate(today.plusWeeks(3));

        MessageManager messageManager = harness.engine().getMessageManager();
        int before = harness.game().getAllMessages() != null ? harness.game().getAllMessages().size() : 0;
        messageManager.createInjuryNewsMessage(player, condition);
        assertEquals(before + 1, harness.game().getAllMessages().size());
        Message latest = harness.game().getAllMessages().get(harness.game().getAllMessages().size() - 1);
        assertTrue(latest.getPlainTextMessage().contains("Knee injury"));
    }
}
