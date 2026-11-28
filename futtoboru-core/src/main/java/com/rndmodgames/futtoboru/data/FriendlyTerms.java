package com.rndmodgames.futtoboru.data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * G-13: how often a club of a standing (a Football League club, any other club, a club of amateurs only) writes for
 * friendlies, and what it is paid when it visits, from {@code seasons/<id>/friendlies.json}. The guarantee is read in
 * shillings and kept in pounds (20s = £1).
 *
 * <ul>
 * <li>{@link #chanceOn}: the chance that a club with a free day writes for a friendly on it, per weekday. The club
 * writes {@link #getLeadDays} days ahead and, if the day is still free, again {@link #getRetryDays} days ahead.</li>
 * <li>{@link #inSeason}: friendlies are arranged between {@code firstDay} and {@code lastDay} (the football season,
 * across the new year).</li>
 * <li>{@link #visitorsTake}: the visitors take the larger of their guarantee and {@code gateShare} of the gate.</li>
 * </ul>
 * Static data, never saved; a game reads the terms of the latest data season starting on or before the season it
 * plays ({@code DatabaseLoader.getFriendlyTerms}).
 */
public class FriendlyTerms {

    private String standing;
    /** weekday -> chance per free day of that weekday */
    private final Map<DayOfWeek, Double> chances = new LinkedHashMap<>();
    private int leadDays = 14;
    private int retryDays = 7;
    private MonthDay firstDay = MonthDay.of(9, 1);
    private MonthDay lastDay = MonthDay.of(4, 30);
    private BigDecimal guarantee = BigDecimal.ZERO;
    private double gateShare;
    private String confidence;

    /** The built-in terms, the same figures as the 1888-89 data, for a game without game data (unit tests). */
    public static List<FriendlyTerms> defaults() {
        List<FriendlyTerms> out = new ArrayList<>();
        out.add(terms(ContractBand.LEAGUE, 0.5, 0.1, 60, 0.5));
        out.add(terms(ContractBand.OTHER, 0.6, 0.05, 20, 0.5));
        out.add(terms(ContractBand.AMATEUR, 0.4, 0.0, 10, 0.5));
        return Collections.unmodifiableList(out);
    }

    private static FriendlyTerms terms(String standing, double saturday, double monday, int guaranteeShillings,
            double gateShare) {
        FriendlyTerms t = new FriendlyTerms();
        t.standing = standing;
        t.chances.put(DayOfWeek.SATURDAY, saturday);
        if (monday > 0) {
            t.chances.put(DayOfWeek.MONDAY, monday);
        }
        t.guarantee = ContractBand.ofShillings(guaranteeShillings);
        t.gateShare = gateShare;
        t.confidence = "placeholder";
        return t;
    }

    /** The terms of a standing; falls back to the built-in terms when {@code terms} lacks it. */
    public static FriendlyTerms forStanding(List<FriendlyTerms> terms, String standing) {
        for (List<FriendlyTerms> list : List.of(terms == null ? List.<FriendlyTerms>of() : terms, defaults())) {
            for (FriendlyTerms t : list) {
                if (t.standing != null && t.standing.equals(standing)) {
                    return t;
                }
            }
        }
        return defaults().get(1);
    }

    /** The chance that a club writes for a friendly on a free {@code day} (0 on other weekdays). */
    public double chanceOn(LocalDate day) {
        Double c = day != null ? chances.get(day.getDayOfWeek()) : null;
        return c != null ? c : 0;
    }

    /** True when {@code day} lies between the first and the last day of the friendly season (across the new year). */
    public boolean inSeason(LocalDate day) {
        if (day == null) {
            return false;
        }
        MonthDay d = MonthDay.from(day);
        if (!firstDay.isAfter(lastDay)) {
            return !d.isBefore(firstDay) && !d.isAfter(lastDay);
        }
        return !d.isBefore(firstDay) || !d.isAfter(lastDay);
    }

    /** What visitors of this standing take from a friendly with {@code gate} pounds: guarantee or share, the larger. */
    public BigDecimal visitorsTake(BigDecimal gate) {
        BigDecimal share = (gate != null ? gate : BigDecimal.ZERO).multiply(BigDecimal.valueOf(gateShare))
                .setScale(2, java.math.RoundingMode.DOWN);
        return share.compareTo(guarantee) > 0 ? share : guarantee;
    }

    public String getStanding() {
        return standing;
    }

    public void setStanding(String standing) {
        this.standing = standing;
    }

    public Map<DayOfWeek, Double> getChances() {
        return chances;
    }

    public int getLeadDays() {
        return leadDays;
    }

    public void setLeadDays(int leadDays) {
        this.leadDays = leadDays;
    }

    public int getRetryDays() {
        return retryDays;
    }

    public void setRetryDays(int retryDays) {
        this.retryDays = retryDays;
    }

    public MonthDay getFirstDay() {
        return firstDay;
    }

    public void setFirstDay(MonthDay firstDay) {
        this.firstDay = firstDay;
    }

    public MonthDay getLastDay() {
        return lastDay;
    }

    public void setLastDay(MonthDay lastDay) {
        this.lastDay = lastDay;
    }

    /** In pounds. */
    public BigDecimal getGuarantee() {
        return guarantee;
    }

    public void setGuarantee(BigDecimal guarantee) {
        this.guarantee = guarantee != null ? guarantee : BigDecimal.ZERO;
    }

    public double getGateShare() {
        return gateShare;
    }

    public void setGateShare(double gateShare) {
        this.gateShare = gateShare;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
