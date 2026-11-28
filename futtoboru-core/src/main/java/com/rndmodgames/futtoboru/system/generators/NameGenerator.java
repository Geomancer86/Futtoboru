package com.rndmodgames.futtoboru.system.generators;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

/**
 * Name Generator v1
 * 
 * TODO: load names and lastnames from scripts / filesystem
 * 
 * @author Geomancer86
 */
public class NameGenerator {

    /**
     * Generates an unique Name and Lastname Pair
     * 
     * Names will be returned as Name,Lastname separated by commas
     * 
     * NOTE: uniqueness will be managed in-game when adding new players/people to the database
     * 
     * TODO: support for dual names and dual lastnames
     * 
     * @param country
     * @param rng picks the names
     * @return
     */
    public static String [] generateName(Country country, RandomSource rng) {
        
        //
        java.util.List<String> names = DatabaseLoader.getInstance().getFirstNames(country);
        java.util.List<String> lastnames = DatabaseLoader.getInstance().getLastNames(country);
        int numberOfNames = names.size();
        int numberOfLastnames = lastnames.size();
        if (numberOfNames < 2 || numberOfLastnames < 2) {
            throw new IllegalArgumentException("No names to generate a person from " + country);
        }
        
        /**
         * TODO: small chance of having more than 1 name or more than 1 lastname or both
         */
        // G-04 fix: nextInt(size - 1) never drew the last name of either list
        String randomName = names.get(rng.nextInt(numberOfNames));
        String randomLastName = lastnames.get(rng.nextInt(numberOfLastnames));
        
        //
        String generatedName [] = new String [2]; 
        
        //
        generatedName[0] = randomName;
        generatedName[1] = randomLastName;        
        
        return generatedName;
    }
}