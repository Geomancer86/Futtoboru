package com.rndmodgames.futtoboru.engine.jobs;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Club Staff Manager v1
 * 
 * Manages club staff positions and detects vacancies.
 * 
 * @author Geomancer86
 */
public class ClubStaffManager {

    private Futtoboru gameInstance;
    private SaveGame currentGame;

    public ClubStaffManager(Futtoboru gameInstance) {
        this.gameInstance = gameInstance;
        this.currentGame = gameInstance.getCurrentGame();
    }

    /**
     * Get staff member for a profession at a club
     * 
     * @param club The club
     * @param profession The profession
     * @return Person holding the position, or null if vacant
     */
    public Person getClubStaff(Club club, Profession profession) {
        if (club == null || profession == null) {
            return null;
        }
        
        Long personId = club.getStaffId(profession.getId());
        if (personId == null) {
            return null;
        }
        
        // Find person in SaveGame
        for (Person person : currentGame.getAllPersons()) {
            if (person.getId().equals(personId)) {
                return person;
            }
        }
        
        return null;
    }

    /**
     * Set staff member for a profession at a club
     * 
     * @param club The club
     * @param profession The profession
     * @param person The person (null to vacate position)
     */
    public void setClubStaff(Club club, Profession profession, Person person) {
        if (club == null || profession == null) {
            return;
        }
        
        Long personId = person != null ? person.getId() : null;
        club.setStaffId(profession.getId(), personId);
        
        // Update person's current club
        if (person != null) {
            person.setCurrentClubId(club.getId());
        }
    }

    /**
     * Check if a position is vacant
     * 
     * @param club The club
     * @param profession The profession
     * @return true if position is vacant
     */
    public boolean isPositionVacant(Club club, Profession profession) {
        if (club == null || profession == null) {
            return false;
        }
        
        return club.isPositionVacant(profession.getId());
    }

    /**
     * Get all vacant positions for a club
     * 
     * @param club The club
     * @return List of professions that are vacant
     */
    public List<Profession> getVacantPositions(Club club) {
        List<Profession> vacant = new ArrayList<>();
        
        try {
            if (club == null) {
                Gdx.app.log("ClubStaffManager", "getVacantPositions: club is null");
                return vacant;
            }
            
            // Check all selectable professions (v1.0: Manager, Director, Scout)
            DatabaseLoader dbLoader = DatabaseLoader.getInstance();
            if (dbLoader == null) {
                Gdx.app.error("ClubStaffManager", "DatabaseLoader.getInstance() returned null");
                return vacant;
            }
            
            List<Profession> selectableProfessions = dbLoader.getSelectableProfessions();
            if (selectableProfessions == null) {
                Gdx.app.error("ClubStaffManager", "getSelectableProfessions() returned null");
                return vacant;
            }
            
            for (Profession profession : selectableProfessions) {
                if (profession == null || profession.getId() == null) {
                    continue;
                }
                
                // Skip Player and Retired Player (not staff positions)
                if (profession.getId().equals(1L) || profession.getId().equals(2L)) {
                    continue;
                }
                
                if (isPositionVacant(club, profession)) {
                    vacant.add(profession);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("ClubStaffManager", "ERROR in getVacantPositions() for club: " + 
                         (club != null ? club.getName() : "null"), e);
            e.printStackTrace();
        }
        
        return vacant;
    }

    /**
     * Fire staff member (creates vacancy)
     * 
     * @param club The club
     * @param profession The profession
     */
    public void fireStaff(Club club, Profession profession) {
        if (club == null || profession == null) {
            return;
        }
        
        Person currentStaff = getClubStaff(club, profession);
        if (currentStaff != null) {
            // Remove from club
            setClubStaff(club, profession, null);
            
            // Set person as unemployed
            currentStaff.setCurrentClubId(null);
            
            Gdx.app.log("ClubStaffManager", "Fired " + profession.getName() + 
                       " from " + club.getName() + ": " + currentStaff.getName());
        }
    }

    /**
     * Hire staff member (fills vacancy)
     * 
     * @param club The club
     * @param profession The profession
     * @param person The person to hire
     */
    public void hireStaff(Club club, Profession profession, Person person) {
        if (club == null || profession == null || person == null) {
            return;
        }
        
        // Set staff
        setClubStaff(club, profession, person);
        
        // Update person's profession if needed
        if (person.getPrimaryProfession() == null || 
            !person.getPrimaryProfession().getId().equals(profession.getId())) {
            person.setPrimaryProfession(profession);
        }
        
        Gdx.app.log("ClubStaffManager", "Hired " + person.getName() + 
                   " as " + profession.getName() + " at " + club.getName());
    }
}

