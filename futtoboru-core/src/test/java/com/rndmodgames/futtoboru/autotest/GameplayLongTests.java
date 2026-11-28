package com.rndmodgames.futtoboru.autotest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * A-01 long scenario: five seasons played by {@link PlayerBot} for each of three seeds (three clubs), every
 * invariant checked each day and week, a manual save and load every other season and the January autosave
 * reloaded; the third seed's committee loses patience in November 1890 (dismissal, Job Board, new post). Tag
 * {@code soak}: only in the nightly soak ({@code -Psoak}, Jenkins "Futtoboru Soak"), never in the local tiers.
 * Knobs: {@code -Dautotest.long.seeds=3 -Dautotest.long.seasons=5}. Report: {@code target/autotest/long.md}.
 */
@Tag("soak")
class GameplayLongTests extends BaseTest {

    static final String[] CLUBS = { "preston-north-end", "stoke", "everton", "accrington", "wolverhampton-wanderers" };

    @AfterAll
    static void finish() {
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @TestFactory
    Stream<DynamicTest> fiveSeasons() {
        GameplayRun.startReport("long");
        int seeds = Integer.getInteger("autotest.long.seeds", 3);
        int seasons = Integer.getInteger("autotest.long.seasons", 5);
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < seeds; i++) {
            final int index = i;
            long seed = 101 + i;
            String club = CLUBS[i % CLUBS.length];
            tests.add(DynamicTest.dynamicTest("seed " + seed + " " + club, () -> {
                GameplayRun.Plan p = GameplayRun.Plan.of(seed);
                p.strategy.clubDataId = club;
                p.seasons = seasons;
                p.manualReloadDays = Set.of(200, 200 + 365 * 2, 200 + 365 * 4);
                p.autosaveReloadMonths = Set.of(1);
                if (index % 3 == 2) {
                    // the third seed's secretary is dismissed in his third season: he plays on as a job seeker
                    p.committeeLosesPatienceOn = java.time.LocalDate.of(1890, 11, 1);
                }
                p.onFailure = r -> GameplayRun.writeReport("long", "long: seed " + seed + " (FAILED)", r.report());
                GameplayRun run = GameplayRun.play(p);
                GameplayRun.writeReport("long", "long: seed " + seed + ", " + seasons + " seasons", run.report());
            }));
        }
        return tests.stream();
    }
}
