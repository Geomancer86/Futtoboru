# Step-by-Step Debug Guide: League Creation & Draw Message

## Overview
This guide will help you identify exactly where the process is failing by checking console logs at each step.

---

## STEP 1: Script Execution Check

**What to Look For:**
```
========================================
STEP 1: CHECKING GAME SCRIPTS
Current game date: [DATE]
Total scripts: [NUMBER]
========================================
Checking script 1: The English Football League Creation
  - Execution time: 1888-04-17T19:30
  - Is executed: false
  - Script type: 1000
  - Should execute: true/false
```

**If Missing:**
- Scripts not loaded → Check if game was started correctly
- Script already executed → League was already created
- Should execute: false → Need to advance time to 17 April 1888

**Expected Output:**
```
========================================
EXECUTING BASIC SCRIPT: The English Football League Creation
========================================
```

---

## STEP 2: League Creation Check

**What to Look For:**
```
========================================
EXECUTING LEAGUE CREATION SCRIPT!
========================================
Creating league: English Football League
League country: England
Found 12 club IDs in script
Total clubs added to league: 12 / 12
========================================
LEAGUE CREATION SUCCESSFUL!
League Name: English Football League
League Clubs: 12
Total Leagues in SaveGame: 1
========================================
```

**If Missing:**
- "ERROR: currentGame is null" → SaveGame not initialized
- "ERROR: mainAuthority is null" → Authority not created
- "ERROR: No club IDs found" → Script data missing
- "ERROR: Club ID X not found" → Clubs not loaded

**Action:** Check if league appears in Competitions screen

---

## STEP 3: Fixture Generation Check

**What to Look For:**
```
========================================
STEP 2: GENERATING FIXTURES
========================================
Season start date: [DATE]
Season end date: [DATE]
Creating LeagueFixtureGenerator...
Calling generateLeagueFixtures()...
========================================
LeagueFixtureGenerator.generateLeagueFixtures() CALLED
League: English Football League
========================================
League has 12 clubs
[Club validation messages]
Generated X fixtures for league English Football League
========================================
FIXTURE GENERATION RESULT:
Generated [NUMBER] fixtures for league English Football League
========================================
```

**Critical Checks:**
1. **"League has X clubs"** → Should be 12 (or number of clubs in league)
2. **"Generated X fixtures"** → Should be > 0 (for 12 clubs, expect ~132 fixtures)
3. **"Total match references in clubs' scheduledMatches"** → Should be fixtures.size() * 2

**If Fixtures = 0:**
- Check "Skipping club X - missing required data" messages
- Clubs might not have players or stadiums
- Check `isClubReady()` validation

**If Matches Not Added to Clubs:**
- Check "ERROR: Home club not found" or "Away club not found" messages
- Clubs might not be in SaveGame

---

## STEP 4: Draw Message Creation Check

**What to Look For:**
```
========================================
STEP 3: CREATING MESSAGES
========================================
STEP 3.1: Creating draw message...
Fixtures status: NOT NULL, Size: [NUMBER]
Calling createLeagueDrawMessage()...
Draw message created successfully!
Setting scheduled date to: 1888-04-18T19:30
Scheduling message...
Total scheduled messages after draw message: [NUMBER]
========================================
MANDATORY DRAW MESSAGE CREATED!
Draw message scheduled for: 1888-04-18T19:30
Draw message ID: [ID]
Is Mandatory: true
Message Type: LEAGUE_DRAW
Action Screen: 10008
Action Data (League ID): [LEAGUE_ID]
========================================
```

**Critical Checks:**
1. **"Fixtures status: NOT NULL, Size: X"** → Should be > 0
2. **"Draw message created successfully!"** → Should appear
3. **"Is Mandatory: true"** → CRITICAL - must be true
4. **"Total scheduled messages"** → Should increase by 1

**If Missing:**
- "ERROR: createLeagueDrawMessage returned null!" → MessageManager issue
- "WARNING: No fixtures generated" → Step 2 failed
- "Fixtures status: NULL" → Fixture generation returned null

