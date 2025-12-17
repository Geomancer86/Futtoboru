package com.rndmodgames.futtoboru.system.generators;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Player Attribute Generator v1
 * 
 * Generates and manages player attributes based on age, position, and reputation.
 * 
 * @author Geomancer86
 */
public class PlayerAttributeGenerator {
    
    /**
     * Generate all attributes for a player
     */
    public void generatePlayerAttributes(Player player, Person person, LocalDateTime currentDate) {
        
        if (player == null || person == null) {
            Gdx.app.error("PlayerAttributeGenerator", "Cannot generate attributes: player or person is null");
            return;
        }
        
        // Calculate age
        int age = (int) ChronoUnit.YEARS.between(person.getBirthDate(), currentDate);
        
        // Calculate base level (age-dependent)
        float baseLevel = calculateBaseLevel(age);
        
        // Reputation influence (if available)
        float reputationBonus = 0.0f;
        if (person.getReputation() != null && person.getReputation() > 0) {
            // Reputation 0-100 affects attributes by -10 to +10
            reputationBonus = (person.getReputation() - 50.0f) * 0.2f;
        }
        
        // Generate Physical Attributes
        generatePhysicalAttributes(player, baseLevel, reputationBonus);
        
        // Generate Mental Attributes
        generateMentalAttributes(player, baseLevel, reputationBonus);
        
        // Generate Technical Attributes
        generateTechnicalAttributes(player, baseLevel, reputationBonus);
        
        // Generate Goalkeeper Attributes (for all players, but only relevant for GKs)
        generateGoalkeeperAttributes(player, baseLevel, reputationBonus);
        
        // Apply profession bonuses (for amateur/semi-pro players)
        applyProfessionBonuses(player);
        
        Gdx.app.debug("PlayerAttributeGenerator", "Generated attributes for: " + person.getName() + " (Age: " + age + ", Base: " + baseLevel + ")");
    }
    
    /**
     * Apply profession bonuses to player attributes (v1.0)
     * 
     * Profession bonuses allow attributes to exceed normal maximum (20) up to 22.
     * Only applies to amateur/semi-professional players (professional players don't have day jobs).
     */
    private void applyProfessionBonuses(Player player) {
        if (player == null || player.getPlayerProfession() == null) {
            return; // No profession, no bonuses
        }
        
        PlayerProfession profession = player.getPlayerProfession();
        
        // Apply physical attribute bonuses
        if (profession.getStrengthBonus() != null && profession.getStrengthBonus() != 0) {
            applyAttributeBonus(player, "strength", profession.getStrengthBonus());
        }
        if (profession.getEnduranceBonus() != null && profession.getEnduranceBonus() != 0) {
            applyAttributeBonus(player, "endurance", profession.getEnduranceBonus());
        }
        if (profession.getStaminaBonus() != null && profession.getStaminaBonus() != 0) {
            applyAttributeBonus(player, "stamina", profession.getStaminaBonus());
        }
        if (profession.getSpeedBonus() != null && profession.getSpeedBonus() != 0) {
            applyAttributeBonus(player, "speed", profession.getSpeedBonus());
        }
        if (profession.getAccelerationBonus() != null && profession.getAccelerationBonus() != 0) {
            applyAttributeBonus(player, "acceleration", profession.getAccelerationBonus());
        }
        if (profession.getJumpingBonus() != null && profession.getJumpingBonus() != 0) {
            applyAttributeBonus(player, "jumping", profession.getJumpingBonus());
        }
        if (profession.getDexterityBonus() != null && profession.getDexterityBonus() != 0) {
            applyAttributeBonus(player, "dexterity", profession.getDexterityBonus());
        }
        
        // Apply mental attribute bonuses
        if (profession.getConcentrationBonus() != null && profession.getConcentrationBonus() != 0) {
            applyAttributeBonus(player, "concentration", profession.getConcentrationBonus());
        }
        if (profession.getCourageBonus() != null && profession.getCourageBonus() != 0) {
            applyAttributeBonus(player, "courage", profession.getCourageBonus());
        }
        if (profession.getDeterminationBonus() != null && profession.getDeterminationBonus() != 0) {
            applyAttributeBonus(player, "determination", profession.getDeterminationBonus());
        }
        if (profession.getLeadershipBonus() != null && profession.getLeadershipBonus() != 0) {
            applyAttributeBonus(player, "leadership", profession.getLeadershipBonus());
        }
        if (profession.getPerceptionBonus() != null && profession.getPerceptionBonus() != 0) {
            applyAttributeBonus(player, "perception", profession.getPerceptionBonus());
        }
        if (profession.getPositioningBonus() != null && profession.getPositioningBonus() != 0) {
            applyAttributeBonus(player, "positioning", profession.getPositioningBonus());
        }
        if (profession.getTeamworkBonus() != null && profession.getTeamworkBonus() != 0) {
            applyAttributeBonus(player, "teamwork", profession.getTeamworkBonus());
        }
    }
    
