# Testing Guide: Phase 1 - League Fixture Generation

## What to Test

Verify that league fixtures are automatically generated when leagues are created and that matches are properly scheduled.

---

## Test Steps

### 1. Start a New Game
- Start a new game and select a season (e.g., 1888-89 English Football League)
- The league should be created via the LeagueCreationScript

### 2. Check Console Logs
After starting the game, check the console/logs for:
```
AuthorityManager: checkCompetitionsSchedule()
AuthorityManager: Generating fixtures for league: [League Name]
LeagueFixtureGenerator: Generating fixtures for league: [League Name] with [N] clubs
LeagueFixtureGenerator: Generated [X] league fixtures
AuthorityManager: Generated [X] fixtures for [League Name]
```

**Expected:** You should see fixture generation messages in the logs.

### 3. Advance Time (Continue Game)
- Click "Continue Game" to advance time by 1 day
- The `AuthorityManager.checkCompetitionsSchedule()` is called daily
- Check logs again to see if fixtures are generated

### 4. Verify Fixtures Are Scheduled
**Option A: Check via Squad/Schedule Screen (if available)**
- Navigate to your club's schedule/fixtures screen
- You should see league matches scheduled

**Option B: Check via Debug/Console**
- Check if your club has matches in `scheduledMatches` list
- Verify match dates are in the future
- Verify matches have `matchType = LEAGUE_MATCH` (value 2)

### 5. Verify Match Properties
For each scheduled league match, verify:
- ✅ `matchType` = `Match.LEAGUE_MATCH` (2)
- ✅ `isAccepted` = `true` (league matches are auto-accepted)
- ✅ `isProposed` = `false` (not a proposal)
- ✅ `matchDateTime` is set to a future date
- ✅ `homeClubId` and `awayClubId` are set correctly
- ✅ Match is in both home and away clubs' `scheduledMatches` lists

### 6. Verify Fixture Distribution
- Check that matches are scheduled across the season (September to May)
- Matches should be scheduled weekly (approximately every 7 days)
- Match times should be Saturdays at 3 PM (15:00)

### 7. Verify No Duplicates
- Advance time multiple days
- Check that fixtures are NOT regenerated if they already exist
- You should see: "League [Name] already has fixtures scheduled" in logs

### 8. Verify All Clubs Have Matches
- For a league with N clubs, each club should have:
  - (N-1) home matches
  - (N-1) away matches
  - Total: 2*(N-1) matches per club
- For a 12-club league: each club should have 22 matches

---

## Expected Results

### ✅ Success Criteria:
1. **Fixtures Generated:** League fixtures are created automatically
2. **Correct Match Type:** All matches have `matchType = LEAGUE_MATCH`
3. **Proper Scheduling:** Matches are distributed across season dates
4. **No Duplicates:** Fixtures are not regenerated if they already exist
5. **Complete Fixtures:** All clubs play each other home and away
6. **Matches in Lists:** Matches appear in clubs' `scheduledMatches` lists

### ❌ Failure Indicators:
- No fixtures generated after league creation
- Matches have wrong `matchType` (should be 2, not 1)
- Duplicate fixtures generated on each day
- Matches not scheduled (null dates)
- Clubs missing matches
- Matches not in clubs' scheduled lists

---

## Debugging Tips

### If Fixtures Are Not Generated:

1. **Check Authority:**
   - Verify `mainAuthority` exists in SaveGame
   - Verify `mainAuthority.getLeagues()` is not null/empty
   - Check if league has clubs: `league.getLeagueClubs()`

2. **Check Logs:**
   - Look for errors in `AuthorityManager.checkCompetitionsSchedule()`
   - Check `LeagueFixtureGenerator` logs for issues

3. **Manual Trigger:**
   - You can manually call `fixtureGenerator.generateLeagueFixtures()` in debug mode

### If Matches Have Wrong Type:

- Check that `match.setMatchType(Match.LEAGUE_MATCH)` is called
- Verify `Match.LEAGUE_MATCH` constant = 2

### If Duplicates Are Created:

- Check `hasFixturesScheduled()` method
- Verify it correctly detects existing league matches
- Check that `checkCompetitionsSchedule()` doesn't run multiple times unnecessarily

---

## Test Data

### For 12-Club League:
- **Total Matches:** 12 * 11 = 132 matches
- **Matches per Club:** 22 matches (11 home + 11 away)
- **Matchdays:** ~22 matchdays (6 matches per matchday)

### Expected Match Dates:
- First matchday: Around season start date (September)
- Last matchday: Around season end date (May)
- Interval: ~7 days between matchdays

---

## Next Steps After Testing

If Phase 1 tests pass:
- ✅ Proceed to Phase 2: League Standings System
- ✅ Test that league matches award points correctly
- ✅ Verify club statistics update after league matches

If Phase 1 tests fail:
- ❌ Report specific issues
- ❌ Check logs for error messages
- ❌ Verify league structure is correct

---

*End of Testing Guide*

