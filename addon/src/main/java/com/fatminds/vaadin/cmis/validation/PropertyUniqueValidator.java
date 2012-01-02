/**
 * 
 */
package com.fatminds.vaadin.cmis.validation;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.fatminds.vaadin.cmis.CmisItem;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractValidator;

/**
 * @author vijay
 *
 */
@Configurable
public class PropertyUniqueValidator extends AbstractValidator{

	private final Log log = LogFactory.getLog(CmisItem.class);
	

	/**
	 * The object below which the search for uniqueness is performed (i.e. IN_TREE(validationScopeRoot) )
	 */
	protected Folder validationScopeRoot; 
	protected String propertyId; // The property to validate
	protected CmisItem thisCmisItem; // item being validated (I guess this means that the validation should be done at the Form level, not field)
	protected String cmisType;
	protected boolean propIsAspect;
	protected String aspectType;
	
	// Cache
	protected Object lastValueChecked;
	protected boolean lastResponse;
	
	@Autowired
	protected Session cmisSession;
	
	/**
	 * 
	 * @param errorMessage - put something appropriate for this field
	 * @param validationScopeRoot - Generally catalog or institution
	 * @param propertyId - The property to search for existence of the proposed value
	 * @param thisCmisItem -- The id of the CmisObject being edited, if any (should be null if validating a transient Item, must not be
	 * null otherwise).
	 * @param propIsAspect -- If the propertyId is part of an Alfresco aspect, it needs to be queried using the Alfresco Cmisql join extension
	 * for querying aspect properties. If set to true, PropertyUniqueValidator will so generate its CMIS queries for propertyId. 
	 * @param aspectType -- If propIsAspect == true, must be set to the CMIS type identifier for the relevant aspect. Else, should be null. 
	 * 
	 * NOTE: This ONLY works with properties stored on derivatives of cmis:folder.
	 * TODO: Refactor to support both cmis:folder and cmis:content.
	 */
	public PropertyUniqueValidator(
			String errorMessage, 
			Folder validationScopeRoot, 
			String cmisType, 
			String propertyId, 
			CmisItem thisCmisItem, 
			boolean propIsAspect,
			String aspectType) 
	{
        super(errorMessage);
		if (null == validationScopeRoot) {
			throw new IllegalArgumentException("validationScopeRoot must not be null");
		}
		if (null == cmisType || cmisType.isEmpty()) {
			throw new IllegalArgumentException("cmisType cannot be null");
		}
		if (null == propertyId) {
			throw new IllegalArgumentException("propertyId must not be null");
		}
		if (null == thisCmisItem) {
			throw new IllegalArgumentException("cmisItem must not be null");
		}


		this.validationScopeRoot = validationScopeRoot;
		this.propertyId = propertyId;
		this.cmisType = cmisType;
		this.thisCmisItem = thisCmisItem;
		this.propIsAspect = propIsAspect;
		this.aspectType = aspectType;
	}
	
	/**
	 * @return the thisCmisItem
	 */
	public CmisItem getThisCmisItemd() {
		return thisCmisItem;
	}

	/**
	 * @param thisCmisItem the thisCmisItem to set
	 */
	public void setThisObjectId(CmisItem thisCmisItem) {
		this.thisCmisItem = thisCmisItem;
	}

	// The isValid() method returns simply a boolean value, so
    // it can not return an error message.
    public boolean isValid(Object value) {
    	// Null is treated as valid (required-ness handled by required state of field)
        if (value == null || !(value instanceof String )) {
            return true;
        }
        if (lastValueChecked == value) {
        	return lastResponse;
        }
        
        String candidateValue = (String)value;
        
        // Build either object or aspect property query using CONTAINS
		StringBuffer querySb = new StringBuffer().append("SELECT d.cmis:objectid FROM ").append(cmisType).append(" as d ");
	    if (propIsAspect) {
        	querySb.append(" join ").append(aspectType).append(" as o on d.cmis:objectid = o.cmis:objectid");
        }
		querySb.append(" WHERE IN_TREE(d, '").append(validationScopeRoot.getId()).append("')");

		if (propIsAspect) {
        	querySb.append(" AND CONTAINS(o, '").append(propertyId).append(":\\'").append(candidateValue).append("\\'')");
		}else {
        	querySb.append(" AND CONTAINS(d, '").append(propertyId).append(":\\'").append(candidateValue).append("\\'')");
		}
		if (null != thisCmisItem.getCmisObject()) {
			// PropertyUniqueValidator treats null thisObjId as valid iff the CmisItem is transient (means that
			// is validating the potential creation of a new object, rather than a different value for
			// an existing object. 
			querySb.append(" AND d.cmis:objectId <> '")
			.append(thisCmisItem.getCmisObject().getId())
			.append("'");
        }
        String query = querySb.toString();        
		log.info("Querying: " + query);
		ItemIterable<QueryResult> results = cmisSession.query(query, false); // false = search only latest versions
		boolean result = results.getTotalNumItems() > 0 ? false : true;
		log.info("Result: " + result );
		lastValueChecked = value;
		lastResponse = result;
		return result;

    }

    // Upon failure, the validate() method throws an exception
    // with an error message.
    /*
    public void validate(Object value)
                throws InvalidValueException {
        if (!isValid(value)) {
            if (value != null &&
                !value.toString().startsWith("")) {
                throw new InvalidValueException(
                    "Invalid Url. Please correct the url!");
            } 
        }
    }*/

}
