package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.GameFingerprint;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * Nightly soak (D33, INF-01): several seeded games, each played headless through several seasons, with the invariants
 * checked at every season rollover. Not part of any tier: {@code ./mvnw -B verify -Psoak} (Jenkins "Futtoboru Soak",
 * nightly). Knobs: {@code -Dsoak.seeds=16 -Dsoak.seasons=3 -Dsoak.firstSeed=1}. One dynamic test per seed, so the
 * report names every failing seed; {@code target/soak-report.txt} lists them, the first failing seed on top.
 *
 * Invariants per season: every league fixture played once, the table adds up under the league rules, a champion and
 * runner-up, a cup winner and runner-up, finite club money, the save round trip equals the live game (fingerprint) at
 * the rollover, the next season's league edition exists with a fresh table.
 */
@Tag("soak")
class SoakTests extends BaseTest {

    static final int DAY_BUDGET = 800; // as FullSeasonTests: about 385 days to a rollover
    static final BigDecimal MONEY_LIMIT = new BigDecimal("1e12");
    static final List<String> report = new ArrayList<>();

    @TestFactory
    Stream<DynamicTest> seeds() {
        int count = Integer.getInteger("soak.seeds", 16);
        int first = Integer.getInteger("soak.firstSeed", 1);
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long seed = first + i;
            tests.add(DynamicTest.dynamicTest("seed " + seed, () -> {
                long t0 = System.currentTimeMillis();
                try {
                    playSeed(seed, Integer.getInteger("soak.seasons", 3));
                    note("seed " + seed + " OK " + (System.currentTimeMillis() - t0) / 1000 + " s");
                } catch (Throwable e) {
                    note("seed " + seed + " FAILED: " + e);
                    throw e;
                }
            }));
        }
        return tests.stream();
    }

    static synchronized void note(String line) {
        report.add(line);
        System.out.println("SOAK " + line);
    }

    @AfterAll
    static void writeReport() throws IOException {
        DatabaseLoader.resetDatabaseLoaderInstance();
        List<String> lines = new ArrayList<>();
        String firstFail = null;
        for (String l : report) {
            if (firstFail == null && l.contains("FAILED")) {
                firstFail = l;
            }
        }
        lines.add(firstFail == null ? "soak: all seeds green" : "soak: FIRST FAILING " + firstFail);
        lines.addAll(report);
        Path out = Paths.get("target", "soak-report.txt");
        Files.createDirectories(out.getParent());
        Files.write(out, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    static void playSeed(long seed, int seasons) {
        Futtoboru app = new Futtoboru(seed);
        SaveGame game = NewGameFixture.newGameInSession(app); // prepareNewGame: fresh database + engine, the forced seed
        for (int season = 1; season <= seasons; season++) {
            int before = app.getGameEngine().getAuthorityManager().getSeasonRollovers();
            int day = 0;
            while (app.getGameEngine().getAuthorityManager().getSeasonRollovers() == before) {
                assertTrue(++day <= DAY_BUDGET, "seed " + seed + " season " + season + " did not roll over within "
                        + DAY_BUDGET + " days (now " + game.getGameDate() + ")");
                NewGameFixture.play(app, game, 1, true);
            }
            // the next season's edition is generated after the annual meeting (G-08), some weeks after the rollover
            for (int extra = 0; extra < 200 && !nextEditionsExist(game, season - 1); extra++) {
                NewGameFixture.play(app, game, 1, true);
            }
            String where = "seed " + seed + " season " + season + " (" + game.getGameDate() + ")";
            checkSeason(where, game, season - 1);
            checkMoney(where, game);
            checkSaveRoundTrip(where, game);
        }
    }

    static boolean nextEditionsExist(SaveGame game, int index) {
        for (League league : game.getMainAuthority().getLeagues()) {
            if (league.getEditions().size() > index && !league.getEditions().get(index).getFinalStandings().isEmpty()
                    && league.getEditions().size() <= index + 1) {
                return false;
            }
        }
        return true;
    }

    /** @param index which edition of each league/cup just finished (0 = the first season) */
    static void checkSeason(String where, SaveGame game, int index) {
        int leagues = 0;
        for (League league : game.getMainAuthority().getLeagues()) {
            if (league.getEditions().size() <= index) {
                continue;
            }
            CompetitionEdition edition = league.getEditions().get(index);
            if (edition.getFinalStandings().isEmpty()) {
                continue;
            }
            leagues++;
            checkLeague(where + " " + league.getName(), game, league, edition);
            assertTrue(league.getEditions().size() > index + 1, where + ": the next season's edition exists");
            assertTrue(league.getEditions().get(index + 1).getFinalStandings().isEmpty(),
                    where + ": the next season starts with no final table");
        }
        assertTrue(leagues > 0, where + ": a league finished its season");
        int cups = 0;
        for (Competition cup : game.getAllCups()) {
            if (cup.getEditions().size() <= index) {
                continue;
            }
            CompetitionEdition edition = cup.getEditions().get(index);
            assertNotNull(edition.getChampionsId(), where + " " + cup.getName() + ": cup winner");
            assertNotNull(edition.getRunnersUpId(), where + " " + cup.getName() + ": cup runner-up");
            cups++;
        }
        assertTrue(cups > 0, where + ": at least one cup");
    }

    static void checkLeague(String where, SaveGame game, League league, CompetitionEdition edition) {
        List<LeagueStandingRow> table = edition.getFinalStandings();
        int n = table.size();
        CompetitionRules rules = league.getRulesOrDefault();
        assertNotNull(edition.getChampionsId(), where + ": champion");
        assertNotNull(edition.getRunnersUpId(), where + ": runner-up");
        assertEquals(table.get(0).getClubId(), edition.getChampionsId(), where + ": champion tops the table");
        int goalsFor = 0;
        int goalsAgainst = 0;
        Set<Long> clubs = new HashSet<>();
        for (int i = 0; i < n; i++) {
            LeagueStandingRow r = table.get(i);
            assertEquals(2 * (n - 1), r.getPlayed(), where + ": played, club " + r.getClubId());
            assertEquals(r.getPlayed(), r.getWon() + r.getDrawn() + r.getLost(), where + ": W+D+L = P");
            assertEquals(r.getWon() * rules.getPointsForWin() + r.getDrawn() * rules.getPointsForDraw()
                    + r.getLost() * rules.getPointsForLoss(), r.getPoints(), where + ": points from the rules");
            if (i > 0) {
                assertTrue(table.get(i - 1).getPoints() >= r.getPoints(), where + ": table sorted by points");
            }
            goalsFor += r.getGoalsFor();
            goalsAgainst += r.getGoalsAgainst();
            assertTrue(clubs.add(r.getClubId()), where + ": a club twice in the table");
        }
        assertEquals(goalsFor, goalsAgainst, where + ": goals for = goals against");

        // every fixture of the edition played exactly once, none left scheduled
        Map<Long, Match> played = new HashMap<>();
        for (Club c : game.getAllClubs()) {
            if (!clubs.contains(c.getId())) {
                continue;
            }
            for (Match m : c.getPlayedMatches()) {
                if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH && inEdition(m, edition)) {
                    played.put(m.getId(), m);
                }
            }
            for (Match m : c.getScheduledMatches()) {
                if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH && inEdition(m, edition)) {
                    throw new AssertionError(where + ": league match left unplayed: " + m.getId() + " on "
                            + m.getMatchDateTime());
                }
            }
        }
        assertEquals(n * (n - 1), played.size(), where + ": league matches played");
        Set<String> pairs = new HashSet<>();
        for (Match m : played.values()) {
            assertTrue(Boolean.TRUE.equals(m.getIsPlayed()), where + ": match " + m.getId() + " played");
            assertTrue(pairs.add(m.getHomeClubId() + "v" + m.getAwayClubId()), where + ": pairing played twice");
        }
    }

    static boolean inEdition(Match m, CompetitionEdition edition) {
        return m.getMatchDateTime() != null && edition.getStartDate() != null
                && !m.getMatchDateTime().toLocalDate().isBefore(edition.getStartDate().toLocalDate())
                && (edition.getEndDate() == null
                        || !m.getMatchDateTime().toLocalDate().isAfter(edition.getEndDate().toLocalDate()));
    }

    static void checkMoney(String where, SaveGame game) {
        for (Club c : game.getAllClubs()) {
            BigDecimal b = c.getClubBalance();
            assertNotNull(b, where + ": balance of " + c.getName());
            assertTrue(b.abs().compareTo(MONEY_LIMIT) < 0, where + ": balance of " + c.getName() + " is " + b);
        }
    }

    static void checkSaveRoundTrip(String where, SaveGame game) {
        String text = SaveCodec.encode(game, "soak", LocalDateTime.of(2000, 1, 1, 0, 0));
        LoadResult back = SaveCodec.decode(text);
        assertTrue(back.isLoaded(), where + ": the save loads: " + back);
        assertEquals(GameFingerprint.of(game).toString(), GameFingerprint.of(back.getGame()).toString(),
                where + ": save/load equality");
    }
}
