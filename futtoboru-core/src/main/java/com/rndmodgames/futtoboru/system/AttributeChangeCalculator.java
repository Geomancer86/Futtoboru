package com.rndmodgames.futtoboru.system;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.AttributeTrackingConstants;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerAttributeSnapshot;

/**
 * Attribute Change Calculator (v1.0)
 * 
 * Calculates attribute changes over specified time periods using stored snapshots.
 * 
 * @author Geomancer86
 */
public class AttributeChangeCalculator {
    
    private SaveGame currentGame;
    
    public AttributeChangeCalculator(SaveGame currentGame) {
        this.currentGame = currentGame;
    }
    
    /**
     * Calculate attribute change over a specified period
     * Uses the oldest available snapshot (could be 1 day, 7 days, 30 days, etc.)
     * Shows changes immediately, becoming more accurate as more time passes
     * 
     * @param player Player to calculate change for
     * @param attributeName Attribute name (e.g., "Acceleration", "Speed")
     * @param periodDays Preferred number of days to look back (default: 30)
     * @return AttributeChangeResult with change amount, trend, and formatted display
     */
    public AttributeChangeResult calculateAttributeChange(Player player, String attributeName, int periodDays) {
        if (player == null || player.getPerson() == null || attributeName == null) {
            return new AttributeChangeResult(0f, "N/A", "Invalid parameters");
        }
        
        // Get current attribute value
        Float currentValue = getCurrentAttributeValue(player, attributeName);
        if (currentValue == null) {
            return new AttributeChangeResult(0f, "N/A", "No current value");
        }
        
        // Find the oldest available snapshot (not necessarily periodDays ago)
        // This allows showing changes immediately, even if only 1 day has passed
        PlayerAttributeSnapshot oldSnapshot = findOldestSnapshot(player.getPerson().getId());
        
        if (oldSnapshot == null) {
            return new AttributeChangeResult(0f, "N/A", "No historical data");
        }
        
        // Get old attribute value - convert display name to snapshot lookup format
        String snapshotLookupName = convertDisplayNameToSnapshotName(attributeName);
        Float oldValue = oldSnapshot.getAttributeValue(snapshotLookupName);
        if (oldValue == null) {
            return new AttributeChangeResult(0f, "N/A", "No historical value");
        }
        
        // Calculate actual days elapsed
        long actualDaysElapsed = ChronoUnit.DAYS.between(oldSnapshot.getSnapshotDate(), currentGame.getGameDate());
        
        // Calculate change
        float change = currentValue - oldValue;
        
        // If we have less data than the preferred period, we could scale the change
        // For now, we'll just show the raw change and indicate the actual period
        // The change will become more accurate as more time passes
        
        // Determine trend and format
        String trend;
        String displayText;
        
        float absChange = Math.abs(change);
        
        if (absChange < AttributeTrackingConstants.MIN_CHANGE_THRESHOLD) {
            trend = "STABLE";
            displayText = "[=]";
        } else if (change > 0) {
            if (absChange >= AttributeTrackingConstants.SIGNIFICANT_CHANGE_THRESHOLD) {
                trend = "IMPROVING_SIGNIFICANT";
                displayText = String.format("[+%.1f ↑↑]", change);
            } else {
                trend = "IMPROVING";
                displayText = String.format("[+%.1f ↑]", change);
            }
        } else {
            if (absChange >= AttributeTrackingConstants.SIGNIFICANT_CHANGE_THRESHOLD) {
                trend = "DECLINING_SIGNIFICANT";
                displayText = String.format("[%.1f ↓↓]", change);
            } else {
                trend = "DECLINING";
                displayText = String.format("[%.1f ↓]", change);
            }
        }
        
        return new AttributeChangeResult(change, trend, displayText, actualDaysElapsed);
    }
    
    /**
     * Calculate attribute change using default period (30 days)
     */
    public AttributeChangeResult calculateAttributeChange(Player player, String attributeName) {
        return calculateAttributeChange(player, attributeName, AttributeTrackingConstants.DEFAULT_CHANGE_PERIOD_DAYS);
    }
    
