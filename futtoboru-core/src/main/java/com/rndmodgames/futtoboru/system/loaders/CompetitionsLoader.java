package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Competition;

/**
 * Competitions Loader v1
 * 
 *  - Load existing competitions, history and rules from file system
 * 
 * @author Geomancer86
 */
public class CompetitionsLoader {

    /**
     * Loads the existing Competitions Data
     */
    public static void load(List<Competition> competitions) {
        
        //
        Gdx.app.log("CompetitionsLoader", "LOADING COMPETITIONS FROM FILE SYSTEM!");
        
        FileHandle competitionsFile = Gdx.files.internal("mods/competitions.txt");
        
        if (competitionsFile.exists()) {
            
            BufferedReader reader = new BufferedReader(competitionsFile.reader());
            
            String line;

            try {
                line = reader.readLine();

                // Use # symbol to comment a line
                while (line != null) {

                    if (!line.startsWith("#")) {

                        String[] splitted = line.split(",");
                        
                        /**
                         * File Format - COLUMNS
                         * 
                         * id, name, fullname, url_source
                         */
                        Competition competition = new Competition();
                        
                        competition.setId(Long.valueOf(splitted[0]));
                        competition.setName(splitted[1]);
                        competition.setFullname(splitted[2]);
                        competition.setUrlSource(splitted[3]);
                        
                        // Add to Database competitions list
                        competitions.add(competition);
                    }
                    
                    //
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                
                //
                Gdx.app.error("CompetitionsLoader", "Error loading competitions.txt", e);
            }
        } else {
            
            //
            Gdx.app.log("CompetitionsLoader", "mods/seasons/competitions.txt doesnt exist");
        }
        
        //
        Gdx.app.log("CompetitionsLoader", "Finished loading " + competitions.size() + " Competitions from file.");
    }
}