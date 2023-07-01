package com.rndmodgames.futtoboru.system.loaders;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;

/**
 * Players Loader v1
 * 
 *  - Load the Players Data from File System after Loading a Season / Clubs
 * 
 * @author Geomancer86
 */
public class PlayersLoader {

    /**
     * Load Club Players
     */
    public static void loadClubPlayers(Club club) {
        
        /**
         * TODO: WIP
         * 
         *  - Load or Generate Players for the Season according to the game bundled files / database / seasons data
         *  
         *  - Existing Players will need contracts with the club to be generated
         *  - We save the current players attached to the club
         *  - We also need to save the player transfer history
         *  
         *  
         */
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
    public static Player generateRandomPlayer() {
     
        Player player = new Player();
        
        /**
         * Generate Random Nationality (initial season should have mostly English players)
         * Generate Random Age (research original average player ages)
         * Generate Random Name (names list)
         * Generate Random Lastname (lastname list)
         * 
         * ---
         * Generate Random Basic Player Attributes
         * Generate Random Basic Goalkeeper Attributes
         */
        
        return player;
    }
}