    /**
     * Find the oldest available snapshot for a player
     * This allows showing changes immediately, even if only 1 day has passed
     */
    private PlayerAttributeSnapshot findOldestSnapshot(Long playerId) {
        List<PlayerAttributeSnapshot> playerSnapshots = currentGame.getPlayerSnapshots(playerId);
        
        if (playerSnapshots == null || playerSnapshots.isEmpty()) {
            return null;
        }
        
        PlayerAttributeSnapshot oldest = null;
        LocalDateTime oldestDate = null;
        
        for (PlayerAttributeSnapshot snapshot : playerSnapshots) {
            if (snapshot == null || snapshot.getSnapshotDate() == null) {
                continue;
            }
            
            // Find the oldest snapshot (earliest date)
            if (oldest == null || snapshot.getSnapshotDate().isBefore(oldestDate)) {
                oldest = snapshot;
                oldestDate = snapshot.getSnapshotDate();
            }
        }
        
        return oldest;
    }
    
    /**
     * Convert display name (e.g., "Long Shots") to snapshot lookup name (e.g., "longshots")
     */
    private String convertDisplayNameToSnapshotName(String displayName) {
        // Convert to lowercase and remove spaces/hyphens for snapshot lookup
        return displayName.toLowerCase().replace(" ", "").replace("-", "").replace("_", "");
    }
    
    /**
     * Get current attribute value from player object
     */
    private Float getCurrentAttributeValue(Player player, String attributeName) {
        // Map display names to getter methods
        switch (attributeName) {
            // Physical
            case "Acceleration": return player.getAcceleration();
            case "Dexterity": return player.getDexterity();
            case "Endurance": return player.getEndurance();
            case "Jumping": return player.getJumping();
            case "Stamina": return player.getStamina();
            case "Strength": return player.getStrength();
            case "Speed": return player.getSpeed();
            
            // Mental
            case "Concentration": return player.getConcentration();
            case "Courage": return player.getCourage();
            case "Determination": return player.getDetermination();
            case "Leadership": return player.getLeadership();
            case "Perception": return player.getPerception();
            case "Positioning": return player.getPositioning();
            case "Teamwork": return player.getTeamwork();
            
            // Technical
            case "Passing": return player.getPassing();
            case "Kicking": return player.getKicking();
            case "Long Shots": return player.getLongShots();
            case "Trick Shots": return player.getTrickShots();
            case "Heading": return player.getHeading();
            case "One-Twos": return player.getOneTwos();
            case "Free Kicks": return player.getFreeKicks();
            case "Corner Kicks": return player.getCornerKicks();
            case "Penalty Kicks": return player.getPenaltyKicks();
            case "Throw-Ins": return player.getThrowIns();
            case "Marking": return player.getMarking();
            case "Tackling": return player.getTackling();
            
            // Goalkeeper
            case "Shot Stopping": return player.getShotStopping();
            case "Area Control": return player.getAreaControl();
            case "Punching": return player.getPunching();
            case "Hand to Hand": return player.getHandToHand();
            case "Rushing Out": return player.getRushingOut();
            case "Area Positioning": return player.getAreaPositioning();
            
            default:
                Gdx.app.error("AttributeChangeCalculator", "Unknown attribute name: " + attributeName);
                return null;
        }
    }
    
    /**
     * Result class for attribute change calculations
     */
    public static class AttributeChangeResult {
        private final float change;
        private final String trend;
        private final String displayText;
        private final long actualDaysElapsed;
        
        public AttributeChangeResult(float change, String trend, String displayText) {
            this(change, trend, displayText, 0);
        }
        
        public AttributeChangeResult(float change, String trend, String displayText, long actualDaysElapsed) {
            this.change = change;
            this.trend = trend;
            this.displayText = displayText;
            this.actualDaysElapsed = actualDaysElapsed;
        }
        
        public float getChange() {
            return change;
        }
        
        public String getTrend() {
            return trend;
        }
        
        public String getDisplayText() {
            return displayText;
        }
        
        public long getActualDaysElapsed() {
            return actualDaysElapsed;
        }
        
        public boolean isImproving() {
            return trend.contains("IMPROVING");
        }
        
        public boolean isDeclining() {
            return trend.contains("DECLINING");
        }
        
        public boolean isStable() {
            return "STABLE".equals(trend) || "N/A".equals(trend);
        }
    }
}

