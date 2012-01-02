/**
 * 
 */
package com.fatminds.vaadin.cmis;

import java.util.HashSet;
import java.util.Set;

/**
 * @author aaronlee
 *
 */
public class CmisConstants {

	public static final Set<String> INVISIBLE_FIELDS = new HashSet<String>();
	public static final Set<String> GENERATED_FIELDS = new HashSet<String>();
	
	static {
		
		// These fields are treated as readonly in edit dialogs
		GENERATED_FIELDS.add("cmis:name");
		
		// These fields are not exposed through CmisContainer*
		// * But they're still accessible from the contained CmisItems/CmisObjects
		INVISIBLE_FIELDS.add("cmis:name");
		INVISIBLE_FIELDS.add("cmis:objectId");
		INVISIBLE_FIELDS.add("cmis:createdBy");
		INVISIBLE_FIELDS.add("cmis:allowedChildObjectTypeIds");
		INVISIBLE_FIELDS.add("cmis:path");
		INVISIBLE_FIELDS.add("cmis:objectTypeId");
		INVISIBLE_FIELDS.add("cmis:creationDate");
		INVISIBLE_FIELDS.add("cmis:changeToken");
		INVISIBLE_FIELDS.add("cmis:lastModifiedBy");
		INVISIBLE_FIELDS.add("cmis:baseTypeId");
		INVISIBLE_FIELDS.add("cmis:lastModificationDate");
		INVISIBLE_FIELDS.add("cmis:parentId");
		INVISIBLE_FIELDS.add("cm:categories");
		INVISIBLE_FIELDS.add("cm:description"); // fmaspect:description is used instead. TODO: refactor all uses of fmaspect:description to use
												// Document content streams	
		// Document fields below
		INVISIBLE_FIELDS.add("cmis:versionSeriesCheckedOutBy");
		INVISIBLE_FIELDS.add("cmis:versionSeriesId");
		INVISIBLE_FIELDS.add("cmis:versionSeriesCheckedOutId");
		INVISIBLE_FIELDS.add("cmis:isLatestVersion");
		INVISIBLE_FIELDS.add("cmis:versionLabel");
		INVISIBLE_FIELDS.add("cmis:isVersionSeriesCheckedOut");
		INVISIBLE_FIELDS.add("cmis:isLatestMajorVersion");
		INVISIBLE_FIELDS.add("cmis:contentStreamId");
		INVISIBLE_FIELDS.add("cmis:contentStreamMimeType");
		INVISIBLE_FIELDS.add("cmis:checkinComment");
		INVISIBLE_FIELDS.add("cmis:isMajorVersion");
		INVISIBLE_FIELDS.add("cmis:isImmutable");
		INVISIBLE_FIELDS.add("cmis:contentStreamFileName");
		INVISIBLE_FIELDS.add("cmis:contentStreamLength");
		
		}
	
}
