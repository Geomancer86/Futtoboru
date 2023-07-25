package com.rndmodgames.futtoboru;

import java.time.LocalDateTime;
import java.time.Month;

import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Base Test Tools v1
 * 
 * Useful stuff to avoid duplicating code everywhere
 * 
 * @author Geomancer86
 */
public class BaseTestTools {

    /**
     * Initializes game data as if the player was starting a new game, 
     *  
     *  This is to make sure nothing is null when testing
     *  
     *  This is "hardcoded" for V1, so same game starting date, same data files, etc
     *  
     *  NOTE: some GDX stuff is not available during the static @before tests so that needs to be done in the actual @test
     */
    public static SaveGame initializeV1SaveGame(SaveGame saveGame) {

        //
        DatabaseLoader.getInstance();
        
        // game start date
        saveGame.setGameDate(LocalDateTime.of(1888, Month.APRIL, 1, 4, 00, 00)); // Game start date is April 1, 1888
        
        // v1 scripts
        saveGame.getGameScripts().addAll(DatabaseLoader.getInstance().getSeasons().get(0).getSeasonScripts());
        
        // game authority
        saveGame.setMainAuthority(DatabaseLoader.getMainAuthority());
        
        // game owner
        saveGame.setOwner(new Person());
        
        return saveGame;
    }
}