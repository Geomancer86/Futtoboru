package com.rndmodgames.futtoboru.system;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.Gdx;

/**
 * The one logging style of the game (P1-5).
 *
 * <pre>
 * Log.info(Log.ENGINE_CUP, "CupBracketManager", "Draw done: " + n + " ties");
 * Log.warn(Log.DATA_LOADING, "Missing optional file " + name);
 * Log.error(Log.DATA_SAVE, "SaveManager", "Could not write " + file, e);
 * </pre>
 *
 * Every line has a category (the constants below, the same ones as {@link DebugLogManager}).
 * info, debug and warn lines are printed only while their category is switched on in the
 * debug-logging settings ({@link DebugLogManager#isEnabled(String)}); errors are always printed,
 * with the stack trace when a {@link Throwable} is given, and are passed to the registered
 * {@link ErrorListener}s (tests use this to assert that nothing went wrong).
 *
 * Output goes through {@code Gdx.app} (its {@link com.badlogic.gdx.ApplicationLogger}) when an
 * application exists, otherwise straight to stdout/stderr with every category on, so plain unit
 * tests can log too. Do not use {@code System.out/err}, {@code printStackTrace()} or
 * {@code Gdx.app.log/debug/error} in main code: {@code LoggingStyleGuardTest} fails on them.
 */
public final class Log {

    // Engine
    public static final String ENGINE_GAME = DebugLogManager.CATEGORY_ENGINE_GAME;
    public static final String ENGINE_MATCH = DebugLogManager.CATEGORY_ENGINE_MATCH;
    public static final String ENGINE_CUP = DebugLogManager.CATEGORY_ENGINE_CUP;
    public static final String ENGINE_LEAGUE = DebugLogManager.CATEGORY_ENGINE_LEAGUE;
    public static final String ENGINE_SCHEDULING = DebugLogManager.CATEGORY_ENGINE_SCHEDULING;
    public static final String ENGINE_MESSAGES = DebugLogManager.CATEGORY_ENGINE_MESSAGES;
    public static final String ENGINE_JOBS = DebugLogManager.CATEGORY_ENGINE_JOBS;
    public static final String ENGINE_SCRIPTS = DebugLogManager.CATEGORY_ENGINE_SCRIPTS;
    public static final String ENGINE_AUTHORITY = DebugLogManager.CATEGORY_ENGINE_AUTHORITY;
    // UI
    public static final String UI_CUP = DebugLogManager.CATEGORY_UI_CUP;
    public static final String UI_LEAGUE = DebugLogManager.CATEGORY_UI_LEAGUE;
    public static final String UI_CLUB = DebugLogManager.CATEGORY_UI_CLUB;
    public static final String UI_MATCH = DebugLogManager.CATEGORY_UI_MATCH;
    public static final String UI_JOBS = DebugLogManager.CATEGORY_UI_JOBS;
    public static final String UI_INBOX = DebugLogManager.CATEGORY_UI_INBOX;
    public static final String UI_COMPETITIONS = DebugLogManager.CATEGORY_UI_COMPETITIONS;
    public static final String UI_SCHEDULE = DebugLogManager.CATEGORY_UI_SCHEDULE;
    public static final String UI_DRAW = DebugLogManager.CATEGORY_UI_DRAW;
    public static final String UI_MENU = DebugLogManager.CATEGORY_UI_MENU;
    // Data
    public static final String DATA_LOADING = DebugLogManager.CATEGORY_DATA_LOADING;
    public static final String DATA_SAVE = DebugLogManager.CATEGORY_DATA_SAVE;
    public static final String DATA_GENERATION = DebugLogManager.CATEGORY_DATA_GENERATION;
    // System
    public static final String SYSTEM_INIT = DebugLogManager.CATEGORY_SYSTEM_INIT;
    public static final String SYSTEM_CONFIG = DebugLogManager.CATEGORY_SYSTEM_CONFIG;

    /** Receives every error logged through {@link Log}, whatever the output. */
    public interface ErrorListener {
        void onError(String category, String tag, String message, Throwable error);
    }

    private static final List<ErrorListener> ERROR_LISTENERS = new CopyOnWriteArrayList<>();

    private Log() {
    }

    public static void addErrorListener(ErrorListener listener) {
        ERROR_LISTENERS.add(listener);
    }

    public static void removeErrorListener(ErrorListener listener) {
        ERROR_LISTENERS.remove(listener);
    }

    // ---- info ----

    public static void info(String category, String message) {
        info(category, null, message);
    }

    public static void info(String category, String tag, String message) {
        if (isEnabled(category)) {
            out(category, tag, message);
        }
    }

    // ---- debug ----

    public static void debug(String category, String message) {
        debug(category, null, message);
    }

    public static void debug(String category, String tag, String message) {
        if (isEnabled(category)) {
            out(category, tag, "[DEBUG] " + message);
        }
    }

    // ---- warn ----

    public static void warn(String category, String message) {
        warn(category, null, message);
    }

    public static void warn(String category, String tag, String message) {
        if (isEnabled(category)) {
            out(category, tag, "[WARN] " + message);
        }
    }

    // ---- error (always printed) ----

    public static void error(String category, String message) {
        error(category, null, message, null);
    }

    public static void error(String category, String tag, String message) {
        error(category, tag, message, null);
    }

    public static void error(String category, String message, Throwable error) {
        error(category, null, message, error);
    }

    /** Logs a caught exception with its stack trace (the replacement for {@code e.printStackTrace()}). */
    public static void error(String category, Throwable error) {
        error(category, null, String.valueOf(error), error);
    }

    public static void error(String category, String tag, String message, Throwable error) {
        String fullTag = tag(category, tag);
        if (Gdx.app != null) {
            if (error != null) {
                Gdx.app.error(fullTag, message, error);
            } else {
                Gdx.app.error(fullTag, message);
            }
        } else {
            System.err.println(fullTag + ": " + message);
            if (error != null) {
                error.printStackTrace(System.err);
            }
        }
        for (ErrorListener listener : ERROR_LISTENERS) {
            listener.onError(category, tag, message, error);
        }
    }

    // ---- internals ----

    /** True when info/debug/warn lines of this category are printed. */
    public static boolean isEnabled(String category) {
        if (Gdx.app == null) {
            return true;
        }
        return DebugLogManager.getInstance().isEnabled(category);
    }

    private static void out(String category, String tag, String message) {
        String fullTag = tag(category, tag);
        if (Gdx.app != null) {
            Gdx.app.log(fullTag, message);
        } else {
            System.out.println(fullTag + ": " + message);
        }
    }

    private static String tag(String category, String tag) {
        return tag == null || tag.isEmpty() ? category : category + "/" + tag;
    }
}
