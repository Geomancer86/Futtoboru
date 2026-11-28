package com.rndmodgames.futtoboru.engine.temporal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.LanguageBundleKeysTest;

/**
 * U-08: a friendly arranged by the manager, answered by letter the next day, played through the one result path
 * (league table untouched) with the gate booked to the home club.
 */
public class FriendlyArrangerTests extends BaseTest {

    private EngineTestWorld world;
    private Club own;
    private Club other;

    @BeforeEach
    void setUp() {
        world = new EngineTestWorld(application, 4);
        own = world.club(0);
        other = world.club(1);
        Person manager = new Person();
        manager.setId(world.game.getIdAllocator().nextId());
        manager.setCurrentClubId(own.getId());
        world.game.setOwner(manager);
    }

    private LocalDate today() {
        return world.game.getGameDate().toLocalDate();
    }

    /** The post: one day passes and the other club answers (as MatchScheduler does each day). */
    private Map<Long, FriendlyArranger.Reason> nextDayAnswer(FriendlyArranger arranger) {
        world.game.setGameDate(world.game.getGameDate().plusDays(1));
        return arranger.answerProposals(own);
    }

    /**
     * End to end in a new 1888-89 game (the manager at Preston, 1 Apr 1888, before the league starts): arrange,
     * the other club accepts, the days are played through the engine and the friendly through the one result path.
     */
    @Test
    void acceptedFriendlyIsPlayedOnceWithoutTouchingTheTableAndTheGateIsBooked() {
        SaveGame game = NewGameFixture.newGame(application);
        Club preston = game.getCurrentClub();
        assertNotNull(preston);
        FriendlyArranger arranger = new FriendlyArranger(application, 1.0);
        List<Club> opponents = arranger.possibleOpponents(preston);
        assertTrue(opponents.size() >= 11, "the other league clubs can be invited: " + opponents.size());
        Club visitors = opponents.get(0);
        LocalDate day = game.getGameDate().toLocalDate().plusDays(4);
        Map<Long, int[]> tableBefore = table(game);
        BigDecimal incomeBefore = preston.getSeasonIncome();
        BigDecimal visitorsIncomeBefore = visitors.getSeasonIncome();

        Match friendly = arranger.propose(preston, visitors, day, true);
        assertNotNull(friendly);
        assertTrue(preston.getProposedMatches().contains(friendly));
        assertEquals(FriendlyArranger.Reason.PENDING, arranger.check(preston, opponents.get(1), day));
        assertTrue(arranger.answerProposals(preston).isEmpty(), "the same day the letter is still in the post");

        game.setGameDate(game.getGameDate().plusDays(1));
        assertEquals(FriendlyArranger.Reason.NONE, arranger.answerProposals(preston).get(friendly.getId()));
        assertTrue(preston.getProposedMatches().isEmpty());
        assertTrue(preston.getScheduledMatches().contains(friendly));
        assertTrue(visitors.getScheduledMatches().contains(friendly));
        assertEquals(1, count(game, FriendlyArranger.MESSAGE_ACCEPTED));
        assertEquals(FriendlyArranger.Reason.OWN_ENGAGED, arranger.check(preston, opponents.get(1), day));

        // tickets on the days before, the match on its day
        NewGameFixture.play(application, game, 4, true);

        assertTrue(friendly.getIsPlayed(), "the friendly was played on its day");
        assertEquals(1, preston.getPlayedMatches().stream().filter(m -> m == friendly).count());
        assertEquals(1, visitors.getPlayedMatches().stream().filter(m -> m == friendly).count());
        assertFalse(preston.getScheduledMatches().contains(friendly));
        assertFalse(visitors.getScheduledMatches().contains(friendly));
        assertEquals(1, game.getAllMessages().stream().filter(m -> "MATCH_RESULT".equals(m.getMessageType())
                && m.getTitle().contains(preston.getName())).count(), "one result letter");
        assertTableEquals(tableBefore, table(game));

        MatchIncome gate = preston.getMatchIncomes().stream()
                .filter(i -> friendly.getId().equals(i.getMatchId())).findFirst().orElse(null);
        assertNotNull(gate, "the home club booked the gate");
        assertEquals(Match.FRIENDLY_MATCH, gate.getMatchType());
        assertTrue(gate.getTicketRevenue().signum() > 0);
        assertEquals(0, gate.getTicketRevenue().compareTo(friendly.getMatchRevenue()));
        assertTrue(preston.getSeasonIncome().subtract(incomeBefore).compareTo(gate.getTicketRevenue()) >= 0,
                "the gate is in the club's income");
        assertTrue(visitors.getMatchIncomes().stream().noneMatch(i -> friendly.getId().equals(i.getMatchId())),
                "the visitors sell no tickets for a friendly at the other club's ground");
        assertEquals(0, visitors.getSeasonIncome().compareTo(visitorsIncomeBefore));

        NewGameFixture.play(application, game, 1, true);
        assertEquals(1, preston.getPlayedMatches().stream().filter(m -> m == friendly).count(), "recorded once");
    }

