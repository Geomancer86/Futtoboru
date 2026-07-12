package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.InjurySeverity;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.engine.simulation.InjuryTreatmentService.TreatmentResult;
import com.rndmodgames.futtoboru.harness.GameTestHarness;
import com.rndmodgames.futtoboru.harness.HeadlessGdxTestSupport;
import com.rndmodgames.futtoboru.system.loaders.MedicalEraRegistry;

@Tag("r1888")
class MedicalEraR1888CostTest {

    private GameTestHarness harness;
    private LocalDate today;
    private Club club;
    private Player player;

    @BeforeEach
    void setUp() {
        HeadlessGdxTestSupport.ensureInitialized();
        MedicalEraRegistry.reloadFromMod();
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
        today = harness.game().getGameDate().toLocalDate();
        club = harness.game().getClubById(GameTestHarness.PRESTON_NORTH_END_CLUB_ID);
        player = club.getPlayers().get(0);
    }

    @Test
    void moderateRehabCostsSeventyFivePenceAndShortensByOneWeek() {
        club.setClubBalance(new BigDecimal("10"));
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("knee");
        condition.setInjurySeverityEnum(InjurySeverity.MODERATE);
        condition.setInjuryReturnDate(today.plusWeeks(4));

        BigDecimal before = club.getClubBalance();
        assertEquals(TreatmentResult.SUCCESS, InjuryTreatmentService.rehabilitate(club, player, today));
        assertEquals(new BigDecimal("0.75"), before.subtract(club.getClubBalance()));
        assertEquals(new BigDecimal("9.25"), club.getClubBalance());
        assertTrue(condition.getInjuryReturnDate().isBefore(today.plusWeeks(4)));
        assertEquals(new BigDecimal("0.75"),
                MedicalEraRegistry.resolve(today).getRehabCost(InjurySeverity.MODERATE));
    }

    @Test
    void minorRehabCostsTwentyFivePence() {
        club.setClubBalance(new BigDecimal("5"));
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("strain");
        condition.setInjurySeverityEnum(InjurySeverity.MINOR);
        condition.setInjuryReturnDate(today.plusWeeks(2));

        BigDecimal before = club.getClubBalance();
        assertEquals(TreatmentResult.SUCCESS, InjuryTreatmentService.rehabilitate(club, player, today));
        assertEquals(new BigDecimal("0.25"), before.subtract(club.getClubBalance()));
        assertEquals(new BigDecimal("4.75"), club.getClubBalance());
    }
}
