# League & Cup Scheduling Analysis & Design v1.0

**Analysis Date:** 2025-12-14  
**Branch:** feature/match-simulation-v1  
**Priority:** CRITICAL - Required for functional leagues and cups

---

## Executive Summary

**Current State:** 
- ✅ Leagues are created via scripts (LeagueCreationScript)
- ✅ Competition structure exists (Competition, CompetitionEdition, League)
- ❌ **League fixtures are NOT being scheduled automatically**
- ❌ **Cup draws are NOT being created automatically**
- ❌ **No league standings system**
- ❌ **No cup round progression**
- ❌ **No league/cup screens in UI**
- ❌ **Points system fixed (only awards for league matches)**

**Goal:** Implement complete league and cup scheduling, progression, and UI systems to enable functional competitions.

---

## 1. CURRENT STATE ANALYSIS

### 1.1 What Exists ✅

#### Data Models
- ✅ **League**: Has `leagueClubs` list, but no standings tracking
- ✅ **Competition**: Has type (CUP/LEAGUE), editions, but incomplete
- ✅ **CompetitionEdition**: Tracks yearly occurrences
- ✅ **Match**: Has `matchType` (FRIENDLY_MATCH, LEAGUE_MATCH, CUP_MATCH constants)
- ✅ **Club**: Has statistics fields (wins, losses, goals, points)

#### Engine Components
- ✅ **ScriptsManager**: Creates leagues via `createLeague()` script
- ✅ **CompetitionScheduler**: Has `competitionDraw()` method (incomplete)
- ✅ **AuthorityManager**: Has `checkCompetitionsSchedule()` (stub, TODO comments)
- ✅ **MatchScheduler**: Handles friendly scheduling only
- ✅ **MatchSimulator**: Simulates matches and updates statistics

#### UI Components
- ✅ **CompetitionsScreenTable**: Basic competitions screen (needs expansion)
- ❌ **No League Standings Screen**
- ❌ **No Cup Draw Screen**
- ❌ **No League Fixtures Screen**

### 1.2 What's Missing ❌

#### League System
- ❌ **League Fixture Generation**: No automatic scheduling of home/away fixtures
- ❌ **League Standings**: No tracking of positions, sorting by points/goal difference
- ❌ **League Standings Screen**: No UI to view league table
- ❌ **League Fixtures Screen**: No UI to view upcoming/past league matches
- ❌ **Season Start/End**: No automatic season initialization or completion

#### Cup System
- ❌ **Cup Draw Generation**: `competitionDraw()` exists but incomplete
- ❌ **Cup Round Progression**: No logic to advance winners to next round
- ❌ **Cup Draw Screen**: No UI to view cup brackets/draws
- ❌ **Cup Fixtures Screen**: No UI to view cup matches by round
- ❌ **Cup Seeding**: No seeding system for cup draws

#### Scheduling System
- ❌ **Automatic League Scheduling**: Leagues created but fixtures never scheduled
- ❌ **Automatic Cup Scheduling**: Cups exist but draws/matches never created
- ❌ **Competition Integration**: `AuthorityManager.checkCompetitionsSchedule()` is empty stub

---

## 2. REQUIREMENTS

### 2.1 League System Requirements

#### League Fixture Generation
1. **Generate Full Season Fixtures**
   - Home and away matches for all clubs in league
   - Round-robin format (each club plays every other club twice)
   - Schedule matches across season dates
   - Avoid scheduling conflicts with other competitions

2. **Fixture Distribution**
   - Spread matches evenly across season
   - Consider stadium availability
   - Avoid too many matches in short periods
   - Respect competition rules (e.g., no matches on certain days)

3. **Match Type Assignment**
   - Set `matchType = Match.LEAGUE_MATCH` for all league fixtures
   - Link matches to league/competition edition

#### League Standings System
1. **Standings Tracking**
   - Position in table
   - Matches played, won, drawn, lost
   - Goals scored, goals conceded, goal difference
   - Points (3 for win, 1 for draw, 0 for loss)
   - Form (last 5 matches)

2. **Standings Sorting**
   - Primary: Points (descending)
   - Secondary: Goal difference (descending)
   - Tertiary: Goals scored (descending)
   - Quaternary: Head-to-head (if applicable)

3. **Standings Updates**
   - Update after each league match
   - Recalculate positions
   - Sort table

