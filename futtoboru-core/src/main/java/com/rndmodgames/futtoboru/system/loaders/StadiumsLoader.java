package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.Stadium;

/**
 * StadiumsLoader
 * 
 * @author Geomancer86
 *
 */
public class StadiumsLoader {

    /**
     * Loads the Club Stadium Data
     */
    public static void loadStadium(Season season, Club club) {
        
        System.out.println("LOADING " + club.getName() + " STADIUM FROM FILE SYSTEM.");
        
        FileHandle clubStadiumFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/club_stadiums/" + club.getId() + ".txt");
        
     // 
        if (clubStadiumFile.exists()) {
            
            BufferedReader reader = new BufferedReader(clubStadiumFile.reader());
            
            String line;

            try {
                line = reader.readLine();

                // Use # symbol to comment a line
                while (line != null) {

                    // Skip empty lines and comment lines
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {

                        System.out.println(line);
                        
                        /**
                         * File Format - COLUMNS (v1.0 Enhanced)
                         * 
                         * id, name, year, capacity, value, description, urlSource, landValue, isOwned, annualRent
                         * 
                         * Description may contain commas, so we use regex to split ignoring commas inside quotes
                         */
                        String[] splitted = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                        
                        if (splitted.length < 4) {
                            System.out.println("ERROR: Stadium file has insufficient columns. Expected at least 4, got: " + splitted.length + " for line: " + line);
                            // Continue to next line (will be read at end of loop)
                            continue;
                        }
                        
                        Stadium stadium = new Stadium();
                        
                        // Id
                        stadium.setId(Long.valueOf(splitted[0].trim()));
                        
                        // Name
                        stadium.setName(splitted[1].trim());
                        
                        // Built Year (was column 2, now column 2)
                        if (splitted.length > 2 && !splitted[2].trim().isEmpty()) {
                            try {
                                stadium.setBuiltYear(Integer.valueOf(splitted[2].trim()));
                            } catch (NumberFormatException e) {
                                System.out.println("WARNING: Could not parse built year: " + splitted[2]);
                            }
                        }
                        
                        // Capacity
                        if (splitted.length > 3 && !splitted[3].trim().isEmpty()) {
                            stadium.setCapacity(Integer.valueOf(splitted[3].trim()));
                        }
                        
                        // Value (v1.0)
                        if (splitted.length > 4 && !splitted[4].trim().isEmpty()) {
                            try {
                                stadium.setValue(new BigDecimal(splitted[4].trim()));
                            } catch (NumberFormatException e) {
                                System.out.println("WARNING: Could not parse stadium value: " + splitted[4]);
                            }
                        }
                        
                        // Description (v1.0) - may be in quotes
                        if (splitted.length > 5 && !splitted[5].trim().isEmpty()) {
                            String desc = splitted[5].trim();
                            // Remove surrounding quotes if present
                            if (desc.startsWith("\"") && desc.endsWith("\"")) {
                                desc = desc.substring(1, desc.length() - 1);
                            }
                            stadium.setDescription(desc);
                        }
                        
                        // URL Source (v1.0)
                        if (splitted.length > 6 && !splitted[6].trim().isEmpty()) {
                            stadium.setUrlSource(splitted[6].trim());
                        }
                        
                        // Land Value (v1.0)
                        if (splitted.length > 7 && !splitted[7].trim().isEmpty()) {
                            try {
                                stadium.setLandValue(new BigDecimal(splitted[7].trim()));
                            } catch (NumberFormatException e) {
                                System.out.println("WARNING: Could not parse land value: " + splitted[7]);
                            }
                        }
                        
                        // Is Owned (v1.0)
                        if (splitted.length > 8 && !splitted[8].trim().isEmpty()) {
                            stadium.setOwned(Boolean.parseBoolean(splitted[8].trim()));
                        }
                        
                        // Annual Rent (v1.0)
                        if (splitted.length > 9 && !splitted[9].trim().isEmpty()) {
                            try {
                                stadium.setAnnualRent(new BigDecimal(splitted[9].trim()));
                            } catch (NumberFormatException e) {
                                System.out.println("WARNING: Could not parse annual rent: " + splitted[9]);
                            }
                        }
                        
                        // Set Stadium Club Owner Id
                        stadium.setClubOwnerId(club.getId());
                        
                        // Set Stadium to Club
                        club.setStadium(stadium);
                    }
                    
                    line = reader.readLine();
                    
                }
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
        } else {
            
            //
            System.out.println("mods/seasons/" + season.getId() + "/club_stadiums/" + club.getId() + ".txt doesnt exist");
        }
    }
}