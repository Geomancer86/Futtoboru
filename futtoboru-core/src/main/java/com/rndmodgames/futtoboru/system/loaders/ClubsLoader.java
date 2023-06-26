package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Season;

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
                         *  id, fullname, url_source, foundation_year
                         */
                        Club club = new Club();
                        
                        //
                        club.setId(Long.valueOf(splitted[0]));
                        club.setFullName(splitted[1]);
                        club.setUrlSource(splitted[2]);
                        club.setYear(Integer.valueOf(splitted[3]));
                        
                        //
                        season.getClubs().add(club);
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
        
        System.out.println("FINISHED LOADING " + season.getClubs().size() + " SEASON CLUBS");
    }
}