    private static Map<Long, int[]> table(SaveGame game) {
        Map<Long, int[]> rows = new LinkedHashMap<>();
        for (Club c : game.getAllClubs()) {
            rows.put(c.getId(), new int[] { c.getMatchesPlayed(), c.getMatchesWon(), c.getMatchesDrawn(),
                c.getMatchesLost(), c.getGoalsScored(), c.getGoalsConceded(), c.getPoints() });
        }
        return rows;
    }

    private static void assertTableEquals(Map<Long, int[]> before, Map<Long, int[]> after) {
        assertEquals(before.keySet(), after.keySet());
        for (Long id : before.keySet()) {
            assertArrayEquals(before.get(id), after.get(id), "league table row of club " + id + " changed");
        }
    }

    private static long count(SaveGame game, String type) {
        return game.getAllMessages().stream().filter(m -> type.equals(m.getMessageType())).count();
    }

    @Test
    void declinedFriendlyLeavesNoMatchAndSendsAReason() {
        FriendlyArranger arranger = new FriendlyArranger(application, 0.0);
        Match friendly = arranger.propose(own, other, today().plusDays(5), false);
        assertNotNull(friendly);

        FriendlyArranger.Reason reason = nextDayAnswer(arranger).get(friendly.getId());
        assertTrue(reason == FriendlyArranger.Reason.NO_SIDE || reason == FriendlyArranger.Reason.GROUND_LET,
                "asked to play at home, they cannot raise a side or have let the ground: " + reason);
        assertTrue(own.getProposedMatches().isEmpty());
        assertTrue(own.getScheduledMatches().isEmpty());
        assertTrue(other.getScheduledMatches().isEmpty());
        assertEquals(1, countMessages(FriendlyArranger.MESSAGE_DECLINED));
        assertEquals(0, countMessages(FriendlyArranger.MESSAGE_ACCEPTED));
    }

    @Test
    void engagedClubDeclines() {
        FriendlyArranger arranger = new FriendlyArranger(application, 1.0);
        LocalDate day = today().plusDays(4);
        Match league = world.leagueMatch(other, world.club(2));
        league.setMatchDateTime(day.atTime(15, 0));

        Match friendly = arranger.propose(own, other, day, true);
        assertEquals(FriendlyArranger.Reason.ENGAGED, nextDayAnswer(arranger).get(friendly.getId()));
        assertFalse(own.getScheduledMatches().contains(friendly));
        assertEquals(1, countMessages(FriendlyArranger.MESSAGE_DECLINED));
    }

