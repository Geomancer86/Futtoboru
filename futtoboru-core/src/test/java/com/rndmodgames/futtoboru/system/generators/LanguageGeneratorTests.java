package com.rndmodgames.futtoboru.system.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.language.LanguageSkill;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.data.LanguageData;
import com.rndmodgames.futtoboru.system.save.SaveMigrations;
import com.rndmodgames.futtoboru.tables.player.PlayerDetailScreenTable;

/**
 * R-11a: languages come only from upbringing, on the person's own fork: deterministic per seed and person, at the
 * sourced rates over many men, and the v2 -> v3 save step gives what a new game gives.
 */
class LanguageGeneratorTests {

    private static final GameData DATA = GameData.loadDefault();
    private static final LanguageData LANG = LanguageData.from(DATA);
    private static final int MEN = 4000;

    private static LanguageGenerator.Result man(String country, String area, int year, int i) {
        return LanguageGenerator.generate(LANG, country, area, year, 0, RandomSource.seeded(1888).fork("lang:t" + i));
    }

    @Test
    void sameSeedAndPersonGiveTheSameLanguages() {
        Person a = person("fred-dewhurst-1863", "england", 1863);
        Person b = person("fred-dewhurst-1863", "england", 1863);
        LanguageGenerator.assign(a, LANG, DATA, RandomSource.seeded(42).fork("database"));
        LanguageGenerator.assign(b, LANG, DATA, RandomSource.seeded(42).fork("database"));
        assertEquals(a.getLanguages(), b.getLanguages());
        assertEquals(a.getBirthArea(), b.getBirthArea());
        assertEquals("english", a.getHomeLanguage());
        assertEquals("english", a.getLanguages().get(0).getLanguage());
        assertEquals(LanguageSkill.MAX, a.getLanguages().get(0).getSpeak());
        assertEquals("lang:fred-dewhurst-1863", LanguageGenerator.streamName(a));
    }

    @Test
    void sourcedRatesComeBackOverManyMen() {
        int welshHome = 0;
        int welshOnly = 0;
        int englishSigns = 0;
        int gaelic = 0;
        int irishParents = 0;
        for (int i = 0; i < MEN; i++) {
            LanguageGenerator.Result w = man("wales", "wales-west", 1865, i);
            if ("welsh".equals(w.homeLanguage())) {
                welshHome++;
                welshOnly += skill(w, "english").getSpeak() < 2 ? 1 : 0;
            }
            LanguageGenerator.Result e = man("england", "lancashire", 1865, i);
            englishSigns += skill(e, "english").getReadWrite() > 0 ? 1 : 0;
            irishParents += "ireland".equals(man("england", "liverpool", 1865, i).parentsOrigin()) ? 1 : 0;
            gaelic += man("scotland", "scotland-lowlands", 1865, i).homeLanguage().equals("scottish-gaelic") ? 1 : 0;
        }
        assertEquals(0.93, welshHome / (double) MEN, 0.02);
        assertEquals(0.6 / 0.93, welshOnly / (double) welshHome, 0.03);
        assertEquals(0.916, englishSigns / (double) MEN, 0.015);
        assertEquals(0.091, irishParents / (double) MEN, 0.015);
        assertTrue(gaelic < MEN * 0.03, "Gaelic at home in the football counties is rare: " + gaelic);
    }

    @Test
    void areaIsDrawnFromTheWeightsAndAForeignManKeepsHisLanguage() {
        int lancashire = 0;
        for (int i = 0; i < MEN; i++) {
            LanguageGenerator.Result r = man("england", null, 1865, i);
            assertNotNull(r.birthArea());
            lancashire += "lancashire".equals(r.birthArea()) ? 1 : 0;
        }
        assertEquals(0.12, lancashire / (double) MEN, 0.02);

        LanguageGenerator.Result german = man("germany", null, 1884, 1);
        assertNull(german.birthArea());
        assertEquals("german", german.homeLanguage());
        assertTrue(skill(german, "english").getSpeak() <= 1);
    }

