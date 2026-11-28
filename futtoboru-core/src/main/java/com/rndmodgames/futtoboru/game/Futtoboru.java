package com.rndmodgames.futtoboru.game;

import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.utils.I18NBundle;
import com.kotcrab.vis.ui.VisUI;
import com.rndmodgames.PreferencesManager;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.save.SaveSlots;
import com.rndmodgames.futtoboru.screens.MainGameScreen;
import com.rndmodgames.futtoboru.screens.MatchEngineDebugScreen;
import com.rndmodgames.futtoboru.screens.MenuScreen;
import com.rndmodgames.futtoboru.screens.NewGameOverviewScreen;
import com.rndmodgames.futtoboru.screens.NewGameSeasonScreen;
import com.rndmodgames.futtoboru.screens.NewGameSetupScreen;
import com.rndmodgames.futtoboru.screens.NewManagerScreen;
import com.rndmodgames.futtoboru.screens.SettingsScreen;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.PersonGenerator;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Futtuboru v1
 * 
 * @author Geomancer86
 */
public class Futtoboru extends Game {

    // LibGDX 
    private Game instance;
    public AssetManager manager;
    public PreferencesManager preferences;
    
    /**
     * Game Screens
     *  TODO: proper loading screen
     *  TODO: other missing screens (compendium, etc)
     *  TODO: credits screen
     */
    public static final int SPLASH_SCREEN               = 1;
    
    // Main Menu
    public static final int MENU_SCREEN                 = 2;
    public static final int SETTINGS_SCREEN             = 3;
    public static final int MATCH_ENGINE_DEBUG_SCREEN    = 4;
    public static final int MATCH_ENGINE_SCREEN          = 5;
    
    // New Game Setup
    public static final int NEW_GAME_SCREEN             = 10;
    public static final int NEW_MANAGER_SCREEN          = 11;
    public static final int NEW_GAME_SETUP_SCREEN       = 12;
    public static final int NEW_GAME_OVERVIEW_SCREEN    = 13;
    
    // Simulation
    public static final int GAME_SCREEN                 = 100;
    public static final int GAME_ADD_PLAYER_SCREEN      = 101; // not implemented, not used
    
    /**
     * GRAPHIC SETTINGS 
     *      - Will be loaded from User Preferences File
     * 
     * NOTE: default values, user settings will be saved
     * 
     * TODO: reset to defaults
     * TODO: restart game functionality (might not be needed and not be possible)
     */
    public static String RESOLUTION = UiScale.DEFAULT_RESOLUTION; // D18: 1280x720 minimum and default
    public static boolean FULLSCREEN = false;                 // Working
    
    /**
     * DEBUGGING SETTINGS
     * 
     * TODO: set from game settings screen
     */
    public static boolean DEBUG_MODE = Boolean.getBoolean("futtoboru.dev"); // -Dfuttoboru.dev=true (debug run scripts)

    /** dev-only tools (Match Engine Debug) show only in dev mode (U-04a) */
    public static boolean isDevMode() {
        return DEBUG_MODE;
    }
    public static int LIBGDX_LOG_LEVEL = Application.LOG_DEBUG;
    
    /**
     * Simulation/Save Game Instance
     */
    private SaveGame currentGame = null;
    private FuttoboruGameEngine gameEngine = null;
    private AuthorityManager authorityManager = null;
    private ScriptsManager scriptsManager = null;
    private PersonGenerator personGenerator = null;
    
    /**
     * Match Engine Configuration (temporary storage for screen transition)
     */
    private com.rndmodgames.futtoboru.match.engine.MatchConfiguration matchConfiguration = null;
    
    /**
     * Job System Managers (v1.0)
     */
    private ClubStaffManager clubStaffManager = null;
    private JobManager jobManager = null;

    /** Ground illustrations (U-11), loaded on first use and disposed with the game. */
    private final com.rndmodgames.futtoboru.ui.GroundArt groundArt = new com.rndmodgames.futtoboru.ui.GroundArt();
    
