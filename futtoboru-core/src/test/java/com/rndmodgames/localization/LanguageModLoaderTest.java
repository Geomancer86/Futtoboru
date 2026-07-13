package com.rndmodgames.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.rndmodgames.PreferencesManager;
import com.rndmodgames.futtoboru.harness.HeadlessGdxTestSupport;

class LanguageModLoaderTest {

    @Test
    void headlessBootstrapLoadsLanguageKeys() {
        HeadlessGdxTestSupport.ensureInitialized();

        assertEquals("English (US)", LanguageModLoader.getValue("en_US"));
        assertEquals("English (UK)", LanguageModLoader.getValue("en_UK"));
        assertEquals("Spanish (Spain)", LanguageModLoader.getValue("sp_SP"));
        assertEquals("Spanish (Latin America)", LanguageModLoader.getValue("sp_LA"));

        assertEquals("English (US)", LanguageModLoader.ENGLISH_US.getName());
        assertFalse(LanguageModLoader.ENGLISH_US.getName().equals("en_US"));
    }

    @Test
    void parseLocaleTagHandlesUsUkAndLegacyForms() {
        Locale us = PreferencesManager.parseLocaleTag("en_US");
        assertEquals("en", us.getLanguage());
        assertEquals("US", us.getCountry());
        assertEquals("en_US", PreferencesManager.toLocaleTag(us));

        Locale legacy = PreferencesManager.parseLocaleTag("en_us");
        assertEquals("en", legacy.getLanguage());
        assertEquals("US", legacy.getCountry());

        Locale hyphen = PreferencesManager.parseLocaleTag("en-UK");
        assertEquals("en", hyphen.getLanguage());
        assertEquals("UK", hyphen.getCountry());
        assertTrue(PreferencesManager.toLocaleTag(hyphen).startsWith("en_"));
    }
}
