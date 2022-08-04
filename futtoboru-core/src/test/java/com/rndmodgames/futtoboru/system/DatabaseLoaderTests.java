package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.game.Futtoboru;

class DatabaseLoaderTests {

    private static Futtoboru application;
    
    @BeforeAll
    static void preloadCharacterClasses() {
        
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        
        new HeadlessApplication(application , config);
    }
    
    @Test
    void loadDatabaseTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
    }
}
