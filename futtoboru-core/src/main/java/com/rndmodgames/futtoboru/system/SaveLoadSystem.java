package com.rndmodgames.futtoboru.system;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;
import com.rndmodgames.futtoboru.system.save.SaveSummary;
import com.rndmodgames.futtoboru.system.serializers.BigDecimalSerializer;
import com.rndmodgames.futtoboru.system.serializers.JsonDateSerializer;

/**
 * Saving & Loading Game Systems
 *
 * Saves v1 (P2-5): one file per save slot, {@code <name>.json} in {@code ~/Documents/RndModGames/Futtoboru/savegames/},
 * written by {@link SaveCodec} (versioned, data by id, game objects once). The folder is made on the first save; an
 * old {@code Futtuboru} folder (the earlier spelling) is moved over once, never overwriting.
 *
 * @author Geomancer86
 */
public class SaveLoadSystem {

    private static final String USER_HOME = "user.home";
    private static final String RNDMOD_FOLDER = "Documents/RndModGames";
    private static final String GAME_FOLDER_NAME = "Futtoboru";
    /** The folder's earlier, misspelt name: its contents are moved to {@link #GAME_FOLDER_NAME} once. */
    static final String OLD_GAME_FOLDER_NAME = "Futtuboru";
    private static final String MANAGERS_FOLDER = "managers";
    private static final String SAVEGAMES_FOLDER = "savegames";

    public static final String SAVE_EXTENSION = ".json";

    private static final boolean PRODUCTION_MODE_ENCRYPT = false;

    /** Set by tests (a temporary folder); null = the player's Documents folder. */
    private static File gameFolderOverride;
    private static boolean oldFolderChecked;

    /** Tests only: where the game folder is (null restores the default). Tests never touch the real Documents. */
    public static synchronized void setGameFolder(File folder) {
        gameFolderOverride = folder;
    }

    /** @return the game folder; the first time, moves the old misspelt folder's contents into it */
    public static synchronized File getGameFolder() {
        if (gameFolderOverride != null) {
            return gameFolderOverride;
        }
        File parent = new File(System.getProperty(USER_HOME), RNDMOD_FOLDER);
        File folder = new File(parent, GAME_FOLDER_NAME);
        if (!oldFolderChecked) {
            oldFolderChecked = true;
            moveOldFolder(new File(parent, OLD_GAME_FOLDER_NAME), folder);
        }
        return folder;
    }

    public static File getSavesFolder() {
        return new File(getGameFolder(), SAVEGAMES_FOLDER);
    }

    private static File getManagersFolder() {
        return new File(getGameFolder(), MANAGERS_FOLDER);
    }

