# Implementation Plan: Season Completion & Cup Draws

**Date:** 2025-01-XX  
**Branch:** `feature/season-completion-cup-draws`  
**Status:** 🔴 IN PROGRESS  
**Priority:** CRITICAL

---

## Executive Summary

This document provides a detailed implementation plan for:
1. **Season Completion Detection & Champion Declaration**
2. **Next Season Generation**
3. **Cup Draw Integration & Round Progression**
4. **Cup Replay Handling**
5. **Cup Completion Detection**

---

## 1. DATA STRUCTURE ANALYSIS

### Current State

#### League Structure
- `League` has `leagueClubs` (List<Club>)
- `League` has `rules` (CompetitionRules)
- Leagues accessed via `Authority.getLeagues()`
- `LeagueStandingsManager` has `isLeagueComplete()` and `getChampion()` ✅

#### Competition Structure
- `Competition` has `competitionType` (CUP/LEAGUE)
- `Competition` has `editions` (List<CompetitionEdition>)
- `CompetitionEdition` has:
  - `participantClubsIds` (List<Long>)
  - `championsId` (Long) ✅
  - `runnersUpId` (Long) ✅
  - `startDate`, `endDate`
- Cups accessed via `SaveGame.getAllCups()`

#### Match Structure
- `Match` has `matchType` (FRIENDLY_MATCH, LEAGUE_MATCH, CUP_MATCH)
- `Match` has `homeClubId`, `awayClubId`
- `Match` has `matchDateTime`
- `Match` has `isPlayed` (Boolean)
- `Match` has `homeGoals`, `awayGoals`
- **Missing:** `competitionId`, `competitionEditionId`, `round` fields

#### Club Structure
- `Club` has `scheduledMatches` (List<Match>)
- `Club` has `playedMatches` (List<Match>)
- `Club` has statistics (points, goals, etc.)

---

## 2. SEASON COMPLETION IMPLEMENTATION

### Phase 1: Add Match Competition Tracking (REQUIRED)

**Problem:** Matches don't track which competition/edition they belong to.

**Solution:** Add fields to `Match.java`:
```java
private Long competitionId;        // Competition this match belongs to
private Long competitionEditionId; // Edition this match belongs to
private Integer round;            // Round number (for cups, null for leagues)
```

**Files to Modify:**
- `Match.java` - Add fields and getters/setters

### Phase 2: Season Completion Detection

**Location:** `FuttoboruGameEngine.continueGame()` after `simulateMatchesForDate()`

**Implementation:**
```java
// After simulating matches for current date
checkSeasonCompletion();
```

**Method:** `FuttoboruGameEngine.checkSeasonCompletion()`
```java
private void checkSeasonCompletion() {
    if (gameInstance == null || gameInstance.getCurrentGame() == null) {
        return;
    }
    
    Authority mainAuthority = gameInstance.getCurrentGame().getMainAuthority();
    if (mainAuthority == null || mainAuthority.getLeagues() == null) {
        return;
    }
    
    LeagueStandingsManager standingsManager = new LeagueStandingsManager();
    
    for (League league : mainAuthority.getLeagues()) {
        if (standingsManager.isLeagueComplete(league)) {
            // Check if we already processed this completion
            if (!isSeasonAlreadyCompleted(league)) {
                completeSeason(league, standingsManager);
            }
        }
    }
}

private boolean isSeasonAlreadyCompleted(League league) {
    // Check if league already has a completed season marker
    // TODO: Add season completion tracking to League or CompetitionEdition
    return false; // For now, always process
}

private void completeSeason(League league, LeagueStandingsManager standingsManager) {
    Club champion = standingsManager.getChampion(league);
    if (champion == null) {
        Gdx.app.error("FuttoboruGameEngine", "League complete but no champion found");
        return;
    }
    
    // Store champion in league history
    // TODO: Add pastChampions list to League
    
    // Create season completion message
    MessageManager messageManager = gameInstance.getMessageManager();
    Message completionMessage = messageManager.createSeasonCompletionMessage(league, champion);
    gameInstance.getCurrentGame().addMessage(completionMessage);
    
    // Generate next season
    generateNextSeason(league);
}
```

**Files to Modify:**
- `FuttoboruGameEngine.java` - Add season completion detection
- `MessageManager.java` - Add `createSeasonCompletionMessage()`

### Phase 3: Champion Declaration

**Implementation:** Already in `completeSeason()` method above.

