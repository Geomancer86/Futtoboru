package com.rndmodgames.futtoboru.finances;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * G-12: a club's budget for one season (1 May - 30 April; the first season of a game from its start date) and what
 * it has actually cost or earned so far, week by week. Every club has one, the player's and the AI's alike (D31).
 *
 * <p>The expected figures are set once by {@code BudgetManager} at the first weekly count of the season; the actual
 * ones are added to every week. Money in pounds, two decimals.
 */
public class Budget implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The contract season the budget is for (1888 = 1888-89); null: not planned yet */
    private Integer season;

    // expected for the whole period (design estimates, see BudgetManager)
    private BigDecimal expectedGates = BigDecimal.ZERO;
    private BigDecimal expectedWages = BigDecimal.ZERO;
    private BigDecimal expectedGround = BigDecimal.ZERO;
    private BigDecimal expectedOther = BigDecimal.ZERO;
    /** How much the bank lets the club overdraw before its patrons must step in */
    private BigDecimal facilityLimit = BigDecimal.ZERO;

    // the period in weeks
    private int weeksPlanned;
    private int playingWeeksPlanned;
    private int weeksDone;
    private int playingWeeksDone;

    // actual so far
    private BigDecimal actualGates = BigDecimal.ZERO;
    private BigDecimal actualWages = BigDecimal.ZERO;
    private BigDecimal actualGround = BigDecimal.ZERO;
    private BigDecimal actualOther = BigDecimal.ZERO;
    private BigDecimal actualInterest = BigDecimal.ZERO;

    /** Highest debt level the club has been warned about since it was last out of debt (0 none, 1-3 see BudgetManager) */
    private int warnedLevel;
    /** The debt level at the last weekly count (for the change hook) */
    private int lastLevel;
    /** The committee is worried about the debt (level 3); the hook G-05 reads */
    private boolean committeeConcern;
    private boolean spendWarned;
    private boolean gatesWarned;

    public boolean isPlanned() {
        return season != null;
    }

    public BigDecimal expectedSpend() {
        return expectedWages.add(expectedGround).add(expectedOther);
    }

    public BigDecimal actualSpend() {
        return actualWages.add(actualGround).add(actualOther).add(actualInterest);
    }

    /** The share of the period's weekly costs that has run (all weeks count) */
    public BigDecimal spendToDate() {
        return share(expectedSpend(), weeksDone, weeksPlanned);
    }

    /** Gates come in the playing season only: the share of the expected gates for the playing weeks done */
    public BigDecimal gatesToDate() {
        return playingWeeksPlanned > 0 ? share(expectedGates, playingWeeksDone, playingWeeksPlanned)
                : share(expectedGates, weeksDone, weeksPlanned);
    }

    public BigDecimal wagesToDate() {
        return share(expectedWages, weeksDone, weeksPlanned);
    }

    public BigDecimal groundToDate() {
        return share(expectedGround, weeksDone, weeksPlanned);
    }

    public BigDecimal otherToDate() {
        return share(expectedOther, weeksDone, weeksPlanned);
    }

    private static BigDecimal share(BigDecimal whole, int done, int planned) {
        if (planned <= 0) {
            return BigDecimal.ZERO;
        }
        return whole.multiply(BigDecimal.valueOf(Math.min(done, planned)))
                .divide(BigDecimal.valueOf(planned), 2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------------------------------------- accessors

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public BigDecimal getExpectedGates() {
        return expectedGates;
    }

    public void setExpectedGates(BigDecimal expectedGates) {
        this.expectedGates = expectedGates;
    }

    public BigDecimal getExpectedWages() {
        return expectedWages;
    }

    public void setExpectedWages(BigDecimal expectedWages) {
        this.expectedWages = expectedWages;
    }

    public BigDecimal getExpectedGround() {
        return expectedGround;
    }

    public void setExpectedGround(BigDecimal expectedGround) {
        this.expectedGround = expectedGround;
    }

    public BigDecimal getExpectedOther() {
        return expectedOther;
    }

    public void setExpectedOther(BigDecimal expectedOther) {
        this.expectedOther = expectedOther;
    }

    public BigDecimal getFacilityLimit() {
        return facilityLimit;
    }

    public void setFacilityLimit(BigDecimal facilityLimit) {
        this.facilityLimit = facilityLimit;
    }

    public int getWeeksPlanned() {
        return weeksPlanned;
    }

    public void setWeeksPlanned(int weeksPlanned) {
        this.weeksPlanned = weeksPlanned;
    }

    public int getPlayingWeeksPlanned() {
        return playingWeeksPlanned;
    }

    public void setPlayingWeeksPlanned(int playingWeeksPlanned) {
        this.playingWeeksPlanned = playingWeeksPlanned;
    }

    public int getWeeksDone() {
        return weeksDone;
    }

    public void setWeeksDone(int weeksDone) {
        this.weeksDone = weeksDone;
    }

    public int getPlayingWeeksDone() {
        return playingWeeksDone;
    }

    public void setPlayingWeeksDone(int playingWeeksDone) {
        this.playingWeeksDone = playingWeeksDone;
    }

    public BigDecimal getActualGates() {
        return actualGates;
    }

    public void setActualGates(BigDecimal actualGates) {
        this.actualGates = actualGates;
    }

    public BigDecimal getActualWages() {
        return actualWages;
    }

    public void setActualWages(BigDecimal actualWages) {
        this.actualWages = actualWages;
    }

    public BigDecimal getActualGround() {
        return actualGround;
    }

    public void setActualGround(BigDecimal actualGround) {
        this.actualGround = actualGround;
    }

    public BigDecimal getActualOther() {
        return actualOther;
    }

    public void setActualOther(BigDecimal actualOther) {
        this.actualOther = actualOther;
    }

    public BigDecimal getActualInterest() {
        return actualInterest;
    }

    public void setActualInterest(BigDecimal actualInterest) {
        this.actualInterest = actualInterest;
    }

    public int getLastLevel() {
        return lastLevel;
    }

    public void setLastLevel(int lastLevel) {
        this.lastLevel = lastLevel;
    }

    public int getWarnedLevel() {
        return warnedLevel;
    }

    public void setWarnedLevel(int warnedLevel) {
        this.warnedLevel = warnedLevel;
    }

    public boolean isCommitteeConcern() {
        return committeeConcern;
    }

    public void setCommitteeConcern(boolean committeeConcern) {
        this.committeeConcern = committeeConcern;
    }

    public boolean isSpendWarned() {
        return spendWarned;
    }

    public void setSpendWarned(boolean spendWarned) {
        this.spendWarned = spendWarned;
    }

    public boolean isGatesWarned() {
        return gatesWarned;
    }

    public void setGatesWarned(boolean gatesWarned) {
        this.gatesWarned = gatesWarned;
    }
}
