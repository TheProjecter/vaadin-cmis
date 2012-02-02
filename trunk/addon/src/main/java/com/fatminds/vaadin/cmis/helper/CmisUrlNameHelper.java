package com.fatminds.vaadin.cmis.helper;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatminds.vaadin.cmis.CmisItem;
import com.fatminds.vaadin.cmis.validation.PropertyUniqueValidator;

import com.fatminds.vaadin.cmis.CmisItem;

public class CmisUrlNameHelper {
	private final static Log logger = LogFactory.getLog(CmisItem.class);

	public static String generateUniqueFriendlyUrl(CmisItem item){
		if (item.isCmisTransient()){
	    	logger.error("Item is Transient. Please save the record first before setting the Primary url Name");
	    	return "";
		}
	    String title = (String) item.getItemProperty("cm:title").getValue();
	    if (null == title){
	    	logger.error("Null cm:title value found for item " + item.getCmisObject().getName());
	    	return "";
	    }
	    // get the parent Institution
	    Folder parentInst = ((Folder)item.getCmisObject()).getFolderParent();
	    String instTitle = parentInst.getPropertyValue("cm:title");
	    if (null == instTitle){
	    	logger.error("Null cm:title found for institution " + parentInst.getName());
	    	return "";
	    }
	    StringBuffer url = new StringBuffer();
		url.append(encodeForUrl(instTitle));
		url.append("-");
		url.append(encodeForUrl (title));
		
		// Find unique variation
		String uniqueUrl = url.toString();
		boolean unique = false;
		int ctr=2;
		while(unique==false){
			PropertyUniqueValidator puv = new PropertyUniqueValidator(
					"Primary Url Name must be unique. Please choose a value not already in use, or edit the existing item with the Primary Url Name you entered.", 
					parentInst,
					"fmbase:educationitem", // must define property 'pid'
					"fmbase:primary_url_name",
					item,
					false, // not an aspect property
					null);
			if (puv.isValid(uniqueUrl)){
				unique=true;
			}else{
				uniqueUrl = url.toString() + "-" + ctr;
			}
			ctr++;
			if(ctr > 10){
				logger.error("Detected ctr > 10 while creating uniqueFriendlyUrl for " + item.getCmisObject().getName() + ", aborting...");
				return null;
			}
		}
		return uniqueUrl;
	}

	
	public static String encodeForUrl(String input) {
        if (input == null){
            return "";
        }
        return input.trim().toLowerCase().replaceAll("[^a-z\\s]", "").trim().replaceAll("\\s", "-");
  }
}
