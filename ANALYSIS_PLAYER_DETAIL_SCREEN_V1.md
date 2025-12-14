# Player Detail Screen Analysis & Design v1.0

**Analysis Date:** 2025-12-14  
**Branch:** feature/match-simulation-v1  
**Priority:** HIGH - Needed for testing attribute changes

---

## Executive Summary

**Current State:** Clicking a player in the Squad screen logs a message but doesn't open any screen. A `PersonDetailsScreenTable` exists but it's only for the player's own details (manager/owner), not for viewing squad players.

**Goal:** Create a comprehensive Player Detail Screen that displays all player information, attributes, and allows tracking attribute changes over time.

---

## 1. CURRENT STATE ANALYSIS

### 1.1 What Exists ✅

- ✅ **PersonDetailsScreenTable**: Exists but only shows basic person info (name, age, country, job, club)
- ✅ **MainMenuManager**: Has screen switching infrastructure
- ✅ **PlayersListTable**: Has clickable player links (but no action)
- ✅ **Player Model**: Has all attributes defined (30+ attributes)

### 1.2 What's Missing ❌

- ❌ **Player Detail Screen**: No dedicated screen for viewing player details
- ❌ **Attribute Display**: No comprehensive attribute display
- ❌ **Attribute History**: No tracking of attribute changes over time
- ❌ **Integration**: Player links don't open any screen

---

## 2. REQUIREMENTS

### 2.1 Core Requirements (v1.0)

1. **Player Information Section**
   - Name, age, nationality
   - Birth date
   - Current club
   - Position (when implemented)

2. **Attributes Display**
   - Physical attributes (acceleration, speed, stamina, strength, endurance, jumping, dexterity)
   - Mental attributes (concentration, courage, determination, leadership, perception, positioning, teamwork)
   - Technical attributes (passing, kicking, longShots, trickShots, heading, oneTwos, freeKicks, cornerKicks, penaltyKicks, throwIns, marking, tackling)
   - Goalkeeper attributes (shotStopping, areaControl, punching, handToHand, rushingOut, areaPositioning)
   - Organized by category with clear labels

3. **Attribute Change Tracking** (v1.0 - Testing)
   - Show attribute changes since last view (e.g., "+2.1 Strength", "-0.5 Speed")
   - Color coding: green for increases, red for decreases
   - Store last viewed attributes for comparison

4. **Navigation**
   - Back button to return to Squad screen
   - Easy to access from player list

### 2.2 Enhanced Requirements (v1.1+)

- Attribute history graph/chart
- Position suitability display
- Overall rating (CA/PA)
- Form tracking
- Match history
- Contract details

---

## 3. DESIGN SPECIFICATION

### 3.1 Screen Layout

