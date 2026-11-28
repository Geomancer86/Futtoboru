package com.rndmodgames.futtoboru.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.simulation.MatchEngine;
import com.rndmodgames.futtoboru.engine.simulation.MatchEngines;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * E-17a soak entry point (D33-1): plays the whole 1888-89 season headless on the ECS engine with the
 * {@link FullSeasonTests} machinery (new game, Continue, the manager's match days, draws read, rollover) and prints
 * the invariants: every league fixture once, the table adds up, scorers play for their side (in that match's kick-off sheet), no errors; plus season
 * runtime and goals per game against 1888-89's 4.45. One ECS match takes about 2.5 s, so a season takes tens of
 * minutes: it only runs when asked for.
 *
 * <pre>
 * mvnw -pl futtoboru-core test -Dtest=EcsSeasonSoakTests -Dfuttoboru.soak=true [-Dfuttoboru.soak.seeds=1888,7]
 * </pre>
 */
@Tag("slow")
@Tag("season")
@EnabledIfSystemProperty(named = "futtoboru.soak", matches = "true")
class EcsSeasonSoakTests extends BaseTest {

    @AfterAll
    static void restore() {
        MatchEngines.reset();
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void aSeasonOnTheEcsEngineKeepsTheInvariants() {
        List<String> failures = new ArrayList<>();
        for (String s : System.getProperty("futtoboru.soak.seeds", String.valueOf(FullSeasonTests.SEED)).split(",")) {
            long seed = Long.parseLong(s.trim());
            MatchEngines.select(MatchEngine.ECS);
            FullSeasonTests.SeasonRun run;
            try {
                run = FullSeasonTests.SeasonRun.play(seed);
            } finally {
                MatchEngines.reset();
            }
            failures.addAll(report(seed, run));
            DatabaseLoader.resetDatabaseLoaderInstance();
        }
        assertTrue(failures.isEmpty(), String.join(FullSeasonTests.NL, failures));
    }

    /** Prints the season's numbers; returns the broken invariants. */
    static List<String> report(long seed, FullSeasonTests.SeasonRun run) {
        List<String> broken = new ArrayList<>();
        Map<Long, Match> all = new LinkedHashMap<>();
        for (Club c : run.save.getAllClubs()) {
            for (Match m : c.getPlayedMatches()) {
                if (m.getMatchDateTime() != null && m.getMatchDateTime().isBefore(LocalDateTime.of(1889, 7, 1, 0, 0))) {
                    all.putIfAbsent(m.getId(), m);
                }
            }
        }
        int goals = 0;
        int home = 0;
        int draw = 0;
        int away = 0;
        int scorersKnown = 0;
        for (Match m : run.leagueMatches) {
            int h = m.getHomeGoals();
            int a = m.getAwayGoals();
            goals += h + a;
            home += h > a ? 1 : 0;
            draw += h == a ? 1 : 0;
            away += h < a ? 1 : 0;
            scorersKnown += m.getHomeScorerIds().size() + m.getAwayScorerIds().size();
        }
        int scorerMisplaced = 0;
        int allGoals = 0;
        for (Match m : all.values()) {
            allGoals += m.getHomeGoals() + m.getAwayGoals();
            scorerMisplaced += misplaced(run, m.getHomeClubId(), m.getHomeSheet(), m.getHomeScorerIds())
                    + misplaced(run, m.getAwayClubId(), m.getAwaySheet(), m.getAwayScorerIds());
            if (m.getHomeScorerIds().size() > m.getHomeGoals() || m.getAwayScorerIds().size() > m.getAwayGoals()) {
                broken.add("more scorers than goals in match " + m.getId());
            }
        }
        Set<String> pairings = new HashSet<>();
        for (Match m : run.leagueMatches) {
            if (!pairings.add(m.getHomeClubId() + "v" + m.getAwayClubId())) {
                broken.add("league pairing twice: " + m.getHomeClubId() + "v" + m.getAwayClubId());
            }
        }
        int n = Math.max(1, run.leagueMatches.size());
        System.out.println(String.format(Locale.ROOT,
                "E-17a ECS season seed %d: %d days, %.1f min (%d ms), %d matches played (%.0f ms per match);"
                        + " league %d matches, %.2f goals/game (1888-89: 4.45), H/D/A %.0f/%.0f/%.0f %% (59/17/24),"
                        + " scorers known for %.0f %% of league goals; all matches %.2f goals/game; cup winner %s;"
                        + " %d errors, %d misplaced scorers",
                seed, run.days, run.millis / 60000.0, run.millis, all.size(), run.millis / (double) Math.max(1, all.size()),
                run.leagueMatches.size(), goals / (double) n, 100.0 * home / n, 100.0 * draw / n, 100.0 * away / n,
                goals == 0 ? 0 : 100.0 * scorersKnown / goals, allGoals / (double) Math.max(1, all.size()),
                run.cupEdition != null ? run.cupEdition.getChampionsId() : null, run.errors.size(), scorerMisplaced));
        if (run.leagueMatches.size() != 132) {
            broken.add("league matches " + run.leagueMatches.size() + " (132 expected)");
        }
        if (!run.errors.isEmpty()) {
            broken.add(run.errors.size() + " errors, first: " + run.errors.get(0));
        }
        if (scorerMisplaced > 0) {
            broken.add(scorerMisplaced + " scorers who do not play for their side");
        }
        if (run.cupEdition == null || run.cupEdition.getChampionsId() == null) {
            broken.add("no FA Cup winner");
        }
        int points = 0;
        for (var row : run.leagueEdition.getFinalStandings()) {
            points += row.getPoints();
        }
        int expected = 0;
        var rules = run.league.getRulesOrDefault();
        for (Match m : run.leagueMatches) {
            expected += m.getHomeGoals().equals(m.getAwayGoals()) ? 2 * rules.getPointsForDraw()
                    : rules.getPointsForWin() + rules.getPointsForLoss();
        }
        if (points != expected) {
            broken.add("table points " + points + ", from results " + expected);
        }
        assertEquals(12, run.leagueEdition.getFinalStandings().size(), "final table rows");
        return broken;
    }

    /**
     * Scorer ids (person ids) that are not men of {@code clubId}'s eleven in that match: its sheet frozen at kick-off
     * (G-01), else the club's 1888-89 squad (G-04). Not the club's squad at the end of the run: the 1 May rollover
     * released men since, and their goals still count for the side they played for.
     */
    private static int misplaced(FullSeasonTests.SeasonRun run, Long clubId, TeamSheet sheet, List<Long> scorerIds) {
        if (scorerIds.isEmpty()) {
            return 0;
        }
        Set<Long> playerIds = sheet != null ? new HashSet<>(sheet.selectedIds())
                : run.seasonMen.getOrDefault(clubId, Set.of());
        Set<Long> ids = new HashSet<>(playerIds); // a game player's id is his person's (DatabaseLoader, contracts)
        for (Player p : allPlayers(run)) {
            if (p != null && p.getPerson() != null && playerIds.contains(p.getId())) {
                ids.add(p.getPerson().getId());
            }
        }
        int out = 0;
        for (Long id : scorerIds) {
            out += ids.contains(id) ? 0 : 1;
        }
        return out;
    }

    private static List<Player> allPlayers(FullSeasonTests.SeasonRun run) {
        List<Player> all = new ArrayList<>(run.save.getFreeAgents());
        for (Club c : run.save.getAllClubs()) {
            all.addAll(c.getPlayers());
        }
        return all;
    }
}
