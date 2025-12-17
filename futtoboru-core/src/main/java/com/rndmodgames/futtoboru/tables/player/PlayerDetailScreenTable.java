package com.rndmodgames.futtoboru.tables.player;

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
import com.rndmodgames.futtoboru.data.NationalityModifier;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.data.RegionModifier;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.AttributeChangeCalculator;
import com.rndmodgames.futtoboru.system.loaders.NationalityModifiersLoader;
import com.rndmodgames.futtoboru.system.loaders.RegionModifiersLoader;

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
        
        // Contract Details Section (v1.0)
        buildContractSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(10);
        contentTable.row();
        
        // Modifiers & Bonuses Section (v2.0 - 3d6 system)
        buildModifiersSection();
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
        infoTable.row();
        
        // Player Profession (day job for amateur/semi-pro)
        infoTable.add(new VisLabel("Profession:")).left().width(150);
        String professionName = "N/A (Professional Player)";
        if (currentPlayer.getPlayerProfession() != null) {
            professionName = currentPlayer.getPlayerProfession().getName();
        }
        infoTable.add(new VisLabel(professionName)).left();
        infoTable.row();
        
        // Region/State (for region modifiers)
        infoTable.add(new VisLabel("Region:")).left().width(150);
        String regionName = "N/A";
        if (currentPlayer.getPerson().getState() != null && 
            currentPlayer.getPerson().getState().getName() != null) {
            regionName = currentPlayer.getPerson().getState().getName();
        }
        infoTable.add(new VisLabel(regionName)).left();
        infoTable.row();
        
        // Contract Information (v1.0)
        infoTable.add(new VisLabel("Contract:")).left().width(150);
        String contractInfo = "No Contract";
        if (currentPlayer.getPerson().getCurrentClubId() != null && futtoboru.getCurrentGame() != null) {
            com.rndmodgames.futtoboru.data.Club playerClub = futtoboru.getCurrentGame().getClubById(
                currentPlayer.getPerson().getCurrentClubId());
            if (playerClub != null) {
                com.rndmodgames.futtoboru.data.PlayerContract contract = playerClub.getContractForPlayer(
                    currentPlayer.getId() != null ? currentPlayer.getId() : currentPlayer.getPerson().getId());
                if (contract != null) {
                    contractInfo = com.rndmodgames.futtoboru.data.ContractType.getName(contract.getContractType()) + 
                        " - £" + contract.getWeeklyWage().setScale(2, java.math.RoundingMode.HALF_UP) + "/week";
                }
            }
        }
        infoTable.add(new VisLabel(contractInfo)).left();
        
        contentTable.add(infoTable).left().pad(10);
    }
    
    /**
     * Build contract details section (v1.0)
     * Shows contract type, wages, bonuses, and expiry
     */
    private void buildContractSection() {
        VisTable contractTable = new VisTable(true);
        
        contractTable.row();
        contractTable.add(new VisLabel("Contract Details")).colspan(2).left().padBottom(5);
        contractTable.row();
        
        // Get contract
        com.rndmodgames.futtoboru.data.PlayerContract contract = null;
        if (currentPlayer.getPerson().getCurrentClubId() != null && futtoboru.getCurrentGame() != null) {
            com.rndmodgames.futtoboru.data.Club playerClub = futtoboru.getCurrentGame().getClubById(
                currentPlayer.getPerson().getCurrentClubId());
            if (playerClub != null) {
                // Try player ID first, then person ID as fallback
                Long lookupId = currentPlayer.getId() != null ? currentPlayer.getId() : 
                    (currentPlayer.getPerson() != null ? currentPlayer.getPerson().getId() : null);
                if (lookupId != null) {
                    contract = playerClub.getContractForPlayer(lookupId);
                }
            }
        }
        
        if (contract != null) {
            // Contract Type
            contractTable.row();
            contractTable.add(new VisLabel("Type:")).left().width(150);
            contractTable.add(new VisLabel(com.rndmodgames.futtoboru.data.ContractType.getName(contract.getContractType()))).left();
            contractTable.row();
            
            // Weekly Wage
            contractTable.add(new VisLabel("Weekly Wage:")).left().width(150);
            contractTable.add(new VisLabel("£" + contract.getWeeklyWage().setScale(2, java.math.RoundingMode.HALF_UP))).left();
            contractTable.row();
            
            // Contract Dates
            if (contract.getStartDate() != null) {
                contractTable.add(new VisLabel("Start Date:")).left().width(150);
                contractTable.add(new VisLabel(dateFormatter.format(contract.getStartDate()))).left();
                contractTable.row();
            }
            
            if (contract.getEndDate() != null) {
                contractTable.add(new VisLabel("End Date:")).left().width(150);
                contractTable.add(new VisLabel(dateFormatter.format(contract.getEndDate()))).left();
                contractTable.row();
                
                // Days remaining
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                    futtoboru.getCurrentGame().getGameDate(), contract.getEndDate());
                contractTable.add(new VisLabel("Days Remaining:")).left().width(150);
                VisLabel daysLabel = new VisLabel(String.valueOf(daysRemaining));
                if (daysRemaining < 90) {
                    daysLabel.setColor(1.0f, 0.5f, 0.0f, 1.0f); // Orange for expiring soon
                }
                if (daysRemaining < 30) {
                    daysLabel.setColor(1.0f, 0.0f, 0.0f, 1.0f); // Red for expiring very soon
                }
                contractTable.add(daysLabel).left();
                contractTable.row();
            }
            
            // Bonuses (only show if non-zero)
            if (contract.getSigningBonus() != null && contract.getSigningBonus().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel("Signing Bonus:")).left().width(150);
                contractTable.add(new VisLabel("£" + contract.getSigningBonus().setScale(2, java.math.RoundingMode.HALF_UP))).left();
                contractTable.row();
            }
            
            if (contract.getGoalBonus() != null && contract.getGoalBonus().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel("Goal Bonus:")).left().width(150);
                contractTable.add(new VisLabel("£" + contract.getGoalBonus().setScale(2, java.math.RoundingMode.HALF_UP) + " per goal")).left();
                contractTable.row();
            }
            
            if (contract.getAppearanceFee() != null && contract.getAppearanceFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel("Appearance Fee:")).left().width(150);
                contractTable.add(new VisLabel("£" + contract.getAppearanceFee().setScale(2, java.math.RoundingMode.HALF_UP) + " per match")).left();
                contractTable.row();
            }
            
            if (contract.getLeagueWinBonus() != null && contract.getLeagueWinBonus().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel("League Win Bonus:")).left().width(150);
                contractTable.add(new VisLabel("£" + contract.getLeagueWinBonus().setScale(2, java.math.RoundingMode.HALF_UP))).left();
                contractTable.row();
            }
        } else {
            contractTable.row();
            contractTable.add(new VisLabel("No contract found")).colspan(2).left();
        }
        
        contentTable.add(contractTable).left().pad(10);
    }
    
    /**
     * Build modifiers and bonuses section (v2.0 - 3d6 system)
     * Shows nationality modifiers, region modifiers, and profession bonuses
     */
    private void buildModifiersSection() {
        VisTable modifiersTable = new VisTable(true);
        
        modifiersTable.row();
        modifiersTable.add(new VisLabel("Attribute Modifiers & Bonuses")).colspan(2).left().padBottom(5);
        modifiersTable.row();
        
        // Get nationality modifier
        NationalityModifier natMod = null;
        if (currentPlayer.getPerson().getCountry() != null && 
            currentPlayer.getPerson().getCountry().getId() != null) {
            Long countryId = currentPlayer.getPerson().getCountry().getId();
            natMod = NationalityModifiersLoader.getModifier(countryId);
            
            // Debug logging
            if (natMod == null) {
                Gdx.app.debug("PlayerDetailScreenTable", "No nationality modifier found for country ID: " + 
                    countryId + " (" + currentPlayer.getPerson().getCountry().getCommonName() + ")");
            }
        } else {
            Gdx.app.debug("PlayerDetailScreenTable", "Player has no country set");
        }
        
        // Get region modifier
        RegionModifier regMod = null;
        if (currentPlayer.getPerson().getState() != null && 
            currentPlayer.getPerson().getState().getName() != null &&
            currentPlayer.getPerson().getCountry() != null && 
            currentPlayer.getPerson().getCountry().getId() != null) {
            String stateName = currentPlayer.getPerson().getState().getName();
            Long countryId = currentPlayer.getPerson().getCountry().getId();
            regMod = RegionModifiersLoader.getModifier(stateName, countryId);
            
            // Debug logging
            if (regMod == null) {
                Gdx.app.debug("PlayerDetailScreenTable", "No region modifier found for state: " + 
                    stateName + ", country ID: " + countryId);
            }
        } else {
            if (currentPlayer.getPerson().getState() == null) {
                Gdx.app.debug("PlayerDetailScreenTable", "Player has no state/region set");
            }
        }
        
        // Nationality Modifiers
        if (natMod != null) {
            modifiersTable.row();
            modifiersTable.add(new VisLabel("Nationality Modifiers (" + natMod.getCountryName() + "):")).colspan(2).left().padTop(5);
            modifiersTable.row();
            
            addModifierRow(modifiersTable, "Strength", natMod.getStrengthModifier());
            addModifierRow(modifiersTable, "Endurance", natMod.getEnduranceModifier());
            addModifierRow(modifiersTable, "Stamina", natMod.getStaminaModifier());
            addModifierRow(modifiersTable, "Speed", natMod.getSpeedModifier());
            addModifierRow(modifiersTable, "Acceleration", natMod.getAccelerationModifier());
            addModifierRow(modifiersTable, "Jumping", natMod.getJumpingModifier());
            addModifierRow(modifiersTable, "Dexterity", natMod.getDexterityModifier());
            addModifierRow(modifiersTable, "Concentration", natMod.getConcentrationModifier());
            addModifierRow(modifiersTable, "Courage", natMod.getCourageModifier());
            addModifierRow(modifiersTable, "Determination", natMod.getDeterminationModifier());
            addModifierRow(modifiersTable, "Leadership", natMod.getLeadershipModifier());
            addModifierRow(modifiersTable, "Perception", natMod.getPerceptionModifier());
            addModifierRow(modifiersTable, "Positioning", natMod.getPositioningModifier());
            addModifierRow(modifiersTable, "Teamwork", natMod.getTeamworkModifier());
        } else {
            modifiersTable.row();
            modifiersTable.add(new VisLabel("Nationality Modifiers: None")).colspan(2).left().padTop(5);
        }
        
        modifiersTable.row();
        modifiersTable.addSeparator().colspan(2).pad(5);
        
        // Region Modifiers
        if (regMod != null) {
            modifiersTable.row();
            modifiersTable.add(new VisLabel("Region Modifiers (" + regMod.getRegionName() + "):")).colspan(2).left().padTop(5);
            modifiersTable.row();
            
            addModifierRow(modifiersTable, "Strength", regMod.getStrengthModifier());
            addModifierRow(modifiersTable, "Endurance", regMod.getEnduranceModifier());
            addModifierRow(modifiersTable, "Stamina", regMod.getStaminaModifier());
            addModifierRow(modifiersTable, "Speed", regMod.getSpeedModifier());
            addModifierRow(modifiersTable, "Acceleration", regMod.getAccelerationModifier());
            addModifierRow(modifiersTable, "Jumping", regMod.getJumpingModifier());
            addModifierRow(modifiersTable, "Dexterity", regMod.getDexterityModifier());
            addModifierRow(modifiersTable, "Concentration", regMod.getConcentrationModifier());
            addModifierRow(modifiersTable, "Courage", regMod.getCourageModifier());
            addModifierRow(modifiersTable, "Determination", regMod.getDeterminationModifier());
            addModifierRow(modifiersTable, "Leadership", regMod.getLeadershipModifier());
            addModifierRow(modifiersTable, "Perception", regMod.getPerceptionModifier());
            addModifierRow(modifiersTable, "Positioning", regMod.getPositioningModifier());
            addModifierRow(modifiersTable, "Teamwork", regMod.getTeamworkModifier());
        } else {
            modifiersTable.row();
            modifiersTable.add(new VisLabel("Region Modifiers: None")).colspan(2).left().padTop(5);
        }
        
        modifiersTable.row();
        modifiersTable.addSeparator().colspan(2).pad(5);
        
        // Profession Bonuses (only for amateur/semi-pro)
        if (currentPlayer.getPlayerProfession() != null) {
            PlayerProfession profession = currentPlayer.getPlayerProfession();
            modifiersTable.row();
            modifiersTable.add(new VisLabel("Profession Bonuses (" + profession.getName() + "):")).colspan(2).left().padTop(5);
            modifiersTable.row();
            
            addModifierRow(modifiersTable, "Strength", profession.getStrengthBonus());
            addModifierRow(modifiersTable, "Endurance", profession.getEnduranceBonus());
            addModifierRow(modifiersTable, "Stamina", profession.getStaminaBonus());
            addModifierRow(modifiersTable, "Speed", profession.getSpeedBonus());
            addModifierRow(modifiersTable, "Acceleration", profession.getAccelerationBonus());
            addModifierRow(modifiersTable, "Jumping", profession.getJumpingBonus());
            addModifierRow(modifiersTable, "Dexterity", profession.getDexterityBonus());
            addModifierRow(modifiersTable, "Concentration", profession.getConcentrationBonus());
            addModifierRow(modifiersTable, "Courage", profession.getCourageBonus());
            addModifierRow(modifiersTable, "Determination", profession.getDeterminationBonus());
            addModifierRow(modifiersTable, "Leadership", profession.getLeadershipBonus());
            addModifierRow(modifiersTable, "Perception", profession.getPerceptionBonus());
            addModifierRow(modifiersTable, "Positioning", profession.getPositioningBonus());
            addModifierRow(modifiersTable, "Teamwork", profession.getTeamworkBonus());
        } else {
            modifiersTable.row();
            modifiersTable.add(new VisLabel("Profession Bonuses: None (Professional Player)")).colspan(2).left().padTop(5);
        }
        
        contentTable.add(modifiersTable).left().pad(10);
    }
    
    /**
     * Add a modifier row (only shows if modifier is non-zero)
     */
    private void addModifierRow(VisTable table, String attributeName, Integer modifier) {
        if (modifier == null || modifier == 0) {
            return; // Skip zero modifiers
        }
        
        table.row();
        table.add(new VisLabel(attributeName + ":")).left().width(150);
        
        String modifierText = modifier > 0 ? "+" + modifier : String.valueOf(modifier);
        VisLabel modifierLabel = new VisLabel(modifierText);
        
        // Color coding: green for positive, red for negative
        if (modifier > 0) {
            modifierLabel.setColor(0.0f, 1.0f, 0.0f, 1.0f); // Green
        } else {
            modifierLabel.setColor(1.0f, 0.0f, 0.0f, 1.0f); // Red
        }
        
        table.add(modifierLabel).left();
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
        
        // Current value (v2.0 - 3d6 system: display as integer)
        String valueText;
        if (currentValue != null) {
            // Display as integer for 3d6 system (3-23 range)
            int intValue = Math.round(currentValue);
            valueText = String.valueOf(intValue);
        } else {
            valueText = "N/A";
        }
        VisLabel valueLabel = new VisLabel(valueText);
        
        // Color coding for exceptional attributes (20+)
        if (currentValue != null && currentValue >= 20) {
            valueLabel.setColor(1.0f, 0.84f, 0.0f, 1.0f); // Gold for exceptional (20+)
        } else if (currentValue != null && currentValue >= 18) {
            valueLabel.setColor(0.0f, 1.0f, 0.0f, 1.0f); // Green for excellent (18-19)
        } else if (currentValue != null && currentValue >= 15) {
            valueLabel.setColor(0.5f, 1.0f, 0.5f, 1.0f); // Light green for good (15-17)
        } else if (currentValue != null && currentValue >= 12) {
            valueLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f); // White for average (12-14)
        } else if (currentValue != null && currentValue >= 9) {
            valueLabel.setColor(1.0f, 0.8f, 0.5f, 1.0f); // Orange for below average (9-11)
        } else if (currentValue != null) {
            valueLabel.setColor(1.0f, 0.5f, 0.5f, 1.0f); // Red for poor (3-8)
        }
        
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

