package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * P2-13: a game lists every club once. A new game used to list the clubs of the selected countries twice
 * (NewGameOverviewScreen added the competitions' participants, then every club of each country), so the daily and
 * weekly per-club steps (tickets, attribute drift, weekly expenses and snapshots) ran twice for them. Saves made
 * before the fix are cleaned on load.
 */
class NewGameClubsTests extends BaseTest {

    private static final LocalDateTime SAVED_AT = LocalDateTime.of(2000, 1, 1, 0, 0);

    @AfterAll
    static void reloadDatabase() {
        // the loads rebuilt the shared database and the days played changed its clubs
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void aNewGameListsEveryParticipantAndEveryClubOfTheSelectedCountriesOnce() {
        SaveGame game = NewGameFixture.newGameInSession(application);
        assertEachClubOnce(game.getAllClubs());

        for (Competition comp : DatabaseLoader.getCompetitions()) {
            for (CompetitionEdition edition : comp.getEditions()) {
                for (Long id : edition.getParticipantClubsIds()) {
                    assertSame(DatabaseLoader.getClubById(id), game.getClubById(id), "participant " + id);
                }
            }
        }
        Country england = DatabaseLoader.getCountryByDataId("england");
        List<Club> english = DatabaseLoader.getClubsByCountry().get(england.getId());
        assertTrue(english.size() > 12);
        for (Club club : english) {
            assertSame(club, game.getClubById(club.getId()), club.getName());
        }
        // what the screen did before: participants, then all of England again
        Set<Long> participants = new HashSet<>();
        for (Club club : game.getAllClubs()) {
            if (!english.contains(club)) {
                participants.add(club.getId());
            }
        }
        assertTrue(participants.size() > 0, "a cup entrant from another country (e.g. Linfield)");
        assertEquals(participants.size() + english.size(), game.getAllClubs().size(), "each club once");
    }

    @Test
    void theParticipantsComeFirstAndAClubAlreadyListedIsNotAddedAgain() {
        DatabaseLoader.getInstance();
        List<Competition> competitions = DatabaseLoader.getCompetitions();
        List<Country> england = List.of(DatabaseLoader.getCountryByDataId("england"));

        SaveGame game = new SaveGame();
        int clubs = NewGameClubs.addTo(game, competitions, england);
        assertEquals(clubs, NewGameClubs.addTo(game, competitions, england), "a second call adds nothing");
        assertEachClubOnce(game.getAllClubs());
        Long firstParticipant = competitions.get(0).getEditions().get(0).getParticipantClubsIds().get(0);
        assertEquals(firstParticipant, game.getAllClubs().get(0).getId(), "the data's order, participants first");

        Club preston = game.getClubById(DatabaseLoader.getClubByDataId("preston-north-end").getId());
        Club copy = new Club(preston.getName());
        copy.setId(preston.getId());
        assertFalse(game.addClub(preston), "the same club");
        assertFalse(game.addClub(copy), "another object with the club's id");
        assertFalse(game.addClub(null));
        assertEquals(clubs, game.getAllClubs().size());
    }

    @Test
    void aSaveRoundTripKeepsEveryClubOnce() {
        SaveGame game = NewGameFixture.newGameInSession(application);
        List<Long> order = ids(game.getAllClubs());

        LoadResult result = SaveCodec.decode(SaveCodec.encode(game, "P2-13", SAVED_AT));

        assertTrue(result.isLoaded(), result.toString());
        assertEquals(order, ids(result.getGame().getAllClubs()));
        assertEachClubOnce(result.getGame().getAllClubs());
    }

    /** What the load does to a pre-P2-13 game: the first entry of each club (by id) and each snapshot stays. */
    @Test
    void removingDuplicatesKeepsTheFirstEntryOfEachClubAndSnapshot() {
        SaveGame game = new SaveGame();
        Club a = new Club("A");
        a.setId(1L);
        Club b = new Club("B");
        b.setId(2L);
        Club sameIdAsB = new Club("B again");
        sameIdAsB.setId(2L);
        Club c = new Club("C");
        c.setId(3L);
        game.getAllClubs().addAll(List.of(a, b, a, c, sameIdAsB, b));
        assertEquals(3, game.removeDuplicateClubs());
        assertEquals(List.of(a, b, c), game.getAllClubs());
        assertEquals(0, game.removeDuplicateClubs(), "nothing left to drop");

        LocalDateTime week = LocalDateTime.of(1888, 4, 2, 0, 0);
        for (int copy = 0; copy < 2; copy++) {
            for (long player = 1; player <= 3; player++) {
                com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot s = new com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot();
                s.setPlayerId(player);
                s.setSnapshotDate(week);
                s.setSnapshotType("WEEKLY");
                game.getPlayerAttributeSnapshots().add(s);
            }
        }
        com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot nextWeek = new com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot();
        nextWeek.setPlayerId(1L);
        nextWeek.setSnapshotDate(week.plusDays(7));
        nextWeek.setSnapshotType("WEEKLY");
        game.getPlayerAttributeSnapshots().add(nextWeek);
        assertEquals(3, game.removeDuplicateSnapshots());
        assertEquals(4, game.getPlayerAttributeSnapshots().size());
    }

    /**
     * A save of a game started before P2-13: the clubs of England listed a second time after the participants and
     * the weekly attribute snapshots of their players taken twice. The load keeps each club once, in its order, as
     * the one object every other reference (league, database lookups) points at, and each snapshot once; the loaded
     * game then books one week's expenses per club. (Slow: a played save written, edited and read back, ~2 s.)
     */
    @Test
    @Tag("slow")
    void loadingASaveThatListsClubsTwiceKeepsEachClubOnceAsTheObjectOthersPointAt() {
        SaveGame game = NewGameFixture.newGameInSession(application);
        NewGameFixture.play(application, game, 1, true); // the first weekly snapshots and expenses
        List<Long> order = ids(game.getAllClubs());
        int snapshots = game.getPlayerAttributeSnapshots().size();
        assertTrue(snapshots > 300, "snapshots: " + snapshots);

        String text = SaveCodec.encode(game, "Before P2-13", SAVED_AT);
        StringBuilder english = new StringBuilder();
        Country england = DatabaseLoader.getCountryByDataId("england");
        for (Club club : DatabaseLoader.getClubsByCountry().get(england.getId())) {
            english.append(", ").append(club.getId());
        }
        // both arrays hold no nested array: the first ']' closes them
        Matcher clubs = Pattern.compile("(\"allClubs\": \\[)([^\\]]*)\\]").matcher(text);
        assertTrue(clubs.find());
        text = text.substring(0, clubs.end(2)) + english + text.substring(clubs.end(2));
        Matcher shots = Pattern.compile("(\"playerAttributeSnapshots\": \\[)([^\\]]*)\\]").matcher(text);
        assertTrue(shots.find());
        text = text.substring(0, shots.end(2)) + "," + shots.group(2) + text.substring(shots.end(2));
        JsonValue saved = new JsonReader().parse(text).get("game");
        assertTrue(saved.get("allClubs").size > order.size() + 12, "the save lists clubs twice");
        assertEquals(2 * snapshots, saved.get("playerAttributeSnapshots").size);

        LoadResult result = SaveCodec.decode(text);

        assertTrue(result.isLoaded(), result.toString());
        assertEquals(new ArrayList<String>(), result.getProblems());
        SaveGame loaded = result.getGame();
        assertEquals(order, ids(loaded.getAllClubs()), "each club once, in the order of its first entry");
        assertEachClubOnce(loaded.getAllClubs());
        for (League league : loaded.getMainAuthority().getLeagues()) {
            for (Club c : league.getLeagueClubs()) {
                assertSame(c, loaded.getClubById(c.getId()), "league club " + c.getName());
                assertSame(c, DatabaseLoader.getClubById(c.getId()), "database lookup " + c.getName());
            }
        }
        assertSame(loaded.getCurrentClub(), DatabaseLoader.getClubByDataId("preston-north-end"));
        assertEquals(snapshots, loaded.getPlayerAttributeSnapshots().size(), "each snapshot once");

        NewGameFixture.bind(application, loaded);
        NewGameFixture.play(application, loaded, 7, true);
        assertOneExpenseEntryPerWeek(loaded, 2);
    }

    /**
     * The daily drift applies one step of the player's {@code training:<player>:<date>} stream (a club listed twice
     * applied the same step twice: +2/-2 instead of +1/-1), the weekly snapshot is taken once per player and the
     * weekly expenses are booked once per club.
     */
    @Test
    void weeklyExpensesSnapshotsAndTheDailyAttributeDriftRunOncePerClub() {
        SaveGame game = NewGameFixture.newGameInSession(application);
        Map<Long, float[]> before = new HashMap<>();
        int players = 0;
        for (Club club : game.getAllClubs()) {
            for (Player p : club.getPlayers()) {
                if (p.getAcceleration() == null) {
                    // the screen generates them at the new game (step 11); the fixture leaves that to the test
                    new com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator().generatePlayerAttributes(
                            p, p.getPerson(), game.getGameDate(),
                            application.random().fork("attributes:" + p.getPerson().getId()));
                }
                before.put(p.getId(), physical(p));
                players++;
            }
        }

        NewGameFixture.play(application, game, 1, true);

        LocalDate day = game.getGameDate().toLocalDate();
        int checked = 0;
        for (Club club : game.getAllClubs()) {
            for (Player p : club.getPlayers()) {
                RandomSource step = application.random().fork("training:" + p.getId() + ":" + day);
                int attribute = step.nextInt(7);
                int change = step.nextInt(3) - 1;
                float was = before.get(p.getId())[attribute];
                long age = java.time.temporal.ChronoUnit.YEARS.between(p.getPerson().getBirthDate(), game.getGameDate());
                if (was >= 4 && was <= 22 && age < 30) { // away from the 3-23 clamp and the age decline
                    assertEquals((int) (was + change), physical(p)[attribute], 0f,
                            club.getName() + " " + p.getId() + ": one drift step on " + day);
                    checked++;
                }
            }
        }
        assertTrue(checked > players / 2, "checked " + checked + " of " + players);
        Set<Long> snapshotted = new HashSet<>();
        for (com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot s : game.getPlayerAttributeSnapshots()) {
            assertTrue(snapshotted.add(s.getPlayerId()), "one weekly snapshot of player " + s.getPlayerId());
        }
        assertEquals(players, snapshotted.size());

        NewGameFixture.play(application, game, 7, true);
        assertOneExpenseEntryPerWeek(game, 2);
    }

    // ------------------------------------------------------------------------------------------------- helpers

    private static void assertEachClubOnce(List<Club> clubs) {
        Set<Long> ids = new HashSet<>();
        Set<Club> objects = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Club c : clubs) {
            assertNotNull(c);
            assertTrue(ids.add(c.getId()), c.getName() + " listed once by id");
            assertTrue(objects.add(c), c.getName() + " listed once");
        }
    }

    private static void assertOneExpenseEntryPerWeek(SaveGame game, int weeks) {
        for (Club c : game.getAllClubs()) {
            Set<LocalDateTime> periods = new HashSet<>();
            for (ClubExpenses e : c.getExpensesHistory()) {
                assertTrue(periods.add(e.getPeriodStart()), c.getName() + ": week of " + e.getPeriodStart() + " once");
            }
            assertEquals(weeks, periods.size(), c.getName() + " weekly expense entries");
        }
    }

    private static List<Long> ids(List<Club> clubs) {
        List<Long> out = new ArrayList<>();
        for (Club c : clubs) {
            out.add(c.getId());
        }
        return out;
    }

    /** In the order of the drift's physical switch: acceleration, speed, stamina, strength, endurance, jumping, dexterity */
    private static float[] physical(Player p) {
        return new float[] { p.getAcceleration(), p.getSpeed(), p.getStamina(), p.getStrength(), p.getEndurance(),
                p.getJumping(), p.getDexterity() };
    }
}
