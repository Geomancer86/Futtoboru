package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.RegionModifier;

/**
 * Region Modifiers Loader v1.0
 * 
 * Loads region modifiers from script files.
 * 
 * File format: mods/seasons/{seasonId}/region_modifiers.txt
 * Format: id,regionName,countryId,strengthMod,enduranceMod,...,applicableCities,historicalNotes
 * 
 * @author Geomancer86
 */
public class RegionModifiersLoader {
    
    private static final String MODIFIERS_FILE = "mods/seasons/18/region_modifiers.txt"; // TODO: Make season-specific
    
    private static Map<String, RegionModifier> modifiers = new HashMap<>(); // Key: regionName_countryId
    
    /**
     * Load all region modifiers from script file
     */
    public static void loadModifiers() {
        modifiers.clear();
        
        FileHandle modifiersFile = Gdx.files.internal(MODIFIERS_FILE);
        
        if (!modifiersFile.exists()) {
            Gdx.app.log("RegionModifiersLoader", "Modifiers file not found: " + MODIFIERS_FILE);
            Gdx.app.log("RegionModifiersLoader", "Will use default modifiers (all +0)");
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
                            RegionModifier modifier = new RegionModifier();
                            
                            // Basic info
                            modifier.setId(Long.parseLong(parts[0].trim()));
                            modifier.setRegionName(parts[1].trim());
                            modifier.setCountryId(Long.parseLong(parts[2].trim()));
                            
                            // Physical modifiers (parts 3-9)
                            if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                                modifier.setStrengthModifier(parseInt(parts[3]));
                            }
                            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                                modifier.setEnduranceModifier(parseInt(parts[4]));
                            }
                            if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                                modifier.setStaminaModifier(parseInt(parts[5]));
                            }
                            if (parts.length > 6 && !parts[6].trim().isEmpty()) {
                                modifier.setSpeedModifier(parseInt(parts[6]));
                            }
                            if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                                modifier.setAccelerationModifier(parseInt(parts[7]));
                            }
                            if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                                modifier.setJumpingModifier(parseInt(parts[8]));
                            }
                            if (parts.length > 9 && !parts[9].trim().isEmpty()) {
                                modifier.setDexterityModifier(parseInt(parts[9]));
                            }
                            
                            // Mental modifiers (parts 10-16)
                            if (parts.length > 10 && !parts[10].trim().isEmpty()) {
                                modifier.setConcentrationModifier(parseInt(parts[10]));
                            }
                            if (parts.length > 11 && !parts[11].trim().isEmpty()) {
                                modifier.setCourageModifier(parseInt(parts[11]));
                            }
                            if (parts.length > 12 && !parts[12].trim().isEmpty()) {
                                modifier.setDeterminationModifier(parseInt(parts[12]));
                            }
                            if (parts.length > 13 && !parts[13].trim().isEmpty()) {
                                modifier.setLeadershipModifier(parseInt(parts[13]));
                            }
                            if (parts.length > 14 && !parts[14].trim().isEmpty()) {
                                modifier.setPerceptionModifier(parseInt(parts[14]));
                            }
                            if (parts.length > 15 && !parts[15].trim().isEmpty()) {
                                modifier.setPositioningModifier(parseInt(parts[15]));
                            }
                            if (parts.length > 16 && !parts[16].trim().isEmpty()) {
                                modifier.setTeamworkModifier(parseInt(parts[16]));
                            }
                            
                            // Applicable cities (part 17)
                            if (parts.length > 17 && !parts[17].trim().isEmpty()) {
                                modifier.setApplicableCities(parts[17].trim());
                            }
                            
                            // Historical notes (part 18, may contain commas)
                            if (parts.length > 18) {
                                StringBuilder notes = new StringBuilder();
                                for (int i = 18; i < parts.length; i++) {
                                    if (i > 18) notes.append(",");
                                    notes.append(parts[i]);
                                }
                                modifier.setHistoricalNotes(notes.toString().trim());
                            }
                            
                            // Store by regionName_countryId for lookup
                            String key = modifier.getRegionName() + "_" + modifier.getCountryId();
                            modifiers.put(key, modifier);
                            
                        } catch (NumberFormatException e) {
                            Gdx.app.error("RegionModifiersLoader", "Error parsing modifier line: " + line + " - " + e.getMessage());
                        }
                    }
                }
                
                line = reader.readLine();
            }
            
            reader.close();
            
        } catch (IOException e) {
            Gdx.app.error("RegionModifiersLoader", "Error reading modifiers file: " + e.getMessage());
        }
        
        Gdx.app.log("RegionModifiersLoader", "Loaded " + modifiers.size() + " region modifiers");
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
     * Get modifier by region name and country ID
     */
    public static RegionModifier getModifier(String regionName, Long countryId) {
        String key = regionName + "_" + countryId;
        return modifiers.get(key);
    }
    
    /**
     * Get modifier by city name (searches applicableCities)
     */
    public static RegionModifier getModifierByCity(String cityName, Long countryId) {
        for (RegionModifier modifier : modifiers.values()) {
            if (modifier.getCountryId().equals(countryId) && 
                modifier.getApplicableCities() != null &&
                modifier.getApplicableCities().toLowerCase().contains(cityName.toLowerCase())) {
                return modifier;
            }
        }
        return null;
    }
    
    /**
     * Get all modifiers
     */
    public static Map<String, RegionModifier> getAllModifiers() {
        return new HashMap<>(modifiers);
    }
}
