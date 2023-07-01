package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

public class ClubsLoader {
    
    /**
     * Load Season Teams
     */
    public static void loadSeasonClubs(Season season) {
        
        FileHandle seasonClubsFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/clubs.txt");
        
        if (seasonClubsFile.exists()) {
            
            BufferedReader reader = new BufferedReader(seasonClubsFile.reader());
            
            String line;

            try {
                line = reader.readLine();

                // Use # symbol to comment a line
                while (line != null) {

                    if (!line.startsWith("#")) {

                        String[] splitted = line.split(",");

                        /**
                         * Clubs
                         * 
                         * COLUMNS
                         * 
                         *  id, name, fullname, url_source, foundation_year
                         */
                        Club club = new Club();
                        
                        //
                        club.setId(Long.valueOf(splitted[0]));
                        club.setName(splitted[1]);
                        club.setFullName(splitted[2]);
                        club.setUrlSource(splitted[3]);
                        club.setYear(Integer.valueOf(splitted[4]));
                        
                        // Country
                        club.setCountry(DatabaseLoader.getCountryById(Long.valueOf(splitted[5])));
                        
                        // Initialize Players At Club List
                        club.setPlayers(new ArrayList<>());
                        
                        //
                        season.getClubs().add(club);
                        
                        /**
                         * TODO WIP:
                         * 
                         *  - Add to the Clubs By ID HashMap
                         */
                    }

                    line = reader.readLine();
                }

            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
            
        } else {
            
            System.out.println("mods/seasons/" + season.getId() + "/clubs.txt doesnt exist");
        }
        
        /**
         * Update Clubs by Country HashMap
         */
        for (Club club : season.getClubs()) {
            
            //
            DatabaseLoader.getInstance().getClubsByCountry(club.getCountry()).add(club);
        }
        
        System.out.println("FINISHED LOADING " + season.getClubs().size() + " SEASON CLUBS");
    }
}