```
┌─────────────────────────────────────────────────────────┐
│ Player Detail Screen                                     │
├─────────────────────────────────────────────────────────┤
│                                                           │
│ [Back to Squad]                                          │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Player Information                                   │ │
│ │ Name: John Smith                                     │ │
│ │ Age: 24 years                                        │ │
│ │ Nationality: England                                 │ │
│ │ Birth Date: 15/03/1864                               │ │
│ │ Current Club: Preston North End                      │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Physical Attributes                                  │ │
│ │ Acceleration:  75.0  [+1.2] 🟢                      │ │
│ │ Speed:         80.0  [-0.5] 🔴                      │ │
│ │ Stamina:       70.0  [+0.3] 🟢                      │ │
│ │ Strength:      65.0  [+2.1] 🟢                      │ │
│ │ Endurance:     72.0  [=]                            │ │
│ │ Jumping:       68.0  [-0.2] 🔴                      │ │
│ │ Dexterity:     73.0  [+0.8] 🟢                      │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Mental Attributes                                    │ │
│ │ Concentration:  70.0  [+0.5] 🟢                     │ │
│ │ Courage:        75.0  [=]                            │ │
│ │ Determination:  80.0  [+1.0] 🟢                     │ │
│ │ Leadership:     60.0  [-0.3] 🔴                     │ │
│ │ Perception:     72.0  [+0.2] 🟢                     │ │
│ │ Positioning:    68.0  [=]                            │ │
│ │ Teamwork:       75.0  [+0.4] 🟢                     │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Technical Attributes                                 │ │
│ │ Passing:       65.0  [+1.5] 🟢                      │ │
│ │ Kicking:       70.0  [-0.5] 🔴                      │ │
│ │ Long Shots:    68.0  [+0.3] 🟢                      │ │
│ │ ... (all technical attributes)                      │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Goalkeeper Attributes (if applicable)                │ │
│ │ Shot Stopping:  N/A                                  │ │
│ │ ...                                                  │ │
│ └─────────────────────────────────────────────────────┘ │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Data Model

#### PlayerDetailViewState (New Class - Optional)
```java
public class PlayerDetailViewState implements Serializable {
    private Long playerId;
    private Map<String, Float> lastViewedAttributes; // Attribute name -> value
    private LocalDateTime lastViewDate;
}
```

**Note:** For v1.0, we can store this in memory or in SaveGame. For simplicity, we'll store last viewed attributes in the screen itself.

### 3.3 Attribute Change Calculation

```java
private String formatAttributeChange(Float currentValue, Float lastValue) {
    if (lastValue == null) {
        return ""; // No previous value
    }
    
    float change = currentValue - lastValue;
    
    if (Math.abs(change) < 0.1f) {
        return "[=]"; // No significant change
    }
    
    String sign = change > 0 ? "+" : "";
    String color = change > 0 ? "🟢" : "🔴";
    return String.format("[%s%.1f] %s", sign, change, color);
}
```

---

## 4. IMPLEMENTATION PLAN

### Phase 1: Basic Player Detail Screen (v1.0)
**Goal:** Display player info and all attributes

1. **Create PlayerDetailScreenTable**
   - New class extending VisTable
   - Display player information section
   - Display all attribute categories
   - Back button to Squad screen

2. **Integrate with MainMenuManager**
   - Add `PLAYER_DETAIL_SCREEN` constant
   - Add screen table instance
   - Add screen switching logic
   - Add method to set selected player

3. **Update PlayersListTable**
   - Make player links open Player Detail Screen
   - Pass player reference to MainMenuManager

4. **Attribute Display**
   - Organize by category (Physical, Mental, Technical, GK)
   - Format values (0-100 range)
   - Show all attributes

### Phase 2: Attribute Change Tracking (v1.0)
**Goal:** Show attribute changes for testing

1. **Store Last Viewed Attributes**
   - Store last viewed attribute values per player
   - Update when screen is opened

2. **Calculate Changes**
   - Compare current vs. last viewed
   - Format as "+X.X" or "-X.X"
   - Color code (green/red)

3. **Display Changes**
   - Show next to each attribute
   - Visual indicators (🟢/🔴)

### Phase 3: Age-Based Decline (v1.0)
**Goal:** Attributes naturally decline with age

1. **Update Daily Attribute Changes**
   - Modify `PlayerAttributeGenerator.applyDailyAttributeChanges()`
   - Add age-based decline factor
   - Older players decline faster

2. **Decline Algorithm**
   - Age 30+: Small decline (0.01-0.05 per day)
   - Age 35+: Moderate decline (0.05-0.1 per day)
   - Age 40+: Rapid decline (0.1-0.2 per day)

---

## 5. FILE STRUCTURE

### New Files:
```
futtoboru-core/src/main/java/com/rndmodgames/futtoboru/
├── tables/
│   └── player/
│       └── PlayerDetailScreenTable.java    # New player detail screen
```

### Files to Modify:
```
futtoboru-core/src/main/java/com/rndmodgames/futtoboru/
├── menu/
│   └── MainMenuManager.java                 # Add screen switching
├── tables/
│   └── squad/
│       └── PlayersListTable.java            # Make links functional
└── system/
    └── generators/
        └── PlayerAttributeGenerator.java    # Add age-based decline
```

---

## 6. ALGORITHM: AGE-BASED ATTRIBUTE DECLINE

```java
public void applyDailyAttributeChanges(Player player) {
    if (player == null || player.getPerson() == null) return;
    
    int age = calculateAge(player.getPerson().getBirthDate(), currentDate);
    
    // Age-based decline factor
    float declineFactor = calculateDeclineFactor(age);
    
    // Apply small random changes (as before)
    // ... existing random change code ...
    
    // Apply age-based decline to all attributes
    if (declineFactor > 0) {
        applyAgeDecline(player, declineFactor);
    }
}

private float calculateDeclineFactor(int age) {
    if (age < 30) {
        return 0.0f; // No decline before 30
    } else if (age < 35) {
        return 0.01f + (DatabaseLoader.RNG.nextFloat() * 0.04f); // 0.01-0.05
    } else if (age < 40) {
        return 0.05f + (DatabaseLoader.RNG.nextFloat() * 0.05f); // 0.05-0.1
    } else {
        return 0.1f + (DatabaseLoader.RNG.nextFloat() * 0.1f); // 0.1-0.2
    }
}

