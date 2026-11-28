package com.rndmodgames.desktop;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.screens.MenuScreen;

/**
 * T-01 smoke tier (D27): {@code -Dfuttoboru.smoke=true} starts the real game (window, data, main menu), lets the main
 * menu render a few frames, prints {@link #OK_LINE} and quits through {@code Gdx.app.exit()} as the Quit button does.
 * Exit codes: 0 passed, 1 failed, 2 no display (GLFW could not start), 3 timed out. Used by {@code DesktopSmokeIT}.
 */
final class SmokeRun {

    static final String PROPERTY = "futtoboru.smoke";
    static final String OK_LINE = "FUTTOBORU SMOKE OK: main menu shown";
    static final String NO_DISPLAY_LINE = "FUTTOBORU SMOKE NO DISPLAY";
    static final int EXIT_FAILED = 1;
    static final int EXIT_NO_DISPLAY = 2;
    static final int EXIT_TIMEOUT = 3;

    /** frames the main menu renders before the smoke run quits */
    private static final int MENU_FRAMES = 30;
    private static final long TIMEOUT_MILLIS = 90_000L;

    private static volatile boolean passed;

    private SmokeRun() {
    }

    static boolean enabled() {
        return Boolean.getBoolean(PROPERTY);
    }

    static boolean passed() {
        return passed;
    }

    /** The game, quitting by itself once the main menu has rendered; a watchdog ends a run that hangs. */
    static Futtoboru game() {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MILLIS);
            } catch (InterruptedException e) {
                return;
            }
            System.out.println("FUTTOBORU SMOKE TIMEOUT after " + TIMEOUT_MILLIS + " ms");
            Runtime.getRuntime().halt(EXIT_TIMEOUT);
        }, "smoke-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();

        return new Futtoboru() {
            private int menuFrames;

            @Override
            public void render() {
                super.render();
                if (!passed && getScreen() instanceof MenuScreen && ++menuFrames >= MENU_FRAMES) {
                    passed = true;
                    System.out.println(OK_LINE);
                    Gdx.app.exit();
                }
            }
        };
    }

    /** @return the exit code for a start-up error: no display when GLFW could not start, else failed */
    static int exitCodeFor(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if (message != null && message.contains("GLFW")) {
                System.out.println(NO_DISPLAY_LINE + ": " + message);
                return EXIT_NO_DISPLAY;
            }
        }
        return EXIT_FAILED;
    }
}