#### League UI Screens
1. **League Standings Screen**
   - Table view with all clubs
   - Sortable columns
   - Highlight current club
   - Show form (last 5 results)
   - Show next fixture

2. **League Fixtures Screen**
   - List all league matches
   - Filter by date range
   - Show results for played matches
   - Show scheduled dates for upcoming matches
   - Group by matchday/round

### 2.2 Cup System Requirements

#### Cup Draw Generation
1. **Initial Draw**
   - Random pairing of clubs
   - Handle odd number of clubs (bye for one club)
   - Create matches for first round
   - Set match dates

2. **Seeding System (v1.1+)**
   - Seed clubs based on reputation/previous performance
   - Avoid top seeds meeting in early rounds
   - For v1.0: Random draw (no seeding)

3. **Round Progression**
   - After each round, advance winners
   - Generate next round draw
   - Continue until final
   - Handle byes if odd number of clubs

#### Cup Round Management
1. **Round Tracking**
   - Track current round (First Round, Second Round, Quarter-Final, Semi-Final, Final)
   - Track which clubs are still in competition
   - Track eliminated clubs

2. **Match Scheduling**
   - Schedule matches for each round
   - Set appropriate dates (e.g., weekends)
   - Avoid conflicts with league matches

#### Cup UI Screens
1. **Cup Draw Screen**
   - Show bracket/tree view of cup
   - Show current round
   - Show matchups
   - Show results (when played)
   - Highlight current club

2. **Cup Fixtures Screen**
   - List matches by round
   - Show results
   - Show upcoming matches
   - Show progression path

### 2.3 Competition Scheduling Integration

#### AuthorityManager Enhancement
1. **Daily Competition Check**
   - Check if leagues need fixtures generated
   - Check if cups need draws created
   - Check if rounds need to be advanced
   - Schedule matches appropriately

2. **Season Management**
   - Initialize new seasons for leagues
   - Complete seasons and determine champions
   - Handle relegation/promotion (v1.1+)
   - Reset statistics for new season

3. **Competition Lifecycle**
   - Create competition editions
   - Schedule initial matches
   - Progress through rounds/seasons
   - Complete competitions
   - Award prizes

---

## 3. TECHNICAL DESIGN

### 3.1 League Fixture Generation Algorithm

#### Round-Robin Tournament Algorithm
```
For a league with N clubs:
1. Create array of club IDs
2. If N is odd, add "BYE" placeholder
3. For each round (N-1 rounds for home-and-away):
   a. Pair clubs: first vs last, second vs second-last, etc.
   b. Rotate array (keep first fixed, rotate others)
   c. Create matches for this round
   d. Alternate home/away for return fixtures
4. Schedule matches across season dates
5. Set matchType = LEAGUE_MATCH
6. Add to clubs' scheduledMatches lists
```

#### Implementation
```java
public class LeagueFixtureGenerator {
    public List<Match> generateLeagueFixtures(League league, LocalDateTime seasonStart, LocalDateTime seasonEnd) {
        // Generate all home-and-away fixtures
        // Schedule across season dates
        // Return list of matches
    }
}
```

### 3.2 League Standings System

#### Data Structure
```java
public class LeagueStandings {
    private League league;
    private List<ClubStanding> standings = new ArrayList<>();
    
    public void updateStandings(Match match) {
        // Update both clubs' records
        // Recalculate positions
        // Sort standings
    }
    
    public void sortStandings() {
        // Sort by points, goal difference, goals scored
    }
}

public class ClubStanding {
    private Club club;
    private Integer position;
    private Integer matchesPlayed;
    private Integer matchesWon;
    private Integer matchesDrawn;
    private Integer matchesLost;
    private Integer goalsScored;
    private Integer goalsConceded;
    private Integer goalDifference;
    private Integer points;
    private String form; // Last 5 results: "WWDLW"
}
```

#### Integration Points
- Update standings after each league match simulation
- Store standings in League or separate LeagueStandings object
- Recalculate positions after each update

### 3.3 Cup Draw Generation

