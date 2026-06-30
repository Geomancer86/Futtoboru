package com.rndmodgames.futtoboru.harness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Counts CRITICAL simulation log lines during harness runs so tests fail on silent corruption.
 */
public final class HarnessCriticalLogCounter {

    private static final Pattern CRITICAL = Pattern.compile("CRITICAL", Pattern.CASE_INSENSITIVE);

    private static final List<String> captured = new ArrayList<>();
    private static Consumer<String> listener;

    private HarnessCriticalLogCounter() {
    }

    public static void start() {
        captured.clear();
        listener = message -> {
            if (message != null && CRITICAL.matcher(message).find()) {
                synchronized (captured) {
                    if (captured.size() < 50) {
                        captured.add(message);
                    }
                }
            }
        };
        DebugLogManager.setTestErrorListener(listener);
    }

    public static void stop() {
        DebugLogManager.setTestErrorListener(null);
        listener = null;
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

    public static void assertNoCriticalLogs() {
        int count = criticalCount();
        if (count > 0) {
            throw new AssertionError("Simulation emitted " + count + "+ CRITICAL log lines. Samples: " + samples());
        }
    }
}
