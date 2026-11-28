package com.rndmodgames.futtoboru.engine.committee;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubCommittee;
import com.rndmodgames.futtoboru.data.CommitteeMember;

/**
 * G-05 (D28 Q1): what the rest of the game asks a club's committee. The secretary recruits; a weekly wage up to the
 * committee's band ({@link ClubCommittee#getWageBandPence()}, the R-02 band maximum for the club's standing) he may
 * agree alone, above it the committee votes ({@link #approveWage}). For G-04 contracts and recruitment.
 *
 * <p>The vote draws nothing: each member's seeded generosity, the confidence in the secretary, how far the wage is
 * above the band and the balance decide it, so asking twice gives the same answer.
 */
public final class Committee {

    /** Without a committee or band (before the first game day): the R-02 "middling" band maximum, 40s. */
    static final int DEFAULT_BAND_PENCE = 480;
    /** Pence in a pound (pre-decimal). */
    static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(240);

    private Committee() {
    }

    /** The committee's answer to a wage. */
    public record WageDecision(boolean approved, boolean voted, int yes, int no, BigDecimal bandWeekly) {
    }

    /**
     * May the secretary offer this weekly wage (pounds, like {@code PlayerContract.getWeeklyWage()})?
     *
     * @return approved without a vote when within the band; otherwise the members' vote (a majority approves)
     */
    public static WageDecision approveWage(Club club, BigDecimal weekly) {
        ClubCommittee committee = club != null ? club.getCommittee() : null;
        BigDecimal band = bandWeekly(club);
        if (weekly == null || weekly.compareTo(band) <= 0) {
            return new WageDecision(true, false, 0, 0, band);
        }
        if (club == null) {
            return new WageDecision(false, false, 0, 0, band);
        }
        double excess = weekly.subtract(band).divide(band, 4, RoundingMode.HALF_UP).doubleValue();
        int confidence = committee != null ? committee.getConfidence() : 50;
        BigDecimal balance = club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
        // a year of the extra wage the club can carry: easier; a club in debt: harder
        double money = balance.signum() < 0 ? -0.25
                : balance.compareTo(weekly.subtract(band).multiply(BigDecimal.valueOf(52))) >= 0 ? 0.1 : -0.1;
        double mood = (confidence - 50) / 100.0 - excess + money;
        int yes = 0;
        int no = 0;
        if (committee == null || committee.getMembers().isEmpty()) {
            return new WageDecision(false, true, 0, 1, band);
        }
        for (CommitteeMember member : committee.getMembers()) {
            if (member.getGenerosity() + mood >= 0.5) {
                yes++;
            } else {
                no++;
            }
        }
        return new WageDecision(yes > no, true, yes, no, band);
    }

    /** @return the weekly wage (pounds) the secretary may agree without a vote */
    public static BigDecimal bandWeekly(Club club) {
        ClubCommittee committee = club != null ? club.getCommittee() : null;
        int pence = committee != null && committee.getWageBandPence() != null ? committee.getWageBandPence()
                : DEFAULT_BAND_PENCE;
        return BigDecimal.valueOf(pence).divide(PENCE_PER_POUND, 2, RoundingMode.HALF_UP);
    }

    /** @return true when this club's committee dismissed that person (it does not take him back) */
    public static boolean hasDismissed(Club club, Long personId) {
        return club != null && personId != null && club.getCommittee() != null
                && club.getCommittee().getDismissedIds().contains(personId);
    }

    /** @return the bundle key of the confidence in words ({@code committee_confidence_*}) */
    public static String confidenceKey(int confidence) {
        if (confidence >= 80) {
            return "committee_confidence_delighted";
        }
        if (confidence >= 65) {
            return "committee_confidence_pleased";
        }
        if (confidence >= 50) {
            return "committee_confidence_satisfied";
        }
        if (confidence >= 35) {
            return "committee_confidence_uneasy";
        }
        if (confidence >= 20) {
            return "committee_confidence_displeased";
        }
        return "committee_confidence_furious";
    }
}