**Message Creation:**
```java
public Message createSeasonCompletionMessage(League league, Club champion) {
    Message message = new Message();
    message.setCategory(MessageCategory.LEAGUE);
    message.setMessageType("SEASON_COMPLETE");
    message.setPriority(MessagePriority.HIGH);
    message.setTitle(league.getName() + " Season Complete");
    
    StringBuilder content = new StringBuilder();
    content.append("The ").append(league.getName()).append(" season has concluded.\n\n");
    content.append("Champions: ").append(champion.getName()).append("\n\n");
    
    // Add standings summary (top 3)
    List<Club> standings = leagueStandingsManager.calculateStandings(league);
    if (standings.size() >= 2) {
        content.append("Runner-up: ").append(standings.get(1).getName()).append("\n");
    }
    if (standings.size() >= 3) {
        content.append("Third Place: ").append(standings.get(2).getName()).append("\n");
    }
    
    message.setPlainTextMessage(content.toString());
    message.setIsRead(false);
    message.setIsDeleted(false);
    
    return message;
}
```

### Phase 4: Next Season Generation

**Implementation:** `FuttoboruGameEngine.generateNextSeason(League league)`

```java
private void generateNextSeason(League league) {
    LocalDateTime currentDate = gameInstance.getCurrentGame().getGameDate();
    
    // Calculate next season dates (3 month break, then 9 month season)
    LocalDateTime nextSeasonStart = currentDate.plusMonths(3);
    LocalDateTime nextSeasonEnd = nextSeasonStart.plusMonths(9);
    
    // Clear old fixtures (optional: archive them first)
    clearLeagueFixtures(league);
    
    // Generate new fixtures
    LeagueFixtureGenerator fixtureGenerator = new LeagueFixtureGenerator(gameInstance);
    List<Match> newFixtures = fixtureGenerator.generateLeagueFixtures(
        league, nextSeasonStart, nextSeasonEnd
    );
    
    // Set competition tracking on new matches
    // TODO: Get CompetitionEdition for next season
    for (Match match : newFixtures) {
        match.setCompetitionId(getCompetitionIdForLeague(league));
        match.setCompetitionEditionId(getNextEditionId(league));
        match.setRound(null); // Leagues don't have rounds
    }
    
    // Create fixture release message
    MessageManager messageManager = gameInstance.getMessageManager();
    Message fixtureMessage = messageManager.createFixtureReleaseMessage(league, nextSeasonStart);
    gameInstance.getCurrentGame().addMessage(fixtureMessage);
    
    Gdx.app.log("FuttoboruGameEngine", "Generated " + newFixtures.size() + " fixtures for next season of " + league.getName());
}

private void clearLeagueFixtures(League league) {
    // Remove scheduled matches for all clubs in league
    for (Club club : league.getLeagueClubs()) {
        if (club == null) continue;
        
        List<Match> toRemove = new ArrayList<>();
        for (Match match : club.getScheduledMatches()) {
            if (match != null && match.getCompetitionId() != null && 
                match.getCompetitionId().equals(getCompetitionIdForLeague(league))) {
                toRemove.add(match);
            }
        }
        club.getScheduledMatches().removeAll(toRemove);
    }
}
```

**Files to Modify:**
- `FuttoboruGameEngine.java` - Add `generateNextSeason()`
- `MessageManager.java` - Add `createFixtureReleaseMessage()`

---

## 3. CUP DRAWS IMPLEMENTATION

### Phase 1: Cup Draw Integration

**Location:** `AuthorityManager.checkCompetitionsSchedule()`

