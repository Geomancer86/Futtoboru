package com.rndmodgames.futtoboru.system.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** EraProfile (H-06): baseline plus dated events, resolved per date. No libGDX application needed. */
class EraProfileTests {

    private static final String BASE = "{\"date\":\"1888-09-08\",\"parameters\":{"
            + "\"rules\":{\"offsideOpponents\":3,\"penaltyKick\":false,\"officials\":\"UMPIRES\"},"
            + "\"ball\":{\"massMinG\":0},"
            + "\"kit\":{\"shinPadShare\":0.6},"
            + "\"league\":{\"maxWagePerWeek\":0}}}";

    private static String event(String id, String announced, String effective, String scope, String effects) {
        return "{\"id\":\"" + id + "\",\"kind\":\"rule\",\"authority\":\"ifab\",\"announced\":\"" + announced
                + "\",\"effective\":\"" + effective + "\"" + (scope == null ? "" : ",\"scope\":" + scope)
                + ",\"effects\":[" + effects + "],\"message\":{\"key\":\"era." + id + "\"}}";
    }

    private static String set(String p, String v) {
        return "{\"param\":\"" + p + "\",\"op\":\"set\",\"value\":" + v + "}";
    }

    private static EraProfile profile(String... events) {
        return EraProfile.parse(BASE, List.of("[" + String.join(",", events) + "]"));
    }

    private static LocalDate d(String s) {
        return LocalDate.parse(s);
    }

    @Test
    void baselineHoldsUntilAnEventTakesEffect() {
        EraProfile p = profile(event("pen", "1891-06-02", "1891-09-01", null, set("rules.penaltyKick", "true")));

        assertFalse(p.at(d("1888-09-08")).getBoolean("rules.penaltyKick"));
        assertFalse(p.at(d("1891-08-31")).getBoolean("rules.penaltyKick")); // announced, not yet in force
        assertTrue(p.at(d("1891-09-01")).getBoolean("rules.penaltyKick")); // the effective day counts
        assertEquals(List.of("pen"), p.at(d("1900-01-01")).appliedEvents());
        assertEquals(3, p.at(d("1900-01-01")).getInt("rules.offsideOpponents"));
        assertEquals("UMPIRES", p.at(d("1900-01-01")).getString("rules.officials"));
    }

    @Test
    void eventsApplyInDateOrderWhateverTheFileOrder() {
        EraProfile p = profile(event("b", "1910-06-01", "1910-09-01", null, set("league.maxWagePerWeek", "5")),
                event("a", "1901-06-01", "1901-09-01", null, set("league.maxWagePerWeek", "4")));

        assertEquals(4, p.at(d("1905-01-01")).getInt("league.maxWagePerWeek"));
        assertEquals(5, p.at(d("1911-01-01")).getInt("league.maxWagePerWeek"));
        assertEquals(List.of("a", "b"), p.at(d("1911-01-01")).appliedEvents());
    }

    @Test
    void scaleAndAdd() {
        String eff = "{\"param\":\"kit.shinPadShare\",\"op\":\"scale\",\"value\":0.5},"
                + "{\"param\":\"ball.massMinG\",\"op\":\"add\",\"value\":10}";
        EraProfile p = profile(event("x", "1900-01-01", "1900-01-01", null, eff));

        assertEquals(0.3, p.at(d("1901-01-01")).getDouble("kit.shinPadShare"), 1e-9);
        assertEquals(10, p.at(d("1901-01-01")).getInt("ball.massMinG"));
    }

    @Test
    void adoptionMovesLinearlyOverTheSeasons() {
        String eff = "{\"param\":\"kit.shinPadShare\",\"op\":\"set\",\"value\":0.9,"
                + "\"adoption\":{\"seasons\":10,\"from\":0.5}}";
        EraProfile p = profile(event("pads", "1900-01-01", "1900-09-01", null, eff));

        assertEquals(0.6, p.at(d("1900-08-31")).getDouble("kit.shinPadShare"), 1e-9); // not in force: baseline
        assertEquals(0.5, p.at(d("1900-09-01")).getDouble("kit.shinPadShare"), 1e-9);
        assertEquals(0.7, p.at(d("1905-09-01")).getDouble("kit.shinPadShare"), 0.002); // halfway
        assertEquals(0.9, p.at(d("1910-09-01")).getDouble("kit.shinPadShare"), 0.002);
        assertEquals(0.9, p.at(d("1950-01-01")).getDouble("kit.shinPadShare"), 1e-9);
    }

