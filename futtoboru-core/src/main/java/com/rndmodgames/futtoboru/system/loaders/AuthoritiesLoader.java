package com.rndmodgames.futtoboru.system.loaders;

import java.time.LocalDateTime;
import java.time.Month;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.system.DatabaseLoader;

public class AuthoritiesLoader {

    /**
     * TODO WIP
     * 
     * UNHARDCODE AUTHORITY,
     * LOAD AUTHORITIES FROM FILE
     * ALLOW HIERARCHIES
     * ETC
     */
    public static void loadAuthorities() {
        
        /**
         * Hardcoded Main Authority NOTE: only one for now
         */
        Authority ifab = new Authority();
        ifab.setName("International Football Association Board");
        ifab.setShortName("IFAB");
        ifab.setFoundingDate(LocalDateTime.of(1886, Month.JUNE, 2, 19, 30, 00)); // 2 June 1886
        ifab.setCountry(DatabaseLoader.getCountryById(1000L)); // England
        ifab.setLevel(1); // Top Level
        ifab.setParent(null); // No Authority on Top
        
        // Set Main Authority
        DatabaseLoader.setMainAuthority(ifab);
    }
}