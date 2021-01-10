package com.rndmodgames;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.rndmodgames.darkblade.screens.tables.SoundSettingsTable;

/**
 * Preferences Manager v1
 * 
 * Graphic Settings:
 * 		- FULLSCREEN 					[DONE]
 * 		- RESOLUTION 					[DONE]
 * 		- DAY NIGHT CYCLE			 	[DONE]
 * 		- SHADOWS 						[DONE-TODO MIGRATE]
 * 		- DAMAGE COUNTERS				[WIP]
 * 		- DEBUG PHYSICS 				[DONE-TODO MIGRATE]
 * 		- DEBUG PATH FINDING 			[DONE-TODO MIGRATE]
 * 		- DEBUG SIGHT RENDERING 		[DONE]
 * 		- DEBUG TABLE RENDERING 		[DONE-TODO MIGRATE]
 * 
 * @author Geomancer86
 */
public class PreferencesManager {

	public Preferences prefs;
	
	// This is the Preferences File Name on User Filesystem 
	public static final String PREFERENCES_NAME = "Futtuboru Preferences";
	
	// GRAPHIC SETTINGS
	public static final String SETTINGS_FULLSCREEN_PREF = "settings.fullscreen";
	public static final String SETTINGS_RESOLUTION_PREF = "settings.resolution";
	public static final String SETTINGS_DAY_NIGHT_RENDERING_PREF = "settings.day_night_cycle_rendering";
	public static final String SETTINGS_DAMAGE_COUNTERS_RENDERING_PREF = "settings.damage_counters_rendering";
	public static final String SETTINGS_DEBUG_SIGHT_PREF = "settings.debug_sight_rendering";
	
	// SOUND SETTINGS
	public static final String SETTINGS_MASTER_SOUND_LEVEL_PREF = "settings.master_sound_level";
	
	// MODS SETTINGS
	public static final String SETTINGS_LANGUAGE_BUNDLE_PREF = "settings.language_bundle";
	
	// TOGGLE SETTINGS
	public static final String ENABLED = "enabled";
	public static final String DISABLED = "disabled";
	
	// DEFAULT VALUES
	public static String DEFAULT_RESOLUTION = "640x480";
	public static String DEFAULT_LOCALE = "en_US";
	
	public PreferencesManager() {
		
		prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
		
		// create default preferences file to avoid null pointers
		if (prefs == null) {
		    
		    System.out.println("PREFS IS NULL");
		}
	}
	
	/**
	 * Toggles the Damage Counters Rendering Preference
	 */
	public void toggleDamageCountersRendering(boolean damageCounterRender) {
		
		updateUserPreference(SETTINGS_DAMAGE_COUNTERS_RENDERING_PREF, damageCounterRender ? ENABLED : DISABLED);
	}
	
	/**
	 * Returns the Damage Counters Rendering Preference
	 */
	public boolean getDamageCountersRendering() {
		
		boolean damageCountersRendering = false;
		
		if (prefs.getString(SETTINGS_DAMAGE_COUNTERS_RENDERING_PREF) != null) {
		
			if (prefs.getString(SETTINGS_DAMAGE_COUNTERS_RENDERING_PREF).equals(ENABLED)) {
				damageCountersRendering = true;
			} else if (prefs.getString(SETTINGS_DAMAGE_COUNTERS_RENDERING_PREF).equals(DISABLED)) {
				damageCountersRendering = false;
			}
		} else {
			// Sight Debug Rendering Disabled By Default
			damageCountersRendering = false;
			prefs.putString(SETTINGS_DAMAGE_COUNTERS_RENDERING_PREF, DISABLED);
		}
		
		return damageCountersRendering;
	}
	
	/**
	 * Toggles the DEBUG_SIGHT preference
	 */
	public void toggleDebugSight(boolean debugSight) {
		
		updateUserPreference(SETTINGS_DEBUG_SIGHT_PREF, debugSight ? ENABLED : DISABLED);
	}
	
	/**
	 * Returns the DEBUG_SIGHT preference
	 */
	public boolean getDebugSight() {
		
		boolean debugSight = false;
		
		if (prefs.getString(SETTINGS_DEBUG_SIGHT_PREF) != null) {
		
			if (prefs.getString(SETTINGS_DEBUG_SIGHT_PREF).equals(ENABLED)) {
				debugSight = true;
			} else if (prefs.getString(SETTINGS_DEBUG_SIGHT_PREF).equals(DISABLED)) {
				debugSight = false;
			}
		} else {
			// Sight Debug Rendering Disabled By Default
			debugSight = false;
			prefs.putString(SETTINGS_DEBUG_SIGHT_PREF, DISABLED);
		}
		
		return debugSight;
	}
	
	/**
	 * Returns the DAY_NIGHT_RENDERING preference
	 */
	public boolean getDayNightRendering() {
		
		boolean dayNightRendering = false;
		
		if (prefs.getString(SETTINGS_DAY_NIGHT_RENDERING_PREF) != null) {
		
			if (prefs.getString(SETTINGS_DAY_NIGHT_RENDERING_PREF).equals(ENABLED)) {
				dayNightRendering = true;
			} else if (prefs.getString(SETTINGS_DAY_NIGHT_RENDERING_PREF).equals(DISABLED)) {
				dayNightRendering = false;
			}
		} else {
			// Day-Night Rendering Disabled By Default
			dayNightRendering = false;
			prefs.putString(SETTINGS_DAY_NIGHT_RENDERING_PREF, DISABLED);
		}
		
		return dayNightRendering;
	}
	
