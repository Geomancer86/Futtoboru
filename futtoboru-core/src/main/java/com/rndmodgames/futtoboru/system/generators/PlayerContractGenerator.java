package com.rndmodgames.futtoboru.system.generators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractType;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Player Contract Generator v1.0
 * 
 * Generates random contracts for players based on:
 * - Player skill level (attributes)
 * - Club financial status
 * - Historical 1888-89 wage ranges
 * - Contract type distribution
 * 
 * For Season 1, contracts are randomly generated.
 * Future seasons will load from scripts with researched historical data.
 * 
 * @author Geomancer86
 */
public class PlayerContractGenerator {
    
    /**
     * Generate a random contract for a player
     * 
     * @param player The player to generate a contract for
     * @param club The club the player belongs to
     * @param contractStartDate When the contract starts (typically season start)
     * @return A new PlayerContract with random terms
     */
    public static PlayerContract generateRandomContract(Player player, Club club, LocalDateTime contractStartDate) {
        if (player == null || club == null || contractStartDate == null) {
            return null;
        }
        
        PlayerContract contract = new PlayerContract();
        
        // Generate unique contract ID (negative for generated contracts)
        contract.setId(-(System.currentTimeMillis() % 1000000) - DatabaseLoader.RNG.nextInt(10000));
        
        // Set player and club IDs
        if (player.getId() != null) {
            contract.setPlayerId(player.getId());
        } else if (player.getPerson() != null && player.getPerson().getId() != null) {
            // Use person ID if player ID not set
            contract.setPlayerId(player.getPerson().getId());
        }
        
        if (club.getId() != null) {
            contract.setClubId(club.getId());
        }
        
        // Determine contract type based on player skill and club size
        Integer contractType = determineContractType(player, club);
        contract.setContractType(contractType);
        
        // Set contract dates
        contract.setStartDate(contractStartDate);
        
        // Contract length: 1-3 years typically (12-36 months)
        int contractLengthMonths = 12 + DatabaseLoader.RNG.nextInt(25); // 12-36 months
        contract.setContractLengthMonths(contractLengthMonths);
        contract.setEndDate(contractStartDate.plusMonths(contractLengthMonths));
        
        // Generate financial terms based on contract type
        generateFinancialTerms(contract, player, club, contractType);
        
        // Generate bonuses (only for professional contracts typically)
        if (contractType == ContractType.PROFESSIONAL || contractType == ContractType.SEMI_PROFESSIONAL) {
            generateBonuses(contract, player, club, contractType);
        }
        
        // Set contract status
        contract.setIsActive(true);
        
        return contract;
    }
    
    /**
     * Determine contract type based on player skill and club size
     * 
     * Distribution (1888-89):
     * - 30% Amateur (no pay)
     * - 40% Semi-Professional (low pay)
     * - 30% Professional (regular pay)
     */
    private static Integer determineContractType(Player player, Club club) {
        // Calculate player skill level (average of key attributes)
        float avgSkill = calculatePlayerSkill(player);
        
        // Random factor (70% skill-based, 30% random)
        float randomFactor = DatabaseLoader.RNG.nextFloat();
        float combinedFactor = (avgSkill * 0.7f) + (randomFactor * 0.3f);
        
        // Determine contract type
        if (combinedFactor < 0.30f) {
            return ContractType.AMATEUR; // 30% amateur
        } else if (combinedFactor < 0.70f) {
            return ContractType.SEMI_PROFESSIONAL; // 40% semi-pro
        } else {
            return ContractType.PROFESSIONAL; // 30% professional
        }
    }
    
    /**
     * Calculate player skill level (0.0-1.0)
     * Based on average of key attributes (normalized to 0-1)
     */
    private static float calculatePlayerSkill(Player player) {
        if (player == null) {
            return 0.5f; // Average
        }
        
        // Average of physical, mental, and technical attributes
        // Attributes are in 3-23 range, normalize to 0-1
        float strength = (player.getStrength() != null ? player.getStrength() : 10.5f - 3.0f) / 20.0f;
        float speed = (player.getSpeed() != null ? player.getSpeed() : 10.5f - 3.0f) / 20.0f;
        float stamina = (player.getStamina() != null ? player.getStamina() : 10.5f - 3.0f) / 20.0f;
        float passing = (player.getPassing() != null ? player.getPassing() : 10.5f - 3.0f) / 20.0f;
        float perception = (player.getPerception() != null ? player.getPerception() : 10.5f - 3.0f) / 20.0f;
        float determination = (player.getDetermination() != null ? player.getDetermination() : 10.5f - 3.0f) / 20.0f;
        
        return (strength + speed + stamina + passing + perception + determination) / 6.0f;
    }
    
