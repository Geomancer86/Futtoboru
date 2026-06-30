package com.rndmodgames.futtoboru.engine.health;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Save-state health scan for long-run simulation tests (and future runtime diagnostics).
 * Detects schedule/bracket defects that do not necessarily block champion recording.
 */
public final class SimulationHealthMonitor {

    public static final int TICKET_SALES_WINDOW_DAYS = 7;

    private SimulationHealthMonitor() {
    }

    public static HealthReport scan(SaveGame save) {
        return scan(save, true);
    }

    public static HealthReport scan(SaveGame save, boolean includeCupChecks) {
        if (save == null) {
            return HealthReport.clean();
        }
        LocalDateTime gameDate = save.getGameDate();
        List<String> issues = new ArrayList<>();
        Map<String, Integer> cupRoundCounts = new HashMap<>();

        int tbdInTicketWindow = 0;
        int duplicateMatchIds = 0;
        for (Club club : allClubs(save)) {
            tbdInTicketWindow += countTbdInTicketWindow(club, gameDate);
            duplicateMatchIds += countDuplicateIdsWithinClub(club);
        }

        if (includeCupChecks && save.getAllCups() != null && !save.getAllCups().isEmpty()) {
            scanCupSemiFinals(save, issues, cupRoundCounts);
        }

        return new HealthReport(tbdInTicketWindow, duplicateMatchIds, cupRoundCounts, List.copyOf(issues));
    }

    private static void scanCupSemiFinals(
            SaveGame save, List<String> issues, Map<String, Integer> cupRoundCounts) {
        Map<Long, Map<Integer, Integer>> byEditionRound = new HashMap<>();
        Set<Long> seen = new HashSet<>();
        for (Club club : allClubs(save)) {
            collectCupRounds(club.getScheduledMatches(), seen, byEditionRound);
            collectCupRounds(club.getPlayedMatches(), seen, byEditionRound);
        }
        Set<Long> editionsToValidate = cupEditionsWithActivity(save, byEditionRound);
        for (Map.Entry<Long, Map<Integer, Integer>> editionEntry : byEditionRound.entrySet()) {
            if (!editionsToValidate.contains(editionEntry.getKey())) {
                continue;
            }
            Map<Integer, Integer> byRound = editionEntry.getValue();
            int maxRound = byRound.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (maxRound >= 2) {
                int semiRound = maxRound - 1;
                int semiCount = byRound.getOrDefault(semiRound, 0);
                String label = "cup edition " + editionEntry.getKey() + " R" + semiRound;
                cupRoundCounts.put(label, semiCount);
                if (semiCount != 2 && semiCount > 0) {
                    issues.add("Cup semi-final round has " + semiCount + " matches (expected 2): " + label);
                }
            }
        }
    }

    private static Set<Long> cupEditionsWithActivity(
            SaveGame save, Map<Long, Map<Integer, Integer>> byEditionRound) {
        Set<Long> active = new HashSet<>();
        if (save.getAllCups() != null) {
            for (Competition cup : save.getAllCups()) {
                if (cup == null || cup.getEditions() == null) {
                    continue;
                }
                for (CompetitionEdition edition : cup.getEditions()) {
                    if (edition != null && edition.getChampionsId() != null && edition.getId() != null) {
                        active.add(edition.getId());
                    }
                }
            }
        }
        for (Club club : allClubs(save)) {
            markPlayedCupEditions(club.getScheduledMatches(), active);
            markPlayedCupEditions(club.getPlayedMatches(), active);
        }
        active.retainAll(byEditionRound.keySet());
        return active;
    }

