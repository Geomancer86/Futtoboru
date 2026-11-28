package com.rndmodgames.futtoboru.engine.simulation;

import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.engine.injuries.InjuryRules;
import com.rndmodgames.futtoboru.engine.injuries.MatchInjuries;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.engine.team.TeamStrength;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Match Simulator v1.0
 * 
 * Simulates football matches and generates results.
 * 
 * Algorithm (v1.0 - Simple; G-01 team sheets):
 * - Each side's team sheet is frozen on the match at kick-off ({@link TeamSheets#kickOff}: the player's XI, an AI
 *   club's own pick); strength comes from the eleven who play ({@link TeamStrength}: attack and defence by place,
 *   out of position and playing short cost, small shape / style / captain modifiers)
 * - Apply home advantage (15% boost)
 * - Add randomness (20-30%)
 * - Generate scores based on strength difference
 * 
 * @author Geomancer86
 */
public class MatchSimulator {
    
    private static final double HOME_ADVANTAGE_MULTIPLIER = 1.15; // 15% home advantage
    private static final double RANDOMNESS_MIN = 0.7;
    private static final double RANDOMNESS_MAX = 1.3;
    private static final int MAX_GOALS = 5; // Cap goals at 5 for v1.0
    
    private SaveGame currentGame;
    private Futtoboru gameInstance;
    private final RandomSource root;
    private RandomSource random;
    
    /** Draws from the game's random source ({@code game.random()}). */
    public MatchSimulator(Futtoboru gameInstance) {
        this(gameInstance, Futtoboru.randomOf(gameInstance));
    }
    
    /**
     * @param root each match draws from {@code root.fork("match:" + id)}, its gate from {@code fork("gate:" + id)} and
     *             its goal minutes from {@code fork("minutes:" + id)} (G-03), its injuries from
     *             {@code fork("injuries:" + id)} (G-02),
     *             so a result depends on the seed and the match, not on the order the day's matches are played
     */
    public MatchSimulator(Futtoboru gameInstance, RandomSource root) {
        this.currentGame = gameInstance.getCurrentGame();
        this.gameInstance = gameInstance;
        this.root = root;
    }
    
    /**
     * Simulate a match and generate result
     * 
     * @param match The match to simulate
     * @return true if simulation was successful, false otherwise
     */
    public boolean simulateMatch(Match match) {
        if (match == null || match.getIsPlayed()) {
            return false; // Match already played or invalid
        }
        
        // CRITICAL: Validate teams exist before simulation
        if (match.getHomeClubId() == null || match.getAwayClubId() == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - missing teams (Home: " + 
                match.getHomeClubId() + ", Away: " + match.getAwayClubId() + ")");
            Log.error(Log.ENGINE_MATCH, "MatchSimulator: Cannot simulate match - missing teams");
            return false;
        }
        
        // Get clubs
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            Log.error(Log.ENGINE_MATCH, "MatchSimulator", "Cannot simulate match - club not found (Home ID: " + 
                match.getHomeClubId() + ", Away ID: " + match.getAwayClubId() + ")");
            Log.error(Log.ENGINE_MATCH, "MatchSimulator: Cannot simulate match - club not found");
            return false;
        }
        
        // One stream per match (P2-0)
        String matchKey = match.getId() != null ? String.valueOf(match.getId())
                : match.getHomeClubId() + "-" + match.getAwayClubId() + "@" + match.getMatchDateTime();
        this.random = root.fork("match:" + matchKey);
        RandomSource gate = root.fork("gate:" + matchKey);
        
        // G-01: the team sheets at kick-off, frozen on the match; strength from the eleven who play
        TeamSheet homeSheet = TeamSheets.kickOff(match, homeClub, true, currentGame, root);
        TeamSheet awaySheet = TeamSheets.kickOff(match, awayClub, false, currentGame, root);
        TeamStrength homeStrength = TeamStrength.of(homeSheet, TeamSheets.squadById(homeClub));
        TeamStrength awayStrength = TeamStrength.of(awaySheet, TeamSheets.squadById(awayClub));

