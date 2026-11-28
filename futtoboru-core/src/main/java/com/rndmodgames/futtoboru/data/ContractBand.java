package com.rndmodgames.futtoboru.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * G-04: the terms a club's men are engaged on for one season, by the club's standing (a Football League club, any
 * other club, a club of amateurs only), from {@code seasons/<id>/contracts.json}. Money is read in shillings a week
 * (as the sources give it) and kept here in pounds, the game's money unit (20s = £1).
 *
 * Static data, never saved: a game reads the bands of the latest data season starting on or before the season it
 * plays ({@code DatabaseLoader.getContractBands}).
 */
public class ContractBand {

    public static final String LEAGUE = "league";
    public static final String OTHER = "other";
    public static final String AMATEUR = "amateur";

    private static final BigDecimal SHILLINGS_PER_POUND = BigDecimal.valueOf(20);

    private String standing;
    private List<String> clubs = new ArrayList<>();
    private double professionalShare;
    private BigDecimal seasonWageMin = BigDecimal.ZERO;
    private BigDecimal seasonWageMax = BigDecimal.ZERO;
    private BigDecimal summerWageMin = BigDecimal.ZERO;
    private BigDecimal summerWageMax = BigDecimal.ZERO;
    private double summerPayShare;
    private BigDecimal amateurExpenses = BigDecimal.ZERO;
    private int minimumSquad = 11;
    private int squadSize = 16;
    private String confidence;

    /** Shillings to pounds (2 decimals: whole shillings are exact) */
    public static BigDecimal pounds(BigDecimal shillings) {
        return shillings == null ? BigDecimal.ZERO : shillings.divide(SHILLINGS_PER_POUND, 2, RoundingMode.HALF_UP);
    }

    /** Pounds to whole shillings (the step wages are offered in) */
    public static int shillings(BigDecimal pounds) {
        return pounds == null ? 0 : pounds.multiply(SHILLINGS_PER_POUND).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /** Whole shillings as pounds */
    public static BigDecimal ofShillings(int shillings) {
        return BigDecimal.valueOf(shillings).divide(SHILLINGS_PER_POUND, 2, RoundingMode.HALF_UP);
    }

    /**
     * The built-in bands, the same figures as the 1888-89 data, for a game without game data (unit tests that build
     * their own clubs).
     */
    public static List<ContractBand> defaults() {
        List<ContractBand> out = new ArrayList<>();
        out.add(band(LEAGUE, 0.85, 20, 50, 10, 20, 0.5, 2, 16, 20));
        out.add(band(OTHER, 0.5, 5, 25, 2, 10, 0.25, 2, 12, 16));
        out.add(band(AMATEUR, 0, 0, 0, 0, 0, 0, 2, 12, 16));
        return Collections.unmodifiableList(out);
    }

    private static ContractBand band(String standing, double pro, int wMin, int wMax, int sMin, int sMax, double summer,
            int expenses, int minimum, int size) {
        ContractBand b = new ContractBand();
        b.standing = standing;
        b.professionalShare = pro;
        b.seasonWageMin = ofShillings(wMin);
        b.seasonWageMax = ofShillings(wMax);
        b.summerWageMin = ofShillings(sMin);
        b.summerWageMax = ofShillings(sMax);
        b.summerPayShare = summer;
        b.amateurExpenses = ofShillings(expenses);
        b.minimumSquad = minimum;
        b.squadSize = size;
        b.confidence = "placeholder";
        return b;
    }

    /**
     * The band of a club: the {@code amateur} band when it lists the club, else {@code league} for a club of a league
     * this season, else {@code other}. Falls back to the built-in bands when {@code bands} lacks one.
     */
    public static ContractBand forClub(List<ContractBand> bands, String clubDataId, boolean leagueClub) {
        List<ContractBand> all = bands == null || bands.isEmpty() ? defaults() : bands;
        if (clubDataId != null) {
            for (ContractBand b : all) {
                if (AMATEUR.equals(b.standing) && b.clubs.contains(clubDataId)) {
                    return b;
                }
            }
        }
        String wanted = leagueClub ? LEAGUE : OTHER;
        for (ContractBand b : all) {
            if (wanted.equals(b.standing)) {
                return b;
            }
        }
        for (ContractBand b : defaults()) {
            if (wanted.equals(b.standing)) {
                return b;
            }
        }
        return defaults().get(0);
    }

    /** The share of the season wage a man with summer pay gets in the close season (mid band / mid band). */
    public double summerRatio() {
        BigDecimal season = seasonWageMin.add(seasonWageMax);
        if (season.signum() <= 0) {
            return 0;
        }
        return summerWageMin.add(summerWageMax).doubleValue() / season.doubleValue();
    }

    public String getStanding() {
        return standing;
    }

    public void setStanding(String standing) {
        this.standing = standing;
    }

    public List<String> getClubs() {
        return clubs;
    }

    public void setClubs(List<String> clubs) {
        this.clubs = clubs != null ? clubs : new ArrayList<>();
    }

    public double getProfessionalShare() {
        return professionalShare;
    }

    public void setProfessionalShare(double professionalShare) {
        this.professionalShare = professionalShare;
    }

    /** @return the lowest season wage a week, in pounds */
    public BigDecimal getSeasonWageMin() {
        return seasonWageMin;
    }

    public void setSeasonWageMin(BigDecimal seasonWageMin) {
        this.seasonWageMin = seasonWageMin;
    }

    public BigDecimal getSeasonWageMax() {
        return seasonWageMax;
    }

    public void setSeasonWageMax(BigDecimal seasonWageMax) {
        this.seasonWageMax = seasonWageMax;
    }

    public BigDecimal getSummerWageMin() {
        return summerWageMin;
    }

    public void setSummerWageMin(BigDecimal summerWageMin) {
        this.summerWageMin = summerWageMin;
    }

    public BigDecimal getSummerWageMax() {
        return summerWageMax;
    }

    public void setSummerWageMax(BigDecimal summerWageMax) {
        this.summerWageMax = summerWageMax;
    }

    public double getSummerPayShare() {
        return summerPayShare;
    }

    public void setSummerPayShare(double summerPayShare) {
        this.summerPayShare = summerPayShare;
    }

    /** @return an amateur's weekly expenses in the season (fares, loss of time), in pounds */
    public BigDecimal getAmateurExpenses() {
        return amateurExpenses;
    }

    public void setAmateurExpenses(BigDecimal amateurExpenses) {
        this.amateurExpenses = amateurExpenses;
    }

    /** @return the fewest men a club of this standing starts a season with (it signs free agents below it) */
    public int getMinimumSquad() {
        return minimumSquad;
    }

    public void setMinimumSquad(int minimumSquad) {
        this.minimumSquad = minimumSquad;
    }

    /** @return the most men a committee of this standing retains */
    public int getSquadSize() {
        return squadSize;
    }

    public void setSquadSize(int squadSize) {
        this.squadSize = squadSize;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