---

## STEP 5: Message Delivery Check

**What to Look For (when advancing to 18 April 1888):**
```
STEP 4: Delivering scheduled messages for date: 1888-04-18T19:30
Scheduled messages before delivery: 1
MessageManager: Delivered scheduled message ID [ID]: League Fixture Draw - English Football League
MANDATORY MESSAGE DELIVERED!
This message blocks time advancement until viewed!
Scheduled messages after delivery: 0
Total messages in inbox: [NUMBER]
```

**Critical Checks:**
1. **"Scheduled messages before delivery: X"** → Should be > 0
2. **"MANDATORY MESSAGE DELIVERED!"** → Should appear
3. **"Scheduled messages after delivery: 0"** → Message moved from scheduled to inbox
4. **"Total messages in inbox"** → Should increase

**If Missing:**
- Scheduled messages = 0 → Message wasn't scheduled (Step 4 failed)
- Message not delivered → Check scheduled date vs current date
- Message delivered but not mandatory → Check isMandatory flag

---

## STEP 6: Mandatory Blocking Check

**What to Look For:**
```
FuttoboruGameEngine: Checking for mandatory messages. Total messages: [NUMBER]
FuttoboruGameEngine: Found mandatory message - Type: LEAGUE_DRAW, IsMandatory: true, IsRead: false
FuttoboruGameEngine: BLOCKING TIME ADVANCEMENT - Mandatory draw message: League Fixture Draw - English Football League
```

**Critical Checks:**
1. **"Checking for mandatory messages"** → Should run every time getNextGameAction() is called
2. **"Found mandatory message"** → Should appear if message exists
3. **"BLOCKING TIME ADVANCEMENT"** → Should appear if message is unread and mandatory

**If Missing:**
- "Total messages: 0" → Message not delivered (Step 5 failed)
- No "Found mandatory message" → Message doesn't have isMandatory=true or isRead=true
- No "BLOCKING" → Message might be read or not mandatory

---

## Quick Diagnostic Checklist

Run through these checks in order:

- [ ] **Step 1:** Script execution logs appear when advancing time
- [ ] **Step 2:** League creation logs show "LEAGUE CREATION SUCCESSFUL!"
- [ ] **Step 3:** Fixture generation shows "Generated X fixtures" where X > 0
- [ ] **Step 4:** Draw message creation shows "MANDATORY DRAW MESSAGE CREATED!"
- [ ] **Step 5:** Message delivery shows "MANDATORY MESSAGE DELIVERED!" on 18 April
- [ ] **Step 6:** Mandatory blocking shows "BLOCKING TIME ADVANCEMENT"

**If any step fails, that's where the issue is!**

---

## Common Issues & Fixes

### Issue: Script Not Executing
**Symptom:** No "EXECUTING BASIC SCRIPT" message
**Fix:** Advance time to at least 17 April 1888

### Issue: League Not Created
**Symptom:** "ERROR: currentGame is null" or "ERROR: mainAuthority is null"
**Fix:** Check SaveGame initialization in new game flow

### Issue: Fixtures = 0
**Symptom:** "Generated 0 fixtures"
**Fix:** Check club data (players, stadiums) - clubs might be missing required data

### Issue: Draw Message Not Created
**Symptom:** "ERROR: createLeagueDrawMessage returned null!"
**Fix:** Check MessageManager initialization

### Issue: Message Not Delivered
**Symptom:** Scheduled messages > 0 but never delivered
**Fix:** Check if scheduled date matches current date when advancing

### Issue: Time Not Blocked
**Symptom:** Can advance time even with unread mandatory message
**Fix:** Check if message has isMandatory=true and isRead=false

---

## Next Steps

1. **Build and run the game**
2. **Start a new game** (select season before 17 April 1888)
3. **Advance time to 17 April 1888** and watch console
4. **Check each step** in the console output
5. **Report which step fails** and we'll fix it!

