package com.rndmodgames.futtoboru.system.data;

import java.util.Collections;
import java.util.List;

/**
 * H-05: a name a club or a person carried: a nickname ({@code period}: true when it was sourced as used at the
 * time, false for a later name) or one entry of a club's {@code nameHistory} (a name by season). Years are
 * inclusive; 0 = open. Read from {@code clubs.json} / {@code people.json}, no gameplay use yet.
 */
public final class HistoryName {

    private final String name;
    private final int from;
    private final int to;
    private final boolean period;
    private final List<String> sources;

    HistoryName(String name, int from, int to, boolean period, List<String> sources) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.period = period;
        this.sources = Collections.unmodifiableList(sources);
    }

    public String getName() {
        return name;
    }

    /** @return the first year it was used, or 0 when not known */
    public int getFrom() {
        return from;
    }

    /** @return the last year it was used, or 0 when still (or not known to have stopped) */
    public int getTo() {
        return to;
    }

    /** @return true when a source shows it in use at the time (the game shows only these in-season) */
    public boolean isPeriod() {
        return period;
    }

    public List<String> getSources() {
        return sources;
    }

    /** @return true when the name was in use in the given year (open ends count as open) */
    public boolean usedIn(int year) {
        return (from == 0 || year >= from) && (to == 0 || year <= to);
    }

    @Override
    public String toString() {
        return name;
    }
}
