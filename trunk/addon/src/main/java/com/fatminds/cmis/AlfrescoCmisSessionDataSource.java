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

	public enum AlfrescoMajorVersion {
		ALFRESCO_MAJOR_VERSION_3,
		ALFRESCO_MAJOR_VERSION_4
		// Note: Edit setAlfrescoMajorVersion(String version) if you
		// add more values here.
	};
	
	private static final Logger log = LoggerFactory.getLogger(AlfrescoCmisSessionDataSource.class);
	
    // OpenCMIS session factory
    private static final SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
	
    // Bean-settable
	protected String protocol;
	protected String hostname;
	protected int port;
	protected String username;
	protected String password;
	// Default to alf 3.x
	//protected AlfrescoMajorVersion alfrescoMajorVersion = AlfrescoMajorVersion.ALFRESCO_MAJOR_VERSION_3;
	protected AlfrescoMajorVersion alfrescoMajorVersion;
	
	// Defaults
	// TODO: Expose this to spring configuration
	protected String bindingType = BindingType.WEBSERVICES.value();
	protected String objectFactoryClass = "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl";
	
	public AlfrescoCmisSessionDataSource() {
		
	}
	/**
	 * Sets alfrescoMajorVersion to ALFRESCO_MAJOR_VERSION_3
	 * @param protocol
	 * @param hostname
	 * @param port
	 * @param username
	 * @param password
	 */
	public AlfrescoCmisSessionDataSource(String protocol, String hostname, int port, String username, String password) {
		this.protocol 	= protocol;
		this.hostname 	= hostname;
		this.port 		= port;
		this.username 	= username;
		this.password 	= password;
	}
	public AlfrescoCmisSessionDataSource(
			String protocol, 
			String hostname, 
			int port, 
			String username, 
			String password, 
			AlfrescoMajorVersion alfrescoMajorVersion) {
		this.protocol 	= protocol;
		this.hostname 	= hostname;
		this.port 		= port;
		this.username 	= username;
		this.password 	= password;
		this.alfrescoMajorVersion = alfrescoMajorVersion;
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
		
		// Select the version of Alfresco in use
		String url;
		if (this.alfrescoMajorVersion.equals(AlfrescoMajorVersion.ALFRESCO_MAJOR_VERSION_3)){
			url = protocol + "://" + hostname + ":" + port + "/alfresco/cmis";			
		}
		else {
			url = protocol + "://" + hostname + ":" + port + "/alfresco/cmisws";						
		}
		
        // create session parameters
		Map<String, String> sessionParameters = new HashMap<String, String>();
		//sessionParameters.put(SessionParameter.ATOMPUB_URL, 			url);
		sessionParameters.put(SessionParameter.BINDING_TYPE, 			bindingType);
		sessionParameters.put(SessionParameter.USER, 					username);
		sessionParameters.put(SessionParameter.PASSWORD, 				password);
		sessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS, 	objectFactoryClass);
        //sessionParameters.put(SessionParameter.SESSION_TYPE, "persistent");
		sessionParameters.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, url + "/RepositoryService");
		sessionParameters.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, url + "/NavigationService");
		sessionParameters.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, url + "/ObjectService");
		sessionParameters.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, url + "/VersioningService");
		sessionParameters.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, url + "/DiscoveryService");
		sessionParameters.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, url + "/RelationshipService");
		sessionParameters.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, url + "/MultiFilingService");
		sessionParameters.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, url + "/PolicyService");
		sessionParameters.put(SessionParameter.WEBSERVICES_ACL_SERVICE, url + "/ACLService");		
				
		log.debug("Connecting to Alfresco version " + this.alfrescoMajorVersion + " with bindingtype "+ bindingType + " at root " + url );
		List<Repository> repositories = sessionFactory.getRepositories(sessionParameters);
        sessionParameters.put(SessionParameter.REPOSITORY_ID, repositories.get(0).getId());
        return sessionFactory.createSession(sessionParameters);
	}

	public static void main(String[] args){
		if (args.length != 5){
			System.err.println("Usage: maincmd proto host port user pass");
			return;
		}
		AlfrescoCmisSessionDataSource ds = new AlfrescoCmisSessionDataSource();
		ds.setProtocol(args[0]);
		ds.setHostname(args[1]);
		ds.setPort(Integer.valueOf(args[2]).intValue());
		ds.setUsername(args[3]);
		ds.setPassword(args[4]);
		ds.getSession();
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
	public AlfrescoMajorVersion getAlfrescoMajorVersion() {
		return alfrescoMajorVersion;
	}
	public void setAlfrescoMajorVersion(AlfrescoMajorVersion alfrescoMajorVersion) {
		this.alfrescoMajorVersion = alfrescoMajorVersion;
	}




















}