    @Test
    void anOriginMovesLiteracyByOneLevel() {
        for (int i = 0; i < 200; i++) {
            RandomSource rng = RandomSource.seeded(7).fork("lang:o" + i);
            int plain = skill(LanguageGenerator.generate(LANG, "england", "england-other", 1865, 0, rng), "english")
                    .getReadWrite();
            int clerk = skill(LanguageGenerator.generate(LANG, "england", "england-other", 1865, 1,
                    RandomSource.seeded(7).fork("lang:o" + i)), "english").getReadWrite();
            assertEquals(Math.min(LanguageSkill.MAX, plain + 1), clerk);
        }
    }

    @Test
    void theV2ToV3SaveStepGivesWhatANewGameGives() {
        long seed = 1888L;
        JsonValue root = new JsonReader().parse("{saveVersion:2,game:{seed:" + seed + "},persons:["
                + "{id:42,dataId:fred-dewhurst-1863,name:Fred,lastname:Dewhurst,birthDate:\"1863-12-16T00:00\","
                + "country:england},{id:1000001,name:Test,lastname:Manager}]}");
        JsonValue migrated = SaveMigrations.addLanguages(root, LANG, DATA);
        JsonValue fred = migrated.get("persons").child;

        Person expected = person("fred-dewhurst-1863", "england", 1863);
        expected.setBirthDate(LocalDateTime.of(1863, 12, 16, 0, 0));
        LanguageGenerator.assign(expected, LANG, DATA, RandomSource.seeded(seed).fork("database"));
        assertEquals(expected.getHomeLanguage(), fred.getString("homeLanguage"));
        assertEquals(expected.getBirthArea(), fred.getString("birthArea"));
        JsonValue first = fred.get("languages").child;
        LanguageSkill s = expected.getLanguages().get(0);
        assertEquals(s.getLanguage(), first.getString("language"));
        assertEquals(s.getReadWrite(), first.getInt("readWrite"));
        assertEquals(expected.getLanguages().size(), fred.get("languages").size);
        // a person without a country or birth date still gets languages (England, the default year)
        assertTrue(fred.next.get("languages").size > 0);
    }

    @Test
    void differentPeopleDrawFromDifferentStreams() {
        int differ = 0;
        for (int i = 0; i < 50; i++) {
            LanguageGenerator.Result a = man("wales", null, 1865, i);
            LanguageGenerator.Result b = man("wales", null, 1865, i + 1000);
            differ += a.equals(b) ? 0 : 1;
        }
        assertTrue(differ > 10);
        assertNotEquals(LanguageGenerator.streamName(person("a-1860", "england", 1860)),
                LanguageGenerator.streamName(person("b-1860", "england", 1860)));
    }

    @Test
    void everyLanguageAndLevelHasItsWordsInAllFourBundles() throws IOException {
        for (String bundle : List.of("BaseBundle_en_us", "BaseBundle_en_uk", "BaseBundle_sp_sp", "BaseBundle_sp_la")) {
            Properties p = new Properties();
            try (InputStream in = getClass().getClassLoader()
                    .getResourceAsStream("mods/language/" + bundle + ".properties")) {
                assertNotNull(in, bundle);
                p.load(new java.io.InputStreamReader(in, StandardCharsets.ISO_8859_1));
            }
            for (int level = 0; level <= LanguageSkill.MAX; level++) {
                assertFalse(p.getProperty(PlayerDetailScreenTable.LANGUAGE_LEVEL_KEY + level, "").isEmpty(), bundle);
                assertFalse(p.getProperty(PlayerDetailScreenTable.READ_WRITE_KEY + level, "").isEmpty(), bundle);
            }
            for (String id : LANG.getLanguages().keySet()) {
                String key = PlayerDetailScreenTable.LANGUAGE_NAME_KEY + id.replace('-', '_');
                assertFalse(p.getProperty(key, "").isEmpty(), bundle + " " + key);
            }
        }
    }

    private static LanguageSkill skill(LanguageGenerator.Result r, String language) {
        for (LanguageSkill s : r.languages()) {
            if (s.getLanguage().equals(language)) {
                return s;
            }
        }
        throw new AssertionError(language + " missing in " + r);
    }

    private static Person person(String dataId, String country, int year) {
        Person p = new Person();
        p.setDataId(dataId);
        p.setBirthDate(LocalDateTime.of(year, 6, 1, 0, 0));
        com.rndmodgames.futtoboru.data.Country c = new com.rndmodgames.futtoboru.data.Country();
        c.setDataId(country);
        p.setCountry(c);
        return p;
    }
}
