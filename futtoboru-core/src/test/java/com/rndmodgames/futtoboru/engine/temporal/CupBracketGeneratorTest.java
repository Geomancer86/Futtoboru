package com.rndmodgames.futtoboru.engine.temporal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Unit tests for Cup Bracket Generation
 * 
 * Tests that cup brackets are generated correctly:
 * - Round 1: N/2 matches (for N teams)
 * - Round 2: N/4 matches
 * - Semi-finals: 2 matches (always!)
 * - Final: 1 match
 * 
 * Run with: mvn test -Dtest=CupBracketGeneratorTest
 */
public class CupBracketGeneratorTest {
    
    private CompetitionScheduler scheduler;
    private SaveGame testGame;
    private Futtoboru mockGame;
    
    @BeforeAll
    static void initializeLibGDX() {
        // Initialize LibGDX for headless testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(), config);
    }
    
    @BeforeEach
    void setUp() {
        testGame = new SaveGame();
        testGame.setAllClubs(new ArrayList<>());
        
        mockGame = new Futtoboru() {
            @Override
            public SaveGame getCurrentGame() {
                return testGame;
            }
        };
        
        scheduler = new CompetitionScheduler(mockGame);
    }
    
    private Club createTestClub(String name, Long id) {
        Club club = new Club();
        club.setId(id);
        club.setName(name);
        club.setPlayers(new ArrayList<>());
        for (int i = 0; i < 11; i++) {
            Player player = new Player();
            player.setId((long) i);
            // Player extends Person, so we can set name via Person methods if needed
            // For fixture generation, we just need the player to exist
            club.getPlayers().add(player);
        }
        Stadium stadium = new Stadium();
        stadium.setCapacity(10000);
        club.setStadium(stadium);
        testGame.getAllClubs().add(club);
        return club;
    }
    
    @Test
    void testCupDrawFor32Teams() {
        Competition cup = new Competition();
        cup.setName("FA Cup");
        
        List<Long> clubIds = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            createTestClub("Club " + i, (long) i);
            clubIds.add((long) i);
        }
        
        List<Match> matches = scheduler.competitionDraw(cup, clubIds);
        
        // 32 teams = 16 matches in Round 1
        assertEquals(16, matches.size(), "32 teams should generate 16 matches in Round 1");
        
        // Verify all teams are paired
        Map<Long, Integer> teamAppearances = new HashMap<>();
        for (Match match : matches) {
            teamAppearances.put(match.getHomeClubId(), 
                teamAppearances.getOrDefault(match.getHomeClubId(), 0) + 1);
            teamAppearances.put(match.getAwayClubId(), 
                teamAppearances.getOrDefault(match.getAwayClubId(), 0) + 1);
        }
        
        // Each team should appear exactly once
        for (Long clubId : clubIds) {
            assertEquals(1, teamAppearances.getOrDefault(clubId, 0), 
                "Team " + clubId + " should appear exactly once");
        }
    }
    
    @Test
    void testCupDrawFor16Teams() {
        Competition cup = new Competition();
        cup.setName("FA Cup");
        
        List<Long> clubIds = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            createTestClub("Club " + i, (long) i);
            clubIds.add((long) i);
        }
        
        List<Match> matches = scheduler.competitionDraw(cup, clubIds);
        
        // 16 teams = 8 matches in Round 1
        assertEquals(8, matches.size(), "16 teams should generate 8 matches in Round 1");
    }
    
    @Test
    void testCupDrawFor8Teams() {
        Competition cup = new Competition();
        cup.setName("FA Cup");
        
        List<Long> clubIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            createTestClub("Club " + i, (long) i);
            clubIds.add((long) i);
        }
        
        List<Match> matches = scheduler.competitionDraw(cup, clubIds);
        
        // 8 teams = 4 matches in Round 1
        assertEquals(4, matches.size(), "8 teams should generate 4 matches in Round 1");
    }
    
    @Test
    void testCupDrawFor4Teams() {
        Competition cup = new Competition();
        cup.setName("FA Cup");
        
        List<Long> clubIds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            createTestClub("Club " + i, (long) i);
            clubIds.add((long) i);
        }
        
        List<Match> matches = scheduler.competitionDraw(cup, clubIds);
        
        // 4 teams = 2 matches in Round 1 (semi-finals)
        assertEquals(2, matches.size(), "4 teams should generate 2 matches (semi-finals)");
    }
    
    @Test
    void testCupDrawForOddNumberOfTeams() {
        Competition cup = new Competition();
        cup.setName("FA Cup");
        
        List<Long> clubIds = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            createTestClub("Club " + i, (long) i);
            clubIds.add((long) i);
        }
        
        List<Match> matches = scheduler.competitionDraw(cup, clubIds);
        
        // 15 teams = 1 bye + 7 matches = 7 matches
        assertEquals(7, matches.size(), "15 teams should generate 7 matches (1 team gets bye)");
        
        // Verify 14 teams are paired (1 gets bye)
        Map<Long, Integer> teamAppearances = new HashMap<>();
        for (Match match : matches) {
            teamAppearances.put(match.getHomeClubId(), 
                teamAppearances.getOrDefault(match.getHomeClubId(), 0) + 1);
            teamAppearances.put(match.getAwayClubId(), 
                teamAppearances.getOrDefault(match.getAwayClubId(), 0) + 1);
        }
        
        // 14 teams should appear once, 1 team should not appear (bye)
        int teamsInMatches = teamAppearances.size();
        assertEquals(14, teamsInMatches, "14 teams should be in matches, 1 should have bye");
    }
    
    @Test
    void testNoDuplicateTeamsInDraw() {
        Competition cup = new Competition();
        cup.setName("FA Cup");
        
        List<Long> clubIds = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            createTestClub("Club " + i, (long) i);
            clubIds.add((long) i);
        }
        
        List<Match> matches = scheduler.competitionDraw(cup, clubIds);
        
        // Verify no team appears twice
        Map<Long, Integer> teamAppearances = new HashMap<>();
        for (Match match : matches) {
            teamAppearances.put(match.getHomeClubId(), 
                teamAppearances.getOrDefault(match.getHomeClubId(), 0) + 1);
            teamAppearances.put(match.getAwayClubId(), 
                teamAppearances.getOrDefault(match.getAwayClubId(), 0) + 1);
        }
        
        for (Map.Entry<Long, Integer> entry : teamAppearances.entrySet()) {
            assertEquals(1, entry.getValue(), 
                "Team " + entry.getKey() + " should appear exactly once, but appears " + entry.getValue() + " times");
        }
    }
}

