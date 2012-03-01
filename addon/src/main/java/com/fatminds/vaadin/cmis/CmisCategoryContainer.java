package com.fatminds.vaadin.cmis;

import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Session;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.fatminds.cmis.AlfrescoCmisHelper;
import com.fatminds.cmis.AlfrescoCmisSessionDataSource;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

public class CmisCategoryContainer extends HierarchicalContainer{

	protected AlfrescoCmisSessionDataSource cmisDataSource; // for AlfrescoCmisHelper statics
	
	public static final String CAT_PROPERTY_NAME = "name";
	
	public CmisCategoryContainer(AlfrescoCmisSessionDataSource cmisDataSource) {
		super();
		this.cmisDataSource = cmisDataSource;
	      // Create containerproperty for name
        addContainerProperty(CAT_PROPERTY_NAME, String.class, null);
  
		populate();
	}

	//populate the container
	public void populate() {
		JsonNode  rootCat = AlfrescoCmisHelper.getFatmindsCategories(cmisDataSource);
		if (rootCat != null){
			addItemtoContainer(rootCat, null);
		}
	}
	
	
	
	private void addItemtoContainer (JsonNode currNode, JsonNode parentNode){
		String itemId= currNode.findValue("cm:nodeRef").getTextValue();
		Item item = this.addItem(itemId);
          // Add name property for item
        item.getItemProperty(CAT_PROPERTY_NAME).setValue( currNode.findValue("cm:name").getTextValue());
        if (parentNode != null){
    		String parentId= parentNode.findValue("cm:nodeRef").getTextValue();
            this.setParent(itemId,parentId);
        }
        // Allow children
        this.setChildrenAllowed(itemId, true);
		Iterator<JsonNode> childIter = currNode.findValues("children").get(0).getElements();
		while(childIter.hasNext()){
			JsonNode childNode = childIter.next();
			addItemtoContainer(childNode, currNode);
		}
	}		
}
