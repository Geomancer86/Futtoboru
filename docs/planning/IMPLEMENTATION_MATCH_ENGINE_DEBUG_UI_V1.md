# Match Engine Debug UI - Implementation Guide

**Version:** 1.0  
**Date:** 2024-12-19  
**Status:** Implementation Ready

---

## Quick Reference: Implementation Order

### Phase 1: Core Structure (Week 1)

**Priority Order:**
1. ✅ Screen structure and layout
2. ✅ Match type selector (Friendly/League/Cup/Custom)
3. ✅ Team selector (Home/Away with search)
4. ✅ Stadium selector (with auto-select)
5. ✅ Date/Time pickers
6. ✅ Basic weather selector
7. ✅ Start Match button (navigates to placeholder)

**Deliverable:** Functional basic match configuration

---

### Phase 2: Enhanced Features (Week 2)

**Priority Order:**
1. Match preview panel
2. Advanced options panel (collapsible)
3. Player condition overrides
4. Team bonuses section
5. League context (if league match)
6. Validation system
7. Error handling

**Deliverable:** Complete match configuration with validation

---

### Phase 3: Polish (Week 3)

**Priority Order:**
1. Quick presets system
2. Visual polish (icons, colors, spacing)
3. Tooltips and help text
4. Responsive design
5. Keyboard navigation
6. Loading states

**Deliverable:** Polished, production-ready UI

---

## Component Checklist

### ✅ Core Components

- [ ] `MatchTypeSelector` - Radio buttons or tabs
- [ ] `TeamSelector` - Dropdown with search
- [ ] `StadiumSelector` - Dropdown with auto-option
- [ ] `DatePicker` - Calendar component
- [ ] `TimePicker` - Time selection
- [ ] `WeatherSelector` - Icon-based selector
- [ ] `TemperatureInput` - Number input with range
- [ ] `PitchConditionSelector` - Dropdown
- [ ] `AttendanceInput` - Auto/Manual toggle
- [ ] `MatchPreviewPanel` - Summary display
- [ ] `AdvancedOptionsPanel` - Collapsible panel

### ✅ Advanced Components

- [ ] `PlayerConditionOverride` - Team/Individual
- [ ] `TeamBonusesSection` - Multiple inputs
- [ ] `LeagueContextSection` - Contextual display
- [ ] `MatchRulesSection` - Rules configuration
- [ ] `DebugOptionsSection` - Debug settings

### ✅ Action Components

- [ ] `QuickPresetsButton` - Preset dropdown
- [ ] `ResetButton` - Clear all
- [ ] `CancelButton` - Return to menu
- [ ] `StartMatchButton` - Primary action

---

## Data Integration Points

### Required Managers/Services

1. **Team Loading:**
   - `SaveGame.getAllClubs()` - Get all teams
   - Filter by league if needed
   - Search functionality

2. **Stadium Loading:**
   - `Club.getStadium()` - Get team stadiums
   - Load all stadiums for selection

3. **League/Cup Loading:**
   - `AuthorityManager.getLeagues()` - Get leagues
   - `SaveGame.getAllCups()` - Get cups
   - Load standings if league match

4. **Weather/Climate:**
   - Create `WeatherService` for climate data
   - Seasonal weather patterns
   - Regional climate data

5. **Player Data:**
   - `Club.getPlayers()` - Get team players
   - Player condition tracking (if exists)

---

## Key Implementation Notes

### Match Type Logic

```java
switch (matchType) {
    case FRIENDLY_MATCH:
        // No league/cup context needed
        // Flexible venue selection
        break;
    case LEAGUE_MATCH:
        // Require league selection
        // Show league standings
        // Stadium typically home team's
        break;
    case CUP_MATCH:
        // Require cup selection
        // Show round information
        // May allow neutral venue
        break;
    case CUSTOM_MATCH:
        // Full control, no restrictions
        break;
}
```

### Team Selection Logic

```java
// Validation
if (homeTeamId.equals(awayTeamId)) {
    showError("Teams must be different");
    return;
}

// Auto-select stadium
if (matchType == FRIENDLY || matchType == LEAGUE) {
    Club homeTeam = getClubById(homeTeamId);
    stadiumId = homeTeam.getStadium().getId();
}
```

### Weather Auto-Suggestion

```java
// Based on date and season
LocalDate matchDate = matchDateTime.toLocalDate();
Season season = getSeasonForDate(matchDate);
Weather suggestedWeather = calculateWeatherForSeason(season, matchDate);

// User can override
```

### Attendance Calculation

```java
// Factors:
// - Match type (League > Cup > Friendly)
// - Team popularity
// - Weather
// - Date/time
// - Stadium capacity
// - Team form

int calculatedAttendance = calculateAttendance(
    matchType, homeTeam, awayTeam, stadium, weather, date
);
```

---

## UI Component Specifications

### Match Type Selector

**Implementation:**
- Use `VisSelectBox` or custom button group
- Visual tabs or radio buttons
- Selected state clearly visible

**Code Structure:**
```java
VisSelectBox<String> matchTypeSelect = new VisSelectBox<>();
matchTypeSelect.setItems("Friendly", "League", "Cup", "Custom");
matchTypeSelect.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        updateAvailableOptions();
    }
});
```

### Team Selector

**Implementation:**
- Custom component with search
- `VisSelectBox` with custom renderer
- Search filter on list

