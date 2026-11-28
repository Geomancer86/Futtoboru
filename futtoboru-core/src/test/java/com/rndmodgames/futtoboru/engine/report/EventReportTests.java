package com.rndmodgames.futtoboru.engine.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEvent;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLevel;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventLog;
import com.rndmodgames.futtoboru.match.ecs.events.MatchEventType;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * E-17: the period report written from a match's stored ECS events. Goals, scorers and minutes come from the GOAL
 * events (an own goal has no scorer); the weather and ground from the log; a real disputed goal, a sending-off, men
 * carried off, a lace cut, a pressing start and a busy keeper from the events, named from the frozen team sheets; at
 * most 8 sentences, no raw keys or placeholders, in all four languages; the same match always reads the same; a
 * match without events still gets the G-03 report.
 */
class EventReportTests {

    private static final Map<String, Properties> bundles = new LinkedHashMap<>();
    private static final int HZ = 30;
    private static final long MIN = 60L * HZ;

    private SaveGame game;
    private Club preston;
    private Club burnley;

    @BeforeAll
    static void loadBundles() throws IOException {
        for (String name : MatchReportTests.BUNDLES) {
            Properties p = new Properties();
            try (InputStream in = EventReportTests.class.getResourceAsStream("/mods/language/" + name
                    + ".properties")) {
                assertNotNull(in, name);
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            bundles.put(name, p);
        }
    }

    @BeforeEach
    void world() {
        game = new SaveGame();
        game.setSeed(1888L);
        preston = club("Preston North End", "Deepdale", "Trainer", "Howarth", "Holmes", "Robertson", "Russell",
                "Graham", "Gordon", "Ross", "Goodall", "Dewhurst", "Drummond");
        burnley = club("Burnley", "Turf Moor", "Kay", "Bury", "Lang", "Abrahams", "Friel", "Keenan", "Brady",
                "McKay", "Yates", "Gallocher", "Haresnape");
        League league = new League();
        league.setId(game.getIdAllocator().nextId());
        league.setName("Football League");
        league.setLeagueClubs(new ArrayList<>(List.of(preston, burnley)));
        Authority authority = new Authority();
        authority.getLeagues().add(league);
        game.setMainAuthority(authority);
    }

    private Club club(String name, String ground, String... surnames) {
        Club club = new Club(name);
        club.setId(game.getIdAllocator().nextId());
        Stadium stadium = new Stadium();
        stadium.setName(ground);
        stadium.setCapacity(10000);
        club.setStadium(stadium);
        for (String surname : surnames) {
            Person person = new Person();
            person.setId(game.getIdAllocator().nextId());
            person.setName("J.");
            person.setLastname(surname);
            Player player = new Player();
            player.setId(game.getIdAllocator().nextId());
            player.setPerson(person);
            club.getPlayers().add(player);
        }
        game.getAllClubs().add(club);
        return club;
    }

    private static TeamSheet sheet(Club club) {
        TeamSheet sheet = new TeamSheet(club.getId(), TeamShape.TWO_THREE_FIVE, ClubStyle.DRIBBLING);
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            sheet.setPlayerId(s, club.getPlayers().get(s).getId());
        }
        return sheet;
    }

    private static final int H = 0, A = 1;

    private static int ref(int team, int slot) {
        return MatchEvent.ref(team, slot);
    }

    /** A played league match carrying {@code log}, its goals and minutes stored as {@code MatchSimulator} does. */
    private Match match(MatchEventLog log) {
        Match m = new Match();
        m.setId(game.getIdAllocator().nextId());
        m.setMatchType(Match.LEAGUE_MATCH);
        m.setHomeClubId(preston.getId());
        m.setAwayClubId(burnley.getId());
        m.setMatchDateTime(LocalDateTime.of(1888, 10, 6, 15, 30));
        m.setIsPlayed(true);
        m.setAttendance(7120);
        m.setHomeSheet(sheet(preston));
        m.setAwaySheet(sheet(burnley));
        int hg = 0;
        int ag = 0;
        for (MatchEvent g : log.of(MatchEventType.GOAL)) {
            boolean home = g.side() == com.rndmodgames.futtoboru.match.ecs.pitch.Team.HOME;
            (home ? m.getHomeGoalMinutes() : m.getAwayGoalMinutes()).add(g.minute());
            if (!g.ownGoal()) {
                (home ? m.getHomeScorerIds() : m.getAwayScorerIds()).add(MatchEvents.personId(game, m, g.actor()));
            }
            if (home) {
                hg++;
            } else {
                ag++;
            }
        }
        m.setHomeGoals(hg);
        m.setAwayGoals(ag);
        m.setEventLog(log.encode());
        preston.getPlayedMatches().add(m);
        burnley.getPlayedMatches().add(m);
        return m;
    }