    @Test
    void proposalNeedsNoticeAnOpponentAndAFreeDay() {
        FriendlyArranger arranger = new FriendlyArranger(application, 1.0);
        assertEquals(FriendlyArranger.Reason.TOO_SOON, arranger.check(own, other, today().plusDays(1)));
        assertEquals(FriendlyArranger.Reason.TOO_SOON, arranger.check(own, other, today()));
        assertEquals(FriendlyArranger.Reason.NO_OPPONENT, arranger.check(own, own, today().plusDays(3)));
        assertEquals(FriendlyArranger.Reason.NO_OPPONENT, arranger.check(own, null, today().plusDays(3)));
        assertNull(arranger.propose(own, other, today().plusDays(1), true));
        assertTrue(own.getProposedMatches().isEmpty());

        assertEquals(3, arranger.possibleOpponents(own).size(), "every other club, not the manager's own");
        assertFalse(arranger.possibleOpponents(own).contains(own));
    }

    @Test
    void competitionMatchOnTheDayCallsTheFriendlyOff() {
        FriendlyArranger arranger = new FriendlyArranger(application, 1.0);
        LocalDate day = today().plusDays(6);
        Match friendly = arranger.propose(own, other, day, true);
        assertEquals(FriendlyArranger.Reason.NONE, nextDayAnswer(arranger).get(friendly.getId()));

        // a league match is fixed later for the visitors on the same day
        Match league = world.leagueMatch(other, world.club(3));
        league.setMatchDateTime(day.atTime(15, 0));
        arranger.cancelClashes(own);
        arranger.cancelClashes(other);

        assertFalse(own.getScheduledMatches().contains(friendly));
        assertFalse(other.getScheduledMatches().contains(friendly));
        assertTrue(other.getScheduledMatches().contains(league));
        assertEquals(1, countMessages(FriendlyArranger.MESSAGE_CANCELLED));
    }

    @Test
    void theSameSeedGivesTheSameAnswer() {
        FriendlyArranger arranger = new FriendlyArranger(application);
        Match friendly = arranger.propose(own, other, today().plusDays(3), true);
        Long id = friendly.getId();
        FriendlyArranger.Reason first = nextDayAnswer(arranger).get(id);

        // propose again with the same match id on a fresh copy of the day
        EngineTestWorld again = new EngineTestWorld(application, 4);
        Club ownAgain = again.club(0);
        Match copy = new FriendlyArranger(application).propose(ownAgain, again.club(1),
                again.game.getGameDate().toLocalDate().plusDays(3), true);
        copy.setId(id);
        again.game.setGameDate(again.game.getGameDate().plusDays(1));
        assertEquals(first, new FriendlyArranger(application).answerProposals(ownAgain).get(id));
    }

    @Test
    void scheduleLabelsExistInEveryLanguage() throws IOException {
        for (FriendlyArranger.Reason reason : FriendlyArranger.Reason.values()) {
            if (reason.key != null) {
                LanguageBundleKeysTest.assertKeyInAllBundles(reason.key);
            }
        }
        // the schedule tables and the arranger look labels up through text("key") / getValue("key")
        Pattern key = Pattern.compile("(?:text|getValue)\\(\\s*\"([a-z_]+)\"\\s*\\)");
        Pattern quoted = Pattern.compile("\"((?:friendly|schedule)_[a-z_]+)\"");
        Path[] sources = { Paths.get("src/main/java/com/rndmodgames/futtoboru/tables/schedule"),
                Paths.get("src/main/java/com/rndmodgames/futtoboru/engine/temporal/FriendlyArranger.java") };
        for (Path source : sources) {
            if (!Files.exists(source)) {
                return; // not run from the module directory
            }
            try (Stream<Path> files = Files.walk(source)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    String text = Files.readString(file);
                    for (Pattern p : new Pattern[] { key, quoted }) {
                        Matcher m = p.matcher(text);
                        while (m.find()) {
                            LanguageBundleKeysTest.assertKeyInAllBundles(m.group(1));
                        }
                    }
                }
            }
        }
    }

    private long countMessages(String type) {
        return world.game.getAllMessages().stream().filter(m -> type.equals(m.getMessageType())).count();
    }
}
