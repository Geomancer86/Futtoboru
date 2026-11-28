package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;

/**
 * Saves v1 (P2-5): save -> load gives the same game, one object per club/person/competition after a load, staff keys
 * as Longs, old/newer/damaged files reported (never thrown), unresolved references reported, the committed v1
 * fixture save loads. Every file goes to a temporary folder, never to the player's Documents.
 */
class SaveGameTests {

    /** Run with -Dfuttoboru.writeSaveFixture=true to write the fixture save again (only when the format changes). */
    static final String FIXTURE = "saves/v1_1888-89_new_game.json";

    private static Futtoboru application;

    @TempDir
    static Path folder;

    @BeforeAll
    static void preload() {
        SaveLoadSystem.setGameFolder(folder.toFile());
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        new HeadlessApplication(application, config);
        BaseTest.awaitHeadlessCreate();
    }

    @AfterAll
    static void restore() {
        SaveLoadSystem.setGameFolder(null);
    }

    /**
     * A new game played 170 days (1 Apr to mid-September 1888): the league and the cup are created and drawn, and
     * the first league match days of the era calendar (P2-11: from 8 September) are played.
     */
    private static SaveGame playedGame() {
        SaveGame game = NewGameFixture.newGame(application);
        NewGameFixture.play(application, game, 170, true);
        return game;
    }

    private static LoadResult saveAndLoad(SaveGame game, String name) throws IOException {
        File file = SaveLoadSystem.saveGame(game, name);
        LoadResult result = SaveLoadSystem.loadGame(file);
        assertTrue(result.isLoaded(), result.toString());
        return result;
    }

    @Test
    void saveThenLoadGivesTheSameGame() throws IOException {
        SaveGame game = playedGame();
        int played = 0;
        for (Club c : game.getAllClubs()) {
            played += c.getPlayedMatches().size();
        }
        assertTrue(played > 0, "some matches should have been played");
        GameFingerprint before = GameFingerprint.of(game);

        LoadResult result = saveAndLoad(game, "Round trip");

        assertEquals(new ArrayList<String>(), result.getProblems());
        assertEquals(before.toString(), GameFingerprint.of(result.getGame()).toString());
        assertEquals(before, GameFingerprint.of(result.getGame()));
        assertEquals("Round trip", result.getGame().getSaveName());

        assertTrue(before.values().get("matches.played").length() > 20, before.values().get("matches.played"));
        assertTrue(before.values().size() > 100);
        assertNotNull(game.getSeed());

        // saving the loaded game again writes the same file
        String again = SaveCodec.encode(result.getGame(), "Round trip", LocalDateTime.of(2000, 1, 1, 0, 0));
        String first = SaveCodec.encode(game, "Round trip", LocalDateTime.of(2000, 1, 1, 0, 0));
        assertEquals(first, again);

        // and the loaded game plays on
        NewGameFixture.bind(application, result.getGame());
        NewGameFixture.play(application, result.getGame(), 3, true);
    }