**Implementation:**
```java
public void checkCompetitionsSchedule() {
    // ... existing league check ...
    
    // Check cups and generate draws if needed
    checkAndScheduleCupDraws();
}

private void checkAndScheduleCupDraws() {
    if (currentGame == null) {
        return;
    }
    
    List<Competition> cups = currentGame.getAllCups();
    if (cups == null || cups.isEmpty()) {
        return;
    }
    
    CompetitionScheduler scheduler = new CompetitionScheduler();
    LocalDateTime currentDate = currentGame.getGameDate();
    
    for (Competition cup : cups) {
        if (cup == null || !Competition.COMPETITION_CUP.equals(cup.getCompetitionType())) {
            continue;
        }
        
        // Get current edition (or create one)
        CompetitionEdition edition = getCurrentCupEdition(cup, currentDate);
        if (edition == null) {
            edition = createNewCupEdition(cup, currentDate);
        }
        
        // Check if cup needs initial draw
        if (needsCupDraw(edition)) {
            generateCupDraw(cup, edition);
        }
        
        // Check if cup round needs to be advanced
        if (isCupRoundComplete(edition)) {
            advanceCupRound(cup, edition);
        }
    }
}

private CompetitionEdition getCurrentCupEdition(Competition cup, LocalDateTime currentDate) {
    if (cup.getEditions() == null || cup.getEditions().isEmpty()) {
        return null;
    }
    
    // Find edition that covers current date
    for (CompetitionEdition edition : cup.getEditions()) {
        if (edition.getStartDate() != null && edition.getEndDate() != null) {
            if (!currentDate.isBefore(edition.getStartDate()) && 
                !currentDate.isAfter(edition.getEndDate())) {
                return edition;
            }
        }
    }
    
    return null;
}

private CompetitionEdition createNewCupEdition(Competition cup, LocalDateTime currentDate) {
    CompetitionEdition edition = new CompetitionEdition();
    edition.setId(System.currentTimeMillis()); // TODO: Better ID generation
    edition.setName(cup.getName() + " " + currentDate.getYear());
    edition.setStartDate(currentDate);
    edition.setEndDate(currentDate.plusMonths(6)); // Cup typically runs 6 months
    
    // Get participant clubs (all clubs or specific list)
    List<Long> participantIds = getCupParticipantClubs(cup);
    edition.setParticipantClubsIds(participantIds);
    edition.setParticipantClubs(participantIds.size());
    
    if (cup.getEditions() == null) {
        cup.setEditions(new ArrayList<>());
    }
    cup.getEditions().add(edition);
    
    return edition;
}

private List<Long> getCupParticipantClubs(Competition cup) {
    // For now, use all clubs from main authority
    // TODO: Use cup-specific participant list
    List<Long> clubIds = new ArrayList<>();
    if (currentGame.getMainAuthority() != null && 
        currentGame.getMainAuthority().getLeagues() != null) {
        for (League league : currentGame.getMainAuthority().getLeagues()) {
            if (league.getLeagueClubs() != null) {
                for (Club club : league.getLeagueClubs()) {
                    if (club != null && club.getId() != null) {
                        clubIds.add(club.getId());
                    }
                }
            }
        }
    }
    return clubIds;
}

private boolean needsCupDraw(CompetitionEdition edition) {
    // Check if any matches exist for this edition
    if (edition.getParticipantClubsIds() == null || edition.getParticipantClubsIds().isEmpty()) {
        return false;
    }
    
    // Check if matches are scheduled
    for (Club club : currentGame.getAllClubs()) {
        if (club == null || club.getScheduledMatches() == null) {
            continue;
        }
        for (Match match : club.getScheduledMatches()) {
            if (match != null && match.getCompetitionEditionId() != null &&
                match.getCompetitionEditionId().equals(edition.getId())) {
                return false; // Matches already scheduled
            }
        }
    }
    
    return true; // No matches found, need draw
}

private void generateCupDraw(Competition cup, CompetitionEdition edition) {
    CompetitionScheduler scheduler = new CompetitionScheduler();
    
    // Generate draw
    List<Match> drawMatches = scheduler.competitionDraw(cup, edition.getParticipantClubsIds());
    
    if (drawMatches == null || drawMatches.isEmpty()) {
        Gdx.app.error("AuthorityManager", "Cup draw generated no matches");
        return;
    }
    
    // Schedule matches with dates
    LocalDateTime currentDate = currentGame.getGameDate();
    LocalDateTime matchDate = currentDate.plusWeeks(2); // First round in 2 weeks
    
    int roundNumber = 1; // First round
    for (Match match : drawMatches) {
        match.setMatchType(Match.CUP_MATCH);
        match.setCompetitionId(cup.getId());
        match.setCompetitionEditionId(edition.getId());
        match.setRound(roundNumber);
        match.setMatchDateTime(matchDate);
        match.setIsProposed(false);
        match.setIsAccepted(true);
        match.setIsPlayed(false);
        
        // Add to clubs' scheduled matches
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub != null) {
            homeClub.getScheduledMatches().add(match);
        }
        if (awayClub != null) {
            awayClub.getScheduledMatches().add(match);
        }
        
        // Create cup draw message
        MessageManager messageManager = gameInstance.getMessageManager();
        Message drawMessage = messageManager.createCupDrawResultMessage(
            cup, homeClub, awayClub, matchDate, "First Round"
        );
        currentGame.addMessage(drawMessage);
    }
    
    Gdx.app.log("AuthorityManager", "Generated cup draw: " + drawMatches.size() + " matches for " + cup.getName());
}
```

**Files to Modify:**
- `AuthorityManager.java` - Add cup draw methods
- `Match.java` - Add competition tracking fields

