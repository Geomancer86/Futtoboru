package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Player v1
 * 
 * Sources:
 *  
 *  - https://www.guidetofm.com/players/attributes/
 * 
 * @author Geomancer86
 */
public class Player implements Serializable {

    private static final long serialVersionUID = -9207330628639921895L;
    
    private Long id;
    private Person person;
    
    /**
     * Player Physical Attributes
     */
    private Float acceleration;
    private Float dexterity;
    private Float endurance;
    private Float jumping;
    private Float stamina;
    private Float strength;
    private Float speed;
    
    /**
     * Player Mental Attributes
     */
    private Float concentration;
    private Float courage;
    private Float determination;
    private Float leadership;
    private Float perception;
    private Float positioning;
    private Float teamwork;
    
    /**
     * Player Technical Attributes
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
     * Goalkeeper Technical Attributes
     */
    private Float shotStopping;
    private Float areaControl;
    private Float punching;
    private Float handToHand;
    private Float rushingOut;
    private Float areaPositioning;
    
    /**
     * Hidden Attributes
     * 
     * TODO
     */
    
    //
    public Player() {
        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Float acceleration) {
        this.acceleration = acceleration;
    }

    public Float getDexterity() {
        return dexterity;
    }

    public void setDexterity(Float dexterity) {
        this.dexterity = dexterity;
    }

    public Float getEndurance() {
        return endurance;
    }

    public void setEndurance(Float endurance) {
        this.endurance = endurance;
    }

    public Float getJumping() {
        return jumping;
    }

    public void setJumping(Float jumping) {
        this.jumping = jumping;
    }

    public Float getStamina() {
        return stamina;
    }

    public void setStamina(Float stamina) {
        this.stamina = stamina;
    }

    public Float getStrength() {
        return strength;
    }

    public void setStrength(Float strength) {
        this.strength = strength;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Float getConcentration() {
        return concentration;
    }

    public void setConcentration(Float concentration) {
        this.concentration = concentration;
    }

    public Float getCourage() {
        return courage;
    }

    public void setCourage(Float courage) {
        this.courage = courage;
    }

    public Float getDetermination() {
        return determination;
    }

    public void setDetermination(Float determination) {
        this.determination = determination;
    }

    public Float getLeadership() {
        return leadership;
    }

    public void setLeadership(Float leadership) {
        this.leadership = leadership;
    }

    public Float getPerception() {
        return perception;
    }

    public void setPerception(Float perception) {
        this.perception = perception;
    }

    public Float getPositioning() {
        return positioning;
    }

    public void setPositioning(Float positioning) {
        this.positioning = positioning;
    }

    public Float getTeamwork() {
        return teamwork;
    }

    public void setTeamwork(Float teamwork) {
        this.teamwork = teamwork;
    }

    public Float getPassing() {
        return passing;
    }

    public void setPassing(Float passing) {
        this.passing = passing;
    }

    public Float getKicking() {
        return kicking;
    }

    public void setKicking(Float kicking) {
        this.kicking = kicking;
    }

    public Float getLongShots() {
        return longShots;
    }

    public void setLongShots(Float longShots) {
        this.longShots = longShots;
    }

    public Float getTrickShots() {
        return trickShots;
    }

    public void setTrickShots(Float trickShots) {
        this.trickShots = trickShots;
    }

    public Float getHeading() {
        return heading;
    }

    public void setHeading(Float heading) {
        this.heading = heading;
    }

    public Float getOneTwos() {
        return oneTwos;
    }

    public void setOneTwos(Float oneTwos) {
        this.oneTwos = oneTwos;
    }

    public Float getFreeKicks() {
        return freeKicks;
    }

    public void setFreeKicks(Float freeKicks) {
        this.freeKicks = freeKicks;
    }

    public Float getCornerKicks() {
        return cornerKicks;
    }

    public void setCornerKicks(Float cornerKicks) {
        this.cornerKicks = cornerKicks;
    }

    public Float getPenaltyKicks() {
        return penaltyKicks;
    }

    public void setPenaltyKicks(Float penaltyKicks) {
        this.penaltyKicks = penaltyKicks;
    }

    public Float getThrowIns() {
        return throwIns;
    }

    public void setThrowIns(Float throwIns) {
        this.throwIns = throwIns;
    }

    public Float getMarking() {
        return marking;
    }

    public void setMarking(Float marking) {
        this.marking = marking;
    }

    public Float getTackling() {
        return tackling;
    }

    public void setTackling(Float tackling) {
        this.tackling = tackling;
    }

    public Float getShotStopping() {
        return shotStopping;
    }

    public void setShotStopping(Float shotStopping) {
        this.shotStopping = shotStopping;
    }

    public Float getAreaControl() {
        return areaControl;
    }

    public void setAreaControl(Float areaControl) {
        this.areaControl = areaControl;
    }

    public Float getPunching() {
        return punching;
    }

    public void setPunching(Float punching) {
        this.punching = punching;
    }

    public Float getHandToHand() {
        return handToHand;
    }

    public void setHandToHand(Float handToHand) {
        this.handToHand = handToHand;
    }

    public Float getRushingOut() {
        return rushingOut;
    }

    public void setRushingOut(Float rushingOut) {
        this.rushingOut = rushingOut;
    }

    public Float getAreaPositioning() {
        return areaPositioning;
    }

    public void setAreaPositioning(Float areaPositioning) {
        this.areaPositioning = areaPositioning;
    }
}