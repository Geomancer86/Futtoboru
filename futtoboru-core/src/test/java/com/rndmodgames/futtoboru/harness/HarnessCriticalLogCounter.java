package com.rndmodgames.futtoboru.harness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Captures CRITICAL / ERROR / exception logs during harness runs so tests fail at the first
 * logged failure instead of continuing multi-season soaks.
 */
public final class HarnessCriticalLogCounter {

    private static final Pattern FAILURE = Pattern.compile(
            "CRITICAL|\\bERROR\\b|Exception|Throwable",
            Pattern.CASE_INSENSITIVE);

    private static final List<String> captured = new ArrayList<>();
    private static Consumer<String> listener;
    private static ApplicationLogger previousLogger;
    private static volatile String firstFailure;
    private static volatile boolean failFast = true;

    private HarnessCriticalLogCounter() {
    }

    public static void start() {
        captured.clear();
        firstFailure = null;
        listener = HarnessCriticalLogCounter::reportFailure;
        DebugLogManager.setTestErrorListener(listener);
        installGdxLogger();
    }

    public static void stop() {
        DebugLogManager.setTestErrorListener(null);
        listener = null;
        restoreGdxLogger();
    }

    public static void setFailFast(boolean enabled) {
        failFast = enabled;
    }

    public static int criticalCount() {
        synchronized (captured) {
            return captured.size();
        }
    }

    public static List<String> samples() {
        synchronized (captured) {
            return Collections.unmodifiableList(new ArrayList<>(captured));
        }
    }

    public static String firstFailure() {
        return firstFailure;
    }

    /**
     * Call each simulation day (or after any engine step) so soaks abort immediately.
     */
    public static void assertNoCriticalLogs() {
        if (firstFailure != null || criticalCount() > 0) {
            throw new AssertionError(
                    "Simulation emitted failure log(s). First: " + firstFailure
                            + " Samples: " + samples());
        }
    }

    public static void reportFailure(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (!FAILURE.matcher(message).find()) {
            return;
        }
        synchronized (captured) {
            if (captured.size() < 50) {
                captured.add(message);
            }
            if (firstFailure == null) {
                firstFailure = message;
            }
        }
        if (failFast) {
            throw new AssertionError("Simulation log failure (fail-fast): " + message);
        }
    }

    private static void installGdxLogger() {
        if (Gdx.app == null) {
            return;
        }
        previousLogger = Gdx.app.getApplicationLogger();
        Gdx.app.setApplicationLogger(new ApplicationLogger() {
            @Override
            public void log(String tag, String message) {
                if (previousLogger != null) {
                    previousLogger.log(tag, message);
                }
            }

            @Override
            public void log(String tag, String message, Throwable exception) {
                if (previousLogger != null) {
                    previousLogger.log(tag, message, exception);
                }
                reportThrowable(tag, message, exception);
            }

            @Override
            public void error(String tag, String message) {
                if (previousLogger != null) {
                    previousLogger.error(tag, message);
                }
                reportFailure("[" + tag + "] ERROR " + message);
            }

            @Override
            public void error(String tag, String message, Throwable exception) {
                if (previousLogger != null) {
                    previousLogger.error(tag, message, exception);
                }
                reportThrowable(tag, message, exception);
            }

            @Override
            public void debug(String tag, String message) {
                if (previousLogger != null) {
                    previousLogger.debug(tag, message);
                }
            }

            @Override
            public void debug(String tag, String message, Throwable exception) {
                if (previousLogger != null) {
                    previousLogger.debug(tag, message, exception);
                }
            }
        });
    }

    private static void reportThrowable(String tag, String message, Throwable exception) {
        String detail = exception != null
                ? exception.getClass().getSimpleName() + ": " + exception.getMessage()
                : "";
        reportFailure("[" + tag + "] ERROR " + message + " | " + detail);
    }

    private static void restoreGdxLogger() {
        if (Gdx.app != null && previousLogger != null) {
            Gdx.app.setApplicationLogger(previousLogger);
        }
        previousLogger = null;
    }
}
