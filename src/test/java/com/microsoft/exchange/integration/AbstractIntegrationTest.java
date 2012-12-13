/**
 * See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Board of Regents of the University of Wisconsin System
 * licenses this file to you under the Apache License,
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.exchange.DateHelp;
import com.microsoft.exchange.DateHelper;
import com.microsoft.exchange.ExchangeWebServices;
import com.microsoft.exchange.impl.ExchangeWebServicesClient;
import com.microsoft.exchange.messages.ArrayOfResponseMessagesType;
import com.microsoft.exchange.messages.CreateItem;
import com.microsoft.exchange.messages.CreateItemResponse;
import com.microsoft.exchange.messages.DeleteItem;
import com.microsoft.exchange.messages.DeleteItemResponse;
import com.microsoft.exchange.messages.FindItem;
import com.microsoft.exchange.messages.GetUserAvailabilityRequest;
import com.microsoft.exchange.messages.ItemInfoResponseMessageType;
import com.microsoft.exchange.messages.ResponseCodeType;
import com.microsoft.exchange.messages.ResponseMessageType;
import com.microsoft.exchange.types.ArrayOfMailboxData;
import com.microsoft.exchange.types.ArrayOfRealItemsType;
import com.microsoft.exchange.types.BodyType;
import com.microsoft.exchange.types.BodyTypeType;
import com.microsoft.exchange.types.CalendarItemCreateOrDeleteOperationType;
import com.microsoft.exchange.types.CalendarItemType;
import com.microsoft.exchange.types.CalendarViewType;
import com.microsoft.exchange.types.DayOfWeekType;
import com.microsoft.exchange.types.DefaultShapeNamesType;
import com.microsoft.exchange.types.DisposalType;
import com.microsoft.exchange.types.DistinguishedFolderIdNameType;
import com.microsoft.exchange.types.DistinguishedFolderIdType;
import com.microsoft.exchange.types.Duration;
import com.microsoft.exchange.types.EmailAddressType;
import com.microsoft.exchange.types.FreeBusyViewOptions;
import com.microsoft.exchange.types.ItemQueryTraversalType;
import com.microsoft.exchange.types.ItemResponseShapeType;
import com.microsoft.exchange.types.ItemType;
import com.microsoft.exchange.types.Mailbox;
import com.microsoft.exchange.types.MailboxData;
import com.microsoft.exchange.types.MeetingAttendeeType;
import com.microsoft.exchange.types.NonEmptyArrayOfAllItemsType;
import com.microsoft.exchange.types.NonEmptyArrayOfBaseFolderIdsType;
import com.microsoft.exchange.types.NonEmptyArrayOfBaseItemIdsType;
import com.microsoft.exchange.types.SerializableTimeZoneTime;
import com.microsoft.exchange.types.TargetFolderIdType;
import com.microsoft.exchange.types.TimeZone;

/**
 * @author Nicholas Blair
 */
public abstract class AbstractIntegrationTest {

	protected final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	protected ExchangeWebServicesClient ewsClient;
	@Autowired
	protected JAXBContext jaxbContext;

	/**
	 * This method gets called at the beginning of each integration test method.
	 * The purpose is for subclasses to set the necessary credentials.
	 */
	public abstract void initializeCredentials();

