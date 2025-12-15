# Schedule & Fixtures System - Current Status & Next Steps

**Date:** 2025-12-15  
**Branch:** feature/match-simulation-v1  
**Status:** Inbox system complete, Schedule/Fixtures system needs work

---

## EXECUTIVE SUMMARY

✅ **COMPLETE:**
- Inbox system (messages, filters, read/unread, detail view)
- League creation with fixture generation
- League detail screen (shows upcoming fixtures)
- Message delivery system

⚠️ **PARTIALLY WORKING:**
- Fixture generation (works, but fixtures may not be visible in schedule screen)
- Schedule screen (shows matches, but only for current club)

❌ **MISSING/BROKEN:**
- League draw screen/UI (no visual draw process)
- Fixture release message timing/visibility
- Schedule screen showing ALL league matches (not just player's club)
- Match simulation on scheduled dates
- League standings calculation

---

## 1. CURRENT FIXTURE GENERATION FLOW

### 1.1 When Fixtures Are Generated

**Timeline:**
1. **League Creation:** 17 April 1888 at 19:30 (script execution date)
2. **Fixture Generation:** Immediately when league is created (in `ScriptsManager.createLeague()`)
3. **Fixture Release Message:** Scheduled for 1 day after league creation (18 April 1888)

**Code Location:**
- `ScriptsManager.createLeague()` lines 240-265: Generates fixtures immediately
- `ScriptsManager.createLeague()` lines 357-370: Schedules fixture release message

**Current Behavior:**
- ✅ Fixtures are generated when league is created
- ✅ Fixtures are added to clubs' `scheduledMatches` lists
- ✅ Fixture release message is scheduled for next day
- ❓ Fixtures may not be visible in schedule screen (see section 2.3)

---

### 1.2 Fixture Scheduling Details

**Season Duration:**
- Start: Game start date (typically September 1888)
- End: Start date + 9 months (typically May 1889)
- Match frequency: One match per week (7 days between matchdays)

**Fixture Distribution:**
- First half: Home fixtures (round-robin)
- Second half: Away fixtures (reverse of home fixtures)
- Matches distributed evenly across season

**Match Properties:**
- `matchType = Match.LEAGUE_MATCH` (value 2)
- `isAccepted = true` (auto-accepted)
- `isProposed = false`
- `isPlayed = false` (initially)

---

## 2. SCHEDULE SCREEN STATUS

### 2.1 Current Implementation

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/schedule/FixturesTable.java`

**What It Shows:**
- ✅ Weekly calendar view (Monday-Sunday)
- ✅ Matches for current club (if player has a club)
- ✅ League matches (prioritized over friendlies)
- ⚠️ Only shows matches from `currentClub.getScheduledMatches()`

**What's Missing:**
- ❌ Shows ALL league matches (currently only shows player's club matches)
- ❌ Shows matches even if player is unemployed
- ❌ Filter by competition type (League, Cup, Friendly)
- ❌ Filter by date range
- ❌ Click on match to view details

**Code Issue:**
```java
// Lines 130-180: Only iterates currentClub.getScheduledMatches()
// Should also collect ALL league matches from all clubs in the league
```

---

### 2.2 League Detail Screen Status

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/competitions/LeagueDetailScreenTable.java`

**What It Shows:**
- ✅ League name, country, club count
- ✅ Participating clubs list
- ✅ League standings (if matches played)
- ✅ Upcoming fixtures (next 5 matches)
- ✅ Back button to competitions screen

**What's Missing:**
- ❌ Full fixture list (only shows next 5)
- ❌ Filter fixtures by date range
- ❌ Click on fixture to view details
- ❌ Recent results section (shows "No matches played yet" placeholder)

**Status:** ✅ **WORKING** - Shows upcoming fixtures correctly

---

## 3. MESSAGE SYSTEM STATUS

### 3.1 Fixture Release Message

**When It's Sent:**
- Scheduled for 1 day after league creation (18 April 1888)
- Delivered via `MessageManager.deliverScheduledMessages()`

**Message Content:**
- Title: "League Fixtures Released"
- Category: `LEAGUE`
- Type: `FIXTURE_RELEASE`
- Content: "The complete fixture list for the [League Name] season has been released..."

**Current Status:**
- ✅ Message is created and scheduled
- ✅ Message is delivered on scheduled date
- ✅ Message appears in inbox (if player has a club in the league)

**To Test:**
1. Start new game on 1 January 1888
2. Advance time to 17 April 1888 → League created, fixtures generated
3. Advance time to 18 April 1888 → Fixture release message should appear in inbox

---

## 4. WHAT'S MISSING / NEXT STEPS

### 4.1 League Draw Screen (UI) ❌

**Problem:**
- No visual "draw" process when league is created
- Fixtures are generated automatically, but there's no UI to show the draw
- In Football Manager, there's a draw screen with "Next" buttons to reveal fixtures

**What's Needed:**
1. **Draw Screen UI:**
   - Show league name and participating clubs
   - Display fixture draw process (animated or step-by-step)
   - "Draw All" button or "Next" button to reveal fixtures one by one
   - Show complete fixture list after draw

2. **Draw Timing:**
   - Currently: Fixtures generated immediately on league creation
   - Should be: Draw happens on a specific date (e.g., 1 day after league creation)
   - Draw screen appears when player advances time to draw date

3. **Implementation:**
   - Create `LeagueDrawScreenTable.java`
   - Add draw date to league creation script
   - Show draw screen when draw date is reached
   - After draw, generate fixtures and show fixture list

**Priority:** Medium (nice to have, but not critical for MVP)

---

### 4.2 Schedule Screen - Show ALL League Matches ❌

**Problem:**
- Schedule screen only shows player's club matches
- Should show ALL matches in the league (like Football Manager's fixture list)

**What's Needed:**
1. **Collect All League Matches:**
   ```java
   // In FixturesTable.updateDynamicComponents()
   // Collect all league matches from all clubs in all leagues
   List<Match> allLeagueMatches = new ArrayList<>();
   for (League league : currentGame.getMainAuthority().getLeagues()) {
       for (Club club : league.getLeagueClubs()) {
           for (Match match : club.getScheduledMatches()) {
               if (match.getMatchType() == Match.LEAGUE_MATCH) {
                   allLeagueMatches.add(match);
               }
           }
       }
   }
   ```

2. **Display All Matches:**
   - Show matches from all clubs in the league
   - Filter by date (show matches for selected week)
   - Highlight player's club matches
   - Show match details on click

**Priority:** High (core feature)

---

### 4.3 Match Simulation on Scheduled Dates ❌

**Problem:**
- Matches are scheduled, but not automatically simulated when match date arrives
- No match results are generated

**What's Needed:**
1. **Daily Match Check:**
   - In `FuttoboruGameEngine.continueGame()`, check for matches scheduled for current date
   - Simulate matches that are due
   - Update club statistics (points, goals, etc.)
   - Create match result messages

2. **Match Simulation:**
   - Already exists: `MatchSimulator.simulateMatch()`
   - Need to call it for scheduled league matches
   - Update match result (homeGoals, awayGoals, isPlayed = true)

3. **Result Messages:**
   - Create match result message after simulation
   - Send to both clubs involved
   - Show in inbox

**Priority:** High (core feature)

---

### 4.4 League Standings Calculation ❌

**Problem:**
- League detail screen shows standings, but they're only calculated from club statistics
- Club statistics are updated after match simulation, but matches aren't being simulated yet

**What's Needed:**
1. **Standings Calculation:**
   - Already implemented in `LeagueDetailScreenTable` (lines 132-157)
   - Sorts clubs by points, goal difference, goals scored
   - Will work once matches are simulated and club stats are updated

2. **Club Statistics Update:**
   - After match simulation, update:
     - `matchesPlayed++`
     - `matchesWon++` or `matchesDrawn++` or `matchesLost++`
     - `goalsScored += homeGoals` (or awayGoals)
     - `goalsConceded += awayGoals` (or homeGoals)
     - `points += 3` (win) or `1` (draw) or `0` (loss)
     - `goalDifference = goalsScored - goalsConceded`

**Priority:** Medium (depends on match simulation)

---

## 5. TESTING GUIDE

### 5.1 To See Fixture Release Message

**Steps:**
1. Start new game (select season starting 1 January 1888 or earlier)
2. Select a club to manage (important: must have a club)
3. Advance time to 17 April 1888
   - League should be created
   - Fixtures should be generated
   - Check console logs for: "Generated X fixtures for league..."
4. Advance time to 18 April 1888
   - Fixture release message should appear in inbox
   - Message title: "League Fixtures Released"

**Expected Result:**
- ✅ Inbox shows "League Fixtures Released" message
- ✅ Message is in LEAGUE category
- ✅ Message content mentions league name and fixture release

---

### 5.2 To See Fixtures in Schedule Screen

**Steps:**
1. After league is created (17 April 1888)
2. Navigate to Schedule screen
3. Check if matches appear in calendar view

**Current Behavior:**
- ⚠️ Only shows matches for player's club
- ⚠️ Matches may not appear if club is not in league
- ⚠️ Matches are scheduled for future dates (September onwards)

**Expected Result:**
- ✅ Schedule shows league matches for player's club
- ✅ Matches are visible in calendar view
- ✅ Match dates are in the future (season start date)

---

### 5.3 To See Fixtures in League Detail Screen

**Steps:**
1. Navigate to Competitions screen
2. Click on league name
3. Scroll to "Upcoming Fixtures" section

**Expected Result:**
- ✅ Shows next 5 upcoming fixtures
- ✅ Fixtures show date, home club, away club
- ✅ Fixtures are sorted by date (earliest first)

---

## 6. RECOMMENDED NEXT STEPS (Priority Order)

### Phase 1: Fix Schedule Screen (High Priority)
1. **Modify `FixturesTable` to show ALL league matches**
   - Collect matches from all clubs in all leagues
   - Display in calendar view
   - Highlight player's club matches

2. **Add match filtering**
   - Filter by competition type (League, Cup, Friendly)
   - Filter by date range
   - Show/hide specific leagues

**Estimated Time:** 2-3 hours

---

### Phase 2: Match Simulation (High Priority)
1. **Add daily match check in `FuttoboruGameEngine`**
   - Check for matches scheduled for current date
   - Simulate matches that are due
   - Update club statistics

2. **Create match result messages**
   - Send to both clubs
   - Show in inbox

**Estimated Time:** 3-4 hours

---

### Phase 3: League Draw Screen (Medium Priority)
1. **Create `LeagueDrawScreenTable`**
   - Show draw process
   - Display fixture list
   - Add "Draw All" / "Next" buttons

2. **Schedule draw date**
   - Add draw date to league creation script
   - Show draw screen when date is reached

**Estimated Time:** 4-5 hours

---

### Phase 4: Enhance League Detail Screen (Low Priority)
1. **Add full fixture list view**
   - Show all fixtures, not just next 5
   - Add pagination or scroll
   - Filter by date range

2. **Add recent results section**
   - Show last 5-10 match results
   - Display scores, dates, opponents

**Estimated Time:** 2-3 hours

---

## 7. QUICK ANSWERS TO USER QUESTIONS

### Q: Which date should the league be drawn?
**A:** Currently, fixtures are generated immediately when the league is created (17 April 1888). There's no separate "draw" date. If you want a draw screen, it should happen 1 day after league creation (18 April 1888), same as the fixture release message.

### Q: How far should I advance time to see match schedule messages?
**A:** 
- **League creation:** Advance to 17 April 1888
- **Fixture release message:** Advance to 18 April 1888 (1 day after league creation)
- **First match:** Matches are scheduled starting from season start date (typically September 1888), so advance to September to see matches in schedule

### Q: Can I see the schedule on the league detail screen?
**A:** Yes! The league detail screen shows the next 5 upcoming fixtures. Navigate to Competitions → Click league name → Scroll to "Upcoming Fixtures" section.

### Q: Can I see my own schedule on my team schedule screen?
**A:** Yes, but currently it only shows matches for your club. The schedule screen should show all league matches (this is a known issue to fix).

---

## 8. SUMMARY

**What Works:**
- ✅ Inbox system (complete)
- ✅ League creation with fixture generation
- ✅ League detail screen (shows upcoming fixtures)
- ✅ Message delivery system

**What Needs Work:**
- ⚠️ Schedule screen (only shows player's club matches, should show all league matches)
- ❌ Match simulation (matches scheduled but not simulated)
- ❌ League draw screen (no UI for draw process)
- ⚠️ League standings (will work once matches are simulated)

**Recommended Next Step:**
Start with **Phase 1: Fix Schedule Screen** to show all league matches, then move to **Phase 2: Match Simulation** to make the matches actually play.

