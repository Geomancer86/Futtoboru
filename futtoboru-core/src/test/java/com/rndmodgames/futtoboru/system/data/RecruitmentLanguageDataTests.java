package com.rndmodgames.futtoboru.system.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.language.LanguageRate;
import com.rndmodgames.futtoboru.data.recruitment.RegistrationRule;
import com.rndmodgames.futtoboru.data.recruitment.RegistrationRule.Kind;

/**
 * R-02 / R-11a: the recruitment and language files load with the same strict checks as tools/data/validate.py, get
 * their ids.json numbers, and the read-only lookups give the sourced rules and rates by date.
 */
class RecruitmentLanguageDataTests {

    private static final GameData.Source BASE = new GameData.ClasspathSource("gamedata");

    @Test
    void shippedRecruitmentAndLanguageDataLoadWithNumbers() {
        GameData data = GameData.loadDefault();
        assertEquals("", data.getReport().errorsText());
        for (String kind : List.of(GameData.RULE, GameData.WAGE, GameData.ORIGIN, GameData.ORIGIN_CLUB,
                GameData.AREA, GameData.LANGUAGE)) {
            assertTrue(data.all(kind).size() > 0, kind);
            for (DataRecord r : data.all(kind)) {
                assertTrue(r.runtimeId() > 0 && r.runtimeId() < IdAllocator.DERIVED_START, r.where());
            }
        }
        assertTrue(data.records("language_rate", null).size() > 20);
    }

    @Test
    void registrationRulesByDate() {
        RecruitmentData rec = RecruitmentData.from(GameData.loadDefault());
        LocalDate season1888 = LocalDate.of(1888, 9, 8);
        assertNotNull(rec.rule(Kind.RESIDENCE, season1888));
        assertEquals(6, rec.rule(Kind.RESIDENCE, season1888).getMiles());
        assertEquals("fa-council-second-registration-1888",
                rec.rule(Kind.SECOND_REGISTRATION_CONSENT, season1888).getDataId());
        assertNotNull(rec.rule(Kind.SCOTTISH_PROFESSIONAL_BAN, season1888));
        assertNull(rec.rule(Kind.REGISTRATION_WAIT, season1888));
        assertNull(rec.rule(Kind.RETAIN_AND_TRANSFER, season1888));

        LocalDate season1890 = LocalDate.of(1890, 9, 20);
        assertNull(rec.rule(Kind.RESIDENCE, season1890)); // dropped in 1889
        assertEquals(7, rec.rule(Kind.REGISTRATION_WAIT, season1890).getDays());

        assertNull(rec.rule(Kind.SCOTTISH_PROFESSIONAL_BAN, LocalDate.of(1893, 9, 1)));
        assertNotNull(rec.rule(Kind.RETAIN_AND_TRANSFER, LocalDate.of(1893, 9, 1)));
        RegistrationRule cap = rec.rule(Kind.MAX_WAGE, LocalDate.of(1901, 9, 1));
        assertEquals(960, cap.getPence()); // £4 a week
        assertNull(rec.rule(Kind.MAX_WAGE, LocalDate.of(1900, 9, 1)));
    }

    @Test
    void wageBandsExamplesAndOrigins() {
        RecruitmentData rec = RecruitmentData.from(GameData.loadDefault());
        assertEquals("band-leading-1885", rec.band("leading", LocalDate.of(1888, 9, 1)).getDataId());
        assertEquals("band-capped-1901", rec.band("leading", LocalDate.of(1905, 9, 1)).getDataId()); // `any`
        assertTrue(rec.getWages().stream().anyMatch(w -> !w.isBand() && "johnny-holt-1865".equals(w.getPersonDataId())
                && w.getWeeklyPence() == 540 && w.getSummerWeeklyPence() == 240));
        assertEquals(1, rec.origin("schoolmaster").getLiteracyShift());
        assertTrue(rec.origin("mill").getOccupations().contains("weaver"));
        for (var o : rec.getOrigins().values()) {
            o.getNudges().values().forEach(n -> assertTrue(Math.abs(n) <= GameData.NUDGE_MAX, o.getDataId()));
        }
        assertTrue(rec.originClubsIn("scotland-lowlands").size() >= 5);
    }

