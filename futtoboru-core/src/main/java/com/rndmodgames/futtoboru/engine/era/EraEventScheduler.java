package com.rndmodgames.futtoboru.engine.era;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.EraProfile;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-11: the authorities and the press announce era changes (EraProfile events) in the inbox. Each game day the
 * engine calls {@link #announce}; every event announced after the previous day up to today becomes one inbox message
 * (category AUTHORITY, sender = the event's authority, text from the {@code <key>.title/.body} language keys with
 * {0}.. filled from the event's args). The ids already announced are kept in the save, so an event is never sent
 * twice (a season jump sends each once, a reload sends none again). Announcements never block time: the data's
 * {@code mandatory} flag only raises the priority to URGENT (the proposal's decision 3 is still open).
 */
public final class EraEventScheduler {

    public static final String MESSAGE_TYPE = "ERA_EVENT";

    private final EraProfile era;
    private final MessageManager messages;

    public EraEventScheduler(EraProfile era, MessageManager messages) {
        this.era = era;
        this.messages = messages;
    }

    /**
     * Delivers the events announced in ({@code previousDay}, {@code today}].
     *
     * @return the messages delivered, in announcement order
     */
    public List<Message> announce(SaveGame game, LocalDate previousDay, LocalDate today) {
        List<Message> out = new ArrayList<>();
        LocalDate start = game.getGameStartDate() == null ? null : game.getGameStartDate().toLocalDate();
        for (EraProfile.Event e : era.announcedBetween(previousDay, today)) {
            if (game.getAnnouncedEraEvents().contains(e.id())) {
                continue;
            }
            game.getAnnouncedEraEvents().add(e.id());
            if (start != null && e.announced().isBefore(start)) {
                continue; // history before the game began: folded into the baseline, not sent
            }
            Message m = build(e, today);
            messages.deliverMessage(m);
            out.add(m);
        }
        return out;
    }

    private static final String TITLE = ".title";
    private static final String BODY = ".body";

    private static String text(EraProfile.Event e, String part, Object[] args) {
        return LanguageModLoader.format(e.messageKey() + part, args);
    }

    static Message build(EraProfile.Event e, LocalDate today) {
        Message m = new Message();
        m.setCategory(MessageCategory.AUTHORITY);
        m.setMessageType(MESSAGE_TYPE);
        m.setSender(e.authority());
        m.setActionData(e.id());
        m.setPriority(e.mandatory() ? MessagePriority.URGENT
                : "LOW".equals(e.priority()) ? MessagePriority.LOW : MessagePriority.NORMAL);
        Object[] args = e.messageArgs().toArray();
        m.setTitle(text(e, TITLE, args));
        m.setPlainTextMessage(text(e, BODY, args));
        m.setMessageTime(today.atStartOfDay());
        m.setIsRead(false);
        m.setIsDeleted(false);
        m.setIsMandatory(false);
        return m;
    }
}
