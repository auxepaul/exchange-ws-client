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

package com.microsoft.exchange.impl.http;

import java.net.URI;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

/**
 * @author Nicholas Blair
 */
public class CustomHttpComponentsMessageSender extends
		HttpComponentsMessageSender {

	private boolean preemptiveAuthEnabled = false;
	private AuthScope preemptiveAuthScope = AuthScope.ANY;
	private CredentialsProviderFactory credentialsProviderFactory = new ThreadLocalCredentialsProviderFactory();
	// preemptiveAuthScheme set by #afterPropertiesSet if enabled
	private AuthScheme preemptiveAuthScheme;
	/**
	 * @return the preemptiveAuthEnabled
	 */
	public boolean isPreemptiveAuthEnabled() {
		return preemptiveAuthEnabled;
	}
	/**
	 * @param preemptiveAuthEnabled the preemptiveAuthEnabled to set
	 */
	public void setPreemptiveAuthEnabled(boolean preemptiveAuthEnabled) {
		this.preemptiveAuthEnabled = preemptiveAuthEnabled;
	}
	/**
	 * @return the preemptiveAuthScope
	 */
	public AuthScope getPreemptiveAuthScope() {
		return preemptiveAuthScope;
	}
	/**
	 * @param preemptiveAuthScope the preemptiveAuthScope to set
	 */
	public void setPreemptiveAuthScope(AuthScope preemptiveAuthScope) {
		this.preemptiveAuthScope = preemptiveAuthScope;
	}
	/**
	 * @return the credentialsProviderFactory
	 */
	public CredentialsProviderFactory getCredentialsProviderFactory() {
		return credentialsProviderFactory;
	}
	/**
	 * @param credentialsProviderFactory the credentialsProviderFactory to set
	 */
	public void setCredentialsProviderFactory(
			CredentialsProviderFactory credentialsProviderFactory) {
		this.credentialsProviderFactory = credentialsProviderFactory;
	}
	/* (non-Javadoc)
	 * @see org.springframework.ws.transport.http.HttpComponentsMessageSender#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if(isPreemptiveAuthEnabled()) {
			this.preemptiveAuthScheme = identifyScheme(getPreemptiveAuthScope().getScheme());
		}
	}
	/**
	 * 
	 * @param scheme
	 * @return
	 */
	protected AuthScheme identifyScheme(String scheme) {
		if(new BasicScheme().getSchemeName().equalsIgnoreCase(scheme)) {
			return new BasicScheme();
		} else if (new DigestScheme().getSchemeName().equalsIgnoreCase(scheme)) {
			return new DigestScheme();
		} else {
			// fallback
			return new BasicScheme();
		}
	}
	/* (non-Javadoc)
	 * @see org.springframework.ws.transport.http.HttpComponentsMessageSender#createContext(java.net.URI)
	 */
	@Override
	protected HttpContext createContext(URI uri) {
		HttpContext context = new BasicHttpContext();
		if(isPreemptiveAuthEnabled()) {
			if(preemptiveAuthScheme == null) {
				throw new IllegalStateException("preemptiveAuth is enabled, but the preemptiveAuthScheme is null. Was afterPropertiesSet invoked?");
			}
			context.setAttribute(PreemptiveAuthInterceptor.PREEMPTIVE_AUTH, preemptiveAuthScheme);
		}
		
		CredentialsProvider credentialsProvider = getCredentialsProviderFactory().getCredentialsProvider(uri);
		context.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider);
		return context;
	}

}
