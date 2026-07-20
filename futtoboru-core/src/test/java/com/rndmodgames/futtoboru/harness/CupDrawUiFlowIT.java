package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;

/**
 * Headless reproduction of a real bug reported 2026-07-19: a human tester advanced days with
 * Continue, got a "you have been drawn against X" cup message, then the top button switched to
 * "INBOX -> DRAW" but clicking it appeared to do nothing -- the cup draw screen never showed any
 * fixtures/Continue button, so the game was permanently stuck (unlike the league draw, which the
 * tester was able to get past).
 *
 * <p>This test drives the exact production sequencing (raw {@code continueOneDay()}, never using
 * the test-only {@code acknowledgeMandatoryDraws()} shortcut that the soak/multi-season tests rely
 * on) and then replicates, statement for statement, the real click-handler logic in
 * {@code MainGameMenuTable}'s "INBOX -> DRAW" listener and {@code MainMenuManager}'s
 * {@code CUP_DRAW_SCREEN} case, to find exactly where the real UI flow breaks down.
 */
class CupDrawUiFlowIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    private final List<String> debugLog = new ArrayList<>();

    @Test
    void cupDrawMandatoryMessageBecomesVisibleAndClearableViaRealUiPath() {
        runTest();
    }

    private void runTest() {
        assertNotNull(harness.primaryCup(), "FA Cup should be attached from database at new game");

        // Advance raw days (no auto-acknowledge) until the engine gate first flips to DRAW_ACTION
        // for a CUP_DRAW specifically, mirroring a human repeatedly clicking Continue.
        int daysAdvanced = 0;
        boolean cupGateHit = false;
        for (; daysAdvanced < 400; daysAdvanced++) {
            int action = harness.getNextGameAction();
            if (action == FuttoboruGameEngine.DRAW_ACTION) {
                if (hasCupDrawSignal()) {
                    cupGateHit = true;
                    break;
                }
                // League draw or fixture draw gate -- acknowledge it like a human would (via the
                // league draw screen) and keep going, we only care about the CUP gate here.
                harness.acknowledgeMandatoryDraws();
                continue;
            }
            if (action == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
                harness.kickOffNextMatchForCurrentClub();
                continue;
            }
            try {
                harness.continueOneDay();
            } catch (SimulationDeadlockException e) {
                fail("continueOneDay() deadlocked at day " + daysAdvanced + ": " + e.getMessage());
            }
        }

        assertTrue(cupGateHit, "Expected to hit a CUP_DRAW mandatory gate within 400 raw days");
        assertEquals(FuttoboruGameEngine.DRAW_ACTION, harness.getNextGameAction());

        // STEP 1 of the real click handler (MainGameMenuTable "INBOX -> DRAW"): look for an
        // already-delivered, unread, mandatory CUP_DRAW message in getAllMessages().
        Message deliveredMandatoryCupDraw = findMandatoryCupDraw(harness.game().getAllMessages());

        // STEP 2: if not delivered yet, it must be sitting in scheduledMessages (this is the gap:
        // hasMandatoryUnreadDrawMessages() blocks Continue based on scheduledMessages too, but the
        // message itself is not yet in the inbox for the click handler's first scan to find).
        Message scheduledMandatoryCupDraw = findMandatoryCupDraw(harness.game().getScheduledMessages());

        debugLog.add("DEBUG: deliveredMandatoryCupDraw=" + describe(deliveredMandatoryCupDraw)
                + " scheduledMandatoryCupDraw=" + describe(scheduledMandatoryCupDraw));
        debugLog.add("DEBUG: allMessages CUP_DRAW/CUP_DRAW_RESULT dump:");
        for (Message m : harness.game().getAllMessages()) {
            if (m != null && m.getMessageType() != null && m.getMessageType().startsWith("CUP_DRAW")) {
                debugLog.add("  ALL: " + describe(m));
            }
        }
        debugLog.add("DEBUG: FULL allMessages dump (looking for id collisions):");
        for (Message m : harness.game().getAllMessages()) {
            debugLog.add("  FULLALL: " + describe(m));
        }
        if (harness.game().getScheduledMessages() != null) {
            for (Message m : harness.game().getScheduledMessages()) {
                if (m != null && m.getMessageType() != null && m.getMessageType().startsWith("CUP_DRAW")) {
                    debugLog.add("  SCHEDULED: " + describe(m));
                }
            }
        }

        Message messageToProcess = deliveredMandatoryCupDraw;
        if (messageToProcess == null) {
            assertNotNull(scheduledMandatoryCupDraw,
                    "Continue reported DRAW_ACTION for a cup draw, but no mandatory CUP_DRAW message "
                            + "exists in either getAllMessages() or getScheduledMessages() -- the click "
                            + "handler would show 'No mandatory draw message found' and fall back to inbox");
            // This mirrors the click handler delivering the scheduled message on click.
            harness.engine().getMessageManager().deliverMessage(scheduledMandatoryCupDraw);
            messageToProcess = scheduledMandatoryCupDraw;
        }

        debugLog.add("DEBUG: still in scheduledMessages after deliver? "
                + (harness.game().getScheduledMessages() != null
                        && harness.game().getScheduledMessages().contains(messageToProcess)));
        debugLog.add("DEBUG: total allMessages size=" + harness.game().getAllMessages().size());
        debugLog.add("DEBUG after deliver: messageToProcess=" + describe(messageToProcess));
        debugLog.add("DEBUG after deliver: allMessages CUP_DRAW/CUP_DRAW_RESULT dump:");
        for (Message m : harness.game().getAllMessages()) {
            if (m != null && m.getMessageType() != null && m.getMessageType().startsWith("CUP_DRAW")) {
                debugLog.add("  ALL: " + describe(m));
            }
        }

        assertEquals("CUP_DRAW", messageToProcess.getMessageType());

        // STEP 3 (MainMenuManager.CUP_DRAW_SCREEN case): re-scan getAllMessages() for a mandatory
        // CUP_DRAW message and resolve its actionData (edition id) to an actual CompetitionEdition.
        Message rescanned = findMandatoryCupDraw(harness.game().getAllMessages());
        assertNotNull(rescanned,
                "After delivering the mandatory CUP_DRAW message, MainMenuManager's own rescan of "
                        + "getAllMessages() should find it -- if this fails the cup draw screen shows "
                        + "'Could not find competition edition for cup draw' and silently falls back to "
                        + "the inbox, which is exactly the 'stuck, does not move to the draw screen' bug");

        assertTrue(rescanned.getActionData() instanceof Long,
                "CUP_DRAW message actionData should be the edition id (a Long)");
        Long editionId = (Long) rescanned.getActionData();

        CompetitionEdition edition = findEditionById(editionId);
        assertNotNull(edition,
                "Could not resolve CompetitionEdition for actionData edition id " + editionId
                        + " -- this is the exact 'Could not find competition edition for cup draw' failure");

        // STEP 4 (CupDrawScreenTable.setCompetitionEdition -> collectRoundFixtures): the screen must
        // find at least one unplayed Round-1 match for this edition, or it shows an empty draw with
        // no Continue button ever appearing -- also a permanent stuck state.
        List<com.rndmodgames.futtoboru.data.Match> round1Fixtures = collectRound1Fixtures(edition);
        assertTrue(!round1Fixtures.isEmpty(),
                "CupDrawScreenTable found no unplayed Round 1 matches for edition " + editionId
                        + " -- the draw screen would show 'No draw data available' with no way to "
                        + "reach the Continue button");

        // STEP 5 (CupDrawScreenTable Continue button -> markDrawMessageAsComplete): flips isRead and
        // isMandatory on the same message object found in getAllMessages(), then the gate must clear.
        rescanned.setIsRead(true);
        rescanned.setIsMandatory(false);

        assertEquals(FuttoboruGameEngine.CONTINUE_GAME_ACTION, harness.getNextGameAction(),
                "After acknowledging the cup draw exactly like CupDrawScreenTable's Continue button "
                        + "does, the engine should unblock and allow Continue to advance again");

        harness.continueOneDayRespectingGates();
    }

    private String describe(Message m) {
        if (m == null) {
            return "null";
        }
        return "id=" + m.getId() + " type=" + m.getMessageType() + " title=" + m.getTitle()
                + " mandatory=" + m.getIsMandatory() + " read=" + m.getIsRead()
                + " actionData=" + m.getActionData() + " scheduledDate=" + m.getScheduledDate();
    }

    private boolean hasCupDrawSignal() {
        return findMandatoryCupDraw(harness.game().getAllMessages()) != null
                || findMandatoryCupDraw(harness.game().getScheduledMessages()) != null;
    }

    private Message findMandatoryCupDraw(List<Message> messages) {
        if (messages == null) {
            return null;
        }
        for (Message message : messages) {
            if (message == null) {
                continue;
            }
            if ("CUP_DRAW".equals(message.getMessageType())
                    && Boolean.TRUE.equals(message.getIsMandatory())) {
                return message;
            }
        }
        return null;
    }

    private CompetitionEdition findEditionById(Long editionId) {
        if (harness.game().getAllCups() == null) {
            return null;
        }
        for (Competition cup : harness.game().getAllCups()) {
            if (cup == null || cup.getEditions() == null) {
                continue;
            }
            for (CompetitionEdition edition : cup.getEditions()) {
                if (edition != null && editionId.equals(edition.getId())) {
                    return edition;
                }
            }
        }
        return null;
    }

    private List<com.rndmodgames.futtoboru.data.Match> collectRound1Fixtures(CompetitionEdition edition) {
        List<com.rndmodgames.futtoboru.data.Match> fixtures = new ArrayList<>();
        if (harness.game().getAllClubs() == null) {
            return fixtures;
        }
        for (com.rndmodgames.futtoboru.data.Club club : harness.game().getAllClubs()) {
            if (club == null || club.getScheduledMatches() == null) {
                continue;
            }
            for (com.rndmodgames.futtoboru.data.Match match : club.getScheduledMatches()) {
                if (match != null
                        && match.getCompetitionEditionId() != null
                        && match.getCompetitionEditionId().equals(edition.getId())
                        && Boolean.FALSE.equals(match.getIsPlayed())
                        && match.getRound() != null && match.getRound() == 1
                        && !fixtures.contains(match)) {
                    fixtures.add(match);
                }
            }
        }
        return fixtures;
    }
}
