package com.rndmodgames.futtoboru;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

public class BaseTest {

    //
    protected static Futtoboru application;

    //
    protected static FuttoboruGameEngine gameEngine;
    protected static ScriptsManager scriptsManager;
    protected static AuthorityManager authorityManager;

    @BeforeAll
    static void preload() {

        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;

        //
        application = new Futtoboru();

        // Initialize the Saved Game
        application.setCurrentGame(new SaveGame());

        // Initialize Script Manager
        scriptsManager = new ScriptsManager(application);

        // Initialize the Authority Manager
        authorityManager = new AuthorityManager(application);

        // Initialize Game Engine
        gameEngine = new FuttoboruGameEngine(application, scriptsManager, authorityManager);

        // Set the Script Manager
        application.setScriptsManager(scriptsManager);

        // Set the Game Engine
        application.setGameEngine(gameEngine);

        new HeadlessApplication(application, config);

        // Wait until create() (which loads the database) has finished on the app thread
        awaitHeadlessCreate();
    }

    /**
     * HeadlessApplication runs create() on its own thread, and DatabaseLoader.getInstance() publishes
     * the instance before it finishes loading, so a test reading the database right away could see
     * empty seasons (failed on Linux CI, 2026-11-27, where test classes run in another order).
     * A posted runnable only runs after create() completes, so waiting for it is enough.
     */
    public static void awaitHeadlessCreate() {
        CountDownLatch created = new CountDownLatch(1);
        Gdx.app.postRunnable(created::countDown);
        try {
            if (!created.await(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Headless application did not finish create() in 60 s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
