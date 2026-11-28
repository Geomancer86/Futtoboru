package com.rndmodgames.futtoboru.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** E-21a: quality knobs (shipped file, live switch, switches, kill flag, renderer), TickGate cadence, CameraZoom. */
class GfxKnobsTest {

    private static final String SMALL = "{\"schemaVersion\":1,\"default\":\"medium\",\"modules\":{"
            + "\"particles\":{\"on\":1,\"ticks\":[4,2,1,1],\"cap\":[10,20,30,40]},"
            + "\"fog\":{\"on\":[0,1,1,1]},"
            + "\"shadows\":{}}}";

    @AfterEach
    void clearProperties() {
        System.clearProperty(GraphicsQuality.QUALITY_PROPERTY);
    }

    // ---- GraphicsQuality ------------------------------------------------------------------------------------------

    @Test
    void theShippedFileLoadsWithEveryPlannedModule() {
        GraphicsQuality q = GraphicsQuality.load();
        assertEquals(GraphicsQuality.HIGH, q.quality());
        assertEquals(List.of("particles", "crowd", "weather", "fog", "light", "shadows", "marks", "skyline", "smoke", "camera_fx"),
                q.modules());
        for (String module : q.modules()) {
            assertTrue(q.knobs(module).contains(GraphicsQuality.ON), module + " has no on knob");
            for (String level : GraphicsQuality.QUALITIES) {
                q.select(level);
                assertTrue(q.on(module), module + " off at " + level);
                if (q.knobs(module).contains(GraphicsQuality.TICKS)) {
                    assertTrue(q.ticks(module) >= 1);
                }
            }
        }
        assertEquals(q.modules(), q.switches().modules());
    }

    @Test
    void levelsSwitchLiveAndUnknownOnesAreIgnored() {
        GraphicsQuality q = GraphicsQuality.parse(SMALL, false);
        assertEquals(GraphicsQuality.MEDIUM, q.quality());
        assertEquals(20f, q.knob("particles", "cap"));
        assertEquals(2, q.ticks("particles"));
        assertTrue(q.select(GraphicsQuality.LOW));
        assertEquals(10f, q.knob("particles", "cap"));
        assertEquals(4, q.ticks("particles"));
        assertFalse(q.on("fog"), "fog's on knob is 0 at low");
        assertFalse(q.select("bajo"));
        assertEquals(GraphicsQuality.LOW, q.quality());
        assertTrue(q.select(GraphicsQuality.ULTRA));
        assertTrue(q.on("fog"));
        assertTrue(q.on("shadows"), "no on knob = on");
        assertTrue(q.on("not-a-module"), "a name without knobs or switch is on");
        assertThrows(IllegalArgumentException.class, () -> q.knob("particles", "radius"));
        assertThrows(IllegalArgumentException.class, () -> q.knob("nope", "cap"));
    }

    @Test
    void moduleMasterAndKillSwitches() {
        GraphicsQuality q = GraphicsQuality.parse(SMALL, false);
        VfxSwitches s = q.switches();
        s.setModule("particles", false);
        assertFalse(q.on("particles"));
        assertTrue(q.on("shadows"));
        s.setModule("particles", true);
        s.setMaster(false);
        assertFalse(q.on("particles"));
        assertFalse(q.on("shadows"));
        assertTrue(s.module("particles"), "the module's own switch is kept under the master");
        s.reset();
        assertTrue(q.on("particles"));
        assertThrows(IllegalArgumentException.class, () -> s.setModule("nope", false));

        GraphicsQuality killed = GraphicsQuality.parse(SMALL, true);
        assertTrue(killed.switches().forcedOff());
        for (String module : killed.modules()) {
            assertFalse(killed.on(module), module);
        }
    }

