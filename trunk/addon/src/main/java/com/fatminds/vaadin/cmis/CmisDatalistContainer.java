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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;


import com.fatminds.cmis.AlfrescoCmisHelper;
import com.vaadin.data.Item;

@SuppressWarnings("serial")
@Configurable
public class CmisDatalistContainer extends CmisContainer<Document> {

	protected static final Logger log = LoggerFactory.getLogger(CmisDatalistContainer.class);
	
	
	public static final String DL_VALUE_PROPERTY = "fmbase:listvalue";
	public static final String DL_LISTS_FOLDER_NAME = "lists";
	protected final Object propertyId; // The name of the property our datalist items will pretend to have
	protected Map<String, CmisItem> propertyIdToItem = new HashMap<String, CmisItem>();
	protected Folder listsRootFolder;
	
	/**
	 * @return the propertyIdToItem
	 */
	protected Map<String, CmisItem> getPropertyIdToItem() {
		return propertyIdToItem;
	}

	/**
	 * 
	 * @param catalogService
	 * @param cmisType
	 * @param listsRootFolder -- NOTE: this is NOT the rootFolder that is passed to the constructor - it is the folder
	 * within which can be found the dl:dataList DL_LISTS_FOLDER_NAME/listPath, which may or may not already exist. If it does
	 * not exist, that structure is created and the resulting Folder node is passed to the constructor.
	 * @param propertyId
	 * @param listPath
	 * @return
	 */
	public static CmisDatalistContainer getInstance(Session session, String cmisType, Folder listsRootFolder, Object propertyId) {
		if (null == listsRootFolder)
			throw new RuntimeException("listsRootFolder cannot be null");
		if (null == propertyId) 
			throw new RuntimeException("propertyId cannot be null");
		ObjectType td = listsRootFolder.getType();
		if (td.getId().equals("F:dl:dataList")){
			throw new RuntimeException("listsRootFolder cannot be a dl:dataList (one will be built under listsRootFolder)");
		}
		String listsPath = listsRootFolder.getPath() + "/" + DL_LISTS_FOLDER_NAME;
		Folder f;
		try {
			f = (Folder) session.getObjectByPath(listsPath);
		}
		catch (CmisObjectNotFoundException e) {
			log.info("Creating cmis:folder " + listsPath);
			Map<String, String> props = new HashMap<String, String>();
			props.put("cmis:name", DL_LISTS_FOLDER_NAME);
	    	props.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			f = listsRootFolder.createFolder(props);
			log.info("Created cmis:folder " + f.getName() + " under parent folder " + listsRootFolder.getName());
		}
		String rootPath = listsPath + "/" + AlfrescoCmisHelper.alfModelToApiClassname(propertyId.toString());
		Folder root;
		try {
			root = (Folder) session.getObjectByPath(rootPath);
		}
		catch (CmisObjectNotFoundException e) {
			log.info("Creating F:dl:dataList " + rootPath);
			Map<String, String> props = new HashMap<String, String>();
			props.put("cmis:name", AlfrescoCmisHelper.alfModelToApiClassname(propertyId.toString()));
	    	props.put(PropertyIds.OBJECT_TYPE_ID, "F:fmbase:simplelist");
			root = f.createFolder(props);
			log.info("Created dl:dataList " + root.getName() + " under parent folder " + f.getName());			
		}
		if (null == root)
			throw new RuntimeException("list root folder " + rootPath + " could not be retrived from repository");
		CmisDatalistContainer cdl = new CmisDatalistContainer(cmisType, root, propertyId);
		cdl.initContainerProperties();
		cdl.populate(10, 0, null);
		return cdl;
	}
	
	protected CmisDatalistContainer(String cmisType, Folder rootFolder, Object propertyId) {
		super(cmisType, rootFolder, Document.class, 0 /** paging off **/, "fmbase:listvalue ASC"); // do not preload, need to set this.propertyId first
		if (null == propertyId)
			throw new RuntimeException("propertyId cannot be null");
		this.propertyId = propertyId;
	}
	
