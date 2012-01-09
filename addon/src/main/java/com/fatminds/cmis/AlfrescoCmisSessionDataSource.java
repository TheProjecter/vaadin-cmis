/*******************************************************************************
 * Copyright 2012 Fatminds, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.fatminds.cmis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author aaronlee
 *
 */
public class AlfrescoCmisSessionDataSource {

	private static final Logger log = LoggerFactory.getLogger(AlfrescoCmisSessionDataSource.class);
	
    // OpenCMIS session factory
    private static final SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
	
    // Bean-settable
	protected String protocol;
	protected String hostname;
	protected int port;
	protected String username;
	protected String password;
	
	// Defaults
	// TODO: Expose this to spring configuration
	protected String bindingType = BindingType.ATOMPUB.value();
	protected String objectFactoryClass = "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl";
	
	public AlfrescoCmisSessionDataSource() {
		
	}
	
	public AlfrescoCmisSessionDataSource(String protocol, String hostname, int port, String username, String password) {
		this.protocol 	= protocol;
		this.hostname 	= hostname;
		this.port 		= port;
		this.username 	= username;
		this.password 	= password;
	}
	
	public Session getSession() {
		if (null == protocol || null == hostname || 0 == port || null == username || null == password) {
			throw new IllegalArgumentException(
					"Must set all parameters - protocol [" + protocol +
					"], hostname [" + hostname + 
					"], port [" + port +
					"], username [" + username +
					"], password [" + password + "]");
		}
		
		String url = protocol + "://" + hostname + ":" + port + "/alfresco/service/cmis";
		
        // create session parameters
		Map<String, String> sessionParameters = new HashMap<String, String>();
		sessionParameters.put(SessionParameter.ATOMPUB_URL, 			url);
		sessionParameters.put(SessionParameter.BINDING_TYPE, 			bindingType);
		sessionParameters.put(SessionParameter.USER, 					username);
		sessionParameters.put(SessionParameter.PASSWORD, 				password);
		sessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS, 	objectFactoryClass);
        //sessionParameters.put(SessionParameter.SESSION_TYPE, "persistent");
		
		log.debug("Connecting to " + bindingType + " endpoint " + url );
		List<Repository> repositories = sessionFactory.getRepositories(sessionParameters);
        sessionParameters.put(SessionParameter.REPOSITORY_ID, repositories.get(0).getId());
        return sessionFactory.createSession(sessionParameters);
	}
	
	
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}
	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
