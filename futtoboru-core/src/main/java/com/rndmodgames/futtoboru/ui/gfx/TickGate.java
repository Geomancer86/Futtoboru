// Adapted from DarkBlade (TickGate), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

/**
 * "Work every N view ticks" for a draw pass, from the real frame delta (1 view tick = 1/60 s; this is display time,
 * not the 30 Hz simulation tick).
 *
 * <ul>
 * <li>{@link #open(float)} is true once every N ticks of elapsed time; a long frame does not open it twice (no catching
 * up: a visual that skips a beat is fine, one that piles up is not).</li>
 * <li>N usually comes from {@link GraphicsQuality#ticks(String)}, so it can change while the match is shown.</li>
 * </ul>
 */
public final class TickGate {

    public static final float TICK = 1f / 60f;

    private int ticks;
    private float elapsed;

    public TickGate(int ticks) {
        setTicks(ticks);
    }

    public void setTicks(int ticks) {
        this.ticks = Math.max(1, ticks);
    }

    public int ticks() {
        return ticks;
    }

    /** Advances by the frame's delta; true when the pass should work this frame. */
    public boolean open(float delta) {
        elapsed += Math.max(0f, delta);
        float period = ticks * TICK;
        if (elapsed + 1e-6f < period) {
            return false;
        }
        elapsed = Math.min(elapsed - period, period);
        return true;
    }

    /** The time a pass that only works when open should advance by: the gate's period. */
    public float period() {
        return ticks * TICK;
    }
}
