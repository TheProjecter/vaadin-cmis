/**
 * 
 */
package com.fatminds.vaadin.cmis.name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fatminds.cmis.AlfrescoCmisHelper;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;

/**
 * @author aaronlee
 *
 */
public class PropertyCmisNameGenerator implements CmisNameGenerator{

	protected String[] propNames;
	/**
	 * 
	 * @param propNames - list of one or more property names. The underlying CMIS property type must be either
	 * <code>String</code> or multi-valued <code>ArrayList<String></code>
	 */
	public PropertyCmisNameGenerator(String[] propNames) {
		if (null == propNames || propNames.length < 1) {
			throw new IllegalArgumentException("propNames cannot be null or empty");
		}
		this.propNames = propNames;
	}
	
	
	public String getName(Form form) {
		StringBuffer name = new StringBuffer();
		for (String prop : propNames) {
			Field f = form.getField(prop);
			if (null == f) {
				throw new IllegalArgumentException("Form is missing expected property " + prop);
			}
			Object obj = f.getValue();
			if (null == obj) {
				continue;
			}

			String value=null;
			if (obj instanceof String) {
				value = (String)obj;
			}
			else if (obj instanceof ArrayList){
				value = flatten((ArrayList)obj);
			}
			else {
				throw new RuntimeException("Cannot handle non-String property " + prop);
			}
			if (name.length() > 0) {
				name.append("_");
			}
			name.append(value);
		}
		if (0 == name.length()) {
			throw new IllegalArgumentException("All configured field data sources are null");
		}
		return AlfrescoCmisHelper.sanitizeForCmisName(name.toString());
	}

	public static String flatten(ArrayList<String> tokens) {
		Collections.sort(tokens);
		StringBuffer sb = new StringBuffer();
		for (String s : tokens) {
			if (null == s)
				continue;
			if (sb.length() > 0)
				sb.append("-=-");
			sb.append(s);
		}
		return sb.toString();
	}
	
}
