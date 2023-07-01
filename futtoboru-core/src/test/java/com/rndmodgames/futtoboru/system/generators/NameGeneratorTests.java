package com.rndmodgames.futtoboru.system.generators;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
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
        
        new HeadlessApplication(application , config);
    }
    
    @Test
    void loadNamesTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // England
        Country england = DatabaseLoader.getCountryById(1000L);
        
        assertNotNull(england);
        
        // Generates a random game
        String randomName [] = NameGenerator.generateName(england);
   
        System.out.println("Generated Name: " + randomName[0] + " " + randomName[1]);
        
        assertNotNull(randomName);
    }
    
    @Test
    void generateUniquePersonTest() {
        
        /**
         * This will generate a random person with a name that shouldn't be duplicated
         */
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        
        assertNotNull(dbLoader);
        
        // England
        Country england = DatabaseLoader.getCountryById(1000L);
        
        assertNotNull(england);
        
        // Existing Persons List
        Person person = application.getPersonGenerator().generateUniquePerson(england, false);
        
        //
        assertNotNull(person);
        
        System.out.println("Generated Person: " + person.toString());
    }
    
    @Test
    void generateManyPersonsTest() {
        
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();

        assertNotNull(dbLoader);
        
        // England
        Country england = DatabaseLoader.getCountryById(1000L);
        
        assertNotNull(england);
        
        int generate = 40;
        int invalid = 0;
        
        for (int a = 0; a < generate; a++) {
        
            // Existing Persons List
            Person person = application.getPersonGenerator().generateUniquePerson(england, true);
            
            //
//            assertNotNull(person);
            
            if (person != null) {
                // Add unduplicated person to Current Saved Game Person List
                application.getCurrentGame().getAllPersons().add(person);
                
//                System.out.println("Generated Person: " + person.toString());
            } else {
                
                //
//                System.out.println("Ignoring Duplicated Person Name!");
                invalid++;
            }
        }
        
        System.out.println("Generated " + application.getCurrentGame().getAllPersons().size() + " Unique Persons with " + invalid + " ignored duplicates.");
    }
}