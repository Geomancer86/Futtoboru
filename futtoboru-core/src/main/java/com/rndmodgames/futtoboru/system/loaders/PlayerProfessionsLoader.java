package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Player Professions Loader v1.0
 * 
 * Loads player professions from script files.
 * 
 * File format: mods/seasons/{seasonId}/player_professions.txt
 * Format: id,name,category,strengthBonus,enduranceBonus,staminaBonus,speedBonus,accelerationBonus,jumpingBonus,dexterityBonus,concentrationBonus,courageBonus,determinationBonus,leadershipBonus,perceptionBonus,positioningBonus,teamworkBonus,historicalNotes
 * 
 * @author Geomancer86
 */
public class PlayerProfessionsLoader {
    
    private static final String PROFESSIONS_FILE = "mods/seasons/18/player_professions.txt"; // TODO: Make season-specific
    
    private static Map<Long, PlayerProfession> professions = new HashMap<>();
    
    /**
     * Load all player professions from script file
     */
    public static void loadProfessions() {
        professions.clear();
        
        FileHandle professionsFile = Gdx.files.internal(PROFESSIONS_FILE);
        
        if (!professionsFile.exists()) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING, "PlayerProfessionsLoader", "Professions file not found: " + PROFESSIONS_FILE);
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING, "PlayerProfessionsLoader", "Will use default professions or random generation");
            return;
        }
        
        BufferedReader reader = new BufferedReader(professionsFile.reader());
        String line;
        
        try {
            line = reader.readLine();
            
            while (line != null) {
                // Skip comments
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    
                    if (parts.length >= 3) {
                        try {
                            PlayerProfession profession = new PlayerProfession();
                            
                            // Basic info
                            profession.setId(Long.parseLong(parts[0].trim()));
                            profession.setName(parts[1].trim());
                            profession.setCategory(parts[2].trim());
                            
                            // Physical bonuses (parts 3-9)
                            if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                                profession.setStrengthBonus(parseInt(parts[3]));
                            }
                            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                                profession.setEnduranceBonus(parseInt(parts[4]));
                            }
                            if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                                profession.setStaminaBonus(parseInt(parts[5]));
                            }
                            if (parts.length > 6 && !parts[6].trim().isEmpty()) {
                                profession.setSpeedBonus(parseInt(parts[6]));
                            }
                            if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                                profession.setAccelerationBonus(parseInt(parts[7]));
                            }
                            if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                                profession.setJumpingBonus(parseInt(parts[8]));
                            }
                            if (parts.length > 9 && !parts[9].trim().isEmpty()) {
                                profession.setDexterityBonus(parseInt(parts[9]));
                            }
                            
                            // Mental bonuses (parts 10-16)
                            if (parts.length > 10 && !parts[10].trim().isEmpty()) {
                                profession.setConcentrationBonus(parseInt(parts[10]));
                            }
                            if (parts.length > 11 && !parts[11].trim().isEmpty()) {
                                profession.setCourageBonus(parseInt(parts[11]));
                            }
                            if (parts.length > 12 && !parts[12].trim().isEmpty()) {
                                profession.setDeterminationBonus(parseInt(parts[12]));
                            }
                            if (parts.length > 13 && !parts[13].trim().isEmpty()) {
                                profession.setLeadershipBonus(parseInt(parts[13]));
                            }
                            if (parts.length > 14 && !parts[14].trim().isEmpty()) {
                                profession.setPerceptionBonus(parseInt(parts[14]));
                            }
                            if (parts.length > 15 && !parts[15].trim().isEmpty()) {
                                profession.setPositioningBonus(parseInt(parts[15]));
                            }
                            if (parts.length > 16 && !parts[16].trim().isEmpty()) {
                                profession.setTeamworkBonus(parseInt(parts[16]));
                            }
                            
                            // Historical notes (part 17, may contain commas, so join rest)
                            if (parts.length > 17) {
                                StringBuilder notes = new StringBuilder();
                                for (int i = 17; i < parts.length; i++) {
                                    if (i > 17) notes.append(",");
                                    notes.append(parts[i]);
                                }
                                profession.setHistoricalNotes(notes.toString().trim());
                            }
                            
                            professions.put(profession.getId(), profession);
                            
                        } catch (NumberFormatException e) {
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_LOADING, "PlayerProfessionsLoader", "Error parsing profession line: " + line + " - " + e.getMessage());
                        }
                    }
                }
                
                line = reader.readLine();
            }
            
            reader.close();
            
        } catch (IOException e) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_LOADING, "PlayerProfessionsLoader", "Error reading professions file: " + e.getMessage());
        }
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING, "PlayerProfessionsLoader", "Loaded " + professions.size() + " player professions");
    }
    
    /**
     * Parse integer with null handling
     */
    private static Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("null")) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Get profession by ID
     */
    public static PlayerProfession getProfession(Long id) {
        return professions.get(id);
    }
    
    /**
     * Get all professions
     */
    public static Map<Long, PlayerProfession> getAllProfessions() {
        return new HashMap<>(professions);
    }
    
    /**
     * Get professions by category
     */
    public static Map<Long, PlayerProfession> getProfessionsByCategory(String category) {
        Map<Long, PlayerProfession> result = new HashMap<>();
        for (PlayerProfession prof : professions.values()) {
            if (prof.getCategory() != null && prof.getCategory().equalsIgnoreCase(category)) {
                result.put(prof.getId(), prof);
            }
        }
        return result;
    }
}
