/**
 * 
 */
package com.microsoft.exchange.impl;

/**
 * This class encapsulates information regarding the throttling policies
 * applicable to Exchange Online.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/exchange/hh881884%28v=exchg.140%29.aspx">http://msdn.microsoft.com/en-us/library/exchange/hh881884%28v=exchg.140%29.aspx</a>
 * @author Nicholas Blair
 */
public class ExchangeOnlineThrottlingPolicy {

	/**
	 * Indicates that there are more concurrent requests against the server than are allowed by a user's policy.
	 */
	public static final String ERROR_EXCEEDED_CONNECTION_COUNT = "ErrorExceededConnectionCount";
	/**
	 * Indicates that a user's throttling policy maximum subscription count has been exceeded.
	 */
	public static final String ERROR_EXCEEDED_SUBSCRIPTION_COUNT = "ErrorExceededSubscriptionCount";
	/**
	 * Indicates that a search operation call has exceeded the total number of items that can be returned.
	 */
	public static final String ERROR_EXCEEDED_FIND_COUNT_LIMIT = "ErrorExceededFindCountLimit";
	/**
	 * Occurs when the server is busy.
	 */
	public static final String ERROR_SERVER_BUSY = "ErrorServerBusy";
	/**
	 * The maximum number of entries returned for FindItem requests.
	 */
	public static final int FIND_ITEM_MAX_ENTRIES_RETURNED = 1000;
	/**
	 * The maximum number of concurrent connections for a service account using impersonation.
	 */
	public static final int MAX_CONCURRENT_CONNECTIONS_IMPERSONATION = 10;
}
