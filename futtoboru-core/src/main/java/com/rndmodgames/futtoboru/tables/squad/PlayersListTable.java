package com.rndmodgames.futtoboru.tables.squad;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.LinkLabel.LinkLabelListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.AttributeChangeCalculator;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Players List Table v1
 * 
 * Enhanced to show player attributes with sorting (v1.0)
 * 
 * SOURCE:
 * https://3.bp.blogspot.com/-ADphk9k3mNs/VmiRVkpfgDI/AAAAAAAAA1g/lK3Ij3MtTmQ8D8Nkl3BrjRowVTbAGvFTgCPcB/s1600/Squad_.png
 * 
 * @author Geomancer86
 */
public class PlayersListTable extends VisTable {

    //
    Futtoboru game;
    
    // Attribute change calculator for showing tendencies
    private AttributeChangeCalculator changeCalculator;
    
    //
    private List<Player> currentPlayers;
    private List<Player> sortedPlayers; // Sorted copy for display
    
    // Sorting state
    private String currentSortColumn = null;
    private boolean sortAscending = true;
    
    // Column visibility - Expanded based on Football Manager squad screen
    // Physical attributes
    private boolean showAcceleration = true;
    private boolean showSpeed = true;
    private boolean showStrength = true;
    private boolean showStamina = true;
    private boolean showJumping = true;
    
    // Technical attributes
    private boolean showPassing = true;
    private boolean showLongShots = true;
    private boolean showHeading = true;
    private boolean showTackling = true;
    private boolean showMarking = true;
    
    // Mental attributes
    private boolean showDetermination = true;
    private boolean showPositioning = true;
    private boolean showTeamwork = true;
    
    // Dynamic Components
    VisTable mainTable;
    
    //
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    DecimalFormat attributeFormat = new DecimalFormat("#0.0");
    
    public PlayersListTable(Futtoboru parent) {
        
        // default table spacing
        super(true);
        
        // 
        this.game = parent;
        
        // Initialize change calculator
        if (game != null && game.getCurrentGame() != null) {
            this.changeCalculator = new AttributeChangeCalculator(game.getCurrentGame());
        }
        
        //
        updateDynamicComponents();
    }