    /**
     * Moves every file of {@code old} into {@code target} (same relative path) unless one is already there; files
     * that would be overwritten stay where they are. Folders left empty are removed.
     *
     * @return how many files were moved
     */
    public static int moveOldFolder(File old, File target) {
        if (!old.isDirectory()) {
            return 0;
        }
        int moved = 0;
        Path from = old.toPath();
        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(from)) {
            walk.filter(Files::isRegularFile).forEach(files::add);
        } catch (IOException e) {
            log("could not list " + old + ": " + e.getMessage());
            return 0;
        }
        for (Path f : files) {
            Path to = target.toPath().resolve(from.relativize(f).toString());
            if (Files.exists(to)) {
                log("kept " + f + ": " + to + " already exists");
                continue;
            }
            try {
                Files.createDirectories(to.getParent());
                Files.move(f, to);
                moved++;
            } catch (IOException e) {
                log("could not move " + f + ": " + e.getMessage());
            }
        }
        // remove the folders that are now empty (deepest first)
        try (Stream<Path> walk = Files.walk(from)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).filter(File::isDirectory).forEach(d -> {
                String[] left = d.list();
                if (left != null && left.length == 0 && !d.delete()) {
                    log("could not remove " + d);
                }
            });
        } catch (IOException e) {
            log("could not clean " + old + ": " + e.getMessage());
        }
        if (moved > 0) {
            log("moved " + moved + " files from " + old + " to " + target);
        }
        return moved;
    }

    // ------------------------------------------------------------------------------------------------- saves

    /** @return a file name for a save name: letters, digits, dot, dash and underscore only, never empty */
    public static String fileNameFor(String saveName) {
        String base = saveName == null ? "" : saveName.trim().replaceAll("[^A-Za-z0-9._-]+", "_");
        base = base.replaceAll("^[._]+", "").replaceAll("[._]+$", "");
        if (base.length() > 60) {
            base = base.substring(0, 60);
        }
        if (base.isEmpty()) {
            base = "save";
        }
        if (base.toUpperCase(Locale.ROOT).matches("(CON|PRN|AUX|NUL|COM\\d|LPT\\d)(\\..*)?")) {
            base = "save_" + base;
        }
        return base + SAVE_EXTENSION;
    }

    /** @return the file a save name goes to (it may exist: saving overwrites it) */
    public static File saveFileFor(String saveName) {
        return new File(getSavesFolder(), fileNameFor(saveName));
    }

    /**
     * Saves the game to the slot {@code saveName}: a new file, or the slot's file overwritten. The folder is made
     * when missing.
     *
     * @return the file written
     */
    public static File saveGame(SaveGame saveGame, String saveName) throws IOException {
        File file = saveFileFor(saveName);
        Files.createDirectories(file.getParentFile().toPath());
        String name = saveName == null || saveName.trim().isEmpty() ? file.getName() : saveName.trim();
        String text = SaveCodec.encode(saveGame, name, LocalDateTime.now());
        // write next to it first, so a failed write never destroys the slot's previous save
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        Files.write(tmp.toPath(), text.getBytes(StandardCharsets.UTF_8));
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        saveGame.setIsSaved(true);
        saveGame.setSaveName(name);
        log("game saved to " + file);
        return file;
    }

    /** Loads a save file; never throws (see {@link LoadResult}). */
    public static LoadResult loadGame(File file) {
        String text;
        try {
            text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log("could not read " + file + ": " + e.getMessage());
            return SaveCodec.decode("");
        }
        LoadResult result = SaveCodec.decode(text);
        if (result.isLoaded()) {
            result.getGame().setSaveName(readSummary(file, text).getName());
            result.getGame().setIsSaved(true);
        }
        for (String problem : result.getProblems()) {
            DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_SYSTEM_INIT, "SaveLoadSystem",
                    file.getName() + ": " + problem);
        }
        log("loaded " + file + ": " + result.getStatus() + ", " + result.getProblems().size() + " problems");
        return result;
    }

    /** @return the saves in the saves folder, newest first (old and unreadable files too, marked not loadable) */
    public static List<SaveSummary> listSaves() {
        List<SaveSummary> out = new ArrayList<>();
        File[] files = getSavesFolder().listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(SAVE_EXTENSION));
        if (files == null) {
            return out;
        }
        for (File f : files) {
            try {
                out.add(readSummary(f, new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.add(new SaveSummary(f, null, f.getName(), null, null, null, fileTime(f), LoadResult.Status.UNREADABLE));
            }
        }
        out.sort(Comparator.comparing(SaveSummary::getSavedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(s -> s.getFile().getName()));
        return out;
    }

    /** @return the summary fields of a save's text (a row that can't be loaded says why) */
    public static SaveSummary readSummary(File file, String text) {
        JsonValue root;
        try {
            root = new JsonReader().parse(text);
        } catch (SerializationException | IllegalArgumentException e) {
            root = null;
        }
        String fallbackName = file.getName().replaceAll("\\.json$", "");
        if (root == null || !root.isObject()) {
            return new SaveSummary(file, null, fallbackName, null, null, null, fileTime(file), LoadResult.Status.UNREADABLE);
        }
        JsonValue sv = root.get("saveVersion");
        if (sv == null || !sv.isLong()) {
            return new SaveSummary(file, null, fallbackName, null, null, null, fileTime(file), LoadResult.Status.OLDER_VERSION);
        }
        Integer version = sv.asInt();
        LoadResult.Status status = version > SaveCodec.SAVE_VERSION ? LoadResult.Status.NEWER_VERSION : LoadResult.Status.OK;
        JsonValue s = root.get("summary");
        String name = s != null ? s.getString("name", fallbackName) : fallbackName;
        String manager = s != null ? s.getString("manager", null) : null;
        String club = s != null ? s.getString("club", null) : null;
        LocalDateTime gameDate = s != null ? parse(s.getString("gameDate", null)) : null;
        LocalDateTime savedAt = parse(root.getString("savedAt", null));
        return new SaveSummary(file, version, name, manager, club, gameDate, savedAt != null ? savedAt : fileTime(file),
                status);
    }

    private static LocalDateTime parse(String s) {
        try {
            return s == null ? null : LocalDateTime.parse(s);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LocalDateTime fileTime(File f) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.systemDefault());
    }

    private static void log(String message) {
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_SYSTEM_INIT, "SaveLoadSystem", message);
    }

    // ---------------------------------------------------------------------------------------------- managers

    /**
     * This will create a Manager Save File
     */
    public static void saveManagerDataFile(Person manager, String filename) {

        Json json = new Json();
        json.setSerializer(LocalDateTime.class, new JsonDateSerializer());
        json.setSerializer(BigDecimal.class, new BigDecimalSerializer());

        File folder = getManagersFolder();
        if (!folder.isDirectory() && !folder.mkdirs()) {
            log("could not make " + folder);
        }
        FileHandle fileHandle = Gdx.files.absolute(new File(folder, filename + SAVE_EXTENSION).getAbsolutePath());

        // Encode only for Releases
        if (PRODUCTION_MODE_ENCRYPT) {
            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(manager)), false);
        } else {
            // Save as Plain Text JSON
            fileHandle.writeString(json.prettyPrint(manager), false);
        }

        log("new manager saved: " + fileHandle.path());
    }

    /**
     * This will return a list of the save files for existing Managers (absolute paths)
     */
    public static List<String> listSavedManagers() {

        List<String> managers = new ArrayList<>();

        File[] files = getManagersFolder().listFiles((dir, name) -> name.endsWith(SAVE_EXTENSION));
        if (files == null) {
            return managers;
        }
        Arrays.sort(files);
        for (File entry : files) {
            managers.add(entry.getAbsolutePath().replace('\\', '/'));
        }
        return managers;
    }

    /**
     * Loads a Person from existing JSON file
     */
    public static Person loadPersonFromFile(String filename) {

        Json json = new Json();
        json.setSerializer(LocalDateTime.class, new JsonDateSerializer());
        json.setSerializer(BigDecimal.class, new BigDecimalSerializer());

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
