package com.rndmodgames.futtoboru.system;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
     *
     * <p>Returns {@code null} both when the save file is missing <b>and</b> when it exists but
     * cannot be parsed (truncated, hand-edited, or corrupted JSON). Callers ({@code LoadGameDialog})
     * already treat {@code null} as "could not load" — before this fix, a corrupt file threw an
     * unhandled {@code SerializationException} straight out of a UI input listener. Found by QA
     * review 2026-07-15; see {@code SaveLoadCorruptionTest}.
     */
    public static SaveGame loadGame(String saveGameName) {
        ensureSaveGamesDirectory();
        String baseName = normalizeSaveBasename(saveGameName);
        Json json = configuredJson();
        FileHandle fileHandle = Gdx.files.absolute(saveGamesDirectoryPath() + baseName + SAVE_EXTENSION);
        if (!fileHandle.exists()) {
            return null;
        }
        try {
            if (PRODUCTION_MODE_ENCRYPT) {
                return json.fromJson(SaveGame.class, Base64Coder.decodeString(fileHandle.readString()));
            }
            return json.fromJson(SaveGame.class, fileHandle.readString());
        } catch (RuntimeException e) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_DATA_LOADING,
                    "SaveLoadSystem", "Could not load save '" + baseName + "': file exists but is not valid save data", e);
            return null;
        }
    }
    
    /**
     * This will return a List of Saved Game Names, so the we can show on a list and the user can pick
     *
     * <p>Sorted most-recently-modified first (PL menu audit 2026-07-19: the Load Game list and the Main
     * Menu "Continue" shortcut both need a stable "most recent save" ordering, and file-system listing
     * order is not guaranteed to be that).
     */
    public static List<String> getSavedGames() {
        List<FileHandle> files = getSavedGameFiles();
        files.sort(Comparator.comparingLong(FileHandle::lastModified).reversed());
        List<String> savedGames = new ArrayList<>();
        for (FileHandle entry : files) {
            savedGames.add(entry.nameWithoutExtension());
        }
        return savedGames;
    }

    private static List<FileHandle> getSavedGameFiles() {
        ensureSaveGamesDirectory();
        List<FileHandle> files = new ArrayList<>();
        if (Gdx.files == null) {
            return files;
        }
        FileHandle dirHandle = Gdx.files.absolute(saveGamesDirectoryPath());
        if (!dirHandle.exists()) {
            return files;
        }
        for (FileHandle entry : dirHandle.list()) {
            if (entry.isDirectory()) {
                continue;
            }
            if (SAVE_EXTENSION.equals("." + entry.extension())) {
                files.add(entry);
            }
        }
        return files;
    }

    /**
     * Name (no extension) of the most recently modified save, or {@code null} if there are none.
     * Backs the Main Menu "Continue" shortcut (PL menu audit 2026-07-19).
     */
    public static String getMostRecentSaveGameName() {
        List<String> savedGames = getSavedGames();
        return savedGames.isEmpty() ? null : savedGames.get(0);
    }

    /**
     * Deletes a save file by base name. No-op (returns {@code false}) if it doesn't exist.
     */
    public static boolean deleteSaveGame(String saveGameName) {
        ensureSaveGamesDirectory();
        String baseName = normalizeSaveBasename(saveGameName);
        FileHandle fileHandle = Gdx.files.absolute(saveGamesDirectoryPath() + baseName + SAVE_EXTENSION);
        if (!fileHandle.exists()) {
            return false;
        }
        return fileHandle.delete();
    }
    
    private static String managersDirectoryPath() {
        return System.getProperty(USER_HOME) + GAME_FOLDER + MANAGERS_FOLDER;
    }

    private static void ensureManagersDirectory() {
        if (Gdx.files == null) {
            return;
        }
        FileHandle dir = Gdx.files.absolute(managersDirectoryPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Strips directory and extension from either a bare filename or a full path (as returned by
     * {@link #listSavedManagers()}) down to the base name expected by {@link #saveManagerDataFile}.
     */
    private static String managerBaseName(String filenameOrPath) {
        if (filenameOrPath == null) {
            return "";
        }
        String normalized = filenameOrPath.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String base = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        if (base.endsWith(SAVE_EXTENSION)) {
            base = base.substring(0, base.length() - SAVE_EXTENSION.length());
        }
        return base;
    }

    /**
     * Whether a manager file with this base name (e.g. a lastname) already exists — used to gate the
     * overwrite {@code ConfirmDialog} on {@code NewManagerScreen} (P0, pre-game menus audit 2026-07-19:
     * creating a manager that collided with an existing saved manager used to overwrite it silently).
     */
    public static boolean managerFileExists(String filename) {
        ensureManagersDirectory();
        String baseName = managerBaseName(filename);
        if (baseName.isEmpty()) {
            return false;
        }
        FileHandle fileHandle = Gdx.files.absolute(managersDirectoryPath() + baseName + SAVE_EXTENSION);
        return fileHandle.exists();
    }

    /**
     * This will create a Manager Save File
     */
    public static void saveManagerDataFile(Person manager, String filename) {
        
        ensureManagersDirectory();
        
        Json json = configuredJson();

        FileHandle fileHandle = Gdx.files.absolute(managersDirectoryPath() + filename + SAVE_EXTENSION);
        
        // Encode only for Releases
        if (PRODUCTION_MODE_ENCRYPT) {
            
            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(manager)), false);
        } else {
            
            // Save as Plain Text JSON
            fileHandle.writeString(json.prettyPrint(manager), false);
        }
    }

    /**
     * Deletes a saved manager file. Accepts either a bare base name or a full path (as returned by
     * {@link #listSavedManagers()}). No-op (returns {@code false}) if it doesn't exist.
     */
    public static boolean deleteSavedManager(String filenameOrPath) {
        ensureManagersDirectory();
        String baseName = managerBaseName(filenameOrPath);
        if (baseName.isEmpty()) {
            return false;
        }
        FileHandle fileHandle = Gdx.files.absolute(managersDirectoryPath() + baseName + SAVE_EXTENSION);
        if (!fileHandle.exists()) {
            return false;
        }
        return fileHandle.delete();
    }
    
    /**
     * This will return a list of the save files for existing Managers
     *
     * <p>Defensively creates the managers directory first (2026-07-19 fix): on a fresh install with no
     * managers ever saved, {@code dirHandle.list()} on a non-existent directory previously risked a
     * {@code NullPointerException}/crash the very first time {@code NewGameSeasonScreen} opened.
     */
    public static List<String> listSavedManagers() {
        
        ensureManagersDirectory();
        
        List<String> savedGames = new ArrayList<>();
        
        FileHandle dirHandle = Gdx.files.absolute(managersDirectoryPath());

        if (!dirHandle.exists()) {
            return savedGames;
        }

        for (FileHandle entry: dirHandle.list()) {
            
            // 
            if (entry.toString().contains(SAVE_EXTENSION)) {
                
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