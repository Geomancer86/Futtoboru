package com.rndmodgames.futtoboru.simulator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Geomancer86
 *
 * Initial similation will be turn based
 * 
 */
public class TurnBasedSimulation {

	private Calendar gameCalendar;
	private Date gameDate;
	
	private int turnMinutes;
	
	/**
	 * Default Game Date & Turns Configuration
	 */
	public TurnBasedSimulation() {
		
		// Default Game Starting Date/Calendar
		gameCalendar = new GregorianCalendar(2014, Calendar.FEBRUARY, 11);
		
		// Default Starting Game Date
		gameDate = gameCalendar.getTime();
		
		// Default Turn Advance Minutes
		turnMinutes = 60;
		
		// TEST
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		
		System.out.println(formatter.format(gameDate));
	}
	
	public static void main (String args[]) {
	
		TurnBasedSimulation sim = new TurnBasedSimulation();
	}
}
