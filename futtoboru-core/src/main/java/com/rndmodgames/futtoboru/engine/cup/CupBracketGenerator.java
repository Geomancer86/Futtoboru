package com.rndmodgames.futtoboru.engine.cup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Generic single-elimination knockout bracket for any participant count.
 *
 * <p>Pads to the next power of two with first-round byes, then builds rounds by pairing
 * consecutive parent matches (round 2 with byes pairs each R1 winner with a bye team).
 * For {@code n} teams the bracket always has {@code n - 1} matches.
 */
public class CupBracketGenerator {

    private static final AtomicLong MATCH_ID_SEQ = new AtomicLong(0);

    /**
     * @param teamIds participant club ids (≥ 2)
     * @return all cup matches for every round; round 1 has teams set, later rounds are TBD
     */
    public List<Match> generateCompleteBracket(
            List<Long> teamIds,
            Long competitionId,
            Long editionId,
            LocalDateTime firstRoundDate,
            int weeksBetweenRounds) {

        if (teamIds == null || teamIds.size() < 2) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_CUP, "CupBracketGenerator",
                    "Need at least 2 teams, got " + (teamIds == null ? 0 : teamIds.size()));
            return List.of();
        }

        int teamCount = teamIds.size();
        int bracketSize = nextPowerOfTwo(teamCount);
        int byeCount = bracketSize - teamCount;
        int roundCount = Integer.numberOfTrailingZeros(bracketSize);

        List<Long> shuffled = new ArrayList<>(teamIds);
        Collections.shuffle(shuffled, DatabaseLoader.RNG);
        List<Long> byeTeams = new ArrayList<>(shuffled.subList(0, byeCount));
        List<Long> roundOneTeams = new ArrayList<>(shuffled.subList(byeCount, teamCount));

        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_CUP,
                "CupBracketGenerator: " + teamCount + " teams, bracketSize=" + bracketSize
                        + ", byes=" + byeCount + ", rounds=" + roundCount);

        List<Match> allMatches = new ArrayList<>();
        List<Match> previousRound = generateFirstRound(
                roundOneTeams, competitionId, editionId, firstRoundDate, allMatches);

        LocalDateTime roundDate = firstRoundDate.plusWeeks(weeksBetweenRounds);
        for (int round = 2; round <= roundCount; round++) {
            previousRound = generateSubsequentRound(
                    previousRound,
                    byeTeams,
                    round,
                    round == 2,
                    competitionId,
                    editionId,
                    roundDate,
                    allMatches);
            roundDate = roundDate.plusWeeks(weeksBetweenRounds);
        }

        validateBracket(teamCount, bracketSize, allMatches);
        return allMatches;
    }

    static int nextPowerOfTwo(int n) {
        if (n <= 2) {
            return 2;
        }
        int size = 1;
        while (size < n) {
            size <<= 1;
        }
        return size;
    }

    private List<Match> generateFirstRound(
            List<Long> teams,
            Long competitionId,
            Long editionId,
            LocalDateTime matchDate,
            List<Match> allMatches) {

        List<Match> roundMatches = new ArrayList<>();
        int matchNumber = 1;
        for (int i = 0; i + 1 < teams.size(); i += 2) {
            Match match = createMatch(competitionId, editionId, matchDate, 1, matchNumber++);
            match.setHomeClubId(teams.get(i));
            match.setAwayClubId(teams.get(i + 1));
            roundMatches.add(match);
            allMatches.add(match);
        }
        return roundMatches;
    }

    private List<Match> generateSubsequentRound(
            List<Match> previousRound,
            List<Long> byeTeams,
            int roundNumber,
            boolean firstRoundAfterByes,
            Long competitionId,
            Long editionId,
            LocalDateTime matchDate,
            List<Match> allMatches) {

        List<Match> roundMatches = new ArrayList<>();
        int matchNumber = 1;

        if (firstRoundAfterByes && !byeTeams.isEmpty()) {
            for (int i = 0; i < previousRound.size(); i++) {
                Match match = createMatch(competitionId, editionId, matchDate, roundNumber, matchNumber++);
                match.setParentMatch1Id(previousRound.get(i).getId());
                match.setHomeClubId(null);
                match.setAwayClubId(byeTeams.get(i));
                roundMatches.add(match);
                allMatches.add(match);
            }
            return roundMatches;
        }

        for (int i = 0; i + 1 < previousRound.size(); i += 2) {
            Match match = createMatch(competitionId, editionId, matchDate, roundNumber, matchNumber++);
            match.setParentMatch1Id(previousRound.get(i).getId());
            match.setParentMatch2Id(previousRound.get(i + 1).getId());
            match.setHomeClubId(null);
            match.setAwayClubId(null);
            roundMatches.add(match);
            allMatches.add(match);
        }
        return roundMatches;
    }

    private Match createMatch(
            Long competitionId,
            Long editionId,
            LocalDateTime matchDate,
            int round,
            int matchNumber) {

        Match match = new Match();
        match.setId(nextMatchId());
        match.setCompetitionId(competitionId);
        match.setCompetitionEditionId(editionId);
        match.setMatchType(Match.CUP_MATCH);
        match.setRound(round);
        match.setBracketPosition(matchNumber);
        match.setBracketPath("R" + round + "M" + matchNumber);
        match.setMatchDateTime(matchDate);
        match.setIsProposed(false);
        match.setIsAccepted(true);
        match.setIsPlayed(false);
        return match;
    }

    private static long nextMatchId() {
        return System.currentTimeMillis() * 1000L + MATCH_ID_SEQ.incrementAndGet();
    }

    private void validateBracket(int teamCount, int bracketSize, List<Match> allMatches) {
        Map<Integer, Integer> byRound = new HashMap<>();
        for (Match match : allMatches) {
            if (match.getRound() != null) {
                byRound.merge(match.getRound(), 1, Integer::sum);
            }
        }

        int roundCount = Integer.numberOfTrailingZeros(bracketSize);
        for (int round = 1; round <= roundCount; round++) {
            int expected = bracketSize >> round;
            int actual = byRound.getOrDefault(round, 0);
            if (actual != expected) {
                DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_CUP, "CupBracketGenerator",
                        "Round " + round + " has " + actual + " matches, expected " + expected
                                + " (teams=" + teamCount + ", bracketSize=" + bracketSize + ")");
            }
        }

        if (allMatches.size() != teamCount - 1) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_CUP, "CupBracketGenerator",
                    "Expected " + (teamCount - 1) + " total matches, got " + allMatches.size());
        }
    }
}
