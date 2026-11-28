package com.rndmodgames.futtoboru.engine.era;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.EraProfile;

/** G-11: era events reach the inbox on their announcement day, once. */
class EraEventSchedulerTests extends BaseTest {

    private static List<Message> era(SaveGame game) {
        return game.getAllMessages().stream().filter(m -> EraEventScheduler.MESSAGE_TYPE.equals(m.getMessageType()))
                .collect(Collectors.toList());
    }

    private static List<String> ids(SaveGame game) {
        return era(game).stream().map(m -> (String) m.getActionData()).collect(Collectors.toList());
    }

    private static EngineTestWorld world() {
        EngineTestWorld world = new EngineTestWorld(application, 2);
        // the day loop trains every player: give the bare test players an age and every attribute
        world.clubs.forEach(c -> c.getPlayers().forEach(p -> {
            p.getPerson().setBirthDate(LocalDateTime.of(1865, 1, 1, 0, 0));
            for (java.lang.reflect.Field f : Player.class.getDeclaredFields()) {
                if (f.getType() == Float.class) {
                    try {
                        f.setAccessible(true);
                        f.set(p, 10f);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }));
        return world;
    }

    @Test
    void messagesAppearOnTheirDaysThroughTheDayLoop() {
        EngineTestWorld world = world();
        world.game.setGameDate(LocalDateTime.of(1891, 5, 31, 12, 0));
        for (int day = 0; day < 3; day++) { // 31 May .. 2 June
            world.engine.continueGame();
        }
        // 1 June: league-1891-fourteen-clubs; 2 June: the two IFAB laws
        assertEquals(List.of("league-1891-fourteen-clubs", "ifab-1891-penalty-kick", "ifab-1891-referee-linesmen"),
                ids(world.game));
        Message penalty = era(world.game).get(1);
        assertEquals(MessageCategory.AUTHORITY, penalty.getCategory());
        assertEquals("ifab", penalty.getSender());
        assertEquals("New law: the penalty kick", penalty.getTitle());
        assertFalse(penalty.getPlainTextMessage().contains("{0}"));
        assertFalse(Boolean.TRUE.equals(penalty.getIsMandatory()), "never blocks time");
        assertEquals(LocalDate.of(1891, 6, 2), penalty.getMessageTime().toLocalDate());
        assertTrue(world.engine.currentEra(LocalDate.of(1891, 9, 1)).getBoolean("rules.penaltyKick"));
        assertFalse(world.engine.currentEra(LocalDate.of(1891, 8, 31)).getBoolean("rules.penaltyKick"));
    }

    @Test
    void aSeasonJumpAnnouncesEachEventOnceAndTheNetsOnTheirDay() {
        EngineTestWorld world = world();
        EraProfile profile = world.engine.era();
        EraEventScheduler scheduler = new EraEventScheduler(profile, world.engine.getMessageManager());
        LocalDate day = LocalDate.of(1891, 7, 31);
        for (; day.isBefore(LocalDate.of(1891, 8, 3)); day = day.plusDays(1)) {
            scheduler.announce(world.game, day, day.plusDays(1));
        }
        assertEquals(List.of("league-1891-goal-nets"), ids(world.game));
        // one big jump over a whole season: everything in it, once
        List<Message> jump = scheduler.announce(world.game, LocalDate.of(1888, 9, 1), LocalDate.of(1892, 9, 1));
        assertFalse(jump.isEmpty());
        assertEquals(ids(world.game).size(), ids(world.game).stream().distinct().count());
        assertTrue(scheduler.announce(world.game, LocalDate.of(1888, 9, 1), LocalDate.of(1892, 9, 1)).isEmpty());
    }

    @Test
    void replayingADayAnnouncesNothingAgain() {
        EngineTestWorld world = world();
        world.game.setGameDate(LocalDateTime.of(1891, 6, 1, 12, 0));
        world.engine.continueGame();
        int before = ids(world.game).size();
        assertTrue(before > 0);
        assertEquals(before, world.game.getAnnouncedEraEvents().size());

        // replay the same day on the loaded game: nothing new
        EraEventScheduler scheduler = new EraEventScheduler(world.engine.era(), world.engine.getMessageManager());
        assertTrue(scheduler.announce(world.game, LocalDate.of(1891, 6, 1), LocalDate.of(1891, 6, 2)).isEmpty());
    }

    @Test
    void everyEventHasEnglishAndSpanishText() throws Exception {
        for (String bundle : new String[] { "en_uk", "en_us", "sp_sp", "sp_la" }) {
            Properties p = new Properties();
            try (java.io.InputStream in = getClass().getResourceAsStream("/mods/language/BaseBundle_" + bundle
                    + ".properties")) {
                p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            }
            for (EraProfile.Event e : EraProfile.loadDefault().events()) {
                for (String part : new String[] { ".title", ".body" }) {
                    String v = p.getProperty(e.messageKey() + part);
                    assertTrue(v != null && !v.isBlank(), bundle + " " + e.messageKey() + part);
                    for (int i = e.messageArgs().size(); i < 5; i++) {
                        assertFalse(v.contains("{" + i + "}"), bundle + " " + e.messageKey() + " unfilled {" + i + "}");
                    }
                }
            }
        }
    }
}
