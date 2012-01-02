package com.fatminds.vaadin.cmis;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.alfresco.cmis.client.AlfrescoFolder;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

public class CmisItem implements Item, Item.PropertySetChangeNotifier {

	private final Log log = LogFactory.getLog(CmisItem.class);
	
	/**
	 * Set at construction time (if CmisObject exists in repository before CmisItem creation), or
	 * at commit() time (if CmisItem is transient)
	 */
	protected CmisObject cmisObject;
	protected Object itemId; // Set and used only by the lazy loader
	// See: CmisItem(CmisContainer container, Object itemId) for more
	protected boolean cmisTransient;
	protected boolean lazyLoaded = true; // By default, assume no initialization is required after construction
	
	protected Map<String, Object> dirtyValuesMap = new HashMap();
	protected CmisContainer container;
	
	public Object getItemId() {
		return this.itemId;
	}
	
	/**
	 * @return the lazyLoaded
	 */
	protected boolean isLazyLoaded() {
		return lazyLoaded;
	}

	/**
	 * @param lazyLoaded the lazyLoaded to set
	 */
	protected void setLazyLoaded(boolean lazyLoaded) {
		this.lazyLoaded = lazyLoaded;
	}

	protected Map<Object, CmisProperty> cmisProperties = new HashMap<Object, CmisProperty>();
	/**
	 * @return the cmisTransient
	 */
	public boolean isCmisTransient() {
		return cmisTransient;
	}

	/**
	 * @return the container
	 */
	public CmisContainer getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	protected void setContainer(CmisContainer container) {
		this.container = container;
	}

	public CmisObject getCmisObject() {
		return this.cmisObject;
	}
	
	/**
	 * 
	 * @param container - the container that will hold this CmisItem
	 * @param cmisObject - The CmisObject to be wrapped by this CmisItem
	 * 
	 * Create a CmisItem in non-transient, fully initialized mode.
	 */
	public CmisItem(CmisContainer container, CmisObject cmisObject) {
		if (null == container) {
			throw new RuntimeException("Container cannot be null");
		}
		if (null == cmisObject) {
			throw new RuntimeException("cmisObject cannot be null if cmisTransient == false and this constructor is used");
		}
		this.container = container;
		this.cmisObject = cmisObject;
		this.cmisTransient=false;
	}
	
	
	/**
	 * 
	 * @param container
	 * 
	 * Create a new container in transient mode.
	 */
	public CmisItem(CmisContainer container) {
		if (null == container) {
			throw new RuntimeException("Container cannot be null");
		}
		this.container = container;
		this.cmisObject = null;
		this.cmisTransient = true; // will create a new CmisObject from dirtyValuesMap on commit
	}
	
	public CmisItem(CmisContainer container, Object itemId) {
		if (null == container) {
			throw new RuntimeException("Container cannot be null");			
		}
		if (null == itemId) {
			throw new RuntimeException("itemId cannot be null");			
		}
		this.container = container;
		this.cmisTransient = false;
		this.lazyLoaded = false;
		this.itemId = itemId;
	}
	
	/**
	 * Protect every public call that retrieves data from the CmisObject (or might cause a Property to do so) with this call
	 * to ensure lazy-loading takes place. 
	 */
	protected void checkInitialized() {
		if (! isLazyLoaded()) {
			if (isCmisTransient()) {
				throw new RuntimeException("Illegal state: cannot be transient and lazy at the same time");
			}
			//log.info("Lazy-loading item " + itemId);
			// Load CmisObject
			CmisObject co = this.getContainer().loadCmisObject(this.itemId.toString());
			if (null == co) {
				throw new RuntimeException("Couldn't retrieve item " + this.itemId);
			}
			setCmisObject(co);
			setLazyLoaded(true);
		}
	}
	
	public Class<?> getType(Object id) {
		//log.info("Getting class for property " + id + ", cmisTransient = " + cmisTransient );
		Class<?> clazz = container.getType(id);
		if (null == clazz) {
			throw new RuntimeException("CmisContainer.getClass("+id+") returned null");
		}
		return clazz;
	}
	
	public Property getItemProperty(Object id) {
		if (null == id)
			return null;
		checkInitialized();
		return getCmisProperty(id);
	}

	
	public Collection<?> getItemPropertyIds() {
		return container.getContainerPropertyIds();
	}

	
	public void addToDirtyValuesMap(String property, Object value){
		checkInitialized();
		//log.info("property " + property + " added to dirty items map");
		dirtyValuesMap.put(property, value);
	}
	
	public Map<String, Object> getDirtyValues(){
		return dirtyValuesMap;
	}

	
	protected CmisProperty getCmisProperty(Object propid) {
		if (propid == null)
			return null;
		checkInitialized();
		if (getCmisProperties().containsKey(propid)) {
			return getCmisProperties().get(propid);
		}
		PropertyDefinition pd = container.getPropertyDefinition(propid); 
		if (null == pd) {
			throw new RuntimeException("Cannot get undefined property " + propid);
		}
		String name = container.getPropertyDefinition(propid).getDisplayName();
		boolean multiple = container.getPropertyDefinition(propid).getCardinality().equals(Cardinality.MULTI);
		boolean mandatory = container.getPropertyDefinition(propid).isRequired();
		getCmisProperties().put(propid, new CmisProperty(this, cmisObject, propid, getType(propid), multiple, name, mandatory));
		return getCmisProperties().get(propid);
	}

	public void addListener(PropertySetChangeListener listener) {
		throw new UnsupportedOperationException();
	}

	public void removeListener(PropertySetChangeListener listener) {
		throw new UnsupportedOperationException();
		
	}

	public boolean addItemProperty(Object id, Property property)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public boolean removeItemProperty(Object id)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @param cmisName - Provide a valid (properly escaped) cmis:name if this object is transient. cmisName is ignored if this item's
	 * cmisObject already exists in the repository. 
	 */
	public void persist(String cmisName) {
		checkInitialized();
		// Create or update the item
		if (this.isCmisTransient()) {
			if (null == cmisName) {
				throw new IllegalArgumentException("cmisName cannot be null when persist() is called on a transient item");
			}
			addToDirtyValuesMap("cmis:name", cmisName);
			this.setCmisObject(this.container.createCmisObject(this));
			this.container.addItem(this);
			// add to container, since this Item was created outside the container
		}
		else {
			this.container.updateObject(this);
		}
		// Update CmisProperty value's
		for (Object id : this.getDirtyValues().keySet()) {
			CmisProperty prop = getCmisProperties().get(id);
			if (null == prop) {
				log.info("Tried to update property not found in cmisitem: " + id);
				continue;
			}
			prop.setCmisObject(getCmisObject());
		}
		
		this.getDirtyValues().clear();
		
	}

	/**
	 * @return the cmisProperties
	 */
	protected Map<Object, CmisProperty> getCmisProperties() {
		return cmisProperties;
	}

	/**
	 * @param cmisProperties the cmisProperties to set
	 */
	protected void setCmisProperties(Map<Object, CmisProperty> cmisProperties) {
		this.cmisProperties = cmisProperties;
	}

	public void setCmisObject(CmisObject obj) {
		if (null == obj) {
			throw new RuntimeException("Can't set null CmisObject on CmisItem");
		}
		this.cmisObject = obj;
		this.cmisTransient = false;
		this.itemId = obj.getId();
	}
	
	
	public String getItemType(){
		return this.container.cmisType;
	}
		
}
