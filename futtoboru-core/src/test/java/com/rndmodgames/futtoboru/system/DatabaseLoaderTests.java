package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Continent;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.DataBase;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;

class DatabaseLoaderTests {

    private static Futtoboru application;
    
    @BeforeAll
    static void preloadCharacterClasses() {
        
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        
        new HeadlessApplication(application , config);
    }
    
    @Test
    void loadDatabaseTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        List<DataBase> allDataBases = dbLoader.getDataBases();
        
        assertNotNull(allDataBases);
        
        assertNotEquals(true, allDataBases.isEmpty());
        
        System.out.println("FOUND " + allDataBases.size() + " DATABASES");
    }
    
    @Test
    void loadContinentsTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        List<Continent> allContinents = dbLoader.getContinents();
        
        assertNotNull(allContinents);
        
        assertNotEquals(true, allContinents.isEmpty());
        
        System.out.println("FOUND " + allContinents.size() + " CONTINENTS");
    }
    
    @Test
    void loadCountriesTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        List<Country> allCountries = dbLoader.getCountriesByContinent(dbLoader.getContinents().get(0));
        
        assertNotNull(allCountries);
        
        assertNotEquals(true, allCountries.isEmpty());
        
        System.out.println("FOUND " + allCountries.size() + " COUNTRIES");
    }
    
    @Test
    void loadSeasonsTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        List<Season> allSeasons = dbLoader.getSeasons();
        
        assertNotNull(allSeasons);
        
        assertNotEquals(true, allSeasons.isEmpty());
        
        System.out.println("FOUND " + allSeasons.size() + " SEASONS");
    }
}