**Code Structure:**
```java
// Load teams
List<Club> allClubs = saveGame.getAllClubs();

// Filter/search
List<Club> filteredClubs = allClubs.stream()
    .filter(c -> c.getName().contains(searchText))
    .collect(Collectors.toList());

// Display in select box
```

### Weather Selector

**Implementation:**
- Icon buttons in horizontal layout
- Selected state highlighted
- Temperature input below

**Code Structure:**
```java
VisTable weatherTable = new VisTable();
for (Weather weather : Weather.values()) {
    VisImageButton weatherButton = createWeatherButton(weather);
    weatherTable.add(weatherButton);
}
```

---

## Validation Rules

### Required Field Validation

```java
public boolean validateConfiguration() {
    if (matchType == null) {
        addError("Match type must be selected");
        return false;
    }
    
    if (homeTeamId == null || awayTeamId == null) {
        addError("Both teams must be selected");
        return false;
    }
    
    if (homeTeamId.equals(awayTeamId)) {
        addError("Teams must be different");
        return false;
    }
    
    if (stadiumId == null) {
        addError("Stadium must be selected");
        return false;
    }
    
    if (matchDateTime == null) {
        addError("Match date and time must be set");
        return false;
    }
    
    return true;
}
```

### Warning Checks

```java
public List<String> getWarnings() {
    List<String> warnings = new ArrayList<>();
    
    if (attendance > stadium.getCapacity()) {
        warnings.add("Attendance exceeds stadium capacity");
    }
    
    if (temperature < -5 || temperature > 35) {
        warnings.add("Unusual temperature for match");
    }
    
    if (homeTeamCondition < 50 || awayTeamCondition < 50) {
        warnings.add("Teams have low condition");
    }
    
    return warnings;
}
```

---

## State Management

### Configuration Object

```java
public class MatchEngineConfig {
    // Core
    private Integer matchType;
    private Long homeTeamId;
    private Long awayTeamId;
    private Long stadiumId;
    private LocalDateTime matchDateTime;
    
    // Conditions
    private Weather weather;
    private Integer temperature;
    private PitchCondition pitchCondition;
    private Integer attendance;
    private Float crowdAtmosphere;
    
    // Advanced
    private Float homeTeamCondition = 100f;
    private Float awayTeamCondition = 100f;
    private Map<Long, Float> playerConditionOverrides = new HashMap<>();
    private Float homeAdvantage = 1.0f;
    
    // Getters/setters...
}
```

### UI State

```java
public class MatchEngineUIState {
    private boolean advancedOptionsExpanded = false;
    private boolean playerConditionsExpanded = false;
    private String selectedPreset = null;
    private List<String> validationErrors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
}
```

---

## Integration with Match Engine

### Starting a Match

```java
public void startMatch() {
    // Validate configuration
    if (!validateConfiguration()) {
        showValidationErrors();
        return;
    }
    
    // Show warnings if any
    List<String> warnings = getWarnings();
    if (!warnings.isEmpty()) {
        showWarningsDialog(warnings);
    }
    
    // Create match configuration
    MatchEngineConfig config = buildConfiguration();
    
    // Navigate to match visualization screen
    // (To be implemented - match engine screen)
    navigateToMatchScreen(config);
}
```

### Configuration to Match Data

```java
public Match createMatchFromConfig(MatchEngineConfig config) {
    Match match = new Match();
    match.setMatchType(config.getMatchType());
    match.setHomeClubId(config.getHomeTeamId());
    match.setAwayClubId(config.getAwayTeamId());
    match.setMatchDateTime(config.getMatchDateTime());
    // ... set other fields
    
    return match;
}
```

---

## Testing Checklist

### Functional Testing

- [ ] All match types selectable
- [ ] Team selection works (search, filter)
- [ ] Stadium auto-selects correctly
- [ ] Date/time pickers functional
- [ ] Weather selection works
- [ ] Advanced options expand/collapse
- [ ] Validation prevents invalid configs
- [ ] Warnings display correctly
- [ ] Start Match button works
- [ ] Reset clears all fields
- [ ] Cancel returns to menu

### UI/UX Testing

- [ ] Visual hierarchy clear
- [ ] All sections readable
- [ ] Icons and labels clear
- [ ] Hover states work
- [ ] Selected states visible
- [ ] Loading states show
- [ ] Error messages clear
- [ ] Tooltips helpful
- [ ] Keyboard navigation works
- [ ] Responsive on different sizes

### Edge Cases

- [ ] No teams available
- [ ] No stadiums available
- [ ] Invalid date selection
- [ ] Extreme values (temp, attendance)
- [ ] Very long team names
- [ ] Special characters in names
- [ ] Rapid clicking (prevent double-submit)
- [ ] Network delays (data loading)

---

## Performance Considerations

### Optimization

1. **Lazy Loading:**
   - Load teams only when dropdown opened
   - Load stadiums on demand
   - Load league data only if league match

2. **Caching:**
   - Cache team list
   - Cache stadium list
   - Cache league standings

3. **Debouncing:**
   - Debounce search input
   - Debounce validation checks

4. **Efficient Rendering:**
   - Only render visible sections
   - Virtual scrolling for long lists
   - Minimize re-renders

---

## Next Steps

1. **Review Design Document** - Ensure all requirements captured
2. **Create Component Stubs** - Basic structure for all components
3. **Implement Phase 1** - Core functionality
4. **Test & Iterate** - User testing, refine UX
5. **Implement Phase 2** - Enhanced features
6. **Polish & Optimize** - Final touches

---

**Status:** Ready for Implementation  
**Priority:** High - Foundation for Match Engine Development
