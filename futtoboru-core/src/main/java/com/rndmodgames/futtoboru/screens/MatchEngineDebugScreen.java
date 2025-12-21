package com.rndmodgames.futtoboru.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.Stadium;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.match.engine.MatchConfiguration;
import com.rndmodgames.futtoboru.match.engine.MatchEngineVersion;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.EngineParameters;
import com.rndmodgames.futtoboru.system.SaveGame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Match Engine Debug Screen - Prototype v1.0
 * 
 * Comprehensive match configuration screen for testing the 2D match engine.
 * All components are implemented as placeholders ready for data integration.
 * 
 * Features:
 * - Match type selection (Friendly/League/Cup/Custom)
 * - Team selection (Home/Away)
 * - Venue & date configuration
 * - Match conditions (weather, temperature, pitch, attendance)
 * - Advanced options (collapsible)
 * - Match preview panel
 * 
 * @author Geomancer86
 */
public class MatchEngineDebugScreen implements Screen {

    Game game;
    Stage stage;
    Futtoboru futtoboru;
    SaveGame currentGame;
    
    // Main containers
    VisTable mainTable;
    VisScrollPane scrollPane;
    VisTable contentTable;
    
    // Step 1: Match Type
    VisSelectBox<String> matchTypeSelectBox;
    
    // Step 2: Teams
    VisSelectBox<Club> homeTeamSelectBox;
    VisSelectBox<Club> awayTeamSelectBox;
    VisTextButton swapTeamsButton;
    
