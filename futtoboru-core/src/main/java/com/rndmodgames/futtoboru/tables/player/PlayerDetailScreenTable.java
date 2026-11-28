package com.rndmodgames.futtoboru.tables.player;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import com.badlogic.gdx.Game;
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
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

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
    private DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.NUMERIC); // U-12: the game language
    
    // UI Components
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
        
        
        // Player name (will be set dynamically)
        playerNameLabel = new VisLabel("");
        playerNameLabel.setFontScale(UiTheme.fontScale("display"));
        
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
            Log.error(Log.UI_CLUB, "PlayerDetailScreenTable", "Cannot update: player or person is null");
            return;
        }
        
        this.currentPlayer = player;
        
        // Update change calculator if game changed
        if (changeCalculator == null && futtoboru != null && futtoboru.getCurrentGame() != null) {
            this.changeCalculator = new AttributeChangeCalculator(futtoboru.getCurrentGame());
        }
        
        // Clear content table (not the main table - that has the scroll pane)
        contentTable.clear();
        
        // Top section: player name (Back is in the top bar, U-04b)
        contentTable.row();
        contentTable.add(playerNameLabel).colspan(2).center().padBottom(UiTheme.space("lg"));
        contentTable.row();
        
        // Player Information Section
        buildInfoSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(UiTheme.space("sm"));
        contentTable.row();
        
        // Contract Details Section (v1.0)
        buildContractSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(UiTheme.space("sm"));
        contentTable.row();
        
        // Modifiers & Bonuses Section (v2.0 - 3d6 system)
        buildModifiersSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(UiTheme.space("sm"));
        contentTable.row();
        
        // Physical Attributes Section
        buildPhysicalAttributesSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(UiTheme.space("sm"));
        contentTable.row();
        
        // Mental Attributes Section
        buildMentalAttributesSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(UiTheme.space("sm"));
        contentTable.row();
        
        // Technical Attributes Section
        buildTechnicalAttributesSection();
        contentTable.row();
        contentTable.addSeparator().colspan(2).pad(UiTheme.space("sm"));
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
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_name"))).left().width(UiTheme.size("col.label"));
        infoTable.add(new VisLabel(currentPlayer.getPerson().getName() + " " + currentPlayer.getPerson().getLastname())).left();
        infoTable.row();
        
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_age"))).left().width(UiTheme.size("col.label"));
        infoTable.add(new VisLabel(LanguageModLoader.format("player_age_years", age))).left();
        infoTable.row();
        
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_nationality"))).left().width(UiTheme.size("col.label"));
        String countryName = currentPlayer.getPerson().getCountry() != null ? 
            currentPlayer.getPerson().getCountry().getCommonName() : LanguageModLoader.getValue("not_available");
        infoTable.add(new VisLabel(countryName)).left();
        infoTable.row();
        
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_birth_date"))).left().width(UiTheme.size("col.label"));
        infoTable.add(new VisLabel(dateFormatter.format(currentPlayer.getPerson().getBirthDate()))).left();
        infoTable.row();
        
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_current_club"))).left().width(UiTheme.size("col.label"));
        String clubName = LanguageModLoader.getValue("not_available");
        if (currentPlayer.getPerson().getCurrentClubId() != null) {
            com.rndmodgames.futtoboru.data.Club club = futtoboru.getCurrentGame().getClubById(
                currentPlayer.getPerson().getCurrentClubId());
            if (club != null) {
                clubName = club.getName();
            }
        }
        infoTable.add(new VisLabel(clubName)).left();
        infoTable.row();
        
        // E-01: this season's position in the 2-3-5
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_position"))).left().width(UiTheme.size("col.label"));
        infoTable.add(new VisLabel(currentPlayer.getPosition() == null ? LanguageModLoader.getValue("position_unknown")
                : LanguageModLoader.getValue(currentPlayer.getPosition().labelKey()))).left();
        infoTable.row();

        // Player Profession (day job for amateur/semi-pro)
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_profession"))).left().width(UiTheme.size("col.label"));
        String professionName = LanguageModLoader.getValue("player_profession_none");
        if (currentPlayer.getPlayerProfession() != null) {
            professionName = currentPlayer.getPlayerProfession().getName();
        }
        infoTable.add(new VisLabel(professionName)).left();
        infoTable.row();
        
        // Region/State (for region modifiers)
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_region"))).left().width(UiTheme.size("col.label"));
        String regionName = LanguageModLoader.getValue("not_available");
        if (currentPlayer.getPerson().getState() != null && 
            currentPlayer.getPerson().getState().getName() != null) {
            regionName = currentPlayer.getPerson().getState().getName();
        }
        infoTable.add(new VisLabel(regionName)).left();
        infoTable.row();
        
        // Contract Information (v1.0)
        infoTable.add(new VisLabel(LanguageModLoader.getValue("player_contract"))).left().width(UiTheme.size("col.label"));
        String contractInfo = LanguageModLoader.getValue("player_no_contract");
        if (currentPlayer.getPerson().getCurrentClubId() != null && futtoboru.getCurrentGame() != null) {
            com.rndmodgames.futtoboru.data.Club playerClub = futtoboru.getCurrentGame().getClubById(
                currentPlayer.getPerson().getCurrentClubId());
            if (playerClub != null) {
                com.rndmodgames.futtoboru.data.PlayerContract contract = playerClub.getContractForPlayer(
                    currentPlayer.getId() != null ? currentPlayer.getId() : currentPlayer.getPerson().getId());
                if (contract != null) {
                    contractInfo = LanguageModLoader.format("player_contract_summary", com.rndmodgames.futtoboru.data.ContractType.getName(contract.getContractType()),
                        "£" + contract.getWeeklyWage().setScale(2, java.math.RoundingMode.HALF_UP));
                }
            }
        }
        infoTable.add(new VisLabel(contractInfo)).left();
        
        contentTable.add(infoTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * Build contract details section (v1.0)
     * Shows contract type, wages, bonuses, and expiry
     */
    private void buildContractSection() {
        VisTable contractTable = new VisTable(true);
        
        contractTable.row();
        contractTable.add(new VisLabel(LanguageModLoader.getValue("player_contract_details"))).colspan(2).left().padBottom(UiTheme.space("xs"));
        contractTable.row();
        
        // Get contract
        com.rndmodgames.futtoboru.data.PlayerContract contract = null;
        if (currentPlayer.getPerson().getCurrentClubId() != null && futtoboru.getCurrentGame() != null) {
            com.rndmodgames.futtoboru.data.Club playerClub = futtoboru.getCurrentGame().getClubById(
                currentPlayer.getPerson().getCurrentClubId());
            
            Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "Looking for contract - Player ID: " + 
                currentPlayer.getId() + ", Person ID: " + 
                (currentPlayer.getPerson() != null ? currentPlayer.getPerson().getId() : "null") + 
                ", Club: " + (playerClub != null ? playerClub.getName() : "null"));
            
            if (playerClub != null) {
                // Try player ID first, then person ID as fallback
                Long lookupId = currentPlayer.getId() != null ? currentPlayer.getId() : 
                    (currentPlayer.getPerson() != null ? currentPlayer.getPerson().getId() : null);
                if (lookupId != null) {
                    contract = playerClub.getContractForPlayer(lookupId);
                    if (contract == null) {
                        Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "Contract lookup returned null for player ID: " + lookupId);
                    } else {
                        Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "Found contract ID: " + contract.getId() + 
                            " for player ID: " + lookupId);
                    }
                } else {
                    Log.error(Log.UI_CLUB, "PlayerDetailScreenTable", "Cannot lookup contract: both player ID and person ID are null");
                }
            } else {
                Log.error(Log.UI_CLUB, "PlayerDetailScreenTable", "Player club not found for club ID: " + 
                    currentPlayer.getPerson().getCurrentClubId());
            }
        } else {
            Log.error(Log.UI_CLUB, "PlayerDetailScreenTable", "Cannot lookup contract: currentClubId=" + 
                (currentPlayer.getPerson() != null ? currentPlayer.getPerson().getCurrentClubId() : "null") + 
                ", currentGame=" + (futtoboru.getCurrentGame() != null ? "not null" : "null"));
        }
        
        if (contract != null) {
            // Contract Type
            contractTable.row();
            contractTable.add(new VisLabel(LanguageModLoader.getValue("player_contract_type"))).left().width(UiTheme.size("col.label"));
            contractTable.add(new VisLabel(com.rndmodgames.futtoboru.data.ContractType.getName(contract.getContractType()))).left();
            contractTable.row();
            
            // Weekly Wage
            contractTable.add(new VisLabel(LanguageModLoader.getValue("player_weekly_wage"))).left().width(UiTheme.size("col.label"));
            contractTable.add(new VisLabel("£" + contract.getWeeklyWage().setScale(2, java.math.RoundingMode.HALF_UP))).left();
            contractTable.row();
            
            // Contract Dates
            if (contract.getStartDate() != null) {
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_start_date"))).left().width(UiTheme.size("col.label"));
                contractTable.add(new VisLabel(dateFormatter.format(contract.getStartDate()))).left();
                contractTable.row();
            }
            
            if (contract.getEndDate() != null) {
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_end_date"))).left().width(UiTheme.size("col.label"));
                contractTable.add(new VisLabel(dateFormatter.format(contract.getEndDate()))).left();
                contractTable.row();
                
                // Days remaining
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                    futtoboru.getCurrentGame().getGameDate(), contract.getEndDate());
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_days_remaining"))).left().width(UiTheme.size("col.label"));
                VisLabel daysLabel = new VisLabel(String.valueOf(daysRemaining));
                if (daysRemaining < 90) {
                    daysLabel.setColor(UiTheme.color("state.warning")); // Orange for expiring soon
                }
                if (daysRemaining < 30) {
                    daysLabel.setColor(UiTheme.color("state.critical")); // Red for expiring very soon
                }
                contractTable.add(daysLabel).left();
                contractTable.row();
            }
            
            // Bonuses (only show if non-zero)
            if (contract.getSigningBonus() != null && contract.getSigningBonus().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_signing_bonus"))).left().width(UiTheme.size("col.label"));
                contractTable.add(new VisLabel("£" + contract.getSigningBonus().setScale(2, java.math.RoundingMode.HALF_UP))).left();
                contractTable.row();
            }
            
            if (contract.getGoalBonus() != null && contract.getGoalBonus().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_goal_bonus"))).left().width(UiTheme.size("col.label"));
                contractTable.add(new VisLabel(LanguageModLoader.format("player_per_goal", "£" + contract.getGoalBonus().setScale(2, java.math.RoundingMode.HALF_UP)))).left();
                contractTable.row();
            }
            
            if (contract.getAppearanceFee() != null && contract.getAppearanceFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_appearance_fee"))).left().width(UiTheme.size("col.label"));
                contractTable.add(new VisLabel(LanguageModLoader.format("player_per_match", "£" + contract.getAppearanceFee().setScale(2, java.math.RoundingMode.HALF_UP)))).left();
                contractTable.row();
            }
            
            if (contract.getLeagueWinBonus() != null && contract.getLeagueWinBonus().compareTo(java.math.BigDecimal.ZERO) > 0) {
                contractTable.add(new VisLabel(LanguageModLoader.getValue("player_league_win_bonus"))).left().width(UiTheme.size("col.label"));
                contractTable.add(new VisLabel("£" + contract.getLeagueWinBonus().setScale(2, java.math.RoundingMode.HALF_UP))).left();
                contractTable.row();
            }
        } else {
            contractTable.row();
            contractTable.add(new VisLabel(LanguageModLoader.getValue("player_no_contract_found"))).colspan(2).left();
        }
        
        contentTable.add(contractTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * Build modifiers and bonuses section (v2.0 - 3d6 system)
     * Shows nationality modifiers, region modifiers, and profession bonuses
     */
    private void buildModifiersSection() {
        VisTable modifiersTable = new VisTable(true);
        
        modifiersTable.row();
        modifiersTable.add(new VisLabel(LanguageModLoader.getValue("player_modifiers_title"))).colspan(2).left().padBottom(UiTheme.space("xs"));
        modifiersTable.row();
        
        // Get nationality modifier
        NationalityModifier natMod = null;
        if (currentPlayer.getPerson().getCountry() != null && 
            currentPlayer.getPerson().getCountry().getId() != null) {
            Long countryId = currentPlayer.getPerson().getCountry().getId();
            natMod = com.rndmodgames.futtoboru.system.DatabaseLoader.getNationalityModifier(countryId);
            
            // Debug logging
            if (natMod == null) {
                Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "No nationality modifier found for country ID: " + 
                    countryId + " (" + currentPlayer.getPerson().getCountry().getCommonName() + ")");
            }
        } else {
            Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "Player has no country set");
        }
        
        // Get region modifier
        RegionModifier regMod = null;
        if (currentPlayer.getPerson().getState() != null && 
            currentPlayer.getPerson().getState().getName() != null &&
            currentPlayer.getPerson().getCountry() != null && 
            currentPlayer.getPerson().getCountry().getId() != null) {
            String stateName = currentPlayer.getPerson().getState().getName();
            Long countryId = currentPlayer.getPerson().getCountry().getId();
            regMod = com.rndmodgames.futtoboru.system.DatabaseLoader.getRegionModifier(stateName, countryId);
            
            // Debug logging
            if (regMod == null) {
                Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "No region modifier found for state: " + 
                    stateName + ", country ID: " + countryId);
            }
        } else {
            if (currentPlayer.getPerson().getState() == null) {
                Log.debug(Log.UI_CLUB, "PlayerDetailScreenTable", "Player has no state/region set");
            }
        }
        
        // Nationality Modifiers
        if (natMod != null) {
            modifiersTable.row();
            modifiersTable.add(new VisLabel(LanguageModLoader.format("player_nationality_modifiers", natMod.getCountryName()))).colspan(2).left().padTop(UiTheme.space("xs"));
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
            modifiersTable.add(new VisLabel(LanguageModLoader.getValue("player_nationality_modifiers_none"))).colspan(2).left().padTop(UiTheme.space("xs"));
        }
        
        modifiersTable.row();
        modifiersTable.addSeparator().colspan(2).pad(UiTheme.space("xs"));
        
        // Region Modifiers
        if (regMod != null) {
            modifiersTable.row();
            modifiersTable.add(new VisLabel(LanguageModLoader.format("player_region_modifiers", regMod.getRegionName()))).colspan(2).left().padTop(UiTheme.space("xs"));
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
            modifiersTable.add(new VisLabel(LanguageModLoader.getValue("player_region_modifiers_none"))).colspan(2).left().padTop(UiTheme.space("xs"));
        }
        
        modifiersTable.row();
        modifiersTable.addSeparator().colspan(2).pad(UiTheme.space("xs"));
        
        // Profession Bonuses (only for amateur/semi-pro)
        if (currentPlayer.getPlayerProfession() != null) {
            PlayerProfession profession = currentPlayer.getPlayerProfession();
            modifiersTable.row();
            modifiersTable.add(new VisLabel(LanguageModLoader.format("player_profession_bonuses", profession.getName()))).colspan(2).left().padTop(UiTheme.space("xs"));
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
            modifiersTable.add(new VisLabel(LanguageModLoader.getValue("player_profession_bonuses_none"))).colspan(2).left().padTop(UiTheme.space("xs"));
        }
        
        contentTable.add(modifiersTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * The on-screen label of an attribute: {@code attributeName} is the English id the change calculator uses
     * ("Long Shots"), the text comes from the bundle key {@code attr_long_shots}.
     */
    private static String attributeLabel(String attributeName) {
        String key = "attr_" + attributeName.toLowerCase(java.util.Locale.ROOT).replace(' ', '_').replace('-', '_');
        return LanguageModLoader.format("label_with_colon", LanguageModLoader.getValue(key));
    }

    /**
     * Add a modifier row (only shows if modifier is non-zero)
     */
    private void addModifierRow(VisTable table, String attributeName, Integer modifier) {
        if (modifier == null || modifier == 0) {
            return; // Skip zero modifiers
        }
        
        table.row();
        table.add(new VisLabel(attributeLabel(attributeName))).left().width(UiTheme.size("col.label"));
        
        String modifierText = modifier > 0 ? "+" + modifier : String.valueOf(modifier);
        VisLabel modifierLabel = new VisLabel(modifierText);
        
        // Color coding: green for positive, red for negative
        if (modifier > 0) {
            modifierLabel.setColor(UiTheme.color("modifier.up")); // Green
        } else {
            modifierLabel.setColor(UiTheme.color("modifier.down")); // Red
        }
        
        table.add(modifierLabel).left();
    }
    
    /**
     * Build physical attributes section
     */
    private void buildPhysicalAttributesSection() {
        physicalAttributesTable.clear();
        
        physicalAttributesTable.row();
        physicalAttributesTable.add(new VisLabel(LanguageModLoader.getValue("player_physical_attributes"))).colspan(3).left().padBottom(UiTheme.space("xs"));
        physicalAttributesTable.row();
        
        addAttributeRow(physicalAttributesTable, "Acceleration", currentPlayer.getAcceleration());
        addAttributeRow(physicalAttributesTable, "Speed", currentPlayer.getSpeed());
        addAttributeRow(physicalAttributesTable, "Stamina", currentPlayer.getStamina());
        addAttributeRow(physicalAttributesTable, "Strength", currentPlayer.getStrength());
        addAttributeRow(physicalAttributesTable, "Endurance", currentPlayer.getEndurance());
        addAttributeRow(physicalAttributesTable, "Jumping", currentPlayer.getJumping());
        addAttributeRow(physicalAttributesTable, "Dexterity", currentPlayer.getDexterity());
        
        contentTable.add(physicalAttributesTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * Build mental attributes section
     */
    private void buildMentalAttributesSection() {
        mentalAttributesTable.clear();
        
        mentalAttributesTable.row();
        mentalAttributesTable.add(new VisLabel(LanguageModLoader.getValue("player_mental_attributes"))).colspan(3).left().padBottom(UiTheme.space("xs"));
        mentalAttributesTable.row();
        
        addAttributeRow(mentalAttributesTable, "Concentration", currentPlayer.getConcentration());
        addAttributeRow(mentalAttributesTable, "Courage", currentPlayer.getCourage());
        addAttributeRow(mentalAttributesTable, "Determination", currentPlayer.getDetermination());
        addAttributeRow(mentalAttributesTable, "Leadership", currentPlayer.getLeadership());
        addAttributeRow(mentalAttributesTable, "Perception", currentPlayer.getPerception());
        addAttributeRow(mentalAttributesTable, "Positioning", currentPlayer.getPositioning());
        addAttributeRow(mentalAttributesTable, "Teamwork", currentPlayer.getTeamwork());
        
        contentTable.add(mentalAttributesTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * Build technical attributes section
     */
    private void buildTechnicalAttributesSection() {
        technicalAttributesTable.clear();
        
        technicalAttributesTable.row();
        technicalAttributesTable.add(new VisLabel(LanguageModLoader.getValue("player_technical_attributes"))).colspan(3).left().padBottom(UiTheme.space("xs"));
        technicalAttributesTable.row();
        
        addAttributeRow(technicalAttributesTable, "Dribbling", currentPlayer.getDribbling());
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
        
        contentTable.add(technicalAttributesTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * Build goalkeeper attributes section
     */
    private void buildGoalkeeperAttributesSection() {
        goalkeeperAttributesTable.clear();
        
        goalkeeperAttributesTable.row();
        goalkeeperAttributesTable.add(new VisLabel(LanguageModLoader.getValue("player_goalkeeper_attributes"))).colspan(3).left().padBottom(UiTheme.space("xs"));
        goalkeeperAttributesTable.row();
        
        addAttributeRow(goalkeeperAttributesTable, "Shot Stopping", currentPlayer.getShotStopping());
        addAttributeRow(goalkeeperAttributesTable, "Area Control", currentPlayer.getAreaControl());
        addAttributeRow(goalkeeperAttributesTable, "Punching", currentPlayer.getPunching());
        addAttributeRow(goalkeeperAttributesTable, "Hand to Hand", currentPlayer.getHandToHand());
        addAttributeRow(goalkeeperAttributesTable, "Rushing Out", currentPlayer.getRushingOut());
        addAttributeRow(goalkeeperAttributesTable, "Area Positioning", currentPlayer.getAreaPositioning());
        
        contentTable.add(goalkeeperAttributesTable).left().pad(UiTheme.space("sm"));
    }
    
    /**
     * Add an attribute row with change tracking
     */
    /** @param attributeKey the attribute's id for the change tracking; the label comes from its attr_* key */
    private void addAttributeRow(VisTable table, String attributeKey, Float currentValue) {
        table.row();

        // Attribute name
        table.add(new VisLabel(attributeLabel(attributeKey))).left().width(UiTheme.size("col.label"));
        
        // Current value (v2.0 - 3d6 system: display as integer)
        String valueText;
        if (currentValue != null) {
            // Display as integer for 3d6 system (3-23 range)
            int intValue = Math.round(currentValue);
            valueText = String.valueOf(intValue);
        } else {
            valueText = LanguageModLoader.getValue("not_available");
        }
        VisLabel valueLabel = new VisLabel(valueText);
        
        // Color coding for exceptional attributes (20+)
        if (currentValue != null && currentValue >= 20) {
            valueLabel.setColor(UiTheme.color("rating.exceptional")); // Gold for exceptional (20+)
        } else if (currentValue != null && currentValue >= 18) {
            valueLabel.setColor(UiTheme.color("rating.excellent")); // Green for excellent (18-19)
        } else if (currentValue != null && currentValue >= 15) {
            valueLabel.setColor(UiTheme.color("rating.good")); // Light green for good (15-17)
        } else if (currentValue != null && currentValue >= 12) {
            valueLabel.setColor(UiTheme.color("rating.average")); // White for average (12-14)
        } else if (currentValue != null && currentValue >= 9) {
            valueLabel.setColor(UiTheme.color("rating.below")); // Orange for below average (9-11)
        } else if (currentValue != null) {
            valueLabel.setColor(UiTheme.color("rating.poor")); // Red for poor (3-8)
        }
        
        table.add(valueLabel).left().width(UiTheme.size("col.value"));
        
        // Change indicator (30-day tracking)
        String changeText = formatAttributeChange(attributeKey, currentValue);
        VisLabel changeLabel = new VisLabel(changeText);
        
        // Color coding based on trend
        if (changeText.contains("[+")) { // U-12: the font has no arrows; the sign and the colour say it
            changeLabel.setColor(UiTheme.color("trend.up")); // Green for improvement
        } else if (changeText.contains("[-")) {
            changeLabel.setColor(UiTheme.color("trend.down")); // Red for decline
        } else if (changeText.contains("=")) {
            changeLabel.setColor(UiTheme.color("trend.flat")); // Gray for stable
        }
        
        table.add(changeLabel).left().width(UiTheme.size("col.long"));
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

