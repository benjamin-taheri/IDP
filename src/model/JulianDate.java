package model;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class JulianDate {	
	public static double dateToJulian(Calendar date) {
	    int year = date.get(Calendar.YEAR);
	    int month = date.get(Calendar.MONTH)+1;
	    int day = date.get(Calendar.DAY_OF_MONTH);
	    int hour = date.get(Calendar.HOUR_OF_DAY);
	    int minute = date.get(Calendar.MINUTE);
	    int second = date.get(Calendar.SECOND);

	    double extra = (100.0 * year) + month - 190002.5;
	    if (month <= 4 || month >= 12) {
	    	return (367.0 * year) -
	    			(Math.floor(7.0 * (year + Math.floor((month + 9.0) / 12.0)) / 4.0)) + 
	    			Math.floor((275.0 * month) / 9.0) +  
	    			day + ((hour + ((minute + (second / 60.0)) / 60.0)) / 24.0) +
	    			1721013.5 - ((0.5 * extra) / Math.abs(extra)) + 0.5 - 1;
	    } else {
	    	return (367.0 * year) -
	    			(Math.floor(7.0 * (year + Math.floor((month + 9.0) / 12.0)) / 4.0)) + 
	    			Math.floor((275.0 * month) / 9.0) +  
	    			day + ((hour + ((minute + (second / 60.0)) / 60.0)) / 24.0) +
	    			1721013.5 - ((0.5 * extra) / Math.abs(extra)) + 0.5;
		}
	  }
	
	public static void main(String args[]) {
		Calendar cal = new GregorianCalendar(2015, 04, 22);
		System.out.println((int)JulianDate.dateToJulian(cal));
	}
}
