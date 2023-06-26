package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Seasons Loader v1
 * 
 * Used to load existing Seasons from File System
 * 
 * @author Geomancer86
 */
public class SeasonsLoader {

    /**
     * TODO WIP
     * 
     *  - 1888–89 Football League
     *  - https://en.wikipedia.org/wiki/1888%E2%80%9389_Football_League
     *  
     *  - 1st League Ever
     *  - 1st English Title
     *  
     *  - 12 Teams [TODO]
     *  
     *      - Preston North End         []
     *      - Aston Villa               []
     *      - Wolverhampton Wanderers   []
     *      - Blackburn Rovers          []
     *      - Bolton Wanderers          []
     *      - West Bromwich Albion      []
     *      - Accrington                []
     *      - Everton                   []
     *      - Burnley                   []
     *      - Derby County              []
     *      - Notts County              []
     *      - Stoke                     []
     *      
     *  - 132 Matches Played
     *  - 586 Goals (4.44 per match)
     *  
     *  - Scripts (TODO TBD WIP)
     *  
     *      - 1) Created and named in Manchester during a meeting on 17 April 1888
     *      - 2) It was not until late November that a points system was decided upon, with teams being awarded two points for a win and one point for a draw.
     *      - 3) Bottom four (4) clubs are obliged to retire and seek reelection at the Annual General Meeting (AGM) along with any other clubs wishing to become League members.
     *              - The 4 teams are re-elected for the next season.
     * @param seasons 
     */
    public static void load(List<Season> seasons) {

        /**
         * Iterate existing preloaded seasons
         */
        for (Season season : seasons) {

            //
            System.out.println("LOADING SEASON " + season.getName() + " FROM FILESYSTEM");

            // Load Season Countries
            loadSeasonCountries(season);
            
            // Load Season Clubs
            ClubsLoader.loadSeasonClubs(season);
        }
    }
    
    /**
     * Load Season Countries
     */
    public static void loadSeasonCountries(Season season) {
        
        /**
         * Seasons will be saved in a folder by ID
         */
        FileHandle seasonCountriesFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/countries.txt");

        if (seasonCountriesFile.exists()) {
            BufferedReader reader = new BufferedReader(seasonCountriesFile.reader());

            String line;

            try {
                line = reader.readLine();

                // Use # symbol to comment a line
                while (line != null) {

                    if (!line.startsWith("#")) {

                        String[] splitted = line.split(",");

                        /**
                         * FORMAT: COLUMNS id,
                         */
                        season.getCountries().add(DatabaseLoader.getCountryById(Long.valueOf(splitted[0])));
                    }

                    line = reader.readLine();
                }

            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
        } else {
            System.out.println("mods/seasons/" + season.getId() + "/countries.txt doesnt exist");
        }

        System.out.println("FINISHED LOADING " + season.getCountries().size() + " SEASON COUNTRIES");
    }
}