    @Test
    void afterALoadEveryClubPersonAndCompetitionIsOneObject() throws IOException {
        SaveGame game = saveAndLoad(playedGame(), "Identity").getGame();

        // clubs: the game's, the league's, the fixtures' and the database lookups are the same instance
        Club preston = game.getCurrentClub();
        assertNotNull(preston);
        assertSame(preston, DatabaseLoader.getClubById(preston.getId()));
        assertSame(preston, DatabaseLoader.getClubByDataId("preston-north-end"));
        League league = game.getMainAuthority().getLeagues().get(0);
        assertSame(game.getMainAuthority(), DatabaseLoader.getMainAuthority());
        for (Club c : league.getLeagueClubs()) {
            assertSame(c, game.getClubById(c.getId()));
            assertSame(c, DatabaseLoader.getClubById(c.getId()));
            assertSame(c, DatabaseLoader.getClubByDataId(c.getDataId()));
        }
        Map<Long, Club> byId = new HashMap<>();
        Map<Club, Boolean> clubs = new IdentityHashMap<>();
        List<Club> everywhere = new ArrayList<>(game.getAllClubs());
        everywhere.addAll(league.getLeagueClubs());
        everywhere.addAll(DatabaseLoader.getClubs());
        for (Club c : everywhere) {
            Club prev = byId.putIfAbsent(c.getId(), c);
            assertTrue(prev == null || prev == c, "two objects for club " + c.getId());
            clubs.put(c, Boolean.TRUE);
        }

        // a fixture is one object, listed by both clubs, and its clubs resolve to the game's
        int fixtures = 0;
        for (Club c : game.getAllClubs()) {
            for (Match m : c.getScheduledMatches()) {
                if (m.getHomeClubId() == null || m.getAwayClubId() == null) {
                    continue; // a cup tie not drawn yet
                }
                Club home = DatabaseLoader.getClubById(m.getHomeClubId());
                Club away = DatabaseLoader.getClubById(m.getAwayClubId());
                assertTrue(clubs.containsKey(home) && clubs.containsKey(away), "match " + m.getId() + ": "
                        + m.getHomeClubId() + " " + home + " - " + m.getAwayClubId() + " " + away);
                Club other = home == c ? away : home;
                assertTrue(other.getScheduledMatches().stream().anyMatch(x -> x == m),
                        "match " + m.getId() + " should be the same object at both clubs");
                fixtures++;
            }
        }
        assertTrue(fixtures > 0);

        // editions: the league's and its competition's are the same object
        Competition footballLeague = DatabaseLoader.getCompetitionByDataId("football-league");
        assertTrue(game.getAllLeagues().stream().anyMatch(x -> x == footballLeague));
        for (CompetitionEdition e : league.getEditions()) {
            assertTrue(footballLeague.getEditions().stream().anyMatch(x -> x == e));
        }

        // data is never copied: countries, professions and occupations are the database's objects
        for (Club c : game.getAllClubs()) {
            assertSame(DatabaseLoader.getCountryByDataId(c.getCountry().getDataId()), c.getCountry());
            for (Player p : c.getPlayers()) {
                Person person = p.getPerson();
                assertSame(DatabaseLoader.getCountryByDataId(person.getCountry().getDataId()), person.getCountry());
                if (p.getPlayerProfession() != null) {
                    assertSame(DatabaseLoader.getPlayerProfessionByDataId(p.getPlayerProfession().getDataId()),
                            p.getPlayerProfession());
                }
                if (person.getDataId() != null && person.getId().equals(DatabaseLoader.getPersonByDataId(person.getDataId()).getId())) {
                    assertSame(person, DatabaseLoader.getPersonByDataId(person.getDataId()));
                }
            }
            if (c.getStadium() != null && c.getStadium().getDataId() != null) {
                assertNotNull(c.getStadium().getName());
            }
        }
        assertSame(DatabaseLoader.getProfessionByDataId(Profession.MANAGER), game.getOwner().getPrimaryProfession());
    }

    @Test
    void staffKeysComeBackAsLongs() throws IOException {
        SaveGame game = NewGameFixture.newGame(application);
        Club preston = game.getCurrentClub();
        Long manager = DatabaseLoader.getProfessionByDataId(Profession.MANAGER).getId();
        preston.setStaffId(manager, game.getOwner().getId());

        Club loaded = saveAndLoad(game, "Staff").getGame().getCurrentClub();

        assertFalse(loaded.getStaff().isEmpty());
        for (Object key : loaded.getStaff().keySet()) {
            assertTrue(key instanceof Long, "key " + key + " is a " + key.getClass().getSimpleName());
        }
        assertEquals(game.getOwner().getId(), loaded.getStaffId(manager));
    }

    @Test
    void oldNewerAndDamagedFilesAreReportedNotThrown() throws IOException {
        File saves = SaveLoadSystem.getSavesFolder();
        Files.createDirectories(saves.toPath());
        // a v0.4 save: the whole SaveGame graph in libGDX minimal JSON, no saveVersion
        File old = write(saves, "savegame.json", "{owner:{id:1,name:Old,country:{id:44,commonName:England}},allClubs:[]}");
        File newer = write(saves, "newer.json", "{saveVersion:99,summary:{name:Future}}");
        File damaged = write(saves, "damaged.json", "{ this is not json");

        assertEquals(LoadResult.Status.OLDER_VERSION, SaveLoadSystem.loadGame(old).getStatus());
        assertEquals(SaveCodec.OLDER_VERSION_MESSAGE, SaveLoadSystem.loadGame(old).getMessage());
        assertEquals(LoadResult.Status.NEWER_VERSION, SaveLoadSystem.loadGame(newer).getStatus());
        assertEquals(LoadResult.Status.UNREADABLE, SaveLoadSystem.loadGame(damaged).getStatus());
        assertEquals(LoadResult.Status.UNREADABLE, SaveLoadSystem.loadGame(new File(saves, "missing.json")).getStatus());

        Map<String, LoadResult.Status> listed = new HashMap<>();
        for (SaveSummary s : SaveLoadSystem.listSaves()) {
            listed.put(s.getFile().getName(), s.getStatus());
        }
        assertEquals(LoadResult.Status.OLDER_VERSION, listed.get("savegame.json"));
        assertEquals(LoadResult.Status.NEWER_VERSION, listed.get("newer.json"));
        assertEquals(LoadResult.Status.UNREADABLE, listed.get("damaged.json"));
        Files.delete(old.toPath());
        Files.delete(newer.toPath());
        Files.delete(damaged.toPath());
    }

