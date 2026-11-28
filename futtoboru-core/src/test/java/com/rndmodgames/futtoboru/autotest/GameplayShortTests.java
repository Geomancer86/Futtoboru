package com.rndmodgames.futtoboru.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * A-01 short scenario: a new game played by {@link PlayerBot} for six weeks (1 April to mid-May 1888: the
 * committee's expectations, friendlies, injuries, the first autosave), three seeds and three clubs, every invariant
 * checked each day and week; a manual save and load on day 20, the 1 May autosave reloaded. About 1 s a run: seeds 2
 * and 3 are in the fast tier; seed 1, played three times (same seed = same game, with or without the reloads), is
 * slow. Report: {@code target/autotest/short.md}.
 */
class GameplayShortTests extends BaseTest {

    static final int DAYS = 42;
    static final List<String> lines = new ArrayList<>();

    @BeforeAll
    static void start() {
        GameplayRun.startReport("short");
    }

    @AfterAll
    static void finish() {
        GameplayRun.writeReport("short", "short: new game + " + DAYS + " days", lines);
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    static GameplayRun.Plan plan(long seed, String club, boolean reloads) {
        GameplayRun.Plan p = GameplayRun.Plan.of(seed);
        p.days = DAYS;
        p.strategy.clubDataId = club;
        if (reloads) {
            p.manualReloadDays = Set.of(20);
            p.autosaveReloadMonths = Set.of(5);
        }
        return p;
    }

    static GameplayRun play(GameplayRun.Plan p) {
        p.onFailure = r -> lines.addAll(r.report());
        GameplayRun run = GameplayRun.play(p);
        lines.addAll(run.report());
        return run;
    }

    @Test
    @Tag("slow")
    void prestonSeed1PlaysTheSameGameTwiceAndWithoutTheReloads() {
        GameplayRun a = play(plan(1, "preston-north-end", true));
        GameplayRun b = play(plan(1, "preston-north-end", true));
        assertEquals(a.fingerprint, b.fingerprint, "seed 1: same seed, same game");
        GameplayRun c = GameplayRun.play(plan(1, "preston-north-end", false));
        assertEquals(a.fingerprint, c.fingerprint, "seed 1: the reloads changed the game:" + System.lineSeparator()
                + GameplayRun.diff(c.fingerprint, a.fingerprint));
        assertEquals(2, a.bot.reloads.size(), "a manual and an autosave reload");
    }

    @Test
    void stokeSeed2() {
        play(plan(2, "stoke", true));
    }

    @Test
    void evertonSeed3() {
        play(plan(3, "everton", true));
    }
}
