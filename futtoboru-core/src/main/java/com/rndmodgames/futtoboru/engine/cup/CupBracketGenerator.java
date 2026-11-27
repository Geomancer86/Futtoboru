package com.rndmodgames.futtoboru.engine.cup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * Cup Bracket Generator v1.0
 * 
 * Generates complete tournament bracket (all rounds) upfront.
 * Creates match dependencies so winners can automatically advance.
 * 
 * Structure for 32 teams:
 * - Round 1: 16 matches (32 teams)
 * - Round 2: 8 matches (16 teams)
 * - Round 3: 4 matches (8 teams)
 * - Round 4: 2 matches (4 teams - Semi-Finals)
 * - Round 5: 1 match (2 teams - Final)
 * 
 * Total: 31 matches
 * 
 * @author Geomancer86
 */
public class CupBracketGenerator {
    
    /** Gives every generated match its runtime id (the game's allocator, P2-4) */
    private final com.rndmodgames.futtoboru.system.data.IdAllocator ids;
    
    /** Draws byes and pairings (a fork of the game's random source for this cup edition, P2-0) */
    private final RandomSource rng;
    
    /** Without a game (tests, tools): draws from the database loader's source. Game code passes a fork. */
    public CupBracketGenerator(com.rndmodgames.futtoboru.system.data.IdAllocator ids) {
        this(ids, com.rndmodgames.futtoboru.system.DatabaseLoader.getRandom().fork("cup-draw"));
    }

    public CupBracketGenerator(com.rndmodgames.futtoboru.system.data.IdAllocator ids, RandomSource rng) {
        this.ids = ids;
        this.rng = rng;
    }
    
    /**
     * Generate complete bracket for a cup competition
     * 
     * @param teamIds List of team IDs participating
     * @param competitionId Competition ID
     * @param editionId Edition ID
     * @param firstRoundDate Date for first round matches
     * @param weeksBetweenRounds Weeks between each round
     * @return Complete list of all matches for all rounds
     */
    public List<Match> generateCompleteBracket(
            List<Long> teamIds,
            Long competitionId,
            Long editionId,
            LocalDateTime firstRoundDate,
            int weeksBetweenRounds) {
        
        List<Match> allMatches = new ArrayList<>();
        List<Long> teams = new ArrayList<>(teamIds);
        
        Gdx.app.log("CupBracketGenerator", "Generating complete bracket for " + teams.size() + " teams");
        
        // P2-3: a bracket of any size. The bracket has the next power of two of slots; the clubs that do not fit
        // in round 1 get a bye straight into round 2 (12 clubs: 4 first-round ties + 4 byes, then 4, 2, 1 ties).
        // The old generator halved the number of ties each round, so with 12 clubs the winner of R2M3 had no
        // next tie and the semi-final and final rounds were forced to the wrong size.
        if (teams.size() < 2) {
            Gdx.app.error("CupBracketGenerator", "A cup needs at least 2 clubs, got " + teams.size());
            return allMatches;
        }
        int bracketSize = Integer.highestOneBit(teams.size());
        if (bracketSize < teams.size()) {
            bracketSize <<= 1;
        }
        int numRounds = Integer.numberOfTrailingZeros(bracketSize);

        // Draw the byes (none when the number of clubs is a power of two)
        List<Long> byeTeams = new ArrayList<>();
        for (int i = bracketSize - teams.size(); i > 0; i--) {
            byeTeams.add(teams.remove(rng.nextInt(teams.size())));
        }

        Gdx.app.log("CupBracketGenerator", "Tournament structure: " + teamIds.size() + " clubs, " + byeTeams.size()
            + " byes, " + numRounds + " rounds");

        // Round 1: the clubs without a bye, paired at random
        List<Match> round1 = generateFirstRound(teams, competitionId, editionId, firstRoundDate, allMatches);

        // The slots of round 2: each first-round tie (its winner) or a club with a bye. They alternate, so each
        // first-round winner meets a club with a bye while there are byes left.
        List<Object> slots = new ArrayList<>();
        int nextTie = 0;
        int nextBye = 0;
        while (nextTie < round1.size() || nextBye < byeTeams.size()) {
            if (nextTie < round1.size()) {
                slots.add(round1.get(nextTie++));
            }
            if (nextBye < byeTeams.size()) {
                slots.add(byeTeams.get(nextBye++));
            }
        }

        LocalDateTime currentRoundDate = firstRoundDate;
        for (int round = 2; round <= numRounds; round++) {
            currentRoundDate = currentRoundDate.plusWeeks(weeksBetweenRounds);
            List<Match> roundMatches = generateNextRound(slots, competitionId, editionId, round, currentRoundDate,
                allMatches);
            slots = new ArrayList<>(roundMatches);
        }

        System.out.println("CupBracketGenerator: *** COMPLETE BRACKET GENERATED ***");
        System.out.println("CupBracketGenerator: Total matches: " + allMatches.size());
        
        // Log all matches by round
        Map<Integer, List<Match>> matchesByRound = new java.util.HashMap<>();
        for (Match m : allMatches) {
            if (m.getRound() != null) {
                matchesByRound.putIfAbsent(m.getRound(), new java.util.ArrayList<>());
                matchesByRound.get(m.getRound()).add(m);
            }
        }
        
        for (Map.Entry<Integer, List<Match>> entry : matchesByRound.entrySet()) {
            System.out.println("CupBracketGenerator: Round " + entry.getKey() + ": " + entry.getValue().size() + " matches");
            for (Match m : entry.getValue()) {
                System.out.println("  - " + m.getBracketPath() + " (ID: " + m.getId() + 
                    ", Parent1: " + m.getParentMatch1Id() + ", Parent2: " + m.getParentMatch2Id() + 
                    ", Date: " + m.getMatchDateTime() + ")");
            }
        }
        
        Gdx.app.log("CupBracketGenerator", "Complete bracket generated: " + allMatches.size() + " total matches");
        
        return allMatches;
    }
    
