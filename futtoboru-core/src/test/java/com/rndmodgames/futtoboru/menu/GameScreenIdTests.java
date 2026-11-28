package com.rndmodgames.futtoboru.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/** RF-01: inbox messages store screen numbers in saves, so they never change. */
class GameScreenIdTests {

    @Test
    void storedNumbersStayTheSame() {
        assertEquals(100, GameScreenId.HOME.id());
        assertEquals(200, GameScreenId.INBOX.id());
        assertEquals(4000, GameScreenId.FINANCES.id());
        assertEquals(10008, GameScreenId.LEAGUE_DRAW.id());
        assertEquals(10009, GameScreenId.CUP_DRAW.id());
        assertEquals(20000, GameScreenId.MATCH_RESULT.id());
        assertEquals(10010, GameScreenId.RETAINED_LIST.id());
    }

    @Test
    void numbersAreUniqueAndRoundTrip() {
        Set<Integer> ids = new HashSet<>();
        for (GameScreenId screen : GameScreenId.values()) {
            assertEquals(true, ids.add(screen.id()), "number twice: " + screen);
            assertEquals(screen, GameScreenId.fromId(screen.id()));
        }
        assertEquals(GameScreenId.NONE, GameScreenId.fromId(null));
        assertEquals(GameScreenId.NONE, GameScreenId.fromId(12345));
    }
}
