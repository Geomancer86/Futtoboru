package com.rndmodgames.futtoboru.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * G-12a: how a club of a standing (a Football League club, any other club, a club of amateurs only, the same
 * standings as {@link ContractBand}) earns and spends outside its players' wages, from
 * {@code seasons/<id>/economy.json}: its balance at a new game, the admission it charges, how many come to its
 * friendlies, its members' subscriptions, the share of the base overheads it pays and how often it arranges a
 * friendly on a free Saturday.
 *
 * Static data, never saved: a game reads the economy of the latest data season starting on or before the season it
 * plays ({@code DatabaseLoader.getEconomies}).
 */
public class ClubEconomy {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(240);

    private String standing;
    private BigDecimal startingBalance = BigDecimal.ZERO;
    private double leagueAdmissionPence;
    private double cupAdmissionPence;
    private double friendlyAdmissionPence;
    private double friendlyCrowd;
    private int friendlyCrowdMax;
    private BigDecimal subscriptions = BigDecimal.ZERO;
    private double overheads = 1.0;
    private double friendlyChance;
    private String confidence;

    /**
     * The built-in economy of a game without game data (unit tests that build their own clubs): the figures the game
     * used before G-12a (1d friendlies, 2d League, 2.5d Cup, friendlies 15% of a League sale and no cap, the full overheads, no
     * subscriptions and no friendlies between AI clubs).
     */
    public static ClubEconomy defaults(String standing) {
        ClubEconomy e = new ClubEconomy();
        e.standing = standing;
        e.leagueAdmissionPence = 2;
        e.cupAdmissionPence = 2.5;
        e.friendlyAdmissionPence = 1;
        e.friendlyCrowd = 0.15;
        e.confidence = "placeholder";
        return e;
    }

    /** The economy of {@code standing} in {@code economies}, else the built-in one */
    public static ClubEconomy forStanding(List<ClubEconomy> economies, String standing) {
        if (economies != null) {
            for (ClubEconomy e : economies) {
                if (e != null && e.standing != null && e.standing.equals(standing)) {
                    return e;
                }
            }
        }
        return defaults(standing);
    }

    /** The admission for a match of {@code matchType} ({@link Match#LEAGUE_MATCH}, ...), in pounds */
    public BigDecimal admission(Integer matchType) {
        double pence = friendlyAdmissionPence;
        if (matchType != null && matchType == Match.LEAGUE_MATCH) {
            pence = leagueAdmissionPence;
        } else if (matchType != null && matchType == Match.CUP_MATCH) {
            pence = cupAdmissionPence;
        }
        return BigDecimal.valueOf(pence).divide(PENCE_PER_POUND, 11, RoundingMode.HALF_UP);
    }

    /** A week's share of the year's subscriptions, in pounds */
    public BigDecimal weeklySubscriptions() {
        return subscriptions.divide(BigDecimal.valueOf(52), 2, RoundingMode.HALF_UP);
    }

    public String getStanding() {
        return standing;
    }

    public void setStanding(String standing) {
        this.standing = standing;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance != null ? startingBalance : BigDecimal.ZERO;
    }

    public double getLeagueAdmissionPence() {
        return leagueAdmissionPence;
    }

    public void setLeagueAdmissionPence(double leagueAdmissionPence) {
        this.leagueAdmissionPence = leagueAdmissionPence;
    }

    public double getCupAdmissionPence() {
        return cupAdmissionPence;
    }

    public void setCupAdmissionPence(double cupAdmissionPence) {
        this.cupAdmissionPence = cupAdmissionPence;
    }

    public double getFriendlyAdmissionPence() {
        return friendlyAdmissionPence;
    }

    public void setFriendlyAdmissionPence(double friendlyAdmissionPence) {
        this.friendlyAdmissionPence = friendlyAdmissionPence;
    }

    /** A friendly's daily ticket sale as a share of a League or Cup match's */
    public double getFriendlyCrowd() {
        return friendlyCrowd;
    }

    public void setFriendlyCrowd(double friendlyCrowd) {
        this.friendlyCrowd = friendlyCrowd;
    }

    /** The most a friendly at the club's ground draws (0: only the ground's capacity) */
    public int getFriendlyCrowdMax() {
        return friendlyCrowdMax;
    }

    public void setFriendlyCrowdMax(int friendlyCrowdMax) {
        this.friendlyCrowdMax = friendlyCrowdMax;
    }

    /** Members' subscriptions and donations, pounds a year */
    public BigDecimal getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(BigDecimal subscriptions) {
        this.subscriptions = subscriptions != null ? subscriptions : BigDecimal.ZERO;
    }

    /** The share of the base overheads (staff, ground upkeep, kit, office, sundries) the club pays */
    public double getOverheads() {
        return overheads;
    }

    public void setOverheads(double overheads) {
        this.overheads = overheads;
    }

    /** The chance an AI club with a free Saturday in the playing season seeks a friendly for it */
    public double getFriendlyChance() {
        return friendlyChance;
    }

    public void setFriendlyChance(double friendlyChance) {
        this.friendlyChance = friendlyChance;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
