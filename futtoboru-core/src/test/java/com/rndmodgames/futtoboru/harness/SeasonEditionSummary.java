package com.rndmodgames.futtoboru.harness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One finalized league edition captured during headless simulation.
 */
public final class SeasonEditionSummary {

    private final int editionNumber;
    private final String editionName;
    private final Long championClubId;
    private final String championClubName;
    private final LocalDateTime gameDate;
    private final BigDecimal playerClubBalance;
    private final BigDecimal playerSeasonIncome;
    private final BigDecimal playerSeasonExpenditure;
    private final int simulatedSeasonIndex;
    private final int inboxMessageCount;
    private final double wallSeconds;

    SeasonEditionSummary(
            int editionNumber,
            String editionName,
            Long championClubId,
            String championClubName,
            LocalDateTime gameDate,
            BigDecimal playerClubBalance,
            BigDecimal playerSeasonIncome,
            BigDecimal playerSeasonExpenditure,
            int simulatedSeasonIndex,
            int inboxMessageCount,
            double wallSeconds) {
        this.editionNumber = editionNumber;
        this.editionName = editionName;
        this.championClubId = championClubId;
        this.championClubName = championClubName;
        this.gameDate = gameDate;
        this.playerClubBalance = playerClubBalance;
        this.playerSeasonIncome = playerSeasonIncome;
        this.playerSeasonExpenditure = playerSeasonExpenditure;
        this.simulatedSeasonIndex = simulatedSeasonIndex;
        this.inboxMessageCount = inboxMessageCount;
        this.wallSeconds = wallSeconds;
    }

    public int getEditionNumber() {
        return editionNumber;
    }

    public String getEditionName() {
        return editionName;
    }

    public Long getChampionClubId() {
        return championClubId;
    }

    public String getChampionClubName() {
        return championClubName;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public BigDecimal getPlayerClubBalance() {
        return playerClubBalance;
    }

    public BigDecimal getPlayerSeasonIncome() {
        return playerSeasonIncome;
    }

    public BigDecimal getPlayerSeasonExpenditure() {
        return playerSeasonExpenditure;
    }

    public int getSimulatedSeasonIndex() {
        return simulatedSeasonIndex;
    }

    public int getInboxMessageCount() {
        return inboxMessageCount;
    }

    /**
     * Wall-clock seconds this single season took to simulate (time since the previous season's
     * champion was recorded). Used to catch late-run performance regressions that a run-wide
     * average can hide — see {@code LongRunSoakIT#MAX_SECONDS_FOR_SLOWEST_SEASON}.
     */
    public double getWallSeconds() {
        return wallSeconds;
    }
}
