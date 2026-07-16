package com.rndmodgames.futtoboru.harness;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Always-on progress meter for harness / long-run tests.
 * Writes to {@link System#err} so quiet {@code System.out} suppression does not hide progress.
 */
public final class HarnessProgress {

    private final PrintStream out;
    private final String label;
    private final int totalUnits;
    private final long startNs;
    private int completedUnits;
    private long lastUnitNs;
    private long lastHeartbeatNs;
    private double lastUnitDurationSec = -1;

    /**
     * Minimum wall-clock gap between two {@link #heartbeat(String)} lines. A single unit (e.g. one
     * league season) can legitimately take well over a minute; without a heartbeat the console shows
     * nothing for that whole time, which testers have repeatedly (and reasonably) read as "this looks
     * hung" rather than "this is still working." Keeping this short (not tied to unit completion) is
     * the point.
     */
    private static final long HEARTBEAT_MIN_INTERVAL_NS = 5_000_000_000L;

    public HarnessProgress(String label, int totalUnits) {
        this(label, totalUnits, System.err);
    }

    public HarnessProgress(String label, int totalUnits, PrintStream out) {
        this.label = label != null ? label : "TEST";
        this.totalUnits = Math.max(1, totalUnits);
        this.out = out != null ? out : System.err;
        this.startNs = System.nanoTime();
        this.lastUnitNs = startNs;
        this.completedUnits = 0;
        printStart();
    }

    public void tick(int completedUnits) {
        tick(completedUnits, null);
    }

    public void tick(int completedUnits, String detail) {
        int clamped = Math.max(0, Math.min(completedUnits, totalUnits));
        long now = System.nanoTime();
        double unitSeconds = (now - lastUnitNs) / 1_000_000_000.0;
        lastUnitDurationSec = unitSeconds;
        lastUnitNs = now;
        this.completedUnits = clamped;

        double elapsedSec = (now - startNs) / 1_000_000_000.0;
        double pct = 100.0 * clamped / totalUnits;
        int remaining = totalUnits - clamped;
        double avgSecPerUnit = clamped > 0 ? elapsedSec / clamped : 0;
        double etaSec = remaining * avgSecPerUnit;

        StringBuilder line = new StringBuilder();
        line.append("PROGRESS|").append(label)
                .append("|done=").append(clamped).append('/').append(totalUnits)
                .append("|pct=").append(String.format(Locale.US, "%.1f", pct))
                .append("|elapsed=").append(formatDuration(elapsedSec))
                .append("|eta=").append(clamped == 0 ? "?" : formatDuration(etaSec))
                .append("|last=").append(String.format(Locale.US, "%.1fs", unitSeconds));
        if (detail != null && !detail.isBlank()) {
            line.append('|').append(detail);
        }
        out.println(line);
        out.flush();
    }

    /**
     * Print a lightweight "still working" line partway through the *current* unit (e.g. once per
     * simulated day while a single season is being simulated), throttled to at most once every
     * {@link #HEARTBEAT_MIN_INTERVAL_NS}. Safe to call as often as needed (e.g. every loop
     * iteration) — cheap no-op calls between prints are just a nanoTime() read and a comparison.
     * Unlike {@link #tick(int, String)}, this does not advance {@link #completedUnits}.
     *
     * <p>Includes both a per-unit ETA (how much longer the *current* season/unit is expected to take)
     * and a whole-run ETA, both paced off the most recently *completed* unit's duration rather than the
     * run-wide average — this tracks a still-accelerating late-soak trend far more responsively than an
     * average would, which is the actual question testers keep asking ("how much longer for *this*
     * season, and for the whole run").
     */
    public void heartbeat(String detail) {
        long now = System.nanoTime();
        if (lastHeartbeatNs != 0 && (now - lastHeartbeatNs) < HEARTBEAT_MIN_INTERVAL_NS) {
            return;
        }
        lastHeartbeatNs = now;

        double elapsedSec = (now - startNs) / 1_000_000_000.0;
        double pct = 100.0 * completedUnits / totalUnits;
        double elapsedInUnitSec = (now - lastUnitNs) / 1_000_000_000.0;
        int remainingAfterCurrent = totalUnits - completedUnits - 1;

        String unitEta;
        String totalEta;
        if (lastUnitDurationSec > 0) {
            double unitEtaSec = Math.max(0, lastUnitDurationSec - elapsedInUnitSec);
            unitEta = formatDuration(unitEtaSec);
            totalEta = formatDuration(unitEtaSec + Math.max(0, remainingAfterCurrent) * lastUnitDurationSec);
        } else {
            unitEta = "?";
            totalEta = "?";
        }

        StringBuilder line = new StringBuilder();
        line.append("PROGRESS|").append(label)
                .append("|done=").append(completedUnits).append('/').append(totalUnits)
                .append("|pct=").append(String.format(Locale.US, "%.1f", pct))
                .append("|elapsed=").append(formatDuration(elapsedSec))
                .append("|still-working=true")
                .append("|current_unit_elapsed=").append(formatDuration(elapsedInUnitSec))
                .append("|current_unit_eta=").append(unitEta)
                .append("|total_eta=").append(totalEta);
        if (detail != null && !detail.isBlank()) {
            line.append('|').append(detail);
        }
        out.println(line);
        out.flush();
    }

    public void finish(String result) {
        double elapsedSec = (System.nanoTime() - startNs) / 1_000_000_000.0;
        out.println("PROGRESS|" + label
                + "|done=" + completedUnits + '/' + totalUnits
                + "|pct=" + String.format(Locale.US, "%.1f", 100.0 * completedUnits / totalUnits)
                + "|elapsed=" + formatDuration(elapsedSec)
                + "|eta=0s"
                + "|result=" + (result != null ? result : "DONE"));
        out.flush();
    }

    private void printStart() {
        out.println("PROGRESS|" + label
                + "|done=0/" + totalUnits
                + "|pct=0.0"
                + "|elapsed=0s"
                + "|eta=?"
                + "|phase=start");
        out.flush();
    }

    static String formatDuration(double seconds) {
        if (seconds < 0 || Double.isNaN(seconds) || Double.isInfinite(seconds)) {
            return "?";
        }
        long total = Math.round(seconds);
        if (total < 60) {
            return total + "s";
        }
        long mins = total / 60;
        long secs = total % 60;
        if (mins < 60) {
            return mins + "m" + secs + "s";
        }
        long hours = mins / 60;
        mins = mins % 60;
        return hours + "h" + mins + "m";
    }
}
