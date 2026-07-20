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

        // Pre-build the scriptsManager/authorityManager/gameEngine triple and hand it to
        // `application` *before* starting the LibGDX lifecycle below. Futtoboru.create() (called
        // synchronously as part of the HeadlessApplication constructor) now reuses these exact
        // instances instead of building its own second, independent triple -- see the null-guards
        // added around Futtoboru.java's scriptsManager/authorityManager/gameEngine construction in
        // create(). Without those guards, create() used to unconditionally rebuild a *second*
        // FuttoboruGameEngine (and MessageManager) and silently orphan this one: the harness kept
        // driving continueGame()/match simulation through this (orphaned) engine, while
        // AuthorityManager/ScriptsManager internally re-fetch "the current" engine dynamically via
        // game.getGameEngine() and got the second (create()'s) one -- so two independent
        // MessageManager instances, each with their own nextMessageId counter, both wrote into the
        // same SaveGame.allMessages/scheduledMessages lists. Once a match-result message (delivered
        // through this engine) happened to reuse the same numeric id as a cup/league draw message
        // (delivered through create()'s engine), deliverMessage()'s id-based dedup check silently
        // dropped the second one forever -- reproduced headlessly in CupDrawUiFlowIT, matching a real
        // human bug report: a mandatory CUP_DRAW message that never actually reached the inbox,
        // permanently soft-locking the Continue button. See A-13 in docs/registries/blockers-resolved.yaml.
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
