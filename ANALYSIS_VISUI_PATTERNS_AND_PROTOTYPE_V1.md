# VisUI Patterns Analysis & Match Engine Debug Prototype

**Version:** 1.0  
**Date:** 2025-12-20  
**Status:** Research Complete, Prototype Ready

---

## ⚠️ IMPORTANT: UI/UX Style Guide

**Before implementing any UI, read:** `FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md`

**Critical Rules:**
- **ALWAYS use black background:** `Gdx.gl.glClearColor(0, 0, 0, 1)`
- **ALWAYS use white text** for primary content
- **Match existing screen styles** - check similar screens for patterns
- **Test component visibility** - all components must be visible on black background

---

## Executive Summary

This document provides a comprehensive analysis of VisUI patterns used in the Futtoboru codebase, along with a complete prototype design for the Match Engine Debug screen. The prototype includes all UI components as placeholders, ready for incremental implementation.

---

## 1. VisUI Component Patterns Analysis

### 1.1 Core Components Used in Futtoboru

#### **VisSelectBox** (Dropdown/Combo Box)
**Usage Pattern:**
```java
VisSelectBox<Club> clubSelectBox = new VisSelectBox<>();
clubSelectBox.setItems(clubsArray);
clubSelectBox.setSelectedIndex(0);
clubSelectBox.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        Club selected = clubSelectBox.getSelected();
        // Handle selection
    }
});
```

**Key Findings:**
- Used for single-selection dropdowns
- Generic type support (e.g., `VisSelectBox<Club>`)
- `setItems()` accepts array
- `addListener()` for change events
- `setSelectedIndex()` for default selection
- `getSelected()` returns selected item

**Examples in Codebase:**
- `ArrangeFriendlyTable.java` - Match types, venues, rules
- `NewGameOverviewScreen.java` - Country, profession, club selection
- `CountryAndCitySelectBox.java` - Country/state/city cascading dropdowns

---

#### **VisSlider** (Numeric Range Input)
**Usage Pattern:**
```java
VisSlider slider = new VisSlider(0, 100, 1, false); // min, max, step, vertical
slider.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        float value = slider.getValue();
        valueLabel.setText(value + "%");
    }
});
```

**Key Findings:**
- Constructor: `(min, max, stepSize, vertical)`
- `getValue()` returns float
- `setValue()` to set programmatically
- Often paired with label showing current value
- Used for volume, percentages, condition levels

**Examples:**
- `SoundSettingsTable.java` - Master sound, music, effects volume

---

#### **VisTextField / VisValidatableTextField** (Text Input)
**Usage Pattern:**
```java
VisValidatableTextField textField = new VisValidatableTextField();
textField.setTextFieldListener(new TextFieldListener() {
    @Override
    public void keyTyped(VisTextField textField, char key) {
        // Handle input
    }
});
```

**Key Findings:**
- `VisValidatableTextField` for validated inputs
- `setTextFieldListener()` for input handling
- `getText()` / `setText()` for value access
- Used for names, dates, numbers

**Examples:**
- `NewManagerScreen.java` - Name, last name, birth date

---

#### **VisTable** (Layout Container)
**Usage Pattern:**
```java
VisTable table = new VisTable(true); // true = auto spacing
table.setFillParent(true); // Fill parent container
table.pad(20); // Padding
table.add(label).colspan(2).padBottom(10);
table.row();
table.add(button).fill();
```

**Key Findings:**
- Constructor `(true)` enables automatic spacing
- `setFillParent(true)` makes it fill parent
- `.row()` for new row
- `.add()` with chaining for layout constraints
- `.colspan(n)` for column spanning
- `.fill()`, `.expandX()`, `.grow()` for sizing
- `.pad()`, `.padLeft()`, `.padBottom()` for padding
- `.left()`, `.right()`, `.center()` for alignment

**Layout Patterns:**
- Two-column forms: Label | Input
- Multi-column grids: `.colspan()` for headers
- Nested tables for complex layouts
- Scroll panes for long content

---

#### **VisScrollPane** (Scrollable Container)
**Usage Pattern:**
```java
VisTable contentTable = new VisTable(true);
VisScrollPane scrollPane = new VisScrollPane(contentTable);
scrollPane.setFadeScrollBars(false);
mainTable.add(scrollPane).grow();
```

**Key Findings:**
- Wraps a `VisTable` or other `Actor`
- `setFadeScrollBars(false)` keeps scrollbars visible
- `.grow()` to fill available space
- Used for long lists, detailed views

**Examples:**
- `PlayerDetailScreenTable.java` - Scrollable player details
- `LeagueDrawScreenTable.java` - Scrollable fixture list
- `InboxScreenTable.java` - Scrollable message list

---

#### **VisLabel** (Text Display)
**Usage Pattern:**
```java
VisLabel label = new VisLabel("Text");
label.setFontScale(1.5f); // Scale font
label.setColor(0.7f, 0.7f, 0.7f, 1f); // RGBA color
label.setWrap(true); // Text wrapping
label.setAlignment(Align.center); // Text alignment
```

