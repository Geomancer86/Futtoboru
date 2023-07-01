package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            loadCountryNames(country, "names.txt", namesByCountry);
            loadCountryNames(country, "lastnames.txt", lastnamesByCountry);
        }
    }

    //
    public static void loadCountryNames(Country country, String file, HashMap<String, List<String>> namesMap) {
        
        FileHandle countryNamesFile = Gdx.files.internal("mods/names/" + country.getCommonName() + "/" + file);
        
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
                        if (!namesMap.containsKey(country.getCommonName())) {
                            
                            namesMap.put(country.getCommonName(), new ArrayList<>());
                        }
                        
                        // Add name
                        namesMap.get(country.getCommonName()).add(line);
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
        
        // Stats
        if (namesMap.get(country.getCommonName()) != null) {
            // 
            System.out.println("Finished parsing " + country.getCommonName() + " " + file + ": " + namesMap.get(country.getCommonName()).size());
        }
    }
}