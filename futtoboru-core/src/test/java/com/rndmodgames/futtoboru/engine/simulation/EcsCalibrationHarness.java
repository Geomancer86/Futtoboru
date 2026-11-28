package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSelector;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchEngine;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchResult;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;

/**
 * E-20 calibration harness (docs/design/MATCH_ENGINE_ECS.md §7): plays the 132 real 1888-89 league pairings (home,
 * away, date from {@code docs/data/results_1888-89.csv}, reference data only, D17) on the ECS engine through
 * {@link EcsMatchEngineAdapter}'s input, with the clubs' real squads, their committee's best sheets and the seeded
 * weather of the ground and day and the G-02 men hurt in the match (as {@link MatchSimulator} rolls them), once per
 * seed; matches run on a thread pool (a result depends only on its fork).
 * Prints goals per game, home and away goals, H/D/A, 0-0s, the score histogram and its distance to the real one,
 * injuries, offsides and fouls per match. Prints only: the acceptance bands are the calibration's job, not a gate.
 *
 * <pre>
 * mvnw -pl futtoboru-core test -Dtest=EcsCalibrationHarness -Dfuttoboru.calibration=true -Dsurefire.failIfNoSpecifiedTests=false
 *      [-Dfuttoboru.calibration.seeds=1888,1889] [-Dfuttoboru.calibration.matches=132] [-Dfuttoboru.calibration.threads=16]
 *      [-Dfuttoboru.calibration.set=ai.wShot=1.2;body.injuryRiskAtDouble=0.005]   (PhysicsParams overrides, for tuning)
 * </pre>
 */
@Tag("slow")
@EnabledIfSystemProperty(named = "futtoboru.calibration", matches = "true")
class EcsCalibrationHarness extends BaseTest {

    private static final String NL = System.lineSeparator();

    /** One real pairing of 1888-89 with its real score (the score is only compared, never fed in). */
    record Pairing(LocalDate date, String home, String away, int homeGoals, int awayGoals) {
    }

    /** What one simulated match gives the statistics. */
    record Played(int home, int away, int injuries, int off, int passengers, int offsides, int fouls) {
    }

