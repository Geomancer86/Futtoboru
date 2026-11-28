package com.rndmodgames.futtoboru.data.recruitment;

import java.time.LocalDate;
import java.util.List;

/**
 * R-02: one registration rule of {@code recruitment/rules.json} (FA, League or SFA), in force from {@link #getFrom()}
 * until the day before {@link #getUntil()} (null = still in force). Read-only data; R-07 applies it to signings.
 * Money is in pence (240 to the pound).
 */
public final class RegistrationRule {

    /** The rule kinds of {@code rules.json} (data id = the enum name in lower case with dashes). */
    public enum Kind {
        REGISTRATION, RESIDENCE, SECOND_REGISTRATION_CONSENT, REGISTRATION_WAIT, SCOTTISH_PROFESSIONAL_BAN,
        RETAIN_AND_TRANSFER, MAX_WAGE;

        public String dataId() {
            return name().toLowerCase().replace('_', '-');
        }

        public static Kind fromDataId(String id) {
            for (Kind k : values()) {
                if (k.dataId().equals(id)) {
                    return k;
                }
            }
            return null;
        }
    }

    private final String dataId;
    private final long runtimeId;
    private final Kind kind;
    private final LocalDate from;
    private final LocalDate until;
    private final String body;
    private final String appliesTo;
    private final Integer days;
    private final Integer miles;
    private final Integer years;
    private final Integer pence;
    private final String confidence;
    private final List<String> sources;

    public RegistrationRule(String dataId, long runtimeId, Kind kind, LocalDate from, LocalDate until, String body,
            String appliesTo, Integer days, Integer miles, Integer years, Integer pence, String confidence,
            List<String> sources) {
        this.dataId = dataId;
        this.runtimeId = runtimeId;
        this.kind = kind;
        this.from = from;
        this.until = until;
        this.body = body;
        this.appliesTo = appliesTo;
        this.days = days;
        this.miles = miles;
        this.years = years;
        this.pence = pence;
        this.confidence = confidence;
        this.sources = List.copyOf(sources);
    }

    /** @return true when the rule is in force on that day */
    public boolean inForceOn(LocalDate day) {
        return !day.isBefore(from) && (until == null || day.isBefore(until));
    }

    public String getDataId() {
        return dataId;
    }

    public long getRuntimeId() {
        return runtimeId;
    }

    public Kind getKind() {
        return kind;
    }

    public LocalDate getFrom() {
        return from;
    }

    /** @return the first day the rule no longer applies, or null */
    public LocalDate getUntil() {
        return until;
    }

    /** @return {@code fa}, {@code fa-council}, {@code football-league} or {@code sfa} */
    public String getBody() {
        return body;
    }

    /** @return {@code professional}, {@code amateur} or {@code all} */
    public String getAppliesTo() {
        return appliesTo;
    }

    /** @return the approval wait in days (registration-wait), or null */
    public Integer getDays() {
        return days;
    }

    public Integer getMiles() {
        return miles;
    }

    public Integer getYears() {
        return years;
    }

    /** @return an amount in pence (the max wage a week, or a sourced example fee), or null */
    public Integer getPence() {
        return pence;
    }

    public String getConfidence() {
        return confidence;
    }

    public List<String> getSources() {
        return sources;
    }
}