	/**
	 * Create a single {@link CalendarItemType} and submit with {@link ExchangeWebServicesClient#createItem(CreateItem)}.
	 * @throws JAXBException 
	 */
	@Test
	public void testCreateCalendarItem() throws JAXBException {
		NonEmptyArrayOfBaseItemIdsType createdIds = new NonEmptyArrayOfBaseItemIdsType();
		try {
			initializeCredentials();

			CalendarItemType calendarItem = new CalendarItemType();
			final Date start = DateHelp.parseDateTimePhrase("20121109-1200");
			final Date end = DateHelp.parseDateTimePhrase("20121109-1300");

			calendarItem.setStart(DateHelp.convertDateToXMLGregorianCalendar(start));
			calendarItem.setEnd(DateHelp.convertDateToXMLGregorianCalendar(end));
			calendarItem.setSubject("integration test: testCreateCalendarItem");
			calendarItem.setLocation("test location");
			BodyType body = new BodyType();
			body.setBodyType(BodyTypeType.TEXT);
			body.setValue("test ran at " + new Date());
			calendarItem.setBody(body);

			CreateItem request = new CreateItem();
			request.setSendMeetingInvitations(CalendarItemCreateOrDeleteOperationType.SEND_TO_ALL_AND_SAVE_COPY);

			NonEmptyArrayOfAllItemsType arrayOfItems = new NonEmptyArrayOfAllItemsType();
			arrayOfItems.getItemsAndMessagesAndCalendarItems().add(calendarItem);
			request.setItems(arrayOfItems);
			DistinguishedFolderIdType folder = new DistinguishedFolderIdType();
			folder.setId(DistinguishedFolderIdNameType.CALENDAR);
			TargetFolderIdType target = new TargetFolderIdType();
			target.setDistinguishedFolderId(folder);
			request.setSavedItemFolderId(target);

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			CreateItemResponse response = ewsClient.createItem(request);
			stopWatch.stop();
			log.debug("CreateItem request (1 CalendarItem) completed in " + stopWatch);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(response, System.out);

			Assert.assertNotNull(response);
			ArrayOfResponseMessagesType responseMessages = response.getResponseMessages();
			Assert.assertNotNull(responseMessages);
			Assert.assertEquals(1, responseMessages.getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages().size());
			JAXBElement<? extends ResponseMessageType> m = responseMessages.getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages().get(0);
			Assert.assertEquals(ResponseCodeType.NO_ERROR, m.getValue().getResponseCode());

			ItemInfoResponseMessageType itemType = (ItemInfoResponseMessageType) m.getValue();
			ArrayOfRealItemsType itemArray = itemType.getItems();
			ItemType item = itemArray.getItemsAndMessagesAndCalendarItems().get(0);
			createdIds.getItemIdsAndOccurrenceItemIdsAndRecurringMasterItemIds().add(item.getItemId());

		} finally {
			deleteItems(createdIds);
		}
	}

	/**
	 * Create 3 {@link CalendarItemType}s and submit with 1 {@link ExchangeWebServicesClient#createItem(CreateItem)} invocation.
	 */
	@Test
	public void testCreate3CalendarItems() {
		NonEmptyArrayOfBaseItemIdsType createdIds = new NonEmptyArrayOfBaseItemIdsType();
		try {
			initializeCredentials();

			CalendarItemType item1 = constructCalendarItem(DateHelp.parseDateTimePhrase("20121109-1300"), DateHelp.parseDateTimePhrase("20121109-1400"), 
					"integration test: testCreate3CalendarItems, item1", "test location", "test ran at " + new Date());
			CalendarItemType item2 = constructCalendarItem(DateHelp.parseDateTimePhrase("20121109-1400"), DateHelp.parseDateTimePhrase("20121109-1500"), 
					"integration test: testCreate3CalendarItems, item2", "test location", "test ran at " + new Date());
			CalendarItemType item3 = constructCalendarItem(DateHelp.parseDateTimePhrase("20121109-1500"), DateHelp.parseDateTimePhrase("20121109-1600"), 
					"integration test: testCreate3CalendarItems, item3", "test location", "test ran at " + new Date());

			CreateItem request = new CreateItem();
			request.setSendMeetingInvitations(CalendarItemCreateOrDeleteOperationType.SEND_TO_ALL_AND_SAVE_COPY);

			NonEmptyArrayOfAllItemsType arrayOfItems = new NonEmptyArrayOfAllItemsType();
			arrayOfItems.getItemsAndMessagesAndCalendarItems().add(item1);
			arrayOfItems.getItemsAndMessagesAndCalendarItems().add(item2);
			arrayOfItems.getItemsAndMessagesAndCalendarItems().add(item3);
			request.setItems(arrayOfItems);
			DistinguishedFolderIdType folder = new DistinguishedFolderIdType();
			folder.setId(DistinguishedFolderIdNameType.CALENDAR);
			TargetFolderIdType target = new TargetFolderIdType();
			target.setDistinguishedFolderId(folder);
			request.setSavedItemFolderId(target);

			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			CreateItemResponse response = ewsClient.createItem(request);
			stopWatch.stop();
			log.debug("CreateItem request (3 CalendarItems) completed in " + stopWatch);
			Assert.assertNotNull(response);
			ArrayOfResponseMessagesType responseMessages = response.getResponseMessages();
			Assert.assertNotNull(responseMessages);
			Assert.assertEquals(3, responseMessages.getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages().size());
			for(JAXBElement<? extends ResponseMessageType> m : responseMessages.getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages()) {
				Assert.assertEquals(ResponseCodeType.NO_ERROR, m.getValue().getResponseCode());

				ItemInfoResponseMessageType itemType = (ItemInfoResponseMessageType) m.getValue();
				ArrayOfRealItemsType itemArray = itemType.getItems();
				ItemType item = itemArray.getItemsAndMessagesAndCalendarItems().get(0);
				createdIds.getItemIdsAndOccurrenceItemIdsAndRecurringMasterItemIds().add(item.getItemId());
			}
		} finally {
			deleteItems(createdIds);
		}
	}

