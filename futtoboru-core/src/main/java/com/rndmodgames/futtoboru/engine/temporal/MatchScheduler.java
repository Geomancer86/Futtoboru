package com.rndmodgames.futtoboru.engine.temporal;

/**
 * Match Scheduler v1
 * 
 *  - Solve the friendly match requests
 *  - Put a time to the scheduled matches
 *  - Make sure the league matches are scheduled on free stadiums and timeslots to maximize attendance/etc.
 *  
 * NOTE:
 *  - as the matches are played by two teams we need to make sure we only simulate once, for example only take care of the HOME teams,
 *      and after the match is played, attach the match result to the AWAY team and call it done
 * 
 * 
 * @author Geomancer86
 */
public class MatchScheduler {

}