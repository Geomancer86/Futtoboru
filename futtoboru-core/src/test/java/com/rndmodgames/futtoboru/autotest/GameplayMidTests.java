package com.rndmodgames.futtoboru.autotest;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * A-01 mid scenario: one full season played by {@link PlayerBot}, from the new game (1 April 1888) to the 1889-90
 * rollover and the next League season, every invariant checked each day and week, a manual save and load in
 * November and the February autosave reloaded ({@code -Dautotest.dismissal=true}: dismissed on 1 November 1888, he
 * plays on as a job seeker). Report: {@code target/autotest/mid.md}. Seed and club:
 * {@code -Dautotest.seed=1888 -Dautotest.club=stoke}.
 */
@Tag("slow")
class GameplayMidTests extends BaseTest {

    @AfterAll
    static void finish() {
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void oneSeasonToThe1889RolloverKeepsEveryInvariant() {
        GameplayRun.startReport("mid");
        GameplayRun.Plan p = GameplayRun.Plan.of(Long.getLong("autotest.seed", 1888L));
        p.strategy.clubDataId = System.getProperty("autotest.club", "stoke");
        p.seasons = 1;
        if (Boolean.parseBoolean(System.getProperty("autotest.dismissal", "false"))) {
            p.committeeLosesPatienceOn = java.time.LocalDate.of(1888, 11, 1);
        }
        if (Boolean.parseBoolean(System.getProperty("autotest.reloads", "true"))) {
            p.manualReloadDays = Set.of(220);
            p.autosaveReloadMonths = Set.of(2);
        }
        p.onFailure = r -> GameplayRun.writeReport("mid", "mid: one season (FAILED)", r.report());
        GameplayRun run = GameplayRun.play(p);
        GameplayRun.writeReport("mid", "mid: one season", run.report());
    }
}
