package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.generators.PersonGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerGenerator;

public class ClubsLoader {

    /**
     * Load Season Teams
     */
    public static void loadSeasonClubs(Season season) {
        
        FileHandle seasonClubsFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/clubs.txt");
    
        /**
         * We need a way to avoid duplicates, as we only check on the Persons generated in the Save Game
         * 
         * Note: this is a difference instance of person/player generatorss as doesn't have the game reference
         */
        PersonGenerator personGenerator = new PersonGenerator(null);
        PlayerGenerator playerGenerator = new PlayerGenerator(null);
        
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
                        
                        /**
                         * Load or Randomize Players At Club List
                         * 
                         * TODO: WIP
                         * 
                         *  - TODO/TBD: Load existing Players from File System Data Bundled with the Season
                         *  - TODO/TBD: Generate random players depending on basic attributes/scripts (for example, average player level = 8) so the clubs strenght is relative to historic values
                         *  - TODO/TBD: Pick the number and quality of Players to be generated, the stronger club should have more rotation of better players
                         */
                        club.setPlayers(new ArrayList<>());
                        
                        /**
                         * Quick and dirty random generation just to fill the list and make sure the screen looks Ok
                         */
                        int generate = 20;
                        
                        for (int a = 0; a < generate; a++) {
                            
                            // Add Random Player to Club
                            Person person = personGenerator.generateUniquePerson(club.getCountry(), season, true);
                            
//                            System.out.println("GENERATED: " + person);
                            
                            // Add to existing People List if not null (not duplicated)
                            if (person != null) {
                                
                                /**
                                 * TODO: this list is safe to clear after the new game is started or on loading a new game
                                 * TODO: delay until we press new game
                                 */
                                DatabaseLoader.getPersons().add(person);
                                
                                club.getPlayers().add(playerGenerator.generateRandomPlayer(person));
                                
                            } else {
                             
                                System.out.println("Generated Person Was Duplicated, Ignore!");
                            }
                        }
                        
                        //
                        season.getClubs().add(club);
                        
                        /**
                         * TODO WIP Load Club Stadium
                         */
                        
                        // Add to the Clubs By ID HashMap
                        DatabaseLoader.addClub(club);
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
        System.out.println("FINISHED LOADING / GENERATING " + DatabaseLoader.getPersons().size() + " CLUB PLAYERS");
    }
}