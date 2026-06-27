package com.rndmodgames.futtoboru.system.generators;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.NationalityModifier;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.data.RegionModifier;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.DebugLogManager;
import com.rndmodgames.futtoboru.system.loaders.NationalityModifiersLoader;
import com.rndmodgames.futtoboru.system.loaders.RegionModifiersLoader;

/**
 * Player Attribute Generator v1
 * 
 * Generates and manages player attributes based on age, position, and reputation.
 * 
 * @author Geomancer86
 */
public class PlayerAttributeGenerator {
    
    /**
     * Generate all attributes for a player (v2.0 - 3d6 system)
     * 
     * Uses 3d6 (3-18 range) for base attributes, with modifiers from:
     * - Age (small adjustments)
     * - Nationality (country modifiers)
     * - Region (local modifiers)
     * - Profession (day job bonuses for amateur/semi-pro)
     * 
     * Maximum possible: 23 (3-18 base + up to 5 from modifiers)
     */
    public void generatePlayerAttributes(Player player, Person person, LocalDateTime currentDate) {
        
        if (player == null || person == null) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_GENERATION, "PlayerAttributeGenerator", "Cannot generate attributes: player or person is null");
            return;
        }
        
        // Calculate age
        int age = (int) ChronoUnit.YEARS.between(person.getBirthDate(), currentDate);
        
        // Calculate age adjustment (small, ±1 to ±2)
        int ageAdjustment = calculateAgeAdjustment(age);
        
        // Get nationality modifier (attribute-specific)
        NationalityModifier natMod = null;
        if (person.getCountry() != null && person.getCountry().getId() != null) {
            natMod = NationalityModifiersLoader.getModifier(person.getCountry().getId());
        }
        
        // Get region modifier (attribute-specific, based on state/city)
        // For now, use state name if available, or skip if city/state not available
        RegionModifier regMod = null;
        if (person.getState() != null && person.getState().getName() != null && 
            person.getCountry() != null && person.getCountry().getId() != null) {
            // Try to find region by state name
            regMod = RegionModifiersLoader.getModifier(
                person.getState().getName(), person.getCountry().getId());
        }
        
        // Generate Physical Attributes (3d6 + age + nationality + region modifiers)
        generatePhysicalAttributes(player, ageAdjustment, natMod, regMod);
        
        // Generate Mental Attributes (3d6 + age + nationality + region modifiers)
        generateMentalAttributes(player, ageAdjustment, natMod, regMod);
        
        // Generate Technical Attributes (3d6 + age + nationality + region modifiers)
        generateTechnicalAttributes(player, ageAdjustment, natMod, regMod);
        
        // Generate Goalkeeper Attributes (3d6 + age + nationality + region modifiers)
        generateGoalkeeperAttributes(player, ageAdjustment, natMod, regMod);
        
        // Apply profession bonuses (for amateur/semi-pro players)
        // Profession bonuses are applied directly (no conversion needed with 3-18 system)
        applyProfessionBonuses(player);
        
        DebugLogManager.getInstance().debug(DebugLogManager.CATEGORY_DATA_GENERATION, "PlayerAttributeGenerator", "Generated attributes for: " + person.getName() + 
                     " (Age: " + age + ", Age Adj: " + ageAdjustment + ")");
    }
    
    /**
     * Apply profession bonuses to player attributes (v2.0 - 3d6 system)
     * 
     * Profession bonuses are applied directly (attributes are already in 3-18+ scale).
     * Can push attributes to 20+ (up to 23 maximum).
     * Only applies to amateur/semi-professional players (professional players don't have day jobs).
     */
    private void applyProfessionBonuses(Player player) {
        if (player == null || player.getPlayerProfession() == null) {
            return; // No profession, no bonuses
        }
        
        PlayerProfession profession = player.getPlayerProfession();
        
        // Apply physical attribute bonuses (direct addition, no conversion needed)
        if (profession.getStrengthBonus() != null && profession.getStrengthBonus() != 0) {
            applyAttributeBonus3d6(player, "strength", profession.getStrengthBonus());
        }
        if (profession.getEnduranceBonus() != null && profession.getEnduranceBonus() != 0) {
            applyAttributeBonus3d6(player, "endurance", profession.getEnduranceBonus());
        }
        if (profession.getStaminaBonus() != null && profession.getStaminaBonus() != 0) {
            applyAttributeBonus3d6(player, "stamina", profession.getStaminaBonus());
        }
        if (profession.getSpeedBonus() != null && profession.getSpeedBonus() != 0) {
            applyAttributeBonus3d6(player, "speed", profession.getSpeedBonus());
        }
        if (profession.getAccelerationBonus() != null && profession.getAccelerationBonus() != 0) {
            applyAttributeBonus3d6(player, "acceleration", profession.getAccelerationBonus());
        }
        if (profession.getJumpingBonus() != null && profession.getJumpingBonus() != 0) {
            applyAttributeBonus3d6(player, "jumping", profession.getJumpingBonus());
        }
        if (profession.getDexterityBonus() != null && profession.getDexterityBonus() != 0) {
            applyAttributeBonus3d6(player, "dexterity", profession.getDexterityBonus());
        }
        
        // Apply mental attribute bonuses
        if (profession.getConcentrationBonus() != null && profession.getConcentrationBonus() != 0) {
            applyAttributeBonus3d6(player, "concentration", profession.getConcentrationBonus());
        }
        if (profession.getCourageBonus() != null && profession.getCourageBonus() != 0) {
            applyAttributeBonus3d6(player, "courage", profession.getCourageBonus());
        }
        if (profession.getDeterminationBonus() != null && profession.getDeterminationBonus() != 0) {
            applyAttributeBonus3d6(player, "determination", profession.getDeterminationBonus());
        }
        if (profession.getLeadershipBonus() != null && profession.getLeadershipBonus() != 0) {
            applyAttributeBonus3d6(player, "leadership", profession.getLeadershipBonus());
        }
        if (profession.getPerceptionBonus() != null && profession.getPerceptionBonus() != 0) {
            applyAttributeBonus3d6(player, "perception", profession.getPerceptionBonus());
        }
        if (profession.getPositioningBonus() != null && profession.getPositioningBonus() != 0) {
            applyAttributeBonus3d6(player, "positioning", profession.getPositioningBonus());
        }
        if (profession.getTeamworkBonus() != null && profession.getTeamworkBonus() != 0) {
            applyAttributeBonus3d6(player, "teamwork", profession.getTeamworkBonus());
        }
    }
    
    /**
     * Apply a profession bonus to a specific attribute (v2.0 - 3d6 system)
     * 
     * Attributes are already in 3-18+ scale, so just add bonus directly.
     * Maximum is 23 (3-18 base + up to 5 from modifiers).
     */
    private void applyAttributeBonus3d6(Player player, String attributeName, Integer bonus) {
        if (bonus == null || bonus == 0) {
            return;
        }
        
        // Get current attribute value (already in 3-18+ scale)
        Float currentValue = getAttributeValue(player, attributeName);
        if (currentValue == null) {
            return;
        }
        
        // Apply bonus directly
        float newValue = currentValue + bonus;
        
        // Cap at 23 (maximum with all modifiers)
        newValue = Math.min(23.0f, Math.max(3.0f, newValue));
        
        // Set the attribute
        setAttributeValue(player, attributeName, newValue);
    }
    
    /**
     * Get attribute value by name (helper method)
     */
    private Float getAttributeValue(Player player, String attributeName) {
        switch (attributeName.toLowerCase()) {
            case "strength": return player.getStrength();
            case "endurance": return player.getEndurance();
            case "stamina": return player.getStamina();
            case "speed": return player.getSpeed();
            case "acceleration": return player.getAcceleration();
            case "jumping": return player.getJumping();
            case "dexterity": return player.getDexterity();
            case "concentration": return player.getConcentration();
            case "courage": return player.getCourage();
            case "determination": return player.getDetermination();
            case "leadership": return player.getLeadership();
            case "perception": return player.getPerception();
            case "positioning": return player.getPositioning();
            case "teamwork": return player.getTeamwork();
            default: return null;
        }
    }
    
    /**
     * Set attribute value by name (helper method)
     */
    private void setAttributeValue(Player player, String attributeName, float value) {
        value = clampAttribute3d6((int)value); // Ensure 3-23 range (v2.0 - 3d6 system)
        
        switch (attributeName.toLowerCase()) {
            case "strength": player.setStrength(value); break;
            case "endurance": player.setEndurance(value); break;
            case "stamina": player.setStamina(value); break;
            case "speed": player.setSpeed(value); break;
            case "acceleration": player.setAcceleration(value); break;
            case "jumping": player.setJumping(value); break;
            case "dexterity": player.setDexterity(value); break;
            case "concentration": player.setConcentration(value); break;
            case "courage": player.setCourage(value); break;
            case "determination": player.setDetermination(value); break;
            case "leadership": player.setLeadership(value); break;
            case "perception": player.setPerception(value); break;
            case "positioning": player.setPositioning(value); break;
            case "teamwork": player.setTeamwork(value); break;
        }
    }
    
    /**
     * Roll 3d6 for attribute generation (D&D style)
     * Returns value between 3 and 18
     */
    private int roll3d6() {
        int die1 = DatabaseLoader.RNG.nextInt(6) + 1; // 1-6
        int die2 = DatabaseLoader.RNG.nextInt(6) + 1; // 1-6
        int die3 = DatabaseLoader.RNG.nextInt(6) + 1; // 1-6
        return die1 + die2 + die3; // 3-18
    }
    
    /**
     * Calculate age adjustment for attributes (v2.0)
     * Small adjustments (±1 to ±2) based on age
     * Peak performance around age 25-28
     */
    private int calculateAgeAdjustment(int age) {
        if (age < 18) {
            return -2; // Young players: developing
        } else if (age < 22) {
            return -1; // Young adults: improving
        } else if (age <= 25) {
            return 0; // Prime developing
        } else if (age <= 28) {
            return +2; // Peak performance
        } else if (age <= 32) {
            return +1; // Still strong
        } else if (age <= 35) {
            return 0; // Beginning decline
        } else if (age <= 38) {
            return -1; // Noticeable decline
        } else {
            return -2; // Rapid decline
        }
    }
    
    /**
     * Generate physical attributes (v2.0 - 3d6 system)
     * 
     * Each attribute: 3d6 roll + age adjustment + nationality modifier + region modifier
     * Range: 3-18 base, can reach 20+ with modifiers
     */
    private void generatePhysicalAttributes(Player player, int ageAdjustment, NationalityModifier natMod, RegionModifier regMod) {
        // Roll 3d6 for each attribute (independent rolls) + attribute-specific modifiers
        player.setAcceleration(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "acceleration")));
        player.setSpeed(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "speed")));
        player.setStamina(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "stamina")));
        player.setStrength(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "strength")));
        player.setEndurance(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "endurance")));
        player.setJumping(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "jumping")));
        player.setDexterity(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "dexterity")));
    }
    
    /**
     * Generate mental attributes (v2.0 - 3d6 system)
     */
    private void generateMentalAttributes(Player player, int ageAdjustment, NationalityModifier natMod, RegionModifier regMod) {
        // Roll 3d6 for each attribute (independent rolls) + attribute-specific modifiers
        player.setConcentration(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "concentration")));
        player.setCourage(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "courage")));
        player.setDetermination(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "determination")));
        player.setLeadership(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "leadership")));
        player.setPerception(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "perception")));
        player.setPositioning(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "positioning")));
        player.setTeamwork(clampAttribute3d6(roll3d6() + ageAdjustment + 
            getModifier(natMod, regMod, "teamwork")));
    }
    
    /**
     * Generate technical attributes (v2.0 - 3d6 system)
     * 
     * Technical attributes don't have direct nationality/region modifiers,
     * but can be influenced by dexterity/concentration modifiers
     */
    private void generateTechnicalAttributes(Player player, int ageAdjustment, NationalityModifier natMod, RegionModifier regMod) {
        // Technical attributes use dexterity/concentration modifiers as base
        int techBaseMod = getModifier(natMod, regMod, "dexterity") + getModifier(natMod, regMod, "concentration");
        techBaseMod = techBaseMod / 2; // Average of dexterity and concentration modifiers
        
        // Roll 3d6 for each attribute (independent rolls) + base modifier
        player.setPassing(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setKicking(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setLongShots(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setTrickShots(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setHeading(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setOneTwos(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setFreeKicks(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setCornerKicks(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setPenaltyKicks(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setThrowIns(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setMarking(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
        player.setTackling(clampAttribute3d6(roll3d6() + ageAdjustment + techBaseMod));
    }
    
    /**
     * Generate goalkeeper attributes (v2.0 - 3d6 system)
     * 
     * Goalkeeper attributes use perception/positioning modifiers as base
     */
    private void generateGoalkeeperAttributes(Player player, int ageAdjustment, NationalityModifier natMod, RegionModifier regMod) {
        // Goalkeeper attributes use perception/positioning modifiers as base
        int gkBaseMod = getModifier(natMod, regMod, "perception") + getModifier(natMod, regMod, "positioning");
        gkBaseMod = gkBaseMod / 2; // Average of perception and positioning modifiers
        
        // Roll 3d6 for each attribute (independent rolls) + base modifier
        player.setShotStopping(clampAttribute3d6(roll3d6() + ageAdjustment + gkBaseMod));
        player.setAreaControl(clampAttribute3d6(roll3d6() + ageAdjustment + gkBaseMod));
        player.setPunching(clampAttribute3d6(roll3d6() + ageAdjustment + gkBaseMod));
        player.setHandToHand(clampAttribute3d6(roll3d6() + ageAdjustment + gkBaseMod));
        player.setRushingOut(clampAttribute3d6(roll3d6() + ageAdjustment + gkBaseMod));
        player.setAreaPositioning(clampAttribute3d6(roll3d6() + ageAdjustment + gkBaseMod));
    }
    
    /**
     * Get modifier value for a specific attribute from nationality and region modifiers
     */
    private int getModifier(NationalityModifier natMod, RegionModifier regMod, String attributeName) {
        int total = 0;
        
        // Add nationality modifier
        if (natMod != null) {
            switch (attributeName.toLowerCase()) {
                case "strength": total += natMod.getStrengthModifier(); break;
                case "endurance": total += natMod.getEnduranceModifier(); break;
                case "stamina": total += natMod.getStaminaModifier(); break;
                case "speed": total += natMod.getSpeedModifier(); break;
                case "acceleration": total += natMod.getAccelerationModifier(); break;
                case "jumping": total += natMod.getJumpingModifier(); break;
                case "dexterity": total += natMod.getDexterityModifier(); break;
                case "concentration": total += natMod.getConcentrationModifier(); break;
                case "courage": total += natMod.getCourageModifier(); break;
                case "determination": total += natMod.getDeterminationModifier(); break;
                case "leadership": total += natMod.getLeadershipModifier(); break;
                case "perception": total += natMod.getPerceptionModifier(); break;
                case "positioning": total += natMod.getPositioningModifier(); break;
                case "teamwork": total += natMod.getTeamworkModifier(); break;
            }
        }
        
        // Add region modifier
        if (regMod != null) {
            switch (attributeName.toLowerCase()) {
                case "strength": total += regMod.getStrengthModifier(); break;
                case "endurance": total += regMod.getEnduranceModifier(); break;
                case "stamina": total += regMod.getStaminaModifier(); break;
                case "speed": total += regMod.getSpeedModifier(); break;
                case "acceleration": total += regMod.getAccelerationModifier(); break;
                case "jumping": total += regMod.getJumpingModifier(); break;
                case "dexterity": total += regMod.getDexterityModifier(); break;
                case "concentration": total += regMod.getConcentrationModifier(); break;
                case "courage": total += regMod.getCourageModifier(); break;
                case "determination": total += regMod.getDeterminationModifier(); break;
                case "leadership": total += regMod.getLeadershipModifier(); break;
                case "perception": total += regMod.getPerceptionModifier(); break;
                case "positioning": total += regMod.getPositioningModifier(); break;
                case "teamwork": total += regMod.getTeamworkModifier(); break;
            }
        }
        
        return total;
    }
    
    /**
     * Clamp attribute value to 3-23 range (v2.0 - 3d6 system)
     * 
     * Base: 3-18 (3d6)
     * With modifiers: Can reach 20+ (up to 23)
     */
    private float clampAttribute3d6(int value) {
        return Math.max(3.0f, Math.min(23.0f, value));
    }
    
    /**
     * Legacy method for backward compatibility (0-100 scale)
     * @deprecated Use clampAttribute3d6 for new 3-18 system
     */
    @Deprecated
    private float clampAttribute(float value) {
        return Math.max(0.0f, Math.min(100.0f, value));
    }
    
    /**
     * Apply daily attribute changes (for testing - simple random changes + age-based decline)
     * TODO: Replace with proper training system in Phase 3
     */
    public void applyDailyAttributeChanges(Player player, java.time.LocalDateTime currentDate) {
        if (player == null || player.getPerson() == null) return;
        
        // Calculate age
        int age = (int) java.time.temporal.ChronoUnit.YEARS.between(player.getPerson().getBirthDate(), currentDate);
        
        // Simple random changes for testing (+2 strength, -1 agility example)
        // This will be replaced by proper training system later
        
        // Randomly change one physical attribute (v2.0 - 3d6 system)
        // Small changes (±1) for daily variations
        int physicalAttr = DatabaseLoader.RNG.nextInt(7);
        int change = DatabaseLoader.RNG.nextInt(3) - 1; // -1, 0, or +1
        
        switch (physicalAttr) {
            case 0: player.setAcceleration(clampAttribute3d6((int)(player.getAcceleration() + change))); break;
            case 1: player.setSpeed(clampAttribute3d6((int)(player.getSpeed() + change))); break;
            case 2: player.setStamina(clampAttribute3d6((int)(player.getStamina() + change))); break;
            case 3: player.setStrength(clampAttribute3d6((int)(player.getStrength() + change))); break;
            case 4: player.setEndurance(clampAttribute3d6((int)(player.getEndurance() + change))); break;
            case 5: player.setJumping(clampAttribute3d6((int)(player.getJumping() + change))); break;
            case 6: player.setDexterity(clampAttribute3d6((int)(player.getDexterity() + change))); break;
        }
        
        // Randomly change one technical attribute (v2.0 - 3d6 system)
        int technicalAttr = DatabaseLoader.RNG.nextInt(12);
        change = DatabaseLoader.RNG.nextInt(3) - 1; // -1, 0, or +1
        
        switch (technicalAttr) {
            case 0: player.setPassing(clampAttribute3d6((int)(player.getPassing() + change))); break;
            case 1: player.setKicking(clampAttribute3d6((int)(player.getKicking() + change))); break;
            case 2: player.setLongShots(clampAttribute3d6((int)(player.getLongShots() + change))); break;
            case 3: player.setTrickShots(clampAttribute3d6((int)(player.getTrickShots() + change))); break;
            case 4: player.setHeading(clampAttribute3d6((int)(player.getHeading() + change))); break;
            case 5: player.setOneTwos(clampAttribute3d6((int)(player.getOneTwos() + change))); break;
            case 6: player.setFreeKicks(clampAttribute3d6((int)(player.getFreeKicks() + change))); break;
            case 7: player.setCornerKicks(clampAttribute3d6((int)(player.getCornerKicks() + change))); break;
            case 8: player.setPenaltyKicks(clampAttribute3d6((int)(player.getPenaltyKicks() + change))); break;
            case 9: player.setThrowIns(clampAttribute3d6((int)(player.getThrowIns() + change))); break;
            case 10: player.setMarking(clampAttribute3d6((int)(player.getMarking() + change))); break;
            case 11: player.setTackling(clampAttribute3d6((int)(player.getTackling() + change))); break;
        }
        
        // Apply age-based decline (v1.0)
        float declineFactor = calculateDeclineFactor(age);
        if (declineFactor > 0) {
            applyAgeDecline(player, declineFactor);
        }
    }
    
    /**
     * Calculate age-based decline factor
     */
    private float calculateDeclineFactor(int age) {
        if (age < 30) {
            return 0.0f; // No decline before 30
        } else if (age < 35) {
            return 0.01f + (DatabaseLoader.RNG.nextFloat() * 0.04f); // 0.01-0.05
        } else if (age < 40) {
            return 0.05f + (DatabaseLoader.RNG.nextFloat() * 0.05f); // 0.05-0.1
        } else {
            return 0.1f + (DatabaseLoader.RNG.nextFloat() * 0.1f); // 0.1-0.2
        }
    }
    
    /**
     * Apply age-based decline to all attributes (v2.0 - 3d6 system)
     * 
     * Decline is now in integer steps (not float) to match 3-18+ scale
     */
    private void applyAgeDecline(Player player, float declineFactor) {
        // Convert decline factor to integer steps (roughly -1 per 0.1 decline factor)
        int declineSteps = Math.max(0, (int)(declineFactor * 10));
        if (declineSteps == 0) {
            return; // No decline
        }
        
        // Decline all physical attributes
        if (player.getAcceleration() != null) player.setAcceleration(clampAttribute3d6((int)(player.getAcceleration() - declineSteps)));
        if (player.getSpeed() != null) player.setSpeed(clampAttribute3d6((int)(player.getSpeed() - declineSteps)));
        if (player.getStamina() != null) player.setStamina(clampAttribute3d6((int)(player.getStamina() - declineSteps)));
        if (player.getStrength() != null) player.setStrength(clampAttribute3d6((int)(player.getStrength() - declineSteps)));
        if (player.getEndurance() != null) player.setEndurance(clampAttribute3d6((int)(player.getEndurance() - declineSteps)));
        if (player.getJumping() != null) player.setJumping(clampAttribute3d6((int)(player.getJumping() - declineSteps)));
        if (player.getDexterity() != null) player.setDexterity(clampAttribute3d6((int)(player.getDexterity() - declineSteps)));
        
        // Mental attributes decline slower (half steps)
        int mentalDecline = Math.max(0, declineSteps / 2);
        if (mentalDecline > 0) {
            if (player.getConcentration() != null) player.setConcentration(clampAttribute3d6((int)(player.getConcentration() - mentalDecline)));
            if (player.getCourage() != null) player.setCourage(clampAttribute3d6((int)(player.getCourage() - mentalDecline)));
            if (player.getDetermination() != null) player.setDetermination(clampAttribute3d6((int)(player.getDetermination() - mentalDecline)));
            if (player.getPerception() != null) player.setPerception(clampAttribute3d6((int)(player.getPerception() - mentalDecline)));
        }
        
        // Leadership and positioning decline even slower (experience helps)
        int expDecline = Math.max(0, declineSteps / 3);
        if (expDecline > 0) {
            if (player.getLeadership() != null) player.setLeadership(clampAttribute3d6((int)(player.getLeadership() - expDecline)));
            if (player.getPositioning() != null) player.setPositioning(clampAttribute3d6((int)(player.getPositioning() - expDecline)));
            if (player.getTeamwork() != null) player.setTeamwork(clampAttribute3d6((int)(player.getTeamwork() - expDecline)));
        }
        
        // Technical attributes decline at full rate
        if (player.getPassing() != null) player.setPassing(clampAttribute3d6((int)(player.getPassing() - declineSteps)));
        if (player.getKicking() != null) player.setKicking(clampAttribute3d6((int)(player.getKicking() - declineSteps)));
        if (player.getLongShots() != null) player.setLongShots(clampAttribute3d6((int)(player.getLongShots() - declineSteps)));
        if (player.getTrickShots() != null) player.setTrickShots(clampAttribute3d6((int)(player.getTrickShots() - declineSteps)));
        if (player.getHeading() != null) player.setHeading(clampAttribute3d6((int)(player.getHeading() - declineSteps)));
        if (player.getOneTwos() != null) player.setOneTwos(clampAttribute3d6((int)(player.getOneTwos() - declineSteps)));
        if (player.getFreeKicks() != null) player.setFreeKicks(clampAttribute3d6((int)(player.getFreeKicks() - declineSteps)));
        if (player.getCornerKicks() != null) player.setCornerKicks(clampAttribute3d6((int)(player.getCornerKicks() - declineSteps)));
        if (player.getPenaltyKicks() != null) player.setPenaltyKicks(clampAttribute3d6((int)(player.getPenaltyKicks() - declineSteps)));
        if (player.getThrowIns() != null) player.setThrowIns(clampAttribute3d6((int)(player.getThrowIns() - declineSteps)));
        if (player.getMarking() != null) player.setMarking(clampAttribute3d6((int)(player.getMarking() - declineSteps)));
        if (player.getTackling() != null) player.setTackling(clampAttribute3d6((int)(player.getTackling() - declineSteps)));
        
        // Goalkeeper attributes also decline
        if (player.getShotStopping() != null) player.setShotStopping(clampAttribute3d6((int)(player.getShotStopping() - declineSteps)));
        if (player.getAreaControl() != null) player.setAreaControl(clampAttribute3d6((int)(player.getAreaControl() - declineSteps)));
        if (player.getPunching() != null) player.setPunching(clampAttribute3d6((int)(player.getPunching() - declineSteps)));
        if (player.getHandToHand() != null) player.setHandToHand(clampAttribute3d6((int)(player.getHandToHand() - declineSteps)));
        if (player.getRushingOut() != null) player.setRushingOut(clampAttribute3d6((int)(player.getRushingOut() - declineSteps)));
        if (player.getAreaPositioning() != null) player.setAreaPositioning(clampAttribute3d6((int)(player.getAreaPositioning() - declineSteps)));
    }
}