    /** Ross scores, Burnley's disputed goal stands, a Preston own goal, a Burnley man sent off: 1-2 on a wet ground. */
    private static MatchEventLog eventful() {
        MatchEventLog log = new MatchEventLog(HZ, 45, Surface.WET.ordinal(), 360);
        log.add(0, MatchEventType.KICK_OFF, H, ref(H, 8), -1, 0, 0, 1);
        log.add(11 * MIN, MatchEventType.SHOT, H, ref(H, 7), -1, 35, 3, 300 << 1);
        log.add(11 * MIN + 20, MatchEventType.GOAL, H, ref(H, 7), -1, 50, 1, 0);
        log.add(11 * MIN + 200, MatchEventType.KICK_OFF, A, ref(A, 8), -1, 0, 0, 0);
        log.add(29 * MIN, MatchEventType.DISPUTED_GOAL, H, -1, -1, -50, 2,
                MatchEvent.appeal(1, 0, true, 0, false, 0) | MatchEvent.GOAL_STOOD);
        log.add(29 * MIN, MatchEventType.GOAL, A, ref(A, 8), -1, -50, 2, 2);
        log.add(31 * MIN, MatchEventType.HEAVY_BALL, -1, -1, -1, 0, 0, 470);
        log.add(45 * MIN - 1, MatchEventType.HALF_TIME, -1, -1, -1, 0, 0, 1 | (1 << 8));
        log.add(45 * MIN + 100, MatchEventType.KICK_OFF, A, ref(A, 8), -1, 0, 0, 1);
        log.add(55 * MIN, MatchEventType.FOUL, A, ref(A, 4), ref(H, 9), 10, 4, MatchEvent.appeal(1, 0, true, 1, true,
                1));
        log.add(55 * MIN, MatchEventType.SENDING_OFF, A, ref(A, 4), -1, 10, 4, 0);
        log.add(69 * MIN, MatchEventType.GOAL, A, ref(H, 2), -1, -50, 0, 1);
        log.add(90 * MIN - 1, MatchEventType.FULL_TIME, -1, -1, -1, 0, 0, 1 | (2 << 8));
        return log;
    }

    /** Goalless on a fine day: Preston press from the kick-off, Kay saves five times, Goodall carried off. */
    private static MatchEventLog quiet() {
        MatchEventLog log = new MatchEventLog(HZ, 45, Surface.DRY.ordinal(), 0);
        log.add(0, MatchEventType.KICK_OFF, H, ref(H, 8), -1, 0, 0, 1);
        for (int i = 0; i < 5; i++) {
            long t = (2 + i * 2) * MIN;
            log.add(t, MatchEventType.SHOT, H, ref(H, 6 + i), -1, 30, 0, 200 << 1);
            log.add(t + 30, MatchEventType.SAVE, A, ref(A, 0), ref(H, 6 + i), -50, 0, 4);
        }
        log.add(20 * MIN, MatchEventType.LACE_CUT, A, ref(A, 8), -1, 0, 0, 0);
        log.add(45 * MIN - 1, MatchEventType.HALF_TIME, -1, -1, -1, 0, 0, 0);
        log.add(45 * MIN + 100, MatchEventType.KICK_OFF, A, ref(A, 8), -1, 0, 0, 1);
        log.add(60 * MIN, MatchEventType.INJURY, H, ref(H, 8), ref(A, 3), 5, 5, MatchEvent.CARRIED_OFF);
        log.add(90 * MIN - 1, MatchEventType.FULL_TIME, -1, -1, -1, 0, 0, 0);
        return log;
    }

    private static void assertClean(MatchReport r, String where) {
        String text = r.getHeadline() + " " + r.getParagraph();
        assertFalse(text.contains("report_"), where + ": " + text);
        assertFalse(text.contains("{") || text.contains("}"), where + ": " + text);
        assertFalse(text.contains("null"), where + ": " + text);
        assertTrue(r.sentenceCount() >= 3 && r.sentenceCount() <= MatchReport.MAX_SENTENCES_WITH_EVENTS,
                r.sentenceCount() + " sentences, " + where + ": " + text);
    }

