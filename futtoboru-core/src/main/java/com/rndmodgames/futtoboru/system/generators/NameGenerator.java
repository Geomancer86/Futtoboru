package com.rndmodgames.futtoboru.system.generators;

import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.loaders.NamesLoader;

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
     * @return
     */
    public static String [] generateName(Country country) {
        
        //
        int numberOfNames = NamesLoader.namesByCountry.get(country.getCommonName()).size();
        int numberOfLastnames = NamesLoader.lastnamesByCountry.get(country.getCommonName()).size();
        
        /**
         * TODO: small chance of having more than 1 name or more than 1 lastname or both
         */
        String randomName = NamesLoader.namesByCountry.get(country.getCommonName()).get(DatabaseLoader.RNG.nextInt(numberOfNames - 1));
        String randomLastName = NamesLoader.lastnamesByCountry.get(country.getCommonName()).get(DatabaseLoader.RNG.nextInt(numberOfLastnames - 1));
        
        //
        String generatedName [] = new String [2]; 
        
        //
        generatedName[0] = randomName;
        generatedName[1] = randomLastName;        
        
        return generatedName;
    }
}