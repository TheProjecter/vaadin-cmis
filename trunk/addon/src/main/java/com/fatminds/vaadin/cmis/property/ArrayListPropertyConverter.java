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
package com.fatminds.vaadin.cmis.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vaadin.addon.customfield.PropertyConverter;

import com.vaadin.data.Property.ConversionException;

public class ArrayListPropertyConverter extends PropertyConverter<ArrayList, Set> {

	private static final Log log = LogFactory.getLog(ArrayListPropertyConverter.class);

	private static final long serialVersionUID = 1L;

	public ArrayListPropertyConverter() {
		super( ArrayList.class);
	}

	@Override
	// TODO preserve ordering
	public Set<?> format(ArrayList propertyValue) {
		if (null == propertyValue) {
			return null;
		}
		HashSet ret = new HashSet();
		// Creates some extra hashsets but is a lot clearer this way
		for (Object o : propertyValue) {
			ret.add(o);
		}
		//log.info("*** FORMAT " + propertyValue + ", RETURN " + ret);
		return ret;
	}

	@Override
	//TODO preserve ordering
	public ArrayList<?> parse(Set fieldValue) throws ConversionException {
		if (null == fieldValue) {
			return null;
		}
		ArrayList ret = new ArrayList();
		Set s = (Set)fieldValue;
		for (Object o : s) {
			ret.add(o);
		}
		//log.info("*** PARSE " + fieldValue + ", RETURN " + ret);
		return ret;
	}
	
}
