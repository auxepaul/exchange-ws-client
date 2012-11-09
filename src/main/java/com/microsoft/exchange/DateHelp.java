/**
 * 
 */

package com.microsoft.exchange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.time.DateUtils;

/**
 * @author Nicholas Blair
 */
public class DateHelp {

	protected static final String DATE_TIME_FORMAT = "yyyyMMdd-HHmm";
	
	/**
	 * @return a new instance of {@link SimpleDateFormat} that uses this application's common Date/Time format ("yyyyMMdd-HHmm").
	 */
	public static SimpleDateFormat getDateTimeFormat() {
		return new SimpleDateFormat(DATE_TIME_FORMAT);
	}
	
	/**
	 * Convert a {@link String} in the common date/time format for this application into a {@link Date}.
	 * 
	 * @param timePhrase format: "yyyyMMdd-HHmm"
	 * @return the corresponding date
	 * @throws IllegalArgumentException
	 */
	public static Date parseDateTimePhrase(final String timePhrase) {
		try {
			Date time = getDateTimeFormat().parse(timePhrase);
			time = DateUtils.truncate(time, Calendar.MINUTE);
			return time;
		} catch (ParseException e) {
			throw new IllegalArgumentException("cannot parse date/time phrase " + timePhrase, e);
		}
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static XMLGregorianCalendar convertDateToXMLGregorianCalendar(final Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("unable to invoke DatatypeFactory.newInstance", e);
		}
	}
}