    /** True when the text holds one of the group's variants (its longest literal piece, ignoring case). */
    private static boolean containsOneOf(String text, Properties bundle, String group, int variants) {
        String lower = text.toLowerCase();
        for (int i = 1; i <= variants; i++) {
            String longest = "";
            for (String piece : bundle.getProperty(group + "_" + i).split("\\{\\d+\\}")) {
                longest = piece.trim().length() > longest.length() ? piece.trim() : longest;
            }
            if (lower.contains(longest.substring(0, Math.min(20, longest.length())).toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void goalsScorersAndMinutesComeFromTheEvents() {
        Match m = match(eventful());
        MatchReport r = MatchReport.of(game, m, bundles.get("BaseBundle_en_uk")::getProperty);
        assertTrue(r.isMinutesKnown());
        assertEquals(3, r.getGoals().size());
        assertEquals(List.of(12, 30, 70), r.getGoals().stream().map(MatchReport.GoalLine::minute).toList());
        assertEquals("Ross", r.getGoals().get(0).scorer());
        assertEquals("Yates", r.getGoals().get(1).scorer());
        assertNull(r.getGoals().get(2).scorer(), "an own goal has no scorer");
        assertEquals(1, r.getHalfTimeHome());
        assertEquals(1, r.getHalfTimeAway());
        assertTrue(r.getParagraph().contains("Ross (12"), r.getParagraph());
        assertTrue(r.getParagraph().contains("Yates (30"), r.getParagraph());
    }

    @Test
    void incidentsWeatherAndTheGroundAreTold() {
        Properties en = bundles.get("BaseBundle_en_uk");
        MatchReport busy = MatchReport.of(game, match(eventful()), en::getProperty);
        String text = busy.getParagraph();
        System.out.println("E-17 report: " + busy.getHeadline() + "\n" + text);
        assertTrue(busy.hasDisputedGoal(), text);
        assertTrue(containsOneOf(text, en, "report_weather_wet", 3), text);
        assertTrue(text.contains("Friel of Burnley"), "the man sent off: " + text);
        assertTrue(containsOneOf(text, en, "report_sent_off", 2), text);
        assertTrue(text.contains("Deepdale"), text);
        assertTrue(text.contains("7,000"), "the crowd: " + text);

        MatchReport calm = MatchReport.of(game, match(quiet()), en::getProperty);
        String quiet = calm.getParagraph();
        System.out.println("E-17 report: " + calm.getHeadline() + "\n" + quiet);
        assertTrue(containsOneOf(quiet, en, "report_weather_fine", 3), quiet);
        assertTrue(containsOneOf(quiet, en, "report_press", 3), "Preston pressed: " + quiet);
        assertTrue(quiet.contains("Goodall of Preston North End"), "carried off: " + quiet);
        assertTrue(quiet.contains("Yates of Burnley"), "the lace cut him: " + quiet);
        assertTrue(quiet.contains("Kay") && quiet.contains("five"), "the busy keeper: " + quiet);
        assertFalse(calm.hasDisputedGoal());
    }

    @Test
    void everyLanguageReadsCleanAndAMatchAlwaysReadsTheSame() {
        for (Map.Entry<String, Properties> b : bundles.entrySet()) {
            for (MatchEventLog log : List.of(eventful(), quiet())) {
                Match m = match(log);
                MatchReport r = MatchReport.of(game, m, b.getValue()::getProperty);
                assertClean(r, b.getKey());
                MatchReport again = MatchReport.of(game, m, b.getValue()::getProperty);
                assertEquals(r.getParagraph(), again.getParagraph());
            }
        }
    }

    @Test
    void theStoredEventsReadBackForTheTicker() {
        Match m = match(eventful());
        MatchEventLog log = MatchEvents.of(m);
        assertEquals(eventful(), log);
        List<MatchEvent> key = log.filtered(MatchEventLevel.KEY);
        assertTrue(key.stream().allMatch(e -> e.level() == MatchEventLevel.KEY));
        assertEquals("Friel", MatchEvents.surname(game, m, log.of(MatchEventType.SENDING_OFF).get(0).actor()));
        assertNull(MatchEvents.surname(game, m, -1));
        m.setEventLog(null);
        assertNull(MatchEvents.of(m));
        m.setEventLog("garbage");
        assertNull(MatchEvents.of(m));
        MatchReport r = MatchReport.of(game, m, bundles.get("BaseBundle_en_uk")::getProperty);
        assertTrue(r.sentenceCount() >= 3 && r.sentenceCount() <= 6, "the G-03 report without events");
    }
}