    /**
     * Update the players list display with attributes
     */
    public void updateDynamicComponents() {
        
        // clear
        this.clear();
        
        // No Players
        if (currentPlayers == null || currentPlayers.isEmpty()) {
            int columnCount = getColumnCount();
            this.row();
            this.add(new VisLabel(LanguageModLoader.getValue("no_registered_players_at_club"))).colspan(columnCount);
            return;
        }
        
        // Create sorted copy
        sortedPlayers = new ArrayList<>(currentPlayers);
        if (currentSortColumn != null) {
            sortPlayers(sortedPlayers, currentSortColumn, sortAscending);
        }
        
        // Calculate actual number of columns
        int columnCount = getColumnCount();
        
        /**
         * Header Row with Sortable Columns
         */
        this.row();
        this.add(createSortableHeader("Name", "name")).width(150);
        this.add(createSortableHeader("Age", "age")).width(80);
        this.add(createSortableHeader("Country", "country")).width(100);
        
        // Physical attribute columns
        if (showAcceleration) {
            this.add(createSortableHeader("Accel", "acceleration")).width(70);
        }
        if (showSpeed) {
            this.add(createSortableHeader("Speed", "speed")).width(70);
        }
        if (showStrength) {
            this.add(createSortableHeader("Str", "strength")).width(70);
        }
        if (showStamina) {
            this.add(createSortableHeader("Sta", "stamina")).width(70);
        }
        if (showJumping) {
            this.add(createSortableHeader("Jump", "jumping")).width(70);
        }
        
        // Technical attribute columns
        if (showPassing) {
            this.add(createSortableHeader("Pass", "passing")).width(70);
        }
        if (showLongShots) {
            this.add(createSortableHeader("Long", "longShots")).width(70);
        }
        if (showHeading) {
            this.add(createSortableHeader("Head", "heading")).width(70);
        }
        if (showTackling) {
            this.add(createSortableHeader("Tack", "tackling")).width(70);
        }
        if (showMarking) {
            this.add(createSortableHeader("Mark", "marking")).width(70);
        }
        
        // Mental attribute columns
        if (showDetermination) {
            this.add(createSortableHeader("Det", "determination")).width(70);
        }
        if (showPositioning) {
            this.add(createSortableHeader("Pos", "positioning")).width(70);
        }
        if (showTeamwork) {
            this.add(createSortableHeader("Team", "teamwork")).width(70);
        }
        
        // Add separator with correct column span
        this.row();
        this.addSeparator().colspan(columnCount);
        
        /**
         * Player Rows
         */
        for (Player player : sortedPlayers) {
            this.row();
            
            // Name
            LinkLabel playerDetailLink = new LinkLabel(player.getPerson().getName() + " " + player.getPerson().getLastname());
            playerDetailLink.setListener(new LinkLabelListener() {
                @Override
                public void clicked (String url) {
                    // Get MainMenuManager from game engine
                    com.rndmodgames.futtoboru.menu.MainMenuManager menuManager = 
                        game.getGameEngine().getMainMenuManager();
                    if (menuManager != null) {
                        menuManager.setSelectedPlayer(player);
                        menuManager.setActiveMainScreen(com.rndmodgames.futtoboru.menu.MainMenuManager.PLAYER_DETAIL_SCREEN);
                    } else {
                        Gdx.app.error("PlayersListTable", "MainMenuManager is null, cannot open player detail");
                    }
                }
            });
            this.add(playerDetailLink).width(150);
            
            // Age
            long yearsOld = ChronoUnit.YEARS.between(player.getPerson().getBirthDate(), game.getCurrentGame().getGameDate());
            this.add(new VisLabel(String.valueOf(yearsOld))).width(80);
            
            // Country
            String countryName = player.getPerson().getCountry() != null ? 
                player.getPerson().getCountry().getCommonName() : "N/A";
            this.add(new VisLabel(countryName)).width(100);
            
            // Physical attributes (with change indicators)
            if (showAcceleration) {
                addAttributeCell(player, "Acceleration", player.getAcceleration());
            }
            if (showSpeed) {
                addAttributeCell(player, "Speed", player.getSpeed());
            }
            if (showStrength) {
                addAttributeCell(player, "Strength", player.getStrength());
            }
            if (showStamina) {
                addAttributeCell(player, "Stamina", player.getStamina());
            }
            if (showJumping) {
                addAttributeCell(player, "Jumping", player.getJumping());
            }
            
            // Technical attributes (with change indicators)
            if (showPassing) {
                addAttributeCell(player, "Passing", player.getPassing());
            }
            if (showLongShots) {
                addAttributeCell(player, "Long Shots", player.getLongShots());
            }
            if (showHeading) {
                addAttributeCell(player, "Heading", player.getHeading());
            }
            if (showTackling) {
                addAttributeCell(player, "Tackling", player.getTackling());
            }
            if (showMarking) {
                addAttributeCell(player, "Marking", player.getMarking());
            }
            
            // Mental attributes (with change indicators)
            if (showDetermination) {
                addAttributeCell(player, "Determination", player.getDetermination());
            }
            if (showPositioning) {
                addAttributeCell(player, "Positioning", player.getPositioning());
            }
            if (showTeamwork) {
                addAttributeCell(player, "Teamwork", player.getTeamwork());
            }
        }
    }
    