**Key Findings:**
- Simple text display
- `setFontScale()` for size
- `setColor()` for color (RGBA 0-1)
- `setWrap(true)` for text wrapping
- `setAlignment()` for text alignment

---

#### **VisTextButton** (Button)
**Usage Pattern:**
```java
VisTextButton button = new VisTextButton("Label");
button.addCaptureListener(new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return true;
    }
    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        if (button.isPressed()) {
            // Handle click
        }
    }
});
button.setDisabled(true); // Disable button
```

**Key Findings:**
- `addCaptureListener()` for click handling
- `touchDown()` returns true to capture event
- `touchUp()` handles actual click
- `isPressed()` check in touchUp
- `setDisabled()` to disable button

---

#### **VisCheckBox** (Checkbox)
**Usage Pattern:**
```java
VisCheckBox checkBox = new VisCheckBox("Label");
checkBox.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        boolean checked = checkBox.isChecked();
    }
});
```

**Key Findings:**
- Boolean input
- `isChecked()` to get state
- `setChecked()` to set programmatically
- `addListener()` for change events

---

### 1.2 Layout Patterns

#### **Two-Column Form Layout**
```java
table.row();
table.add(label).left().width(150);
table.add(input).left().fillX();
```

#### **Sectioned Layout with Separators**
```java
table.addSeparator().colspan(2).pad(10);
table.row();
```

#### **Nested Tables for Complex Layouts**
```java
VisTable outerTable = new VisTable(true);
VisTable innerTable = new VisTable(true);
innerTable.add(...);
outerTable.add(innerTable).grow();
```

#### **Scrollable Content**
```java
VisTable content = new VisTable(true);
VisScrollPane scroll = new VisScrollPane(content);
mainTable.add(scroll).grow();
```

---

### 1.3 Collapsible Sections Pattern

**No Built-in Collapsible Widget Found**

**Custom Implementation Pattern:**
```java
// Toggle button
VisTextButton expandButton = new VisTextButton("▼ Expand");
VisTable collapsibleContent = new VisTable(true);

boolean isExpanded = false;

expandButton.addCaptureListener(new InputListener() {
    @Override
    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        isExpanded = !isExpanded;
        collapsibleContent.setVisible(isExpanded);
        expandButton.setText(isExpanded ? "▲ Collapse" : "▼ Expand");
    }
});
```

---

### 1.4 Data Loading Patterns

#### **Loading Clubs/Teams**
```java
List<Club> allClubs = currentGame.getAllClubs();
Club[] clubsArray = allClubs.toArray(new Club[allClubs.size()]);
clubSelectBox.setItems(clubsArray);
```

#### **Filtering Based on Selection**
```java
countrySelectBox.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        Country selected = countrySelectBox.getSelected();
        // Filter clubs by country
        List<Club> filtered = filterClubsByCountry(selected);
        clubSelectBox.setItems(filtered.toArray(new Club[filtered.size()]));
    }
});
```

---

## 2. Match Engine Debug Screen Prototype Design

### 2.1 Screen Structure

```
MatchEngineDebugScreen
├── Main Container (VisTable, fillParent)
│   ├── Header Section
│   │   ├── Title
│   │   ├── Version Info
│   │   └── Back Button
│   │
│   ├── Scrollable Content (VisScrollPane)
│   │   └── Content Table (VisTable)
│   │       ├── Step 1: Match Type
│   │       ├── Step 2: Teams
│   │       ├── Step 3: Venue & Date
│   │       ├── Step 4: Match Conditions
│   │       ├── Step 5: Advanced Options (Collapsible)
│   │       └── Match Preview Panel
│   │
│   └── Footer Section
│       ├── Quick Presets
│       ├── Reset Button
│       ├── Cancel Button
│       └── Start Match Button
```

---

### 2.2 Component Specifications

#### **Step 1: Match Type Selector**
- **Component:** `VisSelectBox<String>`
- **Options:** "Friendly", "League", "Cup", "Custom"
- **Default:** "Friendly"
- **Placeholder:** Hardcoded array for now

#### **Step 2: Team Selectors**
- **Component:** `VisSelectBox<Club>` (two instances)
- **Labels:** "Home Team", "Away Team"
- **Placeholder:** Load from `SaveGame.getAllClubs()` (if available)
- **Swap Button:** `VisTextButton` to swap teams

#### **Step 3: Venue & Date**
- **Stadium:** `VisSelectBox<Stadium>` (placeholder: empty for now)
- **Date:** `VisLabel` with "Select Date" (placeholder: date picker later)
- **Time:** `VisSelectBox<String>` with time options
- **Placeholder:** Static options for now

