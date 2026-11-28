package com.rndmodgames.futtoboru.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * S-01 (D25): every new game draws a fresh seed, stored in its save; {@code -Dfuttoboru.seed} and
 * {@link Futtoboru#Futtoboru(long)} force one seed for every new game (tests, replays). The forced-seed replay of a
 * whole new game in a session is {@code NewGameFreshDataTests}; of a season, {@code FullSeasonTests}.
 */
class NewGameSeedTests {

    @Test
    void twoNewGamesInOneSessionGetDifferentSeeds() {
        assumeTrue(System.getProperty(Futtoboru.SEED_PROPERTY) == null, "a seed is forced for this run");
        Futtoboru session = new Futtoboru();
        assertFalse(session.isSeedForced());
        long first = session.reseedForNewGame();
        SaveGame a = new SaveGame();
        session.setCurrentGame(a);
        long second = session.reseedForNewGame();
        SaveGame b = new SaveGame();
        session.setCurrentGame(b);
        assertNotEquals(first, second);
        assertEquals(first, a.getSeed(), "the save keeps its game's seed");
        assertEquals(second, b.getSeed());
        assertEquals(second, session.random().getSeed());
    }

    @Test
    void aForcedSeedIsUsedByEveryNewGame() {
        Futtoboru session = new Futtoboru(1888L);
        assertTrue(session.isSeedForced());
        assertEquals(1888L, session.reseedForNewGame());
        session.setSeed(99L); // e.g. a save loaded in between
        assertEquals(1888L, session.reseedForNewGame());
        assertEquals(1888L, session.getSeed());
    }

    @Test
    void theSeedPropertyForcesTheSeed() {
        String before = System.getProperty(Futtoboru.SEED_PROPERTY);
        System.setProperty(Futtoboru.SEED_PROPERTY, "1889");
        try {
            Futtoboru session = new Futtoboru();
            assertTrue(session.isSeedForced());
            assertEquals(1889L, session.getSeed());
            assertEquals(1889L, session.reseedForNewGame());
        } finally {
            if (before == null) {
                System.clearProperty(Futtoboru.SEED_PROPERTY);
            } else {
                System.setProperty(Futtoboru.SEED_PROPERTY, before);
            }
        }
    }
}
