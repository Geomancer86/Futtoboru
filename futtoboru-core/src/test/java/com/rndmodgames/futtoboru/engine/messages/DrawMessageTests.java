package com.rndmodgames.futtoboru.engine.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;

/**
 * P2-3: a mandatory cup draw message of a later season was dropped as a duplicate (same type and title as the
 * first season's), so its scheduled copy blocked Continue forever (the season test stopped on 1 Jan 1889).
 */
class DrawMessageTests extends BaseTest {

    @Test
    void everySeasonsDrawIsDeliveredAndCanBeRead() {
        EngineTestWorld world = new EngineTestWorld(application, 4);
        MessageManager messages = world.engine.getMessageManager();
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        cup.edition.setName("1888-89 Test Cup");

        Message first = messages.createCupDrawMessage(cup.competition, cup.edition, 1, "First Round");
        messages.deliverMessage(first);
        first.setIsRead(true);
        first.setIsMandatory(false);

        CompetitionEdition next = new CompetitionEdition();
        next.setId(world.game.getIdAllocator().nextId());
        next.setName("Test Cup 1889-90");
        Message second = messages.createCupDrawMessage(cup.competition, next, 1, "First Round");
        assertNotEquals(first.getTitle(), second.getTitle(), "the season is in the title");

        // scheduled for tomorrow, then delivered early by the INBOX -> DRAW button
        second.setScheduledDate(world.game.getGameDate().plusDays(1));
        messages.scheduleMessage(second);
        assertTrue(MatchResultRecorder.hasMandatoryDrawBlocker(world.game));
        messages.deliverMessage(second);
        assertTrue(world.game.getAllMessages().contains(second), "delivered");
        assertFalse(world.game.getScheduledMessages().contains(second), "no longer scheduled");

        // the draw screen's Continue clears the block
        second.setIsRead(true);
        second.setIsMandatory(false);
        assertFalse(MatchResultRecorder.hasMandatoryDrawBlocker(world.game));
    }

    @Test
    void aMandatoryMessageIsNeverDroppedAsADuplicate() {
        EngineTestWorld world = new EngineTestWorld(application, 2);
        MessageManager messages = world.engine.getMessageManager();
        EngineTestWorld.Cup cup = world.cup(world.clubs);

        Message a = messages.createCupDrawMessage(cup.competition, cup.edition, 1, "First Round");
        Message b = messages.createCupDrawMessage(cup.competition, cup.edition, 1, "First Round");
        assertEquals(a.getTitle(), b.getTitle());
        messages.deliverMessage(a);
        messages.deliverMessage(b);
        assertTrue(world.game.getAllMessages().contains(b), "a mandatory message must reach the inbox");

        // ordinary messages are still deduplicated by type and title
        Message w1 = new Message();
        w1.setMessageType("WELCOME");
        w1.setTitle("Welcome");
        Message w2 = new Message();
        w2.setMessageType("WELCOME");
        w2.setTitle("Welcome");
        messages.deliverMessage(w1);
        messages.deliverMessage(w2);
        assertFalse(world.game.getAllMessages().contains(w2));
    }
}
