package com.rndmodgames.futtoboru.harness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Structured output from a multi-season headless run (champions, finances, diagnostics).
 */
public final class MultiSeasonSimulationReport {

    private final long playerClubId;
    private final String playerClubName;
    private final LocalDateTime startDate;
    private final List<SeasonEditionSummary> seasons = new ArrayList<>();
    private LocalDateTime endDate;
    private long elapsedMs;
    private int daysSimulated;
    private int targetEditions;
    private int finalChampionEditions;
    private int finalInboxMessages;
    private int finalSimulatedSeasonIndex;
    private boolean leagueOnly;

    MultiSeasonSimulationReport(long playerClubId, String playerClubName, LocalDateTime startDate) {
        this.playerClubId = playerClubId;
        this.playerClubName = playerClubName;
        this.startDate = startDate;
    }

    void addSeason(SeasonEditionSummary summary) {
        seasons.add(summary);
    }

    void finish(
            LocalDateTime endDate,
            long elapsedMs,
            int daysSimulated,
            int targetEditions,
            int finalChampionEditions,
            int finalInboxMessages,
            int finalSimulatedSeasonIndex,
            boolean leagueOnly) {
        this.endDate = endDate;
        this.elapsedMs = elapsedMs;
        this.daysSimulated = daysSimulated;
        this.targetEditions = targetEditions;
        this.finalChampionEditions = finalChampionEditions;
        this.finalInboxMessages = finalInboxMessages;
        this.finalSimulatedSeasonIndex = finalSimulatedSeasonIndex;
        this.leagueOnly = leagueOnly;
    }

    public List<SeasonEditionSummary> getSeasons() {
        return List.copyOf(seasons);
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public int getDaysSimulated() {
        return daysSimulated;
    }

    public double getMsPerDay() {
        return daysSimulated > 0 ? (double) elapsedMs / daysSimulated : 0;
    }

    public double getSecondsPerSeason() {
        return seasons.isEmpty() ? 0 : elapsedMs / 1000.0 / seasons.size();
    }

    public long getProjectedSecondsForSeasons(int count) {
        return Math.round(getSecondsPerSeason() * count);
    }

    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        md.append("# Multi-season simulation report\n\n");
        md.append("| Field | Value |\n|-------|-------|\n");
        md.append("| Player club | ").append(playerClubName).append(" (id ").append(playerClubId).append(") |\n");
        md.append("| Mode | ").append(leagueOnly ? "League only" : "League + FA Cup").append(" |\n");
        md.append("| Start date | ").append(startDate).append(" |\n");
        md.append("| End date | ").append(endDate).append(" |\n");
        md.append("| Target editions | ").append(targetEditions).append(" |\n");
        md.append("| Champion editions | ").append(finalChampionEditions).append(" |\n");
        md.append("| Simulated season index | ").append(finalSimulatedSeasonIndex).append(" |\n");
        md.append("| Days simulated | ").append(daysSimulated).append(" |\n");
        md.append("| Wall time | ").append(String.format("%.1f s", elapsedMs / 1000.0)).append(" |\n");
        md.append("| ms/day | ").append(String.format("%.2f", getMsPerDay())).append(" |\n");
        md.append("| s/season (avg) | ").append(String.format("%.2f", getSecondsPerSeason())).append(" |\n");
        md.append("| Projected 60 seasons | ").append(getProjectedSecondsForSeasons(60)).append(" s (~")
                .append(String.format("%.1f min", getProjectedSecondsForSeasons(60) / 60.0)).append(") |\n");
        md.append("| Inbox messages (final) | ").append(finalInboxMessages).append(" |\n\n");

        md.append("## League champions by edition\n\n");
        md.append("| # | Edition | Champion | Game date | PNE balance | Season income | Season spend | Sim index | Inbox |\n");
        md.append("|---|---------|----------|-----------|-------------|---------------|--------------|-----------|-------|\n");
        for (SeasonEditionSummary s : seasons) {
            md.append("| ").append(s.getEditionNumber())
                    .append(" | ").append(nullSafe(s.getEditionName()))
                    .append(" | ").append(nullSafe(s.getChampionClubName())).append(" (").append(s.getChampionClubId()).append(")")
                    .append(" | ").append(s.getGameDate())
                    .append(" | ").append(s.getPlayerClubBalance())
                    .append(" | ").append(s.getPlayerSeasonIncome())
                    .append(" | ").append(s.getPlayerSeasonExpenditure())
                    .append(" | ").append(s.getSimulatedSeasonIndex())
                    .append(" | ").append(s.getInboxMessageCount())
                    .append(" |\n");
        }
        return md.toString();
    }

    public void writeTo(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, toMarkdown(), StandardCharsets.UTF_8);
    }

    private static String nullSafe(String value) {
        return value != null ? value : "?";
    }
}
