package com.rndmodgames.futtoboru.system.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * GameData (P2-4): the same rules as tools/data/validate.py, on the shipped data and on small fixture packs
 * under src/test/resources/gamedata-fixtures. No libGDX application is needed.
 */
class GameDataTests {

    private static GameData fixture(String name) {
        return GameData.load(new GameData.ClasspathSource("gamedata-fixtures/" + name));
    }

    private static void assertError(GameData data, String fragment) {
        String errors = data.getReport().errorsText();
        assertTrue(errors.contains(fragment), "expected an error with `" + fragment + "`, got:\n" + errors);
    }

    @Test
    void shippedDataLoadsWithZeroErrors() {
        GameData data = GameData.loadDefault();

        assertEquals("", data.getReport().errorsText());
        assertEquals(List.of("core"), data.getPacks());
        assertEquals(35, data.all(GameData.CLUB).size());
        assertEquals(542, data.all(GameData.PERSON).size()); // H-01b data fixes (539 after slice 4), see REVIEW_1888-89_FINDINGS
        assertEquals(35, data.records("squad", "1888-89").size());
        assertEquals(2, data.get(GameData.SEASON, "1888-89").children().size());
        // the same warnings validate.py gives (copied rosters, short squad, placeholders...)
        assertFalse(data.getReport().getWarnings().isEmpty());
    }

    @Test
    void validFixtureLoadsAndResolvesReferences() {
        GameData data = fixture("valid");

        assertEquals("", data.getReport().errorsText());
        DataRecord preston = data.get(GameData.CLUB, "preston-north-end");
        assertNotNull(preston);
        assertEquals("england", preston.ref("country"));
        assertEquals(Integer.valueOf(1880), preston.integer("founded"));
        // ids are compared ignoring case
        assertNotNull(data.get(GameData.CLUB, "Preston-North-End"));
        assertNull(data.get(GameData.CLUB, "everton"));
    }

    @Test
    void unknownFieldIsAnErrorAndTheRecordIsSkipped() {
        GameData data = fixture("unknown-field");

        assertError(data, "unknown field `fundation`");
        assertNull(data.get(GameData.CLUB, "preston-north-end"));
        assertNotNull(data.get(GameData.CLUB, "queens-park"));
    }

    @Test
    void danglingReferenceIsAnErrorAndTheRecordIsSkipped() {
        GameData data = fixture("dangling");

        assertError(data, "unknown country `atlantis`");
        assertNull(data.get(GameData.CLUB, "preston-north-end"));
        assertEquals(1, data.all(GameData.CLUB).size());
    }

    @Test
    void duplicateIdIgnoringCaseIsAnError() {
        GameData data = fixture("duplicate");

        assertError(data, "duplicate club id `PRESTON-NORTH-END`");
        // the first record keeps the id
        assertEquals("preston-north-end", data.get(GameData.CLUB, "preston-north-end").id());
        assertEquals(2, data.all(GameData.CLUB).size());
    }

    @Test
    void aliasesResolveRenamedIds() {
        GameData data = fixture("alias");

        assertEquals("", data.getReport().errorsText());
        // a club refers to `england`, renamed to `england-and-wales`
        DataRecord club = data.get(GameData.CLUB, "preston-north-end-fc");
        assertEquals("england-and-wales", club.ref("country"));
        // a lookup by the old id finds the renamed record
        assertEquals(club, data.get(GameData.CLUB, "preston-north-end"));
        assertEquals("preston-north-end-fc", data.resolve(GameData.CLUB, "PRESTON-NORTH-END"));
    }

    @Test
    void unlistedFileIsAnError() {
        assertError(fixture("unlisted"), "`extra.json` exists but is not listed in `files`");
    }

    @Test
    void listedButMissingFileIsAnError() {
        GameData.ClasspathSource valid = new GameData.ClasspathSource("gamedata-fixtures/valid");
        GameData data = GameData.load(new GameData.Source() {
            @Override
            public String read(String path) {
                return path.equals("core/clubs.json") ? null : valid.read(path);
            }

            @Override
            public List<String> listJson(String folder) {
                return null;
            }

            @Override
            public List<String> listFolders() {
                return null;
            }
        });

        assertError(data, "core/clubs.json: file listed but missing");
        assertTrue(data.all(GameData.CLUB).isEmpty());
        assertEquals(2, data.all(GameData.COUNTRY).size());
    }

    @Test
    void badIdNumberAndDateAreErrors() {
        GameData data = fixture("bad-values");

        assertError(data, "bad id `Preston North End`");
        assertError(data, "founded: expected a whole number");
        assertError(data, "born: expected a date yyyy, yyyy-mm or yyyy-mm-dd");
        assertTrue(data.all(GameData.CLUB).isEmpty());
        assertTrue(data.all(GameData.PERSON).isEmpty());
    }

