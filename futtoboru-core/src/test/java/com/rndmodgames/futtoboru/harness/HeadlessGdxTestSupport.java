package com.rndmodgames.futtoboru.harness;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Shared headless LibGDX bootstrap for integration tests.
 */
public final class HeadlessGdxTestSupport {

    private static Futtoboru application;
    private static FuttoboruGameEngine gameEngine;
    private static ScriptsManager scriptsManager;
    private static AuthorityManager authorityManager;
    private static boolean initialized;

    private HeadlessGdxTestSupport() {
    }

    public static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        if (System.getProperty(HarnessLogProfile.PROPERTY) == null) {
            System.setProperty(HarnessLogProfile.PROPERTY, HarnessLogProfile.QUIET);
        }

        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;

        application = new Futtoboru();
        application.setCurrentGame(new SaveGame());

        scriptsManager = new ScriptsManager(application);
        authorityManager = new AuthorityManager(application);
        gameEngine = new FuttoboruGameEngine(application, scriptsManager, authorityManager);

        application.setScriptsManager(scriptsManager);
        application.setGameEngine(gameEngine);

        new HeadlessApplication(application, config);
        HarnessLogProfile.applyFromSystemProperty();
        initialized = true;
    }

    public static Futtoboru application() {
        ensureInitialized();
        return application;
    }

    public static FuttoboruGameEngine gameEngine() {
        ensureInitialized();
        return gameEngine;
    }

    public static ScriptsManager scriptsManager() {
        ensureInitialized();
        return scriptsManager;
    }

    public static AuthorityManager authorityManager() {
        ensureInitialized();
        return authorityManager;
    }
}