        // G-02 (D19-3): men hurt during the match, on their own stream (the other draws stay the same); a passenger
        // is worth less from his minute on, a man carried off nothing while off (no substitutes)
        InjuryRules injuryRules = InjuryRules.current();
        RandomSource injuries = root.fork("injuries:" + matchKey);
        java.util.List<Player> homeMen = onField(homeSheet, homeClub);
        java.util.List<Player> awayMen = onField(awaySheet, awayClub);
        match.setHomeInjuries(MatchInjuries.roll(homeMen, injuryRules, injuries.fork("home")));
        match.setAwayInjuries(MatchInjuries.roll(awayMen, injuryRules, injuries.fork("away")));
        double homeHurt = hurtFactor(homeMen.size(), match.getHomeInjuries(), injuryRules);
        double awayHurt = hurtFactor(awayMen.size(), match.getAwayInjuries(), injuryRules);
        
        // Apply home advantage (P2-14: none at a neutral ground, a cup semi-final or final)
        double homeAdvantage = com.rndmodgames.futtoboru.engine.cup.NeutralVenues.isNeutral(match)
                ? 1.0 : HOME_ADVANTAGE_MULTIPLIER;
        
        // Add randomness
        double homeRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double awayRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        
        double homeAttack = homeStrength.attack() * homeAdvantage * homeRandom * homeHurt;
        double homeDefence = homeStrength.defence() * homeAdvantage * homeRandom * homeHurt;
        double awayAttack = awayStrength.attack() * awayRandom * awayHurt;
        double awayDefence = awayStrength.defence() * awayRandom * awayHurt;
        
        // Generate scores: each side's attack against the other side's defence
        int homeScore = generateScore(homeAttack, awayDefence, true);
        int awayScore = generateScore(awayAttack, homeDefence, false);
        
        // Update match with results
        match.setHomeGoals(homeScore);
        match.setAwayGoals(awayScore);
        match.setIsPlayed(true);
        
        // Generate scorers (v1.0 - Awards System)
        generateScorers(match, homeClub, awayClub, homeScore, awayScore);

        // Goal minutes (G-03): after the scorers, on their own stream, so scores and scorers never depend on them
        GoalMinutes.assign(match, root.fork("minutes:" + matchKey));

        // Calculate attendance (simple: based on the capacity of the ground played at, P2-14: a neutral venue)
        com.rndmodgames.futtoboru.data.Stadium ground = com.rndmodgames.futtoboru.engine.cup.NeutralVenues.isNeutral(match)
                ? com.rndmodgames.futtoboru.system.DatabaseLoader.getGroundByDataId(match.getVenue()) : null;
        if (ground == null || ground.getCapacity() == null) {
            ground = homeClub.getStadium();
        }
        if (ground != null && ground.getCapacity() != null) {
            // 60-90% attendance for v1.0
            int capacity = ground.getCapacity();
            int attendance = (int)(capacity * (0.6 + gate.nextDouble() * 0.3));
            match.setAttendance(attendance);
        } else {
            // Default attendance if no stadium data
            match.setAttendance(1000 + gate.nextInt(4000)); // 1000-5000
        }
        
        Log.info(Log.ENGINE_MATCH, "MatchSimulator: " + homeClub.getName() + " " + homeScore + 
                          " - " + awayScore + " " + awayClub.getName());
        
        // Club statistics, points, cup advancement and match lists: MatchResultRecorder.record (P2-2)
        