### Phase 2: Cup Round Progression

**Implementation:**
```java
private boolean isCupRoundComplete(CompetitionEdition edition) {
    // Get all matches for this edition
    List<Match> editionMatches = getMatchesForEdition(edition);
    
    if (editionMatches.isEmpty()) {
        return false;
    }
    
    // Get current round number
    Integer currentRound = getCurrentRound(editionMatches);
    if (currentRound == null) {
        return false; // No round started yet
    }
    
    // Get all matches for current round
    List<Match> roundMatches = new ArrayList<>();
    for (Match match : editionMatches) {
        if (match.getRound() != null && match.getRound().equals(currentRound)) {
            roundMatches.add(match);
        }
    }
    
    // Check if all round matches are played
    for (Match match : roundMatches) {
        if (match.getIsPlayed() == null || !match.getIsPlayed()) {
            return false; // Round not complete
        }
    }
    
    return true; // All matches played
}

private void advanceCupRound(Competition cup, CompetitionEdition edition) {
    // Get winners from current round
    List<Club> winners = getRoundWinners(edition);
    
    if (winners.isEmpty()) {
        Gdx.app.error("AuthorityManager", "No winners found for cup round");
        return;
    }
    
    if (winners.size() == 1) {
        // Cup complete!
        completeCup(cup, edition, winners.get(0));
        return;
    }
    
    // Generate next round draw
    List<Long> winnerIds = new ArrayList<>();
    for (Club winner : winners) {
        winnerIds.add(winner.getId());
    }
    
    CompetitionScheduler scheduler = new CompetitionScheduler();
    List<Match> nextRoundMatches = scheduler.competitionDraw(cup, winnerIds);
    
    // Get next round number
    Integer nextRound = getNextRoundNumber(edition);
    
    // Schedule next round matches
    LocalDateTime currentDate = currentGame.getGameDate();
    LocalDateTime nextRoundDate = currentDate.plusWeeks(2); // Next round in 2 weeks
    
    for (Match match : nextRoundMatches) {
        match.setMatchType(Match.CUP_MATCH);
        match.setCompetitionId(cup.getId());
        match.setCompetitionEditionId(edition.getId());
        match.setRound(nextRound);
        match.setMatchDateTime(nextRoundDate);
        match.setIsProposed(false);
        match.setIsAccepted(true);
        match.setIsPlayed(false);
        
        // Add to clubs' scheduled matches
        Club homeClub = currentGame.getClubById(match.getHomeClubId());
        Club awayClub = currentGame.getClubById(match.getAwayClubId());
        
        if (homeClub != null) {
            homeClub.getScheduledMatches().add(match);
        }
        if (awayClub != null) {
            awayClub.getScheduledMatches().add(match);
        }
    }
    
    String roundName = getRoundName(nextRound, winners.size());
    Gdx.app.log("AuthorityManager", "Advanced cup to " + roundName + ": " + nextRoundMatches.size() + " matches");
}

private List<Club> getRoundWinners(CompetitionEdition edition) {
    List<Club> winners = new ArrayList<>();
    
    // Get current round
    List<Match> editionMatches = getMatchesForEdition(edition);
    Integer currentRound = getCurrentRound(editionMatches);
    if (currentRound == null) {
        return winners;
    }
    
    // Get all matches for current round
    for (Match match : editionMatches) {
        if (match.getRound() != null && match.getRound().equals(currentRound) &&
            match.getIsPlayed() != null && match.getIsPlayed()) {
            
            // Determine winner
            Club winner = getMatchWinner(match);
            if (winner != null) {
                winners.add(winner);
            }
        }
    }
    
    return winners;
}

private Club getMatchWinner(Match match) {
    if (match.getHomeGoals() == null || match.getAwayGoals() == null) {
        return null;
    }
    
    if (match.getHomeGoals() > match.getAwayGoals()) {
        return currentGame.getClubById(match.getHomeClubId());
    } else if (match.getAwayGoals() > match.getHomeGoals()) {
        return currentGame.getClubById(match.getAwayClubId());
    }
    
    // Draw - need replay (handled separately)
    return null;
}

private void completeCup(Competition cup, CompetitionEdition edition, Club winner) {
    // Store winner
    edition.setChampionsId(winner.getId());
    
    // Find runner-up (loser of final)
    List<Match> finalMatches = getMatchesForRound(edition, getCurrentRound(getMatchesForEdition(edition)));
    if (!finalMatches.isEmpty()) {
        Match finalMatch = finalMatches.get(0);
        Club runnerUp = getMatchLoser(finalMatch, winner);
        if (runnerUp != null) {
            edition.setRunnersUpId(runnerUp.getId());
        }
    }
    
    // Create cup completion message
    MessageManager messageManager = gameInstance.getMessageManager();
    Message completionMessage = messageManager.createCupCompletionMessage(cup, winner);
    currentGame.addMessage(completionMessage);
    
    Gdx.app.log("AuthorityManager", "Cup complete: " + cup.getName() + " won by " + winner.getName());
}
```

