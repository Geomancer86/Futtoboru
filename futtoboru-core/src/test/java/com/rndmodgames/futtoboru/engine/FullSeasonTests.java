package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.cup.NeutralVenues;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * P2-3: plays the whole 1888-89 season headless from a new game with the real data, the way the UI does
 * (Continue, Match Result on the manager's match days, read every mandatory draw message), until the
 * Football League and the FA Cup are complete and the next season has rolled over. Then checks the
 * invariants of the final table, the cup and the rollover, and that the same seed plays the same season.
 */
@Tag("slow")
@Tag("season")
class FullSeasonTests extends BaseTest {

    static final long SEED = 1888L;
    /**
     * The game starts on 1 Apr 1888 and the generated league schedule follows the era calendar (P2-11): 8 Sep 1888
     * to 20 Apr 1889, about 385 days to the rollover; twice that and more means the game is stuck or loops.
     */
    static final int DAY_BUDGET = 800;
    static final String STARTING_CLUB = "preston-north-end";
    static final String NL = System.lineSeparator();

    static SeasonRun first;
    static SeasonRun second;

    @BeforeAll
    static void playTwice() {
        first = SeasonRun.play(SEED);
        second = SeasonRun.play(SEED);
    }

    @AfterAll
    static void restore() {
        // the runs rebuilt the shared database and mutated its clubs; let the next class load a fresh one
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void theSeasonFinishesWithinTheDayBudgetAndWithoutErrors() {
        System.out.println("P2-3 season run: " + first.days + " days, " + first.millis + " ms (second run "
                + second.millis + " ms)");
        Map<String, Integer> distinct = new LinkedHashMap<>();
        for (String e : first.errors) {
            distinct.merge(e, 1, Integer::sum);
        }
        StringBuilder report = new StringBuilder();
        distinct.forEach((e, n) -> report.append(n).append(" x ").append(e).append(NL));
        assertTrue(first.errors.isEmpty(), "errors logged during the season:" + NL + report + first.editionsReport());
        assertTrue(first.days < DAY_BUDGET, "days " + first.days);
    }

    @Test
    void everyLeagueFixtureIsPlayedExactlyOnce() {
        List<Long> clubIds = first.leagueClubIds;
        assertEquals(12, clubIds.size(), "12 founder members");
        assertEquals(132, first.leagueMatches.size(), "12 clubs, double round robin: 132 matches");

        Set<String> pairings = new HashSet<>();
        Map<Long, Integer> playedPerClub = new HashMap<>();
        for (Match m : first.leagueMatches) {
            assertTrue(Boolean.TRUE.equals(m.getIsPlayed()), "match " + m.getId() + " played");
            assertNotEquals(m.getHomeClubId(), m.getAwayClubId());
            assertTrue(pairings.add(m.getHomeClubId() + "v" + m.getAwayClubId()),
                    "home and away pairing played once: " + m.getHomeClubId() + "v" + m.getAwayClubId());
            playedPerClub.merge(m.getHomeClubId(), 1, Integer::sum);
            playedPerClub.merge(m.getAwayClubId(), 1, Integer::sum);
        }
        for (Long id : clubIds) {
            assertEquals(22, playedPerClub.getOrDefault(id, 0), "league matches of club " + id);
        }
    }

    @Test
    void theFinalTableAddsUpUnderTheLeagueRules() {
        List<LeagueStandingRow> table = first.leagueEdition.getFinalStandings();
        assertEquals(12, table.size(), "final table kept on the edition");
        CompetitionRules rules = first.league.getRulesOrDefault();

        // recompute the table from the played matches
        Map<Long, int[]> computed = new HashMap<>(); // P W D L F A Pts
        for (Long id : first.leagueClubIds) {
            computed.put(id, new int[7]);
        }
        int goalsFor = 0;
        int goalsAgainst = 0;
        for (Match m : first.leagueMatches) {
            int[] h = computed.get(m.getHomeClubId());
            int[] a = computed.get(m.getAwayClubId());
            add(h, m.getHomeGoals(), m.getAwayGoals(), rules);
            add(a, m.getAwayGoals(), m.getHomeGoals(), rules);
        }

        for (int i = 0; i < table.size(); i++) {
            LeagueStandingRow row = table.get(i);
            int[] c = computed.get(row.getClubId());
            assertNotNull(c, "table row for a league club");
            assertEquals(i + 1, row.getPosition());
            assertEquals(22, row.getPlayed());
            assertEquals(row.getPlayed(), row.getWon() + row.getDrawn() + row.getLost(), "W+D+L = P");
            assertEquals(row.getWon() * rules.getPointsForWin() + row.getDrawn() * rules.getPointsForDraw()
                    + row.getLost() * rules.getPointsForLoss(), row.getPoints(), "points from the rules");
            assertEquals(c[1], row.getWon(), "won");
            assertEquals(c[2], row.getDrawn(), "drawn");
            assertEquals(c[3], row.getLost(), "lost");
            assertEquals(c[4], row.getGoalsFor(), "goals for");
            assertEquals(c[5], row.getGoalsAgainst(), "goals against");
            assertEquals(c[6], row.getPoints(), "points");
            goalsFor += row.getGoalsFor();
            goalsAgainst += row.getGoalsAgainst();
            if (i > 0) {
                assertTrue(table.get(i - 1).getPoints() >= row.getPoints(), "table sorted by points");
            }
        }
        assertEquals(goalsFor, goalsAgainst, "goals for = goals against over the league");
        assertEquals(2, rules.getPointsForWin(), "1888-89: two points for a win");
        assertEquals(1, rules.getPointsForDraw(), "1888-89: one point for a draw");
    }

    private static void add(int[] row, int scored, int conceded, CompetitionRules rules) {
        row[0]++;
        row[4] += scored;
        row[5] += conceded;
        if (scored > conceded) {
            row[1]++;
            row[6] += rules.getPointsForWin();
        } else if (scored == conceded) {
            row[2]++;
            row[6] += rules.getPointsForDraw();
        } else {
            row[3]++;
            row[6] += rules.getPointsForLoss();
        }
    }

    @Test
    void thereIsAChampionACupWinnerAndARunnerUp() {
        CompetitionEdition league = first.leagueEdition;
        assertNotNull(league.getChampionsId(), "league champion");
        assertNotNull(league.getRunnersUpId(), "league runner-up");
        assertEquals(league.getFinalStandings().get(0).getClubId(), league.getChampionsId());
        assertEquals(league.getFinalStandings().get(1).getClubId(), league.getRunnersUpId());

        CompetitionEdition cup = first.cupEdition;
        assertNotNull(cup.getChampionsId(), "FA Cup winner");
        assertNotNull(cup.getRunnersUpId(), "FA Cup runner-up");
        assertNotEquals(cup.getChampionsId(), cup.getRunnersUpId());
        assertTrue(cup.getParticipantClubsIds().contains(cup.getChampionsId()), "winner entered the cup");
        assertTrue(cup.getParticipantClubsIds().contains(cup.getRunnersUpId()), "runner-up entered the cup");
    }

    @Test
    void everyCupEntrantIsKnockedOutOnceExceptTheWinner() {
        CompetitionEdition cup = first.cupEdition;
        int entrants = cup.getParticipantClubsIds().size();
        assertEquals(32, entrants, "1888-89 FA Cup proper: 32 clubs");
        // every decided tie (a won match: the first match or its replay) knocks one club out
        Map<Long, Integer> losses = new HashMap<>();
        for (Match m : first.cupMatches) {
            if (!Boolean.TRUE.equals(m.getIsPlayed())) {
                fail("unplayed cup tie left: " + m.getBracketPath());
            }
            if (m.getHomeGoals().intValue() != m.getAwayGoals().intValue()) {
                Long loser = m.getHomeGoals() > m.getAwayGoals() ? m.getAwayClubId() : m.getHomeClubId();
                losses.merge(loser, 1, Integer::sum);
            }
        }
        for (Map.Entry<Long, Integer> e : losses.entrySet()) {
            assertEquals(1, e.getValue(), "club " + e.getKey() + " knocked out once");
        }
        assertTrue(!losses.containsKey(cup.getChampionsId()), "the winner never lost");
        // a twice-drawn replay is decided without a score; all other entrants lost exactly one match
        assertTrue(losses.size() >= entrants - 1 - first.drawnReplays, "losers " + losses.size());
    }

    @Test
    void prizesAndCompletionMessagesComeOnce() {
        assertEquals(1, first.messages("LEAGUE_CHAMPION"), "one champion announcement");
        assertEquals(1, first.messages("LEAGUE_COMPLETE"), "one season-complete message");
        assertEquals(1, first.messages("CUP_COMPLETE"), "one cup-complete message");
        assertEquals(first.balancesBeforeExtraChecks, first.balancesAfterExtraChecks,
                "checking the competitions again pays no prize twice");
    }

    @Test
    void theGoldenBootGoesToThisSeasonsTopLeagueScorer() {
        Map<Long, Integer> goals = new HashMap<>();
        for (Match m : first.leagueMatches) {
            for (Long id : m.getHomeScorerIds()) {
                goals.merge(id, 1, Integer::sum);
            }
            for (Long id : m.getAwayScorerIds()) {
                goals.merge(id, 1, Integer::sum);
            }
        }
        int most = 0;
        int scored = 0;
        for (int g : goals.values()) {
            most = Math.max(most, g);
            scored += g;
        }
        int leagueGoals = 0;
        for (LeagueStandingRow r : first.leagueEdition.getFinalStandings()) {
            leagueGoals += r.getGoalsFor();
        }
        assertEquals(leagueGoals, scored, "every league goal has a scorer");
        assertEquals(1, first.messages("LEAGUE_TOP_SCORER"), "one Golden Boot");
        Message boot = first.message("LEAGUE_TOP_SCORER");
        assertTrue(boot.getPlainTextMessage().contains(" with " + most + " goals"), boot.getPlainTextMessage());
    }

    @Test
    void oneEditionPerCompetitionAndSeason() {
        for (Competition c : first.save.getAllCups()) {
            int editions = 0;
            for (CompetitionEdition e : c.getEditions()) {
                editions += e.getName() != null && e.getName().contains("1888-89") ? 1 : 0;
            }
            assertEquals(1, editions, c.getName() + " has one 1888-89 edition");
        }
        // P2-9: the league too; the season's edition is the one from season.json, the next season comes once
        for (String season : new String[] { "1888-89", "1889-90" }) {
            int editions = 0;
            for (CompetitionEdition e : first.league.getEditions()) {
                editions += e.getName() != null && e.getName().contains(season) ? 1 : 0;
            }
            assertEquals(1, editions, first.league.getName() + " has one " + season + " edition");
        }
        assertEquals("football-league-1888-89", first.leagueEdition.getDataId());
        for (Competition c : first.save.getAllLeagues()) {
            if (Competition.COMPETITION_LEAGUE.equals(c.getCompetitionType())) {
                int editions = 0;
                for (CompetitionEdition e : c.getEditions()) {
                    editions += e.getName() != null && e.getName().contains("1888-89") ? 1 : 0;
                }
                assertEquals(1, editions, c.getName() + " has one 1888-89 edition");
            }
        }
    }

    @Test
    void theNextSeasonRolledOverWithAFreshTable() {
        assertTrue(first.league.getEditions().size() >= 2, "the next season was generated");
        CompetitionEdition next = first.league.getEditions().get(first.league.getEditions().indexOf(first.leagueEdition) + 1);
        assertTrue(next.getName().contains("1889-90"), next.getName());
        assertTrue(next.getFinalStandings().isEmpty(), "the new season has no final table");
        assertEquals(12, first.leagueEdition.getFinalStandings().size(), "the old final table is kept");
        for (Club club : first.league.getLeagueClubs()) {
            assertEquals(0, club.getMatchesPlayed(), club.getName() + " played in the new table");
            assertEquals(0, club.getPoints(), club.getName() + " points in the new table");
            assertEquals(0, club.getGoalsScored() + club.getGoalsConceded(), club.getName() + " goals");
        }
    }

    /**
     * P2-11: the generated schedule follows the era calendar. League on Saturdays from September to April; FA Cup
     * ties on winter Saturdays (proper rounds February-March, final in late March), replays the following Saturday
     * or midweek; no club plays twice on one day.
     */
    @Test
    void theScheduleFollowsTheEraCalendar() {
        LocalDate leagueFirst = null;
        LocalDate leagueLast = null;
        for (Match m : first.leagueMatches) {
            LocalDate d = m.getMatchDateTime().toLocalDate();
            leagueFirst = leagueFirst == null || d.isBefore(leagueFirst) ? d : leagueFirst;
            leagueLast = leagueLast == null || d.isAfter(leagueLast) ? d : leagueLast;
            assertEquals(DayOfWeek.SATURDAY, d.getDayOfWeek(), "league match " + m.getId() + " on " + d);
            assertTrue(!d.isBefore(LocalDate.of(1888, 9, 1)) && !d.isAfter(LocalDate.of(1889, 4, 30)),
                    "league match " + m.getId() + " within September 1888 - April 1889: " + d);
        }
        LocalDate cupFirst = null;
        LocalDate cupLast = null;
        LocalDate finalDay = null;
        Map<String, LocalDate> tieDays = new HashMap<>();
        for (Match m : first.cupMatches) {
            if (!m.getBracketPath().endsWith("R")) {
                tieDays.put(m.getBracketPath(), m.getMatchDateTime().toLocalDate());
            }
        }
        for (Match m : first.cupMatches) {
            LocalDate d = m.getMatchDateTime().toLocalDate();
            cupFirst = cupFirst == null || d.isBefore(cupFirst) ? d : cupFirst;
            cupLast = cupLast == null || d.isAfter(cupLast) ? d : cupLast;
            String path = m.getBracketPath();
            if (path.endsWith("R")) {
                LocalDate tie = tieDays.get(path.substring(0, path.length() - 1));
                assertNotNull(tie, "replay " + path + " of a tie");
                long after = java.time.temporal.ChronoUnit.DAYS.between(tie, d);
                assertTrue(after >= 2 && after <= 7, "replay " + path + " in the week after the tie: " + tie + " -> " + d);
                assertNotEquals(DayOfWeek.SUNDAY, d.getDayOfWeek(), "no football on Sunday: " + path);
            } else {
                assertEquals(DayOfWeek.SATURDAY, d.getDayOfWeek(), "cup tie " + path + " on " + d);
                Month month = d.getMonth();
                assertTrue(month == Month.FEBRUARY || month == Month.MARCH, "32-club proper round in Feb-Mar: " + path + " " + d);
                finalDay = finalDay == null || d.isAfter(finalDay) ? d : finalDay; // the last tie is the final
            }
        }
        assertEquals(SeasonCalendar.forSeason(1888).cupFinal(), finalDay, "the final on the last Saturday of March");
        assertEquals(Month.MARCH, finalDay.getMonth());

        // no club plays twice on one day (league, cup, replays, friendlies)
        Map<String, Set<Long>> byClubAndDay = new HashMap<>();
        Set<String> seen = new HashSet<>();
        for (Club c : first.save.getAllClubs()) {
            for (Match m : c.getPlayedMatches()) {
                if (m.getMatchDateTime() == null || !m.getMatchDateTime().isBefore(LocalDateTime.of(1889, 7, 1, 0, 0))
                        || !seen.add(String.valueOf(m.getId()))) {
                    continue;
                }
                for (Long club : new Long[] { m.getHomeClubId(), m.getAwayClubId() }) {
                    Set<Long> ids = byClubAndDay.computeIfAbsent(club + "@" + m.getMatchDateTime().toLocalDate(),
                            k -> new HashSet<>());
                    ids.add(m.getId());
                    assertEquals(1, ids.size(), "club " + club + " plays twice on " + m.getMatchDateTime().toLocalDate()
                            + ": matches " + ids);
                }
            }
        }
        Log.info(Log.ENGINE_SCHEDULING, "FullSeasonTests", "P2-11 calendar: league " + leagueFirst + " to " + leagueLast
                + " (" + first.leagueMatches.size() + " matches), FA Cup " + cupFirst + " to " + cupLast + " ("
                + first.cupMatches.size() + " ties incl. replays), final " + finalDay);
        System.out.println("P2-11 calendar: league " + leagueFirst + " to " + leagueLast + ", FA Cup " + cupFirst
                + " to " + cupLast + ", final " + finalDay + ", " + first.cupMatches.size() + " cup matches");
    }

    /**
     * P2-14 (D30 A9): the FA Cup opens on the calendar's day in October (the qualifying rounds), its first round is
     * drawn in January and each later round a few days after the round before is decided, before its own date. The
     * semi-finals are played at one of the season's neutral grounds each (never a semi-finalist's own) and the
     * final at Kennington Oval; a replay stays at its tie's ground; earlier rounds at the home club's ground.
     */
    @Test
    void theFaCupOpensInOctoberIsDrawnRoundByRoundAndEndsAtNeutralGrounds() {
        assertEquals(LocalDate.of(1888, 10, 6), first.cupOpenedOn, "opened with the qualifying rounds");
        assertEquals(LocalDate.of(1888, 10, 6), first.cupEdition.getStartDate().toLocalDate());
        assertEquals(LocalDate.of(1889, 1, 7), first.roundDrawnOn.get(1), "first round drawn in January");

        Map<Integer, LocalDate> roundDay = new HashMap<>();
        int lastRound = 0;
        for (Match m : first.cupMatches) {
            if (!m.getBracketPath().endsWith("R")) {
                roundDay.put(m.getRound(), m.getMatchDateTime().toLocalDate());
                lastRound = Math.max(lastRound, m.getRound());
            }
        }
        assertEquals(5, lastRound, "32 clubs: five rounds");
        for (int r = 2; r <= lastRound; r++) {
            LocalDate drawn = first.roundDrawnOn.get(r);
            assertNotNull(drawn, "round " + r + " drawn");
            assertTrue(!drawn.isBefore(roundDay.get(r - 1).plusDays(2)), "round " + r + " drawn on " + drawn
                    + ", two days after round " + (r - 1) + " (" + roundDay.get(r - 1) + ") at the earliest");
            assertTrue(drawn.isBefore(roundDay.get(r)), "round " + r + " drawn on " + drawn + " before its date "
                    + roundDay.get(r));
        }
        assertEquals(Integer.valueOf(lastRound), first.cupEdition.getDrawnRounds());

        List<String> semiGrounds = List.of("bramall-lane", "alexandra-recreation-ground");
        Set<String> semisAt = new HashSet<>();
        Map<String, String> tieVenue = new HashMap<>();
        for (Match m : first.cupMatches) {
            if (!m.getBracketPath().endsWith("R")) {
                tieVenue.put(m.getBracketPath(), m.getVenue());
            }
        }
        for (Match m : first.cupMatches) {
            String path = m.getBracketPath();
            boolean replay = path.endsWith("R");
            int round = m.getRound();
            if (round == lastRound) {
                assertEquals("kennington-oval", m.getVenue(), "the final " + path + " at the Oval");
            } else if (round == lastRound - 1) {
                assertTrue(semiGrounds.contains(m.getVenue()), "semi-final " + path + " at " + m.getVenue());
                for (Long club : new Long[] { m.getHomeClubId(), m.getAwayClubId() }) {
                    Club c = first.save.getClubById(club);
                    assertNotEquals(m.getVenue(), c.getStadium() != null ? c.getStadium().getDataId() : null,
                            "never a semi-finalist's own ground: " + c.getName());
                }
                if (!replay) {
                    semisAt.add(m.getVenue());
                }
            } else {
                assertNull(m.getVenue(), path + " at the home club's ground");
            }
            if (replay) {
                assertEquals(tieVenue.get(path.substring(0, path.length() - 1)), m.getVenue(),
                        "replay " + path + " at its tie's ground");
            }
        }
        assertEquals(2, semisAt.size(), "the two semi-finals at different grounds");
    }

    @Test
    void everySquadCanFieldATwoThreeFiveAtTheStartAndAfterTheRollover() {
        assertTrue(first.save.getAllClubs().size() > 20, "clubs: " + first.save.getAllClubs().size());
        assertEquals(List.of(), first.shortAtStart, "new game");
        assertEquals(List.of(), first.shortAfterRollover, "1889-90 rollover");
    }

    /**
     * P2-12: only the home club sells tickets for a league or cup match and books its gate, once; the away club's
     * finances never see it. Every played 1888-89 league match and FA Cup tie has exactly one gate entry, the home
     * club's, holding the match's whole revenue, and each club's gate total is the sum over its home matches.
     */
    @Test
    void onlyTheHomeClubBooksTheGateOfLeagueAndCupMatches() {
        Map<Long, List<Long>> gateClubsByMatch = new HashMap<>();
        // the game lists the clubs of the selected countries twice (as NewGameOverviewScreen does): count each once
        Set<Club> clubs = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        clubs.addAll(first.save.getAllClubs());
        Map<Long, BigDecimal> gateByMatch = new HashMap<>();
        for (Club c : clubs) {
            for (MatchIncome income : c.getMatchIncomes()) {
                gateClubsByMatch.computeIfAbsent(income.getMatchId(), k -> new ArrayList<>()).add(c.getId());
                gateByMatch.merge(income.getMatchId(), income.getTicketRevenue(), BigDecimal::add);
            }
        }
        List<Match> played = new ArrayList<>(first.leagueMatches);
        played.addAll(first.cupMatches);
        assertTrue(played.size() > 132, "league + cup matches: " + played.size());
        Map<Long, BigDecimal> homeRevenueByClub = new HashMap<>();
        int neutral = 0;
        for (Match m : played) {
            // P2-14: at a neutral ground (1888-89 semi-finals and final) the Football Association takes the gate:
            // no club's entry; everywhere else the home club's, once
            List<Long> expected = new ArrayList<>(NeutralVenues.gateClubs(m, first.save));
            List<Long> actual = new ArrayList<>(gateClubsByMatch.getOrDefault(m.getId(), List.of()));
            java.util.Collections.sort(expected);
            java.util.Collections.sort(actual);
            assertEquals(expected, actual, "gate entries of match " + m.getId() + " (" + m.getBracketPath()
                    + ", home " + m.getHomeClubId() + ", away " + m.getAwayClubId() + ", venue " + m.getVenue() + ")");
            assertTrue(m.getMatchRevenue().signum() > 0, "gate of match " + m.getId());
            if (NeutralVenues.isNeutral(m)) {
                neutral++;
                assertEquals(List.of(), expected, "1888-89: the Association's gate at " + m.getVenue());
                continue;
            }
            assertEquals(0, m.getMatchRevenue().compareTo(gateByMatch.get(m.getId())), "gate of match " + m.getId());
            homeRevenueByClub.merge(m.getHomeClubId(), m.getMatchRevenue(), BigDecimal::add);
        }
        assertTrue(neutral >= 3, "two semi-finals and the final at neutral grounds: " + neutral);
        Set<Long> playedIds = new HashSet<>();
        for (Match m : played) {
            playedIds.add(m.getId());
        }
        for (Club c : clubs) {
            BigDecimal booked = BigDecimal.ZERO;
            for (MatchIncome income : c.getMatchIncomes()) {
                if (playedIds.contains(income.getMatchId())) {
                    booked = booked.add(income.getTicketRevenue());
                }
            }
            assertEquals(0, homeRevenueByClub.getOrDefault(c.getId(), BigDecimal.ZERO).compareTo(booked),
                    c.getName() + ": gate total = sum over its home matches");
        }
    }

    @Test
    void theSameSeedPlaysTheSameSeason() {
        assertEquals(first.tableFingerprint(), second.tableFingerprint(), "same final table");
        assertEquals(first.cupEdition.getChampionsId(), second.cupEdition.getChampionsId(), "same cup winner");
        assertEquals(first.cupEdition.getRunnersUpId(), second.cupEdition.getRunnersUpId(), "same cup runner-up");
        assertEquals(first.days, second.days, "same number of days");
    }

    /**
     * One headless game from the new-game screen to the next season, as the UI plays it.
     */
    static final class SeasonRun {
        Futtoboru game;
        SaveGame save;
        FuttoboruGameEngine engine;
        League league;
        CompetitionEdition leagueEdition;
        Competition cup;
        CompetitionEdition cupEdition;
        List<Long> leagueClubIds = new ArrayList<>();
        List<Match> leagueMatches = new ArrayList<>();
        List<Match> cupMatches = new ArrayList<>();
        int drawnReplays;
        int drawsRead;
        int days;
        long millis;
        List<String> errors = new ArrayList<>();
        Map<Long, BigDecimal> balancesBeforeExtraChecks;
        Map<Long, BigDecimal> balancesAfterExtraChecks;
        /** E-01b: clubs of the game that cannot field a 2-3-5, at the new game and after the rollover. */
        List<String> shortAtStart;
        List<String> shortAfterRollover;
        /** P2-14: the day the 1888-89 FA Cup opened, and the day each of its rounds was drawn */
        LocalDate cupOpenedOn;
        Map<Integer, LocalDate> roundDrawnOn = new HashMap<>();

        static SeasonRun play(long seed) {
            SeasonRun run = new SeasonRun();
            // Every error in main code goes through Log (LoggingStyleGuardTest keeps it that way).
            Log.ErrorListener recorder = (category, tag, message, error) ->
                    run.errors.add(category + (tag != null ? "/" + tag : "") + ": " + message
                            + (error != null ? " -> " + error : ""));
            Log.addErrorListener(recorder);
            long start = System.currentTimeMillis();
            try {
                run.newGame(seed);
                run.shortAtStart = run.shortSquads();
                run.playUntilRolledOver();
                run.shortAfterRollover = run.shortSquads();
                run.collect();
            } finally {
                Log.removeErrorListener(recorder);
            }
            run.millis = System.currentTimeMillis() - start;
            return run;
        }

        /** What NewGameOverviewScreen's start button does, for a manager of Preston North End. */
        void newGame(long seed) {
            DatabaseLoader.load(GameData.loadDefault(), RandomSource.seeded(seed));
            game = new Futtoboru(seed);
            save = new SaveGame();
            game.setCurrentGame(save);
            ScriptsManager scripts = new ScriptsManager(game);
            AuthorityManager authority = new AuthorityManager(game);
            engine = new FuttoboruGameEngine(game, scripts, authority);
            game.setScriptsManager(scripts);
            game.setGameEngine(engine);

            Season season = DatabaseLoader.getSeasonByDataId("1888-89");
            Profession manager = DatabaseLoader.getProfessionByDataId(Profession.MANAGER);
            Club startingClub = DatabaseLoader.getClubByDataId(STARTING_CLUB);
            assertNotNull(season);
            assertNotNull(manager);
            assertNotNull(startingClub);

            Person owner = new Person();
            owner.setName("Test");
            owner.setLastname("Manager");
            owner.setBirthDate(LocalDateTime.of(1850, 1, 1, 0, 0));
            save.setOwner(owner);
            save.setMainAuthority(DatabaseLoader.getMainAuthority());
            save.setSelectedCountries(new ArrayList<>(season.getCountries()));
            save.assignId(owner);
            save.getAllPersons().add(owner);
            owner.setPrimaryProfession(manager);
            owner.setCurrentCountry(season.getCountries().get(0));
            save.setGameStartDate(season.getStartDate());
            save.setGameDate(season.getStartDate());

            save.setAllClubs(new ArrayList<>());
            for (Competition comp : DatabaseLoader.getCompetitions()) {
                if (Competition.COMPETITION_CUP.equals(comp.getCompetitionType())) {
                    save.getAllCups().add(comp);
                } else if (Competition.COMPETITION_LEAGUE.equals(comp.getCompetitionType())) {
                    save.getAllLeagues().add(comp);
                }
                for (CompetitionEdition edition : comp.getEditions()) {
                    for (Long clubId : edition.getParticipantClubsIds()) {
                        if (save.getClubById(clubId) == null && DatabaseLoader.getClubById(clubId) != null) {
                            save.getAllClubs().add(DatabaseLoader.getClubById(clubId));
                        }
                    }
                }
            }
            for (com.rndmodgames.futtoboru.data.Country country : season.getCountries()) {
                List<Club> clubs = DatabaseLoader.getClubsByCountry().get(country.getId());
                if (clubs != null) {
                    save.getAllClubs().addAll(clubs); // as the screen does (duplicates included)
                }
            }
            owner.setCurrentClubId(startingClub.getId());
            save.getGameScripts().addAll(season.getSeasonScripts());

            game.initializeJobSystem();
            game.getJobManager().initializeJobOpenings();
            com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator attributes =
                    new com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator();
            for (Club club : save.getAllClubs()) {
                for (com.rndmodgames.futtoboru.data.Player player : club.getPlayers()) {
                    if (player != null && player.getPerson() != null && player.getAcceleration() == null) {
                        attributes.generatePlayerAttributes(player, player.getPerson(), save.getGameDate(),
                                game.random().fork("attributes:" + player.getPerson().getId()));
                    }
                }
            }
            engine.getMessageManager().deliverMessage(engine.getMessageManager().createWelcomeMessage(owner, manager,
                    owner.getCurrentCountry(), startingClub, save.getGameStartDate()));
        }

        List<String> shortSquads() {
            List<String> out = new ArrayList<>();
            for (Club club : save.getAllClubs()) {
                if (!com.rndmodgames.futtoboru.system.generators.PositionAssigner.canFieldTwoThreeFive(
                        club.getPlayers())) {
                    out.add(club.getDataId() + " (" + club.getPlayers().size() + " players)");
                }
            }
            return out;
        }

        void playUntilRolledOver() {
            LocalDateTime start = save.getGameDate();
            // a step is one button press; a stuck game (a draw that never clears) also runs out of steps
            for (int step = 0; step < 2 * DAY_BUDGET; step++) {
                days = (int) java.time.temporal.ChronoUnit.DAYS.between(start, save.getGameDate());
                if (days > DAY_BUDGET) {
                    break;
                }
                switch (engine.getNextGameAction()) {
                case FuttoboruGameEngine.DRAW_ACTION:
                    readDrawMessages();
                    break;
                case FuttoboruGameEngine.MATCH_PREVIEW_ACTION:
                    engine.getMatchResult(); // the Match Result button, then Continue
                    engine.continueGame();
                    break;
                default:
                    engine.continueGame();
                    break;
                }
                // a draw message delivered today: the player opens it before pressing Continue again
                if (engine.getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
                    readDrawMessages();
                }
                watchCup();
                if (isRolledOver()) {
                    days = (int) java.time.temporal.ChronoUnit.DAYS.between(start, save.getGameDate());
                    return;
                }
            }
            fail("the season did not finish within " + DAY_BUDGET + " days (now " + save.getGameDate() + "): league "
                    + (leagueEdition() != null ? leagueEdition().getFinalStandings().size() + " rows" : "not created")
                    + ", cup winner " + (cupEdition() != null ? cupEdition().getChampionsId() : "no edition")
                    + System.lineSeparator() + diagnostics());
        }

        /** P2-14: notes the day the FA Cup opens and the day each round first has a tie with both clubs (its draw) */
        void watchCup() {
            CompetitionEdition e = cupEdition();
            if (e == null) {
                return;
            }
            LocalDate today = save.getGameDate().toLocalDate();
            if (cupOpenedOn == null && e.getStartDate() != null) {
                cupOpenedOn = today;
            }
            for (Club c : save.getAllClubs()) {
                for (Match m : c.getScheduledMatches()) {
                    if (e.getId().equals(m.getCompetitionEditionId()) && m.getRound() != null
                            && m.getHomeClubId() != null && m.getAwayClubId() != null
                            && (m.getBracketPath() == null || !m.getBracketPath().endsWith("R"))) {
                        roundDrawnOn.putIfAbsent(m.getRound(), today);
                    }
                }
            }
        }

        /**
         * The INBOX -> DRAW button (MainGameMenuTable) and the draw screen's Continue button: a mandatory draw
         * that is only scheduled is delivered at once, then the draw is marked read and no longer mandatory.
         */
        void readDrawMessages() {
            if (save.getScheduledMessages() != null) {
                for (Message m : new ArrayList<>(save.getScheduledMessages())) {
                    if (m != null && Boolean.TRUE.equals(m.getIsMandatory()) && isDraw(m)) {
                        engine.getMessageManager().deliverMessage(m);
                    }
                }
            }
            for (Message m : save.getAllMessages()) {
                if (m != null && Boolean.TRUE.equals(m.getIsMandatory()) && !Boolean.TRUE.equals(m.getIsRead())
                        && isDraw(m)) {
                    m.setIsRead(true);
                    m.setIsMandatory(false);
                    drawsRead++;
                }
            }
        }

        String editionsReport() {
            StringBuilder sb = new StringBuilder("cup editions:" + NL);
            for (Competition c : save.getAllCups()) {
                for (CompetitionEdition e : c.getEditions()) {
                    sb.append(e.getId()).append(' ').append(e.getName()).append(" champ ").append(e.getChampionsId())
                            .append(" start ").append(e.getStartDate()).append(" n ")
                            .append(e.getParticipantClubsIds().size()).append(NL);
                }
            }
            return sb.toString();
        }

        String diagnostics() {
            StringBuilder sb = new StringBuilder("next action " + engine.getNextGameAction() + NL);
            League l = league();
            if (l != null) {
                for (CompetitionEdition e : l.getEditions()) {
                    sb.append("edition ").append(e.getName()).append(' ').append(e.getStartDate()).append(" rows ")
                            .append(e.getFinalStandings().size()).append(NL);
                }
                for (Club c : l.getLeagueClubs()) {
                    int scheduledLeague = 0;
                    LocalDateTime nextDate = null;
                    for (Match m : c.getScheduledMatches()) {
                        if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH) {
                            scheduledLeague++;
                            if (nextDate == null || m.getMatchDateTime().isBefore(nextDate)) {
                                nextDate = m.getMatchDateTime();
                            }
                        }
                    }
                    sb.append(c.getName()).append(" P").append(c.getMatchesPlayed()).append(" scheduled league ")
                            .append(scheduledLeague).append(" next ").append(nextDate).append(NL);
                }
            } else {
                sb.append("no 12-club league; leagues: ").append(save.getMainAuthority().getLeagues().size()).append(NL);
            }
            for (Message m : save.getAllMessages()) {
                if (Boolean.TRUE.equals(m.getIsMandatory())) {
                    sb.append("mandatory ").append(m.getMessageType()).append(" read=").append(m.getIsRead())
                            .append(' ').append(m.getTitle()).append(NL);
                }
            }
            for (Message m : save.getScheduledMessages()) {
                sb.append("scheduled ").append(m.getMessageType()).append(" mand=").append(m.getIsMandatory())
                        .append(' ').append(m.getScheduledDate()).append(' ').append(m.getTitle()).append(NL);
            }
            return sb.toString();
        }

        static boolean isDraw(Message m) {
            return "LEAGUE_DRAW".equals(m.getMessageType()) || "CUP_DRAW".equals(m.getMessageType())
                    || "FIXTURE_DRAW".equals(m.getMessageType());
        }

        League league() {
            if (save.getMainAuthority() == null) {
                return null;
            }
            for (League l : save.getMainAuthority().getLeagues()) {
                if (l != null && l.getLeagueClubs() != null && l.getLeagueClubs().size() == 12) {
                    return l;
                }
            }
            return null;
        }

        CompetitionEdition leagueEdition() {
            League l = league();
            if (l == null) {
                return null;
            }
            for (CompetitionEdition e : l.getEditions()) {
                if (e.getName() != null && e.getName().contains("1888-89")) {
                    return e;
                }
            }
            return null;
        }

        CompetitionEdition cupEdition() {
            for (Competition c : save.getAllCups()) {
                if ("fa-cup".equals(c.getDataId())) {
                    for (CompetitionEdition e : c.getEditions()) {
                        if ("fa-cup-1888-89".equals(e.getDataId())) {
                            cup = c;
                            return e;
                        }
                    }
                }
            }
            return null;
        }

        boolean isRolledOver() {
            CompetitionEdition le = leagueEdition();
            CompetitionEdition ce = cupEdition();
            return le != null && !le.getFinalStandings().isEmpty() && ce != null && ce.getChampionsId() != null
                    && league().getEditions().size() > league().getEditions().indexOf(le) + 1;
        }

        void collect() {
            league = league();
            leagueEdition = leagueEdition();
            cupEdition = cupEdition();
            for (Club c : league.getLeagueClubs()) {
                leagueClubIds.add(c.getId());
            }
            Map<Long, Match> league188889 = new LinkedHashMap<>();
            Map<Long, Match> cupTies = new LinkedHashMap<>();
            for (Club c : save.getAllClubs()) {
                for (Match m : c.getPlayedMatches()) {
                    if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH
                            && m.getMatchDateTime().isBefore(LocalDateTime.of(1889, 7, 1, 0, 0))) {
                        league188889.put(m.getId(), m);
                    }
                    if (m.getMatchType() != null && m.getMatchType() == Match.CUP_MATCH
                            && cupEdition.getId().equals(m.getCompetitionEditionId())) {
                        cupTies.put(m.getId(), m);
                    }
                }
                for (Match m : c.getScheduledMatches()) {
                    if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH
                            && m.getMatchDateTime() != null
                            && m.getMatchDateTime().isBefore(LocalDateTime.of(1889, 7, 1, 0, 0))) {
                        fail("league match of 1888-89 left unplayed: " + m.getId() + " on " + m.getMatchDateTime());
                    }
                }
            }
            leagueMatches.addAll(league188889.values());
            cupMatches.addAll(cupTies.values());
            for (Match m : cupMatches) {
                if (m.getBracketPath() != null && m.getBracketPath().endsWith("R")
                        && m.getHomeGoals().intValue() == m.getAwayGoals().intValue()) {
                    drawnReplays++;
                }
            }

            // checking the competitions again on the same day must not pay or announce anything twice
            balancesBeforeExtraChecks = balances();
            engine.getAuthorityManager().checkCompetitionsSchedule();
            engine.getAuthorityManager().checkCompetitionsSchedule();
            balancesAfterExtraChecks = balances();
        }

        Map<Long, BigDecimal> balances() {
            Map<Long, BigDecimal> out = new LinkedHashMap<>();
            for (Club c : save.getAllClubs()) {
                out.put(c.getId(), c.getClubBalance());
            }
            return out;
        }

        Message message(String type) {
            for (Message m : save.getAllMessages()) {
                if (m != null && type.equals(m.getMessageType())) {
                    return m;
                }
            }
            return null;
        }

        int messages(String type) {
            int n = 0;
            for (Message m : save.getAllMessages()) {
                if (m != null && type.equals(m.getMessageType())) {
                    n++;
                }
            }
            return n;
        }

        String tableFingerprint() {
            StringBuilder sb = new StringBuilder();
            for (LeagueStandingRow r : leagueEdition.getFinalStandings()) {
                sb.append(r.getPosition()).append(' ').append(DatabaseLoaderName.of(save, r.getClubId())).append(' ')
                        .append(r.getWon()).append('-').append(r.getDrawn()).append('-').append(r.getLost())
                        .append(' ').append(r.getGoalsFor()).append(':').append(r.getGoalsAgainst()).append(' ')
                        .append(r.getPoints()).append('\n');
            }
            return sb.toString();
        }
    }

    /** Club name by runtime id (ids are equal across two loads of the same data). */
    static final class DatabaseLoaderName {
        static String of(SaveGame save, Long clubId) {
            Club c = save.getClubById(clubId);
            return c != null ? c.getName() : String.valueOf(clubId);
        }
    }
}
