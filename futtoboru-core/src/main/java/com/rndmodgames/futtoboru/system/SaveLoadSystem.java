package com.rndmodgames.futtoboru.system;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.system.serializers.BigDecimalSerializer;
import com.rndmodgames.futtoboru.system.serializers.JsonDateSerializer;
import com.rndmodgames.futtoboru.system.serializers.JsonLocalDateSerializer;

/**
 * Saving & Loading Game Systems
 * 
 * TODO: use the same instance for serializer and deserializer everywhere
 * 
 * @author Geomancer86
 */
public class SaveLoadSystem {

    // TODO: parametrize and save in personal preferences
    private static final String USER_HOME = "user.home";
    private static final String GAME_FOLDER = "/Documents/RndModGames/Futtuboru";
    private static final String MANAGERS_FOLDER = "/managers/";
    private static final String SAVEGAMES_FOLDER = "/savegames/";
    
    private static final String SAVE_EXTENSION = ".json";

    private static String saveGamesDirectoryPath() {
        return System.getProperty(USER_HOME) + GAME_FOLDER + SAVEGAMES_FOLDER;
    }

    public static String normalizeSaveBasename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "savegame";
        }
        String base = filename.trim();
        if (base.endsWith(SAVE_EXTENSION)) {
            base = base.substring(0, base.length() - SAVE_EXTENSION.length());
        }
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        if (slash >= 0 && slash < base.length() - 1) {
            base = base.substring(slash + 1);
        }
        return base.isEmpty() ? "savegame" : base;
    }

    public static void ensureSaveGamesDirectory() {
        if (Gdx.files == null) {
            return;
        }
        FileHandle dir = Gdx.files.absolute(saveGamesDirectoryPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static final boolean PRODUCTION_MODE_ENCRYPT = false;

    private static Json configuredJson() {
        Json json = new Json();
        json.setSerializer(LocalDateTime.class, new JsonDateSerializer());
        json.setSerializer(LocalDate.class, new JsonLocalDateSerializer());
        json.setSerializer(BigDecimal.class, new BigDecimalSerializer());
        return json;
    }

    /**
     * In-memory JSON roundtrip (used by integration tests; no filesystem).
     */
    public static SaveGame copySaveGame(SaveGame saveGame) {
        Json json = configuredJson();
        return json.fromJson(SaveGame.class, json.toJson(saveGame));
    }

    public static <T> T copyViaJson(Class<T> type, T source) {
        Json json = configuredJson();
        return json.fromJson(type, json.toJson(source));
    }
    
    public static void saveGame(SaveGame saveGame, String filename) {
        ensureSaveGamesDirectory();
        String baseName = normalizeSaveBasename(filename);
        Json json = configuredJson();
        FileHandle fileHandle = Gdx.files.absolute(saveGamesDirectoryPath() + baseName + SAVE_EXTENSION);
        if (PRODUCTION_MODE_ENCRYPT) {
            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(saveGame)), false);
        } else {
            fileHandle.writeString(json.prettyPrint(saveGame), false);
        }
    }
    
    /**
     * TODO: fix integer keys on hashmaps deserialized as strings
     */
    public static SaveGame loadGame(String saveGameName) {
        ensureSaveGamesDirectory();
        String baseName = normalizeSaveBasename(saveGameName);
        Json json = configuredJson();
        FileHandle fileHandle = Gdx.files.absolute(saveGamesDirectoryPath() + baseName + SAVE_EXTENSION);
        if (!fileHandle.exists()) {
            return null;
        }
        if (PRODUCTION_MODE_ENCRYPT) {
            return json.fromJson(SaveGame.class, Base64Coder.decodeString(fileHandle.readString()));
        }
        return json.fromJson(SaveGame.class, fileHandle.readString());
    }
    
    /**
     * This will return a List of Saved Game Names, so the we can show on a list and the user can pick
     */
    public static List<String> getSavedGames() {
        ensureSaveGamesDirectory();
        List<String> savedGames = new ArrayList<>();
        if (Gdx.files == null) {
            return savedGames;
        }
        FileHandle dirHandle = Gdx.files.absolute(saveGamesDirectoryPath());
        if (!dirHandle.exists()) {
            return savedGames;
        }
        for (FileHandle entry : dirHandle.list()) {
            if (entry.isDirectory()) {
                continue;
            }
            if (SAVE_EXTENSION.equals("." + entry.extension())) {
                savedGames.add(entry.nameWithoutExtension());
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
        
        Json json = configuredJson();

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
        
        Json json = configuredJson();
        
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