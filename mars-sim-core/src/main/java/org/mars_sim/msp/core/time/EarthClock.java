/**
 * Mars Simulation Project
 * EarthClock.java
 * @version 3.07 2015-01-08

 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.mars_sim.msp.core.Simulation;

/**
 * The EarthClock class keeps track of Earth Universal Time.
 * It should be synchronized with the Mars clock.
 * TODO format date strings in an internationalized fashion depending on user locale.
 */
public class EarthClock
//extends GregorianCalendar
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private String fullDateTimeString;
	private final GregorianCalendar cal;

	// Data members
	private final SimpleDateFormat formatter;
	private final DateFormat formatter_millis;
	//private final DateTimeFormatter dtFormatter_millis;

	/**
	 * Constructor.
	 * @param fullDateTimeString the UT date string in format: "MM/dd/yyyy hh:mm:ss" e.g. 09/30/2043 00:00:00.
	 * @throws Exception if date string is invalid.
	 */
	public EarthClock(String fullDateTimeString) {
		this.fullDateTimeString = fullDateTimeString;
		
		// Use GregorianCalendar constructor
		cal = new GregorianCalendar();

		// Set GMT timezone for calendar
		SimpleTimeZone zone = new SimpleTimeZone(0, "GMT");
		cal.setTimeZone(zone);
		
		// Initialize formatter
		formatter = new SimpleDateFormat("yyyy-MMM-dd  HH:mm:ss '(UT)'");
		formatter.setTimeZone(zone);

		// Set Earth clock to Martian Zero-orbit date-time.
		// This date may need to be adjusted if it is inaccurate.
		cal.clear();
		DateFormat tempFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		tempFormatter.setTimeZone(zone);
		try {
			cal.setTime(tempFormatter.parse(fullDateTimeString));
		} catch (ParseException ex) {
			throw new IllegalStateException(ex);
		}
		
		//System.out.println("GMT/UT: cal.getTime() is " + cal.getTime());
		//System.out.println("dateString is " + fullDateTimeString);
		//System.out.println("this.getTimeStamp() is " + this.getTimeStamp()); 
		
		// Initialize a second formatter
		formatter_millis = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");//.SSS"); // :SSS
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		formatter_millis.setTimeZone(gmt);
		formatter_millis.setLenient(false);
		
		// Use Java 8 Date/Time API in java.time package
		//dtFormatter_millis = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss");//.AAAA");//AAAA");
		}

	public Calendar getCalender() {
		return cal;
	}
	
	/**
	 * Returns the number of milliseconds since 1 January 1970 00:00:00
	 * @return long 
	 */
	public static long getMillis(EarthClock clock) {
		long millis = clock.getCalender().getTimeInMillis();
		
		Instant instant = Instant.ofEpochMilli(clock.getCalender().getTimeInMillis());
		//System.out.println("millis from cal is " + clock.getCalender().getTimeInMillis());
		System.out.println("instant is " + instant);
		//ZoneId zoneId = ZoneId.of("UTC-0");
		//LocalDateTime ldt = LocalDateTime.ofInstant(instant, zoneId);// ZoneId.systemDefault());
		
		// convert formatter_millis to dtFormatter
		//String s = getCurrentDateTimeString(clock);
		//s = s.substring(0, 21) + "0" + s.substring(22, 24);
		
		//System.out.println("s mod is "+ s);
		
		//LocalDateTime ldt = LocalDateTime.parse(s, dtFormatter_millis);
		
		//ZoneOffset offset = ZoneOffset.of("+00:00");
		//ZonedDateTime dt = ZonedDateTime.now( ZoneOffset.UTC );	
		//ZoneId zoneId = ZoneId.of("UTC-0");
		//LocalDateTime ldt = LocalDateTime.of(2043, 9, 30, 00, 00, 00);
		//ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC-0"));
		//ZonedDateTime zdt = ZonedDateTime.of(2043, 9, 30, 00, 00, 00, 0000, zoneId);				
		//long millis = zdt.toInstant().toEpochMilli();
		//long sec = zdt.getEpochSecond();
		//long millis2 = zdt.get(ChronoField.MILLI_OF_SECOND);
		//System.out.println("millis2 is " + millis2);
		
		return millis;
	}
	
	/**
	 * Returns the date/time formatted in a string
	 * @return date/time formatted in a string. ex "2055-May-06 03:37:22 01212"
	 */
	public synchronized String getCurrentDateTimeString(EarthClock clock) {
		String result = formatter_millis.format(cal.getTime());
		//System.out.println("getCurrentDateTimeString() is " + result);
		return result;
	}
	
	/* 
	 * Gets Julian Date (UT), the number of days (rather than milliseconds) since the unix epoch
	 * Note: the Julian Date UT at the Unix epoch is 2,440,587.5. 
     * @return days
	*/
	public static long getJulianDateUT(EarthClock clock) {
		long result = (long) (2440587.5 + (getMillis(clock) / 8.64E7));
		return result;
	}

	/* 
	 * Gets Julian Date Terrestrial Time (TT), the number of days (rather than milliseconds) since the unix epoch
	 * Note: add the leap seconds which, since 1 July 2012
     * @return days
	*/
	public static long getJulianDateTT(EarthClock clock) {
		long result = (long) (getJulianDateUT(clock) +  (35D + 32.184) / 86400D);
		return result;
	}
	
	/* 
	 * Gets the days since J2000 Epoch, the number of (fractional) days since 12:00 on 1 January 2000 in Terrestrial Time (TT).
	 * Note: JD TT was 2,451,545.0 at the J2000 epoch 
     * @return days
	*/
	public static long getDaysSinceJ2kEpoch(EarthClock clock) {
		long result = (long) (getJulianDateTT(clock) - 2451545.0);
		return result;
	}
	
	/*
	 * Gets Mars Sol Date (MSD), starting at midnight on 6th January 2000 (when time_J2000 = 4.5), at the Martian prime meridian,
     * Note: by convention, to keep the MSD positive going back to midday December 29th 1873, we add 44,796.
     * @return days
     */
	public static double getMarsSolDate(EarthClock clock) {
		// 0.00096 is a slight adjustment as the midnights by Mars24
		double result = ((getDaysSinceJ2kEpoch(clock) - 4.5) / 1.027491252) + 44796.0 - 0.00096;
		return result;
	}
	
	/*
	 * Gets Coordinated Mars Time (MTC), a mean time for Mars, like Earth's UTC 
     * Note: Calculated directly from the Mars Sol Date
     * @return hours
     */
	public static double getMTC(EarthClock clock) {
		// 0.00096 is a slight adjustment as the midnights by Mars24
		double result = (24 * getMarsSolDate(clock)) % 24;
		return result;
	}
	
	/*
	 * Gets Mars Mean Anomaly, a measure of where Mars is in its orbit, namely how far into the full orbit the body is 
	 * since its last periapsis (the point in the ellipse closest to the focus).
     * @return degree
     */
	public static double getMarsMeanAnomaly(EarthClock clock) {
		// 0.00096 is a slight adjustment as the midnights by Mars24
		double result = (19.3870 + 0.52402075 *getDaysSinceJ2kEpoch(clock))%360;
		return result;
	}
	
	/*
	 * 
     * @return 
     */
	public static double getAlphaFMS(EarthClock clock) {
		double result = (270.3863 + 0.52403840 * getDaysSinceJ2kEpoch(clock)) % 360;
		return result;
	}
	
	/*
	 * 
     * @return 
     */
	public static double getE(EarthClock clock) {
		double result = 0.09340 + 2.477E-9 * getDaysSinceJ2kEpoch(clock) ;
		return result;
	}
	
	/*
	 * 
     * @return 
     */
	public static double getPBS(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double result = 0.0071 * Math.cos((0.985626 * j2000 /  2.2353) +  49.409) +
                0.0057 * Math.cos((0.985626 * j2000 /  2.7543) + 168.173) +
                0.0039 * Math.cos((0.985626 * j2000 /  1.1177) + 191.837) +
                0.0037 * Math.cos((0.985626 * j2000 / 15.7866) +  21.736) +
                0.0021 * Math.cos((0.985626 * j2000 /  2.1354) +  15.704) +
                0.0020 * Math.cos((0.985626 * j2000 /  2.4694) +  95.528) +
                0.0018 * Math.cos((0.985626 * j2000 / 32.8493) +  49.095);
		return result;
	}
	
	/*
	 * 
     * @return 
     */
	public static double getNU(EarthClock clock) {
		double j2000 = getDaysSinceJ2kEpoch(clock);
		double m = getMarsMeanAnomaly(clock);
		double result = (10.691 + 3.0E-7 * j2000) * Math.sin(m) +
                0.623 * Math.sin(2 * m) +
                0.050 * Math.sin(3 * m) +
                0.005 * Math.sin(4 * m) +
                0.0005 * Math.sin(5 * m) +
                getPBS(clock);
		return result;
	}
		
	
	/**
	 * Returns the date/time formatted in a string
	 * @return date/time formatted in a string. ex "2055-May-06 03:37:22 (UT)"
	 */
	//2015-01-08 Added if clause
	public synchronized String getTimeStamp() {
		String result = formatter.format(cal.getTime());// + " (UT)";
		if (result == null) result = "0";
		return result;
	}

	/**
	 * Returns the date formatted in a string
	 * @return date formatted in a string. ex "2055-May-06"
	 */
	// Kung: why do we want to deprecate this method with @deprecated tag?
	public synchronized String getDateString() {
		return getTimeStamp().substring(0,11);
	}

	/**
	 * Adds time to the calendar
	 * @param seconds seconds added to the calendar
	 */
	public synchronized void addTime(double seconds) {
		cal.add(Calendar.MILLISECOND, (int) (seconds * 1000D));
	}

	/**
	 * Displays the string version of the clock.
	 * @return time stamp string.
	 * @deprecated
	 */
	public synchronized String toString() {
		return getTimeStamp();
	}

	public int getDayOfMonth()
	{
		return cal.get(Calendar.DATE);
	}

	public int getMonth()
	{
		return cal.get(Calendar.MONTH);
	}

	public int getYear()
	{
		return cal.get(Calendar.YEAR);
	}
	
}