#### **Step 4: Match Conditions**
- **Weather:** `VisSelectBox<String>` (Sunny, Cloudy, Rain, etc.)
- **Temperature:** `VisSlider` (0-35°C) + `VisLabel` showing value
- **Pitch Condition:** `VisSelectBox<String>` (Poor, Fair, Good, Excellent)
- **Attendance:** `VisCheckBox` (Auto) + `VisTextField` (Manual)
- **Placeholder:** All static options

#### **Step 5: Advanced Options (Collapsible)**
- **Toggle Button:** `VisTextButton` ("▼ Expand" / "▲ Collapse")
- **Content Table:** `VisTable` (visible based on toggle)
- **Sections:**
  - Player Conditions (sliders)
  - Team Bonuses (select boxes)
  - League Context (labels, if league match)
- **Placeholder:** All static, collapsible works

#### **Match Preview Panel**
- **Component:** `VisTable` with labels
- **Content:** Summary of all selections
- **Placeholder:** Static text showing structure

---

### 2.3 Implementation Strategy

**Phase 1: Structure (Current)**
- ✅ Screen layout
- ✅ All sections as placeholders
- ✅ Collapsible advanced options
- ✅ Basic navigation

**Phase 2: Data Integration**
- Load teams from SaveGame
- Load stadiums
- Date/time handling
- Match type context

**Phase 3: Logic**
- Validation
- Smart defaults
- Contextual updates
- Match preview updates

**Phase 4: Polish**
- Icons
- Colors
- Tooltips
- Animations

---

## 3. Prototype Implementation Notes

### 3.1 Key Design Decisions

1. **Scrollable Content:** Long form needs scrolling
2. **Collapsible Advanced:** Reduces initial complexity
3. **Placeholder Data:** Allows testing layout before data integration
4. **Sectioned Layout:** Clear visual hierarchy
5. **Progressive Disclosure:** Basic → Advanced

### 3.2 VisUI Best Practices Applied

1. **Consistent Spacing:** Use `super(true)` for auto-spacing
2. **Proper Padding:** `.pad()` for section separation
3. **Fill Constraints:** `.fillX()` for inputs, `.grow()` for containers
4. **Event Handling:** Proper listener patterns
5. **State Management:** Track selections for preview/validation

### 3.3 Future Enhancements

1. **Search Functionality:** For team selection (filter as you type)
2. **Date Picker:** Custom calendar component
3. **Icons:** Weather icons, team logos
4. **Validation:** Real-time form validation
5. **Presets:** Save/load match configurations

---

## 4. Code Structure

### 4.1 Class Organization

```java
public class MatchEngineDebugScreen implements Screen {
    // Stage and containers
    Stage stage;
    VisTable mainTable;
    VisScrollPane scrollPane;
    VisTable contentTable;
    
    // UI Components (organized by section)
    // Step 1: Match Type
    VisSelectBox<String> matchTypeSelectBox;
    
    // Step 2: Teams
    VisSelectBox<Club> homeTeamSelectBox;
    VisSelectBox<Club> awayTeamSelectBox;
    VisTextButton swapTeamsButton;
    
    // Step 3: Venue & Date
    VisSelectBox<Stadium> stadiumSelectBox;
    VisLabel dateLabel;
    VisSelectBox<String> timeSelectBox;
    
    // Step 4: Conditions
    VisSelectBox<String> weatherSelectBox;
    VisSlider temperatureSlider;
    VisLabel temperatureLabel;
    // ... etc
    
    // Step 5: Advanced (collapsible)
    VisTextButton advancedToggleButton;
    VisTable advancedContentTable;
    boolean advancedExpanded = false;
    
    // Match Preview
    VisTable previewTable;
    
    // Action Buttons
    VisTextButton startMatchButton;
    VisTextButton resetButton;
    VisTextButton cancelButton;
}
```

### 4.2 Method Organization

```java
// Constructor
public MatchEngineDebugScreen(Game parent)

// Section Builders
private void buildMatchTypeSection()
private void buildTeamSelectionSection()
private void buildVenueDateSection()
private void buildConditionsSection()
private void buildAdvancedOptionsSection()
private void buildMatchPreviewSection()
private void buildActionButtons()

// Helpers
private void updateMatchPreview()
private void toggleAdvancedOptions()
private void resetForm()
private void validateForm()
```

---

## 5. Next Steps

1. **Implement Prototype:** Build full UI with placeholders
2. **Test Layout:** Verify spacing, scrolling, collapsible sections
3. **Data Integration:** Connect to SaveGame for teams/stadiums
4. **Logic Implementation:** Add validation, smart defaults
5. **Match Engine Integration:** Connect "Start Match" to engine

---

## Conclusion

This analysis provides a comprehensive foundation for implementing the Match Engine Debug screen. The prototype approach allows us to:

- **Visualize the complete UI** before full implementation
- **Test layout and flow** early
- **Incrementally add functionality** section by section
- **Maintain consistency** with existing codebase patterns

The VisUI patterns identified ensure we follow established conventions and leverage proven component usage patterns from the Futtoboru codebase.