    @Test
    void unresolvedReferencesAreReportedAndLeftOut() {
        SaveGame game = NewGameFixture.newGame(application);
        String text = SaveCodec.encode(game, "Broken", LocalDateTime.now());

        // a country removed from the data, a club that isn't in the save, a fixture against a missing club
        String broken = text.replaceFirst("\"country\": \"england\"", "\"country\": \"atlantis\"")
                .replaceFirst("\"allClubs\": \\[", "\"allClubs\": [ 424242,")
                .replaceFirst("\"participantClubsIds\": \\[", "\"participantClubsIds\": [ 424243,");
        assertFalse(broken.equals(text));

        LoadResult result = SaveCodec.decode(broken);

        assertTrue(result.isLoaded(), result.toString());
        String problems = String.join("\n", result.getProblems());
        assertTrue(problems.contains("atlantis"), problems);
        assertTrue(problems.contains("424242"), problems);
        assertTrue(problems.contains("424243"), problems);
        assertFalse(result.getGame().getAllClubs().contains(null));
    }

    @Test
    void slotsAreNamedFilesListedNewestFirst() throws IOException {
        SaveGame game = NewGameFixture.newGame(application);
        assertEquals("My_save_1888.json", SaveLoadSystem.fileNameFor("  My save: 1888! "));
        assertEquals("save.json", SaveLoadSystem.fileNameFor("..."));
        assertEquals("save_con.json", SaveLoadSystem.fileNameFor("con"));

        File a = SaveLoadSystem.saveGame(game, "Slot A");
        File b = SaveLoadSystem.saveGame(game, "Slot B");
        assertTrue(a.isFile() && b.isFile() && !a.equals(b));
        assertTrue(b.lastModified() >= a.lastModified());
        // overwriting a slot keeps one file
        File again = SaveLoadSystem.saveGame(game, "Slot A");
        assertEquals(a, again);

        List<SaveSummary> saves = SaveLoadSystem.listSaves();
        SaveSummary newest = saves.get(0);
        assertEquals("Slot A", newest.getName());
        assertEquals("Test Manager", newest.getManager());
        assertEquals("Preston North End", newest.getClub());
        assertEquals(game.getGameDate(), newest.getGameDate());
        assertTrue(newest.isLoadable());
    }

    @Test
    void theOldFolderIsMovedWithoutOverwriting(@TempDir Path home) throws IOException {
        File old = home.resolve("Futtuboru").toFile();
        File target = home.resolve("Futtoboru").toFile();
        write(new File(old, "managers"), "smith.json", "{name:Smith}");
        write(new File(old, "savegames"), "savegame.json", "{old:1}");
        write(new File(target, "savegames"), "savegame.json", "{new:1}");

        assertEquals(1, SaveLoadSystem.moveOldFolder(old, target));

        assertTrue(new File(target, "managers/smith.json").isFile());
        assertEquals("{new:1}", read(new File(target, "savegames/savegame.json")));
        assertTrue(new File(old, "savegames/savegame.json").isFile(), "a file that would be overwritten stays");
        assertFalse(new File(old, "managers").exists(), "emptied folders are removed");
        assertEquals(0, SaveLoadSystem.moveOldFolder(old, target));
    }

    @Test
    void theV1FixtureSaveLoads() throws IOException {
        if (Boolean.getBoolean("futtoboru.writeSaveFixture")) {
            SaveGame game = NewGameFixture.newGame(application);
            Path out = Paths.get("src/test/resources", FIXTURE);
            Files.createDirectories(out.getParent());
            Files.write(out, SaveCodec.encode(game, "1888-89 new game", LocalDateTime.of(2026, 11, 27, 12, 0))
                    .getBytes(StandardCharsets.UTF_8));
        }
        String text;
        try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(FIXTURE)) {
            assertNotNull(in, FIXTURE + " is missing");
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        LoadResult result = SaveCodec.decode(text);

        assertTrue(result.isLoaded(), result.toString());
        // later data corrections may leave references unresolved: they are reported, the save still loads
        result.getProblems().forEach(p -> System.out.println("fixture save: " + p));
        SaveGame game = result.getGame();
        assertEquals("Test", game.getOwner().getName());
        assertEquals(LocalDateTime.of(1888, 4, 1, 4, 0), game.getGameDate());
        assertSame(game.getCurrentClub(), DatabaseLoader.getClubByDataId("preston-north-end"));
        assertFalse(game.getAllClubs().isEmpty());

        // v1 -> v2 (E-01): positions from the squad data, dribbling sourced or derived from the saved attributes
        int players = 0;
        int withoutPosition = 0;
        for (Club club : game.getAllClubs()) {
            for (Player p : club.getPlayers()) {
                assertNotNull(p.getDribbling(), p.getPerson().getDataId());
                // a person retired or moved to another club in the data since the fixture was written has no
                // entry in that club's squad, so no position
                withoutPosition += p.getPosition() == null ? 1 : 0;
                players++;
            }
        }
        assertTrue(players > 500, "players: " + players);
        assertTrue(withoutPosition < players / 20, withoutPosition + " of " + players + " without a position");
        Player southworth = player(game, "blackburn-rovers", "jack-southworth-1866");
        assertEquals(com.rndmodgames.futtoboru.data.Position.CENTRE_FORWARD, southworth.getPosition());
        assertEquals(18f, southworth.getDribbling());
        Player dewhurst = player(game, "preston-north-end", "fred-dewhurst-1863");
        assertEquals(PlayerAttributeGenerator.deriveDribbling(dewhurst.getDexterity(), dewhurst.getAcceleration(),
                dewhurst.getTrickShots(), dewhurst.getSpeed()), dewhurst.getDribbling());
    }

