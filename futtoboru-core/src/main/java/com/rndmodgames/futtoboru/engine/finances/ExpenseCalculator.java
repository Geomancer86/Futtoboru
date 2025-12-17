package com.rndmodgames.futtoboru.engine.finances;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.Stadium;

/**
 * Expense Calculator v1.0
 * 
 * Calculates detailed club expenses based on historical 1888-89 data.
 * Uses deterministic randomization for repeatable results with variation.
 * 
 * Based on Everton FC 1888-89 expense records.
 * 
 * @author Geomancer86
 */
public class ExpenseCalculator {
    
    // Historical expense percentages (from Everton 1888-89)
    private static final double STADIUM_MAINTENANCE_PERCENTAGE = 0.05; // 5% of stadium value annually
    private static final double PITCH_MAINTENANCE_PERCENTAGE = 0.02;   // 2% of stadium value annually
    
    // Equipment costs per player (weekly)
    private static final BigDecimal MIN_EQUIPMENT_PER_PLAYER = new BigDecimal("0.025"); // £0.025/week
    private static final BigDecimal MAX_EQUIPMENT_PER_PLAYER = new BigDecimal("0.060"); // £0.060/week
    
    // Staff wages (weekly)
    private static final BigDecimal MIN_TRAINER_WAGE = new BigDecimal("1.00");  // £1.00/week
    private static final BigDecimal MAX_TRAINER_WAGE = new BigDecimal("2.00");  // £2.00/week
    private static final BigDecimal MIN_GROUNDSMAN_WAGE = new BigDecimal("0.50"); // £0.50/week per groundsman
    private static final BigDecimal MAX_GROUNDSMAN_WAGE = new BigDecimal("1.00"); // £1.00/week per groundsman
    private static final int GROUNDSMEN_PER_CAPACITY = 5000; // 1 groundsman per 5000 capacity
    
    // Administrative expenses (weekly)
    private static final BigDecimal MIN_ADMIN_EXPENSE = new BigDecimal("0.50");  // £0.50/week
    private static final BigDecimal MAX_ADMIN_EXPENSE = new BigDecimal("2.00"); // £2.00/week
    
    // Other expenses (weekly)
    private static final BigDecimal MIN_MEDICAL = new BigDecimal("0.20");
    private static final BigDecimal MAX_MEDICAL = new BigDecimal("0.50");
    private static final BigDecimal MIN_INSURANCE = new BigDecimal("0.10");
    private static final BigDecimal MAX_INSURANCE = new BigDecimal("0.30");
    private static final BigDecimal MIN_TRAINING = new BigDecimal("0.30");
    private static final BigDecimal MAX_TRAINING = new BigDecimal("0.80");
    private static final BigDecimal MIN_ENTERTAINMENT = new BigDecimal("0.10");
    private static final BigDecimal MAX_ENTERTAINMENT = new BigDecimal("0.30");
    
    /**
     * Calculate weekly expenses for a club
     * 
     * Uses deterministic randomization based on year/month/clubId for repeatability
     * 
     * @param club The club
     * @param gameDate Current game date (for seasonal/randomization)
     * @return ClubExpenses with all categories filled
     */
    public ClubExpenses calculateWeeklyExpenses(Club club, LocalDateTime gameDate) {
        if (club == null || gameDate == null) {
            return null;
        }
        
        ClubExpenses expenses = new ClubExpenses();
        expenses.setClubId(club.getId());
        expenses.setPeriodStart(gameDate);
        expenses.setPeriodEnd(gameDate.plusWeeks(1));
        expenses.setPeriodType("WEEKLY");
        
        // Get club attributes for scaling
        int rosterSize = club.getPlayers() != null ? club.getPlayers().size() : 20;
        Stadium stadium = club.getStadium();
        BigDecimal stadiumValue = stadium != null && stadium.getValue() != null ? 
            stadium.getValue() : BigDecimal.valueOf(5000);
        int stadiumCapacity = stadium != null && stadium.getCapacity() != null ? 
            stadium.getCapacity() : 10000;
        
        expenses.setRosterSize(rosterSize);
        expenses.setStadiumValue(stadiumValue);
        expenses.setStadiumCapacity(stadiumCapacity);
        
        // Initialize monthly random seed (for repeatability)
        // Same year/month/club = same random values
        long monthSeed = (gameDate.getYear() * 12L) + gameDate.getMonthValue();
        Random monthlyRandom = new Random(monthSeed + club.getId());
        
        // Calculate each expense category
        expenses.setStadiumMaintenance(calculateStadiumMaintenance(stadiumValue, monthlyRandom));
        expenses.setPitchMaintenance(calculatePitchMaintenance(stadiumValue, gameDate, monthlyRandom));
        expenses.setMaterialsEquipment(calculateMaterialsEquipment(rosterSize, monthlyRandom));
        expenses.setStaffWages(calculateStaffWages(stadiumCapacity, monthlyRandom));
        expenses.setAdministrativeExpenses(calculateAdministrativeExpenses(rosterSize, monthlyRandom));
        expenses.setStadiumRent(calculateStadiumRent(stadium));
        expenses.setOtherExpenses(calculateOtherExpenses(monthlyRandom));
        
        // Player wages (v1.0: from PlayerContract system)
        expenses.setPlayerWages(calculatePlayerWages(club, gameDate));
        
        // Travel expenses (calculated per match, not weekly)
        expenses.setTravelExpenses(BigDecimal.ZERO); // Calculated separately per away match
        
        // Matchday expenses (calculated per match, not weekly)
        expenses.setMatchdayExpenses(BigDecimal.ZERO); // Calculated separately per match
        
        // Calculate total
        expenses.setTotalExpenses(expenses.calculateTotal());
        
        return expenses;
    }
    