    /**
     * Create a sortable header label
     */
    private VisLabel createSortableHeader(String text, final String columnName) {
        VisLabel header = new VisLabel(text);
        
        // Add click listener for sorting
        header.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (event.getButton() == 0) { // Left click
                    // Toggle sort direction if same column, otherwise sort ascending
                    if (columnName.equals(currentSortColumn)) {
                        sortAscending = !sortAscending;
                    } else {
                        currentSortColumn = columnName;
                        sortAscending = true;
                    }
                    Gdx.app.log("PlayersListTable", "Sorting by: " + columnName + " (" + (sortAscending ? "ASC" : "DESC") + ")");
                    updateDynamicComponents();
                }
            }
        });
        
        // Add visual indicator if this is the current sort column
        if (columnName.equals(currentSortColumn)) {
            header.setText(text + (sortAscending ? " ↑" : " ↓"));
        }
        
        return header;
    }
    
    /**
     * Sort players by column
     */
    private void sortPlayers(List<Player> players, String column, boolean ascending) {
        Comparator<Player> comparator = null;
        
        switch (column) {
            case "name":
                comparator = Comparator.comparing(p -> p.getPerson().getName() + " " + p.getPerson().getLastname());
                break;
            case "age":
                comparator = Comparator.comparing(p -> 
                    ChronoUnit.YEARS.between(p.getPerson().getBirthDate(), game.getCurrentGame().getGameDate()));
                break;
            case "country":
                comparator = Comparator.comparing(p -> 
                    p.getPerson().getCountry() != null ? p.getPerson().getCountry().getCommonName() : "");
                break;
            case "strength":
                comparator = Comparator.comparing(p -> p.getStrength() != null ? p.getStrength() : 0.0f);
                break;
            case "speed":
                comparator = Comparator.comparing(p -> p.getSpeed() != null ? p.getSpeed() : 0.0f);
                break;
            case "passing":
                comparator = Comparator.comparing(p -> p.getPassing() != null ? p.getPassing() : 0.0f);
                break;
            case "stamina":
                comparator = Comparator.comparing(p -> p.getStamina() != null ? p.getStamina() : 0.0f);
                break;
            case "acceleration":
                comparator = Comparator.comparing(p -> p.getAcceleration() != null ? p.getAcceleration() : 0.0f);
                break;
            case "jumping":
                comparator = Comparator.comparing(p -> p.getJumping() != null ? p.getJumping() : 0.0f);
                break;
            case "longshots":
                comparator = Comparator.comparing(p -> p.getLongShots() != null ? p.getLongShots() : 0.0f);
                break;
            case "heading":
                comparator = Comparator.comparing(p -> p.getHeading() != null ? p.getHeading() : 0.0f);
                break;
            case "tackling":
                comparator = Comparator.comparing(p -> p.getTackling() != null ? p.getTackling() : 0.0f);
                break;
            case "marking":
                comparator = Comparator.comparing(p -> p.getMarking() != null ? p.getMarking() : 0.0f);
                break;
            case "determination":
                comparator = Comparator.comparing(p -> p.getDetermination() != null ? p.getDetermination() : 0.0f);
                break;
            case "positioning":
                comparator = Comparator.comparing(p -> p.getPositioning() != null ? p.getPositioning() : 0.0f);
                break;
            case "teamwork":
                comparator = Comparator.comparing(p -> p.getTeamwork() != null ? p.getTeamwork() : 0.0f);
                break;
        }
        
        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            Collections.sort(players, comparator);
        }
    }
    
    /**
     * Calculates the total number of columns currently displayed.
     */
    private int getColumnCount() {
        int count = 3; // Name, Age, Country (always shown)
        
        // Physical
        if (showAcceleration) count++;
        if (showSpeed) count++;
        if (showStrength) count++;
        if (showStamina) count++;
        if (showJumping) count++;
        
        // Technical
        if (showPassing) count++;
        if (showLongShots) count++;
        if (showHeading) count++;
        if (showTackling) count++;
        if (showMarking) count++;
        
        // Mental
        if (showDetermination) count++;
        if (showPositioning) count++;
        if (showTeamwork) count++;
        
        return count;
    }
    
    /**
     * Add an attribute cell with value and change indicator
     */
    private void addAttributeCell(Player player, String attributeName, Float value) {
        String valueText = value != null ? attributeFormat.format(value) : "N/A";
        
        // Get change indicator
        String changeText = "";
        if (changeCalculator != null && value != null) {
            AttributeChangeCalculator.AttributeChangeResult result = 
                changeCalculator.calculateAttributeChange(player, attributeName);
            if (result != null && !result.getDisplayText().isEmpty() && 
                !result.getDisplayText().equals("No historical data")) {
                changeText = " " + result.getDisplayText();
            }
        }
        
        VisLabel label = new VisLabel(valueText + changeText);
        
        // Color coding based on change
        if (changeText.contains("↑↑") || changeText.contains("↑")) {
            label.setColor(0.0f, 1.0f, 0.0f, 1.0f); // Green for improvement
        } else if (changeText.contains("↓↓") || changeText.contains("↓")) {
            label.setColor(1.0f, 0.0f, 0.0f, 1.0f); // Red for decline
        }
        
        this.add(label).width(70);
    }
    
    public List<Player> getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(List<Player> currentPlayers) {
        this.currentPlayers = currentPlayers;
        // Reset sort when players change
        currentSortColumn = null;
        sortAscending = true;
    }
}
