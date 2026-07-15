package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Negative-path coverage for {@link SaveLoadSystem#loadGame(String)}: a save file that exists
 * but is not valid JSON (truncated write, hand-edited, disk corruption) must not crash the caller.
 *
 * <p>Found by QA review 2026-07-15: before the accompanying {@code SaveLoadSystem} fix, a corrupt
 * file threw an unhandled {@code SerializationException} straight out of {@code LoadGameDialog}'s
 * {@code touchUp} input listener — every other save/load test only exercises the happy-path
 * in-memory JSON roundtrip ({@code SaveLoadIT}), never a real file on disk.
 */
class SaveLoadCorruptionTest {

    private static final String SAVE_NAME = "qa_corruption_test_delete_me";

    private Path saveFilePath;

    @BeforeAll
    static void initializeLibGDX() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(), config);
    }

    @BeforeEach
    void setUp() {
        SaveLoadSystem.ensureSaveGamesDirectory();
        saveFilePath = Paths.get(System.getProperty("user.home"),
                "Documents", "RndModGames", "Futtuboru", "savegames", SAVE_NAME + ".json");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(saveFilePath);
    }

    @Test
    void truncatedJsonReturnsNullInsteadOfThrowing() throws IOException {
        Files.writeString(saveFilePath, "{\"allClubs\": [ { \"id\": 1, \"name\": \"Broken");

        SaveGame result = assertDoesNotThrow(() -> SaveLoadSystem.loadGame(SAVE_NAME),
                "A corrupt save file must not throw out of loadGame()");

        assertNull(result, "Corrupt save data should load as null, same contract as a missing file");
    }

    @Test
    void emptyFileReturnsNullInsteadOfThrowing() throws IOException {
        Files.writeString(saveFilePath, "");

        SaveGame result = assertDoesNotThrow(() -> SaveLoadSystem.loadGame(SAVE_NAME));

        assertNull(result);
    }

    @Test
    void wrongShapeJsonReturnsNullInsteadOfThrowing() throws IOException {
        Files.writeString(saveFilePath, "[1, 2, 3]");

        SaveGame result = assertDoesNotThrow(() -> SaveLoadSystem.loadGame(SAVE_NAME));

        assertNull(result);
    }

    @Test
    void validSaveStillLoadsNormallyAfterHardeningLoadGame() {
        SaveGame game = new SaveGame();
        SaveLoadSystem.saveGame(game, SAVE_NAME);

        SaveGame loaded = SaveLoadSystem.loadGame(SAVE_NAME);

        assertNotNull(loaded, "A well-formed save must still load after hardening loadGame() against corruption");
    }
}
