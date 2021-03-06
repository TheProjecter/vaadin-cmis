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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.vaadin.addon.customfield.FieldWrapper;
import org.vaadin.addon.customfield.PropertyConverter;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

public class CmisManyField extends FieldWrapper<ArrayList> {
	
	private static final long serialVersionUID = 1L;

	
	public CmisManyField(Field wrappedField,
			PropertyConverter<ArrayList, Set> converter,
			Class<ArrayList> class1) {
		super(wrappedField, converter, class1);
		setCompositionRoot(wrappedField);
		
	}
	
	
	
}
