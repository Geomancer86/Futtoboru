package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class HarnessProgressTest {

    @Test
    void printsPercentAndEtaOnTickAndFinish() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);
        HarnessProgress progress = new HarnessProgress("demo", 4, out);

        progress.tick(1, "step=a");
        progress.tick(2, "step=b");
        progress.finish("PASS");

        String text = buf.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("PROGRESS|demo|done=0/4|pct=0.0"));
        assertTrue(text.contains("pct=25.0"));
        assertTrue(text.contains("pct=50.0"));
        assertTrue(text.contains("eta="));
        assertTrue(text.contains("result=PASS"));
        assertEquals(4, text.split("PROGRESS\\|", -1).length - 1);
    }

    @Test
    void formatDurationUsesMinutesAndHours() {
        assertEquals("5s", HarnessProgress.formatDuration(5));
        assertEquals("2m3s", HarnessProgress.formatDuration(123));
        assertEquals("1h5m", HarnessProgress.formatDuration(3900));
    }
}