    // Step 3: Venue & Date
    VisSelectBox<Stadium> stadiumSelectBox;
    VisLabel stadiumInfoLabel;
    VisSelectBox<Season> seasonSelectBox;
    VisLabel dateDisplayLabel;
    VisTextButton datePrevWeekButton;
    VisTextButton datePrevDayButton;
    VisTextButton dateNextDayButton;
    VisTextButton dateNextWeekButton;
    VisTextButton dateSetTodayButton;
    VisSelectBox<String> timeSelectBox;
    VisTable timeHelpTable;
    VisLabel timeHelpLabel;
    LocalDateTime selectedMatchDate;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ENGLISH);
    DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    
    // Step 4: Match Conditions
    VisSelectBox<String> weatherSelectBox;
    VisSlider temperatureSlider;
    VisLabel temperatureLabel;
    VisSelectBox<String> pitchConditionSelectBox;
    VisCheckBox attendanceAutoCheckBox;
    VisTextField attendanceManualField;
    VisSelectBox<String> crowdAtmosphereSelectBox;
    
    // Step 5: Advanced Options (Collapsible)
    VisTextButton advancedToggleButton;
    VisTable advancedContentTable;
    boolean advancedExpanded = false;
    
    // Advanced: Player Conditions
    VisSlider homeTeamConditionSlider;
    VisLabel homeTeamConditionLabel;
    VisSlider awayTeamConditionSlider;
    VisLabel awayTeamConditionLabel;
    
    // Advanced: Team Bonuses
    VisSelectBox<String> homeAdvantageSelectBox;
    VisSelectBox<String> homeFormBonusSelectBox;
    VisSelectBox<String> awayFormBonusSelectBox;
    
    // Match Preview
    VisTable previewTable;
    VisLabel previewContentLabel;
    
    // Action Buttons
    VisTextButton startMatchButton;
    VisTextButton resetButton;
    VisTextButton cancelButton;
    
    /**
     * Constructor - Builds the complete prototype UI
     */
    public MatchEngineDebugScreen(Game parent) {
        
        this.game = parent;
        this.futtoboru = (Futtoboru) parent;
        this.currentGame = futtoboru != null ? futtoboru.getCurrentGame() : null;
        
        stage = new Stage(new ScreenViewport());
        
        // Main table (fills screen)
        mainTable = new VisTable(true);
        mainTable.setFillParent(true);
        mainTable.pad(20);
        
        // Header section
        buildHeaderSection();
        
        // Scrollable content
        contentTable = new VisTable(true);
        contentTable.pad(20);
        
        // Build all sections
        buildMatchTypeSection();
        buildTeamSelectionSection();
        buildVenueDateSection();
        buildConditionsSection();
        buildAdvancedOptionsSection();
        buildMatchPreviewSection();
        
        // Create scroll pane for content
        scrollPane = new VisScrollPane(contentTable);
        scrollPane.setFadeScrollBars(false);
        
        mainTable.row();
        mainTable.add(scrollPane).grow().fill();
        
        // Footer: Action buttons
        buildActionButtons();
        
        // Build info at bottom
        mainTable.row();
        VisLabel buildInfoLabel = new VisLabel(MatchEngineVersion.getBuildInfoString());
        buildInfoLabel.setColor(0.7f, 0.7f, 0.7f, 1f);
        mainTable.add(buildInfoLabel).left().bottom().padLeft(10).padBottom(10);
        
        stage.addActor(mainTable);
    }
    
    /**
     * Build header section with title and back button
     */
    private void buildHeaderSection() {
        VisTable headerTable = new VisTable(true);
        
        // Title
        VisLabel titleLabel = new VisLabel("Match Engine Debug");
        titleLabel.setFontScale(1.5f);
        headerTable.add(titleLabel).left().expandX();
        
        // Version info
        VisLabel versionLabel = new VisLabel(MatchEngineVersion.getVersionString());
        versionLabel.setFontScale(0.9f);
        headerTable.add(versionLabel).right().padLeft(20);
        
        // Back button
        VisTextButton backButton = new VisTextButton("Back");
        backButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (backButton.isPressed()) {
                    ((Futtoboru)game).changeScreen(Futtoboru.MENU_SCREEN);
                }
            }
        });
        headerTable.add(backButton).right().padLeft(10);
        
        mainTable.add(headerTable).growX().fillX().padBottom(10);
    }
    
    /**
     * Step 1: Match Type Selection
     */
    private void buildMatchTypeSection() {
        contentTable.row();
        contentTable.addSeparator().colspan(3).pad(10);
        contentTable.row();
        
        VisLabel sectionLabel = new VisLabel("STEP 1: Match Type");
        sectionLabel.setFontScale(1.2f);
        contentTable.add(sectionLabel).colspan(3).left().padBottom(10);
        contentTable.row();
        
        VisLabel matchTypeLabel = new VisLabel("Match Type:");
        contentTable.add(matchTypeLabel).left().width(150);
        
        matchTypeSelectBox = new VisSelectBox<>();
        // Use real match types from EngineParameters, plus additional options for debug
        List<String> matchTypes = new ArrayList<>();
        for (String type : EngineParameters.matchTypes) {
            matchTypes.add(type);
        }
        matchTypes.add("Cup Match");
        matchTypes.add("Custom");
        matchTypeSelectBox.setItems(matchTypes.toArray(new String[matchTypes.size()]));
        matchTypeSelectBox.setSelectedIndex(0); // Default: Friendly Match
        matchTypeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMatchPreview();
            }
        });
        contentTable.add(matchTypeSelectBox).fillX().colspan(2);
        contentTable.row();
    }
    
    /**
     * Step 2: Team Selection
     */
    private void buildTeamSelectionSection() {
        contentTable.row();
        contentTable.addSeparator().colspan(3).pad(10);
        contentTable.row();
        
        VisLabel sectionLabel = new VisLabel("STEP 2: Teams");
        sectionLabel.setFontScale(1.2f);
        contentTable.add(sectionLabel).colspan(3).left().padBottom(10);
        contentTable.row();
        
        // Load available clubs
        List<Club> availableClubs = loadAvailableClubs();
        
        // Home Team
        VisLabel homeLabel = new VisLabel("Home Team:");
        contentTable.add(homeLabel).left().width(150);
        
        homeTeamSelectBox = new VisSelectBox<>();
        if (!availableClubs.isEmpty()) {
            homeTeamSelectBox.setItems(availableClubs.toArray(new Club[availableClubs.size()]));
        } else {
            // Fallback placeholder
            Club placeholder1 = new Club("Team A (No Data)");
            Club placeholder2 = new Club("Team B (No Data)");
            homeTeamSelectBox.setItems(placeholder1, placeholder2);
        }
        homeTeamSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateStadiumOptions();
                updateMatchPreview();
            }
        });
        contentTable.add(homeTeamSelectBox).fillX();
        contentTable.row();
        
        // Away Team
        VisLabel awayLabel = new VisLabel("Away Team:");
        contentTable.add(awayLabel).left().width(150);
        
        awayTeamSelectBox = new VisSelectBox<>();
        if (!availableClubs.isEmpty()) {
            awayTeamSelectBox.setItems(availableClubs.toArray(new Club[availableClubs.size()]));
            // Set different default if possible
            if (availableClubs.size() > 1) {
                awayTeamSelectBox.setSelectedIndex(1);
            }
        } else {
            Club placeholder1 = new Club("Team A (No Data)");
            Club placeholder2 = new Club("Team B (No Data)");
            awayTeamSelectBox.setItems(placeholder1, placeholder2);
        }
        awayTeamSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMatchPreview();
            }
        });
        contentTable.add(awayTeamSelectBox).fillX();
        contentTable.row();
        
        // Swap Teams Button
        swapTeamsButton = new VisTextButton("Swap Teams");
        swapTeamsButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (swapTeamsButton.isPressed()) {
                    Club home = homeTeamSelectBox.getSelected();
                    Club away = awayTeamSelectBox.getSelected();
                    if (home != null && away != null) {
                        homeTeamSelectBox.setSelected(away);
                        awayTeamSelectBox.setSelected(home);
                        updateStadiumOptions();
                        updateMatchPreview();
                    }
                }
            }
        });
        contentTable.add(swapTeamsButton).colspan(3).padTop(5);
        contentTable.row();
    }
    
    /**
     * Load available clubs from SaveGame or DatabaseLoader
     */
    private List<Club> loadAvailableClubs() {
        List<Club> clubs = new ArrayList<>();
        
        // Try SaveGame first (preferred - has game state)
        if (currentGame != null && currentGame.getAllClubs() != null && !currentGame.getAllClubs().isEmpty()) {
            clubs.addAll(currentGame.getAllClubs());
            Gdx.app.log("MatchEngineDebug", "Loaded " + clubs.size() + " clubs from SaveGame");
        } else {
            // Fallback to DatabaseLoader (static data)
            try {
                if (DatabaseLoader.getInstance() != null) {
                    // Try to get clubs from DatabaseLoader
                    // DatabaseLoader doesn't expose getAllClubs directly, but we can get by country
                    // For debug screen, we'll try to get some clubs
                    java.util.Map<Long, List<Club>> clubsByCountry = DatabaseLoader.getClubsByCountry();
                    if (clubsByCountry != null && !clubsByCountry.isEmpty()) {
                        for (List<Club> countryClubs : clubsByCountry.values()) {
                            if (countryClubs != null) {
                                clubs.addAll(countryClubs);
                            }
                        }
                        Gdx.app.log("MatchEngineDebug", "Loaded " + clubs.size() + " clubs from DatabaseLoader");
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("MatchEngineDebug", "Error loading clubs from DatabaseLoader: " + e.getMessage());
            }
        }
        
        return clubs;
    }
    
    /**
     * Step 3: Venue & Date
     */
    private void buildVenueDateSection() {
        contentTable.row();
        contentTable.addSeparator().colspan(3).pad(10);
        contentTable.row();
        
        VisLabel sectionLabel = new VisLabel("STEP 3: Venue & Date");
        sectionLabel.setFontScale(1.2f);
        contentTable.add(sectionLabel).colspan(3).left().padBottom(10);
        contentTable.row();
        
        // Season
        VisLabel seasonLabel = new VisLabel("Season:");
        contentTable.add(seasonLabel).left().width(150);
        
        seasonSelectBox = new VisSelectBox<>();
        List<Season> availableSeasons = loadAvailableSeasons();
        if (!availableSeasons.isEmpty()) {
            seasonSelectBox.setItems(availableSeasons.toArray(new Season[availableSeasons.size()]));
            // Default to first season
            seasonSelectBox.setSelectedIndex(0);
        } else {
            seasonSelectBox.setItems(new Season[]{}); // Empty
        }
        seasonSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Update date to season start date when season changes
                Season selectedSeason = seasonSelectBox.getSelected();
                if (selectedSeason != null && selectedSeason.getStartDate() != null) {
                    selectedMatchDate = selectedSeason.getStartDate();
                    updateDateDisplay();
                }
                updateTimeHelp();
                updateMatchPreview();
            }
        });
        contentTable.add(seasonSelectBox).fillX().colspan(2);
        contentTable.row();
        
        // Stadium
        VisLabel stadiumLabel = new VisLabel("Stadium:");
        contentTable.add(stadiumLabel).left().width(150);
        
        stadiumSelectBox = new VisSelectBox<>();
        // Will be populated when home team is selected
        stadiumSelectBox.setItems(new Stadium[]{}); // Empty initially
        stadiumSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateStadiumInfo();
                updateMatchPreview();
            }
        });
        contentTable.add(stadiumSelectBox).fillX().colspan(2);
        contentTable.row();
        
        // Stadium Info (shows capacity, etc.)
        stadiumInfoLabel = new VisLabel("");
        stadiumInfoLabel.setColor(0.7f, 0.7f, 0.7f, 1f);
        stadiumInfoLabel.setFontScale(0.9f);
        contentTable.add(new VisLabel("")).left().width(150); // Spacer
        contentTable.add(stadiumInfoLabel).left().colspan(2);
        contentTable.row();
        
        // Date Picker
        VisLabel dateLabelLabel = new VisLabel("Date:");
        contentTable.add(dateLabelLabel).left().width(150);
        
        // Initialize date from season or game or use season start
        Season defaultSeason = seasonSelectBox.getSelected();
        if (defaultSeason != null && defaultSeason.getStartDate() != null) {
            selectedMatchDate = defaultSeason.getStartDate();
        } else if (currentGame != null && currentGame.getGameDate() != null) {
            selectedMatchDate = currentGame.getGameDate();
        } else {
            // Fallback: use a reasonable default (1888 season start)
            selectedMatchDate = LocalDateTime.of(1888, 4, 1, 12, 0);
        }
        
        // Clean date picker with navigation buttons
        VisTable datePickerTable = new VisTable(true);
        
        // Date display (large, readable)
        dateDisplayLabel = new VisLabel(dateFormatter.format(selectedMatchDate));
        dateDisplayLabel.setFontScale(1.1f);
        dateDisplayLabel.setColor(1f, 1f, 1f, 1f);
        datePickerTable.add(dateDisplayLabel).colspan(5).padBottom(5);
        datePickerTable.row();
        
        // Navigation buttons row
        VisTable navTable = new VisTable(true);
        
        // Previous week
        datePrevWeekButton = new VisTextButton("◄◄ Week");
        datePrevWeekButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (datePrevWeekButton.isPressed()) {
                    selectedMatchDate = selectedMatchDate.minusWeeks(1);
                    updateDateDisplay();
                    updateMatchPreview();
                }
            }
        });
        navTable.add(datePrevWeekButton).padRight(5);
        
        // Previous day
        datePrevDayButton = new VisTextButton("◄ Day");
        datePrevDayButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (datePrevDayButton.isPressed()) {
                    selectedMatchDate = selectedMatchDate.minusDays(1);
                    updateDateDisplay();
                    updateMatchPreview();
                }
            }
        });
        navTable.add(datePrevDayButton).padRight(5);
        
        // Set to season start / today button
        dateSetTodayButton = new VisTextButton("Reset");
        dateSetTodayButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (dateSetTodayButton.isPressed()) {
                    // Reset to season start date or game date
                    Season season = seasonSelectBox.getSelected();
                    if (season != null && season.getStartDate() != null) {
                        selectedMatchDate = season.getStartDate();
                    } else if (currentGame != null && currentGame.getGameDate() != null) {
                        selectedMatchDate = currentGame.getGameDate();
                    }
                    updateDateDisplay();
                    updateMatchPreview();
                }
            }
        });
        navTable.add(dateSetTodayButton).padRight(5);
        
        // Next day
        dateNextDayButton = new VisTextButton("Day ►");
        dateNextDayButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (dateNextDayButton.isPressed()) {
                    selectedMatchDate = selectedMatchDate.plusDays(1);
                    updateDateDisplay();
                    updateMatchPreview();
                }
            }
        });
        navTable.add(dateNextDayButton).padRight(5);
        
        // Next week
        dateNextWeekButton = new VisTextButton("Week ►►");
        dateNextWeekButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (dateNextWeekButton.isPressed()) {
                    selectedMatchDate = selectedMatchDate.plusWeeks(1);
                    updateDateDisplay();
                    updateMatchPreview();
                }
            }
        });
        navTable.add(dateNextWeekButton);
        
        datePickerTable.add(navTable).colspan(5).padTop(5);
        
        contentTable.add(datePickerTable).left().colspan(2).padTop(5);
        contentTable.row();
        
        // Time
        VisLabel timeLabel = new VisLabel("Time:");
        contentTable.add(timeLabel).left().width(150);
        
        VisTable timeTable = new VisTable(true);
        timeSelectBox = new VisSelectBox<>();
        // Expanded time options
        timeSelectBox.setItems(
            "Early Morning (08:00)",
            "Morning (10:00)",
            "Midday (12:00)",
            "Afternoon (15:00)",
            "Late Afternoon (17:00)",
            "Evening (19:00)",
            "Night (21:00)",
            "Late Night (22:30)"
        );
        timeSelectBox.setSelectedIndex(3); // Default: Afternoon (15:00)
        timeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateTimeHelp();
                updateMatchPreview();
            }
        });
        timeTable.add(timeSelectBox).fillX();
        contentTable.add(timeTable).fillX().colspan(2);
        contentTable.row();
        
        // Time/Season Help Box
        timeHelpTable = new VisTable(true);
        timeHelpTable.pad(10);
        timeHelpLabel = new VisLabel("");
        timeHelpLabel.setWrap(true);
        timeHelpLabel.setColor(0.8f, 0.8f, 0.6f, 1f); // Light yellow for help text
        timeHelpTable.add(timeHelpLabel).width(500).left();
        contentTable.add(new VisLabel("")).left().width(150); // Spacer
        contentTable.add(timeHelpTable).left().colspan(2).padTop(5);
        contentTable.row();
        
        // Initialize help text
        updateTimeHelp();
    }
    
    /**
     * Load available seasons from SaveGame or DatabaseLoader
     */
    private List<Season> loadAvailableSeasons() {
        List<Season> seasons = new ArrayList<>();
        
        // Try DatabaseLoader first (has all available seasons)
        try {
            if (DatabaseLoader.getInstance() != null) {
                List<Season> dbSeasons = DatabaseLoader.getInstance().getSeasons();
                if (dbSeasons != null && !dbSeasons.isEmpty()) {
                    seasons.addAll(dbSeasons);
                    Gdx.app.log("MatchEngineDebug", "Loaded " + seasons.size() + " seasons from DatabaseLoader");
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MatchEngineDebug", "Error loading seasons from DatabaseLoader: " + e.getMessage());
        }
        
        // If no seasons found, create a placeholder
        if (seasons.isEmpty()) {
            Season placeholder = new Season();
            placeholder.setId(18L);
            placeholder.setName("1888-1889 Season");
            seasons.add(placeholder);
            Gdx.app.log("MatchEngineDebug", "Using placeholder season");
        }
        
        return seasons;
    }
    
    /**
     * Update date display label
     */
    private void updateDateDisplay() {
        if (dateDisplayLabel != null && selectedMatchDate != null) {
            dateDisplayLabel.setText(dateFormatter.format(selectedMatchDate));
        }
    }
    
    /**
     * Update time/season help box with bonuses and penalties
     */
    private void updateTimeHelp() {
        Season season = seasonSelectBox.getSelected();
        String timeSelected = timeSelectBox.getSelected();
        
        if (season == null || timeSelected == null) {
            timeHelpLabel.setText("Select season and time to see match conditions and bonuses.");
            return;
        }
        
        StringBuilder help = new StringBuilder();
        help.append("Match Conditions:\n");
        
        // Determine season year (for artificial lighting check)
        int seasonYear = 1888; // Default
        if (season.getStartDate() != null) {
            seasonYear = season.getStartDate().getYear();
        }
        
        // Check if artificial lighting is available
        boolean hasArtificialLight = seasonYear >= 1950; // Rough estimate - artificial lighting became common in 1950s
        
        // Determine time of day
        boolean isEvening = timeSelected.contains("Evening") || timeSelected.contains("Night") || timeSelected.contains("Late Night");
        boolean isEarlyMorning = timeSelected.contains("Early Morning");
        boolean isAfternoon = timeSelected.contains("Afternoon") || timeSelected.contains("Midday");
        
        // Artificial lighting check
        if (isEvening && !hasArtificialLight) {
            help.append("⚠ WARNING: No artificial lighting available in ").append(seasonYear).append("!\n");
            help.append("Evening/Night matches may have visibility penalties.\n");
            help.append("Consider scheduling for afternoon (15:00-17:00).\n\n");
        } else if (isEvening && hasArtificialLight) {
            help.append("✓ Artificial lighting available (modern era).\n");
            help.append("Evening matches have normal visibility.\n\n");
        }
        
        // Time-based bonuses/penalties
        help.append("Time Effects:\n");
        if (isEarlyMorning) {
            help.append("• Early Morning (08:00): Lower attendance (-10%), cooler temperature\n");
        } else if (isAfternoon) {
            help.append("• Afternoon (12:00-17:00): Optimal conditions, highest attendance potential\n");
        } else if (isEvening) {
            if (hasArtificialLight) {
                help.append("• Evening/Night: Good attendance, modern lighting\n");
            } else {
                help.append("• Evening/Night: Reduced visibility, lower attendance (-15%)\n");
            }
        }
        
        // Season-based effects
        if (seasonYear < 1900) {
            help.append("\nSeason Era: Early Football (1888-1899)\n");
            help.append("• No artificial lighting\n");
            help.append("• Matches typically scheduled 15:00-16:00\n");
            help.append("• Basic pitch conditions\n");
        } else if (seasonYear < 1950) {
            help.append("\nSeason Era: Classic Era (1900-1949)\n");
            help.append("• Limited artificial lighting (rare)\n");
            help.append("• Afternoon matches preferred\n");
        } else {
            help.append("\nSeason Era: Modern Era (1950+)\n");
            help.append("• Artificial lighting available\n");
            help.append("• Evening matches common\n");
            help.append("• Better pitch maintenance\n");
        }
        
        timeHelpLabel.setText(help.toString());
    }
    
    /**
     * Update stadium options based on selected home team
     */
    private void updateStadiumOptions() {
        Club homeTeam = homeTeamSelectBox.getSelected();
        List<Stadium> stadiums = new ArrayList<>();
        
        // Add "Auto (Home Team)" option as first item
        Stadium autoStadium = new Stadium();
        autoStadium.setName("Auto (Home Team)");
        stadiums.add(autoStadium);
        
        if (homeTeam != null && homeTeam.getStadium() != null) {
            // Add home team's stadium
            stadiums.add(homeTeam.getStadium());
            
            // Try to add other available stadiums from all clubs
            List<Club> allClubs = loadAvailableClubs();
            for (Club club : allClubs) {
                if (club != null && club.getStadium() != null && club != homeTeam) {
                    Stadium stadium = club.getStadium();
                    // Avoid duplicates
                    boolean alreadyAdded = false;
                    for (Stadium existing : stadiums) {
                        if (existing.getId() != null && existing.getId().equals(stadium.getId())) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        stadiums.add(stadium);
                    }
                }
            }
        }
        
        stadiumSelectBox.setItems(stadiums.toArray(new Stadium[stadiums.size()]));
        stadiumSelectBox.setSelectedIndex(0); // Default to Auto
        
        // Update stadium info label
        updateStadiumInfo();
    }
    
    /**
     * Update stadium info display
     */
    private void updateStadiumInfo() {
        Stadium selected = stadiumSelectBox.getSelected();
        if (selected != null && selected.getName() != null) {
            if (selected.getName().equals("Auto (Home Team)")) {
                Club homeTeam = homeTeamSelectBox.getSelected();
                if (homeTeam != null && homeTeam.getStadium() != null) {
                    Stadium homeStadium = homeTeam.getStadium();
                    StringBuilder info = new StringBuilder();
                    info.append("Auto: ").append(homeStadium.getName());
                    if (homeStadium.getCapacity() != null) {
                        info.append(" | Capacity: ").append(String.format("%,d", homeStadium.getCapacity()));
                    }
                    if (homeStadium.getBuiltYear() != null) {
                        info.append(" | Built: ").append(homeStadium.getBuiltYear());
                    }
                    stadiumInfoLabel.setText(info.toString());
                } else {
                    stadiumInfoLabel.setText("Auto: No stadium available for home team");
                }
            } else {
                StringBuilder info = new StringBuilder();
                info.append(selected.getName());
                if (selected.getCapacity() != null) {
                    info.append(" | Capacity: ").append(String.format("%,d", selected.getCapacity()));
                }
                if (selected.getBuiltYear() != null) {
                    info.append(" | Built: ").append(selected.getBuiltYear());
                }
                stadiumInfoLabel.setText(info.toString());
            }
        } else {
            stadiumInfoLabel.setText("");
        }
    }
    
    /**
     * Step 4: Match Conditions
     */
    private void buildConditionsSection() {
        contentTable.row();
        contentTable.addSeparator().colspan(3).pad(10);
        contentTable.row();
        
        VisLabel sectionLabel = new VisLabel("STEP 4: Match Conditions");
        sectionLabel.setFontScale(1.2f);
        contentTable.add(sectionLabel).colspan(3).left().padBottom(10);
        contentTable.row();
        
        // Weather
        VisLabel weatherLabel = new VisLabel("Weather:");
        contentTable.add(weatherLabel).left().width(150);
        
        weatherSelectBox = new VisSelectBox<>();
        weatherSelectBox.setItems("Sunny", "Cloudy", "Rain", "Snow", "Windy", "Fog");
        weatherSelectBox.setSelectedIndex(0);
        weatherSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMatchPreview();
            }
        });
        contentTable.add(weatherSelectBox).fillX().colspan(2);
        contentTable.row();
        
        // Temperature
        VisLabel tempLabel = new VisLabel("Temperature:");
        contentTable.add(tempLabel).left().width(150);
        
        VisTable tempTable = new VisTable(true);
        temperatureSlider = new VisSlider(-5, 35, 1, false);
        temperatureSlider.setValue(15); // Default: 15°C
        temperatureLabel = new VisLabel("15°C");
        temperatureSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int temp = (int) temperatureSlider.getValue();
                temperatureLabel.setText(temp + "°C");
                updateMatchPreview();
            }
        });
        tempTable.add(temperatureSlider).fillX();
        tempTable.add(temperatureLabel).padLeft(10);
        contentTable.add(tempTable).fillX().colspan(2);
        contentTable.row();
        
        // Pitch Condition
        VisLabel pitchLabel = new VisLabel("Pitch Condition:");
        contentTable.add(pitchLabel).left().width(150);
        
        pitchConditionSelectBox = new VisSelectBox<>();
        pitchConditionSelectBox.setItems("Poor", "Fair", "Good", "Excellent");
        pitchConditionSelectBox.setSelectedIndex(2); // Default: Good
        pitchConditionSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMatchPreview();
            }
        });
        contentTable.add(pitchConditionSelectBox).fillX().colspan(2);
        contentTable.row();
        
        // Attendance
        VisLabel attendanceLabel = new VisLabel("Attendance:");
        contentTable.add(attendanceLabel).left().width(150);
        
        VisTable attendanceTable = new VisTable(true);
        attendanceAutoCheckBox = new VisCheckBox("Auto");
        attendanceAutoCheckBox.setChecked(true);
        attendanceManualField = new VisTextField("5000");
        attendanceManualField.setDisabled(true); // Disabled when auto is checked
        attendanceAutoCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                attendanceManualField.setDisabled(attendanceAutoCheckBox.isChecked());
                updateMatchPreview();
            }
        });
        attendanceTable.add(attendanceAutoCheckBox);
        attendanceTable.add(new VisLabel("Manual:")).padLeft(10);
        attendanceTable.add(attendanceManualField).width(100).padLeft(5);
        contentTable.add(attendanceTable).left().colspan(2);
        contentTable.row();
        
        // Crowd Atmosphere
        VisLabel atmosphereLabel = new VisLabel("Crowd Atmosphere:");
        contentTable.add(atmosphereLabel).left().width(150);
        
        crowdAtmosphereSelectBox = new VisSelectBox<>();
        crowdAtmosphereSelectBox.setItems("Low", "Normal", "High", "Intense");
        crowdAtmosphereSelectBox.setSelectedIndex(1); // Default: Normal
        crowdAtmosphereSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMatchPreview();
            }
        });
        contentTable.add(crowdAtmosphereSelectBox).fillX().colspan(2);
        contentTable.row();
    }
    
    /**
     * Step 5: Advanced Options (Collapsible)
     */
    private void buildAdvancedOptionsSection() {
        contentTable.row();
        contentTable.addSeparator().colspan(3).pad(10);
        contentTable.row();
        
        // Toggle button
        VisTable headerTable = new VisTable(true);
        VisLabel sectionLabel = new VisLabel("STEP 5: Advanced Options");
        sectionLabel.setFontScale(1.2f);
        headerTable.add(sectionLabel).left().expandX();
        
        advancedToggleButton = new VisTextButton("▼ Expand");
        advancedToggleButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (advancedToggleButton.isPressed()) {
                    toggleAdvancedOptions();
                }
            }
        });
        headerTable.add(advancedToggleButton).right();
        contentTable.add(headerTable).colspan(3).fillX();
        contentTable.row();
        
        // Collapsible content
        advancedContentTable = new VisTable(true);
        advancedContentTable.pad(10);
        advancedContentTable.setVisible(false); // Collapsed by default
        
        // Player Conditions
        VisLabel playerCondLabel = new VisLabel("Player Conditions:");
        playerCondLabel.setFontScale(1.1f);
        advancedContentTable.add(playerCondLabel).colspan(3).left().padBottom(5);
        advancedContentTable.row();
        
        // Home Team Condition
        VisLabel homeCondLabel = new VisLabel("Home Team Condition:");
        advancedContentTable.add(homeCondLabel).left().width(200);
        VisTable homeCondTable = new VisTable(true);
        homeTeamConditionSlider = new VisSlider(0, 100, 1, false);
        homeTeamConditionSlider.setValue(100);
        homeTeamConditionLabel = new VisLabel("100%");
        homeTeamConditionSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int value = (int) homeTeamConditionSlider.getValue();
                homeTeamConditionLabel.setText(value + "%");
            }
        });
        homeCondTable.add(homeTeamConditionSlider).fillX();
        homeCondTable.add(homeTeamConditionLabel).padLeft(10);
        advancedContentTable.add(homeCondTable).fillX().colspan(2);
        advancedContentTable.row();
        
        // Away Team Condition
        VisLabel awayCondLabel = new VisLabel("Away Team Condition:");
        advancedContentTable.add(awayCondLabel).left().width(200);
        VisTable awayCondTable = new VisTable(true);
        awayTeamConditionSlider = new VisSlider(0, 100, 1, false);
        awayTeamConditionSlider.setValue(100);
        awayTeamConditionLabel = new VisLabel("100%");
        awayTeamConditionSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int value = (int) awayTeamConditionSlider.getValue();
                awayTeamConditionLabel.setText(value + "%");
            }
        });
        awayCondTable.add(awayTeamConditionSlider).fillX();
        awayCondTable.add(awayTeamConditionLabel).padLeft(10);
        advancedContentTable.add(awayCondTable).fillX().colspan(2);
        advancedContentTable.row();
        
        advancedContentTable.row();
        advancedContentTable.addSeparator().colspan(3).pad(5);
        advancedContentTable.row();
        
        // Team Bonuses
        VisLabel bonusesLabel = new VisLabel("Team Bonuses:");
        bonusesLabel.setFontScale(1.1f);
        advancedContentTable.add(bonusesLabel).colspan(3).left().padBottom(5);
        advancedContentTable.row();
        
        // Home Advantage
        VisLabel homeAdvLabel = new VisLabel("Home Advantage:");
        advancedContentTable.add(homeAdvLabel).left().width(200);
        homeAdvantageSelectBox = new VisSelectBox<>();
        homeAdvantageSelectBox.setItems("None", "Normal", "High", "Intense");
        homeAdvantageSelectBox.setSelectedIndex(1);
        advancedContentTable.add(homeAdvantageSelectBox).fillX().colspan(2);
        advancedContentTable.row();
        
        // Form Bonuses
        VisLabel homeFormLabel = new VisLabel("Home Team Form:");
        advancedContentTable.add(homeFormLabel).left().width(200);
        homeFormBonusSelectBox = new VisSelectBox<>();
        homeFormBonusSelectBox.setItems("None", "Poor Form", "Good Form");
        homeFormBonusSelectBox.setSelectedIndex(0);
        advancedContentTable.add(homeFormBonusSelectBox).fillX().colspan(2);
        advancedContentTable.row();
        
        VisLabel awayFormLabel = new VisLabel("Away Team Form:");
        advancedContentTable.add(awayFormLabel).left().width(200);
        awayFormBonusSelectBox = new VisSelectBox<>();
        awayFormBonusSelectBox.setItems("None", "Poor Form", "Good Form");
        awayFormBonusSelectBox.setSelectedIndex(0);
        advancedContentTable.add(awayFormBonusSelectBox).fillX().colspan(2);
        advancedContentTable.row();
        
        contentTable.add(advancedContentTable).colspan(3).fillX();
        contentTable.row();
    }
    
    /**
     * Toggle advanced options visibility
     */
    private void toggleAdvancedOptions() {
        advancedExpanded = !advancedExpanded;
        advancedContentTable.setVisible(advancedExpanded);
        advancedToggleButton.setText(advancedExpanded ? "▲ Collapse" : "▼ Expand");
    }
    
    /**
     * Match Preview Panel
     */
    private void buildMatchPreviewSection() {
        contentTable.row();
        contentTable.addSeparator().colspan(3).pad(10);
        contentTable.row();
        
        VisLabel sectionLabel = new VisLabel("Match Preview");
        sectionLabel.setFontScale(1.2f);
        contentTable.add(sectionLabel).colspan(3).left().padBottom(10);
        contentTable.row();
        
        previewTable = new VisTable(true);
        previewTable.pad(10);
        previewContentLabel = new VisLabel("Configure match settings to see preview...");
        previewContentLabel.setWrap(true);
        previewTable.add(previewContentLabel).width(600).left();
        
        contentTable.add(previewTable).colspan(3).fillX().pad(10);
        contentTable.row();
    }
    
    /**
     * Update match preview with current selections
     */
    private void updateMatchPreview() {
        StringBuilder preview = new StringBuilder();
        
        preview.append("Match Type: ").append(matchTypeSelectBox.getSelected()).append("\n");
        
        Club home = homeTeamSelectBox.getSelected();
        Club away = awayTeamSelectBox.getSelected();
        if (home != null && away != null) {
            preview.append("Teams: ").append(home.getName()).append(" vs ").append(away.getName()).append("\n");
            
            // Show team info if available
            if (home.getCountry() != null) {
                preview.append("Home: ").append(home.getName());
                if (home.getCountry() != null) {
                    preview.append(" (").append(home.getCountry().getCommonName()).append(")");
                }
                preview.append("\n");
            }
            if (away.getCountry() != null) {
                preview.append("Away: ").append(away.getName());
                if (away.getCountry() != null) {
                    preview.append(" (").append(away.getCountry().getCommonName()).append(")");
                }
                preview.append("\n");
            }
        }
        
        // Stadium info
        Stadium selectedStadium = stadiumSelectBox.getSelected();
        if (selectedStadium != null) {
            if (selectedStadium.getName() != null && selectedStadium.getName().equals("Auto (Home Team)")) {
                if (home != null && home.getStadium() != null) {
                    preview.append("Stadium: ").append(home.getStadium().getName());
                    if (home.getStadium().getCapacity() != null) {
                        preview.append(" (").append(String.format("%,d", home.getStadium().getCapacity())).append(" capacity)");
                    }
                    preview.append("\n");
                } else {
                    preview.append("Stadium: Auto (Home Team - No Stadium)\n");
                }
            } else {
                preview.append("Stadium: ").append(selectedStadium.getName());
                if (selectedStadium.getCapacity() != null) {
                    preview.append(" (").append(String.format("%,d", selectedStadium.getCapacity())).append(" capacity)");
                }
                preview.append("\n");
            }
        }
        
        // Date
        if (selectedMatchDate != null) {
            preview.append("Date: ").append(shortDateFormatter.format(selectedMatchDate)).append("\n");
        }
        preview.append("Time: ").append(timeSelectBox.getSelected()).append("\n");
        preview.append("Weather: ").append(weatherSelectBox.getSelected()).append("\n");
        preview.append("Temperature: ").append((int)temperatureSlider.getValue()).append("°C\n");
        preview.append("Pitch: ").append(pitchConditionSelectBox.getSelected()).append("\n");
        
        if (attendanceAutoCheckBox.isChecked()) {
            preview.append("Attendance: Auto");
            // Calculate estimated attendance if possible
            if (selectedStadium != null && selectedStadium.getCapacity() != null) {
                int capacity = selectedStadium.getCapacity();
                int estimated = (int)(capacity * 0.6); // Rough estimate: 60% capacity
                preview.append(" (Est: ").append(String.format("%,d", estimated)).append(")");
            }
            preview.append("\n");
        } else {
            preview.append("Attendance: ").append(attendanceManualField.getText()).append("\n");
        }
        
        preview.append("Atmosphere: ").append(crowdAtmosphereSelectBox.getSelected());
        
        previewContentLabel.setText(preview.toString());
    }
    
    /**
     * Build action buttons (footer)
     */
    private void buildActionButtons() {
        mainTable.row();
        
        VisTable buttonTable = new VisTable(true);
        
        // Quick Presets (placeholder)
        VisTextButton presetsButton = new VisTextButton("Quick Presets");
        presetsButton.setDisabled(true); // Placeholder
        buttonTable.add(presetsButton).padRight(10);
        
        // Reset
        resetButton = new VisTextButton("Reset");
        resetButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (resetButton.isPressed()) {
                    resetForm();
                }
            }
        });
        buttonTable.add(resetButton).padRight(10);
        
        buttonTable.add().expandX(); // Spacer
        
        // Cancel
        cancelButton = new VisTextButton("Cancel");
        cancelButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (cancelButton.isPressed()) {
                    ((Futtoboru)game).changeScreen(Futtoboru.MENU_SCREEN);
                }
            }
        });
        buttonTable.add(cancelButton).padRight(10);
        
        // Start Match
        startMatchButton = new VisTextButton("Start Match");
        startMatchButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (startMatchButton.isPressed()) {
                    // Validate match configuration
                    Club homeTeam = homeTeamSelectBox.getSelected();
                    Club awayTeam = awayTeamSelectBox.getSelected();
                    
                    if (homeTeam == null || awayTeam == null) {
                        Gdx.app.error("MatchEngineDebug", "Please select both teams");
                        return;
                    }
                    
                    if (homeTeam == awayTeam) {
                        Gdx.app.error("MatchEngineDebug", "Home and away teams must be different");
                        return;
                    }
                    
                    // Create match configuration
                    MatchConfiguration config = new MatchConfiguration();
                    config.homeTeam = homeTeam;
                    config.awayTeam = awayTeam;
                    config.stadium = stadiumSelectBox.getSelected();
                    config.season = seasonSelectBox.getSelected();
                    config.matchDate = selectedMatchDate;
                    config.matchType = matchTypeSelectBox.getSelected();
                    config.timeOfDay = timeSelectBox.getSelected();
                    config.weather = weatherSelectBox.getSelected();
                    config.temperature = (float) temperatureSlider.getValue();
                    config.pitchCondition = pitchConditionSelectBox.getSelected();
                    config.attendance = attendanceAutoCheckBox.isChecked() ? 
                        (config.stadium != null && config.stadium.getCapacity() != null ? 
                            (int)(config.stadium.getCapacity() * 0.6) : 5000) :
                        Integer.parseInt(attendanceManualField.getText());
                    config.crowdAtmosphere = crowdAtmosphereSelectBox.getSelected();
                    config.homeTeamCondition = (float) homeTeamConditionSlider.getValue();
                    config.awayTeamCondition = (float) awayTeamConditionSlider.getValue();
                    config.homeAdvantage = homeAdvantageSelectBox.getSelected();
                    config.homeFormBonus = homeFormBonusSelectBox.getSelected();
                    config.awayFormBonus = awayFormBonusSelectBox.getSelected();
                    
                    // Store config in game instance for match engine to access
                    futtoboru.setMatchConfiguration(config);
                    
                    // Launch match engine screen
                    Gdx.app.log("MatchEngineDebug", "Starting match: " + homeTeam.getName() + " vs " + awayTeam.getName());
                    ((Futtoboru)game).changeScreen(Futtoboru.MATCH_ENGINE_SCREEN);
                }
            }
        });
        // Enable button - validation will be done on click
        startMatchButton.setDisabled(false);
        buttonTable.add(startMatchButton);
        
        mainTable.add(buttonTable).growX().fillX().padTop(10);
    }
    
    /**
     * Reset form to defaults
     */
    private void resetForm() {
        matchTypeSelectBox.setSelectedIndex(0);
        homeTeamSelectBox.setSelectedIndex(0);
        awayTeamSelectBox.setSelectedIndex(0);
        stadiumSelectBox.setSelectedIndex(0);
        timeSelectBox.setSelectedIndex(1);
        weatherSelectBox.setSelectedIndex(0);
        temperatureSlider.setValue(15);
        temperatureLabel.setText("15°C");
        pitchConditionSelectBox.setSelectedIndex(2);
        attendanceAutoCheckBox.setChecked(true);
        attendanceManualField.setText("5000");
        attendanceManualField.setDisabled(true);
        crowdAtmosphereSelectBox.setSelectedIndex(1);
        homeTeamConditionSlider.setValue(100);
        homeTeamConditionLabel.setText("100%");
        awayTeamConditionSlider.setValue(100);
        awayTeamConditionLabel.setText("100%");
        homeAdvantageSelectBox.setSelectedIndex(1);
        homeFormBonusSelectBox.setSelectedIndex(0);
        awayFormBonusSelectBox.setSelectedIndex(0);
        
        if (advancedExpanded) {
            toggleAdvancedOptions();
        }
        
        updateMatchPreview();
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // Initialize stadium options if teams are selected
        if (homeTeamSelectBox.getSelected() != null) {
            updateStadiumOptions();
        }
        updateMatchPreview(); // Initial preview update
    }

    @Override
    public void render(float delta) {
        // Clear blit - Black background to match game style
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    @Override
    public void hide() {
        // Input processor will be set by next screen
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
