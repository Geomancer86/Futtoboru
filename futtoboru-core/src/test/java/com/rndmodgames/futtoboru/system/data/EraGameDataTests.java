package com.rndmodgames.futtoboru.system.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * G-11: GameData reads the era files of every pack (core baseline + events, mod events) into one EraProfile, the
 * shipped profile changes on each event's effective date and not the day before, and loading is deterministic.
 */
class EraGameDataTests {

    private static final String MOD_PACK = "{\"id\": \"testmod\", \"name\": \"Test mod\", \"version\": 1, "
            + "\"dataVersion\": 1, \"seasons\": [], \"files\": [\"era/events.json\"]}";
    private static final String MOD_EVENT = "[{\"id\": \"testmod-1890-nets-early\", \"kind\": \"equipment\", "
            + "\"authority\": \"press\", \"announced\": \"1890-06-01\", \"effective\": \"1890-09-01\", "
            + "\"effects\": [{\"param\": \"rules.goalNets\", \"op\": \"set\", \"value\": true}], "
            + "\"message\": {\"key\": \"era.testmod-1890-nets-early\"}, \"confidence\": \"unverified\", "
            + "\"sources\": [\"https://example.org/\"]}]";

    /** The shipped data plus a mod pack whose files are given (path inside the mod -> text). */
    private static GameData withMod(Map<String, String> modFiles) {
        GameData.Source base = new GameData.ClasspathSource("gamedata");
        return GameData.load(new GameData.Source() {
            @Override
            public String read(String path) {
                if (path.equals("index.txt")) {
                    return base.read(path).trim() + "\ntestmod\n";
                }
                if (path.startsWith("testmod/")) {
                    return modFiles.get(path.substring("testmod/".length()));
                }
                return base.read(path);
            }

            @Override
            public List<String> listJson(String folder) {
                return folder.equals("testmod") ? new ArrayList<>(modFiles.keySet()) : base.listJson(folder);
            }

            @Override
            public List<String> listFolders() {
                List<String> f = base.listFolders() == null ? null : new ArrayList<>(base.listFolders());
                if (f != null) {
                    f.add("testmod");
                }
                return f;
            }
        });
    }

    private static GameData shippedWith(String path, java.util.function.UnaryOperator<String> edit) {
        GameData.Source base = new GameData.ClasspathSource("gamedata");
        return GameData.load(new GameData.Source() {
            @Override
            public String read(String p) {
                String text = base.read(p);
                return text != null && p.equals(path) ? edit.apply(text) : text;
            }

            @Override
            public List<String> listJson(String folder) {
                return base.listJson(folder);
            }

            @Override
            public List<String> listFolders() {
                return base.listFolders();
            }
        });
    }

    private static List<String> ids(EraProfile p) {
        return p.events().stream().map(EraProfile.Event::id).collect(Collectors.toList());
    }

    @Test
    void shippedDataHasTheSameProfileAsTheCoreFiles() {
        GameData data = GameData.loadDefault();
        assertEquals("", data.getReport().errorsText());
        EraProfile fromData = data.era();
        assertNotNull(fromData);
        EraProfile fromFiles = EraProfile.loadDefault();
        assertEquals(ids(fromFiles), ids(fromData));
        for (LocalDate d = LocalDate.of(1888, 9, 8); d.getYear() < 1940; d = d.plusMonths(5)) {
            assertEquals(fromFiles.at(d).asMap(), fromData.at(d).asMap(), d.toString());
        }
    }

    @Test
    void loadingTwiceGivesTheSameProfile() {
        EraProfile a = GameData.loadDefault().era();
        EraProfile b = GameData.loadDefault().era();
        assertEquals(ids(a), ids(b));
        for (EraProfile.Event e : a.events()) {
            assertEquals(a.at(e.effective()).asMap(), b.at(e.effective()).asMap(), e.id());
            assertEquals(a.at(e.effective()).appliedEvents(), b.at(e.effective()).appliedEvents(), e.id());
        }
    }