**Files to Modify:**
- `AuthorityManager.java` - Add round progression methods

### Phase 3: Cup Replay Handling

**Implementation:**
```java
private void checkCupReplays(Competition cup, CompetitionEdition edition) {
    List<Match> editionMatches = getMatchesForEdition(edition);
    
    for (Match match : editionMatches) {
        if (match.getIsPlayed() != null && match.getIsPlayed() &&
            match.getHomeGoals() != null && match.getAwayGoals() != null &&
            match.getHomeGoals().equals(match.getAwayGoals())) {
            
            // Check if replay already scheduled
            if (!isReplayScheduled(match)) {
                scheduleCupReplay(match);
            }
        }
    }
}

private boolean isReplayScheduled(Match originalMatch) {
    // Check if a replay match exists for this original match
    // TODO: Add replay tracking to Match (replayOfMatchId field)
    return false;
}

private void scheduleCupReplay(Match originalMatch) {
    Match replay = new Match();
    replay.setHomeClubId(originalMatch.getAwayClubId()); // Swap home/away
    replay.setAwayClubId(originalMatch.getHomeClubId());
    replay.setMatchType(Match.CUP_MATCH);
    replay.setCompetitionId(originalMatch.getCompetitionId());
    replay.setCompetitionEditionId(originalMatch.getCompetitionEditionId());
    replay.setRound(originalMatch.getRound());
    replay.setMatchDateTime(originalMatch.getMatchDateTime().plusDays(7)); // 7 days later
    replay.setIsProposed(false);
    replay.setIsAccepted(true);
    replay.setIsPlayed(false);
    
    // Add to clubs' scheduled matches
    Club homeClub = currentGame.getClubById(replay.getHomeClubId());
    Club awayClub = currentGame.getClubById(replay.getAwayClubId());
    
    if (homeClub != null) {
        homeClub.getScheduledMatches().add(replay);
    }
    if (awayClub != null) {
        awayClub.getScheduledMatches().add(replay);
    }
    
    Gdx.app.log("AuthorityManager", "Scheduled cup replay: " + homeClub.getName() + " v " + awayClub.getName());
}
```

**Files to Modify:**
- `AuthorityManager.java` - Add replay handling
- `Match.java` - Add `replayOfMatchId` field (optional)

---

## 4. IMPLEMENTATION ORDER

### Step 1: Add Match Competition Tracking
- Add fields to `Match.java`
- Update existing match creation to set these fields

### Step 2: Season Completion Detection
- Add `checkSeasonCompletion()` to `FuttoboruGameEngine`
- Add `completeSeason()` method
- Add `createSeasonCompletionMessage()` to `MessageManager`

### Step 3: Next Season Generation
- Add `generateNextSeason()` to `FuttoboruGameEngine`
- Add `createFixtureReleaseMessage()` to `MessageManager`

### Step 4: Cup Draw Integration
- Add `checkAndScheduleCupDraws()` to `AuthorityManager`
- Add helper methods for cup edition management

### Step 5: Cup Round Progression
- Add `isCupRoundComplete()` and `advanceCupRound()` to `AuthorityManager`
- Add helper methods for round management

### Step 6: Cup Replay Handling
- Add `checkCupReplays()` to `AuthorityManager`
- Add replay scheduling logic

### Step 7: Cup Completion
- Add `completeCup()` to `AuthorityManager`
- Add `createCupCompletionMessage()` to `MessageManager`

---

## 5. TESTING CHECKLIST

- [ ] Season completion detected when all league matches played
- [ ] Champion declared correctly
- [ ] Season completion message created
- [ ] Next season fixtures generated
- [ ] Cup draws created automatically
- [ ] Cup matches scheduled with dates
- [ ] Cup rounds advance correctly
- [ ] Cup replays scheduled for tied matches
- [ ] Cup winner declared when final complete
- [ ] Game can continue to Season 2
- [ ] Cup competitions regenerate for Season 2

---

**Status:** Ready for implementation  
**Next Step:** Begin with Step 1 (Add Match Competition Tracking)
