# Match Engine Test Interface - Quick Implementation Guide

**Quick Reference:** Step-by-step implementation guide for Match Engine Test Interface

---

## Step 1: Add Screen Constant (5 minutes)

**File:** `MainMenuManager.java`

```java
// Add to screen ID constants section (around line 98)
public static final int MATCH_ENGINE_TEST_SCREEN = 40000;
```

---

## Step 2: Create MatchEngineButton (15 minutes)

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/menu/buttons/MatchEngineButton.java`

```java
package com.rndmodgames.futtoboru.menu.buttons;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

public class MatchEngineButton extends VisTextButton {

    public MatchEngineButton(MainMenuManager menuManager) {
        super("Match Engine");
        
        this.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                menuManager.setActiveMainScreen(MainMenuManager.MATCH_ENGINE_TEST_SCREEN);
            }
        });
    }
}
```

---

## Step 3: Create MatchEngineTestScreenTable (2-3 hours)

**File:** `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/tables/match/engine/MatchEngineTestScreenTable.java`

**Basic Structure:**
```java
package com.rndmodgames.futtoboru.tables.match.engine;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.*;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

public class MatchEngineTestScreenTable extends VisTable {
    
    private Game game;
    private SaveGame currentGame;
    
    // Team selection
    private VisSelectBox<Club> homeTeamSelectBox;
    private VisSelectBox<Club> awayTeamSelectBox;
    private VisLabel homeTeamInfoLabel;
    private VisLabel awayTeamInfoLabel;
    
    // Stadium selection
    private VisSelectBox<Stadium> stadiumSelectBox;
    private VisLabel stadiumInfoLabel;
    
    // Environmental conditions
    private VisSelectBox<String> weatherSelectBox;
    private VisSelectBox<String> pitchConditionSelectBox;
    private VisSlider temperatureSlider;
    private VisLabel temperatureLabel;
    
    // Match controls
    private VisTextButton simulateButton;
    private VisTextButton resetButton;
    
    // Match view
    private VisScrollPane matchViewScrollPane;
    private VisLabel matchCommentaryLabel;
    
    // Match result
    private VisTable matchResultPanel;
    private VisLabel scoreLabel;
    private VisLabel statsLabel;
    
    public MatchEngineTestScreenTable(Game game) {
        super(true);
        pad(10);
        
        this.game = game;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        // Initialize components
        initializeComponents();
        
        // Setup layout
        setupLayout();
    }
    
    private void initializeComponents() {
        // Team select boxes
        homeTeamSelectBox = new VisSelectBox<>();
        awayTeamSelectBox = new VisSelectBox<>();
        homeTeamInfoLabel = new VisLabel("");
        awayTeamInfoLabel = new VisLabel("");
        
        // Stadium select box
        stadiumSelectBox = new VisSelectBox<>();
        stadiumInfoLabel = new VisLabel("");
        
        // Weather and pitch
        weatherSelectBox = new VisSelectBox<>();
        weatherSelectBox.setItems("Sunny", "Cloudy", "Rain", "Snow", "Wind");
        weatherSelectBox.setSelected("Sunny");
        
        pitchConditionSelectBox = new VisSelectBox<>();
        pitchConditionSelectBox.setItems("Poor", "Fair", "Good", "Excellent");
        pitchConditionSelectBox.setSelected("Good");
        
        // Temperature
        temperatureSlider = new VisSlider(-10, 40, 1, false);
        temperatureSlider.setValue(15);
        temperatureLabel = new VisLabel("Temperature: 15°C");
        
        // Buttons
        simulateButton = new VisTextButton("Simulate Match");
        resetButton = new VisTextButton("Reset");
        
        // Match view
        matchCommentaryLabel = new VisLabel("Match commentary will appear here...");
        matchViewScrollPane = new VisScrollPane(matchCommentaryLabel);
        
        // Match result
        matchResultPanel = new VisTable(true);
        scoreLabel = new VisLabel("Final Score: -");
        statsLabel = new VisLabel("Statistics: -");
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadClubs();
    }
    
