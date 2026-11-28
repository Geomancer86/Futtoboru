package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.report.MatchEvents;
import com.rndmodgames.futtoboru.engine.report.MatchReport;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEvent;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLog;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventType;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * E-17 and saves: a match's event log (D19-9) is an optional field of the stored match. It survives a save and load
 * unchanged; a save made before E-17 (no field) still loads, its match has no events and still gets a report.
 */
class EventLogSaveTests {

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(SeasonMemo.SEED), config);
        BaseTest.awaitHeadlessCreate();
    }

    private static SaveGame fixtureGame() throws IOException {
        try (InputStream in = EventLogSaveTests.class.getClassLoader().getResourceAsStream(SaveGameTests.FIXTURE)) {
            assertNotNull(in, SaveGameTests.FIXTURE);
            LoadResult result = SaveCodec.decode(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            assertTrue(result.isLoaded(), result.toString());
            return result.getGame();
        }
    }

    private static Match played(SaveGame game, Club home, Club away, MatchEventLog log) {
        Match m = new Match();
        m.setId(game.getIdAllocator().nextId());
        m.setMatchType(Match.LEAGUE_MATCH);
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setMatchDateTime(LocalDateTime.of(1888, 9, 15, 15, 0));
        m.setIsPlayed(true);
        m.setAttendance(4000);
        m.setHomeGoals(1);
        m.setAwayGoals(0);
        m.getHomeGoalMinutes().add(23);
        if (log != null) {
            m.setEventLog(log.encode());
        }
        home.getPlayedMatches().add(m);
        away.getPlayedMatches().add(m);
        return m;
    }

    private static Match find(SaveGame game, Long id) {
        for (Club club : game.getAllClubs()) {
            for (Match m : club.getPlayedMatches()) {
                if (id.equals(m.getId())) {
                    return m;
                }
            }
        }
        throw new AssertionError("match " + id + " not loaded");
    }

    @Test
    void theEventLogSurvivesAndAnOldSaveWithoutItLoads() throws IOException {
        SaveGame game = fixtureGame();
        Club home = game.getCurrentClub();
        Club away = null;
        for (Club c : game.getAllClubs()) {
            if (c != home) {
                away = c;
                break;
            }
        }
        assertNotNull(away);
        MatchEventLog log = new MatchEventLog(30, 45, Surface.MUDDY.ordinal(), 800);
        log.add(0, MatchEventType.KICK_OFF, 0, MatchEvent.ref(0, 8), -1, 0, 0, 1);
        log.add(22 * 1800 + 5, MatchEventType.GOAL, 0, MatchEvent.ref(0, 9), -1, 50.3, -1.2, 0);
        log.add(30 * 1800, MatchEventType.INJURY, 1, MatchEvent.ref(1, 2), MatchEvent.ref(0, 9), -20, 10,
                MatchEvent.PASSENGER);
        log.add(45 * 1800 - 1, MatchEventType.HALF_TIME, -1, -1, -1, 0, 0, 1);
        log.add(90 * 1800 - 1, MatchEventType.FULL_TIME, -1, -1, -1, 0, 0, 1);
        Match withEvents = played(game, home, away, log);
        Match old = played(game, away, home, log);

        String text = SaveCodec.encode(game, "E-17", LocalDateTime.of(2026, 11, 28, 12, 0));
        JsonValue root = new JsonReader().parse(text);
        boolean written = false;
        for (JsonValue m = root.get("matches").child; m != null; m = m.next) {
            if (m.getLong("id") == withEvents.getId()) {
                written = log.encode().equals(m.getString("eventLog", null));
            }
            if (m.getLong("id") == old.getId()) {
                m.remove("eventLog"); // a save made before E-17 never had the field
            }
        }
        assertTrue(written, "the log is written as its compact string");
        LoadResult loaded = SaveCodec.decode(root.toJson(OutputType.json));
        assertTrue(loaded.isLoaded(), loaded.toString());
        SaveGame again = loaded.getGame();

        Match back = find(again, withEvents.getId());
        assertEquals(withEvents.getEventLog(), back.getEventLog());
        assertEquals(log, MatchEvents.of(back));
        assertEquals(23, MatchEvents.of(back).of(MatchEventType.GOAL).get(0).minute());

        Match oldBack = find(again, old.getId());
        assertNull(oldBack.getEventLog());
        assertNull(MatchEvents.of(oldBack));
        MatchReport report = MatchReport.of(again, oldBack);
        assertTrue(!report.getParagraph().isBlank() && !report.getParagraph().contains("report_"),
                report.getParagraph());
    }
}
