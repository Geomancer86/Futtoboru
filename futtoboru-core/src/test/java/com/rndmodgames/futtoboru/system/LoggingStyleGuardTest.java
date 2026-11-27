package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * P1-5: main code logs through {@link Log} only. Fails on a new {@code System.out/err.print*},
 * {@code printStackTrace(...)} or {@code Gdx.app.log/debug/error} outside the allow-list.
 * Tests may log however they like.
 */
class LoggingStyleGuardTest {

    /** Main source roots, relative to the core module directory (where Surefire runs). */
    private static final List<Path> MAIN_ROOTS = List.of(
            Paths.get("src", "main", "java"),
            Paths.get("..", "futtoboru-desktop", "src", "main", "java"));

    /** Files allowed to bypass Log, with the reason. */
    private static final Map<String, String> ALLOW_LIST = Map.of(
            "com/rndmodgames/futtoboru/system/Log.java",
            "the facade itself: writes to Gdx.app, or stdout/stderr when no application exists",
            "com/rndmodgames/desktop/DesktopLauncher.java",
            "startup and uncaught-exception handler: runs before Gdx.app exists or after it died");

    static final Map<String, Pattern> FORBIDDEN = Map.of(
            "System.out/err", Pattern.compile("\\bSystem\\s*\\.\\s*(out|err)\\s*\\.\\s*(print|println|printf|format)\\s*\\("),
            "printStackTrace", Pattern.compile("\\.\\s*printStackTrace\\s*\\("),
            "Gdx.app.log/debug/error", Pattern.compile("\\bGdx\\s*\\.\\s*app\\s*\\.\\s*(log|debug|error)\\s*\\("));

    @Test
    void mainCodeLogsThroughLogOnly() throws IOException {
        List<String> hits = new ArrayList<>();
        int scanned = 0;
        for (Path root : MAIN_ROOTS) {
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                    scanned++;
                    String rel = root.relativize(file).toString().replace('\\', '/');
                    if (ALLOW_LIST.containsKey(rel)) {
                        continue;
                    }
                    hits.addAll(findForbidden(rel, Files.readString(file)));
                }
            }
        }
        if (scanned == 0) {
            return; // not run from the module directory
        }
        assertTrue(hits.isEmpty(), "use Log.info/debug/warn/error(Log.<CATEGORY>, ...) instead of:\n"
                + String.join("\n", hits));
    }

    @Test
    void detectorFindsCodeButNotComments() {
        String src = String.join("\n",
                "class A {",
                "  void f() {",
                "    System.out.println(\"x\");",
                "    // System.err.println(\"commented out\");",
                "    /* e.printStackTrace(); */",
                "    try { g(); } catch (Exception e) { e.printStackTrace(); }",
                "    Gdx.app.error(\"T\", \"m\");",
                "    Log.error(Log.SYSTEM_INIT, e);",
                "  }",
                "}");
        List<String> hits = findForbidden("A.java", src);
        assertEquals(3, hits.size(), hits.toString());
    }

    /** "file:line: kind: code" for every forbidden call outside comments. */
    static List<String> findForbidden(String relPath, String source) {
        List<String> hits = new ArrayList<>();
        String[] lines = stripComments(source).split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            for (Map.Entry<String, Pattern> e : FORBIDDEN.entrySet()) {
                if (e.getValue().matcher(lines[i]).find()) {
                    hits.add(relPath + ":" + (i + 1) + ": " + e.getKey() + ": " + lines[i].trim());
                }
            }
        }
        return hits;
    }

    /** Blanks comments and string literals, keeping line breaks so line numbers stay right. */
    static String stripComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/') {
                while (i < s.length() && s.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
                int end = s.indexOf("*/", i + 2);
                end = end < 0 ? s.length() : end + 2;
                for (; i < end; i++) {
                    if (s.charAt(i) == '\n') {
                        out.append('\n');
                    }
                }
            } else if (c == '"' || c == '\'') {
                out.append(c);
                i++;
                while (i < s.length() && s.charAt(i) != c && s.charAt(i) != '\n') {
                    if (s.charAt(i) == '\\') {
                        i++;
                    }
                    i++;
                }
                if (i < s.length() && s.charAt(i) == c) {
                    out.append(c);
                    i++;
                }
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}