private void applyAgeDecline(Player player, float declineFactor) {
    // Decline all attributes slightly
    if (player.getAcceleration() != null) {
        player.setAcceleration(clampAttribute(player.getAcceleration() - declineFactor));
    }
    if (player.getSpeed() != null) {
        player.setSpeed(clampAttribute(player.getSpeed() - declineFactor));
    }
    // ... apply to all attributes ...
}
```

---

## 7. UI/UX DESIGN

### 7.1 Layout Structure

**Top Section:**
- Back button (left)
- Player name (center, large font)

**Information Section:**
- Two-column layout (Label | Value)
- Name, Age, Nationality, Birth Date, Club

**Attributes Sections:**
- Collapsible sections (optional for v1.1)
- Each attribute on its own row
- Format: `Attribute Name: Value [Change] Indicator`

### 7.2 Color Coding

- **Attribute Values:**
  - 80-100: Green (excellent)
  - 60-79: Yellow (good)
  - 40-59: Orange (average)
  - 0-39: Red (poor)

- **Attribute Changes:**
  - Positive change: 🟢 Green
  - Negative change: 🔴 Red
  - No change: Gray or "="

### 7.3 Responsive Design

- Scrollable if content is too long
- Proper spacing between sections
- Clear visual separation

---

## 8. INTEGRATION POINTS

### 8.1 MainMenuManager Integration

```java
// Add constant
public static final int PLAYER_DETAIL_SCREEN = 10007;

// Add screen table
private PlayerDetailScreenTable playerDetailScreenTable = null;

// Add selected player
private Player selectedPlayer = null;

// Add method
public void setSelectedPlayer(Player player) {
    this.selectedPlayer = player;
}

// Add screen case
case PLAYER_DETAIL_SCREEN:
    if (selectedPlayer != null) {
        playerDetailScreenTable.updateDynamicComponents(selectedPlayer);
        parentTable.add(playerDetailScreenTable).grow();
    }
    break;
```

### 8.2 PlayersListTable Integration

```java
playerDetailLink.setListener(new LinkLabelListener() {
    @Override
    public void clicked (String url) {
        // Get MainMenuManager from game
        MainMenuManager menuManager = ((Futtoboru)game).getGameEngine().getMainMenuManager();
        if (menuManager != null) {
            menuManager.setSelectedPlayer(player);
            menuManager.setActiveMainScreen(MainMenuManager.PLAYER_DETAIL_SCREEN);
        }
    }
});
```

---

## 9. TESTING STRATEGY

### 9.1 Manual Testing

1. **Open Player Detail**
   - Click player in Squad screen
   - Verify screen opens
   - Verify all information displays

2. **Attribute Display**
   - Verify all attributes show
   - Verify values are in 0-100 range
   - Verify formatting is correct

3. **Attribute Changes**
   - View player detail
   - Advance days
   - View player detail again
   - Verify changes are shown
   - Verify color coding works

4. **Age Decline**
   - Test with young player (20-25): No decline
   - Test with middle-aged player (30-35): Small decline
   - Test with old player (35+): Faster decline

### 9.2 Edge Cases

- Player with null attributes (should show "N/A")
- Very old player (40+): Rapid decline
- Very young player (<18): No decline, may improve

---

## 10. SUCCESS CRITERIA

### v1.0 MVP Success:
- ✅ Player Detail Screen opens from Squad screen
- ✅ All player information displays correctly
- ✅ All attributes display organized by category
- ✅ Attribute changes show when viewing again
- ✅ Age-based decline works (older players decline)
- ✅ Back button returns to Squad screen

### v1.1 Enhanced:
- Attribute history graph
- Position suitability
- Overall rating display
- Form tracking

---

## 11. ESTIMATED EFFORT

### Phase 1: Basic Screen (2-3 days)
- Create PlayerDetailScreenTable: 1 day
- Integrate with MainMenuManager: 0.5 days
- Update PlayersListTable: 0.5 days
- Testing: 1 day

### Phase 2: Attribute Changes (1 day)
- Store last viewed: 0.5 days
- Calculate and display: 0.5 days

### Phase 3: Age Decline (1 day)
- Update algorithm: 0.5 days
- Testing: 0.5 days

**Total Estimated Time:** 4-5 days

---

## 12. NEXT STEPS

1. **Review this analysis** - Confirm approach
2. **Create PlayerDetailScreenTable** - Basic screen structure
3. **Integrate with MainMenuManager** - Screen switching
4. **Update PlayersListTable** - Make links functional
5. **Add attribute change tracking** - Show changes
6. **Add age-based decline** - Natural attribute decay

---

## 13. REFERENCES

- `PersonDetailsScreenTable.java`: Reference for person detail screen structure
- `ClubDetailScreenTable.java`: Reference for detail screen with back button
- `ANALYSIS_FOUNDATIONAL_SYSTEMS_V1.md`: Attribute system design
- `PlayerAttributeGenerator.java`: Current attribute generation

---

**Priority:** 🔴 HIGH - Needed for testing  
**Status:** 📋 READY FOR IMPLEMENTATION  
**Estimated Start:** Immediate

