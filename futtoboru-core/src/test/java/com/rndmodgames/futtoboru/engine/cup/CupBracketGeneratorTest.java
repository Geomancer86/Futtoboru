package com.rndmodgames.futtoboru.engine.cup;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Unit tests for CupBracketGenerator
 * 
 * CRITICAL: Tests that semi-finals ALWAYS have exactly 2 matches (not 3!)
 * 
 * Run with: mvn test -Dtest=CupBracketGeneratorTest
 */
public class CupBracketGeneratorTest {
    
    @BeforeAll
    static void initializeLibGDX() {
        // Initialize LibGDX for headless testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(), config);
    }
    
    @Test
    void testSemiFinalsAlwaysHas2Matches() {
        // This is the bug you found - semi-finals should have exactly 2 matches
        CupBracketGenerator generator = new CupBracketGenerator();
        
        // Test with 8 teams (should have semi-finals in round 2)
        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            teamIds.add((long) i);
        }
        
        LocalDateTime firstRoundDate = LocalDateTime.of(1888, 9, 1, 15, 0);
        List<Match> allMatches = generator.generateCompleteBracket(
            teamIds, 1L, 1L, firstRoundDate, 2
        );
        
        // Count matches by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match match : allMatches) {
            if (match.getRound() != null) {
                matchesByRound.put(match.getRound(), 
                    matchesByRound.getOrDefault(match.getRound(), 0) + 1);
            }
        }
        
        // For 8 teams: Round 1 = 4 matches, Round 2 = 2 matches (semi-finals), Round 3 = 1 match (final)
        // Find the round with exactly 2 matches (semi-finals)
        int semiFinalRound = -1;
        for (Map.Entry<Integer, Integer> entry : matchesByRound.entrySet()) {
            if (entry.getValue() == 2) {
                semiFinalRound = entry.getKey();
                break;
            }
        }
        
        assertTrue(semiFinalRound > 0, "Should find a semi-final round with 2 matches");
        assertEquals(2, matchesByRound.get(semiFinalRound), 
            "CRITICAL BUG: Semi-finals (round " + semiFinalRound + ") should have exactly 2 matches, but found " + matchesByRound.get(semiFinalRound));
        
        // Verify final has exactly 1 match
        int maxRound = matchesByRound.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        assertEquals(1, matchesByRound.get(maxRound), 
            "Final (round " + maxRound + ") should have exactly 1 match");
    }
    
    @Test
    void testCompleteBracketStructureFor32Teams() {
        CupBracketGenerator generator = new CupBracketGenerator();
        
        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            teamIds.add((long) i);
        }
        
        LocalDateTime firstRoundDate = LocalDateTime.of(1888, 9, 1, 15, 0);
        List<Match> allMatches = generator.generateCompleteBracket(
            teamIds, 1L, 1L, firstRoundDate, 2
        );
        
        // Count matches by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match match : allMatches) {
            if (match.getRound() != null) {
                matchesByRound.put(match.getRound(), 
                    matchesByRound.getOrDefault(match.getRound(), 0) + 1);
            }
        }
        
        // Verify bracket structure
        assertEquals(16, matchesByRound.getOrDefault(1, 0), "Round 1 should have 16 matches");
        assertEquals(8, matchesByRound.getOrDefault(2, 0), "Round 2 should have 8 matches");
        assertEquals(4, matchesByRound.getOrDefault(3, 0), "Round 3 should have 4 matches");
        assertEquals(2, matchesByRound.getOrDefault(4, 0), "Round 4 (Semi-finals) should have 2 matches");
        assertEquals(1, matchesByRound.getOrDefault(5, 0), "Round 5 (Final) should have 1 match");
        
        // Total matches: 16 + 8 + 4 + 2 + 1 = 31
        assertEquals(31, allMatches.size(), "32 teams should generate 31 total matches");
    }
    
    @Test
    void testCompleteBracketStructureFor16Teams() {
        CupBracketGenerator generator = new CupBracketGenerator();
        
        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            teamIds.add((long) i);
        }
        
        LocalDateTime firstRoundDate = LocalDateTime.of(1888, 9, 1, 15, 0);
        List<Match> allMatches = generator.generateCompleteBracket(
            teamIds, 1L, 1L, firstRoundDate, 2
        );
        
        // Count matches by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match match : allMatches) {
            if (match.getRound() != null) {
                matchesByRound.put(match.getRound(), 
                    matchesByRound.getOrDefault(match.getRound(), 0) + 1);
            }
        }
        
        // Verify bracket structure
        assertEquals(8, matchesByRound.getOrDefault(1, 0), "Round 1 should have 8 matches");
        assertEquals(4, matchesByRound.getOrDefault(2, 0), "Round 2 should have 4 matches");
        assertEquals(2, matchesByRound.getOrDefault(3, 0), "Round 3 (Semi-finals) should have 2 matches");
        assertEquals(1, matchesByRound.getOrDefault(4, 0), "Round 4 (Final) should have 1 match");
        
        // Total matches: 8 + 4 + 2 + 1 = 15
        assertEquals(15, allMatches.size(), "16 teams should generate 15 total matches");
    }
    
    @Test
    void testCompleteBracketStructureFor8Teams() {
        CupBracketGenerator generator = new CupBracketGenerator();
        
        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            teamIds.add((long) i);
        }
        
        LocalDateTime firstRoundDate = LocalDateTime.of(1888, 9, 1, 15, 0);
        List<Match> allMatches = generator.generateCompleteBracket(
            teamIds, 1L, 1L, firstRoundDate, 2
        );
        
        // Count matches by round
        Map<Integer, Integer> matchesByRound = new HashMap<>();
        for (Match match : allMatches) {
            if (match.getRound() != null) {
                matchesByRound.put(match.getRound(), 
                    matchesByRound.getOrDefault(match.getRound(), 0) + 1);
            }
        }
        
        // Verify bracket structure
        assertEquals(4, matchesByRound.getOrDefault(1, 0), "Round 1 should have 4 matches");
        assertEquals(2, matchesByRound.getOrDefault(2, 0), "Round 2 (Semi-finals) should have 2 matches");
        assertEquals(1, matchesByRound.getOrDefault(3, 0), "Round 3 (Final) should have 1 match");
        
        // Total matches: 4 + 2 + 1 = 7
        assertEquals(7, allMatches.size(), "8 teams should generate 7 total matches");
    }
    
    @Test
    void testAllMatchesHaveCorrectRoundNumbers() {
        CupBracketGenerator generator = new CupBracketGenerator();
        
        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            teamIds.add((long) i);
        }
        
        LocalDateTime firstRoundDate = LocalDateTime.of(1888, 9, 1, 15, 0);
        List<Match> allMatches = generator.generateCompleteBracket(
            teamIds, 1L, 1L, firstRoundDate, 2
        );
        
        // Verify all matches have round numbers
        for (Match match : allMatches) {
            assertNotNull(match.getRound(), "All matches should have a round number");
            assertTrue(match.getRound() >= 1 && match.getRound() <= 5, 
                "Round number should be between 1 and 5");
        }
    }
    
    @Test
    void testParentMatchLinksAreCorrect() {
        CupBracketGenerator generator = new CupBracketGenerator();
        
        List<Long> teamIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            teamIds.add((long) i);
        }
        
        LocalDateTime firstRoundDate = LocalDateTime.of(1888, 9, 1, 15, 0);
        List<Match> allMatches = generator.generateCompleteBracket(
            teamIds, 1L, 1L, firstRoundDate, 2
        );
        
        // Round 1 matches should have no parent matches
        for (Match match : allMatches) {
            if (match.getRound() == 1) {
                assertNull(match.getParentMatch1Id(), 
                    "Round 1 matches should have no parent matches");
                assertNull(match.getParentMatch2Id(), 
                    "Round 1 matches should have no parent matches");
            } else {
                // Round 2+ matches should have at least one parent
                assertNotNull(match.getParentMatch1Id(), 
                    "Round " + match.getRound() + " matches should have parent match 1");
            }
        }
    }
}

