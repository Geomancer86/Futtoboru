package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;

/**
 * Integration tests for mandatory draw gates blocking Continue behaviour.
 */
class MandatoryGateIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void leagueCreationEventuallySchedulesMandatoryDraw() {
        advanceToMandatoryDrawDelivered();
        assertTrue(hasMandatoryDrawMessageInInbox());
    }

    @Test
    void unreadMandatoryDrawBlocksContinueAction() {
        advanceToMandatoryDrawDelivered();
        assertEquals(FuttoboruGameEngine.DRAW_ACTION, harness.getNextGameAction());
    }

    @Test
    void continueRespectingGatesThrowsWhenDrawUnread() {
        advanceToMandatoryDrawDelivered();
        assertThrows(SimulationDeadlockException.class, harness::continueOneDayRespectingGates);
    }

    @Test
    void acknowledgingDrawClearsGateAndAllowsRespectingContinue() {
        advanceToMandatoryDrawDelivered();
        LocalDateTime blockedDate = harness.game().getGameDate();

        int acknowledged = harness.acknowledgeMandatoryDraws();
        assertTrue(acknowledged >= 1, "Expected at least one draw message acknowledged");
        assertEquals(FuttoboruGameEngine.CONTINUE_GAME_ACTION, harness.getNextGameAction());

        harness.continueOneDayRespectingGates();
        assertTrue(harness.game().getGameDate().isAfter(blockedDate));
        harness.assertNotBlocked();
    }

    @Test
    void rawContinueStillAdvancesWhileGateUnread() {
        advanceToMandatoryDrawDelivered();
        assertEquals(FuttoboruGameEngine.DRAW_ACTION, harness.getNextGameAction());

        LocalDateTime before = harness.game().getGameDate();
        harness.continueOneDay();
        assertTrue(harness.game().getGameDate().isAfter(before));
    }

    private void advanceToMandatoryDrawDelivered() {
        harness.advanceUntil(save -> {
            if (save.getMainAuthority() == null || save.getMainAuthority().getLeagues() == null) {
                return false;
            }
            return !save.getMainAuthority().getLeagues().isEmpty();
        }, 25);

        harness.advanceUntil(save -> hasMandatoryDrawMessageInInbox(), 10);
        assertEquals(FuttoboruGameEngine.DRAW_ACTION, harness.getNextGameAction());
    }

    private boolean hasMandatoryDrawMessageInInbox() {
        if (harness.game().getAllMessages() == null) {
            return false;
        }
        for (Message message : harness.game().getAllMessages()) {
            if (message == null) {
                continue;
            }
            if ("LEAGUE_DRAW".equals(message.getMessageType())
                    && Boolean.TRUE.equals(message.getIsMandatory())
                    && !Boolean.TRUE.equals(message.getIsRead())) {
                return true;
            }
        }
        return false;
    }
}
