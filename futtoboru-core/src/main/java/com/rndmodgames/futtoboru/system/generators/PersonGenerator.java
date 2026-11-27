package com.rndmodgames.futtoboru.system.generators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Person Generator v1
 * 
 * @author Geomancer86
 */
public class PersonGenerator {

    Futtoboru game;
    SaveGame currentGame;
    
    private List<Person> allPerson = new ArrayList<>();
    
    // the "people" stream of the game's random source (re-forked when the game's seed changes, e.g. on load)
    private RandomSource rootUsed;
    private RandomSource random;
    
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
        
        RandomSource rng = random();
        String generatedName  [] = NameGenerator.generateName(country, rng);

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
        int randomYears = 16 + rng.nextInt(30);
        int randomDays = rng.nextInt(365);
        
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
                    

                    if (person.getName().equals(check.getName()) && person.getLastname().equals(check.getLastname())) {

                        com.rndmodgames.futtoboru.system.Log.info(
                            com.rndmodgames.futtoboru.system.Log.DATA_GENERATION, 
                            "DUPLICATE NAME: " + person.getName() + " " + person.getLastname());
                        return null;
                    }
                }
                
            } else {
                
                // If Generating When Game Running
                for (Person check : game.getCurrentGame().getAllPersons()) {


                    if (person.getName().equals(check.getName()) && person.getLastname().equals(check.getLastname())) {

                        com.rndmodgames.futtoboru.system.Log.info(
                            com.rndmodgames.futtoboru.system.Log.DATA_GENERATION, 
                            "DUPLICATE NAME: " + person.getName() + " " + person.getLastname());
                        return null;
                    }
                }
            }
        }

        /**
         * TODO: DEBUG/log: pretty print person data
         */
        
        // Runtime id from the game's allocator (P2-4); a person made outside a game gets one when it joins
        if (game != null && game.getCurrentGame() != null) {
            game.getCurrentGame().assignId(person);
        }
        
        return person;
    }

    /**
     * One sequential stream for generated people. Retries after a duplicate name draw again, so a person's data
     * depends on the seed and on how many people were generated before (deterministic per seed, not per id).
     */
    private RandomSource random() {
        RandomSource root = Futtoboru.randomOf(game);
        if (random == null || root != rootUsed) {
            rootUsed = root;
            random = root.fork("people");
        }
        return random;
    }
}
