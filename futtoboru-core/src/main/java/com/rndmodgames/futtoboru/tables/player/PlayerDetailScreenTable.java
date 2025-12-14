package com.rndmodgames.futtoboru.tables.player;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.AttributeChangeCalculator;

/**
 * Player Detail Screen Table v1
 * 
 * Displays comprehensive player information including all attributes.
 * Tracks attribute changes for testing purposes.
 * 
 * @author Geomancer86
 */
public class PlayerDetailScreenTable extends VisTable {

    private Futtoboru futtoboru;
    private MainMenuManager menuManager;
    
    // Current player being displayed
    private Player currentPlayer;
    
    // Attribute change calculator (30-day tracking)
    private AttributeChangeCalculator changeCalculator;
    
    // Formatting
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private DecimalFormat attributeFormat = new DecimalFormat("#0.0");
    
    // UI Components
    private VisTextButton backButton;
    private VisLabel playerNameLabel;
    private VisTable contentTable; // Main content table that will be scrollable
    private VisScrollPane scrollPane; // Scroll pane for the content
    private VisTable infoTable;
    private VisTable physicalAttributesTable;
    private VisTable mentalAttributesTable;
    private VisTable technicalAttributesTable;
    private VisTable goalkeeperAttributesTable;
    
    public PlayerDetailScreenTable(Game parent) {
        super(true);
        this.futtoboru = (Futtoboru) parent;
        
        // Initialize change calculator
        if (futtoboru != null && futtoboru.getCurrentGame() != null) {
            this.changeCalculator = new AttributeChangeCalculator(futtoboru.getCurrentGame());
        }
        
        // Back button
        backButton = new VisTextButton("← Back to Squad");
        backButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (event.getButton() == 0 && menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.MAIN_SQUAD_SCREEN);
                }
            }
        });
        
        // Player name (will be set dynamically)
        playerNameLabel = new VisLabel("");
        playerNameLabel.setFontScale(1.5f);
        
        // Initialize content table (will be inside scroll pane)
        contentTable = new VisTable(true);
        
        // Initialize attribute tables
        infoTable = new VisTable(true);
        physicalAttributesTable = new VisTable(true);
        mentalAttributesTable = new VisTable(true);
        technicalAttributesTable = new VisTable(true);
        goalkeeperAttributesTable = new VisTable(true);
        
        // Create scroll pane for content
        scrollPane = new VisScrollPane(contentTable);
        scrollPane.setFadeScrollBars(false);
        
        // Add scroll pane to main table (this will stay, only contentTable will be cleared/updated)
        // Note: Using just .grow() to match other screens, not .grow().fill()
        this.row();
        this.add(scrollPane).grow();
    }
    
    /**
     * Update the screen with player data
     */
    public void updateDynamicComponents(Player player) {
        if (player == null || player.getPerson() == null) {
            Gdx.app.error("PlayerDetailScreenTable", "Cannot update: player or person is null");
            return;
        }
        
        this.currentPlayer = player;
        
        // Update change calculator if game changed
        if (changeCalculator == null && futtoboru != null && futtoboru.getCurrentGame() != null) {
            this.changeCalculator = new AttributeChangeCalculator(futtoboru.getCurrentGame());
        }
        
        // Clear content table (not the main table - that has the scroll pane)
        contentTable.clear();
        
        // Top section: Back button and player name
        contentTable.row();
        contentTable.add(backButton).left().padBottom(10);
        contentTable.row();
        contentTable.add(playerNameLabel).colspan(2).center().padBottom(20);
        contentTable.row();
        
        // Player Information Section
        buildInfoSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(10);
        contentTable.row();
        
        // Physical Attributes Section
        buildPhysicalAttributesSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(10);
        contentTable.row();
        
        // Mental Attributes Section
        buildMentalAttributesSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(10);
        contentTable.row();
        
        // Technical Attributes Section
        buildTechnicalAttributesSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(10);
        contentTable.row();
        
        // Goalkeeper Attributes Section
        buildGoalkeeperAttributesSection();
    }
    
    /**
     * Build player information section
     */
    private void buildInfoSection() {
        infoTable.clear();
        
        if (currentPlayer == null || currentPlayer.getPerson() == null) return;
        
        playerNameLabel.setText(currentPlayer.getPerson().getName() + " " + currentPlayer.getPerson().getLastname());
        
        long age = ChronoUnit.YEARS.between(currentPlayer.getPerson().getBirthDate(), 
            futtoboru.getCurrentGame().getGameDate());
        
        infoTable.row();
        infoTable.add(new VisLabel("Name:")).left().width(150);
        infoTable.add(new VisLabel(currentPlayer.getPerson().getName() + " " + currentPlayer.getPerson().getLastname())).left();
        infoTable.row();
        
        infoTable.add(new VisLabel("Age:")).left().width(150);
        infoTable.add(new VisLabel(age + " years")).left();
        infoTable.row();
        
        infoTable.add(new VisLabel("Nationality:")).left().width(150);
        String countryName = currentPlayer.getPerson().getCountry() != null ? 
            currentPlayer.getPerson().getCountry().getCommonName() : "N/A";
        infoTable.add(new VisLabel(countryName)).left();
        infoTable.row();
        
        infoTable.add(new VisLabel("Birth Date:")).left().width(150);
        infoTable.add(new VisLabel(dateFormatter.format(currentPlayer.getPerson().getBirthDate()))).left();
        infoTable.row();
        
        infoTable.add(new VisLabel("Current Club:")).left().width(150);
        String clubName = "N/A";
        if (currentPlayer.getPerson().getCurrentClubId() != null) {
            com.rndmodgames.futtoboru.data.Club club = futtoboru.getCurrentGame().getClubById(
                currentPlayer.getPerson().getCurrentClubId());
            if (club != null) {
                clubName = club.getName();
            }
        }
        infoTable.add(new VisLabel(clubName)).left();
        
        contentTable.add(infoTable).left().pad(10);
    }
    
    /**
     * Build physical attributes section
     */
    private void buildPhysicalAttributesSection() {
        physicalAttributesTable.clear();
        
        physicalAttributesTable.row();
        physicalAttributesTable.add(new VisLabel("Physical Attributes")).colspan(3).left().padBottom(5);
        physicalAttributesTable.row();
        
        addAttributeRow(physicalAttributesTable, "Acceleration", currentPlayer.getAcceleration());
        addAttributeRow(physicalAttributesTable, "Speed", currentPlayer.getSpeed());
        addAttributeRow(physicalAttributesTable, "Stamina", currentPlayer.getStamina());
        addAttributeRow(physicalAttributesTable, "Strength", currentPlayer.getStrength());
        addAttributeRow(physicalAttributesTable, "Endurance", currentPlayer.getEndurance());
        addAttributeRow(physicalAttributesTable, "Jumping", currentPlayer.getJumping());
        addAttributeRow(physicalAttributesTable, "Dexterity", currentPlayer.getDexterity());
        
        contentTable.add(physicalAttributesTable).left().pad(10);
    }
    
    /**
     * Build mental attributes section
     */
    private void buildMentalAttributesSection() {
        mentalAttributesTable.clear();
        
        mentalAttributesTable.row();
        mentalAttributesTable.add(new VisLabel("Mental Attributes")).colspan(3).left().padBottom(5);
        mentalAttributesTable.row();
        
        addAttributeRow(mentalAttributesTable, "Concentration", currentPlayer.getConcentration());
        addAttributeRow(mentalAttributesTable, "Courage", currentPlayer.getCourage());
        addAttributeRow(mentalAttributesTable, "Determination", currentPlayer.getDetermination());
        addAttributeRow(mentalAttributesTable, "Leadership", currentPlayer.getLeadership());
        addAttributeRow(mentalAttributesTable, "Perception", currentPlayer.getPerception());
        addAttributeRow(mentalAttributesTable, "Positioning", currentPlayer.getPositioning());
        addAttributeRow(mentalAttributesTable, "Teamwork", currentPlayer.getTeamwork());
        
        contentTable.add(mentalAttributesTable).left().pad(10);
    }
    
    /**
     * Build technical attributes section
     */
    private void buildTechnicalAttributesSection() {
        technicalAttributesTable.clear();
        
        technicalAttributesTable.row();
        technicalAttributesTable.add(new VisLabel("Technical Attributes")).colspan(3).left().padBottom(5);
        technicalAttributesTable.row();
        
        addAttributeRow(technicalAttributesTable, "Passing", currentPlayer.getPassing());
        addAttributeRow(technicalAttributesTable, "Kicking", currentPlayer.getKicking());
        addAttributeRow(technicalAttributesTable, "Long Shots", currentPlayer.getLongShots());
        addAttributeRow(technicalAttributesTable, "Trick Shots", currentPlayer.getTrickShots());
        addAttributeRow(technicalAttributesTable, "Heading", currentPlayer.getHeading());
        addAttributeRow(technicalAttributesTable, "One-Twos", currentPlayer.getOneTwos());
        addAttributeRow(technicalAttributesTable, "Free Kicks", currentPlayer.getFreeKicks());
        addAttributeRow(technicalAttributesTable, "Corner Kicks", currentPlayer.getCornerKicks());
        addAttributeRow(technicalAttributesTable, "Penalty Kicks", currentPlayer.getPenaltyKicks());
        addAttributeRow(technicalAttributesTable, "Throw-Ins", currentPlayer.getThrowIns());
        addAttributeRow(technicalAttributesTable, "Marking", currentPlayer.getMarking());
        addAttributeRow(technicalAttributesTable, "Tackling", currentPlayer.getTackling());
        
        contentTable.add(technicalAttributesTable).left().pad(10);
    }
    
    /**
     * Build goalkeeper attributes section
     */
    private void buildGoalkeeperAttributesSection() {
        goalkeeperAttributesTable.clear();
        
        goalkeeperAttributesTable.row();
        goalkeeperAttributesTable.add(new VisLabel("Goalkeeper Attributes")).colspan(3).left().padBottom(5);
        goalkeeperAttributesTable.row();
        
        addAttributeRow(goalkeeperAttributesTable, "Shot Stopping", currentPlayer.getShotStopping());
        addAttributeRow(goalkeeperAttributesTable, "Area Control", currentPlayer.getAreaControl());
        addAttributeRow(goalkeeperAttributesTable, "Punching", currentPlayer.getPunching());
        addAttributeRow(goalkeeperAttributesTable, "Hand to Hand", currentPlayer.getHandToHand());
        addAttributeRow(goalkeeperAttributesTable, "Rushing Out", currentPlayer.getRushingOut());
        addAttributeRow(goalkeeperAttributesTable, "Area Positioning", currentPlayer.getAreaPositioning());
        
        contentTable.add(goalkeeperAttributesTable).left().pad(10);
    }
    
    /**
     * Add an attribute row with change tracking
     */
    private void addAttributeRow(VisTable table, String attributeName, Float currentValue) {
        table.row();
        
        // Attribute name
        table.add(new VisLabel(attributeName + ":")).left().width(150);
        
        // Current value
        String valueText = currentValue != null ? attributeFormat.format(currentValue) : "N/A";
        VisLabel valueLabel = new VisLabel(valueText);
        table.add(valueLabel).left().width(80);
        
        // Change indicator (30-day tracking)
        String changeText = formatAttributeChange(attributeName, currentValue);
        VisLabel changeLabel = new VisLabel(changeText);
        
        // Color coding based on trend
        if (changeText.contains("↑↑") || changeText.contains("↑")) {
            changeLabel.setColor(0.0f, 1.0f, 0.0f, 1.0f); // Green for improvement
        } else if (changeText.contains("↓↓") || changeText.contains("↓")) {
            changeLabel.setColor(1.0f, 0.0f, 0.0f, 1.0f); // Red for decline
        } else if (changeText.contains("=")) {
            changeLabel.setColor(0.8f, 0.8f, 0.8f, 1.0f); // Gray for stable
        }
        
        table.add(changeLabel).left().width(120);
    }
    
    /**
     * Format attribute change for display using 30-day tracking
     */
    private String formatAttributeChange(String attributeName, Float currentValue) {
        if (currentValue == null || changeCalculator == null || currentPlayer == null) {
            return "[=]"; // Show stable if no data
        }
        
        // Calculate change over default period (30 days)
        AttributeChangeCalculator.AttributeChangeResult result = 
            changeCalculator.calculateAttributeChange(currentPlayer, attributeName);
        
        if (result == null) {
            return "[=]"; // Show stable if calculation failed
        }
        
        // If no historical data, show stable (not empty)
        if (result.getDisplayText().equals("No historical data") || result.getDisplayText().isEmpty()) {
            return "[=]";
        }
        
        return result.getDisplayText();
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
}

