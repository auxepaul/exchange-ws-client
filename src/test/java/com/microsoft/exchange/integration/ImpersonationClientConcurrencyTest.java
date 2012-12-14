/**
 * 
 */
package com.microsoft.exchange.integration;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.math.stat.descriptive.SynchronizedSummaryStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.microsoft.exchange.DateHelper;
import com.microsoft.exchange.impl.ThreadLocalImpersonationConnectingSIDSourceImpl;
import com.microsoft.exchange.messages.FindItem;
import com.microsoft.exchange.messages.FindItemResponse;
import com.microsoft.exchange.types.ConnectingSIDType;

/**
 * Perform some tests targeted at observing throttling policy and other issues
 * when using a number of concurrent connections configured with impersonation support.
 * 
 * @author Nicholas Blair
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/com/microsoft/exchange/exchangeContext-usingImpersonation.xml")
public class ImpersonationClientConcurrencyTest extends AbstractIntegrationTest {

	private String emailAddress = "npblair@office-test.doit.wisc.edu";
	private String startDate = "2012-10-11";
	private String endDate = "2012-10-12";

	private int targetConcurrency;
	/**
	 * @return the targetConcurrency
	 */
	public int getTargetConcurrency() {
		return targetConcurrency;
	}
	/**
	 * @param targetConcurrency the targetConcurrency to set
	 */
	@Value("${http.maxTotalConnections}")
	public void setTargetConcurrency(int targetConcurrency) {
		this.targetConcurrency = targetConcurrency;
	}

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
	 * 
	 * @throws InterruptedException 
	 */
	@Test
	public void testConcurrentFindItems() throws InterruptedException {
		final int threadCount = targetConcurrency;
		// setup a latch to stall all threads until ready to run all at once (-1 so the last thread's run invocation triggers the start)
		final CountDownLatch startLatch = new CountDownLatch(threadCount);
		final CountDownLatch endLatch = new CountDownLatch(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		final Date start = DateHelper.makeDate(startDate);
		final Date end = DateHelper.makeDate(endDate);
		final SynchronizedSummaryStatistics stats = new SynchronizedSummaryStatistics();
		try {
			for(int i = 0; i < threadCount; i++) {
				final int index = i;
				executor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							initializeCredentials();
							FindItem request = constructFindItemRequest(DateUtils.addDays(start, index), DateUtils.addDays(end, index), emailAddress);
							startLatch.countDown();
							try {
								startLatch.await();
							} catch (InterruptedException e) {
								throw new IllegalStateException("interrupted while waiting to start", e);
							}
							for(int j = 0; j < 10; j++) {
								StopWatch time = new StopWatch();
								time.start();
								FindItemResponse response = ewsClient.findItem(request);
								time.stop();
								String capture = capture(response);
								if(log.isTraceEnabled()) {
									log.trace(Thread.currentThread().getName() + " response: " + capture);
								}
								stats.addValue(time.getTime());
							}
						} finally {
							endLatch.countDown();
						}
					}
				});
			}
			// now block until everybody is done
			endLatch.await();
			log.info("testConcurrentFindItems complete for " + targetConcurrency + " threads, stats: " + stats);
		} finally {
			executor.shutdown();
		}

	}
}
