package com.fatminds.vaadin.cmis;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class CmisCategoryItem implements Item, Item.PropertySetChangeNotifier{

	private final Log log = LogFactory.getLog(CmisCategoryItem.class);
	protected CmisCategoryContainer container;
	
	protected String nodeRef;
	protected String displayPath;
	protected String name;
	protected boolean isCmisTransient;
	
	
	public CmisCategoryItem(CmisCategoryContainer container,String name, String nodeRef, String displayPath ){
		this.container = container;
		this.name = name;
		this.displayPath = displayPath;
		this.nodeRef = nodeRef;
		this.isCmisTransient = false;
	}
	

	@Override
	public void addListener(PropertySetChangeListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeListener(PropertySetChangeListener listener) {
		throw new UnsupportedOperationException();		
	}

	@Override
	public Property getItemProperty(Object id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<?> getItemPropertyIds() {
		// TODO Auto-generated method stub
		return container.getContainerPropertyIds();
	}

	@Override
	public boolean addItemProperty(Object id, Property property)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeItemProperty(Object id)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