    private void setupListeners() {
        // Home team selection
        homeTeamSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Club selected = homeTeamSelectBox.getSelected();
                updateTeamInfo(selected, true);
                updateStadiumOptions(selected);
            }
        });
        
        // Away team selection
        awayTeamSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Club selected = awayTeamSelectBox.getSelected();
                updateTeamInfo(selected, false);
            }
        });
        
        // Stadium selection
        stadiumSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Stadium selected = stadiumSelectBox.getSelected();
                updateStadiumInfo(selected);
            }
        });
        
        // Temperature slider
        temperatureSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int temp = (int)temperatureSlider.getValue();
                temperatureLabel.setText("Temperature: " + temp + "°C");
            }
        });
        
        // Simulate button
        simulateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                simulateMatch();
            }
        });
        
        // Reset button
        resetButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetForm();
            }
        });
    }
    
    private void loadClubs() {
        // Get all clubs from game
        java.util.List<Club> allClubs = currentGame.getAllClubs();
        
        if (allClubs != null && !allClubs.isEmpty()) {
            homeTeamSelectBox.setItems(allClubs.toArray(new Club[0]));
            awayTeamSelectBox.setItems(allClubs.toArray(new Club[0]));
            
            // Set custom renderer for club names
            homeTeamSelectBox.setItemToStringFunction(club -> club.getName());
            awayTeamSelectBox.setItemToStringFunction(club -> club.getName());
            
            // Select first club by default
            if (allClubs.size() > 0) {
                homeTeamSelectBox.setSelected(allClubs.get(0));
                if (allClubs.size() > 1) {
                    awayTeamSelectBox.setSelected(allClubs.get(1));
                }
            }
        }
    }
    
    private void updateTeamInfo(Club club, boolean isHome) {
        if (club == null) return;
        
        // Calculate team strength (simplified)
        float strength = calculateTeamStrength(club);
        int playerCount = club.getPlayers() != null ? club.getPlayers().size() : 0;
        String stadiumName = club.getStadium() != null ? club.getStadium().getName() : "None";
        
        String info = String.format(
            "Strength: %d | Players: %d | Stadium: %s",
            (int)strength, playerCount, stadiumName
        );
        
        if (isHome) {
            homeTeamInfoLabel.setText(info);
        } else {
            awayTeamInfoLabel.setText(info);
        }
    }
    
    private float calculateTeamStrength(Club club) {
        // Simplified strength calculation
        // TODO: Use proper match engine strength calculation
        if (club.getPlayers() == null || club.getPlayers().isEmpty()) {
            return 50.0f;
        }
        
        float totalStrength = 0.0f;
        int count = 0;
        
        for (com.rndmodgames.futtoboru.data.Player player : club.getPlayers()) {
            // Simple average of some attributes
            // TODO: Use proper attribute calculation
            totalStrength += 50.0f; // Placeholder
            count++;
        }
        
        return count > 0 ? totalStrength / count : 50.0f;
    }
    
    private void updateStadiumOptions(Club homeTeam) {
        if (homeTeam == null || homeTeam.getStadium() == null) {
            return;
        }
        
        Stadium homeStadium = homeTeam.getStadium();
        stadiumSelectBox.setItems(homeStadium);
        stadiumSelectBox.setSelected(homeStadium);
        updateStadiumInfo(homeStadium);
    }
    
    private void updateStadiumInfo(Stadium stadium) {
        if (stadium == null) return;
        
        int capacity = stadium.getCapacity() != null ? stadium.getCapacity() : 0;
        String info = String.format("Capacity: %d", capacity);
        stadiumInfoLabel.setText(info);
    }
    
    private void setupLayout() {
        // Title
        VisLabel title = new VisLabel("Match Engine Test");
        title.setFontScale(1.5f);
        add(title).colspan(2).padBottom(10);
        row();
        
        // Left panel - Match setup
        VisTable leftPanel = createMatchSetupPanel();
        add(leftPanel).width(400).top().left();
        
        // Right panel - Match view
        VisTable rightPanel = createMatchViewPanel();
        add(rightPanel).expand().fill().top().left();
        row();
        
        // Controls panel
        VisTable controlsPanel = createControlsPanel();
        add(controlsPanel).colspan(2).fillX().padTop(10);
        row();
        
        // Result panel
        matchResultPanel.add(scoreLabel).left();
        matchResultPanel.row();
        matchResultPanel.add(statsLabel).left();
        matchResultPanel.setVisible(false);
        add(matchResultPanel).colspan(2).expand().fill().padTop(10);
    }
    
    private VisTable createMatchSetupPanel() {
        VisTable panel = new VisTable(true);
        panel.pad(10);
        
        // Home team
        panel.add(new VisLabel("Home Team:")).left();
        panel.row();
        panel.add(homeTeamSelectBox).fillX();
        panel.row();
        panel.add(homeTeamInfoLabel).left();
        panel.row().padTop(5);
        
        // Away team
        panel.add(new VisLabel("Away Team:")).left();
        panel.row();
        panel.add(awayTeamSelectBox).fillX();
        panel.row();
        panel.add(awayTeamInfoLabel).left();
        panel.row().padTop(10);
        
        // Stadium
        panel.add(new VisLabel("Stadium:")).left();
        panel.row();
        panel.add(stadiumSelectBox).fillX();
        panel.row();
        panel.add(stadiumInfoLabel).left();
        panel.row().padTop(10);
        
        // Weather
        panel.add(new VisLabel("Weather:")).left();
        panel.row();
        panel.add(weatherSelectBox).fillX();
        panel.row().padTop(5);
        
        // Pitch condition
        panel.add(new VisLabel("Pitch Condition:")).left();
        panel.row();
        panel.add(pitchConditionSelectBox).fillX();
        panel.row().padTop(5);
        
        // Temperature
        panel.add(new VisLabel("Temperature:")).left();
        panel.row();
        panel.add(temperatureSlider).fillX();
        panel.row();
        panel.add(temperatureLabel).left();
        
        return panel;
    }
    
    private VisTable createMatchViewPanel() {
        VisTable panel = new VisTable(true);
        panel.pad(10);
        
        panel.add(new VisLabel("Match View")).left();
        panel.row();
        panel.add(matchViewScrollPane).expand().fill();
        
        return panel;
    }
    
    private VisTable createControlsPanel() {
        VisTable panel = new VisTable(true);
        
        VisTable buttonRow = new VisTable(true);
        buttonRow.add(simulateButton).padRight(5);
        buttonRow.add(resetButton);
        
        panel.add(buttonRow).left();
        
        return panel;
    }
    
    private void simulateMatch() {
        // Get selected conditions
        Club homeTeam = homeTeamSelectBox.getSelected();
        Club awayTeam = awayTeamSelectBox.getSelected();
        
        if (homeTeam == null || awayTeam == null) {
            matchCommentaryLabel.setText("ERROR: Please select both teams!");
            return;
        }
        
        // Create match
        com.rndmodgames.futtoboru.data.Match match = new com.rndmodgames.futtoboru.data.Match();
        match.setHomeClubId(homeTeam.getId());
        match.setAwayClubId(awayTeam.getId());
        match.setMatchType(com.rndmodgames.futtoboru.data.Match.FRIENDLY_MATCH);
        
        // Simulate match using existing MatchSimulator
        com.rndmodgames.futtoboru.engine.simulation.MatchSimulator simulator = 
            new com.rndmodgames.futtoboru.engine.simulation.MatchSimulator((Futtoboru)game);
        
        boolean success = simulator.simulateMatch(match);
        
        if (success) {
            // Display results
            String commentary = String.format(
                "Match Simulated!\n\n" +
                "Final Score: %s %d - %d %s\n\n" +
                "Match completed successfully.",
                homeTeam.getName(),
                match.getHomeGoals(),
                match.getAwayGoals(),
                awayTeam.getName()
            );
            
            matchCommentaryLabel.setText(commentary);
            
            // Display result panel
            scoreLabel.setText(String.format(
                "Final Score: %s %d - %d %s",
                homeTeam.getName(),
                match.getHomeGoals(),
                match.getAwayGoals(),
                awayTeam.getName()
            ));
            
            statsLabel.setText("Match simulated successfully.");
            matchResultPanel.setVisible(true);
        } else {
            matchCommentaryLabel.setText("ERROR: Failed to simulate match!");
        }
    }
    
    private void resetForm() {
        // Reset all selections to defaults
        loadClubs();
        weatherSelectBox.setSelected("Sunny");
        pitchConditionSelectBox.setSelected("Good");
        temperatureSlider.setValue(15);
        temperatureLabel.setText("Temperature: 15°C");
        
        matchCommentaryLabel.setText("Match commentary will appear here...");
        matchResultPanel.setVisible(false);
    }
    
    public void updateDynamicComponents() {
        // Refresh club lists if needed
        loadClubs();
    }
}
```

---

## Step 4: Integrate with MainMenuManager (30 minutes)

**File:** `MainMenuManager.java`

**4.1 Add Screen Table Instance:**
```java
// Add to screen table declarations (around line 158)
private com.rndmodgames.futtoboru.tables.match.engine.MatchEngineTestScreenTable matchEngineTestScreenTable = null;
```

**4.2 Initialize Screen Table:**
```java
// Add to constructor after other screen tables (around line 223)
matchEngineTestScreenTable = new com.rndmodgames.futtoboru.tables.match.engine.MatchEngineTestScreenTable(game);
```

**4.3 Create Button:**
```java
// Add to button declarations (around line 124)
private VisTextButton matchEngineButton = null;
```

**4.4 Initialize Button:**
```java
// Add after other button initializations (around line 249)
matchEngineButton = new com.rndmodgames.futtoboru.menu.buttons.MatchEngineButton(this);
```

**4.5 Add Button to Menu:**
```java
// Add to setDynamicButtonsMenu() method
// Add for BOTH employed and unemployed (always visible for testing)