#### Algorithm
```java
public class CupDrawGenerator {
    public List<Match> generateCupDraw(Competition cup, CompetitionEdition edition, int round) {
        // Get remaining clubs in competition
        // Randomly pair clubs
        // Handle bye if odd number
        // Create matches
        // Set matchType = CUP_MATCH
        // Schedule matches
        // Return list of matches
    }
    
    public void advanceCupRound(Competition cup, CompetitionEdition edition) {
        // Get winners from previous round
        // Generate next round draw
        // Schedule matches
    }
}
```

### 3.4 Competition Scheduling Integration

#### AuthorityManager Implementation
```java
public void checkCompetitionsSchedule() {
    // Iterate all leagues
    for (League league : getAllLeagues()) {
        // Check if season exists
        // Check if fixtures are scheduled
        if (!hasFixturesScheduled(league)) {
            generateLeagueFixtures(league);
        }
        
        // Check if season is complete
        if (isSeasonComplete(league)) {
            completeSeason(league);
            initializeNewSeason(league);
        }
    }
    
    // Iterate all cups
    for (Competition cup : getAllCups()) {
        // Check if current edition exists
        CompetitionEdition edition = getCurrentEdition(cup);
        
        if (edition == null) {
            createNewEdition(cup);
        } else {
            // Check if draw is needed
            if (needsDraw(edition)) {
                generateCupDraw(cup, edition);
            }
            
            // Check if round needs to be advanced
            if (isRoundComplete(edition)) {
                advanceCupRound(cup, edition);
            }
        }
    }
}
```

---

## 4. IMPLEMENTATION PLAN

### Phase 1: League Fixture Generation (Priority 1)
**Goal:** Automatically schedule league matches

1. **Create LeagueFixtureGenerator class**
   - Implement round-robin algorithm
   - Generate home-and-away fixtures
   - Schedule matches across season

2. **Integrate into AuthorityManager**
   - Check if league needs fixtures
   - Generate fixtures on season start
   - Set matchType = LEAGUE_MATCH

3. **Test Fixture Generation**
   - Verify all clubs play each other twice
   - Verify matches are scheduled
   - Verify matchType is set correctly

### Phase 2: League Standings System (Priority 2)
**Goal:** Track and display league table

1. **Create LeagueStandings class**
   - Track standings per club
   - Implement sorting logic
   - Update after matches

2. **Integrate with MatchSimulator**
   - Update standings after league matches
   - Recalculate positions

3. **Create League Standings Screen**
   - Display table
   - Sortable columns
   - Highlight current club

### Phase 3: Cup Draw Generation (Priority 3)
**Goal:** Generate cup draws and manage rounds

1. **Complete CupDrawGenerator**
   - Implement draw algorithm
   - Handle byes
   - Create matches

2. **Implement Round Progression**
   - Advance winners
   - Generate next round
   - Track eliminated clubs

3. **Integrate into AuthorityManager**
   - Check if cup needs draw
   - Generate draws
   - Advance rounds

### Phase 4: Cup UI Screens (Priority 4)
**Goal:** Display cup draws and progression

1. **Create Cup Draw Screen**
   - Show bracket/tree
   - Show current round
   - Show results

2. **Create Cup Fixtures Screen**
   - List matches by round
   - Show progression

### Phase 5: League UI Screens (Priority 5)
**Goal:** Display league fixtures and information

1. **Create League Fixtures Screen**
   - List all league matches
   - Filter by date
   - Show results

2. **Enhance League Standings Screen**
   - Add form column
   - Add next fixture
   - Add matchday grouping

---

## 5. DATA MODEL CHANGES

### 5.1 League Class Extensions
```java
// Add to League class:
private List<LeagueStanding> standings; // Or use Club statistics
private LocalDateTime seasonStartDate;
private LocalDateTime seasonEndDate;
private Integer currentMatchday;
private Boolean seasonComplete = false;
```

### 5.2 CompetitionEdition Extensions
```java
// Add to CompetitionEdition class:
private Integer currentRound; // For cups
private List<Long> remainingClubIds; // Clubs still in competition
private List<Long> eliminatedClubIds; // Eliminated clubs
private Boolean isComplete = false;
```

### 5.3 Match Class Extensions
```java
// Add to Match class (if not exists):
private Long leagueId; // Link to league
private Long competitionEditionId; // Link to competition edition
private Integer matchday; // For leagues
private Integer round; // For cups
```

---

## 6. UI SCREEN DESIGNS

