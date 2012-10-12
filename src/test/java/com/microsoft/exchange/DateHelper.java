/**
 * Copyright 2012, Board of Regents of the University of
 * Wisconsin System. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Board of Regents of the University of Wisconsin
 * System licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/**
 * 
 */

package com.microsoft.exchange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.time.DateUtils;

/**
 * @author Nicholas Blair
 */
public final class DateHelper {

	private static final DatatypeFactory DATATYPE_FACTORY;
	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("unable to initialize a DataTypeFactory", e);
		}
	}
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String DATE_TIME_FORMAT = "yyyyMMdd-hh:mm";
	/**
	 * Convert the {@link Date} into a {@link XMLGregorianCalendar}.
	 * 
	 * @param date
	 * @return
	 * @throws IllegalStateException wrapping a {@link DatatypeConfigurationException}.
	 */
	public static XMLGregorianCalendar convertDateToXMLGregorianCalendar(final Date date) {
		if(date == null) {
			return null;
		}
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		return DATATYPE_FACTORY.newXMLGregorianCalendar(calendar);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public static Date makeDate(String value) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
		try {
			Date date = df.parse(value);
			return DateUtils.truncate(date, java.util.Calendar.DATE);
		} catch (ParseException e) {
			throw new IllegalArgumentException(value + " does not match expected format " + DATE_FORMAT, e);
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public static Date makeDateTime(String value) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
		try {
			Date date = df.parse(value);
			return DateUtils.truncate(date, java.util.Calendar.MINUTE);
		} catch (ParseException e) {
			throw new IllegalArgumentException(value + " does not match expected format " + DATE_FORMAT, e);
		}
	}
}
