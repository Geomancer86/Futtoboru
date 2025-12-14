package com.rndmodgames.futtoboru;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

public class BaseTestToolTests extends BaseTest {
    
    @Test
    void testBaseSaveGameV1InitializerTest() {
        
        // reset db loader just in case
        DatabaseLoader.resetDatabaseLoaderInstance();
        
        // fresh instance for this test
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        // SaveGame instance to test
        SaveGame saveGame = new SaveGame();
        
        // Initialize savegame
        BaseTestTools tools = new BaseTestTools();
        tools.initializeV1SaveGame(saveGame, dbLoader);
        
        //
        assertNotNull(saveGame.getOwner());
    }
}