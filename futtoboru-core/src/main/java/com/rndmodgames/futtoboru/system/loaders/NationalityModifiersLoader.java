package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.NationalityModifier;
import com.rndmodgames.futtoboru.system.DebugLogManager;

/**
 * Nationality Modifiers Loader v1.0
 * 
 * Loads nationality modifiers from script files.
 * 
 * File format: mods/seasons/{seasonId}/nationality_modifiers.txt
 * Format: countryId,countryName,strengthMod,enduranceMod,...,historicalNotes,footballingStrength
 * 
 * @author Geomancer86
 */
public class NationalityModifiersLoader {
    
    private static final String MODIFIERS_FILE = "mods/seasons/18/nationality_modifiers.txt"; // TODO: Make season-specific
    
    private static Map<Long, NationalityModifier> modifiers = new HashMap<>();
    
    /**
     * Load all nationality modifiers from script file
     */
    public static void loadModifiers() {
        modifiers.clear();
        
        FileHandle modifiersFile = Gdx.files.internal(MODIFIERS_FILE);
        
        if (!modifiersFile.exists()) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING, "NationalityModifiersLoader", "Modifiers file not found: " + MODIFIERS_FILE);
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING, "NationalityModifiersLoader", "Will use default modifiers (all +0)");
            return;
        }
        
        BufferedReader reader = new BufferedReader(modifiersFile.reader());
        String line;
        
        try {
            line = reader.readLine();
            
            while (line != null) {
                // Skip comments
                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                    String[] parts = line.split(",");
                    
                    if (parts.length >= 3) {
                        try {
                            NationalityModifier modifier = new NationalityModifier();
                            
                            // Basic info
                            modifier.setId(Long.parseLong(parts[0].trim()));
                            modifier.setCountryId(Long.parseLong(parts[0].trim())); // Same as ID for now
                            modifier.setCountryName(parts[1].trim());
                            
                            // Physical modifiers (parts 2-8)
                            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                                modifier.setStrengthModifier(parseInt(parts[2]));
                            }
                            if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                                modifier.setEnduranceModifier(parseInt(parts[3]));
                            }
                            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                                modifier.setStaminaModifier(parseInt(parts[4]));
                            }
                            if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                                modifier.setSpeedModifier(parseInt(parts[5]));
                            }
                            if (parts.length > 6 && !parts[6].trim().isEmpty()) {
                                modifier.setAccelerationModifier(parseInt(parts[6]));
                            }
                            if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                                modifier.setJumpingModifier(parseInt(parts[7]));
                            }
                            if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                                modifier.setDexterityModifier(parseInt(parts[8]));
                            }
                            
                            // Mental modifiers (parts 9-15)
                            if (parts.length > 9 && !parts[9].trim().isEmpty()) {
                                modifier.setConcentrationModifier(parseInt(parts[9]));
                            }
                            if (parts.length > 10 && !parts[10].trim().isEmpty()) {
                                modifier.setCourageModifier(parseInt(parts[10]));
                            }
                            if (parts.length > 11 && !parts[11].trim().isEmpty()) {
                                modifier.setDeterminationModifier(parseInt(parts[11]));
                            }
                            if (parts.length > 12 && !parts[12].trim().isEmpty()) {
                                modifier.setLeadershipModifier(parseInt(parts[12]));
                            }
                            if (parts.length > 13 && !parts[13].trim().isEmpty()) {
                                modifier.setPerceptionModifier(parseInt(parts[13]));
                            }
                            if (parts.length > 14 && !parts[14].trim().isEmpty()) {
                                modifier.setPositioningModifier(parseInt(parts[14]));
                            }
                            if (parts.length > 15 && !parts[15].trim().isEmpty()) {
                                modifier.setTeamworkModifier(parseInt(parts[15]));
                            }
                            
                            // Historical notes (part 16, may contain commas)
                            if (parts.length > 16) {
                                StringBuilder notes = new StringBuilder();
                                for (int i = 16; i < parts.length - 1; i++) {
                                    if (i > 16) notes.append(",");
                                    notes.append(parts[i]);
                                }
                                modifier.setHistoricalNotes(notes.toString().trim());
                            }
                            
                            // Footballing strength (last part)
                            if (parts.length > 17 && !parts[parts.length - 1].trim().isEmpty()) {
                                modifier.setFootbalingStrength(parseInt(parts[parts.length - 1]));
                            }
                            
                            modifiers.put(modifier.getCountryId(), modifier);
                            
                        } catch (NumberFormatException e) {
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_LOADING, "NationalityModifiersLoader", "Error parsing modifier line: " + line + " - " + e.getMessage());
                        }
                    }
                }
                
                line = reader.readLine();
            }
            
            reader.close();
            
        } catch (IOException e) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_LOADING, "NationalityModifiersLoader", "Error reading modifiers file: " + e.getMessage());
        }
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_DATA_LOADING, "NationalityModifiersLoader", "Loaded " + modifiers.size() + " nationality modifiers");
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
     * Get modifier by country ID
     */
    public static NationalityModifier getModifier(Long countryId) {
        return modifiers.get(countryId);
    }
    
    /**
     * Get all modifiers
     */
    public static Map<Long, NationalityModifier> getAllModifiers() {
        return new HashMap<>(modifiers);
    }
}