	/**
	 * Returns the RESOLUTION preference
	 */
	public String getResolution() {

		String resolution = null;
		
		if (prefs.getString(SETTINGS_RESOLUTION_PREF) != null) {
			resolution = prefs.getString(SETTINGS_RESOLUTION_PREF);
		} else {
			prefs.putString(SETTINGS_RESOLUTION_PREF, DEFAULT_RESOLUTION);
		}
		
		return resolution;
	}
	
	/**
	 * Toggles the FULLSCREEN user preference
	 */
	public void updateFullScreen(boolean fullscreen) {
		
		updateUserPreference(SETTINGS_FULLSCREEN_PREF, fullscreen ? ENABLED : DISABLED);
	}
	
	/**
	 * Returns the FULLSCREEN preference
	 */
	public boolean getFullscreen() {
		
		boolean fullscreen = false;

		if (prefs.getString(SETTINGS_FULLSCREEN_PREF) != null) {
		    
			if (prefs.getString(SETTINGS_FULLSCREEN_PREF).equals(ENABLED)) {
			    
				fullscreen = true;
				
			} else if (prefs.getString(SETTINGS_FULLSCREEN_PREF).equals(DISABLED)) {
			    
				fullscreen = false;
				
			}
			
		} else {
		    
			// Default to windowed mode
			fullscreen = false;
			
			prefs.putString(SETTINGS_FULLSCREEN_PREF, DISABLED);
		}
		
		return fullscreen;
	}
	
	/**
	 * Updates Saved Resolution
	 */
	public void updateScreenResolution(String resolution) {

		updateUserPreference(SETTINGS_RESOLUTION_PREF, resolution);
	}
	
	/**
	 * Get Saved Master Sound Level
	 */
	public float getMasterSoundLevel() {
		
		float masterSoundLevel = SoundSettingsTable.DEFAULT_MASTER_SOUND;
		
		if (prefs.getString(SETTINGS_MASTER_SOUND_LEVEL_PREF) != null && !"".equals(prefs.getString(SETTINGS_MASTER_SOUND_LEVEL_PREF))) {
			
			masterSoundLevel = Float.parseFloat(prefs.getString(SETTINGS_MASTER_SOUND_LEVEL_PREF));
			
		} else {
			
			prefs.putString(SETTINGS_MASTER_SOUND_LEVEL_PREF, masterSoundLevel+"");
		}
		
		return masterSoundLevel;
	}
	
	/**
	 * Update Saved Master Sound Level
	 */
	public void updateMasterSoundLevel(float level) {
		
		updateUserPreference(SETTINGS_MASTER_SOUND_LEVEL_PREF, level+"");
	}
	
	/**
	 * Restores the Saved Locale to load on Bundle
	 * 
	 * SETTINGS_LANGUAGE_BUNDLE_PREF
	 * 
	 */
	public Locale getSavedLocale() {
	
		System.out.println("GETTING SAVED LOCALE FROM PREFERENCES");
		
		Locale locale = null;

		// NOT NULL - NOT EMPTY
		if (prefs.getString(SETTINGS_LANGUAGE_BUNDLE_PREF) != null && !"".equals(prefs.getString(SETTINGS_LANGUAGE_BUNDLE_PREF))) {
			
			locale = new Locale(prefs.getString(SETTINGS_LANGUAGE_BUNDLE_PREF));
			
			System.out.println("LOCALE RESTORED: " + locale.getLanguage());
			
		} else {
			
			locale = new Locale(DEFAULT_LOCALE);
			prefs.putString(SETTINGS_LANGUAGE_BUNDLE_PREF, DEFAULT_LOCALE);
			
			System.out.println("LOCALE NOT FOUND - DEFAULT IS : " + locale.getLanguage());
			
			// Save after each change
			saveUserPreferences();
		}

		return locale;
	}
	
	public void saveLocale(Locale locale) {
		
		System.out.println("SAVING SELECTED LOCALE: " + locale.getLanguage());
		
		prefs.putString(SETTINGS_LANGUAGE_BUNDLE_PREF, locale.getLanguage());
		
		// Save after each change
		saveUserPreferences();
	}
	
	/**
	 * Returns a Saved Preference
	 */
	public String getPreference(String key) {
		
		return prefs.getString(key);
	}
	
	/**
	 * Utility method used to update User Preferences
	 */
	public void updateUserPreference(String key, String value) {
		
//		System.out.println("updateUserPreference() " + key + ", " + value);
		
		prefs.putString(key, value);
		
		// Save after each change
		saveUserPreferences();
	}
	
	/**
	 * Utility method used to bulk save User Preferences on Settings Screen Exit
	 */
	public void saveUserPreferences() {
		// bulk update your preferences
		// NOTE: only will get persisted if you explicitly call the flush() method
		prefs.flush();
	}
}