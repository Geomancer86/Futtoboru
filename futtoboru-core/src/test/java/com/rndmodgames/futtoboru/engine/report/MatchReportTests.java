package com.rndmodgames.futtoboru.engine.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.simulation.GoalMinutes;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-03: the match report stand-in. Every key in all four languages with the same placeholders; a report for wins,
 * draws, defeats, goalless games, routs, cup ties, friendlies and old matches without minutes in every language,
 * 3 to 6 sentences, no raw keys; goals in minute order with the half-time score; the same match always reads the
 * same and a season does not repeat itself; disputed goals only before goal nets.
 */
class MatchReportTests {

    static final List<String> BUNDLES = List.of("BaseBundle_en_us", "BaseBundle_en_uk", "BaseBundle_sp_sp",
            "BaseBundle_sp_la");

    private static final Map<String, Properties> bundles = new LinkedHashMap<>();

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\d+\\}");

    private SaveGame game;
    private Club preston;
    private Club burnley;
    private Competition cup;
    private CompetitionEdition edition;

    @BeforeAll
    static void loadBundles() throws IOException {
        for (String name : BUNDLES) {
            Properties p = new Properties();
            try (InputStream in = MatchReportTests.class.getResourceAsStream("/mods/language/" + name + ".properties")) {
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
        preston = club("Preston North End", "Deepdale", "Ross", "Goodall", "Dewhurst", "Drummond", "Gordon");
        burnley = club("Burnley", "Turf Moor", "Brady", "McKay", "Yates", "Friel", "Abrahams");
        League league = new League();
        league.setId(game.getIdAllocator().nextId());
        league.setName("Football League");
        league.setLeagueClubs(new ArrayList<>(List.of(preston, burnley)));
        Authority authority = new Authority();
        authority.getLeagues().add(league);
        game.setMainAuthority(authority);

        cup = new Competition();
        cup.setId(game.getIdAllocator().nextId());
        cup.setName("FA Cup");
        cup.setCompetitionType(Competition.COMPETITION_CUP);
        edition = new CompetitionEdition();
        edition.setId(game.getIdAllocator().nextId());
        cup.getEditions().add(edition);
        game.getAllCups().add(cup);
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
            player.setPerson(person);
            club.getPlayers().add(player);
        }
        game.getAllClubs().add(club);
        return club;
    }

    /** A club without players has no scorer recorded (as {@code MatchSimulator} leaves it): a fresh, unknown id. */
    private Long scorer(Club club, int i) {
        if (club.getPlayers().isEmpty()) {
            return game.getIdAllocator().nextId();
        }
        return club.getPlayers().get(i % club.getPlayers().size()).getPerson().getId();
    }

    /** A played match; {@code minutes} null = an old match without minutes. Scorers: players 0, 1, 2... */
    private Match match(int type, Club home, Club away, int[] homeMinutes, int[] awayMinutes, boolean withMinutes) {
        Match m = new Match();
        m.setId(game.getIdAllocator().nextId());
        m.setMatchType(type);
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setMatchDateTime(LocalDateTime.of(1888, 9, 8, 15, 0));
        m.setIsPlayed(true);
        m.setAttendance(6170);
        m.setHomeGoals(homeMinutes.length);
        m.setAwayGoals(awayMinutes.length);
        for (int i = 0; i < homeMinutes.length; i++) {
            m.getHomeScorerIds().add(scorer(home, i));
            if (withMinutes) {
                m.getHomeGoalMinutes().add(homeMinutes[i]);
            }
        }
        for (int i = 0; i < awayMinutes.length; i++) {
            m.getAwayScorerIds().add(scorer(away, i));
            if (withMinutes) {
                m.getAwayGoalMinutes().add(awayMinutes[i]);
            }
        }
        if (type == Match.CUP_MATCH) {
            m.setCompetitionId(cup.getId());
            m.setCompetitionEditionId(edition.getId());
            m.setRound(1);
        }
        home.getPlayedMatches().add(m);
        away.getPlayedMatches().add(m);
        return m;
    }

    private Match league(int[] home, int[] away) {
        return match(Match.LEAGUE_MATCH, preston, burnley, home, away, true);
    }

    private static int[] mins(int... m) {
        return m;
    }

    private static MatchReport report(SaveGame game, Match m, Properties bundle) {
        return MatchReport.of(game, m, bundle::getProperty);
    }

    // ------------------------------------------------------------------------------------------------ keys

    private static Set<String> placeholders(String value) {
        Set<String> found = new TreeSet<>();
        Matcher m = PLACEHOLDER.matcher(value);
        while (m.find()) {
            found.add(m.group());
        }
        return found;
    }

    @Test
    void everyKeyIsInAllFourBundlesWithTheSamePlaceholders() {
        List<String> keys = new ArrayList<>(MatchReport.keys());
        keys.addAll(List.of("match_goals_heading", "match_half_time", "match_report_heading", "match_goal_minute",
                "match_no_minutes", "match_no_goals", "match_scorer_unknown", "inbox_read_report",
                "league_latest_results"));
        Properties english = bundles.get("BaseBundle_en_uk");
        List<String> problems = new ArrayList<>();
        for (String key : keys) {
            String reference = english.getProperty(key);
            for (Map.Entry<String, Properties> bundle : bundles.entrySet()) {
                String value = bundle.getValue().getProperty(key);
                if (value == null || value.isBlank()) {
                    problems.add(key + " missing in " + bundle.getKey());
                } else if (reference != null && !placeholders(value).equals(placeholders(reference))) {
                    problems.add(key + " in " + bundle.getKey() + " has " + placeholders(value) + ", en_UK has "
                            + placeholders(reference));
                }
            }
        }
        if (!problems.isEmpty()) {
            fail(String.join("\n", problems));
        }
    }

    // --------------------------------------------------------------------------------------- situations

    /** One match of each kind the report tells apart. */
    private List<Match> everySituation() {
        List<Match> matches = new ArrayList<>();
        matches.add(league(mins(88), mins()));                          // late winner
        matches.add(league(mins(50, 81), mins(20)));                     // comeback
        matches.add(league(mins(4, 30, 77), mins()));                    // early lead, led throughout
        matches.add(league(mins(3, 11, 25, 44, 60, 89), mins()));        // rout
        matches.add(league(mins(70), mins(12, 33, 85)));                 // away win
        matches.add(league(mins(10, 40), mins(55, 86)));                 // late equaliser
        matches.add(league(mins(35), mins(62)));                         // draw
        matches.add(league(mins(), mins()));                             // goalless
        matches.add(league(mins(1, 2, 3, 4, 5, 6, 7), mins(90)));         // big score, a goal in the last minute
        matches.add(match(Match.FRIENDLY_MATCH, burnley, preston, mins(9, 66, 70, 80), mins(30, 46), true));
        matches.add(match(Match.LEAGUE_MATCH, preston, burnley, mins(10, 60), mins(30), false)); // old: no minutes
        matches.add(match(Match.CUP_MATCH, preston, burnley, mins(30, 60), mins(75), true));     // cup final
        matches.add(match(Match.CUP_MATCH, burnley, preston, mins(44), mins(80), true));         // cup draw
        Club nobody = club("Halliwell", "Bennetts Lane"); // no players: scorers unknown
        matches.add(match(Match.LEAGUE_MATCH, nobody, preston, mins(15, 70), mins(), true));
        return matches;
    }

    @Test
    void everySituationReadsWellInEveryLanguage() {
        List<Match> matches = everySituation();
        for (Map.Entry<String, Properties> bundle : bundles.entrySet()) {
            for (long seed = 1; seed <= 40; seed++) {
                game.setSeed(seed);
                for (Match m : matches) {
                    MatchReport r = report(game, m, bundle.getValue());
                    String where = bundle.getKey() + " seed " + seed + " " + m.getHomeGoals() + "-" + m.getAwayGoals()
                            + ": " + r.getHeadline() + " | " + r.getParagraph();
                    for (String text : List.of(r.getHeadline(), r.getParagraph())) {
                        assertFalse(text.isBlank(), where);
                        assertFalse(text.contains("report_"), "a raw key: " + where);
                        assertFalse(text.contains("{") || text.contains("}"), "a placeholder left: " + where);
                        assertFalse(text.contains("  ") || text.contains(" ."), "spacing: " + where);
                        assertFalse(text.contains("null"), where);
                    }
                    assertTrue(r.sentenceCount() >= 3 && r.sentenceCount() <= 6,
                            r.sentenceCount() + " sentences: " + where);
                    Club home = game.getClubById(m.getHomeClubId());
                    Club away = game.getClubById(m.getAwayClubId());
                    assertTrue(r.getParagraph().contains(home.getName()) && r.getParagraph().contains(away.getName()),
                            where);
                    assertEquals(m.getHomeGoals() + m.getAwayGoals(), r.getGoals().size(), where);
                    for (MatchReport.GoalLine g : r.getGoals()) {
                        if (g.scorer() != null) {
                            assertTrue(r.getParagraph().contains(g.scorer()), "scorer " + g.scorer() + ": " + where);
                        }
                    }
                }
            }
        }
    }

    @Test
    void goalsComeInMinuteOrderWithTheHalfTimeScore() {
        Match m = league(mins(10, 70), mins(30));
        MatchReport r = report(game, m, bundles.get("BaseBundle_en_uk"));
        assertTrue(r.isMinutesKnown());
        List<MatchReport.GoalLine> g = r.getGoals();
        assertEquals(List.of(10, 30, 70), List.of(g.get(0).minute(), g.get(1).minute(), g.get(2).minute()));
        assertEquals("Ross", g.get(0).scorer());
        assertEquals("Brady", g.get(1).scorer());
        assertEquals("Goodall", g.get(2).scorer());
        assertEquals("1-0 1-1 2-1", g.get(0).homeScore() + "-" + g.get(0).awayScore() + " " + g.get(1).homeScore()
                + "-" + g.get(1).awayScore() + " " + g.get(2).homeScore() + "-" + g.get(2).awayScore());
        assertEquals(1, r.getHalfTimeHome());
        assertEquals(1, r.getHalfTimeAway());
        assertTrue(r.getParagraph().contains("Ross (10 min.)"), r.getParagraph());
        assertTrue(r.getParagraph().contains("Deepdale"), r.getParagraph());
        assertTrue(r.getParagraph().contains("Saturday"), r.getParagraph());
        assertTrue(r.getParagraph().contains("6,000"), "crowd rounded like a press estimate: " + r.getParagraph());

        MatchReport es = report(game, m, bundles.get("BaseBundle_sp_sp"));
        assertTrue(es.getParagraph().contains("Ross (min. 10)"), es.getParagraph());
        assertTrue(es.getParagraph().contains("6.000"), es.getParagraph());
    }

    @Test
    void anOldMatchWithoutMinutesStillGetsAReport() {
        Match m = match(Match.LEAGUE_MATCH, preston, burnley, mins(0, 0), mins(0), false);
        m.getHomeScorerIds().set(1, m.getHomeScorerIds().get(0)); // Ross twice
        for (Properties bundle : bundles.values()) {
            MatchReport r = report(game, m, bundle);
            assertFalse(r.isMinutesKnown());
            assertEquals(-1, r.getHalfTimeHome());
            assertEquals(-1, r.getHalfTimeAway());
            assertEquals(3, r.getGoals().size());
            assertTrue(r.getParagraph().contains("Ross (2)"), r.getParagraph());
            assertTrue(r.getParagraph().contains("Brady"), r.getParagraph());
            assertFalse(r.getParagraph().contains("min."), r.getParagraph());
        }
    }

    @Test
    void aGoallessGameHasNoScorers() {
        Match m = league(mins(), mins());
        for (Properties bundle : bundles.values()) {
            MatchReport r = report(game, m, bundle);
            assertTrue(r.getGoals().isEmpty());
            assertEquals(0, r.getHalfTimeHome());
            for (Club club : List.of(preston, burnley)) {
                for (Player p : club.getPlayers()) {
                    assertFalse(r.getParagraph().contains(p.getPerson().getLastname()), r.getParagraph());
                }
            }
        }
    }

    @Test
    void theSameMatchReadsTheSameAndASeasonDoesNotRepeat() {
        Properties english = bundles.get("BaseBundle_en_uk");
        RandomSource minutes = RandomSource.seeded(3);
        Set<String> headlines = new HashSet<>();
        Set<String> paragraphs = new HashSet<>();
        int matches = 132;
        for (int i = 0; i < matches; i++) {
            Match m = league(new int[i % 4], new int[(i / 4) % 3]);
            GoalMinutes.assign(m, minutes.fork("minutes:" + i));
            MatchReport a = report(game, m, english);
            MatchReport b = report(game, m, english);
            assertEquals(a.getHeadline(), b.getHeadline());
            assertEquals(a.getParagraph(), b.getParagraph());
            headlines.add(a.getHeadline());
            paragraphs.add(a.getParagraph());
        }
        assertTrue(headlines.size() >= 20, "distinct headlines in a season: " + headlines.size());
        // goalless games have the fewest sentences to vary; all the others differ
        assertTrue(paragraphs.size() >= matches - 4, "distinct reports in a season: " + paragraphs.size());
    }

    @Test
    void goalsAreDisputedOnlyBeforeNets() {
        Properties english = bundles.get("BaseBundle_en_uk");
        int disputed1888 = 0;
        int disputed1895 = 0;
        for (int i = 0; i < 300; i++) {
            Match m = league(mins(20), mins(70));
            MatchReport r = report(game, m, english);
            disputed1888 += r.hasDisputedGoal() ? 1 : 0;
            m.setMatchDateTime(LocalDateTime.of(1895, 10, 5, 15, 0));
            disputed1895 += report(game, m, english).hasDisputedGoal() ? 1 : 0;
        }
        assertTrue(disputed1888 > 10 && disputed1888 < 75, "disputed goals in 300 matches of 1888: " + disputed1888);
        assertEquals(0, disputed1895, "no disputed goals once nets were in use");
    }

    @Test
    void cupTiesCloseWithTheCupOrTheReplay() {
        Properties english = bundles.get("BaseBundle_en_uk");
        Match tie1 = match(Match.CUP_MATCH, preston, burnley, mins(30, 60), mins(75), true);
        tie1.setBracketPath("R1M1");
        Club bolton = club("Bolton Wanderers", "Pikes Lane", "Davenport");
        Club everton = club("Everton", "Anfield", "Farmer");
        Match tie2 = match(Match.CUP_MATCH, bolton, everton, mins(5), mins(50), true);
        tie2.setBracketPath("R1M2");
        Match finalTie = match(Match.CUP_MATCH, preston, bolton, mins(10, 20, 30), mins(), true);
        finalTie.setRound(2);
        finalTie.setBracketPath("R2M1");
        finalTie.setParentMatch1Id(tie1.getId());
        finalTie.setParentMatch2Id(tie2.getId());

        for (long seed = 1; seed <= 10; seed++) {
            game.setSeed(seed);
            String semi = report(game, tie1, english).getParagraph();
            assertTrue(semi.contains("semi-final"), semi);
            assertTrue(semi.contains("round") || semi.contains("competition"), semi);
            String drawn = report(game, tie2, english).getParagraph();
            assertTrue(drawn.contains("replayed") || drawn.contains("meet again"), drawn);
            String won = report(game, finalTie, english).getParagraph();
            assertTrue(won.contains("the final") && won.contains("the Cup"), won);
        }
    }
}
