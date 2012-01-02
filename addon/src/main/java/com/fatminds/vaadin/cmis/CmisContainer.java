/**
 * 
 */
package com.fatminds.vaadin.cmis;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.client.AlfrescoFolder;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.fatminds.cmis.AlfrescoCmisHelper;
import com.fatminds.cmis.AlfrescoCmisSessionDataSource;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.filter.UnsupportedFilterException;

/**
 * @author aaronlee
 *
 */
@Configurable
public class CmisContainer<CMISTYPE extends CmisObject> 
	extends 	AbstractInMemoryContainer<String, String, CmisItem>  implements Container.Sortable, Container.Filterable
	{
	
	private static final long serialVersionUID = 1L;

	protected final Logger log = LoggerFactory.getLogger(CmisContainer.class);

  @Autowired
  protected Session session;
  @Autowired
  protected AlfrescoCmisSessionDataSource cmisDataSource; // for AlfrescoCmisHelper statics

  /**
   * Maps all item ids in the container (including filtered) to their
   * corresponding CmisItem.
   */
  protected final LinkedHashMap<String, CmisItem> itemIdToItem = new LinkedHashMap<String, CmisItem>();
  
  /**
   * Maps all items in the container to a Set specific to each type
   */
  protected final HashMap<String, Set<CmisItem>> itemTypeToItemSet = new HashMap<String, Set<CmisItem>>();

  protected Folder rootFolder;
  
	/**
	 * Type qualifier with which CmisContainer was constructed, i.e. cmis:folder or D:myns:mydoctype
	 */
	protected String cmisType;

	/**
	 * Definition of item properties retrieve from CMIS dictionary
	 */
	protected ObjectType cmisObjectType;
	/**
	 * Type string with mandatory aspects applied (F:myns:myfolder,P:myns:myaspect)
	 */
	protected String cmisTypeAndAspectCSV;
	/**
	 * List of mandatory aspect type strings
	 */
	protected Set<String> aspects;
	protected Map<String, PropertyDefinition<?>> cmisPropDefs;
	protected Class<CMISTYPE> type;
	
	// Set if autopopulation/pagination is activated
	protected boolean initialized=false;
	// Pagination state (current page)
	protected int currentStartIndex;
	protected int pagesize;
	protected String orderBy="";
	protected int realSize;
	protected ItemIterable<QueryResult> sourceQueryResult;
	protected String objectIdQName;
	
	public CmisContainer(String cmisType, Folder rootFolder, Class<CMISTYPE> type, int pagesize,String orderBy) {
		super();
		this.type = type;
		this.cmisTypeAndAspectCSV = this.cmisType = cmisType;
		this.rootFolder = rootFolder;
        this.orderBy = orderBy;
        this.currentStartIndex = 0;
        this.pagesize = pagesize;
	}
	
	public CmisContainer() {
		super();
		this.type = null;
		this.cmisTypeAndAspectCSV = null;
		this.rootFolder = null;
        this.orderBy = null;
        this.currentStartIndex = 0;
        this.pagesize = 0;
        this.cmisType = null;
	}

	
	/**
	 * @return the cmisType
	 */
	public String getCmisType() {
		return cmisType;
	}


  @Override
	public void addListener(Container.ItemSetChangeListener listener) {
		super.addListener(listener);
	}
	
	
	/**
	 * @return the rootFolder
	 */
	public Folder getRootFolder() {
		return rootFolder;
	}

	
	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param initialized the initialized to set
	 */
	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	public int getStartIndex(){
		return this.currentStartIndex;
	}

	public int getRealSize(){
		return size();
	}
	
	/**
	 * Call BEFORE initialize(...)
	 * @return - false if initialization fails, true otherwise
	 */
	public boolean initContainerProperties() {
		
		AlfrescoCmisHelper.AlfrescoCmisTypeInfo cmisTypeInfo = 
				AlfrescoCmisHelper.getTypeAndMandatoryAspectProperyDefinitions(cmisDataSource, cmisType);
		
        this.cmisObjectType = session.getTypeDefinition(cmisType);
        this.cmisPropDefs = cmisTypeInfo.getPropertyDefinitions();
        this.cmisTypeAndAspectCSV = cmisTypeInfo.getCmisTypeIdWithMandatoryAspects();
        return true;
	}
	
	public void navigate(int pagenumber ){
        log.debug("Navigate: Page number = " + pagenumber + " this.pageSize = " + this.pagesize);
		if (pagenumber <1){
			this.currentStartIndex = 0;
		}else if (pagenumber > 1){
			this.currentStartIndex = pagenumber * this.pagesize;
		}
		//this.initialize(this.pagesize, currentStartIndex ,this.orderBy );
		
	}
	
	public void populate() {
		populate(10, 0, null);
	}
	
	public void populate(int pagesize, int startindex, String orderBy) {
		populate(pagesize, startindex, orderBy, "IN_FOLDER");
	}
	/**
	 * 
	 * @param pagesize
	 * @param startindex
	 * @param orderBy
	 * @param cmisqlRootFunction IN_FOLDER or IN_TREE
	 */
	public void populate(int pagesize, int startindex, String orderBy, String cmisqlRootFunction) {
		if (pagesize < 0 || startindex < 0) {
			throw new RuntimeException("Pagesize was < 1 or startpage was < 0");
		}
		if (null == this.rootFolder) {
			throw new RuntimeException("Cannot initialize() if rootFolder == null");
		}
		if (null == this.cmisObjectType) {
			throw new RuntimeException("cmisObjectType cannot be null");
		}
		if (null != orderBy) {
			this.orderBy = orderBy;
		}

		log.debug("initialize( pagesize = "+pagesize+", startindex = "+startindex+ ", rootFolder = " + rootFolder.getPath()+ " )");
		String qName = this.cmisObjectType.getQueryName();
		OperationContext opCxt = session.createOperationContext();
		//opCxt.setMaxItemsPerPage(0 < pagesize ? pagesize : 10);
		
		PropertyDefinition<?> objectIdPropDef = cmisObjectType.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
		this.objectIdQName = objectIdPropDef.getQueryName();
		StringBuffer querySb = new StringBuffer().append("SELECT cmis:objectTypeId, ")
												.append(objectIdQName)
												.append(" FROM ")
												.append(qName)
												.append(" WHERE ")
												.append(cmisqlRootFunction)
												.append("('")
												.append(this.rootFolder.getId())
												.append("')");
		if (!"".equals(this.orderBy)) {
			querySb.append(" ORDER BY ").append(this.orderBy);
		}
		String query = querySb.toString();
		
		//List<CMISTYPE> allResults = new Vector<CMISTYPE>();
		log.info("**************Query: " + query);
		ItemIterable<QueryResult> results = session.query(query, false, opCxt); // false = search only latest versions
		this.realSize = new Long(results.getTotalNumItems()).intValue(); ;
		log.debug("Loading " + results.getTotalNumItems() + " results ");
		for (QueryResult qResult : results) {
			String objectId = qResult.getPropertyValueByQueryName(objectIdQName);
			String type = qResult.getPropertyValueByQueryName("cmis:objectTypeId");
			log.trace("Retrieved object type " + type + ", id " + objectId);
			addLazyItem(objectId);
//			@SuppressWarnings("unchecked")
//			CMISTYPE obj = (CMISTYPE) sess.getObject(sess.createObjectId(objectId));
//			log.info("Retrieved object " + obj.getId());
//			allResults.add(obj);
		}
//		this.addAll(allResults);
		//super.fireContainerPropertySetChange();
		super.fireItemSetChange();
		setInitialized(true);		
	}
	
	/**
	 * Reload all item IDs from repository after some external event invalidates the current set
	 */
	public void refresh() {
		getItems().clear();
		internalRemoveAllItems();
		populate(pagesize, 0, orderBy);
	}
	
	@Override
	public boolean removeAllItems() {
		getItems().clear();
		internalRemoveAllItems();
		super.fireItemSetChange();
		return true;
	}
	
	protected LinkedHashMap<String, CmisItem> getItems(){
		return this.itemIdToItem;
	}

	@Override
    protected void registerNewItem(int position, String itemId,
            CmisItem item) {
		// TODO: Actually handle insertion position. For now, ordering is in order of insertion
    	//log.info("Adding itemId " + itemId + " in postition " + position + ", itemIdToItem.size() == " + itemIdToItem.size());
    	itemIdToItem.put(itemId, item);
    	
    	
    	
    }

    public Class<?> getType(Object propertyId) {
    	PropertyDefinition<?> pd = cmisPropDefs.get(propertyId);
    	if (null == pd)
    		return null;
    	return AlfrescoCmisHelper.getPropertyClass(pd);
    }
    
    
    public PropertyDefinition getPropertyDefinition(Object id) {
    	return cmisPropDefs.get(id);
    }
	
    public Collection<String> getContainerPropertyIds() {
 
    	return cmisPropDefs.keySet();
    }
	
    /**
     * Adds the bean to the Container.
     * 
     * Note: the behavior of this method changed in Vaadin 6.6 - now items are
     * added at the very end of the unfiltered container and not after the last
     * visible item if filtering is used.
     * 
     * @see com.vaadin.data.Container#addItem(Object)
     */
	@SuppressWarnings("unchecked")
    public CmisItem addItem(CmisObject bean) {
        if (null == bean) {
            return null;
        }
        return internalAddItemAtEnd(getCmisId(bean), createCmisItem(bean), false);
    }
	
	public void addLazyItem(String itemId) {
		if (null == itemId || itemId.isEmpty()) {
			throw new IllegalArgumentException("Null itemId passed to addLazyItem");
		}
		if (this.getItemIds().contains(itemId)) {
			log.info("Tried to add existing item " + itemId);
			return;
		}
		// Create lazily-loaded CmisItem from id
		CmisItem item = createLazyCmisItem(itemId);
		internalAddItemAtEnd(itemId, item, false);
	}
	
	protected CmisItem createLazyCmisItem(String itemId) {
		return (itemId == null) ? null : new CmisItem(this, itemId);
	}

    /**
     * Adds all the beans from a {@link Collection} in one operation. More efficient than adding them one by
     * 
     * @param collection
     *            The collection of CmisObjects to add. Must not be null.
     * @throws IllegalStateException
     *             if no bean identifier resolver has been set
     */
    public void addAll(Collection<? extends CmisObject> collection)
            throws IllegalStateException {
        boolean modified = false;
        for (CmisObject bean : collection) {
            // TODO skipping invalid beans - should not allow them in javadoc?
            if (bean == null) {
                continue;
            }
            String itemId = getCmisId(bean);
            if (internalAddItemAtEnd(itemId, createCmisItem(bean), false) != null) {
                modified = true;
            }
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
    
    public void addItem(CmisItem item) {
    	if (null == item) {
    		return;
    	}
    	String itemId = (String)item.getItemId();
    	if (null == itemId) {
    		throw new IllegalArgumentException("Cannot add transient or lazy items using addItem(CmisItem) - see addLazyItem()");
    	}
    	if (internalAddItemAtEnd(itemId, item, false) != null) {
    		fireItemSetChange();
        	fireContainerPropertySetChange();
    	}
    	
    }
    

    protected String getCmisId(CmisObject obj) {
    	return (null == obj) ? null : obj.getId();
    }
    
    /**
     * Wrap a CmisObject in a CmisItem
     * 
     * @param bean
     * @return created {@link CmisItem} or null if bean is null
     */
    protected CmisItem createCmisItem(CmisObject bean) {
        return bean == null ? null : new CmisItem(this, bean);
    }
    
    public CmisObject createCmisObject(CmisItem item) {
    	if (!item.isCmisTransient()) {
    		throw new RuntimeException("Attempted to create a new CmisObject from a non-transient CmisItem");
    	}
    	Map<String, Object> dirtyProps = item.getDirtyValues();
    	if (dirtyProps.size() < 1) {
    		throw new RuntimeException("Attempted to create empty " + this.cmisType + ", no dirty properties found in CmisItem");
    	}
    	dirtyProps.put(PropertyIds.OBJECT_TYPE_ID, this.cmisTypeAndAspectCSV);
    	for (String id : dirtyProps.keySet()) {
    		log.trace("Prop: " + id + ". Value = " + dirtyProps.get(id));
    	}

    	CmisObject obj;
    	if (Folder.class.isAssignableFrom(this.type)) {
        	log.debug("Creating a " + this.cmisType + " folder");
    		obj = ((AlfrescoFolder)this.rootFolder).createFolder(dirtyProps);
    	} else { // it's a Document type
        	log.debug("Creating a " + this.cmisType + " document");
    		obj = this.rootFolder.createDocument(dirtyProps,null, null);
    	}
    	
    	item.setCmisObject(obj);
    	//this.addItem(obj); // happens in Cmisitem.persist()
    	return obj;
    }
    
    /**
     * 
     * Note, you shouldn't use this method to retrieve stuff from the container. What you want is an Item, not a CmisObject, and
     * you can get and create those via the Container interface properties (primarily addItem(*) and get(Unfiltered)Item. Don't
     * use this unless you know what you're doing (but don't retrieve CmisObjects from the session and skip this, either). 
     */
    protected CmisObject loadCmisObject(String id) {
    	
    	if (null == id) {
    		return null;
    	}
    	return AlfrescoCmisHelper.getObjectByIdAndAddAspects(session, id, aspects);
    	
    }
    
    
	public void updateObject(CmisItem item) {
    	if (item.isCmisTransient()) {
    		throw new RuntimeException("Attempted to update a transient CmisItem; must persist() to create in repository first");
    	}
    	// update only if there are changes to write
    	if (item.getDirtyValues().size() < 1)
    		return;
    	
    	item.setCmisObject(item.getCmisObject().updateProperties(item.getDirtyValues()));
    	fireContainerPropertySetChange();
	}

    
	public Property getContainerProperty(Object itemId, Object propertyId) {
		if (null == itemId || null == propertyId) {
			return null;
		}
		CmisItem c = getUnfilteredItem(itemId);
		if (null != c) {
			return c.getItemProperty(propertyId);
		}
		return null;
	}
	
	@Override
	protected CmisItem getUnfilteredItem(Object itemId) {
		if (null == itemId) {
			log.warn("Tried to retrieve null itemId");
			return null;
		}
		String sItemId = (itemId instanceof ObjectId) ? ((ObjectId) itemId).getId() : (String)itemId;
		if (null != itemIdToItem.get(itemId)) {
			return itemIdToItem.get(itemId);
		}
		log.trace("Cache miss, retrieving " + itemId.toString() + " from chemistry client cache and/or repository");
		try {
			CmisObject co = loadCmisObject((String)itemId);
			addItem(co);
		}
		catch (Exception e) {
			log.info("Failed to load object " + sItemId + ": " + e.getStackTrace());
			return null;
		}
		return itemIdToItem.get(sItemId);
	}

	/**
	 * Remove CmisItem from CmisContainer, and DELETE underlying CmisObject.
	 * @param itemId - CMIS id of the item to delete
	 */
	@Override
    public boolean removeItem(Object itemId){
		return removeItem(itemId, true);
	}
	/**
	 * 
	 * @param itemId
	 * @param deleteCmisObject - if true, will delete CmisObject underlying CmisItem from CMIS repository. If false,
	 * merely removes the item from CmisContainer's internal references.
	 * @return
	 */
    public boolean removeItem(Object itemId, boolean deleteCmisObject){
		if (null == itemId) {
			return false;
		}
		if (!containsId(itemId)) {
			return false;
		}
		
		CmisItem ci = getItem(itemId);
		if (null == ci) {
			return false;
		}
		
		itemIdToItem.remove(itemId);
		internalRemoveItem(itemId); 
		
		CmisObject co = ci.getCmisObject();
		if (null != co && deleteCmisObject) {
			
		  	if (Folder.class.isAssignableFrom(this.type)) {
				Folder folder = (Folder) co;
				 folder.deleteTree(true, null, true);
		  	}else{
				co.delete(true);
		  	}
			log.info("Deleted " + itemId);
		}
		fireItemSetChange();
		return true;
    }


	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		 sortContainer(propertyId, ascending);
	}


	@Override
	public Collection<?> getSortableContainerPropertyIds() {
	  return getSortablePropertyIds();
	}

/*******************************************************************
 *********** That which pertaineth to Container.Filterable ***********
 *******************************************************************/

	@Override
	public void addContainerFilter(Filter filter)
			throws UnsupportedFilterException {
		super.addFilter(filter);
	}

	@Override
	public void removeContainerFilter(Filter filter) {
		super.removeFilter(filter);
	}

	@Override
	public void removeAllContainerFilters() {
		super.removeAllFilters();
	}
	
	/**
	 * Call this if your Filter's state happens to change, and you want to re-filter the container
	 * without needing to remove and re-add the filter. 
	 */
	@Override
	public void filterAll() {
		super.filterAll();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
