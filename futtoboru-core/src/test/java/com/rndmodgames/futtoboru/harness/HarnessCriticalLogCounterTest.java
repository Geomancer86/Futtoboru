package com.rndmodgames.futtoboru.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HarnessCriticalLogCounterTest {

    @AfterEach
    void tearDown() {
        HarnessCriticalLogCounter.stop();
    }

    @Test
    void failFastOnCriticalLogLine() {
        HarnessCriticalLogCounter.start();
        AssertionError error = assertThrows(AssertionError.class,
                () -> HarnessCriticalLogCounter.reportFailure("CRITICAL ERROR: club has no players"));
        assertEquals(1, HarnessCriticalLogCounter.criticalCount());
        assertEquals("CRITICAL ERROR: club has no players", HarnessCriticalLogCounter.firstFailure());
        assertThrows(AssertionError.class, HarnessCriticalLogCounter::assertNoCriticalLogs);
    }
}
