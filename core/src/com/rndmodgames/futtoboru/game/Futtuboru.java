package com.rndmodgames.futtoboru.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.VisUI;
import com.rndmodgames.PreferencesManager;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.screens.MainGameScreen;
import com.rndmodgames.futtoboru.screens.MenuScreen;
import com.rndmodgames.futtoboru.screens.NewGameOverviewScreen;
import com.rndmodgames.futtoboru.screens.NewGameScreen;
import com.rndmodgames.futtoboru.screens.NewGameSetupScreen;
import com.rndmodgames.futtoboru.screens.NewManagerScreen;
import com.rndmodgames.futtoboru.screens.SettingsScreen;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Rock-Bottom Futtuboru Challenge
 * 
 * @author WORKSTATION
 *
 */
public class Futtuboru extends Game {

    // LibGDX 
    private Game instance;
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
    
    // New Game Setup
    public static final int NEW_GAME_SCREEN             = 4;
    public static final int NEW_MANAGER_SCREEN          = 5;
    public static final int NEW_GAME_SETUP_SCREEN       = 6;
    public static final int NEW_GAME_OVERVIEW_SCREEN    = 7;
    
    // Simulation
    public static final int GAME_SCREEN                 = 100;
    public static final int GAME_ADD_PLAYER_SCREEN      = 101;
    
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
    
    /**
     * Simulation/Save Game Instance
     */
    private SaveGame currentGame = null;
    
    // main constructor
    public Futtuboru() {
    
        // keep reference of the running instance
        instance = this;
    }
    
    // create
    @Override
    public void create() {
        
        /**
         * Create User Preferences Manager
         */
        preferences = new PreferencesManager();
        
        /**
         * Check for Saved Settings
         */

        FULLSCREEN = preferences.getFullscreen();
        
        // avoid crashing on empty or missing preferences file
        if (preferences.getResolution() != null && !preferences.getResolution().equals("")) {
            
            RESOLUTION = preferences.getResolution();
        }
        
        // This will take care of Setting the Right Fullscreen/Windowed Resolution mode
        setScreen();
        
        // Load VisUI
        VisUI.load(Gdx.files.internal("skin/tixel.json"));
        
        // Preload the Database
        DatabaseLoader.getInstance();
        
        // Show Main Menu
        changeScreen(MENU_SCREEN);
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
        
//        case SPLASH_SCREEN:
//            this.setScreen(new SplashScreen(this));
//            break;
            
        case MENU_SCREEN:
            this.setScreen(new MenuScreen(this));
            break;
            
        case NEW_GAME_SCREEN:
            this.setScreen(new NewGameScreen(this));
            break;
            
        case NEW_MANAGER_SCREEN:
            this.setScreen(new NewManagerScreen(this));
            break;
            
        case NEW_GAME_SETUP_SCREEN:
            this.setScreen(new NewGameSetupScreen(this, new ArrayList<>()));
            break;
            
        case GAME_ADD_PLAYER_SCREEN:
            
            break;
            
        case GAME_SCREEN:
            this.setScreen(new MainGameScreen(this));
            break;
            
        case SETTINGS_SCREEN:
            this.setScreen(new SettingsScreen(this));
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
    public void changeScreen(int screen, List<Country> selectedCountries) {
        
        switch (screen) {
        case NEW_GAME_SETUP_SCREEN:
            this.setScreen(new NewGameSetupScreen(this, selectedCountries));
            break;
            
        case NEW_GAME_OVERVIEW_SCREEN:
            this.setScreen(new NewGameOverviewScreen(this, selectedCountries));
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
            String res = Futtuboru.RESOLUTION;
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
}