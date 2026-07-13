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
