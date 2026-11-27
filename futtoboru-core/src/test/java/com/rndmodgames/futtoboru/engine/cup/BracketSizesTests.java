package com.rndmodgames.futtoboru.engine.cup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.data.IdAllocator;

/**
 * P2-3: a bracket of any number of clubs is one knockout tree (byes into round 2 when the number is not a
 * power of two). With 12 clubs the old generator left the winner of R2M3 with no next tie.
 */
class BracketSizesTests extends BaseTest {

    private static List<Match> bracket(int clubs) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < clubs; i++) {
            ids.add(100L + i);
        }
        return new CupBracketGenerator(new IdAllocator(IdAllocator.GAME_START), RandomSource.seeded(clubs))
                .generateCompleteBracket(ids, 1L, 2L, LocalDateTime.of(1888, 10, 6, 15, 0), 2);
    }

    @Test
    void twelveClubsGetFourByesAndOneFinal() {
        List<Match> matches = bracket(12);
        Map<Integer, Integer> perRound = new HashMap<>();
        for (Match m : matches) {
            perRound.merge(m.getRound(), 1, Integer::sum);
        }
        assertEquals(4, perRound.get(1), "8 clubs play in round 1");
        assertEquals(4, perRound.get(2), "4 winners + 4 byes");
        assertEquals(2, perRound.get(3), "semi-finals");
        assertEquals(1, perRound.get(4), "final");
        assertEquals(11, matches.size(), "12 clubs: 11 ties");

        // the old bug: every round-2 tie has a next tie
        for (Match m : matches) {
            if (m.getRound() == 2) {
                assertNotNull(nextTie(matches, m), m.getBracketPath() + " feeds a semi-final");
            }
        }
    }

    @Test
    void everyBracketSizeIsOneKnockoutTree() {
        for (int clubs = 2; clubs <= 40; clubs++) {
            List<Match> matches = bracket(clubs);
            assertEquals(clubs - 1, matches.size(), clubs + " clubs: one tie per club knocked out");

            int lastRound = 0;
            for (Match m : matches) {
                lastRound = Math.max(lastRound, m.getRound());
            }
            Set<Long> seen = new HashSet<>();
            int finals = 0;
            for (Match m : matches) {
                // every club enters exactly once: in round 1 or with a bye into round 2
                for (Long club : new Long[] { m.getHomeClubId(), m.getAwayClubId() }) {
                    if (club != null) {
                        assertTrue(m.getRound() <= 2, clubs + " clubs: a known club in " + m.getBracketPath());
                        assertTrue(seen.add(club), clubs + " clubs: club " + club + " enters twice");
                    }
                }
                // each side of a later tie is a club or the winner of an earlier tie, never neither or both
                if (m.getRound() > 1) {
                    assertTrue((m.getHomeClubId() == null) == (m.getParentMatch1Id() != null),
                            clubs + " clubs: home side of " + m.getBracketPath());
                    assertTrue((m.getAwayClubId() == null) == (m.getParentMatch2Id() != null),
                            clubs + " clubs: away side of " + m.getBracketPath());
                } else {
                    assertNotNull(m.getHomeClubId());
                    assertNotNull(m.getAwayClubId());
                }
                // every tie but the final feeds exactly one later tie
                if (m.getRound() == lastRound) {
                    finals++;
                    assertEquals(null, nextTie(matches, m), "the final feeds nothing");
                } else {
                    assertNotNull(nextTie(matches, m), clubs + " clubs: " + m.getBracketPath() + " has a next tie");
                }
            }
            assertEquals(clubs, seen.size(), clubs + " clubs all entered");
            assertEquals(1, finals, clubs + " clubs: one final");
        }
    }

    private static Match nextTie(List<Match> matches, Match tie) {
        Match found = null;
        for (Match m : matches) {
            if (tie.getId().equals(m.getParentMatch1Id()) || tie.getId().equals(m.getParentMatch2Id())) {
                assertEquals(null, found, tie.getBracketPath() + " feeds two ties");
                found = m;
            }
        }
        return found;
    }
}