    @Test
    void softwareRendererDropsToLowUnlessTheCommandLineSaysOtherwise() {
        assertTrue(GraphicsQuality.isSoftwareRenderer("llvmpipe (LLVM 15.0.7, 256 bits)"));
        assertTrue(GraphicsQuality.isSoftwareRenderer("GDI Generic"));
        assertFalse(GraphicsQuality.isSoftwareRenderer("NVIDIA GeForce GTX 1060/PCIe/SSE2"));
        assertFalse(GraphicsQuality.isSoftwareRenderer(null));

        GraphicsQuality q = GraphicsQuality.parse(SMALL, false);
        assertEquals(GraphicsQuality.LOW, q.adoptRenderer("llvmpipe"));
        assertEquals(GraphicsQuality.MEDIUM, q.adoptRenderer("Some GPU"));

        System.setProperty(GraphicsQuality.QUALITY_PROPERTY, GraphicsQuality.ULTRA);
        assertEquals(GraphicsQuality.ULTRA, q.adoptRenderer("llvmpipe"));
        assertEquals(GraphicsQuality.ULTRA, GraphicsQuality.parse(SMALL, false).quality());
    }

    @Test
    void parsingIsStrict() {
        assertThrows(IllegalArgumentException.class,
                () -> GraphicsQuality.parse("{\"schemaVersion\":1,\"modules\":{},\"extra\":1}", false));
        assertThrows(IllegalArgumentException.class,
                () -> GraphicsQuality.parse("{\"schemaVersion\":2,\"modules\":{}}", false));
        assertThrows(IllegalArgumentException.class,
                () -> GraphicsQuality.parse("{\"schemaVersion\":1,\"default\":\"alto\",\"modules\":{}}", false));
        assertThrows(IllegalArgumentException.class,
                () -> GraphicsQuality.parse("{\"schemaVersion\":1,\"modules\":{\"fog\":{\"ticks\":[1,2]}}}", false));
        assertThrows(IllegalArgumentException.class,
                () -> GraphicsQuality.parse("{\"schemaVersion\":1,\"modules\":{\"fog\":{\"ticks\":\"x\"}}}", false));
    }

    // ---- TickGate -------------------------------------------------------------------------------------------------

    @Test
    void tickGateOpensOnceEveryNTicksAndNeverCatchesUp() {
        TickGate gate = new TickGate(2);
        int opened = 0;
        StringBuilder pattern = new StringBuilder();
        for (int frame = 0; frame < 60; frame++) {
            boolean open = gate.open(TickGate.TICK);
            pattern.append(open ? 'x' : '.');
            opened += open ? 1 : 0;
        }
        assertEquals(30, opened);
        assertTrue(pattern.toString().startsWith(".x.x.x"), pattern.toString());

        TickGate slow = new TickGate(3);
        assertTrue(slow.open(1f), "a long frame opens once");
        int afterLongFrame = 0;
        for (int frame = 0; frame < 3; frame++) {
            afterLongFrame += slow.open(TickGate.TICK) ? 1 : 0;
        }
        assertTrue(afterLongFrame <= 2, "no burst of catch-up opens: " + afterLongFrame);

        TickGate live = new TickGate(1);
        assertTrue(live.open(TickGate.TICK));
        live.setTicks(4);
        assertEquals(4 * TickGate.TICK, live.period(), 1e-7);
        int opens = 0;
        for (int frame = 0; frame < 8; frame++) {
            opens += live.open(TickGate.TICK) ? 1 : 0;
        }
        assertEquals(2, opens);
        live.setTicks(0);
        assertEquals(1, live.ticks());
        assertFalse(new TickGate(1).open(-1f), "negative deltas count as zero");
    }

    // ---- CameraZoom -----------------------------------------------------------------------------------------------

    @Test
    void cameraZoomSnapsToWholePixels() {
        assertEquals(1f, CameraZoom.wholePixel(1f));
        assertEquals(0.5f, CameraZoom.wholePixel(0.45f));
        assertEquals(1f / 3, CameraZoom.wholePixel(0.3f), 1e-6);
        assertEquals(2f, CameraZoom.wholePixel(2.4f));
        assertEquals(3f, CameraZoom.wholePixel(2.6f));
        assertEquals(1f / 10000, CameraZoom.wholePixel(0f), 1e-9);
        assertEquals(0.25f, CameraZoom.forMagnification(4));
        assertEquals(1f, CameraZoom.forMagnification(0));
    }
}
