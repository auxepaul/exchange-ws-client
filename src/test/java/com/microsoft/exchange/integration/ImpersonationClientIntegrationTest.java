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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.microsoft.exchange.DateHelper;
import com.microsoft.exchange.impl.ExchangeWebServicesClient;
import com.microsoft.exchange.impl.ThreadLocalImpersonationConnectingSIDSourceImpl;
import com.microsoft.exchange.messages.GetUserAvailabilityRequest;
import com.microsoft.exchange.messages.GetUserAvailabilityResponse;
import com.microsoft.exchange.types.ConnectingSIDType;

/**
 * @author Nicholas Blair
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/com/microsoft/exchange/exchangeContext-usingImpersonation.xml")
public class ImpersonationClientIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private ExchangeWebServicesClient ewsClient;
	
	private String emailAddress = "npblair@office-test.doit.wisc.edu";
	private String startDate = "2012-10-11";
	private String endDate = "2012-10-12";
	private int expectedEventCount = 1;
	
	/**
	 * Issues a {@link GetUserAvailabilityRequest} for the configured emailAddress, startDate and endDate.
	 * Verifies a response, and that the freebusy responses match expectedEventCount.
	 */
	@Test
	public void testGetUserAvailability() {
		ConnectingSIDType connectingSID = new ConnectingSIDType();
		connectingSID.setPrincipalName(emailAddress);
		ThreadLocalImpersonationConnectingSIDSourceImpl.setConnectingSID(connectingSID);
		
		GetUserAvailabilityRequest request = constructAvailabilityRequest(DateHelper.makeDate(startDate), DateHelper.makeDate(endDate), emailAddress);
		GetUserAvailabilityResponse response = ewsClient.getUserAvailability(request);
	
		Assert.assertNotNull(response);
		Assert.assertEquals(expectedEventCount, response.getFreeBusyResponseArray().getFreeBusyResponses().size());
	}
	
}
