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

package com.microsoft.exchange.integration;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import com.microsoft.exchange.DateHelper;
import com.microsoft.exchange.messages.GetUserAvailabilityRequest;
import com.microsoft.exchange.types.ArrayOfMailboxData;
import com.microsoft.exchange.types.DayOfWeekType;
import com.microsoft.exchange.types.Duration;
import com.microsoft.exchange.types.FreeBusyViewOptions;
import com.microsoft.exchange.types.Mailbox;
import com.microsoft.exchange.types.MailboxData;
import com.microsoft.exchange.types.MeetingAttendeeType;
import com.microsoft.exchange.types.SerializableTimeZoneTime;
import com.microsoft.exchange.types.TimeZone;

/**
 * @author Nicholas Blair
 */
public abstract class AbstractIntegrationTest {
	
	/**
	 * Technique borrowed from Jasig CalendarPortlet for constructing a
	 * {@link GetUserAvailabilityRequest}.
	 * 
	 * @param startTime
	 * @param endTime
	 * @param emailAddress
	 * @return
	 */
	protected GetUserAvailabilityRequest constructAvailabilityRequest(Date startTime, Date endTime, String emailAddress) {
		 // construct the SOAP request object to use
        GetUserAvailabilityRequest soapRequest = new GetUserAvailabilityRequest();

        // create an array of mailbox data representing the current user
        ArrayOfMailboxData mailboxes = new ArrayOfMailboxData();
        MailboxData mailbox = new MailboxData();
        Mailbox address = new Mailbox();
        address.setAddress(emailAddress);
        address.setName("");
        mailbox.setAttendeeType(MeetingAttendeeType.REQUIRED);
        mailbox.setExcludeConflicts(false);
        mailbox.setEmail(address);            
        mailboxes.getMailboxDatas().add(mailbox);
        soapRequest.setMailboxDataArray(mailboxes);

        // create a FreeBusyViewOptions representing the specified period
        FreeBusyViewOptions view = new FreeBusyViewOptions();
        view.setMergedFreeBusyIntervalInMinutes(60);
        view.getRequestedView().add("DetailedMerged");
        
        Duration dur = new Duration();
        
        XMLGregorianCalendar start = DateHelper.convertDateToXMLGregorianCalendar(startTime); 
        XMLGregorianCalendar end = DateHelper.convertDateToXMLGregorianCalendar(endTime); 
        dur.setEndTime(end);
        dur.setStartTime(start);
        
        view.setTimeWindow(dur);
        soapRequest.setFreeBusyViewOptions(view);
        
        // set the bias to the start time's timezone offset (in minutes 
        // rather than milliseconds)
        TimeZone tz = new TimeZone();
        java.util.TimeZone tZone = java.util.TimeZone.getTimeZone("UTC");
        tz.setBias(tZone.getRawOffset() / 1000 / 60 );
        
        // TODO: time zone standard vs. daylight info is temporarily hard-coded
        SerializableTimeZoneTime standard = new SerializableTimeZoneTime();
        standard.setBias(0);            
        standard.setDayOfWeek(DayOfWeekType.SUNDAY);
        standard.setDayOrder((short)1);
        standard.setMonth((short)11);
        standard.setTime("02:00:00");
        //standard.setYear("2012");
        SerializableTimeZoneTime daylight = new SerializableTimeZoneTime();
        daylight.setBias(0);
        daylight.setDayOfWeek(DayOfWeekType.SUNDAY);
        daylight.setDayOrder((short)1);
        daylight.setMonth((short)3);
        daylight.setTime("02:00:00");
        //daylight.setYear("2012");
        tz.setStandardTime(standard);
        tz.setDaylightTime(daylight);
        
        soapRequest.setTimeZone(tz);
        
        return soapRequest;
	}
}
