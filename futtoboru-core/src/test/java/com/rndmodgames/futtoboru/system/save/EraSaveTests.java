package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-11 and saves: the ids of the era events already announced are an optional save field (no saveVersion bump). They
 * survive save and load, so a reloaded game announces nothing twice; an older save has none.
 */
class EraSaveTests {

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(SeasonMemo.SEED), config);
        BaseTest.awaitHeadlessCreate();
    }

    @Test
    void announcedEventsSurviveSaveAndLoad() throws IOException {
        SaveGame game;
        try (InputStream in = EraSaveTests.class.getClassLoader().getResourceAsStream(SaveGameTests.FIXTURE)) {
            assertNotNull(in, SaveGameTests.FIXTURE);
            LoadResult old = SaveCodec.decode(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            assertTrue(old.isLoaded(), old.toString());
            game = old.getGame();
        }
        assertTrue(game.getAnnouncedEraEvents().isEmpty(), "a save made before G-11 has announced nothing");

        game.getAnnouncedEraEvents().add("ifab-1891-penalty-kick");
        game.getAnnouncedEraEvents().add("league-1891-goal-nets");
        List<String> ids = List.copyOf(game.getAnnouncedEraEvents());

        String text = SaveCodec.encode(game, "Era", LocalDateTime.of(2000, 1, 1, 0, 0));
        LoadResult result = SaveCodec.decode(text);
        assertTrue(result.isLoaded(), result.toString());
        assertEquals(ids, result.getGame().getAnnouncedEraEvents());
        assertEquals(text, SaveCodec.encode(result.getGame(), "Era", LocalDateTime.of(2000, 1, 1, 0, 0)));
    }
}
