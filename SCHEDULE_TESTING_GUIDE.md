# Schedule Engine Testing Guide

## The Problem

You've been testing manually 500+ times, finding critical bugs like:
- **3 matches in semi-finals** (should be 2!)
- Wrong number of matches per round
- Schedule conflicts
- Missing matches

## The Solution: Automated Unit Tests

I've created automated tests that run **WITHOUT starting the game**. These tests validate fixture generation in seconds, not hours.

## Running the Tests

### Quick Test (Windows)
```batch
run-schedule-tests.bat
```

### Manual Test (Maven)
```bash
mvn test -Dtest=LeagueFixtureGeneratorTest,CupBracketGeneratorTest
```

### Run All Tests
```bash
mvn test
```

## What the Tests Check

### LeagueFixtureGeneratorTest
- ✅ Correct number of matches for N clubs (N * (N-1) total)
- ✅ Each club plays exactly (N-1) * 2 matches
- ✅ Each pair of clubs plays exactly twice (home + away)
- ✅ No duplicate matches
- ✅ All matches have dates within season
- ✅ All matches are marked as LEAGUE_MATCH

### CupBracketGeneratorTest
- ✅ Round 1: N/2 matches (for N teams)
- ✅ Semi-finals: **ALWAYS 2 matches** (critical!)
- ✅ Final: **ALWAYS 1 match**
- ✅ No duplicate teams in draw
- ✅ Odd number of teams handled correctly (byes)

## Test Results Interpretation

### ✅ All Tests Pass
The schedule engine is working correctly. If you still see bugs in-game, the issue is likely:
- Data corruption
- Match advancement logic
- UI display issues

### ❌ Tests Fail
The schedule engine has bugs. The test output will show:
- Which test failed
- Expected vs actual values
- What's wrong

## Adding New Tests

When you find a new bug:

1. **Create a test that reproduces it:**
```java
@Test
void testBugDescription() {
    // Setup
    // Execute
    // Assert - this should fail initially
}
```

2. **Run the test** - it should fail (proving the bug exists)

3. **Fix the bug** in the actual code

4. **Run the test again** - it should pass (proving the bug is fixed)

## Example: Testing Semi-Finals Bug

```java
@Test
void testSemiFinalsAlwaysHas2Matches() {
    Competition cup = new Competition();
    List<Long> clubIds = new ArrayList<>();
    
    // Create 4 teams (should result in 2 semi-final matches)
    for (int i = 0; i < 4; i++) {
        createTestClub("Club " + i, (long) i);
        clubIds.add((long) i);
    }
    
    List<Match> matches = scheduler.competitionDraw(cup, clubIds);
    
    // CRITICAL: Semi-finals must have exactly 2 matches
    assertEquals(2, matches.size(), 
        "4 teams should generate 2 semi-final matches, not 3!");
}
```

## Benefits

1. **Fast**: Tests run in seconds, not hours
2. **Repeatable**: Same test always checks same thing
3. **Isolated**: Tests fixture generation without game UI
4. **Documentation**: Tests show how the system should work
5. **Regression Prevention**: Once fixed, tests prevent bugs from returning

## Next Steps

1. Run `run-schedule-tests.bat` to see current test status
2. If tests fail, fix the bugs they reveal
3. Add tests for any new bugs you find
4. Run tests before every commit

## Debugging Failed Tests

When a test fails, you'll see output like:
```
Expected: 2
Actual: 3
```

This tells you exactly what's wrong. The test name tells you what feature is broken.

## Integration with CI/CD

These tests can be run automatically:
- Before commits (git hooks)
- In CI/CD pipelines
- On every build

This prevents schedule bugs from reaching production.


