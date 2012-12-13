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

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.microsoft.exchange.DateHelper;
import com.microsoft.exchange.impl.ThreadLocalImpersonationConnectingSIDSourceImpl;
import com.microsoft.exchange.messages.FindItem;
import com.microsoft.exchange.messages.FindItemResponse;
import com.microsoft.exchange.messages.GetUserAvailabilityRequest;
import com.microsoft.exchange.messages.GetUserAvailabilityResponse;
import com.microsoft.exchange.types.ConnectingSIDType;

/**
 * Integration test that depends on the Impersonation technique.
 * 
 * @author Nicholas Blair
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/com/microsoft/exchange/exchangeContext-usingImpersonation.xml")
public class ImpersonationClientIntegrationTest extends AbstractIntegrationTest {
	
	private String emailAddress = "npblair@office-test.doit.wisc.edu";
	private String startDate = "2012-10-11";
	private String endDate = "2012-10-12";
	private int expectedEventCount = 1;
	
	/* (non-Javadoc)
	 * @see com.microsoft.exchange.integration.AbstractIntegrationTest#initializeCredentials()
	 */
	@Override
	public void initializeCredentials() {
		ConnectingSIDType connectingSID = new ConnectingSIDType();
		connectingSID.setPrincipalName(emailAddress);
		ThreadLocalImpersonationConnectingSIDSourceImpl.setConnectingSID(connectingSID);
	}
	/**
	 * Issues a {@link GetUserAvailabilityRequest} for the configured emailAddress, startDate and endDate.
	 * Verifies a response, and that the freebusy responses match expectedEventCount.
	 */
	@Test
	public void testGetUserAvailability() {	
		initializeCredentials();
		GetUserAvailabilityRequest request = constructAvailabilityRequest(DateHelper.makeDate(startDate), DateHelper.makeDate(endDate), emailAddress);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		GetUserAvailabilityResponse response = ewsClient.getUserAvailability(request);
		stopWatch.stop();
		log.debug("GetUserAvailability request completed in " + stopWatch);
		Assert.assertNotNull(response);
		Assert.assertEquals(expectedEventCount, response.getFreeBusyResponseArray().getFreeBusyResponses().size());
	}
	/**
	 * Similar to {@link #testGetUserAvailability()}, but uses {@link FindItem}.
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testFindItemCalendarType() throws JAXBException {
		initializeCredentials();
		FindItem request = constructFindItemRequest(DateHelper.makeDate(startDate), DateHelper.makeDate(endDate), emailAddress);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		FindItemResponse response = ewsClient.findItem(request);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(response, System.out);
		stopWatch.stop();
		log.debug("FindItem request completed in " + stopWatch);
		Assert.assertNotNull(response);
		Assert.assertEquals(expectedEventCount, response.getResponseMessages().getCreateItemResponseMessagesAndDeleteItemResponseMessagesAndGetItemResponseMessages().size());
	}
}