    @AfterAll
    static void restore() {
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void playTheRealPairingsAndPrintTheStatistics() throws Exception {
        List<Pairing> real = readPairings();
        int limit = Integer.getInteger("futtoboru.calibration.matches", real.size());
        int threads = Integer.getInteger("futtoboru.calibration.threads",
                Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        PhysicsParams params = overrides(new PhysicsParams(), System.getProperty("futtoboru.calibration.set", ""));
        EcsMatchEngine engine = new EcsMatchEngine(params);
        List<Played> all = new ArrayList<>();
        long start = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (String s : System.getProperty("futtoboru.calibration.seeds", "1888").split(",")) {
                long seed = Long.parseLong(s.trim());
                all.addAll(season(seed, real.subList(0, Math.min(limit, real.size())), engine, pool));
            }
        } finally {
            pool.shutdown();
        }
        long ms = System.currentTimeMillis() - start;
        System.out.println(report(all, real, ms, threads, System.getProperty("futtoboru.calibration.set", "")));
        assertTrue(!all.isEmpty());
    }

    /** One season of the real pairings on {@code seed}: squads and attributes as a new game makes them. */
    private static List<Played> season(long seed, List<Pairing> pairings, EcsMatchEngine engine, ExecutorService pool)
            throws Exception {
        DatabaseLoader.load(GameData.loadDefault(), RandomSource.seeded(seed));
        RandomSource root = RandomSource.seeded(seed);
        Map<String, Club> clubs = new HashMap<>();
        PlayerAttributeGenerator attributes = new PlayerAttributeGenerator();
        for (Pairing p : pairings) {
            for (String id : new String[] { p.home(), p.away() }) {
                clubs.computeIfAbsent(id, k -> {
                    Club c = DatabaseLoader.getClubByDataId(k);
                    if (c == null) {
                        throw new IllegalStateException("no club " + k);
                    }
                    for (Player player : c.getPlayers()) {
                        if (player != null && player.getPerson() != null && player.getAcceleration() == null) {
                            attributes.generatePlayerAttributes(player, player.getPerson(),
                                    p.date().atStartOfDay(), root.fork("attributes:" + player.getPerson().getId()));
                        }
                    }
                    return c;
                });
            }
        }
        Map<String, TeamSheet> sheets = new HashMap<>();
        for (Map.Entry<String, Club> e : clubs.entrySet()) {
            sheets.put(e.getKey(), TeamSelector.bestSheet(e.getValue().getId(), TeamSheets.available(e.getValue()),
                    null));
        }
        List<Future<Played>> futures = new ArrayList<>();
        for (Pairing p : pairings) {
            Club home = clubs.get(p.home());
            Club away = clubs.get(p.away());
            String key = "cal:" + p.date() + ":" + p.home() + "-" + p.away();
            MatchWeather weather = MatchWeather.of(root, p.date(), MatchWeather.groundOf(home));
            // G-02 men hurt in the match, rolled as MatchSimulator does (the adapter weakens them, E-17)
            com.rndmodgames.futtoboru.engine.injuries.InjuryRules rules =
                    com.rndmodgames.futtoboru.engine.injuries.InjuryRules.current();
            RandomSource hurt = root.fork("injuries:" + key);
            MatchFixture f = new MatchFixture(key, home, away, p.date().atTime(15, 30), weather, false,
                    sheets.get(p.home()), sheets.get(p.away()),
                    com.rndmodgames.futtoboru.engine.injuries.MatchInjuries.roll(
                            InstantMatchEngine.onField(sheets.get(p.home()), home), rules, hurt.fork("home")),
                    com.rndmodgames.futtoboru.engine.injuries.MatchInjuries.roll(
                            InstantMatchEngine.onField(sheets.get(p.away()), away), rules, hurt.fork("away")),
                    rules);
            EcsMatchEngineAdapter.Eleven h = EcsMatchEngineAdapter.Eleven.of(f.homeSheet(), home, f);
            EcsMatchEngineAdapter.Eleven a = EcsMatchEngineAdapter.Eleven.of(f.awaySheet(), away, f);
            RandomSource rng = root.fork("match:" + key);
            java.util.concurrent.Callable<Played> job = () -> {
                int[] inj = new int[3];
                EcsMatchResult r = engine.play(EcsMatchEngineAdapter.input(f, h, a), rng, world -> {
                    for (Entity e : world.engine().getEntitiesFor(Family.all(InjuryComponent.class).get())) {
                        InjuryComponent c = e.getComponent(InjuryComponent.class);
                        inj[0] += c.count;
                        inj[1] += c.isOff() ? 1 : 0;
                        inj[2] += c.state == InjuryComponent.PASSENGER ? 1 : 0;
                    }
                });
                return new Played(r.homeGoals(), r.awayGoals(), inj[0], inj[1], inj[2], r.offsides(), r.fouls());
            };
            futures.add(pool.submit(job));
        }
        List<Played> out = new ArrayList<>();
        for (Future<Played> fu : futures) {
            out.add(fu.get());
        }
        return out;
    }

    static String report(List<Played> sim, List<Pairing> real, long ms, int threads, String set) {
        int n = sim.size();
        double[] s = stats(sim.stream().map(p -> new int[] { p.home(), p.away() }).toList());
        double[] r = stats(real.stream().map(p -> new int[] { p.homeGoals(), p.awayGoals() }).toList());
        Map<String, Integer> simHist = histogram(sim.stream().map(p -> new int[] { p.home(), p.away() }).toList());
        Map<String, Integer> realHist = histogram(real.stream().map(p -> new int[] { p.homeGoals(), p.awayGoals() })
                .toList());
        double tv = 0;
        java.util.Set<String> keys = new java.util.TreeSet<>(simHist.keySet());
        keys.addAll(realHist.keySet());
        for (String k : keys) {
            tv += Math.abs(simHist.getOrDefault(k, 0) / (double) n - realHist.getOrDefault(k, 0) / (double) real.size());
        }
        tv /= 2;
        // total goals per match 0..9, 10+ (the shape the bands mean; a 132-match score grid is too sparse to compare)
        double[] gs = new double[11];
        double[] gr = new double[11];
        sim.forEach(p -> gs[Math.min(10, p.home() + p.away())] += 1.0 / n);
        real.forEach(p -> gr[Math.min(10, p.homeGoals() + p.awayGoals())] += 1.0 / real.size());
        double tvGoals = 0;
        for (int i = 0; i < gs.length; i++) {
            tvGoals += Math.abs(gs[i] - gr[i]) / 2;
        }
        double inj = 0;
        double off = 0;
        double pas = 0;
        double offs = 0;
        double fouls = 0;
        for (Played p : sim) {
            inj += p.injuries();
            off += p.off();
            pas += p.passengers();
            offs += p.offsides();
            fouls += p.fouls();
        }
        StringBuilder b = new StringBuilder();
        b.append(String.format(Locale.ROOT, "E-20 calibration: N=%d matches, %d threads, %.0f s (%.0f ms CPU-ish per match)"
                + "  set=[%s]%n", n, threads, ms / 1000.0, ms * (double) threads / Math.max(1, n), set));
        b.append(String.format(Locale.ROOT, "%-22s %8s %8s%n", "", "ECS", "1888-89"));
        String[] names = { "goals/game", "home goals", "away goals", "home win %", "draw %", "away win %",
            "0-0 per 132", "margin (abs)" };
        for (int i = 0; i < names.length; i++) {
            b.append(String.format(Locale.ROOT, "%-22s %8.2f %8.2f%n", names[i], s[i], r[i]));
        }
        b.append(String.format(Locale.ROOT, "%-22s %8.3f%n", "goals hist TV dist", tvGoals));
        b.append(String.format(Locale.ROOT, "%-22s %8.3f%n", "score grid TV dist", tv));
        b.append(String.format(Locale.ROOT, "%-22s %8.2f%n", "injuries/match", inj / n));
        b.append(String.format(Locale.ROOT, "%-22s %8.2f%n", "carried off/match", off / n));
        b.append(String.format(Locale.ROOT, "%-22s %8.2f%n", "passengers/match", pas / n));
        b.append(String.format(Locale.ROOT, "%-22s %8.2f%n", "offsides/match", offs / n));
        b.append(String.format(Locale.ROOT, "%-22s %8.2f%n", "fouls/match", fouls / n));
        b.append("most common scores ECS: ").append(top(simHist, 8)).append(NL);
        b.append("most common scores real: ").append(top(realHist, 8)).append(NL);
        return b.toString();
    }

    /** goals/game, home, away, H %, D %, A %, 0-0 per 132, mean absolute margin. */
    private static double[] stats(List<int[]> scores) {
        double n = Math.max(1, scores.size());
        double h = 0;
        double a = 0;
        double hw = 0;
        double d = 0;
        double aw = 0;
        double nil = 0;
        double margin = 0;
        for (int[] x : scores) {
            h += x[0];
            a += x[1];
            hw += x[0] > x[1] ? 1 : 0;
            d += x[0] == x[1] ? 1 : 0;
            aw += x[0] < x[1] ? 1 : 0;
            nil += x[0] == 0 && x[1] == 0 ? 1 : 0;
            margin += Math.abs(x[0] - x[1]);
        }
        return new double[] { (h + a) / n, h / n, a / n, 100 * hw / n, 100 * d / n, 100 * aw / n, 132 * nil / n,
            margin / n };
    }

    private static Map<String, Integer> histogram(List<int[]> scores) {
        Map<String, Integer> m = new TreeMap<>();
        for (int[] x : scores) {
            m.merge(Math.min(x[0], 7) + "-" + Math.min(x[1], 7), 1, Integer::sum);
        }
        return m;
    }

    private static String top(Map<String, Integer> hist, int k) {
        int total = hist.values().stream().mapToInt(Integer::intValue).sum();
        StringBuilder b = new StringBuilder();
        hist.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey())).limit(k)
                .forEach(e -> b.append(String.format(Locale.ROOT, "%s %.0f%%  ", e.getKey(),
                        100.0 * e.getValue() / total)));
        return b.toString().trim();
    }

    static List<Pairing> readPairings() throws IOException {
        Path csv = Path.of("../docs/data/results_1888-89.csv");
        if (!Files.exists(csv)) {
            csv = Path.of("docs/data/results_1888-89.csv");
        }
        List<Pairing> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        for (String line : lines.subList(1, lines.size())) {
            String[] c = line.split(",");
            if (c.length < 5 || c[3].isBlank()) {
                continue;
            }
            out.add(new Pairing(LocalDate.parse(c[0]), c[1], c[2], Integer.parseInt(c[3]), Integer.parseInt(c[4])));
        }
        return out;
    }

    /** Applies {@code path=value;path=value} to public fields of {@code params} (e.g. {@code ai.wShot=1.2}). */
    static PhysicsParams overrides(PhysicsParams params, String spec) throws ReflectiveOperationException {
        Map<String, String> pairs = new LinkedHashMap<>();
        for (String kv : spec.split(";")) {
            if (!kv.isBlank()) {
                String[] p = kv.split("=", 2);
                pairs.put(p[0].trim(), p[1].trim());
            }
        }
        for (Map.Entry<String, String> e : pairs.entrySet()) {
            Object target = params;
            String[] path = e.getKey().split("\\.");
            for (int i = 0; i < path.length - 1; i++) {
                target = target.getClass().getField(path[i]).get(target);
            }
            Field f = target.getClass().getField(path[path.length - 1]);
            Class<?> t = f.getType();
            String v = e.getValue();
            if (t == double.class) {
                f.setDouble(target, Double.parseDouble(v));
            } else if (t == int.class) {
                f.setInt(target, Integer.parseInt(v));
            } else if (t == boolean.class) {
                f.setBoolean(target, Boolean.parseBoolean(v));
            } else {
                throw new IllegalArgumentException("unsupported field type " + t + " for " + e.getKey());
            }
        }
        return params;
    }
}
