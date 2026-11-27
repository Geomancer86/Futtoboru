package com.rndmodgames.futtoboru.system.generators;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

public class NameGeneratorTests {

    private static Futtoboru application;
    
    @BeforeAll
    static void preload() {
        
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new Futtoboru();
        
        // Initialize the Saved Game
        application.setCurrentGame(new SaveGame());
        
        // Set the Person Generator
        application.setPersonGenerator(new PersonGenerator(application));
        
        new HeadlessApplication(application , config);
        com.rndmodgames.futtoboru.BaseTest.awaitHeadlessCreate(); // create() must finish: DatabaseLoader publishes its instance before loading
    }
    
    @Test
    void loadNamesTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // England
        Country england = DatabaseLoader.getCountryByDataId("england");
        
        assertNotNull(england);
        
        // Generates a random game
        String randomName [] = NameGenerator.generateName(england, RandomSource.seeded(1888));
   
        assertNotNull(randomName);
        
        // 
        Gdx.app.log("NameGeneratorTests", "Generated Name: " + randomName[0] + " " + randomName[1]);
    }
    
    @Test
    void generateUniquePersonTest() {
        
        /**
         * This will generate a random person with a name that shouldn't be duplicated
         */
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // England
        Country england = DatabaseLoader.getCountryByDataId("england");
        
        assertNotNull(england);
        
        // Existing Persons List
        Person person = application.getPersonGenerator().generateUniquePerson(england, null, false);
        
        //
        assertNotNull(person);
        
        //
        Gdx.app.log("NameGeneratorTests", "Generated Person: " + person.toString());
    }
    
    @Test
    void generateManyPersonsTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();

        assertNotNull(dbLoader);
        
        // England
        Country england = DatabaseLoader.getCountryByDataId("england");
        
        assertNotNull(england);
        
        int generate = 40;
        int invalid = 0;
        
        for (int a = 0; a < generate; a++) {
        
            // Existing Persons List
            Person person = application.getPersonGenerator().generateUniquePerson(england, null, true);
            
            //
//            assertNotNull(person);
            
            if (person != null) {
                
                // Add unduplicated person to Current Saved Game Person List
                application.getCurrentGame().getAllPersons().add(person);
            } else {
                
                //
                invalid++;
            }
        }
        
        //
        Gdx.app.log("NameGeneratorTests", "Generated " + application.getCurrentGame().getAllPersons().size() + " Unique Persons with " + invalid + " ignored duplicates.");
    }
}