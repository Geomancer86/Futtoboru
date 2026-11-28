package com.rndmodgames.futtoboru.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.IdAllocator;
import com.rndmodgames.futtoboru.system.save.GameFingerprint;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * P2-10: a New Game started after another game was played or loaded in the same session starts from the data as it
 * is in gamedata (no played fixtures, zeroed tables, starting balances, unexecuted scripts, only data editions) and
 * shares no object with the earlier game; runtime ids stay the same.
 */
class NewGameFreshDataTests {

    private static Futtoboru application;

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        new HeadlessApplication(application, config);
        BaseTest.awaitHeadlessCreate();
    }

    @Test
    void newGameAfterPlayingAnotherStartsFresh() {
        SaveGame a = NewGameFixture.newGame(application);
        Map<String, BigDecimal> startBalances = balances(a);
        Map<String, Long> startIds = ids(a);
        NewGameFixture.play(application, a, 170, true);
        GameFingerprint aAfter170 = GameFingerprint.of(a);
        assertTrue(playedMatches(a) > 0, "game A should have played matches");
        assertFalse(startBalances.equals(balances(a)), "game A should have changed some balances");

        SaveGame b = NewGameFixture.newGameInSession(application);

        assertFresh(b, startBalances);
        assertNoSharedObjects(a, b);
        assertEquals(startIds, ids(b), "runtime ids are the same in every new game");

        // nothing of A is left anywhere: B plays the same 170 days exactly as A did (same seed)
        NewGameFixture.play(application, b, 170, true);
        assertEquals(aAfter170.toString(), GameFingerprint.of(b).toString());
    }

    @Test
    void newGameAfterLoadingASaveStartsFresh() throws Exception {
        SaveGame fresh = NewGameFixture.newGame(application);
        Map<String, BigDecimal> startBalances = balances(fresh);
        Map<String, Long> startIds = ids(fresh);

        String text;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("saves/v1_1888-89_new_game.json")) {
            assertNotNull(in);
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        LoadResult loaded = SaveCodec.decode(text);
        assertTrue(loaded.isLoaded(), loaded.toString());
        SaveGame a = loaded.getGame();
        NewGameFixture.bind(application, a);
        NewGameFixture.play(application, a, 170, true);
        assertTrue(playedMatches(a) > 0, "the loaded game should have played matches");

        SaveGame b = NewGameFixture.newGameInSession(application);

        assertFresh(b, startBalances);
        assertNoSharedObjects(a, b);
        assertEquals(startIds, ids(b), "runtime ids are the same in every new game");
    }

    // ------------------------------------------------------------------------------------------------- checks

    private static void assertFresh(SaveGame b, Map<String, BigDecimal> startBalances) {
        assertEquals(0, playedMatches(b), "no played fixtures");
        assertEquals(startBalances, balances(b), "starting balances");
        for (Club club : b.getAllClubs()) {
            String c = club.getDataId();
            assertTrue(club.getScheduledMatches().isEmpty(), c + " scheduled matches");
            assertTrue(club.getProposedMatches().isEmpty(), c + " proposed matches");
            assertZero(club.getMatchesPlayed(), c + " played");
            assertZero(club.getMatchesWon(), c + " won");
            assertZero(club.getMatchesDrawn(), c + " drawn");
            assertZero(club.getMatchesLost(), c + " lost");
            assertZero(club.getGoalsScored(), c + " goals for");
            assertZero(club.getGoalsConceded(), c + " goals against");
            assertZero(club.getPoints(), c + " points");
            assertTrue(club.getMatchIncomes().isEmpty(), c + " match incomes");
            assertTrue(club.getExpensesHistory().isEmpty(), c + " expenses");
            assertNull(club.getCurrentLeague(), c + " league (the league is created by a script during play)");
        }
        for (Competition comp : DatabaseLoader.getCompetitions()) {
            for (CompetitionEdition edition : comp.getEditions()) {
                assertTrue(edition.getId() < IdAllocator.GAME_START, "only data editions: " + edition.getName());
                assertNull(edition.getChampionsId(), edition.getName() + " champion");
                assertTrue(edition.getFinalStandings() == null || edition.getFinalStandings().isEmpty());
            }
        }
        for (BasicScript script : b.getGameScripts()) {
            assertFalse(script.getIsExecuted(), "script " + script.getName() + " not executed yet");
        }
    }

    private static void assertNoSharedObjects(SaveGame a, SaveGame b) {
        Set<Object> ofA = identities(a);
        List<String> shared = new ArrayList<>();
        for (Object o : identities(b)) {
            if (ofA.contains(o)) {
                shared.add(o.getClass().getSimpleName() + " " + o);
            }
        }
        assertEquals(new ArrayList<String>(), shared, "objects shared between game A and game B");
    }

    /** Every club, ground, player, person, contract, competition, edition and script the game reaches. */
    private static Set<Object> identities(SaveGame game) {
        Set<Object> all = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Club club : game.getAllClubs()) {
            all.add(club);
            if (club.getStadium() != null) {
                all.add(club.getStadium());
            }
            for (Player player : club.getPlayers()) {
                all.add(player);
                all.add(player.getPerson());
            }
            all.addAll(club.getPlayerContracts());
        }
        for (Person person : game.getAllPersons()) {
            all.add(person);
        }
        List<Competition> comps = new ArrayList<>(game.getAllCups());
        comps.addAll(game.getAllLeagues());
        for (Competition comp : comps) {
            all.add(comp);
            all.addAll(comp.getEditions());
        }
        all.addAll(game.getGameScripts());
        if (game.getMainAuthority() != null) {
            all.add(game.getMainAuthority());
        }
        return all;
    }

    private static void assertZero(Integer value, String what) {
        assertTrue(value == null || value == 0, what + " = " + value);
    }

    private static int playedMatches(SaveGame game) {
        int played = 0;
        for (Club club : game.getAllClubs()) {
            played += club.getPlayedMatches().size();
        }
        return played;
    }

    private static Map<String, BigDecimal> balances(SaveGame game) {
        Map<String, BigDecimal> balances = new LinkedHashMap<>();
        for (Club club : game.getAllClubs()) {
            balances.put(club.getDataId(), club.getClubBalance());
        }
        return balances;
    }

    /** Runtime ids of the game's data objects (clubs, grounds, players, contracts, competitions, the authority). */
    private static Map<String, Long> ids(SaveGame game) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (Club club : game.getAllClubs()) {
            ids.put("club " + club.getDataId(), club.getId());
            if (club.getStadium() != null) {
                ids.put("ground of " + club.getDataId(), club.getStadium().getId());
            }
            for (Player player : club.getPlayers()) {
                ids.put("player " + club.getDataId() + "/" + player.getPerson().getDataId(), player.getId());
            }
            for (PlayerContract contract : club.getPlayerContracts()) {
                ids.put("contract " + club.getDataId() + "/" + contract.getPlayerId(), contract.getId());
            }
        }
        for (Competition comp : DatabaseLoader.getCompetitions()) {
            ids.put("competition " + comp.getDataId(), comp.getId());
        }
        ids.put("authority", DatabaseLoader.getMainAuthority().getId());
        return ids;
    }
}
