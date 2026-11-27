# Debug Guide: Draw Message Not Appearing

## Issue
- No draw messages appearing in inbox
- Cannot see the draw
- Can progress time indefinitely (no blocking)
- Matches are never generated

## Root Causes to Check

### 1. League Creation Script Not Executing
**Check Console Logs For:**
```
CHECKING GAME SCRIPTS! X
EXECUTING BASIC SCRIPT: The English Football League Creation
LEAGUE CREATION SUCCESSFUL!
```

**If Missing:**
- Script executes on **17 April 1888 at 19:30**
- Make sure you advance time to at least **17 April 1888**
- Check if script is loaded: `gameInstance.getCurrentGame().getGameScripts().size()`

### 2. Fixtures Not Being Generated
**Check Console Logs For:**
```
Generated X fixtures for league English Football League
Successfully generated X fixtures for league English Football League
```

**If Missing:**
- Check if league has clubs: `league.getLeagueClubs().size()`
- Check if clubs have required data (players, stadium)
- Check `LeagueFixtureGenerator` logs for errors

### 3. Draw Message Not Being Created
**Check Console Logs For:**
```
MANDATORY DRAW MESSAGE CREATED!
Draw message scheduled for: [date]
Draw message ID: [id]
Is Mandatory: true
Message Type: LEAGUE_DRAW
```

**If Missing:**
- Check if `fixtures != null && fixtures.size() > 0`
- Check if `createLeagueDrawMessage()` returns null
- Check MessageManager logs

### 4. Draw Message Not Being Delivered
**Check Console Logs For:**
```
MessageManager: Delivered scheduled message ID [id]: League Fixture Draw - [league name]
MANDATORY MESSAGE DELIVERED!
This message blocks time advancement until viewed!
```

**If Missing:**
- Check if message is in `scheduledMessages` list
- Check if `deliverScheduledMessages()` is being called
- Check if scheduled date matches current date

### 5. Mandatory Check Not Working
**Check Console Logs For:**
```
FuttoboruGameEngine: Checking for mandatory messages. Total messages: X
FuttoboruGameEngine: Found mandatory message - Type: LEAGUE_DRAW, IsMandatory: true, IsRead: false
FuttoboruGameEngine: BLOCKING TIME ADVANCEMENT - Mandatory draw message: [title]
```

**If Missing:**
- Check if message has `isMandatory = true`
- Check if message is unread (`isRead = false`)
- Check if message is not deleted (`isDeleted = false`)

## Testing Steps

1. **Start New Game**
   - Select season starting before 17 April 1888
   - Select a club to manage

2. **Advance Time to 17 April 1888**
   - Click "Continue Game" repeatedly
   - Watch console for league creation messages

3. **Check League Creation**
   - Navigate to Competitions screen
   - Should see "English Football League"
   - Click on it to see league details

4. **Advance Time to 18 April 1888**
   - Draw message should be delivered
   - Check inbox for "League Fixture Draw" message
   - Button should change to "INBOX -> DRAW"

5. **Check Console Logs**
   - Look for all the debug messages listed above
   - If any are missing, that's where the issue is

## Quick Fixes

### If League Not Created:
- Make sure game date reaches 17 April 1888
- Check if script is loaded in SaveGame

### If Fixtures Not Generated:
- Check if clubs have players and stadiums
- Check LeagueFixtureGenerator error logs

### If Draw Message Not Created:
- Check if fixtures list is empty
- Check if createLeagueDrawMessage() is called
- Check MessageManager initialization

### If Draw Message Not Delivered:
- Check if message is in scheduledMessages
- Check if deliverScheduledMessages() is called
- Check if scheduled date matches current date

### If Time Not Blocked:
- Check if message has isMandatory = true
- Check if message is unread
- Check if getNextGameAction() is checking mandatory messages

## Expected Console Output (Success Case)

```
CHECKING GAME SCRIPTS! 1
EXECUTING BASIC SCRIPT: The English Football League Creation
LEAGUE CREATION SUCCESSFUL!
League Name: English Football League
League Clubs: 12
Generated 132 fixtures for league English Football League
MANDATORY DRAW MESSAGE CREATED!
Draw message scheduled for: 1888-04-18T19:30
Draw message ID: [id]
Is Mandatory: true
Message Type: LEAGUE_DRAW
MessageManager: Scheduled message: League Fixture Draw - English Football League for 1888-04-18T19:30
[Advance time to 18 April 1888]
MessageManager: Delivered scheduled message ID [id]: League Fixture Draw - English Football League
MANDATORY MESSAGE DELIVERED!
FuttoboruGameEngine: Checking for mandatory messages. Total messages: X
FuttoboruGameEngine: Found mandatory message - Type: LEAGUE_DRAW, IsMandatory: true
FuttoboruGameEngine: BLOCKING TIME ADVANCEMENT - Mandatory draw message: League Fixture Draw - English Football League
```

