package com.rndmodgames.futtoboru.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.data.HistoryName;

/** G-10: the headless lookups behind the History panel and the fan-love badge. */
class HistoryFactsTests {

    private static final GameData DATA = GameData.loadDefault();

    @Test
    void clubHistoryHasTextNicknamesAndRivals() {
        HistoryFacts facts = new HistoryFacts(DATA, "en");
        HistoryFacts.ClubHistory preston = facts.club("preston-north-end");
        assertFalse(preston.isEmpty());
        assertNotNull(preston.hook);
        assertFalse(preston.history.isEmpty());
        assertTrue(preston.seasons.containsKey("1888-89"));
        assertEquals("The Invincibles", preston.nicknames.get(0).getName());
        assertTrue(preston.rivals.contains("blackburn-rovers"));
        assertEquals("Preston North End", facts.clubName("preston-north-end"));
        // another language falls back to English text
        assertEquals(preston.hook, new HistoryFacts(DATA, "es").club("preston-north-end").hook);
    }

    @Test
    void unknownIdsAndNoDataGiveEmptyResults() {
        assertTrue(new HistoryFacts(DATA, "en").club("no-such-club").isEmpty());
        assertTrue(new HistoryFacts(null, "en").club("preston-north-end").isEmpty());
        assertEquals(0, new HistoryFacts(null, "en").fanTier("fred-dewhurst-1863", "preston-north-end"));
        assertNull(new HistoryFacts(DATA, "en").person("no-such-person", null).bio);
    }

    @Test
    void personHasTierBioAndNicknames() {
        HistoryFacts facts = new HistoryFacts(DATA, "en");
        assertEquals(4, facts.fanTier("fred-dewhurst-1863", "preston-north-end"));
        assertEquals(0, facts.fanTier("fred-dewhurst-1863", "aston-villa")); // not in that squad
        HistoryFacts.PersonHistory dewhurst = facts.person("fred-dewhurst-1863", "preston-north-end");
        assertEquals(4, dewhurst.tier);
        assertNotNull(dewhurst.bio);
        HistoryFacts.PersonHistory hunter = facts.person("archie-hunter-1859", "aston-villa");
        assertEquals("The Old Warhorse", hunter.nicknames.get(0).getName());
    }

    @Test
    void tierKeysAreClampedAndLanguageMapped() {
        assertEquals("fan_tier_0", HistoryFacts.tierKey(-3));
        assertEquals("fan_tier_5", HistoryFacts.tierKey(9));
        assertEquals("es", HistoryFacts.dataLanguage("sp_SP"));
        assertEquals("en", HistoryFacts.dataLanguage("en_UK"));
        assertEquals("en", HistoryFacts.dataLanguage(null));
    }

    @Test
    void yearsText() {
        HistoryName st = new HistoryFacts(DATA, "en").club("wolverhampton-wanderers").nameHistory.get(0);
        assertEquals("St Luke's", st.getName());
        assertTrue(HistoryFacts.years(st).startsWith("18"));
    }
}
