package com.rndmodgames.futtoboru.system;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.rndmodgames.futtoboru.data.Person;

/**
 * Saving & Loading Game Systems
 * 
 * @author Geomancer86
 */
public class SaveLoadSystem {

    private static final String USER_HOME = "user.home";
    private static final String GAME_FOLDER = "/Documents/RndModGames/Futtuboru"; // TBD NAME
    private static final String MANAGERS_FOLDER = "/managers/";
    private static final String SAVEGAMES_FOLDER = "/savegames/";
    
    private static final String SAVE_EXTENSION = ".json";
    
    private static final boolean PRODUCTION_MODE_ENCRYPT = false;
    
    public static void saveGame(SaveGame saveGame, String filename) {
        
        String userHomePath = System.getProperty(USER_HOME);
        
        System.out.println("Saving Game File on USER HOME PATH: " + userHomePath);
        
        Json json = new Json();
        json.setSerializer(LocalDateTime.class, new JsonDateSerializer());

        FileHandle fileHandle = Gdx.files.absolute(userHomePath + GAME_FOLDER + SAVEGAMES_FOLDER + filename + SAVE_EXTENSION);
        
        // Encode only for Releases
        if (PRODUCTION_MODE_ENCRYPT) {
            
            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(saveGame)), false);
        } else {
            
//            System.out.println(json.prettyPrint(saveGame));
            
            // Save as Plain Text JSON
            fileHandle.writeString(json.prettyPrint(saveGame), false);
        }
        
        System.out.println("GAME SAVED!");
    }
    
    /**
     * TODO: fix integer keys on hashmaps deserialized as strings
     */
    public static SaveGame loadGame(String saveGameName) {

        Json json = new Json();
        json.setSerializer(LocalDateTime.class, new JsonDateSerializer());
        
        String userHomePath = System.getProperty(USER_HOME);
        FileHandle fileHandle = Gdx.files.absolute(userHomePath + GAME_FOLDER + SAVEGAMES_FOLDER + saveGameName);

        SaveGame saveGame = null;
        
        // Encode only for Releases
        if (PRODUCTION_MODE_ENCRYPT) {
            
            saveGame = json.fromJson(SaveGame.class, Base64Coder.decodeString(fileHandle.readString()));
        } else {
            
            saveGame = json.fromJson(SaveGame.class, fileHandle.readString());
        }

        return saveGame;
    }
    
    /**
     * This will return a List of Saved Game Names, so the we can show on a list and the user can pick
     */
    public static List<String> getSavedGames(){
        
        List<String> savedGames = new ArrayList<>();
        
        String userHomePath = System.getProperty(USER_HOME);
        
        System.out.println("Listing Saved Games on USER HOME PATH: " + userHomePath);
        
        FileHandle dirHandle = Gdx.files.absolute(userHomePath + GAME_FOLDER + SAVEGAMES_FOLDER);

        System.out.println("Found " + dirHandle.list().length + " Saved Games");
        
        for (FileHandle entry: dirHandle.list()) {
            
            // TODO: also validate if save game is right format
            if (entry.toString().contains(SAVE_EXTENSION)) {
                
                System.out.println("Saved Game: " + entry.toString());
                
                // 
                savedGames.add(entry.toString());
            }
        }
        
        return savedGames;
    }
    
    /**
     * This will create a Manager Save File
     */
    public static void saveManagerDataFile(Person manager, String filename) {
        
        String userHomePath = System.getProperty(USER_HOME);
        
        System.out.println("Saving New Manager File on USER HOME PATH: " + userHomePath);
        
        Json json = new Json();

        FileHandle fileHandle = Gdx.files.absolute(userHomePath + GAME_FOLDER + MANAGERS_FOLDER + filename + SAVE_EXTENSION);
        
        // Encode only for Releases
        if (PRODUCTION_MODE_ENCRYPT) {
            
            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(manager)), false);
        } else {
           
//            System.out.println(manager.getBirthdate());
            
            System.out.println(json.prettyPrint(manager));
//            System.out.println(json.toJson(manager));
            
            // Save as Plain Text JSON
            fileHandle.writeString(json.prettyPrint(manager), false);
        }
        
        System.out.println("NEW MANAGER SAVED!");
    }
    
    /**
     * This will return a list of the save files for existing Managers
     */
    public static List<String> listSavedManagers() {
        
        List<String> savedGames = new ArrayList<>();
        
        String userHomePath = System.getProperty(USER_HOME);
        
        System.out.println("Listing Saved Games on USER HOME PATH: " + userHomePath);
        
        FileHandle dirHandle = Gdx.files.absolute(userHomePath + GAME_FOLDER + MANAGERS_FOLDER);

        System.out.println("Found " + dirHandle.list().length + " Saved Games");
        
        for (FileHandle entry: dirHandle.list()) {
            
            // 
            if (entry.toString().contains(SAVE_EXTENSION)) {
                
                System.out.println("Saved Game: " + entry.toString());
                
                // 
                savedGames.add(entry.toString());
            }
        }
        
        return savedGames;
    }
    
    /**
     * Loads a Person from existing JSON file
     */
    public static Person loadPersonFromFile(String filename) {
        
        Json json = new Json();
        
        FileHandle fileHandle = Gdx.files.absolute(filename);

        Person person = null;
        
        // Decode only for Releases
        if (PRODUCTION_MODE_ENCRYPT) {
            person = json.fromJson(Person.class, Base64Coder.decodeString(fileHandle.readString()));
        } else {
            person = json.fromJson(Person.class, fileHandle.readString());
        }
        
        return person;
    }
}