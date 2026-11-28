package com.rndmodgames.futtoboru.engine.cup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;
import com.rndmodgames.futtoboru.engine.temporal.MatchScheduler;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * P2-14: cup ties at neutral grounds (semi-finals and final, the cup season's data) and the open draw of a cup drawn
 * round by round. The test cup gets 1888-89-like rules: semi-finals at Bramall Lane or Crewe, the final at the Oval.
 */
class NeutralVenuesTests extends BaseTest {

    static final String CUP = "test-cup";

    private Map<String, NavigableMap<Integer, NeutralVenues.Rules>> before;
    private EngineTestWorld world;

    @BeforeEach
    void setUp() {
        before = NeutralVenues.installed();
        install(NeutralVenues.Gate.ASSOCIATION);
        world = new EngineTestWorld(application, 4);
        world.game.setGameDate(LocalDateTime.of(1889, 3, 1, 12, 0));
    }

    @AfterEach
    void restore() {
        NeutralVenues.install(before);
    }

    private void install(NeutralVenues.Gate gate) {
        Map<String, List<String>> grounds = new LinkedHashMap<>();
        grounds.put("semi-final", List.of("bramall-lane", "alexandra-recreation-ground"));
        grounds.put("final", List.of("kennington-oval"));
        Map<String, NavigableMap<Integer, NeutralVenues.Rules>> rules = new HashMap<>(before);
        NavigableMap<Integer, NeutralVenues.Rules> bySeason = new TreeMap<>();
        bySeason.put(1888, new NeutralVenues.Rules(grounds, gate));
        rules.put(CUP, bySeason);
        NeutralVenues.install(rules);
    }

    private EngineTestWorld.Cup cup() {
        EngineTestWorld.Cup cup = world.cup(world.clubs); // 4 clubs: semi-finals then the final
        cup.competition.setDataId(CUP);
        return cup;
    }

    @Test
    void roundsAreCountedBackFromTheFinal() {
        assertEquals("final", NeutralVenues.roundName(5, 5));
        assertEquals("semi-final", NeutralVenues.roundName(4, 5));
        assertEquals("quarter-final", NeutralVenues.roundName(3, 5));
        assertNull(NeutralVenues.roundName(1, 5));
    }

    @Test
    void theShippedDataPlaysTheFinalAtTheOvalAndKeepsTheRulesInLaterSeasons() {
        NeutralVenues.Rules r1888 = NeutralVenues.rulesFor("fa-cup", 1888);
        assertNotNull(r1888, "fa-cup-1888-89 neutralVenues");
        assertEquals(List.of("kennington-oval"), r1888.groundsFor("final"));
        assertEquals(2, r1888.groundsFor("semi-final").size());
        assertTrue(r1888.groundsFor("quarter-final").isEmpty(), "quarter-finals at home grounds");
        assertEquals(NeutralVenues.Gate.ASSOCIATION, r1888.getGate());
        assertEquals(r1888, NeutralVenues.rulesFor("fa-cup", 1890), "a later season keeps 1888-89's rules");
        assertNull(NeutralVenues.rulesFor("fa-cup", 1887));
        assertNotNull(DatabaseLoader.getGroundByDataId("kennington-oval"), "the Oval is a ground record");
        assertEquals("Kennington Oval", DatabaseLoader.getGroundByDataId("kennington-oval").getName());
    }

    @Test
    void aSemiFinalGetsACandidateGroundNeverAClubsOwnNorOneTakenThatDay() {
        EngineTestWorld.Cup cup = cup();
        world.club(0).getStadium().setDataId("bramall-lane"); // club A plays at home at Bramall Lane
        Match sf1 = cup.byPath("R1M1");
        Match sf2 = cup.byPath("R1M2");
        Long a = world.club(0).getId();
        sf1.setHomeClubId(a);
        sf1.setAwayClubId(world.club(1).getId());
        sf2.setHomeClubId(world.club(2).getId());
        sf2.setAwayClubId(world.club(3).getId());

        RandomSource root = RandomSource.seeded(7L);
        assertTrue(NeutralVenues.assign(world.game, cup.competition, sf1, cup.matches, 2, root));
        assertEquals("alexandra-recreation-ground", sf1.getVenue(), "not club A's own Bramall Lane");
        assertTrue(NeutralVenues.assign(world.game, cup.competition, sf2, cup.matches, 2, root));
        assertEquals("bramall-lane", sf2.getVenue(), "Crewe is taken that day");
        assertFalse(NeutralVenues.assign(world.game, cup.competition, sf2, cup.matches, 2, root), "only once");

        // the final, when its clubs are known
        Match fin = cup.byPath("R2M1");
        assertFalse(NeutralVenues.assign(world.game, cup.competition, fin, cup.matches, 2, root), "clubs unknown");
        fin.setHomeClubId(a);
        fin.setAwayClubId(world.club(3).getId());
        assertTrue(NeutralVenues.assign(world.game, cup.competition, fin, cup.matches, 2, root));
        assertEquals("kennington-oval", fin.getVenue());
        assertEquals("Kennington Oval", NeutralVenues.stadiumOf(fin, world.game).getName());
        Match atHome = new Match();
        atHome.setHomeClubId(world.club(2).getId());
        assertEquals(world.club(2).getStadium(), NeutralVenues.stadiumOf(atHome, world.game), "no venue: home ground");
    }

    @Test
    void theBracketFinalGetsItsGroundAndAReplayStaysThere() {
        EngineTestWorld.Cup cup = cup();
        MatchResultRecorder recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
        Match sf1 = cup.byPath("R1M1");
        Match sf2 = cup.byPath("R1M2");
        assertNull(sf1.getVenue(), "the test bracket was made without grounds");
        EngineTestWorld.score(sf1, 1, 0);
        recorder.record(sf1);
        EngineTestWorld.score(sf2, 2, 0);
        recorder.record(sf2);
        Match fin = cup.byPath("R2M1");
        assertNotNull(fin.getAwayClubId());
        assertEquals("kennington-oval", fin.getVenue(), "both finalists known: the final goes to the Oval");

        EngineTestWorld.score(fin, 1, 1);
        recorder.record(fin);
        assertEquals("Kennington Oval", com.rndmodgames.futtoboru.engine.report.MatchFacts.of(world.game, fin).ground(),
                "the match report names the neutral ground (G-03)");
        Match replay = null;
        for (Match m : world.game.getClubById(fin.getHomeClubId()).getScheduledMatches()) {
            if (fin.getId().equals(m.getParentMatch1Id())) {
                replay = m;
            }
        }
        assertNotNull(replay, "the drawn final is replayed");
        assertEquals("kennington-oval", replay.getVenue(), "at the same ground");
    }

    @Test
    void theAssociationTakesTheGateAtANeutralGround() {
        EngineTestWorld.Cup cup = cup();
        Match fin = neutralFinal(cup);
        Club home = world.game.getClubById(fin.getHomeClubId());
        Club away = world.game.getClubById(fin.getAwayClubId());

        MatchScheduler scheduler = new MatchScheduler(application);
        scheduler.checkClubSheduledMatches(home);
        scheduler.checkClubSheduledMatches(away);
        assertTrue(fin.getAttendance() > 0, "tickets sold at the Oval");
        assertTrue(fin.getMatchRevenue().signum() > 0, "the gate is recorded on the match");
        for (Club c : new Club[] { home, away }) {
            assertTrue(c.getMatchIncomes().isEmpty(), c.getName() + ": no gate entry");
            assertEquals(0, BigDecimal.ZERO.compareTo(c.getClubBalance()), c.getName() + ": balance");
        }
        assertEquals(List.of(), NeutralVenues.gateClubs(fin, world.game));
    }

    @Test
    void aSharedGateIsBookedOnceSplitBetweenTheClubs() {
        install(NeutralVenues.Gate.SHARED);
        EngineTestWorld.Cup cup = cup();
        Match fin = neutralFinal(cup);
        Club home = world.game.getClubById(fin.getHomeClubId());
        Club away = world.game.getClubById(fin.getAwayClubId());

        MatchScheduler scheduler = new MatchScheduler(application);
        scheduler.checkClubSheduledMatches(home);
        scheduler.checkClubSheduledMatches(away); // the away club sells nothing: once per match and day
        BigDecimal booked = home.getClubBalance().add(away.getClubBalance());
        assertEquals(0, fin.getMatchRevenue().compareTo(booked), "the whole gate, booked once");
        assertEquals(1, home.getMatchIncomes().size());
        assertEquals(1, away.getMatchIncomes().size());
        assertTrue(home.getClubBalance().subtract(away.getClubBalance()).abs().compareTo(new BigDecimal("0.01")) <= 0,
                "equal halves");
        assertEquals(List.of(fin.getHomeClubId(), fin.getAwayClubId()), NeutralVenues.gateClubs(fin, world.game));
    }

    /** The final of the test cup with both clubs known, at the Oval, three days from now */
    private Match neutralFinal(EngineTestWorld.Cup cup) {
        for (Club c : world.clubs) {
            c.setClubBalance(BigDecimal.ZERO);
            c.getScheduledMatches().clear(); // only the final sells tickets here
        }
        Match fin = cup.byPath("R2M1");
        fin.setHomeClubId(world.club(0).getId());
        fin.setAwayClubId(world.club(1).getId());
        fin.setMatchDateTime(world.game.getGameDate().plusDays(3));
        fin.setVenue("kennington-oval");
        world.club(0).getScheduledMatches().add(fin);
        world.club(1).getScheduledMatches().add(fin);
        return fin;
    }

    /** P2-14: a cup drawn round by round: winners wait for the next round's draw, made a few days after the round */
    @Test
    void anOpenDrawPairsTheWinnersAfterTheRound() {
        EngineTestWorld.Cup cup = cup();
        cup.edition.setDrawnRounds(1);
        MatchResultRecorder recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
        Match sf1 = cup.byPath("R1M1");
        Match sf2 = cup.byPath("R1M2");
        Match fin = cup.byPath("R2M1");
        LocalDate semiDay = sf1.getMatchDateTime().toLocalDate();
        world.game.setGameDate(sf1.getMatchDateTime());
        EngineTestWorld.score(sf1, 1, 0);
        recorder.record(sf1);
        EngineTestWorld.score(sf2, 0, 3);
        recorder.record(sf2);
        assertNull(fin.getHomeClubId(), "no bracket slot: the winner waits for the draw");
        assertNull(fin.getAwayClubId());
        assertEquals(2, cup.edition.getDrawPool().size());

        world.game.setGameDate(semiDay.plusDays(1).atTime(12, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertNull(fin.getHomeClubId(), "drawn two days after the round (the Monday after a Saturday)");

        world.game.setGameDate(semiDay.plusDays(2).atTime(12, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertNotNull(fin.getHomeClubId(), "the final is drawn");
        assertEquals(java.util.Set.of(sf1.getHomeClubId(), sf2.getAwayClubId()),
                java.util.Set.of(fin.getHomeClubId(), fin.getAwayClubId()), "the two winners");
        assertTrue(cup.edition.getDrawPool().isEmpty());
        assertEquals(2, cup.edition.getDrawnRounds());
        assertEquals("kennington-oval", fin.getVenue(), "the final's ground with its draw");

        // drawn once: the next day changes nothing
        Long home = fin.getHomeClubId();
        world.game.setGameDate(semiDay.plusDays(3).atTime(12, 0));
        world.authorityManager.checkCompetitionsSchedule();
        assertEquals(home, fin.getHomeClubId());
    }
}
