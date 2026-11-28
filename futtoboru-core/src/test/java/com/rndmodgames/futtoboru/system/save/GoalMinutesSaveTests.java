package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

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
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.report.MatchReport;
import com.rndmodgames.futtoboru.engine.simulation.GoalMinutes;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-03 and saves: goal minutes are an optional field of the stored match. They survive a save and load; a save made
 * before G-03 (its matches have no minutes) still loads and its matches get a report without minutes; the inbox
 * result keeps the id of its match.
 */
class GoalMinutesSaveTests {

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(SeasonMemo.SEED), config);
        BaseTest.awaitHeadlessCreate();
    }

    private static SaveGame fixtureGame() throws IOException {
        try (InputStream in = GoalMinutesSaveTests.class.getClassLoader().getResourceAsStream(SaveGameTests.FIXTURE)) {
            assertNotNull(in, SaveGameTests.FIXTURE);
            LoadResult result = SaveCodec.decode(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            assertTrue(result.isLoaded(), result.toString());
            return result.getGame();
        }
    }

    private static Match played(SaveGame game, Club home, Club away, int homeGoals, int awayGoals) {
        Match m = new Match();
        m.setId(game.getIdAllocator().nextId());
        m.setMatchType(Match.LEAGUE_MATCH);
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setMatchDateTime(LocalDateTime.of(1888, 9, 8, 15, 0));
        m.setIsPlayed(true);
        m.setAttendance(5000);
        m.setHomeGoals(homeGoals);
        m.setAwayGoals(awayGoals);
        for (int i = 0; i < homeGoals; i++) {
            m.getHomeScorerIds().add(home.getPlayers().get(i).getPerson().getId());
        }
        for (int i = 0; i < awayGoals; i++) {
            m.getAwayScorerIds().add(away.getPlayers().get(i).getPerson().getId());
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
    void minutesSurviveAndAnOldSaveWithoutThemLoadsAndReports() throws IOException {
        SaveGame game = fixtureGame();
        Club home = game.getCurrentClub();
        Club away = null;
        for (Club c : game.getAllClubs()) {
            if (c != home && c.getPlayers().size() >= 3) {
                away = c;
                break;
            }
        }
        assertNotNull(away);
        Match timed = played(game, home, away, 2, 1);
        GoalMinutes.assign(timed, RandomSource.seeded(5).fork("minutes:" + timed.getId()));
        Match old = played(game, away, home, 3, 0);
        assertFalse(GoalMinutes.hasMinutes(old));

        Message result = new Message();
        result.setMessageType("MATCH_RESULT");
        result.setTitle("result");
        result.setActionData(old.getId());
        game.getAllMessages().add(result);

        String text = SaveCodec.encode(game, "G-03", LocalDateTime.of(2026, 11, 28, 12, 0));
        // a save made before G-03 never had the fields: take them out of the old match whatever the writer did
        JsonValue root = new JsonReader().parse(text);
        for (JsonValue m = root.get("matches").child; m != null; m = m.next) {
            if (m.getLong("id") == old.getId()) {
                m.remove("homeGoalMinutes");
                m.remove("awayGoalMinutes");
            }
        }
        LoadResult loaded = SaveCodec.decode(root.toJson(OutputType.json));
        assertTrue(loaded.isLoaded(), loaded.toString());
        SaveGame again = loaded.getGame();

        Match timedAgain = find(again, timed.getId());
        assertEquals(timed.getHomeGoalMinutes(), timedAgain.getHomeGoalMinutes());
        assertEquals(timed.getAwayGoalMinutes(), timedAgain.getAwayGoalMinutes());
        MatchReport timedReport = MatchReport.of(again, timedAgain);
        assertTrue(timedReport.isMinutesKnown());
        assertTrue(timedReport.getHalfTimeHome() >= 0);

        Match oldAgain = find(again, old.getId());
        assertTrue(oldAgain.getHomeGoalMinutes().isEmpty() && oldAgain.getAwayGoalMinutes().isEmpty());
        MatchReport oldReport = MatchReport.of(again, oldAgain);
        assertFalse(oldReport.isMinutesKnown());
        assertEquals(-1, oldReport.getHalfTimeHome());
        assertEquals(3, oldReport.getGoals().size());
        assertFalse(oldReport.getParagraph().isBlank());
        assertFalse(oldReport.getParagraph().contains("report_"), oldReport.getParagraph());
        String scorer = away.getPlayers().get(0).getPerson().getLastname();
        assertTrue(oldReport.getParagraph().contains(scorer), scorer + ": " + oldReport.getParagraph());

        Message resultAgain = null;
        for (Message m : again.getAllMessages()) {
            if ("MATCH_RESULT".equals(m.getMessageType())) {
                resultAgain = m;
            }
        }
        assertNotNull(resultAgain);
        assertTrue(resultAgain.getActionData() instanceof Number, String.valueOf(resultAgain.getActionData()));
        assertEquals(old.getId().longValue(), ((Number) resultAgain.getActionData()).longValue());
        assertEquals(List.of(), oldAgain.getHomeGoalMinutes());
    }
}
