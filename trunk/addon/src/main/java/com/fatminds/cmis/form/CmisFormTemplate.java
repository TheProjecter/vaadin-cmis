/**
 * 
 */
package com.fatminds.cmis.form;

import java.util.HashMap;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

/**
 * @author vijay
 *
 */
public interface CmisFormTemplate {
	
	public HashMap<String, Component> getCmisFieldComponents();
	
	public List<ComponentContainer> getComponentContainers();

}