    @Test
    void adoptionWithoutFromStartsAtTheCurrentValue() {
        String eff = "{\"param\":\"kit.shinPadShare\",\"op\":\"set\",\"value\":1.0,\"adoption\":{\"seasons\":4}}";
        EraProfile p = profile(event("pads", "1900-01-01", "1900-01-01", null, eff));

        assertEquals(0.6, p.at(d("1900-01-01")).getDouble("kit.shinPadShare"), 1e-9);
        assertEquals(0.8, p.at(d("1902-01-01")).getDouble("kit.shinPadShare"), 0.002);
    }

    @Test
    void eventsAtOrBeforeTheBaselineAreAlreadyFolded() {
        EraProfile p = profile(event("old", "1874-01-01", "1874-01-01", null, set("kit.shinPadShare", "0.9")));

        assertEquals(0.6, p.at(d("1890-01-01")).getDouble("kit.shinPadShare"), 1e-9);
        assertTrue(p.at(d("1890-01-01")).appliedEvents().isEmpty());
        assertEquals(1, p.events().size()); // still there for history screens
    }

    @Test
    void scopeFiltersByCountryAndCompetition() {
        String scope = "{\"competitions\":[\"football-league\"]}";
        EraProfile p = profile(event("wage", "1901-04-01", "1901-09-01", scope, set("league.maxWagePerWeek", "4")));

        assertEquals(4, p.at(d("1902-01-01"), "england", "football-league").getInt("league.maxWagePerWeek"));
        assertEquals(0, p.at(d("1902-01-01"), "england", "fa-cup").getInt("league.maxWagePerWeek"));
        assertEquals(4, p.at(d("1902-01-01")).getInt("league.maxWagePerWeek")); // null: not filtered
    }

    @Test
    void announcedBetweenIsExclusiveInclusiveAndSorted() {
        EraProfile p = profile(event("b", "1892-06-01", "1892-09-01", null, set("rules.penaltyKick", "true")),
                event("a", "1891-06-02", "1891-09-01", null, set("rules.penaltyKick", "true")));

        assertEquals(List.of("a"), p.announcedBetween(d("1891-06-01"), d("1891-06-02")).stream()
                .map(EraProfile.Event::id).toList());
        assertTrue(p.announcedBetween(d("1891-06-02"), d("1892-05-31")).isEmpty());
        assertEquals(List.of("a", "b"), p.announcedBetween(d("1891-01-01"), d("1892-06-01")).stream()
                .map(EraProfile.Event::id).toList());
    }

    @Test
    void unknownParameterOrWrongTypeIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> profile(event("x", "1900-01-01", "1900-01-01", null, set("rules.nope", "true"))));
        assertThrows(IllegalArgumentException.class,
                () -> profile(event("x", "1900-01-01", "1900-01-01", null, set("rules.penaltyKick", "3"))));
        assertThrows(IllegalArgumentException.class, () -> profile().at(d("1900-01-01")).getInt("rules.nope"));
    }

    @Test
    void shippedDataResolvesTheKnownHistory() {
        EraProfile p = EraProfile.loadDefault();

        assertEquals(d("1888-09-08"), p.baselineDate());
        EraProfile.Resolved start = p.at(d("1888-09-08"));
        assertEquals(3, start.getInt("rules.offsideOpponents"));
        assertFalse(start.getBoolean("rules.penaltyKick"));
        assertEquals("UMPIRES_AND_REFEREE", start.getString("rules.officials"));
        assertTrue(start.appliedEvents().isEmpty());

        EraProfile.Resolved y1892 = p.at(d("1892-10-01"));
        assertTrue(y1892.getBoolean("rules.penaltyKick"));
        assertEquals("REFEREE_AND_LINESMEN", y1892.getString("rules.officials"));
        assertTrue(y1892.getBoolean("rules.goalNets"));
        assertEquals(2, y1892.getInt("league.divisions"));
        assertEquals(369, y1892.getInt("ball.massMinG"));

        assertEquals(2, p.at(d("1926-01-01")).getInt("rules.offsideOpponents"));
        assertTrue(p.at(d("1926-01-01")).getBoolean("rules.goalFromCorner"));
        assertEquals("PENALTY_AREA", p.at(d("1926-01-01")).getString("rules.keeperHandling"));
        assertEquals(8, p.at(d("1926-01-01")).getInt("league.maxWagePerWeek"));
        assertTrue(p.at(d("1917-01-01")).getBoolean("league.suspended"));
        assertEquals(454, p.at(d("1938-01-01")).getInt("ball.massMaxG"));
        assertTrue(p.at(d("1939-09-01")).getBoolean("kit.numbers"));
        assertEquals(0.9, p.at(d("1930-01-01")).getDouble("kit.shinPadShare"), 0.05);
    }

    @Test
    void shippedEventsAreInAnnouncedBeforeEffectiveOrder() {
        for (EraProfile.Event e : EraProfile.loadDefault().events()) {
            assertFalse(e.effective().isBefore(e.announced()), e.id());
        }
    }
}
