package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;

/**
 * Headless E2E: 1888–89 league and FA Cup complete in the same save.
 * Fails on cup bracket health until CUP-B1 is fixed — run in nightly profile.
 */
@Tag("nightly")
class LeagueAndCupIT {

    private GameTestHarness harness;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        harness.newGameEmployedAtClub(GameTestHarness.PRESTON_NORTH_END_CLUB_ID, GameTestHarness.SEASON_1888_89_ID);
    }

    @Test
    void leagueAndFaCupBothCompleteWithChampions() {
        assertNotNull(harness.primaryCup(), "FA Cup should be attached from database at new game");

        harness.simulateUntilLeagueAndCupComplete();

        League league = harness.primaryLeague();
        assertNotNull(league);
        LeagueStandingsManager standingsManager = new LeagueStandingsManager();
        assertTrue(standingsManager.isLeagueComplete(league));
        assertNotNull(standingsManager.getChampion(league));

        assertTrue(harness.countCupEditionsWithChampions() >= 1,
                "Expected at least one FA Cup edition with a champion");

        Competition cup = harness.primaryCup();
        boolean hasCupChampion = false;
        for (CompetitionEdition edition : cup.getEditions()) {
            if (edition != null && edition.getChampionsId() != null) {
                hasCupChampion = true;
                break;
            }
        }
        assertTrue(hasCupChampion, "Primary cup should record championsId on an edition");
    }
}
