package com.rndmodgames.futtoboru.tables.player;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

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
    
    // Last viewed attributes (for change tracking)
    private Map<String, Float> lastViewedAttributes = new HashMap<>();
    
    // Formatting
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private DecimalFormat attributeFormat = new DecimalFormat("#0.0");
    
    // UI Components
    private VisTextButton backButton;
    private VisLabel playerNameLabel;
    private VisTable infoTable;
    private VisTable physicalAttributesTable;
    private VisTable mentalAttributesTable;
    private VisTable technicalAttributesTable;
    private VisTable goalkeeperAttributesTable;
    
    public PlayerDetailScreenTable(Game parent) {
        super(true);
        this.futtoboru = (Futtoboru) parent;
        
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
        
        // Initialize tables
        infoTable = new VisTable(true);
        physicalAttributesTable = new VisTable(true);
        mentalAttributesTable = new VisTable(true);
        technicalAttributesTable = new VisTable(true);
        goalkeeperAttributesTable = new VisTable(true);
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
        
        // Clear everything
        this.clear();
        
        // Store current attributes as "last viewed" for next time
        storeCurrentAttributes();
        
        // Top section: Back button and player name
        this.row();
        this.add(backButton).left().padBottom(10);
        this.row();
        this.add(playerNameLabel).colspan(2).center().padBottom(20);
        this.row();
        
        // Player Information Section
        buildInfoSection();
        this.row();
        this.addSeparator().colspan(2).pad(10);
        this.row();
        
        // Physical Attributes Section
        buildPhysicalAttributesSection();
        this.row();
        this.addSeparator().colspan(2).pad(10);
        this.row();
        
        // Mental Attributes Section
        buildMentalAttributesSection();
        this.row();
        this.addSeparator().colspan(2).pad(10);
        this.row();
        
        // Technical Attributes Section
        buildTechnicalAttributesSection();
        this.row();
        this.addSeparator().colspan(2).pad(10);
        this.row();
        
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
        
        this.add(infoTable).left().pad(10);
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
        
        this.add(physicalAttributesTable).left().pad(10);
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
        
        this.add(mentalAttributesTable).left().pad(10);
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
        
        this.add(technicalAttributesTable).left().pad(10);
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
        
        this.add(goalkeeperAttributesTable).left().pad(10);
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
        
        // Change indicator
        String changeText = formatAttributeChange(attributeName, currentValue);
        VisLabel changeLabel = new VisLabel(changeText);
        if (changeText.contains("🟢")) {
            changeLabel.setColor(0.0f, 1.0f, 0.0f, 1.0f); // Green
        } else if (changeText.contains("🔴")) {
            changeLabel.setColor(1.0f, 0.0f, 0.0f, 1.0f); // Red
        }
        table.add(changeLabel).left().width(100);
    }
    
    /**
     * Format attribute change for display
     */
    private String formatAttributeChange(String attributeName, Float currentValue) {
        if (currentValue == null) {
            return "";
        }
        
        Float lastValue = lastViewedAttributes.get(attributeName);
        if (lastValue == null) {
            return ""; // First time viewing
        }
        
        float change = currentValue - lastValue;
        
        if (Math.abs(change) < 0.1f) {
            return "[=]"; // No significant change
        }
        
        String sign = change > 0 ? "+" : "";
        String indicator = change > 0 ? "🟢" : "🔴";
        return String.format("[%s%.1f] %s", sign, change, indicator);
    }
    
    /**
     * Store current attributes as "last viewed" for next comparison
     */
    private void storeCurrentAttributes() {
        if (currentPlayer == null) return;
        
        lastViewedAttributes.clear();
        
        // Physical
        if (currentPlayer.getAcceleration() != null) lastViewedAttributes.put("Acceleration", currentPlayer.getAcceleration());
        if (currentPlayer.getSpeed() != null) lastViewedAttributes.put("Speed", currentPlayer.getSpeed());
        if (currentPlayer.getStamina() != null) lastViewedAttributes.put("Stamina", currentPlayer.getStamina());
        if (currentPlayer.getStrength() != null) lastViewedAttributes.put("Strength", currentPlayer.getStrength());
        if (currentPlayer.getEndurance() != null) lastViewedAttributes.put("Endurance", currentPlayer.getEndurance());
        if (currentPlayer.getJumping() != null) lastViewedAttributes.put("Jumping", currentPlayer.getJumping());
        if (currentPlayer.getDexterity() != null) lastViewedAttributes.put("Dexterity", currentPlayer.getDexterity());
        
        // Mental
        if (currentPlayer.getConcentration() != null) lastViewedAttributes.put("Concentration", currentPlayer.getConcentration());
        if (currentPlayer.getCourage() != null) lastViewedAttributes.put("Courage", currentPlayer.getCourage());
        if (currentPlayer.getDetermination() != null) lastViewedAttributes.put("Determination", currentPlayer.getDetermination());
        if (currentPlayer.getLeadership() != null) lastViewedAttributes.put("Leadership", currentPlayer.getLeadership());
        if (currentPlayer.getPerception() != null) lastViewedAttributes.put("Perception", currentPlayer.getPerception());
        if (currentPlayer.getPositioning() != null) lastViewedAttributes.put("Positioning", currentPlayer.getPositioning());
        if (currentPlayer.getTeamwork() != null) lastViewedAttributes.put("Teamwork", currentPlayer.getTeamwork());
        
        // Technical
        if (currentPlayer.getPassing() != null) lastViewedAttributes.put("Passing", currentPlayer.getPassing());
        if (currentPlayer.getKicking() != null) lastViewedAttributes.put("Kicking", currentPlayer.getKicking());
        if (currentPlayer.getLongShots() != null) lastViewedAttributes.put("Long Shots", currentPlayer.getLongShots());
        if (currentPlayer.getTrickShots() != null) lastViewedAttributes.put("Trick Shots", currentPlayer.getTrickShots());
        if (currentPlayer.getHeading() != null) lastViewedAttributes.put("Heading", currentPlayer.getHeading());
        if (currentPlayer.getOneTwos() != null) lastViewedAttributes.put("One-Twos", currentPlayer.getOneTwos());
        if (currentPlayer.getFreeKicks() != null) lastViewedAttributes.put("Free Kicks", currentPlayer.getFreeKicks());
        if (currentPlayer.getCornerKicks() != null) lastViewedAttributes.put("Corner Kicks", currentPlayer.getCornerKicks());
        if (currentPlayer.getPenaltyKicks() != null) lastViewedAttributes.put("Penalty Kicks", currentPlayer.getPenaltyKicks());
        if (currentPlayer.getThrowIns() != null) lastViewedAttributes.put("Throw-Ins", currentPlayer.getThrowIns());
        if (currentPlayer.getMarking() != null) lastViewedAttributes.put("Marking", currentPlayer.getMarking());
        if (currentPlayer.getTackling() != null) lastViewedAttributes.put("Tackling", currentPlayer.getTackling());
        
        // Goalkeeper
        if (currentPlayer.getShotStopping() != null) lastViewedAttributes.put("Shot Stopping", currentPlayer.getShotStopping());
        if (currentPlayer.getAreaControl() != null) lastViewedAttributes.put("Area Control", currentPlayer.getAreaControl());
        if (currentPlayer.getPunching() != null) lastViewedAttributes.put("Punching", currentPlayer.getPunching());
        if (currentPlayer.getHandToHand() != null) lastViewedAttributes.put("Hand to Hand", currentPlayer.getHandToHand());
        if (currentPlayer.getRushingOut() != null) lastViewedAttributes.put("Rushing Out", currentPlayer.getRushingOut());
        if (currentPlayer.getAreaPositioning() != null) lastViewedAttributes.put("Area Positioning", currentPlayer.getAreaPositioning());
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
}