// In unemployed section (around line 293):
buttonsMenu.add(matchEngineButton).fill();
buttonsMenu.row();

// In manager section (around line 359):
buttonsMenu.add(matchEngineButton).fill();
buttonsMenu.row();
```

**4.6 Add Screen Case:**
```java
// Add to setActiveMainScreen() switch statement (around line 857)
case MATCH_ENGINE_TEST_SCREEN:
    matchEngineTestScreenTable.updateDynamicComponents();
    parentTable.add(matchEngineTestScreenTable).grow();
    break;
```

---

## Step 5: Test (30 minutes)

1. **Start Game:**
   - Load or start a game
   - Navigate to main menu

2. **Access Match Engine:**
   - Click "Match Engine" button
   - Verify screen loads

3. **Test Team Selection:**
   - Select home team
   - Select away team
   - Verify team info displays

4. **Test Stadium Selection:**
   - Verify stadium updates when home team changes
   - Verify stadium info displays

5. **Test Environmental Conditions:**
   - Change weather
   - Change pitch condition
   - Change temperature
   - Verify labels update

6. **Test Match Simulation:**
   - Click "Simulate Match"
   - Verify match simulates
   - Verify results display

7. **Test Reset:**
   - Click "Reset"
   - Verify form resets

---

## Quick Checklist

- [ ] Add `MATCH_ENGINE_TEST_SCREEN` constant
- [ ] Create `MatchEngineButton.java`
- [ ] Create `MatchEngineTestScreenTable.java`
- [ ] Add screen table to `MainMenuManager`
- [ ] Add button to `MainMenuManager`
- [ ] Add button to menu (both employed/unemployed)
- [ ] Add screen case to `setActiveMainScreen()`
- [ ] Test team selection
- [ ] Test stadium selection
- [ ] Test environmental conditions
- [ ] Test match simulation
- [ ] Test reset functionality

---

## Estimated Time

- **Step 1:** 5 minutes
- **Step 2:** 15 minutes
- **Step 3:** 2-3 hours (basic version)
- **Step 4:** 30 minutes
- **Step 5:** 30 minutes

**Total:** 3-4 hours for basic working version

---

## Next Steps After Basic Version

1. **Enhance Team Info Display:**
   - Show more detailed team information
   - Show player list
   - Show formation

2. **Add More Conditions:**
   - Match type selection
   - Match importance
   - Referee selection
   - Travel distance

3. **Enhance Match Display:**
   - Better commentary display
   - Match statistics
   - Player performances

4. **Add Advanced Options:**
   - Team morale override
   - Player condition override
   - Random seed control

5. **Integrate 2D Visualization:**
   - When 2D engine available
   - Add view mode selector
   - Switch between text and 2D

---

**Status:** Ready for Implementation  
**Start:** Step 1 - Add Screen Constant


