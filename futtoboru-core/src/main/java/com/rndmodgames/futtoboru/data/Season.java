package com.rndmodgames.futtoboru.data;

import java.io.Serializable;

/**
 * Season v1
 * 
 *  - This is the base of all Football Games, without a Season you cannot have anything else
 *      
 *      - This will be the first thing Players Pick on New Game
 *      - Every Season must have one or more databases
 *      
 *  - Alpha Season
 *  
 *      - 1888-1889 England Football Season
 *      - 12 Teams:
 *          - TODO:
 *          
 *      - Home and Away Games
 *      - Standings by Win, Draws and Losses, Goal Average to break ties
 *      - Points were decided mid Season with 2 for wins and 1 for ties
 *      - Average goals was something like 4.44 per match [TODO: citation needed]
 *      
 * 
 * @author Geomancer86
 */
public class Season implements Serializable {

    private static final long serialVersionUID = -72888293779564961L;

    private Long id;
    private String name;
    private String description;
    
}