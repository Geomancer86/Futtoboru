package com.rndmodgames.futtoboru.system.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.utils.Array;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.loaders.ScriptsLoader;

/**
 * Runtime ids (P2-4, D11): data objects below IdAllocator.GAME_START, objects made during play from the game's
 * allocator, and no two objects of any kind with the same id after a new-game setup and a few weeks of play.
 */
class RuntimeIdsTests {

    private static Futtoboru application;

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        new HeadlessApplication(application, config);
        // create() runs on the app thread and replaces the engine: wait for it before building games
        BaseTest.awaitHeadlessCreate();
    }

    /** Builds a fresh database and a new game the way NewGameOverviewScreen does, then plays some days. */
    private static SaveGame newGame(int days) {
        SaveGame game = NewGameFixture.newGame(application);
        NewGameFixture.play(application, game, days, false);
        // time stops at the mandatory draw message: jump to the cup creation date (P2-14: the season calendar's
        // opening day, 6 Oct 1888)
        game.setGameDate(com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar.forSeason(1888).withCup("fa-cup")
                .cupOpens().atTime(12, 0));
        application.getScriptsManager().checkGameScripts();
        return game;
    }

    @Test
    void dataIdsAreBelowGameIdsAndNoTwoObjectsShareAnId() {
        SaveGame game = newGame(45);

        Map<Long, Object> byId = new HashMap<>();
        Map<Object, Boolean> seen = new IdentityHashMap<>();
        Map<Long, String> matchKeys = new HashMap<>();
        List<String> problems = new ArrayList<>();

        // data objects (built at startup)
        List<Object> data = new ArrayList<>();
        data.addAll(DatabaseLoader.getInstance().getContinents());
        data.addAll(DatabaseLoader.getInstance().getCountries());
        data.addAll(DatabaseLoader.getInstance().getProfessions());
        data.addAll(DatabaseLoader.getInstance().getAllPlayerProfessions().values());
        data.addAll(DatabaseLoader.getInstance().getSeasons());
        data.addAll(DatabaseLoader.getInstance().getAuthorities());
        data.addAll(DatabaseLoader.getInstance().getPersons());
        for (Club club : DatabaseLoader.getInstance().getClubs()) {
            data.add(club);
            if (club.getStadium() != null) {
                data.add(club.getStadium());
            }
            for (Player p : club.getPlayers()) {
                data.add(p.getPerson()); // a Player shares its Person's id (it is the person's football role)
            }
            data.addAll(club.getPlayerContracts());
        }
        for (Competition comp : DatabaseLoader.getInstance().getCompetitions()) {
            data.add(comp);
            for (CompetitionEdition e : comp.getEditions()) {
                if (e.getDataId() != null) {
                    data.add(e);
                }
            }
        }

        // objects made during play
        List<Object> made = new ArrayList<>();
        made.addAll(game.getAllPersons());
        made.addAll(game.getMainAuthority().getLeagues());
        for (League league : game.getMainAuthority().getLeagues()) {
            made.addAll(league.getEditions());
        }
        List<Competition> competitions = new ArrayList<>(game.getAllCups());
        competitions.addAll(game.getAllLeagues());
        for (Competition comp : competitions) {
            for (CompetitionEdition e : comp.getEditions()) {
                if (e.getDataId() == null) {
                    made.add(e);
                }
            }
        }
        made.addAll(game.getAllMessages());
        made.addAll(game.getScheduledMessages());
        made.addAll(game.getActiveJobOpenings());
        made.addAll(game.getAllApplications());
        made.addAll(game.getPendingOffers());

        // the cup opened by its script (the season's data edition, P2-9), with a full bracket
        Competition faCup = DatabaseLoader.getInstance().getCompetitionByDataId("fa-cup");
        CompetitionEdition cupEdition = faCup.getEditions().get(faCup.getEditions().size() - 1);
        assertEquals("fa-cup-1888-89", cupEdition.getDataId(), "the cup script should open the data edition");
        assertEquals(1, faCup.getEditions().size(), "no second 1888-89 edition");
        assertNotNull(cupEdition.getStartDate(), "the cup script should have opened the edition");
        assertEquals(32, cupEdition.getParticipantClubsIds().size());
        List<Match> bracket = new com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator(game.getIdAllocator())
                .generateCompleteBracket(cupEdition.getParticipantClubsIds(), faCup.getId(), cupEdition.getId(),
                        game.getGameDate().plusWeeks(2), 2);
        assertFalse(bracket.isEmpty());
        made.addAll(bracket);

        int matches = 0;
        for (Club club : game.getAllClubs()) {
            List<Match> all = new ArrayList<>();
            all.addAll(club.getProposedMatches());
            all.addAll(club.getScheduledMatches());
            all.addAll(club.getPlayedMatches());
            for (Match m : all) {
                // a match is listed by both clubs: the same id must mean the same fixture
                String key = m.getHomeClubId() + "-" + m.getAwayClubId() + "@" + m.getMatchDateTime();
                String prev = matchKeys.putIfAbsent(m.getId(), key);
                if (prev == null) {
                    made.add(m);
                    matches++;
                } else if (!prev.equals(key)) {
                    problems.add("match id " + m.getId() + " used by " + prev + " and " + key);
                }
            }
        }

        for (Object o : data) {
            check(o, byId, seen, problems, true);
        }
        for (Object o : made) {
            check(o, byId, seen, problems, false);
        }

        assertTrue(matches > 0, "the league fixtures should exist");
        assertFalse(game.getAllMessages().isEmpty(), "messages should exist");
        assertEquals(new ArrayList<String>(), problems);
        assertTrue(game.getIdAllocator().peek() > IdAllocator.GAME_START);
    }

    private static void check(Object o, Map<Long, Object> byId, Map<Object, Boolean> seen, List<String> problems,
            boolean isData) {
        if (o == null || seen.put(o, Boolean.TRUE) != null) {
            return; // the same object listed twice is fine
        }
        Long id = idOf(o);
        String what = o.getClass().getSimpleName() + " " + o;
        if (id == null) {
            problems.add("no id: " + what);
            return;
        }
        if (isData && id >= IdAllocator.GAME_START) {
            problems.add("data id " + id + " not below " + IdAllocator.GAME_START + ": " + what);
        }
        if (!isData && id < IdAllocator.GAME_START) {
            problems.add("game id " + id + " below " + IdAllocator.GAME_START + ": " + what);
        }
        Object prev = byId.putIfAbsent(id, o);
        if (prev != null) {
            problems.add("id " + id + " shared by " + prev.getClass().getSimpleName() + " " + prev + " and " + what);
        }
    }

    private static Long idOf(Object o) {
        try {
            return (Long) o.getClass().getMethod("getId").invoke(o);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void leagueFoundersAndCupEntrantsComeFromSeasonData() {
        DatabaseLoader.resetDatabaseLoaderInstance();
        DatabaseLoader.getInstance();
        Season season = DatabaseLoader.getInstance().getSeasonByDataId("1888-89");

        BasicScript league = null;
        BasicScript cup = null;
        for (BasicScript script : season.getSeasonScripts()) {
            if (script.getScriptType() == BasicScript.LEAGUE_CREATION_SCRIPT) {
                league = script;
            } else if (script.getScriptType() == BasicScript.CUP_CREATION_SCRIPT) {
                cup = script;
            }
        }
        assertNotNull(league);
        assertNotNull(cup);

        @SuppressWarnings("unchecked")
        Array<Long> founders = (Array<Long>) league.getScriptValues().get(ScriptsLoader.LEAGUE_FOUNDING_TEAMS);
        assertEquals(12, founders.size);
        assertEquals(DatabaseLoader.getInstance().getClubByDataId("preston-north-end").getId(), founders.get(0));
        assertEquals(DatabaseLoader.getInstance().getClubByDataId("stoke").getId(), founders.get(11));
        assertEquals(DatabaseLoader.getInstance().getCompetitionByDataId("football-league").getId(),
                league.getScriptValues().get(ScriptsLoader.LEAGUE_COMPETITION));

        @SuppressWarnings("unchecked")
        Array<Long> entrants = (Array<Long>) cup.getScriptValues().get(ScriptsLoader.CUP_PARTICIPANTS);
        assertEquals(32, entrants.size);
        Set<Long> ids = new HashSet<>();
        for (Long id : entrants) {
            ids.add(id);
        }
        assertEquals(32, ids.size());
        // the data's list, not the old hard-coded 1..32: Small Heath and Heeley are in, Everton is not
        assertTrue(ids.contains(DatabaseLoader.getInstance().getClubByDataId("small-heath").getId()));
        assertTrue(ids.contains(DatabaseLoader.getInstance().getClubByDataId("heeley").getId()));
        assertFalse(ids.contains(DatabaseLoader.getInstance().getClubByDataId("everton").getId()));
    }

    @Test
    void personInSeveralSquadsIsCopiedAndLookupsReturnTheCanonicalOne() {
        DatabaseLoader.resetDatabaseLoaderInstance();
        DatabaseLoader.getInstance();

        Person canonical = DatabaseLoader.getInstance().getPersonByDataId("archie-goodall-1865");
        Player atPreston = playerOf(DatabaseLoader.getInstance().getClubByDataId("preston-north-end"), "archie-goodall-1865");
        Player atVilla = playerOf(DatabaseLoader.getInstance().getClubByDataId("aston-villa"), "archie-goodall-1865");

        assertSame(canonical, atPreston.getPerson());
        assertNotSame(canonical, atVilla.getPerson());
        assertEquals(canonical.getDataId(), atVilla.getPerson().getDataId());
        assertTrue(atVilla.getPerson().getId() < IdAllocator.GAME_START);
        assertTrue(DatabaseLoader.getInstance().getPersonCopies() > 0);
    }

    private static Player playerOf(Club club, String personDataId) {
        for (Player p : club.getPlayers()) {
            if (personDataId.equals(p.getPerson().getDataId())) {
                return p;
            }
        }
        throw new AssertionError(personDataId + " not at " + club);
    }

    @Test
    void newSaveGameStartsItsAllocatorAtGameStart() {
        SaveGame game = new SaveGame();
        assertEquals(IdAllocator.GAME_START, game.getIdAllocator().peek());
        Person p = new Person();
        game.assignId(p);
        assertEquals(Long.valueOf(IdAllocator.GAME_START), p.getId());
    }
}