	/**
	 * Find all distinct values for the property associated with the lookup list managed by this container, and populate the list with that
	 * set of values.
	 * @param clearListFirst -- if true, remove all list values before populating.
	 * @param typeCandidates -- List of candidate base types to scan for the presence of the (Alfresco) CMIS type
	 * that owns the specified property. If the field is found to exist in any of the candidate types, the type hierarchy
	 * is recursed until the type that DEFINES the property is found. It is this (base) type which is returned, such that 
	 * all values of propertyId managed in this CMIS repository can be queried using the type returned. 
	 */
	public void populateListFromCatalogPropertyData(boolean clearListFirst, String[] typeCandidates) {
		ObjectType ot = AlfrescoCmisHelper.findPropertyOwnerClass(session, (String)propertyId, typeCandidates);
		if (null == ot) {
			throw new RuntimeException("Cannot find definition of property owner class for " + propertyId);
		}
		if (clearListFirst) {
			clearList();
		}
		StringBuffer querySb = new StringBuffer().append("SELECT ")
				.append(this.propertyId)
				.append(" FROM ")
				.append(ot.getQueryName())
				.append(" WHERE IN_TREE('")
				.append(this.rootFolder.getParents().get(0).getId()) // We want the parent one level above container's root (DL_LISTS_FOLDER_NAME)
				.append("')");
		// ORDER BY is not a good idea here, as not all properties that may have choice lists are
		// necessarily able to be sorted by alfresco (depends on indexer settings for the property definition).
		// Order in code instead.
		// ORDER BY ")
//				.append(this.propertyId)
//				.append(" ASC");
		String query = querySb.toString();

		log.debug("**************Query: " + query);
		OperationContext opCxt = session.createOperationContext();
		ItemIterable<QueryResult> results = session.query(query, false, opCxt); // false = search only latest versions
		log.debug("Loading " + results.getTotalNumItems() + " results ");
		Set<String> values = new HashSet<String>();
		for (QueryResult qResult : results) {
			String objectId = qResult.getPropertyValueByQueryName(objectIdQName);
			if (null == objectId || objectId.length() < 1) {
				continue;
			}
			values.add(objectId);
		}
		List<String> sortedList = new Vector<String>(values);
		Collections.sort(sortedList);
		for (String objectId: sortedList) {
			if (null == this.getItem(objectId)) {
				
			}
			addItem(objectId, false);
			log.trace("Added " + objectId);			
		}
	}

	public void clearList() {
		for (Object id : this.getItemIds()) {
			CmisDatalistItem ci = (CmisDatalistItem) this.getItem(id);
			removeItem(ci);
		}
	}
	
    @Override
    public void populate(int pagesize, int startindex, String orderBy) {
    	super.populate(pagesize, startindex, orderBy);
    	log.debug("Mapping property values from " + propertyId + " to items");
    	Collection<?> c = super.getItemIds();
    	for (Object o : c) {
    		CmisItem i = super.getItem(o);
    		CmisProperty id = i.getCmisProperty(propertyId);
    		if (null == id) {
    			continue;
    		}
    		log.trace("Adding property value-ID " + id);
    		getPropertyIdToItem().put((String)id.getValue(), i);
    	}
    };

    @Override
    public boolean containsId(Object id) {
    	/** all of CmisContainer's machinery assumes that containsId() searches the underlying container's 
    		CMIS id collection, but @see <code>AbstractSelect.setValue</code> wants items.containsId to respond
    		to the notion of itemId used in TokenField (which is the (String) button/token value). 
    		Therefore, respond to both possibilities (either CMIS id or String token ID) here.
    	**/
    	if (super.containsId(id))
    		return true;
    	return getItemIds().contains(id);
    }
    
    @Override
	protected CmisItem getUnfilteredItem(Object itemId) {
    	if (null == itemId) {
    		throw new IllegalArgumentException("itemId cannot be null");
    	}
    	if (! (itemId instanceof String)) {
    		throw new IllegalArgumentException("itemId must be a String");
    	}
		if (null != getPropertyIdToItem().get(itemId)) {
			return getPropertyIdToItem().get(itemId);
		}
		CmisDatalistItem cdi;
		try {
			// Try retrieving from parent (falls back to Cmis)
			cdi = (CmisDatalistItem) super.getUnfilteredItem(itemId);
			if (null == cdi) {
				return null;
			}
		}
		catch (CmisInvalidArgumentException e) {
			// not found
			return null;
		}
		// register in propertyIdToItem map
		//getPropertyIdToItem().put((String)cdi.getItemProperty(this.propertyId).getValue(), cdi);
		return cdi;
	}

