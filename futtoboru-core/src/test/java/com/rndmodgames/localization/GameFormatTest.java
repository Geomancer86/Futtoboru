package com.rndmodgames.localization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

/**
 * U-12: dates, decimals and month/day names follow the game language, not the machine's locale (the bug: a Spanish
 * Windows showed "13 oct. 1888" and "11,0" in an English game). Headless: the real bundles, no GL.
 */
class GameFormatTest {

    private static final LocalDateTime SATURDAY = LocalDateTime.of(1888, 10, 13, 15, 0);

    private I18NBundle previousBundle;
    private Locale previousDefault;

    @BeforeEach
    void spanishMachine() {
        previousBundle = LanguageModLoader.baseBundle;
        previousDefault = Locale.getDefault();
        Locale.setDefault(new Locale("es", "ES")); // a Spanish Windows: must not leak into any of these
    }

    @AfterEach
    void restore() {
        Locale.setDefault(previousDefault);
        LanguageModLoader.setBundle(previousBundle);
    }

    /** Loads one of the game's bundles the way the game does ({@code new Locale(<saved preference>)}). */
    static void load(String language) {
        try {
            File dir = new File(GameFormatTest.class.getResource("/mods/language/BaseBundle_en_us.properties").toURI())
                    .getParentFile();
            LanguageModLoader.setBundle(I18NBundle.createBundle(new FileHandle(new File(dir, "BaseBundle")),
                    new Locale(language)));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void withoutABundleItIsEnglishUsWhateverTheMachine() {
        LanguageModLoader.setBundle(null);
        assertEquals(Locale.US, GameFormat.locale());
        assertEquals("Oct 13, 1888", GameFormat.mediumDate(SATURDAY));
        assertEquals("10/13/1888", GameFormat.numericDate(SATURDAY));
        assertEquals("11.0", GameFormat.decimal(11.0, 1));
        assertEquals("12,000", GameFormat.integer(12000));
        assertEquals("[+1.5]", GameFormat.format("[+%.1f]", 1.5f));
    }

    @Test
    void englishUs() {
        load("en_US");
        assertEquals(Locale.US, GameFormat.locale());
        assertEquals("Oct 13, 1888", GameFormat.mediumDate(SATURDAY));
        assertEquals("10/13/1888", GameFormat.numericDate(SATURDAY));
        assertEquals("Saturday, October 13, 1888", GameFormat.longDate(SATURDAY));
        assertEquals("Sat, Oct 13, 1888", GameFormat.weekdayDate(SATURDAY));
        assertEquals("3:00 PM", GameFormat.time(SATURDAY));
        assertEquals("11.0", GameFormat.decimal(11.0, 1));
    }

    @Test
    void englishUk() {
        load("en_UK");
        assertEquals(Locale.UK, GameFormat.locale());
        assertEquals("13 Oct 1888", GameFormat.mediumDate(SATURDAY));
        assertEquals("13/10/1888", GameFormat.numericDate(SATURDAY));
        assertEquals("Saturday 13 October 1888", GameFormat.longDate(SATURDAY));
        assertEquals("15:00", GameFormat.time(SATURDAY));
        assertEquals("11.0", GameFormat.decimal(11.0, 1));
        assertEquals("12,000", GameFormat.integer(12000));
    }

    @Test
    void spanishSpain() {
        load("sp_SP");
        assertEquals("13 oct 1888", GameFormat.mediumDate(SATURDAY));
        assertEquals("13/10/1888", GameFormat.numericDate(SATURDAY));
        assertEquals("sábado 13 de octubre de 1888", GameFormat.longDate(SATURDAY));
        assertEquals("15:00", GameFormat.time(SATURDAY));
        assertEquals("11,0", GameFormat.decimal(11.0, 1));
        assertEquals("12.000", GameFormat.integer(12000));
    }

    @Test
    void spanishLatinAmerica() {
        load("sp_LA");
        assertEquals("13 oct. 1888", GameFormat.mediumDate(SATURDAY));
        assertEquals("sábado 13 de octubre de 1888", GameFormat.longDate(SATURDAY));
        assertEquals("11.0", GameFormat.decimal(11.0, 1));
    }

    @Test
    void theSavedPreferenceSpellingsAllMap() {
        // the preference keeps Locale#getLanguage ("sp_la"), the language list uses "sp_LA"
        assertEquals(GameFormat.localeOf("sp_LA"), GameFormat.localeOf("sp_la"));
        assertEquals(Locale.UK, GameFormat.localeOf("en_uk"));
        assertEquals(Locale.US, GameFormat.localeOf("nonsense"));
        assertEquals(Locale.US, GameFormat.localeOf(null));
    }

    @Test
    void everyPatternParsesInEveryLanguage() {
        for (String language : List.of("en_US", "en_UK", "sp_SP", "sp_LA")) {
            load(language);
            for (String key : List.of(GameFormat.NUMERIC, GameFormat.MEDIUM, GameFormat.LONG, GameFormat.WEEKDAY,
                    GameFormat.TIME, GameFormat.CLOCK, "schedule_date_pattern", "schedule_day_pattern")) {
                String pattern = LanguageModLoader.baseBundle.get(key);
                assertDoesNotThrow(() -> DateTimeFormatter.ofPattern(pattern), language + " " + key + "=" + pattern);
                assertEquals(DateTimeFormatter.ofPattern(pattern, GameFormat.locale()).format(SATURDAY),
                        GameFormat.date(SATURDAY, key), language + " " + key);
            }
        }
    }

    @Test
    void aBadPatternFallsBackInsteadOfFailing() {
        LanguageModLoader.setBundle(null);
        // a key no bundle has: the built-in English (US) pattern
        assertEquals("Oct 13, 1888", GameFormat.formatter("no_such_pattern_key", "MMM d, yyyy").format(SATURDAY));
        assertEquals("13/10/1888", GameFormat.formatter("no_such_pattern_key", "dd/MM/yyyy").format(LocalDate.of(1888, 10, 13)));
        assertEquals("", GameFormat.mediumDate(null));
    }
}
