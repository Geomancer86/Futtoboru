package com.rndmodgames.futtoboru.system.generators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Person Generator v1
 * 
 * @author Geomancer86
 */
public class PersonGenerator {

    Futtoboru game;
    SaveGame currentGame;
    
    private List<Person> allPerson = new ArrayList<>();
    
    public PersonGenerator(Futtoboru parent) {
        
        this.game = parent;
    }
    
    /**
     * @param season 
     * 
     */
    public Person generateUniquePerson(Country country, Season season, boolean unique) {
        
        /**
         * - Generate Name
         * - Generate Lastname
         * 
         * - TODO: 
         *      - small chance of having more than 1 name
         *      - small chance of having more than 1 lastname
         *      
         *  - Check the generated name and lastname combination against the SaveGame person list, make sure there is no duplicates
         *      - Keep regenerating (a number of times) until the Person is unique
         */
        
        Person person = new Person();
        
        String generatedName  [] = NameGenerator.generateName(country);

        // Generated Name
        person.setName(generatedName[0]);
        person.setLastname(generatedName[1]);
    
        // Nationality
        person.setCountry(country);
        
        /**
         * Birthdate / Age
         * 
         *  - Generate default birth dates for Players to be 16-40 years old
         *  - Birth Date should be relative to Current Game / Current Date
         */
        
        LocalDateTime randomBirthDate = null;
        
        // Randomize
        int randomYears = 16 + DatabaseLoader.RNG.nextInt(30);
        int randomDays = DatabaseLoader.RNG.nextInt(365);
        
        if (currentGame != null) {
            
            // Set the relative birth date to the Current Game Date
            // Used in Real Time Person Generation
            randomBirthDate = currentGame.getGameDate();
            
        } else if (season != null) {    
        
            // Use the Season Date
            // Used when generating new data During New Game
            randomBirthDate = season.getStartDate();
            
        } else {

            // Set the relative birth date to NOW
            // NOTE: Used by Unit Tests
            randomBirthDate = LocalDateTime.now();
        }
        
        // Substract the Years and Days to get the Birthdate
        randomBirthDate = randomBirthDate.minusYears(randomYears);
        randomBirthDate = randomBirthDate.minusDays(randomDays);
        
        //
        person.setBirthDate(randomBirthDate);
        
        /**
         * Check for Uniqueness
         */
        if (unique) {

            // If Generating Before Game Running
            if (game == null) {
                                
                for (Person check : DatabaseLoader.getPersons()) {
                    
//                    System.out.println("CHECKING BEFORE RUNNING - GENERATED NAME AGAINST " + DatabaseLoader.getPersons().size() + " EXISTING PERSON NAMES");

                    if (person.getName().equals(check.getName()) && person.getLastname().equals(check.getLastname())) {

                        System.out.println("DUPLICATE NAME: " + person.getName() + " " + person.getLastname());
                        return null;
                    }
                }
                
            } else {
                
                // If Generating When Game Running
                for (Person check : game.getCurrentGame().getAllPersons()) {

                    // System.out.println("CHECKING GENERATED NAME AGAINST: " + check.getName() + " " + check.getLastname());
//                    System.out.println("CHECKING INGAME - GENERATED NAME AGAINST " + game.getCurrentGame().getAllPersons().size() + " EXISTING PERSON NAMES");

                    if (person.getName().equals(check.getName()) && person.getLastname().equals(check.getLastname())) {

                        System.out.println("DUPLICATE NAME: " + person.getName() + " " + person.getLastname());
                        return null;
                    }
                }
            }
        }

        /**
         * TODO: DEBUG/log: pretty print person data
         */
        
        return person;
    }
}