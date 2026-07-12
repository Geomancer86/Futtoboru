package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SaveLoadSystemPathsTest {

    @Test
    void normalizeStripsExtensionAndPath() {
        assertEquals("savegame", SaveLoadSystem.normalizeSaveBasename("savegame.json"));
        assertEquals("my_save", SaveLoadSystem.normalizeSaveBasename("C:/tmp/my_save.json"));
        assertEquals("savegame", SaveLoadSystem.normalizeSaveBasename(""));
    }
}
