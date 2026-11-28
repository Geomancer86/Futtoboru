package com.rndmodgames.futtoboru.match.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * E-DET guard over all of {@code futtoboru-core/src/main/java}: seeded results must not depend on the JDK. JDK
 * generators whose algorithms are unspecified or may change ({@code SplittableRandom}, {@code java.util.random.*},
 * {@code ThreadLocalRandom}) are banned outright: game code draws from {@code RandomSource}, whose SplitMix64 core and
 * Gaussian are implemented in {@code SplittableRandomSource}. {@code nextGaussian} is banned in any file that uses
 * {@code java.util.Random} (cosmetic viewer randomness included), so every Gaussian comes from
 * {@code RandomSource.nextGaussian} (the JDK's {@code SplittableRandom.nextGaussian} differs between JDK 17 and 25).
 */
class JdkRandomnessGuardTest {

    private static final Path ROOT = Paths.get("src", "main", "java");
    /** RandomSource itself: declares nextGaussian and the java.util.Random view that delegates to it. */
    private static final String RANDOM_SOURCE = "com/rndmodgames/futtoboru/engine/random/";

    private static final Pattern JDK_GENERATOR = Pattern.compile(
            "\\bSplittableRandom\\b|\\bjava\\s*\\.\\s*util\\s*\\.\\s*random\\b|\\bRandomGenerator\\b|\\bThreadLocalRandom\\b");
    private static final Pattern USES_JAVA_RANDOM = Pattern.compile(
            "\\bimport\\s+java\\.util\\.(Random|\\*)\\s*;|\\bjava\\.util\\.Random\\b|\\bnew\\s+Random\\s*\\(|\\bextends\\s+Random\\b");
    private static final Pattern GAUSSIAN = Pattern.compile("\\bnextGaussian\\s*\\(");

    @Test
    void noJdkGeneratorsAndNoJdkGaussians() throws IOException {
        if (!Files.isDirectory(ROOT)) {
            return; // not run from the module directory
        }
        List<String> hits = new ArrayList<>();
        int scanned = 0;
        try (Stream<Path> files = Files.walk(ROOT)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String rel = ROOT.relativize(file).toString().replace('\\', '/');
                scanned++;
                hits.addAll(find(rel, Files.readString(file)));
            }
        }
        assertTrue(scanned >= 100, "scanned " + scanned);
        assertTrue(hits.isEmpty(), "JDK-dependent randomness:\n" + String.join("\n", hits));
    }

    @Test
    void detectorFindsJdkGeneratorsAndGaussians() {
        String src = String.join("\n",
                "import java.util.Random;",
                "class A {",
                "  Random r = new Random(1);",
                "  double g = r.nextGaussian();",
                "  java.util.SplittableRandom s = new java.util.SplittableRandom(2);",
                "  // nextGaussian() in a comment",
                "}");
        List<String> hits = find("A.java", src);
        assertEquals(2, hits.size(), String.join("\n", hits));
        assertTrue(hits.get(0).startsWith("A.java:5 JDK generator"), hits.get(0));
        assertTrue(hits.get(1).startsWith("A.java:4 nextGaussian"), hits.get(1));
        // a RandomSource Gaussian in a file without java.util.Random is fine
        assertTrue(find("B.java", "class B { double g(RandomSource r) { return r.nextGaussian(); } }").isEmpty());
        assertTrue(find(RANDOM_SOURCE + "SplittableRandomSource.java",
                "import java.util.Random; class C { double g() { return s.nextGaussian(); } }").isEmpty());
    }

    static List<String> find(String rel, String source) {
        String code = EcsDeterminismGuardTest.blankCommentsAndStrings(source);
        String[] lines = code.split("\n", -1);
        String[] raw = source.split("\n", -1);
        List<String> hits = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (JDK_GENERATOR.matcher(lines[i]).find()) {
                hits.add(rel + ":" + (i + 1) + " JDK generator (use RandomSource): " + raw[i].trim());
            }
        }
        if (!rel.startsWith(RANDOM_SOURCE) && USES_JAVA_RANDOM.matcher(code).find()) {
            for (int i = 0; i < lines.length; i++) {
                if (GAUSSIAN.matcher(lines[i]).find()) {
                    hits.add(rel + ":" + (i + 1) + " nextGaussian in a file using java.util.Random"
                            + " (use RandomSource.nextGaussian): " + raw[i].trim());
                }
            }
        }
        return hits;
    }
}