    @Test
    void positionAndDribblingSurviveSaveAndLoad() throws IOException {
        SaveGame game = NewGameFixture.newGame(application);
        Player southworth = player(game, "blackburn-rovers", "jack-southworth-1866");
        assertEquals(com.rndmodgames.futtoboru.data.Position.CENTRE_FORWARD, southworth.getPosition());
        assertEquals(18f, southworth.getDribbling());
        Map<Long, String> before = new HashMap<>();
        for (Club club : game.getAllClubs()) {
            for (Player p : club.getPlayers()) {
                assertNotNull(p.getDribbling());
                assertTrue(p.getDribbling() >= 3 && p.getDribbling() <= 23, String.valueOf(p.getDribbling()));
                before.put(p.getId(), p.getPosition() + " " + p.getDribbling());
            }
        }
        String text = SaveCodec.encode(game, "E-01", LocalDateTime.of(2000, 1, 1, 0, 0));
        assertTrue(text.contains("\"saveVersion\":" + SaveCodec.SAVE_VERSION)
                || text.contains("\"saveVersion\": " + SaveCodec.SAVE_VERSION));

        LoadResult result = SaveCodec.decode(text);
        assertTrue(result.isLoaded(), result.toString());
        Map<Long, String> after = new HashMap<>();
        for (Club club : result.getGame().getAllClubs()) {
            for (Player p : club.getPlayers()) {
                after.put(p.getId(), p.getPosition() + " " + p.getDribbling());
            }
        }
        assertEquals(before, after);
    }

    private static Player player(SaveGame game, String club, String person) {
        for (Club c : game.getAllClubs()) {
            if (club.equals(c.getDataId())) {
                for (Player p : c.getPlayers()) {
                    if (person.equals(p.getPerson().getDataId())) {
                        return p;
                    }
                }
            }
        }
        throw new AssertionError(person + " not at " + club);
    }

    /**
     * P2-9: the creation scripts of a v1 save (made before the scripts carried the edition id) open the season's
     * data editions from season.json instead of adding a second 1888-89 edition next to them.
     */
    @Test
    void theV1FixtureScriptsOpenTheDataEditions() throws IOException {
        String text;
        try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(FIXTURE)) {
            assertNotNull(in, FIXTURE + " is missing");
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertFalse(text.contains("CUP_EDITION") || text.contains("LEAGUE_EDITION"), "a save made before P2-9");
        SaveGame game = SaveCodec.decode(text).getGame();
        NewGameFixture.bind(application, game);
        NewGameFixture.play(application, game, 45, true); // past both scripts (17 April, 1 May)

        League league = game.getMainAuthority().getLeagues().get(0);
        assertEquals(1, league.getEditions().size(), "one league edition: " + league.getEditions());
        CompetitionEdition leagueEdition = league.getEditions().get(0);
        assertEquals("football-league-1888-89", leagueEdition.getDataId());
        assertNotNull(leagueEdition.getStartDate(), "the league script opened the data edition");
        assertEquals(12, leagueEdition.getParticipantClubsIds().size(), "participants not added twice");

        int cups = 0;
        for (Competition c : game.getAllCups()) {
            if (!"fa-cup".equals(c.getDataId())) {
                continue;
            }
            cups++;
            assertEquals(1, c.getEditions().size(), "one FA Cup edition");
            CompetitionEdition cupEdition = c.getEditions().get(0);
            assertEquals("fa-cup-1888-89", cupEdition.getDataId());
            assertNotNull(cupEdition.getStartDate(), "the cup script opened the data edition");
            assertEquals(32, cupEdition.getParticipantClubsIds().size(), "participants not added twice");
        }
        assertEquals(1, cups);
        for (Competition c : game.getAllLeagues()) {
            if ("football-league".equals(c.getDataId())) {
                assertEquals(1, c.getEditions().size(), "one Football League edition");
            }
        }
    }

    private static File write(File dir, String name, String text) throws IOException {
        Files.createDirectories(dir.toPath());
        File f = new File(dir, name);
        Files.write(f.toPath(), text.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    private static String read(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
}