        return true;
    }
    
    /**
     * Generate goal scorers for a match
     */
    private void generateScorers(Match match, Club homeClub, Club awayClub, int homeScore, int awayScore) {
        // Clear existing scorers
        match.getHomeScorerIds().clear();
        match.getAwayScorerIds().clear();
        
        // Generate home scorers
        if (homeScore > 0 && homeClub.getPlayers() != null && !homeClub.getPlayers().isEmpty()) {
            for (int i = 0; i < homeScore; i++) {
                Player scorer = pickScorer(match.getHomeSheet(), homeClub, match.getHomeInjuries());
                if (scorer != null && scorer.getPerson() != null) {
                    match.getHomeScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
        
        // Generate away scorers
        if (awayScore > 0 && awayClub.getPlayers() != null && !awayClub.getPlayers().isEmpty()) {
            for (int i = 0; i < awayScore; i++) {
                Player scorer = pickScorer(match.getAwaySheet(), awayClub, match.getAwayInjuries());
                if (scorer != null && scorer.getPerson() != null) {
                    match.getAwayScorerIds().add(scorer.getPerson().getId());
                }
            }
        }
    }
    
    /**
     * G-01: one of the eleven who played scores, weighted by the position of his place (forwards most, the keeper
     * almost never) and his finishing; one draw per goal ({@link TeamStrength#pickScorer}).
     */
    private Player pickScorer(TeamSheet sheet, Club club, java.util.List<MatchInjury> injuries) {
        Map<Long, Player> squad = TeamSheets.squadById(club);
        // G-02: a man carried off for good scores no more (kept simple: not at all in this match)
        if (injuries != null && !injuries.isEmpty()) {
            squad.values().removeIf(p -> p.getPerson() != null && injuries.stream().anyMatch(
                    i -> MatchInjuries.leftForGood(i) && p.getPerson().getId().equals(i.getPersonId())));
        }
        return TeamStrength.pickScorer(sheet, squad, random);
    }

    /** G-02: the men on the sheet's places, in slot order (empty places: the side plays short). */
    private static java.util.List<Player> onField(TeamSheet sheet, Club club) {
        java.util.List<Player> out = new java.util.ArrayList<>();
        if (sheet == null) {
            return out;
        }
        Map<Long, Player> squad = TeamSheets.squadById(club);
        for (int s = 0; s < com.rndmodgames.futtoboru.data.TeamShape.SLOTS; s++) {
            Player p = sheet.getPlayerId(s) != null ? squad.get(sheet.getPlayerId(s)) : null;
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }

    /**
     * G-02: what the side is worth over the match next to the same men unhurt (1 when nobody is hurt; playing short
     * from kick-off is already in {@link TeamStrength}).
     */
    static double hurtFactor(int menOnField, java.util.List<MatchInjury> injuries, InjuryRules rules) {
        if (menOnField == 0 || injuries == null || injuries.isEmpty()) {
            return 1.0;
        }
        return MatchInjuries.effectiveMen(menOnField, injuries, rules) / menOnField;
    }
    
    /**
     * Generate score for a team based on strength
     * 
     * Score distribution (v1.0):
     * - 30% chance: 0 goals
     * - 30% chance: 1 goal
     * - 25% chance: 2 goals
     * - 10% chance: 3 goals
     * - 5% chance: 4-5 goals
     * 
     * Adjusted by team strength vs opponent
     */
    private int generateScore(double teamStrength, double opponentStrength, boolean isHome) {
        // Calculate strength ratio
        double totalStrength = teamStrength + opponentStrength;
        double strengthRatio = totalStrength > 0 ? teamStrength / totalStrength : 0.5;
        
        // Base goal distribution
        double random = this.random.nextDouble();
        int baseGoals = 0;
        
        if (random < 0.3) {
            baseGoals = 0;      // 30% chance: 0 goals
        } else if (random < 0.6) {
            baseGoals = 1;      // 30% chance: 1 goal
        } else if (random < 0.85) {
            baseGoals = 2;      // 25% chance: 2 goals
        } else if (random < 0.95) {
            baseGoals = 3;      // 10% chance: 3 goals
        } else {
            baseGoals = 4 + this.random.nextInt(2); // 5% chance: 4-5 goals
        }
        
        // Adjust based on strength ratio
        if (strengthRatio > 0.6) {
            // Strong team: +1 goal bonus
            baseGoals += 1;
        } else if (strengthRatio > 0.55) {
            // Slightly stronger: +0.5 goal (round up 50% of the time)
            if (this.random.nextDouble() < 0.5) {
                baseGoals += 1;
            }
        } else if (strengthRatio < 0.4) {
            // Weak team: -1 goal penalty
            baseGoals = Math.max(0, baseGoals - 1);
        } else if (strengthRatio < 0.45) {
            // Slightly weaker: -0.5 goal (round down 50% of the time)
            if (this.random.nextDouble() < 0.5) {
                baseGoals = Math.max(0, baseGoals - 1);
            }
        }
        
        // Cap at maximum
        return Math.min(MAX_GOALS, baseGoals);
    }
    
}