    private static void markPlayedCupEditions(List<Match> matches, Set<Long> active) {
        if (matches == null) {
            return;
        }
        for (Match match : matches) {
            if (match == null || match.getCompetitionEditionId() == null) {
                continue;
            }
            if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH
                    && Boolean.TRUE.equals(match.getIsPlayed())) {
                active.add(match.getCompetitionEditionId());
            }
        }
    }

    private static void collectCupRounds(
            List<Match> matches,
            Set<Long> seen,
            Map<Long, Map<Integer, Integer>> byEditionRound) {
        if (matches == null) {
            return;
        }
        for (Match match : matches) {
            if (match == null || match.getId() == null || !seen.add(match.getId())) {
                continue;
            }
            if (match.getMatchType() == null || match.getMatchType() != Match.CUP_MATCH) {
                continue;
            }
            if (isCupReplayMatch(match)) {
                continue;
            }
            if (match.getCompetitionEditionId() == null || match.getRound() == null) {
                continue;
            }
            byEditionRound
                    .computeIfAbsent(match.getCompetitionEditionId(), k -> new HashMap<>())
                    .merge(match.getRound(), 1, Integer::sum);
        }
    }

    private static int countTbdInTicketWindow(Club club, LocalDateTime gameDate) {
        if (club == null || gameDate == null) {
            return 0;
        }
        return countTbdInList(club.getScheduledMatches(), gameDate);
    }

    private static int countTbdInList(List<Match> matches, LocalDateTime gameDate) {
        if (matches == null) {
            return 0;
        }
        int count = 0;
        for (Match match : matches) {
            if (match == null || Boolean.TRUE.equals(match.getIsPlayed())) {
                continue;
            }
            if (match.getHomeClubId() != null && match.getAwayClubId() != null) {
                continue;
            }
            // Cup bracket placeholders stay TBD until parent ties complete
            if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH) {
                continue;
            }
            if (match.getMatchDateTime() == null) {
                continue;
            }
            long daysUntil = ChronoUnit.DAYS.between(gameDate, match.getMatchDateTime());
            if (daysUntil >= 0 && daysUntil <= TICKET_SALES_WINDOW_DAYS) {
                count++;
            }
        }
        return count;
    }

    private static boolean isCupReplayMatch(Match match) {
        String path = match.getBracketPath();
        return path != null && path.endsWith("R");
    }

    private static int countDuplicateIdsWithinClub(Club club) {
        if (club == null) {
            return 0;
        }
        return dupesWithinList(club.getScheduledMatches()) + dupesWithinList(club.getPlayedMatches());
    }

    private static int dupesWithinList(List<Match> matches) {
        if (matches == null) {
            return 0;
        }
        Set<Long> seen = new HashSet<>();
        int dupes = 0;
        for (Match match : matches) {
            if (match == null || match.getId() == null) {
                continue;
            }
            if (!seen.add(match.getId())) {
                dupes++;
            }
        }
        return dupes;
    }

    private static List<Club> allClubs(SaveGame save) {
        if (save.getAllClubs() == null) {
            return Collections.emptyList();
        }
        return save.getAllClubs();
    }

    public static final class HealthReport {
        private final int tbdMatchesInTicketWindow;
        private final int duplicateMatchIds;
        private final Map<String, Integer> cupSemiFinalRoundCounts;
        private final List<String> issues;

        HealthReport(
                int tbdMatchesInTicketWindow,
                int duplicateMatchIds,
                Map<String, Integer> cupSemiFinalRoundCounts,
                List<String> issues) {
            this.tbdMatchesInTicketWindow = tbdMatchesInTicketWindow;
            this.duplicateMatchIds = duplicateMatchIds;
            this.cupSemiFinalRoundCounts = Collections.unmodifiableMap(cupSemiFinalRoundCounts);
            this.issues = issues;
        }

        public static HealthReport clean() {
            return new HealthReport(0, 0, Map.of(), List.of());
        }

        public boolean isHealthy() {
            return tbdMatchesInTicketWindow == 0 && issues.isEmpty();
        }

        public int getTbdMatchesInTicketWindow() {
            return tbdMatchesInTicketWindow;
        }

        public int getDuplicateMatchIds() {
            return duplicateMatchIds;
        }

        public Map<String, Integer> getCupSemiFinalRoundCounts() {
            return cupSemiFinalRoundCounts;
        }

        public List<String> getIssues() {
            return issues;
        }

        @Override
        public String toString() {
            return "HealthReport{tbdInTicketWindow=" + tbdMatchesInTicketWindow
                    + ", duplicateMatchIds=" + duplicateMatchIds
                    + ", issues=" + issues + "}";
        }
    }
}