	@Override
	public Item addItem(Object itemId) {
		return addItem(itemId, true);
	}    
	
	/**
	 * @param itemId 
	 */
	public Item addItem(Object itemId, boolean fireItemSetChange) {
		if (null == itemId) {
			return null;
		}
		String sItemId = (itemId instanceof ObjectId) ? ((ObjectId) itemId).getId() : (String)itemId;
		if (getPropertyIdToItem().containsKey(itemId)) {
			return getPropertyIdToItem().get(itemId);
		}
		if (super.getItemIds().contains(itemId)) {
			return getItem(itemId);
		}
		
		CmisDatalistItem item = new CmisDatalistItem(this, this.propertyId, DL_VALUE_PROPERTY); // transient mode
		// Set essential properties. We set the aliased property name, CmisDatalistItem handles the conversion to 
		// DL_VALUE_PROPERTY
		item.getCmisProperty(this.propertyId).setValue(itemId);
		item.persist(AlfrescoCmisHelper.sanitizeForCmisName(itemId.toString()));
		addItem(item); // Add to internal list. Cannot be called before persist(). 
		getPropertyIdToItem().put(sItemId, item); // Add to propertyId-based map
		if (fireItemSetChange) {
			fireItemSetChange();
		}
		return item;
	}

	
    public void addItem(CmisItem item)
            throws IllegalStateException {
        boolean modified = false;
        if (item == null) {
        	return;
        }
        String itemId = item.getCmisObject().getId();
        if (internalAddItemAtEnd(itemId, item, false) != null) {
                modified = true;
        }
        if (modified) {
            // Filter the contents when all items have been added
            if (isFiltered()) {
                filterAll();
            } else {
                fireItemSetChange();
            }
        }
    }

	
	@Override
	protected CmisItem createCmisItem(CmisObject object) {
		return null == object ? null : new CmisDatalistItem(this, object, propertyId, DL_VALUE_PROPERTY);
	}

	@Override
	protected CmisItem createLazyCmisItem(String itemId) {		
		return (itemId == null) ? null : new CmisDatalistItem(this, itemId, propertyId, DL_VALUE_PROPERTY);
	}
	
    @Override
    public Class<?> getType(Object propertyId) {
    	if (null == propertyId)
    		return null;
    	if (propertyId.equals(this.propertyId)){
    		return super.getType(DL_VALUE_PROPERTY);
    	}
    	return super.getType(propertyId);
    }
    
    @Override
    public PropertyDefinition getPropertyDefinition(Object id) {
    	if (null == id)
    		return null;
    	if (id.equals(propertyId)) {
    		return super.getPropertyDefinition(DL_VALUE_PROPERTY);
    	}
    	return super.getPropertyDefinition(id);
    }
    
    @Override
    public Collection<String> getContainerPropertyIds() {
    	Collection<String> ret = new Vector<String>();
    	ret.addAll(super.getContainerPropertyIds());
    	ret.add((String)propertyId);
    	return ret;
    }
    
//    @Override
//    public boolean containsId(Object itemId) {
//        // only look at visible items after filtering
//        if (itemId == null) {
//            return false;
//        } else {
//            return getVisibleItemIds().contains(itemId) ? true : ;
//        }
//   }

    
    @Override
    public List<String> getItemIds() {
    	List<String> allIds = new Vector<String>();
        for (Object o : getPropertyIdToItem().keySet()) {
        	String s  = (o instanceof String) ? (String)o : o.toString();
        	allIds.add(s);
        }
        //allIds.addAll(super.getAllItemIds());
        return allIds;
    }

    
    @Override
    public CmisItem getItem(Object itemId) {
    	if (null != getPropertyIdToItem().get(itemId)) {
    		return getPropertyIdToItem().get(itemId);
    	}
    	return super.getItem(itemId);
    }
    
    
    
    @Override
    public boolean removeItem(Object itemId){
    	if (itemId instanceof CmisItem) {
    		itemId = ((CmisDatalistItem)itemId).getItemId();
    	}
    	CmisDatalistItem item=  (CmisDatalistItem) super.getItem(itemId);
		getPropertyIdToItem().remove(item.getCmisProperty("fmbase:listvalue").getValue());
		return super.removeItem(itemId);
    }
 	
}