    /**
     * Generate financial terms (wages, bonuses) based on contract type
     * 
     * Historical 1888-89 ranges:
     * - Amateur: £0/week
     * - Semi-Pro: £0.25-0.50/week (2s 6d - 5s)
     * - Professional: £0.50-5.00/week (10s - £1+)
     */
    private static void generateFinancialTerms(PlayerContract contract, Player player, Club club, Integer contractType) {
        if (contractType == null) {
            return;
        }
        
        float skillLevel = calculatePlayerSkill(player);
        
        switch (contractType) {
            case ContractType.AMATEUR:
                contract.setWeeklyWage(BigDecimal.ZERO);
                break;
                
            case ContractType.SEMI_PROFESSIONAL:
                // £0.25-0.50/week, scaled by skill
                BigDecimal semiProBase = new BigDecimal("0.25");
                BigDecimal semiProMax = new BigDecimal("0.50");
                BigDecimal semiProRange = semiProMax.subtract(semiProBase);
                BigDecimal semiProWage = semiProBase.add(semiProRange.multiply(BigDecimal.valueOf(skillLevel)));
                contract.setWeeklyWage(semiProWage.setScale(2, RoundingMode.HALF_UP));
                break;
                
            case ContractType.PROFESSIONAL:
                // £0.50-5.00/week, scaled by skill and club size
                BigDecimal proBase = new BigDecimal("0.50");
                BigDecimal proMax = new BigDecimal("5.00");
                BigDecimal proRange = proMax.subtract(proBase);
                
                // Club size factor (bigger clubs pay more)
                float clubSizeFactor = 1.0f; // TODO: Calculate based on club balance/stadium capacity
                float combinedFactor = skillLevel * clubSizeFactor;
                combinedFactor = Math.min(1.0f, combinedFactor); // Cap at 1.0
                
                BigDecimal proWage = proBase.add(proRange.multiply(BigDecimal.valueOf(combinedFactor)));
                contract.setWeeklyWage(proWage.setScale(2, RoundingMode.HALF_UP));
                
                // Signing bonus: 1-4 weeks of wages
                int signingBonusWeeks = 1 + DatabaseLoader.RNG.nextInt(4);
                contract.setSigningBonus(proWage.multiply(BigDecimal.valueOf(signingBonusWeeks)).setScale(2, RoundingMode.HALF_UP));
                break;
                
            default:
                contract.setWeeklyWage(BigDecimal.ZERO);
                break;
        }
    }
    
    /**
     * Generate performance and achievement bonuses
     * Only for professional and semi-professional contracts
     */
    private static void generateBonuses(PlayerContract contract, Player player, Club club, Integer contractType) {
        if (contractType == null || contractType == ContractType.AMATEUR) {
            return;
        }
        
        float skillLevel = calculatePlayerSkill(player);
        boolean isProfessional = (contractType == ContractType.PROFESSIONAL);
        
        // Performance bonuses (per occurrence)
        if (isProfessional) {
            // Appearance fee: £0.05-0.25 per match
            BigDecimal appearanceBase = new BigDecimal("0.05");
            BigDecimal appearanceMax = new BigDecimal("0.25");
            contract.setAppearanceFee(appearanceBase.add(
                appearanceMax.subtract(appearanceBase).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
            
            // Goal bonus: £0.10-1.00 per goal
            BigDecimal goalBase = new BigDecimal("0.10");
            BigDecimal goalMax = new BigDecimal("1.00");
            contract.setGoalBonus(goalBase.add(
                goalMax.subtract(goalBase).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
            
            // Assist bonus: £0.05-0.50 per assist
            BigDecimal assistBase = new BigDecimal("0.05");
            BigDecimal assistMax = new BigDecimal("0.50");
            contract.setAssistBonus(assistBase.add(
                assistMax.subtract(assistBase).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
            
            // Clean sheet bonus: £0.10-0.75 (for goalkeepers/defenders)
            BigDecimal cleanSheetBase = new BigDecimal("0.10");
            BigDecimal cleanSheetMax = new BigDecimal("0.75");
            contract.setCleanSheetBonus(cleanSheetBase.add(
                cleanSheetMax.subtract(cleanSheetBase).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
        }
        
        // Achievement bonuses (one-time, only for professionals)
        if (isProfessional) {
            // League win bonus: £10-200
            BigDecimal leagueWinBase = new BigDecimal("10.00");
            BigDecimal leagueWinMax = new BigDecimal("200.00");
            contract.setLeagueWinBonus(leagueWinBase.add(
                leagueWinMax.subtract(leagueWinBase).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
            
            // Cup win bonus: £5-100
            BigDecimal cupWinBase = new BigDecimal("5.00");
            BigDecimal cupWinMax = new BigDecimal("100.00");
            contract.setCupWinBonus(cupWinBase.add(
                cupWinMax.subtract(cupWinBase).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
            
            // Top 3 finish bonus: £3-50
            BigDecimal top3Base = new BigDecimal("3.00");
            BigDecimal top3Max = new BigDecimal("50.00");
            contract.setTop3FinishBonus(top3Base.add(
                top3Max.subtract(top3Base).multiply(BigDecimal.valueOf(skillLevel)))
                .setScale(2, RoundingMode.HALF_UP));
        }
    }
}