    /**
     * The one seeded random source of the game (P2-0). Game logic forks it ({@code random().fork("match:" + id)});
     * see {@link RandomSource}. Set {@value #SEED_PROPERTY} (e.g. {@code -Dfuttoboru.seed=1888}) to replay a run.
     */
    public static final String SEED_PROPERTY = "futtoboru.seed";
    private RandomSource random;
    /** S-01 (D25): the seed every new game uses (tests, replays), or null: each new game draws a fresh one. */
    private final Long forcedSeed;
    
    // main constructor: seed from -Dfuttoboru.seed (forced for every new game), else a fresh one per new game
    public Futtoboru() {
        this(Long.getLong(SEED_PROPERTY));
    }
    
    /** A game whose randomness is fully determined by {@code seed}; every new game of the session uses it again. */
    public Futtoboru(long seed) {
        this(Long.valueOf(seed));
    }
    
    private Futtoboru(Long forcedSeed) {
    
        // keep reference of the running instance
        instance = this;
        this.forcedSeed = forcedSeed;
        this.random = RandomSource.seeded(forcedSeed != null ? forcedSeed : RandomSource.newSeed());
    }
    
    /** @return true when the seed was forced ({@value #SEED_PROPERTY} or {@link #Futtoboru(long)}) */
    public boolean isSeedForced() {
        return forcedSeed != null;
    }
    
    /**
     * S-01 (D25): the root random source for a new game: the forced seed again, else a fresh seed (the new game's
     * save stores it, so a load replays it).
     *
     * @return the new game's seed
     */
    public long reseedForNewGame() {
        setSeed(forcedSeed != null ? forcedSeed : RandomSource.newSeed());
        Log.info(Log.SYSTEM_INIT, "Futtoboru", "New game seed " + getSeed() + (forcedSeed != null ? " (forced)" : ""));
        return getSeed();
    }
    
    /** The root random source; fork it, don't draw from it directly. */
    public RandomSource random() {
        return random;
    }
    
    /** The seed of the root random source (to be stored in the save game). */
    public long getSeed() {
        return random.getSeed();
    }
    
    /** Replaces the root random source, e.g. when a saved game is loaded: {@code setSeed(save.getSeed())}. */
    public void setSeed(long seed) {
        this.random = RandomSource.seeded(seed);
    }
    
    /** The game's root source, or the database loader's when there is no game (headless tools and tests). */
    public static RandomSource randomOf(Futtoboru game) {
        if (game != null && game.random() != null) {
            return game.random();
        }
        return DatabaseLoader.getRandom();
    }
    
    // create
    @Override
    public void create() {
        
        // Game initialization logging
        Log.info(Log.SYSTEM_INIT, "========================================");
        Log.info(Log.SYSTEM_INIT, "FUTTOBORU GAME CREATE() CALLED");
        Log.info(Log.SYSTEM_INIT, "========================================");
        
        // Create Asset Manager
        manager = new AssetManager();
        
        /**
         * Create User Preferences Manager
         */
        preferences = new PreferencesManager();
        
        /**
         * Restore Prefered Locale from User Properties
         */
        Locale locale = preferences.getSavedLocale();
        manager.load("mods/language/BaseBundle", I18NBundle.class, new I18NBundleLoader.I18NBundleParameter(locale));
        
        /**
         * Check for Saved Settings
         */
        FULLSCREEN = preferences.getFullscreen();
        
        // at least 1280x720; a missing or old sub-720p value becomes the default (D18)
        RESOLUTION = preferences.getResolution();

        // U-03a: Auto (default), 100% or 200%; every UI stage reads it through UiScale
        UiScale.setSetting(preferences.getUiScale());

        // The desktop launcher already opened the window from the same preferences; this only changes the mode
        // when it differs (no startup double resize)
        setScreen();
        
        // Load VisUI NOTE: this fails during unit tests because "com.badlogic.gdx.Gdx.gl" is null
        if (com.badlogic.gdx.Gdx.gl != null) {
            VisUI.load(Gdx.files.internal("skin/tixel.json"));
        }
        com.rndmodgames.futtoboru.ui.UiTheme.loadDefault(); // U-01a: theme tokens (no GL needed); fails fast if ui/theme.json is invalid
        
        // 
        manager.finishLoading();
        
        /**
         * TODO: load debug setting from saved user preferences / settings screen
         */
        Gdx.app.setLogLevel(LIBGDX_LOG_LEVEL);
        
        // 
        LanguageModLoader.setBundle(manager.get("mods/language/BaseBundle", I18NBundle.class));
        
        /**
         * Preload the Database
         */
        DatabaseLoader.setRandom(random.fork("database"));
        DatabaseLoader.getInstance();

        // Script Manager, Person Generator, Authority Manager and Game Engine
        createEngine();

        // Show Main Menu 
        // NOTE: this fails during unit tests because "com.badlogic.gdx.Gdx.gl" is null
        if (com.badlogic.gdx.Gdx.gl != null) {
            changeScreen(MENU_SCREEN);
        }
    }
    
