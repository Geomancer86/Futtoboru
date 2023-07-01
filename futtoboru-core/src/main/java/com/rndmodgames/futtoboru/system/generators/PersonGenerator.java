package com.rndmodgames.futtoboru.system.generators;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Person Generator v1
 * 
 * @author Geomancer86
 */
public class PersonGenerator {

    Futtoboru game;
    
    public PersonGenerator(Futtoboru parent) {
        
        this.game = parent;
    }
    
    /**
     * 
     */
    public Person generateUniquePerson(Country country, boolean unique) {
        
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

        person.setName(generatedName[0]);
        person.setLastname(generatedName[1]);
        
//        System.out.println("GENERATED NAME: " + person.getName() + " " + person.getLastname());
        
        /**
         * Check for Uniqueness
         */
        if (unique) {
            
            /**
             * Check against current saved game existing people list
             */
            for (Person check : game.getCurrentGame().getAllPersons()) {
                
//                System.out.println("CHECKING GENERATED NAME AGAINST: " + check.getName() + " " + check.getLastname());
                
                if (person.getName().equals(check.getName())
                        && person.getLastname().equals(check.getLastname())) {
                    
                    System.out.println("DUPLICATE NAME: " + person.getName() + " " + person.getLastname());
                    return null;
                }
            }
        }
        
//        System.out.println("VALID NAME, CREATE PERSON!");
        
        return person;
    }
}