# Match Income & Attendance Analysis v1.0

**Analysis Date:** 2025-01-XX  
**Feature:** Financial Screen Enhancements  
**Priority:** Core Feature Enhancement

---

## Executive Summary

This document analyzes match income and attendance calculations, with focus on historical accuracy for the 1888-89 season and alignment with Football Manager mechanics.

---

## 1. HISTORICAL RESEARCH FINDINGS

### 1.1 1888-89 Season Attendance Data

**Friendly Matches:**
- Royal Arsenal F.C.: 500-700 spectators typical
- St. Mary's F.C.: Unenclosed grounds, no entrance fees for most friendlies
- Semi-final replays: ~2,000 spectators (competitive context)

**League Matches:**
- 1888-89 Football League: Higher attendance than friendlies
- Average attendance data not fully documented, but significantly higher than friendlies

**International Matches:**
- England vs Scotland: 10,000 spectators
- England vs Ireland: 7,000 spectators
- England vs Wales: 6,000 spectators

### 1.2 Key Findings

1. **Friendly matches had MUCH lower attendance** than competitive matches
2. **Typical friendly attendance:** 500-700 (10-15% of stadium capacity for small clubs)
3. **League matches:** Significantly higher, but exact percentages vary
4. **Cup matches:** Higher than friendlies, lower than league typically

---

## 2. FOOTBALL MANAGER MECHANICS

### 2.1 Attendance Factors

Football Manager uses several factors to calculate attendance:

1. **Ticket Price:** Directly affects attendance and revenue
2. **Team Performance:** 
   - Win: +15% base attendance
   - Loss: -13% base attendance
3. **Opponent Strength:** Higher-ranked/rival teams = larger crowds
4. **Stadium Facilities:** Upgraded facilities attract more spectators
5. **Weather Conditions:** 
   - Snow: -35% attendance
   - Rain: Moderate reduction
6. **Match Importance:** Competitive matches > friendlies

### 2.2 Revenue Distribution

- **Home Team:** Receives majority of ticket revenue
- **Away Team:** May receive fixed fee or percentage (e.g., 33% in some games)
- **Revenue Sharing:** Varies by competition rules

---

## 3. CURRENT IMPLEMENTATION ANALYSIS

### 3.1 Current Ticket Sales Logic

**MatchScheduler.java:**
- Sells tickets for all match types using same logic
- Sells 1/7 of capacity per day over 7 days
- Minimum 20 tickets per day
- Uses same ticket price for all match types

**Issues:**
- Friendly matches get same ticket sales as league matches
- No differentiation by match type
- No historical accuracy for 1888-89 period

### 3.2 Financial Screen Display

**Current:**
- Shows income breakdown by match type (League, Cup, Friendly)
- No attendance information displayed
- No average attendance calculation

**Needed:**
- Add average attendance column
- Show match count
- Better visual presentation

---

## 4. PROPOSED CHANGES

### 4.1 Friendly Match Ticket Sales Reduction

**Historical Reality:**
- Friendly matches: 10-20% of league match attendance
- Based on 500-700 vs typical league attendance

**Implementation:**
- Reduce friendly match ticket sales to 15% of league match sales
- Keep league and cup matches at current levels (for now)
- Formula: `friendlyTickets = leagueTickets * 0.15`

### 4.2 Financial Screen Enhancements

**New Table Format:**
```
Match Type          | Revenue      | Avg Attendance | Matches
--------------------|--------------|----------------|--------
League Matches      | $XXX.XX      | X,XXX          | XX
Cup Matches         | $XXX.XX      | X,XXX          | XX
Friendly Matches    | $XXX.XX      | XXX            | XX
```

**Data Needed:**
- Total revenue per match type
- Total attendance per match type
- Match count per match type
- Average = total attendance / match count

### 4.3 Future Enhancements (Documented for Later)

**Club Reputation System:**
- Higher reputation = higher base attendance
- Multiplier: 0.8x to 1.5x based on reputation

**Current Standings:**
- Top of table: +10-20% attendance
- Bottom of table: -10-20% attendance
- Mid-table: Base attendance

**Opponent Strength:**
- Strong opponent: +15-25% attendance
- Weak opponent: -10-15% attendance

**Weather System:**
- Snow: -35% attendance
- Heavy rain: -20% attendance
- Light rain: -10% attendance
- Clear: No modifier

**Stadium Facilities:**
- Basic: Base attendance
- Improved: +5-10% attendance
- Excellent: +10-20% attendance

**Match Importance:**
- League title decider: +30-50%
- Relegation battle: +20-30%
- Mid-season: Base

---

## 5. IMPLEMENTATION PLAN

### Phase 1: Immediate Changes (Current)

1. ✅ Reduce friendly match ticket sales to 15% of league match sales
2. ✅ Add average attendance calculation
3. ✅ Update financial screen to show attendance table
4. ✅ Track match count per type

### Phase 2: Future Enhancements

1. Club reputation system
2. Standings-based attendance modifiers
3. Opponent strength calculations
4. Weather system integration
5. Stadium facilities impact

---

## 6. TECHNICAL DETAILS

### 6.1 Ticket Sales Formula

**Current (All Match Types):**
```
ticketsPerDay = (capacity / 7) with min 20, max = available
```

**New (Friendly Matches):**
```
friendlyTicketsPerDay = (capacity / 7) * 0.15 with min 5, max = available * 0.15
```

**New (League/Cup Matches):**
```
competitiveTicketsPerDay = (capacity / 7) with min 20, max = available
```

### 6.2 Attendance Calculation

**Average Attendance:**
```
avgAttendance = totalAttendance / matchCount
```

**Per Match Type:**
- Filter MatchIncome by matchType
- Sum attendance
- Count matches
- Calculate average

---

## 7. TESTING CONSIDERATIONS

### 7.1 Verification

- Friendly matches should have ~15% of league match attendance
- Financial screen should show accurate averages
- Match counts should be correct
- Revenue should match attendance * ticket price

### 7.2 Edge Cases

- No matches played yet (show 0 or N/A)
- Only one match (average = that match's attendance)
- Matches with 0 attendance (handle gracefully)

---

## 8. CONCLUSION

This analysis provides the foundation for realistic match income and attendance calculations, with immediate focus on historical accuracy for friendly matches and enhanced financial screen display. Future enhancements can be added incrementally as the game systems mature.

---

**Next Steps:**
1. Implement friendly match ticket reduction
2. Add attendance tracking to financial screen
3. Test and verify calculations
4. Document any issues or adjustments needed