    /** The engine managers of a game: none of them keeps anything of a previous game. */
    private void createEngine() {
        this.scriptsManager = new ScriptsManager(this);
        this.setPersonGenerator(new PersonGenerator(this));
        this.authorityManager = new AuthorityManager(this);
        this.setGameEngine(new FuttoboruGameEngine(this, scriptsManager, authorityManager));
        this.jobManager = null;
        this.clubStaffManager = null;
    }

    /**
     * Before the New Game screens (P2-10): a game played or loaded earlier in this session changed the database's
     * clubs, people, competitions, editions, tables and balances (the game plays with those objects). This drops the
     * current game, rebuilds every database object fresh from the game data already read (same runtime ids: data
     * records keep their {@code ids.json} number and derived ids are handed out the same way on every build) and
     * makes new engine managers, so the new game starts from the data as it is in gamedata. S-01 (D25): the new
     * game gets a fresh seed unless one is forced ({@link #reseedForNewGame()}).
     */
    public void prepareNewGame() {
        Log.info(Log.SYSTEM_INIT, "Futtoboru", "New game: rebuilding the database from game data");
        awaitAutosave();
        this.currentGame = null;
        reseedForNewGame();
        DatabaseLoader.setRandom(random.fork("database"));
        DatabaseLoader.rebuild();
        createEngine();
    }

    /**
     * Manage the Active Screen
     *
     * NOTE: each screen handles if dispose() itself at the end
     * 
     * @param screen
     */
    public void changeScreen(int screen) {
        
        switch(screen) {
        
        // TODO: restore splash screen before the main menu screen
//        case SPLASH_SCREEN:
//            this.setScreen(new SplashScreen(this));
//            break;
            
        case MENU_SCREEN:
            awaitAutosave(); // Quit to menu: the running autosave of the game left is finished first (U-06)
            this.setScreen(new MenuScreen(this));
            break;
            
        case NEW_GAME_SCREEN:
            this.setScreen(new NewGameSeasonScreen(this));
            break;
            
        case NEW_MANAGER_SCREEN:
            this.setScreen(new NewManagerScreen(this));
            break;
            
        case GAME_ADD_PLAYER_SCREEN:
            
            break;
            
        case GAME_SCREEN:
            Log.info(Log.SYSTEM_INIT, "Futtoboru", "Changing to GAME_SCREEN...");
            Log.info(Log.SYSTEM_INIT, "Futtoboru", "Current game: " + (currentGame != null ? "exists" : "NULL"));
            Log.info(Log.SYSTEM_INIT, "Futtoboru", "Game engine: " + (gameEngine != null ? "exists" : "NULL"));
            try {
                this.setScreen(new MainGameScreen(this));
                Log.info(Log.SYSTEM_INIT, "Futtoboru", "GAME_SCREEN created successfully");
            } catch (Exception e) {
                Log.error(Log.SYSTEM_INIT, "Futtoboru", "ERROR creating GAME_SCREEN!", e);
                Log.error(Log.SYSTEM_INIT, "Futtoboru", "Exception: " + e.getClass().getName());
                Log.error(Log.SYSTEM_INIT, "Futtoboru", "Message: " + e.getMessage());
                Log.error(Log.SYSTEM_INIT, e);
                // Don't rethrow - keep the current screen
                Log.error(Log.SYSTEM_INIT, "Futtoboru", "Failed to create GAME_SCREEN. Staying on current screen.");
            }
            break;
            
        case SETTINGS_SCREEN:
            this.setScreen(new SettingsScreen(this));
            break;
            
        case MATCH_ENGINE_DEBUG_SCREEN:
            this.setScreen(new MatchEngineDebugScreen(this));
            break;
            
        case MATCH_ENGINE_SCREEN:
            com.rndmodgames.futtoboru.match.engine.MatchEngineScreen matchScreen = 
                new com.rndmodgames.futtoboru.match.engine.MatchEngineScreen(this, matchConfiguration);
            this.setScreen(matchScreen);
            matchConfiguration = null; // Clear after use
            break;
            
        default:
            // TODO: handle unknown screen
            break;
        }
    }
    
