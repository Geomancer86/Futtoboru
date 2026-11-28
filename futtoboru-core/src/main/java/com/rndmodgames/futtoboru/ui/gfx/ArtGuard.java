// Adapted from DarkBlade (ArtGuard), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

/**
 * Simulation code never asks for art. Every door to the art (atlas lookups, code-generated sprites, ground art in the
 * viewer) calls {@link #check(String)}; it throws
 *
 * <ul>
 * <li>while the current thread runs a simulation tick ({@code MatchWorld.tick()} wraps every tick in
 * {@link #enterSimulation()} / {@link #exitSimulation()}), so a sim system that asks for a texture fails at once
 * instead of passing by luck (a cached region, a null without GL);</li>
 * <li>anywhere while a test has {@link #forbid(boolean) forbidden} art (headless tests that must not touch art at
 * all).</li>
 * </ul>
 *
 * Off in the game outside ticks: one boolean and one thread-local read per lookup.
 */
public final class ArtGuard {

    private static volatile boolean forbidden;
    private static final ThreadLocal<int[]> SIMULATING = ThreadLocal.withInitial(() -> new int[1]);

    private ArtGuard() {
    }

    /** Tests: asking for art throws while on, on every thread. */
    public static void forbid(boolean on) {
        forbidden = on;
    }

    public static boolean forbidden() {
        return forbidden;
    }

    /** The current thread starts a simulation tick (nests). */
    public static void enterSimulation() {
        SIMULATING.get()[0]++;
    }

    /** The current thread leaves a simulation tick. */
    public static void exitSimulation() {
        int[] depth = SIMULATING.get();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }

    /** Whether the current thread is inside a simulation tick. */
    public static boolean inSimulation() {
        return SIMULATING.get()[0] > 0;
    }

    /**
     * Throws while art is forbidden or the current thread runs a simulation tick.
     *
     * @param what the name asked for, for the message
     */
    public static void check(String what) {
        if (forbidden) {
            throw new IllegalStateException("art asked for while art is forbidden (headless test): " + what);
        }
        if (inSimulation()) {
            throw new IllegalStateException("art asked for from simulation code (inside a tick): " + what);
        }
    }
}
