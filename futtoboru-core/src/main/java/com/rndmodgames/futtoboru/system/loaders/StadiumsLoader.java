package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;

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

                    if (!line.startsWith("#")) {

                        System.out.println(line);
                        
                        /**
                         * File Format - COLUMNS
                         * 
                         * id, name, year, capacity, 
                         */
                        String[] splitted = line.split(",");
                        
                        
                        Stadium stadium = new Stadium();
                        
                        // Id
                        stadium.setId(Long.valueOf(splitted[0]));
                        
                        // Name
                        stadium.setName(splitted[1]);
                        
                        // Capacity
                        stadium.setCapacity(Integer.valueOf(splitted[3]));
                        
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