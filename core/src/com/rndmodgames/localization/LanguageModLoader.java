package com.rndmodgames.localization;

import com.badlogic.gdx.utils.I18NBundle;

/**
 * Language Mod Loader v1
 * 
 * @author Geomancer86
 */
public class LanguageModLoader {

	public static Language ENGLISH_US = new Language("en_US", LanguageModLoader.getValue("en_US"), null);
	public static Language ENGLISH_UK = new Language("en_UK", LanguageModLoader.getValue("en_UK"), null);
	public static Language SPANISH_SPAIN = new Language("sp_SP", LanguageModLoader.getValue("sp_SP"), null);
	public static Language SPANISH_LA = new Language("sp_LA", LanguageModLoader.getValue("sp_LA"), null);
//	public static Language PORTUGUESE_PT = new Language("pt_PT", LanguageModLoader.getValue("portuguese_PT"), null);
//	public static Language PORTUGUESE_BZ = new Language("pt_BZ", LanguageModLoader.getValue("portuguese_BZ"), null);
//	public static Language ITALIAN = new Language("it_IT", LanguageModLoader.getValue("italian_IT"), null);
//	public static Language GERMAN = new Language("gm_GM", LanguageModLoader.getValue("german_GM"), null);
//	public static Language RUSSIAN = new Language("ru_RU", LanguageModLoader.getValue("russian_RU"), null);
//	public static Language JAPANESE = new Language("jp_JP", LanguageModLoader.getValue("japanese_JP"), null);
//	public static Language CHINESE = new Language("ch_CH", LanguageModLoader.getValue("chinese_CH"), null);
	
	private static final Language [] allAvailableLanguages = new Language [] { ENGLISH_US,
																			   ENGLISH_UK,
																			   SPANISH_SPAIN,
																			   SPANISH_LA };
	
	public static I18NBundle baseBundle = null;
	
	public static Language [] getAllAvailableLanguages() {
		return allAvailableLanguages;
	}

	public static void setBundle(I18NBundle bundle) {
	
		baseBundle = bundle;
	}
	
	/**
	 * Returns a value from the loaded Bundle
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {
	
		if (baseBundle == null) {
			return key;
		}
		
		if (baseBundle.get(key) == null || baseBundle.get(key).equals("")) {
			return "EMPTY_LABEL";
		}
		
		return baseBundle.get(key);
	}
}