    /**
     * Manages the Active Screen when you need to pass parameters between them
     *  
     *  - This will be used to switch back and forth on some screens such as NEW GAME and NEW GAME OVERVIEW
     *      while keeping track of User Selections (Countries, Leagues, Options, etc).
     */
    public void changeScreen(int screen, Season startingSeason, List<Country> selectedCountries) {
        
        Log.info(Log.SYSTEM_INIT, "Starting Season: " + startingSeason);
        
        switch (screen) {
        case NEW_GAME_SETUP_SCREEN:
            this.setScreen(new NewGameSetupScreen(this, startingSeason, selectedCountries));
            break;
            
        case NEW_GAME_OVERVIEW_SCREEN:
            this.setScreen(new NewGameOverviewScreen(this, startingSeason, selectedCountries));
            break;
        }
    }
    
    /**
     * Utility method used to update Screen Resolution
     */
    public void updateScreenResolution(String resolution) {

        preferences.updateScreenResolution(resolution);
    }
    
    /**
     * U-03a: saves the UI scale ({@link UiScale#AUTO}, 1 or 2) and applies it to the current screen's stage at once;
     * screens opened later build their stages with it.
     */
    public void updateUiScale(float scale) {

        UiScale.setSetting(scale);
        preferences.updateUiScale(UiScale.getSetting());
        Log.info(Log.SYSTEM_CONFIG, "UI scale " + (UiScale.isAuto(UiScale.getSetting()) ? "Auto" : UiScale.formatPercent(UiScale.getSetting()))
                + " -> " + UiScale.formatPercent(UiScale.currentScale()));

        if (screen != null && Gdx.graphics != null) {
            screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    /**
     * U-12: switches the game language ({@code en_US}, {@code en_UK}, {@code sp_SP}, {@code sp_LA}): saves it and loads
     * its bundle at once, so labels, dates and numbers of every screen built from now on use it (the caller rebuilds
     * the screen it is on). Letters already in the inbox keep the language they were written in.
     */
    public void updateLanguage(String languageId) {

        Locale locale = new Locale(languageId);
        if (preferences != null) {
            preferences.saveLocale(locale);
        }
        LanguageModLoader.setBundle(I18NBundle.createBundle(Gdx.files.internal("mods/language/BaseBundle"), locale));
        Log.info(Log.SYSTEM_CONFIG, "language " + languageId + " (dates and numbers: " + com.rndmodgames.localization.GameFormat.locale() + ")");
    }

    /**
     * Utility method used to toggle Fullscreen and Windowed Mode
     */
    public void toggleFullscreen() {

        FULLSCREEN = !FULLSCREEN;
        
        setScreen();
    }
    
    /**
     * Utility method to set Fullscreen or Windowed depending on settings
     */
    public void setScreen() {
        
        if (FULLSCREEN){

            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        } else {

            // The selected resolution (at least 1280x720)
            int[] size = UiScale.windowSize(Futtoboru.RESOLUTION);

            // Resize on this Screen updates the Viewport (for event coordinates)
            //  and is called automatically; skipped when the window already has this size
            if (Gdx.graphics.isFullscreen() || Gdx.graphics.getWidth() != size[0] || Gdx.graphics.getHeight() != size[1]) {
                Gdx.graphics.setWindowedMode(size[0], size[1]);
            }
        }
        
        preferences.updateFullScreen(FULLSCREEN);
    }
    
    /**
     * Utility method used to change windows screen or fullscreen modes
     * 
     * NOTE: automatically called by parents
     */
    public void resize(int width, int height) {
        
        //
        this.screen.resize(width, height);
    }
    
    /**
     * Dispose elements after closing
     */
    @Override
    public void dispose () {

        // window close and Quit to desktop (Gdx.app.exit) both end here: never exit mid-autosave (U-06)
        awaitAutosave();
        groundArt.dispose();
        VisUI.dispose();
    }

    /**
     * Waits (at most {@link SaveSlots#QUIT_WAIT_MILLIS}) for an autosave that is still being written (U-06): called
     * before quitting to the menu or the desktop, closing the window and starting a new game.
     *
     * @return false when the write was still running after the wait
     */
    public boolean awaitAutosave() {
        return gameEngine == null || SaveSlots.awaitAutosave(gameEngine.getAutosave(), SaveSlots.QUIT_WAIT_MILLIS);
    }

    /**
     *
     */
    public SaveGame getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(SaveGame currentGame) {
        this.currentGame = currentGame;
        // the game keeps the seed its database and play are drawn from, so a save can restore it (P2-5)
        if (currentGame != null && currentGame.getSeed() == null) {
            currentGame.setSeed(getSeed());
        }
    }

    /**
     * Makes a loaded game the current one (P2-5): the job managers are rebuilt so none keeps the previous game.
     * The database lookups were already bound to the loaded game by the load.
     */
    public void startLoadedGame(SaveGame loaded) {
        if (loaded.getSeed() != null) {
            setSeed(loaded.getSeed());
            DatabaseLoader.setRandom(random.fork("database"));
        }
        setCurrentGame(loaded);
        this.jobManager = null;
        this.clubStaffManager = null;
        initializeJobSystem();
    }

    public ScriptsManager getScriptsManager() {
        return scriptsManager;
    }

    public void setScriptsManager(ScriptsManager scriptsManager) {
        this.scriptsManager = scriptsManager;
    }

    public PersonGenerator getPersonGenerator() {
        return personGenerator;
    }

    public void setPersonGenerator(PersonGenerator personGenerator) {
        this.personGenerator = personGenerator;
    }

    public FuttoboruGameEngine getGameEngine() {
        return gameEngine;
    }
    
    /**
     * Set match configuration for match engine screen
     */
    public void setMatchConfiguration(com.rndmodgames.futtoboru.match.engine.MatchConfiguration config) {
        this.matchConfiguration = config;
    }
    
    /**
     * Get match configuration (clears after retrieval)
     */
    public com.rndmodgames.futtoboru.match.engine.MatchConfiguration getMatchConfiguration() {
        return matchConfiguration;
    }

    public void setGameEngine(FuttoboruGameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }
    
    /**
     * Initialize Job System (v1.0)
     * 
     * Called when a game is loaded or started to initialize job system managers.
     * This must be called after currentGame is set.
     */
    public void initializeJobSystem() {
        if (currentGame == null) {
            Log.error(Log.SYSTEM_INIT, "Futtoboru", "Cannot initialize job system: currentGame is null");
            return;
        }
        
        // Initialize Club Staff Manager
        if (clubStaffManager == null) {
            clubStaffManager = new ClubStaffManager(this);
        }
        
        // Initialize Job Manager
        if (jobManager == null) {
            jobManager = new JobManager(this, clubStaffManager);
        }
        
        Log.info(Log.SYSTEM_INIT, "Futtoboru", "Job system initialized");
    }
    
    /**
     * Get Club Staff Manager
     */
    public ClubStaffManager getClubStaffManager() {
        return clubStaffManager;
    }
    
    /**
     * Get Job Manager
     */
    /** Ground pictures (U-11): {@code art/grounds/<id>.png}, Nearest filter, whole-number scale. */
    public com.rndmodgames.futtoboru.ui.GroundArt getGroundArt() {
        return groundArt;
    }

    public JobManager getJobManager() {
        return jobManager;
    }
}