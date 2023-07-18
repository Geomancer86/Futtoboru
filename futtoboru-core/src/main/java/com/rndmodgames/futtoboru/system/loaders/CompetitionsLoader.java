package com.rndmodgames.futtoboru.system.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Season;

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
                         * id, name, fullname, url_source, competition_type
                         */
                        Competition competition = new Competition();
                        
                        competition.setId(Long.valueOf(splitted[0]));
                        competition.setName(splitted[1]);
                        competition.setFullname(splitted[2]);
                        competition.setUrlSource(splitted[3]);
                        competition.setCompetitionType(splitted[4]);
                        
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
    
    /**
     * Loads the specific data for a Competition Edition
     * 
     *  - We need to load multiple data for each edition so we will need many files, maybe they can be optional
     *  - 
     *  
     *  
     *  - 1887–88 FA Cup: 149 teams, all placed on the first round
     *  - 1888–89 FA Cup: Following the formation of the Football League, this season saw the introduction qualifying rounds,
     *                       with League clubs given the right to request automatic exemption to the first round proper.
     *                       
     *  
     */
    public static void loadCompetitionEditions(List<Competition> competitions, List<Season> seasons) {

        Gdx.app.log("CompetitionsLoader", "LOADING COMPETITIONS EDITIONS FOR " + competitions.size() + " COMPETITIONS FROM FILE SYSTEM!");

        // Iterate seasons and look for the competitions historic and current editions
        for (Season season : seasons) {

            for (Competition competition : competitions) {

                // One Competition Edition per Season
                CompetitionEdition edition = new CompetitionEdition();
                
                //
                FileHandle participantClubsFile = Gdx.files.internal("mods/seasons/" + season.getId() + "/competitions/" + competition.getId() + "/competition_edition.txt");

                //
                if (participantClubsFile.exists()) {

                    BufferedReader reader = new BufferedReader(participantClubsFile.reader());

                    String line;

                    try {
                        line = reader.readLine();

                        // Use # symbol to comment a line
                        while (line != null) {

                            if (!line.startsWith("#")) {

                                String[] splitted = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                                /**
                                 * COLUMNS
                                 * 
                                 * TODO: we can use a single list if we add a third column for
                                 *          the round of the cup this team starts
                                 *          
                                 *       NOTE: we need to save the first round proper in the cup data
                                 *              or make sure the AI / Authority draws the matches with
                                 *              some rules and the draws/playoffs/rounds are accurate
                                 * 
                                 * id, name, url_source, participant_clubs_id_separated_by_commas
                                 */
                                edition.setId(Long.valueOf(splitted[0]));
                                edition.setName(splitted[1]);
                                edition.setUrlSource(splitted[2]);
                                
                                // clean participant ids string
                                String participantIdsString = splitted[3].replace("\"", ""); // remove opening and closing quotes
                                
                                // Load Participant Clubs
                                for (String clubId : Arrays.asList(participantIdsString.split(","))) {
                                
                                    edition.getParticipantClubsIds().add(Long.valueOf(clubId));
                                }
                            }

                            //
                            line = reader.readLine();
                        }
                    } catch (IOException e) {

                        //
                        Gdx.app.error("CompetitionsLoader", "Error loading competition_edition.txt", e);
                    }
                    
                    // add to competition edition list
                    competition.getEditions().add(edition);
                }
            }
        }
    }
}