package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
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
        
        FileHandle clubPlayersFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/club_players/" + club.getId() + ".txt");
        
        // 
        if (clubPlayersFile.exists()) {
            
            BufferedReader reader = new BufferedReader(clubPlayersFile.reader());
            
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
                         * id, name, lastname, country, birthdate
                         */
                        String[] splitted = line.split(",");
                        
                        // Validate we have at least 4 columns (id, name, lastname, country)
                        if (splitted.length < 4) {
                            System.out.println("ERROR: Invalid line format (expected at least 4 columns, got " + splitted.length + "): " + line);
                            line = reader.readLine();
                            continue;
                        }
                        
                        /**
                         * Create new Person to hold the personal data for this Player (name, nationality, age, etc).
                         */
                        Person person = new Person();
                        
                        // Name and Lastname
                        person.setId(Long.valueOf(splitted[0].trim()));
                        person.setName(splitted[1].trim());
                        person.setLastname(splitted[2].trim());
                        
                        // Country of birth (column 3 in actual file format)
                        person.setCountry(DatabaseLoader.getCountryById(Long.valueOf(splitted[3].trim())));
                        
                        /**
                         * Birthdate (column 4 in actual file format, optional)
                         * 
                         * NOTE: File format is: id, name, lastname, country, birthdate
                         * Some lines may be missing the birthdate column
                         * 
                         * NOTES: 
                         *  - all dates ingame are LocalDateTime
                         *  - cannot parse a localdatetime without the time portion
                         *  - do we really need the time for birthdates/etc?
                         *      
                         */
                        try {
                            
                            // Check if birthdate column exists
                            if (splitted.length < 5 || splitted[4].trim().isEmpty()) {
                                // No birthdate provided, randomize year (use 1865 as default historical year)
                                int year = 1865;
                                int month = DatabaseLoader.RNG.nextInt(12) + 1;
                                int day;
                                
                                switch(month) {
                                case 1,3,5,7,8,10,12:
                                    day = DatabaseLoader.RNG.nextInt(31) + 1;
                                    break;
                                case 2:
                                    day = DatabaseLoader.RNG.nextInt(28) + 1;
                                    break;
                                default:
                                    day = DatabaseLoader.RNG.nextInt(30) + 1;
                                    break;
                                }
                                
                                person.setBirthDate(LocalDate.of(year, month, day).atStartOfDay());
                                System.out.println("WARNING: No birthdate provided for " + person.getName() + " " + person.getLastname() + 
                                                 ", using randomized date: " + year + "-" + month + "-" + day);
                            } else {
                                person.setBirthDate(LocalDate.parse(splitted[4].trim(), PlayersLoader.birthDateFormatter).atStartOfDay());
                            }
                        
                        } catch (DateTimeParseException de) {
                            
                            /**
                             * Date parsing failed - could be:
                             * 1. Just a year: "1867"
                             * 2. Year and month: "1867-07"
                             * 3. Invalid format
                             * 
                             * Extract the year and randomize missing parts
                             */
                            // Check if birthdate column exists before accessing it
                            if (splitted.length < 5 || splitted[4].trim().isEmpty()) {
                                // No birthdate provided, randomize
                                int year = 1865;
                                int month = DatabaseLoader.RNG.nextInt(12) + 1;
                                int day;
                                
                                switch(month) {
                                case 1,3,5,7,8,10,12:
                                    day = DatabaseLoader.RNG.nextInt(31) + 1;
                                    break;
                                case 2:
                                    day = DatabaseLoader.RNG.nextInt(28) + 1;
                                    break;
                                default:
                                    day = DatabaseLoader.RNG.nextInt(30) + 1;
                                    break;
                                }
                                
                                person.setBirthDate(LocalDate.of(year, month, day).atStartOfDay());
                                System.out.println("WARNING: No birthdate provided for " + person.getName() + " " + person.getLastname() + 
                                                 ", using randomized date: " + year + "-" + month + "-" + day);
                            } else {
                                String dateStr = splitted[4].trim();
                                int year;
                                int month;
                                int day;
                                
                                // Try to extract year from the date string
                                if (dateStr.contains("-")) {
                                    // Has dashes, try to parse year-month or year-month-day
                                    String[] dateParts = dateStr.split("-");
                                    if (dateParts.length >= 1) {
                                        year = Integer.valueOf(dateParts[0]);
                                    } else {
                                        year = 1865; // Default fallback year
                                    }
                                    
                                    if (dateParts.length >= 2) {
                                        // Month is provided
                                        month = Integer.valueOf(dateParts[1]);
                                    } else {
                                        // Randomize month
                                        month = DatabaseLoader.RNG.nextInt(12) + 1;
                                    }
                                } else {
                                    // Just a year
                                    year = Integer.valueOf(dateStr);
                                    month = DatabaseLoader.RNG.nextInt(12) + 1;
                                }
                                
                                // Randomize day based on month
                                switch(month) {
                                case 1,3,5,7,8,10,12:
                                    day = DatabaseLoader.RNG.nextInt(31) + 1;
                                    break;
                                case 2:
                                    day = DatabaseLoader.RNG.nextInt(28) + 1;
                                    break;
                                default:
                                    day = DatabaseLoader.RNG.nextInt(30) + 1;
                                    break;
                                }
                                
                                // Set random birthday
                                person.setBirthDate(LocalDate.of(year, month, day).atStartOfDay());
                                
                                System.out.println("WARNING: Could not parse birthdate '" + dateStr + "' for " + person.getName() + " " + person.getLastname() + 
                                                 ", using randomized date: " + year + "-" + month + "-" + day);
                            }
                        }
                        
                        /**
                         * Create new Player
                         * 
                         * TODO: basic player attributes, positions, etc
                         */
                        Player player = new Player();
                        
                        // Set player ID to match person ID (players and persons share IDs)
                        player.setId(person.getId());
                        player.setPerson(person);
                        
                        // Generate attributes for loaded player (v1.0)
                        // Note: We need the season start date for proper age calculation
                        // For now, use person's birth date + 20 years as fallback
                        java.time.LocalDateTime estimatedDate = person.getBirthDate().plusYears(20);
                        com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator attrGen = 
                            new com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator();
                        attrGen.generatePlayerAttributes(player, person, estimatedDate);
                        
                        // Generate contract for player (v1.0)
                        // For Season 1, contracts are randomly generated
                        // Future seasons will load from scripts with researched historical data
                        java.time.LocalDateTime seasonStartDate = season.getStartDate() != null ? 
                            season.getStartDate() : estimatedDate;
                        com.rndmodgames.futtoboru.data.PlayerContract contract = 
                            com.rndmodgames.futtoboru.system.generators.PlayerContractGenerator.generateRandomContract(
                                player, club, seasonStartDate);
                        
                        if (contract != null) {
                            club.addPlayerContract(contract);
                            Gdx.app.debug("PlayersLoader", "Added contract to club " + club.getName() + 
                                " for player ID: " + contract.getPlayerId() + 
                                ", Total contracts at club: " + club.getPlayerContracts().size());
                        } else {
                            Gdx.app.error("PlayersLoader", "Contract generation returned NULL for player: " + 
                                (person.getName() + " " + person.getLastname()) + " at club: " + club.getName());
                        }
                        
                        // Assign profession (for amateur/semi-pro players)
                        // Professional players won't get professions (full-time football)
                        Integer contractType = contract != null ? contract.getContractType() : null;
                        com.rndmodgames.futtoboru.system.generators.PlayerProfessionAssigner.assignProfession(
                            player, club, contractType);
                        
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