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

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fatminds.cmis.AlfrescoCmisHelper;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.vaadin.data.Property;

public class CmisProperty implements Property {
	
	private static final Log log = LogFactory.getLog(AlfrescoCmisHelper.class);

	
	private boolean readonly = true;
	private final Class valueType;
	private boolean multivalued;
	private Object value;
	private String title;
	private boolean mandatory;

	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * @param mandatory the mandatory to set
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	protected void setTitle(String title) {
		this.title = title;
	}

	protected CmisObject cmisObject;
	public CmisObject getCmisObject() {
		return cmisObject;
	}

	protected CmisItem cmisItem;
	public CmisItem getCmisItem() {
		return cmisItem;
	}

	public void setCmisObject(CmisObject cmisObject) {
		this.cmisObject = cmisObject;
		//setId(cmisObject.getId());
		setValue(cmisObject.getPropertyValue((String)getId()), false); // Don't add to dirty map
	}

	protected Object id;
	
	public Object getId() {
		return id;
	}


	public void setId(Object id) {
		this.id = id;
	}


	public CmisProperty(CmisItem cmisItem, CmisObject cmisObject, Object id, Class valueType, boolean multivalued, String title, boolean mandatory) {
		this.valueType = valueType;
		this.cmisItem = cmisItem;
		this.id = id;
		this.multivalued = multivalued;
		this.title = title;
		this.mandatory = mandatory;
		if (null != cmisObject) {
			setCmisObject(cmisObject);
		}
	}
	
	
	public Object getValue() {
		return AlfrescoCmisHelper.convert(this.value, getType());
	}

	public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
		setValue(newValue, true);
	}
	
	@SuppressWarnings("unchecked")
	public void setValue(Object newValue, boolean addToDirtyMap) throws ReadOnlyException,
			ConversionException {
		if (null == newValue ) {
			this.cmisItem.addToDirtyValuesMap(id.toString(), null);
			return;
		}
		Object oldVal = this.getValue();
		if (isMultivalued()) {
 			if (newValue instanceof ArrayList){
				this.value = newValue;
			}
			else {
				ArrayList<Object> valArray;
				if (newValue.getClass().isArray()) {
					valArray = new ArrayList<Object>(Arrays.asList(newValue));
				}
				else {
					//log.info("**** typeof " + getId() + " is " + newValue.getClass().toString() + ", and isArray() == " + newValue.getClass().isArray());
					valArray = new ArrayList<Object>();
					Object firstVal = AlfrescoCmisHelper.convert(newValue, getType());
					valArray.add(firstVal);
				}
				this.value = valArray;
			}
		}
		else {
			this.value = AlfrescoCmisHelper.convert(newValue, getType());
		}
		//System.out.println("*** new Class = " + getValue().getClass() + ", NewVal=" + getValue() + " Old Value=" + oldVal );
		//Add to dirty map if the value is changed and it is requested
		if (
				(oldVal == null || !oldVal.equals(getValue()))
				&& true == addToDirtyMap
			){
			this.cmisItem.addToDirtyValuesMap(id.toString(), getValue());
		}
	}

	public Class<?> getType() {
		return valueType;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setReadOnly(boolean newStatus) {
		// TODO Auto-generated method stub
		this.readonly = newStatus;
	}
	
	@Override
	public String toString() {
		return getValue() == null ? null : getValue().toString();
	}

	public boolean isMultivalued() {
		return multivalued;
	}

	public void setMultivalued(boolean isMultivalued) {
		this.multivalued = isMultivalued;
	}

}