    /**
     * Calculate stadium maintenance (stands, facilities)
     * Base: 5% of stadium value annually
     * Variation: ±20% monthly
     */
    private BigDecimal calculateStadiumMaintenance(BigDecimal stadiumValue, Random random) {
        // Annual: 5% of stadium value
        BigDecimal annualMaintenance = stadiumValue.multiply(BigDecimal.valueOf(STADIUM_MAINTENANCE_PERCENTAGE));
        BigDecimal weeklyBase = annualMaintenance.divide(BigDecimal.valueOf(52), 4, RoundingMode.HALF_UP);
        
        // Monthly variation: ±20% (0.8 to 1.2)
        double variation = 0.8 + (random.nextDouble() * 0.4);
        
        return weeklyBase.multiply(BigDecimal.valueOf(variation)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate pitch maintenance
     * Base: 2% of stadium value annually
     * Variation: ±30% monthly
     * Seasonal: Higher in winter (wet weather)
     */
    private BigDecimal calculatePitchMaintenance(BigDecimal stadiumValue, LocalDateTime gameDate, Random random) {
        // Annual: 2% of stadium value
        BigDecimal annualMaintenance = stadiumValue.multiply(BigDecimal.valueOf(PITCH_MAINTENANCE_PERCENTAGE));
        BigDecimal weeklyBase = annualMaintenance.divide(BigDecimal.valueOf(52), 4, RoundingMode.HALF_UP);
        
        // Monthly variation: ±30% (0.7 to 1.3)
        double variation = 0.7 + (random.nextDouble() * 0.6);
        
        // Seasonal multiplier (winter = higher maintenance)
        double seasonalMultiplier = 1.0;
        int month = gameDate.getMonthValue();
        if (month >= 11 || month <= 2) {
            // Winter months (Nov, Dec, Jan, Feb): 1.2x
            seasonalMultiplier = 1.2;
        } else if (month >= 3 && month <= 4) {
            // Spring (Mar, Apr): 1.1x
            seasonalMultiplier = 1.1;
        } else if (month >= 9 && month <= 10) {
            // Autumn (Sep, Oct): 1.1x
            seasonalMultiplier = 1.1;
        }
        // Summer (May-Aug): 1.0x (base)
        
        return weeklyBase.multiply(BigDecimal.valueOf(variation))
                         .multiply(BigDecimal.valueOf(seasonalMultiplier))
                         .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate materials and equipment costs
     * Per player: £0.025-0.060/week
     * Variation: ±25% monthly
     */
    private BigDecimal calculateMaterialsEquipment(int rosterSize, Random random) {
        // Per player weekly cost
        BigDecimal perPlayerRange = MAX_EQUIPMENT_PER_PLAYER.subtract(MIN_EQUIPMENT_PER_PLAYER);
        BigDecimal perPlayerCost = MIN_EQUIPMENT_PER_PLAYER.add(
            perPlayerRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        );
        
        // Monthly variation: ±25% (0.75 to 1.25)
        double variation = 0.75 + (random.nextDouble() * 0.5);
        
        return perPlayerCost.multiply(BigDecimal.valueOf(rosterSize))
                            .multiply(BigDecimal.valueOf(variation))
                            .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate staff wages
     * Trainer: £1.00-2.00/week
     * Groundsmen: £0.50-1.00/week per groundsman (1 per 5000 capacity)
     */
    private BigDecimal calculateStaffWages(int stadiumCapacity, Random random) {
        BigDecimal total = BigDecimal.ZERO;
        
        // Trainer wage
        BigDecimal trainerRange = MAX_TRAINER_WAGE.subtract(MIN_TRAINER_WAGE);
        BigDecimal trainerWage = MIN_TRAINER_WAGE.add(
            trainerRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        );
        total = total.add(trainerWage);
        
        // Groundsmen (1 per 5000 capacity, minimum 1)
        int numGroundsmen = Math.max(1, stadiumCapacity / GROUNDSMEN_PER_CAPACITY);
        BigDecimal groundsmanRange = MAX_GROUNDSMAN_WAGE.subtract(MIN_GROUNDSMAN_WAGE);
        BigDecimal groundsmanWage = MIN_GROUNDSMAN_WAGE.add(
            groundsmanRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        );
        total = total.add(groundsmanWage.multiply(BigDecimal.valueOf(numGroundsmen)));
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate administrative expenses
     * Base: £0.50-2.00/week
     * Scales with roster size
     * Variation: ±15% monthly
     */
    private BigDecimal calculateAdministrativeExpenses(int rosterSize, Random random) {
        // Base weekly cost
        BigDecimal adminRange = MAX_ADMIN_EXPENSE.subtract(MIN_ADMIN_EXPENSE);
        BigDecimal baseAdmin = MIN_ADMIN_EXPENSE.add(
            adminRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        );
        
        // Scale with roster size (20 players = 1.0x, 30 players = 1.2x)
        double clubSizeMultiplier = 0.8 + ((double)rosterSize / 20.0 * 0.4);
        
        // Monthly variation: ±15% (0.85 to 1.15)
        double variation = 0.85 + (random.nextDouble() * 0.3);
        
        return baseAdmin.multiply(BigDecimal.valueOf(clubSizeMultiplier))
                        .multiply(BigDecimal.valueOf(variation))
                        .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate stadium rent (if rented)
     * From Stadium data: annualRent / 52
     */
    private BigDecimal calculateStadiumRent(Stadium stadium) {
        if (stadium == null || stadium.isOwned()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal annualRent = stadium.getAnnualRent();
        if (annualRent == null || annualRent.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Weekly rent = annual rent / 52
        return annualRent.divide(BigDecimal.valueOf(52), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate other expenses
     * Medical, insurance, training, entertainment
     * Variation: ±20% monthly
     */
    private BigDecimal calculateOtherExpenses(Random random) {
        BigDecimal total = BigDecimal.ZERO;
        
        // Monthly variation: ±20% (0.8 to 1.2)
        double variation = 0.8 + (random.nextDouble() * 0.4);
        
        // Medical expenses
        BigDecimal medicalRange = MAX_MEDICAL.subtract(MIN_MEDICAL);
        BigDecimal medical = MIN_MEDICAL.add(
            medicalRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        ).multiply(BigDecimal.valueOf(variation));
        total = total.add(medical);
        
        // Insurance
        BigDecimal insuranceRange = MAX_INSURANCE.subtract(MIN_INSURANCE);
        BigDecimal insurance = MIN_INSURANCE.add(
            insuranceRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        ).multiply(BigDecimal.valueOf(variation));
        total = total.add(insurance);
        
        // Training expenses
        BigDecimal trainingRange = MAX_TRAINING.subtract(MIN_TRAINING);
        BigDecimal training = MIN_TRAINING.add(
            trainingRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        ).multiply(BigDecimal.valueOf(variation));
        total = total.add(training);
        
        // Entertainment
        BigDecimal entertainmentRange = MAX_ENTERTAINMENT.subtract(MIN_ENTERTAINMENT);
        BigDecimal entertainment = MIN_ENTERTAINMENT.add(
            entertainmentRange.multiply(BigDecimal.valueOf(random.nextDouble()))
        ).multiply(BigDecimal.valueOf(variation));
        total = total.add(entertainment);
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total player wages for the week (v1.0)
     * 
     * Sums weekly wages from all active, non-expired contracts.
     * 
     * @param club The club
     * @param gameDate Current game date (to check contract expiry)
     * @return Total weekly player wages
     */
    private BigDecimal calculatePlayerWages(Club club, LocalDateTime gameDate) {
        if (club == null || club.getPlayerContracts() == null || club.getPlayerContracts().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalWages = BigDecimal.ZERO;
        
        for (PlayerContract contract : club.getPlayerContracts()) {
            if (contract == null) {
                continue;
            }
            
            // Check if contract is active
            if (contract.getIsActive() == null || !contract.getIsActive()) {
                continue; // Skip inactive contracts
            }
            
            // Check if contract has expired
            if (contract.getEndDate() != null) {
                if (gameDate.isAfter(contract.getEndDate())) {
                    continue; // Contract expired, skip
                }
            }
            
            // Check if contract has started
            if (contract.getStartDate() != null) {
                if (gameDate.isBefore(contract.getStartDate())) {
                    continue; // Contract hasn't started yet, skip
                }
            }
            
            // Add weekly wage to total
            BigDecimal weeklyWage = contract.getWeeklyWage();
            if (weeklyWage != null && weeklyWage.compareTo(BigDecimal.ZERO) > 0) {
                totalWages = totalWages.add(weeklyWage);
            }
        }
        
        return totalWages.setScale(2, RoundingMode.HALF_UP);
    }
}
