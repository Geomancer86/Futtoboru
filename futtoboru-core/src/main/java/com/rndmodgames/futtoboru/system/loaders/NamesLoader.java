package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Country;

/**
 * Names Loader v1
 * 
 *  - Loads the Names and Lastnames from Filesystem 
 *      and associates them to a Nationality (by folder) 
 *      to be used later on the Name Generator
 * 
 * @author Geomancer86
 */
public class NamesLoader {

    public static HashMap<String, List<String>> namesByCountry = new HashMap<>();
    public static HashMap<String, List<String>> lastnamesByCountry = new HashMap<>();
    
    /**
     * Load All Country Names
     */
    public static void loadAllCountryNames(List<Country> countries) {
        
        //
        for (Country country : countries) {
            
            //
            loadCountryNames(country);
        }
    }
    
    public static void loadCountryNames(Country country) {
        
        FileHandle countryNamesFile = Gdx.files.internal("mods/names/" + country.getCommonName() + "/names.txt");
        FileHandle countryLastnamesFile = Gdx.files.internal("mods/names/" + country.getCommonName() + "/lastnames.txt");
        
        // Load Name Files
        if (countryNamesFile.exists()) {
            
            //
            BufferedReader reader = new BufferedReader(countryNamesFile.reader());
            
            String line;

            try {
                line = reader.readLine();

                // Use # symbol to comment a line
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
                        
//                        System.out.println("PARSING LINE: " + line);
                        
                        // One Name Per Line
                        if (!namesByCountry.containsKey(country.getCommonName())) {
                            
                            namesByCountry.put(country.getCommonName(), new ArrayList<>());
                        }
                        
                        // Add name
                        namesByCountry.get(country.getCommonName()).add(line);
                    }
                    
                    // New Line
                    line = reader.readLine();
                }

            } catch (IOException e) {
                
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
                
        } else {
            
            // ignore
            // System.out.println("mods/names/" + country.getCommonName() + "/names.txt doesn't exist");
        }
        
        // Load Lastname Files
        if (countryLastnamesFile.exists()) {
            
        }
        
        // Stats
        if (namesByCountry.get(country.getCommonName()) != null) {
            // 
            System.out.println("Finished parsing " + country.getCommonName() + " names: " + namesByCountry.get(country.getCommonName()).size());
        }
    }
}