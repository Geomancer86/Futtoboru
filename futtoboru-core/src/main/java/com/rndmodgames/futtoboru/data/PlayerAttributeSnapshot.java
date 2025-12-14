package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Player Attribute Snapshot (v1.0)
 * 
 * Stores a snapshot of all player attributes at a specific date.
 * Used for tracking attribute changes over time periods (e.g., 30 days).
 * 
 * Snapshots are created weekly to balance storage efficiency with change tracking accuracy.
 * 
 * @author Geomancer86
 */
public class PlayerAttributeSnapshot implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long playerId;
    private LocalDateTime snapshotDate;
    private String snapshotType; // "WEEKLY", "DAILY", "MANUAL"
    
    /**
     * Physical Attributes
     */
    private Float acceleration;
    private Float dexterity;
    private Float endurance;
    private Float jumping;
    private Float stamina;
    private Float strength;
    private Float speed;
    
    /**
     * Mental Attributes
     */
    private Float concentration;
    private Float courage;
    private Float determination;
    private Float leadership;
    private Float perception;
    private Float positioning;
    private Float teamwork;
    
    /**
     * Technical Attributes
     */
    private Float passing;
    private Float kicking;
    private Float longShots;
    private Float trickShots;
    private Float heading;
    private Float oneTwos;
    private Float freeKicks;
    private Float cornerKicks;
    private Float penaltyKicks;
    private Float throwIns;
    private Float marking;
    private Float tackling;
    
    /**
     * Goalkeeper Attributes
     */
    private Float shotStopping;
    private Float areaControl;
    private Float punching;
    private Float handToHand;
    private Float rushingOut;
    private Float areaPositioning;
    
    /**
     * Constructor
     */
    public PlayerAttributeSnapshot() {
        // Default constructor
    }
    
    /**
     * Create a snapshot from a Player object
     */
    public static PlayerAttributeSnapshot fromPlayer(Player player, LocalDateTime snapshotDate, String snapshotType) {
        if (player == null || player.getPerson() == null) {
            return null;
        }
        
        PlayerAttributeSnapshot snapshot = new PlayerAttributeSnapshot();
        snapshot.setPlayerId(player.getPerson().getId());
        snapshot.setSnapshotDate(snapshotDate);
        snapshot.setSnapshotType(snapshotType);
        
        // Copy all attributes
        snapshot.setAcceleration(player.getAcceleration());
        snapshot.setDexterity(player.getDexterity());
        snapshot.setEndurance(player.getEndurance());
        snapshot.setJumping(player.getJumping());
        snapshot.setStamina(player.getStamina());
        snapshot.setStrength(player.getStrength());
        snapshot.setSpeed(player.getSpeed());
        
        snapshot.setConcentration(player.getConcentration());
        snapshot.setCourage(player.getCourage());
        snapshot.setDetermination(player.getDetermination());
        snapshot.setLeadership(player.getLeadership());
        snapshot.setPerception(player.getPerception());
        snapshot.setPositioning(player.getPositioning());
        snapshot.setTeamwork(player.getTeamwork());
        
        snapshot.setPassing(player.getPassing());
        snapshot.setKicking(player.getKicking());
        snapshot.setLongShots(player.getLongShots());
        snapshot.setTrickShots(player.getTrickShots());
        snapshot.setHeading(player.getHeading());
        snapshot.setOneTwos(player.getOneTwos());
        snapshot.setFreeKicks(player.getFreeKicks());
        snapshot.setCornerKicks(player.getCornerKicks());
        snapshot.setPenaltyKicks(player.getPenaltyKicks());
        snapshot.setThrowIns(player.getThrowIns());
        snapshot.setMarking(player.getMarking());
        snapshot.setTackling(player.getTackling());
        
        snapshot.setShotStopping(player.getShotStopping());
        snapshot.setAreaControl(player.getAreaControl());
        snapshot.setPunching(player.getPunching());
        snapshot.setHandToHand(player.getHandToHand());
        snapshot.setRushingOut(player.getRushingOut());
        snapshot.setAreaPositioning(player.getAreaPositioning());
        
        return snapshot;
    }
    
    /**
     * Get attribute value by name (using reflection-like approach)
     */
    public Float getAttributeValue(String attributeName) {
        switch (attributeName.toLowerCase()) {
            // Physical
            case "acceleration": return acceleration;
            case "dexterity": return dexterity;
            case "endurance": return endurance;
            case "jumping": return jumping;
            case "stamina": return stamina;
            case "strength": return strength;
            case "speed": return speed;
            
            // Mental
            case "concentration": return concentration;
            case "courage": return courage;
            case "determination": return determination;
            case "leadership": return leadership;
            case "perception": return perception;
            case "positioning": return positioning;
            case "teamwork": return teamwork;
            
            // Technical
            case "passing": return passing;
            case "kicking": return kicking;
            case "longshots": case "long_shots": return longShots;
            case "trickshots": case "trick_shots": return trickShots;
            case "heading": return heading;
            case "onetwos": case "one_twos": case "one-twos": return oneTwos;
            case "freekicks": case "free_kicks": case "free-kicks": return freeKicks;
            case "cornerkicks": case "corner_kicks": case "corner-kicks": return cornerKicks;
            case "penaltykicks": case "penalty_kicks": case "penalty-kicks": return penaltyKicks;
            case "throwins": case "throw_ins": case "throw-ins": return throwIns;
            case "marking": return marking;
            case "tackling": return tackling;
            
            // Goalkeeper
            case "shotstopping": case "shot_stopping": case "shot-stopping": return shotStopping;
            case "areacontrol": case "area_control": case "area-control": return areaControl;
            case "punching": return punching;
            case "handtohand": case "hand_to_hand": case "hand-to-hand": return handToHand;
            case "rushingout": case "rushing_out": case "rushing-out": return rushingOut;
            case "areapositioning": case "area_positioning": case "area-positioning": return areaPositioning;
            
            default: return null;
        }
    }
    
    // Getters and Setters
    public Long getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
    
    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }
    
    public void setSnapshotDate(LocalDateTime snapshotDate) {
        this.snapshotDate = snapshotDate;
    }
    
    public String getSnapshotType() {
        return snapshotType;
    }
    
    public void setSnapshotType(String snapshotType) {
        this.snapshotType = snapshotType;
    }
    
    // Physical attribute getters/setters
    public Float getAcceleration() { return acceleration; }
    public void setAcceleration(Float acceleration) { this.acceleration = acceleration; }
    
    public Float getDexterity() { return dexterity; }
    public void setDexterity(Float dexterity) { this.dexterity = dexterity; }
    
    public Float getEndurance() { return endurance; }
    public void setEndurance(Float endurance) { this.endurance = endurance; }
    
    public Float getJumping() { return jumping; }
    public void setJumping(Float jumping) { this.jumping = jumping; }
    
    public Float getStamina() { return stamina; }
    public void setStamina(Float stamina) { this.stamina = stamina; }
    
    public Float getStrength() { return strength; }
    public void setStrength(Float strength) { this.strength = strength; }
    
    public Float getSpeed() { return speed; }
    public void setSpeed(Float speed) { this.speed = speed; }
    
    // Mental attribute getters/setters
    public Float getConcentration() { return concentration; }
    public void setConcentration(Float concentration) { this.concentration = concentration; }
    
    public Float getCourage() { return courage; }
    public void setCourage(Float courage) { this.courage = courage; }
    
    public Float getDetermination() { return determination; }
    public void setDetermination(Float determination) { this.determination = determination; }
    
    public Float getLeadership() { return leadership; }
    public void setLeadership(Float leadership) { this.leadership = leadership; }
    
    public Float getPerception() { return perception; }
    public void setPerception(Float perception) { this.perception = perception; }
    
    public Float getPositioning() { return positioning; }
    public void setPositioning(Float positioning) { this.positioning = positioning; }
    
    public Float getTeamwork() { return teamwork; }
    public void setTeamwork(Float teamwork) { this.teamwork = teamwork; }
    
    // Technical attribute getters/setters
    public Float getPassing() { return passing; }
    public void setPassing(Float passing) { this.passing = passing; }
    
    public Float getKicking() { return kicking; }
    public void setKicking(Float kicking) { this.kicking = kicking; }
    
    public Float getLongShots() { return longShots; }
    public void setLongShots(Float longShots) { this.longShots = longShots; }
    
    public Float getTrickShots() { return trickShots; }
    public void setTrickShots(Float trickShots) { this.trickShots = trickShots; }
    
    public Float getHeading() { return heading; }
    public void setHeading(Float heading) { this.heading = heading; }
    
    public Float getOneTwos() { return oneTwos; }
    public void setOneTwos(Float oneTwos) { this.oneTwos = oneTwos; }
    
    public Float getFreeKicks() { return freeKicks; }
    public void setFreeKicks(Float freeKicks) { this.freeKicks = freeKicks; }
    
    public Float getCornerKicks() { return cornerKicks; }
    public void setCornerKicks(Float cornerKicks) { this.cornerKicks = cornerKicks; }
    
    public Float getPenaltyKicks() { return penaltyKicks; }
    public void setPenaltyKicks(Float penaltyKicks) { this.penaltyKicks = penaltyKicks; }
    
    public Float getThrowIns() { return throwIns; }
    public void setThrowIns(Float throwIns) { this.throwIns = throwIns; }
    
    public Float getMarking() { return marking; }
    public void setMarking(Float marking) { this.marking = marking; }
    
    public Float getTackling() { return tackling; }
    public void setTackling(Float tackling) { this.tackling = tackling; }
    
    // Goalkeeper attribute getters/setters
    public Float getShotStopping() { return shotStopping; }
    public void setShotStopping(Float shotStopping) { this.shotStopping = shotStopping; }
    
    public Float getAreaControl() { return areaControl; }
    public void setAreaControl(Float areaControl) { this.areaControl = areaControl; }
    
    public Float getPunching() { return punching; }
    public void setPunching(Float punching) { this.punching = punching; }
    
    public Float getHandToHand() { return handToHand; }
    public void setHandToHand(Float handToHand) { this.handToHand = handToHand; }
    
    public Float getRushingOut() { return rushingOut; }
    public void setRushingOut(Float rushingOut) { this.rushingOut = rushingOut; }
    
    public Float getAreaPositioning() { return areaPositioning; }
    public void setAreaPositioning(Float areaPositioning) { this.areaPositioning = areaPositioning; }
}

