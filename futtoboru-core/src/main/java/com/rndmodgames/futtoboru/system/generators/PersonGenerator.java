package com.rndmodgames.futtoboru.system.generators;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;

/**
 * Person Generator v1
 * 
 * @author Geomancer86
 */
public class PersonGenerator {

    /**
     * 
     */
    public static Person generateUniquePerson(Country country) {
        
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
        
        return person;
    }
}