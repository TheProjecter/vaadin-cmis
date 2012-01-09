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

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;

import com.vaadin.data.Property;

public class CmisDatalistItem extends CmisItem {

	protected Object propertyId;
	protected List<Object> propertyIds = new Vector<Object>();
	protected Object realPropertyId;
	
	/**
	 * Use to create an eagerly loaded object (if this constructor is used, all Item properties are immediately updated using values from the CmisObject)
	 * @param container
	 * @param cmisObject
	 * @param propertyId
	 * @param realPropertyId
	 */
	public CmisDatalistItem(CmisDatalistContainer container, CmisObject cmisObject, Object propertyId, Object realPropertyId) {
		super(container, cmisObject);
		if (null == propertyId || null == realPropertyId) {
			throw new IllegalArgumentException("propertyId and realPropertyId cannot be null");
		}
		setPropertyRedirect(propertyId, realPropertyId);
		this.propertyIds.addAll(super.getItemPropertyIds());
	}
	/**
	 * Use to create a transient object - a new Item will be created when persist() is called.
	 * @param container
	 * @param propertyId
	 * @param realPropertyId
	 */
	public CmisDatalistItem(CmisDatalistContainer container, Object propertyId, Object realPropertyId) {
		super(container);
		setPropertyRedirect(propertyId, realPropertyId);
	}

	/**
	 * Use to create a lazy-loaded CmisDatalistItem
	 * @param c
	 * @param cmisId
	 * @param propertyId
	 * @param realPropertyId
	 */
	public CmisDatalistItem(CmisDatalistContainer c, String cmisId, Object propertyId, Object realPropertyId) {
		super(c, cmisId);
		setPropertyRedirect(propertyId, realPropertyId);
	}

	protected void setPropertyRedirect(Object propertyId, Object realPropertyId) {
		if (null == propertyId || null == realPropertyId) {
			throw new IllegalArgumentException("propertyId and realPropertyId cannot be null");
		}
		this.propertyId = propertyId;
		this.realPropertyId = realPropertyId;
		this.propertyIds.add(this.propertyId);		
	}
	
	@Override
	public Collection<?> getItemPropertyIds() {
		return this.propertyIds;
	}
	
	@Override
	public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
		if (id.equals(propertyId)) {
			return super.addItemProperty(realPropertyId, property);
		}
		return super.addItemProperty(id, property);
	};
	
	@Override
	protected CmisProperty getCmisProperty(Object propid) {
		if (propid == null)
			return null;
		if (propid.equals(propertyId)) {
			return super.getCmisProperty(realPropertyId);
		}
		return super.getCmisProperty(propid);
	}


}