    /**
     * Apply a profession bonus to a specific attribute
     * 
     * Converts from 0-100 scale to 1-22 scale, applies bonus, then converts back.
     * Maximum is 22 (exceptional player level).
     */
    private void applyAttributeBonus(Player player, String attributeName, Integer bonus) {
        if (bonus == null || bonus == 0) {
            return;
        }
        
        // Get current attribute value (0-100 scale)
        Float currentValue = getAttributeValue(player, attributeName);
        if (currentValue == null) {
            return;
        }
        
        // Convert to 1-22 scale (0-100 -> 1-22)
        // Formula: (value / 100) * 21 + 1 = 1-22 range
        float normalizedValue = (currentValue / 100.0f) * 21.0f + 1.0f;
        
        // Apply bonus
        float newValue = normalizedValue + bonus;
        
        // Cap at 22 (exceptional maximum)
        newValue = Math.min(22.0f, Math.max(1.0f, newValue));
        
        // Convert back to 0-100 scale (1-22 -> 0-100)
        // Formula: ((value - 1) / 21) * 100 = 0-100 range
        float scaledValue = ((newValue - 1.0f) / 21.0f) * 100.0f;
        
        // Set the attribute
        setAttributeValue(player, attributeName, scaledValue);
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
        value = clampAttribute(value); // Ensure 0-100 range
        
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
     * Calculate base attribute level based on age
     * Peak performance around age 25-28
     */
    private float calculateBaseLevel(int age) {
        if (age < 18) {
            return 30.0f + (age * 2.0f);        // Young players: 30-66
        } else if (age <= 25) {
            return 50.0f + ((age - 18) * 3.0f); // 18-25: 50-71
        } else if (age <= 28) {
            return 75.0f + ((age - 25) * 2.0f); // 25-28: 75-81 (peak)
        } else if (age <= 35) {
            return 81.0f - ((age - 28) * 2.0f); // 28-35: 81-67 (decline)
        } else {
            return Math.max(30.0f, 67.0f - ((age - 35) * 2.0f)); // 35+: 67-30 (rapid decline)
        }
    }
    
    /**
     * Generate physical attributes
     */
    private void generatePhysicalAttributes(Player player, float baseLevel, float reputationBonus) {
        float randomVariation = (DatabaseLoader.RNG.nextFloat() * 20.0f) - 10.0f; // -10 to +10
        
        player.setAcceleration(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setSpeed(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setStamina(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setStrength(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setEndurance(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setJumping(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setDexterity(clampAttribute(baseLevel + randomVariation + reputationBonus));
    }
    
    /**
     * Generate mental attributes
     */
    private void generateMentalAttributes(Player player, float baseLevel, float reputationBonus) {
        float randomVariation = (DatabaseLoader.RNG.nextFloat() * 20.0f) - 10.0f; // -10 to +10
        
        player.setConcentration(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setCourage(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setDetermination(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setLeadership(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setPerception(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setPositioning(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setTeamwork(clampAttribute(baseLevel + randomVariation + reputationBonus));
    }
    
    /**
     * Generate technical attributes
     */
    private void generateTechnicalAttributes(Player player, float baseLevel, float reputationBonus) {
        float randomVariation = (DatabaseLoader.RNG.nextFloat() * 20.0f) - 10.0f; // -10 to +10
        
        player.setPassing(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setKicking(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setLongShots(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setTrickShots(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setHeading(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setOneTwos(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setFreeKicks(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setCornerKicks(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setPenaltyKicks(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setThrowIns(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setMarking(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setTackling(clampAttribute(baseLevel + randomVariation + reputationBonus));
    }
    
    /**
     * Generate goalkeeper attributes
     */
    private void generateGoalkeeperAttributes(Player player, float baseLevel, float reputationBonus) {
        float randomVariation = (DatabaseLoader.RNG.nextFloat() * 20.0f) - 10.0f; // -10 to +10
        
        player.setShotStopping(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setAreaControl(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setPunching(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setHandToHand(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setRushingOut(clampAttribute(baseLevel + randomVariation + reputationBonus));
        player.setAreaPositioning(clampAttribute(baseLevel + randomVariation + reputationBonus));
    }
    
    /**
     * Clamp attribute value to 0-100 range
     */
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
        
        // Randomly change one physical attribute
        int physicalAttr = DatabaseLoader.RNG.nextInt(7);
        float change = (DatabaseLoader.RNG.nextFloat() * 3.0f) - 1.5f; // -1.5 to +1.5
        
        switch (physicalAttr) {
            case 0: player.setAcceleration(clampAttribute(player.getAcceleration() + change)); break;
            case 1: player.setSpeed(clampAttribute(player.getSpeed() + change)); break;
            case 2: player.setStamina(clampAttribute(player.getStamina() + change)); break;
            case 3: player.setStrength(clampAttribute(player.getStrength() + change)); break;
            case 4: player.setEndurance(clampAttribute(player.getEndurance() + change)); break;
            case 5: player.setJumping(clampAttribute(player.getJumping() + change)); break;
            case 6: player.setDexterity(clampAttribute(player.getDexterity() + change)); break;
        }
        
        // Randomly change one technical attribute
        int technicalAttr = DatabaseLoader.RNG.nextInt(12);
        change = (DatabaseLoader.RNG.nextFloat() * 3.0f) - 1.5f; // -1.5 to +1.5
        
        switch (technicalAttr) {
            case 0: player.setPassing(clampAttribute(player.getPassing() + change)); break;
            case 1: player.setKicking(clampAttribute(player.getKicking() + change)); break;
            case 2: player.setLongShots(clampAttribute(player.getLongShots() + change)); break;
            case 3: player.setTrickShots(clampAttribute(player.getTrickShots() + change)); break;
            case 4: player.setHeading(clampAttribute(player.getHeading() + change)); break;
            case 5: player.setOneTwos(clampAttribute(player.getOneTwos() + change)); break;
            case 6: player.setFreeKicks(clampAttribute(player.getFreeKicks() + change)); break;
            case 7: player.setCornerKicks(clampAttribute(player.getCornerKicks() + change)); break;
            case 8: player.setPenaltyKicks(clampAttribute(player.getPenaltyKicks() + change)); break;
            case 9: player.setThrowIns(clampAttribute(player.getThrowIns() + change)); break;
            case 10: player.setMarking(clampAttribute(player.getMarking() + change)); break;
            case 11: player.setTackling(clampAttribute(player.getTackling() + change)); break;
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
     * Apply age-based decline to all attributes
     */
    private void applyAgeDecline(Player player, float declineFactor) {
        // Decline all attributes slightly
        if (player.getAcceleration() != null) player.setAcceleration(clampAttribute(player.getAcceleration() - declineFactor));
        if (player.getSpeed() != null) player.setSpeed(clampAttribute(player.getSpeed() - declineFactor));
        if (player.getStamina() != null) player.setStamina(clampAttribute(player.getStamina() - declineFactor));
        if (player.getStrength() != null) player.setStrength(clampAttribute(player.getStrength() - declineFactor));
        if (player.getEndurance() != null) player.setEndurance(clampAttribute(player.getEndurance() - declineFactor));
        if (player.getJumping() != null) player.setJumping(clampAttribute(player.getJumping() - declineFactor));
        if (player.getDexterity() != null) player.setDexterity(clampAttribute(player.getDexterity() - declineFactor));
        
        // Mental attributes also decline with age
        if (player.getConcentration() != null) player.setConcentration(clampAttribute(player.getConcentration() - declineFactor * 0.5f));
        if (player.getCourage() != null) player.setCourage(clampAttribute(player.getCourage() - declineFactor * 0.5f));
        if (player.getDetermination() != null) player.setDetermination(clampAttribute(player.getDetermination() - declineFactor * 0.5f));
        if (player.getLeadership() != null) player.setLeadership(clampAttribute(player.getLeadership() - declineFactor * 0.3f)); // Leadership declines slower
        if (player.getPerception() != null) player.setPerception(clampAttribute(player.getPerception() - declineFactor * 0.5f));
        if (player.getPositioning() != null) player.setPositioning(clampAttribute(player.getPositioning() - declineFactor * 0.3f)); // Experience helps
        if (player.getTeamwork() != null) player.setTeamwork(clampAttribute(player.getTeamwork() - declineFactor * 0.3f));
        
        // Technical attributes decline faster
        if (player.getPassing() != null) player.setPassing(clampAttribute(player.getPassing() - declineFactor));
        if (player.getKicking() != null) player.setKicking(clampAttribute(player.getKicking() - declineFactor));
        if (player.getLongShots() != null) player.setLongShots(clampAttribute(player.getLongShots() - declineFactor));
        if (player.getTrickShots() != null) player.setTrickShots(clampAttribute(player.getTrickShots() - declineFactor));
        if (player.getHeading() != null) player.setHeading(clampAttribute(player.getHeading() - declineFactor));
        if (player.getOneTwos() != null) player.setOneTwos(clampAttribute(player.getOneTwos() - declineFactor));
        if (player.getFreeKicks() != null) player.setFreeKicks(clampAttribute(player.getFreeKicks() - declineFactor));
        if (player.getCornerKicks() != null) player.setCornerKicks(clampAttribute(player.getCornerKicks() - declineFactor));
        if (player.getPenaltyKicks() != null) player.setPenaltyKicks(clampAttribute(player.getPenaltyKicks() - declineFactor));
        if (player.getThrowIns() != null) player.setThrowIns(clampAttribute(player.getThrowIns() - declineFactor));
        if (player.getMarking() != null) player.setMarking(clampAttribute(player.getMarking() - declineFactor));
        if (player.getTackling() != null) player.setTackling(clampAttribute(player.getTackling() - declineFactor));
        
        // Goalkeeper attributes also decline
        if (player.getShotStopping() != null) player.setShotStopping(clampAttribute(player.getShotStopping() - declineFactor));
        if (player.getAreaControl() != null) player.setAreaControl(clampAttribute(player.getAreaControl() - declineFactor));
        if (player.getPunching() != null) player.setPunching(clampAttribute(player.getPunching() - declineFactor));
        if (player.getHandToHand() != null) player.setHandToHand(clampAttribute(player.getHandToHand() - declineFactor));
        if (player.getRushingOut() != null) player.setRushingOut(clampAttribute(player.getRushingOut() - declineFactor));
        if (player.getAreaPositioning() != null) player.setAreaPositioning(clampAttribute(player.getAreaPositioning() - declineFactor));
    }
}

