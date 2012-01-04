/**
 * 
 */
package com.fatminds.cmis.form;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fatminds.vaadin.cmis.CmisConstants;
import com.fatminds.vaadin.cmis.CmisItem;
import com.fatminds.vaadin.cmis.name.CmisNameGenerator;
import com.fatminds.vaadin.cmis.name.PropertyCmisNameGenerator;
import com.vaadin.data.Item;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbsoluteLayout.ComponentPosition;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.Area;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CmisForm extends Form implements ClickListener  {
	
	private static final Logger log = LoggerFactory.getLogger(CmisForm.class);
	
	protected final Button save = new Button("Save", (ClickListener) this);
	//protected Button edit = new Button("Edit", (ClickListener) this);
  
	protected boolean cmisObjectExists = false;
     
     //protected GridLayout formLayout;
     protected GridLayout nonTemplateFormLayout;
	 protected VerticalLayout formLayout;
     
     protected  CmisFormTemplate templateForm;
     protected int rows;
     protected int cols;
     
     protected CmisNameGenerator cmisNameGenerator;
     
     /**
      * List of previously replaced form fields (i.e. we replaced the template field in the templateform
      * held by this cmisform with the correct (from the FieldFactory) form already, and stored the fact here). 
      */
     protected Map<Object, ComponentContainer> replacedFields = new HashMap<Object, ComponentContainer>();
 
     
     /**
	 * @return the cmisNameGenerator
	 */
	public CmisNameGenerator getCmisNameGenerator() {
		return cmisNameGenerator;
	}

	/**
	 * @param cmisNameGenerator the cmisNameGenerator to set
	 */
	public void setCmisNameGenerator(CmisNameGenerator cmisNameGenerator) {
		this.cmisNameGenerator = cmisNameGenerator;
	}
	
	public CmisForm(CmisFormTemplate templateForm, FormFieldFactory fieldFactory) {
	   	 this(templateForm, fieldFactory, new PropertyCmisNameGenerator(new String[] {"cm:title"}));
	}

	public CmisForm(CmisFormTemplate templateForm,FormFieldFactory fieldFactory, CmisNameGenerator cmisNameGenerator) {
    	if (null == fieldFactory)
    		throw new IllegalArgumentException("fieldFactory cannot be null");
    	if (null == cmisNameGenerator)
    		throw new IllegalArgumentException("cmisNameGenerator cannot be null");
    	this.cmisNameGenerator = cmisNameGenerator;

    	setFormFieldFactory(fieldFactory);
    	
    	this.templateForm = templateForm;
    	this.formLayout = new VerticalLayout();
        formLayout.removeAllComponents();
        nonTemplateFormLayout = new GridLayout(2, 5);

        formLayout.addComponent((Component) templateForm);
    	formLayout.addComponent(nonTemplateFormLayout);
    	nonTemplateFormLayout.setSizeFull();
    	formLayout.setWidth("350px");
    	//((Component) templateForm).setSizeFull();
    	this.setSizeFull();
    	setImmediate(true);
    	//setWriteThrough(true);
     
    	// Enable buffering so that commit() must be called for the form
         // before input is written to the data. (Form input is not written
         // immediately through to the underlying object.)
          /*
    	   setWriteThrough(false);
           setImmediate(true);
           setValidationVisible(false);
           setValidationVisibleOnCommit(true);*/
        setLayout(this.formLayout);
        
        HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.setMargin(false);
        footer.addComponent(save);
        footer.setComponentAlignment(save, Alignment.TOP_CENTER);
        footer.setWidth("100%");
        footer.setHeight("100px");
	    setFooter(footer);
	    getFooter().setVisible(false);
       }
    
    /*
     * Override to get control over where fields are placed.
     */
	
    @Override
    protected void attachField(Object propertyId, Field field) {
    	
    	// Handled already, don't bother looking up layout again.
    	if (false && replacedFields.containsKey(propertyId)) {
    		replacedFields.get(propertyId).replaceComponent(getField(propertyId), field);
    		field.setVisible(true);
    		return;
    	}

    	Component replaceComponent = templateForm.getCmisFieldComponents().get(propertyId);
    	ComponentContainer layout = getComponentContainer(replaceComponent);
    	if (replaceComponent != null && layout != null){
    		field.setWidth(replaceComponent.getWidth());
    		field.setHeight(replaceComponent.getHeight());
    		
    		String caption = replaceComponent.getCaption();
    		//just copy the caption. if its blank its as designed in the UI
    		field.setCaption(caption);
    		if (replaceComponent instanceof AbstractTextField){
    			String inputPrompt = ((AbstractTextField)replaceComponent).getInputPrompt();
    			if (inputPrompt != null && !inputPrompt.equals("")){
    				((AbstractTextField)field).setInputPrompt(inputPrompt);
    			}
    		}
    		if (caption == null || caption.equals("") ){
    			caption = field.getCaption();
    			if (null == caption) {
    				caption = propertyId.toString();
    			}
    		}
    		field.setRequiredError(caption + " is a required field!");
    		layout.replaceComponent(replaceComponent, (Component) field);
        	replacedFields.put(propertyId, layout);
    	}else{
    		log.info("XXXXX - Could not find component for property=" + propertyId);
    		//nonTemplateFormLayout.addComponent(field);
    	}
    }
    
    
    /**
     * TODO: Make work for all layout types. Currently: must pass in all ComponentContainers that 
     * have as immediate children fields that are replace-able, and those containers must be of 
     * type Absolute or GridLayout.
     * 
     * @param replaceComponent
     * @return
     */
    protected ComponentContainer getComponentContainer(Component replaceComponent){
    	if (replaceComponent == null){
    		return null;
    	}
    	List<ComponentContainer> layouts = templateForm.getComponentContainers();
    	if (layouts != null){
    		Iterator<ComponentContainer> iter = layouts.iterator();
    		while (iter.hasNext()){
    			ComponentContainer layout = iter.next();
    			if (layout instanceof AbsoluteLayout ){
        			ComponentPosition position = ((AbsoluteLayout) layout).getPosition(replaceComponent);
        			if (position != null){
        				//found the position. return this layout
        				return layout;
        			}
    			}
    			if (layout instanceof GridLayout){
    				Area area = ((GridLayout) layout).getComponentArea( replaceComponent);
           			if (area != null){
        				//found the position. return this layout
        				return layout;
        			}
    				
    			}
    		}
    	}
    	return null;
    }
    
   

    protected Component getFieldComponent(CustomComponent templateForm, String caption){
    	Iterator<Component> iter = templateForm.getComponentIterator();
    	while (iter.hasNext()){
    		Component comp = iter.next();
    		if (comp != null && comp.getCaption() != null && comp.getCaption().equalsIgnoreCase(caption)){
    	    	log.info("Found component with field name=" + caption);
    			return comp;
    		}
    	}
    	log.info("Could not find component with field name=" + caption);
    	return null;
    	
    }
    
    public void buttonClick(ClickEvent event) {
        Button source = event.getButton();
        
        if (source == save) {
             /* If the given input is not valid there is no point in continuing */
            //This is not needed as commit calls isValid
        	/*
        	if (!isValid()) {
            	 setValidationVisible(true);
            	 getWindow().showNotification("There are Validation Errors on the form.Please correct them!");
            	 return;
             }*/
             commit();
             saveCmisObject();
             Window currWindow = getWindow();
             currWindow.showNotification(
	                    "Item Saved!");
             if (currWindow != null && currWindow.getParent()  != null){
            	 ((Window) currWindow.getParent()).removeWindow(currWindow);
             }
             setReadOnly(false);
         } 
    }

    
    public Object getFieldValue(String propertyId){
    	Field field = getField(propertyId);
    	if (field != null){
    		return field.getValue();
    	}
   	return null;
    }
    
    public void setFieldValue(String propertyId, Object value){
      	Field field = getField(propertyId);
    	if (field != null){
    		field.setValue(value);
    	}
    	
    }
    
    
    
    protected void saveCmisObject() {
    	
    	CmisItem ci = (CmisItem)getItemDataSource();
    	
    	// If we're transient, and cmis:name isn't already set, then set it here
    	// using the name generator. It's ignored if the item wraps an existing cmisObject. 
    	ci.persist(cmisNameGenerator.getName(this));
	}


	@Override
    public void setItemDataSource(Item newDataSource) {
        if (newDataSource != null && newDataSource instanceof CmisItem) {
        	CmisItem newSrc = (CmisItem) newDataSource;
/**     	Collection<?> formFields = newDataSource.getItemPropertyIds();
        	formFields.removeAll(CmisConstants.INVISIBLE_FIELDS);
            super.setItemDataSource(newDataSource, formFields);
**/
        	super.setItemDataSource(newDataSource);
            getFooter().setVisible(true);
            requestRepaint();
        } else {
            super.setItemDataSource(null);
            getFooter().setVisible(false);
        }
    }
    
    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        save.setVisible(!readOnly);
       // edit.setVisible(readOnly);
        // If setting readOnly = false, then
        // undo super.setReadOnly() for those fields
        // in the CMIS read-only list
        for (String fieldName : CmisConstants.GENERATED_FIELDS) {
        	if (null != getField(fieldName)) {
        		getField(fieldName).setReadOnly(true);
        	}
        }
    }
    
    
  
}
