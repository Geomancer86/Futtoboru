package com.rndmodgames.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * T-01 smoke tier (D27): runs the real desktop jar ({@code -Psmoke}, after {@code package}) with
 * {@code -Dfuttoboru.smoke=true}: the window opens, the game data loads, the main menu renders, and the game quits
 * cleanly with exit code 0. Skipped when there is no display (Linux without DISPLAY, or GLFW cannot start).
 */
@Tag("smoke")
class DesktopSmokeIT {

    private static final long TIMEOUT_SECONDS = 120;

    @Test
    void theGameStartsShowsTheMainMenuAndExitsCleanly() throws IOException, InterruptedException {
        boolean linux = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("linux");
        assumeTrue(!linux || System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null,
                "no display (run under xvfb-run)");

        File module = new File(System.getProperty("basedir", "."));
        File jar = findJar(new File(module, "target"));
        assertNotNull(jar, "no *-jar-with-dependencies.jar in " + module + "/target (run the package phase first)");

        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        ProcessBuilder builder = new ProcessBuilder(java, "-D" + SmokeRun.PROPERTY + "=true", "-jar",
                jar.getAbsolutePath());
        builder.directory(module.getAbsoluteFile().getParentFile()); // the repo root, as run-game does
        builder.redirectErrorStream(true);
        long start = System.currentTimeMillis();
        Process process = builder.start();
        process.getOutputStream().close(); // EOF for the launcher's "Press Enter" after a start-up error

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> copy(process.getInputStream(), output), "smoke-output");
        reader.start();
        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
        }
        reader.join(5_000);
        String log = new String(output.toByteArray(), StandardCharsets.UTF_8);
        long millis = System.currentTimeMillis() - start;
        System.out.println("smoke run: " + millis + " ms, exit " + (finished ? process.exitValue() : "timeout"));

        assertTrue(finished, "the game did not exit within " + TIMEOUT_SECONDS + " s" + tail(log));
        assumeTrue(process.exitValue() != SmokeRun.EXIT_NO_DISPLAY, "no display: GLFW could not start" + tail(log));
        assertEquals(0, process.exitValue(), "exit code" + tail(log));
        assertTrue(log.contains(SmokeRun.OK_LINE), "the main menu was not shown" + tail(log));
    }

    private static File findJar(File target) {
        File[] jars = target.listFiles((dir, name) -> name.endsWith("-jar-with-dependencies.jar"));
        return jars == null || jars.length == 0 ? null : jars[0];
    }

    private static void copy(InputStream in, ByteArrayOutputStream out) {
        byte[] buffer = new byte[8192];
        try {
            for (int n; (n = in.read(buffer)) > 0;) {
                synchronized (out) {
                    out.write(buffer, 0, n);
                }
            }
        } catch (IOException ignored) {
            // the process ended
        }
    }

    private static String tail(String log) {
        int from = Math.max(0, log.length() - 4000);
        return System.lineSeparator() + "--- game output (tail) ---" + System.lineSeparator() + log.substring(from);
    }
}
