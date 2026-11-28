package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * G-05: a club's committee, kept on its {@link Club} (saved with it, optional: older saves read it as null and get
 * one on the next game day). It sets the season's expectations, keeps a confidence in the secretary(-manager)
 * (0..100) that moves with results, gates and the balance, writes warnings and dismisses him after them.
 *
 * <p>Season years are the calendar's ({@code SeasonCalendar.startYearOf}: April turns it; 1888 = 1888-89).
 */
public class ClubCommittee implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<CommitteeMember> members = new ArrayList<>();

    /** The football year the expectations below are for; null until first set. */
    private Integer seasonYear;
    /** The football year whose expectations were judged at its end. */
    private Integer reviewedYear;

    /** League: finish at or above this place (null: no League this season). */
    private Integer targetPosition;
    /** Clubs in the League when the expectation was set. */
    private Integer leagueSize;
    /** True: keep the balance positive; false: the club starts in debt, reduce it (end at or above the start). */
    private Boolean keepBalancePositive;
    /** The balance when the expectations were set. */
    private BigDecimal startBalance;
    /** Cup ties to win (0: no cup expectation). */
    private Integer cupTiesExpected;
    /** "leading", "middling" or "small" (the R-02 wage-band standings) by the club's size. */
    private String standing;
    /** The weekly wage the secretary may agree without a vote, in pence (R-02 band maximum for the standing). */
    private Integer wageBandPence;

    /** 0..100. */
    private Integer confidence;
    /** Warnings written to the present secretary (0, 1, 2: the second is the final one). */
    private Integer warnings;
    private LocalDateTime lastWarning;
    private LocalDateTime lastPraise;
    /** The last weekly review (results, gates, balance since then are judged at the next). */
    private LocalDateTime lastReview;
    /** The secretary the confidence is in (a new one starts afresh). */
    private Long secretaryId;
    private LocalDateTime secretarySince;
    /** People this committee dismissed: it never appoints them again. */
    private List<Long> dismissedIds = new ArrayList<>();

    public ClubCommittee() {
    }

    public List<CommitteeMember> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
        }
        return members;
    }

    public void setMembers(List<CommitteeMember> members) {
        this.members = members;
    }

    public Integer getSeasonYear() {
        return seasonYear;
    }

    public void setSeasonYear(Integer seasonYear) {
        this.seasonYear = seasonYear;
    }

    public Integer getReviewedYear() {
        return reviewedYear;
    }

    public void setReviewedYear(Integer reviewedYear) {
        this.reviewedYear = reviewedYear;
    }

    public Integer getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Integer targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Integer getLeagueSize() {
        return leagueSize;
    }

    public void setLeagueSize(Integer leagueSize) {
        this.leagueSize = leagueSize;
    }

    public boolean isKeepBalancePositive() {
        return !Boolean.FALSE.equals(keepBalancePositive);
    }

    public void setKeepBalancePositive(Boolean keepBalancePositive) {
        this.keepBalancePositive = keepBalancePositive;
    }

    public BigDecimal getStartBalance() {
        return startBalance != null ? startBalance : BigDecimal.ZERO;
    }

    public void setStartBalance(BigDecimal startBalance) {
        this.startBalance = startBalance;
    }

    public int getCupTiesExpected() {
        return cupTiesExpected != null ? cupTiesExpected : 0;
    }

    public void setCupTiesExpected(Integer cupTiesExpected) {
        this.cupTiesExpected = cupTiesExpected;
    }

    public String getStanding() {
        return standing;
    }

    public void setStanding(String standing) {
        this.standing = standing;
    }

    public Integer getWageBandPence() {
        return wageBandPence;
    }

    public void setWageBandPence(Integer wageBandPence) {
        this.wageBandPence = wageBandPence;
    }

    public int getConfidence() {
        return confidence != null ? confidence : 50;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public int getWarnings() {
        return warnings != null ? warnings : 0;
    }

    public void setWarnings(Integer warnings) {
        this.warnings = warnings;
    }

    public LocalDateTime getLastWarning() {
        return lastWarning;
    }

    public void setLastWarning(LocalDateTime lastWarning) {
        this.lastWarning = lastWarning;
    }

    public LocalDateTime getLastPraise() {
        return lastPraise;
    }

    public void setLastPraise(LocalDateTime lastPraise) {
        this.lastPraise = lastPraise;
    }

    public LocalDateTime getLastReview() {
        return lastReview;
    }

    public void setLastReview(LocalDateTime lastReview) {
        this.lastReview = lastReview;
    }

    public Long getSecretaryId() {
        return secretaryId;
    }

    public void setSecretaryId(Long secretaryId) {
        this.secretaryId = secretaryId;
    }

    public LocalDateTime getSecretarySince() {
        return secretarySince;
    }

    public void setSecretarySince(LocalDateTime secretarySince) {
        this.secretarySince = secretarySince;
    }

    public List<Long> getDismissedIds() {
        if (dismissedIds == null) {
            dismissedIds = new ArrayList<>();
        }
        return dismissedIds;
    }

    public void setDismissedIds(List<Long> dismissedIds) {
        this.dismissedIds = dismissedIds;
    }

    /** @return the member with that role (the first), or null */
    public CommitteeMember member(String role) {
        for (CommitteeMember m : getMembers()) {
            if (m != null && role.equals(m.getRole())) {
                return m;
            }
        }
        return null;
    }
}