    @Test
    void theProfileChangesOnEachEffectiveDateAndNotTheDayBefore() {
        EraProfile p = EraProfile.loadDefault();
        int checked = 0;
        for (EraProfile.Event e : p.events()) {
            if (e.effects().isEmpty() || !e.effective().isAfter(p.baselineDate())) {
                continue; // news only, or folded into the baseline
            }
            EraProfile.Resolved before = p.at(e.effective().minusDays(1));
            EraProfile.Resolved on = p.at(e.effective());
            assertFalse(before.appliedEvents().contains(e.id()), e.id() + " applied the day before");
            assertTrue(on.appliedEvents().contains(e.id()), e.id() + " not applied on its date");
            boolean changed = false;
            for (EraProfile.Effect f : e.effects()) {
                changed |= !before.asMap().get(f.param()).equals(on.asMap().get(f.param()));
                if (f.op() == EraProfile.Op.SET && f.adoptionSeasons() == 0 && !setAgainSameDay(p, e, f.param())) {
                    assertEquals(f.value(), on.asMap().get(f.param()), e.id() + " " + f.param());
                }
            }
            assertTrue(changed, e.id() + " changes nothing on " + e.effective());
            checked++;
        }
        assertTrue(checked >= 30, "events with effects: " + checked);
    }

    private static boolean setAgainSameDay(EraProfile p, EraProfile.Event e, String param) {
        return p.events().stream().anyMatch(o -> o != e && o.effective().equals(e.effective())
                && o.effects().stream().anyMatch(f -> f.param().equals(param)));
    }

    @Test
    void theFirstDecadeOfLawsIsInTheData() {
        EraProfile p = EraProfile.loadDefault();
        List<String> ids = ids(p);
        for (String id : new String[] { "league-1888-points-system", "ifab-1889-ball-weight",
                "ifab-1889-referee-free-kick", "press-1889-brodie-goal-nets", "ifab-1890-no-goal-from-goal-kick",
                "press-1891-first-goal-nets", "ifab-1891-penalty-kick", "ifab-1891-referee-linesmen",
                "league-1891-goal-nets", "league-1892-second-division", "ifab-1892-keeper-charge",
                "ifab-1893-referee-decision-final", "league-1893-retain-and-transfer", "ifab-1897-eleven-players",
                "press-1898-players-union", "league-1898-automatic-promotion", "ifab-1898-added-time" }) {
            assertTrue(ids.contains(id), id);
        }
        // penalties, nets and the referee on the field: from 1 September 1891, never in the first three seasons
        for (String param : new String[] { "rules.penaltyKick", "rules.goalNets", "rules.refereeActsWithoutAppeal" }) {
            assertFalse(p.at(LocalDate.of(1891, 8, 31)).getBoolean(param), param);
            assertTrue(p.at(LocalDate.of(1891, 9, 1)).getBoolean(param), param);
        }
        // the points system announced in the first season changes nothing: 2-1, goal average from the start
        EraProfile.Resolved start = p.at(p.baselineDate());
        EraProfile.Resolved after = p.at(LocalDate.of(1888, 11, 22));
        assertEquals(start.asMap(), after.asMap());
        assertEquals(2, after.getInt("league.pointsWin"));
        assertEquals("GOAL_AVERAGE", after.getString("league.tieBreak"));
    }

    @Test
    void aModAddsEventsToTheCoreProfile() {
        GameData data = withMod(Map.of("pack.json", MOD_PACK, "era/events.json", MOD_EVENT));
        assertFalse(data.getReport().errorsText().contains("era"), data.getReport().errorsText());
        EraProfile p = data.era();
        assertNotNull(p);
        assertTrue(ids(p).contains("testmod-1890-nets-early"));
        assertTrue(ids(p).containsAll(ids(EraProfile.loadDefault())));
        assertTrue(p.at(LocalDate.of(1890, 9, 1)).getBoolean("rules.goalNets"));
        assertFalse(EraProfile.loadDefault().at(LocalDate.of(1890, 9, 1)).getBoolean("rules.goalNets"));
    }

    @Test
    void aSecondBaselineOrABadEventIsALoadError() {
        String coreBaseline = new GameData.ClasspathSource("gamedata").read("core/era/baseline.json");
        GameData twoBaselines = withMod(Map.of("pack.json", MOD_PACK.replace("\"era/events.json\"",
                "\"era/baseline.json\""), "era/baseline.json", coreBaseline));
        assertTrue(twoBaselines.getReport().errorsText().contains("a second era baseline"),
                twoBaselines.getReport().errorsText());
        assertNotNull(twoBaselines.era()); // the core one still holds

        GameData bad = shippedWith("core/era/events.json",
                t -> t.replace("\"param\": \"rules.penaltyKick\"", "\"param\": \"rules.penaltyKik\""));
        assertTrue(bad.getReport().errorsText().contains("unknown parameter `rules.penaltyKik`"),
                bad.getReport().errorsText());
        assertNull(bad.era());
    }
}
