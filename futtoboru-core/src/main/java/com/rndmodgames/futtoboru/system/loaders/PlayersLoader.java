package com.rndmodgames.futtoboru.system.loaders;

import java.util.List;

import com.rndmodgames.futtoboru.data.Club;

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

    /**
     * Load the existing Club Players from file
     * 
     *  - file structure:
     *      - all players on single file, kind of messi, easy at the beginning
     *      - one folder for players and one file per club by club id
     *          - complex but more flexible
     *          
     *      - one folder per club and a players file per club
     *      
     *  
     * 
     * @param clubs
     */
    public static void loadClubPlayers(List<Club> clubs) {
        
    }
    
    /**
     * Load Club Players
     */
//    public static void loadClubPlayers(Club club) {
//        
//        /**
//         * TODO: WIP
//         * 
//         *  - Load or Generate Players for the Season according to the game bundled files / database / seasons data
//         *  
//         *  - Existing Players will need contracts with the club to be generated
//         *  - We save the current players attached to the club
//         *  - We also need to save the player transfer history
//         *  
//         *  
//         */
//    }
    
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