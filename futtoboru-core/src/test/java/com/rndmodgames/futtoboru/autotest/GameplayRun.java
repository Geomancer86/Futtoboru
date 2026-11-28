package com.rndmodgames.futtoboru.autotest;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.save.GameFingerprint;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * A-01: one seeded game played by {@link PlayerBot} to a stop (days or seasons), with {@link GameInvariants}
 * checked every day and week, saves reloaded on the planned days, and the numbers of the run for the report.
 */
public final class GameplayRun {

    /** What to play. */
    public static final class Plan {
        public long seed;
        public PlayerBot.Strategy strategy = new PlayerBot.Strategy();
        /** stop after this many days (0: no day stop) */
        public int days;
        /** stop when this many seasons rolled over (1 May) and the next league season exists (0: no season stop) */
        public int seasons;
        /** the manual save + load on these days of each run (days since the start) */
        public Set<Integer> manualReloadDays = new HashSet<>();
        /** the autosave reloaded on the first autosave day of each of these months (1-12) */
        public Set<Integer> autosaveReloadMonths = new HashSet<>();
        /** fail when the mean time of a day is over this (ms) */
        public long msPerDayBudget = 250;
        /** fail when a single day takes longer than this (ms; rollover days included) */
        public long msMaxDay = 15_000;
        /** at most this many dismissals (all clubs) in a season */
        public int dismissalsPerSeason = 3;
        /**
         * On this day the bot's committee has lost patience (two warnings, confidence 5, the last warning long ago):
         * its own weekly judgement dismisses him (G-05) and he plays on as a job seeker (J-01). Null: never.
         */
        public LocalDate committeeLosesPatienceOn;
        /** a dismissed bot must have applied for a post within this many days */
        public int applyWithinDays = 60;
        /** called with the run when an invariant breaks (the report of the failed run) */
        public java.util.function.Consumer<GameplayRun> onFailure;

        public static Plan of(long seed) {
            Plan p = new Plan();
            p.seed = seed;
            return p;
        }
    }

    public final Plan plan;
    public PlayerBot bot;
    public final List<String> errors = new ArrayList<>();
    public int days;
    public long millis;
    public long slowestDay;
    public LocalDate slowestDate;
    public int injuries;
    public final List<String> seasonLines = new ArrayList<>();
    public final List<Integer> dismissalsBySeason = new ArrayList<>();
    public int moved;
    public int released;
    public int stayed;
    public int joined;
    public String fingerprint;
    public String failure;
    private long startedAt;

    private final Set<String> injuriesSeen = new HashSet<>();
    private Map<Long, Long> clubOfPlayer;
    private Integer lastContractsSeason;
    private int dismissalsAtSeasonStart;
    private Set<Integer> autosaveMonthsDone = new HashSet<>();

    private GameplayRun(Plan plan) {
        this.plan = plan;
    }

