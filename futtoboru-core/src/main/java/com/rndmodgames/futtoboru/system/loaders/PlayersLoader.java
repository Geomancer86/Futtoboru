package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Players Loader v1
 * 
 *  - Load the Players Data from File System after Loading a Season / Clubs
 * 
 * TODO: WIP
 * 
 *  - Load original player data for clubs from file system
 *  
 *  - Generate basic contracts for the loaded players
 *      - Research for some historical data on average wages or contract durations/etc.
 * 
 *  - The Players/Squad Screen shows basic wage or contract data.
 *      - On player row click we go to the Player Detail Screen
 *  
 * @author Geomancer86
 */
public class PlayersLoader {

    //
    public static DateTimeFormatter birthDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
    /**
     * Load the existing Season Club Players from file
     *      
     *      - One folder per club and a players file per club
     *      
     *  
     */
    public static void loadSeasonClubPlayers(Season season, Club club) {
     
        System.out.println("LOADING " + club.getName() + " PLAYERS FROM FILE SYSTEM.");
        
        FileHandle playersClubsFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/players_clubs/" + club.getId() + ".txt");
        
        // 
        if (playersClubsFile.exists()) {
            
            BufferedReader reader = new BufferedReader(playersClubsFile.reader());
            
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
                         * id, name, lastname, birthdate, country, 
                         */
                        String[] splitted = line.split(",");
                        
                        /**
                         * Create new Person to hold the personal data for this Player (name, nationality, age, etc).
                         */
                        Person person = new Person();
                        
                        // Name and Lastname
                        person.setId(Long.valueOf(splitted[0]));
                        person.setName(splitted[1]);
                        person.setLastname(splitted[2]);
                        
                        // Country of birth
                        person.setCountry(DatabaseLoader.getCountryById(Long.valueOf(splitted[3])));
                        
                        /**
                         * Birthdate
                         * 
                         * NOTES: 
                         *  - all dates ingame are LocalDateTime
                         *  - cannot parse a localdatetime without the time portion
                         *  - do we really need the time for birthdates/etc?
                         *      
                         */
                        try {
                            person.setBirthDate(LocalDate.parse(splitted[4], PlayersLoader.birthDateFormatter).atStartOfDay());
                        
                        } catch (DateTimeParseException de) {
                            
                            /**
                             * unknown date, but year is OK, randomize day and month
                             * 
                             * TODO: proper day of month generation, this will overflow february and other 30 day months
                             */
                            int month = DatabaseLoader.RNG.nextInt(12) + 1;
                            int day = DatabaseLoader.RNG.nextInt(30) + 1;
                            
                            // Set random birthday for this year
                            person.setBirthDate(LocalDate.of(Integer.valueOf(splitted[4]), month, day).atStartOfDay());
                        }
                        
                        /**
                         * Create new Player
                         * 
                         * TODO: basic player attributes, positions, etc
                         */
                        Player player = new Player();
                        
                        player.setPerson(person);
                        
                        // Add to Players at Club list
                        club.getPlayers().add(player);
                    }
                    
                    line = reader.readLine();
                }

            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
                e.printStackTrace();
            }
            
        } else {
            
            System.out.println("mods/seasons/" + season.getId() + "/club/" + club.getId() + ".txt doesnt exist");
        }
    }
    
    /**
     * Generate a Random Player
     * 
     *  - need many parameters as possible to avoid too much randomization
     *  - need a way to generate players by level, so we can average better teams when randomizing, to approximate historic results
     *  - need names/lastnames crossed by nationality/country of birth
     *  
     *  - data structure to allow parents/sons as the game will span 140+ years
     *  
     *  - PROTOTYPE:
     *      - First name lists
     *      - Last name lists
     *      - Basic Attributes Only
     *      - Generate and Attach to Teams
     *      
     *      - Team Squad Screen List Players
     *      
     *      - Once all teams have ~30 Players Generated
     *      
     *      - Friendly Matches Scheduling
     *      
     */
//    public static Player generateRandomPlayer() {
//     
//        Player player = new Player();
//        
//        /**
//         * Generate Random Nationality (initial season should have mostly English players)
//         * Generate Random Age (research original average player ages)
//         * Generate Random Name (names list)
//         * Generate Random Lastname (lastname list)
//         * 
//         * ---
//         * Generate Random Basic Player Attributes
//         * Generate Random Basic Goalkeeper Attributes
//         */
//        
//        return player;
//    }
}