    @Test
    void rateLookupGoesAreaThenCountryThenDefault() {
        LanguageData lang = LanguageData.from(GameData.loadDefault());
        // area row
        assertEquals(0.93, lang.rate(LanguageRate::getSpeaks, "wales-west", "wales", 1865).get("welsh"), 1e-9);
        assertEquals(0.878, lang.rate(LanguageRate::getSpeaks, "wales-west", "wales", 1890).get("welsh"), 1e-9);
        // country row (England and Wales marriage register, married about 1886-90)
        assertEquals(0.916, lang.rate(LanguageRate::getSignsMen, "lancashire", "england", 1865), 1e-9);
        assertEquals(0.744, lang.rate(LanguageRate::getSignsMen, "ulster", "ireland", 1855), 1e-9);
        assertEquals(0.65, lang.rate(LanguageRate::getSignsMen, "connacht", "ireland", 1855), 1e-9);
        // default row (born abroad) and a year outside every band
        assertEquals(0.7, lang.rate(LanguageRate::getSignsMen, null, "germany", 1865), 1e-9);
        assertEquals(0.971, lang.rate(LanguageRate::getSignsMen, null, "england", 1990), 1e-9);
        assertNull(lang.rate(LanguageRate::getSpeaks, "lancashire", "england", 1865));
        // the Welsh areas weigh to about the sourced 54% of 1891
        assertEquals(0.54, lang.speakersIn("wales", "welsh", 1865), 0.01);
        assertEquals("german", lang.nationalLanguage("germany"));
        assertEquals("english", lang.nationalLanguage("scotland"));
    }

    @Test
    void brokenRecruitmentAndLanguageRecordsAreErrors() {
        GameData data = GameData.load(patched(path -> {
            switch (path) {
            case "core/recruitment/wages.json":
                return s -> s.replace("\"minWeeklyPence\": 360,", "\"minWeeklyPence\": 900,");
            case "core/recruitment/rules.json":
                return s -> s.replace("\"days\": 7,", "");
            case "core/recruitment/origins.json":
                return s -> s.replace("\"endurance\": 1, \"dexterity\": 1, \"strength\": -1",
                        "\"endurance\": 5, \"dexterity\": 1, \"strength\": -1");
            case "core/areas.json":
                return s -> s.replace("\"weight\": 0.022,", "\"weight\": 0.5,");
            case "core/language_rates.json":
                return s -> s.replace("\"only\": { \"welsh\": 0.22 }", "\"only\": { \"welsh\": 0.9 }");
            default:
                return null;
            }
        }));
        String errors = data.getReport().errorsText();
        assertTrue(errors.contains("minWeeklyPence 900 is above maxWeeklyPence 600"), errors);
        assertTrue(errors.contains("a `registration-wait` rule needs `days`"), errors);
        assertTrue(errors.contains("a nudge is a whole number from -2 to 2"), errors);
        assertTrue(errors.contains("the area weights of `england` add up to"), errors);
        assertTrue(errors.contains("speak only it but"), errors);
        // a broken record is skipped, like any other
        assertNull(data.get(GameData.WAGE, "band-leading-1885"));
        assertNull(data.get(GameData.RULE, "league-registration-wait-1890"));
        assertNotNull(data.get(GameData.RULE, "fa-maximum-wage-1901"));
    }

    /** The shipped data with some files changed by text replacement. */
    private static GameData.Source patched(java.util.function.Function<String, UnaryOperator<String>> edits) {
        return new GameData.Source() {
            @Override
            public String read(String path) {
                String text = BASE.read(path);
                UnaryOperator<String> edit = edits.apply(path);
                return text == null || edit == null ? text : edit.apply(text);
            }

            @Override
            public List<String> listJson(String folder) {
                return BASE.listJson(folder);
            }

            @Override
            public List<String> listFolders() {
                return BASE.listFolders();
            }
        };
    }
}
