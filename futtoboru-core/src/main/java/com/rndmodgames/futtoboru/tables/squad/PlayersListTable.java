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
    
    //
    private List<Player> currentPlayers;
    private List<Player> sortedPlayers; // Sorted copy for display
    
    // Sorting state
    private String currentSortColumn = null;
    private boolean sortAscending = true;
    
    // Column visibility (v1.0 - basic implementation)
    private boolean showStrength = true;
    private boolean showSpeed = true;
    private boolean showPassing = true;
    private boolean showStamina = true;
    
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
            // Calculate column count for empty message
            int columnCount = 3; // Name, Age, Country (always shown)
            if (showStrength) columnCount++;
            if (showSpeed) columnCount++;
            if (showPassing) columnCount++;
            if (showStamina) columnCount++;
            
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
        int columnCount = 3; // Name, Age, Country (always shown)
        if (showStrength) columnCount++;
        if (showSpeed) columnCount++;
        if (showPassing) columnCount++;
        if (showStamina) columnCount++;
        
        /**
         * Header Row with Sortable Columns
         */
        this.row();
        this.add(createSortableHeader("Name", "name")).width(150);
        this.add(createSortableHeader("Age", "age")).width(80);
        this.add(createSortableHeader("Country", "country")).width(100);
        
        // Attribute columns
        if (showStrength) {
            this.add(createSortableHeader("Strength", "strength")).width(80);
        }
        if (showSpeed) {
            this.add(createSortableHeader("Speed", "speed")).width(80);
        }
        if (showPassing) {
            this.add(createSortableHeader("Passing", "passing")).width(80);
        }
        if (showStamina) {
            this.add(createSortableHeader("Stamina", "stamina")).width(80);
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
            
            // Attributes (with formatting)
            if (showStrength) {
                Float strength = player.getStrength();
                String strengthText = strength != null ? attributeFormat.format(strength) : "N/A";
                this.add(new VisLabel(strengthText)).width(80);
            }
            if (showSpeed) {
                Float speed = player.getSpeed();
                String speedText = speed != null ? attributeFormat.format(speed) : "N/A";
                this.add(new VisLabel(speedText)).width(80);
            }
            if (showPassing) {
                Float passing = player.getPassing();
                String passingText = passing != null ? attributeFormat.format(passing) : "N/A";
                this.add(new VisLabel(passingText)).width(80);
            }
            if (showStamina) {
                Float stamina = player.getStamina();
                String staminaText = stamina != null ? attributeFormat.format(stamina) : "N/A";
                this.add(new VisLabel(staminaText)).width(80);
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
        }
        
        if (comparator != null) {
            if (!ascending) {
                comparator = comparator.reversed();
            }
            Collections.sort(players, comparator);
        }
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
