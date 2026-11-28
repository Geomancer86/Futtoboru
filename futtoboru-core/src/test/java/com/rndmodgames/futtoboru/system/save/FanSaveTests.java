package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.FanStanding;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.fans.FanMeter;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-10 and saves: a player's fan meter is an optional field (no saveVersion bump). It survives a save and load; the
 * v1 fixture save (made before G-10) loads with no meters, and the tiers then come from the data.
 */
class FanSaveTests {

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(SeasonMemo.SEED), config);
        BaseTest.awaitHeadlessCreate();
    }

    private static SaveGame load(String text) {
        LoadResult result = SaveCodec.decode(text);
        assertTrue(result.isLoaded(), result.toString());
        return result.getGame();
    }

    @Test
    void fanMetersSurviveSaveAndLoadAndAnOldSaveHasNone() throws IOException {
        SaveGame game;
        try (InputStream in = FanSaveTests.class.getClassLoader().getResourceAsStream(SaveGameTests.FIXTURE)) {
            assertNotNull(in, SaveGameTests.FIXTURE);
            game = load(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        Club club = game.getCurrentClub();
        for (Club c : game.getAllClubs()) {
            for (Player p : c.getPlayers()) {
                assertNull(p.getFans(), "a save made before G-10 has no meters");
                int t = FanMeter.tierOf(p, c);
                assertTrue(t >= 0 && t <= 5);
            }
        }
        Player idol = club.getPlayers().get(0);
        idol.setFans(new FanStanding(337.5, 3));

        SaveGame again = load(SaveCodec.encode(game, "G-10", LocalDateTime.of(2026, 11, 28, 12, 0)));
        Player idolAgain = null;
        for (Player p : again.getCurrentClub().getPlayers()) {
            if (p.getId().equals(idol.getId())) {
                idolAgain = p;
            }
        }
        assertNotNull(idolAgain);
        assertNotNull(idolAgain.getFans());
        assertEquals(337.5, idolAgain.getFans().getPoints(), 1e-9);
        assertEquals(3, idolAgain.getFans().getTier());
        assertEquals(3, FanMeter.tierOf(idolAgain, again.getCurrentClub()));
    }
}
