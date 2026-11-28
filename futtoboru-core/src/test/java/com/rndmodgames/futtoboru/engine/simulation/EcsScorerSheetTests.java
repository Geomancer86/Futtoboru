package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;

/**
 * E-FIX-scorers: on the ECS engine every recorded scorer is a man of his side's frozen team sheet (player ids and
 * person ids kept apart, so a mix-up shows), over matches with goals in both halves.
 */
@Tag("slow")
public class EcsScorerSheetTests extends BaseTest {

    @Test
    void everyEcsScorerIsInHisSidesKickOffEleven() {
        EngineTestWorld world = new EngineTestWorld(application, 6);
        for (Club c : world.clubs) {
            for (Player p : c.getPlayers()) {
                p.setId(world.game.getIdAllocator().nextId() + 100_000); // player id != person id
            }
        }
        MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(1888), new EcsMatchEngineAdapter());
        int scorers = 0;
        boolean secondHalf = false;
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < 6; i += 2) {
                Club home = world.club((i + round) % 6);
                Club away = world.club((i + round + 1) % 6);
                Match m = world.leagueMatch(home, away);
                assertTrue(sim.simulateMatch(m));
                assertNotNull(m.getHomeSheet());
                scorers += check(m, home, m.getHomeSheet(), true) + check(m, away, m.getAwaySheet(), false);
                for (Integer min : m.getHomeGoalMinutes()) {
                    secondHalf |= min > 45;
                }
                for (Integer min : m.getAwayGoalMinutes()) {
                    secondHalf |= min > 45;
                }
            }
        }
        assertTrue(scorers > 0, "some goals have scorers");
        assertTrue(secondHalf, "a second-half goal was played");
    }

    private static int check(Match m, Club club, TeamSheet sheet, boolean home) {
        Map<Long, Player> squad = TeamSheets.squadById(club);
        Set<Long> eleven = new HashSet<>();
        for (Long playerId : sheet.selectedIds()) {
            Player p = squad.get(playerId);
            if (p != null && p.getPerson() != null) {
                eleven.add(p.getPerson().getId());
            }
        }
        var ids = home ? m.getHomeScorerIds() : m.getAwayScorerIds();
        for (Long id : ids) {
            assertTrue(eleven.contains(id), club.getName() + " scorer " + id + " not in the sheet " + eleven
                    + " of match " + m.getId());
        }
        assertTrue(ids.size() <= (home ? m.getHomeGoals() : m.getAwayGoals()), "more scorers than goals");
        return ids.size();
    }
}