    /** Plays the plan in a temporary save folder; throws the first broken invariant. */
    public static GameplayRun play(Plan plan) {
        GameplayRun run = new GameplayRun(plan);
        File previous = SaveLoadSystem.getGameFolder();
        File folder;
        try {
            folder = Files.createTempDirectory("futtoboru-bot-" + plan.seed).toFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        SaveLoadSystem.setGameFolder(folder);
        Log.ErrorListener listener = (category, tag, message, error) -> run.errors.add(category + "/" + tag + ": "
                + message + (error != null ? " (" + error + ")" : ""));
        Log.addErrorListener(listener);
        try {
            run.run();
        } catch (AssertionError | RuntimeException e) {
            run.failure = e.getMessage();
            run.millis = System.currentTimeMillis() - run.startedAt;
            if (plan.onFailure != null && run.bot != null) {
                try {
                    plan.onFailure.accept(run);
                } catch (RuntimeException ignored) {
                    // the report of a broken game must not hide the failure
                }
            }
            throw e;
        } finally {
            Log.removeErrorListener(listener);
            if (run.bot != null) {
                run.bot.app.getGameEngine().getAutosave().setEnabled(false);
                run.bot.app.awaitAutosave();
            }
            SaveLoadSystem.setGameFolder(previous);
            deleteTree(folder);
        }
        return run;
    }

    private static void deleteTree(File f) {
        File[] kids = f.listFiles();
        if (kids != null) {
            for (File k : kids) {
                deleteTree(k);
            }
        }
        f.delete();
    }

    String where() {
        return "seed " + plan.seed + ", " + bot.today();
    }

    private void run() {
        startedAt = System.currentTimeMillis();
        long start = startedAt;
        bot = PlayerBot.newGame(plan.seed, plan.strategy);
        bot.app.getGameEngine().getAutosave().setEnabled(true);
        noErrors();
        clubOfPlayer = clubOfPlayer(bot.game());
        int budget = plan.days > 0 ? plan.days : plan.seasons * 420 + 60;
        while (true) {
            if (plan.days > 0 && days >= plan.days) {
                break;
            }
            if (plan.seasons > 0 && seasonsDone() >= plan.seasons && nextSeasonReady()) {
                break;
            }
            check(days < budget, "the run did not reach its stop within " + budget + " days");
            day();
        }
        endOfRun();
        millis = System.currentTimeMillis() - start;
        check(millis / Math.max(1, days) <= plan.msPerDayBudget, "mean " + millis / Math.max(1, days)
                + " ms a day, budget " + plan.msPerDayBudget);
    }

    void check(boolean ok, String what) {
        GameInvariants.check(ok, where(), what);
    }

    int seasonsDone() {
        Integer s = bot.game().getContractsSeason();
        return s == null ? 0 : s - 1888;
    }

    boolean nextSeasonReady() {
        for (League l : bot.game().getMainAuthority().getLeagues()) {
            if (l.getEditions().size() < seasonsDone() + 1) {
                return false;
            }
        }
        return true;
    }

    private void day() {
        SaveGame before = bot.game();
        LocalDate today = bot.today();
        int autosaves = bot.app.getGameEngine().getAutosave().getWrites();
        long t0 = System.nanoTime();
        Map<String, Integer> fit = bot.playDay();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        days++;
        if (ms > slowestDay) {
            slowestDay = ms;
            slowestDate = today;
        }
        check(ms <= plan.msMaxDay, "the day took " + ms + " ms (limit " + plan.msMaxDay + ")");
        check(bot.game() == before, "the game object changed during a day");
        check(bot.today().equals(today.plusDays(1)), "the day moved from " + today + " to " + bot.today());
        noErrors();
        String w = where();
        GameInvariants.oneClubPerMan(w, bot.game());
        GameInvariants.todaysSides(w, bot.game(), fit, today);
        countInjuries();
        rolloverBookkeeping();

        LocalDate now = bot.today();
        jobSeeker(now);
        if (now.getDayOfWeek() == DayOfWeek.MONDAY) {
            weekly();
        }
        if (now.getMonthValue() == 9 && now.getDayOfMonth() == 1) {
            GameInvariants.clubsAtSeasonStart(w, bot.game(), bot.game().getActiveJobOpenings());
        }
        // saves and reloads
        if (plan.manualReloadDays.contains(days)) {
            reload("manual");
        }
        if (bot.app.getGameEngine().getAutosave().getWrites() > autosaves
                && plan.autosaveReloadMonths.contains(now.getMonthValue())
                && autosaveMonthsDone.add(now.getYear() * 100 + now.getMonthValue())) {
            reload("autosave");
        }
    }

    private LocalDate unemployedSince;

    /** The committee's patience runs out on the planned day; a dismissed bot applies for posts and may find one. */
    private void jobSeeker(LocalDate now) {
        SaveGame g = bot.game();
        Club club = g.getCurrentClub();
        if (club != null && now.equals(plan.committeeLosesPatienceOn)) {
            com.rndmodgames.futtoboru.data.ClubCommittee c = club.getCommittee();
            check(c != null, "the bot's club has no committee");
            c.setWarnings(2);
            c.setConfidence(5);
            c.setLastWarning(now.minusDays(120).atStartOfDay());
        }
        if (club == null && unemployedSince == null) {
            unemployedSince = now;
        } else if (club != null && unemployedSince != null) {
            unemployedSince = null;
        }
        if (unemployedSince != null && now.equals(unemployedSince.plusDays(plan.applyWithinDays))) {
            check(bot.actions.applications > 0, "out of work since " + unemployedSince + " and no application made");
        }
    }

    void weekly() {
        String w = where();
        SaveGame g = bot.game();
        GameInvariants.matchesPlayedOnce(w, g, bot.today());
        GameInvariants.tables(w, g);
        GameInvariants.oneEditionPerSeason(w, g);
        GameInvariants.finishedCupsLeaveNoTies(w, g);
        GameInvariants.values(w, g, bot.today());
        GameInvariants.injuriesHeal(w, g, bot.today());
        GameInvariants.perceivedRangesHoldTheTruth(w, g, bot.app.random());
        GameInvariants.contracts(w, g, bot.app.getGameEngine().getContractManager().runningSeason(g));
    }

    private void reload(String kind) {
        PlayerBot.Reload r = bot.saveAndReload(kind);
        noErrors();
        if (!r.before().equals(r.after())) {
            GameInvariants.fail(where(), "the " + kind + " reload is not the game:" + System.lineSeparator()
                    + diff(r.before(), r.after()));
        }
    }

    static String diff(String a, String b) {
        String[] x = a.split("\n");
        String[] y = b.split("\n");
        Set<String> ys = new HashSet<>(List.of(y));
        Set<String> xs = new HashSet<>(List.of(x));
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (String l : x) {
            if (!ys.contains(l) && n++ < 8) {
                sb.append("  live:   ").append(shorten(l)).append('\n');
            }
        }
        n = 0;
        for (String l : y) {
            if (!xs.contains(l) && n++ < 8) {
                sb.append("  loaded: ").append(shorten(l)).append('\n');
            }
        }
        return sb.toString();
    }

    static String shorten(String s) {
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    private void noErrors() {
        if (!errors.isEmpty()) {
            GameInvariants.fail(where(), "errors logged: " + errors);
        }
    }

    private void countInjuries() {
        for (Club c : bot.game().getAllClubs()) {
            for (Player p : c.getPlayers()) {
                if (p.getInjury() != null) {
                    injuriesSeen.add(p.getId() + "@" + p.getInjury().getSince());
                }
            }
        }
        injuries = injuriesSeen.size();
    }

    /** At each 1 May rollover: who stayed, moved, left, joined; the season's dismissals; re-election places. */
    private void rolloverBookkeeping() {
        SaveGame g = bot.game();
        Integer cs = g.getContractsSeason();
        if (cs != null && !cs.equals(lastContractsSeason) && lastContractsSeason != null) {
            Map<Long, Long> now = clubOfPlayer(g);
            for (Map.Entry<Long, Long> e : clubOfPlayer.entrySet()) {
                Long after = now.get(e.getKey());
                if (after == null) {
                    released++;
                } else if (after.equals(e.getValue())) {
                    stayed++;
                } else {
                    moved++;
                }
            }
            for (Long id : now.keySet()) {
                if (!clubOfPlayer.containsKey(id)) {
                    joined++;
                }
            }
            int d = GameInvariants.dismissals(g) - dismissalsAtSeasonStart;
            dismissalsBySeason.add(d);
            dismissalsAtSeasonStart = GameInvariants.dismissals(g);
            check(d <= plan.dismissalsPerSeason, d + " secretaries dismissed in the season (bound "
                    + plan.dismissalsPerSeason + ")");
            seasonLines.add(seasonLine(g, cs - 1));
            checkSaveRoundTrip();
        }
        if (cs == null || !cs.equals(lastContractsSeason)) {
            lastContractsSeason = cs;
            clubOfPlayer = clubOfPlayer(g);
        } else if (bot.today().getMonthValue() == 4) {
            clubOfPlayer = clubOfPlayer(g); // the squads as they are just before the rollover
        }
        if (seasonsDone() > 0 && nextSeasonReady() && bot.today().getDayOfMonth() == 1) {
            GameInvariants.reElectionFillsPlaces(where(), g);
        }
    }

    private void checkSaveRoundTrip() {
        SaveGame g = bot.game();
        String text = SaveCodec.encode(g, "round trip", LocalDateTime.of(2000, 1, 1, 0, 0));
        String live = GameFingerprint.of(g).toString();
        LoadResult back = SaveCodec.decode(text);
        check(back.isLoaded(), "the round trip does not load: " + back);
        String loaded = GameFingerprint.of(back.getGame()).toString();
        if (!live.equals(loaded)) {
            GameInvariants.fail(where(), "save round trip differs:" + System.lineSeparator() + diff(live, loaded));
        }
    }

    static Map<Long, Long> clubOfPlayer(SaveGame g) {
        Map<Long, Long> out = new HashMap<>();
        for (Club c : g.getAllClubs()) {
            for (Player p : c.getPlayers()) {
                out.put(p.getId(), c.getId());
            }
        }
        return out;
    }

    private void endOfRun() {
        weekly();
        noErrors();
        fingerprint = GameFingerprint.of(bot.game()).toString();
        if (seasonLines.isEmpty() || plan.days > 0) {
            seasonLines.add(seasonLine(bot.game(), null));
        }
    }

    // --------------------------------------------------------------------------------------------- the numbers

    /** The played matches of the game, each once. */
    List<Match> played(Integer footballYear) {
        List<Match> out = new ArrayList<>();
        for (Match m : GameInvariants.playedById(bot.game()).values()) {
            if (footballYear == null || reportYear(m.getMatchDateTime().toLocalDate()) == footballYear) {
                out.add(m);
            }
        }
        return out;
    }

    /** The season a match belongs to in the report: July to June (the game's own year turns in April). */
    static int reportYear(LocalDate d) {
        return d.getMonthValue() >= 7 ? d.getYear() : d.getYear() - 1;
    }

    String seasonLine(SaveGame g, Integer year) {
        List<Match> ms = played(year);
        int league = 0;
        int cup = 0;
        int friendly = 0;
        int goals = 0;
        int home = 0;
        int draw = 0;
        int away = 0;
        for (Match m : ms) {
            int t = m.getMatchType() == null ? 0 : m.getMatchType();
            if (t == Match.LEAGUE_MATCH) {
                league++;
            } else if (t == Match.CUP_MATCH) {
                cup++;
            } else {
                friendly++;
            }
            goals += m.getHomeGoals() + m.getAwayGoals();
            if (t == Match.LEAGUE_MATCH) {
                if (m.getHomeGoals() > m.getAwayGoals()) {
                    home++;
                } else if (m.getHomeGoals().equals(m.getAwayGoals())) {
                    draw++;
                } else {
                    away++;
                }
            }
        }
        List<BigDecimal> balances = new ArrayList<>();
        for (Club c : g.getAllClubs()) {
            balances.add(c.getClubBalance());
        }
        Collections.sort(balances);
        int lm = Math.max(1, home + draw + away);
        return String.format(Locale.ROOT, "%s: %d matches (%d league, %d cup, %d friendly), %.2f goals/game, "
                + "league H/D/A %d/%d/%d %%, balances min %s median %s max %s",
                year == null ? "to " + bot.today() : year + "-" + String.valueOf(year + 1).substring(2), ms.size(),
                league, cup, friendly, ms.isEmpty() ? 0.0 : goals / (double) ms.size(), 100 * home / lm,
                100 * draw / lm, 100 * away / lm, pounds(balances.get(0)), pounds(balances.get(balances.size() / 2)),
                pounds(balances.get(balances.size() - 1)));
    }

    static String pounds(BigDecimal b) {
        return "£" + b.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /** The one-line summary of the run. */
    public String summary() {
        List<Match> ms = played(null);
        int goals = 0;
        int home = 0;
        int draw = 0;
        int away = 0;
        int league = 0;
        for (Match m : ms) {
            goals += m.getHomeGoals() + m.getAwayGoals();
            if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH) {
                league++;
                if (m.getHomeGoals() > m.getAwayGoals()) {
                    home++;
                } else if (m.getHomeGoals().equals(m.getAwayGoals())) {
                    draw++;
                } else {
                    away++;
                }
            }
        }
        List<BigDecimal> balances = new ArrayList<>();
        for (Club c : bot.game().getAllClubs()) {
            balances.add(c.getClubBalance());
        }
        Collections.sort(balances);
        int lm = Math.max(1, league);
        PlayerBot.Actions a = bot.actions;
        return String.format(Locale.ROOT, "seed %d (%s): %d seasons, %d days, %d matches, %.2f goals/game, league "
                + "H/D/A %d/%d/%d %%, %d injuries, %d dismissals (bot %d), rollover %d stayed / %d moved / %d left / "
                + "%d joined, re-signed %d, released %d, balances %s..%s (median %s), %d ms/day (slowest %d ms %s), "
                + "reloads %d manual + %d autosave, friendlies proposed %d, job applications %d",
                plan.seed, plan.strategy.clubDataId, seasonsDone(), days, ms.size(),
                ms.isEmpty() ? 0.0 : goals / (double) ms.size(), 100 * home / lm, 100 * draw / lm, 100 * away / lm,
                injuries, GameInvariants.dismissals(bot.game()), a.dismissals, stayed, moved, released, joined,
                a.retained, a.released, pounds(balances.get(0)), pounds(balances.get(balances.size() - 1)),
                pounds(balances.get(balances.size() / 2)), millis / Math.max(1, days), slowestDay, slowestDate,
                a.manualReloads, a.autosaveReloads, a.friendliesProposed, a.applications);
    }

    /** The full report of one run. */
    public List<String> report() {
        List<String> out = new ArrayList<>();
        out.add("### " + summary());
        if (failure != null) {
            out.add("- **FAILED**: " + failure.replace(System.lineSeparator(), " | ").replace("\n", " | "));
        }
        out.addAll(seasonLines.stream().map(s -> "- " + s).toList());
        PlayerBot.Actions a = bot.actions;
        out.add(String.format(Locale.ROOT, "- bot: %d letters read (%d injury, %d committee, %d re-election), %d draws, "
                + "%d matches prepared, %d auto-picks, %d tinkers, %d terms sent, %d offers raised, %d jobs accepted, "
                + "%d days unemployed", a.lettersRead, a.injuryLetters, a.committeeLetters, a.reElectionLetters,
                a.drawsOpened, a.matchesPrepared, a.autoPicks, a.tinkers, a.termsSent, a.offersRaised, a.jobsAccepted,
                a.unemployedDays));
        out.add("- dismissals by season: " + dismissalsBySeason + ", running season "
                + bot.app.getGameEngine().getContractManager().runningSeason(bot.game()) + ", next action "
                + bot.app.getGameEngine().getNextGameAction() + ", contracts season " + bot.game().getContractsSeason()
                + " (" + ContractManager.MESSAGE_CLOSE_SEASON + " letters "
                + bot.game().getAllMessages().stream().filter(m -> ContractManager.MESSAGE_CLOSE_SEASON
                        .equals(m.getMessageType())).count() + ")");
        return out;
    }

    /** Appends {@code lines} under {@code title} to target/autotest/{@code scenario}.md (the file is replaced per JVM). */
    public static synchronized void writeReport(String scenario, String title, List<String> lines) {
        Path out = Paths.get("target", "autotest", scenario + ".md");
        try {
            Files.createDirectories(out.getParent());
            List<String> all = new ArrayList<>();
            all.add("## " + title);
            all.addAll(lines);
            all.add("");
            Files.write(out, all, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (String l : lines) {
            System.out.println("AUTOTEST " + scenario + " " + l);
        }
    }

    public static synchronized void startReport(String scenario) {
        try {
            Files.deleteIfExists(Paths.get("target", "autotest", scenario + ".md"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