    /**
     * Generate first round matches
     */
    private List<Match> generateFirstRound(
            List<Long> teams,
            Long competitionId,
            Long editionId,
            LocalDateTime matchDate,
            List<Match> allMatches) {
        
        List<Match> roundMatches = new ArrayList<>();
        List<Long> remainingTeams = new ArrayList<>(teams);
        int matchNumber = 1;
        
        while (!remainingTeams.isEmpty() && remainingTeams.size() >= 2) {
            Match match = new Match();
            
            // Set match ID
            match.setId(ids.nextId());
            
            // Pick random teams
            int homeIndex = rng.nextInt(remainingTeams.size());
            Long homeTeamId = remainingTeams.remove(homeIndex);
            
            int awayIndex = rng.nextInt(remainingTeams.size());
            Long awayTeamId = remainingTeams.remove(awayIndex);
            
            // Set teams
            match.setHomeClubId(homeTeamId);
            match.setAwayClubId(awayTeamId);
            
            // Set competition info
            match.setCompetitionId(competitionId);
            match.setCompetitionEditionId(editionId);
            match.setMatchType(Match.CUP_MATCH);
            match.setRound(1);
            match.setBracketPosition(matchNumber);
            match.setBracketPath("R1M" + matchNumber);
            
            // First round has no parent matches
            match.setParentMatch1Id(null);
            match.setParentMatch2Id(null);
            
            // Set match date
            match.setMatchDateTime(matchDate);
            match.setIsProposed(false);
            match.setIsAccepted(true);
            match.setIsPlayed(false);
            
            roundMatches.add(match);
            allMatches.add(match);
            
            matchNumber++;
        }
        
        Gdx.app.log("CupBracketGenerator", "Round 1: Generated " + roundMatches.size() + " matches");
        
        return roundMatches;
    }
    
    /**
     * Generate a round after the first: pairs consecutive slots. A slot is the tie whose winner comes here
     * (parent 1 = home, parent 2 = away, as CupBracketManager.advanceWinner fills them) or a club already known
     * (a bye into round 2).
     */
    private List<Match> generateNextRound(
            List<Object> slots,
            Long competitionId,
            Long editionId,
            int roundNumber,
            LocalDateTime matchDate,
            List<Match> allMatches) {

        List<Match> roundMatches = new ArrayList<>();
        int matchNumber = 1;
        for (int i = 0; i + 1 < slots.size(); i += 2) {
            Match match = new Match();
            match.setId(ids.nextId());

            Object home = slots.get(i);
            Object away = slots.get(i + 1);
            if (home instanceof Match) {
                match.setParentMatch1Id(((Match) home).getId());
            } else {
                match.setHomeClubId((Long) home);
            }
            if (away instanceof Match) {
                match.setParentMatch2Id(((Match) away).getId());
            } else {
                match.setAwayClubId((Long) away);
            }

            match.setCompetitionId(competitionId);
            match.setCompetitionEditionId(editionId);
            match.setMatchType(Match.CUP_MATCH);
            match.setRound(roundNumber);
            match.setBracketPosition(matchNumber);
            match.setBracketPath("R" + roundNumber + "M" + matchNumber);
            match.setMatchDateTime(matchDate);
            match.setIsProposed(false);
            match.setIsAccepted(true);
            match.setIsPlayed(false);

            roundMatches.add(match);
            allMatches.add(match);
            matchNumber++;
        }

        String roundName = getRoundName(roundNumber, roundMatches.size() * 2);
        Gdx.app.log("CupBracketGenerator", roundName + " (Round " + roundNumber + "): Generated " + roundMatches.size() + " matches");

        return roundMatches;
    }

    /**
     * Get round name (First Round, Second Round, Semi-Final, Final, etc.)
     */
    private String getRoundName(int roundNumber, int participants) {
        if (participants == 2) return "Final";
        if (participants == 4) return "Semi-Final";
        if (participants == 8) return "Quarter-Final";
        
        String[] ordinals = {"", "First", "Second", "Third", "Fourth", "Fifth", "Sixth"};
        if (roundNumber > 0 && roundNumber < ordinals.length) {
            return ordinals[roundNumber] + " Round";
        }
        return "Round " + roundNumber;
    }
}
