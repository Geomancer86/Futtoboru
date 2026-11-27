package com.rndmodgames.futtoboru.game;

import java.util.ArrayList;
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
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.screens.MainGameScreen;
import com.rndmodgames.futtoboru.screens.MatchEngineDebugScreen;
import com.rndmodgames.futtoboru.screens.MenuScreen;
import com.rndmodgames.futtoboru.screens.NewGameOverviewScreen;
import com.rndmodgames.futtoboru.screens.NewGameSeasonScreen;
import com.rndmodgames.futtoboru.screens.NewGameSetupScreen;
import com.rndmodgames.futtoboru.screens.NewManagerScreen;
import com.rndmodgames.futtoboru.screens.SettingsScreen;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.HistoricalMode;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.DebugLogManager;
import com.rndmodgames.futtoboru.system.generators.PersonGenerator;
import com.rndmodgames.localization.LanguageModLoader;

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
    public static String RESOLUTION = "640x480";              // Working
    public static boolean FULLSCREEN = false;                 // Working
    public static float UI_SCALE = 0f;                        // 0 = Auto (UiScale), 1.0 = 100% UI zoom
    /** the OS scale of the monitor (2.0 at Windows 200%), set by the desktop launcher; 1 when unknown */
    public static float SYSTEM_UI_SCALE = 1f;
    
    private int currentScreenId = MENU_SCREEN;
    
    /**
     * DEBUGGING SETTINGS
     * 
     * TODO: set from game settings screen
     */
    public static boolean DEBUG_MODE = false; // default to false
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
    
    // main constructor
    public Futtoboru() {
    
        // keep reference of the running instance
        instance = this;
    }
    
    // create
    @Override
    public void create() {
        DebugLogManager logManager = DebugLogManager.getInstance();
        
        // Game initialization logging
        logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "========================================");
        logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "FUTTOBORU GAME CREATE() CALLED");
        logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "========================================");
        
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
        
        // avoid crashing on empty or missing preferences file
        if (preferences.getResolution() != null && !preferences.getResolution().equals("")) {
            
            RESOLUTION = preferences.getResolution();
        }

        preferences.migrateUiScalePreferences();

        UI_SCALE = preferences.getUiScale();
        
        // This will take care of Setting the Right Fullscreen/Windowed Resolution mode
        setScreen();
        
        // Load VisUI NOTE: this fails during unit tests because "com.badlogic.gdx.Gdx.gl" is null
        loadVisUiSkin();
        
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
        DatabaseLoader.getInstance();

        // Initialize the Script Manager
        // NOTE: null-guarded (not unconditional) so that a caller who has already wired up this
        // exact triple before create() runs (see HeadlessGdxTestSupport, used by every headless
        // integration test) doesn't get a second, independent set silently built on top of it here.
        // Before this guard existed, that was happening: create() unconditionally overwrote
        // scriptsManager/authorityManager/gameEngine with brand-new instances, orphaning whatever
        // the caller had already set, which meant two independent MessageManager instances (each
        // with their own message-id counter) both wrote into the same SaveGame message lists --
        // colliding ids caused deliverMessage() to silently drop a real mandatory CUP_DRAW message
        // forever (see A-13 in docs/registries/blockers-resolved.yaml). In normal desktop play these
        // fields are always null here (create() runs exactly once), so behavior is unchanged.
        if (this.scriptsManager == null) {
            this.scriptsManager = new ScriptsManager(this);
        }
        
        // Initialize the Person Generator
        this.setPersonGenerator(new PersonGenerator(this));
        
        // Initialize the Authority Manager
        if (this.authorityManager == null) {
            this.authorityManager = new AuthorityManager(this);
        }
        
        // Initialize the Game Engine
        if (this.gameEngine == null) {
            this.setGameEngine(new FuttoboruGameEngine(this, scriptsManager, authorityManager));
        }
        
        // Show Main Menu 
        // NOTE: this fails during unit tests because "com.badlogic.gdx.Gdx.gl" is null
        if (com.badlogic.gdx.Gdx.gl != null) {
            changeScreen(MENU_SCREEN);
        }

        // Manual E2E smoke-testing hook: launch with
        // -Dfuttoboru.debug.startScreen=NEW_GAME_SCREEN (or NEW_MANAGER_SCREEN / SETTINGS_SCREEN) to
        // jump straight to a pre-game screen on boot, so real crashes in screen construction (which
        // require a live GL context and are otherwise only reachable by actually clicking through the
        // real window) can be caught headlessly-by-a-human/CI without manual clicking. Added 2026-07-19
        // after a launch-blocking crash was found only by a human tester, not by any automated test.
        String debugStartScreen = System.getProperty("futtoboru.debug.startScreen");
        if (debugStartScreen != null && !debugStartScreen.isBlank()) {
            System.out.println("DEBUG: jumping straight to screen '" + debugStartScreen + "' on boot");
            try {
                switch (debugStartScreen.trim()) {
                    case "NEW_GAME_SCREEN":
                        changeScreen(NEW_GAME_SCREEN);
                        break;
                    case "NEW_MANAGER_SCREEN":
                        changeScreen(NEW_MANAGER_SCREEN);
                        break;
                    case "SETTINGS_SCREEN":
                        changeScreen(SETTINGS_SCREEN);
                        break;
                    case "NEW_GAME_FULL_FLOW":
                        runFullNewGameFlowForTesting();
                        break;
                    default:
                        System.err.println("DEBUG: unknown futtoboru.debug.startScreen value: " + debugStartScreen);
                }
                System.out.println("DEBUG: reached screen '" + debugStartScreen + "' without throwing");
            } catch (RuntimeException e) {
                System.err.println("DEBUG: crash while jumping to screen '" + debugStartScreen + "'");
                e.printStackTrace();
                throw e;
            }
            if ("true".equals(System.getProperty("futtoboru.debug.exitAfterStartScreen"))) {
                System.out.println("DEBUG: exiting after reaching debug start screen");
                Gdx.app.exit();
            }
        }
    }
    
    /**
     * Manage the Active Screen
     * 
     * NOTE: each screen handles if dispose() itself at the end
     * 
     * @param screen
     */
    public void changeScreen(int screen) {
        
        currentScreenId = screen;
        
        switch(screen) {
        
        // TODO: restore splash screen before the main menu screen
//        case SPLASH_SCREEN:
//            this.setScreen(new SplashScreen(this));
//            break;
            
        case MENU_SCREEN:
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
            DebugLogManager logManager = DebugLogManager.getInstance();
            logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Changing to GAME_SCREEN...");
            logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Current game: " + (currentGame != null ? "exists" : "NULL"));
            logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Game engine: " + (gameEngine != null ? "exists" : "NULL"));
            try {
                this.setScreen(new MainGameScreen(this));
                logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "GAME_SCREEN created successfully");
            } catch (Exception e) {
                logManager.error(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "ERROR creating GAME_SCREEN!", e);
                logManager.error(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Exception: " + e.getClass().getName());
                logManager.error(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Message: " + e.getMessage());
                e.printStackTrace();
                // Don't rethrow - keep the current screen
                logManager.error(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Failed to create GAME_SCREEN. Staying on current screen.");
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
        
        currentScreenId = screen;
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Starting Season: " + startingSeason);
        
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
     * Manages the Active Screen when only a Season needs to survive the trip (New Game Step 1
     * &lt;-&gt; Create Manager). Added 2026-07-19 (pre-game menus UX audit): previously
     * {@code Create Manager} discarded the player's season choice, forcing a re-pick on return.
     */
    public void changeScreen(int screen, Season startingSeason) {

        currentScreenId = screen;

        switch (screen) {
        case NEW_GAME_SCREEN:
            this.setScreen(new NewGameSeasonScreen(this, startingSeason));
            break;

        case NEW_MANAGER_SCREEN:
            this.setScreen(new NewManagerScreen(this, startingSeason));
            break;

        default:
            changeScreen(screen);
            break;
        }
    }

    /**
     * Test-only: drives the entire New Game wizard headlessly (season -> manager -> countries ->
     * Start Game) using the first available season/country and a throwaway manager, exactly mirroring
     * what a human clicking through every screen would trigger -- including {@code NEW_GAME_SETUP_SCREEN}
     * (Step 2, choose countries), which the real Start button always routes through and which an
     * earlier version of this hook wrongly skipped straight past, missing a real crash there (A-12) --
     * so a crash anywhere in the chain shows up in a scripted run instead of only being found by a
     * human tester. Invoked via {@code -Dfuttoboru.debug.startScreen=NEW_GAME_FULL_FLOW}. Added
     * 2026-07-19, fixed 2026-07-19 to actually go through Step 2 instead of skipping it.
     */
    private void runFullNewGameFlowForTesting() {
        List<Season> allSeasons = DatabaseLoader.getInstance().getSeasons();
        if (allSeasons == null || allSeasons.isEmpty()) {
            throw new IllegalStateException("DEBUG NEW_GAME_FULL_FLOW: no seasons loaded from database");
        }
        Season season = allSeasons.get(0);

        List<Country> allCountries = season.getCountries();
        if (allCountries == null || allCountries.isEmpty()) {
            throw new IllegalStateException("DEBUG NEW_GAME_FULL_FLOW: season '" + season.getName() + "' has no countries");
        }

        Person owner = new Person();
        owner.setName("Debug");
        owner.setLastname("Tester");
        owner.setBirthDate(java.time.LocalDateTime.of(1850, 1, 1, 0, 0));
        owner.setCountry(allCountries.get(0));

        SaveGame currentGame = new SaveGame();
        setCurrentGame(currentGame);
        currentGame.setOwner(owner);

        System.out.println("DEBUG NEW_GAME_FULL_FLOW: season=" + season.getName() + ", countries=" + allCountries.size());

        // Step 1 -> Step 2, exactly like NewGameSeasonScreen's Start button (empty list -> Step 2
        // defaults to every active country selected).
        changeScreen(NEW_GAME_SETUP_SCREEN, season, new ArrayList<>());
        if (!(getScreen() instanceof NewGameSetupScreen)) {
            throw new IllegalStateException("DEBUG NEW_GAME_FULL_FLOW: expected NewGameSetupScreen, got "
                    + (getScreen() != null ? getScreen().getClass().getName() : "null"));
        }
        System.out.println("DEBUG NEW_GAME_FULL_FLOW: setup screen constructed OK, triggering Next...");

        // Step 2 -> Step 3, exactly like the real Next button.
        ((NewGameSetupScreen) getScreen()).triggerNextForTesting();

        if (!(getScreen() instanceof NewGameOverviewScreen)) {
            throw new IllegalStateException("DEBUG NEW_GAME_FULL_FLOW: expected NewGameOverviewScreen, got "
                    + (getScreen() != null ? getScreen().getClass().getName() : "null"));
        }

        String debugProfessionId = System.getProperty("futtoboru.debug.professionId");
        if (debugProfessionId != null && !debugProfessionId.isBlank()) {
            String debugClubId = System.getProperty("futtoboru.debug.clubId");
            System.out.println("DEBUG NEW_GAME_FULL_FLOW: selecting profession=" + debugProfessionId
                    + ", club=" + debugClubId);
            ((NewGameOverviewScreen) getScreen()).selectProfessionAndClubForTesting(
                    Long.parseLong(debugProfessionId),
                    debugClubId != null && !debugClubId.isBlank() ? Long.parseLong(debugClubId) : null);
        }

        System.out.println("DEBUG NEW_GAME_FULL_FLOW: overview screen constructed OK, triggering Start Game...");
        // runStartGame() deliberately swallows its own exceptions and shows an in-game ErrorDialog
        // instead of propagating (P0 fix from the earlier menu audit) -- so success/failure here can
        // only be told apart by whether the screen actually advanced past the overview screen.
        ((NewGameOverviewScreen) getScreen()).triggerStartGameForTesting();

        if (getScreen() instanceof NewGameOverviewScreen) {
            throw new IllegalStateException(
                    "DEBUG NEW_GAME_FULL_FLOW: Start Game failed -- screen did not advance past "
                    + "NewGameOverviewScreen (see CRITICAL ERROR DURING GAME STARTUP! in the log above "
                    + "for the real exception; runStartGame() catches it and shows an ErrorDialog instead "
                    + "of propagating)");
        }
        System.out.println("DEBUG NEW_GAME_FULL_FLOW: SUCCESS, current screen is now "
                + getScreen().getClass().getSimpleName());
    }

    /**
     * Routes back to the Season screen with a just-created manager wired directly in memory as the
     * pre-selected manager — avoids the save-to-file-then-reload-by-filename round trip that
     * {@code NewGameSeasonScreen}'s saved-managers dropdown otherwise requires. Added 2026-07-19
     * (pre-game menus UX audit, Create Manager unit).
     */
    public void changeScreen(int screen, Season startingSeason, com.rndmodgames.futtoboru.data.Person newlyCreatedManager) {

        currentScreenId = screen;

        if (screen == NEW_GAME_SCREEN) {
            this.setScreen(new NewGameSeasonScreen(this, startingSeason, newlyCreatedManager));
        } else {
            changeScreen(screen, startingSeason);
        }
    }
    
    /**
     * Utility method used to update Screen Resolution
     */
    public void updateScreenResolution(String resolution) {

        preferences.updateScreenResolution(resolution);
    }

    /**
     * Updates UI zoom factor, persists, reloads VisUI skin, and rebuilds the active screen.
     */
    public void updateUiScale(float scale) {
        if (scale <= 0f) {
            scale = 0f; // Auto
        }
        UI_SCALE = scale;
        preferences.updateUiScale(scale);
        loadVisUiSkin();
        changeScreen(currentScreenId);
    }

    private void loadVisUiSkin() {
        if (Gdx.gl == null) {
            return;
        }
        if (VisUI.isLoaded()) {
            VisUI.dispose();
        }
        VisUI.load(Gdx.files.internal("skin/tixel.json"));
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
            
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {

            // The selected resolution
            String res = Futtoboru.RESOLUTION;
            String [] splitRes = res.split("x"); 
    
            // Resize on this Screen updates the Viewport (for event coordinates)
            //  and is called automatically
            Gdx.graphics.setWindowedMode(Integer.valueOf(splitRes[0]), Integer.valueOf(splitRes[1]));
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
     
        VisUI.dispose();
    }

    /**
     * 
     */
    public SaveGame getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(SaveGame currentGame) {
        this.currentGame = currentGame;
    }

    /**
     * Load or resume a save: apply defaults, bind job system, ready for {@link #changeScreen(int)} to game UI.
     * Fixes C-3 / H-9 (load skips job init).
     */
    public void activateSaveGame(SaveGame saveGame) {
        if (saveGame == null) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru",
                "activateSaveGame: saveGame is null");
            throw new IllegalArgumentException("saveGame cannot be null");
        }
        if (saveGame.getHistoricalMode() == null) {
            saveGame.setHistoricalMode(HistoricalMode.SANDBOX_FIRST);
        }
        setCurrentGame(saveGame);
        clubStaffManager = null;
        jobManager = null;
        initializeJobSystem();
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru",
            "Save activated — historicalMode=" + saveGame.getHistoricalMode()
                + ", date=" + saveGame.getGameDate());
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
        DebugLogManager logManager = DebugLogManager.getInstance();
        if (currentGame == null) {
            logManager.error(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Cannot initialize job system: currentGame is null");
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
        
        logManager.log(DebugLogManager.CATEGORY_SYSTEM_INIT, "Futtoboru", "Job system initialized");
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
    public JobManager getJobManager() {
        return jobManager;
    }
}