### 6.1 League Standings Screen
```
┌─────────────────────────────────────────────────────────┐
│ League Standings: English Football League 1888-89       │
├────┬──────────────────┬──┬──┬──┬──┬──┬──┬──┬──┬─────────┤
│Pos │ Club            │MP│W │D │L │GF│GA│GD│Pts│Form     │
├────┼──────────────────┼──┼──┼──┼──┼──┼──┼──┼──┼─────────┤
│ 1  │ Preston North E │10│8 │2 │0 │25│8 │17│26│WWWWW    │
│ 2  │ Aston Villa     │10│7 │2 │1 │22│12│10│23│WWDLW    │
│ 3  │ Wolverhampton   │10│6 │3 │1 │20│11│9 │21│WWDWW    │
│... │ ...             │  │  │  │  │  │  │  │  │         │
└────┴──────────────────┴──┴──┴──┴──┴──┴──┴──┴──┴─────────┘
```

### 6.2 League Fixtures Screen
```
┌─────────────────────────────────────────────────────────┐
│ League Fixtures: English Football League 1888-89         │
├─────────────────────────────────────────────────────────┤
│ Matchday 1                                               │
│   Sep 8, 1888: Preston North End 5-2 Burnley            │
│   Sep 8, 1888: Aston Villa 1-1 Wolverhampton            │
│                                                           │
│ Matchday 2                                               │
│   Sep 15, 1888: Burnley vs Preston North End             │
│   Sep 15, 1888: Wolverhampton vs Aston Villa             │
└─────────────────────────────────────────────────────────┘
```

### 6.3 Cup Draw Screen
```
┌─────────────────────────────────────────────────────────┐
│ FA Cup 1888-89 Draw                                      │
├─────────────────────────────────────────────────────────┤
│ First Round                                              │
│   Preston North End 3-1 Burnley                          │
│   Aston Villa vs Wolverhampton (Sep 20, 1888)           │
│                                                           │
│ Second Round                                             │
│   Preston North End vs [Winner of Villa/Wolves]          │
│   [TBD]                                                  │
└─────────────────────────────────────────────────────────┘
```

---

## 7. INTEGRATION POINTS

### 7.1 AuthorityManager Integration
- Call `checkCompetitionsSchedule()` daily in `FuttoboruGameEngine.continueGame()`
- Generate fixtures when leagues are created
- Generate draws when cups start
- Advance rounds when complete

### 7.2 MatchSimulator Integration
- Already updates club statistics
- Need to update league standings after league matches
- Need to advance cup rounds after cup matches

### 7.3 UI Integration
- Add League Standings to main menu
- Add League Fixtures to main menu
- Add Cup Draw to main menu
- Add Cup Fixtures to main menu
- Link from Competitions screen

---

## 8. TESTING PLAN

### 8.1 League Fixture Generation
- [ ] Verify all clubs play each other twice
- [ ] Verify matches are scheduled across season
- [ ] Verify matchType is LEAGUE_MATCH
- [ ] Verify no scheduling conflicts

### 8.2 League Standings
- [ ] Verify standings update after matches
- [ ] Verify sorting is correct
- [ ] Verify positions recalculate
- [ ] Verify UI displays correctly

### 8.3 Cup Draws
- [ ] Verify draws are created
- [ ] Verify matches are scheduled
- [ ] Verify round progression works
- [ ] Verify winners advance correctly

---

## 9. ESTIMATED TIMELINE

- **Phase 1 (League Fixtures):** 2-3 days
- **Phase 2 (League Standings):** 2-3 days
- **Phase 3 (Cup Draws):** 2-3 days
- **Phase 4 (Cup UI):** 2-3 days
- **Phase 5 (League UI):** 2-3 days

**Total:** 10-15 days for complete implementation

---

## 10. DEPENDENCIES

- ✅ Match simulation (done)
- ✅ Club statistics (done)
- ✅ Match type constants (done)
- ❌ League fixture generation (needed)
- ❌ League standings system (needed)
- ❌ Cup draw generation (needed)
- ❌ UI screens (needed)

---

## 11. NEXT STEPS

1. **Immediate:** Fix points system (✅ DONE)
2. **Phase 1:** Implement league fixture generation
3. **Phase 2:** Implement league standings
4. **Phase 3:** Implement cup draws
5. **Phase 4-5:** Create UI screens

---

*End of Analysis*