    @Test
    void missingIndexIsReportedNotThrown() {
        GameData data = GameData.load(new GameData.ClasspathSource("no-such-folder"));

        assertError(data, "index.txt: missing");
        assertTrue(data.all(GameData.CLUB).isEmpty());
    }

    @Test
    void dataRuntimeIdsAreUniqueDeterministicAndBelowGameIds() {
        GameData first = GameData.loadDefault();
        GameData second = GameData.loadDefault();

        Set<Long> seen = new HashSet<>();
        for (String kind : GameData.KINDS) {
            for (DataRecord r : first.all(kind)) {
                assertTrue(r.runtimeId() >= IdAllocator.DATA_START && r.runtimeId() < IdAllocator.GAME_START, r.where());
                assertTrue(seen.add(r.runtimeId()), "runtime id used twice: " + r.where());
                assertEquals(r.runtimeId(), second.get(kind, r.id()).runtimeId(), "same id on every load: " + r.where());
            }
        }
        assertTrue(first.getDataIdAllocator().peek() > seen.size());
    }

    @Test
    void shippedRuntimeIdsComeFromTheRegistry() {
        GameData data = GameData.loadDefault();

        for (String warning : data.getReport().getWarnings()) {
            assertFalse(warning.contains("ids.json"), warning);
        }
        // the registry kept the ids the load order gave before it existed (P2-5)
        assertEquals(1L, data.get(GameData.CLUB, "preston-north-end").runtimeId());
        assertEquals(40L, data.get(GameData.CONTINENT, "europe").runtimeId());
        assertEquals(914L, data.get(GameData.EDITION, "fa-cup-1888-89").runtimeId());
        for (String kind : GameData.KINDS) {
            for (DataRecord r : data.all(kind)) {
                assertTrue(r.runtimeId() <= IdAllocator.REGISTRY_MAX, r.where());
                assertEquals(Long.valueOf(r.runtimeId()), data.numberOf(kind, r.id()));
            }
        }
        assertEquals(IdAllocator.DERIVED_START, data.getDataIdAllocator().peek());
        assertEquals(1, data.getDataVersion());
    }

    @Test
    void removedAddedOrRenamedRecordsDoNotShiftOtherRuntimeIds() {
        GameData before = fixture("registry");
        GameData after = fixture("registry-changed");

        assertEquals("", before.getReport().errorsText());
        assertEquals("", after.getReport().errorsText());
        assertTrue(after.getReport().getWarnings().isEmpty(), after.getReport().getWarnings().toString());

        // queens-park removed (retired), vale-of-leven added first in the file, preston renamed with an alias
        assertEquals(before.get(GameData.CLUB, "renton").runtimeId(), after.get(GameData.CLUB, "renton").runtimeId());
        assertEquals(before.get(GameData.COUNTRY, "scotland").runtimeId(),
                after.get(GameData.COUNTRY, "scotland").runtimeId());
        assertEquals(before.get(GameData.CLUB, "preston-north-end").runtimeId(),
                after.get(GameData.CLUB, "preston-north-end-fc").runtimeId());
        assertEquals(Long.valueOf(1), after.numberOf(GameData.CLUB, "preston-north-end"));
        assertEquals(7L, after.get(GameData.CLUB, "vale-of-leven").runtimeId());
        assertNull(after.numberOf(GameData.CLUB, "queens-park"));
    }

    @Test
    void aPackWithoutRegistryGetsTemporaryIdsAndAWarning() {
        GameData data = fixture("valid");

        assertTrue(data.getReport().getWarnings().toString().contains("core/ids.json: missing"));
        assertTrue(data.get(GameData.CLUB, "preston-north-end").runtimeId() >= IdAllocator.DERIVED_START);
    }

    @Test
    void partialBirthDatesKeepWhatIsKnown() {
        Random rng = new Random(42);
        for (int i = 0; i < 200; i++) {
            LocalDateTime yearOnly = BirthDates.complete("1865", rng);
            assertEquals(1865, yearOnly.getYear());

            LocalDateTime yearMonth = BirthDates.complete("1866-02", rng);
            assertEquals(1866, yearMonth.getYear());
            assertEquals(2, yearMonth.getMonthValue());
            assertTrue(yearMonth.getDayOfMonth() <= 28);
        }
        assertEquals(LocalDateTime.of(1863, 12, 16, 0, 0), BirthDates.complete("1863-12-16", rng));
        // the same seed gives the same dates
        assertEquals(BirthDates.complete("1865", new Random(7)), BirthDates.complete("1865", new Random(7)));
    }

    @Test
    void idAllocatorNeverRepeats() {
        IdAllocator ids = new IdAllocator();
        assertEquals(IdAllocator.GAME_START, ids.nextId());
        assertEquals(IdAllocator.GAME_START + 1, ids.nextId());
        ids.reserveUpTo(IdAllocator.GAME_START + 10);
        assertEquals(IdAllocator.GAME_START + 11, ids.nextId());
    }
}
