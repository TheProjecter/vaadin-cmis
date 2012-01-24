/**
 * 
 */
package com.fatminds.vaadin.cmis.name;

import java.util.Map;

import com.vaadin.ui.Form;

/**
 * @author aaronlee
 *
 */
public interface CmisNameGenerator {

	public String getName(Form form);
	
	public String getName(Map<String, Object> properties);
	
}
