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
package com.fatminds.vaadin_cmis_integration.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.client.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.dellroad.stuff.vaadin.ContextApplication;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.util.StringUtils;

import com.fatminds.vaadin.cmis.CmisContainer;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Window.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configurable(preConstruction = true)
public class UploadReceiver implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver{

	private final Logger log = LoggerFactory.getLogger(UploadReceiver.class);
	
    @Autowired
    protected Session cmisSession;
	
    /**
     * Where uploads are deposited
     */
	protected Folder uploadRoot;
	
	/**
	 * Upload state
	 */
	protected ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	protected Map<String, Object> props = new HashMap<String, Object>();
	protected String filename;
	protected String mimeType;
	
	/**
	 * Reference to containing application (for {@link ContextApplication#invoke})
	 */
	protected ContextApplication theApp;
	
	
	/**
	 * The cmis type id and (if applicable), comma-separated aspects to apply
	 */
	protected String cmisTypeAndAspects;
	
	/**
	 * refresh() this container after upload (if not null)
	 */
	protected CmisContainer<?> uploadContainer=null;
	
	/**
	 * 
	 * @param uploadRoot - generally, fatminds_catalog_root. UploadReceiver will create uploadRoot/uploads/incoming folder if 
	 * it does not exist. 
	 * -- Note, at least in this example, your CMIS type needs to include ',P:cm:titled', because this UploadReceiver will attempt
	 * to set the cm:title property (to the filename, same as the cmis:name) on creation. 
	 */
	public UploadReceiver(String cmisTypeAndAspects, Folder uploadRoot, CmisContainer<?> uploadContainer, ContextApplication theApp) {
		if (!StringUtils.hasText(cmisTypeAndAspects)) {
			throw new IllegalArgumentException("cmisTypeAndAspects cannot be null");
		}
		this.cmisTypeAndAspects = cmisTypeAndAspects;
		if (null == uploadRoot) {
			throw new IllegalArgumentException("uploadRoot must exist, your user user must have permission to write to it, and it cannot be null");
		}
		this.uploadRoot = uploadRoot;
		if (null != uploadContainer) { // optional
			this.uploadContainer = uploadContainer;
		}
		this.uploadContainer = uploadContainer;
		this.theApp = theApp;
		init();
	}

	public void init() {
		// Do something
	}
	
	public Folder getIncomingFolder() {
		return uploadRoot;
	}
	
	/**
	 * Initialize for receipt of completed upload. Note, this thing isn't threadsafe, so make sure there is one and only one
	 * thread accessing it from the time that receiveUpload is called until the resulting success or failure is called. 
	 * 
	 * TODO: parameterize custom properties that are set on newly created documents.
	 * 
	 */
	public OutputStream receiveUpload(String filename, String mimeType) {
		this.filename = filename;
		this.mimeType = mimeType;
		props.put("cmis:name", filename);
		props.put("cmiscustom:docprop_string", filename);
		ArrayList<Boolean> bools = new ArrayList<Boolean>(1);
		bools.add(Boolean.valueOf(true));
		props.put("cmiscustom:docprop_boolean_multi", bools);
    	props.put(PropertyIds.OBJECT_TYPE_ID, this.cmisTypeAndAspects);
    	log.info("Created buffer for " + filename);
    	return buffer;
	}

	public void uploadFailed(final FailedEvent event) {
		log.warn("Upload of " + event.getFilename() + " failed because: " + event.getReason());
		if (null != theApp){
			theApp.invoke(new Runnable() {
				
				@Override
				public void run() {
					SpringContextApplication.get().getMainWindow().showNotification("Upload failed: " + event.getReason(), Notification.TYPE_WARNING_MESSAGE);
				}
			});
		}
		buffer.reset();
	}

	public void uploadSucceeded(SucceededEvent event) {
		log.info("Upload to catalog manager succeeded, passing along to alfresco...");

		List<Ace> addAces = new LinkedList<Ace>();
		List<Ace> removeAces = new LinkedList<Ace>();
		List<Policy> policies = new LinkedList<Policy>();
		Document newDoc;
		try {
			ContentStream contentStream = 
					new ContentStreamImpl(
							filename, 
							null, 
							mimeType,
							new ByteArrayInputStream(buffer.toByteArray()));

			
			try {
				newDoc = uploadRoot.createDocument(props, 
										contentStream, 
										VersioningState.MAJOR, 
										policies, 
										removeAces, 
										addAces, 
										cmisSession.getDefaultContext());
			}
			catch (CmisConstraintException e1) {
				// File exists. 
				String path = uploadRoot.getPath();
				path = path + "/" + (String)props.get("cmis:name");
				log.info("Updating existing document " + path);
				newDoc = (Document)cmisSession.getObjectByPath(path);
				newDoc.setContentStream(contentStream, true);
				newDoc.refresh();
			}
			if (null == newDoc) {
				log.error("Something went wrong... uploaded document is null");
				return;
			}
			//ExcelRecordImporter importer = new ExcelRecordImporter();
			//importer.importDocument(newDoc);
		}
		catch (Exception e) {
			log.error("Something bad happened when I tried to upload a content stream", e);
			return;
		}
		if (null != this.uploadContainer) {
			this.uploadContainer.refresh();
		}
		if (null != theApp){
			theApp.invoke(new Runnable() {
				
				@Override
				public void run() {
					SpringContextApplication.get().getMainWindow()
						.showNotification("Upload succeeded", Notification.TYPE_TRAY_NOTIFICATION);
				}
			});
		}
		log.info("Upload to alfresco succeeded, document ID " + newDoc.getId() );
		buffer.reset();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
