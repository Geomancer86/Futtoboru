package com.rndmodgames.futtoboru.data.recruitment;

import java.time.LocalDate;
import java.util.List;

/**
 * R-02: a wage of {@code recruitment/wages.json}: a sourced {@code example} (one man's terms) or a {@code band} (the
 * plausible weekly range for a club standing in a span of years; most bands are {@code estimate}). Pence a week.
 * Read-only data: the contracts lane (G-04) and signing (R-07) use it.
 */
public final class WageRecord {

    private final String dataId;
    private final long runtimeId;
    private final boolean band;
    private final LocalDate from;
    private final LocalDate until;
    private final String standing;
    private final String clubDataId;
    private final String personDataId;
    private final Integer weeklyPence;
    private final Integer summerWeeklyPence;
    private final Integer minWeeklyPence;
    private final Integer maxWeeklyPence;
    private final String confidence;
    private final List<String> sources;

    public WageRecord(String dataId, long runtimeId, boolean band, LocalDate from, LocalDate until, String standing,
            String clubDataId, String personDataId, Integer weeklyPence, Integer summerWeeklyPence,
            Integer minWeeklyPence, Integer maxWeeklyPence, String confidence, List<String> sources) {
        this.dataId = dataId;
        this.runtimeId = runtimeId;
        this.band = band;
        this.from = from;
        this.until = until;
        this.standing = standing;
        this.clubDataId = clubDataId;
        this.personDataId = personDataId;
        this.weeklyPence = weeklyPence;
        this.summerWeeklyPence = summerWeeklyPence;
        this.minWeeklyPence = minWeeklyPence;
        this.maxWeeklyPence = maxWeeklyPence;
        this.confidence = confidence;
        this.sources = List.copyOf(sources);
    }

    /** @return true when the record covers that day ({@code until} exclusive) */
    public boolean covers(LocalDate day) {
        return !day.isBefore(from) && (until == null || day.isBefore(until));
    }

    public String getDataId() {
        return dataId;
    }

    public long getRuntimeId() {
        return runtimeId;
    }

    public boolean isBand() {
        return band;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getUntil() {
        return until;
    }

    /** @return {@code leading}, {@code middling}, {@code small} or {@code any} */
    public String getStanding() {
        return standing;
    }

    public String getClubDataId() {
        return clubDataId;
    }

    public String getPersonDataId() {
        return personDataId;
    }

    public Integer getWeeklyPence() {
        return weeklyPence;
    }

    public Integer getSummerWeeklyPence() {
        return summerWeeklyPence;
    }

    public Integer getMinWeeklyPence() {
        return minWeeklyPence;
    }

    public Integer getMaxWeeklyPence() {
        return maxWeeklyPence;
    }

    public String getConfidence() {
        return confidence;
    }

    public List<String> getSources() {
        return sources;
    }
}
