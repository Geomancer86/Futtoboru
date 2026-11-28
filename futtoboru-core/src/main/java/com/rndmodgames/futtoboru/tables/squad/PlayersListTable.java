package com.rndmodgames.futtoboru.tables.squad;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.LinkLabel.LinkLabelListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.AttributeChangeCalculator;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.engine.injuries.Availability;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiTooltips;
import com.rndmodgames.futtoboru.system.Log;

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
    DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.NUMERIC); // U-12: the game language
    
    public PlayersListTable(Futtoboru parent) {
        
        // default table spacing
        super(true);
        
        // 
        this.game = parent;
        
        // Initialize change calculator
        if (game != null && game.getSession().getGame() != null) {
            this.changeCalculator = new AttributeChangeCalculator(game.getSession().getGame());
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
        this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_name"), "name")).width(UiTheme.size("table.squad.name"));
        this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_age"), "age")).width(UiTheme.size("table.squad.age"));
        this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_country"), "country")).width(UiTheme.size("table.squad.country"));
        this.add(new VisLabel(LanguageModLoader.getValue("squad_col_fan"))).width(UiTheme.size("table.squad.fan")); // G-10
        this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_injury"), "injury")).width(UiTheme.size("table.squad.injury"));
        
        // Physical attribute columns
        if (showAcceleration) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_acceleration"), "acceleration")).width(UiTheme.size("table.squad.attr"));
        }
        if (showSpeed) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_speed"), "speed")).width(UiTheme.size("table.squad.attr"));
        }
        if (showStrength) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_strength"), "strength")).width(UiTheme.size("table.squad.attr"));
        }
        if (showStamina) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_stamina"), "stamina")).width(UiTheme.size("table.squad.attr"));
        }
        if (showJumping) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_jumping"), "jumping")).width(UiTheme.size("table.squad.attr"));
        }
        
        // Technical attribute columns
        if (showPassing) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_passing"), "passing")).width(UiTheme.size("table.squad.attr"));
        }
        if (showLongShots) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_long_shots"), "longShots")).width(UiTheme.size("table.squad.attr"));
        }
        if (showHeading) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_heading"), "heading")).width(UiTheme.size("table.squad.attr"));
        }
        if (showTackling) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_tackling"), "tackling")).width(UiTheme.size("table.squad.attr"));
        }
        if (showMarking) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_marking"), "marking")).width(UiTheme.size("table.squad.attr"));
        }
        
        // Mental attribute columns
        if (showDetermination) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_determination"), "determination")).width(UiTheme.size("table.squad.attr"));
        }
        if (showPositioning) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_positioning"), "positioning")).width(UiTheme.size("table.squad.attr"));
        }
        if (showTeamwork) {
            this.add(createSortableHeader(LanguageModLoader.getValue("squad_col_teamwork"), "teamwork")).width(UiTheme.size("table.squad.attr"));
        }
        
        // Add separator with correct column span
        this.row();
        this.addSeparator().colspan(columnCount);
        
        /**
         * Player Rows
         */
        com.rndmodgames.futtoboru.history.HistoryFacts facts = new com.rndmodgames.futtoboru.history.HistoryFacts(
                game.getSession().database().getGameData(),
                com.rndmodgames.futtoboru.history.HistoryFacts.dataLanguage(LanguageModLoader.getCurrentLanguage().getId()));
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
                        menuManager.setActiveMainScreen(GameScreenId.PLAYER_DETAIL);
                    } else {
                        Log.error(Log.UI_CLUB, "PlayersListTable", "MainMenuManager is null, cannot open player detail");
                    }
                }
            });
            this.add(playerDetailLink).width(UiTheme.size("table.squad.name"));
            
            // Age
            long yearsOld = ChronoUnit.YEARS.between(player.getPerson().getBirthDate(), game.getSession().getGame().getGameDate());
            this.add(new VisLabel(String.valueOf(yearsOld))).width(UiTheme.size("table.squad.age"));
            
            // Country
            String countryName = player.getPerson().getCountry() != null ? 
                player.getPerson().getCountry().getCommonName() : LanguageModLoader.getValue("not_available");
            this.add(new VisLabel(countryName)).width(UiTheme.size("table.squad.country"));

            // G-02: injury mark + days out (empty when available)
            this.add(injuryCell(player)).width(UiTheme.size("table.squad.injury"));
            
            // G-10: fan-love tier marker (0-5), shown as the tier number in the tier colour
            this.add(fanMarker(player, facts)).width(UiTheme.size("table.squad.fan"));
            
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
        // abbreviated attribute columns explain themselves on hover (U-09)
        if (UiTooltips.SQUAD_ATTRIBUTE_COLUMNS.contains(columnName)) {
            UiTooltips.tip(header, "tip_attr_" + columnName);
        }
        
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
                    Log.info(Log.UI_CLUB, "PlayersListTable", "Sorting by: " + columnName + " (" + (sortAscending ? "ASC" : "DESC") + ")");
                    updateDynamicComponents();
                }
            }
        });
        
        // Add visual indicator if this is the current sort column
        if (columnName.equals(currentSortColumn)) {
            // U-12: "^" / "v" from the bundles (the font has no arrow glyphs)
            String mark = sortAscending ? LanguageModLoader.getValue("sort_ascending_mark")
                    : LanguageModLoader.getValue("sort_descending_mark");
            header.setText(text + " " + mark);
        }
        
        return header;
    }
    
    /** G-02: "Inj. 12d" in the injury colour while he is out, else nothing. */
    private VisLabel injuryCell(Player player) {
        Availability.Status status = Availability.of(player, game.getSession().getGame().getGameDate());
        if (status.isAvailable()) {
            return new VisLabel("");
        }
        VisLabel label = new VisLabel(LanguageModLoader.format("squad_injury_mark", status.getDaysLeft()));
        label.setColor(UiTheme.color("state.injured"));
        return label;
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
                    ChronoUnit.YEARS.between(p.getPerson().getBirthDate(), game.getSession().getGame().getGameDate()));
                break;
            case "country":
                comparator = Comparator.comparing(p -> 
                    p.getPerson().getCountry() != null ? p.getPerson().getCountry().getCommonName() : "");
                break;
            case "injury":
                comparator = Comparator.comparing(p -> Availability.of(p, game.getSession().getGame().getGameDate()).getDaysLeft());
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
    /** G-10: the tier of a player at his club as a small coloured number; empty when he has no data id. */
    private VisLabel fanMarker(Player player, com.rndmodgames.futtoboru.history.HistoryFacts facts) {
        com.rndmodgames.futtoboru.data.Person person = player.getPerson();
        com.rndmodgames.futtoboru.data.Club club = person.getCurrentClubId() == null || game.getSession().getGame() == null ? null
                : game.getSession().getGame().getClubById(person.getCurrentClubId());
        int tier = facts.fanTier(person.getDataId(), club == null ? null : club.getDataId());
        VisLabel label = new VisLabel(String.valueOf(tier));
        label.setColor(UiTheme.color(com.rndmodgames.futtoboru.tables.player.PlayerDetailScreenTable.fanTierColor(tier)));
        return label;
    }

    private int getColumnCount() {
        int count = 5; // Name, Age, Country, Fans (G-10), Injury (always shown)
        
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
        String valueText = value != null ? GameFormat.decimal(value, 1) : LanguageModLoader.getValue("not_available");
        
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
        if (changeText.contains("[+")) { // U-12: the font has no arrows; the sign and the colour say it
            label.setColor(UiTheme.color("trend.up")); // Green for improvement
        } else if (changeText.contains("[-")) {
            label.setColor(UiTheme.color("trend.down")); // Red for decline
        }
        
        this.add(label).width(UiTheme.size("table.squad.attr"));
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
