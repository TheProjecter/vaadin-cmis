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
package com.fatminds.vaadin_cmis_integration.demo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.FieldWrapper;
import org.vaadin.addon.customfield.PropertyConverter;
//import org.vaadin.tokenfield.TokenField;

//import com.datawave.dwtextfields.DWDecimalTextField;
import com.fatminds.vaadin.cmis.CmisDateField;
import com.fatminds.vaadin.cmis.CmisItem;
import com.fatminds.vaadin.cmis.CmisManyField;
import com.fatminds.vaadin.cmis.CmisProperty;
import com.fatminds.vaadin.cmis.property.ArrayListPropertyConverter;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class DemoFieldFactory extends DefaultFieldFactory {

	private static final Logger log = LoggerFactory.getLogger(DefaultFieldFactory.class);
	
	// Holds reference to each field created by BCFF and concrete classes
	Map<Object, Field> fields = new HashMap<Object, Field>();

	public DemoFieldFactory() {
		super();
	}
	
	protected void putField(Object id, Field f) {
		if (id == null)
			throw new IllegalArgumentException("id cannot be null in addField()");
		if (f == null)
			throw new IllegalArgumentException("field cannot be null in addField()");
		fields.put(id, f);
	}
	
	protected Field getField(Object id) {
		return fields.get(id);
	}
	
	protected Field deleteField(Object id) {
		return fields.remove(id);
	}

	public Field createField(Item item, Object propertyId,
                             Component uiContext) {

		if (null == propertyId)
        	throw new IllegalArgumentException("Cannot create field for null propertyId");

		if (null != getField(propertyId)) {
			return getField(propertyId);
		}
		
        // Identify the fields by their Property ID or other metadata
        String pid = (String) propertyId;
        CmisProperty prop = (CmisProperty) item.getItemProperty(propertyId);
        
        Class<?> type = prop.getType();
        CmisItem cmisItem=(CmisItem)item;
        
        String title = prop.getTitle();
        if (title == null || title.isEmpty()){
        	title = (String) propertyId;
        }
        
        // Note, THIS IS NULL if we have a transient (new) Item
        String thisObjId = (null == cmisItem.getCmisObject() ? null : cmisItem.getCmisObject().getId());
        
        // This field is returned by the method.
        // If you don't want a TextField, set it to something else in your specialization clause. 
        Field retField=null;
        // By default, a TextField is returned. Use this reference to set TextField-specific properties in your 
        // specialization clause.
        TextField textField;
        retField = textField = new TextField(); 
        textField.setNullRepresentation("");
        //textField.setWidth("100%");
        textField.setCaption(title);
        textField.setImmediate(true);
		textField.setValidationVisible(false);

		/***
		 * Handle your non-standard fields by name or id here. 
		 */
        if ("fmexample:description".equals(pid)) {
            //RichTextArea rta = new RichTextArea();
        	TextArea ta = new TextArea();
            ta.setCaption(title);
            ta.setNullRepresentation("Enter formatted description");
         //   rta.setSizeFull();
            //rta.setImmediate(true);
            retField = ta;
        }
/**
 * Adapt to taste for handling multi-valued CMIS properties. The following uses TokenField plus a FieldWrapper and ArrayListPropertyConverter
 * to provide for inline suggestion, and inline storage of newly entered values for future suggestions, for multi-valued string properties. 
 * 
            else if (prop.isMultivalued()) {
        	TokenField tf = new TokenField();
        	tf.setWidth("1000px");
        	PropertyConverter pc = new ArrayListPropertyConverter();
        	
        	FieldWrapper fw = new CmisManyField(tf, pc, ArrayList.class);
        	fw.setCaption(title);
        	fw.setReadOnly(true);
        	retField = fw;
        	
        	if (null != catSvc.getCmisDatalistContainer(parentInstitution, pid)) {
        		tf.setContainerDataSource(catSvc.getCmisDatalistContainer(parentInstitution, pid));
        		tf.setTokenCaptionPropertyId(propertyId);
        		tf.setImmediate(true);
        	}
        	else {
	        	PropertysetItem pitem = new PropertysetItem();
	        	pitem.addItemProperty(propertyId, new ObjectProperty(propertyId));
	        	Set<Object> s = new HashSet<Object>();
	        	s.add(propertyId);
	        	IndexedContainer ic = new IndexedContainer(s);
	        	
	        	tf.setContainerDataSource(ic);
	        	tf.setTokenCaptionPropertyId(propertyId);
        	}	        	
        }
   **/     
        else if (
        	Double.class.isAssignableFrom(type) ||
        	Float.class.isAssignableFrom(type) ||
        	//Number.class.isAssignableFrom(type) ||
        	//BigInteger.class.isAssignableFrom(type) ||
        	BigDecimal.class.isAssignableFrom(type)) {
        	textField.setMaxLength(10);
        	textField.setCaption(title);
        	textField.setImmediate(true);
        	textField.setPropertyDataSource(new PropertyFormatter(item.getItemProperty(propertyId)) {
                    public String format(Object value) {
                        return value.toString();
                    }

                    public Object parse(String formattedValue) throws Exception {
                        return new BigDecimal(Double.parseDouble(formattedValue));
                    }
                });
        	retField = textField;
        }
        else if (Calendar.class.isAssignableFrom(type)) {
    		CmisDateField dateFld = new CmisDateField(title);
        	retField = dateFld;
    }
    
    else if (Boolean.class.isAssignableFrom(type)) {
    	CheckBox cb = new CheckBox(title);
        cb.setDescription(title);
    	retField = cb;
    }       
    
    
    
    // Bind to CmisProperty
	retField.setPropertyDataSource(prop);
	// Be careful with this - if it's <mandatory> in the alfresco content model, it's mandatory in your forms.
	boolean isRequired = prop.isMandatory();
	retField.setRequired(false);
	
    putField(propertyId, retField);
    
    //Add Validators
    addValidators(pid, retField, title, cmisItem);
    log.debug("Returning a " + retField.getClass() + " for cmis property " + propertyId);
    return retField;		
        
	}
	
	private void addValidators(String pid, Field retField, String title,
			CmisItem cmisItem) {
		/**
		 * Add custom validators (property unique, conforming, existence, etc) to
		 * fields here
		 */
		
	}

	public Field createField(Container container, Object itemId,
			Object propertyId, Component uiContext) {
		throw new RuntimeException("Not implemented!");
	}

	public Field createField(Class<?> type, Component uiContext) {
		throw new RuntimeException("Not implemented!");
	}

	public Field createField(Property property, Component uiContext) {
		throw new RuntimeException("Not implemented!");
	}

}
