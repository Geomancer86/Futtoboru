package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Competitions v1
 * 
 *  - TODO:
 *      - Leagues are a Competition, rework
 *      
 *      - Add the competition type ID:
 *      
 *          - 1: Knockout / Cup
 *          - 2: League
 *          - X: Flexible for other cup types (for example leagues with playoffs)
 *          
 *          - NOTE: we can use the competition type to implement PROMOTION/RELEGATION matches,
 *                   as they are a single match competition on their own with a defined prize besides money
 *          
 *      - Authority ID
 *      
 *      - Historic Dates, cup draw date, initial match date, final match date
 *      
 *      - Historic Participants:
 *          - Invited
 *          - Accepting
 *          
 *      - Winner, Runner Up, Etc.
 *      - Final Match Result and Attendance
 * 
 * @author Geomancer86
 */
public class Competition implements Serializable {

    //
    private static final long serialVersionUID = 1028475113111090045L;

    private Long id;
    private Long name;
    private Long fullname;
    
    private Long authorityId;
    private Long countryId;
    
    private Integer level;
    
    private LocalDateTime creationDate;
    
    /**
     * Competition Season / Historical Data
     * 
     *  - TODO WIP: load the historic teams invited and playing the fa cup for the previous 17 seasons
     */
}