	/**
	 * Utility method to issue {@link ExchangeWebServices#deleteItem(DeleteItem)} on the 
	 * {@link NonEmptyArrayOfBaseItemIdsType} argument.
	 * Skips the call if the argument is empty
	 * @param itemIds
	 */
	public void deleteItems(NonEmptyArrayOfBaseItemIdsType itemIds) {
		if(itemIds != null && !itemIds.getItemIdsAndOccurrenceItemIdsAndRecurringMasterItemIds().isEmpty()) {
			DeleteItem request = new DeleteItem();
			request.setSendMeetingCancellations(CalendarItemCreateOrDeleteOperationType.SEND_TO_NONE);
			request.setDeleteType(DisposalType.HARD_DELETE);
			request.setItemIds(itemIds);
			DeleteItemResponse response = ewsClient.deleteItem(request);
			log.info("submitted DeleteItem request for " + itemIds);
			ArrayOfResponseMessagesType responseMessages = response.getResponseMessages();
			for(JAXBElement<? extends ResponseMessageType> m : responseMessages.getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages()) {
				if(!ResponseCodeType.NO_ERROR.equals(m.getValue().getResponseCode())) {
					String capture = capture(response);
					log.error("suspected failure detected for DeleteItem: " + capture);
					break;
				}
			}
		}
	}

	/**
	 * Marshal the JAXB element and return the output as a {@link String}.
	 * 
	 * @param jaxbElement
	 * @return
	 */
	protected String capture(Object jaxbElement) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(jaxbElement, stream);
			stream.close();
			return stream.toString();
		} catch (JAXBException e) {
			log.error("failed to marshal response " + jaxbElement, e);
			throw new IllegalStateException();
		} catch (IOException e) {
			log.error("unexpected IOException for " + jaxbElement, e);
			throw new IllegalStateException();
		}

	}
	/**
	 * 
	 * @param startTime
	 * @param endTime
	 * @param subject
	 * @param location
	 * @param bodyText
	 * @return
	 */
	protected CalendarItemType constructCalendarItem(Date startTime, Date endTime, String subject, String location, String bodyText) {
		CalendarItemType calendarItem = new CalendarItemType();
		calendarItem.setStart(DateHelp.convertDateToXMLGregorianCalendar(startTime));
		calendarItem.setEnd(DateHelp.convertDateToXMLGregorianCalendar(endTime));
		calendarItem.setSubject(subject);
		calendarItem.setLocation(location);
		BodyType body = new BodyType();
		body.setBodyType(BodyTypeType.TEXT);
		body.setValue(bodyText);
		calendarItem.setBody(body);
		return calendarItem;
	}

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
		// see http://msdn.microsoft.com/en-us/library/exchange/aa565898%28v=exchg.140%29.aspx for description of acceptable values
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

	/**
	 * Construct a {@link FindItem} request object for the specified email address and bounded by
	 * 
	 * @param startTime
	 * @param endTime
	 * @param emailAddress
	 * @return
	 */
	protected FindItem constructFindItemRequest(Date startTime, Date endTime, String emailAddress) {
		FindItem findItem = new FindItem();

		CalendarViewType calendarView = new CalendarViewType();
		calendarView.setStartDate(DateHelp.convertDateToXMLGregorianCalendar(startTime));
		calendarView.setEndDate(DateHelp.convertDateToXMLGregorianCalendar(endTime));
		calendarView.setMaxEntriesReturned(1000);
		findItem.setCalendarView(calendarView);
		findItem.setTraversal(ItemQueryTraversalType.SHALLOW);
		ItemResponseShapeType responseShape = new ItemResponseShapeType();
		responseShape.setBaseShape(DefaultShapeNamesType.DEFAULT);
		findItem.setItemShape(responseShape);
		NonEmptyArrayOfBaseFolderIdsType array = new NonEmptyArrayOfBaseFolderIdsType();
		DistinguishedFolderIdType folderId = new DistinguishedFolderIdType();
		EmailAddressType emailAddressType = new EmailAddressType();
		emailAddressType.setEmailAddress(emailAddress);

		folderId.setId(DistinguishedFolderIdNameType.CALENDAR);
		folderId.setMailbox(emailAddressType);
		array.getFolderIdsAndDistinguishedFolderIds().add(folderId);
		findItem.setParentFolderIds(array);
		return findItem;
	}
}
