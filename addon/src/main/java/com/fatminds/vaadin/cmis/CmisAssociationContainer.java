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
package com.fatminds.vaadin.cmis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.management.relation.Relation;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.fatminds.cmis.AlfrescoCmisHelper;
import com.fatminds.cmis.AlfrescoCmisSessionDataSource;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;

@Configurable
public class CmisAssociationContainer<CMISTYPE extends CmisObject> extends
		CmisContainer<CMISTYPE> {

	private static final long serialVersionUID = 1L;

	protected final Logger log = LoggerFactory.getLogger(CmisAssociationContainer.class);

	protected final CmisObject source;
	protected final String assocCmisName;
	protected Map<String, CmisItem> targets;
	protected Map<CmisItem, String> associations; // Links target item and cmid:objectId of association, not of item
	protected CmisContainer<?> parent;

	@Autowired
	protected Session session;
	
	public CmisAssociationContainer(CmisContainer parent, CmisObject source, String assocCmisName) {
		super();
		if (null == assocCmisName) {
			throw new IllegalArgumentException("assocCmisName cannot be null");
		}
		if (null == source) {
			throw new IllegalArgumentException("item cannot be null");
		}
		this.targets = new HashMap<String, CmisItem>();
		this.associations = new HashMap<CmisItem, String>();
		this.parent = parent;
		this.source = source;
		this.assocCmisName = assocCmisName;
		
		//TODO: Make this less embarrassing... 
		this.type = parent.type;
		this.cmisTypeAndAspectCSV = parent.cmisTypeAndAspectCSV;
		this.rootFolder = parent.rootFolder;
        this.orderBy = null;
        this.currentStartIndex = 0;
        this.pagesize = 0;
        this.cmisType = parent.cmisType;

	}
	
	@Override
	public java.util.Collection<?> getItemIds() {
		return targets.keySet();
	};
	
	@Override
	public void populate() {
		List<Relationship> relations = source.getRelationships();
		for (Relationship relation : relations) {
			log.debug("Found relation " 
						+ relation.getId() 
						+ " on object " 
						+ relation.getSourceId() 
						+ " which points to target " 
						+ relation.getTargetId());
			if (relation.getType().getId().equals(this.assocCmisName)) {
				String targetId = relation.getTargetId().getId();
				CmisItem item = parent.getItem(targetId);
				if (null == item) {
					log.error("Can't retrieve relationship target " + targetId);
					continue;
				}
				registerAssociation(item, relation.getId());
				super.addItem(item);
				log.debug("Added relation " + relation.getId() + " to collection");
			}
		}
	}
	
	protected void registerAssociation(CmisItem item, String associationId) {
		if (null == item || item.isCmisTransient()) {
			throw new IllegalArgumentException("item cannot be null or transient");
		}
		if (null == associationId) {
			throw new IllegalArgumentException("associationId cannot be null");
		}
		String targetId = (String) item.getItemId();
		if (targets.containsKey(targetId)) {
			throw new IllegalArgumentException("Cannot register existing item in association");
		}
		targets.put(targetId, item);
		associations.put(item, associationId);
		fireItemSetChange();
	}
	
	protected void unregisterAssociation(CmisItem item) {
		if (null == item) {
			throw new IllegalArgumentException("item cannot be null");
		}
		if (!containsId(item.getItemId())) {
			throw new IllegalArgumentException("Cannot unregister association because it is not found in the list of current associations");
		}
		targets.remove(item.getItemId());
		associations.remove(item);
		fireItemSetChange();
	}
	
    public void addItem(CmisItem item) {
    	if (null == item) {
    		return;
    	}
    	// Try to get id from CmisObject, fall back to item.getItemId() to handle lazily-loaded items
    	String itemId = (null != item.getCmisObject() ? item.getCmisObject().getId() : null);
    	if (null == itemId) {
    		throw new IllegalArgumentException("Cannot add transient or lazy items using addItem(CmisItem) - see addLazyItem()");
    	}
    	if (containsId(itemId)) {
    		log.error("Tried to add existing item " + itemId + " to CmisAssociationContainer");
    		return;
    	}
    	
    	Map<String, Object> props = new HashMap<String, Object>();
    	props.put(PropertyIds.SOURCE_ID, source.getId());
    	props.put(PropertyIds.TARGET_ID, itemId);
    	props.put(PropertyIds.OBJECT_TYPE_ID, this.assocCmisName);
    	ObjectId newRelationId = session.createRelationship(props);
    	log.info("Created relationship " + newRelationId);
    	registerAssociation(item, newRelationId.getId());
    	super.addItem(item);
    }
    
    @Override
    public boolean removeItem(Object itemId) {
    	if (!containsId(itemId)) {
    		log.error("Tried to remove non-existent item " + itemId + " from CmisAssociationContainer");
    		return false;
    	}
    	CmisItem item = getItem(itemId);
    	String associationId = associations.get(item);
    	List<Relationship> assocs = source.getRelationships();
    	for (Relationship r : assocs) {
    		if (r.getId().equals(associationId)) {
    			log.info("Removing association " + associationId + " from target node " + item.getItemId());
    			AlfrescoCmisHelper.deleteRelationship(cmisDataSource.getProtocol(), 
    													cmisDataSource.getHostname(), 
    													cmisDataSource.getPort(), 
    													cmisDataSource.getUsername(), 
    													cmisDataSource.getPassword(), 
    													associationId);
    		}
    	}
    	super.removeItem(itemId, false);
    	unregisterAssociation(item);
    	return true;
    }
    
    @Override
    public boolean containsId(Object itemId) {
    	return targets.containsKey(itemId);
    }

	
	@Override
	public Collection getContainerPropertyIds() {
		Collection<String> props = parent.getContainerPropertyIds();
		return props;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		return parent.getContainerProperty(itemId, propertyId);
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return parent.getType(propertyId);
	}

	@Override
	protected CmisItem getUnfilteredItem(Object itemId) {
		return targets.get(itemId);
	}

}
