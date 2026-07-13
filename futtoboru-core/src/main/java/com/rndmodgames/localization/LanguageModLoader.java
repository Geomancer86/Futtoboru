package com.rndmodgames.localization;

import com.badlogic.gdx.utils.I18NBundle;

/**
 * Language Mod Loader v1
 *
 * NOTE: The bundle needs to be preloaded while loading the game assets or will be null here
 *
 * TODO: this component is a great candidate for a separate JAR PROJECT
 *
 * @author Geomancer86
 */
public class LanguageModLoader {

	// Fallback names avoid class-init spam before the I18N bundle is loaded.
	public static Language ENGLISH_US = new Language("en_US", "English (US)", null);
	public static Language ENGLISH_UK = new Language("en_UK", "English (UK)", null);
	public static Language SPANISH_SPAIN = new Language("sp_SP", "Spanish (Spain)", null);
	public static Language SPANISH_LA = new Language("sp_LA", "Spanish (Latin America)", null);

	private static final Language[] allAvailableLanguages = new Language[] {
			ENGLISH_US, ENGLISH_UK, SPANISH_SPAIN, SPANISH_LA };

	public static I18NBundle baseBundle = null;

	public static Language[] getAllAvailableLanguages() {
		return allAvailableLanguages;
	}

	public static void setBundle(I18NBundle bundle) {
		baseBundle = bundle;
		// DO NOT FAIL ON MISSING KEY in production UI
		I18NBundle.setExceptionOnMissingKey(false);
		refreshLanguageNames();
	}

	private static void refreshLanguageNames() {
		ENGLISH_US.setName(getValue("en_US"));
		ENGLISH_UK.setName(getValue("en_UK"));
		SPANISH_SPAIN.setName(getValue("sp_SP"));
		SPANISH_LA.setName(getValue("sp_LA"));
	}

	/**
	 * Returns a value from the loaded Bundle.
	 *
	 * @param key bundle key
	 * @return localized value, or the key when missing / unloaded
	 */
	public static String getValue(String key) {
		if (baseBundle == null) {
			return key;
		}

		String value = baseBundle.get(key);
		if (value == null || value.isEmpty() || value.contains("???")) {
			System.out.println("missing bundle key: " + key);
			return key;
		}

		return value.replace("???", "");
	}
}
