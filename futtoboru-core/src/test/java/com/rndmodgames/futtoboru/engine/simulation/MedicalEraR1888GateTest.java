package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.rndmodgames.futtoboru.system.loaders.MedicalEraFeatureGate;
import com.rndmodgames.futtoboru.system.loaders.MedicalEraRegistry;

@Tag("r1888")
class MedicalEraR1888GateTest {

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
    void me1DisablesInsuranceLilleshallSurgeon() {
        assertFalse(MedicalEraFeatureGate.canInsure(today));
        assertFalse(MedicalEraFeatureGate.canLilleshall(today));
        assertFalse(MedicalEraFeatureGate.canSurgeonAbroad(today));
        assertTrue(MedicalEraFeatureGate.canRehabilitate(today));
    }

    @Test
    void insureLilleshallSurgeonReturnNotAvailableInEra() {
        club.setClubBalance(new BigDecimal("5000"));
        PlayerCondition condition = PlayerConditionService.conditionOf(player);
        condition.setInjuryTypeId("serious");
        condition.setInjurySeverityEnum(InjurySeverity.SERIOUS);
        condition.setInjuryReturnDate(today.plusWeeks(8));

        assertEquals(TreatmentResult.NOT_AVAILABLE_IN_ERA,
                InjuryTreatmentService.insure(club, player, today));
        assertEquals(TreatmentResult.NOT_AVAILABLE_IN_ERA,
                InjuryTreatmentService.sendToLilleshall(club, player, today));
        assertEquals(TreatmentResult.NOT_AVAILABLE_IN_ERA,
                InjuryTreatmentService.surgeonAbroad(club, player, today, new java.util.Random(1)));
    }
}
