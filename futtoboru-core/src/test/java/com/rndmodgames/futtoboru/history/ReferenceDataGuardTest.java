package com.rndmodgames.futtoboru.history;

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
 * D17: the real results ({@code results.json}, {@code result} records) are reference data. In main code only the
 * {@code history} package (the history screens' data) reads them; engine calibration reads them in test code.
 * Fails when anything else in main code reads them, so they can't drift into scheduling or deciding a match.
 */
class ReferenceDataGuardTest {

    private static final Path MAIN = Paths.get("src", "main", "java");
    private static final String ALLOWED = "com/rndmodgames/futtoboru/history/";
    /** GameData only declares the file kind and its field spec. */
    private static final String LOADER = "com/rndmodgames/futtoboru/system/data/GameData.java";
    static final Pattern READS = Pattern.compile("records\\s*\\(\\s*\"result\"|\\bReferenceResults\\b|results\\.json");

    @Test
    void onlyTheHistoryPackageReadsRealResults() throws IOException {
        if (!Files.isDirectory(MAIN)) {
            return; // not run from the module directory
        }
        List<String> hits = new ArrayList<>();
        int scanned = 0;
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path f : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                scanned++;
                String rel = MAIN.relativize(f).toString().replace('\\', '/');
                if (rel.startsWith(ALLOWED) || rel.equals(LOADER)) {
                    continue;
                }
                if (READS.matcher(Files.readString(f)).find()) {
                    hits.add(rel);
                }
            }
        }
        assertTrue(scanned > 100, "scanned the main sources");
        assertTrue(hits.isEmpty(), "real results are reference data (D17); read them through history/ only:\n"
                + String.join("\n", hits));
    }

    @Test
    void detectorFindsReads() {
        assertTrue(READS.matcher("data.records(\"result\", season)").find());
        assertTrue(READS.matcher("ReferenceResults.entries(data, s)").find());
        assertTrue(!READS.matcher("data.records(\"squad\", season)").find());
    }
}
