package com.rndmodgames.futtoboru.match.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * E-10 guard (docs/design/MATCH_ENGINE_ECS.md §2, §2.1): simulation code under {@code match/ecs} (except the viewer
 * package {@code match/ecs/view}) is headless and deterministic. It fails on Gdx graphics or {@code Gdx} itself,
 * unseeded randomness, wall clocks, hash-ordered collections, CPU-dependent {@code Math} transcendentals (use
 * {@code StrictMath}), fused multiply-add, parallel streams and mutable static fields.
 */
class EcsDeterminismGuardTest {

    private static final Path ROOT = Paths.get("src", "main", "java", "com", "rndmodgames", "futtoboru", "match",
            "ecs");

    static final Map<String, Pattern> FORBIDDEN = new LinkedHashMap<>();

    static {
        FORBIDDEN.put("Gdx graphics", Pattern.compile("\\bcom\\.badlogic\\.gdx\\.(graphics|Gdx\\b)"));
        FORBIDDEN.put("Gdx static access", Pattern.compile("\\bGdx\\s*\\."));
        FORBIDDEN.put("java.util.Random / ThreadLocalRandom",
                Pattern.compile("\\bjava\\.util\\.(Random|concurrent\\.ThreadLocalRandom)\\b|\\bnew\\s+Random\\s*\\("));
        FORBIDDEN.put("Math.random", Pattern.compile("\\bMath\\s*\\.\\s*random\\s*\\("));
        FORBIDDEN.put("wall clock", Pattern.compile(
                "\\bSystem\\s*\\.\\s*(nanoTime|currentTimeMillis)\\s*\\(|\\bInstant\\s*\\.\\s*now|\\bLocalDateTime\\s*\\.\\s*now"));
        FORBIDDEN.put("HashMap/HashSet (iteration order)", Pattern.compile("(?<!Linked)\\bHash(Map|Set)\\b"));
        FORBIDDEN.put("Math transcendental (use StrictMath)", Pattern.compile(
                "(?<!Strict)\\bMath\\s*\\.\\s*(exp|expm1|log|log10|log1p|pow|sin|cos|tan|asin|acos|atan|atan2|sinh|cosh|tanh|cbrt|hypot)\\s*\\("));
        FORBIDDEN.put("fused multiply-add", Pattern.compile("\\bfma\\s*\\("));
        FORBIDDEN.put("parallel stream", Pattern.compile("\\bparallel(Stream)?\\s*\\("));
        FORBIDDEN.put("mutable static field", Pattern.compile(
                "\\bstatic\\s+(?!final\\b)(?!class\\b|interface\\b|enum\\b|record\\b|\\{)[\\w<>\\[\\],.?\\s]*?\\b\\w+\\s*(=|;)"));
    }

    @Test
    void simulationCodeIsHeadlessAndDeterministic() throws IOException {
        if (!Files.isDirectory(ROOT)) {
            return; // not run from the module directory
        }
        List<String> hits = new ArrayList<>();
        int scanned = 0;
        try (Stream<Path> files = Files.walk(ROOT)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String rel = ROOT.relativize(file).toString().replace('\\', '/');
                if (rel.startsWith("view/")) {
                    continue; // draw passes (E-21) may use Gdx graphics
                }
                scanned++;
                hits.addAll(find(rel, Files.readString(file)));
            }
        }
        assertTrue(scanned >= 10, "scanned " + scanned);
        assertTrue(hits.isEmpty(), "non-deterministic or non-headless code in match/ecs:\n" + String.join("\n", hits));
    }

    @Test
    void detectorFindsCodeButNotCommentsOrStrings() {
        String src = String.join("\n",
                "import java.util.HashMap;",
                "import java.util.LinkedHashMap;",
                "class A {",
                "  static int counter = 0;",
                "  static final int OK = 1;",
                "  static class Nested {}",
                "  static double f(double x) { return Math.exp(x) + StrictMath.exp(x) + Math.sqrt(x); }",
                "  // Math.sin(x) in a comment, System.nanoTime()",
                "  /* new Random() */",
                "  String s = \"Math.cos(1)\";",
                "  long t = System.nanoTime();",
                "}");
        List<String> hits = find("A.java", src);
        assertEquals(4, hits.size(), String.join("\n", hits));
        assertTrue(hits.get(0).contains("HashMap/HashSet") && hits.get(0).contains(":1"), hits.get(0));
        assertTrue(String.join("\n", hits).contains("A.java:4 mutable static field"));
        assertTrue(String.join("\n", hits).contains("A.java:7 Math transcendental"));
        assertTrue(String.join("\n", hits).contains("A.java:11 wall clock"));
    }

    /** Hits as {@code file:line rule}, with comments and string literals blanked (line numbers kept). */
    static List<String> find(String rel, String source) {
        String code = blankCommentsAndStrings(source);
        String[] lines = code.split("\n", -1);
        List<String> hits = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            for (Map.Entry<String, Pattern> rule : FORBIDDEN.entrySet()) {
                Matcher m = rule.getValue().matcher(lines[i]);
                if (m.find()) {
                    hits.add(rel + ":" + (i + 1) + " " + rule.getKey() + ": " + source.split("\n", -1)[i].trim());
                }
            }
        }
        return hits;
    }

    static String blankCommentsAndStrings(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/') {
                while (i < s.length() && s.charAt(i) != '\n') {
                    out.append(' ');
                    i++;
                }
            } else if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
                int end = s.indexOf("*/", i + 2);
                end = end < 0 ? s.length() : end + 2;
                for (; i < end; i++) {
                    out.append(s.charAt(i) == '\n' ? '\n' : ' ');
                }
            } else if (c == '"' || c == '\'') {
                out.append(' ');
                i++;
                while (i < s.length() && s.charAt(i) != c && s.charAt(i) != '\n') {
                    if (s.charAt(i) == '\\') {
                        out.append(' ');
                        i++;
                    }
                    out.append(' ');
                    i++;
                }
                if (i < s.length() && s.charAt(i) == c) {
                    out.append(' ');
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
