package com.rndmodgames.futtoboru.system;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;

class SeasonLeaguesAndTeamsTests {

    private static Futtoboru application;
    
    @BeforeAll
    static void preloadCharacterClasses() {
        
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        
        new HeadlessApplication(application , config);
    }
    
    @Test
    void initialSeasonLeagueAndTeamsTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        List<Season> allSeasons = dbLoader.getSeasons();
        
        // 1st season
        Season season = allSeasons.get(0);
        
        // Country should be England
        Country country = season.getCountries().get(0);
        
        assertNotNull(country);
        
        // Season should have 1 League
        
        
        // League should have 12 Teams
    }
}