package com.fatminds.vaadin.cmis.components;

import java.util.Collection;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.vaadin.dialogs.ConfirmDialog;

import com.fatminds.vaadin.cmis.CmisAssociationContainer;
import com.fatminds.vaadin.cmis.CmisConstants;
import com.fatminds.vaadin.cmis.CmisContainer;
import com.fatminds.vaadin.cmis.CmisItem;
import com.vaadin.data.Container;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;


public class CmisList<CMISTYPE extends CmisObject> extends Table implements Container.ItemSetChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(CmisList.class);
	protected CmisItem selectedItem;
	
	public CmisList() {
		super();
    	setWidth("100%");
    	setHeight("100%");
	}
	
	@Override
	public void setContainerDataSource(Container newDataSource) {
		super.setContainerDataSource(newDataSource);
		if (newDataSource instanceof CmisContainer) {
			CmisContainer<CMISTYPE> container = (CmisContainer) newDataSource;
	    	container.addListener(this);
	    	this.setColumnReorderingAllowed(true);
	    	this.setColumnCollapsingAllowed(true);
	    	this.alwaysRecalculateColumnWidths = true;
	    	//Collection<String> visible = container.getContainerPropertyIds();
	    	//visible.removeAll(CmisConstants.INVISIBLE_FIELDS);
	    	//setVisibleColumns(visible.toArray());
	    	
	    	this.setPageLength(0); // disable
	    	setSelectable(true);
	    	setMultiSelect(false);
	    	setImmediate(true);  
	        /* We don't want to allow users to de-select a row */
	    	setNullSelectionAllowed(false);
		}
	}
	
    public CmisList(CmisContainer<CMISTYPE> container, int pagesize) {
    	this();    	
    }
    
    public void addDeleteButtonColumn(){
		this.addGeneratedColumn("Delete", new Table.ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
            	CmisItem item = (CmisItem)getItem(itemId);
                // the Link -component:
                Button itemsButton = new Button("X");
                itemsButton.setData(item);
                itemsButton.addListener(new Button.ClickListener() {
                	
                    public void buttonClick(ClickEvent event) {
                    	final CmisItem item =(CmisItem)event.getButton().getData();
                    	setSelectedItem(item);
	                	getContainerDataSource().removeItem(item.getItemId());
                    	
/**
 * In practice, you'll want to confirm before deleting...
 * 
 * 
 						ConfirmDialog.show(CatalogManagerApplication.getCurrentNavigableAppLevelWindow(), "Please Confirm:", "Are you sure you want to delete this item.?",
                    	        "Yes", "No", new ConfirmDialog.Listener() {

                    	            public void onClose(ConfirmDialog dialog) {
                    	                if (dialog.isConfirmed()) {
                    	                    // Confirmed to continue
                    	                	container.removeItem(item.getItemId());
                    	                } 
                    	            }
                    	        });
**/
                    }

                });
                
                return itemsButton;
            }

        });	
    }
    
    public void addAssociationButtonColumn(final String headerText, 
    											final String buttonTextAdd, 
    											final String buttonTextRemove,
    											final String dialogTextConfirmAdd,
    											final String dialogTextConfirmRemove,
    											final CmisAssociationContainer assocContainer,
    											final CmisObject associationSource){
		this.addGeneratedColumn(headerText, new Table.ColumnGenerator() {
            public Component generateCell(Table source, final Object itemId,
                    final Object columnId) {
            	CmisItem item = (CmisItem)getItem(itemId);
            	// Find out if this item is already in the association
            	boolean isAssociated = assocContainer.containsId(itemId);
                // the Link -component:
                final Button itemsButton = new Button(isAssociated ? buttonTextRemove : buttonTextAdd);
                itemsButton.setData(item);
                itemsButton.addListener(new Button.ClickListener() {
                	
                    public void buttonClick(ClickEvent event) {
                    	final CmisItem item =(CmisItem)event.getButton().getData();
                    	setSelectedItem(item);
                    	boolean exists = assocContainer.containsId(itemId);
   	                	if (assocContainer.containsId(itemId)) {
	                		assocContainer.removeItem(itemId);
	                		itemsButton.setCaption(buttonTextAdd);
	                	}
	                	else {
    	                	assocContainer.addItem(item);
    	                	itemsButton.setCaption(buttonTextRemove);
	                	}
/**
 * Preferred impl needs an app-level window to display its confirmation dialog; replace with your favorite flavor 
 * (we use an adaptation of navigator7)
 * 
 *                    	ConfirmDialog.show(CatalogManagerApplication.getCurrentNavigableAppLevelWindow(), 
                    			"Please Confirm", 
                    			(exists ? dialogTextConfirmRemove : dialogTextConfirmAdd),
                    	        "Yes", 
                    	        "No", 
                    	        new ConfirmDialog.Listener() {

                    	            public void onClose(ConfirmDialog dialog) {
                    	                if (dialog.isConfirmed()) {
                    	                    // Confirmed to continue
                    	                	if (assocContainer.containsId(itemId)) {
                    	                		assocContainer.removeItem(itemId);
                    	                		itemsButton.setCaption(buttonTextAdd);
                    	                	}
                    	                	else {
                        	                	assocContainer.addItem(item);
                        	                	itemsButton.setCaption(buttonTextRemove);
                    	                	}
                    	                } 
                    	            }
                    	        });
**/
                    }

                });
                
                return itemsButton;
            }
        });	
    }


    
    public void refreshList(CmisContainer container){
    	log.info("Table pagesize before setContainerDataSource = " + getPageLength());
       	setContainerDataSource(container);
    	log.info("Table pagesize after setContainerDataSource = " + getPageLength());
     	setVisibleColumns(getContainerDataSource().getContainerPropertyIds().toArray());
    	log.info("Table pagesize after setVisibleColumns() = " + getPageLength());
    	//setColumnHeaders(this.getContainerDataSource().getContainerPropertyIds().toArray());
    }
    

	public CmisItem getSelectedItem(){
		return this.selectedItem;
	}
	public void setSelectedItem(CmisItem item){
		 this.selectedItem = item;
	}
 
}
